package org.hqtp.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.Uri;
import android.util.Log;

//HQTPサーバーとの通信をラップするクラス
//とりあえずSingletonにしておきます
public class HQTPProxy {
    private static HQTPProxy instance = null;

    // APIを投げる先のURL
    // 今はデバッグのためエミュレータからPC側のローカルホストへアクセスするURLを指定している
    private final String api_gateway = "http://10.0.2.2:8000/api/";
    // private final String api_gateway = "http://www.hqtp.org/api/";

    private CookieStore cookie_store = null;

    private HQTPProxy() {
        this.cookie_store = new BasicCookieStore();
    }

    // TODO: 引数でパラメータを受け取る。連想配列かなんか？
    // 戻り値をHttpResponseで返しているがレスポンスの文字列を返してもいいかもしれない
    private HttpResponse sendByGet(String path) throws ClientProtocolException,
            IOException {
        Log.d("info", ">>sendByGet");
        Uri.Builder builder = Uri.parse(api_gateway).buildUpon();
        builder.appendPath(path);
        // TODO: パラメータの指定
        HttpGet http_get = new HttpGet(builder.build().toString());
        DefaultHttpClient client = new DefaultHttpClient();
        client.setCookieStore(cookie_store);
        HttpResponse response = client.execute(http_get);
        this.cookie_store = client.getCookieStore();
        return response;
    }

    // TODO: パラメータの受け取り
    private HttpResponse sendByPost(String path) {
        // TODO: implement
        return null;
    }

    public static HQTPProxy getInstance() {
        if (instance == null) {
            instance = new HQTPProxy();
        }
        return instance;
    }

    // Instance methods
    public HQTPProxy setAccessToken(String access_token) {
        // TODO: implement
        return this;
    }

    public boolean authenticate() {
        // TODO: implement
        return false;
    }

    public boolean postQuestion(String title, String body) {
        // TODO: implement
        return false;
    }

    public List<Question> getQuestions() {
        // TODO:implement
        // return null;
        Log.d("info", ">>getQuestions");
        // ネットワークからの読込テスト
        try {
            HttpResponse res = sendByGet("");
            Log.d("info", getResponseContentText(res));
        } catch (Exception e1) {
            Log.d("err", e1.getMessage());
            e1.printStackTrace();
        }
        // テスト
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ArrayList<Question> res = new ArrayList<Question>();
        for (int i = 0; i < 10; i++) {
            res.add(new Question("質問" + new Integer(i).toString(), "質問です",
                    "author"));
        }
        return res;
    }

    private static String getResponseContentText(HttpResponse response)
            throws IllegalStateException, IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                entity.getContent()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
