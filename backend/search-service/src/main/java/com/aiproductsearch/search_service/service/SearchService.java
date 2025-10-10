package com.aiproductsearch.search_service.service;

import static com.aiproductsearch.search_service.util.VectorUtils.toVectorLiteral;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import com.aiproductsearch.search_service.dto.ProductHit;
import com.aiproductsearch.search_service.dto.SearchResponse;
import com.aiproductsearch.search_service.obs.Metrics;
import com.aiproductsearch.search_service.repository.ProductRepository;
import io.micrometer.core.annotation.Timed;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SearchService {

  private static final int MAX_SIZE = 50;

  private final EmbeddingService embeddings;
  private final ProductRepository productrepo;
  private final Metrics metrics;

  public SearchService(
      EmbeddingService embeddings, ProductRepository productrepo, Metrics metrics) {
    this.embeddings = embeddings;
    this.productrepo = productrepo;
    this.metrics = metrics;
  }

  @Timed(value = "search.semantic", histogram = true)
  public SearchResponse<ProductHit> search(
      String q,
      Integer page,
      Integer size,
      String category,
      String brand,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      BigDecimal minRating) {
    int p = page == null || page < 0 ? 0 : page;
    int s =
        size == null
            ? 10
            : Math.min(Math.max(size, 1), MAX_SIZE); // size minimum 10 maximum is MAX_SIZE if null;

    int offset = p * s;

    long t0 = System.currentTimeMillis();
    var vec = embeddings.embed(q);
    var literal = toVectorLiteral(vec);

    var items =
        productrepo.semanticSearchWithFilters(
            literal,
            s,
            offset,
            blankToNull(category),
            blankToNull(brand),
            minPrice,
            maxPrice,
            minRating);
    long ms = System.currentTimeMillis() - t0;

    log.info(
        "vector.search q='{}' page={} size={} filters=[cat={}, brand={}, minPrice={}, maxPrice={}, minRating={}] took={}ms items={}",
        q,
        p,
        s,
        category,
        brand,
        minPrice,
        maxPrice,
        minRating,
        ms,
        items.size());

    long total = -1;
    return new SearchResponse<>(items, p, s, total);
  }

  @Timed(value = "search.hybrid", histogram = true)
  public SearchResponse<ProductHit> hybridSearch(
      String q,
      Integer page,
      Integer size,
      String category,
      String brand,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      BigDecimal minRating,
      Double wVec, // 0..1 (nullable)
      Double wText, // 0..1 (nullable)
      Boolean forceText // nullable; default false
      ) {
    int p = (page == null || page < 0) ? 0 : page;
    int s = (size == null) ? 10 : Math.min(Math.max(size, 1), MAX_SIZE);
    int offset = p * s;

    final double vecWInit = (wVec == null) ? 0.7 : clamp(wVec, 0.0, 1.0);
    final double txtWInit = (wText == null) ? (1.0 - vecWInit) : clamp(wText, 0.0, 1.0);
    double sum = vecWInit + txtWInit;
    final double vecW, txtW;
    if (sum == 0) {
      vecW = 0.7;
      txtW = 0.3;
    } else if (Math.abs(sum - 1.0) > 1e-6) {
      vecW = vecWInit / sum;
      txtW = txtWInit / sum;
    } else {
      vecW = vecWInit;
      txtW = txtWInit;
    }
    boolean requireText = (forceText != null) && forceText;

    long t0 = System.currentTimeMillis();
    var vec = embeddings.embed(q);
    var literal = toVectorLiteral(vec);

    BigDecimal minVecSim = new BigDecimal("0.2");
    System.out.println("All here");
    // List<ProductHit> items = productrepo.hybridSearchWithFilters(literal, q, vecW, txtW, s,
    // offset, blankToNull(category), blankToNull(brand), minPrice, maxPrice, minRating,
    // requireText);
    List<ProductHit> items =
        metrics.time(
            "search.query", // 7c-28
            Map.of("mode", "hybrid"), // 7c-29
            () ->
                productrepo.hybridSearchWithFilters( // 7c-30
                    literal,
                    q,
                    vecW,
                    txtW,
                    s,
                    offset,
                    blankToNull(category),
                    blankToNull(brand),
                    minPrice,
                    maxPrice,
                    minRating,
                    requireText,
                    minVecSim));

    long total =
        metrics.time(
            "search.count", // 7c-31
            Map.of("mode", "hybrid"),
            () ->
                productrepo.hybridSearchCount(
                    literal,
                    q,
                    blankToNull(category),
                    blankToNull(brand),
                    minPrice,
                    maxPrice,
                    minRating,
                    requireText,
                    minVecSim));

    long ms = System.currentTimeMillis() - t0;

    log.info("search.hybrid q='{}' page={} size={} took={}ms items={}", q, p, s, ms, items.size());

    return new SearchResponse<>(items, p, s, total);
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
