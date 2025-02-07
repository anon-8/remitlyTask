import com.remitly.exercise.swiftCode.core.SwiftCodeEntity;
import com.remitly.exercise.swiftCode.core.SwiftCodeRepository;
import com.remitly.exercise.swiftCode.features.ExcelParserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class ExcelParserServiceTest {

    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @InjectMocks
    private ExcelParserService excelParserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseAndSaveExcelFile_AssociatesBranches() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("COUNTRY ISO2 CODE");
            headerRow.createCell(1).setCellValue("SWIFT CODE");
            headerRow.createCell(2).setCellValue("CODE TYPE");
            headerRow.createCell(3).setCellValue("NAME");
            headerRow.createCell(4).setCellValue("ADDRESS");
            headerRow.createCell(5).setCellValue("TOWN NAME");
            headerRow.createCell(6).setCellValue("COUNTRY NAME");
            headerRow.createCell(7).setCellValue("TIME ZONE");

            Row hqRow = sheet.createRow(1);
            hqRow.createCell(0).setCellValue("US");
            hqRow.createCell(1).setCellValue("HEADUS33XXX");
            hqRow.createCell(2).setCellValue("BIC11");
            hqRow.createCell(3).setCellValue("Headquarter Bank");
            hqRow.createCell(4).setCellValue("100 HQ St");
            hqRow.createCell(5).setCellValue("City");
            hqRow.createCell(6).setCellValue("United States");
            hqRow.createCell(7).setCellValue("America/New_York");

            Row branchRow = sheet.createRow(2);
            branchRow.createCell(0).setCellValue("US");
            branchRow.createCell(1).setCellValue("HEADUS33BR1");
            branchRow.createCell(2).setCellValue("BIC11");
            branchRow.createCell(3).setCellValue("Branch Bank 1");
            branchRow.createCell(4).setCellValue("200 Branch Ave");
            branchRow.createCell(5).setCellValue("City");
            branchRow.createCell(6).setCellValue("United States");
            branchRow.createCell(7).setCellValue("America/New_York");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] excelBytes = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);

            when(swiftCodeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            SwiftCodeEntity headquarterStub = SwiftCodeEntity.builder()
                    .swiftCode("HEADUS33XXX")
                    .bankName("Headquarter Bank")
                    .address("100 HQ St, City")
                    .countryISO2("US")
                    .countryName("UNITED STATES")
                    .build();
            when(swiftCodeRepository.findById("HEADUS33XXX")).thenReturn(Optional.of(headquarterStub));

            excelParserService.parseAndSaveExcelFile(bis);
        }

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SwiftCodeEntity>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(swiftCodeRepository).saveAll(listCaptor.capture());
        List<SwiftCodeEntity> savedEntities = listCaptor.getValue();

        SwiftCodeEntity headquarter = savedEntities.stream()
                .filter(e -> "HEADUS33XXX".equals(e.getSwiftCode()))
                .findFirst().orElse(null);
        SwiftCodeEntity branch = savedEntities.stream()
                .filter(e -> "HEADUS33BR1".equals(e.getSwiftCode()))
                .findFirst().orElse(null);

        assertNotNull(headquarter, "Headquarter record should be saved");
        assertNotNull(branch, "Branch record should be saved");

        ArgumentCaptor<SwiftCodeEntity> entityCaptor = ArgumentCaptor.forClass(SwiftCodeEntity.class);
        verify(swiftCodeRepository, atLeastOnce()).save(entityCaptor.capture());
        List<SwiftCodeEntity> savedUpdates = entityCaptor.getAllValues();

        SwiftCodeEntity updatedBranch = savedUpdates.stream()
                .filter(e -> "HEADUS33BR1".equals(e.getSwiftCode()))
                .findFirst().orElse(null);

        assertNotNull(updatedBranch, "Branch record should have been updated");
        assertNotNull(updatedBranch.getHeadquarter(), "Branch record should reference its headquarter");
        assertEquals("HEADUS33XXX", updatedBranch.getHeadquarter().getSwiftCode(),
                "Branch's headquarter swift code should match the headquarter record");
    }
}
