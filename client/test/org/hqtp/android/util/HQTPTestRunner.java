package org.hqtp.android.util;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

public class HQTPTestRunner extends RobolectricTestRunner {
    public HQTPTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected void bindShadowClasses() {
        super.bindShadowClasses();
        Robolectric.bindShadowClass(ObservableShadow.class);
        Robolectric.bindShadowClass(DataSetObservableShadow.class);
        Robolectric.bindShadowClass(HQTPShadowArrayAdapter.class);
        Robolectric.bindShadowClass(HQTPShadowListView.class);
    }
}
