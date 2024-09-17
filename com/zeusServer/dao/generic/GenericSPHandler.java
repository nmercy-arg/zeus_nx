/*     */ package com.zeusServer.dao.generic;
/*     */ 
/*     */ import com.zeusServer.dto.SP_013DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_029DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_062DataHolder;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
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
/*     */ public class GenericSPHandler
/*     */ {
/*     */   public static void executeSP_008(int idEvent, Connection conn) throws SQLException {
/*  29 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  32 */       cst = conn.prepareCall("call SP_008(?)");
/*  33 */       cst.setInt(1, idEvent);
/*  34 */       cst.execute();
/*  35 */       cst.close();
/*     */     }
/*  37 */     catch (SQLException e) {
/*  38 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  39 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  41 */       throw e;
/*     */     } finally {
/*  43 */       if (cst != null) {
/*  44 */         cst.close();
/*     */       }
/*  46 */       if (conn != null) {
/*  47 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_005(int idEvent, short transmissionRetries, Connection conn) throws SQLException {
/*  54 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  57 */       cst = conn.prepareCall("call SP_005(?,?)");
/*  58 */       cst.setInt(1, idEvent);
/*  59 */       cst.setShort(2, transmissionRetries);
/*  60 */       cst.execute();
/*  61 */       cst.close();
/*     */     }
/*  63 */     catch (SQLException e) {
/*  64 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  65 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  67 */       throw e;
/*     */     } finally {
/*  69 */       if (cst != null) {
/*  70 */         cst.close();
/*     */       }
/*  72 */       if (conn != null) {
/*  73 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_013DataHolder executeSP_013(Connection conn) throws SQLException {
/*  80 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  83 */       cst = conn.prepareCall("call SP_013(?,?,?,?)");
/*  84 */       cst.registerOutParameter(1, 4);
/*  85 */       cst.registerOutParameter(2, 4);
/*  86 */       cst.registerOutParameter(3, 4);
/*  87 */       cst.registerOutParameter(4, 4);
/*  88 */       cst.execute();
/*  89 */       SP_013DataHolder sp13DH = new SP_013DataHolder();
/*  90 */       sp13DH.setNum_Pending_Events(cst.getInt(1));
/*  91 */       sp13DH.setNum_Pending_Alives(cst.getInt(2));
/*  92 */       sp13DH.setNum_Registered_Modules(cst.getInt(3));
/*  93 */       sp13DH.setDisableModulesCount(cst.getInt(4));
/*  94 */       return sp13DH;
/*     */     }
/*  96 */     catch (SQLException e) {
/*  97 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  98 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 100 */       throw e;
/*     */     } finally {
/* 102 */       if (cst != null) {
/* 103 */         cst.close();
/*     */       }
/* 105 */       if (conn != null) {
/* 106 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_019(int idClient, Connection conn) throws SQLException {
/* 113 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 116 */       cst = conn.prepareCall("call SP_019(?)");
/* 117 */       cst.setInt(1, idClient);
/* 118 */       cst.execute();
/* 119 */       cst.close();
/*     */     }
/* 121 */     catch (SQLException e) {
/* 122 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 123 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 125 */       throw e;
/*     */     } finally {
/* 127 */       if (cst != null) {
/* 128 */         cst.close();
/*     */       }
/* 130 */       if (conn != null) {
/* 131 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_018(int idEvent, short transmissionRetries, Connection conn) throws SQLException {
/* 138 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 141 */       cst = conn.prepareCall("call SP_018(?,?)");
/* 142 */       cst.setInt(1, idEvent);
/* 143 */       cst.setShort(2, transmissionRetries);
/* 144 */       cst.execute();
/* 145 */       cst.close();
/*     */     }
/* 147 */     catch (SQLException e) {
/* 148 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 149 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 151 */       throw e;
/*     */     } finally {
/* 153 */       if (cst != null) {
/* 154 */         cst.close();
/*     */       }
/* 156 */       if (conn != null) {
/* 157 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_016(int idEvent, Connection conn) throws SQLException {
/* 164 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 167 */       cst = conn.prepareCall("call SP_016(?)");
/* 168 */       cst.setInt(1, idEvent);
/* 169 */       cst.execute();
/* 170 */       cst.close();
/*     */     }
/* 172 */     catch (SQLException e) {
/* 173 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 174 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 176 */       throw e;
/*     */     } finally {
/* 178 */       if (cst != null) {
/* 179 */         cst.close();
/*     */       }
/* 181 */       if (conn != null) {
/* 182 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_029DataHolder executeSP_029(String phonePegasus, Connection conn) throws SQLException {
/* 189 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 192 */       cst = conn.prepareCall("call SP_029(?,?)");
/* 193 */       cst.setString(1, phonePegasus);
/* 194 */       cst.registerOutParameter(2, 2000);
/* 195 */       cst.execute();
/* 196 */       return (SP_029DataHolder)cst.getObject(2);
/*     */     }
/* 198 */     catch (SQLException e) {
/* 199 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 200 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 202 */       throw e;
/*     */     } finally {
/* 204 */       if (cst != null) {
/* 205 */         cst.close();
/*     */       }
/* 207 */       if (conn != null) {
/* 208 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_062DataHolder executeSP_062(String phonePegasus, Connection conn) throws SQLException {
/* 215 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 218 */       cst = conn.prepareCall("call SP_062(?,?)");
/* 219 */       cst.setString(1, phonePegasus);
/* 220 */       cst.registerOutParameter(2, 2000);
/* 221 */       cst.execute();
/* 222 */       return (SP_062DataHolder)cst.getObject(2);
/*     */     }
/* 224 */     catch (SQLException e) {
/* 225 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 226 */         Logger.getLogger(GenericSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 228 */       throw e;
/*     */     } finally {
/* 230 */       if (cst != null) {
/* 231 */         cst.close();
/*     */       }
/* 233 */       if (conn != null)
/* 234 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\generic\GenericSPHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */