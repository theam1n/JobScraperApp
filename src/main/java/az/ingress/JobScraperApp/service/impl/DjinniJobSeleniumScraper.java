package az.ingress.JobScraperApp.service.impl;

import az.ingress.JobScraperApp.model.dto.JobDto;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DjinniJobSeleniumScraper {

    private static final String BASE_URL = "https://djinni.co";

    public List<JobDto> scrapeJobs() {
        List<JobDto> jobs = new ArrayList<>();
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusDays(5);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-javascript");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {

            driver.get("https://djinni.co/login?from=frontpage_main");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys("aminaliyev838@gmail.com");
            driver.findElement(By.name("password")).sendKeys("Amin1234567890!");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            wait.until(driver1 -> driver1.getCurrentUrl().startsWith("https://djinni.co/my/"));


            int page = 1;
            boolean stop = false;

            while (true) {
                String url = "https://djinni.co/jobs/?page=" + page;
                driver.get(url);

                List<WebElement> jobElements = driver.findElements(By.cssSelector("ul.list-jobs li[id^=job-item]"));
                System.out.println("Page: " + page);
                System.out.println("Size: " + jobElements.size());
                if (jobElements.isEmpty()) break;

                for (WebElement jobElement : jobElements) {
                    WebElement dateElement = jobElement.findElement(By.cssSelector("span.text-nowrap[title]"));
                    String fullDateStr = dateElement.getAttribute("data-original-title");

                    if (fullDateStr == null || fullDateStr.isBlank()) continue;


                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
                    LocalDateTime utcPostedDateTime = LocalDateTime.parse(fullDateStr, formatter);
                    ZonedDateTime zonedDateTime = utcPostedDateTime.atZone(ZoneId.of("UTC+3"));
                    ZonedDateTime bakuDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Baku"));
                    LocalDateTime localPostedDateTime = bakuDateTime.toLocalDateTime();

                    if (localPostedDateTime.isBefore(tenMinutesAgo)) {
                        stop = true;
                        break;
                    }

                    String title = jobElement.findElement(By.cssSelector("a.job-item__title-link")).getText();
                    String company;
                    try {
                        company = jobElement.findElement(By.cssSelector("a[data-analytics='company_page']")).getText();
                    } catch (Exception e) {
                        company = "No Company";
                    }
                    String location = jobElement.findElement(By.cssSelector(".location-text")).getText();
                    String link = jobElement.findElement(By.cssSelector("a.job-item__title-link")).getAttribute("href");

                    String fullText = jobElement.getText().toLowerCase();

                    if (isValidJob(location.toLowerCase(), fullText)) {
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
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