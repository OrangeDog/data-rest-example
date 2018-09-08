package com.example.worldpay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WorldpayApplicationTests {

  @LocalServerPort
  private int port;

  @Test
  public void contextLoads() {
  }

  @Test
  public void offerIntegrationTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    URI offersUri = UriComponentsBuilder.newInstance()
        .scheme("http")
        .host("localhost")
        .port(this.port)
        .pathSegment("offers")
        .build(emptyMap());

    URI offerUri = restTemplate.postForLocation(offersUri,
        "{\"description\": \"foo\"," +
            "     \"price\": {\"currency\": \"GBP\", \"value\": 20}," +
            "     \"duration\": \"P1D\"}"
    );
    assertNotNull(offerUri);
    assertThat(offerUri.toString(), startsWith(offersUri.toString()));

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        offerUri, HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class)
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> expectedPrice = new HashMap<>();
    expectedPrice.put("currency", "GBP");
    expectedPrice.put("value", 20);
    assertThat(response.getBody(), allOf(
        hasEntry("description", "foo"),
        hasEntry("price", (Object) expectedPrice),
        hasEntry("duration", "P1D"),
        hasEntry("active", (Object) true)
    ));

    restTemplate.delete(offersUri);
    response = restTemplate.exchange(
        offerUri, HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class)
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody(), allOf(
        hasEntry("active", false),
        hasEntry("cancelled", true)
    ));
  }

}
