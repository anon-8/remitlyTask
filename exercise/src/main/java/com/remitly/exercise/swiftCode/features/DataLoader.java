package com.remitly.exercise.swiftCode.features;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DataLoader implements CommandLineRunner {

    private final ExcelParserService excelParserService;

    public DataLoader(ExcelParserService excelParserService) {
        this.excelParserService = excelParserService;
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/Interns_2025_SWIFT_CODES.xlsx");
        if (inputStream == null) {
            System.out.println("Excel file not found in resources. Skipping data load.");
            return;
        }
        excelParserService.parseAndSaveExcelFile(inputStream);
        System.out.println("SWIFT codes have been loaded successfully.");
    }
}