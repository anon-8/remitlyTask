package com.remitly.exercise.swiftCode.features;

import com.remitly.exercise.swiftCode.core.SwiftCodeEntity;
import com.remitly.exercise.swiftCode.core.SwiftCodeRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelParserService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelParserService.class);

    private final SwiftCodeRepository swiftCodeRepository;

    public ExcelParserService(SwiftCodeRepository swiftCodeRepository) {
        this.swiftCodeRepository = swiftCodeRepository;
    }

    @Transactional
    public void parseAndSaveExcelFile(InputStream inputStream) {
        List<SwiftCodeEntity> entities = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                SwiftCodeEntity entity = parseRow(row);
                if (entity != null) {
                    entities.add(entity);
                }
            }
            logger.info("Parsed {} rows from the Excel file.", entities.size());
        } catch (IOException e) {
            logger.error("Failed to parse Excel file", e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        swiftCodeRepository.saveAll(entities);
        logger.info("Saved {} SwiftCodeEntities to the database.", entities.size());

        for (SwiftCodeEntity entity : entities) {
            if (!entity.getIsHeadquarter()) {
                String branchSwiftCode = entity.getSwiftCode();
                if (branchSwiftCode != null && branchSwiftCode.length() >= 8) {
                    String headquarterSwiftCode = branchSwiftCode.substring(0, 8) + "XXX";
                    swiftCodeRepository.findById(headquarterSwiftCode).ifPresent(hq -> {
                        entity.setHeadquarter(hq);
                        swiftCodeRepository.save(entity);
                        logger.debug("Updated branch {} with headquarter {}.", entity.getSwiftCode(), hq.getSwiftCode());
                    });
                }
            }
        }
    }

    private SwiftCodeEntity parseRow(Row row) {
        String countryISO2 = getCellValueAsString(row.getCell(0));
        String swiftCode = getCellValueAsString(row.getCell(1));
        String bankName = getCellValueAsString(row.getCell(3));
        String address = getCellValueAsString(row.getCell(4));
        String townName = getCellValueAsString(row.getCell(5));
        String countryName = getCellValueAsString(row.getCell(6));

        if (swiftCode == null || swiftCode.trim().isEmpty()) {
            return null;
        }

        countryISO2 = (countryISO2 != null) ? countryISO2.toUpperCase() : null;
        countryName = (countryName != null) ? countryName.toUpperCase() : null;

        if (townName != null && !townName.trim().isEmpty()) {
            address = (address != null ? address : "") + ", " + townName.trim();
        }

        return SwiftCodeEntity.builder()
                .swiftCode(swiftCode.trim())
                .bankName(bankName != null ? bankName.trim() : null)
                .address(address != null ? address.trim() : null)
                .countryISO2(countryISO2)
                .countryName(countryName)
                .isHeadquarter(swiftCode.endsWith("XXX"))
                .build();
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
