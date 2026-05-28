package com.example.rw.demo;

import com.example.rw.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final UserService userService;

    public DemoRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.createUser(1L, "Alice");
        userService.createUser(2L, "Bob");
        var users = userService.listUsers();
        log.info("Users from read datasource: {}", users);
    }
}
