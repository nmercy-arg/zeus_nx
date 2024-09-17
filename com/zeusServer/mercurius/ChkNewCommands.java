/*    */ package com.zeusServer.mercurius;
/*    */ 
/*    */ import com.zeus.settings.beans.Util;
/*    */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*    */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.Functions;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import com.zeusServer.util.InfoModule;
/*    */ import com.zeusServer.util.LocaleMessage;
/*    */ import java.util.List;
/*    */ import java.util.Map;
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
/*    */ 
/*    */ 
/*    */ public class ChkNewCommands
/*    */   implements Runnable
/*    */ {
/* 32 */   private final int TIME_BETWEEN_CHECK_NEW_COMMANDS_IN_THE_DATABASE = 5000;
/*    */   public static Long wdt;
/*    */   public boolean flag;
/*    */   
/*    */   public ChkNewCommands() {
/* 37 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/* 38 */     this.flag = true;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/*    */     try {
/* 44 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_NEW_COMMANDS_started"), Enums.EnumMessagePriority.LOW, null, null);
/* 45 */       while (this.flag) {
/*    */         try {
/* 47 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/* 48 */             wdt = Functions.updateWatchdog(wdt, 100L);
/*    */             continue;
/*    */           } 
/* 51 */           List<Integer> moduleList = MercuriusDBManager.executeSP_006();
/* 52 */           for (Integer idModule : moduleList) {
/* 53 */             synchronized (TblMercuriusActiveConnections.getInstance()) {
/* 54 */               for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblMercuriusActiveConnections.getInstance().entrySet()) {
/* 55 */                 if (idModule != null && connection != null && ((InfoModule)connection.getValue()).idModule == idModule.intValue()) {
/* 56 */                   ((InfoModule)connection.getValue()).newCommand = true;
/*    */                   break;
/*    */                 } 
/*    */               } 
/*    */             } 
/* 61 */             wdt = Functions.updateWatchdog(wdt, 0L);
/*    */           } 
/* 63 */         } catch (InterruptedException|java.sql.SQLException|NullPointerException interruptedException) {}
/*    */         
/* 65 */         wdt = Functions.updateWatchdog(wdt, 5000L);
/*    */       } 
/* 67 */     } catch (Exception ex) {
/* 68 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_verification_of_NEW_COMMANDS"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 69 */       GlobalVariables.buzzerActivated = true;
/*    */     } finally {
/* 71 */       dispose();
/*    */     } 
/*    */   }
/*    */   
/*    */   private void dispose() {
/* 76 */     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_NEW_COMMANDS_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\ChkNewCommands.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */