package ru.esmukov.kpfu.lightningrodandroid.settings.model.board;

/**
 * Created by kostya on 23/03/2017.
 */

public class Board {
    String code;
    String status; // todo enum
    Position position;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Position getPosition() {
        return position;
    }

}
