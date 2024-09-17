/*     */ package com.zeusServer.griffon;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GriffonDBManager;
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
/*     */   public void run() {
/*     */     try {
/*  46 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_event_generation_and_transmission_of_e-mails_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  47 */       if (isEmailConfigured()) {
/*  48 */         SendEmail.initializeSMTP(30000);
/*     */       }
/*  50 */       while (this.flag) {
/*     */         try {
/*  52 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  53 */             wdt = Functions.updateWatchdog(wdt, 100L);
/*     */             
/*     */             continue;
/*     */           } 
/*  57 */           List<SP_011DataHolder> sp11DHList = GriffonDBManager.executeSP_011();
/*  58 */           if (sp11DHList != null) {
/*  59 */             for (SP_011DataHolder sp11DH : sp11DHList) {
/*  60 */               boolean newEvent = false;
/*  61 */               if (sp11DH.getAcknowledged() == null) {
/*  62 */                 if (sp11DH.getNotified() != null) {
/*  63 */                   Calendar cal = sp11DH.getNotified();
/*  64 */                   cal = Functions.addTime2Calendar(cal, 12, sp11DH.getEvent_Freq());
/*  65 */                   newEvent = (sp11DH.getEvent_Freq() > 0 && cal.getTimeInMillis() < System.currentTimeMillis());
/*     */                 } else {
/*  67 */                   newEvent = true;
/*     */                 } 
/*     */               }
/*  70 */               boolean newRestore = (sp11DH.getTerminated() != null);
/*  71 */               if (newEvent || newRestore) {
/*  72 */                 if (sp11DH.getEvent_Code() != null && sp11DH.getEvent_Code().length() == 8 && sp11DH.getClient_Code() != null && sp11DH.getClient_Code().length() == 4) {
/*     */                   
/*  74 */                   if (newEvent)
/*     */                   {
/*  76 */                     Functions.saveEvent(Util.EnumProductIDs.GRIFFON_V1.getProductId(), sp11DH.getId_Module(), sp11DH.getId_Group(), sp11DH.getId_Client(), null, sp11DH.getClient_Code(), sp11DH.getEvent_Code(), Enums.EnumEventQualifier.NEW_EVENT, sp11DH.getVersionRcvd(), sp11DH.getNwProtocol(), 0, sp11DH.getEvent_Desc(), -1);
/*     */                   }
/*  78 */                   if (newRestore)
/*     */                   {
/*  80 */                     Functions.saveEvent(Util.EnumProductIDs.GRIFFON_V1.getProductId(), sp11DH.getId_Module(), sp11DH.getId_Group(), sp11DH.getId_Client(), null, sp11DH.getClient_Code(), sp11DH.getEvent_Code(), Enums.EnumEventQualifier.NEW_RESTORE, sp11DH.getVersionRcvd(), sp11DH.getNwProtocol(), 0, sp11DH.getEvent_Desc(), -1);
/*     */                   }
/*     */                 } 
/*  83 */                 if (isEmailConfigured() && SendEmail.getMailSessionExists()) {
/*  84 */                   if (newEvent) {
/*  85 */                     fetchReceipts(sp11DH.getId_Client(), sp11DH.getName(), sp11DH.getOccurrence_Type(), sp11DH.getOccurred(), false);
/*     */                   }
/*  87 */                   if (newRestore) {
/*  88 */                     fetchReceipts(sp11DH.getId_Client(), sp11DH.getName(), sp11DH.getOccurrence_Type(), sp11DH.getOccurred(), true);
/*     */                   }
/*     */                 } 
/*  91 */                 if (newEvent) {
/*  92 */                   GriffonDBManager.executeSP_020(sp11DH.getId_Occurrence());
/*     */                 }
/*  94 */                 if (newRestore) {
/*  95 */                   GriffonDBManager.executeSP_017(sp11DH.getId_Occurrence());
/*     */                 }
/*     */               } 
/*  98 */               wdt = Functions.updateWatchdog(wdt, 0L);
/*     */             } 
/*     */           }
/* 101 */         } catch (Exception ex) {
/* 102 */           ex.printStackTrace();
/*     */         } finally {}
/*     */         
/* 105 */         wdt = Functions.updateWatchdog(wdt, 10000L);
/*     */       }
/*     */     
/* 108 */     } catch (Exception ex) {
/* 109 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_event_generation_and_transmission_of_e-mails"), Enums.EnumMessagePriority.LOW, null, ex);
/* 110 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/* 112 */       dispose();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void fetchReceipts(int id_Client, String name, short occurrence_Type, Calendar occured, boolean restorationOccured) {
/*     */     try {
/* 119 */       for (String email : GriffonDBManager.executeSP_031(id_Client)) {
/* 120 */         if (email != null && email.length() > 0) {
/* 121 */           String mailMessage = makeMsgBody(name, occurrence_Type, occured, restorationOccured);
/* 122 */           if (mailMessage != null && mailMessage.length() > 0) {
/* 123 */             SendEmail.sendMail(email, LocaleMessage.getLocaleMessage("Occurrence"), mailMessage, false, 1);
/*     */           }
/*     */         } 
/*     */       } 
/* 127 */     } catch (Exception ex) {
/* 128 */       ex.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean isEmailConfigured() {
/* 133 */     boolean boolRc = false;
/* 134 */     if (ZeusServerCfg.getInstance().getNameSender() != null && ZeusServerCfg.getInstance().getMailAccount() != null && ZeusServerCfg.getInstance().getSmtpServer() != null && ZeusServerCfg.getInstance().getNameSender().length() > 0 && ZeusServerCfg.getInstance().getMailAccount().length() > 0 && ZeusServerCfg.getInstance().getSmtpServer().length() > 0) {
/* 135 */       boolRc = true;
/*     */     }
/* 137 */     if (ZeusServerCfg.getInstance().getSmtpServerRequiresAuth()) {
/* 138 */       boolRc = (boolRc && ZeusServerCfg.getInstance().getPop3User() != null && ZeusServerCfg.getInstance().getPop3User().length() > 0 && ZeusServerCfg.getInstance().getPop3Pass() != null && ZeusServerCfg.getInstance().getPop3Pass().length() > 0);
/*     */     }
/* 140 */     return boolRc;
/*     */   }
/*     */   
/*     */   private void dispose() {
/* 144 */     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_event_generation_and_transmission_of_e-mails_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private String makeMsgBody(String name, short occurrence_Type, Calendar occured, boolean restorationOccured) {
/* 148 */     return "";
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\GenerateEvents.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */