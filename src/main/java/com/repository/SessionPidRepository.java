package com.repository;

import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionPidRepository extends JpaRepository<SessionPid, Long> {

}
