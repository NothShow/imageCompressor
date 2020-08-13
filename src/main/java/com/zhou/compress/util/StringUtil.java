package com.zhou.compress.util;

public class StringUtil {
    public static boolean isEmpty(String str){
        return str == null || str.length() == 0 || "null".equalsIgnoreCase(str);
    }
}
