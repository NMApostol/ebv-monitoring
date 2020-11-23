package com.ebvmonitoring.application.views.list;

import java.sql.Time;
import java.sql.Date;

public class Service {

    private String service;
    private String datum;
    private String uhrzeit;
    private String status;
    private String antwortzeit;

    public String getAntwortzeit() {
        return antwortzeit;
    }

    public void setAntwortzeit(String antwortzeit) {
        this.antwortzeit = antwortzeit;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getUhrzeit() {
        return uhrzeit;
    }

    public void setUhrzeit(String uhrzeit) {
        this.uhrzeit = uhrzeit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}