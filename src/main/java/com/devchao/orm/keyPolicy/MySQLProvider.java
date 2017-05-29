package com.devchao.orm.keyPolicy;

public class MySQLProvider implements KeyPolicy {

	@Override
	public long nextVal() {
		return 0L;
	}
	
}
