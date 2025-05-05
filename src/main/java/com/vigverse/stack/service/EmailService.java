package com.vigverse.stack.service;


import com.vigverse.stack.dto.MailBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendSimpleMessage(MailBody mailBody){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(mailBody.to());
        simpleMailMessage.setSubject(mailBody.subject());
        simpleMailMessage.setFrom(mailBody.from());
        simpleMailMessage.setText(mailBody.text());
        javaMailSender.send(simpleMailMessage);
    }
}
