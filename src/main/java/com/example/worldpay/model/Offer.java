package com.example.worldpay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;

import static java.time.ZoneOffset.UTC;

@Data
@Entity
//@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Offer extends ResourceSupport {

  @GeneratedValue
  @Id
  @JsonIgnore
  private long offerId;

  @CreatedDate
  @JsonIgnore
  private Instant createdAt;

  @Length(max=1024)
  @NotEmpty
  @Setter
  private String description;

  @Embedded
  @NotNull
  @Setter
  private Price price;

  @NotNull
  @Setter
  private Period duration;

  @Setter
  private boolean cancelled;

  @JsonProperty ("_expired")
  public boolean isExpired() {
    return LocalDateTime
        .ofInstant(getCreatedAt(), UTC)
        .plus(getDuration())
        .isBefore(LocalDateTime.now(UTC));
  }

  @JsonProperty ("_active")
  public boolean isActive() {
    return !isCancelled() && !isExpired();
  }


  public Offer(String description, Price price, Period duration, boolean cancelled) {
    this.description = description;
    this.price = price;
    this.duration = duration;
    this.cancelled = cancelled;
  }
}
