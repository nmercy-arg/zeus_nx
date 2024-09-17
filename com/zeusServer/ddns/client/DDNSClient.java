/*     */ package com.zeusServer.ddns.client;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.util.ChkInternetLink;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.URL;
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
/*     */ 
/*     */ public class DDNSClient
/*     */   implements Runnable
/*     */ {
/*     */   public static Long wdt;
/*     */   private final String setting;
/*     */   public boolean flag;
/*  32 */   private long TIME_BETWEEN_DDNS_UPDATES = 600000L;
/*     */   private String ddnsClientName;
/*  34 */   private String currentIP = "0.0.0.0";
/*     */   
/*     */   public DDNSClient(String setting) {
/*  37 */     this.setting = setting;
/*  38 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  39 */     this.flag = true;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  45 */       long lastThreadExecution = 0L;
/*  46 */       String[] settings = this.setting.split(";");
/*  47 */       this.TIME_BETWEEN_DDNS_UPDATES = (Integer.parseInt(settings[settings.length - 1]) * 60 * 1000);
/*  48 */       int ddnsSPType = Integer.parseInt(settings[0]);
/*  49 */       DDNSUpdater updater = Functions.getDDNSServiceProviderClientByID(ddnsSPType, settings);
/*  50 */       this.ddnsClientName = updater.getDDNSProviderName();
/*  51 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("DDNS_client_task_started_for_provider") + " [ " + this.ddnsClientName + " ]", Enums.EnumMessagePriority.LOW, null, null);
/*     */ 
/*     */       
/*  54 */       wdt = Functions.updateWatchdog(wdt, 60000L);
/*     */       
/*  56 */       while (this.flag) {
/*  57 */         if (lastThreadExecution < System.currentTimeMillis()) {
/*  58 */           if (ChkInternetLink.online) {
/*     */             try {
/*  60 */               String newIP = null;
/*  61 */               int retries = 0;
/*  62 */               while (retries++ < 3) {
/*  63 */                 URL ipURL = new URL("http://checkip.amazonaws.com");
/*  64 */                 BufferedReader br = null;
/*     */                 
/*     */                 try {
/*  67 */                   br = new BufferedReader(new InputStreamReader(ipURL.openStream()));
/*  68 */                   newIP = br.readLine();
/*     */                   break;
/*  70 */                 } catch (IOException ex) {
/*  71 */                   ex.printStackTrace();
/*     */                 } finally {
/*  73 */                   if (br != null) {
/*     */                     try {
/*  75 */                       br.close();
/*  76 */                     } catch (IOException iOException) {}
/*     */                   }
/*     */                 } 
/*     */ 
/*     */                 
/*  81 */                 wdt = Functions.updateWatchdog(wdt, 1000L);
/*     */               } 
/*     */               
/*  84 */               if (newIP != null && !this.currentIP.equalsIgnoreCase(newIP)) {
/*  85 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Public_IP_change_detected_trying_to_update_it_on_the_DDNS_provider") + " [ " + this.ddnsClientName + " ] old IP [" + this.currentIP + "] new IP [" + newIP + "]", Enums.EnumMessagePriority.LOW, null, null);
/*  86 */                 if (updater.update(newIP)) {
/*  87 */                   this.currentIP = newIP;
/*  88 */                   lastThreadExecution = System.currentTimeMillis() + this.TIME_BETWEEN_DDNS_UPDATES;
/*     */                 } 
/*     */               } 
/*  91 */             } catch (Exception ex) {
/*  92 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_DDNS_client_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  93 */               GlobalVariables.buzzerActivated = true;
/*     */             } 
/*     */           } else {
/*     */             
/*  97 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Internet_not_available_to_update_IP_on_the_DDNS_provider"), Enums.EnumMessagePriority.HIGH, null, null);
/*  98 */             GlobalVariables.buzzerActivated = true;
/*     */           } 
/*     */         }
/* 101 */         wdt = Functions.updateWatchdog(wdt, 60000L);
/*     */       } 
/* 103 */     } catch (NumberFormatException ex) {
/* 104 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_DDNS_client_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 105 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/* 107 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void dispose() {
/* 112 */     this.flag = false;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\ddns\client\DDNSClient.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */