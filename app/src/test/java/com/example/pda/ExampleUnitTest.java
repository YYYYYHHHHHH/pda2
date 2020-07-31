package com.example.pda;

import com.example.pda.util.TimeUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void text1() {
        ArrayList<String> dateList = TimeUtils.getDateList(5);
        for (String s : dateList) {
            System.out.println(s);
        }
    }
}