package com.example.sort_api.repository;

import com.example.sort_api.entity.SortingLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SortingLogRepository extends JpaRepository<SortingLog, Long> {
}
