package com.example.worldpay.controller.offers;

import com.example.worldpay.model.Offer;
import com.example.worldpay.model.Price;
import com.example.worldpay.repository.OffersRepository;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.TransactionSystemException;

import javax.persistence.RollbackException;
import javax.validation.ValidationException;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Currency;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CreateOfferTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired  // mocking the repo (with @MockBean) disables generation of the controller
  private OffersRepository repository;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private static final String URL = "/offers";


  @Test
  public void emptyPost() throws Exception {
    mockMvc.perform(post(URL))
        .andExpect(status().isBadRequest());

    assertThat(repository.findAll(), emptyIterable());
  }

  @Test
  public void emptyBody() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    assertThat(repository.findAll(), emptyIterable());
  }

  @Test
  public void emptyObject() throws Exception {
    // Spring Data Rest does validation inside Repository, not Controller
    // A custom Controller is required to @Valid these as BindException (and a 400 response)
    exception.expectCause(allOf(
        isA(TransactionSystemException.class),
        hasProperty("cause", allOf(
            isA(RollbackException.class),
            hasProperty("cause", isA(ValidationException.class))
        ))
    ));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"));
  }

  @Test
  public void validOffer() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
            "\"duration\": \"P1D\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/offers/1"))
        .andExpect(content().string(""));

    assertThat(repository.findAll(), iterableWithSize(1));
    Offer offer = repository.findById(1L).orElseThrow(AssertionError::new);
    assertEquals("foo", offer.getDescription());
    assertEquals(Currency.getInstance("GBP"), offer.getPrice().getCurrency());
    assertEquals(0, BigDecimal.valueOf(20).compareTo(offer.getPrice().getValue()));
    assertEquals(Period.ofDays(1), offer.getDuration());
    assertTrue(offer.isActive());
    assertFalse(offer.isCancelled());
    assertFalse(offer.isExpired());
  }

  @Test
  public void invalidCurrency() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"X\", \"value\": 20}, " +
            "\"duration\": \"P1D\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("not a valid textual representation")));

    assertThat(repository.findAll(), emptyIterable());
  }

  @Test
  public void invalidDuration() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
            "\"duration\": \"PD5M\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("cannot be parsed to a Period")));

    assertThat(repository.findAll(), emptyIterable());
  }

  @Test
  public void invalidPrice() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": \"foo\"}, " +
            "\"duration\": \"P1D\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("not a valid representation")));

    assertThat(repository.findAll(), emptyIterable());
  }

  @Test
  public void incrementalIds() throws Exception {
    for (int ii = 0; ii < 3; ii++) {
      mockMvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{" +
              "\"description\": \"foo\", " +
              "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
              "\"duration\": \"P1D\"}"))
          .andExpect(status().isCreated());
    }

    //noinspection unchecked
    assertThat(repository.findAll(), containsInAnyOrder(
        hasProperty("offerId", is(1L)),
        hasProperty("offerId", is(2L)),
        hasProperty("offerId", is(3L))
    ));
  }

  @Test
  public void freeOffer() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": 0}, " +
            "\"duration\": \"P1D\"}"))
        .andExpect(status().isCreated());

    assertEquals(Optional.of(0.0), repository.findById(1L).map(offer -> offer.getPrice().getValue().doubleValue()));
  }

  @Test
  public void decimalOffer() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": \"0.50\"}, " +
            "\"duration\": \"P1D\"}"))
        .andExpect(status().isCreated());

    assertEquals(Optional.of(0.5), repository.findById(1L).map(offer -> offer.getPrice().getValue().doubleValue()));
  }

  @Test
  public void expiredOffer() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
            "\"duration\": \"P-1D\"}"))
        .andExpect(status().isCreated());

    Offer offer = repository.findById(1L).orElseThrow(AssertionError::new);
    assertFalse(offer.isActive());
    assertTrue(offer.isExpired());
    assertFalse(offer.isCancelled());
  }

  @Test
  public void cancelledOffer() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
            "\"duration\": \"P2M\", " +
            "\"cancelled\": true}"))
        .andExpect(status().isCreated());

    Offer offer = repository.findById(1L).orElseThrow(AssertionError::new);
    assertFalse(offer.isActive());
    assertFalse(offer.isExpired());
    assertTrue(offer.isCancelled());
  }

}
