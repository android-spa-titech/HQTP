package org.hqtp.android;

import java.io.File;
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
        return postTimeline(body, null, lectureId, prevVirtualTimestamp, nextVirtualTimestamp);
    }

    @Override
    public Post postTimeline(File image, int lectureId, long prevVirtualTimestamp, long nextVirtualTimestamp)
            throws IOException, HQTPAPIException, JSONException, ParseException {
        return postTimeline(null, image, lectureId, prevVirtualTimestamp, nextVirtualTimestamp);
    }

    private Post postTimeline(String body, File image, int lectureId, long prevVirtualTimestamp,
            long nextVirtualTimestamp)
            throws IOException, HQTPAPIException, JSONException, ParseException {
        APIRequestBuilder.APIRequest request = builder.post("lecture/timeline/").param("id", lectureId);
        if (body != null) {
            request.param("body", body);
        }
        if (image != null) {
            try {
                request.param("image", image);
            } catch (Exception e) { // Maybe unreachable
                e.printStackTrace();
            }
        }
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

    @Override
    public Lecture addLecture(String lectureCode, String lectureName) throws HQTPAPIException, IOException,
            JSONException, java.text.ParseException {
        String response = builder.post("lecture/add/")
            .param("code", lectureCode)
            .param("name", lectureName)
            .send();

        JSONObject json = new JSONObject(response);
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Creating a lecture was failed. : POST /api/lecture/add/ returned status="
                    + json.getString("status"));
        }

        Lecture lecture = Lecture.fromJSON(json.getJSONObject("lecture"));
        if (!isCreated(json)) {
            throw new LectureAlreadyCreatedException(lecture, "Lecture is already created.");
        } else {
            return lecture;
        }
    }

    @Override
    public List<Lecture> getLectures() throws HQTPAPIException, IOException, JSONException, ParseException {
        String response = builder.get("lecture/get/").send();

        JSONObject json = new JSONObject(response);
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Getting lectures was failed. : GET /api/lecture/get/ returned status="
                    + json.getString("status"));
        }

        ArrayList<Lecture> lectures = new ArrayList<Lecture>();
        JSONArray array = json.getJSONArray("lectures");
        for (int i = 0, length = array.length(); i < length; i++) {
            lectures.add(Lecture.fromJSON(array.getJSONObject(i)));
        }
        return lectures;
    }

    private static boolean isStatusOK(JSONObject json) throws JSONException {
        return json.has("status") && json.getString("status").equals("OK");
    }

    private static boolean isCreated(JSONObject json) throws JSONException {
        return json.has("created") && json.getBoolean("created");
    }
}
