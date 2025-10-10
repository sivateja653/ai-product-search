package com.aiproductsearch.search_service.repository;

import com.aiproductsearch.search_service.dto.ProductHit;
import com.aiproductsearch.search_service.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  /*@Query( nativeQuery = true, value="SELECT category FROM products;")
  List<String> findCategory();*/

  @Query(
      nativeQuery = true,
      value =
          """
			SELECT * FROM PRODUCTS
			WHERE embedding IS NOT NULL
			ORDER BY embedding <-> CAST(?1 AS vector)  -- cosine/L2/IP via index ops
			LIMIT ?2
			""")
  List<Product> semanticSearch(String queryVectorLiteral, int limit);

  @Query(
      value =
          """
			SELECT p.id, p.title, p.description, p.category, p.brand, p.price, p.rating,
			       (p.embedding <-> CAST(?1 AS vector)) AS score
			FROM products p
			WHERE p.embedding IS NOT NULL
			AND p.embedding_ready = TRUE
			ORDER BY score
			LIMIT ?2
			""",
      nativeQuery = true)
  List<ProductHit> semanticSearchWithScore(String literal, int limit);

  @Query(
      value =
          """
		      SELECT
		        p.id                                AS id,
		        p.title                             AS title,
		        p.description                       AS description,
		        p.category                          AS category,
		        p.brand                             AS brand,
		        p.price                             AS price,
		        p.rating::float8                    AS rating,
		        (p.embedding <-> CAST(:qvec AS vector))::float8 AS score
		      FROM products p
		      WHERE p.embedding IS NOT NULL
		      	AND p.embedding_ready = TRUE
		        AND (:category IS NULL OR p.category = :category)
		        AND (:brand    IS NULL OR p.brand    = :brand)
		        AND (:minPrice IS NULL OR p.price   >= :minPrice)
		        AND (:maxPrice IS NULL OR p.price   <= :maxPrice)
		        AND (:minRating IS NULL OR p.rating >= :minRating)
		      ORDER BY score
		      LIMIT :limit OFFSET :offset
		      """,
      nativeQuery = true)
  List<ProductHit> semanticSearchWithFilters(
      @Param("qvec") String queryVectorLiteral,
      @Param("limit") int limit,
      @Param("offset") int offset,
      @Param("category") String category,
      @Param("brand") String brand,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("minRating") BigDecimal minRating);

  @Query(
      value =
          """
		      WITH ranked AS (
		        SELECT
		          p.id,
		          p.title,
		          p.description,
		          p.category,
		          p.brand,
		          p.price,
		          p.rating,
		          -- vec distance d in [0,2]; convert to similarity [0,1]
		          (1.0 - ((p.embedding <-> CAST(:qvec AS vector)) / 2.0))        AS vec_sim,
		          LEAST(ts_rank_cd(p.tsv, plainto_tsquery('english', :q)), 1.0) AS txt_rank

		        FROM products p
		        WHERE p.embedding IS NOT NULL
		          AND p.embedding_ready = TRUE
		          AND (:category IS NULL OR p.category = :category)
		          AND (:brand    IS NULL OR p.brand    = :brand)
		          AND (:minPrice IS NULL OR p.price   >= :minPrice)
		          AND (:maxPrice IS NULL OR p.price   <= :maxPrice)
		          AND (:minRating IS NULL OR p.rating >= :minRating)
		          AND (plainto_tsquery('english', :q) @@ p.tsv OR :forceText = FALSE)
		          AND (1.0 - ((p.embedding <-> CAST(:qvec AS vector)) / 2.0)) >= :minVecSim
		      )
		      SELECT
		        id                         AS id,
		        title                      AS title,
		        description                AS description,
		        category                   AS category,
		        brand                      AS brand,
		        price                      AS price,
		        rating::float8             AS rating,
		        (:wVec * vec_sim + :wText * txt_rank)::float8 AS score
		      FROM ranked
		      ORDER BY score DESC
		      LIMIT :limit OFFSET :offset
		      """,
      nativeQuery = true)
  List<ProductHit> hybridSearchWithFilters(
      @Param("qvec") String qvecLiteral,
      @Param("q") String qPlain,
      @Param("wVec") double wVec, // 0..1
      @Param("wText") double wText, // 0..1
      @Param("limit") int limit,
      @Param("offset") int offset,
      @Param("category") String category,
      @Param("brand") String brand,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("minRating") BigDecimal minRating,
      @Param("forceText") boolean forceText,
      @Param("minVecSim") BigDecimal minVecSim);

  @Query(
      value =
          """
		      SELECT COUNT(*)::bigint
		      FROM products p
		      WHERE p.embedding IS NOT NULL
		      	AND p.embedding_ready = TRUE
		        AND (:category IS NULL OR p.category = :category)
		        AND (:brand    IS NULL OR p.brand    = :brand)
		        AND (:minPrice IS NULL OR p.price   >= :minPrice)
		        AND (:maxPrice IS NULL OR p.price   <= :maxPrice)
		        AND (:minRating IS NULL OR p.rating >= :minRating)
		        AND (plainto_tsquery('english', :q) @@ p.tsv OR :forceText = FALSE)
		        AND (1.0 - ((p.embedding <-> CAST(:qvec AS vector)) / 2.0)) >= :minVecSim
		      """,
      nativeQuery = true)
  long hybridSearchCount(
      @Param("qvec") String qvecLiteral,
      @Param("q") String qPlain,
      @Param("category") String category,
      @Param("brand") String brand,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("minRating") BigDecimal minRating,
      @Param("forceText") boolean forceText,
      @Param("minVecSim") BigDecimal minVecSim);

  @Query("SELECT COUNT(p) FROM Product p")
  long countAll();

  @Query("SELECT COUNT(p) FROM Product p WHERE p.embeddingReady = TRUE")
  long countReady();
}
