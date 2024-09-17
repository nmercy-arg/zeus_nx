/*     */ package com.zeusServer.mercurius;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.SendEmail;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.util.Calendar;
/*     */ import java.util.List;
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
/*     */ 
/*     */ 
/*     */ public class GenerateEvents
/*     */   implements Runnable
/*     */ {
/*     */   public static Long wdt;
/*  33 */   private final int TIME_BETWEEN_EXECUTIONS_THREAD_EVENTS_GENERATION_AND_EMAILS_DISPATCH = 10000;
/*  34 */   private final int DISPATCH_EMAIL_TIMEOUT = 30000;
/*     */   public boolean flag;
/*     */   
/*     */   public GenerateEvents() {
/*  38 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  39 */     this.flag = true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  47 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_event_generation_and_transmission_of_e-mails_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  48 */       if (isEmailConfigured()) {
/*  49 */         SendEmail.initializeSMTP(30000);
/*     */       }
/*  51 */       while (this.flag) {
/*     */         try {
/*  53 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  54 */             wdt = Functions.updateWatchdog(wdt, 100L);
/*     */             
/*     */             continue;
/*     */           } 
/*  58 */           List<SP_011DataHolder> sp11DHList = MercuriusDBManager.executeSP_011();
/*  59 */           if (sp11DHList != null) {
/*  60 */             for (SP_011DataHolder sp11DH : sp11DHList) {
/*  61 */               boolean newEvent = false;
/*  62 */               if (sp11DH.getAcknowledged() == null) {
/*  63 */                 if (sp11DH.getNotified() != null) {
/*  64 */                   Calendar cal = sp11DH.getNotified();
/*  65 */                   cal = Functions.addTime2Calendar(cal, 12, sp11DH.getEvent_Freq());
/*  66 */                   newEvent = (sp11DH.getEvent_Freq() > 0 && cal.getTimeInMillis() < System.currentTimeMillis());
/*     */                 } else {
/*  68 */                   newEvent = true;
/*     */                 } 
/*     */               }
/*  71 */               boolean newRestore = (sp11DH.getTerminated() != null);
/*  72 */               if (newEvent || newRestore) {
/*  73 */                 if (sp11DH.getEvent_Code() != null && sp11DH.getEvent_Code().length() == 8 && sp11DH.getClient_Code() != null && sp11DH.getClient_Code().length() == 4) {
/*     */                   
/*  75 */                   if (newEvent) {
/*  76 */                     Functions.saveEvent(Util.EnumProductIDs.MERCURIUS.getProductId(), sp11DH.getId_Module(), sp11DH.getId_Group(), sp11DH.getId_Client(), null, sp11DH.getClient_Code(), sp11DH.getEvent_Code(), Enums.EnumEventQualifier.NEW_EVENT, sp11DH.getVersionRcvd(), sp11DH.getNwProtocol(), 0, -1, -1);
/*     */                   }
/*  78 */                   if (newRestore) {
/*  79 */                     Functions.saveEvent(Util.EnumProductIDs.MERCURIUS.getProductId(), sp11DH.getId_Module(), sp11DH.getId_Group(), sp11DH.getId_Client(), null, sp11DH.getClient_Code(), sp11DH.getEvent_Code(), Enums.EnumEventQualifier.NEW_RESTORE, sp11DH.getVersionRcvd(), sp11DH.getNwProtocol(), 0, -1, -1);
/*     */                   }
/*     */                 } 
/*  82 */                 if (isEmailConfigured() && SendEmail.getMailSessionExists()) {
/*  83 */                   if (newEvent) {
/*  84 */                     fetchReceipts(sp11DH.getId_Client(), sp11DH.getName(), sp11DH.getOccurrence_Type(), sp11DH.getOccurred(), false);
/*     */                   }
/*  86 */                   if (newRestore) {
/*  87 */                     fetchReceipts(sp11DH.getId_Client(), sp11DH.getName(), sp11DH.getOccurrence_Type(), sp11DH.getOccurred(), true);
/*     */                   }
/*     */                 } 
/*  90 */                 if (newEvent) {
/*  91 */                   MercuriusDBManager.executeSP_020(sp11DH.getId_Occurrence());
/*     */                 }
/*  93 */                 if (newRestore) {
/*  94 */                   MercuriusDBManager.executeSP_017(sp11DH.getId_Occurrence());
/*     */                 }
/*     */               } 
/*  97 */               wdt = Functions.updateWatchdog(wdt, 0L);
/*     */             } 
/*     */           }
/* 100 */         } catch (Exception ex) {
/* 101 */           ex.printStackTrace();
/*     */         } finally {}
/*     */         
/* 104 */         wdt = Functions.updateWatchdog(wdt, 10000L);
/*     */       }
/*     */     
/* 107 */     } catch (Exception ex) {
/* 108 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_event_generation_and_transmission_of_e-mails"), Enums.EnumMessagePriority.LOW, null, ex);
/* 109 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/* 111 */       dispose();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void fetchReceipts(int id_Client, String name, short occurrence_Type, Calendar occured, boolean restorationOccured) {
/*     */     try {
/* 118 */       for (String email : MercuriusDBManager.executeSP_031(id_Client)) {
/* 119 */         if (email != null && email.length() > 0) {
/* 120 */           String mailMessage = makeMsgBody(name, occurrence_Type, occured, restorationOccured);
/* 121 */           if (mailMessage != null && mailMessage.length() > 0) {
/* 122 */             SendEmail.sendMail(email, LocaleMessage.getLocaleMessage("Occurrence"), mailMessage, false, 1);
/*     */           }
/*     */         } 
/*     */       } 
/* 126 */     } catch (Exception ex) {
/* 127 */       ex.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean isEmailConfigured() {
/* 132 */     boolean boolRc = false;
/* 133 */     if (ZeusServerCfg.getInstance().getNameSender() != null && ZeusServerCfg.getInstance().getMailAccount() != null && ZeusServerCfg.getInstance().getSmtpServer() != null && ZeusServerCfg.getInstance().getNameSender().length() > 0 && ZeusServerCfg.getInstance().getMailAccount().length() > 0 && ZeusServerCfg.getInstance().getSmtpServer().length() > 0) {
/* 134 */       boolRc = true;
/*     */     }
/* 136 */     if (ZeusServerCfg.getInstance().getSmtpServerRequiresAuth()) {
/* 137 */       boolRc = (boolRc && ZeusServerCfg.getInstance().getPop3User() != null && ZeusServerCfg.getInstance().getPop3User().length() > 0 && ZeusServerCfg.getInstance().getPop3Pass() != null && ZeusServerCfg.getInstance().getPop3Pass().length() > 0);
/*     */     }
/* 139 */     return boolRc;
/*     */   }
/*     */   
/*     */   private void dispose() {
/* 143 */     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_event_generation_and_transmission_of_e-mails_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private String makeMsgBody(String name, short occurrence_Type, Calendar occured, boolean restorationOccured) {
/* 147 */     return "";
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\GenerateEvents.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */