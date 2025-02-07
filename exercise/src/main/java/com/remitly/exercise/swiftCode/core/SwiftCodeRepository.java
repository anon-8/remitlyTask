package com.remitly.exercise.swiftCode.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwiftCodeRepository extends JpaRepository<SwiftCodeEntity, String> {

    List<SwiftCodeEntity> findByCountryISO2(String countryISO2);

    List<SwiftCodeEntity> findBySwiftCodeStartingWith(String prefix);
}
