package com.aiproductsearch.search_service.service;

import static com.aiproductsearch.search_service.util.Hashing.sha256Hex;
import static com.aiproductsearch.search_service.util.VectorUtils.toVectorLiteral;

import com.aiproductsearch.search_service.ai.EmbeddingService;
import com.aiproductsearch.search_service.dto.CreateProductRequest;
import com.aiproductsearch.search_service.dto.ProductHit;
import com.aiproductsearch.search_service.dto.UpdateProductRequest;
import com.aiproductsearch.search_service.exception.NotFoundException;
import com.aiproductsearch.search_service.index.EmbeddingTaskQueue;
import com.aiproductsearch.search_service.model.Product;
import com.aiproductsearch.search_service.repository.ProductRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductService {

  private static final int MAX_LIMIT = 50;
  private static final int DIM = 1536;

  private final ProductRepository productrepo;
  private final EmbeddingService embeddings;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed singleton dependency; safe to retain reference")
  private final JdbcTemplate jdbc;

  private final EmbeddingTaskQueue queue;

  public ProductService(
      ProductRepository repo,
      JdbcTemplate jdbc,
      EmbeddingService embeddings,
      EmbeddingTaskQueue queue) {
    this.productrepo = repo;
    this.embeddings = embeddings;
    this.jdbc = jdbc;
    this.queue = queue;
  }

  public List<Product> list() {
    return productrepo.findAll();
  }

  public Product get(UUID id) {
    return productrepo
        .findById(id)
        .orElseThrow(() -> new NotFoundException("product", id.toString()));
  }

  private static String buildSearchable(String t, String d, String c, String b) {
    return String.join(" ", ns(t), ns(d), ns(c), ns(b)).trim();
  }

  private static String ns(String s) {
    return s == null ? "" : s;
  }

  // Use this method for local dev and test
  private void persistEmbedding(Product p) {
    var vec = embeddings.embed(p.getSearchableText());
    if (vec == null || vec.size() != DIM) {
      throw new IllegalStateException(
          "Embedding dimension mismatch: " + (vec == null ? 0 : vec.size()));
    }
    String vectorLiteral = toVectorLiteral(vec); // "[x1,x2,...,x1536]"
    int rows =
        jdbc.update(
            "UPDATE products SET embedding = ?::vector WHERE id = ?",
            ps -> {
              ps.setString(1, vectorLiteral); // pgvector text literal
              ps.setObject(2, p.getId()); // UUID
            });
    if (rows != 1) {
      throw new IllegalStateException("Failed to set embedding for product " + p.getId());
    }
  }

  private void persistEmbeddingIfChanged(Product p) {
    String content = p.getSearchableText();
    String newHash = sha256Hex(content == null ? "" : content);

    if (newHash.equals(p.getSearchableHash())) {
      log.info("Embedding skipped for {} (hash unchanged)", p.getId());
      return;
    }

    if (content == null || content.isBlank()) {
      jdbc.update(
          "UPDATE products SET embedding = NULL, searchable_hash = ? WHERE id = ?;",
          ps -> {
            ps.setString(2, newHash);
            ps.setObject(3, p.getId());
          });

      return;
    }

    var vec = embeddings.embed(content);
    var literal = toVectorLiteral(vec);

    int rows =
        jdbc.update(
            "UPDATE products SET embedding=CAST(? AS vector), searchable_hash = ? WHERE id = ?;",
            ps -> {
              ps.setString(1, literal);
              ps.setString(2, newHash);
              ps.setObject(3, p.getId());
            });

    if (rows != 1) {
      throw new IllegalStateException("Failed to update embedding for product " + p.getId());
    }

    p.setSearchableHash(newHash);
    return;
  }

  @Transactional
  public Product create(CreateProductRequest req) {

    Product p =
        Product.builder()
            .id(UUID.randomUUID())
            .title(req.title())
            .description(req.description())
            .category(req.category())
            .brand(req.brand())
            .price(req.price())
            .rating(req.rating())
            .searchableText(
                buildSearchable(req.title(), req.description(), req.category(), req.brand()))
            .build();

    Product saved = productrepo.saveAndFlush(p);
    queue.submit(saved.getId());
    // persistEmbeddingIfChanged(p);
    return saved;
  }

  @Transactional
  public Product update(UUID id, UpdateProductRequest req) {
    Product p = get(id);

    p.setTitle(req.title());
    p.setDescription(req.description());
    p.setCategory(req.category());
    p.setBrand(req.brand());
    p.setPrice(req.price());
    p.setRating(req.rating());
    p.setSearchableText(
        buildSearchable(req.title(), req.description(), req.category(), req.brand()));
    p.setEmbeddingReady(false);
    Product saved = productrepo.saveAndFlush(p);
    queue.submit(saved.getId());
    // persistEmbeddingIfChanged(p);
    return saved;
  }

  public List<Product> search(String q, int limit) {

    int topK = Math.max(1, Math.min(limit, MAX_LIMIT));
    var vec = embeddings.embed(q);
    if (vec == null || vec.size() != DIM) {
      throw new IllegalStateException(
          "Embedding dimension mismatch: " + (vec == null ? 0 : vec.size()));
    }
    String vectorLiteral = toVectorLiteral(vec); // "[x1,x2,...,x1536]"

    return productrepo.semanticSearch(vectorLiteral, topK);
  }

  public List<ProductHit> searchscore(String q, int limit) {
    int topK = Math.max(1, Math.min(limit, MAX_LIMIT));
    var vec = embeddings.embed(q);
    if (vec == null || vec.size() != DIM) {
      throw new IllegalStateException(
          "Embedding dimension mismatch: " + (vec == null ? 0 : vec.size()));
    }
    String vectorLiteral = toVectorLiteral(vec); // "[x1,x2,...,x1536]"

    return productrepo.semanticSearchWithScore(vectorLiteral, topK);
  }
}
