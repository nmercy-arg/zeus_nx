/*     */ package com.zeusServer.DBGeneral;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import java.io.IOException;
/*     */ import java.net.UnknownHostException;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DatabaseMetaData;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
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
/*     */ public class DBUpdater
/*     */ {
/*     */   public static void updateDB(Connection conn) throws SQLException, IOException, Exception {
/*     */     try {
/*  32 */       for (String schemaName : Util.getAvailbleSchemas()) {
/*  33 */         updateSchema(conn, schemaName);
/*     */       }
/*  35 */     } catch (ClassNotFoundException|UnknownHostException|SQLException ex) {
/*  36 */       ex.printStackTrace();
/*  37 */       throw ex;
/*     */     } finally {
/*  39 */       if (conn != null) {
/*  40 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static void deleteProcedure(Connection conn, String procedureName) throws SQLException {
/*  47 */     PreparedStatement ps = null;
/*     */ 
/*     */     
/*     */     try {
/*  51 */       ps = conn.prepareStatement("DROP PROCEDURE ?");
/*  52 */       ps.setString(1, procedureName);
/*  53 */       ps.execute();
/*  54 */       ps.close();
/*     */     }
/*  56 */     catch (SQLException sQLException) {
/*     */ 
/*     */     
/*     */     } finally {
/*     */       
/*  61 */       if (ps != null) {
/*  62 */         ps.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static void updateSchema(Connection conn, String schema) throws SQLException, ClassNotFoundException, UnknownHostException {
/*  69 */     PreparedStatement ps1 = null;
/*  70 */     PreparedStatement ps2 = null;
/*  71 */     Statement st = null;
/*  72 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  75 */       if (schema.equalsIgnoreCase("PEGASUS")) {
/*  76 */         conn.setSchema("PEGASUS");
/*  77 */         st = conn.createStatement();
/*  78 */         DatabaseMetaData dmd = conn.getMetaData();
/*  79 */         List<String> columns = new ArrayList<>();
/*     */ 
/*     */         
/*  82 */         rs = dmd.getColumns(null, schema, "SIGNAL_LEVEL", "%");
/*  83 */         while (rs.next()) {
/*  84 */           columns.add(rs.getString(4));
/*     */         }
/*  86 */         rs.close();
/*     */         
/*  88 */         if (!columns.contains("LAST_COMM_INTERFACE")) {
/*  89 */           st.addBatch("ALTER TABLE SIGNAL_LEVEL ADD COLUMN LAST_COMM_INTERFACE SMALLINT DEFAULT -1");
/*     */         }
/*     */ 
/*     */         
/*  93 */         columns = new ArrayList<>();
/*  94 */         rs = dmd.getColumns(null, schema, "RECEIVED_COMM", "%");
/*  95 */         while (rs.next()) {
/*  96 */           columns.add(rs.getString(4));
/*     */         }
/*  98 */         rs.close();
/*     */         
/* 100 */         if (!columns.contains("LAST_COMM_INTERFACE")) {
/* 101 */           st.addBatch("ALTER TABLE RECEIVED_COMM ADD COLUMN LAST_COMM_INTERFACE SMALLINT DEFAULT -1");
/*     */         }
/*     */ 
/*     */         
/* 105 */         columns = new ArrayList<>();
/* 106 */         rs = dmd.getColumns(null, schema, "DEVICE_CONNECTION", "%");
/* 107 */         while (rs.next()) {
/* 108 */           columns.add(rs.getString(4));
/*     */         }
/* 110 */         rs.close();
/*     */         
/* 112 */         if (!columns.contains("LAST_COMM_INTERFACE")) {
/* 113 */           st.addBatch("ALTER TABLE DEVICE_CONNECTION ADD COLUMN LAST_COMM_INTERFACE SMALLINT DEFAULT -1");
/*     */         }
/*     */ 
/*     */         
/* 117 */         columns = new ArrayList<>();
/* 118 */         rs = dmd.getColumns(null, schema, "MODULE", "%");
/* 119 */         String wifiFWDT = null;
/* 120 */         while (rs.next()) {
/* 121 */           String colName = rs.getString(4);
/* 122 */           columns.add(colName);
/* 123 */           if (colName.equals("WIFI_FIRMWARE")) {
/* 124 */             wifiFWDT = rs.getString("TYPE_NAME");
/*     */           }
/*     */         } 
/* 127 */         rs.close();
/*     */         
/* 129 */         if (!columns.contains("IS_RED")) {
/* 130 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN IS_RED SMALLINT DEFAULT -1");
/*     */         }
/* 132 */         if (!columns.contains("TIMEZONE")) {
/* 133 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN TIMEZONE SMALLINT DEFAULT 15");
/*     */         }
/* 135 */         if (!columns.contains("SYSTEM_STATUS")) {
/* 136 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN SYSTEM_STATUS SMALLINT DEFAULT -1");
/*     */         }
/* 138 */         if (!columns.contains("KEYFOB_LOW_BATTERY")) {
/* 139 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN KEYFOB_LOW_BATTERY INTEGER DEFAULT 0");
/*     */         }
/* 141 */         if (!columns.contains("LAST_ALIVE_PACKET")) {
/* 142 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN LAST_ALIVE_PACKET TIMESTAMP");
/*     */         }
/* 144 */         if (!columns.contains("SYSTEM_IN_ALARM")) {
/* 145 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN SYSTEM_IN_ALARM SMALLINT DEFAULT -1");
/*     */         }
/* 147 */         if (!columns.contains("COMM_LOG_ENABLED_DATE")) {
/* 148 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN COMM_LOG_ENABLED_DATE TIMESTAMP");
/*     */         }
/* 150 */         if (!columns.contains("COMM_LOG")) {
/* 151 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN COMM_LOG SMALLINT DEFAULT -1");
/*     */         }
/* 153 */         if (!columns.contains("IPIC")) {
/* 154 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN IPIC SMALLINT DEFAULT 0");
/*     */         }
/* 156 */         if (!columns.contains("GSM_DATA_CARRIER")) {
/* 157 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN GSM_DATA_CARRIER SMALLINT DEFAULT -1");
/*     */         }
/* 159 */         if (!columns.contains("BATTERY_STATUS")) {
/* 160 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN BATTERY_STATUS SMALLINT DEFAULT -1");
/*     */         }
/* 162 */         if (!columns.contains("MODEL")) {
/* 163 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN MODEL SMALLINT DEFAULT -1");
/*     */         }
/* 165 */         if (!columns.contains("WIFI_COMM_TEST_STATUS_AP_1")) {
/* 166 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN WIFI_COMM_TEST_STATUS_AP_1 SMALLINT DEFAULT -1");
/*     */         }
/* 168 */         if (!columns.contains("WIFI_COMM_TEST_STATUS_AP_2")) {
/* 169 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN WIFI_COMM_TEST_STATUS_AP_2 SMALLINT DEFAULT -1");
/*     */         }
/* 171 */         if (!columns.contains("LONGITUDE")) {
/* 172 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN LONGITUDE FLOAT DEFAULT -1");
/*     */         }
/* 174 */         if (!columns.contains("LATTITUDE")) {
/* 175 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN LATTITUDE FLOAT DEFAULT -1");
/*     */         }
/* 177 */         if (!columns.contains("ALTITUDE")) {
/* 178 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN ALTITUDE FLOAT DEFAULT -1");
/*     */         }
/* 180 */         if (!columns.contains("BATTERY_CONNECTED_NOT_CONFIGURED_STATUS")) {
/* 181 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN BATTERY_CONNECTED_NOT_CONFIGURED_STATUS SMALLINT DEFAULT -1");
/*     */         }
/* 183 */         if (columns.contains("WIFI_COMM_TEST_STATUS")) {
/* 184 */           st.addBatch("ALTER TABLE MODULE DROP COLUMN WIFI_COMM_TEST_STATUS");
/*     */         }
/* 186 */         if (wifiFWDT != null && !wifiFWDT.equals("VARCHAR")) {
/* 187 */           st.addBatch("ALTER TABLE MODULE DROP COLUMN WIFI_FIRMWARE");
/* 188 */           st.addBatch("ALTER TABLE MODULE ADD COLUMN WIFI_FIRMWARE VARCHAR(12)");
/*     */         } 
/*     */ 
/*     */         
/* 192 */         columns = new ArrayList<>();
/* 193 */         rs = dmd.getColumns(null, schema, "SIGNAL_LEVEL", "%");
/* 194 */         while (rs.next()) {
/* 195 */           columns.add(rs.getString(4));
/*     */         }
/* 197 */         rs.close();
/*     */         
/* 199 */         if (!columns.contains("SIGNAL_GSM_CARRIER")) {
/* 200 */           st.addBatch("ALTER TABLE SIGNAL_LEVEL ADD COLUMN SIGNAL_GSM_CARRIER SMALLINT DEFAULT -1");
/*     */         }
/*     */ 
/*     */         
/* 204 */         rs = dmd.getColumns(null, schema, "USERS", "%");
/* 205 */         while (rs.next()) {
/* 206 */           if (rs.getString(4).equals("PERMISSIONS") && 
/* 207 */             !rs.getString("TYPE_NAME").equals("VARCHAR")) {
/* 208 */             st.addBatch("ALTER TABLE USERS DROP COLUMN PERMISSIONS");
/* 209 */             st.addBatch("ALTER TABLE USERS ADD COLUMN PERMISSIONS VARCHAR(32)");
/* 210 */             st.addBatch("DROP PROCEDURE  SP_046");
/* 211 */             st.addBatch("CREATE PROCEDURE SP_046 ( IN  ID_CLIENT  INTEGER , IN  NAME  VARCHAR (64) , IN  USERID  VARCHAR (32) , IN  PASSW  VARCHAR (32) , IN  TIMEZONE  VARCHAR (32) , IN  DATEFORMAT  INTEGER , IN  LANGUAGE  VARCHAR (12) , IN  PERMISSIONS  VARCHAR (32) , OUT  NAMEEXISTS  INTEGER , OUT  USEREXISTS  INTEGER ) PARAMETER STYLE JAVA  LANGUAGE JAVA DYNAMIC RESULT SETS 0 EXTERNAL NAME 'com.zeuscc.pegasus.derby.sp.SP_40_50.sp_046'");
/* 212 */             st.addBatch("DROP PROCEDURE  SP_047");
/* 213 */             st.addBatch("CREATE PROCEDURE SP_047 ( IN  ID_CLIENT  INTEGER , IN  ID_USER  INTEGER , IN  NAME  VARCHAR (64) , IN  USERID  VARCHAR (32) , IN  CURRENT_PASSWORD  VARCHAR (32) , IN  TIMEZONE  VARCHAR (32) , IN  DATEFORMAT  INTEGER , IN  LANGUAGE  VARCHAR (12) , IN  PERMISSIONS  VARCHAR (32) , OUT  NAMEEXISTS  INTEGER , OUT  USEREXISTS  INTEGER ) PARAMETER STYLE JAVA  LANGUAGE JAVA DYNAMIC RESULT SETS 0 EXTERNAL NAME 'com.zeuscc.pegasus.derby.sp.SP_40_50.sp_047'");
/* 214 */             st.addBatch("DROP PROCEDURE  SP_055");
/* 215 */             st.addBatch("CREATE PROCEDURE SP_055 ( IN  USERID  VARCHAR (32) , IN  PASSW  VARCHAR (32) , OUT  ICLIENT  INTEGER , OUT  IGROUP  INTEGER , OUT  CTYPE  INTEGER , OUT  PERMISSIONS  VARCHAR (32) , OUT  ENABLED  INTEGER , OUT  DATEFORMAT  INTEGER , OUT  TIMEZONE  VARCHAR (32) , OUT  LANGUAGE  VARCHAR (12) , OUT  USERNAME  VARCHAR (64) , OUT  IDUSER  INTEGER ) PARAMETER STYLE JAVA  LANGUAGE JAVA DYNAMIC RESULT SETS 0 EXTERNAL NAME 'com.zeuscc.pegasus.derby.sp.SP_50_63.sp_055'");
/*     */           } 
/*     */         } 
/*     */         
/* 219 */         rs.close();
/*     */ 
/*     */         
/* 222 */         columns = new ArrayList<>();
/* 223 */         rs = dmd.getColumns(null, schema, "COMMAND", "%");
/* 224 */         while (rs.next()) {
/* 225 */           columns.add(rs.getString(4));
/*     */         }
/* 227 */         rs.close();
/*     */         
/* 229 */         if (!columns.contains("CANCELLED")) {
/* 230 */           st.addBatch("ALTER TABLE COMMAND ADD COLUMN CANCELLED TIMESTAMP ");
/*     */         }
/* 232 */         if (!columns.contains("CANCELLED_USER")) {
/* 233 */           st.addBatch("ALTER TABLE COMMAND ADD COLUMN CANCELLED_USER VARCHAR(32)");
/*     */         }
/* 235 */         if (!columns.contains("IN_PROGRESS")) {
/* 236 */           st.addBatch("ALTER TABLE COMMAND ADD COLUMN IN_PROGRESS SMALLINT DEFAULT 0");
/*     */         }
/*     */ 
/*     */         
/* 240 */         columns = new ArrayList<>();
/* 241 */         rs = dmd.getColumns(null, schema, "DEFAULT_REGISTRATION", "%");
/* 242 */         while (rs.next()) {
/* 243 */           columns.add(rs.getString(4));
/*     */         }
/* 245 */         rs.close();
/*     */         
/* 247 */         if (!columns.contains("E_HW_SIM_1_FAIL")) {
/* 248 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_HW_SIM_1_FAIL VARCHAR(8)");
/*     */         }
/* 250 */         if (!columns.contains("F_HW_SIM_1_FAIL")) {
/* 251 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_HW_SIM_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 253 */         if (!columns.contains("E_HW_SIM_2_FAIL")) {
/* 254 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_HW_SIM_2_FAIL VARCHAR(8)");
/*     */         }
/* 256 */         if (!columns.contains("F_HW_SIM_2_FAIL")) {
/* 257 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_HW_SIM_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 259 */         if (!columns.contains("E_HW_GSM_MODEM_FAIL")) {
/* 260 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_HW_GSM_MODEM_FAIL VARCHAR(8)");
/*     */         }
/* 262 */         if (!columns.contains("F_HW_GSM_MODEM_FAIL")) {
/* 263 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_HW_GSM_MODEM_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 265 */         if (!columns.contains("E_HW_ETH_FAIL")) {
/* 266 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_HW_ETH_FAIL VARCHAR(8)");
/*     */         }
/* 268 */         if (!columns.contains("F_HW_ETH_FAIL")) {
/* 269 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_HW_ETH_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 271 */         if (!columns.contains("E_HW_WIFI_FAIL")) {
/* 272 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_HW_WIFI_FAIL VARCHAR(8)");
/*     */         }
/* 274 */         if (!columns.contains("F_HW_WIFI_FAIL")) {
/* 275 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_HW_WIFI_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 277 */         if (!columns.contains("E_HW_KEELOQ_FAIL")) {
/* 278 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_HW_KEELOQ_FAIL VARCHAR(8)");
/*     */         }
/* 280 */         if (!columns.contains("F_HW_KEELOQ_FAIL")) {
/* 281 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_HW_KEELOQ_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 283 */         if (!columns.contains("E_GPRS_COMM_TEST_1_1_FAIL")) {
/* 284 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_GPRS_COMM_TEST_1_1_FAIL VARCHAR(8)");
/*     */         }
/* 286 */         if (!columns.contains("F_GPRS_COMM_TEST_1_1_FAIL")) {
/* 287 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_GPRS_COMM_TEST_1_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 289 */         if (!columns.contains("E_GPRS_COMM_TEST_1_2_FAIL")) {
/* 290 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_GPRS_COMM_TEST_1_2_FAIL VARCHAR(8)");
/*     */         }
/* 292 */         if (!columns.contains("F_GPRS_COMM_TEST_1_2_FAIL")) {
/* 293 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_GPRS_COMM_TEST_1_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 295 */         if (!columns.contains("E_GPRS_COMM_TEST_2_1_FAIL")) {
/* 296 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_GPRS_COMM_TEST_2_1_FAIL VARCHAR(8)");
/*     */         }
/* 298 */         if (!columns.contains("F_GPRS_COMM_TEST_2_1_FAIL")) {
/* 299 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_GPRS_COMM_TEST_2_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 301 */         if (!columns.contains("E_GPRS_COMM_TEST_2_2_FAIL")) {
/* 302 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_GPRS_COMM_TEST_2_2_FAIL VARCHAR(8)");
/*     */         }
/* 304 */         if (!columns.contains("F_GPRS_COMM_TEST_2_2_FAIL")) {
/* 305 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_GPRS_COMM_TEST_2_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 307 */         if (!columns.contains("E_WIFI_COMM_TEST_AP_1_FAIL")) {
/* 308 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_WIFI_COMM_TEST_AP_1_FAIL VARCHAR(8)");
/*     */         }
/* 310 */         if (!columns.contains("F_WIFI_COMM_TEST_AP_1_FAIL")) {
/* 311 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_WIFI_COMM_TEST_AP_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 313 */         if (!columns.contains("E_WIFI_COMM_TEST_AP_2_FAIL")) {
/* 314 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_WIFI_COMM_TEST_AP_2_FAIL VARCHAR(8)");
/*     */         }
/* 316 */         if (!columns.contains("F_WIFI_COMM_TEST_AP_2_FAIL")) {
/* 317 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_WIFI_COMM_TEST_AP_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 319 */         if (!columns.contains("E_SYSTEM_AWAY_ARM")) {
/* 320 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_SYSTEM_AWAY_ARM VARCHAR(8)");
/*     */         }
/* 322 */         if (!columns.contains("F_SYSTEM_AWAY_ARM")) {
/* 323 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_SYSTEM_AWAY_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 325 */         if (!columns.contains("E_SYSTEM_FORCE_ARM")) {
/* 326 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_SYSTEM_FORCE_ARM VARCHAR(8)");
/*     */         }
/* 328 */         if (!columns.contains("F_SYSTEM_FORCE_ARM")) {
/* 329 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_SYSTEM_FORCE_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 331 */         if (!columns.contains("E_SYSTEM_STAY_ARM")) {
/* 332 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_SYSTEM_STAY_ARM VARCHAR(8)");
/*     */         }
/* 334 */         if (!columns.contains("F_SYSTEM_STAY_ARM")) {
/* 335 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_SYSTEM_STAY_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 337 */         if (!columns.contains("E_SYSTEM_FORCE_STAY_ARM")) {
/* 338 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_SYSTEM_FORCE_STAY_ARM VARCHAR(8)");
/*     */         }
/* 340 */         if (!columns.contains("F_SYSTEM_FORCE_STAY_ARM")) {
/* 341 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_SYSTEM_FORCE_STAY_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 343 */         if (!columns.contains("E_ZONE_BYPASS_UNBYPASS")) {
/* 344 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_ZONE_BYPASS_UNBYPASS VARCHAR(8)");
/*     */         }
/* 346 */         if (!columns.contains("F_ZONE_BYPASS_UNBYPASS")) {
/* 347 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_ZONE_BYPASS_UNBYPASS SMALLINT DEFAULT 0");
/*     */         }
/* 349 */         if (!columns.contains("E_ZONE_FORCE_ARM_DISARM")) {
/* 350 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_ZONE_FORCE_ARM_DISARM VARCHAR(8)");
/*     */         }
/* 352 */         if (!columns.contains("F_ZONE_FORCE_ARM_DISARM")) {
/* 353 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_ZONE_FORCE_ARM_DISARM SMALLINT DEFAULT 0");
/*     */         }
/* 355 */         if (!columns.contains("E_ZONE_WIRE_FAULT")) {
/* 356 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_ZONE_WIRE_FAULT VARCHAR(8)");
/*     */         }
/* 358 */         if (!columns.contains("F_ZONE_WIRE_FAULT")) {
/* 359 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_ZONE_WIRE_FAULT SMALLINT DEFAULT 0");
/*     */         }
/* 361 */         if (!columns.contains("E_ZONE_TAMPER")) {
/* 362 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_ZONE_TAMPER VARCHAR(8)");
/*     */         }
/* 364 */         if (!columns.contains("F_ZONE_TAMPER")) {
/* 365 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_ZONE_TAMPER SMALLINT DEFAULT 0");
/*     */         }
/* 367 */         if (!columns.contains("E_KEYFOB_PANIC")) {
/* 368 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_KEYFOB_PANIC VARCHAR(8)");
/*     */         }
/* 370 */         if (!columns.contains("F_KEYFOB_PANIC")) {
/* 371 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_KEYFOB_PANIC SMALLINT DEFAULT 0");
/*     */         }
/* 373 */         if (!columns.contains("E_KEYFOB_LOW_BATTERY")) {
/* 374 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_KEYFOB_LOW_BATTERY VARCHAR(8)");
/*     */         }
/* 376 */         if (!columns.contains("F_KEYFOB_LOW_BATTERY")) {
/* 377 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_KEYFOB_LOW_BATTERY SMALLINT DEFAULT 0");
/*     */         }
/* 379 */         if (!columns.contains("E_KEYFOB_COMM_TEST")) {
/* 380 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_KEYFOB_COMM_TEST VARCHAR(8)");
/*     */         }
/* 382 */         if (!columns.contains("F_KEYFOB_COMM_TEST")) {
/* 383 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_KEYFOB_COMM_TEST SMALLINT DEFAULT 0");
/*     */         }
/* 385 */         if (!columns.contains("E_IPIC")) {
/* 386 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_IPIC VARCHAR(8)");
/*     */         }
/* 388 */         if (!columns.contains("F_IPIC")) {
/* 389 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_IPIC SMALLINT DEFAULT 0");
/*     */         }
/* 391 */         if (!columns.contains("E_BATT_DISCONNECT")) {
/* 392 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_BATT_DISCONNECT VARCHAR(8)");
/*     */         }
/* 394 */         if (!columns.contains("F_BATT_DISCONNECT")) {
/* 395 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_BATT_DISCONNECT SMALLINT DEFAULT 0");
/*     */         }
/* 397 */         if (!columns.contains("E_BATTERY_OVER_TEMP")) {
/* 398 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_BATTERY_OVER_TEMP VARCHAR(8)");
/*     */         }
/* 400 */         if (!columns.contains("F_BATTERY_OVER_TEMP")) {
/* 401 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_BATTERY_OVER_TEMP SMALLINT DEFAULT 0");
/*     */         }
/* 403 */         if (!columns.contains("E_BATTERY_OVERTIME_CHARGE")) {
/* 404 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_BATTERY_OVERTIME_CHARGE VARCHAR(8)");
/*     */         }
/* 406 */         if (!columns.contains("F_BATTERY_OVERTIME_CHARGE")) {
/* 407 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_BATTERY_OVERTIME_CHARGE SMALLINT DEFAULT 0");
/*     */         }
/* 409 */         if (!columns.contains("E_BATT_PRESENT_NOT_CONFIGED")) {
/* 410 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN E_BATT_PRESENT_NOT_CONFIGED VARCHAR(8)");
/*     */         }
/* 412 */         if (!columns.contains("F_BATT_PRESENT_NOT_CONFIGED")) {
/* 413 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION ADD COLUMN F_BATT_PRESENT_NOT_CONFIGED SMALLINT DEFAULT 0");
/*     */         }
/* 415 */         if (columns.contains("E_HARDWARE_FAIL")) {
/* 416 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_HARDWARE_FAIL");
/*     */         }
/* 418 */         if (columns.contains("F_HARDWARE_FAIL")) {
/* 419 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_HARDWARE_FAIL");
/*     */         }
/* 421 */         if (columns.contains("E_GPRS_COMM_TEST_FAIL")) {
/* 422 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_GPRS_COMM_TEST_FAIL");
/*     */         }
/* 424 */         if (columns.contains("F_GPRS_COMM_TEST_FAIL")) {
/* 425 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_GPRS_COMM_TEST_FAIL");
/*     */         }
/* 427 */         if (columns.contains("E_WIFI_COMM_TEST_FAIL")) {
/* 428 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_WIFI_COMM_TEST_FAIL");
/*     */         }
/* 430 */         if (columns.contains("F_WIFI_COMM_TEST_FAIL")) {
/* 431 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_WIFI_COMM_TEST_FAIL");
/*     */         }
/* 433 */         if (columns.contains("E_ZONE1_WIRE_FAULT")) {
/* 434 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE1_WIRE_FAULT");
/*     */         }
/* 436 */         if (columns.contains("F_ZONE1_WIRE_FAULT")) {
/* 437 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE1_WIRE_FAULT");
/*     */         }
/* 439 */         if (columns.contains("E_ZONE1_TAMPER")) {
/* 440 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE1_TAMPER");
/*     */         }
/* 442 */         if (columns.contains("F_ZONE1_TAMPER")) {
/* 443 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE1_TAMPER");
/*     */         }
/* 445 */         if (columns.contains("E_ZONE2_WIRE_FAULT")) {
/* 446 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE2_WIRE_FAULT");
/*     */         }
/* 448 */         if (columns.contains("F_ZONE2_WIRE_FAULT")) {
/* 449 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE2_WIRE_FAULT");
/*     */         }
/* 451 */         if (columns.contains("E_ZONE2_TAMPER")) {
/* 452 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE2_TAMPER");
/*     */         }
/* 454 */         if (columns.contains("F_ZONE2_TAMPER")) {
/* 455 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE2_TAMPER");
/*     */         }
/* 457 */         if (columns.contains("E_ZONE3_WIRE_FAULT")) {
/* 458 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE3_WIRE_FAULT");
/*     */         }
/* 460 */         if (columns.contains("F_ZONE3_WIRE_FAULT")) {
/* 461 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE3_WIRE_FAULT");
/*     */         }
/* 463 */         if (columns.contains("E_ZONE3_TAMPER")) {
/* 464 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE3_TAMPER");
/*     */         }
/* 466 */         if (columns.contains("F_ZONE3_TAMPER")) {
/* 467 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE3_TAMPER");
/*     */         }
/* 469 */         if (columns.contains("E_ZONE4_WIRE_FAULT")) {
/* 470 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE4_WIRE_FAULT");
/*     */         }
/* 472 */         if (columns.contains("F_ZONE4_WIRE_FAULT")) {
/* 473 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE4_WIRE_FAULT");
/*     */         }
/* 475 */         if (columns.contains("E_ZONE4_TAMPER")) {
/* 476 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN E_ZONE4_TAMPER");
/*     */         }
/* 478 */         if (columns.contains("F_ZONE4_TAMPER")) {
/* 479 */           st.addBatch("ALTER TABLE DEFAULT_REGISTRATION DROP COLUMN F_ZONE4_TAMPER");
/*     */         }
/*     */ 
/*     */         
/* 483 */         columns = new ArrayList<>();
/* 484 */         rs = dmd.getColumns(null, schema, "CLIENT", "%");
/* 485 */         while (rs.next()) {
/* 486 */           columns.add(rs.getString(4));
/*     */         }
/* 488 */         rs.close();
/*     */         
/* 490 */         if (!columns.contains("E_HW_SIM_1_FAIL")) {
/* 491 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_HW_SIM_1_FAIL VARCHAR(8)");
/*     */         }
/* 493 */         if (!columns.contains("F_HW_SIM_1_FAIL")) {
/* 494 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_HW_SIM_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 496 */         if (!columns.contains("E_HW_SIM_2_FAIL")) {
/* 497 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_HW_SIM_2_FAIL VARCHAR(8)");
/*     */         }
/* 499 */         if (!columns.contains("F_HW_SIM_2_FAIL")) {
/* 500 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_HW_SIM_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 502 */         if (!columns.contains("E_HW_GSM_MODEM_FAIL")) {
/* 503 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_HW_GSM_MODEM_FAIL VARCHAR(8)");
/*     */         }
/* 505 */         if (!columns.contains("F_HW_GSM_MODEM_FAIL")) {
/* 506 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_HW_GSM_MODEM_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 508 */         if (!columns.contains("E_HW_ETH_FAIL")) {
/* 509 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_HW_ETH_FAIL VARCHAR(8)");
/*     */         }
/* 511 */         if (!columns.contains("F_HW_ETH_FAIL")) {
/* 512 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_HW_ETH_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 514 */         if (!columns.contains("E_HW_WIFI_FAIL")) {
/* 515 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_HW_WIFI_FAIL VARCHAR(8)");
/*     */         }
/* 517 */         if (!columns.contains("F_HW_WIFI_FAIL")) {
/* 518 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_HW_WIFI_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 520 */         if (!columns.contains("E_HW_KEELOQ_FAIL")) {
/* 521 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_HW_KEELOQ_FAIL VARCHAR(8)");
/*     */         }
/* 523 */         if (!columns.contains("F_HW_KEELOQ_FAIL")) {
/* 524 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_HW_KEELOQ_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 526 */         if (!columns.contains("E_GPRS_COMM_TEST_1_1_FAIL")) {
/* 527 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_GPRS_COMM_TEST_1_1_FAIL VARCHAR(8)");
/*     */         }
/* 529 */         if (!columns.contains("F_GPRS_COMM_TEST_1_1_FAIL")) {
/* 530 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_GPRS_COMM_TEST_1_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 532 */         if (!columns.contains("E_GPRS_COMM_TEST_1_2_FAIL")) {
/* 533 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_GPRS_COMM_TEST_1_2_FAIL VARCHAR(8)");
/*     */         }
/* 535 */         if (!columns.contains("F_GPRS_COMM_TEST_1_2_FAIL")) {
/* 536 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_GPRS_COMM_TEST_1_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 538 */         if (!columns.contains("E_GPRS_COMM_TEST_2_1_FAIL")) {
/* 539 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_GPRS_COMM_TEST_2_1_FAIL VARCHAR(8)");
/*     */         }
/* 541 */         if (!columns.contains("F_GPRS_COMM_TEST_2_1_FAIL")) {
/* 542 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_GPRS_COMM_TEST_2_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 544 */         if (!columns.contains("E_GPRS_COMM_TEST_2_2_FAIL")) {
/* 545 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_GPRS_COMM_TEST_2_2_FAIL VARCHAR(8)");
/*     */         }
/* 547 */         if (!columns.contains("F_GPRS_COMM_TEST_2_2_FAIL")) {
/* 548 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_GPRS_COMM_TEST_2_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 550 */         if (!columns.contains("E_WIFI_COMM_TEST_AP_1_FAIL")) {
/* 551 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_WIFI_COMM_TEST_AP_1_FAIL VARCHAR(8)");
/*     */         }
/* 553 */         if (!columns.contains("F_WIFI_COMM_TEST_AP_1_FAIL")) {
/* 554 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_WIFI_COMM_TEST_AP_1_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 556 */         if (!columns.contains("E_WIFI_COMM_TEST_AP_2_FAIL")) {
/* 557 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_WIFI_COMM_TEST_AP_2_FAIL VARCHAR(8)");
/*     */         }
/* 559 */         if (!columns.contains("F_WIFI_COMM_TEST_AP_2_FAIL")) {
/* 560 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_WIFI_COMM_TEST_AP_2_FAIL SMALLINT DEFAULT 0");
/*     */         }
/* 562 */         if (!columns.contains("E_SYSTEM_AWAY_ARM")) {
/* 563 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_SYSTEM_AWAY_ARM VARCHAR(8)");
/*     */         }
/* 565 */         if (!columns.contains("F_SYSTEM_AWAY_ARM")) {
/* 566 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_SYSTEM_AWAY_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 568 */         if (!columns.contains("E_SYSTEM_FORCE_ARM")) {
/* 569 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_SYSTEM_FORCE_ARM VARCHAR(8)");
/*     */         }
/* 571 */         if (!columns.contains("F_SYSTEM_FORCE_ARM")) {
/* 572 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_SYSTEM_FORCE_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 574 */         if (!columns.contains("E_SYSTEM_STAY_ARM")) {
/* 575 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_SYSTEM_STAY_ARM VARCHAR(8)");
/*     */         }
/* 577 */         if (!columns.contains("F_SYSTEM_STAY_ARM")) {
/* 578 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_SYSTEM_STAY_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 580 */         if (!columns.contains("E_SYSTEM_FORCE_STAY_ARM")) {
/* 581 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_SYSTEM_FORCE_STAY_ARM VARCHAR(8)");
/*     */         }
/* 583 */         if (!columns.contains("F_SYSTEM_FORCE_STAY_ARM")) {
/* 584 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_SYSTEM_FORCE_STAY_ARM SMALLINT DEFAULT 0");
/*     */         }
/* 586 */         if (!columns.contains("E_ZONE_BYPASS_UNBYPASS")) {
/* 587 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_ZONE_BYPASS_UNBYPASS VARCHAR(8)");
/*     */         }
/* 589 */         if (!columns.contains("F_ZONE_BYPASS_UNBYPASS")) {
/* 590 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_ZONE_BYPASS_UNBYPASS SMALLINT DEFAULT 0");
/*     */         }
/* 592 */         if (!columns.contains("E_ZONE_FORCE_ARM_DISARM")) {
/* 593 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_ZONE_FORCE_ARM_DISARM VARCHAR(8)");
/*     */         }
/* 595 */         if (!columns.contains("F_ZONE_FORCE_ARM_DISARM")) {
/* 596 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_ZONE_FORCE_ARM_DISARM SMALLINT DEFAULT 0");
/*     */         }
/* 598 */         if (!columns.contains("E_ZONE_WIRE_FAULT")) {
/* 599 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_ZONE_WIRE_FAULT VARCHAR(8)");
/*     */         }
/* 601 */         if (!columns.contains("F_ZONE_WIRE_FAULT")) {
/* 602 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_ZONE_WIRE_FAULT SMALLINT DEFAULT 0");
/*     */         }
/* 604 */         if (!columns.contains("E_ZONE_TAMPER")) {
/* 605 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_ZONE_TAMPER VARCHAR(8)");
/*     */         }
/* 607 */         if (!columns.contains("F_ZONE_TAMPER")) {
/* 608 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_ZONE_TAMPER SMALLINT DEFAULT 0");
/*     */         }
/* 610 */         if (!columns.contains("E_KEYFOB_PANIC")) {
/* 611 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_KEYFOB_PANIC VARCHAR(8)");
/*     */         }
/* 613 */         if (!columns.contains("F_KEYFOB_PANIC")) {
/* 614 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_KEYFOB_PANIC SMALLINT DEFAULT 0");
/*     */         }
/* 616 */         if (!columns.contains("E_KEYFOB_LOW_BATTERY")) {
/* 617 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_KEYFOB_LOW_BATTERY VARCHAR(8)");
/*     */         }
/* 619 */         if (!columns.contains("F_KEYFOB_LOW_BATTERY")) {
/* 620 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_KEYFOB_LOW_BATTERY SMALLINT DEFAULT 0");
/*     */         }
/* 622 */         if (!columns.contains("E_KEYFOB_COMM_TEST")) {
/* 623 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_KEYFOB_COMM_TEST VARCHAR(8)");
/*     */         }
/* 625 */         if (!columns.contains("F_KEYFOB_COMM_TEST")) {
/* 626 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_KEYFOB_COMM_TEST SMALLINT DEFAULT 0");
/*     */         }
/* 628 */         if (!columns.contains("E_IPIC")) {
/* 629 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_IPIC VARCHAR(8)");
/*     */         }
/* 631 */         if (!columns.contains("F_IPIC")) {
/* 632 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_IPIC SMALLINT DEFAULT 0");
/*     */         }
/* 634 */         if (!columns.contains("E_BATT_DISCONNECT")) {
/* 635 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_BATT_DISCONNECT VARCHAR(8)");
/*     */         }
/* 637 */         if (!columns.contains("F_BATT_DISCONNECT")) {
/* 638 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_BATT_DISCONNECT SMALLINT DEFAULT 0");
/*     */         }
/* 640 */         if (!columns.contains("E_BATTERY_OVER_TEMP")) {
/* 641 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_BATTERY_OVER_TEMP VARCHAR(8)");
/*     */         }
/* 643 */         if (!columns.contains("F_BATTERY_OVER_TEMP")) {
/* 644 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_BATTERY_OVER_TEMP SMALLINT DEFAULT 0");
/*     */         }
/* 646 */         if (!columns.contains("E_BATTERY_OVERTIME_CHARGE")) {
/* 647 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_BATTERY_OVERTIME_CHARGE VARCHAR(8)");
/*     */         }
/* 649 */         if (!columns.contains("F_BATTERY_OVERTIME_CHARGE")) {
/* 650 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_BATTERY_OVERTIME_CHARGE SMALLINT DEFAULT 0");
/*     */         }
/* 652 */         if (!columns.contains("E_BATT_PRESENT_NOT_CONFIGED")) {
/* 653 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN E_BATT_PRESENT_NOT_CONFIGED VARCHAR(8)");
/*     */         }
/* 655 */         if (!columns.contains("F_BATT_PRESENT_NOT_CONFIGED")) {
/* 656 */           st.addBatch("ALTER TABLE CLIENT ADD COLUMN F_BATT_PRESENT_NOT_CONFIGED SMALLINT DEFAULT 0");
/*     */         }
/* 658 */         if (columns.contains("E_HARDWARE_FAIL")) {
/* 659 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_HARDWARE_FAIL");
/*     */         }
/* 661 */         if (columns.contains("F_HARDWARE_FAIL")) {
/* 662 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_HARDWARE_FAIL");
/*     */         }
/* 664 */         if (columns.contains("E_GPRS_COMM_TEST_FAIL")) {
/* 665 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_GPRS_COMM_TEST_FAIL");
/*     */         }
/* 667 */         if (columns.contains("F_GPRS_COMM_TEST_FAIL")) {
/* 668 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_GPRS_COMM_TEST_FAIL");
/*     */         }
/* 670 */         if (columns.contains("E_WIFI_COMM_TEST_FAIL")) {
/* 671 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_WIFI_COMM_TEST_FAIL");
/*     */         }
/* 673 */         if (columns.contains("F_WIFI_COMM_TEST_FAIL")) {
/* 674 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_WIFI_COMM_TEST_FAIL");
/*     */         }
/* 676 */         if (columns.contains("E_ZONE1_WIRE_FAULT")) {
/* 677 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE1_WIRE_FAULT");
/*     */         }
/* 679 */         if (columns.contains("F_ZONE1_WIRE_FAULT")) {
/* 680 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE1_WIRE_FAULT");
/*     */         }
/* 682 */         if (columns.contains("E_ZONE1_TAMPER")) {
/* 683 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE1_TAMPER");
/*     */         }
/* 685 */         if (columns.contains("F_ZONE1_TAMPER")) {
/* 686 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE1_TAMPER");
/*     */         }
/* 688 */         if (columns.contains("E_ZONE2_WIRE_FAULT")) {
/* 689 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE2_WIRE_FAULT");
/*     */         }
/* 691 */         if (columns.contains("F_ZONE2_WIRE_FAULT")) {
/* 692 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE2_WIRE_FAULT");
/*     */         }
/* 694 */         if (columns.contains("E_ZONE2_TAMPER")) {
/* 695 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE2_TAMPER");
/*     */         }
/* 697 */         if (columns.contains("F_ZONE2_TAMPER")) {
/* 698 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE2_TAMPER");
/*     */         }
/* 700 */         if (columns.contains("E_ZONE3_WIRE_FAULT")) {
/* 701 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE3_WIRE_FAULT");
/*     */         }
/* 703 */         if (columns.contains("F_ZONE3_WIRE_FAULT")) {
/* 704 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE3_WIRE_FAULT");
/*     */         }
/* 706 */         if (columns.contains("E_ZONE3_TAMPER")) {
/* 707 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE3_TAMPER");
/*     */         }
/* 709 */         if (columns.contains("F_ZONE3_TAMPER")) {
/* 710 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE3_TAMPER");
/*     */         }
/* 712 */         if (columns.contains("E_ZONE4_WIRE_FAULT")) {
/* 713 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE4_WIRE_FAULT");
/*     */         }
/* 715 */         if (columns.contains("F_ZONE4_WIRE_FAULT")) {
/* 716 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE4_WIRE_FAULT");
/*     */         }
/* 718 */         if (columns.contains("E_ZONE4_TAMPER")) {
/* 719 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN E_ZONE4_TAMPER");
/*     */         }
/* 721 */         if (columns.contains("F_ZONE4_TAMPER")) {
/* 722 */           st.addBatch("ALTER TABLE CLIENT DROP COLUMN F_ZONE4_TAMPER");
/*     */         }
/*     */ 
/*     */         
/* 726 */         st.addBatch("DROP PROCEDURE SP_013");
/* 727 */         st.addBatch("CREATE PROCEDURE SP_013 (OUT NUM_PENDING_EVENTS INTEGER, OUT NUM_PENDING_ALIVES INTEGER, OUT NUM_REGISTERED_MODULES INTEGER, OUT DISABLED_MODULES INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   DYNAMIC RESULT SETS 0   EXTERNAL NAME 'com.zeuscc.pegasus.derby.sp.SP_11_20.sp_013'");
/*     */ 
/*     */         
/* 730 */         st.addBatch("DROP PROCEDURE SP_009");
/* 731 */         st.addBatch("CREATE PROCEDURE SP_009 (IN ID_CLIENT INTEGER, IN OCCURRENCE_TYPE INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   EXTERNAL NAME 'com.zeuscc.pegasus.derby.sp.SP_01_10.sp_009'");
/*     */ 
/*     */         
/* 734 */         st.addBatch("DROP PROCEDURE SP_017");
/* 735 */         st.addBatch("CREATE PROCEDURE SP_017 (IN ID_OCCURRENCE INTEGER, IN DELETE_OCCURRENCE INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   EXTERNAL NAME 'com.zeuscc.pegasus.derby.sp.SP_11_20.sp_017'");
/*     */ 
/*     */         
/* 738 */         st.addBatch("UPDATE USERS SET LANGUAGE='pt_BR' WHERE LANGUAGE='po_PO'");
/*     */         
/* 740 */         st.executeBatch();
/* 741 */         st.close();
/*     */         
/* 743 */         ps1 = conn.prepareStatement("SELECT ID_MODULE, PEGASUS_FW_VERSION, GSM_MODEM_FW_VERSION FROM MODULE");
/* 744 */         ps2 = conn.prepareStatement("UPDATE MODULE SET PEGASUS_FW_VERSION = ?, GSM_MODEM_FW_VERSION = ? WHERE ID_MODULE = ?");
/* 745 */         rs = ps1.executeQuery();
/* 746 */         while (rs.next()) {
/* 747 */           String version = rs.getString("PEGASUS_FW_VERSION");
/* 748 */           if (version != null && version.equals("-1")) {
/* 749 */             version = null;
/*     */           }
/* 751 */           if (version != null && !version.contains(".")) {
/* 752 */             ps2.setString(1, String.valueOf(Integer.parseInt(version) / 100.0F));
/*     */           } else {
/* 754 */             ps2.setString(1, version);
/*     */           } 
/*     */           
/* 757 */           version = rs.getString("GSM_MODEM_FW_VERSION");
/* 758 */           if (version != null && version.equals("-1")) {
/* 759 */             version = null;
/*     */           }
/* 761 */           if (version != null && !version.contains(".")) {
/* 762 */             ps2.setString(2, String.valueOf(Integer.parseInt(version) / 100.0F));
/*     */           } else {
/* 764 */             ps2.setString(2, version);
/*     */           } 
/* 766 */           ps2.setInt(3, rs.getInt("ID_MODULE"));
/* 767 */           ps2.addBatch();
/*     */         } 
/* 769 */         rs.close();
/* 770 */         ps1.close();
/* 771 */         ps2.executeBatch();
/* 772 */         ps2.close();
/*     */         
/* 774 */         ps1 = conn.prepareStatement("UPDATE EVENT SET RECEIVER_COMPORT = ? WHERE ((TRANSMITTED is NULL) AND (TRANSMISSION_CANCELLED = 0))");
/* 775 */         ps1.setString(1, "NONE");
/* 776 */         ps1.executeUpdate();
/* 777 */         ps1.close();
/*     */ 
/*     */ 
/*     */         
/* 781 */         deleteProcedure(conn, "SP_002");
/* 782 */         deleteProcedure(conn, "SP_012");
/* 783 */         deleteProcedure(conn, "SP_021");
/* 784 */         deleteProcedure(conn, "SP_032");
/* 785 */         deleteProcedure(conn, "SP_057");
/* 786 */         deleteProcedure(conn, "SP_060");
/*     */       
/*     */       }
/* 789 */       else if (schema.equalsIgnoreCase("GRIFFON")) {
/* 790 */         conn.setSchema("GRIFFON");
/* 791 */         st = conn.createStatement();
/* 792 */         DatabaseMetaData dmd = conn.getMetaData();
/* 793 */         List<String> columns = new ArrayList<>();
/*     */ 
/*     */         
/* 796 */         rs = dmd.getColumns(null, schema, "GRCP_USERS", "%");
/* 797 */         while (rs.next()) {
/* 798 */           columns.add(rs.getString(4));
/*     */         }
/* 800 */         rs.close();
/*     */         
/* 802 */         if (!columns.contains("ENABLED")) {
/* 803 */           st.addBatch("ALTER TABLE GRCP_USERS ADD COLUMN ENABLED SMALLINT DEFAULT 1");
/*     */         }
/*     */ 
/*     */         
/* 807 */         st.addBatch("DROP PROCEDURE SP_013");
/* 808 */         st.addBatch("CREATE PROCEDURE SP_013 (OUT NUM_PENDING_EVENTS INTEGER, OUT NUM_PENDING_ALIVES INTEGER, OUT NUM_REGISTERED_MODULES INTEGER, OUT DISABLED_MODULES INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   DYNAMIC RESULT SETS 0   EXTERNAL NAME 'com.zeus.griffon.derby.sp.SP_11_20.sp_013'");
/*     */ 
/*     */         
/* 811 */         st.addBatch("DROP PROCEDURE SP_009");
/* 812 */         st.addBatch("CREATE PROCEDURE SP_009 (IN ID_CLIENT INTEGER, IN OCCURRENCE_TYPE INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   EXTERNAL NAME 'com.zeus.griffon.derby.sp.SP_01_10.sp_009'");
/*     */ 
/*     */         
/* 815 */         st.addBatch("DROP PROCEDURE SP_017");
/* 816 */         st.addBatch("CREATE PROCEDURE SP_017 (IN ID_OCCURRENCE INTEGER, IN DELETE_OCCURRENCE INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   EXTERNAL NAME 'com.zeus.griffon.derby.sp.SP_11_20.sp_017'");
/*     */ 
/*     */         
/* 819 */         st.addBatch("UPDATE GRCP_USERS SET LANGUAGE='pt_BR' WHERE LANGUAGE='po_PO'");
/*     */         
/* 821 */         st.executeBatch();
/* 822 */         st.close();
/*     */ 
/*     */         
/* 825 */         deleteProcedure(conn, "SP_057");
/*     */       
/*     */       }
/* 828 */       else if (schema.equalsIgnoreCase("MERCURIUS")) {
/* 829 */         conn.setSchema("MERCURIUS");
/* 830 */         st = conn.createStatement();
/*     */         
/* 832 */         DatabaseMetaData dmd = conn.getMetaData();
/* 833 */         List<String> columns = new ArrayList<>();
/* 834 */         rs = dmd.getColumns(null, schema, "AVL_MODULE", "%");
/* 835 */         while (rs.next()) {
/* 836 */           columns.add(rs.getString(4));
/*     */         }
/* 838 */         rs.close();
/*     */         
/* 840 */         if (!columns.contains("MIN_GSM_SIGNAL_LEVEL")) {
/* 841 */           st.addBatch("ALTER TABLE AVL_MODULE ADD COLUMN MIN_GSM_SIGNAL_LEVEL SMALLINT DEFAULT 0");
/*     */         }
/*     */ 
/*     */         
/* 845 */         st.addBatch("DROP PROCEDURE SP_013");
/* 846 */         st.addBatch("CREATE PROCEDURE SP_013 (OUT NUM_PENDING_EVENTS INTEGER, OUT NUM_PENDING_ALIVES INTEGER, OUT NUM_REGISTERED_MODULES INTEGER, OUT DISABLED_MODULES INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   DYNAMIC RESULT SETS 0   EXTERNAL NAME 'com.zeus.mercurius.derby.sp.SP_11_20.sp_013'");
/*     */ 
/*     */         
/* 849 */         st.addBatch("DROP PROCEDURE SP_009");
/* 850 */         st.addBatch("CREATE PROCEDURE SP_009 (IN ID_CLIENT INTEGER, IN OCCURRENCE_TYPE INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   EXTERNAL NAME 'com.zeus.mercurius.derby.sp.SP_01_10.sp_009'");
/*     */ 
/*     */         
/* 853 */         st.addBatch("DROP PROCEDURE SP_017");
/* 854 */         st.addBatch("CREATE PROCEDURE SP_017 (IN ID_OCCURRENCE INTEGER, IN DELETE_OCCURRENCE INTEGER)  PARAMETER STYLE JAVA   LANGUAGE JAVA   EXTERNAL NAME 'com.zeus.mercurius.derby.sp.SP_11_20.sp_017'");
/*     */ 
/*     */         
/* 857 */         st.addBatch("UPDATE AVL_USERS SET LANGUAGE='pt_BR' WHERE LANGUAGE='po_PO'");
/*     */         
/* 859 */         st.executeBatch();
/* 860 */         st.close();
/*     */       
/*     */       }
/* 863 */       else if (schema.equalsIgnoreCase("ZEUSSETTINGS")) {
/* 864 */         conn.setSchema("ZEUSSETTINGS");
/* 865 */         DatabaseMetaData dmd = conn.getMetaData();
/*     */         
/* 867 */         List<String> tables = new ArrayList<>();
/* 868 */         rs = dmd.getTables(null, schema, null, new String[] { "TABLE" });
/* 869 */         while (rs.next()) {
/* 870 */           tables.add(rs.getString("TABLE_NAME"));
/*     */         }
/* 872 */         rs.close();
/*     */         
/* 874 */         st = conn.createStatement();
/* 875 */         if (tables.size() <= 4) {
/* 876 */           st.addBatch("DROP INDEX  IDX_TB_PROP_NAME");
/* 877 */           st.addBatch("DROP INDEX  IDX_TB_RS_APPLICATION_ID");
/* 878 */           st.addBatch("DROP INDEX  IDX_TB_RS_USERID");
/* 879 */           st.addBatch("DROP INDEX  IDX_TB_USER_SESSION");
/* 880 */           st.addBatch("DROP TABLE  ZEUSBOX_EVENTS");
/* 881 */           st.addBatch("DROP TABLE  PROPERTIES");
/* 882 */           st.addBatch("DROP TABLE  REPORT_SETTINGS");
/* 883 */           st.addBatch("DROP TABLE  USER_SESSION");
/* 884 */           st.addBatch("DROP PROCEDURE  SP_064");
/* 885 */           st.addBatch("DROP PROCEDURE  SP_066");
/* 886 */           st.addBatch("DROP TYPE  LIST RESTRICT ");
/* 887 */           st.addBatch("DROP SCHEMA ZEUSSETTINGS RESTRICT ");
/* 888 */         } else if (!tables.contains("ZEUS_EVENTS")) {
/* 889 */           st.addBatch("CREATE TABLE ZEUS_EVENTS (ID_EVENT  INTEGER   GENERATED ALWAYS AS IDENTITY ( START WITH 1, INCREMENT BY 1), ID_MODULE  INTEGER   NOT NULL  ,RECEIVER_GROUP  INTEGER   NOT NULL  ,RECEIVER_COMPORT  VARCHAR  (120)  NOT NULL  ,TRANSMITTED  TIMESTAMP  ,RECEIVED  TIMESTAMP   NOT NULL  ,TRANSMISSION_RETRIES  SMALLINT   DEFAULT 0 NOT NULL  ,TRANSMISSION_CANCELLED  SMALLINT   DEFAULT 0 NOT NULL  ,EVENT_PROTOCOL  SMALLINT   NOT NULL  ,EVENT_DATA  BLOB  (8K)  NOT NULL  ,MONITORING  INTEGER   DEFAULT -1 NOT NULL  , CONSTRAINT PK_ZEUS_EVENTS PRIMARY KEY (ID_EVENT))");
/* 890 */           st.addBatch("CREATE PROCEDURE SP_S006 ( IN  ID_MODULE  INTEGER , IN  RECEIVER_GROUP  INTEGER , IN  RECEIVER_COMPORT  VARCHAR (120) , IN  PROTOCOL  INTEGER , IN  CLIENT_CODE  VARCHAR (4) , IN  EVENT_DATA  LONG VARCHAR FOR BIT DATA , IN  LAST_MODULE_PROTOCOL_RCVD  INTEGER , IN  LAST_NW_PROTOCOL_RCVD  VARCHAR (3) , IN  LAST_COMM_INTERFACE  INTEGER , IN  MONITORING  INTEGER ) PARAMETER STYLE JAVA  LANGUAGE JAVA EXTERNAL NAME 'com.zeus.settings.sp.SP_Settings.sp_S006'");
/* 891 */           st.addBatch("CREATE PROCEDURE SP_S007 ( IN  ID_EVENT  INTEGER ) PARAMETER STYLE JAVA  LANGUAGE JAVA EXTERNAL NAME 'com.zeus.settings.sp.SP_Settings.sp_S007'");
/*     */         } 
/*     */ 
/*     */         
/* 895 */         List<String> columns = new ArrayList<>();
/* 896 */         rs = dmd.getColumns(null, schema, "ZEUS_LIVE_DATA", "%");
/* 897 */         while (rs.next()) {
/* 898 */           columns.add(rs.getString(4));
/*     */         }
/* 900 */         rs.close();
/*     */         
/* 902 */         if (!columns.contains("PENDING_ALIVES")) {
/* 903 */           st.addBatch("ALTER TABLE ZEUS_LIVE_DATA ADD COLUMN PENDING_ALIVES INTEGER");
/*     */         }
/*     */ 
/*     */         
/* 907 */         st.addBatch("UPDATE USERS SET LANGUAGE='pt_BR' WHERE LANGUAGE='po_PO'");
/*     */         
/* 909 */         st.executeBatch();
/* 910 */         st.close();
/*     */       } 
/*     */     } finally {
/*     */       
/* 914 */       if (rs != null) {
/* 915 */         rs.close();
/*     */       }
/* 917 */       if (st != null) {
/* 918 */         st.close();
/*     */       }
/* 920 */       if (ps1 != null) {
/* 921 */         ps1.close();
/*     */       }
/* 923 */       if (ps2 != null)
/* 924 */         ps2.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBGeneral\DBUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */