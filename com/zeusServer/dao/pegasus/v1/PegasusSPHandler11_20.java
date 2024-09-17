/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_015DataHolder;
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
/*     */ public class PegasusSPHandler11_20
/*     */ {
/*     */   public static List<SP_011DataHolder> executeSP_011(Connection conn) throws SQLException {
/*  37 */     CallableStatement cst = null;
/*  38 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  41 */       List<SP_011DataHolder> list = new ArrayList<>();
/*     */       
/*  43 */       cst = conn.prepareCall("call SP_011()");
/*  44 */       rs = cst.executeQuery();
/*  45 */       while (rs.next()) {
/*  46 */         SP_011DataHolder sp11DH = new SP_011DataHolder();
/*  47 */         sp11DH.setId_Client(rs.getInt("ID_CLIENT"));
/*  48 */         sp11DH.setName(rs.getString("NAME"));
/*  49 */         sp11DH.setClient_Code(rs.getString("CLIENT_CODE"));
/*  50 */         sp11DH.setId_Module(rs.getInt("ID_MODULE"));
/*  51 */         sp11DH.setId_Group(rs.getInt("ID_GROUP"));
/*  52 */         sp11DH.setId_Occurrence(rs.getInt("ID_OCCURRENCE"));
/*  53 */         sp11DH.setOccurrence_Type(rs.getShort("OCCURRENCE_TYPE"));
/*  54 */         sp11DH.setEvent_Code(rs.getString("EVENT_CODE"));
/*  55 */         sp11DH.setEvent_Freq(rs.getInt("EVENT_FREQ"));
/*  56 */         sp11DH.setVersionRcvd(rs.getInt("VERSION_RECEIVED"));
/*  57 */         sp11DH.setNwProtocol(rs.getString("LAST_NW_PROTOCOL_RCVD"));
/*  58 */         if (rs.getTimestamp("OCCURRED") != null) {
/*  59 */           Calendar cal1 = Calendar.getInstance();
/*  60 */           cal1.setTimeInMillis(rs.getTimestamp("OCCURRED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  61 */           sp11DH.setOccurred(cal1);
/*     */         } 
/*  63 */         if (rs.getTimestamp("NOTIFIED") != null) {
/*  64 */           Calendar cal2 = Calendar.getInstance();
/*  65 */           cal2.setTimeInMillis(rs.getTimestamp("NOTIFIED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  66 */           sp11DH.setNotified(cal2);
/*     */         } 
/*  68 */         if (rs.getTimestamp("ACKNOWLEDGED") != null) {
/*  69 */           Calendar cal3 = Calendar.getInstance();
/*  70 */           cal3.setTimeInMillis(rs.getTimestamp("ACKNOWLEDGED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  71 */           sp11DH.setAcknowledged(cal3);
/*     */         } 
/*  73 */         if (rs.getTimestamp("TERMINATED") != null) {
/*  74 */           Calendar cal4 = Calendar.getInstance();
/*  75 */           cal4.setTimeInMillis(rs.getTimestamp("TERMINATED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))).getTime());
/*  76 */           sp11DH.setTerminated(cal4);
/*     */         } 
/*  78 */         list.add(sp11DH);
/*     */       } 
/*  80 */       rs.close();
/*  81 */       cst.close();
/*     */       
/*  83 */       return list;
/*     */     }
/*  85 */     catch (SQLException e) {
/*  86 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  87 */         Logger.getLogger(PegasusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  89 */       throw e;
/*     */     } finally {
/*  91 */       if (rs != null) {
/*  92 */         rs.close();
/*     */       }
/*  94 */       if (cst != null) {
/*  95 */         cst.close();
/*     */       }
/*  97 */       if (conn != null) {
/*  98 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_014(int idModule, int rcvrGroup, String rcvrCOMPort, short protocol, String clientCode, byte[] eventData, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface, Connection conn) throws SQLException {
/* 105 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 108 */       cst = conn.prepareCall("call SP_014(?,?,?,?,?,?,?,?,?)");
/* 109 */       cst.setInt(1, idModule);
/* 110 */       cst.setInt(2, rcvrGroup);
/* 111 */       cst.setString(3, rcvrCOMPort);
/* 112 */       cst.setShort(4, protocol);
/* 113 */       cst.setString(5, clientCode);
/* 114 */       cst.setBytes(6, eventData);
/* 115 */       cst.setInt(7, lastMProtocolRcvd);
/* 116 */       cst.setString(8, nwProtocol);
/* 117 */       cst.setInt(9, lastCommInterface);
/* 118 */       cst.execute();
/* 119 */       cst.close();
/*     */     }
/* 121 */     catch (SQLException e) {
/* 122 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 123 */         Logger.getLogger(PegasusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
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
/*     */   public static SP_015DataHolder executeSP_015(SP_015DataHolder sp15DH, Connection conn) throws SQLException {
/* 138 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 141 */       cst = conn.prepareCall("call SP_015(?)");
/* 142 */       cst.registerOutParameter(1, 2000);
/* 143 */       cst.setObject(1, sp15DH);
/* 144 */       cst.execute();
/* 145 */       return (SP_015DataHolder)cst.getObject(1);
/*     */     }
/* 147 */     catch (SQLException e) {
/* 148 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 149 */         Logger.getLogger(PegasusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
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
/*     */   public static void executeSP_017(int idOccurance, Connection conn) throws SQLException {
/* 164 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 167 */       cst = conn.prepareCall("call SP_017(?,?)");
/* 168 */       cst.setInt(1, idOccurance);
/* 169 */       cst.setInt(2, ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0);
/* 170 */       cst.execute();
/* 171 */       cst.close();
/*     */     }
/* 173 */     catch (SQLException e) {
/* 174 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 175 */         Logger.getLogger(PegasusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 177 */       throw e;
/*     */     } finally {
/* 179 */       if (cst != null) {
/* 180 */         cst.close();
/*     */       }
/* 182 */       if (conn != null) {
/* 183 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_020(int idOccurance, Connection conn) throws SQLException {
/* 190 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 193 */       cst = conn.prepareCall("call SP_020(?)");
/* 194 */       cst.setInt(1, idOccurance);
/* 195 */       cst.execute();
/* 196 */       cst.close();
/*     */     }
/* 198 */     catch (SQLException e) {
/* 199 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 200 */         Logger.getLogger(PegasusSPHandler11_20.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 202 */       throw e;
/*     */     } finally {
/* 204 */       if (cst != null) {
/* 205 */         cst.close();
/*     */       }
/* 207 */       if (conn != null)
/* 208 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusSPHandler11_20.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */