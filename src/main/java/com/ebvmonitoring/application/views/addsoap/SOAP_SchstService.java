package com.ebvmonitoring.application.views.addsoap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class SOAP_SchstService extends CrudService<SOAP_Schst, Integer> {

    private final SOAP_SchstRepository repository;

    public SOAP_SchstService(@Autowired SOAP_SchstRepository repository) {
        this.repository = repository;
    }

    @Override
    protected SOAP_SchstRepository getRepository() {
        return repository;
    }

}
