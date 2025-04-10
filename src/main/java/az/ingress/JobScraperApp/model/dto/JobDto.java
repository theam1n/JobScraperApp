package az.ingress.JobScraperApp.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobDto {

    String title;
    String companyName;
    String location;
    String jobType;
    String salaryRange;
    String jobDescription;
    String requirements;
    String experienceLevel;
    String educationLevel;
    String industry;
    LocalDate postedDate;
    LocalDate applicationDeadline;
    String howToApply;
    String companyLogo;
    List<String> benefits;
    List<String> tags;
    String source;
}