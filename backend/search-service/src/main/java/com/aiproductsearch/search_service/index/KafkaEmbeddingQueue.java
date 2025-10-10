package com.aiproductsearch.search_service.index;

import com.aiproductsearch.search_service.obs.QueueMetrics;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("enterprise")
@Primary
@RequiredArgsConstructor
public class KafkaEmbeddingQueue implements EmbeddingTaskQueue {

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final KafkaTemplate<String, String> kafka;

  private final Environment env;
  private final QueueMetrics metrics;

  @Override
  public void submit(UUID productId) {

    String topic = env.getProperty("embedding.topic", "embedding-tasks");
    String key = productId.toString();
    kafka
        .send(topic, key, key)
        .whenComplete(
            (res, ex) -> {
              if (ex != null) {
                log.error("kafka send failed for {}", key, ex);
              } else {
                metrics.incSent();
                log.debug(
                    "kafka sent {} to {}-{}@{}",
                    key,
                    res.getRecordMetadata().topic(),
                    res.getRecordMetadata().partition(),
                    res.getRecordMetadata().offset());
              }
            });
  }
}
