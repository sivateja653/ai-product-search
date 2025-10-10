package com.aiproductsearch.search_service.index;

import com.aiproductsearch.search_service.obs.QueueMetrics;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("enterprise")
@RequiredArgsConstructor
public class KafkaEmbeddingConsumer {

  private final EmbeddingIndexer indexer;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final KafkaTemplate<String, String> kafka;

  private final Environment env;
  private final QueueMetrics metrics;

  private static final int MAX_ATTEMPTS = 3;

  @KafkaListener(
      topics = "#{environment.getProperty('embedding.topic','embedding-tasks')}",
      containerFactory = "embeddingKafkaFactory")
  public void onMessage(ConsumerRecord<String, String> rec, Acknowledgment ack) {
    String idStr = rec.value();
    int attempt = 1;
    try {
      while (true) {
        try {
          metrics.timedIndexing(() -> indexer.index(UUID.fromString(idStr)));
          metrics.incProcessed();
          ack.acknowledge();
          return;
        } catch (Exception e) {
          if (attempt++ >= MAX_ATTEMPTS) {
            sendToDlt(rec);
            metrics.incDlt();
            ack.acknowledge();
            log.error("DLT after {} attempts for {}", MAX_ATTEMPTS, idStr, e);
            return;
          }
          try {
            Thread.sleep(1000L * (1L << (attempt - 2)));
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("retry {}/{} for {}", attempt - 1, MAX_ATTEMPTS, idStr);
          }
        }
      }
    } catch (Exception fatal) {
      sendToDlt(rec);
      metrics.incDlt();
      ack.acknowledge();
      log.error("fatal, moved to DLT: {}", idStr, fatal);
    }
  }

  private void sendToDlt(ConsumerRecord<String, String> rec) {
    String dlt = env.getProperty("embedding.dlt", "embedding-tasks-dlt");
    kafka.send(dlt, rec.key(), rec.value());
  }
}
