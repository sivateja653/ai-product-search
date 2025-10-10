package com.aiproductsearch.search_service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Hashing {
  private Hashing() {}

  public static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(64);
      for (byte b : d) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }
}
