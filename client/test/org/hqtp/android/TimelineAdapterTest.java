package org.hqtp.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.activity.RoboActivity;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class TimelineAdapterTest extends RoboGuiceTest {
    static final int TEST_LECTURE_ID = 42;

    @Inject
    TimelineAdapter adapter;
    @Inject
    ImageLoader imageLoader;

    private Activity activity;
    private Post testPost;
    private Post testOldPost;
    private User testUser;
    private Lecture testLecture;
    private List<Post> testPosts;

    private static final int INITIAL_FORM = 1;
    private static final int NUM_INITIAL_CELLS = 2;

    // This test fixture is expected to shown as...
    // OldDateSeparator(vts=(0, 1970/01/01 09:00:00.00000 JST))
    // testOldPost(vts=(0, 1970/01/01 09:00:00.00000 JST), date=1970/01/01 09:00:00.00000 JST)
    // NewDateSeparator(vts=(?, NOW - ONE_DAY))
    // testPost(vts=(?, NOW - ONE_DAY), date=NOW)
    // NowDateSeparator(vts=(?, NOW))
    // PostingCell()
    private static final int OLD_DATE_SEPARATOR = 0;
    private static final int OLD_TEST_POST = 1;
    private static final int NEW_DATE_SEPARATOR = 2;
    private static final int TEST_POST = 3;
    private static final int NOW_DATE_SEPARATOR = 4;
    private static final int FORM_CELL = 5;
    private static final int NUM_CELLS = 6;

    @Before
    public void setUpFixture() throws Exception {
        testUser = new User(1, "testUser", "http://example.com/icon.png", 0);
        testLecture = new Lecture(2, "testLecture", "testLectureCode");
        testOldPost = new Post(4, "body", new Date(0), 0, testUser, testLecture);
        Date d = new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
        testPost = new Post(3, "body", new Date(), Post.dateToVirtualTimestamp(d),
                testUser, testLecture);
        testPosts = new ArrayList<Post>();
        testPosts.add(testOldPost);
        testPosts.add(testPost);
    }

    @Test
    public void shouldHaveOnePostingCell() throws Exception {
        View view;

        // Initially the last cell should be the posting cell.
        assertThat(adapter.getCount(), equalTo(2));
        view = adapter.getView(1, null, null);
        assertThat(view.findViewById(R.id.postButton), notNullValue());

        // The last cell should be the posting cell after some posts have been added.
        adapter.onUpdate(testPosts);
        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        view = adapter.getView(FORM_CELL, null, null);
        assertThat(view.findViewById(R.id.postButton), notNullValue());
    }

    @Test
    public void shouldShowPosts() throws Exception {
        adapter.onUpdate(testPosts);
        View view = adapter.getView(1, null, null);
        TextView postContent = (TextView) view.findViewById(R.id.postContent);
        TextView postedTime = (TextView) view.findViewById(R.id.postedTime);
        TextView userName = (TextView) view.findViewById(R.id.userName);
        ImageView userIcon = (ImageView) view.findViewById(R.id.userIcon);

        assertThat(postContent, notNullValue());
        assertThat(postedTime, notNullValue());
        assertThat(userName, notNullValue());
        assertThat(userIcon, notNullValue());

        assertThat(postContent.getText().toString(), equalTo(testOldPost.getBody()));
        assertThat(userName.getText().toString(), equalTo(testUser.getName()));
        assertThat(userIcon.getTag(), instanceOf(String.class));
        assertThat((String) userIcon.getTag(), equalTo(testUser.getIconURL()));
        verify(imageLoader).displayImage(userIcon, activity);
    }

    @Test
    public void shouldShowPostingTimeInProperFormat() throws Exception {
        View view;
        TextView postedTime;
        adapter.onUpdate(testPosts);

        // oldTestPost is posted at 1970/01/01 09:00:00.00000 JST.
        // 1970/01/01 00:00 GMT is 1970/01/01 09:00 JST
        view = adapter.getView(OLD_TEST_POST, null, null);
        postedTime = (TextView) view.findViewById(R.id.postedTime);
        assertThat(postedTime, notNullValue());
        assertThat(postedTime.getText().toString(), equalTo("1970/01/01 09:00"));

        // testPost is posted just now.
        view = adapter.getView(TEST_POST, null, null);
        postedTime = (TextView) view.findViewById(R.id.postedTime);
        assertThat(postedTime, notNullValue());
        assertThat(postedTime.getText().toString(), equalTo("0分前"));
    }

    @Test
    public void shouldShowDateSeparatorInProperFormat() throws Exception {
        adapter.onUpdate(testPosts);
        View view = adapter.getView(OLD_DATE_SEPARATOR, null, null);
        TextView dateText = (TextView) view.findViewById(R.id.dateText);
        assertThat(dateText, notNullValue());
        // The date separator's date is calculated based on the following post, which is oldTestPost.
        // oldTestPost's virtual timestamp is 1970/01/01 09:00:00.00000 JST
        assertThat(dateText.getText().toString(), equalTo("1970/01/01"));
    }

    @Test
    public void shouldShowDateSeparatorEndOfList() throws Exception {
        adapter.onUpdate(testPosts);
        View view = adapter.getView(NOW_DATE_SEPARATOR, null, null);
        TextView dateText = (TextView) view.findViewById(R.id.dateText);
        assertThat(dateText, notNullValue());

        // The last date separator should show current date.
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        assertThat(dateText.getText().toString(), equalTo(df.format(new Date())));
    }

    @Test
    public void shouldMovePostingCell() throws Exception {
        adapter.onUpdate(testPosts);
        View view = adapter.getView(OLD_DATE_SEPARATOR, null, null);
        OnItemLongClickListener listener = adapter.new ItemLongClickListener();
        listener.onItemLongClick(null, view, OLD_DATE_SEPARATOR, adapter.getItemId(OLD_DATE_SEPARATOR));

        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        View postView = adapter.getView(OLD_DATE_SEPARATOR + 1, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());
    }

    @Test
    public void virtualTSOfDateSeparatorShouldHaveProperValue() throws Exception {
        // Check this using post. If you post just after date separator, PREV_VIRTUAL_TS should be the one that the date
        // separator has.
        adapter.onUpdate(testPosts);
        View view = adapter.getView(NEW_DATE_SEPARATOR, null, null);
        OnItemLongClickListener listener = adapter.new ItemLongClickListener();
        listener.onItemLongClick(null, view, NEW_DATE_SEPARATOR, adapter.getItemId(NEW_DATE_SEPARATOR));

        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        View postView = adapter.getView(NEW_DATE_SEPARATOR + 1, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());
        postView.findViewById(R.id.postButton).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = shadowOf(startedIntent);

        // The date separator's virtual timestamp is calculated based on the following post.
        Calendar cal = Calendar.getInstance();
        cal.setTime(Post.virtualTimestampToDate(testPost.getVirtualTimestamp()));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long prevVirtualTS = shadowIntent.getLongExtra(PostTimelineActivity.PREV_VIRTUAL_TS, 100);
        assertThat(prevVirtualTS, equalTo(Post.dateToVirtualTimestamp(cal.getTime())));
    }

    @Test
    public void shouldMovePostingCellToEnd() throws Exception {
        // Move the posting cell.
        adapter.onUpdate(testPosts);
        View view = adapter.getView(OLD_DATE_SEPARATOR, null, null);
        OnItemLongClickListener listener = adapter.new ItemLongClickListener();
        listener.onItemLongClick(null, view, OLD_DATE_SEPARATOR, adapter.getItemId(OLD_DATE_SEPARATOR));

        // Check if the posting cell was moved.
        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        View postView = adapter.getView(OLD_DATE_SEPARATOR + 1, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());

        // Check if the posting cell was moved after posting.
        adapter.onActivityResult(0, Activity.RESULT_OK, null);
        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        postView = adapter.getView(FORM_CELL, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());
    }

    @Test
    public void shouldNotMovePostingCellToEnd() throws Exception {
        // Move the posting cell.
        adapter.onUpdate(testPosts);
        View view = adapter.getView(OLD_DATE_SEPARATOR, null, null);
        OnItemLongClickListener listener = adapter.new ItemLongClickListener();
        listener.onItemLongClick(null, view, OLD_DATE_SEPARATOR, adapter.getItemId(OLD_DATE_SEPARATOR));

        // Check if the posting cell was moved.
        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        View postView = adapter.getView(OLD_DATE_SEPARATOR + 1, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());

        // Check if the posting cell wasn't moved after canceling posting.
        adapter.onActivityResult(0, Activity.RESULT_CANCELED, null);
        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        postView = adapter.getView(OLD_DATE_SEPARATOR + 1, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());
    }

    @Test
    public void pressingButtonShouldCallActivity() throws Exception {
        adapter.setLectureId(TEST_LECTURE_ID);

        assertThat(adapter.getCount(), equalTo(NUM_INITIAL_CELLS));
        View view = adapter.getView(INITIAL_FORM, null, null);
        assertThat(view, notNullValue());
        assertThat(view.findViewById(R.id.postButton), notNullValue());
        Button postButton = (Button) view.findViewById(R.id.postButton);
        postButton.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(PostTimelineActivity.class.getName()));
        assertThat(shadowIntent.getIntExtra(PostTimelineActivity.LECTURE_ID, -1), equalTo(TEST_LECTURE_ID));
        assertThat(shadowIntent.getLongExtra(PostTimelineActivity.PREV_VIRTUAL_TS, 100), equalTo(-1L));
        assertThat(shadowIntent.getLongExtra(PostTimelineActivity.NEXT_VIRTUAL_TS, 100), equalTo(-1L));
    }

    @Test
    public void pressingButtonShouldCallActivityWithProperExtras() throws Exception {
        adapter.setLectureId(TEST_LECTURE_ID);
        adapter.onUpdate(testPosts);

        View view = adapter.getView(NEW_DATE_SEPARATOR, null, null);
        OnItemLongClickListener listener = adapter.new ItemLongClickListener();
        listener.onItemLongClick(null, view, NEW_DATE_SEPARATOR, adapter.getItemId(NEW_DATE_SEPARATOR));

        assertThat(adapter.getCount(), equalTo(NUM_CELLS));
        View postView = adapter.getView(NEW_DATE_SEPARATOR + 1, null, null);
        assertThat(postView.findViewById(R.id.postButton), notNullValue());
        postView.findViewById(R.id.postButton).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        Calendar cal = Calendar.getInstance();
        cal.setTime(Post.virtualTimestampToDate(testPost.getVirtualTimestamp()));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long prevVirtualTS = shadowIntent.getLongExtra(PostTimelineActivity.PREV_VIRTUAL_TS, 100);
        long nextVirtualTS = shadowIntent.getLongExtra(PostTimelineActivity.NEXT_VIRTUAL_TS, 100);
        assertThat(prevVirtualTS, equalTo(Post.dateToVirtualTimestamp(cal.getTime())));
        assertThat(nextVirtualTS, equalTo(testPost.getVirtualTimestamp()));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ImageLoader.class).toInstance(mock(ImageLoader.class));
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new RoboActivity() {
        };
        setUpRoboGuice(new TestModule(), activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
