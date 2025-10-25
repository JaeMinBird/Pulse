package com.peraton.cicd.controller;

import com.peraton.cicd.dto.BuildDto;
import com.peraton.cicd.dto.SyncRequest;
import com.peraton.cicd.dto.SyncResponse;
import com.peraton.cicd.exception.GitHubApiException;
import com.peraton.cicd.service.BuildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/builds")
@RequiredArgsConstructor
@Slf4j
public class BuildController {

    private final BuildService buildService;

    /**
     * GET /api/builds - Get all builds
     *
     * @return List of all builds
     */
    @GetMapping
    public ResponseEntity<List<BuildDto>> getAllBuilds() {
        log.info("GET /api/builds - Fetching all builds");
        List<BuildDto> builds = buildService.getAllBuilds();
        return ResponseEntity.ok(builds);
    }

    /**
     * GET /api/builds/{id} - Get build by ID
     *
     * @param id Build ID
     * @return Build details
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuildDto> getBuildById(@PathVariable Long id) {
        log.info("GET /api/builds/{} - Fetching build details", id);
        return buildService.getBuildById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/builds/repository/{repositoryId} - Get builds by repository
     *
     * @param repositoryId Repository ID
     * @return List of builds for the repository
     */
    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<BuildDto>> getBuildsByRepositoryId(@PathVariable Long repositoryId) {
        log.info("GET /api/builds/repository/{} - Fetching builds for repository", repositoryId);
        List<BuildDto> builds = buildService.getBuildsByRepositoryId(repositoryId);
        return ResponseEntity.ok(builds);
    }

    /**
     * GET /api/builds/status/{status} - Get builds by status
     *
     * @param status Build status
     * @return List of builds with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BuildDto>> getBuildsByStatus(@PathVariable String status) {
        log.info("GET /api/builds/status/{} - Fetching builds by status", status);
        try {
            com.peraton.cicd.model.Build.BuildStatus buildStatus =
                    com.peraton.cicd.model.Build.BuildStatus.valueOf(status.toUpperCase());
            List<BuildDto> builds = buildService.getBuildsByStatus(buildStatus);
            return ResponseEntity.ok(builds);
        } catch (IllegalArgumentException e) {
            log.error("Invalid build status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/builds/sync - Trigger sync from GitHub API
     *
     * @param syncRequest Sync request with owner, repo, and repositoryId
     * @return Sync response with results
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncBuilds(@Valid @RequestBody SyncRequest syncRequest) {
        log.info("POST /api/builds/sync - Syncing builds from GitHub for {}/{}",
                syncRequest.getOwner(), syncRequest.getRepo());

        try {
            SyncResponse response = buildService.syncBuildsFromGitHub(syncRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid sync request: {}", e.getMessage());
            SyncResponse errorResponse = SyncResponse.builder()
                    .success(false)
                    .syncedCount(0)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (GitHubApiException e) {
            log.error("GitHub API error during sync: {}", e.getMessage());
            SyncResponse errorResponse = SyncResponse.builder()
                    .success(false)
                    .syncedCount(0)
                    .message("GitHub API error: " + e.getMessage())
                    .build();
            int statusCode = e.getStatusCode() > 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value();
            return ResponseEntity.status(statusCode).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during sync: {}", e.getMessage(), e);
            SyncResponse errorResponse = SyncResponse.builder()
                    .success(false)
                    .syncedCount(0)
                    .message("Internal server error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/builds - Create a new build
     *
     * @param buildDto Build data
     * @param repositoryId Repository ID
     * @return Created build
     */
    @PostMapping
    public ResponseEntity<BuildDto> createBuild(
            @RequestBody BuildDto buildDto,
            @RequestParam Long repositoryId) {
        log.info("POST /api/builds - Creating new build for repository {}", repositoryId);
        try {
            BuildDto created = buildService.createBuild(buildDto, repositoryId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/builds/{id} - Update a build
     *
     * @param id Build ID
     * @param buildDto Updated build data
     * @return Updated build
     */
    @PutMapping("/{id}")
    public ResponseEntity<BuildDto> updateBuild(
            @PathVariable Long id,
            @RequestBody BuildDto buildDto) {
        log.info("PUT /api/builds/{} - Updating build", id);
        try {
            BuildDto updated = buildService.updateBuild(id, buildDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Build not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/builds/{id} - Delete a build
     *
     * @param id Build ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuild(@PathVariable Long id) {
        log.info("DELETE /api/builds/{} - Deleting build", id);
        try {
            buildService.deleteBuild(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Build not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
