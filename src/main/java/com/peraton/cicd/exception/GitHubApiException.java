package com.peraton.cicd.exception;

public class GitHubApiException extends RuntimeException {

    private final int statusCode;

    public GitHubApiException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public GitHubApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public GitHubApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
