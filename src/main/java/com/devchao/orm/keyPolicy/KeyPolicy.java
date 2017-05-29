package com.devchao.orm.keyPolicy;

/**
 * 主键生成策略规范
 */
public interface KeyPolicy {
	
	/**
	 * 主键的值
	 */
	long nextVal();
	
}
