package com.ebvmonitoring.application.views.addrest;





import com.ebvmonitoring.application.views.AbstractEntity;

import javax.persistence.Entity;

@Entity
public class REST_Fields extends AbstractEntity {

    private String rest_link;
    private String string_input;

    public String getRest_link() {
        return rest_link;
    }
    public void setRest_link(String rest_link) {
        this.rest_link = rest_link;
    }

    public String getString_input() { return string_input; }
    public void setString_input(String string_input) { this.string_input = string_input; }
}
