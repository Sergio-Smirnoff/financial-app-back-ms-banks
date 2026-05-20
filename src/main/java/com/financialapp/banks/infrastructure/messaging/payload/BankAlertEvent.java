package com.financialapp.banks.infrastructure.messaging.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAlertEvent {
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String metadata;
}
