package com.aiproductsearch.search_service.service;

import com.aiproductsearch.search_service.index.EmbeddingTaskQueue;
import com.aiproductsearch.search_service.model.Product;
import com.aiproductsearch.search_service.repository.ProductRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportService {

  private final ProductRepository productrepo;
  private final EmbeddingTaskQueue queue;

  @Transactional
  public int importCsv(MultipartFile file) {
    int count = 0;
    try (var reader =
            new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        var parser =
            new CSVParser(
                reader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
      for (CSVRecord r : parser) {
        var p =
            Product.builder()
                .id(UUID.randomUUID())
                .title(r.get("title"))
                .description(r.get("description"))
                .category(r.get("category"))
                .brand(r.get("brand"))
                .price(parseBigDecimal(r.get("price")))
                .rating(parseBigDecimal(r.get("rating")))
                .embeddingReady(false)
                .build();

        var saved = productrepo.saveAndFlush(p);
        queue.submit(saved.getId());
        count++;
      }
      log.info("bulk-import: {} rows from {}", count, file.getOriginalFilename());
      return count;
    } catch (Exception e) {
      throw new RuntimeException("Failed to import CSV: " + e.getMessage(), e);
    }
  }

  private static BigDecimal parseBigDecimal(String s) {
    if (s == null || s.isBlank()) return null;
    try {
      return new BigDecimal(s);
    } catch (Exception e) {
      return null;
    }
  }
}
