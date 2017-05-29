package com.devchao.orm;

import java.util.List;
import java.util.Map;

import com.devchao.orm.data.Dao;

/**
 * @author devchao TODO
 */
public class SimpleDao {

	private Dao dao;

	public int executeSql(String sql, Object... params) {
		return dao.update(sql, params);
	}

	public <T> List<T> list(Class<T> type, String sql, Object... params) {
		return dao.list(type, sql, params);
	}

	public <T> List<T> listPage(Class<T> type, String sql, Integer pageNo, Integer pageSize, Object... params) {
		return dao.listPage(type, sql, pageNo, pageSize, params);
	}

	public <T> T findFirst(Class<T> type, String sql, Object... params) {
		return dao.findFirst(type, sql, params);
	}

	public <T> List<T> basicList(Class<T> type, String sql, Object... params) {
		return dao.getJdbcTemplateSlave().queryForList(sql, type, params);
	}

	public <T> T basicQuery(Class<T> type, String sql, Object... params) {
		T result = null;
		List<T> list = dao.getJdbcTemplateSlave().queryForList(sql, type, params);
		if(list.size() > 0){
			result = list.get(0);
		}
		return result;
	}

	public <T> Long insert(T t) {
		return dao.create(t);
	}

	public <T> int upt(T t) {
		return dao.update(t);
	}

	public <T> int upt(T t, String fields) {
		return dao.update(t, fields);
	}
	
	public <T> int del(Class<T> t, Long key) {
		return dao.delete(t, key);
	}

	public <T> T get(Class<T> t, Long key) {
		return dao.find(t, key);
	}

	public <T> boolean exists(Class<T> t, Long key){
		return dao.exists(t, key);
	}
	
	public List<Map<String, Object>> queryForMapList(String sql, Object... params) {
		return dao.queryForMapList(sql, params);
	}
	
	public List<Map<String, Object>> queryForMapListPage(String sql, Integer pageNo, Integer pageSize, Object... params) {
		return dao.queryForMapListPage(sql, pageNo, pageSize, params);
	}

	public Map<String, Object> queryForMap(String sql, Object... params) {
		return dao.queryForMap(sql, params);
	}
	
	public void setDao(Dao dao) {
		this.dao = dao;
	}

	public Dao getDao() {
		return dao;
	}
}