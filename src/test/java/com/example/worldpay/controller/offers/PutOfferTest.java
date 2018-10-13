package com.example.worldpay.controller.offers;

import com.example.worldpay.model.Offer;
import com.example.worldpay.model.Price;
import com.example.worldpay.repository.OffersRepository;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
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

import java.math.BigDecimal;
import java.time.Period;
import java.util.Currency;

import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PutOfferTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired  // mocking the repo (with @MockBean) disables generation of the controller
  private OffersRepository repository;

  private static final String URL = "/offers/{id}";

  @Before
  public void createOffer() {
    repository.save(new Offer("foo", new Price(Currency.getInstance("USD"), BigDecimal.ONE), Period.ofMonths(3), false));
  }


  @Test
  public void notFound() throws Exception {
    assumeFalse(repository.findById(2L).isPresent());

    mockMvc.perform(put(URL, "2")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"GBP\", \"value\": 20}, " +
            "\"duration\": \"P1D\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/offers/2"))
        .andExpect(content().string(""));

    assertThat(repository.findAll(), iterableWithSize(2));
    Offer offer = repository.findById(2L).orElseThrow(AssertionError::new);
    assertEquals("foo", offer.getDescription());
    assertEquals(Currency.getInstance("GBP"), offer.getPrice().getCurrency());
    assertEquals(0, BigDecimal.valueOf(20).compareTo(offer.getPrice().getValue()));
    assertEquals(Period.ofDays(1), offer.getDuration());
    assertTrue(offer.isActive());
    assertFalse(offer.isCancelled());
    assertFalse(offer.isExpired());
  }

  // could duplicate most of the tests from CreateOfferTest, but we're only really interested in cancelling

  @Test
  public void cancelOffer() throws Exception {
    assumeFalse(repository.findById(1L).map(Offer::isCancelled).orElse(true));

    mockMvc.perform(put(URL, "1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{" +
            "\"description\": \"foo\", " +
            "\"price\": {\"currency\": \"USD\", \"value\": 1}, " +
            "\"duration\": \"P3M\"," +
            "\"cancelled\": true}"))
        .andExpect(status().isNoContent())
        .andExpect(header().string("Location", "http://localhost/offers/1"))
        .andExpect(content().string(""));

    assertTrue(repository.findById(1L).map(Offer::isCancelled).orElse(false));
    assertThat(repository.findAll(), iterableWithSize(1));
  }

}
