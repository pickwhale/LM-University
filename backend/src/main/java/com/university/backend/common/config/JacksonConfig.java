package com.university.backend.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class JacksonConfig {

    private static final long MAX_SAFE_JAVASCRIPT_INTEGER = 9_007_199_254_740_991L;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder
            .serializerByType(Long.class, SafeLongSerializer.INSTANCE)
            .serializerByType(Long.TYPE, SafeLongSerializer.INSTANCE);
    }

    private static final class SafeLongSerializer extends JsonSerializer<Long> {
        private static final SafeLongSerializer INSTANCE = new SafeLongSerializer();

        @Override
        public void serialize(Long value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            if (value == null) {
                generator.writeNull();
                return;
            }
            if (Math.abs(value) > MAX_SAFE_JAVASCRIPT_INTEGER) {
                generator.writeString(value.toString());
                return;
            }
            generator.writeNumber(value);
        }
    }
}
