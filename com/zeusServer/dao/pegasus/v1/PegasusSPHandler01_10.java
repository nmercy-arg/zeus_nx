/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_003DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_003_VO;
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
/*     */ 
/*     */ public class PegasusSPHandler01_10
/*     */ {
/*     */   public static void executeSP_001(int idClient, int occuranceType, Connection conn) throws SQLException {
/*  45 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  48 */       cst = conn.prepareCall("call SP_001(?,?)");
/*  49 */       cst.setInt(1, idClient);
/*  50 */       cst.setInt(2, occuranceType);
/*  51 */       cst.execute();
/*  52 */       cst.close();
/*     */     }
/*  54 */     catch (SQLException e) {
/*  55 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  56 */         Logger.getLogger(PegasusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  58 */       throw e;
/*     */     } finally {
/*  60 */       if (cst != null) {
/*  61 */         cst.close();
/*     */       }
/*  63 */       if (conn != null) {
/*  64 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
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
/*     */   public static SP_003DataHolder executeSP_003(List<SP_003_VO> list, Connection conn) throws SQLException {
/*  79 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  82 */       cst = conn.prepareCall("call SP_003(?,?)");
/*  83 */       cst.registerOutParameter(2, 2000);
/*  84 */       cst.setObject(1, list);
/*  85 */       cst.execute();
/*  86 */       return (SP_003DataHolder)cst.getObject(2);
/*     */     }
/*  88 */     catch (SQLException e) {
/*  89 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  90 */         Logger.getLogger(PegasusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  92 */       throw e;
/*     */     } finally {
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
/*     */   public static void executeSP_004(int idClient, String newClientCode, Connection conn) throws SQLException {
/* 105 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 108 */       cst = conn.prepareCall("call SP_004(?,?)");
/* 109 */       cst.setInt(1, idClient);
/* 110 */       cst.setString(2, newClientCode);
/* 111 */       cst.execute();
/* 112 */       cst.close();
/*     */     }
/* 114 */     catch (SQLException e) {
/* 115 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 116 */         Logger.getLogger(PegasusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 118 */       throw e;
/*     */     } finally {
/* 120 */       if (cst != null) {
/* 121 */         cst.close();
/*     */       }
/* 123 */       if (conn != null) {
/* 124 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> executeSP_006(Connection conn) throws SQLException {
/* 131 */     CallableStatement cst = null;
/* 132 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 135 */       List<Integer> data = new ArrayList<>();
/*     */       
/* 137 */       cst = conn.prepareCall("call SP_006()");
/* 138 */       rs = cst.executeQuery();
/* 139 */       while (rs.next()) {
/* 140 */         data.add(Integer.valueOf(rs.getInt("ID_MODULE")));
/*     */       }
/* 142 */       rs.close();
/* 143 */       cst.close();
/*     */       
/* 145 */       return data;
/*     */     }
/* 147 */     catch (SQLException e) {
/* 148 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 149 */         Logger.getLogger(PegasusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 151 */       throw e;
/*     */     } finally {
/* 153 */       if (rs != null) {
/* 154 */         rs.close();
/*     */       }
/* 156 */       if (cst != null) {
/* 157 */         cst.close();
/*     */       }
/* 159 */       if (conn != null) {
/* 160 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_007DataHolder> executeSP_007(Connection conn) throws SQLException {
/* 167 */     CallableStatement cst = null;
/* 168 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 171 */       List<SP_007DataHolder> list = new ArrayList<>();
/*     */       
/* 173 */       cst = conn.prepareCall("call SP_007()");
/* 174 */       rs = cst.executeQuery();
/* 175 */       while (rs.next()) {
/* 176 */         SP_007DataHolder sp7DH = new SP_007DataHolder();
/* 177 */         sp7DH.setId_Client(rs.getInt("ID_CLIENT"));
/* 178 */         if (rs.getTimestamp("LAST_COMMUNICATION") != null) {
/* 179 */           Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/* 180 */           cal.setTimeInMillis(rs.getTimestamp("LAST_COMMUNICATION").getTime());
/* 181 */           sp7DH.setLast_Communication(cal);
/*     */         } 
/* 183 */         sp7DH.setComm_Timeout(rs.getInt("COMM_TIMEOUT"));
/* 184 */         sp7DH.setPhone_Line_Status(rs.getShort("PHONE_LINE_STATUS"));
/* 185 */         sp7DH.setAlarm_Panel_Return_Status(rs.getShort("ALARM_PANEL_RETURN_STATUS"));
/* 186 */         sp7DH.setDigital_Input_1_Status(rs.getShort("ZONE1_STATUS"));
/* 187 */         sp7DH.setDigital_Input_2_Status(rs.getShort("ZONE2_STATUS"));
/* 188 */         sp7DH.setDigital_Input_3_Status(rs.getShort("ZONE3_STATUS"));
/* 189 */         sp7DH.setDigital_Input_4_Status(rs.getShort("ZONE4_STATUS"));
/* 190 */         sp7DH.setAlarm_Panel_Connection_Status(rs.getShort("ALARM_PANEL_CONNECTION_STATUS"));
/* 191 */         sp7DH.setDual_Monitoring_Status(rs.getShort("DUAL_MONITORING_STATUS"));
/* 192 */         sp7DH.setMin_Signal_Level(rs.getShort("MIN_GSM_SIGNAL_LEVEL"));
/* 193 */         sp7DH.setLast_Signal_Level(rs.getShort("LAST_GSM_SIGNAL_LEVEL"));
/* 194 */         sp7DH.setAlarm_Panel_Comm_Status(rs.getShort("ALARM_PANEL_COMM_STATUS"));
/* 195 */         sp7DH.setLast_Battery_Level(rs.getFloat("CURRENT_BATT_VOLTAGE"));
/* 196 */         sp7DH.setAc_Supply_Status(rs.getShort("MAIN_PWR_SUPPLY_STATUS"));
/* 197 */         list.add(sp7DH);
/*     */       } 
/* 199 */       rs.close();
/* 200 */       cst.close();
/*     */       
/* 202 */       return list;
/*     */     }
/* 204 */     catch (SQLException e) {
/* 205 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 206 */         Logger.getLogger(PegasusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 208 */       throw e;
/*     */     } finally {
/* 210 */       if (rs != null) {
/* 211 */         rs.close();
/*     */       }
/* 213 */       if (cst != null) {
/* 214 */         cst.close();
/*     */       }
/* 216 */       if (conn != null) {
/* 217 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_009(int idClient, short occurrenceType, Connection conn) throws SQLException {
/* 224 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 227 */       cst = conn.prepareCall("call SP_009(?,?)");
/* 228 */       cst.setInt(1, idClient);
/* 229 */       cst.setInt(2, occurrenceType);
/* 230 */       cst.execute();
/* 231 */       cst.close();
/*     */     }
/* 233 */     catch (SQLException e) {
/* 234 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 235 */         Logger.getLogger(PegasusSPHandler01_10.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 237 */       throw e;
/*     */     } finally {
/* 239 */       if (cst != null) {
/* 240 */         cst.close();
/*     */       }
/* 242 */       if (conn != null)
/* 243 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusSPHandler01_10.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */