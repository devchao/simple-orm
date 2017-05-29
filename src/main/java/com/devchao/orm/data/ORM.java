package com.devchao.orm.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import com.devchao.orm.annotition.Column;
import com.devchao.orm.annotition.Entity;
import com.devchao.orm.annotition.Key;
import com.devchao.orm.utils.StringUtils;

//TODO
public class ORM {
	
	private String tablePrefix = "t_";

	/**
	 * 收集表
	 */
	private Map<Class<?>, String> tableNameMap = new HashMap<Class<?>, String>();

	/**
	 * 收集set和get方法
	 */
	private Map<Class<?>, List<Setter>> setterMap = new HashMap<Class<?>, List<Setter>>();
	private Map<Class<?>, List<Getter>> getterMap = new HashMap<Class<?>, List<Getter>>();
	private Map<Class<?>, Map<String, Setter>> entityColumnSetterMap = new HashMap<Class<?>, Map<String,Setter>>();

	/**
	 * 收集字段和属性
	 */
	private Map<String, String> field2columnMap = new HashMap<String, String>();

	/**
	 * 收集实体
	 */
	private Map<Class<?>, Integer> entityMap = new HashMap<Class<?>, Integer>();

	/**
	 * 收集@Key
	 */
	private Map<Class<?>, String> keyColumnMap = new HashMap<Class<?>, String>();
	private Map<Class<?>, String> keyFieldMap = new HashMap<Class<?>, String>();

	/**
	 * 收集key method
	 */
	private Map<Class<?>, Method> getKeyMethodMap = new HashMap<Class<?>, Method>();
	private Map<Class<?>, Method> setKeyMethodMap = new HashMap<Class<?>, Method>();

	/**
	 * 收集缓存
	 */
	private Map<Class<?>, boolean[]> cacheMap = new HashMap<Class<?>, boolean[]>();

	/**
	 * 收集sql
	 */
	private Map<Class<?>, String> createSqlMap = new HashMap<Class<?>, String>();
	private Map<Class<?>, String> selectSqlMap = new HashMap<Class<?>, String>();
	private Map<Class<?>, String> deleteSqlMap = new HashMap<Class<?>, String>();
	private Map<Class<?>, String> updateSqlMap = new HashMap<Class<?>, String>();
	private Map<Class<?>, String> existsSqlMap = new HashMap<Class<?>, String>();
	private Map<String, String> updateSqlFieldsMap = new ConcurrentHashMap<String, String>();

	public ORM() {
	}

	public ORM(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	/**
	 * spring初始化ORM时执行入口
	 * @param entityPackageList
	 */
	public void setEntityPackageList(List<String> entityPackageList) {
		if (entityPackageList != null && !entityPackageList.isEmpty()) {
			for (String entityPackage : entityPackageList) {
				setEntitiyPackage(entityPackage);
			}
		}
	}
	
	//TODO
	public static boolean isSupportedProperty(Class<?> type) {
		if (type == String.class || type == long.class || type == int.class
				|| type == Date.class || type == boolean.class
				|| type == double.class || type == long[].class
				|| type == Long.class || type == Integer.class
				|| type == Boolean.class || type == Double.class
				|| type == byte.class || type == short.class
				|| type == float.class || type == Byte.class
				|| type == Short.class || type == Float.class || type == BigDecimal.class || type == BigInteger.class) {
			return true;
		}
		return false;
	}

	public void setEntityList(List<Class<?>> types) {
		if (types != null && !types.isEmpty()) {
			for (Class<?> type : types) {
				registerEntity(type);
			}
		}
	}

	/**
	 * 根据包路径读取被@Entity标注的类
	 * 
	 * @param entityPackage
	 */
	public void setEntitiyPackage(String entityPackage) {
		// if is true, it will scan the @Component, @Service 类, but we only get @Entity, it set false;
		ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
		scan.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (BeanDefinition candidate : scan.findCandidateComponents(entityPackage)) {
			try {
				Class<?> cls = ClassUtils.resolveClassName(candidate.getBeanClassName(), ClassUtils.getDefaultClassLoader());
				list.add(cls);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
		setEntityList(list);
	}

	public void registerEntity(Class<?> type) {
		if (type == null) {
			return;
		}
		prepareCacheMap(type);
		prepareKeyMap(type);
		prepareSetterGetterMap(type);
		prepareTableMap(type);
		prepareGetKeyMethodMap(type);
		prepareSetKeyMethodMap(type);

		prepareCreateSqlMap(type);
		prepareUpdateSqlMap(type);
		prepareDeleteSqlMap(type);
		prepareSelectSqlMap(type);
		entityMap.put(type, 1);
	}

	private void prepareSelectSqlMap(Class<?> type) {
		StringBuilder buf = new StringBuilder();
		buf.append("SELECT * FROM ").append(tableNameMap.get(type));
		String key = keyColumnMap.get(type);
		buf.append(" WHERE ").append(key).append("=?");
		selectSqlMap.put(type, buf.toString());
		existsSqlMap.put(type, buf.toString().replace("*", "count(*)"));
	}

	private void prepareDeleteSqlMap(Class<?> type) {
		StringBuilder buf = new StringBuilder();
		buf.append("DELETE FROM ").append(tableNameMap.get(type));

		String key = keyColumnMap.get(type);
		buf.append(" WHERE ").append(key).append("=?");
		deleteSqlMap.put(type, buf.toString());
	}

	private void prepareUpdateSqlMap(Class<?> type) {
		String key = keyColumnMap.get(type);
		StringBuilder buf = new StringBuilder();
		buf.append("update ").append(tableNameMap.get(type)).append(" set ");
		 
		for (Setter setter : setterMap.get(type)) {
			String columnName = setter.getColumnName();
			if (!columnName.equalsIgnoreCase(key)) {
				buf.append(columnName).append("=:").append(columnName).append(',');
			}
		}
		buf.setCharAt(buf.length() - 1, ' ');
		buf.append("where ").append(key).append("=:").append(key);

		updateSqlMap.put(type, buf.toString());
	}

	private void prepareCreateSqlMap(Class<?> type) {
		StringBuilder buf = new StringBuilder();
		buf.append("INSERT INTO ").append(tableNameMap.get(type)).append(" (");
		for (Setter setter : setterMap.get(type)) {
			buf.append(setter.getColumnName()).append(',');
		}
		buf.setCharAt(buf.length() - 1, ')');
		buf.append(" values (");
		for (Setter setter : setterMap.get(type)) {
			buf.append(':').append(setter.getColumnName()).append(',');
		}
		buf.setCharAt(buf.length() - 1, ')');
		createSqlMap.put(type, buf.toString());
	}

	private void prepareSetKeyMethodMap(Class<?> type) {
		String keyField = keyFieldMap.get(type);
		try {
			Method set = setter(type, type.getDeclaredField(keyField));
			setKeyMethodMap.put(type, set);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private void prepareGetKeyMethodMap(Class<?> type) {
		String keyField = keyFieldMap.get(type);
		try {
			Method get = getter(type, type.getDeclaredField(keyField));
			getKeyMethodMap.put(type, get);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private void prepareKeyMap(Class<?> type) {
		if (type == null) {
			return;
		}
		Field[] fields = type.getDeclaredFields();
		List<Field> keys = new ArrayList<Field>();
		for (Field field : fields) {
			Key keyAnnotation = field.getAnnotation(Key.class);
			if (keyAnnotation != null) {
				keys.add(field);
			}
		}

		if (keys.size() == 1) {
			Field field = keys.get(0);
			
			//判断key的返回类型是否为Long
			if(field.getType() != Long.class){
				throw new RuntimeException("Entity[" + type.getName() + "] key type must be Long!");
			}
			
			String columnName = field.getName();
			Column columnAnnotation = field.getAnnotation(Column.class);
			if (columnAnnotation != null) {
				String value = columnAnnotation.value();
				if (StringUtils.isNotBlank(value)) {
					columnName = value;
				}
			}
			keyColumnMap.put(type, columnName);
			keyFieldMap.put(type, field.getName());
		} else if(keys.size() == 0){
			throw new RuntimeException("Entity[" + type.getName() + "] has no key!");
		} else if (keys.size() > 1) {
			throw new RuntimeException("Entity[" + type.getName() + "] find more than one key!");
		}
	}

	private void prepareCacheMap(Class<?> type) {
		Entity entityAnnotation = type.getAnnotation(Entity.class);
		if (entityAnnotation != null) {
			boolean[] caches = new boolean[2];
			if (entityAnnotation.localCache()) {
				caches[0] = true;
			}
			if (entityAnnotation.extCache()) {
				caches[1] = true;
			}
			cacheMap.put(type, caches);
		}
	}

	private void prepareSetterGetterMap(Class<?> type) {
		
		if (type == null) {
			return;
		}
		
		int columnNum = 0;//column的数量，如果为0，抛出异常
		Map<String, Setter> columnSetterMap = new HashMap<String, Setter>();
		List<Setter> setterList = new ArrayList<Setter>();
		List<Getter> getterList = new ArrayList<Getter>();

		for (Field field : type.getDeclaredFields()) {
			
			int modifiers = field.getModifiers();
			 
			// 判断是否是支持的类型
			if (!isSupportedProperty(field.getType())) {
				continue;
			}
			 
			// 屏蔽 transient、static、final 修饰符
			if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
				
				// 处理字段
				Column columnAnnotation = field.getAnnotation(Column.class);
				if (columnAnnotation != null) { // 标记了Column注解的field才持久化
					
					columnNum = columnNum + 1;
					
					String fieldName = field.getName();
					String columnName = fieldName; // 默认为属性名
					
					Method set = setter(type, field);
					Method get = getter(type, field);
					
					if (StringUtils.isNotBlank(columnAnnotation.value())) {
						columnName = columnAnnotation.value();
					}
					
					// def支持空字符串(只有是String 类型才默认为空字串)
					Object def = null;
					if (columnAnnotation.def() != null) {
						def = columnAnnotation.def();
						if (field.getType() != String.class && StringUtils.isBlank(columnAnnotation.def())) {
							def = null;
						}
					}
					
					if (set != null && get != null) {
						Getter getter = new Getter(get, fieldName, columnName, def);
						Setter setter = new Setter(set, fieldName, columnName);
						setter.setGetter(getter);
	
						getterList.add(getter);
						setterList.add(setter);
						
						field2columnMap.put(type.getName() + "." + fieldName, columnName);
						columnSetterMap.put(columnName, setter);
					}
				} 
			}
		}
		
		//判断Entity是否有Column
		if(columnNum == 0){
			throw new RuntimeException("Entity[" + type.getName() + "] has no column!");
		}
		
		setterMap.put(type, setterList);
		getterMap.put(type, getterList);
		entityColumnSetterMap.put(type, columnSetterMap);
	}

	/**
	 * 获取get方法
	 * 
	 * @param type
	 * @param field
	 * @return
	 */
	private static Method getter(Class<?> type, Field field) {
		String fieldName = field.getName();
		String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		Method result = null;
		try {
			result = type.getMethod(methodName);
		} catch (NoSuchMethodException ex) { // isXxx 方法
			if (field.getType() == boolean.class || field.getType() == Boolean.class) {
				methodName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				try {
					result = type.getMethod(methodName);
				} catch (NoSuchMethodException ex1) {
					throw new RuntimeException(ex1);
				}
			} else {
				throw new RuntimeException(ex);
			}
		}
		
		if (result.getReturnType() != field.getType()) {
			result = null;
		}
		
		if(result == null){
			throw new RuntimeException("Entity[" + type.getName() + "] field '" + fieldName + "' has no get method!");
		}
		
		return result;
	}

	/**
	 * 获取set方法
	 * 
	 * @param type
	 * @param field
	 * @return
	 */
	private static Method setter(Class<?> type, Field field) {
		String fieldName = field.getName();
		String methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		Method result = null;
		try {
			result = type.getMethod(methodName, field.getType());
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
		
		if (result.getReturnType() != void.class) {
			result = null;
		}
		
		if(result == null){
			throw new RuntimeException("Entity[" + type.getName() + "] field '" + fieldName + "' has no set method!");
		}
		
		return result;
	}

	private void prepareTableMap(Class<?> type) {
		if (type == null) {
			return;
		}
		Entity entityAnnotation = type.getAnnotation(Entity.class);
		String tableName = null;
		
		if (entityAnnotation != null) {
			// 处理表名
			if (StringUtils.isNotBlank(entityAnnotation.tableName())) {
				tableName = entityAnnotation.tableName();
			} else {
				String className = type.getSimpleName();
				tableName = tablePrefix + Character.toLowerCase(className.charAt(0)) + className.substring(1);
			}
			tableNameMap.put(type, tableName);
		}
	}
	
	public <T> T map(Class<T> type, ResultSet rs) {
		T obj = null;
		try {
			//初始化对象
			obj = type.newInstance();
			
			//获取resultSet的列名
			Map<String, Setter> columnSetterMap = entityColumnSetterMap.get(type);
			ResultSetMetaData metaData = rs.getMetaData();
			int colNum = metaData.getColumnCount();
			Setter setter = null;
			for (int i = 1; i <= colNum; i++) {
				String columnName = metaData.getColumnLabel(i);
				setter = columnSetterMap.get(columnName);
				if(setter != null){
					setter.set(obj, rs);
				}
			}
		} catch (InstantiationException | IllegalAccessException | SQLException ex) {
			throw new RuntimeException(ex);
		}
		return obj;
	}

	public Map<String, Object> paramsMap(Object obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Getter getter : getterMap.get(obj.getClass())) {
			m.put(getter.getColumnName(), getter.get(obj));
		}
		return m;
	} 

	public Long getObjectKey(Object obj) {
		try {
			Method getId = getKeyMethodMap.get(obj.getClass());
			return (Long)getId.invoke(obj);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void setObjectKey(Object obj, Object id) {
		try {
			Method setId = setKeyMethodMap.get(obj.getClass());
			setId.invoke(obj, id);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getTableName(Class<?> type) {
		return tableNameMap.get(type);
	}

	public String getKeyColumn(Class<?> type) {
		String result = keyColumnMap.get(type);
		return result;
	}

	public String getKeyField(Class<?> type) {
		String result = keyFieldMap.get(type);
		return result;
	}

	public String getCreateSql(Object entity) {
		Class<?> type = entity.getClass();
		return createSqlMap.get(type);
	}

	public String getUpdateSql(Object entity) {
		Class<?> type = entity.getClass();
		return updateSqlMap.get(type);
	}

	public String getUpdateSql(Object entity, String fields) {
		Class<?> type = entity.getClass();
		String sqlKey = type.getName() + "-fields:" + fields;
		String sql = updateSqlFieldsMap.get(sqlKey);
		String tableName = tableNameMap.get(type);
	 
		if (sql != null) {
			return sql;
		}

		StringBuilder buf = new StringBuilder();
		buf.append("update ").append(tableName).append(" set ");
		String key = keyColumnMap.get(type);

		// very quick process multi-filed split only by char ',' 
		int x = 0, y;
		while ((y = fields.indexOf(',', x)) > -1) {
			appendUpdateField(type, fields.substring(x, y), key, buf);
			x = y + 1;
		}
		appendUpdateField(type, fields.substring(x), key, buf);

		buf.setCharAt(buf.length() - 1, ' ');
		buf.append("where ").append(key).append("=:").append(key);

		sql = buf.toString();
		 
		updateSqlFieldsMap.put(sqlKey, sql);
		return sql;
	}

	private void appendUpdateField(Class<?> type, String field, String key, StringBuilder buf) {
		field = field.trim();
		String column = field2columnMap.get(type.getName() + '.' + field);
		if (!column.equalsIgnoreCase(key)) {
			buf.append(column).append("=:").append(column).append(',');
		}
	}
 
	public String getDeleteSql(Class<?> type) {
		return deleteSqlMap.get(type);
	}

	public String getSelectSql(Class<?> type) {
		return selectSqlMap.get(type);
	}
	
	public String getExistsSql(Class<?> type) {
		return existsSqlMap.get(type);
	}

	public boolean hasLocalCache(Class<?> type) {
		boolean[] cache = cacheMap.get(type);
		if (cache == null) {
			return false;
		}
		return cache[0];
	}

	public boolean hasExternalCache(Class<?> type) {
		boolean[] cache = cacheMap.get(type);
		if (cache == null) {
			return false;
		}
		return cache[1];
	}

	public String getColumnByField(Class<?> type, String field) {
		String column = field2columnMap.get(type.getName() + '.' + field);
		if (column == null) {
			column = field;
		}
		return column;
	}
	
	/**
	 * 是否为orm框架管理的实体
	 * @param type
	 * @return
	 */
	public boolean isOrmManagerEntity(Class<?> type) {
		return entityMap.get(type) != null;
	}
}
