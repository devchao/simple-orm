package com.devchao.orm.keyPolicy;

/**
 * 主键生成策略规范
 */
public interface KeyPolicy {
	
	/**
	 * 主键的值
	 */
	String nextVal();
	
	/**
	 * 主键值的数据类型：支持Long和String
	 */
	Class<?> getKeyType();
	
}
