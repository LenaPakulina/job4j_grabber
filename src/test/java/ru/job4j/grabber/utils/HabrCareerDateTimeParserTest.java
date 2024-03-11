package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    @Test
    void parse() {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        String textDate = "2024-03-06T14:05:04+03:00";
        LocalDateTime ex = LocalDateTime.of(2024, Month.MARCH, 6, 14, 5, 4);
        assertThat(ex).isEqualTo(parser.parse(textDate));
    }
}