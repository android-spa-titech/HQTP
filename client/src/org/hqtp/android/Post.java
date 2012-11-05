package org.hqtp.android;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class Post implements Comparable<Post> {
    private final static int VIRTUAL_TS_SCALE = 100000;

    private int id;
    private String body;
    private Date time;
    private long virtualTimestamp;
    private User user;
    private Lecture lecture;

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

    public User getUser() {
        return user;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public Post(int id, String body, Date time, long virtualTimestamp,
            User user, Lecture lecture) {
        this.id = id;
        this.body = body;
        this.time = time;
        this.virtualTimestamp = virtualTimestamp;
        this.user = user;
        this.lecture = lecture;
    }

    public static Post fromJSON(JSONObject json) throws JSONException, ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        User user = User.fromJSON(json.getJSONObject("user"));
        Lecture lecture = Lecture.fromJSON(json.getJSONObject("lecture"));
        Post post = new Post(
                json.getInt("id"),
                json.getString("body"),
                df.parse(json.getString("time")),
                json.getLong("virtual_ts"),
                user,
                lecture
                );
        return post;
    }

    public static Date virtualTimestampToDate(long virtualTimestamp) {
        return new Date(virtualTimestamp / VIRTUAL_TS_SCALE);
    }

    public static long dateToVirtualTimestamp(Date date) {
        return date.getTime() * VIRTUAL_TS_SCALE;
    }

    @Override
    public int compareTo(Post another) {
        if (this.virtualTimestamp < another.virtualTimestamp) {
            return -1;
        } else if (this.virtualTimestamp == another.virtualTimestamp) {
            return 0;
        } else {
            return 1;
        }
    }
}
