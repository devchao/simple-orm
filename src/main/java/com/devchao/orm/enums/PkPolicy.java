package com.devchao.orm.enums;

/**
 * 主键策略
 */
public enum PkPolicy {
	
	/**
	 * 数据库自增
	 */
	INCREMENT, 
	
	/**
	 * twitter主键算法
	 */
	TWITTER, 
	
	/**
	 * 用户提供的算法
	 */
	ONMYOWN
}
