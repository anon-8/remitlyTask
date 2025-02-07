package com.remitly.exercise.swiftCode.core;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SwiftCodesByCountryDTO {
    private String countryISO2;
    private String countryName;
    private List<SwiftCodeDTO> swiftCodes;
}
