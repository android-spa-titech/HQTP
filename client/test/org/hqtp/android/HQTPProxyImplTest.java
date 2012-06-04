package org.hqtp.android;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;
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

}
