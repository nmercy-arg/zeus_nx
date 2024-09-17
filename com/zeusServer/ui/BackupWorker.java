/*    */ package com.zeusServer.ui;
/*    */ 
/*    */ import com.zeusServer.util.LocaleMessage;
/*    */ import java.awt.HeadlessException;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ import javax.swing.JOptionPane;
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
/*    */ public class BackupWorker
/*    */   extends Worker<Integer, String>
/*    */ {
/*    */   protected Integer doInBackground() throws Exception {
/* 27 */     BackupUtil bu = new BackupUtil();
/* 28 */     return Integer.valueOf(bu.backup());
/*    */   }
/*    */ 
/*    */   
/*    */   protected void done() {
/*    */     try {
/* 34 */       if (ProcessingPopup.getActivePopupFrame() != null) {
/* 35 */         ProcessingPopup.getActivePopupFrame().setVisible(false);
/*    */       }
/* 37 */       int res = get().intValue();
/* 38 */       if (res == 6) {
/* 39 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Database_backup_file_created_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/* 40 */       } else if (res == 1) {
/* 41 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("AutoBackup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/* 42 */       } else if (res == 2) {
/* 43 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Restore_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/* 44 */       } else if (res == 3) {
/* 45 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Manual_Backup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/* 46 */       } else if (res == 4) {
/* 47 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Cleanup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*    */       } else {
/* 49 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Error_while_creating_a_backup_file"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*    */       } 
/* 51 */       MonitorZeus.getInstance().setTaskRunningStatus(false);
/* 52 */     } catch (HeadlessException|InterruptedException|java.util.concurrent.ExecutionException ex) {
/* 53 */       Logger.getLogger(BackupWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 54 */       JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Error_while_creating_a_backup_file"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/* 55 */       MonitorZeus.getInstance().setTaskRunningStatus(false);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\BackupWorker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */