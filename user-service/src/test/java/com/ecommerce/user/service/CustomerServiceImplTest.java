package com.ecommerce.user.service;

// BC References: BC-001..012

import com.ecommerce.shared.constants.CustomerType;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.Customer;
import com.ecommerce.user.mapper.CustomerMapper;
import com.ecommerce.user.repository.CustomerRepository;
import com.ecommerce.user.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock CustomerRepository customerRepository;
    @Mock CustomerMapper customerMapper;
    @InjectMocks CustomerServiceImpl service;

    private Customer sampleCustomer() {
        Customer c = Customer.builder()
                .customerNumber("CUST-001")
                .firstName("Jane").lastName("Doe")
                .email("jane@example.com")
                .customerType(CustomerType.B2C)
                .prospect(false)
                .build();
        ReflectionTestUtils.setField(c, "id", 1L);
        return c;
    }

    // BC-001
    @Test
    void getCustomerSummary_found_returnsDto() {
        Customer c = sampleCustomer();
        CustomerSummaryDto dto = CustomerSummaryDto.builder()
                .id(1L).customerNumber("CUST-001").wishlistVisible(true).build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(customerMapper.toSummaryDto(c)).thenReturn(dto);

        CustomerSummaryDto result = service.getCustomerSummary(1L);

        assertThat(result.getCustomerNumber()).isEqualTo("CUST-001");
        assertThat(result.isWishlistVisible()).isTrue();    // BC-005: B2C → wishlist visible
    }

    @Test
    void getCustomerSummary_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getCustomerSummary(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // BC-003
    @Test
    void createCustomer_newEmail_savesAndReturnsDto() {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setFirstName("John"); req.setLastName("Smith");
        req.setEmail("john@example.com"); req.setCustomerType("B2B");

        Customer entity = sampleCustomer();
        CustomerDto dto = CustomerDto.builder().email("john@example.com").build();

        when(customerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(customerMapper.toEntity(req)).thenReturn(entity);
        when(customerRepository.save(any())).thenReturn(entity);
        when(customerMapper.toDto(entity)).thenReturn(dto);

        CustomerDto result = service.createCustomer(req);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateEmail_throwsDuplicateResourceException() {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setEmail("dup@example.com");
        when(customerRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createCustomer(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // BC-002
    @Test
    void listCustomers_withQuery_searchesByName() {
        Customer c = sampleCustomer();
        Page<Customer> page = new PageImpl<>(List.of(c));
        when(customerRepository.search(eq("Jane"), any(Pageable.class))).thenReturn(page);
        when(customerMapper.toDto(c)).thenReturn(CustomerDto.builder().firstName("Jane").build());

        Page<CustomerDto> result = service.listCustomers("Jane", Pageable.unpaged());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Jane");
    }

    // BC-005
    @Test
    void b2cCustomer_summaryDto_hasWishlistVisibleTrue() {
        Customer c = sampleCustomer(); // B2C
        CustomerSummaryDto dto = CustomerSummaryDto.builder()
                .wishlistVisible(true).businessAccountVisible(false).build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(customerMapper.toSummaryDto(c)).thenReturn(dto);

        CustomerSummaryDto result = service.getCustomerSummary(1L);
        assertThat(result.isWishlistVisible()).isTrue();
        assertThat(result.isBusinessAccountVisible()).isFalse();
    }

    // BC-004
    @Test
    void updateCustomer_changesFields() {
        Customer c = sampleCustomer();
        UpdateCustomerRequest req = new UpdateCustomerRequest();
        req.setFirstName("Updated"); req.setLastName("Name");
        req.setEmail("jane@example.com"); // same email — no duplicate check needed
        req.setCustomerType("B2C");
        CustomerDto dto = CustomerDto.builder().firstName("Updated").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));
        when(customerRepository.save(any())).thenReturn(c);
        when(customerMapper.toDto(any())).thenReturn(dto);

        CustomerDto result = service.updateCustomer(1L, req);
        assertThat(result.getFirstName()).isEqualTo("Updated");
    }
}
