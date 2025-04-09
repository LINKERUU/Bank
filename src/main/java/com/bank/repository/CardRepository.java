package com.bank.repository;

import com.bank.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Card} entities.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}