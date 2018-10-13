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

import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(printOnlyOnFailure = false)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class ListOffersTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired  // mocking the repo (with @MockBean) disables generation of the controller
  private OffersRepository repository;

  private static final String URL = "/offers";
  private static final Currency GBP = Currency.getInstance("GBP");


  @Test
  public void listNone() throws Exception {
    mockMvc.perform(get(URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.offers").isArray())
        .andExpect(jsonPath("_embedded.offers").value(hasSize(0)))
        .andExpect(jsonPath("page.totalElements").value(0))
        .andExpect(jsonPath("page.number").value(0))
    ;
  }

  @Test
  @DirtiesContext
  public void listOne() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), false));

    mockMvc.perform(get(URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.offers").isArray())
        .andExpect(jsonPath("_embedded.offers").value(hasSize(1)))
        .andExpect(jsonPath("_embedded.offers[0].description").value("foo"))
        .andExpect(jsonPath("_embedded.offers[0].price.currency").value("GBP"))
        .andExpect(jsonPath("_embedded.offers[0].price.value").value(1.00))
        .andExpect(jsonPath("_embedded.offers[0].duration").value("P3M"))
        .andExpect(jsonPath("_embedded.offers[0].cancelled").value(false))
        .andExpect(jsonPath("_embedded.offers[0]._expired").value(false))
        .andExpect(jsonPath("_embedded.offers[0]._active").value(true))
        .andExpect(jsonPath("_embedded.offers[0]._links.self.href").value("http://localhost/offers/1"))
        .andExpect(jsonPath("page.totalElements").value(1))
        .andExpect(jsonPath("page.number").value(0))
    ;
  }

  @Test
  @DirtiesContext
  public void listTwo() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), false));
    repository.save(new Offer("bar", new Price(GBP, BigDecimal.TEN), Period.ofDays(14), true));

    mockMvc.perform(get(URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.offers").isArray())
        .andExpect(jsonPath("_embedded.offers").value(hasSize(2)))
        .andExpect(jsonPath("_embedded.offers[0].cancelled").value(false))
        .andExpect(jsonPath("_embedded.offers[0]._expired").value(false))
        .andExpect(jsonPath("_embedded.offers[0]._active").value(true))
        .andExpect(jsonPath("_embedded.offers[0]._links.self.href").value("http://localhost/offers/1"))
        .andExpect(jsonPath("_embedded.offers[1].cancelled").value(true))
        .andExpect(jsonPath("_embedded.offers[1]._expired").value(false))
        .andExpect(jsonPath("_embedded.offers[1]._active").value(false))
        .andExpect(jsonPath("_embedded.offers[1]._links.self.href").value("http://localhost/offers/2"))
        .andExpect(jsonPath("page.totalElements").value(2))
        .andExpect(jsonPath("page.number").value(0))
    ;
  }

  @Test
  @DirtiesContext
  public void firstPage() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), false));
    repository.save(new Offer("bar", new Price(GBP, BigDecimal.TEN), Period.ofDays(14), true));

    mockMvc.perform(get(URL + "?size=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.offers").isArray())
        .andExpect(jsonPath("_embedded.offers").value(hasSize(1)))
        .andExpect(jsonPath("_embedded.offers[0].description").value("foo"))
        .andExpect(jsonPath("_links.prev").doesNotExist())
        .andExpect(jsonPath("_links.next.href").value(allOf(
            startsWith("http://localhost/offers?"),
            containsString("size=1"),
            containsString("page=1")
        )))
        .andExpect(jsonPath("page.size").value(1))
        .andExpect(jsonPath("page.totalElements").value(2))
        .andExpect(jsonPath("page.totalPages").value(2))
        .andExpect(jsonPath("page.number").value(0))
    ;
  }

  @Test
  @DirtiesContext
  public void lastPage() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), false));
    repository.save(new Offer("bar", new Price(GBP, BigDecimal.TEN), Period.ofDays(14), true));

    mockMvc.perform(get(URL + "?size=1&page=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.offers").isArray())
        .andExpect(jsonPath("_embedded.offers").value(hasSize(1)))
        .andExpect(jsonPath("_embedded.offers[0].description").value("bar"))
        .andExpect(jsonPath("_links.next").doesNotExist())
        .andExpect(jsonPath("_links.prev.href").value(allOf(
            startsWith("http://localhost/offers?"),
            containsString("size=1"),
            containsString("page=0")
        )))
        .andExpect(jsonPath("page.size").value(1))
        .andExpect(jsonPath("page.totalElements").value(2))
        .andExpect(jsonPath("page.totalPages").value(2))
        .andExpect(jsonPath("page.number").value(1))
    ;
  }

  @Test
  @DirtiesContext
  public void emptyPage() throws Exception {
    repository.save(new Offer("foo", new Price(GBP, BigDecimal.ONE), Period.ofMonths(3), false));
    repository.save(new Offer("bar", new Price(GBP, BigDecimal.TEN), Period.ofDays(14), true));

    mockMvc.perform(get(URL + "?size=1&page=2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.offers").isArray())
        .andExpect(jsonPath("_embedded.offers").value(hasSize(0)))
        .andExpect(jsonPath("_links.next").doesNotExist())
        .andExpect(jsonPath("_links.prev.href").value(allOf(
            startsWith("http://localhost/offers?"),
            containsString("size=1"),
            containsString("page=1")
        )))
        .andExpect(jsonPath("page.size").value(1))
        .andExpect(jsonPath("page.totalElements").value(2))
        .andExpect(jsonPath("page.totalPages").value(2))
        .andExpect(jsonPath("page.number").value(2))
    ;
  }

}
