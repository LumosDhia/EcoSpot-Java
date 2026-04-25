package tn.esprit.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public static void sendEmail(String to, String subject, String body) throws MessagingException {
        String host = Config.get("MAIL_HOST", "smtp.gmail.com");
        String port = Config.get("MAIL_PORT", "587");
        String user = Config.get("MAIL_USER");
        String pass = Config.get("MAIL_PASS");

        if (user == null || pass == null) {
            throw new MessagingException("Email configuration missing in .env file (MAIL_USER or MAIL_PASS)");
        }
        System.out.println("Attempting to send email using: " + user);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        props.put("mail.smtp.trust", "*");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
