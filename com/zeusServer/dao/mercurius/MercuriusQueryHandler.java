/*     */ package com.zeusServer.dao.mercurius;
/*     */ 
/*     */ import com.zeus.mercuriusAVL.derby.beans.AudioNJSFileInfo;
/*     */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.dto.GeofenceBean;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.mercurius.MercuriusAVLHandlerHelper;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
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
/*     */ public class MercuriusQueryHandler
/*     */ {
/*     */   public static MercuriusAVLModule executeSPM_001(MercuriusAVLModule gModule, Connection conn) throws SQLException {
/*  46 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  49 */       cst = conn.prepareCall("call SP_M001(?)");
/*  50 */       cst.registerOutParameter(1, 2000);
/*  51 */       cst.setObject(1, gModule);
/*  52 */       cst.execute();
/*  53 */       return (MercuriusAVLModule)cst.getObject(1);
/*     */     }
/*  55 */     catch (SQLException ex) {
/*  56 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  57 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/*  59 */       throw ex;
/*     */     } finally {
/*  61 */       if (cst != null) {
/*  62 */         cst.close();
/*     */       }
/*  64 */       if (conn != null) {
/*  65 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSPM_002(List<MercuriusAVLModule> deviceList, Connection conn) throws SQLException {
/*  72 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  75 */       cst = conn.prepareCall("call SP_M002(?)");
/*  76 */       for (MercuriusAVLModule avl : deviceList) {
/*  77 */         cst.setObject(1, avl);
/*  78 */         cst.addBatch();
/*     */       } 
/*  80 */       cst.executeBatch();
/*  81 */       cst.close();
/*     */     }
/*  83 */     catch (SQLException e) {
/*  84 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  85 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  87 */       throw e;
/*     */     } finally {
/*  89 */       if (cst != null) {
/*  90 */         cst.close();
/*     */       }
/*  92 */       if (conn != null) {
/*  93 */         conn.close();
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
/*     */   public static List<PendingDataHolder> getAllPendingAlive(Connection conn) throws SQLException {
/* 108 */     PreparedStatement ps = null;
/* 109 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 112 */       int limit = (GlobalVariables.currentPlatform == Enums.Platform.ARM) ? 100 : 250;
/* 113 */       List<PendingDataHolder> dataList = new ArrayList<>(limit);
/*     */       
/* 115 */       ps = conn.prepareStatement("SELECT ID_PENDING_DATA_FIELD, ID_CLIENT, ID_MODULE, TIMEZONE, RECEIVED, CONTENT, LAST_COMM_INTERFACE, MIN_NUMBER_SATILITES FROM AVL_PENDING_DATA_FIELDS ORDER BY RECEIVED ASC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
/* 116 */       ps.setInt(1, limit);
/* 117 */       rs = ps.executeQuery();
/* 118 */       while (rs.next()) {
/* 119 */         PendingDataHolder pdh = new PendingDataHolder();
/* 120 */         pdh.setIdClient(rs.getInt("ID_CLIENT"));
/* 121 */         pdh.setIdModule(rs.getInt("ID_MODULE"));
/* 122 */         pdh.setIdPendingAlive(rs.getInt("ID_PENDING_DATA_FIELD"));
/* 123 */         pdh.setReceived(rs.getTimestamp("RECEIVED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))));
/* 124 */         pdh.setContent(rs.getBytes("CONTENT"));
/* 125 */         pdh.setLastCommInterface(rs.getShort("LAST_COMM_INTERFACE"));
/* 126 */         pdh.setMinGsmSignalLevel(rs.getShort("MIN_NUMBER_SATILITES"));
/* 127 */         pdh.setTimezone(rs.getInt("TIMEZONE"));
/* 128 */         dataList.add(pdh);
/*     */       } 
/* 130 */       rs.close();
/* 131 */       ps.close();
/*     */       
/* 133 */       return dataList;
/*     */     }
/* 135 */     catch (SQLException e) {
/* 136 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 137 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 139 */       throw e;
/*     */     } finally {
/* 141 */       if (rs != null) {
/* 142 */         rs.close();
/*     */       }
/* 144 */       if (ps != null) {
/* 145 */         ps.close();
/*     */       }
/* 147 */       if (conn != null) {
/* 148 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<EventDataHolder> getNonProcessedEvents(Connection conn) throws SQLException {
/* 155 */     PreparedStatement ps = null;
/* 156 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 159 */       List<EventDataHolder> edhList = new ArrayList<>();
/*     */       
/* 161 */       ps = conn.prepareStatement("SELECT ID_EVENT, RECEIVER_GROUP, EVENT_PROTOCOL,EVENT_DATA,TRANSMISSION_RETRIES FROM AVL_EVENT WHERE ((TRANSMITTED is NULL) AND (TRANSMISSION_CANCELLED = 0)) ORDER BY RECEIVED ASC OFFSET 0 ROWS FETCH NEXT 1000 ROWS ONLY");
/* 162 */       rs = ps.executeQuery();
/* 163 */       while (rs.next()) {
/* 164 */         EventDataHolder edh = new EventDataHolder();
/* 165 */         edh.setId_Event(rs.getInt("ID_EVENT"));
/* 166 */         edh.setIdGroup(rs.getInt("RECEIVER_GROUP"));
/* 167 */         edh.setEvent_Protocol(rs.getShort("EVENT_PROTOCOL"));
/* 168 */         edh.setEventBuffer(rs.getBytes("EVENT_DATA"));
/* 169 */         edh.setTransmission_Retries(rs.getShort("TRANSMISSION_RETRIES"));
/* 170 */         edh.setProductId(Util.EnumProductIDs.MERCURIUS.getProductId());
/* 171 */         edhList.add(edh);
/*     */       } 
/* 173 */       rs.close();
/* 174 */       ps.close();
/*     */       
/* 176 */       return edhList;
/*     */     }
/* 178 */     catch (SQLException e) {
/* 179 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 180 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 182 */       throw e;
/*     */     } finally {
/* 184 */       if (rs != null) {
/* 185 */         rs.close();
/*     */       }
/* 187 */       if (ps != null) {
/* 188 */         ps.close();
/*     */       }
/* 190 */       if (conn != null) {
/* 191 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void disableCommunicationLog(int idModule, Connection conn) throws SQLException {
/* 198 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 201 */       ps = conn.prepareStatement("UPDATE AVL_MODULE SET COMM_LOG=0 WHERE ID_MODULE=?");
/* 202 */       ps.setInt(1, idModule);
/* 203 */       ps.executeUpdate();
/* 204 */       ps.close();
/*     */     }
/* 206 */     catch (SQLException e) {
/* 207 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 208 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 210 */       throw e;
/*     */     } finally {
/* 212 */       if (ps != null) {
/* 213 */         ps.close();
/*     */       }
/* 215 */       if (conn != null) {
/* 216 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean isCommandCancelled(int commandId, Connection conn) throws SQLException {
/* 223 */     PreparedStatement ps = null;
/* 224 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 227 */       boolean isCancel = false;
/*     */       
/* 229 */       ps = conn.prepareStatement("SELECT CANCELLED FROM AVL_COMMAND WHERE ID_COMMAND = ?");
/* 230 */       ps.setInt(1, commandId);
/* 231 */       rs = ps.executeQuery();
/* 232 */       if (rs.next()) {
/* 233 */         isCancel = (rs.getTimestamp("CANCELLED") != null);
/*     */       }
/* 235 */       rs.close();
/* 236 */       ps.close();
/*     */       
/* 238 */       return isCancel;
/*     */     }
/* 240 */     catch (SQLException e) {
/* 241 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 242 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 244 */       throw e;
/*     */     } finally {
/* 246 */       if (rs != null) {
/* 247 */         rs.close();
/*     */       }
/* 249 */       if (ps != null) {
/* 250 */         ps.close();
/*     */       }
/* 252 */       if (conn != null) {
/* 253 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateCommandStatus(int commandId, Connection conn) throws SQLException {
/* 260 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 263 */       ps = conn.prepareStatement("UPDATE AVL_COMMAND SET IN_PROGRESS=1 WHERE ID_COMMAND=?");
/* 264 */       ps.setInt(1, commandId);
/* 265 */       ps.executeUpdate();
/* 266 */       ps.close();
/*     */     }
/* 268 */     catch (SQLException e) {
/* 269 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 270 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 272 */       throw e;
/*     */     } finally {
/* 274 */       if (ps != null) {
/* 275 */         ps.close();
/*     */       }
/* 277 */       if (conn != null) {
/* 278 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void saveGeofenceData(int idModule, int idClient, int geoCRC32, List<GeofenceBean> gList, Connection conn) throws SQLException {
/* 285 */     PreparedStatement ps1 = null;
/* 286 */     PreparedStatement ps2 = null;
/* 287 */     Statement st = null;
/* 288 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 291 */       st = conn.createStatement();
/* 292 */       st.addBatch("DELETE FROM AVL_GEOFENCE_COORDINATES WHERE ID_MODULE=" + idModule);
/* 293 */       st.addBatch("DELETE FROM AVL_GEOFENCES WHERE ID_MODULE=" + idModule);
/* 294 */       st.executeBatch();
/* 295 */       st.close();
/*     */       
/* 297 */       ps1 = conn.prepareStatement("INSERT INTO AVL_GEOFENCES (ID_MODULE, ID_CLIENT, NAME, TYPE, RADIUS, GEOFENCE_INDEX) VALUES (?,?,?,?,?,?)", 1);
/* 298 */       ps2 = conn.prepareStatement("INSERT INTO AVL_GEOFENCE_COORDINATES (ID_GEOFENCE, ID_MODULE, ID_CLIENT, LATTITUDE, LONGITUDE) VALUES (?,?,?,?,?)");
/*     */       
/* 300 */       for (GeofenceBean gBean : gList) {
/* 301 */         int idGeofence = 0;
/* 302 */         ps1.setInt(1, idModule);
/* 303 */         ps1.setInt(2, idClient);
/* 304 */         ps1.setString(3, gBean.getGeofenceName());
/* 305 */         ps1.setShort(4, gBean.getType());
/* 306 */         if (gBean.getType() == MercuriusAVLHandlerHelper.CIRCLE_TYPE) {
/* 307 */           ps1.setFloat(5, gBean.getRadius());
/*     */         } else {
/* 309 */           ps1.setNull(5, 6);
/*     */         } 
/* 311 */         ps1.setInt(6, gBean.getGeofenceIndex());
/* 312 */         ps1.execute();
/* 313 */         rs = ps1.getGeneratedKeys();
/* 314 */         if (rs.next()) {
/* 315 */           idGeofence = rs.getInt(1);
/*     */         }
/* 317 */         rs.close();
/*     */         
/* 319 */         if (idGeofence > 0) {
/* 320 */           if (gBean.getType() == MercuriusAVLHandlerHelper.CIRCLE_TYPE) {
/* 321 */             ps2.setInt(1, idGeofence);
/* 322 */             ps2.setInt(2, idModule);
/* 323 */             ps2.setInt(3, idClient);
/* 324 */             ps2.setFloat(4, ((Float)gBean.getLatList().get(0)).floatValue());
/* 325 */             ps2.setFloat(5, ((Float)gBean.getLongList().get(0)).floatValue());
/* 326 */             ps2.addBatch(); continue;
/*     */           } 
/* 328 */           for (int i = 0; i < gBean.getLatList().size(); i++) {
/* 329 */             ps2.setInt(1, idGeofence);
/* 330 */             ps2.setInt(2, idModule);
/* 331 */             ps2.setInt(3, idClient);
/* 332 */             ps2.setFloat(4, ((Float)gBean.getLatList().get(i)).floatValue());
/* 333 */             ps2.setFloat(5, ((Float)gBean.getLongList().get(i)).floatValue());
/* 334 */             ps2.addBatch();
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 339 */       ps1.close();
/* 340 */       ps2.executeBatch();
/* 341 */       ps2.close();
/*     */       
/* 343 */       ps1 = conn.prepareStatement("UPDATE AVL_MODULE SET GEOFENCE_CRC32=? WHERE ID_MODULE=?");
/* 344 */       ps1.setInt(1, geoCRC32);
/* 345 */       ps1.setInt(2, idModule);
/* 346 */       ps1.executeUpdate();
/* 347 */       ps1.close();
/*     */     }
/* 349 */     catch (SQLException e) {
/* 350 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 351 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 353 */       throw e;
/*     */     } finally {
/* 355 */       if (rs != null) {
/* 356 */         rs.close();
/*     */       }
/* 358 */       if (st != null) {
/* 359 */         st.close();
/*     */       }
/* 361 */       if (ps1 != null) {
/* 362 */         ps1.close();
/*     */       }
/* 364 */       if (ps2 != null) {
/* 365 */         ps2.close();
/*     */       }
/* 367 */       if (conn != null) {
/* 368 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateGeofenceCRC32(int idModule, int geoCRC32, Connection conn) throws SQLException {
/* 375 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 378 */       ps = conn.prepareStatement("UPDATE AVL_MODULE SET GEOFENCE_CRC32  = ? WHERE ID_MODULE = ?");
/* 379 */       ps.setInt(1, geoCRC32);
/* 380 */       ps.setInt(2, idModule);
/* 381 */       ps.executeUpdate();
/* 382 */       ps.close();
/*     */     }
/* 384 */     catch (SQLException e) {
/* 385 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 386 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 388 */       throw e;
/*     */     } finally {
/* 390 */       if (ps != null) {
/* 391 */         ps.close();
/*     */       }
/* 393 */       if (conn != null) {
/* 394 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateGPSFWVersion(int idModule, String custVersion, String sirfVersion, Connection conn) throws SQLException {
/* 401 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 404 */       ps = conn.prepareStatement("UPDATE AVL_MODULE SET GPS_CUSTOMER_FW_VERSION  = ?, GPS_SIRF_FW_VERSION = ? WHERE ID_MODULE = ?");
/* 405 */       ps.setString(1, custVersion);
/* 406 */       ps.setString(2, sirfVersion);
/* 407 */       ps.setInt(3, idModule);
/* 408 */       ps.executeUpdate();
/* 409 */       ps.close();
/*     */     }
/* 411 */     catch (SQLException e) {
/* 412 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 413 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 415 */       throw e;
/*     */     } finally {
/* 417 */       if (ps != null) {
/* 418 */         ps.close();
/*     */       }
/* 420 */       if (conn != null) {
/* 421 */         conn.close();
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
/*     */   public static void updateCommandData(int idCommand, InputStream commandFileDataStream, Connection conn) throws SQLException {
/* 435 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 438 */       ps = conn.prepareStatement("UPDATE AVL_COMMAND SET COMMAND_FILE_DATA = ? WHERE ID_COMMAND =?");
/* 439 */       ps.setBlob(1, commandFileDataStream);
/* 440 */       ps.setInt(2, idCommand);
/* 441 */       ps.executeUpdate();
/*     */     }
/* 443 */     catch (SQLException e) {
/* 444 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 445 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
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
/* 459 */       if (conn != null) {
/* 460 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> getGroupIds(Connection conn) throws SQLException {
/* 467 */     PreparedStatement ps = null;
/* 468 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 471 */       List<Integer> gList = new ArrayList<>();
/*     */       
/* 473 */       ps = conn.prepareStatement("SELECT ID_CLIENT FROM AVL_CLIENT WHERE CLIENT_TYPE=3");
/* 474 */       rs = ps.executeQuery();
/* 475 */       while (rs.next()) {
/* 476 */         gList.add(Integer.valueOf(rs.getInt("ID_CLIENT")));
/*     */       }
/* 478 */       rs.close();
/* 479 */       ps.close();
/*     */       
/* 481 */       return gList;
/*     */     }
/* 483 */     catch (SQLException e) {
/* 484 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 485 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 487 */       throw e;
/*     */     } finally {
/* 489 */       if (rs != null) {
/* 490 */         rs.close();
/*     */       }
/* 492 */       if (ps != null) {
/* 493 */         ps.close();
/*     */       }
/* 495 */       if (conn != null) {
/* 496 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void saveVoiceMessage(byte[] fileContent, int fileLength, String fileName, int crc32, int dir, Connection conn) throws SQLException {
/* 503 */     PreparedStatement ps = null;
/* 504 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 507 */       ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM AVL_AJS_FILES_REPO WHERE UPPER(NAME) = ? AND FILE_CRC32 = ? AND FILE_LENGTH = ? AND DIRECTORY = ?");
/* 508 */       ps.setString(1, fileName.toUpperCase());
/* 509 */       ps.setInt(2, crc32);
/* 510 */       ps.setInt(3, fileLength);
/* 511 */       ps.setInt(4, dir);
/* 512 */       rs = ps.executeQuery();
/* 513 */       if (rs.next() && 
/* 514 */         rs.getInt("CNT") == 0) {
/* 515 */         rs.close();
/* 516 */         ps.close();
/*     */         
/* 518 */         ps = conn.prepareStatement("INSERT INTO AVL_AJS_FILES_REPO (FILE_LENGTH, FILE_CRC32, DIRECTORY, NAME, FILE_DATA) VALUES (?,?,?,?,?)");
/* 519 */         ps.setInt(1, fileLength);
/* 520 */         ps.setInt(2, crc32);
/* 521 */         ps.setInt(3, dir);
/* 522 */         ps.setString(4, fileName);
/* 523 */         ps.setBlob(5, new ByteArrayInputStream(fileContent));
/* 524 */         ps.executeUpdate();
/* 525 */         ps.close();
/*     */       }
/*     */     
/*     */     }
/* 529 */     catch (SQLException e) {
/* 530 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 531 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 533 */       throw e;
/*     */     } finally {
/* 535 */       if (rs != null) {
/* 536 */         rs.close();
/*     */       }
/* 538 */       if (ps != null) {
/* 539 */         ps.close();
/*     */       }
/* 541 */       if (conn != null) {
/* 542 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<AudioNJSFileInfo> getMissingVoiceMessagesInfo(List<AudioNJSFileInfo> vmList, Connection conn) throws SQLException {
/* 549 */     PreparedStatement ps = null;
/* 550 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 553 */       List<AudioNJSFileInfo> reqVMList = new ArrayList<>();
/*     */       
/* 555 */       ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM AVL_AJS_FILES_REPO WHERE NAME = ? AND FILE_CRC32 = ? AND FILE_LENGTH = ? AND DIRECTORY = ?");
/* 556 */       for (AudioNJSFileInfo vm : vmList) {
/* 557 */         ps.setString(1, vm.getName());
/* 558 */         ps.setInt(2, vm.getCrc32());
/* 559 */         ps.setInt(3, vm.getLength());
/* 560 */         ps.setInt(4, vm.getDir());
/* 561 */         rs = ps.executeQuery();
/* 562 */         if (rs.next() && 
/* 563 */           rs.getInt("CNT") == 0) {
/* 564 */           reqVMList.add(vm);
/*     */         }
/*     */         
/* 567 */         rs.close();
/*     */       } 
/* 569 */       ps.close();
/*     */       
/* 571 */       return reqVMList;
/*     */     }
/* 573 */     catch (SQLException e) {
/* 574 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 575 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 577 */       throw e;
/*     */     } finally {
/* 579 */       if (rs != null) {
/* 580 */         rs.close();
/*     */       }
/* 582 */       if (ps != null) {
/* 583 */         ps.close();
/*     */       }
/* 585 */       if (conn != null) {
/* 586 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] getVoiceMessagesByName(int fileLength, String name, int cmCrc32, int dir, Connection conn) throws SQLException {
/* 593 */     PreparedStatement ps = null;
/* 594 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 597 */       byte[] content = null;
/*     */       
/* 599 */       ps = conn.prepareStatement("SELECT FILE_DATA FROM AVL_AJS_FILES_REPO WHERE UPPER(NAME) = ? AND FILE_CRC32 = ? AND FILE_LENGTH = ? AND DIRECTORY = ?");
/* 600 */       ps.setString(1, name.toUpperCase());
/* 601 */       ps.setInt(2, cmCrc32);
/* 602 */       ps.setInt(3, fileLength);
/* 603 */       ps.setInt(4, dir);
/* 604 */       rs = ps.executeQuery();
/* 605 */       if (rs.next()) {
/* 606 */         content = rs.getBytes("FILE_DATA");
/*     */       }
/* 608 */       rs.close();
/* 609 */       ps.close();
/*     */       
/* 611 */       return content;
/*     */     }
/* 613 */     catch (SQLException e) {
/* 614 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 615 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 617 */       throw e;
/*     */     } finally {
/* 619 */       if (rs != null) {
/* 620 */         rs.close();
/*     */       }
/* 622 */       if (ps != null) {
/* 623 */         ps.close();
/*     */       }
/* 625 */       if (conn != null) {
/* 626 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static String getSnFromMercuriusModule(int idClient, Connection conn) throws SQLException {
/* 633 */     PreparedStatement ps = null;
/* 634 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 637 */       String sn = "";
/*     */       
/* 639 */       ps = conn.prepareStatement("SELECT SN FROM AVL_MODULE  WHERE ID_CLIENT = ? WITH UR");
/* 640 */       ps.setInt(1, idClient);
/* 641 */       rs = ps.executeQuery();
/* 642 */       if (rs.next()) {
/* 643 */         sn = rs.getString("SN");
/*     */       }
/* 645 */       rs.close();
/* 646 */       ps.close();
/*     */       
/* 648 */       return sn;
/*     */     }
/* 650 */     catch (SQLException e) {
/* 651 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 652 */         Logger.getLogger(MercuriusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 654 */       throw e;
/*     */     } finally {
/* 656 */       if (rs != null) {
/* 657 */         rs.close();
/*     */       }
/* 659 */       if (ps != null) {
/* 660 */         ps.close();
/*     */       }
/* 662 */       if (conn != null)
/* 663 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\mercurius\MercuriusQueryHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */