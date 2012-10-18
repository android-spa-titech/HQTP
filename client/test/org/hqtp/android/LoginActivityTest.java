package org.hqtp.android;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.widget.Button;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class LoginActivityTest extends RoboGuiceTest {
    @Inject
    LoginActivity activity;
    @Inject
    OAuthAuthorization oauth;
    @Inject
    HQTPProxy proxy;
    @InjectView(R.id.twitter_login)
    Button loginButton;

    private ShadowActivity shadowActivity;
    private SharedPreferences preferences;

    @Test
    public void activityShouldFinishWhenBackButtonPressed() throws Exception {
        activity.onCreate(null);
        activity.onBackPressed();
        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void pressingButtonShouldCallActivity() throws Exception {
        activity.onCreate(null);

        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);

        loginButton.performClick();
        Thread.sleep(100);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
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
        assertThat(preferences.getString(LoginActivity.SAVED_AUTH_TOKEN, ""), equalTo("123-accessToken"));
        assertThat(preferences.getString(LoginActivity.SAVED_AUTH_TOKEN_SECRET, ""), equalTo("accessTokenSecret"));
        assertThat(preferences.getBoolean(LoginActivity.SAVED_AUTH_TOKEN_STATE, false), is(true));

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(LectureListActivity.class.getName()));
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

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(alert, notNullValue());
    }

    @Test
    public void activityShouldUseSavedLoginInformation() throws Exception {
        Editor e = preferences.edit();
        e.putString(LoginActivity.SAVED_AUTH_TOKEN, "123-accessToken");
        e.putString(LoginActivity.SAVED_AUTH_TOKEN_SECRET, "accessTokenSecret");
        e.putBoolean(LoginActivity.SAVED_AUTH_TOKEN_STATE, true);
        e.commit();

        activity.onCreate(null);
        loginButton.performClick();
        Thread.sleep(100);

        verify(proxy).authenticate("123-accessToken", "accessTokenSecret");

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(LectureListActivity.class.getName()));
    }

    @Test
    public void activityShouldUseValidLoginInformation() throws Exception {
        Editor e = preferences.edit();
        e.putString(LoginActivity.SAVED_AUTH_TOKEN, "123-accessToken");
        e.putString(LoginActivity.SAVED_AUTH_TOKEN_SECRET, "accessTokenSecret");
        e.commit();

        activity.onCreate(null);
        RequestToken requestToken = new RequestToken("token", "secret");
        when(oauth.getOAuthRequestToken("hqtp://request_callback/")).thenReturn(requestToken);
        loginButton.performClick();
        Thread.sleep(100);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getData().toString(),
                equalTo("http://api.twitter.com/oauth/authorize?oauth_token=token"));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
            bind(OAuthAuthorization.class).toInstance(mock(OAuthAuthorization.class));
            bind(LoginActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new LoginActivity();
        shadowActivity = shadowOf(activity);
        setUpRoboGuice(new TestModule(), activity);

        preferences = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
