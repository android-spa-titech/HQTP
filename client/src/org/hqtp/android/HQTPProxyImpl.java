package org.hqtp.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

import com.google.inject.Singleton;

//HQTPサーバーとの通信をラップするクラス
//とりあえずSingletonにしておきます
@Singleton
public class HQTPProxyImpl implements HQTPProxy {
    // APIを投げる先のURL
    // 今はデバッグのためエミュレータからPC側のローカルホストへアクセスするURLを指定している
    private final String api_gateway = "http://10.0.2.2:8000/api/";
    // private final String api_gateway = "http://www.hqtp.org/api/";

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
        // TODO: implement
        return false;
    }

    @Override
    public List<Question> getQuestions() {
        // ネットワークからの読込テスト
        try {
            HttpResponse res = sendByGet("get/",null);
            //TODO: ネットワークまわりの例外とレスポンスまわりの例外処理がごっちゃになってるのをどうにかしたい
            String res_str = EntityUtils.toString(res.getEntity());
            Log.d("info", res_str);
        } catch (Exception e1) {
            Log.d("err", e1.getMessage());
            e1.printStackTrace();
        }
        //TODO: resをJSON文字列としてパース
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

    // 戻り値をHttpResponseで返しているがレスポンスの文字列を返してもいいかもしれない
    private HttpResponse sendByGet(String path,Map<String, String> params) throws ClientProtocolException,
            IOException {
        Uri.Builder builder = Uri.parse(api_gateway).buildUpon();
        builder.appendEncodedPath(path);
        if(params!=null){
            for (Map.Entry<String, String> param : params.entrySet()) {
                //TODO: パラメータ
                builder.appendQueryParameter(param.getKey(), param.getValue());
            }
        }
        HttpGet http_get = new HttpGet(builder.build().toString());
        DefaultHttpClient client = new DefaultHttpClient();
        client.setCookieStore(cookie_store);
        HttpResponse response = client.execute(http_get);
        this.cookie_store = client.getCookieStore();
        {//Cookieのデバッグ表示
            Iterator<Cookie> c_it = cookie_store.getCookies().iterator();
            while (c_it.hasNext()) {
                Cookie c = c_it.next();
                Log.d("cookie", c.getName() + ":" + c.getValue());
            }
        }
        return response;
    }

    // TODO: パラメータの受け取り
    private HttpResponse sendByPost(String path,Map<String, String> params) {
        // TODO: implement
        return null;
    }
}
