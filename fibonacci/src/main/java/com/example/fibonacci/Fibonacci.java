package com.example.fibonacci;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 斐波那契数列：迭代实现，O(n) 时间、O(1) 空间（单值计算）。
 */
public final class Fibonacci {

    private Fibonacci() {
    }

    /** 返回第 n 项（F(0)=0, F(1)=1）。 */
    public static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }
        if (n <= 1) {
            return n;
        }
        long a = 0;
        long b = 1;
        for (int i = 2; i <= n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return b;
    }

    /** 生成前 count 项（count &gt; 0 时以 F(0) 开头）。 */
    public static List<Long> sequence(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>(count);
        result.add(0L);
        if (count == 1) {
            return result;
        }
        result.add(1L);
        for (int i = 2; i < count; i++) {
            result.add(result.get(i - 1) + result.get(i - 2));
        }
        return result;
    }
}
