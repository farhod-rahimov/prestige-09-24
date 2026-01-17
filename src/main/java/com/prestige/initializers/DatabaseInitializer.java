package com.prestige.initializers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Transactional
    public void initializeDatabase() {

        try {
            executeSqlScript("classpath:sql/schema.sql");
        }
        catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void executeSqlScript(String scriptPath) {

        try {
            Resource resource = resourceLoader.getResource(scriptPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String scriptContent = reader.lines().collect(Collectors.joining("\n"));
            jdbcTemplate.execute(scriptContent);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении SQL-скрипта: " + scriptPath, e);
        }
    }
}
