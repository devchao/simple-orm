package com.devchao.orm.cache;

import java.util.List;

/**
 * redis外部缓存策略
 */
public class RedisProvider implements ExternalCache {
	
    private JedisClient client;
    
	public JedisClient getClient() {
		return client;
	}

	public void setClient(JedisClient client) {
		this.client = client;
	}

	@Override
    public void delete(String key) {
		client.del(key);
    }

    @Override
    public Object get(String key) {
        return client.get(key);
    }

    @Override
    public Object[] gets(String[] keys) {
    	if (keys == null || keys.length <= 0) {
    		return null;
    	}
    	List<String> l = client.batchGet(keys);
    	Object[] result = new Object[keys.length];
    	for (int i = 0; i < l.size(); i++) {
    		result[i] = l.get(i);
    	}
    	return result;
    }

    @Override
    public void set(String key, Object obj) {
    	client.set(key, obj);
    }

	@Override
	public void set(String key, Object obj, int liveTime) {
		client.set(key, obj, liveTime);
	}
}
