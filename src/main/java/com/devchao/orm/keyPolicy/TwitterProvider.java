package com.devchao.orm.keyPolicy;

/**
 * Snowflake算法
 */
public class TwitterProvider implements KeyPolicy {

	private SnowflakeIdWorker idWorker;
	
	/**
	 * @param workerId 工作ID (0~31)
	 * @param dataCenterId 数据中心ID (0~31)
	 */
	public TwitterProvider(int workerId, int dataCenterId) {
		idWorker = new SnowflakeIdWorker(workerId, dataCenterId);
	}
	
	@Override
	public String nextVal() {
		return String.valueOf(idWorker.nextId());
	}

	@Override
	public Class<?> getKeyType() {
		return Long.class;
	}

}
