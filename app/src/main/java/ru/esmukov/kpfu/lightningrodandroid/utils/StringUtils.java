package ru.esmukov.kpfu.lightningrodandroid.utils;

/**
 * Created by kostya on 25/03/2017.
 */

public class StringUtils {

    public static String capfirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
