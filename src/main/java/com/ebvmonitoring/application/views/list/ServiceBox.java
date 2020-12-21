package com.ebvmonitoring.application.views.list;

import com.vaadin.flow.component.button.Button;

public class ServiceBox {
    private String servicesName;
    private Button servicesStatus;

    public ServiceBox(String servicesName, Button servicesStatus) {
        this.servicesName = servicesName;
        this.servicesStatus = servicesStatus;
    }

    public ServiceBox() {

    }

    public String getServicesName() {
        return servicesName;
    }

    public void setServicesName(String servicesName) {
        this.servicesName = servicesName;
    }

    public Button getServicesStatus() {
        return servicesStatus;
    }

    public void setServicesStatus(Button servicesStatus) {
        this.servicesStatus = servicesStatus;
    }
}
