package ru.esmukov.kpfu.lightningrodandroid.settings.model.board;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kostya on 23/03/2017.
 */

public class Board {
    String code;
    Status status;
    Position position;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Position getPosition() {
        return position;
    }

    public enum Status {
        @SerializedName("new")
        NEW,
        @SerializedName("registered")
        REGISTERED
    }
}
