package com.aiproductsearch.search_service.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

public record SearchResponse<T>(
    List<T> items, // results
    int page, // current page (0-based)
    int size, // page size
    long total, // total matches across all pages
    int totalPages, // derived = ceil(total / size)
    boolean hasNext // derived = (page+1 < totalPages)
    ) {

  @SuppressFBWarnings(
      value = "IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN",
      justification = "Derived fields validated/recomputed to ensure consistency")
  public SearchResponse {
    items = (items == null) ? List.of() : List.copyOf(items);
    int safeSize = (size <= 0) ? 1 : size;
    page = Math.max(page, 0);

    int computedTotalPages = (int) Math.ceil((double) total / safeSize);
    boolean computedHasNext = (page + 1) < computedTotalPages;

    if (totalPages != computedTotalPages) totalPages = computedTotalPages;
    if (hasNext != computedHasNext) hasNext = computedHasNext;
  }

  public SearchResponse(List<T> items, int page, int size, long total) {
    this(
        items,
        page,
        size,
        total,
        (int) Math.ceil((double) total / (size <= 0 ? 1 : size)),
        (page + 1) < (int) Math.ceil((double) total / (size <= 0 ? 1 : size)));
  }
}
