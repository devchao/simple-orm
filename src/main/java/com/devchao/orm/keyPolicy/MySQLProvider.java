package com.devchao.orm.keyPolicy;

public class MySQLProvider implements KeyPolicy {

	@Override
	public String nextVal() {
		return "0";
	}

	@Override
	public Class<?> getKeyType() {
		return Long.class;
	}

}
