package az.ingress.JobScraperApp.service;

import az.ingress.JobScraperApp.model.dto.FilterRequestDto;
import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.model.dto.SortRequestDto;

import java.io.IOException;
import java.util.List;

public interface DjinniJobScrapeService {
    List<JobDto> scrapeJobs(Long daysBefore, List<String> keywords,
                            SortRequestDto sortRequest, FilterRequestDto filterRequest)
            throws IOException;
}
