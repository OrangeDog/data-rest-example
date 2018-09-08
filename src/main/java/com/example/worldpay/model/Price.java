package com.example.worldpay.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

@Data
public class Price {

  @NotNull
  private Currency currency;

  @Min(0)
  @NotNull
  private BigDecimal value;

}
