package com.ebvmonitoring.application.views.settings;

import com.ebvmonitoring.application.views.JavaEmail;
import com.ebvmonitoring.application.views.addrest.REST_Fields;
import com.ebvmonitoring.application.views.addsoap.SOAP_Fields;
import com.ebvmonitoring.application.views.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.tabs.PagedTabs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@Route(value = "settings", layout = MainView.class)
@PageTitle("Einstellungen")
public class SettingsView extends Div{

    private Crud<REST_Fields> crud = new Crud<>(REST_Fields.class, createRESTEditor());
    private Crud<SOAP_Fields> crudsoap = new Crud<>(SOAP_Fields.class, createSOAPEditor());
    private VerticalLayout emailLayout = new VerticalLayout();

    public SettingsView() throws IOException {
        setId("settings-view");

        RESTCrud();
        SOAPCrud();
        generalContent();

        VerticalLayout layout = new VerticalLayout();

        Properties prop=new Properties();
        FileInputStream ip= new FileInputStream("src/schnittstellen.cfg");
        prop.load(ip);
        System.out.println(prop);

        TextField cfg = new TextField(prop.toString());

        VerticalLayout container = new VerticalLayout();
        PagedTabs tabs = new PagedTabs(container);
        tabs.add("Generelles", emailLayout, false);
        tabs.add("REST Schnittstellen bearbeiten", crud, false);
        tabs.add("SOAP Schnittstellen bearbeiten", crudsoap, false);
        tabs.add("Config File", cfg, false);
        layout.add(tabs, container);

        add(layout);
    }

    private void generalContent(){
        EmailField emailField = new EmailField("E-Mail");
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Bitte geben Sie eine gültige E-Mail Adresse ein");
        emailField.setValue(JavaEmail.toEmails[0]);
        emailField.getStyle().set("width", "30%");
        Button savebutton = new Button("Speichern");
        savebutton.setEnabled(false);
        emailField.addValueChangeListener(e -> {
            if(!emailField.isInvalid()) {
                savebutton.setEnabled(true);
            }
        });

        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Text t1 = new Text("Soll die E-Mail Adresse, an die Warnungen von Servicesgeschickt werden soll, geändert werden.");

        Button confirmButton = new Button("Bestätigen", event -> {
            JavaEmail.toEmails[0] = emailField.getValue();
            dialog.close();
        });
        Button cancelButton = new Button("Abbrechen", event -> {
            dialog.close();
        });
        dialog.add(t1, confirmButton, cancelButton);

        savebutton.addClickListener(e -> {
            dialog.open();
            System.out.println(emailField.getValue());
            Notification.show("Alerting Email Adresse geändert zu " + emailField.getValue());
            savebutton.setEnabled(false);
        });

        emailLayout.add(emailField, savebutton);
    }

    private void RESTCrud(){

        Span footer = new Span();
        footer.getElement().getStyle().set("flex", "1");

        Button newItemButton = new Button("REST hinzufügen ...");
        newItemButton.addClickListener(e -> crud.edit(new REST_Fields(), Crud.EditMode.NEW_ITEM));

        crud.setToolbar(footer, newItemButton);

        /*PersonDataProvider dataProvider = new PersonDataProvider();
        dataProvider.setSizeChangeListener(count -> footer.setText("Total: " + count));

        crud.getGrid().removeColumnByKey("id");
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));*/
    }

    private CrudEditor<REST_Fields> createRESTEditor() {
        TextField restName = new TextField("REST Name");
        TextField restLink = new TextField("REST Link");
        TextField stringInput = new TextField("String Input");
        FormLayout form = new FormLayout(restName, restLink, stringInput);

        Binder<REST_Fields> binder = new Binder<>(REST_Fields.class);
        binder.bind(restName, REST_Fields::getRest_name, REST_Fields::setRest_name);
        binder.bind(restLink, REST_Fields::getRest_link, REST_Fields::setRest_link);
        binder.bind(stringInput, REST_Fields::getString_input, REST_Fields::setString_input);

        return new BinderCrudEditor<>(binder, form);
    }

    private void SOAPCrud(){


        Span footer = new Span();
        footer.getElement().getStyle().set("flex", "1");

        Button newItemButton = new Button("SOAP hinzufügen ...");
        newItemButton.addClickListener(e -> crudsoap.edit(new SOAP_Fields(), Crud.EditMode.NEW_ITEM));

        crudsoap.setToolbar(footer, newItemButton);

        /*PersonDataProvider dataProvider = new PersonDataProvider();
        dataProvider.setSizeChangeListener(count -> footer.setText("Total: " + count));

        crud.getGrid().removeColumnByKey("id");
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));*/
    }

    private CrudEditor<SOAP_Fields> createSOAPEditor() {
        TextField soapLink = new TextField("SOAP Link");
        TextField stringInput = new TextField("String Input");
        FormLayout form = new FormLayout(soapLink, stringInput);

        Binder<SOAP_Fields> binder = new Binder<>(SOAP_Fields.class);
        binder.bind(soapLink, SOAP_Fields::getSoap_link, SOAP_Fields::setSoap_link);
        binder.bind(stringInput, SOAP_Fields::getString_input, SOAP_Fields::setString_input);

        return new BinderCrudEditor<>(binder, form);
    }
}
