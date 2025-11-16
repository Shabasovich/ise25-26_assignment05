package de.seuhd.campuscoffee.acctest;

import de.seuhd.campuscoffee.api.dtos.PosDto;
import de.seuhd.campuscoffee.domain.model.CampusType;
import de.seuhd.campuscoffee.domain.model.PosType;
import de.seuhd.campuscoffee.domain.ports.PosService;
import io.cucumber.java.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static de.seuhd.campuscoffee.TestUtils.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the POS Cucumber tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CucumberContextConfiguration
public class CucumberPosSteps {
    static final PostgreSQLContainer<?> postgresContainer;

    static {
        // share the same testcontainers instance across all Cucumber tests
        postgresContainer = getPostgresContainer();
        postgresContainer.start();
        // testcontainers are automatically stopped when the JVM exits
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        configurePostgresContainers(registry, postgresContainer);
    }

    @Autowired
    protected PosService posService;

    @LocalServerPort
    private Integer port;

    @Before
    public void beforeEach() {
        posService.clear();
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @After
    public void afterEach() {
        posService.clear();
    }

    private List<PosDto> createdPosList;
    private PosDto updatedPos;

    /**
     * Register a Cucumber DataTable type for PosDto.
     * @param row the DataTable row to map to a PosDto object
     * @return the mapped PosDto object
     */
    @DataTableType
    @SuppressWarnings("unused")
    public PosDto toPosDto(Map<String,String> row) {
        return PosDto.builder()
                .name(row.get("name"))
                .description(row.get("description"))
                .type(PosType.valueOf(row.get("type")))
                .campus(CampusType.valueOf(row.get("campus")))
                .street(row.get("street"))
                .houseNumber(row.get("houseNumber"))
                .postalCode(Integer.parseInt(row.get("postalCode")))
                .city(row.get("city"))
                .build();
    }

    // Given -----------------------------------------------------------------------

    @Given("an empty POS list")
    public void anEmptyPosList() {
        List<PosDto> retrievedPosList = retrievePos();
        assertThat(retrievedPosList).isEmpty();
    }

    // TODO: Add Given step for new scenario
    @Given("the following POS list exists:")
    public void theFollowingPosExists(List<PosDto> posList) {
        List<PosDto> createdPosList = createPos(posList);
        for (int i = 0; i < createdPosList.size(); i++) {
            PosDto created = createdPosList.get(i);
            PosDto retrieved = retrievePosById(created.id());
            assertThat(retrieved)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "createdAt", "updatedAt")
                    .isEqualTo(created);
        }

    }

    // When -----------------------------------------------------------------------

    @When("I insert POS with the following elements")
    public void insertPosWithTheFollowingValues(List<PosDto> posList) {
        createdPosList = createPos(posList);
        assertThat(createdPosList).size().isEqualTo(posList.size());
    }

    // TODO: Add When step for new scenario
    @When("I update the POS {string} with the following details:")

    // Then -----------------------------------------------------------------------
    public void iUpdateThePosWithTheFollowingDetails(String posName, PosDto updatedDetails) {
        PosDto existingPos = retrievePosByName(posName);
        PosDto posToUpdate = PosDto.builder()
                .id(existingPos.id())
                .name(updatedDetails.name())
                .description(updatedDetails.description())
                .type(updatedDetails.type())
                .campus(updatedDetails.campus())
                .street(updatedDetails.street())
                .houseNumber(updatedDetails.houseNumber())
                .postalCode(updatedDetails.postalCode())
                .city(updatedDetails.city())
                .build();

        updatedPos = given()
                .contentType("application/json")
                .body(posToUpdate)
                .when()
                .put("/api/pos/{id}", existingPos.id())
                .then()
                .statusCode(200)
                .extract().as(PosDto.class);
    }

    @Then("the POS list should contain the same elements in the same order")
    public void thePosListShouldContainTheSameElementsInTheSameOrder() {
        List<PosDto> retrievedPosList = retrievePos();
        assertThat(retrievedPosList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsExactlyInAnyOrderElementsOf(createdPosList);
    }
    @Then("the POS list should contain the following elements in the same order:")
    public void thePosListShouldContainTheFollowingElementsInTheSameOrder(List<PosDto> expectedPosList) {
        // Retrieve actual POS list from the API
        List<PosDto> actualPosList = retrievePos();

        // Compare the actual list with the expected list from the feature file
        assertThat(actualPosList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsExactlyInAnyOrderElementsOf(expectedPosList);
    }
    // TODO: Add Then step for new scenario
}
