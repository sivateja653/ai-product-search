package com.aiproductsearch.search_service.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record EmbeddingPros(String provider, OpenAI openai, Embedtuning embed) {
  public record OpenAI(
      String baseUrl,
      String model,
      Integer timeoutMs,
      Integer maxRetries,
      Integer retryBackoffMs,
      String apiKey) {}

  public record Embedtuning(
      Integer maxConcurrency, long acquireTimeoutMs, Integer cacheSize, long ttlMs) {}
}
