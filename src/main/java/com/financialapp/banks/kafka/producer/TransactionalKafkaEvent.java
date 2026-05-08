package com.financialapp.banks.kafka.producer;

public record TransactionalKafkaEvent(String topic, String key, Object payload) {}
