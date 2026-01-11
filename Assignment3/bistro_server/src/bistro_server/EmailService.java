package bistro_server;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

	private final String username = System.getenv("BISTRO_EMAIL_USER");
    private final String password = System.getenv("BISTRO_EMAIL_PASS");
    public void sendEmail(String recipient, String subject, String content) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com"); 
        prop.put("mail.smtp.port", "587");            
        prop.put("mail.smtp.auth", "true");           
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username,"Bistro Management"));

            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipient)
            );

            message.setSubject(subject);
            message.setText(content); 

            Transport.send(message);

            System.out.println("Email Sent Successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }

}