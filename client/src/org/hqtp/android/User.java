package org.hqtp.android;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int id;
    private String name;
    private String iconURL;

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
        User user = new User();
        user.id = json.getInt("id");
        user.name = json.getString("name");
        user.iconURL = json.getString("icon_url");
        return user;
    }
}
