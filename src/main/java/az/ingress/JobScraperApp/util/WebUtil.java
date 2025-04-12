package az.ingress.JobScraperApp.util;

import az.ingress.JobScraperApp.config.DjinniProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebUtil {

    private final DjinniProperties djinniProperties;

    public Map<String, String> loginWithJsoup() throws IOException {
        Connection.Response loginPageResponse = Jsoup.connect(djinniProperties.getLoginUrl())
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0")
                .header("Connection", "keep-alive")
                .execute();

        Document loginPage = loginPageResponse.parse();
        String csrfToken = loginPage.select("input[name=csrfmiddlewaretoken]").val();

        Connection.Response response = Jsoup.connect(djinniProperties.getLoginUrl())
                .cookies(loginPageResponse.cookies())
                .data("csrfmiddlewaretoken", csrfToken)
                .data("email", djinniProperties.getEmail())
                .data("password", djinniProperties.getPassword())
                .method(Connection.Method.POST)
                .followRedirects(true)
                .execute();

        return response.cookies();
    }

}
