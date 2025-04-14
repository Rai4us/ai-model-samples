package org.example.text_to_json.format;

import com.fasterxml.jackson.databind.JsonNode;

public record JsonSchema(String name, JsonNode schema) {
}
