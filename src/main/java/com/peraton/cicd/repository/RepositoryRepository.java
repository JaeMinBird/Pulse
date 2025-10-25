package com.peraton.cicd.repository;

import com.peraton.cicd.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    Optional<Repository> findByName(String name);

    Optional<Repository> findByGithubUrl(String githubUrl);

    boolean existsByName(String name);

    boolean existsByGithubUrl(String githubUrl);

    @Query("SELECT r FROM Repository r LEFT JOIN FETCH r.builds WHERE r.id = :id")
    Optional<Repository> findByIdWithBuilds(Long id);

    @Query("SELECT r FROM Repository r ORDER BY r.createdAt DESC")
    List<Repository> findAllOrderByCreatedAtDesc();
}
