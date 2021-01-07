package com.ebvmonitoring.application.views.addrest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class REST_FieldsService extends CrudService<REST_Fields, Integer> {

    private final REST_FieldsRepository repository;

    public REST_FieldsService(@Autowired REST_FieldsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected REST_FieldsRepository getRepository() {
        return repository;
    }

}
