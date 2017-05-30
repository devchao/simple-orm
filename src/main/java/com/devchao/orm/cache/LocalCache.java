package com.devchao.orm.cache;

/**
 * 本地缓存接口规范
 */
public interface LocalCache {
    public <T> T get(String key, Class<T> type);
    public void set(String key, Object obj);
    public void delete(String key);
}
