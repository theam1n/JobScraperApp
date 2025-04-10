package az.ingress.JobScraperApp.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "djinni")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DjinniProperties {

    String email;

    String password;

    String baseUrl;

    String loginUrl;

    String myUrl;

    String jobUrl;

    String keywordPath;

    String pagePath;
}
