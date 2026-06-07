package com.financialapp.banks.infrastructure.persistence.entity;

import com.financialapp.commons.messaging.infrastructure.persistence.entity.ProcessedEventEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inbound_events", schema = "banks")
@Getter
@Setter
public class InboundEventEntity extends ProcessedEventEntity {

    @Id
    private String eventId;
}
