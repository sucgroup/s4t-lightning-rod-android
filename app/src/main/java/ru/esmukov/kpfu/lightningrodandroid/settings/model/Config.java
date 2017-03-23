package ru.esmukov.kpfu.lightningrodandroid.settings.model;

import ru.esmukov.kpfu.lightningrodandroid.settings.model.board.Board;
import ru.esmukov.kpfu.lightningrodandroid.settings.model.reverse.Reverse;
import ru.esmukov.kpfu.lightningrodandroid.settings.model.socat.Socat;
import ru.esmukov.kpfu.lightningrodandroid.settings.model.wamp.Wamp;

/**
 * Created by kostya on 23/03/2017.
 */

public class Config {
    String device;
    Wamp wamp;
    Reverse reverse;
    Socat socat;
    Board board;

    public String getDevice() {
        return device;
    }

    public Wamp getWamp() {
        return wamp;
    }

    public Reverse getReverse() {
        return reverse;
    }

    public Socat getSocat() {
        return socat;
    }

    public Board getBoard() {
        return board;
    }
}
