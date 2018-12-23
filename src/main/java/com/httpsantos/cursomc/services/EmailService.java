package com.httpsantos.cursomc.services;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.SimpleMailMessage;

import com.httpsantos.cursomc.domain.Cliente;
import com.httpsantos.cursomc.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);

	void sendEmail(SimpleMailMessage msg);
	
	void sendOrderConfirmationHtmlEmail(Pedido obj);
	
	void sendEmailHtml(MimeMessage msg);
	
	void sendNewPasswordEmail(Cliente cliente, String newPass);

}
