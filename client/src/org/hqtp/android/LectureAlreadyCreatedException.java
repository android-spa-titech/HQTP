package org.hqtp.android;

public class LectureAlreadyCreatedException extends HQTPAPIException {
    private static final long serialVersionUID = 1068631803208877460L;
    public Lecture lecture;

    public LectureAlreadyCreatedException(Lecture lecture, String message) {
        super(message);
        this.lecture = lecture;
    }
}
