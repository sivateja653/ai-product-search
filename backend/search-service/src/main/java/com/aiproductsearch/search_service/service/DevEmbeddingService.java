package com.aiproductsearch.search_service.service;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DevEmbeddingService implements EmbeddingService {
  private static final int DIM = 1536;

  @Override
  public List<Float> embed(String text) {

    byte[] b = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);

    ArrayList<Float> v = new ArrayList<>(DIM);

    long h = 1125899906842597L;

    for (byte x : b) {
      h = 1315423911L + (h ^ ((h << 5) + x + (h >> 2)));
    }

    for (int i = 0; i < DIM; i++) {
      long mixed = (h + 31L * i) ^ (h >>> 13);

      float val = (float) ((mixed % 2000) / 1000.0 - 1.0);
      v.add(val);
    }

    return v;
  }
}
