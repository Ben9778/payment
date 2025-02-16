package com.ylz.yx.pay.utils;

import cn.hutool.core.date.DateUtil;
import com.ylz.yx.pay.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
* 时间工具类
*/
public class DateKit {

	/** 获取参数时间当天的开始时间  **/
	public static Date getBegin(Date date){

		if(date == null) {
            return null;
        }
		return DateUtil.beginOfDay(date).toJdkDate();
	}

	/** 获取参数时间当天的结束时间 **/
	public static Date getEnd(Date date){
		if(date == null) {
            return null;
        }
		return DateUtil.endOfDay(date).toJdkDate();
	}

	/**
	 * 把日期字符串转换为Date类型。
	 *
	 * @param s           日期字符串
	 * @param datePattern 要输出的时间格式
	 * @return Date 返回Date类型日期
	 */
	public static Date parseDate(String s, String datePattern) {
		if (s == null || s.equals("")) {
			return null;
		}
		Date d = null;
		SimpleDateFormat sf = new SimpleDateFormat(datePattern);
		try {
			d = sf.parse(s);
		} catch (ParseException parseexception) {
		}
		return d;
	}

	/**
	 * 获取自定义查询时间
	 * today|0  -- 今天
	 * yesterday|0  -- 昨天
	 * near2now|7  -- 近xx天， 到今天
	 * near2yesterday|30   -- 近xx天， 到昨天
	 * customDate|2020-01-01,N  -- 自定义日期格式  N表示为空， 占位使用
	 * customDateTime|2020-01-01 23:00:00,2020-01-01 23:00:00 -- 自定义日期时间格式
	 *
	 * @return
	 */
	public static Date[] getQueryDateRange(String queryParamVal){

		//查询全部
		if(StringUtils.isEmpty(queryParamVal)){
			return new Date[]{null, null};
		}

		//根据 | 分割
		String[] valArray = queryParamVal.split("\\|");
		if(valArray.length != 2){ //参数有误
			throw new BizException("查询时间参数有误");
		}
		String dateType = valArray[0];  //时间类型
		String dateVal = valArray[1];  //搜索时间值

		Date nowDateTime = new Date();  //当前时间

		if("today".equals(dateType)){ //今天

			return new Date[]{getBegin(nowDateTime), getEnd(nowDateTime)};

		}else if("yesterday".equals(dateType)){  //昨天

			Date yesterdayDateTime = DateUtil.offsetDay(nowDateTime, -1).toJdkDate(); //昨天
			return new Date[]{getBegin(yesterdayDateTime), getEnd(yesterdayDateTime)};

		}else if("near2now".equals(dateType)){  //近xx天， xx天之前 ~ 当前时间

			Integer offsetDay = 1 - Integer.parseInt(dateVal);  //获取时间偏移量
			Date offsetDayDate = DateUtil.offsetDay(nowDateTime, offsetDay).toJdkDate();
			return new Date[]{getBegin(offsetDayDate), getEnd(nowDateTime)};

		}else if("near2yesterday".equals(dateType)){  //近xx天， xx天之前 ~ 昨天

			Date yesterdayDateTime = DateUtil.offsetDay(nowDateTime, -1).toJdkDate(); //昨天

			Integer offsetDay = 1 - Integer.parseInt(dateVal);  //获取时间偏移量
			Date offsetDayDate = DateUtil.offsetDay(yesterdayDateTime, offsetDay).toJdkDate();
			return new Date[]{getBegin(offsetDayDate), getEnd(yesterdayDateTime)};

		}else if("customDate".equals(dateType) || "customDateTime".equals(dateType)){ //自定义格式

			String[] timeArray = dateVal.split(","); //以逗号分割
			if(timeArray.length != 2) {
                throw new BizException("查询自定义时间参数有误");
            }

			String timeStr1 = "N".equalsIgnoreCase(timeArray[0]) ? null : timeArray[0] ;  //开始时间，
			String timeStr2 = "N".equalsIgnoreCase(timeArray[1]) ? null : timeArray[1];  //结束时间， N表示为空， 占位使用

			Date time1 = null;
			Date time2 = null;

			if(StringUtils.isNotEmpty(timeStr1)){
				time1 = DateUtil.parseDateTime("customDate".equals(dateType) ? (timeStr1 + " 00:00:00" ) : timeStr1);
			}
			if(StringUtils.isNotEmpty(timeStr2)){
				time2 = DateUtil.parseDateTime("customDate".equals(dateType) ? (timeStr2 + " 23:59:59" ) : timeStr2);
			}
			return new Date[]{time1, time2};

		}else{
			throw new BizException("查询时间参数有误");
		}
	}

	/**
	 * 返回两个日期相差几天
	 *
	 * @param begin 日期字符串的格式 "yyyy-MM-dd"
	 * @param end   日期字符串的格式 "yyyy-MM-dd"
	 * @return
	 */
	public static long getDifferDay(String begin, String end) {
		long result = 0;
		if (begin == null || end == null || begin.equals("") || end.equals("")) {
			return 0;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date beginDate = formatter.parse(begin);
			Date endDate = formatter.parse(end);
			result = (endDate.getTime() - beginDate.getTime()) / (3600 * 24 * 1000);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 字符串转时间(yyyy-MM-dd)
	 */
	public static Date formatDate(String str_date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(str_date);
		} catch (ParseException e) {

		}
		return date;
	}

	/**
	 * 将date日期数增加n，返回新的日期
	 *
	 * @param date
	 * @param n
	 * @return
	 */
	public static Date addDay(Date date, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, n);
		return calendar.getTime();
	}

	/**
	 * 指定日期格式化
	 *
	 * @param d
	 * @param datePattern
	 * @return
	 */
	public static String DateFormat(Date d, String datePattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
		String dateString = formatter.format(d);
		return dateString;
	}
}
