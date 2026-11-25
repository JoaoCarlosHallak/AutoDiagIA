package com.autodiag.AutoDiagIA.repository;

import com.autodiag.AutoDiagIA.entity.SessionPid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionPidRepository extends JpaRepository<SessionPid, Long> {
    List<SessionPid> findBySession_Id(Long sessionId);

    @Modifying
    @Query("delete from SessionPid sessionPid where sessionPid.session.id = :sessionId")
    int deleteAllBySessionId(@Param("sessionId") Long sessionId);

}
