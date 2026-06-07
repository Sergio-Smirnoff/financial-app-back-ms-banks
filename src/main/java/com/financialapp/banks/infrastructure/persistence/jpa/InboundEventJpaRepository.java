package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.InboundEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundEventJpaRepository extends JpaRepository<InboundEventEntity, String> {
}
