package com.ylz.yx.pay.utils;

import java.util.Map;

/**
 apMap工具类
 * 
 * @ClassName MapTool
 * @version V1.0
 * @author linshikun
 */
public class MapTool {
	public static Map<String, Object> setFailResult(Map<String, Object> map, String des) {
		map.put("result", "fail");
		map.put("faildes", des);
		return map;
	}

	public static Map<String, Object> setSuccessResult(Map<String, Object> map) {
		map.put("result", "success");
		return map;
	}
}
