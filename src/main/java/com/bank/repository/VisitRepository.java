package com.bank.repository;

import com.bank.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
  Visit findByUrl(String url);

  @Query("SELECT new map(v.url as url, v.count as count) FROM Visit v")
  List<Map<String, Object>> findAllVisitCounts();
}