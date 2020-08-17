package com.example.jpda;

import android.os.Process;
import android.util.Log;

import com.example.jpda.util.TimeUtils;

import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

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
    @Test
    public void text() {
        int[] ints = {-1, -100, 3, 99};
        rotate(ints, 2);
        System.out.println(Arrays.toString(ints));
    }
    public void rotate(int[] nums, int k) {
        int[] ints = Arrays.copyOf(nums, nums.length);
        for (int i = 0; i < ints.length; i++) {
            nums[(i + k) % nums.length] = ints[i];
        }
    }
    public int dg(List<Integer> arrayList, int index, int num, int k) {
        int item = arrayList.get(index);
        int n = (num == arrayList.size()) ? arrayList.get(0) :
                dg(arrayList, (index - k + arrayList.size()) % arrayList.size(), num + 1, k);
        arrayList.set(index, n);
        return item;
    }

    @Test
    public void text2() {
        Thread thread = new Thread(new BaseRunable());
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    class BaseRunable implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            Long num = 0L;
            for (int i = 0; i < 1000000000; i++) {
                num *= i;
                num *= i;
            }
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
    }
}