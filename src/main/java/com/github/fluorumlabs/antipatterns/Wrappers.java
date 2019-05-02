package com.github.fluorumlabs.antipatterns;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility wrappers for {@link Supplier}, {@link Runnable} and {@link Function}
 */
public final class Wrappers {
    private Wrappers() {
    } // not instantiable

    /**
     * Call supplier, catch RuntimeException and return empty optional
     * <p>
     * Example usage:
     * {@code Wrappers.guarded(() -> Integer.parse("234234j")).ifPresent(...);}
     *
     * @param supplier supplier of T
     * @param <T>      result type
     * @return Optional of {@code T}
     */
    public static <T> Optional<T> guarded(@Nonnull Supplier<T> supplier) {
        Validate.notNull(supplier, "supplier must not be null");

        try {
            return Optional.of(supplier.get());
        } catch (RuntimeException ignore) {
            return Optional.empty();
        }
    }

    /**
     * Call supplier, catch RuntimeException and return empty optional
     * <p>
     * Example usage:
     * {@code Wrappers.guarded(() -> Optional.of(Integer.parse("234234j"))).ifPresent(...);}
     *
     * @param supplier supplier of {@link Optional} of {@code T}
     * @param <T>      result type
     * @return Optionsl of {@code T}
     */
    public static <T> Optional<T> guardedOptional(@Nonnull Supplier<Optional<T>> supplier) {
        Validate.notNull(supplier, "supplier must not be null");

        try {
            return supplier.get();
        } catch (RuntimeException ignore) {
            return Optional.empty();
        }
    }

    /**
     * Call runnable, catch RuntimeException and return
     * <p>
     * Example usage:
     * {@code Wrappers.guarded(() -> discussionService.incrementViewCount(root));}
     *
     * @param runnable runnable to execute
     */
    public static void guarded(@Nonnull Runnable runnable) {
        Validate.notNull(runnable, "runnable must not be null");

        try {
            runnable.run();
        } catch (RuntimeException ignore) {
            // ignore
        }
    }

    /**
     * Wrap function with a try..catch block and return null if RuntimeException occurred
     * <p>
     * Example usage:
     * {@code randomOptional.map(Wrappers.guarded(id -> Integer.parse(id)).ifPresent(...);}
     *
     * @param mapper function to wrap
     * @param <T>    function argument type
     * @param <U>    function result type
     * @return wrapped function
     */
    public static <T, U> Function<T, U> guarded(@Nonnull Function<T, U> mapper) {
        Validate.notNull(mapper, "mapper must not be null");

        return x -> {
            try {
                return mapper.apply(x);
            } catch (RuntimeException ignore) {
                return null;
            }
        };
    }
}
