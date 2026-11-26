package io.podwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de PodWatch.
 *
 * <p>Organisation en monolithe modulaire (ADR-001) : un package par domaine
 * ({@code cluster}, {@code resource}, {@code logs}, {@code security}, …).
 */
@SpringBootApplication
public class PodWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(PodWatchApplication.class, args);
    }
}
