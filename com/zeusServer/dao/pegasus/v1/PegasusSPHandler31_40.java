/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeusServer.dto.SP_035DataHolder;
/*     */ import com.zeusServer.dto.SP_036DataHolder;
/*     */ import com.zeusServer.dto.SP_037DataHolder;
/*     */ import com.zeusServer.dto.SP_038DataHolder;
/*     */ import com.zeusServer.dto.SP_039DataHolder;
/*     */ import com.zeusServer.dto.SP_040DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
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
/*     */ public class PegasusSPHandler31_40
/*     */ {
/*     */   public static List<String> executeSP_031(int idClient, Connection conn) throws SQLException {
/*  38 */     CallableStatement cst = null;
/*  39 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  42 */       List<String> data = new ArrayList<>();
/*     */       
/*  44 */       cst = conn.prepareCall("call SP_031(?)");
/*  45 */       cst.setInt(1, idClient);
/*  46 */       cst.execute();
/*  47 */       rs = cst.getResultSet();
/*  48 */       while (rs.next()) {
/*  49 */         data.add(rs.getString("EMAIL"));
/*     */       }
/*  51 */       rs.close();
/*  52 */       cst.close();
/*     */       
/*  54 */       return data;
/*     */     }
/*  56 */     catch (SQLException e) {
/*  57 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  58 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  60 */       throw e;
/*     */     } finally {
/*  62 */       if (rs != null) {
/*  63 */         rs.close();
/*     */       }
/*  65 */       if (cst != null) {
/*  66 */         cst.close();
/*     */       }
/*  68 */       if (conn != null) {
/*  69 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_035DataHolder> executeSP_035(int idClient, Connection conn) throws SQLException {
/*  76 */     CallableStatement cst = null;
/*  77 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  80 */       List<SP_035DataHolder> data = new ArrayList<>();
/*     */       
/*  82 */       cst = conn.prepareCall("call SP_035(?,?,?,?,?,?,?,?,?,?,?,?)");
/*  83 */       cst.setInt(1, idClient);
/*  84 */       cst.registerOutParameter(2, 5);
/*  85 */       cst.registerOutParameter(3, 12);
/*  86 */       cst.registerOutParameter(4, 5);
/*  87 */       cst.registerOutParameter(5, 12);
/*  88 */       cst.registerOutParameter(6, 5);
/*  89 */       cst.registerOutParameter(7, 4);
/*  90 */       cst.registerOutParameter(8, 4);
/*  91 */       cst.registerOutParameter(9, 4);
/*  92 */       cst.registerOutParameter(10, 5);
/*  93 */       cst.registerOutParameter(11, 4);
/*  94 */       cst.registerOutParameter(12, 12);
/*  95 */       rs = cst.executeQuery();
/*  96 */       while (rs.next()) {
/*  97 */         SP_035DataHolder sp35DH = new SP_035DataHolder();
/*  98 */         sp35DH.setEnabled(Short.valueOf(rs.getShort("ENABLED")));
/*  99 */         sp35DH.setIccid(rs.getString("ICCID"));
/* 100 */         sp35DH.setModule_Type(rs.getShort("MODULE_TYPE"));
/* 101 */         sp35DH.setPhone_Pegasus(rs.getString("PHONE_PEGASUS"));
/* 102 */         sp35DH.setMin_Signal_Level(rs.getShort("MIN_SIGNAL_LEVEL"));
/* 103 */         sp35DH.setGprs_Comm_Timeout(rs.getInt("GPRS_COMM_TIMEOUT"));
/* 104 */         sp35DH.setCsd_Comm_Timeout(rs.getInt("CSD_COMM_TIMEOUT"));
/* 105 */         sp35DH.setEth_Comm_Timeout(rs.getInt("ETH_COMM_TIMEOUT"));
/* 106 */         sp35DH.setComm_Debug(rs.getShort("COMM_DEBUG"));
/* 107 */         sp35DH.setId_Group(rs.getInt("ID_GROUP"));
/* 108 */         sp35DH.setClient_Code(rs.getString("CLIENT_CODE"));
/* 109 */         data.add(sp35DH);
/*     */       } 
/* 111 */       rs.close();
/* 112 */       cst.close();
/*     */       
/* 114 */       return data;
/*     */     }
/* 116 */     catch (SQLException e) {
/* 117 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 118 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 120 */       throw e;
/*     */     } finally {
/* 122 */       if (rs != null) {
/* 123 */         rs.close();
/*     */       }
/* 125 */       if (cst != null) {
/* 126 */         cst.close();
/*     */       }
/* 128 */       if (conn != null) {
/* 129 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_036DataHolder> executeSP_036(int idClient, Connection conn) throws SQLException {
/* 136 */     CallableStatement cst = null;
/* 137 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 140 */       List<SP_036DataHolder> data = new ArrayList<>();
/*     */       
/* 142 */       cst = conn.prepareCall("call SP_036(?,?,?)");
/* 143 */       cst.setInt(1, idClient);
/* 144 */       cst.registerOutParameter(2, 4);
/* 145 */       cst.registerOutParameter(3, 12);
/* 146 */       rs = cst.executeQuery();
/* 147 */       while (rs.next()) {
/* 148 */         SP_036DataHolder sp36DH = new SP_036DataHolder();
/* 149 */         sp36DH.setId_User(rs.getInt("ID_USER"));
/* 150 */         sp36DH.setName(rs.getString("NAME"));
/* 151 */         data.add(sp36DH);
/*     */       } 
/* 153 */       rs.close();
/* 154 */       cst.close();
/*     */       
/* 156 */       return data;
/*     */     }
/* 158 */     catch (SQLException e) {
/* 159 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 160 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 162 */       throw e;
/*     */     } finally {
/* 164 */       if (rs != null) {
/* 165 */         rs.close();
/*     */       }
/* 167 */       if (cst != null) {
/* 168 */         cst.close();
/*     */       }
/* 170 */       if (conn != null) {
/* 171 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_037DataHolder> executeSP_037(int idClient, Connection conn) throws SQLException {
/* 178 */     CallableStatement cst = null;
/* 179 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 182 */       List<SP_037DataHolder> data = new ArrayList<>();
/*     */       
/* 184 */       cst = conn.prepareCall("call SP_037(?,?,?)");
/* 185 */       cst.setInt(1, idClient);
/* 186 */       cst.registerOutParameter(2, 4);
/* 187 */       cst.registerOutParameter(3, 12);
/* 188 */       rs = cst.executeQuery();
/* 189 */       while (rs.next()) {
/* 190 */         SP_037DataHolder sp37DH = new SP_037DataHolder();
/* 191 */         sp37DH.setId_Responsible(rs.getInt("ID_RESPONSIBLE"));
/* 192 */         sp37DH.setName(rs.getString("NAME"));
/* 193 */         data.add(sp37DH);
/*     */       } 
/* 195 */       rs.close();
/* 196 */       cst.close();
/*     */       
/* 198 */       return data;
/*     */     }
/* 200 */     catch (SQLException e) {
/* 201 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 202 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 204 */       throw e;
/*     */     } finally {
/* 206 */       if (rs != null) {
/* 207 */         rs.close();
/*     */       }
/* 209 */       if (cst != null) {
/* 210 */         cst.close();
/*     */       }
/* 212 */       if (conn != null) {
/* 213 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_038DataHolder> executeSP_038(int idClient, Connection conn) throws SQLException {
/* 220 */     CallableStatement cst = null;
/* 221 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 224 */       List<SP_038DataHolder> data = new ArrayList<>();
/*     */       
/* 226 */       cst = conn.prepareCall("call SP_038(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/* 227 */       cst.setInt(1, idClient);
/* 228 */       cst.registerOutParameter(2, 12);
/* 229 */       cst.registerOutParameter(3, 4);
/* 230 */       cst.registerOutParameter(4, 12);
/* 231 */       cst.registerOutParameter(5, 4);
/* 232 */       cst.registerOutParameter(6, 12);
/* 233 */       cst.registerOutParameter(7, 4);
/* 234 */       cst.registerOutParameter(8, 12);
/* 235 */       cst.registerOutParameter(9, 4);
/* 236 */       cst.registerOutParameter(10, 12);
/* 237 */       cst.registerOutParameter(11, 4);
/* 238 */       cst.registerOutParameter(12, 12);
/* 239 */       cst.registerOutParameter(13, 4);
/* 240 */       cst.registerOutParameter(14, 12);
/* 241 */       cst.registerOutParameter(15, 12);
/* 242 */       cst.registerOutParameter(16, 4);
/* 243 */       cst.registerOutParameter(17, 12);
/* 244 */       cst.registerOutParameter(18, 4);
/* 245 */       cst.registerOutParameter(19, 12);
/* 246 */       cst.registerOutParameter(20, 4);
/* 247 */       cst.registerOutParameter(21, 12);
/* 248 */       cst.registerOutParameter(22, 4);
/* 249 */       cst.registerOutParameter(23, 12);
/* 250 */       cst.registerOutParameter(24, 4);
/* 251 */       cst.registerOutParameter(25, 12);
/* 252 */       cst.registerOutParameter(26, 4);
/* 253 */       cst.registerOutParameter(27, 12);
/* 254 */       cst.registerOutParameter(28, 4);
/* 255 */       cst.registerOutParameter(29, 12);
/* 256 */       cst.registerOutParameter(30, 4);
/* 257 */       cst.registerOutParameter(31, 12);
/* 258 */       cst.registerOutParameter(32, 4);
/* 259 */       cst.registerOutParameter(33, 12);
/* 260 */       cst.registerOutParameter(34, 4);
/* 261 */       cst.registerOutParameter(35, 12);
/* 262 */       cst.registerOutParameter(36, 4);
/* 263 */       rs = cst.executeQuery();
/* 264 */       while (rs.next()) {
/* 265 */         SP_038DataHolder sp38DH = new SP_038DataHolder();
/* 266 */         sp38DH.setE__Module_Offline(rs.getString("E_MODULE_OFFLINE"));
/* 267 */         sp38DH.setF_Module_Offline(rs.getInt("F_MODULE_OFFLINE"));
/* 268 */         sp38DH.setE_Line_Simulator_Offline(rs.getString("E_LINE_SIMULATOR_OFFLINE"));
/* 269 */         sp38DH.setF_Line_Simulator_Offline(rs.getInt("F_LINE_SIMULATOR_OFFLINE"));
/* 270 */         sp38DH.setE_Alarm_Panel_Conn_Manual(rs.getString("E_ALARM_PANEL_CONN_MANUAL"));
/* 271 */         sp38DH.setF_Alarm_Panel_Conn_Manual(rs.getInt("F_ALARM_PANEL_CONN_MANUAL"));
/* 272 */         sp38DH.setE_Phone_Line_Not_Det(rs.getString("E_PHONE_LINE_NOT_DET"));
/* 273 */         sp38DH.setF_Phone_Line_Not_Det(rs.getInt("F_PHONE_LINE_NOT_DET"));
/* 274 */         sp38DH.setE_Dual_Monitoring_Fail(rs.getString("F_PHONE_LINE_NOT_DET"));
/* 275 */         sp38DH.setF_Dual_Monitoring_Fail(rs.getInt("F_PHONE_LINE_NOT_DET"));
/* 276 */         sp38DH.setE_Alive_Received(rs.getString("E_ALIVE_RECEIVED"));
/* 277 */         sp38DH.setF_Alive_Received(rs.getInt("F_ALIVE_RECEIVED"));
/* 278 */         sp38DH.setE_Phone_Line_Transmission_Fail(rs.getString("E_PHONE_LINE_TRANSMISSION_FAIL"));
/* 279 */         sp38DH.setE_Signal_Level_Below_Min(rs.getString("E_SIGNAL_LEVEL_BELOW_MIN"));
/* 280 */         sp38DH.setF_Signal_Level_Below_Min(rs.getInt("F_SIGNAL_LEVEL_BELOW_MIN"));
/* 281 */         sp38DH.setE_Alarm_Panel_Comm_Fail(rs.getString("E_ALARM_PANEL_COMM_FAIL"));
/* 282 */         sp38DH.setF_Alarm_Panel_Comm_Fail(rs.getInt("F_ALARM_PANEL_COMM_FAIL"));
/* 283 */         sp38DH.setE_Gprs_Network_Offline(rs.getString("E_GPRS_NETWORK_OFFLINE"));
/* 284 */         sp38DH.setF_Gprs_Network_Offline(rs.getInt("F_GPRS_NETWORK_OFFLINE"));
/* 285 */         sp38DH.setE_Batt_Level_Below_Min(rs.getString("E_BATT_LEVEL_BELOW_MIN"));
/* 286 */         sp38DH.setF_Batt_Level_Below_Min(rs.getInt("F_BATT_LEVEL_BELOW_MIN"));
/* 287 */         sp38DH.setE_Ac_Supply_Not_Det(rs.getString("E_AC_SUPPLY_NOT_DET"));
/* 288 */         sp38DH.setF_Ac_Supply_Not_Det(rs.getInt("F_AC_SUPPLY_NOT_DET"));
/* 289 */         sp38DH.setE_Peripheral_Offline(rs.getString("E_PERIPHERAL_OFFLINE"));
/* 290 */         sp38DH.setF_Peripheral_Offline(rs.getInt("F_PERIPHERAL_OFFLINE"));
/* 291 */         sp38DH.setE_Alarm_Panel_Return_Not_(rs.getString("E_ALARM_PANEL_RETURN_NOT_DET"));
/* 292 */         sp38DH.setF_Alarm_Panel_Return_Not_Det(rs.getInt("F_ALARM_PANEL_RETURN_NOT_DET"));
/* 293 */         sp38DH.setE_Digital_Input_1(rs.getString("E_DIGITAL_INPUT_1"));
/* 294 */         sp38DH.setF_Digital_Input_1(rs.getInt("F_DIGITAL_INPUT_1"));
/* 295 */         sp38DH.setE_Digital_Input_2(rs.getString("E_DIGITAL_INPUT_2"));
/* 296 */         sp38DH.setF_Digital_Input_2(rs.getInt("F_DIGITAL_INPUT_2"));
/* 297 */         sp38DH.setE_Digital_Input_3(rs.getString("E_DIGITAL_INPUT_3"));
/* 298 */         sp38DH.setF_Digital_Input_3(rs.getInt("F_DIGITAL_INPUT_3"));
/* 299 */         sp38DH.setE_Digital_Input_4(rs.getString("E_DIGITAL_INPUT_4"));
/* 300 */         sp38DH.setF_Digital_Input_4(rs.getInt("F_DIGITAL_INPUT_4"));
/* 301 */         data.add(sp38DH);
/*     */       } 
/* 303 */       rs.close();
/* 304 */       cst.close();
/*     */       
/* 306 */       return data;
/*     */     }
/* 308 */     catch (SQLException e) {
/* 309 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 310 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 312 */       throw e;
/*     */     } finally {
/* 314 */       if (rs != null) {
/* 315 */         rs.close();
/*     */       }
/* 317 */       if (cst != null) {
/* 318 */         cst.close();
/*     */       }
/* 320 */       if (conn != null) {
/* 321 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_039DataHolder> executeSP_039(Connection conn) throws SQLException {
/* 328 */     CallableStatement cst = null;
/* 329 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 332 */       List<SP_039DataHolder> data = new ArrayList<>();
/*     */       
/* 334 */       cst = conn.prepareCall("call SP_039(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/* 335 */       cst.registerOutParameter(1, 12);
/* 336 */       cst.registerOutParameter(2, 4);
/* 337 */       cst.registerOutParameter(3, 12);
/* 338 */       cst.registerOutParameter(4, 4);
/* 339 */       cst.registerOutParameter(5, 12);
/* 340 */       cst.registerOutParameter(6, 4);
/* 341 */       cst.registerOutParameter(7, 12);
/* 342 */       cst.registerOutParameter(8, 4);
/* 343 */       cst.registerOutParameter(9, 12);
/* 344 */       cst.registerOutParameter(10, 4);
/* 345 */       cst.registerOutParameter(11, 12);
/* 346 */       cst.registerOutParameter(12, 4);
/* 347 */       cst.registerOutParameter(13, 12);
/* 348 */       cst.registerOutParameter(14, 12);
/* 349 */       cst.registerOutParameter(15, 4);
/* 350 */       cst.registerOutParameter(16, 12);
/* 351 */       cst.registerOutParameter(17, 4);
/* 352 */       cst.registerOutParameter(18, 12);
/* 353 */       cst.registerOutParameter(19, 4);
/* 354 */       cst.registerOutParameter(20, 12);
/* 355 */       cst.registerOutParameter(21, 4);
/* 356 */       cst.registerOutParameter(22, 12);
/* 357 */       cst.registerOutParameter(23, 4);
/* 358 */       cst.registerOutParameter(24, 12);
/* 359 */       cst.registerOutParameter(25, 4);
/* 360 */       cst.registerOutParameter(26, 12);
/* 361 */       cst.registerOutParameter(27, 4);
/* 362 */       cst.registerOutParameter(28, 12);
/* 363 */       cst.registerOutParameter(29, 4);
/* 364 */       cst.registerOutParameter(30, 12);
/* 365 */       cst.registerOutParameter(31, 4);
/* 366 */       cst.registerOutParameter(32, 12);
/* 367 */       cst.registerOutParameter(33, 4);
/* 368 */       cst.registerOutParameter(34, 12);
/* 369 */       cst.registerOutParameter(35, 4);
/* 370 */       rs = cst.executeQuery();
/* 371 */       while (rs.next()) {
/* 372 */         SP_039DataHolder sp39DH = new SP_039DataHolder();
/* 373 */         sp39DH.setE_Module_Offline(rs.getString("E_MODULE_OFFLINE"));
/* 374 */         sp39DH.setF_Module_Offline(rs.getInt("F_MODULE_OFFLINE"));
/* 375 */         sp39DH.setE_Line_Simulator_Offline(rs.getString("E_LINE_SIMULATOR_OFFLINE"));
/* 376 */         sp39DH.setF_Line_Simulator_Offline(rs.getInt("F_LINE_SIMULATOR_OFFLINE"));
/* 377 */         sp39DH.setE_Alarm_Panel_Conn_Manual(rs.getString("E_ALARM_PANEL_CONN_MANUAL"));
/* 378 */         sp39DH.setF_Alarm_Panel_Conn_Manual(rs.getInt("F_ALARM_PANEL_CONN_MANUAL"));
/* 379 */         sp39DH.setE_Phone_Line_Not_Det(rs.getString("E_PHONE_LINE_NOT_DET"));
/* 380 */         sp39DH.setF_Phone_Line_Not_Det(rs.getInt("F_PHONE_LINE_NOT_DET"));
/* 381 */         sp39DH.setE_Dual_Monitoring_Fail(rs.getString("E_DUAL_MONITORING_FAIL"));
/* 382 */         sp39DH.setF_Dual_Monitoring_Fail(rs.getInt("F_DUAL_MONITORING_FAIL"));
/* 383 */         sp39DH.setE_Alive_Received(rs.getString("E_ALIVE_RECEIVED"));
/* 384 */         sp39DH.setF_Alive_Received(rs.getInt("F_ALIVE_RECEIVED"));
/* 385 */         sp39DH.setE_Phone_Line_Transmission_Fail(rs.getString("E_PHONE_LINE_TRANSMISSION_FAIL"));
/* 386 */         sp39DH.setE_Signal_Level_Below_Min(rs.getString("E_SIGNAL_LEVEL_BELOW_MIN"));
/* 387 */         sp39DH.setF_Signal_Level_Below_Min(rs.getInt("F_SIGNAL_LEVEL_BELOW_MIN"));
/* 388 */         sp39DH.setE_Alarm_Panel_Comm_Fail(rs.getString("E_ALARM_PANEL_COMM_FAIL"));
/* 389 */         sp39DH.setF_Alarm_Panel_Comm_Fail(rs.getInt("F_ALARM_PANEL_COMM_FAIL"));
/* 390 */         sp39DH.setE_Gprs_Network_Offline(rs.getString("E_GPRS_NETWORK_OFFLINE"));
/* 391 */         sp39DH.setF_Gprs_Network_Offline(rs.getInt("F_GPRS_NETWORK_OFFLINE"));
/* 392 */         sp39DH.setE_Batt_Level_Below_Min(rs.getString("E_BATT_LEVEL_BELOW_MIN"));
/* 393 */         sp39DH.setF_Batt_Level_Below_Min(rs.getInt("F_BATT_LEVEL_BELOW_MIN"));
/* 394 */         sp39DH.setE_Ac_Supply_Not_Det(rs.getString("E_AC_SUPPLY_NOT_DET"));
/* 395 */         sp39DH.setF_Ac_Supply_Not_Det(rs.getInt("F_AC_SUPPLY_NOT_DET"));
/* 396 */         sp39DH.setE_Peripheral_Offline(rs.getString("E_PERIPHERAL_OFFLINE"));
/* 397 */         sp39DH.setF_Peripheral_Offline(rs.getInt("F_PERIPHERAL_OFFLINE"));
/* 398 */         sp39DH.setE_Alarm_Panel_Return_Not_Det(rs.getString("E_ALARM_PANEL_RETURN_NOT_DET"));
/* 399 */         sp39DH.setF_Alarm_Panel_Return_Not_Det(rs.getInt("F_ALARM_PANEL_RETURN_NOT_DET"));
/* 400 */         sp39DH.setE_Digital_Input_1(rs.getString("E_DIGITAL_INPUT_1"));
/* 401 */         sp39DH.setF_Digital_Input_1(rs.getInt("F_DIGITAL_INPUT_1"));
/* 402 */         sp39DH.setE_Digital_Input_2(rs.getString("E_DIGITAL_INPUT_2"));
/* 403 */         sp39DH.setF_Digital_Input_2(rs.getInt("F_DIGITAL_INPUT_2"));
/* 404 */         sp39DH.setE_Digital_Input_3(rs.getString("E_DIGITAL_INPUT_3"));
/* 405 */         sp39DH.setF_Digital_Input_3(rs.getInt("F_DIGITAL_INPUT_3"));
/* 406 */         sp39DH.setE_Digital_Input_4(rs.getString("E_DIGITAL_INPUT_4"));
/* 407 */         sp39DH.setF_Digital_Input_4(rs.getInt("F_DIGITAL_INPUT_4"));
/* 408 */         data.add(sp39DH);
/*     */       } 
/* 410 */       rs.close();
/* 411 */       cst.close();
/*     */       
/* 413 */       return data;
/*     */     }
/* 415 */     catch (SQLException e) {
/* 416 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 417 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 419 */       throw e;
/*     */     } finally {
/* 421 */       if (rs != null) {
/* 422 */         rs.close();
/*     */       }
/* 424 */       if (cst != null) {
/* 425 */         cst.close();
/*     */       }
/* 427 */       if (conn != null) {
/* 428 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_040DataHolder> executeSP_040(int idResponsible, Connection conn) throws SQLException {
/* 435 */     CallableStatement cst = null;
/* 436 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 439 */       List<SP_040DataHolder> data = new ArrayList<>();
/*     */       
/* 441 */       cst = conn.prepareCall("call SP_040(?,?,?,?,?)");
/* 442 */       cst.setInt(1, idResponsible);
/* 443 */       cst.registerOutParameter(2, 12);
/* 444 */       cst.registerOutParameter(3, 12);
/* 445 */       cst.registerOutParameter(4, 12);
/* 446 */       cst.registerOutParameter(5, 12);
/* 447 */       rs = cst.executeQuery();
/* 448 */       while (rs.next()) {
/* 449 */         SP_040DataHolder sp40DH = new SP_040DataHolder();
/* 450 */         sp40DH.setPhone(rs.getString("PHONE"));
/* 451 */         sp40DH.setFax(rs.getString("FAX"));
/* 452 */         sp40DH.setEmail(rs.getString("EMAIL"));
/* 453 */         sp40DH.setName(rs.getString("NAME"));
/* 454 */         data.add(sp40DH);
/*     */       } 
/* 456 */       rs.close();
/* 457 */       cst.close();
/*     */       
/* 459 */       return data;
/*     */     }
/* 461 */     catch (SQLException e) {
/* 462 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 463 */         Logger.getLogger(PegasusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 465 */       throw e;
/*     */     } finally {
/* 467 */       if (rs != null) {
/* 468 */         rs.close();
/*     */       }
/* 470 */       if (cst != null) {
/* 471 */         cst.close();
/*     */       }
/* 473 */       if (conn != null)
/* 474 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusSPHandler31_40.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */