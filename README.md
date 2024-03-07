# job4j_grabber

## О проекте
Парсер вакансий для сайта https://career.habr.com/vacancies/java_developer.
Ищет вакансии для Java программистов и сохраняет их в базе данных.
С заданным интервал выполняет актуализацию имеющейся информации.
Интервал между запусками и параметры БД указываются в файле app.properties.

## Технологии
- Java Core
- Quartz-scheduler
- JSOUP
- JDBC (PostgreSQL)
- JUnit