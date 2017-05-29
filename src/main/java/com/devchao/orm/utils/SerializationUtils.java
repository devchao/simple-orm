package com.devchao.orm.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.alibaba.fastjson.JSON;

/**
 * 序列化工具类
 */
public class SerializationUtils {
	
	/**
	 * Java对象序列化
	 */
	public static byte[] toSerialization(Object object) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOut;
			objectOut = new ObjectOutputStream(output);
			objectOut.writeObject(object);
			objectOut.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toByteArray();
	}
	
	/**
	 * Java对象反序列化
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromSerialization(byte[] bytes, Class<T> className) {
		T object = null;
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			ObjectInputStream objectIn;
			objectIn = new ObjectInputStream(input);	
			object = (T)objectIn.readObject();
			objectIn.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}
	
	/**
	 * Json序列化
	 */
	public static String toJsonString(Object o) {
		return JSON.toJSONString(o);
	}
	
	/**
	 * Json反序列化
	 */
	public static Object fromJsonString(String jsonStr, Class<?> clazz) {
		return JSON.parseObject(jsonStr, clazz);
	}
	
}
