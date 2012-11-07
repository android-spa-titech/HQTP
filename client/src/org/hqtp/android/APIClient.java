package org.hqtp.android;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;

public interface APIClient {

    // Instance methods
    public abstract User authenticate(String access_token_key, String access_token_secret) throws IOException,
            JSONException, HQTPAPIException;

    public abstract void setUserId(int userId);

    public abstract int getUserId();

    public abstract List<Post> getTimeline(int lectureId) throws IOException, HQTPAPIException, JSONException,
            ParseException;

    public abstract List<Post> getTimeline(int lectureId, int sinceId) throws IOException, HQTPAPIException, JSONException,
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

    /**
     * @param image 投稿する画像ファイル
     * @param lectureId 授業ID
     * @param prevVirtualTimestamp 挿入したい時間の前の投稿。指定しない場合は負数を指定する
     * @param nextVirtualTimestamp 挿入したい時間の次の投稿。指定しない場合は負数を指定する
     * @return 投稿したPostを返す
     * @throws IOException
     * @throws HQTPAPIException
     * @throws JSONException
     * @throws ParseException
     */
    public abstract Post postTimeline(File image, int lectureId, long prevVirtualTimestamp,
            long nextVirtualTimestamp) throws IOException, HQTPAPIException, JSONException, ParseException;

    public abstract List<Lecture> getLectures() throws HQTPAPIException, IOException, JSONException, ParseException;

    public abstract Lecture addLecture(String lectureCode, String lectureName) throws HQTPAPIException, IOException,
            JSONException, LectureAlreadyCreatedException, ParseException;

    public abstract AchievementResponse getAchievements(int user_id) throws HQTPAPIException, IOException,
            JSONException, ParseException;

    public abstract AchievementResponse getAchievements(int user_id, int since_id) throws HQTPAPIException,
            IOException, JSONException, ParseException;

    public abstract User getUser(int user_id) throws HQTPAPIException, IOException, JSONException, ParseException;
}
