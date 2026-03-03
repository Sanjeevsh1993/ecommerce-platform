package com.ecommerce.user.mapper;

// BC References: BC-001..012

import com.ecommerce.shared.constants.CustomerType;
import com.ecommerce.user.dto.CustomerDto;
import com.ecommerce.user.dto.CustomerSummaryDto;
import com.ecommerce.user.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "customerType", expression = "java(customer.getCustomerType().getShortDescription())")
    @Mapping(target = "wishlistVisible", expression = "java(customer.getCustomerType().isWishlistVisible())")
    @Mapping(target = "businessAccountVisible", expression = "java(customer.getCustomerType().isBusinessAccountVisible())")
    // BC-009: crmUrl only shown for non-B2C and non-prospect
    @Mapping(target = "crmUrl", expression = "java(shouldShowCrmUrl(customer) ? customer.getCrmUrl() : null)")
    CustomerSummaryDto toSummaryDto(Customer customer);

    @Mapping(target = "customerType", expression = "java(customer.getCustomerType().getShortDescription())")
    @Mapping(target = "createdAt", expression = "java(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(customer.getUpdatedAt() != null ? customer.getUpdatedAt().toString() : null)")
    CustomerDto toDto(Customer customer);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "customerType", expression = "java(com.ecommerce.shared.constants.CustomerType.fromLongDescription(request.getCustomerType()) != null ? com.ecommerce.shared.constants.CustomerType.fromLongDescription(request.getCustomerType()) : com.ecommerce.shared.constants.CustomerType.valueOf(request.getCustomerType()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "testCustomerIndicator", ignore = true)
    Customer toEntity(com.ecommerce.user.dto.CreateCustomerRequest request);

    default boolean shouldShowCrmUrl(Customer customer) {
        return customer.getCustomerType() != CustomerType.B2C && !customer.isProspect();
    }
}
