package az.ingress.JobScraperApp.util;

import az.ingress.JobScraperApp.config.MessageConstants;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StringUtil {

    public static String extractExperience(Element jobElement) {
        Elements spans = jobElement.select("span");

        for (Element span : spans) {
            String text = span.text().toLowerCase();

            if (text.contains("experience")) {
                return text;
            }
        }

        return MessageConstants.NO_EXPERIENCE_FOUND;
    }
}
