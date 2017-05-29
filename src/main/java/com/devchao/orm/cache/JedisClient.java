package com.devchao.orm.cache;

import java.util.List;
import java.util.Set;

import com.devchao.orm.utils.SerializationUtils;
import com.devchao.orm.utils.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisException;

public class JedisClient {
	
	protected String redisHost;
	protected Integer redisPort;
	protected Integer timeout; // 读的超时时间(单位为毫秒)，根据业务设置
	public static final Integer DEFAULT_READ_TIMEOUT = Protocol.DEFAULT_TIMEOUT;

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public Integer getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(Integer redisPort) {
		this.redisPort = redisPort;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	protected Jedis getJedis() throws JedisException {
		if (timeout == null) {
			timeout = DEFAULT_READ_TIMEOUT;
		}
		Jedis jedis = new Jedis(redisHost, redisPort, timeout);
		return jedis;
	}

	public List<String> configGet(String name) {
		Jedis jedis = null;
		List<String> value = null;
		try {
			jedis = this.getJedis();
			value = jedis.configGet(name);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return value;
	}

	/**
	 * redis 信息
	 */
	public String info(String section) {
		Jedis jedis = null;
		String info = null;
		try {
			jedis = this.getJedis();
			if (StringUtils.isBlank(section)) {
				info = jedis.info();
			} else {
				info = jedis.info(section);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return info;
	}

	/**
	 * 批量获取
	 */
	public List<String> batchGet(String... keys) {
		List<String> result = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			result = getJedis().mget(keys);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return result;

	}

	protected void release(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
			jedis = null;
		}
	}

	public boolean exists(String key) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			return jedis.exists(key);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	public Long incr(String key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			if (jedis.exists(key)) {
				jedis.expire(key, Integer.valueOf(jedis.ttl(key) + ""));
			} else {
				jedis.expire(key, seconds);
			}
			return jedis.incr(key);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 通过key删除
	 */
	public Long del(String key) {
		Long result = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			result = jedis.del(key);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return result;
	}

	/**
	 * 添加key value(string)
	 */
	public void set(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.set(key, value);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 添加key value 并且设置存活时间
	 */
	public void set(String key, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.set(key, value);
			jedis.expire(key, seconds);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 保存对象
	 */
	public void set(String key, Object obj) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.set(key.getBytes(), SerializationUtils.toSerialization(obj));
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 保存对象，并且设置存活时间
	 */
	public void set(String key, Object obj, int seconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.set(key.getBytes(), SerializationUtils.toSerialization(obj));
			jedis.expire(key, seconds);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	public String get(String key) {
		Jedis jedis = null;
		String value = null;
		try {
			jedis = this.getJedis();
			value = jedis.get(key);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return value;
	}

	public <T> T get(Class<T> clazz, String key) {
		
		Jedis jedis = null;
		T t = null;
		try {
			jedis = this.getJedis();
			byte[] bytes = jedis.get(key.getBytes());
			if (bytes != null) {
				t = SerializationUtils.fromSerialization(bytes, clazz);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return t;
	}

	/**
	 * 模糊查询所有前缀为入参的KEY
	 */
	public Set<String> keys(String key) {
		Jedis jedis = null;
		Set<String> keys = null;
		try {
			jedis = this.getJedis();
			keys = jedis.keys(key + "*");
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return keys;
	}

	/**
	 * 自增
	 */
	public void increase(String key) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.incr(key);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * redis set集合-添加
	 */
	public void sadd(String key, String... value) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.sadd(key, value);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * redis set集合-查询
	 */
	public Set<String> smembers(String key) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
		return jedis.smembers(key);
	}

	/**
	 * redis set集合-删除
	 */
	public void srem(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.srem(key, value);
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 清空当前redis信息
	 */
	public void flushdb() {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.flushDB();
		} catch (Exception e) {
			throw e;
		} finally {
			release(jedis);
		}
	}
}
