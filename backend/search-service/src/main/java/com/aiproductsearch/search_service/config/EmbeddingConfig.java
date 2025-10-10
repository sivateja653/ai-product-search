package com.aiproductsearch.search_service.config;

import com.aiproductsearch.search_service.ai.EmbeddingPros;
import com.aiproductsearch.search_service.ai.EmbeddingService;
import com.aiproductsearch.search_service.service.CachingEmbeddingService;
import com.aiproductsearch.search_service.service.DevEmbeddingService;
import com.aiproductsearch.search_service.service.OpenAIEmbeddingService;
import com.aiproductsearch.search_service.service.RateLimitedEmbeddingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class EmbeddingConfig {

  @Bean(name = "provider")
  @Profile("prod")
  public EmbeddingService prodProvider(OpenAIEmbeddingService svc) {
    return svc;
  }

  @Bean(name = "provider")
  @Profile("dev")
  public EmbeddingService devProvider(DevEmbeddingService svc) {
    return svc;
  }

  @Bean(name = "rateLimited")
  @Profile({"prod", "dev"})
  public EmbeddingService rateLimited(
      @Qualifier("provider") EmbeddingService provider, EmbeddingPros props)
      throws IllegalAccessException {
    return new RateLimitedEmbeddingService(provider, props);
  }

  @Bean
  @Primary
  @Profile({"prod", "dev"})
  public EmbeddingService caching(
      @Qualifier("rateLimited") EmbeddingService rateLimited, EmbeddingPros props) {

    return new CachingEmbeddingService(rateLimited, props);
  }
}
