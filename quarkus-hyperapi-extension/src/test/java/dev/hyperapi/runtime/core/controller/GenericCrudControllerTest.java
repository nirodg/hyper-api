package dev.hyperapi.runtime.core.controller;

import dev.hyperapi.runtime.annotations.ExposeAPI;
import dev.hyperapi.runtime.core.common.EntityConfigProvider;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import dev.hyperapi.runtime.core.service.GenericCrudService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.base.MockitoException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

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
