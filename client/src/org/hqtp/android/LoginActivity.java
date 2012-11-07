package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.SafeAsyncTask;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import android.app.ProgressDialog;
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
    public static final String SAVED_REQUEST_TOKEN = "SAVED_REQUEST_TOKEN";
    public static final String SAVED_AUTH_TOKEN = "SAVED_AUTH_TOKEN";
    public static final String SAVED_AUTH_TOKEN_SECRET = "SAVED_AUTH_TOKEN_SECRET";
    public static final String SAVED_AUTH_TOKEN_STATE = "SAVED_AUTH_TOKEN_STATE";

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
    private APIClient proxy;
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

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_REQUEST_TOKEN)) {
            requestToken = (RequestToken) savedInstanceState.getSerializable(SAVED_REQUEST_TOKEN);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.twitter_login) {
            if (preferences.getBoolean(SAVED_AUTH_TOKEN_STATE, false)) {
                AfterTwitterAuthorizationTask task = new AfterTwitterAuthorizationTask(
                        preferences.getString(SAVED_AUTH_TOKEN, ""), preferences.getString(SAVED_AUTH_TOKEN_SECRET, ""));
                task.execute();
            } else {
                TwitterAuthorizationTask task = new TwitterAuthorizationTask();
                task.execute();
            }
        } else if (v.getId() == R.id.changeEndpointButton) {// DEBUG: change endpoint(for local test)
            String new_endpoint = endpointText.getText().toString();
            if (!new_endpoint.isEmpty()) {
                ((APIClientImpl) proxy).setEndpoint(new_endpoint);
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
        outState.putSerializable(SAVED_REQUEST_TOKEN, requestToken);
    }

    private class AfterTwitterAuthorizationTask extends SafeAsyncTask<Void>
    {
        private String token;
        private String tokenSecret;
        private Uri uri;
        private ProgressDialog dialog;

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
        protected void onPreExecute() throws Exception {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("HQTPサーバー");
            dialog.setMessage("通信中です");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
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
                e.putString(SAVED_AUTH_TOKEN, token);
                e.putString(SAVED_AUTH_TOKEN_SECRET, tokenSecret);
                e.commit();
            }
            User user = proxy.authenticate(token, tokenSecret);
            proxy.setUserId(user.getId());
            return null;
        }

        @Override
        protected void onSuccess(Void v) {
            if (!preferences.getBoolean(SAVED_AUTH_TOKEN_STATE, false)) {
                Editor e = preferences.edit();
                e.putBoolean(SAVED_AUTH_TOKEN_STATE, true);
                e.commit();
            }
            Intent intent = new Intent(LoginActivity.this, LectureListActivity.class);
            startActivity(intent);
            // ログイン画面へのbackを無効にする(#93)ためにfinish()してアクティビティを無くしておく
            // 参考：
            // http://stackoverflow.com/questions/3473168/clear-the-entire-history-stack-and-start-a-new-activity-on-android/10015648#10015648
            finish();
        }

        @Override
        protected void onException(Exception e) {
            dialog.dismiss();
            dialog = null;
            alerter.alert(getString(R.string.authentication_failed_title),
                    getString(R.string.authentication_failed_message));
        }

        @Override
        protected void onFinally() throws RuntimeException {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        }

    }

    private class TwitterAuthorizationTask extends SafeAsyncTask<Uri>
    {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() throws Exception {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("Twitter");
            dialog.setMessage("通信中です");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
        }

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
            dialog.dismiss();
            dialog = null;
            alerter.alert(getString(R.string.authentication_failed_title),
                    getString(R.string.authentication_failed_message));
        }

        @Override
        protected void onFinally() throws RuntimeException {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        }
    }
}
