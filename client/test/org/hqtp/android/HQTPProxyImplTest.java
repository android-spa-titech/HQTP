package org.hqtp.android;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;
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

    @Inject HQTPProxy proxy;

    @Before
    public void setUp() throws Exception {
        proxy.setAccessToken("DUMMY_ACCESS_TOKEN");
    }

    @Test
    public void authenticateShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{ \"status\": \"OK\" }");

        proxy.authenticate();

        HttpUriRequest sentHttpRequest = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(sentHttpRequest.getURI(),
                equalTo(URI.create("www.hqtp.org/api/auth?access_token=DUMMY_ACCESS_TOKEN")));

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
        //TODO: パラメータについても検査したい
        //Assert.assertTrue((EntityUtils.toString(((HttpPost)sentHttpRequest).getEntity())), false);
    }

    @Test
    public void getQuestionsShouldCallAPI() throws Exception {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(200, "{\"status\":\"OK\",\"posts\":[{\"title\":\"title1\",\"body\":\"body1\"},{\"title\":\"title2\",\"body\":\"body2\"}]}");

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

}
