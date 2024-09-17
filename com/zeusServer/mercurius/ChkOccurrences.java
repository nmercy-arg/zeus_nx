/*     */ package com.zeusServer.mercurius;
/*     */ 
/*     */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLEnums;
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
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
/*     */ public class ChkOccurrences
/*     */   implements Runnable
/*     */ {
/*  32 */   private final long TIME_BETWEEN_EXECUTIONS_THREAD_CHECK_OCCURRENCES = 30000L;
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
/*  44 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_occurrences_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  45 */       while (this.flag) {
/*  46 */         if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  47 */           wdt = Functions.updateWatchdog(wdt, 100L);
/*     */           continue;
/*     */         } 
/*     */         try {
/*  51 */           chkModuleOccurrences();
/*  52 */           wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  53 */         } catch (InterruptedException|SQLException ex) {
/*  54 */           Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_verification_of_the_occurrences"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  55 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/*  57 */         Thread.sleep(30000L);
/*     */       } 
/*  59 */     } catch (InterruptedException interruptedException) {
/*     */     
/*     */     } finally {
/*  62 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void dispose() {
/*  67 */     this.flag = false;
/*  68 */     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_occurrences_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private void chkModuleOccurrences() throws SQLException, InterruptedException {
/*  72 */     List<SP_007DataHolder> sp7DHList = MercuriusDBManager.executeSP_007();
/*  73 */     if (sp7DHList != null) {
/*  74 */       for (SP_007DataHolder sp7DH : sp7DHList) {
/*  75 */         if (sp7DH.getLast_Communication() != null) {
/*  76 */           Calendar cal = Functions.addTime2Calendar(sp7DH.getLast_Communication(), 13, sp7DH.getComm_Timeout());
/*  77 */           if (cal.getTimeInMillis() < getCurrentTimeInMillisInGMT()) {
/*  78 */             if (System.currentTimeMillis() - GlobalVariables.applicationStartupTime.getTimeInMillis() > (sp7DH.getComm_Timeout() * 1000)) {
/*  79 */               MercuriusDBManager.executeSP_001(sp7DH.getId_Client(), MercuriusAVLEnums.EnumAVLOccurrences.E_MODULE_OFFLINE.getOccurrence());
/*     */             }
/*     */           } else {
/*  82 */             MercuriusDBManager.executeSP_009(sp7DH.getId_Client(), (short)MercuriusAVLEnums.EnumAVLOccurrences.E_MODULE_OFFLINE.getOccurrence());
/*     */           } 
/*     */         } 
/*  85 */         wdt = Functions.updateWatchdog(wdt, 0L);
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   private long getCurrentTimeInMillisInGMT() {
/*  91 */     Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/*  92 */     this.c1.set(1, c.get(1));
/*  93 */     this.c1.set(2, c.get(2));
/*  94 */     this.c1.set(5, c.get(5));
/*  95 */     this.c1.set(11, c.get(11));
/*  96 */     this.c1.set(12, c.get(12));
/*  97 */     this.c1.set(13, c.get(13));
/*  98 */     return this.c1.getTimeInMillis();
/*     */   } public ChkOccurrences() {
/* 100 */     this.c1 = Calendar.getInstance();
/*     */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*     */     this.flag = true;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\ChkOccurrences.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */