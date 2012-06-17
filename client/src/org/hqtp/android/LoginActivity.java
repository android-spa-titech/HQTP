package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.util.SafeAsyncTask;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.inject.Inject;

public class LoginActivity extends RoboActivity {

    private final String callback_url = "hqtp://request_callback/";
    private OAuthAuthorization oauth;
    private RequestToken requestToken;

    @Inject
    private HQTPProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Configuration config = ConfigurationContext.getInstance();
        oauth = new OAuthAuthorization(config);
        oauth.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));

        if (savedInstanceState != null && savedInstanceState.containsKey("REQUEST_TOKEN")) {
            requestToken = (RequestToken) savedInstanceState.getSerializable("REQUEST_TOKEN");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals("hqtp")) {
            AfterTwitterAuthorizationTask task = new AfterTwitterAuthorizationTask(uri);
            getInjector().injectMembers(task);
            task.execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("REQUEST_TOKEN", requestToken);
    }

    private class AfterTwitterAuthorizationTask extends SafeAsyncTask<Void>
    {
        Uri uri;

        AfterTwitterAuthorizationTask(Uri uri) {
            super();
            this.uri = uri;
        }

        @Override
        public Void call() throws Exception {
            AccessToken accessToken = oauth.getOAuthAccessToken(requestToken, uri.getQueryParameter("oauth_verifier"));
            proxy.authenticate(accessToken.getToken(), accessToken.getTokenSecret());
            return null;
        }

        @Override
        protected void onException(Exception e) {
            // TODO: ここで上手く行かなかった場合の通知を行う。
        }

        @Override
        protected void onSuccess(Void v) {
            startActivity(new Intent(LoginActivity.this, HQTPActivity.class));
        }
    }

    // TODO: onClickで呼ぶ
    private class TwitterAuthorizationTask extends SafeAsyncTask<Void>
    {
        @Override
        public Void call() throws Exception {
            requestToken = oauth.getOAuthRequestToken(callback_url);
            String uri = requestToken.getAuthorizationURL();
            startActivity((new Intent(Intent.ACTION_VIEW, Uri.parse(uri))));
            return null;
        }

        @Override
        protected void onException(Exception e) {
            // TODO: ここで上手く行かなかった場合の通知を行う。
        }
    }
}
