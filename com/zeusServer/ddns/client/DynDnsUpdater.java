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
/*     */ import java.net.IDN;
/*     */ import java.net.PasswordAuthentication;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.text.MessageFormat;
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
/*     */ public class DynDnsUpdater
/*     */   extends DDNSUpdater
/*     */ {
/*  33 */   private final String URL_TEMPLATE = "https://members.dyndns.org/nic/update?hostname={0}&myip={1}&wildcard=NOCHG&mx=NOCHG&backmx=NOCHG";
/*     */   
/*     */   public DynDnsUpdater(String[] settings) {
/*  36 */     super(settings);
/*  37 */     this.name = "Dyn DNS";
/*     */   }
/*     */ 
/*     */   
/*     */   protected boolean update(String newIP) throws Exception {
/*  42 */     String urlString = MessageFormat.format("https://members.dyndns.org/nic/update?hostname={0}&myip={1}&wildcard=NOCHG&mx=NOCHG&backmx=NOCHG", new Object[] { idnToAscii(this.settings[3]), newIP });
/*     */     
/*  44 */     URL url2 = new URL(urlString);
/*  45 */     Authenticator.setDefault(new Authenticator()
/*     */         {
/*     */           protected PasswordAuthentication getPasswordAuthentication() {
/*  48 */             return new PasswordAuthentication(DynDnsUpdater.this.settings[1], DynDnsUpdater.this.settings[2].toCharArray());
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
/*     */     
/*  67 */     String result = sb.toString();
/*  68 */     if (result != null) {
/*  69 */       if (result.contains("good 127.0.0.1") || result.contains("badauth") || result
/*  70 */         .contains("badauth") || result.contains("!donator") || result
/*  71 */         .contains("notfqdn") || result.contains("nohost") || result
/*  72 */         .contains("numhost") || result.contains("abuse") || result
/*  73 */         .contains("badagent") || 
/*  74 */         resultIncludesOtherThanSuccessfulNoChangeMaintenance(result))
/*  75 */       { Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("The_DDNS_provider_reported_a_permanent_failure_status") + result, Enums.EnumMessagePriority.HIGH, null, null);
/*  76 */         GlobalVariables.buzzerActivated = true; }
/*  77 */       else { if (result.equals("dnserr") || result.equals("911")) {
/*  78 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("An_error_occurred_or_a_maintenance_is_going_on_with_the_DDNS_provider") + result, Enums.EnumMessagePriority.HIGH, null, null);
/*  79 */           GlobalVariables.buzzerActivated = true;
/*  80 */           return false;
/*  81 */         }  if (result.contains("nochg")) {
/*  82 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("IP_address_updated_successfully_on_the_DDNS_provider") + " [ " + this.name + " ]", Enums.EnumMessagePriority.LOW, null, null);
/*  83 */           return true;
/*     */         } 
/*  85 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("IP_address_updated_successfully_on_the_DDNS_provider") + " [ " + this.name + " ]", Enums.EnumMessagePriority.LOW, null, null);
/*  86 */         return true; }
/*     */     
/*     */     }
/*  89 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   protected String getDDNSProviderName() {
/*  94 */     return this.name;
/*     */   }
/*     */   
/*     */   private String idnToAscii(String domain) {
/*  98 */     return IDN.toASCII(domain, 3);
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean resultIncludesOtherThanSuccessfulNoChangeMaintenance(String result) {
/*     */     try {
/* 104 */       BufferedReader reader = new BufferedReader(new StringReader(result));
/*     */       
/*     */       String line;
/* 107 */       while (null != (line = reader.readLine())) {
/* 108 */         line = line.trim();
/* 109 */         if (line.isEmpty()) {
/*     */           continue;
/*     */         }
/*     */         
/* 113 */         int i = line.indexOf(' ');
/* 114 */         String word = (i == -1) ? line : line.substring(0, i);
/* 115 */         if (!word.equals("good") && !word.equals("nochg") && 
/* 116 */           !word.equals("dnserr") && !word.equals("911")) {
/* 117 */           return true;
/*     */         }
/*     */       } 
/* 120 */       return false;
/* 121 */     } catch (IOException e) {
/* 122 */       throw new RuntimeException();
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\ddns\client\DynDnsUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */