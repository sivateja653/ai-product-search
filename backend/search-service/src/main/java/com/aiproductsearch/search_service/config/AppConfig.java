package com.aiproductsearch.search_service.config;

import com.aiproductsearch.search_service.ai.EmbeddingPros;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbeddingPros.class)
public class AppConfig {}
