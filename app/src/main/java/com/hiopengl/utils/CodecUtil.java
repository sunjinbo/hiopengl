package com.hiopengl.utils;

public class CodecUtil {
    public static int getSize(int size) {
        if (size % 4 == 0) {
            return size;
        } else {
            return size + (4 - size % 4);
        }
    }
}
