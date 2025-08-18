package com.example.user_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessageRequest {
    @JsonProperty
    private String to;
    @JsonProperty
    private String subject;
    @JsonProperty
    private String text;
    @JsonProperty
    private boolean isHtml;
}
