package org.hqtp.android;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.xtremelabs.robolectric.Robolectric;

@RunWith(HQTPProxyImplTestRunner.class)
public class HQTPProxyImplTest {

    @Inject
    HQTPProxy proxy;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void authenticateShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"created\":false," +
                "\"user\":{\"id\":12,\"name\":\"test user\",\"icon_url\":\"http://example.com/test.png\"}}");

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
    }

    @Test
    public void postQuestionShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{ \"status\": \"OK\" }");

        proxy.postQuestion("test title", "test body");

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getURI(), equalTo(URI.create("http://www.hqtp.org/api/post/")));
        assertThat(sentHttpRequest.getMethod(), equalTo("POST"));
        // TODO: パラメータについても検査したい
        // Assert.assertTrue((EntityUtils.toString(((HttpPost)sentHttpRequest).getEntity())), false);
    }

    @Test
    public void getQuestionsShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric
            .addPendingHttpResponse(200,
                    "{\"status\":\"OK\",\"posts\":[{\"title\":\"title1\",\"body\":\"body1\"},{\"title\":\"title2\",\"body\":\"body2\"}]}");

        List<Question> res = proxy.getQuestions();

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getURI(), equalTo(URI.create("http://www.hqtp.org/api/get/")));
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
        assertThat(res.get(0).getTitle(), equalTo("title1"));
        assertThat(res.get(0).getBody(), equalTo("body1"));
        assertThat(res.get(1).getTitle(), equalTo("title2"));
        assertThat(res.get(1).getBody(), equalTo("body2"));
    }

    @Test(expected = HQTPAPIException.class)
    public void authenticateShouldThrowAPIException() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(403, "{ \"status\": \"Forbidden\" }");

        proxy.authenticate("DUMMY_ACCESS_TOKEN_KEY", "DUMMY_ACCESS_TOKEN_SECRET");
    }

    @Test(expected = HQTPAPIException.class)
    public void postQuestionShouldThrowAPIException() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(403, "{ \"status\": \"Forbidden\" }");

        proxy.postQuestion("test title", "test body");
    }

    @Test(expected = HQTPAPIException.class)
    public void getQuestionsShouldThrowAPIException() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(403, "{ \"status\": \"Forbidden\" }");

        proxy.getQuestions();
    }

    @Test
    public void getTimelineShouldCallAPI() throws Exception
    {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"posts\":[" +
                "{\"id\":\"1\",\"lecture\":{\"id\":\"1\",\"name\":\"a lecture\",\"code\":\"1234\"}," +
                "\"body\":\"test content\"," +
                "\"user\":{\"id\":\"1\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\"}," +
                "\"time\":\"2012-06-22T17:44:23.092839\"," +
                "\"virtual_ts\":\"1234567890\"}," +
                "{\"id\":\"2\",\"lecture\":{\"id\":\"1\",\"name\":\"a lecture\",\"code\":\"1234\"}," +
                "\"body\":\"test content\"," +
                "\"user\":{\"id\":\"2\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\"}," +
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
                "\"user\":{\"id\":\"1\",\"name\":\"a user\",\"icon_url\":\"http://example.com/icon\"}," +
                "\"time\":\"2012-06-22T17:44:23.092839\"," +
                "\"virtual_ts\":\"1234567890\"}}");

        Post res = proxy.postTimeline("test content", 1, 1234567890, 1234568910);

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getMethod(), equalTo("POST"));
        assertThat(sentHttpRequest.getURI().getHost(), equalTo("www.hqtp.org"));
        assertThat(sentHttpRequest.getURI().getPath(), equalTo("/api/lecture/timeline/"));
        // TODO: クエリパラメータの検査もしたい
        Assert.assertNotNull(res);
    }
}
