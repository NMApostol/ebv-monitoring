package com.ebvmonitoring.application.views.service;

public class Service {

    private String statusimg;
    private String service;
    private String datum;
    private String uhrzeit;
    private String status;
    private String antwortzeit;

    public Service(String statusimg, String service, String datum, String uhrzeit, String status, String antwortzeit) {
        this.statusimg = statusimg;
        this.service = service;
        this.datum = datum;
        this.uhrzeit = uhrzeit;
        this.status = status;
        this.antwortzeit = antwortzeit;
    }

    public Service() {

    }

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

    public void setUhrzeit(String uhrzeit) { this.uhrzeit = uhrzeit;
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

    public String getStatusimg() {
        return statusimg;
    }

    public void setStatusimg(String statusimg) {
        this.statusimg = statusimg;
    }
}