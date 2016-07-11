package com.mobile.android.smartick.network;

/**
 * Created by sbarrio on 5/7/16.
 */
public class RememberPasswordMobileResponse {

    private String studentUsername;
    private String tutorUsername;
    private String response;

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getTutorUsername() {
        return tutorUsername;
    }

    public void setTutorUsername(String tutorUsername) {
        this.tutorUsername = tutorUsername;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
