package com.aiproductsearch.search_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductHit {
  UUID getId();

  String getTitle();

  String getDescription();

  String getCategory();

  String getBrand();

  BigDecimal getPrice(); // NUMERIC -> BigDecimal

  Double getRating(); // cast to float8 in SQL (nullable)

  Double getScore(); // distance (<->) -> float8
}
