package com.autodiag.AutoDiagIA.repository;

import com.autodiag.AutoDiagIA.entity.SessionPid;
import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionPidRepository extends JpaRepository<SessionPid, Long> {
}
