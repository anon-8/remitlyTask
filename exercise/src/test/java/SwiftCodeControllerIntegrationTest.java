import com.remitly.exercise.ExerciseApplication;
import com.remitly.exercise.swiftCode.core.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = ExerciseApplication.class)
@AutoConfigureMockMvc
class SwiftCodeControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void overrideDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @BeforeEach
    void setUp() {
        swiftCodeRepository.deleteAll();
    }

    @Test
    void shouldReturnNotFoundWhenSwiftCodeDoesNotExist() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("SWIFT code UNKNOWN not found."));
    }

    @Test
    void shouldCreateAndRetrieveSwiftCode() throws Exception {
        String requestBody = """
            {
              "swiftCode": "BANKUS33XXX",
              "bankName": "Test Bank",
              "address": "456 Integration Road",
              "countryISO2": "us",
              "countryName": "united states",
              "isHeadquarter": true
            }
            """;

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("SWIFT code created successfully."));

        mockMvc.perform(get("/v1/swift-codes/BANKUS33XXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("BANKUS33XXX"))
                .andExpect(jsonPath("$.bankName").value("Test Bank"))
                .andExpect(jsonPath("$.address").value("456 Integration Road"))
                .andExpect(jsonPath("$.countryISO2").value("US"))
                .andExpect(jsonPath("$.countryName").value("UNITED STATES"));
    }

    @Test
    void shouldReturnAllSwiftCodesForACountry() throws Exception {
        String hqBody = """
        {
          "swiftCode": "TEST0001XXX",
          "bankName": "Test HQ Bank",
          "address": "123 HQ Street",
          "countryISO2": "us",
          "countryName": "united states",
          "isHeadquarter": true
        }
        """;

        String branchBody = """
        {
          "swiftCode": "TEST0001BR1",
          "bankName": "Test Branch Bank",
          "address": "456 Branch Street",
          "countryISO2": "us",
          "countryName": "united states",
          "isHeadquarter": false
        }
        """;

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hqBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(branchBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/country/us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2").value("US"))
                .andExpect(jsonPath("$.countryName").value("UNITED STATES"))
                .andExpect(jsonPath("$.swiftCodes").isArray())
                .andExpect(jsonPath("$.swiftCodes.length()").value(2))
                .andExpect(jsonPath("$.swiftCodes[?(@.swiftCode=='TEST0001XXX')]").exists())
                .andExpect(jsonPath("$.swiftCodes[?(@.swiftCode=='TEST0001BR1')]").exists());
    }


    @Test
    void shouldDeleteSwiftCode() throws Exception {
        String requestBody = """
            {
              "swiftCode": "DELETEUSXXX",
              "bankName": "Delete Bank",
              "address": "789 Remove Blvd",
              "countryISO2": "us",
              "countryName": "united states",
              "isHeadquarter": true
            }
            """;

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/v1/swift-codes/DELETEUSXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SWIFT code deleted successfully."));

        mockMvc.perform(get("/v1/swift-codes/DELETEUSXXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRetrieveHeadquarterWithBranches() throws Exception {
        String headquarterRequest = """
            {
              "swiftCode": "TEST0000XXX",
              "bankName": "Headquarter Bank",
              "address": "100 HQ St",
              "countryISO2": "us",
              "countryName": "united states"
            }
            """;
        String branchRequest1 = """
            {
              "swiftCode": "TEST0000BR1",
              "bankName": "Branch Bank 1",
              "address": "200 Branch Ave",
              "countryISO2": "us",
              "countryName": "united states"
            }
            """;
        String branchRequest2 = """
            {
              "swiftCode": "TEST0000BR2",
              "bankName": "Branch Bank 2",
              "address": "300 Branch Blvd",
              "countryISO2": "us",
              "countryName": "united states"
            }
            """;

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(headquarterRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(branchRequest1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(branchRequest2))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/TEST0000XXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("TEST0000XXX"))
                .andExpect(jsonPath("$.branches").isArray())
                .andExpect(jsonPath("$.branches.length()").value(2))
                .andExpect(jsonPath("$.branches[?(@.swiftCode=='TEST0000BR1')]").exists())
                .andExpect(jsonPath("$.branches[?(@.swiftCode=='TEST0000BR2')]").exists());

        mockMvc.perform(get("/v1/swift-codes/TEST0000BR1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("TEST0000BR1"))
                .andExpect(jsonPath("$.isHeadquarter").value(false))
                .andExpect(jsonPath("$.branches").isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateSwiftCode() throws Exception {
        String requestBody = """
            {
              "swiftCode": "DUPLUS33XXX",
              "bankName": "Duplicate Bank",
              "address": "456 Duplicate Ave",
              "countryISO2": "us",
              "countryName": "united states",
              "isHeadquarter": true
            }
            """;

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("SWIFT code already exists."));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingSwiftCode() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/NONEXISTING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("SWIFT code NONEXISTING not found."));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingSwiftCodeWithEmptyBody() throws Exception {
        String emptyBody = "{}";

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingSwiftCodeWithInvalidData() throws Exception {
        String invalidRequestBody = """
            {
              "swiftCode": "INVALID123ABCDE",
              "bankName": "",
              "address": "123 Some Street",
              "countryISO2": "USA", 
              "countryName": "united states",
              "isHeadquarter": true
            }
            """;

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }

}
