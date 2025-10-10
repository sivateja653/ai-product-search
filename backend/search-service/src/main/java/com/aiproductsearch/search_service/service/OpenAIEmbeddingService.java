package com.aiproductsearch.search_service.service;

import com.aiproductsearch.search_service.ai.EmbeddingPros;
import com.aiproductsearch.search_service.ai.EmbeddingService;
import com.aiproductsearch.search_service.obs.Metrics;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@Service
@Profile("prod")
public class OpenAIEmbeddingService implements EmbeddingService {

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final WebClient client;

  private final EmbeddingPros.OpenAI cfg;
  private final Metrics metrics;
  private final CircuitBreaker cb;

  public OpenAIEmbeddingService(
      @Qualifier("openaiClient") WebClient client,
      EmbeddingPros props,
      Metrics metrics,
      CircuitBreakerRegistry cbRegistry) {
    this.client = client;
    this.cfg = props.openai();
    this.metrics = metrics;
    this.cb = cbRegistry.circuitBreaker("embedding");
  }

  record EmbReq(String model, String input) {}

  record EmbData(List<Double> embedding) {}

  record EmbResp(List<EmbData> data) {}

  @Override
  public List<Float> embed(String text) {
    return CircuitBreaker.decorateSupplier(
            cb,
            () ->
                metrics.time(
                    "embedding.request",
                    Map.of("model", cfg.model() == null ? "unknown" : cfg.model()),
                    () -> doEmbed(text)))
        .get();
  }

  public List<Float> doEmbed(String text) {
    if (text == null) text = "";
    int maxRetries = cfg.maxRetries() == null ? 3 : cfg.maxRetries();
    int backoff = cfg.retryBackoffMs() == null ? 300 : cfg.retryBackoffMs();

    EmbResp resp =
        client
            .post()
            .uri("/embeddings")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.apiKey())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(new EmbReq(cfg.model(), text))
            .retrieve()
            .bodyToMono(EmbResp.class)
            .retryWhen(
                Retry.backoff(maxRetries, Duration.ofMillis(backoff))
                    .filter(err -> isRetryable(err)))
            .block(); // safe in service; bounded by timeouts above

    if (resp == null || resp.data() == null || resp.data().isEmpty())
      throw new IllegalStateException("Empty embedding response");

    List<Double> d = resp.data().get(0).embedding();
    // Optional sanity check: enforce dimension matches DB (1536 if using text-embedding-3-small)
    if (d.size() != 1536) {
      throw new IllegalStateException("Unexpected embedding dimension: " + d.size());
    }
    ArrayList<Float> out = new ArrayList<>(d.size());
    for (Double x : d) out.add(x.floatValue());
    return out;
  }

  private boolean isRetryable(Throwable err) {
    String s = err.getMessage();
    if (s == null) return false;
    s = s.toLowerCase(Locale.ROOT);
    // crude but effective: retry 429 / 5xx / timeouts surfaced by WebClient
    return s.contains("429") || s.contains("5") || s.contains("timeout");
  }
}
