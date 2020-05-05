package org.jusecase.jte.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForSupportTest {
    StringBuilder result = new StringBuilder();

    @Test
    void name() {
        String[] items = {"one", "two", "three"};

        for (ForSupport<String> item : ForSupport.of(items)) {
            result.append("First: ").append(item.isFirst()).append(", Last: ").append(item.isLast()).append(", Index: ").append(item.getIndex()).append(", Item: ").append(item.get()).append('\n');
        }

        assertThat(result.toString()).isEqualTo(
                "First: true, Last: false, Index: 0, Item: one\n" +
                "First: false, Last: false, Index: 1, Item: two\n" +
                "First: false, Last: true, Index: 2, Item: three\n");
    }
}