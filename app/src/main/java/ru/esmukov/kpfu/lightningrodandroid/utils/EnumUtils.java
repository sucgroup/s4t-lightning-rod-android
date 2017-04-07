package ru.esmukov.kpfu.lightningrodandroid.utils;

/**
 * Created by kostya on 07/04/2017.
 */

public class EnumUtils {

    private static <E extends Enum> E findEnumByStringValue(String value, Class<E> enumClass) {
        if (value == null)
            throw new IllegalArgumentException("String value is null");

        value = value.toLowerCase();
        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().toLowerCase().equals(value))
                return enumValue;
        }
        throw new IllegalArgumentException();
    }

}
