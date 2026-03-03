package com.ecommerce.order.dto;

// BC References: BC-014 (maintainScreen), BC-016 (workQueue), BC-056 (webService)

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateOrderHistoryRequest {
    @NotNull private Long customerId;
    @NotBlank @Size(max = 50) private String customerNumber;
    @NotNull @Min(1) @Max(5) private Integer orderEntryTypeId;
    @NotNull @Min(1) @Max(3) private Integer orderIdentifierTypeId;
    @NotBlank @Size(max = 100) private String orderId;
    private LocalDate orderDate;
    @Size(max = 2000) private String notes;
    private boolean external;
    // BC-016/017: work queue
    private Integer workQueueTypeId;
    private Integer workQueueReasonId;
    @Size(max = 2000) private String workQueueNotes;
    // BC-014/015: navigation after save
    private boolean maintainScreen;
    // BC-056: web service mode — skips navigation hint
    private boolean webService;
}
