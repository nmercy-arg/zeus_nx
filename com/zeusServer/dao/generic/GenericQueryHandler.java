/*     */ package com.zeusServer.dao.generic;
/*     */ 
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
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
/*     */ public class GenericQueryHandler
/*     */ {
/*     */   public static void compressTable(String schemaName, String tableName, Connection conn) throws SQLException {
/*  30 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  33 */       cst = conn.prepareCall("call SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?,?,1)");
/*  34 */       cst.setString(1, schemaName);
/*  35 */       cst.setString(2, tableName);
/*  36 */       cst.execute();
/*  37 */       cst.close();
/*     */     }
/*  39 */     catch (SQLException ex) {
/*  40 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  41 */         Logger.getLogger(GenericQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/*  43 */       throw ex;
/*     */     } finally {
/*  45 */       if (cst != null) {
/*  46 */         cst.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void setDatabaseProperty(String property, String value, Connection conn) throws SQLException {
/*  53 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  56 */       cst = conn.prepareCall("call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?,?)");
/*  57 */       cst.setString(1, property);
/*  58 */       cst.setString(2, value);
/*  59 */       cst.execute();
/*  60 */       cst.close();
/*     */     }
/*  62 */     catch (SQLException ex) {
/*  63 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  64 */         Logger.getLogger(GenericQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/*  66 */       throw ex;
/*     */     } finally {
/*  68 */       if (cst != null) {
/*  69 */         cst.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getTableNameBySchema(String schemaName, Enums.EnumDbTableIDs dbTableID) {
/*  77 */     switch (schemaName) {
/*     */       case "PEGASUS":
/*  79 */         switch (dbTableID) {
/*     */           case TB_DEVICE_CONNECTION:
/*  81 */             return "DEVICE_CONNECTION";
/*     */           case TB_EVENT:
/*  83 */             return "EVENT";
/*     */           case TB_SIGNAL_LEVEL:
/*  85 */             return "SIGNAL_LEVEL";
/*     */           case TB_OCCURRENCE:
/*  87 */             return "OCCURRENCE";
/*     */           case TB_RECEIVED_COMM:
/*  89 */             return "RECEIVED_COMM";
/*     */           case TB_COMMAND:
/*  91 */             return "COMMAND";
/*     */         } 
/*  93 */         return "";
/*     */ 
/*     */       
/*     */       case "GRIFFON":
/*  97 */         switch (dbTableID) {
/*     */           case TB_DEVICE_CONNECTION:
/*  99 */             return "GRCP_DEVICE_CONNECTION";
/*     */           case TB_EVENT:
/* 101 */             return "GRCP_EVENT";
/*     */           case TB_SIGNAL_LEVEL:
/* 103 */             return "GRCP_SIGNAL_LEVEL";
/*     */           case TB_OCCURRENCE:
/* 105 */             return "GRCP_OCCURRENCE";
/*     */           case TB_RECEIVED_COMM:
/* 107 */             return "GRCP_RECEIVED_COMM";
/*     */           case TB_COMMAND:
/* 109 */             return "GRCP_COMMAND";
/*     */         } 
/* 111 */         return "";
/*     */ 
/*     */       
/*     */       case "MERCURIUS":
/* 115 */         switch (dbTableID) {
/*     */           case TB_DEVICE_CONNECTION:
/* 117 */             return "AVL_DEVICE_CONNECTION";
/*     */           case TB_EVENT:
/* 119 */             return "AVL_EVENT";
/*     */           case TB_SIGNAL_LEVEL:
/* 121 */             return "AVL_SIGNAL_LEVEL";
/*     */           case TB_OCCURRENCE:
/* 123 */             return "AVL_OCCURRENCE";
/*     */           case TB_RECEIVED_COMM:
/* 125 */             return "AVL_RECEIVED_COMM";
/*     */           case TB_COMMAND:
/* 127 */             return "AVL_COMMAND";
/*     */         } 
/* 129 */         return "";
/*     */     } 
/*     */ 
/*     */     
/* 133 */     return "";
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static int getNumberRecordsToBeDeleted(String schemaName, Enums.EnumDbTableIDs dbTableID, Timestamp limitTimestamp) throws SQLException {
/* 139 */     Connection conn = null;
/* 140 */     PreparedStatement ps = null;
/* 141 */     ResultSet rs = null;
/* 142 */     int numberRecordsToBeDeleted = 0;
/*     */     
/*     */     try {
/* 145 */       String sql, tbName = getTableNameBySchema(schemaName, dbTableID);
/* 146 */       if (tbName.isEmpty()) {
/* 147 */         return 0;
/*     */       }
/*     */ 
/*     */       
/* 151 */       switch (dbTableID) {
/*     */         case TB_DEVICE_CONNECTION:
/* 153 */           sql = "(CONNECTION_TIMESTAMP <= ?)";
/*     */           break;
/*     */         case TB_EVENT:
/* 156 */           sql = "((RECEIVED <= ?) AND (TRANSMITTED IS NOT NULL))";
/*     */           break;
/*     */         case TB_SIGNAL_LEVEL:
/* 159 */           sql = "(MEASURE_TIMESTAMP <= ?)";
/*     */           break;
/*     */         case TB_OCCURRENCE:
/* 162 */           sql = "((OCCURRED <= ?) AND (RESTORATION_NOTIFIED IS NOT NULL))";
/*     */           break;
/*     */         case TB_RECEIVED_COMM:
/* 165 */           sql = "(RECEPTION_TIMESTAMP <= ?)";
/*     */           break;
/*     */         case TB_COMMAND:
/* 168 */           sql = "((STORED <= ?) AND ((TRANSMITTED IS NOT NULL) OR (EXEC_CANCELLED=1)))";
/*     */           break;
/*     */         default:
/* 171 */           return 0;
/*     */       } 
/*     */       
/* 174 */       conn = GenericDBManager.getConnectionBySchemaName(schemaName, false);
/* 175 */       ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM " + tbName + " WHERE " + sql);
/* 176 */       ps.setTimestamp(1, limitTimestamp);
/* 177 */       rs = ps.executeQuery();
/* 178 */       if (rs.next()) {
/* 179 */         numberRecordsToBeDeleted = rs.getInt("CNT");
/*     */       }
/*     */     }
/* 182 */     catch (SQLException ex) {
/* 183 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 184 */         Logger.getLogger(GenericQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 186 */       throw ex;
/*     */     } finally {
/* 188 */       if (rs != null) {
/* 189 */         rs.close();
/*     */       }
/* 191 */       if (ps != null) {
/* 192 */         ps.close();
/*     */       }
/* 194 */       if (conn != null) {
/* 195 */         conn.close();
/*     */       }
/*     */     } 
/*     */     
/* 199 */     return numberRecordsToBeDeleted;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void deleteRecords(String schemaName, Enums.EnumDbTableIDs dbTableID, Timestamp limitTimestamp, int numRecordsToBeDeleted) throws SQLException {
/* 204 */     Connection conn = null;
/* 205 */     PreparedStatement ps = null;
/* 206 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 209 */       String sql, tbName = getTableNameBySchema(schemaName, dbTableID);
/* 210 */       if (tbName.isEmpty()) {
/*     */         return;
/*     */       }
/*     */ 
/*     */       
/* 215 */       switch (dbTableID) {
/*     */         case TB_DEVICE_CONNECTION:
/* 217 */           sql = "ID_CONNECTION IN (SELECT ID_CONNECTION FROM " + tbName + " WHERE (CONNECTION_TIMESTAMP <= ?) FETCH FIRST ? ROWS ONLY)";
/*     */           break;
/*     */         case TB_EVENT:
/* 220 */           sql = "ID_EVENT IN (SELECT ID_EVENT FROM " + tbName + " WHERE ((RECEIVED <= ?) AND (TRANSMITTED IS NOT NULL)) FETCH FIRST ? ROWS ONLY)";
/*     */           break;
/*     */         case TB_SIGNAL_LEVEL:
/* 223 */           sql = "ID_SIGNAL_LEVEL IN (SELECT ID_SIGNAL_LEVEL FROM " + tbName + " WHERE (MEASURE_TIMESTAMP <= ?) FETCH FIRST ? ROWS ONLY)";
/*     */           break;
/*     */         case TB_OCCURRENCE:
/* 226 */           sql = "ID_OCCURRENCE IN (SELECT ID_OCCURRENCE FROM " + tbName + " WHERE ((OCCURRED <= ?) AND (RESTORATION_NOTIFIED IS NOT NULL)) FETCH FIRST ? ROWS ONLY)";
/*     */           break;
/*     */         case TB_RECEIVED_COMM:
/* 229 */           sql = "ID_RECEIVED_COMM IN (SELECT ID_RECEIVED_COMM FROM " + tbName + " WHERE (RECEPTION_TIMESTAMP <= ?) FETCH FIRST ? ROWS ONLY)";
/*     */           break;
/*     */         case TB_COMMAND:
/* 232 */           sql = "ID_COMMAND IN (SELECT ID_COMMAND FROM " + tbName + " WHERE ((STORED <= ?) AND ((TRANSMITTED IS NOT NULL) OR (EXEC_CANCELLED=1))) FETCH FIRST ? ROWS ONLY)";
/*     */           break;
/*     */         
/*     */         default:
/*     */           return;
/*     */       } 
/* 238 */       conn = GenericDBManager.getConnectionBySchemaName(schemaName, false);
/* 239 */       ps = conn.prepareStatement("DELETE FROM " + tbName + " WHERE " + sql);
/* 240 */       ps.setTimestamp(1, limitTimestamp);
/* 241 */       ps.setInt(2, numRecordsToBeDeleted);
/* 242 */       ps.executeUpdate();
/* 243 */       ps.close();
/*     */     }
/* 245 */     catch (SQLException ex) {
/* 246 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 247 */         Logger.getLogger(GenericQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 249 */       throw ex;
/*     */     } finally {
/* 251 */       if (rs != null) {
/* 252 */         rs.close();
/*     */       }
/* 254 */       if (ps != null) {
/* 255 */         ps.close();
/*     */       }
/* 257 */       if (conn != null)
/* 258 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\generic\GenericQueryHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */