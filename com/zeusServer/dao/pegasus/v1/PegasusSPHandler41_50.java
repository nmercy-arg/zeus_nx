/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeusServer.dto.SP_041DataHolder;
/*     */ import com.zeusServer.dto.SP_044DataHolder;
/*     */ import com.zeusServer.dto.SP_045DataHolder;
/*     */ import com.zeusServer.dto.SP_046DataHolder;
/*     */ import com.zeusServer.dto.SP_047DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
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
/*     */ public class PegasusSPHandler41_50
/*     */ {
/*     */   public static List<SP_041DataHolder> executeSP_041(int idUser, Connection conn) throws SQLException {
/*  38 */     CallableStatement cst = null;
/*  39 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  42 */       List<SP_041DataHolder> data = new ArrayList<>();
/*     */       
/*  44 */       cst = conn.prepareCall("call SP_041(?,?,?,?)");
/*  45 */       cst.setInt(1, idUser);
/*  46 */       cst.registerOutParameter(2, 12);
/*  47 */       cst.registerOutParameter(3, 12);
/*  48 */       cst.registerOutParameter(4, 4);
/*  49 */       rs = cst.executeQuery();
/*  50 */       while (rs.next()) {
/*  51 */         SP_041DataHolder sp41DH = new SP_041DataHolder();
/*  52 */         sp41DH.setName(rs.getString("NAME"));
/*  53 */         sp41DH.setUserId(rs.getString("USERID"));
/*  54 */         sp41DH.setPermissions(rs.getInt("PERMISSIONS"));
/*  55 */         data.add(sp41DH);
/*     */       } 
/*  57 */       rs.close();
/*  58 */       cst.close();
/*     */       
/*  60 */       return data;
/*     */     }
/*  62 */     catch (SQLException e) {
/*  63 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  64 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  66 */       throw e;
/*     */     } finally {
/*  68 */       if (rs != null) {
/*  69 */         rs.close();
/*     */       }
/*  71 */       if (cst != null) {
/*  72 */         cst.close();
/*     */       }
/*  74 */       if (conn != null) {
/*  75 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> executeSP_042(Connection conn) throws SQLException {
/*  82 */     CallableStatement cst = null;
/*  83 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  86 */       List<Integer> data = new ArrayList<>();
/*     */       
/*  88 */       cst = conn.prepareCall("call SP_042(?)");
/*  89 */       cst.registerOutParameter(1, 4);
/*  90 */       rs = cst.executeQuery();
/*  91 */       while (rs.next()) {
/*  92 */         data.add(Integer.valueOf(rs.getInt("ID_CLIENT")));
/*     */       }
/*  94 */       rs.close();
/*  95 */       cst.close();
/*     */       
/*  97 */       return data;
/*     */     }
/*  99 */     catch (SQLException e) {
/* 100 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 101 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 103 */       throw e;
/*     */     } finally {
/* 105 */       if (rs != null) {
/* 106 */         rs.close();
/*     */       }
/* 108 */       if (cst != null) {
/* 109 */         cst.close();
/*     */       }
/* 111 */       if (conn != null) {
/* 112 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static short executeSP_043(int idClient, short clientType, Connection conn) throws SQLException {
/* 119 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 122 */       cst = conn.prepareCall("call SP_043(?,?,?)");
/* 123 */       cst.setInt(1, idClient);
/* 124 */       cst.setShort(2, clientType);
/* 125 */       cst.registerOutParameter(3, 5);
/* 126 */       cst.execute();
/* 127 */       return cst.getShort("NOT_DELETED");
/*     */     }
/* 129 */     catch (SQLException e) {
/* 130 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 131 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 133 */       throw e;
/*     */     } finally {
/* 135 */       if (cst != null) {
/* 136 */         cst.close();
/*     */       }
/* 138 */       if (conn != null) {
/* 139 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_044DataHolder> executeSP_044(int idClient, Connection conn) throws SQLException {
/* 146 */     CallableStatement cst = null;
/* 147 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 150 */       List<SP_044DataHolder> data = new ArrayList<>();
/*     */       
/* 152 */       cst = conn.prepareCall("call SP_044(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/* 153 */       cst.setInt(1, idClient);
/* 154 */       cst.registerOutParameter(2, 4);
/* 155 */       cst.registerOutParameter(3, 12);
/* 156 */       cst.registerOutParameter(4, 4);
/* 157 */       cst.registerOutParameter(5, 93);
/* 158 */       cst.registerOutParameter(6, 5);
/* 159 */       cst.registerOutParameter(7, 93);
/* 160 */       cst.registerOutParameter(8, 12);
/* 161 */       cst.registerOutParameter(9, 5);
/* 162 */       cst.registerOutParameter(10, 5);
/* 163 */       cst.registerOutParameter(11, 5);
/* 164 */       cst.registerOutParameter(12, 5);
/* 165 */       cst.registerOutParameter(13, 5);
/* 166 */       cst.registerOutParameter(14, 5);
/* 167 */       cst.registerOutParameter(15, 5);
/* 168 */       cst.registerOutParameter(16, 4);
/* 169 */       cst.registerOutParameter(17, 4);
/* 170 */       cst.registerOutParameter(18, 4);
/* 171 */       cst.registerOutParameter(19, 5);
/* 172 */       cst.registerOutParameter(20, 6);
/* 173 */       cst.registerOutParameter(21, 5);
/* 174 */       cst.registerOutParameter(22, 5);
/* 175 */       cst.registerOutParameter(23, 5);
/* 176 */       cst.registerOutParameter(24, 5);
/* 177 */       cst.registerOutParameter(25, 5);
/* 178 */       cst.registerOutParameter(26, 5);
/* 179 */       cst.registerOutParameter(27, 5);
/* 180 */       rs = cst.executeQuery();
/* 181 */       while (rs.next()) {
/* 182 */         SP_044DataHolder sp44DH = new SP_044DataHolder();
/* 183 */         sp44DH.setId_Module(rs.getInt("ID_MODULE"));
/* 184 */         sp44DH.setIccid(rs.getString("ICCID"));
/* 185 */         sp44DH.setOperation_Mode(rs.getInt("OPERATION_MODE"));
/* 186 */         Calendar cal = Calendar.getInstance();
/* 187 */         cal.setTimeInMillis(rs.getTimestamp("CONNECTION_TIMESTAMP").getTime());
/* 188 */         sp44DH.setConnection_Timestamp(cal);
/* 189 */         sp44DH.setAlarm_Panel_Connection_Status(rs.getShort("ALARM_PANEL_CONNECTION_STATUS"));
/* 190 */         cal.setTimeInMillis(rs.getTimestamp("LAST_COMMUNICATION").getTime());
/* 191 */         sp44DH.setLast_Communication(cal);
/* 192 */         sp44DH.setModule_Ip_Addr(rs.getString("MODULE_IP_ADDR"));
/* 193 */         sp44DH.setLine_Simulator_Status(rs.getShort("LINE_SIMULATOR_STATUS"));
/* 194 */         sp44DH.setPhone_Line_Status(rs.getShort("PHONE_LINE_STATUS"));
/* 195 */         sp44DH.setPegasus_Firmware_Version(rs.getShort("PEGASUS_FIRMWARE_VERSION"));
/* 196 */         sp44DH.setLine_Simulator_Firmware_Version(rs.getShort("LINE_SIMULATOR_FIRMWARE_VERSION"));
/* 197 */         sp44DH.setDual_Monitoring_Status(rs.getShort("DUAL_MONITORING_STATUS"));
/* 198 */         sp44DH.setLast_Signal_Level(rs.getShort("LAST_SIGNAL_LEVEL"));
/* 199 */         sp44DH.setAlarm_Panel_Comm_Status(rs.getShort("ALARM_PANEL_COMM_STATUS"));
/* 200 */         sp44DH.setGprs_Comm_Timeout(rs.getInt("GPRS_COMM_TIMEOUT"));
/* 201 */         sp44DH.setCsd_Comm_Timeout(rs.getInt("CSD_COMM_TIMEOUT"));
/* 202 */         sp44DH.setEth_Comm_Timeout(rs.getInt("ETH_COMM_TIMEOUT"));
/* 203 */         sp44DH.setLast_Comm_Interface(rs.getShort("LAST_COMM_INTERFACE"));
/* 204 */         sp44DH.setLast_Battery_Level(rs.getFloat("LAST_BATTERY_LEVEL"));
/* 205 */         sp44DH.setAc_Supply_Status(rs.getShort("AC_SUPPLY_STATUS"));
/* 206 */         sp44DH.setGsm_Freq(rs.getShort("GSM_FREQ"));
/* 207 */         sp44DH.setAlarm_Panel_Return_Status(rs.getShort("ALARM_PANEL_RETURN_STATUS"));
/* 208 */         sp44DH.setDigital_Input_1_Status(rs.getShort("DIGITAL_INPUT_1_STATUS"));
/* 209 */         sp44DH.setDigital_Input_2_Status(rs.getShort("DIGITAL_INPUT_2_STATUS"));
/* 210 */         sp44DH.setDigital_Input_3_Status(rs.getShort("DIGITAL_INPUT_3_STATUS"));
/* 211 */         sp44DH.setDigital_Input_4_Status(rs.getShort("DIGITAL_INPUT_4_STATUS"));
/* 212 */         data.add(sp44DH);
/*     */       } 
/* 214 */       rs.close();
/* 215 */       cst.close();
/*     */       
/* 217 */       return data;
/*     */     }
/* 219 */     catch (SQLException e) {
/* 220 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 221 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 223 */       throw e;
/*     */     } finally {
/* 225 */       if (rs != null) {
/* 226 */         rs.close();
/*     */       }
/* 228 */       if (cst != null) {
/* 229 */         cst.close();
/*     */       }
/* 231 */       if (conn != null) {
/* 232 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_045DataHolder executeSP_045(SP_045DataHolder sp45DH, Connection conn) throws SQLException {
/* 239 */     CallableStatement cst = null;
/* 240 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 243 */       cst = conn.prepareCall("call SP_045(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/* 244 */       cst.setInt(1, sp45DH.getId_Client());
/* 245 */       cst.setInt(2, sp45DH.getId_Group());
/* 246 */       cst.setShort(3, sp45DH.getClient_Type());
/* 247 */       cst.setString(4, sp45DH.getName());
/* 248 */       cst.setString(5, sp45DH.getIccid());
/* 249 */       cst.setString(6, sp45DH.getClient_Code());
/* 250 */       cst.setShort(7, sp45DH.getEnabled());
/* 251 */       cst.setShort(8, sp45DH.getModule_Type());
/* 252 */       cst.setString(9, sp45DH.getPhone_Pegasus());
/* 253 */       cst.setShort(10, sp45DH.getComm_Debug());
/* 254 */       cst.setShort(11, sp45DH.getMin_Signal_Level());
/* 255 */       cst.setInt(12, sp45DH.getGprs_Comm_Timeout());
/* 256 */       cst.setInt(13, sp45DH.getCsd_Comm_Timeout());
/* 257 */       cst.setInt(14, sp45DH.getEth_Comm_Timeout());
/* 258 */       cst.registerOutParameter(15, 5);
/* 259 */       cst.registerOutParameter(16, 5);
/* 260 */       cst.registerOutParameter(17, 5);
/* 261 */       cst.registerOutParameter(18, 4);
/* 262 */       rs = cst.executeQuery();
/* 263 */       if (rs.next()) {
/* 264 */         sp45DH.setName_Exists(rs.getShort("NAME_EXISTS"));
/* 265 */         sp45DH.setIccid_Exists(rs.getShort("ICCID_EXISTS"));
/* 266 */         sp45DH.setPhone_Exists(rs.getShort("PHONE_EXISTS"));
/* 267 */         sp45DH.setNew_Id_Client(rs.getInt("NEW_ID_CLIENT"));
/*     */       } 
/* 269 */       rs.close();
/* 270 */       cst.close();
/*     */       
/* 272 */       return sp45DH;
/*     */     }
/* 274 */     catch (SQLException e) {
/* 275 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 276 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 278 */       throw e;
/*     */     } finally {
/* 280 */       if (rs != null) {
/* 281 */         rs.close();
/*     */       }
/* 283 */       if (cst != null) {
/* 284 */         cst.close();
/*     */       }
/* 286 */       if (conn != null) {
/* 287 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_046DataHolder executeSP_046(SP_046DataHolder sp46DH, Connection conn) throws SQLException {
/* 294 */     CallableStatement cst = null;
/* 295 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 298 */       cst = conn.prepareCall("call SP_046(?,?,?,?,?,?,?)");
/* 299 */       cst.setInt(1, sp46DH.getIdClient());
/* 300 */       cst.setString(2, sp46DH.getName());
/* 301 */       cst.setString(3, sp46DH.getUserID());
/* 302 */       cst.setString(4, sp46DH.getPassword());
/* 303 */       cst.setInt(5, sp46DH.getPermissions());
/* 304 */       cst.registerOutParameter(6, 5);
/* 305 */       cst.registerOutParameter(7, 5);
/* 306 */       rs = cst.executeQuery();
/* 307 */       if (rs.next()) {
/* 308 */         sp46DH.setNameExists(rs.getShort("NAME_EXISTS"));
/* 309 */         sp46DH.setUserExists(rs.getShort("USER_EXISTS"));
/*     */       } 
/* 311 */       rs.close();
/* 312 */       cst.close();
/*     */       
/* 314 */       return sp46DH;
/*     */     }
/* 316 */     catch (SQLException e) {
/* 317 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 318 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 320 */       throw e;
/*     */     } finally {
/* 322 */       if (rs != null) {
/* 323 */         rs.close();
/*     */       }
/* 325 */       if (cst != null) {
/* 326 */         cst.close();
/*     */       }
/* 328 */       if (conn != null) {
/* 329 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_047DataHolder executeSP_047(SP_047DataHolder sp47DH, Connection conn) throws SQLException {
/* 336 */     CallableStatement cst = null;
/* 337 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 340 */       cst = conn.prepareCall("call SP_047(?,?,?,?,?,?,?,?,?,?,?)");
/* 341 */       cst.setInt(1, sp47DH.getIdClient());
/* 342 */       cst.setInt(2, sp47DH.getIdUser());
/* 343 */       cst.setString(3, sp47DH.getName());
/* 344 */       cst.setString(4, sp47DH.getUserID());
/* 345 */       cst.setShort(5, sp47DH.getChangePass());
/* 346 */       cst.setString(6, sp47DH.getCurrentPass());
/* 347 */       cst.setString(7, sp47DH.getNewPass());
/* 348 */       cst.setInt(8, sp47DH.getPermissions());
/* 349 */       cst.registerOutParameter(9, 5);
/* 350 */       cst.registerOutParameter(10, 5);
/* 351 */       cst.registerOutParameter(11, 5);
/* 352 */       rs = cst.executeQuery();
/* 353 */       if (rs.next()) {
/* 354 */         sp47DH.setNameExists(rs.getShort("NAME_EXISTS"));
/* 355 */         sp47DH.setUserExists(rs.getShort("USER_EXISTS"));
/* 356 */         sp47DH.setIncorrectPass(rs.getShort("INCORRECT_PASS"));
/*     */       } 
/* 358 */       rs.close();
/* 359 */       cst.close();
/*     */       
/* 361 */       return sp47DH;
/*     */     }
/* 363 */     catch (SQLException e) {
/* 364 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 365 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 367 */       throw e;
/*     */     } finally {
/* 369 */       if (rs != null) {
/* 370 */         rs.close();
/*     */       }
/* 372 */       if (cst != null) {
/* 373 */         cst.close();
/*     */       }
/* 375 */       if (conn != null) {
/* 376 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_048(int userID, Connection conn) throws SQLException {
/* 383 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 386 */       cst = conn.prepareCall("call SP_048(?)");
/* 387 */       cst.setInt(1, userID);
/* 388 */       cst.execute();
/* 389 */       cst.close();
/*     */     }
/* 391 */     catch (SQLException e) {
/* 392 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 393 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 395 */       throw e;
/*     */     } finally {
/* 397 */       if (cst != null) {
/* 398 */         cst.close();
/*     */       }
/* 400 */       if (conn != null) {
/* 401 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static short executeSP_049(int idClient, String name, String phone, String fax, String email, Connection conn) throws SQLException {
/* 408 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 411 */       cst = conn.prepareCall("call SP_049(?,?,?,?,?,?)");
/* 412 */       cst.setInt(1, idClient);
/* 413 */       cst.setString(2, name);
/* 414 */       cst.setString(3, phone);
/* 415 */       cst.setString(4, fax);
/* 416 */       cst.setString(5, email);
/* 417 */       cst.registerOutParameter(6, 5);
/* 418 */       cst.execute();
/* 419 */       return cst.getShort("NAME_EXISTS");
/*     */     }
/* 421 */     catch (SQLException e) {
/* 422 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 423 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 425 */       throw e;
/*     */     } finally {
/* 427 */       if (cst != null) {
/* 428 */         cst.close();
/*     */       }
/* 430 */       if (conn != null) {
/* 431 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static short executeSP_050(int idClient, int idResponsible, String name, String phone, String fax, String email, Connection conn) throws SQLException {
/* 438 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 441 */       cst = conn.prepareCall("call SP_050(?,?,?,?,?,?,?)");
/* 442 */       cst.setInt(1, idClient);
/* 443 */       cst.setInt(2, idResponsible);
/* 444 */       cst.setString(3, name);
/* 445 */       cst.setString(4, phone);
/* 446 */       cst.setString(5, fax);
/* 447 */       cst.setString(6, email);
/* 448 */       cst.registerOutParameter(7, 5);
/* 449 */       cst.execute();
/* 450 */       return cst.getShort("NAME_EXISTS");
/*     */     }
/* 452 */     catch (SQLException e) {
/* 453 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 454 */         Logger.getLogger(PegasusSPHandler41_50.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 456 */       throw e;
/*     */     } finally {
/* 458 */       if (cst != null) {
/* 459 */         cst.close();
/*     */       }
/* 461 */       if (conn != null)
/* 462 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusSPHandler41_50.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */