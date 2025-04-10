package az.ingress.JobScraperApp.util;

import org.jsoup.nodes.Element;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateParser {

    public static LocalDateTime parsePostedDate(Element dateElement) {

        String fullDateStr = dateElement != null ? dateElement.attr("title") : "";

        if (fullDateStr == null || fullDateStr.isBlank()) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        LocalDateTime postedDateTime = LocalDateTime.parse(fullDateStr, formatter);
        ZonedDateTime zonedDateTime = postedDateTime.atZone(ZoneId.of("UTC+3"));
        return zonedDateTime.toLocalDateTime();
    }
}
