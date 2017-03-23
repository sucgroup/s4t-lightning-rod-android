package ru.esmukov.kpfu.lightningrodandroid.settings.model.reverse;

/**
 * Created by kostya on 23/03/2017.
 */

public class Server {
    String url_reverse;
    String port_reverse;

    public String getUrl() {
        return url_reverse;
    }

    public void setUrl(String url_reverse) {
        this.url_reverse = url_reverse;
    }

    public String getPort() {
        return port_reverse;
    }

    public void setPort(String port_reverse) {
        this.port_reverse = port_reverse;
    }
}
