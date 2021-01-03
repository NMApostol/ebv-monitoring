package com.ebvmonitoring.application.views.addsoap;



import com.ebvmonitoring.application.views.AbstractEntity;

import javax.persistence.Entity;


@Entity
public class SOAP_Schst extends AbstractEntity {

    private String wert1;
    private String wert2;

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

}
