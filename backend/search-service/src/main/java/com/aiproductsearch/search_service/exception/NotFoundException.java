package com.aiproductsearch.search_service.exception;

@SuppressWarnings("serial")
public class NotFoundException extends RuntimeException {

  public NotFoundException(String resource, String id) {
    super(resource + " not found: " + id);
  }
}
