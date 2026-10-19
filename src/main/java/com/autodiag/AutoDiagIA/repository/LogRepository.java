package com.autodiag.AutoDiagIA.repository;

import com.autodiag.AutoDiagIA.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface  LogRepository extends JpaRepository<Log, Long> {

}
