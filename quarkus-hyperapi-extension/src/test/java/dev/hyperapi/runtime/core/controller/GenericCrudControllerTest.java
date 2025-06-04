package dev.hyperapi.runtime.core.controller;

import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

//@QuarkusTest
public class GenericCrudControllerTest {

    @TestHTTPResource
    URL baseUrl; // http://localhost:8081 or whatever port

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
    public void testGetAllEndpoint_returnsJsonArray() {
        given()
                .when()
                .get(baseUrl + "api/dummy?page=0&size=10")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(0));
    }

}
