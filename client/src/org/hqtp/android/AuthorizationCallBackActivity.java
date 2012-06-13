package org.hqtp.android;

import com.google.inject.Inject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import roboguice.activity.RoboActivity;
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

        AccessToken access_token = updateAccessTokenFromUri(getIntent().getData());
        if (access_token != null && proxy.authenticate(access_token.getToken(), access_token.getTokenSecret())) {
            startActivity(new Intent(this, HQTPActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    // TODO: 戻り値どうする？
    private AccessToken updateAccessTokenFromUri(Uri uri) {
        if (uri == null || uri.getScheme() != "hqtp") {
            return null;
        }
        Configuration config = ConfigurationContext.getInstance();
        OAuthAuthorization oauth = new OAuthAuthorization(config);
        AccessToken token;
        try {
            // TODO: ここは本当に動くのか?
            token = oauth.getOAuthAccessToken(uri.getQueryParameter("oauth_verifier"));
        } catch (TwitterException e) {
            token = null;
        }
        return token;
    }
}
