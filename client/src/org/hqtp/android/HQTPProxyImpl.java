package org.hqtp.android;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
    public boolean authenticate(String access_token_key, String access_token_secret) {
        // TODO: implement
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token_key", access_token_key);
        params.put("access_token_secret", access_token_secret);
        boolean res = false;
        try {
            HttpResponse response = sendByGet("auth/", params);
            JSONObject json = toJSON(response.getEntity());
            if (json.getString("status") == "OK") {
                res = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean postQuestion(String title, String body) {
        HttpResponse response;
        HashMap<String , String> params = new HashMap<String, String>();
        params.put("title", title);
        params.put("body", body);
        boolean res = false;
        try{
            response = sendByPost("post/", params);
            JSONObject json = toJSON(response.getEntity());
            Log.d("info", json.toString());
            if(json.getString("status")=="OK"){
                res = true;
            }
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<Question> getQuestions() {
        // ネットワークからの読込テスト
        JSONObject json;
        try {
            HttpResponse response = sendByGet("get/",null);
            //TODO: ネットワークまわりの例外とレスポンスまわりの例外処理がごっちゃになってるのをどうにかしたい
            json = toJSON(response.getEntity());
            Log.d("info", json.toString());
        } catch (Exception e1) {
            //TODO: 真面目に例外処理しましょう
            Log.d("err", e1.getMessage());
            e1.printStackTrace();
            return null;
        }

        try{
            if(json.getString("status")!="OK"){
                //TODO: エラー処理
            }
            JSONArray array = json.getJSONArray("posts");
            ArrayList<Question> res = new ArrayList<Question>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject post = array.getJSONObject(i);
                res.add(new Question(post.getString("title"), post.getString("body"), null));
            }
            return res;
        }catch(JSONException e){
            //TODO: JSONまわりのエラー対応
            Log.d("err",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private HttpResponse sendByGet(String path, Map<String, String> params)
            throws ClientProtocolException, IOException {
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
            throws ClientProtocolException, IOException {
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
                e.printStackTrace();    // I think here is unreachable...
            }
        }
        return send(http_post);
    }
    
    private HttpResponse send(HttpUriRequest request) throws ClientProtocolException, IOException
    {
        //TODO: Timeoutの設定をすべき？
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        client.setCookieStore(cookie_store);
        try {
            response = client.execute(request);
            this.cookie_store = client.getCookieStore();
        } finally {
            client.getConnectionManager().shutdown();
        }
        return response;
    }
    
    private static JSONObject toJSON(HttpEntity entity) throws ParseException, IOException, JSONException
    {
        String response = EntityUtils.toString(entity);
        entity.consumeContent();    //ここでconsumeするのはキモいのだろうか・・・
        return new JSONObject(response);
    }
}
