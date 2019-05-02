package com.github.fluorumlabs.antipatterns;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Helper methods for working with {@link Optional}
 */
public final class Optionals {
    private Optionals() {
    } // not instantiable

    /**
     * Try suppliers sequentially until first success and return optional value
     *
     * @param suppliers Suppliers
     * @param <T>       value type
     * @return Optional of T if successfull, or Optional.empty() otherwise
     */
    @SafeVarargs
    public static <T> Optional<T> trySequentially(@Nonnull Supplier<Optional<T>>... suppliers) {
        Validate.notNull(suppliers, "suppliers must not be null");
        Validate.noNullElements(suppliers, "suppliers must not contain null elements");

        return Stream.of(suppliers)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    /**
     * Convert optional value to stream of zero or one elements
     *
     * @param value value (can be Optional.empty())
     * @param <T>   value type
     * @return stream containing this value
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Stream<T> asStream(@Nonnull Optional<T> value) {
        Validate.notNull(value, "value must not be null");

        return value.map(Stream::of).orElseGet(Stream::empty);
    }

    /**
     * Return first element of list if any
     *
     * @param list list
     * @param <T>  element type
     * @return optional first element, or empty if list is empty or first element is null
     */
    public static <T> Optional<T> getFirst(List<T> list) {
        return get(list, 0);
    }

    /**
     * Return first element of array if any
     *
     * @param array array
     * @param <T>   element type
     * @return optional first element, or empty if list is empty or first element is null
     */
    public static <T> Optional<T> getFirst(T[] array) {
        return get(array, 0);
    }

    /**
     * Return Nth element of list if any
     *
     * @param list  list
     * @param index index of element to get
     * @param <T>   element type
     * @return optional Nth element, or empty if list has not enough elements
     * or Nth element is null
     */
    public static <T> Optional<T> get(List<T> list, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index must not be negative");
        }
        if (list == null || list.size() <= index) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(list.get(index));
        }
    }

    /**
     * Return Nth element of array if any
     *
     * @param array array
     * @param index index of element to get
     * @param <T>   element type
     * @return optional Nth element, or empty if array has not enough elements
     * or Nth element is null
     */
    public static <T> Optional<T> get(T[] array, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index must not be negative");
        }
        if (array == null || array.length <= index) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(array[index]);
        }
    }

}
