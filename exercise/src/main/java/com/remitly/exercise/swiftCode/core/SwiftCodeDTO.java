package com.remitly.exercise.swiftCode.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class SwiftCodeDTO {
    
    private String swiftCode;
    private String bankName;
    private String address;
    private String countryISO2;
    private String countryName;

    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;

    @Builder.Default
    private List<SwiftCodeDTO> branches = Collections.emptyList();
}
