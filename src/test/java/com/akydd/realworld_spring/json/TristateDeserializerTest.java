package com.akydd.realworld_spring.json;

import com.akydd.realworld_spring.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the three Tristate states survive real Jackson 3 deserialization of a record. This is the
 * behaviour the openapi JsonNullable module could not provide on Spring Boot 4 (Jackson 3).
 */
class TristateDeserializerTest {

    private final JsonMapper mapper = JsonMapper.builder().addModule(new TristateModule()).build();

    private UpdateUserRequest read(String json) {
        return mapper.readValue(json, UpdateUserRequest.class);
    }

    @Test
    void absentFieldIsUndefined() {
        UpdateUserRequest r = read("{\"bio\":\"x\"}"); // image omitted entirely
        assertFalse(r.image().isPresent(), "an omitted field must be undefined (preserve)");
    }

    @Test
    void explicitNullIsPresentAndNull() {
        UpdateUserRequest r = read("{\"bio\":null}");
        assertTrue(r.bio().isPresent(), "an explicit null field must be present");
        assertNull(r.bio().get(), "an explicit null must carry a null value (clear)");
    }

    @Test
    void emptyStringIsPresentEmpty() {
        UpdateUserRequest r = read("{\"bio\":\"\"}");
        assertTrue(r.bio().isPresent());
        assertEquals("", r.bio().get(), "normalization to null happens in the mapper, not here");
    }

    @Test
    void valueIsPresentWithValue() {
        UpdateUserRequest r = read("{\"bio\":\"hello\"}");
        assertTrue(r.bio().isPresent());
        assertEquals("hello", r.bio().get());
    }
}
