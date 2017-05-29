
package com.devchao.orm.cache;

import java.util.HashMap;
import java.util.Map;


/**
 * 本地缓存策略
 */
public class HashCacheProvider implements LocalCache {
	
	private Map<String, Object> cache = new HashMap<String, Object>();
	
    @Override
    public Object get(String key) {
    	return cache.get(key);
    }

    @Override
    public void set(String key, Object obj) {
        cache.put(key, obj);
    }

    @Override
    public void delete(String key) {
    	cache.remove(key);
    }
}
