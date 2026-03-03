package com.ecommerce.order.bdd.steps;

// BC References: BC-013..025, BC-014, BC-015, BC-016, BC-026..028, BC-056

import com.ecommerce.order.client.CustomerSummaryClientDto;
import com.ecommerce.order.client.UserServiceClient;
import com.ecommerce.order.dto.CreateOrderHistoryRequest;
import com.ecommerce.order.repository.OrderHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@CucumberContextConfiguration
public class OrderHistoryStepDefinitions {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean OrderHistoryRepository orderHistoryRepository;
    @MockBean UserServiceClient userServiceClient;

    private MvcResult lastResult;
    private CreateOrderHistoryRequest currentRequest;

    @Before
    public void resetState() {
        reset(orderHistoryRepository, userServiceClient);
        when(orderHistoryRepository.save(any())).thenAnswer(inv -> {
            var e = inv.getArgument(0);
            ReflectionTestUtils.setField(e, "id", 1L);
            return e;
        });
        when(orderHistoryRepository.findByCustomerIdWithExternalFilter(anyLong(), anyBoolean()))
                .thenReturn(List.of());
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("customer {int} with number {string} exists in user-service")
    public void customerExistsInUserService(int id, String customerNumber) {
        CustomerSummaryClientDto cust = new CustomerSummaryClientDto();
        cust.setId((long) id); cust.setCustomerNumber(customerNumber);
        cust.setFirstName("Test"); cust.setLastName("Customer");
        when(userServiceClient.getCustomerSummary((long) id))
                .thenReturn(cust);
    }

    @Given("an order history request for order {string} with maintainScreen false")
    public void orderHistoryRequestMaintainFalse(String orderId) {
        currentRequest = buildRequest(orderId, false, false, null, null);
    }

    @Given("an order history request for order {string} with maintainScreen true")
    public void orderHistoryRequestMaintainTrue(String orderId) {
        currentRequest = buildRequest(orderId, true, false, null, null);
    }

    @Given("an order history request for order {string} in web service mode")
    public void orderHistoryRequestWebService(String orderId) {
        currentRequest = buildRequest(orderId, false, true, null, null);
    }

    @Given("a work queue request with typeId {int} and reasonId {int} for order {string}")
    public void workQueueRequestWithTypeAndReason(int typeId, int reasonId, String orderId) {
        currentRequest = buildRequest(orderId, false, false, typeId, reasonId);
    }

    @Given("a work queue request with no type id for order {string}")
    public void workQueueRequestWithoutType(String orderId) {
        currentRequest = buildRequest(orderId, false, false, null, 101);
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("the order history entry is submitted")
    public void theOrderHistoryEntryIsSubmitted() throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/order-history")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(currentRequest)))
                .andReturn();
    }

    @When("the work queue entry is submitted")
    public void theWorkQueueEntryIsSubmitted() throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/order-history/work-queue")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(currentRequest)))
                .andReturn();
    }

    @When("the client lists order history for customer {int}")
    public void theClientListsOrderHistory(int customerId) throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/order-history/customer/" + customerId)
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andReturn();
    }

    @When("the client requests work queue types reference data")
    public void theClientRequestsWorkQueueTypes() throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/reference-data/work-queue-types")).andReturn();
    }

    @When("the client requests work queue reasons reference data")
    public void theClientRequestsWorkQueueReasons() throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/reference-data/work-queue-reasons")).andReturn();
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the navigation hint is {string}")
    public void theNavigationHintIs(String expectedHint) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(root.path("data").path("navigationHint").asText()).isEqualTo(expectedHint);
    }

    @Then("the error code is {string}")
    public void theErrorCodeIs(String code) throws Exception {
        assertThat(lastResult.getResponse().getContentAsString()).contains(code);
    }

    @Then("external orders are excluded")
    public void externalOrdersAreExcluded() throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(root.path("data").path("includeExternal").asBoolean()).isFalse();
    }

    @Then("{int} work queue types are returned")
    public void workQueueTypesCountIs(int count) throws Exception {
        JsonNode root = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(root.path("data").size()).isEqualTo(count);
    }

    @Then("reason id {int} has name {string}")
    public void reasonIdHasName(int id, String name) throws Exception {
        JsonNode reasons = objectMapper.readTree(lastResult.getResponse().getContentAsString()).path("data");
        boolean found = false;
        for (JsonNode r : reasons) {
            if (r.path("id").asInt() == id && name.equals(r.path("name").asText())) {
                found = true; break;
            }
        }
        assertThat(found).as("Expected reason id=%d name=%s", id, name).isTrue();
    }

    @Then("reason id {int} does not exist")
    public void reasonIdDoesNotExist(int id) throws Exception {
        JsonNode reasons = objectMapper.readTree(lastResult.getResponse().getContentAsString()).path("data");
        for (JsonNode r : reasons) {
            assertThat(r.path("id").asInt()).isNotEqualTo(id);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private CreateOrderHistoryRequest buildRequest(String orderId, boolean maintainScreen,
                                                   boolean webService, Integer wqTypeId, Integer wqReasonId) {
        CreateOrderHistoryRequest req = new CreateOrderHistoryRequest();
        req.setCustomerId(10L); req.setCustomerNumber("CUST-001");
        req.setOrderEntryTypeId(1); req.setOrderIdentifierTypeId(1);
        req.setOrderId(orderId);
        req.setMaintainScreen(maintainScreen); req.setWebService(webService);
        req.setWorkQueueTypeId(wqTypeId); req.setWorkQueueReasonId(wqReasonId);
        return req;
    }
}
