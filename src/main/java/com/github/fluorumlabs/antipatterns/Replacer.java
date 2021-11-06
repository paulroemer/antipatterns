package com.github.fluorumlabs.antipatterns;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Functional interface for computing replacement string for {@link AntiPatterns#replaceFunctional(Pattern, String, Replacer)}
 */
@FunctionalInterface
public interface Replacer {
    /**
     * Compute replacement
     *
     * @param groups Matcher groups with {@code group(0)} as a first element
     * @return Optional replacement string
     */
    Optional<String> getReplacement(String... groups);
}
