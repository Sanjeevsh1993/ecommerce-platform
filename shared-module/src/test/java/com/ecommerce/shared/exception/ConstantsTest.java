package com.ecommerce.shared.exception;

// BC References: BC-005 (CustomerType B2C/B2B rules), BC-030 (OrderIdentifierType)

import com.ecommerce.shared.constants.CustomerType;
import com.ecommerce.shared.constants.OrderIdentifierType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConstantsTest {

    // ── CustomerType ──────────────────────────────────────────────────────────

    @Test
    void customerType_b2c_hasCorrectAttributes() {
        CustomerType b2c = CustomerType.B2C;
        assertThat(b2c.getId()).isEqualTo(1);
        assertThat(b2c.getShortDescription()).isEqualTo("B2C");
        assertThat(b2c.getLongDescription()).isEqualTo("Business to Consumer");
    }

    @Test
    void customerType_b2b_hasCorrectAttributes() {
        CustomerType b2b = CustomerType.B2B;
        assertThat(b2b.getId()).isEqualTo(2);
        assertThat(b2b.getShortDescription()).isEqualTo("B2B");
        assertThat(b2b.getLongDescription()).isEqualTo("Business to Business");
    }

    @Test
    void customerType_reseller_hasId3() {
        assertThat(CustomerType.RESELLER.getId()).isEqualTo(3);
    }

    @Test
    void customerType_wholesale_hasId4() {
        assertThat(CustomerType.WHOLESALE.getId()).isEqualTo(4);
    }

    // BC-005: Wishlist only visible to B2C
    @Test
    void customerType_b2c_isWishlistVisible() {
        assertThat(CustomerType.B2C.isWishlistVisible()).isTrue();
        assertThat(CustomerType.B2B.isWishlistVisible()).isFalse();
        assertThat(CustomerType.RESELLER.isWishlistVisible()).isFalse();
        assertThat(CustomerType.WHOLESALE.isWishlistVisible()).isFalse();
    }

    // BC-005: Business account only visible to non-B2C
    @Test
    void customerType_nonB2c_isBusinessAccountVisible() {
        assertThat(CustomerType.B2C.isBusinessAccountVisible()).isFalse();
        assertThat(CustomerType.B2B.isBusinessAccountVisible()).isTrue();
        assertThat(CustomerType.RESELLER.isBusinessAccountVisible()).isTrue();
        assertThat(CustomerType.WHOLESALE.isBusinessAccountVisible()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"1,B2C", "2,B2B", "3,RESELLER", "4,WHOLESALE"})
    void customerType_fromId_returnsCorrectType(int id, String expectedName) {
        assertThat(CustomerType.fromId(id).name()).isEqualTo(expectedName);
    }

    @Test
    void customerType_fromId_unknownId_throwsException() {
        assertThatThrownBy(() -> CustomerType.fromId(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void customerType_fromLongDescription_returnsCorrectType() {
        assertThat(CustomerType.fromLongDescription("Business to Consumer"))
                .isEqualTo(CustomerType.B2C);
        assertThat(CustomerType.fromLongDescription("business to business"))
                .isEqualTo(CustomerType.B2B);
    }

    // ── OrderIdentifierType ───────────────────────────────────────────────────

    @Test
    void orderIdentifierType_customer_hasId1() {
        assertThat(OrderIdentifierType.CUSTOMER.getId()).isEqualTo(1);
    }

    @Test
    void orderIdentifierType_product_hasId2() {
        assertThat(OrderIdentifierType.PRODUCT.getId()).isEqualTo(2);
    }

    @Test
    void orderIdentifierType_order_hasId3() {
        assertThat(OrderIdentifierType.ORDER.getId()).isEqualTo(3);
    }

    @ParameterizedTest
    @CsvSource({"1,CUSTOMER", "2,PRODUCT", "3,ORDER"})
    void orderIdentifierType_fromId_returnsCorrectType(int id, String expectedName) {
        assertThat(OrderIdentifierType.fromId(id).name()).isEqualTo(expectedName);
    }

    @Test
    void orderIdentifierType_fromId_unknownId_throwsException() {
        assertThatThrownBy(() -> OrderIdentifierType.fromId(99))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
