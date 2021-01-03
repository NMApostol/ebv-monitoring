package com.ebvmonitoring.application.views.addjson;





import com.ebvmonitoring.application.views.AbstractEntity;

import javax.persistence.Entity;

@Entity
public class  JSON_Schst extends AbstractEntity {

    private String wert1;
    private String wert2;
    private String wert3;
    private String wert4;
    private String id;

    public String getWert1() {
        return wert1;
    }
    public void setWert1(String wert1) {
        this.wert1 = wert1;
    }
    public String getWert2() {
        return wert2;
    }
    public void setWert2(String wert2) {
        this.wert2 = wert2;
    }
    public String getWert3() {
        return wert3;
    }
    public void setWert3(String wert3) {
        this.wert3 = wert3;
    }
    public String getWert4() {
        return wert4;
    }
    public void setWert4(String wert4) {
        this.wert4 = wert4;
    }

    public void setId(String id) {
        this.id = id;
    }
}
