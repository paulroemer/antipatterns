package com.github.fluorumlabs.antipatterns;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Artem Godin on 5/1/2019.
 */
public class BuildersTest {

    @SuppressWarnings("StandardVariableNames")
    @Test
    public void hashMap() {
        Assert.assertThat("Empty map is constructed",
                Builders.hashMap().isEmpty(), is(true));

        Map<String, Integer> map = Builders.hashMap(a -> 1, b -> 2);

        Assert.assertThat("Map contains value for 'a'",
                map.get("a"), is(1));

        Assert.assertThat("Map contains value for 'b'",
                map.get("b"), is(2));
    }

}
