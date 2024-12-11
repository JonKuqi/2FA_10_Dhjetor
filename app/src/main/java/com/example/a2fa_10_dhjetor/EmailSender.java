package com.example.a2fa_10_dhjetor;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;




public class EmailSender {


    final String host = "smtp.gmail.com";
    final String port = "465";
    final String sendermail = "jonkuqi04@gmail.com";
    final String senderpassword = "uftzvxnmhfrrrwsd";



    public void sendOTPEmail(final String destinationEmail, final String subject, final String OTP) {
        new SendEmailTask().execute(destinationEmail, subject, OTP);
    }


    private static class SendEmailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String destinationEmail = params[0];
            String subject = params[1];
            String OTP = params[2];

            // Create an instance of EmailSender and send the email
            EmailSender emailSender = new EmailSender();
            emailSender.sendEmail(destinationEmail, subject, OTP);
            return null;
        }
    }


    private void sendEmail(String destinationEmail, String subject, String OTP) {

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL/TLS
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.debug", "true");


        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sendermail, senderpassword);
            }
        });


        try {
            MimeMessage message = prepareMessage(session, destinationEmail, subject,OTP);

            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {

            System.err.println("Error occurred while sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private MimeMessage prepareMessage(Session session, String destinationEmail, String subject,  String OTP) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sendermail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinationEmail));
        message.setSubject(subject);
        message.setText(OTP);
        return message;
    }
}