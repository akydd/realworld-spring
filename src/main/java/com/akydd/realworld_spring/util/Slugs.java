package com.akydd.realworld_spring.util;

public final class Slugs {
    private Slugs() {}

    public static String slugify(String str) {
        return str
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s+]", "")
                .replaceAll("\\s+", "-");
    }
}
