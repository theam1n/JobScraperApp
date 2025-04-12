package az.ingress.JobScraperApp.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilterRequestDto {

    String location;

    Integer minExperienceYear;

    Integer maxExperienceYear;

    List<String> keywords;
}
