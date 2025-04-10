package az.ingress.JobScraperApp.service;

import az.ingress.JobScraperApp.model.dto.JobDto;

import java.io.IOException;
import java.util.List;

public interface DjinniJobScrapeService {

    List<JobDto> scrapeJobs(Long daysBefore, List<String> keywords) throws IOException;
}
