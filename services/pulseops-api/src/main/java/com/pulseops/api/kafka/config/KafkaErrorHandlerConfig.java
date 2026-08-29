package com.pulseops.api.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    /*
     * A bad record should not block the entire partition forever.
     *
     * We retry once, then skip the record and continue with the next
     * Kafka offset. Later, we can replace this with a dead-letter topic.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        FixedBackOff backOff =
                new FixedBackOff(1000L, 1L);

        DefaultErrorHandler handler =
                new DefaultErrorHandler(backOff);

        handler.setCommitRecovered(true);

        return handler;
    }
}