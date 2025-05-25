package com.bank.repository;

import com.bank.model.Visit;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Visit entities.
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
  /**
   * Finds a Visit entity by its URL.
   *
   * @param url the URL to search for
   * @return the Visit entity with the specified URL
   */
  Visit findByUrl(String url);

  /**
   * Retrieves all visit counts as a list of maps containing URL and count.
   *
   * @return list of maps with URL-count pairs
   */
  @Query("SELECT new map(v.url as url, v.count as count) FROM Visit v")
  List<Map<String, Object>> findAllVisitCounts();
}