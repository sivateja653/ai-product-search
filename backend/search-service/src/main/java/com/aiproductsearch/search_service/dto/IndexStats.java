package com.aiproductsearch.search_service.dto;

public record IndexStats(long total, long ready, long pending) {
  public static IndexStats of(long total, long ready) {
    return new IndexStats(total, ready, Math.max(0, total - ready));
  }
}
