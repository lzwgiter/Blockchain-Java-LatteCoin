package com.latte.blockchain.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Key;

/**
 * json工具类
 *
 * @author float
 * @since 2021/1/27
 */
@Component
public class JsonUtil extends JsonSerializer<Key> {
    public static String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serialize(Key value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(CryptoUtil.getStringFromKey(value));
    }
}
