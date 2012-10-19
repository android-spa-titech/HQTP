package org.hqtp.android.util;

import java.util.Collection;

import android.widget.ArrayAdapter;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowArrayAdapter;

@Implements(ArrayAdapter.class)
public class HQTPShadowArrayAdapter<T> extends ShadowArrayAdapter<T> {
    public void addAll(Collection<T> items) {
        for (T obj : items) {
            this.add(obj);
        }
    }

    public void addAll(T... items) {
        for (T obj : items) {
            this.add(obj);
        }
    }
}
