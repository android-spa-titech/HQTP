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
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import com.google.inject.Singleton;

//HQTPサーバーとの通信をラップするクラス
//とりあえずSingletonにしておきます
@Singleton
public class HQTPProxyImpl implements HQTPProxy {
    // APIを投げる先のURL
    // 今はデバッグのためエミュレータからPC側のローカルホストへアクセスするURLを指定している
    private final String api_gateway = "http://10.0.2.2:8000/api/";
    //private final String api_gateway = "http://www.hqtp.org/api/";

    private CookieStore cookie_store = null;

    // Instance methods
    @Override
    public HQTPProxy setAccessToken(String access_token) {
        // TODO: implement
        return this;
    }

    @Override
    public boolean authenticate() {
        // TODO: implement
        return false;
    }

    @Override
    public boolean postQuestion(String title, String body) {
        HttpResponse response;
        HashMap<String , String> params = new HashMap<String, String>();
        params.put("title", title);
        params.put("body", body);
        try{
            response = sendByPost("post/", params);
            JSONObject json = toJSON(response.getEntity());
            Log.d("info", json.toString());
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        // TODO: 妥当な戻り値を返す
        return false;
    }

    @Override
    public List<Question> getQuestions() {
        // ネットワークからの読込テスト
        HttpResponse response;
        try {
            response = sendByGet("get/",null);
            //TODO: ネットワークまわりの例外とレスポンスまわりの例外処理がごっちゃになってるのをどうにかしたい
            JSONObject json = toJSON(response.getEntity());
            Log.d("info", json.toString());
        } catch (Exception e1) {
            Log.d("err", e1.getMessage());
            e1.printStackTrace();
        }
        //TODO: パースしたJSONに基づいて戻り値を構成する
//テスト
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ArrayList<Question> res = new ArrayList<Question>();
        for(int i=0;i<10;i++){
            res.add(new Question("質問"+new Integer(i).toString(), "質問です", "author"));
        }
        return res;
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
