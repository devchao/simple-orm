
package com.devchao.orm.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 本地缓存策略
 */
public class HashCacheProvider implements LocalCache {
	
	private Map<String, Object> cache = new ConcurrentHashMap<String, Object>();
	
    @Override
    public void set(String key, Object obj) {
        cache.put(key, obj);
    }

    @Override
    public void delete(String key) {
    	cache.remove(key);
    }

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type) {
		return (T) cache.get(key);
	}
}
