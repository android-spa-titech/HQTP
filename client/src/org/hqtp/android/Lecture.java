package org.hqtp.android;

public class Lecture {
    private int id;
    private String name;
    private String code;

    public Lecture(int id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

}
