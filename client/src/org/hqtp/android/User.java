package org.hqtp.android;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int id;
    private String name;
    private String iconURL;
    private int totalPoint;

    public User(int id, String name, String iconURL, int totalPoint) {
        this.id = id;
        this.name = name;
        this.iconURL = iconURL;
        this.totalPoint = totalPoint;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getIconURL()
    {
        return iconURL;
    }

    public int getTotalPoint()
    {
        return totalPoint;
    }

    public static User fromJSON(JSONObject json) throws JSONException
    {
        User user = new User(
                json.getInt("id"),
                json.getString("name"),
                json.getString("icon_url"),
                json.getInt("total_point")
                );
        return user;
    }
}
