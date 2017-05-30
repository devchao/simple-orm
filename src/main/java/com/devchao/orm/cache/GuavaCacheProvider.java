package com.devchao.orm.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCacheProvider implements LocalCache {
	
	private Cache<String, Object> cache;
	
	public GuavaCacheProvider(int expireInSeconds, int initSize, int maxSize) {
		cache = CacheBuilder.newBuilder().expireAfterAccess(expireInSeconds, TimeUnit.SECONDS)
				.initialCapacity(initSize).maximumSize(maxSize).build();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type) {
		return (T) cache.getIfPresent(key);
	}
	
	@Override
	public void set(String key, Object obj) {
		cache.put(key, obj);
	}
	
	@Override
	public void delete(String key) {
		cache.invalidate(key);
	}
	
}
