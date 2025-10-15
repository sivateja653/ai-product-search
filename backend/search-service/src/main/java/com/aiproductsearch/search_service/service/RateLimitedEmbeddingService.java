package com.aiproductsearch.search_service.service;

import com.aiproductsearch.search_service.ai.EmbeddingPros;
import com.aiproductsearch.search_service.ai.EmbeddingService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

// prefer this bean when 'prod' profile is active
@Service
@Profile("prod")
public final class RateLimitedEmbeddingService implements EmbeddingService {

  private final EmbeddingService delegate;
  private final Semaphore sem;
  private final long acquireTimeoutMs;

  @SuppressFBWarnings(
      value = "CT_CONSTRUCTOR_THROW",
      justification = "Constructor only validates inputs; Spring manages lifecycle; no finalizer.")
  public RateLimitedEmbeddingService(EmbeddingService delegate, EmbeddingPros props) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    Objects.requireNonNull(props, "props");

    int maxConc = props.embed().maxConcurrency();
    if (maxConc < 1) {
      throw new IllegalArgumentException("maxConcurrency must be >= 1");
    }
    this.sem = new Semaphore(maxConc);

    long timeout = props.embed().acquireTimeoutMs();
    this.acquireTimeoutMs = Math.max(0L, timeout);
  }

  @Override
  public List<Float> embed(String text) {
    boolean acquired = false;
    try {
      if (acquireTimeoutMs == 0L) {
        sem.acquire();
        acquired = true;
      } else {
        acquired = sem.tryAcquire(acquireTimeoutMs, TimeUnit.MILLISECONDS);
        if (!acquired) {
          throw new RuntimeException("Embedding throttle: acquire timeout");
        }
      }
      return delegate.embed(text);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Embedding call interrupted", ie);
    } finally {
      if (acquired) {
        sem.release();
      }
    }
  }
}
