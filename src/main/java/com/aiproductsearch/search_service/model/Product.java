package com.aiproductsearch.search_service.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
	
	@Id
	private UUID id;
	
	@NotBlank @Column(length = 200)
	private String title;
	
	@NotBlank @Column(columnDefinition = "text")
	private String description;
	
	@NotBlank @Column(length = 100)
	private String category;
	
	@NotBlank @Column(length = 100)
	private String brand;
	
	@NotBlank @DecimalMin("0.0")
	private BigDecimal price;
	
	@NotBlank @DecimalMin("0.0") @ DecimalMax("5.0")
	private BigDecimal rating;
	
	@Column(columnDefinition = "text")
	private String searchableText;
	
	@Override public String toString() { return title + " (" + id + ")"; }

}
