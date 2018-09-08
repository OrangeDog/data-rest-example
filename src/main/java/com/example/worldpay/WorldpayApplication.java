package com.example.worldpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@EnableJpaAuditing
@SpringBootApplication
public class WorldpayApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorldpayApplication.class, args);
  }

  @Bean
  public AuditorAware auditorAware() {
    return Optional::empty;
  }

}
