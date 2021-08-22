package com.revature.p1.orm;

import com.revature.p1.orm.util.Metamodel;

public class RandomDriver {
    public static void main(String[] args) {
        StringBuilder test = new StringBuilder("123");
        test.insert(1,"aa");
        System.out.println(test.toString());
    }
}
