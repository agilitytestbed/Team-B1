package nl.utwente.ing;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import nl.utwente.ing.database.DatabaseCommunication;

import static org.springframework.boot.SpringApplication.*;

@SpringBootApplication
public class WebApp {

    public static void main(String[] args) {
        DatabaseCommunication.generateTables();
        run(WebApp.class, args);
    }
}
