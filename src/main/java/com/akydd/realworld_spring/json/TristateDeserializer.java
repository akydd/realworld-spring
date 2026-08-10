package com.akydd.realworld_spring.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Produces the three {@link Tristate} states from Jackson 3's parsing callbacks:
 * <ul>
 *     <li>a present value token -> {@link #deserialize} -> {@code Tristate.of(value)};</li>
 *     <li>an explicit JSON null -> {@link #getNullValue} -> {@code Tristate.of(null)} (a clear);</li>
 *     <li>an absent property     -> {@link #getAbsentValue} -> {@code Tristate.undefined()}.</li>
 * </ul>
 * The absent/null distinction is the whole point - a plain nullable String cannot express it.
 */
@SuppressWarnings("rawtypes")
public class TristateDeserializer extends ValueDeserializer<Tristate> {

    @Override
    public Tristate deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return Tristate.of(p.getValueAsString());
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) {
        return Tristate.of(null);
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return Tristate.undefined();
    }
}
