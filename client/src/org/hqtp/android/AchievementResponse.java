package org.hqtp.android;

import java.util.List;

public class AchievementResponse {
    private List<Achievement> achievements = null;
    private int total_point = 0;

    public AchievementResponse(List<Achievement> achievements, int total_point) {
        this.achievements = achievements;
        this.total_point = total_point;
    }

    public List<Achievement> getAchievements()
    {
        return achievements;
    }

    public int getTotalPoint()
    {
        return total_point;
    }
}
