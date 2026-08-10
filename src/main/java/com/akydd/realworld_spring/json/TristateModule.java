package com.akydd.realworld_spring.json;

import tools.jackson.databind.module.SimpleModule;

/**
 * Registers {@link TristateDeserializer} with the Jackson 3 mapper. Exposed as a Spring bean so
 * Spring Boot 4 picks it up (it collects {@code tools.jackson.databind.JacksonModule} beans).
 */
public class TristateModule extends SimpleModule {

    public TristateModule() {
        super("TristateModule");
        addDeserializer(Tristate.class, new TristateDeserializer());
    }
}
