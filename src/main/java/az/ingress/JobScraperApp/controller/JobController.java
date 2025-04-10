package az.ingress.JobScraperApp.controller;

import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.service.DjinniJobScrapeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobController {

    final DjinniJobScrapeService djinniJobScrapeService;

    @GetMapping
    public ResponseEntity<List<JobDto>> scrapeJobs(
            @RequestParam Long daysBefore,  @RequestParam List<String> keywords) throws IOException {

        var response = djinniJobScrapeService.scrapeJobs(daysBefore,keywords);
        return ResponseEntity.ok(response);
    }
}
