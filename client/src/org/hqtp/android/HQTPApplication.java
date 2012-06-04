package org.hqtp.android;

import java.util.List;

import roboguice.application.RoboApplication;

import com.google.inject.Module;

public class HQTPApplication extends RoboApplication {

    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new HQTPModule());
    }

}
