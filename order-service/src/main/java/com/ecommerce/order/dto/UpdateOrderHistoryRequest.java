package com.ecommerce.order.dto;

// BC References: BC-018

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateOrderHistoryRequest {
    @NotNull @Min(1) @Max(5) private Integer orderEntryTypeId;
    @NotNull @Min(1) @Max(3) private Integer orderIdentifierTypeId;
    @NotBlank @Size(max = 100) private String orderId;
    private LocalDate orderDate;
    @Size(max = 2000) private String notes;
    private boolean external;
    private Integer workQueueTypeId;
    private Integer workQueueReasonId;
    @Size(max = 2000) private String workQueueNotes;
    private boolean maintainScreen;
    private boolean webService;
}
