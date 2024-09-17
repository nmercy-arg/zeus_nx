/*     */ package com.zeusServer.DBGeneral;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.dao.generic.GenericQueryHandler;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DatabaseMetaData;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
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
/*     */ public class DBCleaner
/*     */   implements Runnable
/*     */ {
/*  32 */   private final int MAX_RECORDS_TO_DELETE = 100;
/*  33 */   private final int TIME_BETWEEN_DELETE_OPERATIONS = 100;
/*     */   
/*     */   private static final int TIME_BETWEEN_DB_RECLAIMS = 2500;
/*     */   
/*     */   private final Timestamp limitTimestamp;
/*     */   
/*     */   public DBCleaner(Timestamp limitTimestamp) {
/*  40 */     this.limitTimestamp = limitTimestamp;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  47 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Starting_database_cleanup"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  48 */       GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.CLEANUP;
/*  49 */       for (String schemaName : Util.getAvailbleProductSchemas()) {
/*  50 */         for (Enums.EnumDbTableIDs dbTableID : Enums.EnumDbTableIDs.values()) {
/*  51 */           String tbName = GenericQueryHandler.getTableNameBySchema(schemaName, dbTableID);
/*  52 */           int numTotalRecordsToBeDeleted = GenericDBManager.getNumberRecordsToBeDeleted(schemaName, dbTableID, this.limitTimestamp);
/*  53 */           while (numTotalRecordsToBeDeleted > 0) {
/*  54 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, String.format(LocaleMessage.getLocaleMessage("Cleanup_Task_Deleting_Records_Progress"), new Object[] { tbName, Integer.valueOf(numTotalRecordsToBeDeleted) }), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  55 */             int numRecordsToBeDeleted = (numTotalRecordsToBeDeleted > 100) ? 100 : numTotalRecordsToBeDeleted;
/*  56 */             GenericDBManager.deleteRecords(schemaName, dbTableID, this.limitTimestamp, numRecordsToBeDeleted);
/*  57 */             numTotalRecordsToBeDeleted -= numRecordsToBeDeleted;
/*  58 */             Thread.sleep(100L);
/*     */           } 
/*     */         } 
/*     */       } 
/*  62 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Database_cleanup_completed_successfully"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  63 */       GlobalVariables.lastDbCleanup = System.currentTimeMillis();
/*  64 */       GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/*  65 */       reclaimUnusedDbSpace();
/*     */     }
/*  67 */     catch (InterruptedException|SQLException ex) {
/*  68 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_cleaning_database"), Enums.EnumMessagePriority.HIGH, null, ex);
/*     */     } finally {
/*  70 */       GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void reclaimUnusedDbSpace() throws InterruptedException, SQLException {
/*  76 */     Connection conn = null;
/*  77 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  80 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Reclaiming_database_unused_space"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  81 */       for (String schemaName : Util.getAvailbleSchemas()) {
/*  82 */         ArrayList<String> tableNames = new ArrayList<>();
/*  83 */         conn = GenericDBManager.getConnectionBySchemaName("SYS", true);
/*  84 */         DatabaseMetaData dmd = conn.getMetaData();
/*  85 */         rs = dmd.getTables(null, schemaName, null, new String[] { "TABLE" });
/*  86 */         while (rs.next()) {
/*  87 */           tableNames.add(rs.getString("TABLE_NAME"));
/*     */         }
/*  89 */         rs.close();
/*  90 */         for (String tbName : tableNames) {
/*  91 */           GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.SPACE_RECLAIM;
/*  92 */           GenericDBManager.compressTable(schemaName, tbName);
/*  93 */           GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/*  94 */           Thread.sleep(2500L);
/*     */         } 
/*     */       } 
/*  97 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Database_unused_space_reclaimed_successfully"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */     }
/*  99 */     catch (InterruptedException|SQLException ex) {
/* 100 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_reclaiming_database_unused_space"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 101 */       throw ex;
/*     */     } finally {
/* 103 */       GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/* 104 */       if (rs != null) {
/* 105 */         rs.close();
/*     */       }
/* 107 */       if (conn != null) {
/* 108 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void stopCleaner() {
/* 115 */     Thread.currentThread().interrupt();
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBGeneral\DBCleaner.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */