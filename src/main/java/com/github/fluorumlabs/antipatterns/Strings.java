package com.github.fluorumlabs.antipatterns;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.fluorumlabs.antipatterns.Builders.hashMap;

/**
 * Helper methods for working strings
 */
public final class Strings {
    private Strings() {
    } // not instantiable

    private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("([\\$]{1,2})\\{([a-zA-Z0-9_.]+)(\\s%%([^}]+))?\\}");

    /**
     * String replacement in a functional style. {@code replacement} is a function,
     * whose argument is an array of Strings containing mathed groups (with {@code group(0)}
     * as a first element), and returning String to replace current matched occurence.
     * <p>
     * If {@code replacer} returns {@link Optional#empty()}, no replacement will occur.
     *
     * @param pattern  compiled pattern
     * @param input    input string
     * @param replacer function mapping array of groups to replacement string
     * @return replacement result
     */
    @Nonnull
    public static String replaceFunctional(@Nonnull Pattern pattern, @Nonnull String input, @Nonnull Replacer replacer) {
        Validate.notNull(input, "input must not be null");
        Validate.notNull(pattern, "pattern must not be null");
        Validate.notNull(replacer, "replacer must not be null");

        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int groupCount = matcher.groupCount() + 1;
            String[] groups = new String[groupCount];
            for (int i = 0; i < groupCount; i++) {
                groups[i] = matcher.group(i);
            }
            replacer.getReplacement(groups)
                    .ifPresent(replacement -> matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement)));
        }
        return matcher.appendTail(sb).toString();
    }

    /**
     * Return iterator over array of Strings containing matched groups, with
     * {@code group(0)} as a first element.
     *
     * @param pattern compiled pattern
     * @param input   input string
     * @return iterator
     */
    @Nonnull
    public static Iterator<String[]> matchIterator(@Nonnull Pattern pattern, @Nonnull String input) {
        Validate.notNull(pattern, "pattern must not be null");
        Validate.notNull(input, "input must not be null");

        return new Strings.MatcherIterator(pattern, input);
    }

    /**
     * Return stream of matched groups
     *
     * @param pattern compiled pattern
     * @param input   input string
     * @return stream of array of strings, containing matched groups
     */
    @Nonnull
    public static Stream<String[]> matchAsStream(@Nonnull Pattern pattern, @Nonnull String input) {
        Validate.notNull(pattern, "pattern must not be null");
        Validate.notNull(input, "input must not be null");

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                matchIterator(pattern, input), Spliterator.ORDERED | Spliterator.NONNULL), false);

    }

    /**
     * Functional interface for computing replacement string for {@link Strings#replaceFunctional(Pattern, String, Replacer)}
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

    /**
     * Iterator for RegExp matches
     */
    private static class MatcherIterator implements Iterator<String[]> {
        private final Matcher matcher;
        private boolean hasNonConsumedMatch = false;

        private MatcherIterator(Pattern pattern, String input) {
            this.matcher = pattern.matcher(input);
        }

        public String[] next() {
            if (!hasNext())
                throw new NoSuchElementException();

            int groupCount = matcher.groupCount() + 1;
            String[] groups = new String[groupCount];
            for (int i = 0; i < groupCount; i++) {
                groups[i] = matcher.group(i);
            }

            return groups;
        }

        public boolean hasNext() {
            return hasNonConsumedMatch || (hasNonConsumedMatch = matcher.find());
        }
    }


    /**
     * Perform string interpolation (with benefits).
     * <p>
     * It's possible to specify formatting rules after space (all String.format specifiers are allowed); default is
     * to use %s. Tokens can be a deep reference for inner objects; it is possible to access value.property via
     * ${value.property} syntax.
     * <p>
     * If token can be escaped by putting double '$' signs: "$${ignoredToken}" will be shown as "${ignoredToken}"
     * without any errors.
     * <p>
     * Usage:
     * {@code String result = interpolate("File '${fileName}' cannot be found", fileName -> file.getPath()); }
     * <p>
     * {@code String result = interpolate("Found ${entries %10d} entries", fileName -> entries.length; }
     *
     * @param format        template
     * @param keyValuePairs parameters
     * @return formatted string
     */
    @SafeVarargs
    public static String interpolate(@Nonnull String format, Classes.NamedValue<Object>... keyValuePairs) {
        Validate.notNull(format, "format must not be null");
        Validate.noNullElements(keyValuePairs, "keyValuePairs must not contain null elements");

        // Parameter indices
        Map<String, Object> parameters = hashMap(keyValuePairs);
        Map<String, Integer> paramIndices = new HashMap<>(); // parameter indices
        List<Object> values = new ArrayList<>();

        // Process format string
        String metaFormat = replaceFunctional(INTERPOLATION_PATTERN, StringUtils.replace(format, "%", "%%"), matches -> {
            if (matches[1].length() > 1) {
                // Meaning we have '$$' in the beginning -- just dump the token as is
                return Optional.of("${" + matches[2] + StringUtils.defaultString(matches[3], "") + "}");
            } else {
                // We have a token, with or without extra formatting
                String key = matches[2];
                String fmt = StringUtils.defaultString(matches[4], "s");

                if (paramIndices.containsKey(key)) {
                    // We already seen that key
                    return Optional.of("%" + paramIndices.get(key) + "$" + fmt);
                } else {
                    // Not seen yet
                    Object value = null;
                    try {
                        value = PropertyUtils.getNestedProperty(parameters, key);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new IllegalArgumentException(String.format("Cannot get value for token %s", key), e);
                    }

                    int index = values.size() + 1;
                    paramIndices.put(key, index);
                    values.add(value);

                    return Optional.of("%" + index + "$" + fmt);
                }

            }
        });

        // kthxbye
        return String.format(metaFormat, values.toArray());
    }

}
