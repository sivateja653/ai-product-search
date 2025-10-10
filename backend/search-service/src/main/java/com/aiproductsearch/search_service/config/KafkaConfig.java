package com.aiproductsearch.search_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

@Configuration
@Profile("enterprise")
public class KafkaConfig {

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> embeddingKafkaFactory(
      ConsumerFactory<String, String> consumerFactory) {

    var f = new ConcurrentKafkaListenerContainerFactory<String, String>();
    f.setConsumerFactory(consumerFactory);
    f.getContainerProperties().setAckMode(AckMode.MANUAL);
    f.setConcurrency(3);
    return f;
  }
}
