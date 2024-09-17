/*     */ package com.zeusServer.dao.griffon;
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
/*     */ 
/*     */ public class GriffonSPHandler01_10
/*     */ {
/*     */   public static void executeSP_001(int idClient, int occuranceType, Connection con) throws SQLException {
/*  42 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  45 */       cst = con.prepareCall("call SP_001(?,?)");
/*  46 */       cst.setInt(1, idClient);
/*  47 */       cst.setInt(2, occuranceType);
/*  48 */       cst.execute();
/*  49 */       cst.close();
/*     */     }
/*  51 */     catch (SQLException e) {
/*  52 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  53 */         Logger.getLogger(GriffonSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  55 */       throw e;
/*     */     } finally {
/*  57 */       if (cst != null) {
/*  58 */         cst.close();
/*     */       }
/*  60 */       if (con != null) {
/*  61 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> executeSP_006(Connection con) throws SQLException {
/*  68 */     CallableStatement cst = null;
/*  69 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  72 */       List<Integer> data = new ArrayList<>();
/*     */       
/*  74 */       cst = con.prepareCall("call SP_006()");
/*  75 */       rs = cst.executeQuery();
/*  76 */       while (rs.next()) {
/*  77 */         data.add(Integer.valueOf(rs.getInt("ID_MODULE")));
/*     */       }
/*  79 */       rs.close();
/*  80 */       cst.close();
/*     */       
/*  82 */       return data;
/*     */     }
/*  84 */     catch (SQLException e) {
/*  85 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  86 */         Logger.getLogger(GriffonSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  88 */       throw e;
/*     */     } finally {
/*  90 */       if (rs != null) {
/*  91 */         rs.close();
/*     */       }
/*  93 */       if (cst != null) {
/*  94 */         cst.close();
/*     */       }
/*  96 */       if (con != null) {
/*  97 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_007DataHolder> executeSP_007(Connection con) throws SQLException {
/* 104 */     CallableStatement cst = null;
/* 105 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 108 */       List<SP_007DataHolder> list = new ArrayList<>();
/*     */       
/* 110 */       cst = con.prepareCall("call SP_007()");
/* 111 */       rs = cst.executeQuery();
/* 112 */       while (rs.next()) {
/* 113 */         SP_007DataHolder sp7DH = new SP_007DataHolder();
/* 114 */         sp7DH.setId_Client(rs.getInt("ID_CLIENT"));
/* 115 */         if (rs.getTimestamp("LAST_COMMUNICATION") != null) {
/* 116 */           Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/* 117 */           cal.setTimeInMillis(rs.getTimestamp("LAST_COMMUNICATION").getTime());
/* 118 */           sp7DH.setLast_Communication(cal);
/*     */         } 
/* 120 */         sp7DH.setComm_Timeout(rs.getInt("COMM_TIMEOUT"));
/* 121 */         list.add(sp7DH);
/*     */       } 
/* 123 */       rs.close();
/* 124 */       cst.close();
/*     */       
/* 126 */       return list;
/*     */     }
/* 128 */     catch (SQLException e) {
/* 129 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 130 */         Logger.getLogger(GriffonSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 132 */       throw e;
/*     */     } finally {
/* 134 */       if (rs != null) {
/* 135 */         rs.close();
/*     */       }
/* 137 */       if (cst != null) {
/* 138 */         cst.close();
/*     */       }
/* 140 */       if (con != null) {
/* 141 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_009(int idClient, short occurrenceType, Connection con) throws SQLException {
/* 148 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 151 */       cst = con.prepareCall("call SP_009(?,?)");
/* 152 */       cst.setInt(1, idClient);
/* 153 */       cst.setInt(2, occurrenceType);
/* 154 */       cst.execute();
/* 155 */       cst.close();
/*     */     }
/* 157 */     catch (SQLException e) {
/* 158 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 159 */         Logger.getLogger(GriffonSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 161 */       throw e;
/*     */     } finally {
/* 163 */       if (cst != null) {
/* 164 */         cst.close();
/*     */       }
/* 166 */       if (con != null)
/* 167 */         con.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\griffon\GriffonSPHandler01_10.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */