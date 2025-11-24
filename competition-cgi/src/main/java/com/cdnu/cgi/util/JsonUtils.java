package com.cdnu.cgi.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.Iterator;

public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ObjectMapper objectMapper;

    private JsonUtils() {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static JsonUtils create() {
        return new JsonUtils();
    }
    private ObjectMapper getObjectMapper() {
        return this.objectMapper != null ? this.objectMapper : OBJECT_MAPPER;
    }

    private void buildJsonMapper() {
        if (this.objectMapper == null) {
            this.objectMapper = OBJECT_MAPPER.copy();
        }

    }

    public <T> String toJson(T object) {
        Validate.notNull(object, "Object must not be null", new Object[0]);

        try {
            return this.getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException var3) {
            JsonProcessingException e = var3;
            throw CheckUtils.unchecked(e, "Failed to mapping object to json");
        }
    }

    public <T> String toJsonPretty(T object) {
        Validate.notNull(object, "Object must not be null", new Object[0]);

        try {
            return this.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException var3) {
            JsonProcessingException e = var3;
            throw CheckUtils.unchecked(e, "Failed to mapping object to json");
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        Validate.notNull(json, "Json must not be null");
        Validate.notNull(clazz, "Class must not be null");
        return this.parse(json, clazz, null);
    }

    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        Validate.notNull(json, "Json must not be null", new Object[0]);
        Validate.notNull(typeReference, "TypeReference must not be null", new Object[0]);
        return this.parse(json, null, typeReference);
    }

    public <T> T convert(Object object, Class<T> clazz) {
        Validate.notNull(object, "Object must not be null", new Object[0]);
        Validate.notNull(clazz, "Class must not be null", new Object[0]);
        return this.getObjectMapper().convertValue(object, clazz);
    }

    public <T> T convert(Object object, TypeReference<T> typeReference) {
        Validate.notNull(object, "Object must not be null", new Object[0]);
        Validate.notNull(typeReference, "TypeReference must not be null", new Object[0]);
        return this.getObjectMapper().convertValue(object, typeReference);
    }

    private <T> T parse(String json, Class<T> clazz, TypeReference<T> typeReference) {
        try {
            return clazz != null ? this.getObjectMapper().readValue(json, clazz) : this.getObjectMapper().readValue(json, typeReference);
        } catch (IOException var5) {
            IOException e = var5;
            throw CheckUtils.unchecked(e, "Failed to mapping json to object");
        }
    }

    private <T> T parse(JsonNode node, Class<T> clazz, TypeReference<T> typeReference) {
        return clazz != null ? this.getObjectMapper().convertValue(node, clazz) : this.getObjectMapper().convertValue(node, typeReference);
    }
}
