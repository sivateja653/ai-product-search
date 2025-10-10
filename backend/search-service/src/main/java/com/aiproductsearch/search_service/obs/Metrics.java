package com.aiproductsearch.search_service.obs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.*;
import java.util.Map;
import java.util.concurrent.Callable;
import org.springframework.stereotype.Component;

@Component
public class Metrics {

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final MeterRegistry reg;

  public Metrics(MeterRegistry reg) {
    this.reg = reg;
  }

  public <T> T time(String name, Map<String, String> tags, Callable<T> fn) {
    Timer.Sample sample = Timer.start(reg); // 7c-10: start timer
    boolean success = false; // 7c-11: track outcome
    try {
      T out = fn.call(); // 7c-12: run code
      success = true; // 7c-13: mark success
      return out; // 7c-14
    } catch (Exception e) {
      throw new RuntimeException(e); // 7c-15: bubble up
    } finally {
      // 7c-16: register a timer with tags + success, then stop the sample
      var builder = Timer.builder(name);
      if (tags != null) tags.forEach(builder::tag);
      builder.tag("success", String.valueOf(success));
      Timer timer = builder.register(reg);
      sample.stop(timer);
      // 7c-17: also increment a counter
      Counter.builder(name + ".count").tags(timer.getId().getTags()).register(reg).increment();
    }
  }
}
