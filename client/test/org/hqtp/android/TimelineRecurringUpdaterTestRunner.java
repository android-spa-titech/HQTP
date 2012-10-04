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

import static org.mockito.Mockito.mock;

public class TimelineRecurringUpdaterTestRunner extends RobolectricTestRunner {

    public TimelineRecurringUpdaterTestRunner(Class<?> testClass)
            throws InitializationError {
        super(testClass);
    }

    @Override
    protected Application createApplication() {
        return new RoboApplication() {
            @Override
            protected void addApplicationModules(List<Module> modules) {
                modules.add(new AbstractAndroidModule() {
                    @Override
                    protected void configure() {
                        bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
                        bind(TimelineRecurringUpdater.class).to(TimelineRecurringUpdaterImpl.class);
                        bind(Long.class).annotatedWith(Names.named("TimelineUpdatePeriod")).toInstance(
                                Long.valueOf(500));
                    }
                });
            }
        };
    }

    @Override
    public void prepareTest(Object test) {
        RoboApplication app = (RoboApplication) Robolectric.application;
        Injector injector = app.getInjector();
        injector.injectMembers(test);
    }

}
