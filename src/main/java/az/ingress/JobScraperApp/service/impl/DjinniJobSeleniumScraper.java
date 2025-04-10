package az.ingress.JobScraperApp.service.impl;

import az.ingress.JobScraperApp.model.dto.JobDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DjinniJobSeleniumScraper {

    private static final String BASE_URL = "https://djinni.co";

    public List<JobDto> scrapeJobs() {
        List<JobDto> jobs = new ArrayList<>();
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        try {
            int page = 1;
            boolean stop = false;

            while (true) {
                String url = BASE_URL + "/jobs/?page=" + page;
                driver.get(url);
                List<WebElement> jobElements = driver.findElements(By.cssSelector("ul.list-jobs li[id^=job-item]"));

                System.out.println(jobElements.get(0).findElement(By.cssSelector("a.job-item__title-link")).getText());
                if (jobElements.isEmpty()) break;

                for (WebElement jobElement : jobElements) {
                    WebElement dateElement = jobElement.findElement(By.cssSelector("span.text-nowrap[title]"));
                    String fullDateStr = dateElement.getAttribute("title");

                    if (fullDateStr == null || fullDateStr.isBlank()) continue;

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
                    LocalDateTime postedDateTime = LocalDateTime.parse(fullDateStr, formatter);

                    if (postedDateTime.isBefore(tenMinutesAgo)) {
                        stop = true;
                        break;
                    }

                    String title = jobElement.findElement(By.cssSelector("a.job-item__title-link")).getText();
                    String company = jobElement.findElement(By.cssSelector(".job-list-item__company")).getText();
                    String location = jobElement.findElement(By.cssSelector(".location-text")).getText();
                    String link = BASE_URL + jobElement.findElement(By.cssSelector("a.job-item__title-link")).getAttribute("href");
                    String fullText = jobElement.getText().toLowerCase();

                    if (isValidJob(location.toLowerCase(), fullText)) {
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
        } finally {
            driver.quit();
        }

        return jobs;
    }

    private boolean isValidJob(String location, String fullText) {
        return (location.contains("remote") && location.contains("worldwide"))
                || (location.contains("remote") && fullText.contains("azerbaijan"))
                || fullText.contains("relocation");
    }
}
