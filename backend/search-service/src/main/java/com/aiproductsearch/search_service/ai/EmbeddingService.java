package com.aiproductsearch.search_service.ai;

import java.util.List;

public interface EmbeddingService {
  List<Float> embed(String text);
}
