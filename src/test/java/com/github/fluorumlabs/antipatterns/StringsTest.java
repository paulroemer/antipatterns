package com.github.fluorumlabs.antipatterns;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Artem Godin on 5/1/2019.
 */
public class StringsTest {
    @Test
    public void interpolate() {
        Assert.assertThat("String with no tokens is processed",
                AntiPatterns.interpolate("Abc Abc"), is("Abc Abc"));

        Assert.assertThat("String with no tokens is processed, named parameters ignored",
                AntiPatterns.interpolate("Abc Abc", number -> -5), is("Abc Abc"));

        Assert.assertThat("String with tokens is processed",
                AntiPatterns.interpolate("Text ${number}", number -> -5), is("Text -5"));

        Assert.assertThat("String with deep tokens is processed",
                AntiPatterns.interpolate("${value.simpleName}", value -> AntiPatterns.class), is("AntiPatterns"));

        Assert.assertThat("String with tokens is processed, format applied",
                AntiPatterns.interpolate("${number %(d}", number -> -5), is("(5)"));

        Assert.assertThat("String with tokens is processed, escaped token",
                AntiPatterns.interpolate("$${number %(d}"), is("${number %(d}"));

        MutableInt mutableInt = new MutableInt(0);

        Assert.assertThat("String with tokens is processed, parameters are computed only once",
                AntiPatterns.interpolate("${number %(d} ${number %(d}", number -> mutableInt.decrementAndGet()), is("(1) (1)"));
    }

}
