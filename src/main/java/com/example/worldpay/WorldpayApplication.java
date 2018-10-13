package com.example.worldpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@EnableJpaAuditing
@EnableTransactionManagement
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
