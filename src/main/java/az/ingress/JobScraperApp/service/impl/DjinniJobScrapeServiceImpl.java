package az.ingress.JobScraperApp.service.impl;

import az.ingress.JobScraperApp.config.DjinniProperties;
import az.ingress.JobScraperApp.config.MessageConstants;
import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.service.DjinniJobScrapeService;
import az.ingress.JobScraperApp.util.DateParser;
import az.ingress.JobScraperApp.util.StringUtil;
import az.ingress.JobScraperApp.util.WebDriverHelper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DjinniJobScrapeServiceImpl implements DjinniJobScrapeService {

    private final DjinniProperties djinniProperties;
    private final WebDriverHelper webDriverHelper;

    @Override
    public List<JobDto> scrapeJobs(Long daysBefore, List<String> keywords) throws IOException {
        List<JobDto> jobs = new ArrayList<>();
        LocalDateTime referenceTime = LocalDateTime.now().minusDays(daysBefore);

        Map<String, String> jsoupCookies = webDriverHelper.login();

        int page = 1;

        String keywordPath = "";
        if (keywords != null && !keywords.isEmpty()) {
            keywordPath = keywords.stream()
                    .map(keyword -> djinniProperties.getKeywordPath() + keyword)
                    .collect(Collectors.joining("&", "?", ""));
        }

        String url = djinniProperties.getJobUrl() + keywordPath + (keywordPath.isEmpty() ? "" : "&");


        while (true) {
            String pagePath = djinniProperties.getPagePath() + page;

            Document document = Jsoup.connect(url + pagePath).cookies(jsoupCookies).get();

            Elements jobElements = document.select("ul.list-jobs li[id^=job-item]");

            if (jobElements.isEmpty()) break;

            boolean stop = false;

            for (Element jobElement : jobElements) {

                Element dateElement = jobElement.selectFirst("span.text-nowrap[title]");
                LocalDateTime localPostedDateTime = DateParser.parsePostedDate(dateElement);

                if (localPostedDateTime.isBefore(referenceTime)) {
                    stop = true;
                    break;
                }

                String title = jobElement.select("a.job-item__title-link").text();

                String company;
                try {
                    company = jobElement.select("a[data-analytics='company_page']").text();
                } catch (Exception e) {
                    company = MessageConstants.EMPTY_COMPANY;
                }

                String companyLogo;
                try {
                    companyLogo = jobElement.select("img.userpic-image_img").attr("src");
                    if (companyLogo == null || companyLogo.isEmpty()) {
                        companyLogo = MessageConstants.EMPTY_COMPANY_LOGO;
                    }
                } catch (Exception e) {
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
                    jobs.add(job);
                }
            }

            if (stop) break;
            page++;
        }

        return jobs;
    }

    private boolean isValidJob(String location, String fullText) {
        location = location.toLowerCase();
        fullText = fullText.toLowerCase();

        return (location.contains("remote") && location.contains("worldwide"))
                || (location.contains("remote") && fullText.contains("azerbaijan"))
                || fullText.contains("relocation");
    }


}
