package com.ebvmonitoring.application.views.settings;

import com.ebvmonitoring.application.views.mail.JavaEmail;
import com.ebvmonitoring.application.views.addrest.REST_Fields;
import com.ebvmonitoring.application.views.main.MainView;
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
import java.io.IOException;
import java.util.Properties;

@Route(value = "settings", layout = MainView.class)
@PageTitle("Einstellungen")
public class SettingsView extends Div{

    private Crud<REST_Fields> crud = new Crud<>(REST_Fields.class, createRESTEditor());
    private VerticalLayout emailLayout = new VerticalLayout();

    public SettingsView() throws IOException {
        setId("settings-view");

        RESTCrud();
        generalContent();

        VerticalLayout layout = new VerticalLayout();

        add(emailLayout);
    }

    private void generalContent(){
        TextField emailsenderField = new TextField("E-Mail Sender");
        emailsenderField.setClearButtonVisible(true);
        emailsenderField.setErrorMessage("Bitte geben Sie eine gültige E-Mail Adresse ein");
        emailsenderField.setValue(JavaEmail.toEmails[0]);
        emailsenderField.getStyle().set("width", "30%");

        TextField emailreceiverField = new TextField("E-Mail Receiver");
        emailreceiverField.setClearButtonVisible(true);
        emailreceiverField.setErrorMessage("Bitte geben Sie eine gültige E-Mail Adresse ein");
        emailreceiverField.setValue(JavaEmail.toEmails[0]);
        emailreceiverField.getStyle().set("width", "30%");

        Button savebutton = new Button("Speichern");

        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Text t1 = new Text("Soll die E-Mail Adresse, an die Warnungen von Servicesgeschickt werden soll, geändert werden.");

        Button confirmButton = new Button("Bestätigen", event -> {
            JavaEmail.toEmails[0] = emailsenderField.getValue();
            JavaEmail.fromUser = emailreceiverField.getValue();
            dialog.close();
        });
        Button cancelButton = new Button("Abbrechen", event -> {
            dialog.close();
        });
        dialog.add(t1, confirmButton, cancelButton);

        savebutton.addClickListener(e -> {
            dialog.open();
            System.out.println(emailsenderField.getValue());
            System.out.println(emailsenderField.getValue());
            Notification.show("Alerting Email Sender Adresse geändert zu " + emailsenderField.getValue());
            Notification.show("Alerting Email Receiver Adresse geändert zu " + emailreceiverField.getValue());
            savebutton.setEnabled(false);
        });

        emailLayout.add(emailsenderField, emailreceiverField, savebutton);
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

}
