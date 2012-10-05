package org.hqtp.android.util;

import roboguice.RoboGuice;
import android.app.Application;
import android.content.Context;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.xtremelabs.robolectric.Robolectric;

public class RoboGuiceTest {
    public void setUpRoboGuice(Module module, Context injectionContext) {
        Application app = Robolectric.application;
        Module m = Modules.override(RoboGuice.newDefaultRoboModule(app))
            .with(module);
        RoboGuice.setBaseApplicationInjector(app, RoboGuice.DEFAULT_STAGE, m);
        RoboGuice.injectMembers(injectionContext, this);
    }

    public void setUpRoboGuice(Module module) {
        setUpRoboGuice(module, Robolectric.application);
    }

    public void tearDownRoboGuice() {
        RoboGuice.util.reset();
    }
}
