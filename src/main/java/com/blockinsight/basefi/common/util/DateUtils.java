package com.blockinsight.basefi.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class DateUtils {



    public static final int DATE_LEN = 19;
    public static final String FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_MSDETAIL = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final String FORMAT_SIMPLE = "yyyy-MM-dd";
    public static final String FORMAT_YYMMDD = "yyyyMMdd";
    public static final String FORMAT_MEDIUM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_SIMPLE_CN = "yyyy年MM月dd日";
    private static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static String formatDateForBillingTaskCode(Date date)
    {
        return formatDate(date, "yyMMdd");
    }

    public static String formatDetailDate(Date date)
    {
        return formatDate(date, FORMAT_DETAIL);
    }
    /*时间转换方法*/
   public static Date parseDates(String dateStr, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("日期转化错误");
        }
        return date;
    }
    public static String formatDetailMsDate(Date date)
    {
        return formatDate(date, FORMAT_MSDETAIL);
    }

    public static Date parseDate(String format, String date)
    {
        return convertToDate(date, format);
    }

    public static Date parseDetailDate(String date)
    {
        return convertToDate(date, FORMAT_DETAIL);
    }

    public static String formatSubYearMonth(Date date)
    {
        return formatDate(date, "yyyyMM").substring(2);
    }

    public static String formatSimpleDate(Date date)
    {
        return formatDate(date, FORMAT_SIMPLE);
    }

    public static String formatMediumDate(Date date)
    {
        return formatDate(date, FORMAT_MEDIUM);
    }

    /**
     * 16进制转换10进制
     * @param hexInput
     * @return
     */
    public static int hexToTen(String hexInput) {
        int ten = 0;
        String hex = StrUtil.sub(hexInput, 2, hexInput.length());
        ten = HexUtil.toBigInteger(hex).intValue();
        return ten;
    }

    public static Date eventTimeStamp(String timeStamp) {
        String timeStamp_str = timeStamp + "000";
        Long longTimeStamp = Long.valueOf(timeStamp_str);
        return DateUtil.date(longTimeStamp);
    }

    public static String formatDate(Date date, String pattern)
    {
        if (date == null)
        {
            return "";
        }
        SimpleDateFormat ft = new SimpleDateFormat(pattern);
        return ft.format(date);
    }

    public static Date convertToDate(String dateStr, String pattern)
    {
        if (StringUtils.isBlank(dateStr))
        {
            return null;
        }
        SimpleDateFormat ft = new SimpleDateFormat(pattern);
        try
        {
            return ft.parse(dateStr);
        }
        catch (ParseException e)
        {
            log.error("convert to date failed:" + e);
            return null;
        }
    }


    /**
     * 日期格式转换 "yyyy-MM-dd HH:mm:ss.SSS"  "yyyy-MM-dd HH:mm:ss"
     *
     * @param date
     * @return
     */
    public static String dateFormat(Date date, String pattern)
    {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }


    public static Date millisecondsToDate(String dateStr, String pattern)
    {
        if (StringUtils.isBlank(dateStr))
        {
            return null;
        }
        Date tempDate = new Date(Long.parseLong(dateStr));
        SimpleDateFormat ft = new SimpleDateFormat(pattern);
        try
        {
            return ft.parse(ft.format(tempDate));
        }
        catch (ParseException e)
        {
            log.error("convert to date failed:" + e);
            return null;
        }
    }

    public static Date getDayBegin(Date day)
    {
        return convertToDate(formatSimpleDate(day) + " 00:00:00", FORMAT_DETAIL);
    }

    public static Date getDayEnd(Date day)
    {
        return convertToDate(formatSimpleDate(day) + " 23:59:59", FORMAT_DETAIL);
    }

    public static int getDateDaysDiff(Date start, Date end)
    {
        int days = (int) ((end.getTime() - start.getTime()) / 86400000);
        return days;
    }

    public static Date getFinalDate()
    {
        try
        {
            return convertToDate("2100-01-01 00:00:00", FORMAT_DETAIL);
        }
        catch (Exception ex)
        {
            // Do nothing
        }
        return null;
    }
    /***
     * 获取当前月的第一天
     */
    public static Date getCurrentMonthFirstDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.add(Calendar.MONTH,0);
        return getDayBegin(calendar.getTime());
    }

    /**
     * 获取当前月的最后一天
     * @return
     */
    public static Date getCurrentMonthLastDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,0);
        calendar.add(Calendar.MONTH,1);
        return getDayEnd(calendar.getTime());
    }

    /***
     * 获取上个月的第一天
     */
    public static Date getLastMonthFirstDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, -1);
        return getDayBegin(calendar.getTime());
    }

    /***
     * 获取上个月的最后一天
     */
    public static Date getLastMonthLastDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getDayBegin(calendar.getTime());
    }

    /***
     * 获取上个周的第一天
     */
    public static Date getLastWeekFirstDay()
    {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int offset = 1 - dayOfWeek;
        calendar.add(Calendar.DATE, offset - 7);
        return getDayBegin(calendar.getTime());
    }

    /***
     * 获取上个周的最后一天
     */
    public static Date getLastWeekLastDay()
    {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int offset = 7 - dayOfWeek;
        calendar.add(Calendar.DATE, offset - 7);
        return getDayBegin(calendar.getTime());
    }

    /**
     * 返回上季的第一天 0 1 2 Q1 /3=0 1月 3 4 5 Q2 /3=1 4月 6 7 8 Q3 /3=2 7月 9 10 11 Q4 /3=3
     * 10月
     */
    public static Date getFirstDayOfLastQuarter()
    {
        Calendar calendar = Calendar.getInstance();
        int month = 0;
        int quarter = calendar.get(Calendar.MONTH) / 3;
        int year = calendar.get(Calendar.YEAR);
        if (quarter == 0)
        {
            month = 9;
            year--;
        }
        else if (quarter == 1)
        {
            month = 0;
        }
        else if (quarter == 2)
        {
            month = 3;
        }
        else if (quarter == 3)
        {
            month = 6;
        }
        calendar.clear();
        calendar.set(year, month, 1);
        return getDayBegin(calendar.getTime());
    }

    /**
     * 返回上个季的最后一天 0 1 2 Q1 /3=0 3月 3 4 5 Q2 /3=1 6月 6 7 8 Q3 /3=2 9月 9 10 11 Q4 /3=3
     * 12月
     */
    public static Date getLastDayOfLastQuarter()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = 0;
        // 当前季
        int quarter = calendar.get(Calendar.MONTH) / 3;
        if (quarter == 0)
        {
            month = 11;
            year--;
        }
        else if (quarter == 1)
        {
            month = 2;
        }
        else if (quarter == 2)
        {
            month = 5;
        }
        else if (quarter == 3)
        {
            month = 8;
        }
        calendar.clear();
        calendar.set(year, month, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getDayBegin(calendar.getTime());
    }

    /***
     * 获取上个年的第一天
     */
    public static Date getLastYearFirstDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.add(Calendar.YEAR, -1);
        return getDayBegin(calendar.getTime());
    }

    /***
     * 获取上个年的最后一天
     */
    public static Date getLastYearLastDay()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.MONTH, 11);
        calendar.add(Calendar.YEAR, -1);
        return getDayBegin(calendar.getTime());
    }

    public static Date getOtherDate(Date date, int day, int hour, int minute, int second)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, day);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }


   /* public static void main(String[] args)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        for (int i = 0; i < 7; i++)
        {
            c.add(Calendar.DATE, -1);
            Date m = c.getTime();
            String mon = format.format(m);
            map.put(mon, 0);
        }
    }*/

    /**
     * 获取超期日期
     */
    public static Long getOverdueDays(Date requestFinishdate, Date actualFinishDate)
    {
        if (actualFinishDate == null)
        {
            actualFinishDate = new Date();
        }
        if (requestFinishdate == null || requestFinishdate.after(actualFinishDate))
        {
            return 0L;
        }
        long daysBetween = (actualFinishDate.getTime() - requestFinishdate.getTime() + 1000000) / (60 * 60 * 24 * 1000);
        return daysBetween;
    }

    /**
     * 获取超期时间
     */
    public static String getOverdueTimes(Date startDate, Date endDate)
    {
        if (startDate == null || endDate == null || startDate.after(endDate))
        {
            return "";
        }
        //除以1000是为了转换成秒
        long between = (endDate.getTime() - startDate.getTime()) / 1000;
        long day = between / (24 * 3600);
        StringBuffer overTime = new StringBuffer();
        if (day > 0)
        {
            overTime.append(day).append("天");
        }
        long hour = between % (24 * 3600) / 3600;
        if (hour > 0)
        {
            overTime.append(hour).append("小时");
        }
        long minute = between % 3600 / 60;
        if (minute > 0)
        {
            overTime.append(minute).append("分钟");
        }
        return overTime.toString();
    }

    /**
     * 获取某年某月到某年某月按天的切片日期集合（间隔天数的集合）
     *
     * @param beginYear
     * @param beginMonth
     * @param endYear
     * @param endMonth
     * @param k
     * @return
     */
    public static List getTimeList(int beginYear, int beginMonth, int endYear, int endMonth, int k)
    {
        List list = new ArrayList();
        if (beginYear == endYear)
        {
            for (int j = beginMonth; j <= endMonth; j++)
            {
                list.add(getTimeList(beginYear, j, k));
            }
        }
        else
        {
            {
                for (int j = beginMonth; j < 12; j++)
                {
                    list.add(getTimeList(beginYear, j, k));
                }
                for (int i = beginYear + 1; i < endYear; i++)
                {
                    for (int j = 0; j < 12; j++)
                    {
                        list.add(getTimeList(i, j, k));
                    }
                }
                for (int j = 0; j <= endMonth; j++)
                {
                    list.add(getTimeList(endYear, j, k));
                }
            }
        }
        return list;
    }

    /**
     * 获取本月是哪一月
     *
     * @return
     */
    public static int getNowMonth()
    {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }

    /**
     * 获取今年是哪一年
     *
     * @return
     */
    public static Integer getNowYear()
    {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }

    public static Map<String, Object> getHalfYear()
    {
        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 1);
        for (int i = 0; i < 6; i++)
        {
            c.add(Calendar.MONTH, -1);
            Date m = c.getTime();
            String mon = format.format(m);
            map.put(mon, 0);
        }
        return map;
    }

    /**
     *      * 获取某一月份的前六个月
     *      * @param date 日期,格式:"2018-10"
     *      * @return
     *
     */
    public static List<String> getSixMonth(String date){
        //返回值
        List<String> list = new ArrayList<>();
        int month = Integer.parseInt(date.substring(5, 7));
        int year = Integer.parseInt(date.substring(0, 4));
        for (int i = 5; i >= 0; i--) {
            if (month > 6) {
                if (month - i >= 10) {
                    list.add(year + "-" + String.valueOf(month - i));
                } else {
                    list.add(year + "-0" + String.valueOf(month - i));
                }
            } else {
                if (month - i <= 0) {
                    if (month - i + 12 >= 10) {
                        list.add(String.valueOf(year - 1) + "-" + String.valueOf(month - i + 12));
                    } else {
                        list.add(String.valueOf(year - 1) + "-0" + String.valueOf(month - i + 12));
                    }
                } else {
                    if (month - i >= 10) {
                        list.add(String.valueOf(year) + "-" + String.valueOf(month - i));
                    } else {
                        list.add(String.valueOf(year) + "-0" + String.valueOf(month - i));
                    }
                }
            }
        }
        return list;
    }

    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    public static Map<String, Object> getYearDateMap()
    {
        Map<String, Object> map = new TreeMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 1);
        for (int i = 0; i < 6; i++)
        {
            c.add(Calendar.MONTH, -1);
            Date m = c.getTime();
            String mon = format.format(m);
            map.put(mon, 0);
        }
        return map;
    }

    public static Map<String, Object> getWeekDateMap()
    {
        Map<String, Object> map = new TreeMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        for (int i = 0; i < 7; i++)
        {
            c.add(Calendar.DATE, -1);
            Date m = c.getTime();
            String mon = format.format(m);
            map.put(mon, 0);
        }
        return map;
    }

    public static Map<String, Object> getMonthDateMap()
    {
        Map<String, Object> map = new TreeMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        for (int i = 0; i < 31; i++)
        {
            c.add(Calendar.DATE, -1);
            Date m = c.getTime();
            String mon = format.format(m);
            map.put(mon, 0);
        }
        return map;
    }

    public static Map<String, Object> MatchMap(List<Map<String, Object>> mapList)
    {
        Map<String, Object> monthDateMap = getMonthDateMap();

        for (Map m : mapList)
        {
            for (String key : monthDateMap.keySet())
            {
                if (m != null && key.equals(m.get("days")))
                {
                    monthDateMap.put(key, m.get("count"));
                }
            }
        }
        return monthDateMap;
    }

    /**
     * 周统计处理
     * @param mapList
     * @return
     */
    public static Map<String, Object> WeekMatchMap(List<Map<String, Object>> mapList)
    {
        Map<String, Object> weekDateMap = getWeekDateMap();
        if (mapList.isEmpty()){
            return weekDateMap;
        }
        for (Map m : mapList)
        {
            for (String key : weekDateMap.keySet())
            {
                if (m != null && key.equals(m.get("days")))
                {
                    weekDateMap.put(key, m.get("count"));
                }
            }
        }
        return weekDateMap;
    }

    public static Map<String, Object> YearMatchMap(List<Map<String, Object>> mapList)
    {
        Map<String, Object> YearDateMap = getYearDateMap();

        for (Map m : mapList)
        {
            for (String key : YearDateMap.keySet())
            {
                if (m != null && key.equals(m.get("days")))
                {
                    YearDateMap.put(key, m.get("count"));
                }
            }
        }
        return YearDateMap;
    }

    /**
     * 获取某年某月按天切片日期集合（某个月间隔多少天的日期集合）
     *
     * @param beginYear
     * @param beginMonth
     * @param k
     * @return
     */
    public static List getTimeList(int beginYear, int beginMonth, int k)
    {
        List list = new ArrayList();
        Calendar begincal = new GregorianCalendar(beginYear, beginMonth, 1);
        int max = begincal.getActualMaximum(Calendar.DATE);
        for (int i = 1; i < max; i = i + k)
        {
            list.add(dateFormat(begincal.getTime(), "yyyy-MM-dd HH:mm:ss"));
            begincal.add(Calendar.DATE, k);
        }
        begincal = new GregorianCalendar(beginYear, beginMonth, max);
        list.add(dateFormat(begincal.getTime(), "yyyy-MM-dd HH:mm:ss"));
        return list;
    }

    /**
     * 获取当天的开始时间凌晨
     * @return
     */
    public static Date getStartDay()
    {
        java.time.LocalDate localDate = java.time.LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(localDate, LocalTime.MIN);//当天零点
        ZoneId zoneId = ZoneId.systemDefault();
        Date startDate = Date.from(todayStart.atZone(zoneId).toInstant());
        return startDate;
    }

    /**
     * 获取当天的开始时间凌晨
     * @return
     */
    public static long getStartDay(int hour)
    {
        java.time.LocalDate localDate = java.time.LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(localDate, LocalTime.MIN.withHour(hour));//当天 hour 点

        ZoneId zoneId = ZoneId.systemDefault();
        Date startDate = Date.from(todayStart.atZone(zoneId).toInstant());
        return startDate.getTime();
    }

    /**
     * 获取任意几天前的凌晨开始时间
     * @return
     */
    public static Date getStartDayToNum(int num)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, - num);
        Date time = c.getTime();
        Instant instant = time.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime startTime = LocalDateTime.of(instant.atZone(zoneId).toLocalDate(), LocalTime.MIN);//当天零点
        Date startDate = Date.from(startTime.atZone(zoneId).toInstant());
        return startDate;
    }


    /**
     * 获取任意几天前的结束时间
     * @return
     */
    public static Date getEndDayToNum(int num)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, - num);
        Date time = c.getTime();
        Instant instant = time.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime endTime = LocalDateTime.of(instant.atZone(zoneId).toLocalDate(), LocalTime.MAX);//当天零点
        Date endDate = Date.from(endTime.atZone(zoneId).toInstant());
        return endDate;
    }


}
