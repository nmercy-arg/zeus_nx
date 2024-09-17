/*     */ package com.zeusServer.dao.mercurius;
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
/*     */ public class MercuriusSPHandler11_20
/*     */ {
/*     */   public static List<SP_011DataHolder> executeSP_011(Connection conn) throws NumberFormatException, SQLException {
/*  35 */     CallableStatement cst = null;
/*  36 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  39 */       List<SP_011DataHolder> list = new ArrayList<>();
/*     */       
/*  41 */       cst = conn.prepareCall("call SP_011()");
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
/*  52 */         sp11DH.setNwProtocol(rs.getString("LAST_NW_PROTOCOL_RCVD"));
/*     */         
/*  54 */         Calendar cal1 = Calendar.getInstance();
/*  55 */         if (rs.getTimestamp("OCCURRED") != null) {
/*  56 */           cal1.setTimeInMillis(rs.getTimestamp("OCCURRED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  57 */           sp11DH.setOccurred(cal1);
/*     */         } 
/*  59 */         Calendar cal2 = Calendar.getInstance();
/*  60 */         if (rs.getTimestamp("NOTIFIED") != null) {
/*  61 */           cal2.setTimeInMillis(rs.getTimestamp("NOTIFIED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  62 */           sp11DH.setNotified(cal2);
/*     */         } 
/*  64 */         Calendar cal3 = Calendar.getInstance();
/*  65 */         if (rs.getTimestamp("ACKNOWLEDGED") != null) {
/*  66 */           cal3.setTimeInMillis(rs.getTimestamp("ACKNOWLEDGED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  67 */           sp11DH.setAcknowledged(cal3);
/*     */         } 
/*  69 */         Calendar cal4 = Calendar.getInstance();
/*  70 */         if (rs.getTimestamp("TERMINATED") != null) {
/*  71 */           cal4.setTimeInMillis(rs.getTimestamp("TERMINATED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  72 */           sp11DH.setTerminated(cal4);
/*     */         } 
/*     */         
/*  75 */         String eventData = rs.getString("EVENT_DATA");
/*  76 */         if (eventData != null && !eventData.isEmpty()) {
/*  77 */           String[] eData = eventData.split(";");
/*  78 */           if (eData != null && eData.length == 2) {
/*  79 */             sp11DH.setEvent_Code(eData[0]);
/*  80 */             sp11DH.setEvent_Freq(Integer.parseInt(eData[1]));
/*     */           } 
/*  82 */           list.add(sp11DH);
/*     */         } 
/*     */       } 
/*  85 */       rs.close();
/*  86 */       cst.close();
/*     */       
/*  88 */       return list;
/*     */     }
/*  90 */     catch (NumberFormatException|SQLException e) {
/*  91 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  92 */         Logger.getLogger(MercuriusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  94 */       throw e;
/*     */     } finally {
/*  96 */       if (cst != null) {
/*  97 */         cst.close();
/*     */       }
/*  99 */       if (rs != null) {
/* 100 */         rs.close();
/*     */       }
/* 102 */       if (conn != null) {
/* 103 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_014(int idModule, int rcvrGroup, short protocol, byte[] eventData, Connection conn) throws SQLException {
/* 110 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 113 */       cst = conn.prepareCall("call SP_014(?,?,?,?)");
/* 114 */       cst.setInt(1, idModule);
/* 115 */       cst.setInt(2, rcvrGroup);
/* 116 */       cst.setShort(3, protocol);
/* 117 */       cst.setBytes(4, eventData);
/* 118 */       cst.execute();
/* 119 */       cst.close();
/*     */     }
/* 121 */     catch (SQLException e) {
/* 122 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 123 */         Logger.getLogger(MercuriusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
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
/*     */   public static void executeSP_017(int idOccurance, Connection conn) throws SQLException {
/* 138 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 141 */       cst = conn.prepareCall("call SP_017(?,?)");
/* 142 */       cst.setInt(1, idOccurance);
/* 143 */       cst.setInt(2, ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0);
/* 144 */       cst.execute();
/* 145 */       cst.close();
/*     */     }
/* 147 */     catch (SQLException e) {
/* 148 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 149 */         Logger.getLogger(MercuriusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
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
/*     */   public static void executeSP_020(int idOccurance, Connection conn) throws SQLException {
/* 164 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 167 */       cst = conn.prepareCall("call SP_020(?)");
/* 168 */       cst.setInt(1, idOccurance);
/* 169 */       cst.execute();
/* 170 */       cst.close();
/*     */     }
/* 172 */     catch (SQLException e) {
/* 173 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 174 */         Logger.getLogger(MercuriusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 176 */       throw e;
/*     */     } finally {
/* 178 */       if (cst != null) {
/* 179 */         cst.close();
/*     */       }
/* 181 */       if (conn != null)
/* 182 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\mercurius\MercuriusSPHandler11_20.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */