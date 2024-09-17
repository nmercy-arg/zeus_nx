/*     */ package com.zeusServer.dao.ZeusSettings;
/*     */ 
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LoggedInUser;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.ResultSet;
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
/*     */ 
/*     */ public class ZeusSettingsSPHandler
/*     */ {
/*     */   public static LoggedInUser executeSP_S004(String userName, String password, Connection conn) throws SQLException {
/*  29 */     CallableStatement cst = null;
/*  30 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  33 */       LoggedInUser user = null;
/*     */       
/*  35 */       cst = conn.prepareCall("{call SP_S004(?,?)}");
/*  36 */       cst.setString(1, userName);
/*  37 */       cst.setString(2, password);
/*  38 */       rs = cst.executeQuery();
/*  39 */       if (rs.next()) {
/*  40 */         user = new LoggedInUser();
/*  41 */         user.setUserName(rs.getString("NAME"));
/*  42 */         user.setClientType(rs.getInt("USER_TYPE"));
/*  43 */         user.setAssignedProducts(rs.getString("ASSIGNED_PRODUCTS"));
/*     */       } 
/*  45 */       rs.close();
/*  46 */       cst.close();
/*     */       
/*  48 */       return user;
/*     */     } finally {
/*     */       
/*  51 */       if (cst != null) {
/*  52 */         cst.close();
/*     */       }
/*  54 */       if (rs != null) {
/*  55 */         rs.close();
/*     */       }
/*  57 */       if (conn != null) {
/*  58 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void executeSP_S006(int idModule, int rcvrGroup, String rcvrCOMPort, short protocol, String clientCode, byte[] eventData, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface, int isMonitorEvent, Connection conn) throws SQLException {
/*  64 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  67 */       cst = conn.prepareCall("call SP_S006(?,?,?,?,?,?,?,?,?,?)");
/*  68 */       cst.setInt(1, idModule);
/*  69 */       cst.setInt(2, rcvrGroup);
/*  70 */       cst.setString(3, rcvrCOMPort);
/*  71 */       cst.setShort(4, protocol);
/*  72 */       cst.setString(5, clientCode);
/*  73 */       cst.setBytes(6, eventData);
/*  74 */       cst.setInt(7, lastMProtocolRcvd);
/*  75 */       cst.setString(8, nwProtocol);
/*  76 */       cst.setInt(9, lastCommInterface);
/*  77 */       cst.setInt(10, isMonitorEvent);
/*  78 */       cst.execute();
/*  79 */       cst.close();
/*     */     }
/*  81 */     catch (SQLException e) {
/*  82 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  83 */         Logger.getLogger(ZeusSettingsSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  85 */       throw e;
/*     */     } finally {
/*  87 */       if (cst != null) {
/*  88 */         cst.close();
/*     */       }
/*  90 */       if (conn != null) {
/*  91 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void executeSP_S007(int idEvent, Connection conn) throws SQLException {
/*  97 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 100 */       cst = conn.prepareCall("call SP_S007(?)");
/* 101 */       cst.setInt(1, idEvent);
/* 102 */       cst.execute();
/* 103 */       cst.close();
/*     */     }
/* 105 */     catch (SQLException e) {
/* 106 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 107 */         Logger.getLogger(ZeusSettingsSPHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 109 */       throw e;
/*     */     } finally {
/* 111 */       if (cst != null) {
/* 112 */         cst.close();
/*     */       }
/* 114 */       if (conn != null)
/* 115 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\ZeusSettings\ZeusSettingsSPHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */