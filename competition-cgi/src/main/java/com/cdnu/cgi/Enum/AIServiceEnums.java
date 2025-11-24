package com.cdnu.cgi.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * AI服务商枚举类
 */
@AllArgsConstructor
@Getter
public enum AIServiceEnums {
    OPENAI("openai", "OpenAI服务"),
    CLAUDE("anthropic", "Anthropic Claude服务"),
    ONMYGPT("onmygpt", "OnMyGPT服务"),
    DEEP("deepseek", "深度求索deepseek服务"),
    OLLAMA("ollama", "Ollama本地服务"),
    KIMI("Moonshot", "月之暗面服务"),
    ;

    private final String key;
    private final String description;
}
