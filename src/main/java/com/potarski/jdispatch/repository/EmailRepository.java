package com.potarski.jdispatch.repository;

import com.potarski.jdispatch.domain.Email;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Email e WHERE e.id IN (SELECT e.id FROM Email e ORDER BY e.timestamp ASC LIMIT 5)")
    void deleteOldestFive();

}