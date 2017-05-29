package com.devchao.orm.data;

import java.lang.reflect.Method;

/**
 * 封装get 方法
 */
public class Getter {
	
	private Method method;
	private String fieldName;
	private String columnName;
	private Object def; // 默认值

	public Getter(Method method, String fieldName, Object def) {
		this.method = method;
		this.fieldName = fieldName;
		this.columnName = fieldName;
		this.def =def;
	}

	public Getter(Method method, String fieldName, String columName, Object def) {
		this.method = method;
		this.fieldName = fieldName;
		this.columnName = columName;
		this.def =def;
	}

	public Object get(Object obj) {
		try {
			Object o = method.invoke(obj);
			if (o == null) {
				return def;
			}
			return o;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Object getDef() {
		return def;
	}

	public void setDef(Object def) {
		this.def = def;
	}

}
