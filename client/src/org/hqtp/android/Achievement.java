package org.hqtp.android;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

// TODO: どのユーザーが所有する実績か示すフィールドが必要？
public class Achievement {
    private int id;
    private String name;
    private int point;
    private Date created_at;

    public Achievement(int id, String name, int point, Date created_at) {
        this.id = id;
        this.name = name;
        this.point = point;
        this.created_at = created_at;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getPoint() {
        return point;
    }

    public Date getCreatedAt()
    {
        return created_at;
    }

    public static Achievement fromJSON(JSONObject json) throws JSONException, ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        return new Achievement(
                json.getInt("id"),
                json.getString("name"),
                json.getInt("point"),
                df.parse(json.getString("created_at")));
    }
}
