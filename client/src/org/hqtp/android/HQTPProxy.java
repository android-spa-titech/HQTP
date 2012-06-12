package org.hqtp.android;

import java.util.List;

public interface HQTPProxy {

    // Instance methods
    public abstract boolean authenticate(String access_token_key, String access_token_secret);

    public abstract boolean postQuestion(String title, String body);

    public abstract List<Question> getQuestions();

}
