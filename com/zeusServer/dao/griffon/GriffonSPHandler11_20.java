/*     */ package com.zeusServer.dao.griffon;
/*     */ 
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.ZeusServerCfg;
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
/*     */ public class GriffonSPHandler11_20
/*     */ {
/*     */   public static List<SP_011DataHolder> executeSP_011(Connection con) throws SQLException {
/*  35 */     CallableStatement cst = null;
/*  36 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  39 */       List<SP_011DataHolder> list = new ArrayList<>();
/*     */       
/*  41 */       cst = con.prepareCall("call SP_011()");
/*  42 */       rs = cst.executeQuery();
/*  43 */       while (rs.next()) {
/*  44 */         SP_011DataHolder sp11DH = new SP_011DataHolder();
/*  45 */         sp11DH.setId_Client(rs.getInt("ID_CLIENT"));
/*  46 */         sp11DH.setName(rs.getString("NAME"));
/*  47 */         sp11DH.setClient_Code(rs.getString("CLIENT_CODE"));
/*  48 */         sp11DH.setId_Module(rs.getInt("ID_MODULE"));
/*  49 */         sp11DH.setId_Group(rs.getInt("ID_GROUP"));
/*  50 */         sp11DH.setId_Occurrence(rs.getInt("ID_OCCURRENCE"));
/*  51 */         sp11DH.setOccurrence_Type(rs.getShort("OCCURRENCE_TYPE"));
/*  52 */         sp11DH.setEvent_Code(rs.getString("EVENT_CODE"));
/*  53 */         sp11DH.setEvent_Freq(rs.getInt("EVENT_FREQ"));
/*  54 */         sp11DH.setNwProtocol(rs.getString("LAST_NW_PROTOCOL_RCVD"));
/*  55 */         sp11DH.setEvent_Desc(rs.getInt("EVENT_DESC"));
/*  56 */         Calendar cal1 = Calendar.getInstance();
/*  57 */         if (rs.getTimestamp("OCCURRED") != null) {
/*  58 */           cal1.setTimeInMillis(rs.getTimestamp("OCCURRED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  59 */           sp11DH.setOccurred(cal1);
/*     */         } 
/*  61 */         Calendar cal2 = Calendar.getInstance();
/*  62 */         if (rs.getTimestamp("NOTIFIED") != null) {
/*  63 */           cal2.setTimeInMillis(rs.getTimestamp("NOTIFIED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  64 */           sp11DH.setNotified(cal2);
/*     */         } 
/*  66 */         Calendar cal3 = Calendar.getInstance();
/*  67 */         if (rs.getTimestamp("ACKNOWLEDGED") != null) {
/*  68 */           cal3.setTimeInMillis(rs.getTimestamp("ACKNOWLEDGED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  69 */           sp11DH.setAcknowledged(cal3);
/*     */         } 
/*  71 */         Calendar cal4 = Calendar.getInstance();
/*  72 */         if (rs.getTimestamp("TERMINATED") != null) {
/*  73 */           cal4.setTimeInMillis(rs.getTimestamp("TERMINATED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  74 */           sp11DH.setTerminated(cal4);
/*     */         } 
/*  76 */         list.add(sp11DH);
/*     */       } 
/*  78 */       rs.close();
/*  79 */       cst.close();
/*     */       
/*  81 */       return list;
/*     */     }
/*  83 */     catch (SQLException e) {
/*  84 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  85 */         Logger.getLogger(GriffonSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  87 */       throw e;
/*     */     } finally {
/*  89 */       if (rs != null) {
/*  90 */         rs.close();
/*     */       }
/*  92 */       if (cst != null) {
/*  93 */         cst.close();
/*     */       }
/*  95 */       if (con != null) {
/*  96 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_017(int idOccurance, Connection con) throws SQLException {
/* 103 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 106 */       cst = con.prepareCall("call SP_017(?,?)");
/* 107 */       cst.setInt(1, idOccurance);
/* 108 */       cst.setInt(2, ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0);
/* 109 */       cst.execute();
/* 110 */       cst.close();
/*     */     }
/* 112 */     catch (SQLException e) {
/* 113 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 114 */         Logger.getLogger(GriffonSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 116 */       throw e;
/*     */     } finally {
/* 118 */       if (cst != null) {
/* 119 */         cst.close();
/*     */       }
/* 121 */       if (con != null) {
/* 122 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_020(int idOccurance, Connection con) throws SQLException {
/* 129 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 132 */       cst = con.prepareCall("call SP_020(?)");
/* 133 */       cst.setInt(1, idOccurance);
/* 134 */       cst.execute();
/* 135 */       cst.close();
/*     */     }
/* 137 */     catch (SQLException e) {
/* 138 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 139 */         Logger.getLogger(GriffonSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 141 */       throw e;
/*     */     } finally {
/* 143 */       if (cst != null) {
/* 144 */         cst.close();
/*     */       }
/* 146 */       if (con != null)
/* 147 */         con.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\griffon\GriffonSPHandler11_20.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */