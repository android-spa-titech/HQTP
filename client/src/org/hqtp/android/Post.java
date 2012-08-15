package org.hqtp.android;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Post {
    private String id;
    private String body;
    private Date time;
    private String virtualTimestamp;

    // TODO: toggle comment
    // private Lecture lecture;
    // private User user;

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public Date getTime() {
        return time;
    }

    public String getVirtualTimestamp()
    {
        return virtualTimestamp;
    }

    public static Post fromJSON(JSONObject json) throws JSONException, ParseException {
        Post post = new Post();
        post.id = json.getString("id");
        post.body = json.getString("body");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        post.time = df.parse(json.getString("time"));
        post.virtualTimestamp = json.getString("virtual_ts");
        // TODO: toggle comment
        // post.user = User.fromJSON(json.getString("user"));
        // post.lecture = Lecture.fromJSON(json.getString("lecture"));
        return post;
    }
    // TODO: toggle comment
    // public User getUser() {
    // //TODO: implement
    // return null;
    // }
    //
    // public Lecture getLecture() {
    // //TODO: implement
    // return null;
    // }
}
