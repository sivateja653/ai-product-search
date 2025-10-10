package com.aiproductsearch.search_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

  @Id private UUID id;

  @NotBlank
  @Column(nullable = false, length = 200)
  private String title;

  @NotBlank
  @Column(columnDefinition = "text")
  private String description;

  @NotBlank
  @Column(length = 100)
  private String category;

  @NotBlank
  @Column(length = 100)
  private String brand;

  @NotNull @DecimalMin("0.0")
  private BigDecimal price;

  @NotNull @DecimalMin("0.0")
  @DecimalMax("5.0")
  private BigDecimal rating;

  @Column(columnDefinition = "text")
  private String searchableText;

  @Column(updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  @Column(name = "searchable_hash")
  private String searchableHash;

  @Column(name = "embedding_ready")
  private Boolean embeddingReady;

  @Override
  public String toString() {
    return title + " (" + id + ")";
  }
}
