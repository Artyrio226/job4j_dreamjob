package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;
    static Sql2o sql2o;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oVacancyRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        sql2o = configuration.databaseClient(datasource);

        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("DELETE FROM users WHERE 1 = 1");
            query.executeUpdate();
        }
    }

    @Test
    public void whenSaveUserThenSuccess() {
        var user = sql2oUserRepository.save(new User("user1@mail.ru", "Anton", "123")).get();
        Optional<User> userOptional = sql2oUserRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());
        assertThat(userOptional.isPresent()).isTrue();
    }

    @Test
    public void whenTryingToAddSameUserThenFailure() {
        var user = sql2oUserRepository.save(new User("user1@mail.ru", "Anton", "123")).get();
        Optional<User> userOptional = sql2oUserRepository.save(user);
        assertThat(userOptional).isEmpty();
    }

    @Test
    public void whenUserIsFound() {
        var user = sql2oUserRepository.save(new User("user1@mail.ru", "Anton", "123")).get();
        Optional<User> userOptional = sql2oUserRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());
        assertThat(userOptional.isPresent()).isTrue();
    }

    @Test
    public void whenUserIsNotFoundByEmail() {
        var user = sql2oUserRepository.save(new User("user1@mail.ru", "Anton", "123")).get();
        Optional<User> userOptional = sql2oUserRepository.findByEmailAndPassword("user2@mail.ru", user.getPassword());
        assertThat(userOptional.isEmpty()).isTrue();
    }

    @Test
    public void whenUserIsNotFoundByPassword() {
        var user = sql2oUserRepository.save(new User("user1@mail.ru", "Anton", "123")).get();
        Optional<User> userOptional = sql2oUserRepository.findByEmailAndPassword(user.getEmail(), "1r3");
        assertThat(userOptional.isEmpty()).isTrue();
    }
}