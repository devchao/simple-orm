package com.devchao.orm.data;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.alibaba.fastjson.JSON;
import com.devchao.orm.cache.ExternalCache;
import com.devchao.orm.cache.LocalCache;
import com.devchao.orm.page.SqlPageBuilder;
import com.devchao.orm.utils.StringUtils;

public class Dao {
	
	private JdbcTemplate jdbcTemplate;
	private JdbcTemplate jdbcTemplateSlave;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private ORM orm;
	private SqlPageBuilder sqlPageBuilder;
	private LocalCache cacheFirstLevel;
	private ExternalCache cacheSecondLevel;

	/**
	 * 根据@Key定义的主键查找数据, 首先会从一级缓存和二级缓存寻找，如果没有，从数据库寻找，如果有将数据放到缓存中
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public <T> T find(Class<T> type, Long key) {
		
		T obj = null;
		if (enableCache(type)) {
			obj = findCache(type, key);
		}
		if (obj == null) {
			obj = findDb(type, key);
			if (enableCache(type)) {
				if (obj != null) {
					setCache(type, obj, key);
				}
			}
		}
		return obj;
	}

	/**
	 * 从缓存中查找数据，查找顺序是：1.一级 2.二级
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findCache(Class<T> type, Object key) {
		
		if (!orm.isOrmManagerEntity(type)) {
			throw new RuntimeException("Class[" + type.getName() + "] is not an Entity!");
		}
		
		String cacheKey = getCacheKey(type, key);
		
		// 从一级缓存获取
		if (enableFirstCache(type)) {
			Object o = cacheFirstLevel.get(cacheKey);
			if (o != null) {
				return (T)o;
			}
		}
		
		// 再从二级缓存获取
		if (enableSecondCache(type)) {
			String s = (String) cacheSecondLevel.get(cacheKey);
			if (s != null) {
				T o = string2Object(type, s);
				if (enableFirstCache(type)) {
					cacheFirstLevel.set(cacheKey, o);
				}
				return o;
			}
		}
		return null;
	}

	/**
	 * 创建一条数据，返回主键ID,并放入缓存中
	 */
	public long create(Object entity){
		
		if (!orm.isOrmManagerEntity(entity.getClass())) {
			throw new RuntimeException("Class[" + entity.getClass().getName() + "] is not an Entity!");
		}
		
		Long keyValue = orm.getObjectKey(entity);
		if (keyValue == null) {
			 keyValue = 0L;
		}
		
		KeyHolder keyholder = new GeneratedKeyHolder();
		MapSqlParameterSource ps = new MapSqlParameterSource();
		ps.addValues(orm.paramsMap(entity));
		namedParameterJdbcTemplate.update(orm.getCreateSql(entity), ps, keyholder);
		keyValue = keyholder.getKey().longValue();
		orm.setObjectKey(entity, keyValue);
		return keyValue;
	}

	/**
	 * 根据@key标注的主键删除对应的数据，返回删除的条数，对应的缓存也会对应删除
	 * 
	 * @param entity
	 * @param key
	 * @return
	 */
	public <T> int delete(Class<T> t, Long key){
		
		if (!orm.isOrmManagerEntity(t)) {
			throw new RuntimeException("Class[" + t.getName() + "] is not an Entity!");
		}
		
		Class<?> type = t;
		int result = jdbcTemplate.update(orm.getDeleteSql(type), key);
		
		deleteCache(type, key);
		return result;
	}

	/**
	 * 根据sql更新
	 * @param sql
	 * @param params
	 * @return
	 */
	public int update(String sql, Object... params) {
		return jdbcTemplate.update(sql, params);
	}
	
	/**
	 * 根据@key标注的主键更新
	 * 
	 * @param obj
	 * @return
	 */
	public int update(Object obj) {
		return update(obj, null);
	}
	
	/**
	 * 根据@key标注的主键更新指定的属性，属性间用逗号分隔
	 * @param entity
	 * @param fields field name split by comma, with out any blank
	 * @return update record in database
	 */
	public int update(Object entity, String fields) {
		
		if (!orm.isOrmManagerEntity(entity.getClass())) {
			throw new RuntimeException("Class[" + entity.getClass().getName() + "] is not an Entity!");
		}
		
		Class<?> type = entity.getClass();
		String sql;
		if (StringUtils.isBlank(fields)) {
			sql = orm.getUpdateSql(entity);
		} else {
			sql = orm.getUpdateSql(entity, fields);
		}
		int result = namedParameterJdbcTemplate.update(sql, orm.paramsMap(entity));
		deleteCache(type, orm.getObjectKey(entity));
		return result;
	}

	/**
	 * 获取第一条记录
	 * 
	 * @param type
	 * @param sql
	 * @param params
	 * @return
	 */
	public <T> T findFirst(Class<T> type, String sql, Object... params){
		
		T result = null;
		String firstSql = sqlPageBuilder.buildPageSql(sql, 1, 1);
	
		RowMapper<T> rm = new DimRowMapper<T>(type, orm);
		if (!orm.isOrmManagerEntity(type)) {
			rm = new BeanPropertyRowMapper<T>(type);
		}
		
		List<T> list = jdbcTemplateSlave.query(firstSql, rm, params);
		if(list.size() > 0){
			result = list.get(0);
		}
		return result;
	}

	/**
	 * 获取数据列表
	 * 
	 * @param type
	 * @param sql
	 * @param params
	 * @return
	 */
	public <T> List<T> list(Class<T> type, String sql, Object... params){
		if (orm.isOrmManagerEntity(type)) {
			return jdbcTemplateSlave.query(sql, new DimRowMapper<T>(type, orm), params);
		} else {
			return jdbcTemplateSlave.query(sql, new BeanPropertyRowMapper<T>(type), params);
		}
	}

	/**
	 * 分页获取数据列表
	 * 
	 * @param type
	 * @param sql
	 * @param pageNo 当前页
	 * @param pageSize 每页条数
	 * @param params
	 * @return
	 */
	public <T> List<T> listPage(Class<T> type, String sql, Integer pageNo, Integer pageSize, Object... params){
		return list(type, sqlPageBuilder.buildPageSql(sql, pageNo, pageSize), params);
	}

	public <T> boolean exists(Class<T> type, Long id) {
		if (!orm.isOrmManagerEntity(type)) {
			throw new RuntimeException("Class[" + type.getName() + "] is not an Entity!");
		}
		return jdbcTemplateSlave.queryForObject(orm.getExistsSql(type), Integer.class, id) > 0;
	}

	/**
	 * 根据@key标注的主键清除缓存
	 * 
	 * @param type
	 * @param key
	 */
	public void deleteCache(Class<?> type, Long key) {
		
		if (!orm.isOrmManagerEntity(type)) {
			throw new RuntimeException("Class[" + type.getName() + "] is not an Entity!");
		}
		
		String cacheKey = null;
		if (enableSecondCache(type)) {
			cacheKey = getCacheKey(type, key);
			cacheSecondLevel.delete(cacheKey);
		}
		if (enableFirstCache(type)) {
			if (cacheKey == null) {
				cacheKey = getCacheKey(type, key);
			}
			cacheFirstLevel.delete(cacheKey);
		}
	}

	/**
	 * 根据@key标注的主键从数据库查找数据
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public <T> T findDb(Class<T> type, Long key) {
		
		T result = null;
		if (!orm.isOrmManagerEntity(type)) {
			throw new RuntimeException("Class[" + type.getName() + "] is not an Entity!");
		}
		List<T> list = jdbcTemplateSlave.query(orm.getSelectSql(type), new DimRowMapper<T>(type, orm), key);
		if(list.size() > 0){
			result = list.get(0);
		}
		return result;
	}
	
	/**
	 * 查询并返回Map
	 */
	public Map<String, Object> queryForMap(String sql, Object... params) {
		
		Map<String, Object> result = null;
		List<Map<String, Object>> list = jdbcTemplateSlave.queryForList(sql, params);
		if(list.size() > 0) {
			result = list.get(0);
		}
		return result;
	}
	
	/**
	 * 查询并返回MapList
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object... params) {
		return jdbcTemplateSlave.queryForList(sql, params);
	}
	
	/**
	 * 分页查询并返回MapList
	 */
	public List<Map<String, Object>> queryForMapListPage(String sql, Integer pageNo, Integer pageSize, Object... params) {
		return this.queryForMapList(sqlPageBuilder.buildPageSql(sql, pageNo, pageSize), params);
	}

	/**
	 * 将数据放入缓存中
	 * 
	 * @param type
	 * @param obj
	 * @param key
	 */
	public <T> void setCache(Class<T> type, T obj, Object key) {
		
		if (!orm.isOrmManagerEntity(type)) {
			throw new RuntimeException("Class[" + type.getName() + "] is not an Entity!");
		}
		
		String cacheKey = getCacheKey(type, key);
		if (enableFirstCache(type)) {
			cacheFirstLevel.set(cacheKey, obj);
		}

		if (enableSecondCache(type)) {
			cacheSecondLevel.set(cacheKey, object2String(obj));
		}
	}

	/**
	 * 将json 转成对象
	 * 
	 * @param type
	 * @param s
	 * @return
	 */
	public static <T> T string2Object(Class<T> type, String s) {
		return JSON.parseObject(s, type);
	}

	/**
	 * 将对象转成json
	 * 
	 * @param obj
	 * @return
	 */
	public String object2String(Object obj) {	 
		return JSON.toJSONString(obj);
	}

	/**
	 * 根据@key标注的主键获取缓存key , key的组成形式为类名-key
	 * 
	 * @param type
	 * @param key
	 * @return
	 */
	public static String getCacheKey(Class<?> type, Object key) {
		return type.getName() + '-' + key;
	}

	private boolean enableCache(Class<?> type) {
		return enableSecondCache(type) || enableFirstCache(type);
	}

	private boolean enableSecondCache(Class<?> type) {
		boolean cached = cacheSecondLevel != null && orm.getSecondLevelCache(type);
		return cached;
	}

	private boolean enableFirstCache(Class<?> type) {
		boolean cached = cacheFirstLevel != null && orm.getFirstLevelCache(type);
		return cached;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	public ORM getOrm() {
		return orm;
	}

	public void setOrm(ORM orm) {
		this.orm = orm;
	}

	public SqlPageBuilder getSqlPageBuilder() {
		return sqlPageBuilder;
	}

	public void setSqlPageBuilder(SqlPageBuilder sqlPageBuilder) {
		this.sqlPageBuilder = sqlPageBuilder;
	}

	public LocalCache getCacheFirstLevel() {
		return cacheFirstLevel;
	}

	public JdbcTemplate getJdbcTemplateSlave() {
		return jdbcTemplateSlave;
	}

	public void setJdbcTemplateSlave(JdbcTemplate jdbcTemplateSlave) {
		this.jdbcTemplateSlave = jdbcTemplateSlave;
	}

	public void setCacheFirstLevel(LocalCache cacheFirstLevel) {
		this.cacheFirstLevel = cacheFirstLevel;
	}

	public ExternalCache getCacheSecondLevel() {
		return cacheSecondLevel;
	}

	public void setCacheSecondLevel(ExternalCache cacheSecondLevel) {
		this.cacheSecondLevel = cacheSecondLevel;
	}
}
