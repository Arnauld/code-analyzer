package coda.util;

import org.junit.Test;

import static coda.util.CamelCaseSplitter.split;
import static org.assertj.core.api.Assertions.assertThat;

public class CamelCaseSplitterTest {

    @Test
    public void should_split_camelCaseWord_into_words() {
        assertThat(split("camelCaseSplitter")).containsExactly("camel", "Case", "Splitter");
    }

    @Test
    public void should_split_camelCaseWord_into_words_even_when_starting_with_upper_case() {
        assertThat(split("CamelCaseSplitter")).containsExactly("Camel", "Case", "Splitter");
    }

    @Test
    public void should_split_camelCaseWord_into_words_considering_uppercased_word_as_a_word() {
        assertThat(split("CamelCASESplitter")).containsExactly("Camel", "CASE", "Splitter");
    }

}