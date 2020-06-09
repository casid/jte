package org.jusecase.jte.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForSupportTest {
    StringBuilder result = new StringBuilder();

    @Test
    void name() {
        String[] items = {"one", "two", "three"};

        for (ForSupport<String> item : ForSupport.of(items)) {
            result.append("First: ").append(item.isFirst())
                    .append(", Last: ").append(item.isLast())
                    .append(", Even: ").append(item.isEven())
                    .append(", Odd: ").append(item.isOdd())
                    .append(", Index: ").append(item.getIndex())
                    .append(", Item: ").append(item.get())
                    .append('\n');
        }

        assertThat(result.toString()).isEqualTo(
                "First: true, Last: false, Even: true, Odd: false, Index: 0, Item: one\n" +
                "First: false, Last: false, Even: false, Odd: true, Index: 1, Item: two\n" +
                "First: false, Last: true, Even: true, Odd: false, Index: 2, Item: three\n");
    }
}