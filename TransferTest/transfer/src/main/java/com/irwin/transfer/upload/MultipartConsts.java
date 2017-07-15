package com.irwin.transfer.upload;



public interface MultipartConsts {


    public static final String CRLF = "\r\n";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_LENGTH= "Content-Length";
    public static final String HEADER_CONNECTION= "Connection";
    public static final String HEADER_ACCEPT= "Accept";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; charset=%s; boundary=%s";
    public static final String BINARY = "binary";
    public static final String EIGHT_BIT = "8bit";
    public static final String FORM_DATA = "form-data; name=\"%s\"";
    public static final String BOUNDARY_PREFIX = "--";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final String FILENAME = "filename=\"%s\"";
    public static final String COLON_SPACE = ": ";
    public static final String SEMICOLON_SPACE = "; ";

    public static final String MIME_TYPE_ALL= "*/*";
    public static final String MIME_TYPE_STREAM = "application/octet-stream";
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CONTENT_TYPE_TEXT = "text/plain";


}
