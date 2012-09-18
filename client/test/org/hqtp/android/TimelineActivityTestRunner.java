package org.hqtp.android;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.runners.model.InitializationError;

import roboguice.application.RoboApplication;
import roboguice.config.AbstractAndroidModule;
import android.app.Application;
import android.content.Intent;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

public class TimelineActivityTestRunner extends RobolectricTestRunner {

    public TimelineActivityTestRunner(Class<?> testClass)
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

                        TimelineActivity activity = new TimelineActivity();
                        Intent intent = new Intent();
                        intent.putExtra(
                                TimelineActivity.LECTURE_ID,
                                TimelineActivityTest.LECTURE_ID);
                        activity.setIntent(intent);
                        bind(TimelineActivity.class).toInstance(activity);
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
