package com.example.rw.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 根据 ThreadLocal 中的类型动态选择主库或从库。
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.get();
        return type != null ? type.name() : DataSourceType.MASTER.name();
    }
}
