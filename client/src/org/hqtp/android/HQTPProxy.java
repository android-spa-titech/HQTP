package org.hqtp.android;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

public interface HQTPProxy {

    // Instance methods
    // TODO: 認証の成否は例外によって知られるべき。何を戻り値にするか検討が必要。
    public abstract boolean authenticate(String access_token_key, String access_token_secret) throws IOException,
            JSONException, HQTPAPIException;

    // TODO: 質問投稿の成否は例外によって知られるべき。戻り値としてはおそらく投稿IDが妥当と考えられる。
    @Deprecated
    public abstract boolean postQuestion(String title, String body) throws JSONException, IOException, HQTPAPIException;

    @Deprecated
    public abstract List<Question> getQuestions() throws JSONException, IOException, HQTPAPIException;

    public abstract List<Post> getTimeline();

    public abstract Post postTimeline(String body, String lectureId, String prevVirtualTimestamp,
            String nextVirtualTimestamp);
}
