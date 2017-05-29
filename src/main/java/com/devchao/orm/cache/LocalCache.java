package com.devchao.orm.cache;

/**
 * 本地缓存接口规范
 */
public interface LocalCache {
    Object get(String key);
    void set(String key, Object obj);
    void delete(String key);
}
