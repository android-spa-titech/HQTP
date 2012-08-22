package org.hqtp.android;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

public interface HQTPProxy {

    // Instance methods
    // TODO: 認証の成否は例外によって知られるべき。何を戻り値にするか検討が必要。
    public abstract User authenticate(String access_token_key, String access_token_secret) throws IOException,
            JSONException, HQTPAPIException;

    // TODO: 質問投稿の成否は例外によって知られるべき。戻り値としてはおそらく投稿IDが妥当と考えられる。
    public abstract boolean postQuestion(String title, String body) throws JSONException, IOException, HQTPAPIException;

    public abstract List<Question> getQuestions() throws JSONException, IOException, HQTPAPIException;

}
