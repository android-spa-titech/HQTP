package org.hqtp.android;

import java.util.List;

public interface HQTPProxy {

    // Instance methods
    public abstract HQTPProxy setAccessToken(String access_token);

    public abstract boolean authenticate();

    public abstract boolean postQuestion(String title, String body);

    public abstract List<Question> getQuestions();

}
