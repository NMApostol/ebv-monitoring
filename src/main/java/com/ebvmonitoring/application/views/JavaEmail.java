package com.ebvmonitoring.application.views;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class JavaEmail {
    Session mailSession;

    public JavaEmail(){}

    public static String[] toEmails = {"xxxxxxx@xxxxx.com"}; //an wen gesendet werden soll

    public static void JavaEmailMain() throws MessagingException {
        JavaEmail javaEmail = new JavaEmail();
        javaEmail.setMailServerProperties();
        javaEmail.draftEmailMessage();
        javaEmail.sendEmail();
    }

    private void setMailServerProperties() {
        Properties emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", "587");
        emailProperties.put("mail.smtp.auth", "true");
        emailProperties.put("mail.smtp.starttls.enable", "true");
        emailProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com"); //auf outlook office356 statt gmail
        mailSession = Session.getDefaultInstance(emailProperties, null);
    }

    private MimeMessage draftEmailMessage() throws AddressException, MessagingException {
        String emailSubject = "SERVICE WARNUNG!";
        String emailBody = "Der Service <Hier den Servicename aus der DB einfügen> hat ein Problerm und funktioniert nicht wie sie soll!";
        MimeMessage emailMessage = new MimeMessage(mailSession);
        /**
         * Set the mail recipients
         * */
        for (int i = 0; i < toEmails.length; i++) {
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
        }
        emailMessage.setSubject(emailSubject);
        /**
         * If sending HTML mail
         * */
        emailMessage.setContent(emailBody, "text/html");
        /**
         * If sending only text mail
         * */
        emailMessage.setText(emailBody);// for a text email
        return emailMessage;
    }

    private void sendEmail() throws MessagingException {
        /**
         * Sender's credentials
         * */
        String fromUser = "xxxx@xxxxx.com"; //hier die helpdesk mail
        String fromUserEmailPassword = "xxxxx"; //hier das helpdesk passwort

        String emailHost = "smtp.gmail.com"; //auf outlook office356 statt gmail
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromUser, fromUserEmailPassword);
        /**
         * Draft the message
         * */
        MimeMessage emailMessage = draftEmailMessage();
        /**
         * Send the mail
         * */
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        System.out.println("Email sent successfully.");
    }
}
