package com.remitly.exercise.swiftCode.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwiftCodeRequest {


    @NotBlank(message = "SWIFT code cannot be blank.")
    @Pattern(
            regexp = "^[A-Z0-9]{8}$|^[A-Z0-9]{11}$",
            message = "SWIFT code must be 8 or 11 alphanumeric characters."
    )
    private String swiftCode;

    @NotBlank(message = "Bank name cannot be blank.")
    private String bankName;

    @NotBlank(message = "Address cannot be blank.")
    private String address;


    @NotBlank(message = "Country ISO2 code cannot be blank.")
    @Size(min = 2, max = 2, message = "Country ISO2 code must be exactly 2 characters.")
    private String countryISO2;

    @NotBlank(message = "Country name cannot be blank.")
    private String countryName;
}
