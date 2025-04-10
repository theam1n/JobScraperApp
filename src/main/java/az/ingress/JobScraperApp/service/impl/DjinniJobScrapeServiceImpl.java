package az.ingress.JobScraperApp.service.impl;

import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.service.DjinniJobScrapeService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DjinniJobScrapeServiceImpl implements DjinniJobScrapeService {

    private static final String BASE_URL = "https://djinni.co";

    @Override
    public List<JobDto> scrapeJobs() throws IOException {
        List<JobDto> jobs = new ArrayList<>();
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusDays(5);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-javascript");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("https://djinni.co/login?from=frontpage_main");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys("aminaliyev838@gmail.com");
        driver.findElement(By.name("password")).sendKeys("Amin1234567890!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(driver1 -> driver1.getCurrentUrl().startsWith("https://djinni.co/my/"));

        Set<Cookie> seleniumCookies = driver.manage().getCookies();
        Map<String, String> jsoupCookies = new HashMap<>();
        for (Cookie cookie : seleniumCookies) {
            jsoupCookies.put(cookie.getName(), cookie.getValue());
        }

        driver.quit();

        int page = 1;

        while (true) {
            String url = BASE_URL + "/jobs" + "/?page=" + page;
            Document document = Jsoup.connect(url).cookies(jsoupCookies).get();

            Elements jobElements = document.select("ul.list-jobs li[id^=job-item]");

            if (jobElements.isEmpty()) break;

            boolean stop = false;

            for (Element jobElement : jobElements) {

                Element dateElement = jobElement.selectFirst("span.text-nowrap[title]");
                String fullDateStr = dateElement != null ? dateElement.attr("title") : "";

                if (fullDateStr == null || fullDateStr.isBlank()) continue;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
                LocalDateTime postedDateTime = LocalDateTime.parse(fullDateStr, formatter);
                ZonedDateTime zonedDateTime = postedDateTime.atZone(ZoneId.of("UTC+3"));
                LocalDateTime localPostedDateTime = zonedDateTime.toLocalDateTime();

                if (localPostedDateTime.isBefore(tenMinutesAgo)) {
                    stop = true;
                    break;
                }

                String title = jobElement.select("a.job-item__title-link").text();
                String company;
                try {
                    company = jobElement.select("a[data-analytics='company_page']").text();
                } catch (Exception e) {
                    company = "No Company";
                }
                String location = jobElement.select(".location-text").text();
                String link = BASE_URL + jobElement.select("a.job-item__title-link").attr("href");

                if (isValidJob(location, jobElement.text())) {
                    JobDto job = new JobDto();
                    job.setTitle(title);
                    job.setCompanyName(company);
                    job.setLocation(location);
                    job.setPostedDate(localPostedDateTime.toLocalDate());
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

}
