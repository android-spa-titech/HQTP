package org.hqtp.android;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int id;
    private String name;
    private String iconURL;

    public User(int id, String name, String iconURL) {
        this.id = id;
        this.name = name;
        this.iconURL = iconURL;
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

    public static User fromJSON(JSONObject json) throws JSONException
    {
        User user = new User(
                json.getInt("id"),
                json.getString("name"),
                json.getString("icon_url")
                );
        return user;
    }
}
