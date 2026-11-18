package com.autodiag.AutoDiagIA.repository;

import com.autodiag.AutoDiagIA.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;





@Repository
public interface SessionRepository extends JpaRepository<Session, Long>{

}
