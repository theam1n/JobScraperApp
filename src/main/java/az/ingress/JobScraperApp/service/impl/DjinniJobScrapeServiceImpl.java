package az.ingress.JobScraperApp.service.impl;

import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.service.DjinniJobScrapeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DjinniJobScrapeServiceImpl implements DjinniJobScrapeService {

    private static final String BASE_URL = "https://djinni.co";

    @Override
    public List<JobDto> scrapeJobs() throws IOException {
        List<JobDto> jobs = new ArrayList<>();
//        LocalDate threeMonthsAgo = LocalDate.now().minusDays(3);
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(9000);

        int page = 1;

        while (true) {
            String url = BASE_URL + "/jobs" + "/?page=" + page;
            Document document = Jsoup.connect(url).get();

            Elements jobElements = document.select("ul.list-jobs li[id^=job-item]");
            System.out.println(jobElements.get(0).select("a.job-item__title-link").text());

            if (jobElements.isEmpty()) break;

            boolean stop = false;

            for (Element jobElement : jobElements) {

                Element dateElement = jobElement.selectFirst("span.text-nowrap[title]");
                String fullDateStr = dateElement != null ? dateElement.attr("title") : "";


                if (fullDateStr == null || fullDateStr.isBlank()) continue;

//                LocalDate postedDate = parseExactDate(fullDateStr);
//                if (postedDate.isBefore(threeMonthsAgo)) {
//                    stop = true;
//                    break;
//                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
                LocalDateTime postedDateTime = LocalDateTime.parse(fullDateStr, formatter);

                if (postedDateTime.isBefore(tenMinutesAgo)) {
                    stop = true;
                    break;
                }


                String title = jobElement.select("a.job-item__title-link").text();
                String company = jobElement.select(".job-list-item__company").text();
                String location = jobElement.select(".location-text").text();
                String link = "https://djinni.co" + jobElement.select("a.job-item__title-link").attr("href");

                if (isValidJob(location, jobElement.text())) {
                    JobDto job = new JobDto();
                    job.setTitle(title);
                    job.setCompanyName(company);
                    job.setLocation(location);
                    job.setPostedDate(null);
                    job.setSource(link);
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

    private LocalDate parseExactDate(String tooltipDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        try {
            return LocalDate.parse(tooltipDate, formatter);
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

}
