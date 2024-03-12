package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descElement = document.select(".vacancy-description__text").first();
        return descElement.text();
    }

    private Post fillPost(Element row, String link) throws IOException {
        Post post = new Post();
        Element dateElement = row.select(".vacancy-card__date").first();
        Element dateInfoChild = dateElement.child(0);
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        post.setCreated(dateTimeParser.parse(dateInfoChild.attr("datetime")));
        post.setTitle(titleElement.text());
        post.setLink(String.format("%s%s", link, linkElement.attr("href")));
        post.setDescription(retrieveDescription(post.getLink()));
        return post;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            for (int pageNumber = 1; pageNumber <= 5; pageNumber++) {
                String fullLink = "%s%s%d%s".formatted(link, PREFIX, pageNumber, SUFFIX);
                Connection connection = Jsoup.connect(fullLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    try {
                        posts.add(fillPost(row, link));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posts;
    }
}