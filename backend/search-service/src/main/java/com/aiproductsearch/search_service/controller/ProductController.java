package com.aiproductsearch.search_service.controller;

import com.aiproductsearch.search_service.dto.CreateProductRequest;
import com.aiproductsearch.search_service.dto.ProductHit;
import com.aiproductsearch.search_service.dto.SearchResponse;
import com.aiproductsearch.search_service.dto.UpdateProductRequest;
import com.aiproductsearch.search_service.model.Product;
import com.aiproductsearch.search_service.service.ProductService;
import com.aiproductsearch.search_service.service.SearchService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService service;
  private final SearchService searchservice;

  public ProductController(ProductService service, SearchService searchservice) {
    this.service = service;
    this.searchservice = searchservice;
  }

  // private static Logger log = LoggerFactory.getLogger(ProductController.class);

  @GetMapping
  public List<Product> list() {

    return service.list();
  }

  @GetMapping("/{id}")
  public Product get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product create(@RequestBody @Valid CreateProductRequest req) {
    return service.create(req);
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Product update(@RequestBody @Valid UpdateProductRequest req, @PathVariable UUID id) {
    return service.update(id, req);
  }

  @GetMapping("/search")
  public SearchResponse search(
      @RequestParam("q") String query,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "brand", required = false) String brand,
      @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
      @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
      @RequestParam(value = "minRating", required = false) BigDecimal minRating) {
    return searchservice.search(query, page, size, category, brand, minPrice, maxPrice, minRating);
  }

  @GetMapping("/searchscore")
  public List<ProductHit> searchscore(
      @RequestParam("q") String q, @RequestParam(value = "limit", defaultValue = "10") int limit) {
    return service.searchscore(q, limit);
  }

  @GetMapping("/search/hybrid")
  public SearchResponse hybrid(
      @RequestParam("q") String q,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "brand", required = false) String brand,
      @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
      @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
      @RequestParam(value = "minRating", required = false) BigDecimal minRating,
      @RequestParam(value = "wVec", required = false) Double wVec,
      @RequestParam(value = "wText", required = false) Double wText,
      @RequestParam(value = "forceText", required = false) Boolean forceText) {

    return searchservice.hybridSearch(
        q, page, size, category, brand, minPrice, maxPrice, minRating, wVec, wText, forceText);
  }
}
