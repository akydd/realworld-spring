package com.akydd.realworld_spring.json;

import java.util.Objects;

/**
 * A three-state value for request bodies where a field can be:
 * <ul>
 *     <li><b>undefined</b> - the field was absent from the JSON, so leave the existing value alone;</li>
 *     <li><b>present and null</b> - the field was sent as {@code null}, so clear the value;</li>
 *     <li><b>present with a value</b> - the field was sent with a value, so set it.</li>
 * </ul>
 * <p>
 * This replaces {@code org.openapitools.jackson.nullable.JsonNullable}, which only ships a Jackson 2
 * module and therefore does not register against the Jackson 3 mapper used by Spring Boot 4.
 * See {@link TristateDeserializer} for how the three states are produced during deserialization.
 */
public final class Tristate<T> {

    private static final Tristate<?> UNDEFINED = new Tristate<>(false, null);

    private final boolean present;
    private final T value;

    private Tristate(boolean present, T value) {
        this.present = present;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> Tristate<T> undefined() {
        return (Tristate<T>) UNDEFINED;
    }

    public static <T> Tristate<T> of(T value) {
        return new Tristate<>(true, value);
    }

    /**
     * True when the field was present in the JSON (even if its value was null).
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * The submitted value; null when the field was present-and-null (i.e. a clear).
     */
    public T get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tristate<?> other)) return false;
        return present == other.present && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(present, value);
    }

    @Override
    public String toString() {
        return present ? "Tristate[" + value + "]" : "Tristate.undefined";
    }
}
