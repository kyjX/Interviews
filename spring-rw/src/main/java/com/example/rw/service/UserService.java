package com.example.rw.service;

import com.example.rw.annotation.ReadOnly;
import com.example.rw.annotation.Timed;
import com.example.rw.annotation.WriteOnly;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @WriteOnly
    @Timed("createUser")
    public void createUser(long id, String name) {
        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", id, name);
    }

    @ReadOnly
    @Timed("listUsers")
    public List<Map<String, Object>> listUsers() {
        return jdbcTemplate.queryForList("SELECT id, name FROM users");
    }
}
