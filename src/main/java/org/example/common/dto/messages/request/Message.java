package org.example.common.dto.messages.request;

import org.example.common.dto.messages.Role;
import org.example.common.dto.messages.content.Content;

import java.util.List;

public record Message(Role role, List<Content> content) {
}
