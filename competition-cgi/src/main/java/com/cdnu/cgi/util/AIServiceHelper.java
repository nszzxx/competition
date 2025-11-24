package com.cdnu.cgi.util;

import com.cdnu.cgi.config.AIConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * AI服务调用辅助类
 * 封装通用的AI模型调用逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIServiceHelper {
    private final AIConfig aiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
    
    /**
     * 调用AI模型
     */
    public String callAIModel(String prompt) {
        try {
            String ServiceName = aiConfig.getServiceName();
            if(ServiceName != null && aiConfig.getServiceEnabled()) {
                switch (ServiceName.toLowerCase()) {
                    case "openai":
                        return callOpenAI(prompt);
                    case "anthropic":
                        return callClaude(prompt);
                    case "onmygpt":
                        return callOnMyGpt(prompt);
                    case "deepseek":
                        return callDeepSeek(prompt);
                    case "ollama":
                        return callOllama(prompt);
                    case "Moonshot":
                        return "月之暗面服务暂未集成，请联系管理员。";
                    default:
                        throw new IllegalArgumentException("不支持的AI提供商: " + ServiceName);
                }
            }
            return "AI服务未配置，请联系管理员。";
        } catch (Exception e) {
            log.error("AI模型调用失败", e);
            return "AI服务暂时不可用，请稍后再试。";
        }
    }
    
    /**
     * 带缓存的AI服务调用
     */
    public <T> T executeWithCache(String cacheKey, Supplier<T> serviceCall, Supplier<T> fallbackSupplier, String operationName) {
        try {
            // 检查缓存
            if (aiConfig.getCache().getEnabled()) {
                CacheItem cached = cache.get(cacheKey);
                if (cached != null && !cached.isExpired()) {
                    log.info("使用缓存的{}结果", operationName);
                    return (T) cached.getData();
                }
            }
            // 执行服务调用
            T result = serviceCall.get();
            // 缓存结果
            if (aiConfig.getCache().getEnabled()) {
                cache.put(cacheKey, new CacheItem(result, aiConfig.getCache().getTtlMinutes()));
            }
            
            return result;
        } catch (Exception e) {
            log.error("{}失败", operationName, e);
            return fallbackSupplier.get();
        }
    }
    
    /**
     * 通用的AI服务调用模板方法
     */
    public <T> T executeAIServiceCall(Supplier<T> serviceCall, Supplier<T> fallbackSupplier, String operationName) {
        try {
            log.info("执行{}操作", operationName);
            T result = serviceCall.get();
            log.info("{}操作完成", operationName);
            return result;
        } catch (Exception e) {
            log.error("{}操作失败", operationName, e);
            return fallbackSupplier.get();
        }
    }
    
    /**
     * 调用OpenAI API
     */
    private String callOpenAI(String prompt) throws Exception {
        AIRequestBuilder builder = new AIRequestBuilder()
            .setModel(aiConfig.getAIServiceConfig().getModel())
            .setMaxTokens(aiConfig.getAIServiceConfig().getMaxTokens())
            .setTemperature(aiConfig.getAIServiceConfig().getTemperature())
            .addSystemMessage(aiConfig.getSystemPrompt())
            .addUserMessage(prompt)
            .setBearerAuth(aiConfig.getAIServiceConfig().getApiKey());

        String url = aiConfig.getAIServiceConfig().getBaseUrl() + "/chat/completions";
        ResponseEntity<String> response = restTemplate.postForEntity(url, builder.build(), String.class);
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.path("choices").get(0).path("message").path("content").asText();
    }

    /**
     * 调用GLM API
     */
    private String callOnMyGpt(String prompt) throws Exception {
        AIRequestBuilder builder = new AIRequestBuilder()
            .setModel(aiConfig.getAIServiceConfig().getModel())
            .setMaxTokens(aiConfig.getAIServiceConfig().getMaxTokens())
            .setTemperature(aiConfig.getAIServiceConfig().getTemperature())
            .addSystemMessage(aiConfig.getSystemPrompt())
            .addUserMessage(prompt)
            .setBearerAuth(aiConfig.getAIServiceConfig().getApiKey());

        String url = aiConfig.getAIServiceConfig().getBaseUrl() + "/chat/completions";
        ResponseEntity<String> response = restTemplate.postForEntity(url, builder.build(), String.class);
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.path("choices").get(0).path("message").path("content").asText();
    }
    
    /**
     * 调用Claude API
     */
    private String callClaude(String prompt) throws Exception {
        AIRequestBuilder builder = new AIRequestBuilder()
            .setModel(aiConfig.getAIServiceConfig().getModel())
            .setMaxTokens(aiConfig.getAIServiceConfig().getMaxTokens())
            .addUserMessage(prompt)
            .setHeader("x-api-key", aiConfig.getAIServiceConfig().getApiKey())
            .setHeader("anthropic-version", "2023-06-01")
            .setSystemPrompt(aiConfig.getSystemPrompt());
        
        String url = aiConfig.getAIServiceConfig().getBaseUrl() + "/v1/messages";
        ResponseEntity<String> response = restTemplate.postForEntity(url, builder.build(), String.class);
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.path("content").get(0).path("text").asText();
    }
    
    /**
     * 调用DeepSeek API
     */
    private String callDeepSeek(String prompt) throws Exception {
        AIRequestBuilder builder = new AIRequestBuilder()
            .setModel(aiConfig.getAIServiceConfig().getModel())
            .setMaxTokens(aiConfig.getAIServiceConfig().getMaxTokens())
            .addSystemMessage(aiConfig.getSystemPrompt())
            .addUserMessage(prompt)
            .setBearerAuth(aiConfig.getAIServiceConfig().getApiKey());
        
        String url = aiConfig.getAIServiceConfig().getBaseUrl() + "/chat/completions";
        ResponseEntity<String> response = restTemplate.postForEntity(url, builder.build(), String.class);
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.path("choices").get(0).path("message").path("content").asText();
    }
    
    /**
     * 调用Ollama API
     */
    private String callOllama(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getAIServiceConfig().getModel());
        requestBody.put("prompt", aiConfig.getSystemPrompt() + "\n\n" + prompt);
        requestBody.put("stream", false);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = aiConfig.getAIServiceConfig().getBaseUrl() + "/api/generate";
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.path("response").asText();
    }
    
    /**
     * AI请求构建器
     */
    public static class AIRequestBuilder {
        private final Map<String, Object> requestBody = new HashMap<>();
        private final List<Map<String, String>> messages = new ArrayList<>();
        private final HttpHeaders headers = new HttpHeaders();
        
        public AIRequestBuilder() {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        
        public AIRequestBuilder setModel(String model) {
            requestBody.put("model", model);
            return this;
        }
        
        public AIRequestBuilder setMaxTokens(Integer maxTokens) {
            if (maxTokens != null) {
                requestBody.put("max_tokens", maxTokens);
            }
            return this;
        }
        
        public AIRequestBuilder setTemperature(Double temperature) {
            if (temperature != null) {
                requestBody.put("temperature", temperature);
            }
            return this;
        }
        
        public AIRequestBuilder addSystemMessage(String content) {
            Map<String, String> message = new HashMap<>();
            message.put("role", "system");
            message.put("content", content);
            messages.add(message);
            return this;
        }
        
        public AIRequestBuilder addUserMessage(String content) {
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", content);
            messages.add(message);
            return this;
        }
        
        public AIRequestBuilder setBearerAuth(String token) {
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }
            return this;
        }
        
        public AIRequestBuilder setHeader(String name, String value) {
            headers.set(name, value);
            return this;
        }
        
        public AIRequestBuilder setSystemPrompt(String prompt) {
            requestBody.put("system", prompt);
            return this;
        }
        
        public HttpEntity<Map<String, Object>> build() {
            if (!messages.isEmpty()) {
                requestBody.put("messages", messages);
            }
            return new HttpEntity<>(requestBody, headers);
        }
    }
    
    /**
     * 缓存项
     */
    private static class CacheItem {
        private final Object data;
        private final long expireTime;
        
        public CacheItem(Object data, int ttlMinutes) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + (ttlMinutes * 60 * 1000L);
        }
        
        public Object getData() {
            return data;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}