package com.pewee.rpc.test;

import com.pewee.rpc.anno.Implbean;

@Implbean(interfaces="com.pewee.rpc.test.Calculate")
public class CalculateImpl implements Calculate {
    //两数相加
    public int add(int a, int b) {
        return a + b;
    }
}