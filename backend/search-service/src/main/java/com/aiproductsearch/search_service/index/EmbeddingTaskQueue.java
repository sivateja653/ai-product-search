package com.aiproductsearch.search_service.index;

import java.util.UUID;

public interface EmbeddingTaskQueue {

  void submit(UUID productId);
}
