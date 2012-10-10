package org.hqtp.android;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Post {
    private int id;
    private String body;
    private Date time;
    private long virtualTimestamp;

    // TODO: toggle comment
    // private Lecture lecture;
    private User user;

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public Date getTime() {
        return time;
    }

    public long getVirtualTimestamp()
    {
        return virtualTimestamp;
    }

    public Post(int id, String body, Date time, long virtualTimestamp) {
        this.id = id;
        this.body = body;
        this.time = time;
        this.virtualTimestamp = virtualTimestamp;
    }

    public static Post fromJSON(JSONObject json) throws JSONException, ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Post post = new Post(
                json.getInt("id"),
                json.getString("body"),
                df.parse(json.getString("time")),
                json.getLong("virtual_ts")
                );
        post.user = User.fromJSON(json.getJSONObject("user"));
        // TODO: toggle comment
        // post.lecture = Lecture.fromJSON(json.getString("lecture"));
        return post;
    }

    public User getUser() {
        return user;
    }
    // TODO: toggle comment
    //
    // public Lecture getLecture() {
    // //TODO: implement
    // return null;
    // }
}
