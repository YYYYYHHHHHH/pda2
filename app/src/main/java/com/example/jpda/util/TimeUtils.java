package com.example.jpda.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public static ArrayList<String> getDateList(int day) {
        //创建集合储存日期
        ArrayList<String> dateList = new ArrayList<>();
        //获取当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(new Date().getTime());
        String format1 = sdf.format(new Date().getTime());
        //此处是我项目的需求，需要保存当前日期在第一位，你也可以根据自己的需求自行决定
        dateList.add(format);
        for (int i = 0; i < day;i++){
            // 将当前的日期转为Date类型，ParsePosition(0)表示从第一个字符开始解析
            Date date = sdf.parse(format, new ParsePosition(0));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // add方法的第二个参数n中，正数表示该日期后n天，负数表示该日期的前n天，你可根据自己的需求自行决定,
            //如果项目中需要多次调用，你也可把这个参数，通过方法动态传入
            calendar.add(Calendar.DATE, -1);
            Date date1 = calendar.getTime();
            format = sdf.format(date1);
            dateList.add(0,format);
        }
        for (int i = 0; i < day;i++){
            // 将当前的日期转为Date类型，ParsePosition(0)表示从第一个字符开始解析
            Date date = sdf.parse(format1, new ParsePosition(0));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // add方法的第二个参数n中，正数表示该日期后n天，负数表示该日期的前n天，你可根据自己的需求自行决定,
            //如果项目中需要多次调用，你也可把这个参数，通过方法动态传入
            calendar.add(Calendar.DATE, 1);
            Date date1 = calendar.getTime();
            format1 = sdf.format(date1);
            dateList.add(format1);
        }



        return dateList;
    }
}
