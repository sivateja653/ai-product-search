package com.aiproductsearch.search_service.index;

import com.aiproductsearch.search_service.ai.EmbeddingPros;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"prod", "dev"})
public class InMemoryEmbeddingQueue implements EmbeddingTaskQueue {

  private final EmbeddingIndexer indexer;
  private final ExecutorService pool;
  private final ScheduledExecutorService scheduler;

  private final int maxRetries;
  private final Duration initialBackoff = Duration.ofSeconds(2);

  public InMemoryEmbeddingQueue(EmbeddingIndexer indexer, EmbeddingPros props) {
    this.indexer = indexer;
    this.maxRetries = props.openai().maxRetries();
    this.pool =
        Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
    this.scheduler = Executors.newScheduledThreadPool(1);
  }

  @Override
  public void submit(UUID productId) {
    dispatch(productId, 0);
  }

  private void dispatch(UUID productId, int attempt) {
    pool.execute(
        () -> {
          try {
            indexer.index(productId);
          } catch (Exception e) {
            if (attempt < maxRetries) {
              long delayMs = initialBackoff.multipliedBy(1L << attempt).toMillis();
              log.warn(
                  "queue: {} attempt {}/{} failed -> retrying in {} ms: {}",
                  productId,
                  attempt + 1,
                  maxRetries,
                  delayMs,
                  e.getMessage());
              scheduler.schedule(
                  () -> dispatch(productId, attempt + 1), delayMs, TimeUnit.MILLISECONDS);
            } else {
              log.error("queue: giving up on {} after {} attempts", productId, maxRetries, e);
            }
          }
        });
  }
}
