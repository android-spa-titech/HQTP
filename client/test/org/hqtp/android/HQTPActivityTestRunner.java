package org.hqtp.android;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.runners.model.InitializationError;

import roboguice.application.RoboApplication;
import roboguice.config.AbstractAndroidModule;
import android.app.Application;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

class HQTPActivityTestApplication extends RoboApplication {

    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new HQTPActivityTestModule());
    }

}

class HQTPActivityTestModule extends AbstractAndroidModule {

    @Override
    protected void configure() {
        bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
    }

}

public class HQTPActivityTestRunner extends RobolectricTestRunner {

    public HQTPActivityTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Application createApplication() {
        return new HQTPActivityTestApplication();
    }

    @Override
    public void prepareTest(Object test) {
        RoboApplication app = (RoboApplication) Robolectric.application;
        Injector injector = app.getInjector();
        injector.injectMembers(test);
    }

}