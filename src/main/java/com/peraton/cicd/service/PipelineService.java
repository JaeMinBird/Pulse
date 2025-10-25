package com.peraton.cicd.service;

import com.peraton.cicd.model.Pipeline;
import com.peraton.cicd.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineService {

    private final PipelineRepository pipelineRepository;

    @Transactional(readOnly = true)
    public List<Pipeline> getAllPipelines() {
        log.debug("Fetching all pipelines");
        return pipelineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pipeline> getPipelineById(Long id) {
        log.debug("Fetching pipeline with id: {}", id);
        return pipelineRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Pipeline> getPipelineByName(String name) {
        log.debug("Fetching pipeline with name: {}", name);
        return pipelineRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Pipeline> getPipelinesByStatus(Pipeline.PipelineStatus status) {
        log.debug("Fetching pipelines with status: {}", status);
        return pipelineRepository.findByStatus(status);
    }

    @Transactional
    public Pipeline createPipeline(Pipeline pipeline) {
        log.info("Creating new pipeline: {}", pipeline.getName());
        return pipelineRepository.save(pipeline);
    }

    @Transactional
    public Pipeline updatePipeline(Long id, Pipeline pipelineDetails) {
        log.info("Updating pipeline with id: {}", id);
        Pipeline pipeline = pipelineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pipeline not found with id: " + id));

        pipeline.setName(pipelineDetails.getName());
        pipeline.setDescription(pipelineDetails.getDescription());
        pipeline.setRepository(pipelineDetails.getRepository());
        pipeline.setBranch(pipelineDetails.getBranch());
        pipeline.setStatus(pipelineDetails.getStatus());

        return pipelineRepository.save(pipeline);
    }

    @Transactional
    public void deletePipeline(Long id) {
        log.info("Deleting pipeline with id: {}", id);
        pipelineRepository.deleteById(id);
    }
}
