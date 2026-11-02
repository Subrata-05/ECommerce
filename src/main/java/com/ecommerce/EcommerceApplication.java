package com.ecommerce;

import com.ecommerce.config.EnvConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(EnvConfig.class)
public class EcommerceApplication {
  public static void main(String[] args) {
    // 1. Load .env file (ignores if missing, like in Docker/Prod)
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    // 2. Export entries into Java System Properties
    dotenv
        .entries()
        .forEach(
            entry -> {
              // Only set if not already set by OS environment
              if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
              }
            });

    // 3. Launch Spring Boot
    SpringApplication.run(EcommerceApplication.class, args);
  }
}
