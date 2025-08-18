package com.example.job_system_social.detect_spam_comment;

import com.example.job_system_social.model.CommentEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class CommentEventDeserializationSchema implements DeserializationSchema<CommentEventRequest> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public CommentEventRequest deserialize(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, CommentEventRequest.class);
    }

    @Override
    public boolean isEndOfStream(CommentEventRequest event) {
        return false;
    }

    @Override
    public TypeInformation<CommentEventRequest> getProducedType() {
        return TypeInformation.of(CommentEventRequest.class);
    }
}

