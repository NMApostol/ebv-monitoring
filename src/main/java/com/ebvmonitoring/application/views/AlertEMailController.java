package com.ebvmonitoring.application.views;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static javax.mail.Transport.send;

public class AlertEMailController {

    public AlertEMailController(){}


    public static void sendMail(String recipient) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        String myAccount = "xxxxx@gmail.com";
        String password = "xxxxx";

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccount, password);
            }
        });

        Message msg = prepareMessage(session, myAccount, recipient);

        assert msg != null;
        send(msg);
        System.out.println("Message erfolgreich");
    }

    private static Message prepareMessage(Session session, String myAccount, String recipient) {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(myAccount));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            msg.setSubject("Hallo");
            String htmlCode = "<h1> WARNUNG </h1>";
            msg.setContent(htmlCode, "text/html");
            return msg;
        } catch (Exception ex) {
            //Logger.getLogger(JavaMailUtil.class.getName()).log(level.SEVERE, null, ex);
        }

        return null;
    }

}

