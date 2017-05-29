package com.devchao.orm.utils;

public class StringUtils {
	
	public static boolean isBlank(String str) {
		if (str == null || str.trim().equals("")) {
			return true;
		}
		return false;
	}
	
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
	
	/**
	 * 驼峰转下划线
	 */
	public static String camelToUnderline(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		if (param.indexOf("_") != -1) {
			return param;
		}
		int len = param.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = param.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append('_');
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
