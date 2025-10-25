package com.peraton.cicd.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun {

    private Long id;

    private String name;

    @JsonProperty("head_branch")
    private String headBranch;

    @JsonProperty("head_sha")
    private String headSha;

    private String status;

    private String conclusion;

    @JsonProperty("workflow_id")
    private Long workflowId;

    @JsonProperty("run_number")
    private Integer runNumber;

    @JsonProperty("run_attempt")
    private Integer runAttempt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("run_started_at")
    private LocalDateTime runStartedAt;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("jobs_url")
    private String jobsUrl;

    @JsonProperty("logs_url")
    private String logsUrl;

    private Repository repository;

    @JsonProperty("head_commit")
    private Commit headCommit;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private Long id;
        private String name;
        @JsonProperty("full_name")
        private String fullName;
        @JsonProperty("html_url")
        private String htmlUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String id;
        private String message;
        private String timestamp;
        private Author author;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
    }
}
