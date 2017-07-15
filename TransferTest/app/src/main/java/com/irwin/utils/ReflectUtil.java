package com.irwin.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Irwin on 2015/12/31.
 */
public class ReflectUtil {

    /**
     * Create a new instance of class and copy same fields' value from source. By which field name
     * and field type are the same be considered as same.
     *
     * @param source
     * @param classOfT Target class which must contains an empty constructor.
     * @param <T>
     * @return null if failed.
     */
    public static <T> T copyCreate(Object source, Class<? extends T> classOfT) {
        try {
            T ret = null;
            ret = classOfT.newInstance();
            Field[] sourceFields = getFields(source.getClass());
            Field[] targetFields = getFields(classOfT);
            if (sourceFields == null || targetFields == null) {
                return null;
            }
            Field sf = null;
            for (Field f : targetFields) {
                if ((sf = getRelativeField(sourceFields, f)) != null) {
                    f.setAccessible(true);
                    sf.setAccessible(true);
                    f.set(ret, sf.get(source));
                }
            }
            return ret;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get relative field by judge field name and type.
     *
     * @param array
     * @param f
     * @return
     */
    private static Field getRelativeField(Field[] array, Field f) {
        int modifiers = f.getModifiers();
        if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
            return null;
        }
        for (Field temp : array) {
            if (temp.getName().equals(f.getName()) && temp.getType() == f.getType()) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Copy all field's value from source Object to target Object.
     *
     * @param source
     * @param target
     * @param <SOURCE>
     * @param <TARGET>
     * @return 0 if success.otherwise error.
     */
    public static <SOURCE, TARGET extends SOURCE> int copyFields(SOURCE source, TARGET target) {
        try {
            Class clazz = source.getClass();
            Field[] fieldsArray = getFields(clazz);
            if (fieldsArray == null || fieldsArray.length == 0) {
                return 0;
            }
            for (Field f : fieldsArray) {
                // We can't and no need to copy constant fields.
                if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                f.set(target, f.get(source));
            }
            return 0;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get all declared fields include all access such as Private, Protected etc
     * of class and it's super classes.
     *
     * @param clazz
     * @return Field array or null if no fields.
     */
    public static Field[] getFields(Class clazz) {
        Field[] ret = null;
        do {
            Field[] array = clazz.getDeclaredFields();
            if (array != null && array.length > 0) {
                int offset = 0;
                if (ret == null) {
                    ret = new Field[array.length];
                } else {
                    Field[] newArray = new Field[ret.length + array.length];
                    offset = ret.length;
                    System.arraycopy(ret, 0, newArray, 0, ret.length);
                    ret = newArray;
                }
                System.arraycopy(array, 0, ret, offset, array.length);
            }
        } while ((clazz = clazz.getSuperclass()) != null);

        return ret;
    }
}
