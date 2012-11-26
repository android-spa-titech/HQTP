package org.hqtp.android;

import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationContext;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class HQTPModule extends AbstractModule {

    @Override
    public void configure() {
        bind(APIClient.class).to(APIClientImpl.class);
        bind(TimelineRecurringUpdater.class).to(TimelineRecurringUpdaterImpl.class);
        bind(ImageLoader.class).to(ImageLoaderImpl.class);
        bind(String.class).annotatedWith(Names.named("HQTP API Endpoint URL")).toInstance("http://www16307ue.sakura.ne.jp:61234/api/");
        bind(Long.class).annotatedWith(Names.named("TimelineUpdatePeriod")).toInstance(Long.valueOf(500));

        OAuthAuthorization oauth = new OAuthAuthorization(ConfigurationContext.getInstance());
        bind(OAuthAuthorization.class).toInstance(oauth);
    }

}
