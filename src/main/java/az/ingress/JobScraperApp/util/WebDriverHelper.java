package az.ingress.JobScraperApp.util;

import az.ingress.JobScraperApp.config.DjinniProperties;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebDriverHelper {

    private final DjinniProperties djinniProperties;

    public Map<String, String> login() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-javascript");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get(djinniProperties.getLoginUrl());
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys(djinniProperties.getEmail());
            driver.findElement(By.name("password")).sendKeys(djinniProperties.getPassword());
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(driver1 -> driver1.getCurrentUrl().startsWith(djinniProperties.getMyUrl()));

            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            Map<String, String> jsoupCookies = new HashMap<>();
            for (Cookie cookie : seleniumCookies) {
                jsoupCookies.put(cookie.getName(), cookie.getValue());
            }

            return jsoupCookies;
        } finally {
            driver.quit();
        }
    }
}
