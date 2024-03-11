package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descElement = document.select(".vacancy-description__text").first();
        return descElement.text();
    }

    public static void main(String[] args) throws IOException {
        for (int pageNumber = 1; pageNumber <= 5; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateInfoChild = dateElement.child(0);
                HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
                LocalDateTime dateTime = dateTimeParser.parse(dateInfoChild.attr("datetime"));

                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s [from: %s]%n", vacancyName, link,
                        dateTime.format(DateTimeFormatter.ofPattern("yyyy-mm-ddТhh:mm:ss")));
                try {
                    System.out.println(retrieveDescription(link));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}