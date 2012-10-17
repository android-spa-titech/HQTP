package org.hqtp.android;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static Lecture fromJSON(JSONObject json) throws JSONException
    {
        int id = json.getInt("id");
        String name = json.getString("name");
        String code = json.getString("code");
        return new Lecture(id, name, code);
    }
}
