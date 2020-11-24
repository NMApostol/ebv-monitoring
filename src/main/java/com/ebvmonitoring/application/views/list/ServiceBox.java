package com.ebvmonitoring.application.views.list;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;

public class ServiceBox {
    private String servicesName;
    private String servicesStatus;

    public ServiceBox(String servicesName, String servicesStatus) {
        this.servicesName = servicesName;
        this.servicesStatus = servicesStatus;
    }

    public String getServicesName() {
        return servicesName;
    }

    public void setServicesName(String servicesName) {
        this.servicesName = servicesName;
    }

    public String getServicesStatus() {
        return servicesStatus;
    }

    public void setServicesStatus(String servicesStatus) {
        this.servicesStatus = servicesStatus;
    }
}
