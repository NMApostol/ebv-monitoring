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
import com.vaadin.flow.component.textfield.PasswordField;
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

    //private Crud<REST_Fields> crud = new Crud<>(REST_Fields.class, createRESTEditor());
    private final VerticalLayout emailLayout = new VerticalLayout();

    public SettingsView() throws IOException {
        setId("settings-view");

        //RESTCrud();
        generalContent();

        add(emailLayout);
    }

    private void generalContent(){
        TextField emailsenderField = new TextField("E-Mail Sender");
        emailsenderField.setClearButtonVisible(true);
        emailsenderField.setErrorMessage("Bitte geben Sie eine gültige E-Mail Adresse ein");
        emailsenderField.setValue(JavaEmail.fromUser);
        emailsenderField.getStyle().set("width", "30%");

        PasswordField emailsenderPWField = new PasswordField("E-Mail Sender Passwort");
        emailsenderPWField.setClearButtonVisible(true);
        emailsenderPWField.setValue(JavaEmail.fromUserPW);
        emailsenderPWField.setRevealButtonVisible(false);
        emailsenderPWField.getStyle().set("width", "30%");

        TextField emailreceiverField = new TextField("E-Mail Receiver");
        emailreceiverField.setClearButtonVisible(true);
        emailreceiverField.setErrorMessage("Bitte geben Sie eine gültige E-Mail Adresse ein");
        emailreceiverField.setValue(JavaEmail.toEmails[0]);
        emailreceiverField.getStyle().set("width", "30%");

        TextField smtpField = new TextField("SMTP Host");
        smtpField.setClearButtonVisible(true);
        smtpField.setErrorMessage("Bitte geben Sie einen gültigen SMTP Host ein");
        smtpField.setValue(JavaEmail.smtp_host);
        smtpField.getStyle().set("width", "30%");

        TextField smtpportField = new TextField("SMTP Port");
        smtpportField.setClearButtonVisible(true);
        smtpportField.setErrorMessage("Bitte geben Sie einen gültigen SMTP Host ein");
        smtpportField.setValue(JavaEmail.smtp_port);
        smtpportField.getStyle().set("width", "30%");

        Button savebutton = new Button("Speichern");

        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Text t1 = new Text("Soll die E-Mail Adresse, an die Warnungen von Servicesgeschickt werden soll, geändert werden.");

        Button confirmButton = new Button("Bestätigen", event -> {
            JavaEmail.toEmails[0] = emailreceiverField.getValue();
            JavaEmail.fromUser = emailsenderField.getValue();
            JavaEmail.smtp_host = smtpField.getValue();
            JavaEmail.smtp_port = smtpportField.getValue();
            JavaEmail.fromUserPW = emailsenderPWField.getValue();
            System.out.println(emailsenderField.getValue());
            System.out.println(emailsenderField.getValue());
            System.out.println(smtpField.getValue());
            Notification.show("Alerting Email Einstellungen gespeichert");
            dialog.close();
        });
        Button cancelButton = new Button("Abbrechen", event -> {
            dialog.close();
        });
        dialog.add(t1, confirmButton, cancelButton);

        savebutton.addClickListener(e -> {
            dialog.open();
            savebutton.setEnabled(false);
        });

        emailLayout.add(emailsenderField, emailsenderPWField, smtpField, smtpportField, emailreceiverField, savebutton);
    }

}
