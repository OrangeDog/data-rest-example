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

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class DeleteOfferTest {

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

    mockMvc.perform(delete(URL, "2"))
        .andExpect(status().isNotFound());

    assertThat(repository.findAll(), iterableWithSize(1));
  }

  @Test
  public void deleteOffer() throws Exception {
    assumeTrue(repository.findById(1L).isPresent());

    mockMvc.perform(delete(URL, "1"))
        .andExpect(status().isNoContent());

    assertThat(repository.findAll(), emptyIterable());
  }

}
