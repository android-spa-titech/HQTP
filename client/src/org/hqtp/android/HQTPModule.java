package org.hqtp.android;

import roboguice.config.AbstractAndroidModule;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationContext;

import com.google.inject.name.Names;

public class HQTPModule extends AbstractAndroidModule {

    @Override
    public void configure() {
        bind(HQTPProxy.class).to(HQTPProxyImpl.class);
        bind(String.class).annotatedWith(Names.named("HQTP API Endpoint URL")).toInstance("http://www.hqtp.org/api/");

        OAuthAuthorization oauth = new OAuthAuthorization(ConfigurationContext.getInstance());
        bind(OAuthAuthorization.class).toInstance(oauth);
    }

}
