package org.hqtp.android;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
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
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

@RunWith(LoginActivityTestRunner.class)
public class LoginActivityTest {
    @Inject
    Injector injector;
    @Inject
    OAuthAuthorization oauth;
    @Inject
    HQTPProxy proxy;
    @Inject
    LoginActivity activity;
    @InjectView(R.id.twitter_login)
    Button loginButton;

    private ShadowActivity shadowActivity;

    @Before
    public void setUp() throws Exception {
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void activityShouldFinishWhenBackButtonPressed() throws Exception {
        activity.onCreate(null);
        activity.onBackPressed();
        assertTrue(activity.isFinishing());
    }

    @Test
    public void pressingButtonShouldCallActivity() throws Exception {
        activity.onCreate(null);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        loginButton.performClick();
        Thread.sleep(100);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getData().toString(),
                equalTo("http://api.twitter.com/oauth/authorize?oauth_token=token"));
    }

    @Test
    public void returnedActivityShouldRetrieveAccessToken() throws Exception {
        activity.onCreate(null);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        loginButton.performClick();
        Thread.sleep(100);
        shadowActivity.getNextStartedActivity();

        AccessToken accessToken = new AccessToken("123-accessToken", "accessTokenSecret");
        when(oauth.getOAuthAccessToken(requestToken, "verifier")).thenReturn(accessToken);

        activity.onNewIntent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("hqtp://request_callback?oauth_verifier=verifier")));
        Thread.sleep(100);

        verify(proxy).authenticate("123-accessToken", "accessTokenSecret");

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(HQTPActivity.class.getName()));
    }

    @Test
    public void returnedActivityShouldShowAlert() throws Exception {
        activity.onCreate(null);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        loginButton.performClick();
        Thread.sleep(100);
        shadowActivity.getNextStartedActivity();

        when(oauth.getOAuthAccessToken(requestToken, "verifier")).thenThrow(new TwitterException("Cannot verified"));

        activity.onNewIntent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("hqtp://request_callback?oauth_verifier=verifier")));
        Thread.sleep(100);

        assertNull(shadowActivity.getNextStartedActivity());
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alert);
    }
}
