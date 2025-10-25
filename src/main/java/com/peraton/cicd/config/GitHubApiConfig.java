package com.peraton.cicd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github.api")
@Data
public class GitHubApiConfig {

    private String baseUrl;
    private String token;
    private Integer timeout;
}
