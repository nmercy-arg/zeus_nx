/*     */ package com.zeusServer.ddns.client;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.StringReader;
/*     */ import java.net.Authenticator;
/*     */ import java.net.PasswordAuthentication;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.text.MessageFormat;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class DNSMadeEasyUpdater
/*     */   extends DDNSUpdater
/*     */ {
/*  33 */   private final String URL_TEMPLATE = "https://www.dnsmadeeasy.com/servlet/updateip?username={0}&password={1}&id={2}&ip={3}";
/*     */   
/*     */   public DNSMadeEasyUpdater(String[] settings) {
/*  36 */     super(settings);
/*  37 */     this.name = "DNS Made Easy";
/*     */   }
/*     */ 
/*     */   
/*     */   protected boolean update(String newIP) throws Exception {
/*  42 */     String urlString = MessageFormat.format("https://www.dnsmadeeasy.com/servlet/updateip?username={0}&password={1}&id={2}&ip={3}", new Object[] { this.settings[1], this.settings[2], this.settings[3], newIP });
/*     */     
/*  44 */     URL url2 = new URL(urlString);
/*  45 */     Authenticator.setDefault(new Authenticator()
/*     */         {
/*     */           protected PasswordAuthentication getPasswordAuthentication() {
/*  48 */             return new PasswordAuthentication(DNSMadeEasyUpdater.this.settings[1], DNSMadeEasyUpdater.this.settings[2].toCharArray());
/*     */           }
/*     */         });
/*  51 */     URLConnection conn = url2.openConnection();
/*  52 */     BufferedReader br2 = null;
/*  53 */     StringBuilder sb = new StringBuilder();
/*     */     try {
/*  55 */       br2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
/*  56 */       sb.append(br2.readLine());
/*     */     } finally {
/*  58 */       if (br2 != null) {
/*     */         try {
/*  60 */           br2.close();
/*  61 */         } catch (IOException iOException) {}
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/*  66 */     String result = sb.toString();
/*  67 */     if (result != null) {
/*  68 */       if (result.equals("success")) {
/*  69 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("IP_address_updated_successfully_on_the_DDNS_provider") + " [ " + this.name + " ]", Enums.EnumMessagePriority.LOW, null, null);
/*  70 */         return true;
/*  71 */       }  if (result.contains("error-auth") || result
/*  72 */         .contains("error-auth-suspend") || result
/*  73 */         .contains("error-auth-voided") || result
/*  74 */         .contains("error-record-auth")) {
/*  75 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("The_DDNS_provider_reported_a_permanent_failure_status") + result, Enums.EnumMessagePriority.HIGH, null, null);
/*  76 */         GlobalVariables.buzzerActivated = true;
/*  77 */         return false;
/*  78 */       }  if (resultIncludesOtherThanNoChange(result)) {
/*  79 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("The_DDNS_provider_reported_a_transient_failure_status") + result, Enums.EnumMessagePriority.AVERAGE, null, null);
/*  80 */         return false;
/*     */       } 
/*     */     } 
/*  83 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   protected String getDDNSProviderName() {
/*  88 */     return this.name;
/*     */   }
/*     */   
/*     */   private boolean resultIncludesOtherThanNoChange(String result) {
/*     */     try {
/*  93 */       Pattern pattern = Pattern.compile("\\d* error-record-ip-same");
/*  94 */       BufferedReader reader = new BufferedReader(new StringReader(result));
/*     */       
/*     */       String line;
/*  97 */       while (null != (line = reader.readLine())) {
/*  98 */         line = line.trim();
/*  99 */         if (line.isEmpty()) {
/*     */           continue;
/*     */         }
/* 102 */         if (!pattern.matcher(line).matches()) {
/* 103 */           return true;
/*     */         }
/*     */       } 
/* 106 */       return false;
/* 107 */     } catch (IOException e) {
/* 108 */       throw new RuntimeException(e);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\ddns\client\DNSMadeEasyUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */