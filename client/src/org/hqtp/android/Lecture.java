package org.hqtp.android;

public class Lecture {
    private String id;
    private String name;
    private String code;

    public Lecture(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

}
