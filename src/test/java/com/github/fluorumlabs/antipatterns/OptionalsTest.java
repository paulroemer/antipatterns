package com.github.fluorumlabs.antipatterns;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Artem Godin on 5/1/2019.
 */
public class OptionalsTest {
    @Test
    public void trySequentially() {
        Assert.assertThat("Result is empty if no suppliers are specified",
                AntiPatterns.<String>trySequentially().isPresent(), is(false));

        Assert.assertThat("Result is empty if no suppliers return values",
                AntiPatterns.<String>trySequentially(Optional::empty).isPresent(), is(false));

        Assert.assertThat("Result is not empty for one supplier",
                AntiPatterns.trySequentially(
                        () -> Optional.of("abc")
                ).orElse(null), is("abc"));

        Assert.assertThat("Result is not empty for two suppliers",
                AntiPatterns.trySequentially(
                        () -> Optional.of("abc"),
                        () -> Optional.of("def")
                ).orElse(null), is("abc"));

        Assert.assertThat("Result is not empty for three suppliers",
                AntiPatterns.trySequentially(
                        Optional::empty,
                        () -> Optional.of("abc"),
                        () -> Optional.of("def")
                ).orElse(null), is("abc"));
    }

    @Test
    public void getFirst() {
        Assert.assertThat("Result is empty if List is empty",
                AntiPatterns.getFirst(Collections.emptyList()).isPresent(), is(false));

        Assert.assertThat("Result is first element if List has one element",
                AntiPatterns.getFirst(Collections.singletonList("abc")).orElse(null), is("abc"));

        Assert.assertThat("Result is first element if List has two element",
                AntiPatterns.getFirst(Arrays.asList("abc", "def")).orElse(null), is("abc"));

        Assert.assertThat("Result is first element if List has three element",
                AntiPatterns.getFirst(Arrays.asList("abc", "def", "ghi")).orElse(null), is("abc"));
    }

    @Test
    public void getFirstArray() {
        Assert.assertThat("Result is empty if array is empty",
                AntiPatterns.getFirst(AntiPatterns.array()).isPresent(), is(false));

        Assert.assertThat("Result is first element if array has one element",
                AntiPatterns.getFirst(AntiPatterns.array("abc")).orElse(null), is("abc"));

        Assert.assertThat("Result is first element if array has two element",
                AntiPatterns.getFirst(AntiPatterns.array("abc", "def")).orElse(null), is("abc"));

        Assert.assertThat("Result is first element if array has two element",
                AntiPatterns.getFirst(AntiPatterns.array("abc", "def", "ghi")).orElse(null), is("abc"));
    }

    @Test
    public void get() {
        Assert.assertThat("Result is empty if List is empty",
                AntiPatterns.get(Collections.emptyList(), 1).isPresent(), is(false));

        Assert.assertThat("Result is second element if List has one element",
                AntiPatterns.get(Collections.singletonList("abc"), 1).isPresent(), is(false));

        Assert.assertThat("Result is second element if List has two element",
                AntiPatterns.get(Arrays.asList("abc", "def"), 1).orElse(null), is("def"));

        Assert.assertThat("Result is second element if List has three element",
                AntiPatterns.get(Arrays.asList("abc", "def", "ghi"), 1).orElse(null), is("def"));
    }

    @Test
    public void getArray() {
        Assert.assertThat("Result is empty if array is empty",
                AntiPatterns.get(AntiPatterns.array(), 1).isPresent(), is(false));

        Assert.assertThat("Result is second element if array has one element",
                AntiPatterns.get(AntiPatterns.array("abc"), 1).isPresent(), is(false));

        Assert.assertThat("Result is second element if array has two element",
                AntiPatterns.get(AntiPatterns.array("abc", "def"), 1).orElse(null), is("def"));

        Assert.assertThat("Result is second element if array has three element",
                AntiPatterns.get(AntiPatterns.array("abc", "def", "ghi"), 1).orElse(null), is("def"));
    }

}
