package org.example.common.dto.messages.response.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.common.dto.messages.Role;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
    Role role,
    Object content,
    Object refusal
) {}
