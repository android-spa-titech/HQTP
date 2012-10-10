package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.SafeAsyncTask;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

public class LoginActivity extends RoboActivity implements OnClickListener {
    @InjectView(R.id.twitter_login)
    Button loginButton;
    @InjectView(R.id.changeEndpointButton)
    Button changeEndpointButton;
    @InjectView(R.id.endpointText)
    TextView endpointText;

    private final String callback_url = "hqtp://request_callback/";
    private RequestToken requestToken;

    @Inject
    OAuthAuthorization oauth;
    @Inject
    private HQTPProxy proxy;
    @Inject
    Alerter alerter;

    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton.setOnClickListener(this);
        changeEndpointButton.setOnClickListener(this);

        preferences = getPreferences(MODE_PRIVATE);

        oauth.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));

        if (savedInstanceState != null && savedInstanceState.containsKey("REQUEST_TOKEN")) {
            requestToken = (RequestToken) savedInstanceState.getSerializable("REQUEST_TOKEN");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.twitter_login) {
            if (preferences.getString("authentication", "").equals("succeeded")) {
                AfterTwitterAuthorizationTask task = new AfterTwitterAuthorizationTask(
                        preferences.getString("token", ""), preferences.getString("tokenSecret", ""));
                task.execute();
            } else {
                TwitterAuthorizationTask task = new TwitterAuthorizationTask();
                task.execute();
            }
        } else if (v.getId() == R.id.changeEndpointButton) {// DEBUG: change endpoint(for local test)
            String new_endpoint = endpointText.getText().toString();
            if (!new_endpoint.isEmpty()) {
                ((HQTPProxyImpl) proxy).setEndpoint(new_endpoint);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals("hqtp")) {
            AfterTwitterAuthorizationTask task = new AfterTwitterAuthorizationTask(uri);
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
        String token;
        String tokenSecret;
        Uri uri;

        AfterTwitterAuthorizationTask(Uri uri) {
            super();
            this.uri = uri;
        }

        AfterTwitterAuthorizationTask(String token, String tokenSecret) {
            super();
            this.token = token;
            this.tokenSecret = tokenSecret;
        }

        @Override
        public Void call() throws Exception {
            if (uri != null) {
                AccessToken accessToken;
                accessToken = oauth.getOAuthAccessToken(requestToken,
                        uri.getQueryParameter("oauth_verifier"));
                token = accessToken.getToken();
                tokenSecret = accessToken.getTokenSecret();
                Editor e = preferences.edit();
                e.putString("token", token);
                e.putString("tokenSecret", tokenSecret);
                e.commit();
            }
            proxy.authenticate(token, tokenSecret);
            return null;
        }

        @Override
        protected void onException(Exception e) {
            alerter.alert(getString(R.string.authentication_failed_title),
                    getString(R.string.authentication_failed_message));
        }

        @Override
        protected void onSuccess(Void v) {
            if (!preferences.getString("authentication", "").equals("succeeded")) {
                Editor e = preferences.edit();
                e.putString("authentication", "succeeded");
                e.commit();
            }
            Intent intent = new Intent(LoginActivity.this, LectureListActivity.class);
            startActivity(intent);
            // ログイン画面へのbackを無効にする(#93)ためにfinish()してアクティビティを無くしておく
            // 参考：
            // http://stackoverflow.com/questions/3473168/clear-the-entire-history-stack-and-start-a-new-activity-on-android/10015648#10015648
            finish();
        }
    }

    private class TwitterAuthorizationTask extends SafeAsyncTask<Uri>
    {
        @Override
        public Uri call() throws Exception {
            // #135のバグ回避のための措置
            // AccessTokenを一端クリアすることで再認証時にgetOAuthRequestToken()が失敗しないようにする
            oauth.setOAuthAccessToken(null);
            requestToken = oauth.getOAuthRequestToken(callback_url);
            return Uri.parse(requestToken.getAuthorizationURL());
        }

        @Override
        public void onSuccess(Uri uri) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }

        @Override
        protected void onException(Exception e) {
            alerter.alert(getString(R.string.authentication_failed_title),
                    getString(R.string.authentication_failed_message));
        }
    }
}
