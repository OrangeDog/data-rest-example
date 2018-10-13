package com.example.worldpay.controller.offers;

import com.example.worldpay.model.Offer;
import com.example.worldpay.model.Price;
import com.example.worldpay.repository.OffersRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class GetOfferTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired  // mocking the repo (with @MockBean) disables generation of the controller
    private OffersRepository repository;

    private static final String URL = "/offers/{id}";
    private static final Currency GBP = Currency.getInstance("GBP");


    @Test
    public void notFound() throws Exception {
      mockMvc.perform(get(URL, "1"))
          .andExpect(status().isNotFound())
          .andExpect(content().string(""))
      ;
    }

    @Test
    @DirtiesContext
    public void active() throws Exception {
      repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), false));

      mockMvc.perform(get(URL, "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("description").value("foo"))
          .andExpect(jsonPath("price.currency").value("GBP"))
          .andExpect(jsonPath("price.value").value(1.00))
          .andExpect(jsonPath("duration").value("P3M"))
          .andExpect(jsonPath("cancelled").value(false))
          .andExpect(jsonPath("_expired").value(false))
          .andExpect(jsonPath("_active").value(true))
          .andExpect(jsonPath("_links.self.href").value("http://localhost/offers/1"))
      ;
    }

  @Test
  @DirtiesContext
  public void cancelled() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), true));

    mockMvc.perform(get(URL, "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("description").value("foo"))
        .andExpect(jsonPath("price.currency").value("GBP"))
        .andExpect(jsonPath("price.value").value(1.00))
        .andExpect(jsonPath("duration").value("P3M"))
        .andExpect(jsonPath("cancelled").value(true))
        .andExpect(jsonPath("_expired").value(false))
        .andExpect(jsonPath("_active").value(false))
        .andExpect(jsonPath("_links.self.href").value("http://localhost/offers/1"))
    ;
  }

  @Test
  @DirtiesContext
  public void expired() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofDays(0), false));

    mockMvc.perform(get(URL, "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("description").value("foo"))
        .andExpect(jsonPath("price.currency").value("GBP"))
        .andExpect(jsonPath("price.value").value(1.00))
        .andExpect(jsonPath("duration").value("P0D"))
        .andExpect(jsonPath("cancelled").value(false))
        .andExpect(jsonPath("_expired").value(true))
        .andExpect(jsonPath("_active").value(false))
        .andExpect(jsonPath("_links.self.href").value("http://localhost/offers/1"))
    ;
  }

}
