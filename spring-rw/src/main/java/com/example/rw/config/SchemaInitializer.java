package com.example.rw.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 主从库各自初始化相同表结构（演示环境）。
 */
@Component
public class SchemaInitializer implements CommandLineRunner {

    private final DataSource master;
    private final DataSource slave;

    public SchemaInitializer(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {
        this.master = master;
        this.slave = slave;
    }

    @Override
    public void run(String... args) {
        var populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(master);
        populator.execute(slave);
    }
}
