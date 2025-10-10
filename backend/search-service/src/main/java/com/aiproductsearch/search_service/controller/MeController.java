package com.aiproductsearch.search_service.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

  @GetMapping("/me")
  public Map<String, Object> me() {
    return Map.of(
        "id", "dev-user-123",
        "name", "Dev User",
        "email", "dev.user@example.com",
        "roles", new String[] {"USER"});
  }
}
