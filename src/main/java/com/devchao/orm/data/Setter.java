package com.devchao.orm.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.devchao.orm.utils.StringUtils;

/**
 * 封装set方法
 */
public class Setter {
	private Method method;
	private String fieldName;
	private String columnName;
	private Class<?> fieldType;
	private Getter getter;

	public Setter(Method method, String fieldName) {
		this.method = method;
		this.fieldName = fieldName;
		this.columnName = fieldName;
		this.fieldType = method.getParameterTypes()[0];
	}

	public Setter(Method method, String fieldName, String columnName) {
		this.method = method;
		this.fieldName = fieldName;
		this.columnName = columnName;
		this.fieldType = method.getParameterTypes()[0];
	}
	
	static Date parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        boolean isAllNumber = true;
        for (int i = 0, c = value.length(); i < c; ++i) {
            char ch = value.charAt(i);
            if (ch < '0' || ch > '9') {
                isAllNumber = false;
                break;
            }
        }
        
        if (isAllNumber) {
            return new Date(Long.parseLong(value));
        }

        if (value.length() == 10) {
            return java.sql.Date.valueOf(value);
        }

        if (value.length() == 19) {
            return java.sql.Timestamp.valueOf(value);
        }
        return null;
    }
	
	public boolean isEmpty(String str) {
		return StringUtils.isBlank(str);
	}

	public void set(Object obj, ResultSet rs) throws SQLException {
        try {
    		if (fieldType == BigInteger.class) {
        		BigDecimal bigDecimal = rs.getBigDecimal(columnName);
        		BigInteger bigInteger = bigDecimal == null ? null : bigDecimal.toBigInteger();
        		method.invoke(obj, bigInteger);
        	} else if (fieldType == BigDecimal.class) {
                method.invoke(obj, rs.getBigDecimal(columnName));
            } else if (fieldType == String.class) {
                method.invoke(obj, rs.getString(columnName));
            } else if (fieldType == long.class || fieldType == Long.class) {
            	Object o = rs.getObject(columnName);
            	Long val = null;
            	if (o != null) {
            		val = Long.parseLong(o + "");
            	}
                method.invoke(obj, val);
            } else if (fieldType == int.class || fieldType == Integer.class) {
            	Object o = rs.getObject(columnName);
            	Integer val = null;
            	if (o != null) {
            		val = Integer.parseInt(o + "");
            	}
                method.invoke(obj, val);
            } else if (fieldType == Date.class) {
                Timestamp ts = rs.getTimestamp(columnName);
                if (ts != null) {
                    method.invoke(obj, new Date(ts.getTime()));
                } else {
                    method.invoke(obj, ts);
                }
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            	Object o = rs.getObject(columnName);
            	Boolean val = null;
            	if (o != null) {
            		val = Boolean.parseBoolean(o + "");
            	}
                method.invoke(obj, val);
            } else if (fieldType == double.class || fieldType == Double.class) {
            	Object o = rs.getObject(columnName);
            	Double val = null;
            	if (o != null) {
            		val = Double.parseDouble(o + "");
            	}
                method.invoke(obj, val);
            } else if (fieldType == byte.class || fieldType == Byte.class) {
            	Object o = rs.getObject(columnName);
            	Byte val = null;
            	if (o != null) {
            		val = Byte.parseByte(o + "");
            	}
                method.invoke(obj, val);
            } else if (fieldType == short.class || fieldType == Short.class) {
            	Object o = rs.getObject(columnName);
            	Short val = null;
            	if (o != null) {
            		val = Short.parseShort(o + "");
            	}
                method.invoke(obj, val);
            } else if (fieldType == float.class || fieldType == Float.class) {
            	Object o = rs.getObject(columnName);
            	Float val = null;
            	if (o != null) {
            		val = Float.parseFloat(o + "");
            	}
                method.invoke(obj, val);
            } else {
                throw new RuntimeException(fieldType + " is not supported! only support: String, long, int, java.util.Date, boolean.");
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
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

	public Class<?> getFieldType() {
		return fieldType;
	}

	public void setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
	}

	public Getter getGetter() {
		return getter;
	}

	public void setGetter(Getter getter) {
		this.getter = getter;
	}

}
