package az.ingress.JobScraperApp.controller;

import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.service.DjinniJobScrapeService;
import az.ingress.JobScraperApp.service.impl.DjinniJobSeleniumScraper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobController {

    final DjinniJobScrapeService djinniJobScrapeService;

    final DjinniJobSeleniumScraper djinniJobSeleniumScraper;

    @GetMapping
    public ResponseEntity<List<JobDto>> scrapeJobs() throws IOException {
        var response = djinniJobSeleniumScraper.scrapeJobs();
        return ResponseEntity.ok(response);
    }
}
