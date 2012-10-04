package org.hqtp.android;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;

public interface HQTPProxy {

    // Instance methods
    // TODO: 認証の成否は例外によって知られるべき。何を戻り値にするか検討が必要。
    public abstract User authenticate(String access_token_key, String access_token_secret) throws IOException,
            JSONException, HQTPAPIException;

    public abstract List<Post> getTimeline(int lectureId) throws IOException, HQTPAPIException, JSONException,
            ParseException;

    /**
     * @param body 送信する投稿本文
     * @param lectureId 授業ID
     * @param prevVirtualTimestamp 挿入したい時間の前の投稿。指定しない場合は負数を指定する
     * @param nextVirtualTimestamp 挿入したい時間の次の投稿。指定しない場合は負数を指定する
     * @return 投稿したPostを返す
     * @throws IOException
     * @throws HQTPAPIException
     * @throws JSONException
     * @throws ParseException
     */
    public abstract Post postTimeline(String body, int lectureId, long prevVirtualTimestamp,
            long nextVirtualTimestamp) throws IOException, HQTPAPIException, JSONException, ParseException;

    public abstract Lecture addLecture(String lectureCode, String lectureName) throws HQTPAPIException, IOException,
            JSONException, ParseException;
}
