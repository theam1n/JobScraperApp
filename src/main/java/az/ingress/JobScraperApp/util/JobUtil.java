package az.ingress.JobScraperApp.util;

import az.ingress.JobScraperApp.model.dto.FilterRequestDto;
import az.ingress.JobScraperApp.model.dto.JobDto;
import az.ingress.JobScraperApp.model.dto.SortRequestDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JobUtil {


    public static void sortJobs(List<JobDto> jobs, SortRequestDto sortRequest) {

        if (sortRequest == null || sortRequest.getField() == null || sortRequest.getDirection() == null) {
            return;
        }

        Comparator<JobDto> comparator = null;

        switch (sortRequest.getField()) {
            case "title":
                comparator = Comparator.comparing(JobDto::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "companyName":
                comparator = Comparator.comparing(JobDto::getCompanyName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "location":
                comparator = Comparator.comparing(JobDto::getLocation, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "postedDate":
                comparator = Comparator.comparing(JobDto::getPostedDate, Comparator.nullsLast(LocalDateTime::compareTo));
                break;
            case "experienceLevel":
                comparator = Comparator.comparing(JobDto::getExperienceLevel, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            default:
                return;
        }

        if ("DESC".equalsIgnoreCase(sortRequest.getDirection())) {
            comparator = comparator.reversed();
        }

        jobs.sort(comparator);
    }

    public static List<JobDto> filterJobs(List<JobDto> jobs, FilterRequestDto filter) {
        if (filter == null) {
            return jobs;
        }

        return jobs.stream()
                .filter(job -> job.getLocation() != null &&
                        (filter.getLocation() == null || job.getLocation().equalsIgnoreCase(filter.getLocation())))
                .filter(job -> job.getExperienceLevel() != null &&
                        (filter.getMinExperienceYear() == null || StringUtil.extractExperienceYear(job.getExperienceLevel()) != null
                                && StringUtil.extractExperienceYear(job.getExperienceLevel()) >= filter.getMinExperienceYear()) &&
                        (filter.getMaxExperienceYear() == null || StringUtil.extractExperienceYear(job.getExperienceLevel()) != null
                                && StringUtil.extractExperienceYear(job.getExperienceLevel()) <= filter.getMaxExperienceYear()))
                .filter(job -> job.getTitle() != null &&
                        filter.getKeywords() == null ||
                        filter.getKeywords().stream().anyMatch(keyword ->
                                job.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                        (job.getJobDescription() != null && job.getJobDescription().toLowerCase().contains(keyword.toLowerCase()))))
                .collect(Collectors.toList());
    }
}
