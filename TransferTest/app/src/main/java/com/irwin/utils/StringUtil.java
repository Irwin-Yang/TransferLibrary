package com.irwin.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Irwin on 2015/11/10.
 */
public class StringUtil {
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    private StringUtil() {
       /* cannot be instantiated */
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(@Nullable CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

    /**
     * Returns the length that the specified CharSequence would have if
     * spaces and control characters were trimmed from the start and end,
     * as by {@link String#trim}.
     */
    public static int getTrimmedLength(CharSequence s) {
        int len = s.length();

        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start++;
        }

        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }

        return end - start;
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.</i></p>
     *
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param context
     * @param target  Target string to check if empty.
     * @param resId   Resource id of String which will be used if target is empty.
     * @return
     */
    public static String getStringIfEmpty(Context context, String target, int resId) {
        if (isEmpty(target)) {
            return context.getResources().getString(resId);
        }
        return target;
    }

    public static String getDoubleIfZero(Context context, Double target, int resId) {
        if (isNullDouble(target)) {
            return context.getString(resId);
        }
        return new BigDecimal(target).toString();
    }

    /**
     * Tell if double is 0 or null.
     *
     * @param target
     * @return
     */
    public static boolean isNullDouble(Double target) {
        return (target == null || target == 0D);
    }

    public static String getDoubleIfZero(Double target, String defValue) {
        if (isNullDouble(target)) {
            return defValue;
        }
        return new BigDecimal(target).toString();
    }

    public static String getDoubleIfZeroForEDT(Double target) {
        return getDoubleIfZero(target, "");
    }

    public static String formatDateTime(Object dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DATETIME_FORMAT.format(dateTime);
    }

}
