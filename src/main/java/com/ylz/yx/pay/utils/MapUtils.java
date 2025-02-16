package com.ylz.yx.pay.utils;

import com.alibaba.fastjson.JSON;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MapUtils {

    private String key;
    private Object value;
    public MapUtils(String key, Object value) {
        super();
        this.key = key;
        this.value = value;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public static String getMapToString(Map<String,String[]> map) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
//			  System.out.println("Key = " + entry.getKey() + ", Value = " + new String(entry.getValue()[0].getBytes("ISO-8859-1"), "UTF-8"));
            sb.append(entry.getKey()).append("=").append(new String(entry.getValue()[0].getBytes("ISO-8859-1"), "UTF-8")).append("&");
        }
        return sb.toString();
    }

    public static Map<String, Object> Obj2Map(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //getDeclaredFields()获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.get(obj) != null)
                map.put(field.getName(), field.get(obj));
            else
                map.put(field.getName(), "");
        }
        return map;
    }

    public static Map<String, Object> Obj2MapAndFather(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //getDeclaredFields()获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.get(obj) != null)
                map.put(field.getName(), field.get(obj));
            else
                map.put(field.getName(), "");
        }
        List<Field> allDeclaredFields = getAllDeclaredFields(obj.getClass());
        for (Field field : allDeclaredFields) {
            field.setAccessible(true);
            if (field.get(obj) != null)
                map.put(field.getName(), field.get(obj));
            else
                map.put(field.getName(), "");
        }
        return map;
    }

    //返回父类所有属性的list
    public static List<Field> getAllDeclaredFields(final Class<?> cl) {
        final List<Field> fieldList = new ArrayList<>();
        Class<?> currentClass = cl;
        for (; currentClass != null; currentClass.getSuperclass()) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            fieldList.addAll(Arrays.asList(declaredFields));
        }
        return fieldList;
    }

    public static <T> T convertMapToBean(Class<T> clazz, Map<String, Object> map) {
        T obj = null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            obj = clazz.newInstance(); // 创建 JavaBean 对象


            // 给 JavaBean 对象的属性赋值
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                String propertyName = descriptor.getName();
                if (map.containsKey(propertyName)) {
                    // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                    Object value = map.get(propertyName);
                    if ("".equals(value)) {
                        value = null;
                    }
                    Object[] args = new Object[1];
                    args[0] = value;
                    descriptor.getWriteMethod().invoke(obj, args);


                }
            }
        } catch (IllegalAccessException e) {

        } catch (IntrospectionException e) {
//            logger.error("convertMapToBean 分析类属性失败 Error{}" ,e);
        } catch (IllegalArgumentException e) {
//            logger.error("convertMapToBean 映射错误 Error{}" ,e);
        } catch (InstantiationException e) {
//            logger.error("convertMapToBean 实例化 JavaBean 失败 Error{}" ,e);
        } catch (InvocationTargetException e) {
//            logger.error("convertMapToBean字段映射失败 Error{}" ,e);
        } catch (Exception e) {
//            logger.error("convertMapToBean Error{}" ,e);
        }
        return obj;
    }

    public static Map JsonToMap(String json){
        Map maps = (Map)JSON.parse(json);
        return maps;
    }
}
