package com.example.job_system_social.user_preference_score;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class JacksonDeserializationSchema<T> implements DeserializationSchema<T> {

    private final Class<T> clazz;
    private final ObjectMapper mapper = new ObjectMapper();

    public JacksonDeserializationSchema(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, clazz);
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(clazz);
    }
}
