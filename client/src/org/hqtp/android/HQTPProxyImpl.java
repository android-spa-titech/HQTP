package org.hqtp.android;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HQTPProxyImpl implements HQTPProxy {
    @Inject
    APIRequestBuilder builder;

    // DEBUG: デバッグ用のエンドポイント切り替え
    public void setEndpoint(String api_gateway) {
        builder.setEndpoint(api_gateway);
    }

    @Override
    public User authenticate(String access_token_key, String access_token_secret) throws HQTPAPIException, IOException,
            JSONException {
        String response = builder.get("auth/")
            .param("access_token_key", access_token_key)
            .param("access_token_secret", access_token_secret)
            .send();

        JSONObject json = new JSONObject(response);
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Authentication failed. : /api/auth returned status='"
                    + json.getString("status") + "'");
        }

        return User.fromJSON(json.getJSONObject("user"));
    }

    @Override
    public List<Post> getTimeline(int lectureId) throws IOException, HQTPAPIException, JSONException,
            java.text.ParseException {
        String response = builder.get("lecture/timeline/")
            .param("id", lectureId)
            .send();
        JSONObject json = new JSONObject(response);
        ArrayList<Post> posts = new ArrayList<Post>();
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Getting timeline failed. : GET /api/lecture/timeline/?id=" + lectureId
                    + " returned status=" + json.getString("status"));
        }
        JSONArray array = json.getJSONArray("posts");
        for (int i = 0, length = array.length(); i < length; i++) {
            posts.add(Post.fromJSON(array.getJSONObject(i)));
        }
        return posts;
    }

    @Override
    public Post postTimeline(String body, int lectureId, long prevVirtualTimestamp, long nextVirtualTimestamp)
            throws HQTPAPIException, IOException, JSONException, java.text.ParseException {
        APIRequestBuilder.APIRequest request = builder.post("lecture/timeline/")
            .param("id", lectureId)
            .param("body", body);
        if (prevVirtualTimestamp >= 0) {
            request.param("before_virtual_ts", prevVirtualTimestamp);
        }
        if (nextVirtualTimestamp >= 0) {
            request.param("after_virtual_ts", nextVirtualTimestamp);
        }

        JSONObject json = new JSONObject(request.send());
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Posting to timeline failed. : POST /api/lecture/timeline/ returned status="
                    + json.getString("status"));
        }
        return Post.fromJSON(json.getJSONObject("post"));
    }

    private static boolean isStatusOK(JSONObject json) throws JSONException
    {
        return json.has("status") && json.getString("status").equals("OK");
    }

    @Override
    public Lecture addLecture(String lectureCode, String lectureName) throws HQTPAPIException, IOException,
            JSONException, java.text.ParseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Lecture> getLectures() throws HQTPAPIException, IOException, JSONException, ParseException {
        // TODO Auto-generated method stub
        List<Lecture> lectures = new ArrayList<Lecture>();
        lectures.add(new Lecture(1, "Test Class", "Test Class Code"));
        return lectures;
    }
}
