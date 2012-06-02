package org.hqtp.android;

import roboguice.config.AbstractAndroidModule;

public class HQTPModule extends AbstractAndroidModule {

    @Override
    public void configure() {
        bind(HQTPProxy.class).to(HQTPProxyImpl.class);
    }

}
