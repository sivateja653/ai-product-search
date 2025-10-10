package com.aiproductsearch.search_service.obs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class QueueMetrics {

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final MeterRegistry reg;

  public QueueMetrics(MeterRegistry reg) {
    this.reg = reg;
  }

  public void incSent() {
    reg.counter("embedding.queue.sent").increment();
  }

  public void incProcessed() {
    reg.counter("embedding.queue.processed").increment();
  }

  public void incDlt() {
    reg.counter("embedding.queue.dlt").increment();
  }

  public <T> T timedIndexing(java.util.concurrent.Callable<T> c) {
    Timer t = reg.timer("embedding.indexer.duration");
    try {
      return t.recordCallable(c);
    } catch (Exception e) {
      reg.counter("embedding.indexer.errors").increment();
      throw new RuntimeException(e);
    }
  }

  public void timedIndexing(Runnable r) {
    Timer t = reg.timer("embedding.indexer.duration");
    t.record(r);
  }
}
