package org.hqtp.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

public class LoginActivity extends RoboActivity {
    //TODO: リクエストトークンの渡し方の改善
    //AuthorizationCallBackActivityにリクエストトークンを渡すために
    //request_tokenをstaticに用意している。
    //あまりいい方法とは思えないので何かいい案があれば変えて欲しい。
    private static RequestToken request_token;
    public static RequestToken getRequestToken() {
        return request_token;
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    private final String callback_url = "hqtp://request_callback/";

    //TODO: onclickで呼ぶ
    private class TwitterAuthorizationTask extends RoboAsyncTask<Void>
    {
        @Override
        public Void call() throws Exception {
            Configuration config = ConfigurationContext.getInstance();
            OAuthAuthorization oauth = new OAuthAuthorization(config);
            oauth.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));
            try {
                request_token = oauth.getOAuthRequestToken(callback_url);
                String uri = request_token.getAuthorizationURL();
                startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
