package com.financialapp.banks.domain.common.model;

import java.time.LocalDateTime;

public abstract class Payment {

    Installment installment;
    LocalDateTime paymentDate;
    
}
