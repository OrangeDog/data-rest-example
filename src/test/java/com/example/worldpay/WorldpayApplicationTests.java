package com.example.worldpay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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

  @Autowired
  private TestRestTemplate restTemplate;

  @TestConfiguration
  public static class Config {
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
      return new RestTemplateBuilder().interceptors((request, body, execution) -> {
        if (body != null && body.length > 0) {
          request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        }
        return execution.execute(request, body);
      });
    }
  }

  @Test
  public void contextLoads() {
  }

  @Test
  public void offerIntegrationTest() {
    URI offersUri = UriComponentsBuilder.newInstance()
        .scheme("http")
        .host("localhost")
        .port(this.port)
        .pathSegment("offers")
        .build(emptyMap());

    URI offerUri = restTemplate.postForLocation(offersUri, "{" +
        "\"description\": \"foo\", " +
        "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
        "\"duration\": \"P1D\"}"
    );
    assertNotNull(offerUri);
    assertThat(offerUri.toString(), startsWith(offersUri.toString()));

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        offerUri, HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class)
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> expectedPrice = new HashMap<>();
    expectedPrice.put("currency", "GBP");
    expectedPrice.put("value", 20.0);
    assertThat(response.getBody(), allOf(
        hasEntry("description", "foo"),
        hasEntry("price", (Object) expectedPrice),
        hasEntry("duration", "P1D"),
        hasEntry("_active", (Object) true)
    ));

    restTemplate.put(offerUri,"{" +
        "\"description\": \"foo\", " +
        "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
        "\"duration\": \"P1D\"," +
        "\"cancelled\": true}"
    );
    response = restTemplate.exchange(
        offerUri, HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class)
    );
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody(), allOf(
        hasEntry("_active", false),
        hasEntry("cancelled", true)
    ));

    restTemplate.delete(offerUri);
    response = restTemplate.exchange(
        offerUri, HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class)
    );
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

}
