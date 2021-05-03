package com.ecm.batchprocess.util;

import com.ecm.batchprocess.ControlFileGeneration;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class Email {
    public static void SendEmail() {
        ControlFileGeneration cf = new ControlFileGeneration();
        String to = cf.getPropertyValue("BusinessEmailID");
        final String user = cf.getPropertyValue("FromEmailID");
        final String password = cf.getPropertyValue("PasswordForEmail");
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", cf.getPropertyValue("HostDetailForEmail"));
        properties.setProperty("mail.smtp.port", cf.getPropertyValue("PortNumberForEmail"));
        properties.put("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(properties,
                new Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }

                });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom((Address) new InternetAddress(user));
            message.addRecipient(MimeMessage.RecipientType.TO, (Address) new InternetAddress(to));
            message.setSubject(cf.getPropertyValue("Subject"));
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setText(cf.getPropertyValue("Message"));

            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart((BodyPart) mimeBodyPart);
            message.setContent((Multipart) mimeMultipart);
            Transport.send((Message) message);
            System.out.println("message sent....");
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }

    }
}