package com.aiproductsearch.search_service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "title is required") String title,
    String description,
    @NotBlank(message = "category is required") String category,
    @NotBlank(message = "brand is required") String brand,
    @NotNull(message = "price is required") @DecimalMin(value = "0.0", message = "price cannot be negative")
        BigDecimal price,
    @NotNull(message = "rating is required") @DecimalMin("0.0") @DecimalMax("5.0")
        BigDecimal rating) {}
