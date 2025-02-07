package com.remitly.exercise.swiftCode.features;

import com.remitly.exercise.exceptions.ResourceNotFoundException;
import com.remitly.exercise.swiftCode.core.SwiftCodeDTO;
import com.remitly.exercise.swiftCode.core.SwiftCodeEntity;
import com.remitly.exercise.swiftCode.core.SwiftCodeRepository;
import com.remitly.exercise.swiftCode.core.SwiftCodeRequest;
import com.remitly.exercise.swiftCode.core.SwiftCodesByCountryDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {

    private final SwiftCodeRepository repository;

    public SwiftCodeController(final SwiftCodeRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{swiftCode}")
    public ResponseEntity<SwiftCodeDTO> findSwiftCode(@PathVariable final String swiftCode) {
        final SwiftCodeEntity entity = repository.findById(swiftCode)
                .orElseThrow(() -> new ResourceNotFoundException("SWIFT code " + swiftCode + " not found."));
        return ResponseEntity.ok(toDetailedDTO(entity));
    }

    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<SwiftCodesByCountryDTO> findSwiftCodesByCountry(@PathVariable final String countryISO2code) {
        final String upperCountryCode = countryISO2code.toUpperCase();
        final List<SwiftCodeEntity> entities = repository.findByCountryISO2(upperCountryCode);

        if (entities.isEmpty()) {
            throw new ResourceNotFoundException("No SWIFT codes found for country code " + countryISO2code);
        }

        final String countryName = entities.getFirst().getCountryName();
        final List<SwiftCodeDTO> swiftCodeDTOs = entities.stream()
                .map(this::toBasicDTO)
                .collect(Collectors.toList());

        final SwiftCodesByCountryDTO response = SwiftCodesByCountryDTO.builder()
                .countryISO2(upperCountryCode)
                .countryName(countryName)
                .swiftCodes(swiftCodeDTOs)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> createNewSwiftCode(@Valid @RequestBody final SwiftCodeRequest request) {
        final String swiftCode = request.getSwiftCode();

        if (repository.existsById(swiftCode)) {
            throw new IllegalArgumentException("SWIFT code already exists.");
        }

        final SwiftCodeEntity entity = SwiftCodeEntity.builder()
                .swiftCode(swiftCode)
                .bankName(request.getBankName().trim())
                .address(request.getAddress().trim())
                .countryISO2(request.getCountryISO2().toUpperCase())
                .countryName(request.getCountryName().toUpperCase())
                .isHeadquarter(swiftCode.endsWith("XXX"))
                .build();

        if (!entity.getIsHeadquarter()) {
            final String headquarterSwiftCode = swiftCode.substring(0, 8) + "XXX";
            repository.findById(headquarterSwiftCode).ifPresent(entity::setHeadquarter);
        } else {
            repository.saveAndFlush(entity);
            final String prefix = swiftCode.substring(0, 8);
            final List<SwiftCodeEntity> candidateBranches = repository.findBySwiftCodeStartingWith(prefix);
            for (final SwiftCodeEntity branch : candidateBranches) {
                if (!branch.getSwiftCode().equals(entity.getSwiftCode())) {
                    branch.setHeadquarter(entity);
                    entity.getBranches().add(branch);
                }
            }
        }

        repository.saveAndFlush(entity);

        final Map<String, String> response = new HashMap<>();
        response.put("message", "SWIFT code created successfully.");
        response.put("swiftCode", swiftCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<Map<String, String>> removeSwiftCode(@PathVariable final String swiftCode) {
        if (!repository.existsById(swiftCode)) {
            throw new ResourceNotFoundException("SWIFT code " + swiftCode + " not found.");
        }
        repository.deleteById(swiftCode);

        return ResponseEntity.ok(Map.of("message", "SWIFT code deleted successfully."));
    }

    private SwiftCodeDTO toDetailedDTO(final SwiftCodeEntity entity) {
        final SwiftCodeDTO.SwiftCodeDTOBuilder builder = SwiftCodeDTO.builder()
                .swiftCode(entity.getSwiftCode())
                .bankName(entity.getBankName())
                .address(entity.getAddress())
                .countryISO2(entity.getCountryISO2())
                .countryName(entity.getCountryName())
                .isHeadquarter(entity.getIsHeadquarter());

        if (entity.getIsHeadquarter() && entity.getBranches() != null && !entity.getBranches().isEmpty()) {
            final List<SwiftCodeDTO> branchDTOs = entity.getBranches().stream()
                    .map(this::toDetailedDTO)
                    .collect(Collectors.toList());
            builder.branches(branchDTOs);
        }
        return builder.build();
    }

    private SwiftCodeDTO toBasicDTO(final SwiftCodeEntity entity) {
        return SwiftCodeDTO.builder()
                .swiftCode(entity.getSwiftCode())
                .bankName(entity.getBankName())
                .address(entity.getAddress())
                .countryISO2(entity.getCountryISO2())
                .countryName(entity.getCountryName())
                .isHeadquarter(entity.getIsHeadquarter())
                .build();
    }
}
