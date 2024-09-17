/*    */ package com.zeusServer.griffon;
/*    */ 
/*    */ import com.zeus.settings.beans.Util;
/*    */ import com.zeusServer.DBManagers.GriffonDBManager;
/*    */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
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
/*    */ 
/*    */ public class ChkNewCommands
/*    */   implements Runnable
/*    */ {
/* 33 */   private final int TIME_BETWEEN_CHECK_NEW_COMMANDS_IN_THE_DATABASE = 5000;
/*    */   public static Long wdt;
/*    */   public boolean flag;
/*    */   
/*    */   public ChkNewCommands() {
/* 38 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/* 39 */     this.flag = true;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/*    */     try {
/* 45 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_verification_of_NEW_COMMANDS_started"), Enums.EnumMessagePriority.LOW, null, null);
/* 46 */       while (this.flag) {
/*    */         try {
/* 48 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/* 49 */             wdt = Functions.updateWatchdog(wdt, 100L);
/*    */             continue;
/*    */           } 
/* 52 */           List<Integer> moduleList = GriffonDBManager.executeSP_006();
/* 53 */           for (Integer idModule : moduleList) {
/* 54 */             synchronized (TblGriffonActiveConnections.getInstance()) {
/* 55 */               for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblGriffonActiveConnections.getInstance().entrySet()) {
/* 56 */                 if (idModule != null && connection != null && ((InfoModule)connection.getValue()).idModule == idModule.intValue()) {
/* 57 */                   ((InfoModule)connection.getValue()).newCommand = true;
/*    */                   break;
/*    */                 } 
/*    */               } 
/*    */             } 
/* 62 */             wdt = Functions.updateWatchdog(wdt, 0L);
/*    */           } 
/* 64 */         } catch (InterruptedException|java.sql.SQLException|NullPointerException interruptedException) {}
/*    */         
/* 66 */         wdt = Functions.updateWatchdog(wdt, 5000L);
/*    */       } 
/* 68 */     } catch (Exception ex) {
/* 69 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_verification_of_NEW_COMMANDS"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 70 */       GlobalVariables.buzzerActivated = true;
/*    */     } finally {
/* 72 */       dispose();
/*    */     } 
/*    */   }
/*    */   
/*    */   private void dispose() {
/* 77 */     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_verification_of_NEW_COMMANDS_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\ChkNewCommands.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */