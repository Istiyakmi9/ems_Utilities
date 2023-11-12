package com.bot.utilities.service;

import com.bot.utilities.utils.Template;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DbManager {

    @Autowired
    DbUtils dbUtils;

    @Autowired
    ObjectMapper mapper;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    DbManager(Template template) {
        jdbcTemplate = template.getTemplate();
    }

    public <T> void save(T instance) throws Exception {
        String query = dbUtils.save(instance);
        jdbcTemplate.execute(query);
    }

    public <T> List<T> get(Class<T> type) throws Exception {
        String query = dbUtils.get(type);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

        List<T> emps = mapper.convertValue(result, new TypeReference<List<T>>() {});
        return emps;
    }

    public <T> T getById(T instance, Class<T> type) throws Exception {
        String query = dbUtils.getById(type);

        T emp = jdbcTemplate.queryForObject(query, type);

        List<Map<String, Object>> result = jdbcTemplate.queryForList(query);

        T emps = mapper.convertValue(result, new TypeReference<T>() {});
        return emps;
    }
}
