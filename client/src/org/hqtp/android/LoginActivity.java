package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.SafeAsyncTask;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginButton.setOnClickListener(this);
        changeEndpointButton.setOnClickListener(this);
        oauth.setOAuthConsumer(getString(R.string.consumer_key), getString(R.string.consumer_secret));

        if (savedInstanceState != null && savedInstanceState.containsKey("REQUEST_TOKEN")) {
            requestToken = (RequestToken) savedInstanceState.getSerializable("REQUEST_TOKEN");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.twitter_login) {
            TwitterAuthorizationTask task = new TwitterAuthorizationTask();
            task.execute();
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
            showAlert(getString(R.string.authentication_failed_title),
                    getString(R.string.authentication_failed_message));
        }

        @Override
        protected void onSuccess(Void v) {
            Intent intent = new Intent(LoginActivity.this, HQTPActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private class TwitterAuthorizationTask extends SafeAsyncTask<Uri>
    {
        @Override
        public Uri call() throws Exception {
            requestToken = oauth.getOAuthRequestToken(callback_url);
            return Uri.parse(requestToken.getAuthorizationURL());
        }

        @Override
        public void onSuccess(Uri uri) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }

        @Override
        protected void onException(Exception e) {
            showAlert(getString(R.string.authentication_failed_title),
                    getString(R.string.authentication_failed_message));
        }
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
    }
}
