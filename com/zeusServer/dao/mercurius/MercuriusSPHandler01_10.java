/*     */ package com.zeusServer.dao.mercurius;
/*     */ 
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
/*     */ import java.util.List;
/*     */ import java.util.TimeZone;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class MercuriusSPHandler01_10
/*     */ {
/*     */   public static void executeSP_001(int idClient, int occuranceType, Connection conn) throws SQLException {
/*  41 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  44 */       cst = conn.prepareCall("call SP_001(?,?)");
/*  45 */       cst.setInt(1, idClient);
/*  46 */       cst.setInt(2, occuranceType);
/*  47 */       cst.execute();
/*  48 */       cst.close();
/*     */     }
/*  50 */     catch (SQLException e) {
/*  51 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  52 */         Logger.getLogger(MercuriusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  54 */       throw e;
/*     */     } finally {
/*  56 */       if (cst != null) {
/*  57 */         cst.close();
/*     */       }
/*  59 */       if (conn != null) {
/*  60 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> executeSP_006(Connection con) throws SQLException {
/*  67 */     CallableStatement cst = null;
/*  68 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  71 */       List<Integer> data = new ArrayList<>();
/*     */       
/*  73 */       cst = con.prepareCall("call SP_006()");
/*  74 */       rs = cst.executeQuery();
/*  75 */       while (rs.next()) {
/*  76 */         data.add(Integer.valueOf(rs.getInt("ID_MODULE")));
/*     */       }
/*  78 */       rs.close();
/*  79 */       cst.close();
/*     */       
/*  81 */       return data;
/*     */     }
/*  83 */     catch (SQLException e) {
/*  84 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  85 */         Logger.getLogger(MercuriusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  87 */       throw e;
/*     */     } finally {
/*  89 */       if (cst != null) {
/*  90 */         cst.close();
/*     */       }
/*  92 */       if (rs != null) {
/*  93 */         rs.close();
/*     */       }
/*  95 */       if (con != null) {
/*  96 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_007DataHolder> executeSP_007(Connection conn) throws SQLException {
/* 103 */     CallableStatement cst = null;
/* 104 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 107 */       List<SP_007DataHolder> list = new ArrayList<>();
/*     */       
/* 109 */       cst = conn.prepareCall("call SP_007()");
/* 110 */       rs = cst.executeQuery();
/* 111 */       while (rs.next()) {
/* 112 */         SP_007DataHolder sp7DH = new SP_007DataHolder();
/* 113 */         sp7DH.setId_Client(rs.getInt("ID_CLIENT"));
/* 114 */         if (rs.getTimestamp("LAST_COMMUNICATION") != null) {
/* 115 */           Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/* 116 */           cal.setTimeInMillis(rs.getTimestamp("LAST_COMMUNICATION").getTime());
/* 117 */           sp7DH.setLast_Communication(cal);
/*     */         } 
/* 119 */         sp7DH.setComm_Timeout(rs.getInt("COMM_TIMEOUT"));
/* 120 */         list.add(sp7DH);
/*     */       } 
/* 122 */       rs.close();
/* 123 */       cst.close();
/*     */       
/* 125 */       return list;
/*     */     }
/* 127 */     catch (SQLException e) {
/* 128 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 129 */         Logger.getLogger(MercuriusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 131 */       throw e;
/*     */     } finally {
/* 133 */       if (cst != null) {
/* 134 */         cst.close();
/*     */       }
/* 136 */       if (rs != null) {
/* 137 */         rs.close();
/*     */       }
/* 139 */       if (conn != null) {
/* 140 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_009(int idClient, short occurrenceType, Connection con) throws SQLException {
/* 147 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 150 */       cst = con.prepareCall("call SP_009(?,?)");
/* 151 */       cst.setInt(1, idClient);
/* 152 */       cst.setInt(2, occurrenceType);
/* 153 */       cst.execute();
/* 154 */       cst.close();
/*     */     }
/* 156 */     catch (SQLException e) {
/* 157 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 158 */         Logger.getLogger(MercuriusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 160 */       throw e;
/*     */     } finally {
/* 162 */       if (cst != null) {
/* 163 */         cst.close();
/*     */       }
/* 165 */       if (con != null)
/* 166 */         con.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\mercurius\MercuriusSPHandler01_10.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */