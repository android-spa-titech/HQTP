package org.hqtp.android;

@Deprecated
public class Question {
    private String title;
    private String body;
    private String author;

    public Question(String title, String body, String author) {
        this.title = title;
        this.body = body;
        this.author = author;
    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return this.body;
    }

    public String getAuthor() {
        return this.author;
    }
}
