package com.aiproductsearch.search_service.index;

import static com.aiproductsearch.search_service.util.VectorUtils.toVectorLiteral;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import com.aiproductsearch.search_service.model.Product;
import com.aiproductsearch.search_service.repository.ProductRepository;
import com.aiproductsearch.search_service.util.Hashing;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingIndexer {

  private final ProductRepository productrepo;
  private final EmbeddingService embedding;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final JdbcTemplate jdbc;

  public void index(UUID productId) {

    try {
      Optional<Product> opt = productrepo.findById(productId);
      if (opt.isEmpty()) {
        log.info("index: {} not found", productId);
        return;
      }

      Product p = opt.get();

      String text = p.getSearchableText();
      String newHash = Hashing.sha256Hex(text == null ? "" : text);

      if (newHash.equals(p.getSearchableHash()) && Boolean.TRUE.equals(p.getEmbeddingReady())) {
        log.info("index: {} up-to-date, skip", productId);
        return;
      }

      if (text == null || text.isBlank()) {
        jdbc.update(
            "UPDATE products SET embedding = NULL, searchable_hash = ?, embedding_ready = TRUE WHERE id = ?",
            ps -> {
              ps.setString(1, newHash);
              ps.setObject(2, productId);
            });
        log.info("index: {} blank -> cleared embedding", productId);
        return;
      }

      var vec = embedding.embed(text);
      var literal = toVectorLiteral(vec);

      jdbc.update(
          "UPDATE products SET embedding = CAST(? AS vector), searchable_hash = ?, embedding_ready = TRUE WHERE id = ?",
          ps -> {
            ps.setString(1, literal);
            ps.setString(2, newHash);
            ps.setObject(3, productId);
          });

      log.info("index: {} updated (dims={})", productId, vec.size());

    } catch (Exception e) {
      throw new RuntimeException("index failed for " + productId, e);
    }
  }
}
