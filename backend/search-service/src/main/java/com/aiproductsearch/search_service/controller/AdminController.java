package com.aiproductsearch.search_service.controller;

import com.aiproductsearch.search_service.dto.IndexStats;
import com.aiproductsearch.search_service.service.AdminService;
import com.aiproductsearch.search_service.service.BulkImportService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin") // admin scope
@RequiredArgsConstructor
public class AdminController {

  private final BulkImportService bulk;
  private final AdminService admin;

  @PostMapping("/import/csv")
  public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {

    int imported = bulk.importCsv(file);

    return ResponseEntity.ok(Map.of("imported", imported, "filename", file.getOriginalFilename()));
  }

  @GetMapping("/index-stats")
  public IndexStats stats() {
    System.out.println("Hello world\\\\");
    return admin.stats();
  }

  @PostMapping("/reindex/{id}")
  public ResponseEntity<?> reindexOne(@PathVariable UUID id) {

    admin.reindexOne(id);
    return ResponseEntity.accepted().body(Map.of("submitted", true, "id", id));
  }

  @PostMapping("/reindex-all")
  public ResponseEntity<?> reindexAll(@RequestParam(defaultValue = "500") int pageSize) {
    long n = admin.reindexAll(Math.max(50, Math.min(pageSize, 2000)));
    return ResponseEntity.accepted().body(Map.of("submitted", n));
  }

  @PostMapping("/replay/{id}")
  public ResponseEntity<?> replay(@PathVariable UUID id) {

    admin.reindexOne(id);
    return ResponseEntity.accepted().body(java.util.Map.of("replayed", id.toString()));
  }
}
