package org.hqtp.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LoginActivityTestRunner.class)
public class LoginActivityTest {
    @Inject
    Injector injector;
    @Inject
    OAuthAuthorization oauth;
    @Inject
    HQTPProxy proxy;

    @Test
    public void activityShouldHaveTwitterLoginButton() {
        LoginActivity activity = new LoginActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        assertNotNull(activity.findViewById(R.id.twitter_login));
    }

    @Test
    public void activityShouldFinishWhenBackButtonPressed() {
        LoginActivity activity = new LoginActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        activity.onBackPressed();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void pressingButtonShouldCallActivity() throws Exception {
        LoginActivity activity = new LoginActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        Button loginButton = (Button) activity.findViewById(R.id.twitter_login);
        loginButton.performClick();
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getData().toString(),
                equalTo("http://api.twitter.com/oauth/authorize?oauth_token=token"));
    }

    @Test
    public void returnedActivityShouldRetrieveAccessToken() throws Exception {
        LoginActivity activity = new LoginActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        ShadowActivity shadowActivity = shadowOf(activity);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        Button loginButton = (Button) activity.findViewById(R.id.twitter_login);
        loginButton.performClick();
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);
        shadowActivity.getNextStartedActivity();

        AccessToken accessToken = new AccessToken("123-accessToken", "accessTokenSecret");
        when(oauth.getOAuthAccessToken(requestToken, "verifier")).thenReturn(accessToken);

        activity.onNewIntent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("hqtp://request_callback?oauth_verifier=verifier")));
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        verify(proxy).authenticate("123-accessToken", "accessTokenSecret");

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(HQTPActivity.class.getName()));
    }

    public void returnedActivityShouldShowAlert() throws Exception {
        LoginActivity activity = new LoginActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        ShadowActivity shadowActivity = shadowOf(activity);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        Button loginButton = (Button) activity.findViewById(R.id.twitter_login);
        loginButton.performClick();
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);
        shadowActivity.getNextStartedActivity();

        when(oauth.getOAuthAccessToken(requestToken, "verifier")).thenThrow(new TwitterException("Cannot verified"));

        activity.onNewIntent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("hqtp://request_callback?oauth_verifier=verifier")));
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        assertNull(shadowActivity.getNextStartedActivity());
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alert);
    }
}
