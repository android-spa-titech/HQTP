package org.hqtp.android;

import java.util.List;

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

    public List<Question> getQuestions() {
        // TODO:implement
        return null;
    }
}
