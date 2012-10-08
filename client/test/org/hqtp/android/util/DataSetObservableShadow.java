package org.hqtp.android.util;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(DataSetObservable.class)
public class DataSetObservableShadow extends ObservableShadow<DataSetObserver> {
    public void unregisterAll() {
        observers.clear();
    }

    public void notifyChanged() {
        for (DataSetObserver observer : observers) {
            observer.onChanged();
        }
    }

    public void notifyInvalidated() {
        for (DataSetObserver observer : observers) {
            observer.onInvalidated();
        }

    }
}
