package ru.job4j.grabber;

import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        String query = "INSERT INTO post(name, text, link, created) VALUES(?,?,?,?)"
                + "ON CONFLICT(link) DO UPDATE SET name = EXCLUDED.name, text = EXCLUDED.text, created = EXCLUDED.created";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(new Post(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            resultSet.getTimestamp("created").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = new Post(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getString("text"),
                            resultSet.getTimestamp("created").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream stream = PsqlStore.class
                .getClassLoader()
                .getResourceAsStream("db/rabbit.properties")) {
            config.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PsqlStore base = new PsqlStore(config);
        base.save(new Post(1, "Developer", "/link/varvar", "Подробнее тут...", LocalDateTime.now()));
        base.save(new Post(2, "Java Developer", "/link/varvar/var", "Нет описания", LocalDateTime.now()));
        base.save(new Post(3, "Java", "/link...", "Подробнее тут...", LocalDateTime.now()));
        base.save(new Post(4, "Java", "/link/varvar", "Описание...", LocalDateTime.of(2024, 12, 12, 12, 12, 12)));
        System.out.println(base.findById(4));
        System.out.println(base.findById(3));
        System.out.println(base.getAll());
    }
}
