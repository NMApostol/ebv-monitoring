package com.ebvmonitoring.application.views.addjson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class JSON_SchstService extends CrudService<JSON_Schst, Integer> {

    private final JSON_SchstRepository repository;

    public JSON_SchstService(@Autowired JSON_SchstRepository repository) {
        this.repository = repository;
    }

    @Override
    protected JSON_SchstRepository getRepository() {
        return repository;
    }

}
