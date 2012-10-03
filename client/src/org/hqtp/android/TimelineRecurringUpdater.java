package org.hqtp.android;

public interface TimelineRecurringUpdater {

    public abstract void setLectureId(int lecture_id);

    public abstract void stop();

    public abstract void registerTimelineObserver(TimelineObserver observer);

    public abstract void unregisterTimelineObserver(TimelineObserver observer);

    public abstract void startRecurringUpdateTimeline();

}
