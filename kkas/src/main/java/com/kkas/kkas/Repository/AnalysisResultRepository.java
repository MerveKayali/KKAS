package com.kkas.kkas.Repository;

import com.kkas.kkas.Entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> , JpaSpecificationExecutor<AnalysisResult> {

}
