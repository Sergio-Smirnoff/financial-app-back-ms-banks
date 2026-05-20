package com.financialapp.banks.infrastructure.messaging.payload;

public record TransactionalKafkaEvent(String topic, String key, Object payload) {}
