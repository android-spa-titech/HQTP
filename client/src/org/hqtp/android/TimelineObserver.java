package org.hqtp.android;

import java.util.List;

public interface TimelineObserver {
    void onUpdate(List<Post> posts);
}
