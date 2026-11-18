package com.autodiag.AutoDiagIA.repository;

import com.autodiag.AutoDiagIA.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface  LogRepository extends JpaRepository<Log, Long> {

    Page<Log> findBySession_Id(
            Long sessionId,
            Pageable pageable
    );

    Page<Log> findBySession_IdAndPid(
            Long sessionId,
            String pid,
            Pageable pageable
    );

    Page<Log> findBySession_IdAndTimestampBetween(
            Long sessionId,
            Instant fromTime,
            Instant toTime,
            Pageable pageable
    );

    List<Log> findBySession_IdOrderByTimestampAsc(Long sessionId);
}





