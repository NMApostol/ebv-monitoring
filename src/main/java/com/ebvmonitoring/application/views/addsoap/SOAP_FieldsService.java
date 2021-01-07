package com.ebvmonitoring.application.views.addsoap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class SOAP_FieldsService extends CrudService<SOAP_Fields, Integer> {

    private final SOAP_FieldsRepository repository;

    public SOAP_FieldsService(@Autowired SOAP_FieldsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected SOAP_FieldsRepository getRepository() {
        return repository;
    }

}
