/**
 * 2008-10-15
 * SkyGameServer
 * @author eric.chan
 **/
package common.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

/*import common.GameServer;
import common.utils.date.DateTime;*/

public class DateUtils extends org.apache.commons.lang.time.DateUtils {

	// 格式：年－月－日 小时：分钟：秒
	public static final String FORMAT_ONE = "yyyy-MM-dd HH:mm:ss";

	// 格式：年－月－日 小时：分钟
	public static final String FORMAT_TWO = "yyyy-MM-dd HH:mm";

	// 格式：年月日 小时分钟秒
	public static final String FORMAT_THREE = "yyyyMMdd-HHmmss";

	// 格式：年/月/日 小时:分钟:秒数
	public static final String FORMAT_FOUR = "yyyy-MM-dd HH:mm:ss";

	// 格式：年/月/日
	public static final String FORMAT_FIVE = "yyyy/MM/dd";

	// 格式：时:秒
	public static final String FORMAT_SIX = "HH:mm";
	// 格式： 月-日 时:秒
	public static final String FORMAT_SERVEN = "MM-dd HH:mm";

	// 格式：年－月－日
	public static final String LONG_DATE_FORMAT = "yyyy-MM-dd";

	// 格式：月－日
	public static final String SHORT_DATE_FORMAT = "MM-dd";

	// 格式：小时：分钟：秒
	public static final String LONG_TIME_FORMAT = "HH:mm:ss";

	// 格式：年-月
	public static final String MONTG_DATE_FORMAT = "yyyy-MM";

	/** yyyy-MM-dd */
	public static final DateTimeFormatter FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	/** yyyy-MM-dd HH:mm:ss */
	public static final DateTimeFormatter FORMATTER_3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	/** yyyyMMddHHmmss */
	public static final DateTimeFormatter FORMATTER_4 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	/** 每秒毫秒数 */
	public static final long MILLIS_PER_SECOND = 1000L;
	/** 每分钟毫秒数 */
	public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
	/** 每小时毫秒数 */
	public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
	/** 每日毫秒数 */
	public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
	/** 每周毫秒数 */
	public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;

	static final String dayNames[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

	/**
	 * 得到当前日期格式化对象
	 * 
	 * @param str
	 * @return
	 */
	private static DateFormat getDateFormat(String str) {
		return new SimpleDateFormat(str);
	}

	/**
	 * 返回给定日历字段的值
	 * 
	 * @param field the given calendar field.
	 * @return 2009-8-11
	 * @author lyh
	 */
	public static int getDateField(int field) {
		return Calendar.getInstance().get(field);
	}

	/**
	 * 得到当前日期
	 * 
	 * @date 2006-6-29
	 * @author eric.chen
	 * @return
	 */
	public static String getNowDate() {
		return DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd 00:00:00.0000000");
	}

	public static String getNowDate(String format) {
		return DateFormatUtils.format(System.currentTimeMillis(), format);
	}

	private static String format = "yyyy-MM-dd HH:mm:ss";

	public static String formatDate(Timestamp time) {
		return formatDate(time, format);
	}

	public static String formatDate(Timestamp time, String format) {
		return DateFormatUtils.format(time.getTime(), format);
	}

	public static String formatDate(long time, String format) {
		return DateFormatUtils.format(time, format);
	}

	private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>() {
		protected synchronized DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	public static DateFormat getDateFormat() {
		return threadLocal.get();
	}

	public static String getNowDate2() {
		return LocalDateTime.now().format(FORMATTER_2);
	}

	public static String getNowDate3() {
		return LocalDateTime.now().format(FORMATTER_3);
	}

	public static String getNowDate4() {
		return LocalDateTime.now().format(FORMATTER_4);
	}

	/**
	 * 是否今天
	 * 
	 * @param day
	 * @return
	 * @date 2009-4-7
	 * @author eric.chan
	 */
	public static boolean isToday(String day) {
		boolean sameDay = isToday(new Date(java.sql.Timestamp.valueOf(day).getTime()));
		return sameDay;
	}

	public static boolean isToday(long day) {
		boolean sameDay = isToday(new Date(day));
		return sameDay;
	}

	public static boolean isToday(Date day) {
		boolean sameDay = isSameDay(day, new Date());
		return sameDay;
	}

	/**
	 * 判断是否超过今天
	 * 
	 * @author like
	 * @date 2011-3-14
	 * @param time
	 * @param delay
	 * @return
	 */
	public static boolean isOverTime(long time, int delay) {
		if (time + delay * 60 * 1000 > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	/**
	 * 判断当前日期是否在区间段内
	 * 
	 * @author like
	 * @date 2011-3-22
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isTimeZone(Date begin, Date end) {
		Date d1 = new Date();
		if (d1.after(begin) == true && d1.before(end) == true) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查指定时间是否在本周
	 */
	public static boolean isThisWeek(long time) {
		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(time);
		t.setFirstDayOfWeek(Calendar.MONDAY);

		Calendar n = Calendar.getInstance();
		n.setFirstDayOfWeek(Calendar.MONDAY);

		if (t.get(Calendar.YEAR) == n.get(Calendar.YEAR)
				&& t.get(Calendar.WEEK_OF_YEAR) == n.get(Calendar.WEEK_OF_YEAR)) {
			return true;
		}

		return false;
	}

	/**
	 * 取得指定日期过 months 月后的日期 (当 months 为负数表示指定月之前);
	 * 
	 * @param date 日期 为null时表示当天
	 * @param month 相加(相减)的月数
	 */
	public static Date nextMonth(Date date, int months) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

	/**
	 * 取得指定日期过 day 天后的日期 (当 day 为负数表示指日期之前);
	 * 
	 * @param date 日期 为null时表示当天
	 * @param month 相加(相减)的月数
	 */
	public static Date nextDay(Date date, int day) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.DAY_OF_YEAR, day);
		return cal.getTime();
	}

	/**
	 * 取得距离今天 day 日的日期
	 * 
	 * @param day
	 * @param format
	 * @return
	 * @author chenyz
	 */
	public static String nextDay(int day, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, day);
		return dateToString(cal.getTime(), format);
	}

	/**
	 * 取得指定日期过 day 周后的日期 (当 day 为负数表示指定月之前)
	 * 
	 * @param date 日期 为null时表示当天
	 */
	public static Date nextWeek(Date date, int week) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.WEEK_OF_MONTH, week);
		return cal.getTime();
	}

	/**
	 * 把日期转换为字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(java.util.Date date, String format) {
		String result = "";
		SimpleDateFormat formater = new SimpleDateFormat(format);
		try {
			result = formater.format(date);
		} catch (Exception e) {
			// log.error(e);
		}
		return result;
	}

	/**
	 * 获取明天的日期
	 */
	public static String afterDay() {
		return dateToString(nextDay(new Date(), 1), LONG_DATE_FORMAT);
	}

	/**
	 * 判断 factTime 是否在 referenceTime 的前面： true factTime 在 referenceTime的前面 true
	 * factTime 在 referenceTime的后面 chenjy 2011-11-28
	 * 
	 * @param factTime
	 * @param referenceTime
	 * @return boolean
	 */
	public static boolean isBeforeDay(String factTime, String referenceTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date fact = sdf.parse(factTime);
			Date rf = sdf.parse(referenceTime);
			if (fact.getTime() < rf.getTime()) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * 获取定时器需要等待执行时间
	 * 
	 * @param time
	 * @return
	 * @author lyh
	 * @date 2011-10-24
	 */
	public static long getScheduleRun(long time) {
		long now = System.currentTimeMillis();
		if (time > now) {
			return time - now;
		} else
			return 0;
	}

	/**
	 * 当前时间距离某个时间的差值
	 * 
	 * @param t2
	 * @return
	 * @date 2012-8-31
	 * @author eric.chan
	 */
	public static int dayBreak(String t2) {
		String s = formatDate(System.currentTimeMillis(), "HH:mm:ss");
		Time now = Time.valueOf(s);
		Time target = Time.valueOf(t2);

		// 还没到目标时间
		if (now.getTime() <= target.getTime()) {
			return (int) (target.getTime() - now.getTime());
		} else { // 已经过了目标时间
			return (int) ((TIME_END - now.getTime()) + (target.getTime() - TIME_BEGIN));
		}
	}

	static long TIME_BEGIN = Time.valueOf("00:00:00").getTime();
	static long TIME_END = Time.valueOf("24:00:00").getTime();

	public static String toTimeFormat(long time) {
		if (time < 0)
			return "00:00:00";
		int t = (int) (time / 1000);// 秒数
		int h = t / 3600;
		String sh = h < 10 ? "0" : "";
		int m = (t - 3600 * h) / 60;
		String sm = m < 10 ? "0" : "";
		int s = t - h * 3600 - m * 60;
		String ss = s < 10 ? "0" : "";
		return sh + h + ":" + sm + m + ":" + ss + s;
	}

	/**
	 * 获取国际化的星期几
	 * 
	 * @author Nate
	 * @date 2013-4-11
	 */
	public static String getI18nDayOfWeek(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int day = cal.get(Calendar.DAY_OF_WEEK);
		return dayNames[day - 1];
	}

	/**
	 * 毫秒转国际化时间格式 23小时4分
	 * 
	 * @author Nate
	 * @date 2013-3-10
	 */
	/*public static String to18nTimeFormat(long timeInMillis) {
		StringBuilder sb = new StringBuilder();
		int totalMin = (int) timeInMillis / (60 * 1000);
		int hour = totalMin / 60;
		int minute = totalMin % 60;
		if (hour > 0) {
			sb.append(hour).append(I18n.c("common.unit.hour"));
		}
		if (minute < 10 && hour > 0) {
			sb.append("0").append(minute).append(I18n.c("common.unit.minute"));
		} else {
			sb.append(minute).append(I18n.c("common.unit.minute"));
		}
		return sb.toString();
	}*/

	/**
	 * 获取long格式的时间（月份参数不需要减1）
	 * 
	 * @author Nate
	 * @date 2013-10-4
	 */
	public static long getTimeInMills(int year, int month, int date, int hourOfDay, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, date, hourOfDay, minute, second);
		return cal.getTimeInMillis();
	}

	/**
	 * 获取下一个周日的零点时间
	 * 
	 * @author Nate
	 * @date 2013-11-26
	 */
	public static long getNextSundayZeroTime() {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis();
	}

	/**
	 * 获取明天的0点毫秒数
	 * 
	 * @return
	 * 
	 * @author Nate
	 * @date 2014-4-23
	 */
	public static long getNextDayZeroTimeInMills() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		return cal.getTimeInMillis();
	}

	/**
	 * 新建一个Cron时间表达式对象
	 * 
	 * @param expr
	 * @return
	 */
	/*public static CronExpression newCronExpression(String expr) {
		return new CronExpression(expr);
	}*/

	public static void main(String[] s) throws Exception {
		long t = getNextValidDayTimeAfter(13, 0, 0);
		System.out.println(DateUtils.formatDate(t, DateUtils.FORMAT_ONE));
	}

	/**
	 * 获得现在距离某个时间相差的天数 过期返回-1
	 * 
	 * @param target
	 * @return
	 */
	public static int timeDis(long target) {
		long cur = System.currentTimeMillis();
		long dis = (target - cur) / 1000 / 60 / 60 / 24;
		if (dis >= 0) {
			return (int) dis + 1;
		} else {
			// 过期
			return -1;
		}
	}

	/**
	 * 判断现在是否在time的间隔当中
	 * 
	 * @param time 时间间隔
	 * @param regex 开始和结束时间的分隔符
	 * @param pattern 时间格式
	 * @return
	 */
	public static boolean isInTime(String time, String regex, String pattern) {
		if (time == null) {
			return false;
		}
		try {
			String[] attr = time.split(regex);
			// 时间格式转换器
			DateFormat sf = new SimpleDateFormat(pattern);
			// 获取开始时间的毫秒数
			long begin = sf.parse(DateUtils.timeFormatFilter(timeFormatFilter(attr[0], pattern), pattern)).getTime();
			// 获取结束时间的毫秒数
			long end = sf.parse(DateUtils.timeFormatFilter(timeFormatFilter(attr[1], pattern), pattern)).getTime();
			// 现在的时间毫秒数
			long cur = System.currentTimeMillis();
			if (cur >= begin && cur <= end) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断现在是否在time的间隔当中
	 * 
	 * @param time 时间间隔
	 * @param regex 开始和结束时间的分隔符
	 * @param pattern 时间格式
	 * @return
	 */
	public static boolean isInTime(long start, long end) {
		if (start > 0 && end > 0) {
			long time = System.currentTimeMillis();
			return time >= start && time <= end;
		} else {
			return false;
		}
	}

	/**
	 * 获取第二天某个小时的毫秒数
	 * 
	 * @param hour
	 * @return
	 */
	public static long getNextDayOneTimeInMills(int hour) {
		return getNextDayZeroTimeInMills() + (hour * 60 * 60 * 1000);
	}

	/**
	 * 时间格式转换过滤器 如:将2015-1-1转换成2015-01-01
	 * 
	 * @param time
	 * @param pattern
	 * @return
	 */
	public static String timeFormatFilter(String time, String pattern) {
		try {
			// 时间格式转换器
			DateFormat sf = new SimpleDateFormat(pattern);
			long open_time = sf.parse(time).getTime();
			time = sf.format(new Date(open_time));
			return time;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 时间格式转换处理器
	 * 
	 * @param time 时间
	 * @param bPattern 转换之前格式的模板
	 * @param aPattern 转换之后格式的模板
	 * @param bReg 转换之前的分隔符
	 * @param aReg 转换之后的分隔符
	 * @return
	 */
	public static String timeFormatHandler(String time, String bPattern, String aPattern, String bReg, String aReg) {
		try {
			String[] attrs = time.split(bReg);
			DateFormat sf1 = new SimpleDateFormat(bPattern);
			DateFormat sf2 = new SimpleDateFormat(aPattern);
			long open_time_begin = sf1.parse(attrs[0]).getTime();
			long open_time_end = sf1.parse(attrs[1]).getTime();
			return sf2.format(new Date(open_time_begin)) + aReg + sf2.format(new Date(open_time_end));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static long getNextValidDayTimeAfter(int hour, int minute, int second) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime target = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute,
				second);
		LocalTime lt1 = LocalTime.of(hour, minute, second);
		LocalTime lt2 = now.toLocalTime();
		if (lt2.isAfter(lt1)) {
			target = target.plusDays(1);
		}

		Instant in = target.atZone(ZoneOffset.systemDefault()).toInstant();
		return in.toEpochMilli();
	}

	/**
	 * 指定时间与当前时间的间隔天数
	 * 
	 */
	public static int calDays(long startTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTime);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long now = System.currentTimeMillis();
		if (now < cal.getTimeInMillis()) {
			return 1;
		}
		int d = (int) (Math.ceil((now - cal.getTimeInMillis()) / (double) DateUtils.MILLIS_PER_DAY));
		return 1 + d;
	}

	/**
	 * 当天时间是否在开服时间指定天数之内
	 * 
	 */
	/*public static boolean isInTimes(int days) {
		long openServer = GameServer.getServerOpenTime();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(openServer);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long servenDay = cal.getTimeInMillis() + MILLIS_PER_DAY * days;
		long cur = System.currentTimeMillis();
		if (servenDay >= cur) {
			return true;
		}
		return false;

	}*/

	/**
	 * 构造秒数信息
	 * 
	 * @param second
	 * @return ,秒数:xx,小时:xx,天数:xx
	 */
	public static String logSecondInfo(long second) {
		StringBuilder sb = new StringBuilder();
		sb.append(",秒数:").append(second).append(",小时:").append(second / 60 / 60).append(",天数:")
				.append(second / 24 / 60 / 60);
		return sb.toString();
	}

	/**
	 * 计算指定时间离结束时间剩余秒数
	 * 
	 * @param now 指定时间long
	 * @param nowDay 指定时间所处天数
	 * @param lastDay 最后一天
	 * @return
	 */
	public static long calEndDaySecond(long now, int nowDay, int lastDay) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long startTime = cal.getTimeInMillis();
		int addDay = lastDay - nowDay + 1;
		long leftSecond = (startTime + DateUtils.MILLIS_PER_DAY * addDay - now) / 1000;
		long hour = leftSecond / 60 / 60;
		long day = leftSecond / 24 / 60 / 60;
		/*Trace.debug("计算离结束时间剩余秒数now:" + DateUtils.formatDate(now, DateUtils.FORMAT_ONE) + ",startTime:"
				+ DateUtils.formatDate(startTime, DateUtils.FORMAT_ONE) + ",nowDay:" + nowDay + ",lastDay:" + lastDay
				+ ",addDay:" + addDay + ",leftSecond:" + leftSecond + ",hour:" + hour + ",day:" + day);*/
		return leftSecond;
	}

	/**
	 * 把yyyy-MM-dd HH:mm:ss转换成long时间
	 * 
	 * @param attr
	 * @return
	 */
	public static long getLongTime(String attr) {
		long end = 0;
		try {
			// 时间格式转换器
			DateFormat sf = new SimpleDateFormat(DateUtils.FORMAT_FOUR);
			// 开始时间格式转换
			String endTime = DateUtils.timeFormatFilter(attr, DateUtils.FORMAT_FOUR);
			// 获取开始时间的毫秒数
			end = sf.parse(endTime).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return end;

	}

	/**
	 * 获取2个时间点之间的秒数
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static long getSecond(long startTime, long endTime) {
		return (endTime - startTime) / MILLIS_PER_SECOND;
	}

}
