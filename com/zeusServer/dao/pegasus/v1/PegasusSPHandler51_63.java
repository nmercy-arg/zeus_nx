/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeusServer.dto.SP_052DataHolder;
/*     */ import com.zeusServer.dto.SP_053DataHolder;
/*     */ import com.zeusServer.dto.SP_063DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LoggedInUser;
/*     */ import com.zeuscc.pegasus.derby.beans.ModuleBean;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.sql.Blob;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
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
/*     */ public class PegasusSPHandler51_63
/*     */ {
/*     */   public static void executeSP_051(int idResponsible, Connection conn) throws SQLException {
/*  41 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  44 */       cst = conn.prepareCall("call SP_051(?)");
/*  45 */       cst.setInt(1, idResponsible);
/*  46 */       cst.execute();
/*  47 */       cst.close();
/*     */     }
/*  49 */     catch (SQLException e) {
/*  50 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  51 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  53 */       throw e;
/*     */     } finally {
/*  55 */       if (cst != null) {
/*  56 */         cst.close();
/*     */       }
/*  58 */       if (conn != null) {
/*  59 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_052(SP_052DataHolder sp52DH, Connection conn) throws SQLException {
/*  66 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  69 */       cst = conn.prepareCall("call SP_052(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/*  70 */       cst.setInt(1, sp52DH.getIdClient());
/*  71 */       cst.setString(2, sp52DH.getE_Module_Offline());
/*  72 */       cst.setInt(3, sp52DH.getF_Module_Offline());
/*  73 */       cst.setString(4, sp52DH.getE_Line_Simulator_Offline());
/*  74 */       cst.setInt(5, sp52DH.getF_Line_Simulator_Offline());
/*  75 */       cst.setString(6, sp52DH.getE_Alarm_Panel_Conn_Manual());
/*  76 */       cst.setInt(7, sp52DH.getF_Alarm_Panel_Conn_Manual());
/*  77 */       cst.setString(8, sp52DH.getE_Phone_Line_Not_Det());
/*  78 */       cst.setInt(9, sp52DH.getF_Phone_Line_Not_Det());
/*  79 */       cst.setString(10, sp52DH.getE_Dual_Monitoring_Fail());
/*  80 */       cst.setInt(11, sp52DH.getF_Dual_Monitoring_Fail());
/*  81 */       cst.setString(12, sp52DH.getE_Alive_Received());
/*  82 */       cst.setInt(13, sp52DH.getF_Alive_Received());
/*  83 */       cst.setString(14, sp52DH.getE_Phone_Line_Transmission_Fail());
/*  84 */       cst.setString(15, sp52DH.getE_Signal_Level_Below_Min());
/*  85 */       cst.setInt(16, sp52DH.getF_Signal_Level_Below_Min());
/*  86 */       cst.setString(17, sp52DH.getE_Alarm_Panel_Comm_Fail());
/*  87 */       cst.setInt(18, sp52DH.getF_Alarm_Panel_Comm_Fail());
/*  88 */       cst.setString(19, sp52DH.getE_Gprs_Network_Offline());
/*  89 */       cst.setInt(20, sp52DH.getF_Gprs_Network_Offline());
/*  90 */       cst.setString(21, sp52DH.getE_Batt_Level_Below_Min());
/*  91 */       cst.setInt(22, sp52DH.getF_Batt_Level_Below_Min());
/*  92 */       cst.setString(23, sp52DH.getE_Ac_Supply_Not_Det());
/*  93 */       cst.setInt(24, sp52DH.getF_Ac_Supply_Not_Det());
/*  94 */       cst.setString(25, sp52DH.getE_Alarm_Panel_Return_Not_Det());
/*  95 */       cst.setInt(26, sp52DH.getF_Alarm_Panel_Return_Not_Det());
/*  96 */       cst.setString(27, sp52DH.getE_Digital_Input_1());
/*  97 */       cst.setInt(28, sp52DH.getF_Digital_Input_1());
/*  98 */       cst.setString(29, sp52DH.getE_Digital_Input_2());
/*  99 */       cst.setInt(30, sp52DH.getF_Digital_Input_2());
/* 100 */       cst.setString(31, sp52DH.getE_Digital_Input_3());
/* 101 */       cst.setInt(32, sp52DH.getF_Digital_Input_3());
/* 102 */       cst.setString(33, sp52DH.getE_Digital_Input_4());
/* 103 */       cst.setInt(34, sp52DH.getF_Digital_Input_4());
/* 104 */       cst.execute();
/* 105 */       cst.close();
/*     */     }
/* 107 */     catch (SQLException e) {
/* 108 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 109 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 111 */       throw e;
/*     */     } finally {
/* 113 */       if (cst != null) {
/* 114 */         cst.close();
/*     */       }
/* 116 */       if (conn != null) {
/* 117 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_053(SP_053DataHolder sp53DH, Connection conn) throws SQLException {
/* 124 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 127 */       cst = conn.prepareCall("call SP_053(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/* 128 */       cst.setString(1, sp53DH.getE_Module_Offline());
/* 129 */       cst.setInt(2, sp53DH.getF_Module_Offline());
/* 130 */       cst.setString(3, sp53DH.getE_Line_Simulator_Offline());
/* 131 */       cst.setInt(4, sp53DH.getF_Line_Simulator_Offline());
/* 132 */       cst.setString(5, sp53DH.getE_Alarm_Panel_Conn_Manual());
/* 133 */       cst.setInt(6, sp53DH.getF_Alarm_Panel_Conn_Manual());
/* 134 */       cst.setString(7, sp53DH.getE_Phone_Line_Not_Det());
/* 135 */       cst.setInt(8, sp53DH.getF_Phone_Line_Not_Det());
/* 136 */       cst.setString(9, sp53DH.getE_Dual_Monitoring_Fail());
/* 137 */       cst.setInt(10, sp53DH.getF_Dual_Monitoring_Fail());
/* 138 */       cst.setString(11, sp53DH.getE_Alive_Received());
/* 139 */       cst.setInt(12, sp53DH.getF_Alive_Received());
/* 140 */       cst.setString(13, sp53DH.getE_Phone_Line_Transmission_Fail());
/* 141 */       cst.setString(14, sp53DH.getE_Signal_Level_Below_Min());
/* 142 */       cst.setInt(15, sp53DH.getF_Signal_Level_Below_Min());
/* 143 */       cst.setString(16, sp53DH.getE_Alarm_Panel_Comm_Fail());
/* 144 */       cst.setInt(17, sp53DH.getF_Alarm_Panel_Comm_Fail());
/* 145 */       cst.setString(18, sp53DH.getE_Gprs_Network_Offline());
/* 146 */       cst.setInt(19, sp53DH.getF_Gprs_Network_Offline());
/* 147 */       cst.setString(20, sp53DH.getE_Batt_Level_Below_Min());
/* 148 */       cst.setInt(21, sp53DH.getF_Batt_Level_Below_Min());
/* 149 */       cst.setString(22, sp53DH.getE_Ac_Supply_Not_Det());
/* 150 */       cst.setInt(23, sp53DH.getF_Ac_Supply_Not_Det());
/* 151 */       cst.setString(24, sp53DH.getE_Alarm_Panel_Return_Not_Det());
/* 152 */       cst.setInt(25, sp53DH.getF_Alarm_Panel_Return_Not_Det());
/* 153 */       cst.setString(26, sp53DH.getE_Digital_Input_1());
/* 154 */       cst.setInt(27, sp53DH.getF_Digital_Input_1());
/* 155 */       cst.setString(28, sp53DH.getE_Digital_Input_2());
/* 156 */       cst.setInt(29, sp53DH.getF_Digital_Input_2());
/* 157 */       cst.setString(30, sp53DH.getE_Digital_Input_3());
/* 158 */       cst.setInt(31, sp53DH.getF_Digital_Input_3());
/* 159 */       cst.setString(32, sp53DH.getE_Digital_Input_4());
/* 160 */       cst.setInt(33, sp53DH.getF_Digital_Input_4());
/* 161 */       cst.execute();
/* 162 */       cst.close();
/*     */     }
/* 164 */     catch (SQLException e) {
/* 165 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 166 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 168 */       throw e;
/*     */     } finally {
/* 170 */       if (cst != null) {
/* 171 */         cst.close();
/*     */       }
/* 173 */       if (conn != null) {
/* 174 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_054(int idClient, Blob commandData, short commandType, Connection conn) throws SQLException {
/* 181 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 184 */       cst = conn.prepareCall("call SP_054(?,?,?)");
/* 185 */       cst.setInt(1, idClient);
/* 186 */       cst.setBlob(2, commandData);
/* 187 */       cst.setShort(3, commandType);
/* 188 */       cst.execute();
/* 189 */       cst.close();
/*     */     }
/* 191 */     catch (SQLException e) {
/* 192 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 193 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 195 */       throw e;
/*     */     } finally {
/* 197 */       if (cst != null) {
/* 198 */         cst.close();
/*     */       }
/* 200 */       if (conn != null) {
/* 201 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static LoggedInUser executeSP_055(String userName, String password, Connection conn) throws SQLException {
/* 208 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 211 */       LoggedInUser user = null;
/*     */       
/* 213 */       cst = conn.prepareCall("call SP_055(?,?,?,?,?,?,?,?,?,?,?,?)");
/* 214 */       cst.setString(1, userName);
/* 215 */       cst.setString(2, password);
/* 216 */       cst.registerOutParameter(3, 4);
/* 217 */       cst.registerOutParameter(4, 4);
/* 218 */       cst.registerOutParameter(5, 4);
/* 219 */       cst.registerOutParameter(6, 4);
/* 220 */       cst.registerOutParameter(7, 4);
/* 221 */       cst.registerOutParameter(8, 4);
/* 222 */       cst.registerOutParameter(9, 12);
/* 223 */       cst.registerOutParameter(10, 12);
/* 224 */       cst.registerOutParameter(11, 12);
/* 225 */       cst.registerOutParameter(12, 4);
/* 226 */       cst.execute();
/* 227 */       if (cst.getInt(12) > 0) {
/* 228 */         user = new LoggedInUser();
/* 229 */         user.setIdClient(cst.getInt(3));
/* 230 */         user.setIdGroup(cst.getInt(4));
/* 231 */         user.setClientType(cst.getInt(5));
/* 232 */         user.setPermissions(cst.getString(6));
/* 233 */         user.setEnabled(cst.getInt(7));
/* 234 */         user.setDateFormat(cst.getInt(8));
/* 235 */         user.setTimeZone(cst.getString(9));
/* 236 */         user.setLanguage(cst.getString(10));
/* 237 */         user.setUserName(cst.getString(11));
/* 238 */         user.setIdUser(cst.getInt(12));
/*     */       } 
/* 240 */       cst.close();
/*     */       
/* 242 */       return user;
/*     */     }
/* 244 */     catch (SQLException e) {
/* 245 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 246 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 248 */       throw e;
/*     */     } finally {
/* 250 */       if (cst != null) {
/* 251 */         cst.close();
/*     */       }
/* 253 */       if (conn != null) {
/* 254 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_056(int idOccurrence, Connection conn) throws SQLException {
/* 261 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 264 */       cst = conn.prepareCall("call SP_056(?)");
/* 265 */       cst.setInt(1, idOccurrence);
/* 266 */       cst.execute();
/* 267 */       cst.close();
/*     */     }
/* 269 */     catch (SQLException e) {
/* 270 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 271 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 273 */       throw e;
/*     */     } finally {
/* 275 */       if (cst != null) {
/* 276 */         cst.close();
/*     */       }
/* 278 */       if (conn != null) {
/* 279 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_059(List<ModuleBean> mdBeanList, Connection conn) throws SQLException {
/* 286 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 289 */       cst = conn.prepareCall("call SP_059(?)");
/* 290 */       for (ModuleBean mdBean : mdBeanList) {
/* 291 */         cst.setObject(1, mdBean);
/* 292 */         cst.addBatch();
/*     */       } 
/* 294 */       cst.executeBatch();
/* 295 */       cst.close();
/*     */     }
/* 297 */     catch (SQLException e) {
/* 298 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 299 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 301 */       throw e;
/*     */     } finally {
/* 303 */       if (cst != null) {
/* 304 */         cst.close();
/*     */       }
/* 306 */       if (conn != null) {
/* 307 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<String> executeSP_061(Connection conn) throws SQLException {
/* 314 */     CallableStatement cst = null;
/* 315 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 318 */       List<String> data = new ArrayList<>();
/*     */       
/* 320 */       cst = conn.prepareCall("call SP_061(?)");
/* 321 */       cst.registerOutParameter(1, 12);
/* 322 */       rs = cst.executeQuery();
/* 323 */       while (rs.next()) {
/* 324 */         data.add(rs.getString("NAME"));
/*     */       }
/* 326 */       rs.close();
/* 327 */       cst.close();
/*     */       
/* 329 */       return data;
/*     */     }
/* 331 */     catch (SQLException e) {
/* 332 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 333 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 335 */       throw e;
/*     */     } finally {
/* 337 */       if (rs != null) {
/* 338 */         rs.close();
/*     */       }
/* 340 */       if (cst != null) {
/* 341 */         cst.close();
/*     */       }
/* 343 */       if (conn != null) {
/* 344 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_063DataHolder executeSP_063(int idModule, Connection conn) throws SQLException {
/* 351 */     CallableStatement cst = null;
/* 352 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 355 */       SP_063DataHolder sp63DH = null;
/*     */       
/* 357 */       cst = conn.prepareCall("call SP_063(?,?,?)");
/* 358 */       cst.setInt(1, idModule);
/* 359 */       cst.registerOutParameter(2, 4);
/* 360 */       cst.registerOutParameter(3, 4);
/* 361 */       rs = cst.executeQuery();
/* 362 */       if (rs.next()) {
/* 363 */         sp63DH = new SP_063DataHolder();
/* 364 */         sp63DH.setConnections_24H(rs.getInt("CONNECTIONS_24H"));
/* 365 */         sp63DH.setAlives_24H(rs.getInt("ALIVES_24H"));
/*     */       } 
/* 367 */       rs.close();
/* 368 */       cst.close();
/*     */       
/* 370 */       return sp63DH;
/*     */     }
/* 372 */     catch (SQLException e) {
/* 373 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 374 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 376 */       throw e;
/*     */     } finally {
/* 378 */       if (rs != null) {
/* 379 */         rs.close();
/*     */       }
/* 381 */       if (cst != null) {
/* 382 */         cst.close();
/*     */       }
/* 384 */       if (conn != null) {
/* 385 */         conn.close();
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
/*     */   
/*     */   public static void executeSP_065(int idCommand, byte[] commandFileData, Connection conn) throws SQLException {
/* 401 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 404 */       cst = conn.prepareCall("call SP_065(?,?)");
/* 405 */       cst.setInt(1, idCommand);
/* 406 */       cst.setBytes(2, commandFileData);
/* 407 */       cst.executeUpdate();
/* 408 */       cst.close();
/*     */     }
/* 410 */     catch (SQLException e) {
/* 411 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 412 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 414 */       throw e;
/*     */     } finally {
/* 416 */       if (cst != null) {
/* 417 */         cst.close();
/*     */       }
/* 419 */       if (conn != null) {
/* 420 */         conn.close();
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
/*     */   public static void execute_065Query(int idCommand, InputStream commandFileDataStream, Connection conn) throws SQLException {
/* 434 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 437 */       ps = conn.prepareStatement("UPDATE COMMAND SET COMMAND_FILE_DATA = ? WHERE ID_COMMAND =?");
/* 438 */       ps.setBlob(1, commandFileDataStream);
/* 439 */       ps.setInt(2, idCommand);
/* 440 */       ps.executeUpdate();
/* 441 */       ps.close();
/*     */     }
/* 443 */     catch (SQLException e) {
/* 444 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 445 */         Logger.getLogger(PegasusSPHandler51_63.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 447 */       throw e;
/*     */     } finally {
/* 449 */       if (commandFileDataStream != null) {
/*     */         try {
/* 451 */           commandFileDataStream.close();
/* 452 */         } catch (IOException iOException) {}
/*     */       }
/*     */ 
/*     */       
/* 456 */       if (ps != null) {
/* 457 */         ps.close();
/*     */       }
/* 459 */       if (conn != null)
/* 460 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusSPHandler51_63.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */