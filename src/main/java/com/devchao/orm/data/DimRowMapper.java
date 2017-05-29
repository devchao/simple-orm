package com.devchao.orm.data;

import java.sql.ResultSet;

import org.springframework.jdbc.core.RowMapper;

public class DimRowMapper<T> implements RowMapper<T> {
	
	private Class<T> type;
	private ORM orm;

	public DimRowMapper(Class<T> type, ORM orm) {
		this.type = type;
		this.orm = orm;
	}

	@Override
	public T mapRow(ResultSet rs, int i) {
		return orm.map(type, rs);
	}
}
