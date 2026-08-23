package com.akydd.realworld_spring.dto;

import com.akydd.realworld_spring.json.Tristate;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@JsonRootName(value = "user")
public record UpdateUserRequest(
        @Email(message = "wut")
        String email,
        @Size(min = 1)
        String username,
        @Size(min = 8)
        String password,

        /*
         These two fields have 4 cases:
         1. Missing: keep existing value
         2. null: clear the field to null
         3. "": clear the field to null
         4. Set to a value: update to new value
        */
        Tristate<String> image,
        Tristate<String> bio
) {
    @AssertTrue(message = "Something's gotta be in here")
    public boolean isAtLeastOneNotEmpty() {
        return (email != null && !email.isEmpty()) ||
                (username != null && !username.isEmpty()) ||
                (password != null && !password.isEmpty()) ||
                bio.isPresent() || image.isPresent();
    }
}
