package com.preventium.boxpreventium.module.ErrorEmail;

import android.util.Log;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.PasswordAuthentication;

/**
 * Created by tog on 30/08/2018.
 */

public class GMailSender {
    private final String SEND_FROM = "info@gmail.com";
    private final String SEND_TO = "hoanyprojet@gmail.com";

    private String mailhost = "smtp.gmail.com";
    private String username = "hoanyprojet@gmail.com";
    private String password = "hoanyprojet123**";
    private Properties props;

    private String subject;
    private String body;

    public GMailSender () {
        this.initProp();
    }

    public GMailSender addSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public GMailSender addBody(String body) {
        this.body = body;
        return this;
    }

    public void initProp () {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    }

    public void send () {

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SEND_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(SEND_TO));
            message.setSubject(this.subject);
            message.setText(this.body);

            new Thread() {
                public void run () {
                    try {
                        Transport.send(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    Log.v("Email:", "Sent");
                }
            }.start();


        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
}
