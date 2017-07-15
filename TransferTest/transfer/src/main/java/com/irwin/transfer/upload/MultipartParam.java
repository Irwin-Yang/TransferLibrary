package com.irwin.transfer.upload;


/**
 * Created by IrwinX on 2016/7/24.
 */
public class MultipartParam implements MultipartConsts {
    public String Key;

    public String ContentType;

    public String Value;

    public MultipartParam(String key, String value) {
        this(key, CONTENT_TYPE_TEXT, value);
    }

    public MultipartParam(String key, String contentType, String value) {
        Key = key;
        ContentType = contentType;
        Value = value;
    }
}
