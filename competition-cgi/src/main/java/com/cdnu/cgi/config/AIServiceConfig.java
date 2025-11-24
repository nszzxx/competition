package com.cdnu.cgi.config;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIServiceConfig {
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("service_enabled")
    private Boolean serviceEnabled;
    @JsonProperty("base_url")
    private String baseUrl;
    @JsonProperty("api_key")
    private String apiKey;
    @JsonProperty("model")
    private String model;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    @JsonProperty("temperature")
    private Double temperature;
    @JsonProperty("timeout")
    private Integer timeout;
}
