package org.hqtp.android;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;
import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import com.xtremelabs.robolectric.Robolectric;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class APIClientImplTest extends RoboGuiceTest {
    @Inject
    APIClient proxy;

    @Test
    public void authenticateShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"created\":false," +
                "\"user\":{\"id\":12,\"name\":\"test user\",\"icon_url\":\"http://example.com/test.png\"," +
                "\"total_point\":42}}");

        User res = proxy.authenticate("DUMMY_ACCESS_TOKEN_KEY", "DUMMY_ACCESS_TOKEN_SECRET");

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/auth/"));
        String[] queries = sentHttpRequest.getURI().getQuery().split("&");
        Assert.assertTrue("queries contains access_token_key",
                Arrays.asList(queries).contains("access_token_key=DUMMY_ACCESS_TOKEN_KEY"));
        Assert.assertTrue("queries contains access_token_secret",
                Arrays.asList(queries).contains("access_token_secret=DUMMY_ACCESS_TOKEN_SECRET"));
        Assert.assertNotNull(res);
        assertThat(res.getId(), equalTo(12));
        assertThat(res.getName(), equalTo("test user"));
        assertThat(res.getIconURL(), equalTo("http://example.com/test.png"));
        assertThat(res.getTotalPoint(), equalTo(42));
    }

    @Test(expected = HQTPAPIException.class)
    public void authenticateShouldThrowAPIException() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(403, "{ \"status\": \"Forbidden\" }");

        proxy.authenticate("DUMMY_ACCESS_TOKEN_KEY", "DUMMY_ACCESS_TOKEN_SECRET");
    }

    @Test
    public void getTimelineShouldCallAPI() throws Exception
    {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"posts\":[" +
                "{\"id\":\"1\",\"lecture\":{\"id\":\"1\",\"name\":\"a lecture\",\"code\":\"1234\"}," +
                "\"body\":\"test content\"," +
                "\"user\":{\"id\":\"1\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\",\"total_point\":42}," +
                "\"time\":\"2012-06-22T17:44:23.092839\"," +
                "\"virtual_ts\":\"1234567890\"}," +
                "{\"id\":\"2\",\"lecture\":{\"id\":\"1\",\"name\":\"a lecture\",\"code\":\"1234\"}," +
                "\"body\":\"test content\"," +
                "\"user\":{\"id\":\"2\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\",\"total_point\":42}," +
                "\"time\":\"2012-06-22T17:44:23.092839\",\"virtual_ts\":\"1234568890\"}" +
                "]}");

        List<Post> res = proxy.getTimeline(1);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("GET"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/timeline/"));
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
    }

    @Test
    public void postTimelineShouldCallAPI() throws Exception
    {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"post\":" +
                "{\"id\":\"1\",\"lecture\":{\"id\":\"1\",\"name\":\"a lecture\",\"code\":\"1234\"}," +
                "\"body\":\"test content\"," +
                "\"user\":{\"id\":\"1\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\",\"total_point\":42}," +
                "\"time\":\"2012-06-22T17:44:23.092839\"," +
                "\"virtual_ts\":\"1234567890\"}}");

        Post res = proxy.postTimeline("test content", 1, 1234567890, 1234568910);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("POST"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/timeline/"));
        // TODO: クエリパラメータの検査もしたい
        assertThat(res, notNullValue());
    }

    @Test
    public void postTimelineWithImageFileShouldCallAPI() throws Exception
    {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"post\":" +
                "{\"id\":\"1\",\"lecture\":{\"id\":\"1\",\"name\":\"a lecture\",\"code\":\"1234\"}," +
                "\"body\": null," +
                "\"image_url\": \"http://example.com/image/hoge.png\"," +
                "\"user\":{\"id\":\"1\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\",\"total_point\":42}," +
                "\"time\":\"2012-06-22T17:44:23.092839\"," +
                "\"virtual_ts\":\"1234567890\"}}");

        File file = mock(File.class);
        Post res = proxy.postTimeline(file, 1, 1234567890, 1234568910);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("POST"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/timeline/"));
        // TODO: クエリパラメータの検査もしたい
        assertThat(res, notNullValue());
    }

    @Test
    public void getLectureShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\n" +
                "\"status\": \"OK\",\n" +
                "\"lectures\": [\n" +
                "{\n" +
                "\"id\": 123,\n" +
                "\"name\": \"lectureName\",\n" +
                "\"code\": \"lectureCode\"\n" +
                "},\n" +
                "]\n" +
                "}");

        List<Lecture> res = proxy.getLectures();

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("GET"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/get/"));

        assertThat(res, notNullValue());
        assertThat(res.size(), equalTo(1));
        Lecture lecture = res.get(0);
        assertThat(lecture.getId(), equalTo(123));
        assertThat(lecture.getName(), equalTo("lectureName"));
        assertThat(lecture.getCode(), equalTo("lectureCode"));
    }

    @Test
    public void addLectureShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\n" +
                "\"status\": \"OK\",\n" +
                "\"created\": true,\n" +
                "\"lecture\": {\n" +
                "\"id\": 123,\n" +
                "\"name\": \"lectureName\",\n" +
                "\"code\": \"lectureCode\"\n" +
                "}\n" +
                "}");

        Lecture lecture = proxy.addLecture("lectureName", "lectureCode");

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("POST"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/add/"));

        assertThat(lecture, notNullValue());
        assertThat(lecture.getId(), equalTo(123));
        assertThat(lecture.getName(), equalTo("lectureName"));
        assertThat(lecture.getCode(), equalTo("lectureCode"));
    }

    @Test
    public void addLectureShouldThrowException() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\n" +
                "\"status\": \"OK\",\n" +
                "\"created\": false,\n" +
                "\"lecture\": {\n" +
                "\"id\": 123,\n" +
                "\"name\": \"lectureName\",\n" +
                "\"code\": \"lectureCode\"\n" +
                "}\n" +
                "}");
        try {
            proxy.addLecture("lectureName", "lectureCode");
            fail();
        } catch (LectureAlreadyCreatedException e) {
            HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
            assertThat(sentHttpRequest.getMethod(), equalTo("POST"));
            assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
            assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/add/"));

            Lecture lecture = e.lecture;
            assertThat(lecture, notNullValue());
            assertThat(lecture.getId(), equalTo(123));
            assertThat(lecture.getName(), equalTo("lectureName"));
            assertThat(lecture.getCode(), equalTo("lectureCode"));
        }
    }

    @Test
    public void getAchievementsShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{" +
                "\"status\":\"OK\"," +
                "\"total_point\":100," +
                "\"achievements\":[{" +
                "\"id\":1234," +
                "\"name\":\"test name\"," +
                "\"point\":12," +
                "\"created_at\":\"2012-06-22T17:44:23.092839\"" +
                "}]}");

        AchievementResponse res = proxy.getAchievements(0);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("GET"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/user/achievement/"));

        assertThat(res, notNullValue());
        assertThat(res.getTotalPoint(), equalTo(100));

        List<Achievement> achievements = res.getAchievements();

        assertThat(achievements, notNullValue());
        assertThat(achievements.size(), equalTo(1));
        assertThat(achievements.get(0).getId(), equalTo(1234));
        assertThat(achievements.get(0).getName(), equalTo("test name"));
        assertThat(achievements.get(0).getPoint(), equalTo(12));
    }

    @Test
    public void getAchievementsWithSinceIdShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{" +
                "\"status\":\"OK\"," +
                "\"total_point\":100," +
                "\"achievements\":[{" +
                "\"id\":1234," +
                "\"name\":\"test name\"," +
                "\"point\":12," +
                "\"created_at\":\"2012-06-22T17:44:23.092839\"" +
                "}]}");

        AchievementResponse res = proxy.getAchievements(0, 123);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("GET"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/user/achievement/"));

        assertThat(res, notNullValue());
        assertThat(res.getTotalPoint(), equalTo(100));

        List<Achievement> achievements = res.getAchievements();

        assertThat(achievements, notNullValue());
        assertThat(achievements.size(), equalTo(1));
        assertThat(achievements.get(0).getId(), equalTo(1234));
        assertThat(achievements.get(0).getName(), equalTo("test name"));
        assertThat(achievements.get(0).getPoint(), equalTo(12));
    }

    @Test
    public void getUserShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\"," +
                "\"user\":{\"id\":12,\"name\":\"test user\"," +
                "\"icon_url\":\"http://example.com/test.png\",\"total_point\":42}}");

        User res = proxy.getUser(1);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("GET"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/user/"));

        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(12));
        assertThat(res.getName(), equalTo("test user"));
        assertThat(res.getIconURL(), equalTo("http://example.com/test.png"));
        assertThat(res.getTotalPoint(), equalTo(42));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(APIClient.class).to(APIClientImpl.class);
            bind(String.class).annotatedWith(
                    Names.named("HQTP API Endpoint URL")).toInstance(
                    "http://www.hqtp.org/api/");
        }
    }

    @Before
    public void setUp() {
        setUpRoboGuice(new TestModule());
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
