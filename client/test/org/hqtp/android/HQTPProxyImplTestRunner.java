package org.hqtp.android;

import java.util.List;

import org.junit.runners.model.InitializationError;

import roboguice.application.RoboApplication;
import roboguice.config.AbstractAndroidModule;
import android.app.Application;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;


class HQTPProxyImplTestApplication extends RoboApplication {

    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new HQTPProxyImplTestModule());
    }

}

class HQTPProxyImplTestModule extends AbstractAndroidModule {

    @Override
    protected void configure() {
        bind(HQTPProxy.class).to(HQTPProxyImpl.class);
        bind(String.class).annotatedWith(Names.named("HQTP API Endpoint URL")).toInstance("http://www.hqtp.org/api/");
    }

}


public class HQTPProxyImplTestRunner extends RobolectricTestRunner {

    public HQTPProxyImplTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Application createApplication() {
        return new HQTPProxyImplTestApplication();
    }

    @Override
    public void prepareTest(Object test) {
        RoboApplication app = (RoboApplication) Robolectric.application;
        Injector injector = app.getInjector();
        injector.injectMembers(test);
    }


}
