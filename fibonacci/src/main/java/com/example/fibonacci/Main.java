package com.example.fibonacci;

/**
 * 命令行演示：默认计算 F(10) 并打印前 12 项。
 * 可通过参数指定 n，例如：mvn exec:java -Dexec.args=20
 */
public class Main {

    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 10;
        System.out.println("fib(" + n + ") = " + Fibonacci.fibonacci(n));
        System.out.println("first 12: " + Fibonacci.sequence(12));
    }
}
