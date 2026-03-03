package com.ecommerce.catalog.mapper;

// BC References: BC-031, BC-032

import com.ecommerce.catalog.dto.CatalogItemDto;
import com.ecommerce.catalog.dto.CreateCatalogItemRequest;
import com.ecommerce.catalog.entity.CatalogItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CatalogItemMapper {

    @Mapping(target = "createdAt", expression = "java(item.getCreatedAt() != null ? item.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(item.getUpdatedAt() != null ? item.getUpdatedAt().toString() : null)")
    CatalogItemDto toDto(CatalogItem item);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CatalogItem toEntity(CreateCatalogItemRequest request);
}
