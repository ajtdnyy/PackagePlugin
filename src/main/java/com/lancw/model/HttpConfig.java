package com.lancw.model;

/**
 * File:HttpConfig.java
 * Date:2016-4-26 17:06:53
 * Encoding:UTF-8
 * Author:lancw
 * Description:
 */
public class HttpConfig {

    private String name;
    private String url;
    private String charset;
    private String headerStr;
    private String parameterStr;
    private String requestType;
    private String encodeType;
    private String encodeFieldName;
    private String encodeKey;
    private String contentType;
    private Boolean packHead;
    private Boolean lowercaseEncode;
    private Boolean sendXML;

    public HttpConfig(String name, String url, String charset, String headerStr, String parameterStr, String requestType, String contentType) {
        this.name = name;
        this.url = url;
        this.charset = charset;
        this.headerStr = headerStr;
        this.parameterStr = parameterStr;
        this.requestType = requestType;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getHeaderStr() {
        return headerStr;
    }

    public void setHeaderStr(String headerStr) {
        this.headerStr = headerStr;
    }

    public String getParameterStr() {
        return parameterStr;
    }

    public void setParameterStr(String parameterStr) {
        this.parameterStr = parameterStr;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }

    public String getEncodeFieldName() {
        return encodeFieldName;
    }

    public void setEncodeFieldName(String encodeFieldName) {
        this.encodeFieldName = encodeFieldName;
    }

    public String getEncodeKey() {
        return encodeKey;
    }

    public void setEncodeKey(String encodeKey) {
        this.encodeKey = encodeKey;
    }

    public Boolean getPackHead() {
        return packHead;
    }

    public void setPackHead(Boolean packHead) {
        this.packHead = packHead;
    }

    public Boolean getLowercaseEncode() {
        return lowercaseEncode;
    }

    public void setLowercaseEncode(Boolean lowercaseEncode) {
        this.lowercaseEncode = lowercaseEncode;
    }

    public Boolean getSendXML() {
        return sendXML;
    }

    public void setSendXML(Boolean sendXML) {
        this.sendXML = sendXML;
    }

}
