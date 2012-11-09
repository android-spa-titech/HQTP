package org.hqtp.android;

import java.util.SortedSet;

public interface TimelineObserver {
    void onUpdate(SortedSet<Post> posts);
}
