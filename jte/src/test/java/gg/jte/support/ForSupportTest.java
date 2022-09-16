package gg.jte.support;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForSupportTest {
    StringBuilder result = new StringBuilder();

    @Test
    void array() {
        String[] items = {"one", "two", "three"};

        whenIteratingOverArray(items);

        assertThat(result.toString()).isEqualTo(
                "First: true, Last: false, Index: 0, Item: one\n" +
                "First: false, Last: false, Index: 1, Item: two\n" +
                "First: false, Last: true, Index: 2, Item: three\n");
    }

    @Test
    void iterable() {
        List<String> items = Arrays.asList("one", "two", "three");

        whenIteratingOverIterable(items);

        assertThat(result.toString()).isEqualTo(
                "First: true, Last: false, Index: 0, Item: one\n" +
                "First: false, Last: false, Index: 1, Item: two\n" +
                "First: false, Last: true, Index: 2, Item: three\n");
    }

    private void whenIteratingOverArray(String[] items) {
        for (ForSupport<String> item : ForSupport.of(items)) {
            result.append("First: ").append(item.isFirst()).append(", Last: ").append(item.isLast()).append(", Index: ").append(item.getIndex()).append(", Item: ").append(item.get()).append('\n');
        }
    }

    private void whenIteratingOverIterable(Iterable<String> items) {
        for (ForSupport<String> item : ForSupport.of(items)) {
            result.append("First: ").append(item.isFirst()).append(", Last: ").append(item.isLast()).append(", Index: ").append(item.getIndex()).append(", Item: ").append(item.get()).append('\n');
        }
    }
}