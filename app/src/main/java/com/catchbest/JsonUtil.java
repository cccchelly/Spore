package com.catchbest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * FastJson 操作的Util
 * 
 * @author Iceman
 */
public class JsonUtil {

	/**
	 * 将JSON转成 数组类型对象
	 * 
	 * @param json
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> getArray(String json, Class<T> clazz) {
		List<T> t = null;
		try {
			t = new Gson().fromJson(json,new TypeToken<List<T>>(){}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	/**
	 * 将java对象转换成json字符串
	 * 
	 * @param obj
	 *            准备转换的对象
	 * @return json字符串
	 * @throws Exception
	 */

	public static String getJson(Object obj) {
		String json = "";
		try {
			json = new Gson().toJson(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//LogUtils.e(json);
		return json;
	}

	/**
	 * 
	 * 	 */
	public static String getJsonAndThrow(Object obj) {
		String json = new Gson().toJson(obj);
		//LogUtils.e(json);
		return json;
	}
	
	
	/**
	 * 将json字符串转换成java对象
	 * 
	 * @param json
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T getObject(String json, Class<T> clazz) {
		T t = null;
		try {
			t = new Gson().fromJson(json,new TypeToken<T>(){}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	public static ArrayList<String> StringToArrayList(String str) {
		ArrayList<String> investorFoucusDirectionList = new ArrayList<String>();
		if (str != null) {
			String[] zu = str.split("\\,");
			for (int i = 0; i < zu.length; i++) {
				if (zu[i].equals("")) {
					continue;
				}
				investorFoucusDirectionList.add(zu[i]);
			}
		}
		return investorFoucusDirectionList;
	}
	

}
