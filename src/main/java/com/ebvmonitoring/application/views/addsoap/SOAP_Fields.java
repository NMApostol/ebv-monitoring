package com.ebvmonitoring.application.views.addsoap;



import com.ebvmonitoring.application.views.AbstractEntity;

import javax.persistence.Entity;


@Entity
public class SOAP_Fields extends AbstractEntity {

    private String soap_link;
    private String string_input;

    public String getSoap_link() {
        return soap_link;
    }
    public void setSoap_link(String soap_link) {
        this.soap_link = soap_link;
    }

    public String getString_input() { return string_input; }
    public void setString_input(String string_input) { this.string_input = string_input; }

}
