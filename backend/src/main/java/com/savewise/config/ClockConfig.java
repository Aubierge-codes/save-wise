package com.savewise.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Injected wherever "today" or "this month" matters, so tests can pin the date. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
