/*    */ package com.zeusServer.ddns.client;
/*    */ 
/*    */ import com.zeus.settings.beans.Util;
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.Functions;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import com.zeusServer.util.LocaleMessage;
/*    */ import java.io.BufferedReader;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStreamReader;
/*    */ import java.net.Authenticator;
/*    */ import java.net.PasswordAuthentication;
/*    */ import java.net.URL;
/*    */ import java.net.URLConnection;
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
/*    */ public class NoIPUpdater
/*    */   extends DDNSUpdater
/*    */ {
/*    */   public NoIPUpdater(String[] settings) {
/* 31 */     super(settings);
/* 32 */     this.name = "No-IP";
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean update(String newIP) throws Exception {
/* 37 */     URL url2 = new URL("http://dynupdate.no-ip.com/nic/update?hostname=" + this.settings[3] + "&myip=" + newIP);
/* 38 */     Authenticator.setDefault(new Authenticator()
/*    */         {
/*    */           protected PasswordAuthentication getPasswordAuthentication() {
/* 41 */             return new PasswordAuthentication(NoIPUpdater.this.settings[1], NoIPUpdater.this.settings[2].toCharArray());
/*    */           }
/*    */         });
/* 44 */     URLConnection conn = url2.openConnection();
/* 45 */     BufferedReader br2 = null;
/* 46 */     StringBuilder sb = new StringBuilder();
/*    */     try {
/* 48 */       br2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
/* 49 */       sb.append(br2.readLine());
/*    */     } finally {
/* 51 */       if (br2 != null) {
/*    */         try {
/* 53 */           br2.close();
/* 54 */         } catch (IOException iOException) {}
/*    */       }
/*    */     } 
/*    */ 
/*    */     
/* 59 */     String result = sb.toString();
/* 60 */     if (result != null) {
/* 61 */       if (result.contains("good") || result.contains("nochg")) {
/* 62 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("IP_address_updated_successfully_on_the_DDNS_provider") + " [ " + this.name + " ]", Enums.EnumMessagePriority.LOW, null, null);
/* 63 */         return true;
/* 64 */       }  if (result.contains("nohost")) {
/* 65 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("Host_name_doesnt_exist_under_this_account"), Enums.EnumMessagePriority.HIGH, null, null);
/* 66 */         GlobalVariables.buzzerActivated = true;
/* 67 */         return false;
/* 68 */       }  if (result.contains("badauth")) {
/* 69 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("Invalid_username_password_combination"), Enums.EnumMessagePriority.HIGH, null, null);
/* 70 */         GlobalVariables.buzzerActivated = true;
/* 71 */         return false;
/* 72 */       }  if (result.contains("badagent")) {
/* 73 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("Client_disabled"), Enums.EnumMessagePriority.HIGH, null, null);
/* 74 */         GlobalVariables.buzzerActivated = true;
/* 75 */         return false;
/* 76 */       }  if (result.contains("!donator")) {
/* 77 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("An_update_request_was_sent_including_a_feature_that_is_not_available_to_that_particular_user"), Enums.EnumMessagePriority.HIGH, null, null);
/* 78 */         GlobalVariables.buzzerActivated = true;
/* 79 */         return false;
/* 80 */       }  if (result.contains("abuse")) {
/* 81 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("Username_is_blocked_due_to_abuse"), Enums.EnumMessagePriority.HIGH, null, null);
/* 82 */         GlobalVariables.buzzerActivated = true;
/* 83 */         return false;
/* 84 */       }  if (result.contains("911")) {
/* 85 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[ " + this.name + " ]" + LocaleMessage.getLocaleMessage("Fatal_error_occurred"), Enums.EnumMessagePriority.HIGH, null, null);
/* 86 */         GlobalVariables.buzzerActivated = true;
/* 87 */         return false;
/*    */       } 
/*    */     } 
/* 90 */     return false;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected String getDDNSProviderName() {
/* 96 */     return this.name;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\ddns\client\NoIPUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */