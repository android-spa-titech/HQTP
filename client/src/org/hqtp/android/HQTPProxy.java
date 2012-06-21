package org.hqtp.android;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

public interface HQTPProxy {

    // Instance methods
    public abstract boolean authenticate(String access_token_key, String access_token_secret) throws IOException,
            JSONException;

    public abstract boolean postQuestion(String title, String body) throws JSONException, IOException;

    public abstract List<Question> getQuestions() throws JSONException, IOException;

}
