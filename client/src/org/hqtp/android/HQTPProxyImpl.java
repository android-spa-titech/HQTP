package org.hqtp.android;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

//HQTPサーバーとの通信をラップするクラス
//とりあえずSingletonにしておきます
@Singleton
public class HQTPProxyImpl implements HQTPProxy {

    private final String api_gateway;
    private CookieStore cookie_store = null;

    @Inject
    public HQTPProxyImpl(@Named("HQTP API Endpoint URL") String api_gateway) {
        super();
        this.api_gateway = api_gateway;
    }

    @Override
    public boolean authenticate(String access_token_key, String access_token_secret) throws IOException, JSONException,
            HQTPAPIException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token_key", access_token_key);
        params.put("access_token_secret", access_token_secret);
        HttpResponse response = sendByGet("auth/", params);
        JSONObject json = toJSON(response.getEntity());
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Authentication failed. : /api/auth returned status='"
                    + json.getString("status") + "'");
        }

        return true;
    }

    @Override
    public boolean postQuestion(String title, String body) throws JSONException, IOException, HQTPAPIException {
        HttpResponse response;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("title", title);
        params.put("body", body);
        response = sendByPost("post/", params);
        JSONObject json = toJSON(response.getEntity());
        Log.d("info", json.toString());
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Post question failed. : /api/post returned status='"
                    + json.getString("status") + "'");
        }

        return true;
    }

    @Override
    public List<Question> getQuestions() throws JSONException, IOException, HQTPAPIException {
        // ネットワークからの読込テスト
        JSONObject json;
        HttpResponse response = sendByGet("get/", null);
        json = toJSON(response.getEntity());
        Log.d("info", json.toString());

        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Getting questions failed. : /api/get returned status='"
                    + json.getString("status") + "'");
        }
        JSONArray array = json.getJSONArray("posts");
        ArrayList<Question> res = new ArrayList<Question>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject post = array.getJSONObject(i);
            res.add(new Question(post.getString("title"), post.getString("body"), null));
        }
        return res;
    }

    @Override
    public List<Post> getTimeline(int lectureId) throws IOException, HQTPAPIException, JSONException,
            java.text.ParseException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", Integer.toString(lectureId));
        HttpResponse response = sendByGet("lecture/timeline/", params);
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
            throws IOException, HQTPAPIException, JSONException, java.text.ParseException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", Integer.toString(lectureId));
        params.put("body", body);
        if (prevVirtualTimestamp >= 0) {
            params.put("before_virtual_ts", Long.toString(prevVirtualTimestamp));
        }
        if (nextVirtualTimestamp >= 0) {
            params.put("after_virtual_ts", Long.toString(nextVirtualTimestamp));
        }
        HttpResponse response = sendByPost("lecture/timeline/", params);
        JSONObject json = toJSON(response.getEntity());
        if (!isStatusOK(json)) {
            throw new HQTPAPIException("Posting to timeline failed. : POST /api/lecture/timeline/ returned status="
                    + json.getString("status"));
        }
        return Post.fromJSON(json.getJSONObject("post"));
    }

    private HttpResponse sendByGet(String path, Map<String, String> params)
            throws ClientProtocolException, IOException, HQTPAPIException {
        Uri.Builder builder = Uri.parse(api_gateway).buildUpon();
        builder.appendEncodedPath(path);
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                builder.appendQueryParameter(param.getKey(), param.getValue());
            }
        }
        HttpGet http_get = new HttpGet(builder.build().toString());
        return send(http_get);
    }

    private HttpResponse sendByPost(String path, Map<String, String> params)
            throws ClientProtocolException, IOException, HQTPAPIException {
        Uri.Builder builder = Uri.parse(api_gateway).buildUpon();
        builder.appendEncodedPath(path);
        HttpPost http_post = new HttpPost(builder.build().toString());
        if (params != null) {
            List<NameValuePair> form_params = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> param : params.entrySet()) {
                form_params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            try {
                http_post.setEntity(new UrlEncodedFormEntity(form_params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace(); // I think here is unreachable...
            }
        }
        return send(http_post);
    }

    private HttpResponse send(HttpUriRequest request) throws ClientProtocolException, IOException, HQTPAPIException
    {
        // TODO: Timeoutの設定をすべき？
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        client.setCookieStore(cookie_store);
        try {
            response = client.execute(request);
            int status_code = response.getStatusLine().getStatusCode();
            if (status_code != HttpStatus.SC_OK && status_code != HttpStatus.SC_CREATED) {
                throw new HQTPAPIException("HTTP response returned failure. : return Http status code="
                        + Integer.toString(status_code));
            }
            this.cookie_store = client.getCookieStore();
        } finally {
            client.getConnectionManager().shutdown();
        }
        return response;
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
}
