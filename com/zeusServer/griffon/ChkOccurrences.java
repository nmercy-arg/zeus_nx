/*     */ package com.zeusServer.griffon;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GriffonDBManager;
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeuscc.griffon.derby.beans.GriffonEnums;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Calendar;
/*     */ import java.util.List;
/*     */ import java.util.TimeZone;
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
/*     */ public class ChkOccurrences
/*     */   implements Runnable
/*     */ {
/*  33 */   private final long TIME_BETWEEN_EXECUTIONS_THREAD_CHECK_OCCURRENCES = 15000L;
/*     */ 
/*     */   
/*     */   public static Long wdt;
/*     */   
/*     */   public boolean flag;
/*     */   
/*     */   Calendar c1;
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  45 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_occurrences_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  46 */       while (this.flag) {
/*  47 */         if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  48 */           wdt = Functions.updateWatchdog(wdt, 100L);
/*     */           continue;
/*     */         } 
/*     */         try {
/*  52 */           chkModuleOccurrences();
/*  53 */           wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  54 */         } catch (InterruptedException|SQLException ex) {
/*  55 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_verification_of_the_occurrences"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  56 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/*  58 */         Thread.sleep(15000L);
/*     */       } 
/*  60 */     } catch (InterruptedException interruptedException) {
/*     */     
/*     */     } finally {
/*  63 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void dispose() {
/*  68 */     this.flag = false;
/*  69 */     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_occurrences_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private void chkModuleOccurrences() throws SQLException, InterruptedException {
/*  73 */     List<SP_007DataHolder> sp7DHList = GriffonDBManager.executeSP_007();
/*  74 */     if (sp7DHList != null) {
/*  75 */       for (SP_007DataHolder sp7DH : sp7DHList) {
/*  76 */         if (sp7DH.getLast_Communication() != null) {
/*  77 */           Calendar cal = Functions.addTime2Calendar(sp7DH.getLast_Communication(), 13, sp7DH.getComm_Timeout());
/*  78 */           if (cal.getTimeInMillis() < getCurrentTimeInMillisInGMT()) {
/*  79 */             if (System.currentTimeMillis() - GlobalVariables.applicationStartupTime.getTimeInMillis() > (sp7DH.getComm_Timeout() * 1000)) {
/*  80 */               GriffonDBManager.executeSP_001(sp7DH.getId_Client(), GriffonEnums.EnumGriffonOccurrences.MODULE_OFFLINE.getOccurrence());
/*     */             }
/*     */           } else {
/*  83 */             GriffonDBManager.executeSP_009(sp7DH.getId_Client(), (short)GriffonEnums.EnumGriffonOccurrences.MODULE_OFFLINE.getOccurrence());
/*     */           } 
/*     */         } 
/*  86 */         wdt = Functions.updateWatchdog(wdt, 0L);
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   private long getCurrentTimeInMillisInGMT() {
/*  92 */     Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/*  93 */     this.c1.set(1, c.get(1));
/*  94 */     this.c1.set(2, c.get(2));
/*  95 */     this.c1.set(5, c.get(5));
/*  96 */     this.c1.set(11, c.get(11));
/*  97 */     this.c1.set(12, c.get(12));
/*  98 */     this.c1.set(13, c.get(13));
/*  99 */     return this.c1.getTimeInMillis();
/*     */   } public ChkOccurrences() {
/* 101 */     this.c1 = Calendar.getInstance();
/*     */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*     */     this.flag = true;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\ChkOccurrences.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */