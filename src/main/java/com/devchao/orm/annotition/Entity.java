package com.devchao.orm.annotition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.devchao.orm.enums.PkPolicy;

/**
 * 标记类为实体
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
	
	/**
	 * 映射的表名
	 */
	String tableName() default "";
	
	/**
	 * 开启本地缓存
	 */
	boolean localCache() default false;
	
	/**
	 * 开启外部缓存
	 */
	boolean extCache() default false;
	
	/**
	 * 主键策略
	 */
	PkPolicy primaryKeyPolicy() default PkPolicy.INCREMENT;
}
