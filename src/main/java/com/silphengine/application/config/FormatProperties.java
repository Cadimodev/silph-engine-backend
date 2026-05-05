package com.silphengine.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "pokemon.tcg.format")
@Getter
@Setter
public class FormatProperties {
    private List<String> standardValidMarks;
}