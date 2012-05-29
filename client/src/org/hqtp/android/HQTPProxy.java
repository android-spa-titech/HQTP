package org.hqtp.android;

//HQTPサーバーとの通信をラップするクラス
//とりあえずSingletonにしておきます
public class HQTPProxy {
    private static HQTPProxy instance = null;

    private HQTPProxy() {
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

    // TODO: 投稿されている質問一覧を取得
    // public 戻り値 getQuestions(){
    // }
}
