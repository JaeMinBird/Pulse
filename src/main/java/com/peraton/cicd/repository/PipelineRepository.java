package com.peraton.cicd.repository;

import com.peraton.cicd.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    Optional<Pipeline> findByName(String name);

    List<Pipeline> findByStatus(Pipeline.PipelineStatus status);

    List<Pipeline> findByRepository(String repository);

    List<Pipeline> findByRepositoryAndBranch(String repository, String branch);
}
