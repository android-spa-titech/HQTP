package org.hqtp.android;

import com.google.inject.Inject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

public class AuthorizationCallBackActivity extends RoboActivity {
    @Inject
    HQTPProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("auth", "create AuthorizationCallBackActivity");
        AuthorizationTask a_task = new AuthorizationTask();
        a_task.setOwner(this);
        a_task.execute();
    }

    private class AuthorizationTask extends RoboAsyncTask<Boolean> {
        private Activity owner = null;

        public void setOwner(Activity owner)
        {
            this.owner = owner;
        }

        @Override
        public Boolean call() throws Exception {
            Uri uri = getIntent().getData();
            Log.d("auth", uri.toString());
            AccessToken access_token = updateAccessTokenFromUri(uri);
            Log.d("auth", "access_token is not null? " + (access_token != null ? "true" : "false"));
            if (access_token == null) {
                return false;
            }
            Log.d("auth", "access token:" + access_token.getToken());
            Log.d("auth", "access token secret:" + access_token.getTokenSecret());
            return proxy.authenticate(access_token.getToken(), access_token.getTokenSecret());
        }

        @Override
        protected void onSuccess(Boolean t) throws Exception {
            super.onSuccess(t);
            if (t) {
                startActivity(new Intent(owner, HQTPActivity.class));
            } else {
                startActivity(new Intent(owner, LoginActivity.class));

            }
        }

    }

    private AccessToken updateAccessTokenFromUri(Uri uri) {
        if (uri == null || !uri.getScheme().equals("hqtp")) {
            return null;
        }
        Configuration config = ConfigurationContext.getInstance();
        OAuthAuthorization oauth = new OAuthAuthorization(config);
        AccessToken token;
        oauth.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));
        try {
            // TODO: アクセストークンを取得するのにリクエストトークンは必要なのか？必要だとしてもっといい渡し方はないのか？調査すべき
            token = oauth.getOAuthAccessToken(LoginActivity.getRequestToken(), uri.getQueryParameter("oauth_verifier"));
        } catch (TwitterException e) {
            token = null;
        }
        return token;
    }

}
