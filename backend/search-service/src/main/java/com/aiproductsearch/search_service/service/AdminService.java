package com.aiproductsearch.search_service.service;

import com.aiproductsearch.search_service.dto.IndexStats;
import com.aiproductsearch.search_service.index.EmbeddingTaskQueue;
import com.aiproductsearch.search_service.repository.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final ProductRepository productrepo;
  private final EmbeddingTaskQueue queue;

  @Transactional(readOnly = true)
  public IndexStats stats() {
    long total = productrepo.countAll();
    long ready = productrepo.countReady();

    return IndexStats.of(total, ready);
  }

  @Transactional
  public void reindexOne(UUID id) {
    productrepo
        .findById(id)
        .ifPresent(
            p -> {
              p.setEmbeddingReady(false);
              productrepo.save(p);
            });
    queue.submit(id);
  }

  @Transactional(readOnly = true)
  public long reindexAll(int pageSize) {
    long submitted = 0;
    int page = 0;
    var pageReq = PageRequest.of(page, pageSize);
    var slice = productrepo.findAll(pageReq);
    while (!slice.isEmpty()) {
      slice.forEach(p -> queue.submit(p.getId()));
      submitted += slice.getNumberOfElements();
      page++;
      pageReq = PageRequest.of(page, pageSize);
      slice = productrepo.findAll(pageReq);
    }

    return submitted;
  }
}
