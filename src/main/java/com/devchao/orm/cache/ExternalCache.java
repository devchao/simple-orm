package com.devchao.orm.cache;

/**
 * 外部缓存规范
 */
public interface ExternalCache {
    Object get(String key);
    Object[] gets(String[] keys);
    void set(String key, Object obj);
    void set(String key, Object obj, int liveTime);
    void delete(String key);
}
