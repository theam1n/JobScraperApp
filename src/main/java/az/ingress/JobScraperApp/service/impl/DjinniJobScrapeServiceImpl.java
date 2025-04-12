package az.ingress.JobScraperApp.service.impl;

import az.ingress.JobScraperApp.config.DjinniProperties;
import az.ingress.JobScraperApp.config.MessageConstants;
import az.ingress.JobScraperApp.model.dto.FilterRequestDto;
import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.model.dto.SortRequestDto;
import az.ingress.JobScraperApp.service.DjinniJobScrapeService;
import az.ingress.JobScraperApp.util.DateParser;
import az.ingress.JobScraperApp.util.StringUtil;
import az.ingress.JobScraperApp.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static az.ingress.JobScraperApp.util.JobUtil.sortJobs;
import static az.ingress.JobScraperApp.util.JobUtil.filterJobs;

@Service
@RequiredArgsConstructor
public class DjinniJobScrapeServiceImpl implements DjinniJobScrapeService {

    private final DjinniProperties djinniProperties;
    private final WebUtil webUtil;

    @Override
    public List<JobDto> scrapeJobs(Long daysBefore, List<String> keywords,
                                   SortRequestDto sortRequest, FilterRequestDto filterRequest) throws IOException {
        List<JobDto> jobs = new ArrayList<>();
        LocalDateTime referenceTime = LocalDateTime.now().minusDays(daysBefore);

        Map<String, String> jsoupCookies = webUtil.loginWithJsoup();

        String keywordPath = "";
        if (keywords != null && !keywords.isEmpty()) {
            keywordPath = keywords.stream()
                    .map(keyword -> djinniProperties.getKeywordPath() + keyword)
                    .collect(Collectors.joining("&", "?", ""));
        }

        String url = djinniProperties.getJobUrl() + keywordPath + (keywordPath.isEmpty() ? "" : "&");

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<List<JobDto>>> futures = new ArrayList<>();

        List<String> pageUrls = new ArrayList<>();

        int lastPageNumber = getLastPageNumber(jsoupCookies);

        for (int i = 1; i <= lastPageNumber; i++) {
            String pageUrl = url + (keywordPath.isEmpty() ? "?" : "&") + djinniProperties.getPagePath() + i;
            pageUrls.add(pageUrl);
        }

        AtomicBoolean shouldBreak = new AtomicBoolean(false);

        for (String pageUrl : pageUrls) {
            if (shouldBreak.get()) {
                break;
            }

            Future<List<JobDto>> future = executorService.submit(() -> {
                if (shouldBreak.get()) return Collections.emptyList();

                List<JobDto> pageJobs = new ArrayList<>();
                Document document = Jsoup.connect(pageUrl).cookies(jsoupCookies).get();
                Elements jobElements = document.select("ul.list-jobs li[id^=job-item]");

                if (jobElements.isEmpty()) {
                    return pageJobs;
                }

                for (Element jobElement : jobElements) {
                    Element dateElement = jobElement.selectFirst("span.text-nowrap[title]");
                    LocalDateTime localPostedDateTime = DateParser.parsePostedDate(dateElement);

                    if (localPostedDateTime.isBefore(referenceTime)) {
                        shouldBreak.set(true);
                        break;
                    }

                    String title = jobElement.select("a.job-item__title-link").text();
                    String company = jobElement.select("a[data-analytics='company_page']").text();
                    String companyLogo = jobElement.select("img.userpic-image_img").attr("src");
                    if (companyLogo == null || companyLogo.isEmpty()) {
                        companyLogo = MessageConstants.EMPTY_COMPANY_LOGO;
                    }

                    String location = jobElement.select(".location-text").text();
                    String sourceLink = djinniProperties.getBaseUrl() + jobElement.select("a.job-item__title-link").attr("href");
                    String description = jobElement.select("span.js-original-text").text();
                    String experience = StringUtil.extractExperience(jobElement);

                    if (isValidJob(location, jobElement.text())) {
                        JobDto job = JobDto.builder().title(title).companyName(company).companyLogo(companyLogo)
                                .location(location).postedDate(localPostedDateTime).source(sourceLink)
                                .jobDescription(description).experienceLevel(experience).build();
                        pageJobs.add(job);
                    }
                }

                return pageJobs;
            });

            futures.add(future);
        }


        for (Future<List<JobDto>> future : futures) {
            try {
                jobs.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        var filteredJobs = filterJobs(jobs,filterRequest);
        sortJobs(filteredJobs, sortRequest);

        return filteredJobs;
    }

    private boolean isValidJob(String location, String fullText) {
        location = location.toLowerCase();
        fullText = fullText.toLowerCase();

        return (fullText.contains("full remote") && location.contains("worldwide"))
                || (fullText.contains("full remote") && fullText.contains("azerbaijan"))
                || fullText.contains("relocation");
    }

    private int getLastPageNumber(Map<String, String> jsoupCookies) throws IOException {
        Document firstPageDocument = Jsoup.connect(djinniProperties.getJobUrl()).cookies(jsoupCookies).get();
        Elements paginationLinks = firstPageDocument.select("ul.pagination li.page-item a.page-link");

        int lastPageNumber = 1;
        if (!paginationLinks.isEmpty()) {
            String secondLastPageLink = paginationLinks.get(paginationLinks.size() - 2).attr("href");
            String maxPageString = secondLastPageLink.replaceAll("[^0-9]", "");
            lastPageNumber = Integer.parseInt(maxPageString);
        }
        return lastPageNumber;
    }


}
