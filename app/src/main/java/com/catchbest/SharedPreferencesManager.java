package com.catchbest;

import android.content.Context;
import android.content.SharedPreferences;

/**
* @ClassName:shareperferences相关的辅助类
* @Description:SharedPreferences的一个工具类，调用setParam就能保存String, Integer, Boolean, Float,
*  Long类型的参数 同样调用getParam就能获取到保存在手机里面的数据
* @author 刘永滨 lybsln@126.com
*/
public class SharedPreferencesManager {
	/**
	 * 保存在手机里面的文件名
	 */
	private static final String FILE_NAME = "share_catchbest";

	/**
	 * @Title:保存数据
	 * @Description:保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 * @param context 上下文
	 * @return key 参数名
	 * @return object 参数值
	 */
	public static void setParam(Context context, String key, Object object) {

		String type = object.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();

		if ("String".equals(type)) {
			editor.putString(key, (String) object);
		} else if ("Integer".equals(type)) {
			editor.putInt(key, (Integer) object);
		} else if ("Boolean".equals(type)) {
			editor.putBoolean(key, (Boolean) object);
		} else if ("Float".equals(type)) {
			editor.putFloat(key, (Float) object);
		} else if ("Long".equals(type)) {
			editor.putLong(key, (Long) object);

		}

		editor.commit();
	}

	/**
	 * @Title:获取数据
	 * @Description:得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
	 * @param context 上下文
	 * @return key 参数名
	 * @return defaultObject 默认值
	 */
	public static Object getParam(Context context, String key,
                                  Object defaultObject) {
		String type = defaultObject.getClass().getSimpleName();
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);


		if ("String".equals(type)) {
			return sp.getString(key, (String) defaultObject);
		} else if ("Integer".equals(type)) {
			return sp.getInt(key, (Integer) defaultObject);
		} else if ("Boolean".equals(type)) {
			return sp.getBoolean(key, (Boolean) defaultObject);
		} else if ("Float".equals(type)) {
			return sp.getFloat(key, (Float) defaultObject);
		} else if ("Long".equals(type)) {
			return sp.getLong(key, (Long) defaultObject);
		}
		return null;
	}
}