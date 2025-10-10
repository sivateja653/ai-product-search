package com.aiproductsearch.search_service.service;

import com.aiproductsearch.search_service.ai.EmbeddingPros;
import com.aiproductsearch.search_service.ai.EmbeddingService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "prod"})
public class CachingEmbeddingService implements EmbeddingService {

  private final EmbeddingService delegate;
  private final long ttlMs;

  record CacheItem(List<Float> v, long at) {}

  private final Map<String, CacheItem> cache;

  public CachingEmbeddingService(EmbeddingService delegate, EmbeddingPros props) {
    this.delegate = delegate;
    this.ttlMs = Math.max(0, props.embed().ttlMs());
    this.cache =
        new LinkedHashMap<String, CacheItem>(Math.max(16, props.embed().cacheSize()), 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(Map.Entry<String, CacheItem> e) {
            return size() > props.embed().cacheSize();
          }
        };
  }

  @Override
  public List<Float> embed(String text) {

    final String key = text == null ? "" : text;
    final long now = System.currentTimeMillis();

    CacheItem e;

    synchronized (this) {
      e = cache.get(key);
      if (e != null && (ttlMs == 0 || now - e.at <= ttlMs)) {
        return e.v;
      }
    }

    List<Float> fresh = delegate.embed(key);

    synchronized (this) {
      cache.put(key, new CacheItem(fresh, now));
    }

    return fresh;
  }
}
