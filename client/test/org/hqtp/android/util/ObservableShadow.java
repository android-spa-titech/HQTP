package org.hqtp.android.util;

import java.util.HashSet;
import java.util.Set;

import android.database.Observable;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(Observable.class)
public class ObservableShadow<T> {
    protected Set<T> observers = new HashSet<T>();

    public void registerObserver(T observer) {
        observers.add(observer);
    }

    public void unregisterObserver(T observer) {
        observers.remove(observer);
    }
}
