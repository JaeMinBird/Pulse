package com.peraton.cicd.service;

import com.peraton.cicd.dto.BuildDto;
import com.peraton.cicd.dto.SyncRequest;
import com.peraton.cicd.dto.SyncResponse;
import com.peraton.cicd.exception.GitHubApiException;
import com.peraton.cicd.model.Build;
import com.peraton.cicd.model.Repository;
import com.peraton.cicd.repository.BuildRepository;
import com.peraton.cicd.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildService {

    private final BuildRepository buildRepository;
    private final RepositoryRepository repositoryRepository;
    private final GitHubActionsService gitHubActionsService;

    /**
     * Get all builds
     *
     * @return List of BuildDto
     */
    @Transactional(readOnly = true)
    public List<BuildDto> getAllBuilds() {
        log.debug("Fetching all builds");
        List<Build> builds = buildRepository.findAll();
        return builds.stream()
                .map(BuildDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get build by ID
     *
     * @param id Build ID
     * @return Optional BuildDto
     */
    @Transactional(readOnly = true)
    public Optional<BuildDto> getBuildById(Long id) {
        log.debug("Fetching build with id: {}", id);
        return buildRepository.findById(id)
                .map(BuildDto::fromEntity);
    }

    /**
     * Get builds by repository ID
     *
     * @param repositoryId Repository ID
     * @return List of BuildDto
     */
    @Transactional(readOnly = true)
    public List<BuildDto> getBuildsByRepositoryId(Long repositoryId) {
        log.debug("Fetching builds for repository id: {}", repositoryId);
        List<Build> builds = buildRepository.findByRepositoryId(repositoryId);
        return builds.stream()
                .map(BuildDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get builds by status
     *
     * @param status Build status
     * @return List of BuildDto
     */
    @Transactional(readOnly = true)
    public List<BuildDto> getBuildsByStatus(Build.BuildStatus status) {
        log.debug("Fetching builds with status: {}", status);
        List<Build> builds = buildRepository.findByStatus(status);
        return builds.stream()
                .map(BuildDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Sync builds from GitHub API
     *
     * @param syncRequest Sync request containing owner, repo, and repositoryId
     * @return SyncResponse with sync results
     * @throws IllegalArgumentException if repository not found
     * @throws GitHubApiException if GitHub API call fails
     */
    @Transactional
    public SyncResponse syncBuildsFromGitHub(SyncRequest syncRequest) {
        log.info("Starting sync for repository: {}/{}", syncRequest.getOwner(), syncRequest.getRepo());

        // Validate repository exists
        Optional<Repository> repositoryOpt = repositoryRepository.findById(syncRequest.getRepositoryId());
        if (repositoryOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Repository not found with ID: %d", syncRequest.getRepositoryId())
            );
        }

        Repository repository = repositoryOpt.get();

        try {
            // Call GitHub Actions service to sync
            int syncedCount = gitHubActionsService.syncWorkflowRunsToDatabase(
                    syncRequest.getOwner(),
                    syncRequest.getRepo(),
                    syncRequest.getRepositoryId()
            );

            log.info("Successfully synced {} builds for repository: {}", syncedCount, repository.getName());

            return SyncResponse.builder()
                    .success(true)
                    .syncedCount(syncedCount)
                    .message(String.format("Successfully synced %d builds from GitHub", syncedCount))
                    .repositoryName(repository.getName())
                    .build();

        } catch (GitHubApiException e) {
            log.error("Failed to sync builds from GitHub: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during sync: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync builds: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new build
     *
     * @param buildDto Build DTO
     * @param repositoryId Repository ID
     * @return Created BuildDto
     * @throws IllegalArgumentException if repository not found
     */
    @Transactional
    public BuildDto createBuild(BuildDto buildDto, Long repositoryId) {
        log.info("Creating new build for repository id: {}", repositoryId);

        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Repository not found with ID: %d", repositoryId)
                ));

        Build build = buildDto.toEntity();
        build.setRepository(repository);

        Build savedBuild = buildRepository.save(build);
        log.info("Build created successfully with id: {}", savedBuild.getId());

        return BuildDto.fromEntity(savedBuild);
    }

    /**
     * Update a build
     *
     * @param id Build ID
     * @param buildDto Updated build data
     * @return Updated BuildDto
     * @throws IllegalArgumentException if build not found
     */
    @Transactional
    public BuildDto updateBuild(Long id, BuildDto buildDto) {
        log.info("Updating build with id: {}", id);

        Build build = buildRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Build not found with ID: %d", id)
                ));

        // Update fields
        if (buildDto.getStatus() != null) {
            build.setStatus(Build.BuildStatus.valueOf(buildDto.getStatus()));
        }
        if (buildDto.getCommitSha() != null) {
            build.setCommitSha(buildDto.getCommitSha());
        }
        if (buildDto.getStartedAt() != null) {
            build.setStartedAt(buildDto.getStartedAt());
        }
        if (buildDto.getCompletedAt() != null) {
            build.setCompletedAt(buildDto.getCompletedAt());
        }

        Build updatedBuild = buildRepository.save(build);
        log.info("Build updated successfully: {}", id);

        return BuildDto.fromEntity(updatedBuild);
    }

    /**
     * Delete a build
     *
     * @param id Build ID
     * @throws IllegalArgumentException if build not found
     */
    @Transactional
    public void deleteBuild(Long id) {
        log.info("Deleting build with id: {}", id);

        if (!buildRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    String.format("Build not found with ID: %d", id)
            );
        }

        buildRepository.deleteById(id);
        log.info("Build deleted successfully: {}", id);
    }
}
