/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import java.util.Date;
/*    */ import java.util.Properties;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ import javax.mail.Address;
/*    */ import javax.mail.Authenticator;
/*    */ import javax.mail.Message;
/*    */ import javax.mail.PasswordAuthentication;
/*    */ import javax.mail.Session;
/*    */ import javax.mail.Transport;
/*    */ import javax.mail.internet.InternetAddress;
/*    */ import javax.mail.internet.MimeMessage;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SendEmail
/*    */ {
/*    */   private static Properties smtpProps;
/*    */   private static Session session;
/*    */   
/*    */   public static void initializeSMTP(int ioTimeout) {
/* 36 */     smtpProps = new Properties();
/* 37 */     if (ZeusServerCfg.getInstance().getSmtpServerRequiresAuth()) {
/* 38 */       smtpProps.put("mail.smtp.auth", "true");
/*    */     } else {
/* 40 */       smtpProps.put("mail.smtp.auth", "false");
/*    */     } 
/*    */     
/* 43 */     smtpProps.put("mail.smtp.host", ZeusServerCfg.getInstance().getSmtpServer());
/* 44 */     smtpProps.put("mail.smtp.port", ZeusServerCfg.getInstance().getSmtpPort());
/* 45 */     if (ioTimeout > 0) {
/* 46 */       smtpProps.put("mail.smtp.timeout", Integer.valueOf(ioTimeout));
/*    */     }
/*    */     
/* 49 */     if (ZeusServerCfg.getInstance().getSmtpServerRequiresAuth()) {
/* 50 */       session = Session.getInstance(smtpProps, new Authenticator()
/*    */           {
/*    */             protected PasswordAuthentication getPasswordAuthentication()
/*    */             {
/* 54 */               return new PasswordAuthentication(ZeusServerCfg.getInstance().getPop3User(), ZeusServerCfg.getInstance().getPop3Pass());
/*    */             }
/*    */           });
/*    */     } else {
/* 58 */       session = Session.getInstance(smtpProps, null);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void sendMail(String to, String subject, String mailBody, boolean isBodyHtml, int priority) {
/*    */     try {
/* 64 */       if (mailBody != null && mailBody.length() > 0) {
/* 65 */         MimeMessage mimeMessage = new MimeMessage(session);
/* 66 */         mimeMessage.setHeader("X-Priority", String.valueOf(priority));
/* 67 */         mimeMessage.setFrom((Address)new InternetAddress(ZeusServerCfg.getInstance().getMailAccount(), ZeusServerCfg.getInstance().getNameSender()));
/* 68 */         mimeMessage.setRecipients(Message.RecipientType.TO, (Address[])InternetAddress.parse(to));
/* 69 */         mimeMessage.setSubject(subject);
/* 70 */         mimeMessage.setSentDate(new Date());
/* 71 */         if (isBodyHtml) {
/* 72 */           mimeMessage.setContent(mailBody, "text/html");
/*    */         } else {
/* 74 */           mimeMessage.setText(mailBody);
/*    */         } 
/* 76 */         Transport.send((Message)mimeMessage);
/*    */       } 
/* 78 */     } catch (UnsupportedEncodingException|javax.mail.MessagingException ex) {
/* 79 */       Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static boolean getMailSessionExists() {
/* 84 */     return (smtpProps != null && session != null);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\SendEmail.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */