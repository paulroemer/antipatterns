package com.github.fluorumlabs.antipatterns;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods for building arrays and maps
 */
public final class Builders {
    private Builders() {
    } // not instantiable

    /**
     * Create array of elements without any memory overhead
     * <p>
     * Usage (array creation):
     * {@code String[] array = Builders.array("string a", "string b"); }
     * <p>
     * Usage (List.toArray)
     * {@code String[] array = listOfString.toArray(Builders.array()); }
     *
     * @param args elements
     * @param <T>  element type
     * @return array of elements, never null
     */
    @SafeVarargs
    public static <T> T[] array(T... args) {
        return args;
    }

    /**
     * Construct new HashMap.
     * <p>
     * Usage example:
     * {@code Map<String,String> map = hashMap(name -> "vaadin-board", version -> "1.3.0");}
     * <p>
     * CAUTION: requires `-parameters` option for `javac`
     *
     * @param <T>           map value type
     * @param keyValuePairs key-value pairs
     * @return Map
     */
    @SafeVarargs
    public static <T> Map<String, T> hashMap(Classes.NamedValue<T>... keyValuePairs) {
        Validate.noNullElements(keyValuePairs, "keyValuePairs must not contain null elements");
        Map<String, T> map = new HashMap<>(keyValuePairs.length);

        for (Classes.NamedValue<T> keyValuePair : keyValuePairs) {
            String name = keyValuePair.name();
            T value = keyValuePair.value(name);
            map.put(name, value);
        }

        return map;
    }

}
