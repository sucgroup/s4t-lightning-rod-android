package ru.esmukov.kpfu.lightningrodandroid.settings.model.wamp;

/**
 * Created by kostya on 23/03/2017.
 */

public class Wamp {
    String url_wamp;
    String port_wamp;
    String realm;

    public String getUrl() {
        return url_wamp;
    }

    public void setUrl(String url_wamp) {
        this.url_wamp = url_wamp;
    }

    public String getPort() {
        return port_wamp;
    }

    public void setPort(String port_wamp) {
        this.port_wamp = port_wamp;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
