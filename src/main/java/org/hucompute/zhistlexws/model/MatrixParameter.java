package org.hucompute.zhistlexws.model;

public class MatrixParameter {

    protected String base;
    protected String key;
    protected String value;

    public MatrixParameter(String base, String key, String value) {
        this.base = base;
        this.key = key;
        this.value = value;
    }

    public String getBase() {
        return base;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
