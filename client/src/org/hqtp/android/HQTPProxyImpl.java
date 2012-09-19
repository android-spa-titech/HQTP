package org.hqtp.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
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
        HttpResponse response = builder.get("auth/")
            .param("access_token_key", access_token_key)
            .param("access_token_secret", access_token_secret)
            .send();

        JSONObject json = toJSON(response.getEntity());
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Authentication failed. : /api/auth returned status='"
                    + json.getString("status") + "'");
        }

        return User.fromJSON(json.getJSONObject("user"));
    }

    @Override
    public List<Post> getTimeline(int lectureId) throws IOException, HQTPAPIException, JSONException,
            java.text.ParseException {
        HttpResponse response = builder.get("lecture/timeline/")
            .param("id", lectureId)
            .send();
        JSONObject json = toJSON(response.getEntity());
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
        HttpResponse response = request.send();

        JSONObject json = toJSON(response.getEntity());
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Posting to timeline failed. : POST /api/lecture/timeline/ returned status="
                    + json.getString("status"));
        }
        return Post.fromJSON(json.getJSONObject("post"));
    }

    private static JSONObject toJSON(HttpEntity entity) throws ParseException, IOException, JSONException
    {
        String response = EntityUtils.toString(entity);
        entity.consumeContent(); // ここでconsumeするのはキモいのだろうか・・・
        return new JSONObject(response);
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
}
