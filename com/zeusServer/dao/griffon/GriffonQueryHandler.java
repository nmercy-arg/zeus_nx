/*      */ package com.zeusServer.dao.griffon;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.dto.FAI_Validation;
/*      */ import com.zeusServer.dto.PendingDataHolder;
/*      */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.Rijndael;
/*      */ import com.zeuscc.griffon.derby.beans.AudioFileDetails;
/*      */ import com.zeuscc.griffon.derby.beans.ExpansionModule;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonModule;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonUser;
/*      */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
/*      */ import com.zeuscc.griffon.derby.beans.PGM;
/*      */ import com.zeuscc.griffon.derby.beans.Partition;
/*      */ import com.zeuscc.griffon.derby.beans.PartitionGroup;
/*      */ import com.zeuscc.griffon.derby.beans.Schedular;
/*      */ import com.zeuscc.griffon.derby.beans.UserGroup;
/*      */ import com.zeuscc.griffon.derby.beans.VoiceMessage;
/*      */ import com.zeuscc.griffon.derby.beans.Zone;
/*      */ import com.zeuscc.griffon.derby.beans.ZoneGroup;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.sql.Connection;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.sql.Timestamp;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.TimeZone;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class GriffonQueryHandler
/*      */ {
/*      */   public static List<PendingDataHolder> getAllPendingAlive(Connection conn) throws SQLException {
/*   67 */     PreparedStatement ps = null;
/*   68 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*   71 */       int limit = (GlobalVariables.currentPlatform == Enums.Platform.ARM) ? 100 : 250;
/*   72 */       List<PendingDataHolder> dataList = new ArrayList<>(limit);
/*      */       
/*   74 */       ps = conn.prepareStatement("SELECT ID_CLIENT, ID_MODULE, ID_PENDING_DATA_FIELD, RECEIVED, CONTENT, LAST_COMM_INTERFACE, MIN_GSM_SIGNAL_LEVEL FROM GRCP_PENDING_DATA_FIELDS ORDER BY RECEIVED ASC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
/*   75 */       ps.setInt(1, limit);
/*   76 */       rs = ps.executeQuery();
/*   77 */       while (rs.next()) {
/*   78 */         PendingDataHolder pdh = new PendingDataHolder();
/*   79 */         pdh.setIdClient(rs.getInt("ID_CLIENT"));
/*   80 */         pdh.setIdModule(rs.getInt("ID_MODULE"));
/*   81 */         pdh.setIdPendingAlive(rs.getInt("ID_PENDING_DATA_FIELD"));
/*   82 */         pdh.setReceived(rs.getTimestamp("RECEIVED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))));
/*   83 */         pdh.setContent(rs.getBytes("CONTENT"));
/*   84 */         pdh.setLastCommInterface(rs.getShort("LAST_COMM_INTERFACE"));
/*   85 */         pdh.setMinGsmSignalLevel(rs.getShort("MIN_GSM_SIGNAL_LEVEL"));
/*   86 */         dataList.add(pdh);
/*      */       } 
/*   88 */       rs.close();
/*   89 */       ps.close();
/*      */       
/*   91 */       return dataList;
/*      */     }
/*   93 */     catch (SQLException ex) {
/*   94 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*   95 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*   97 */       throw ex;
/*      */     } finally {
/*   99 */       if (rs != null) {
/*  100 */         rs.close();
/*      */       }
/*  102 */       if (ps != null) {
/*  103 */         ps.close();
/*      */       }
/*  105 */       if (conn != null) {
/*  106 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static List<EventDataHolder> getNonProcessedEvents(Connection conn) throws SQLException {
/*  113 */     PreparedStatement ps = null;
/*  114 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  117 */       List<EventDataHolder> edhList = new ArrayList<>();
/*      */       
/*  119 */       ps = conn.prepareStatement("SELECT ID_EVENT, RECEIVER_GROUP, EVENT_PROTOCOL, PARTITION, ACCOUNT, REPORT_CODE, ZONE_USER, TRANSMISSION_RETRIES FROM GRCP_EVENT WHERE ((TRANSMITTED IS NULL) AND (TRANSMISSION_CANCELLED = 0)) ORDER BY RECEIVED ASC OFFSET 0 ROWS FETCH NEXT 1000 ROWS ONLY");
/*  120 */       rs = ps.executeQuery();
/*  121 */       while (rs.next()) {
/*  122 */         EventDataHolder edh = new EventDataHolder();
/*  123 */         edh.setId_Event(rs.getInt("ID_EVENT"));
/*  124 */         edh.setIdGroup(rs.getInt("RECEIVER_GROUP"));
/*  125 */         edh.setEvent_Protocol(rs.getShort("EVENT_PROTOCOL"));
/*  126 */         edh.setEventBuffer(Functions.getContactIDBuffer(rs.getString("ACCOUNT"), rs.getString("REPORT_CODE"), rs.getString("PARTITION"), rs.getString("ZONE_USER")));
/*  127 */         edh.setTransmission_Retries(rs.getShort("TRANSMISSION_RETRIES"));
/*  128 */         edh.setProductId(Util.EnumProductIDs.GRIFFON_V1.getProductId());
/*  129 */         edhList.add(edh);
/*      */       } 
/*  131 */       rs.close();
/*  132 */       ps.close();
/*      */       
/*  134 */       return edhList;
/*      */     }
/*  136 */     catch (SQLException ex) {
/*  137 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  138 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  140 */       throw ex;
/*      */     } finally {
/*  142 */       if (rs != null) {
/*  143 */         rs.close();
/*      */       }
/*  145 */       if (ps != null) {
/*  146 */         ps.close();
/*      */       }
/*  148 */       if (conn != null) {
/*  149 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static List<Integer> getGroupIds(Connection conn) throws SQLException {
/*  156 */     PreparedStatement ps = null;
/*  157 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  160 */       List<Integer> gList = new ArrayList<>();
/*      */       
/*  162 */       ps = conn.prepareStatement("SELECT ID_CLIENT FROM GRCP_CLIENT WHERE CLIENT_TYPE = 3");
/*  163 */       rs = ps.executeQuery();
/*  164 */       while (rs.next()) {
/*  165 */         gList.add(Integer.valueOf(rs.getInt("ID_CLIENT")));
/*      */       }
/*  167 */       rs.close();
/*  168 */       ps.close();
/*      */       
/*  170 */       return gList;
/*      */     }
/*  172 */     catch (SQLException ex) {
/*  173 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  174 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  176 */       throw ex;
/*      */     } finally {
/*  178 */       if (rs != null) {
/*  179 */         rs.close();
/*      */       }
/*  181 */       if (ps != null) {
/*  182 */         ps.close();
/*      */       }
/*  184 */       if (conn != null) {
/*  185 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static boolean isCommandCancelled(int commandId, Connection conn) throws SQLException {
/*  192 */     PreparedStatement ps = null;
/*  193 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  196 */       boolean isCancel = false;
/*      */       
/*  198 */       ps = conn.prepareStatement("SELECT CANCELLED FROM GRCP_COMMAND WHERE ID_COMMAND = ?");
/*  199 */       ps.setInt(1, commandId);
/*  200 */       rs = ps.executeQuery();
/*  201 */       if (rs.next()) {
/*  202 */         isCancel = (rs.getTimestamp("CANCELLED") != null);
/*      */       }
/*  204 */       rs.close();
/*  205 */       ps.close();
/*      */       
/*  207 */       return isCancel;
/*      */     }
/*  209 */     catch (SQLException ex) {
/*  210 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  211 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  213 */       throw ex;
/*      */     } finally {
/*  215 */       if (rs != null) {
/*  216 */         rs.close();
/*      */       }
/*  218 */       if (ps != null) {
/*  219 */         ps.close();
/*      */       }
/*  221 */       if (conn != null) {
/*  222 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void updateCommandStatus(int commandId, int progressStatus, Connection conn) throws SQLException {
/*  229 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/*  232 */       ps = conn.prepareStatement("UPDATE GRCP_COMMAND SET IN_PROGRESS = ? WHERE ID_COMMAND = ?");
/*  233 */       ps.setInt(1, progressStatus);
/*  234 */       ps.setInt(2, commandId);
/*  235 */       ps.executeUpdate();
/*  236 */       ps.close();
/*      */     }
/*  238 */     catch (SQLException ex) {
/*  239 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  240 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  242 */       throw ex;
/*      */     } finally {
/*  244 */       if (ps != null) {
/*  245 */         ps.close();
/*      */       }
/*  247 */       if (conn != null) {
/*  248 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void updateCommandFailureStatus(int commandId, int eRetries, String content, Connection conn) throws SQLException {
/*  255 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/*  258 */       ps = conn.prepareStatement("UPDATE GRCP_COMMAND SET TRANSMITTED=CURRENT_TIMESTAMP, EXEC_RETRIES =?, EXEC_CANCELLED=1, IN_PROGRESS = 0, COMMAND_DATA =? WHERE ID_COMMAND=?");
/*  259 */       ps.setInt(1, eRetries);
/*  260 */       ps.setBinaryStream(2, new ByteArrayInputStream(content.getBytes()));
/*  261 */       ps.setInt(3, commandId);
/*  262 */       ps.executeUpdate();
/*  263 */       ps.close();
/*      */     }
/*  265 */     catch (SQLException ex) {
/*  266 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  267 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  269 */       throw ex;
/*      */     } finally {
/*  271 */       if (ps != null) {
/*  272 */         ps.close();
/*      */       }
/*  274 */       if (conn != null) {
/*  275 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void updateCommandFileData(int idCommand, InputStream commandFileDataStream, Connection conn) throws SQLException {
/*  289 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/*  292 */       ps = conn.prepareStatement("UPDATE GRCP_COMMAND SET COMMAND_FILE_DATA=? WHERE ID_COMMAND=?");
/*  293 */       ps.setBlob(1, commandFileDataStream);
/*  294 */       ps.setInt(2, idCommand);
/*  295 */       ps.executeUpdate();
/*  296 */       ps.close();
/*      */     }
/*  298 */     catch (SQLException ex) {
/*  299 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  300 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  302 */       throw ex;
/*      */     } finally {
/*  304 */       if (commandFileDataStream != null) {
/*      */         try {
/*  306 */           commandFileDataStream.close();
/*  307 */         } catch (IOException iOException) {}
/*      */       }
/*      */ 
/*      */       
/*  311 */       if (ps != null) {
/*  312 */         ps.close();
/*      */       }
/*  314 */       if (conn != null) {
/*  315 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static List<VoiceMessage> getVoiceMessageInfoByIdModule(int idModule, boolean flag, Connection conn) throws SQLException {
/*  322 */     PreparedStatement ps = null;
/*  323 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  326 */       List<VoiceMessage> vmList = new ArrayList<>();
/*      */       
/*  328 */       ps = conn.prepareStatement("SELECT VM_FILE_INDEX, VM_FILE_LENGTH, VM_FILE_CRC32, NAME FROM GRCP_VOICE_MESSAGES WHERE ID_MODULE = ?");
/*  329 */       ps.setInt(1, idModule);
/*  330 */       rs = ps.executeQuery();
/*  331 */       while (rs.next()) {
/*  332 */         VoiceMessage vm = new VoiceMessage();
/*  333 */         vm.setVmCRC32(rs.getInt("VM_FILE_CRC32"));
/*  334 */         vm.setVmIndex(rs.getInt("VM_FILE_INDEX"));
/*  335 */         vm.setVmLength(rs.getInt("VM_FILE_LENGTH"));
/*  336 */         vm.setVmName(rs.getString("NAME"));
/*  337 */         vmList.add(vm);
/*      */       } 
/*  339 */       rs.close();
/*  340 */       ps.close();
/*      */       
/*  342 */       return vmList;
/*      */     }
/*  344 */     catch (SQLException ex) {
/*  345 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  346 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  348 */       throw ex;
/*      */     } finally {
/*  350 */       if (rs != null) {
/*  351 */         rs.close();
/*      */       }
/*  353 */       if (ps != null) {
/*  354 */         ps.close();
/*      */       }
/*  356 */       if (flag && conn != null) {
/*  357 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte[] getVoiceMessagesByName(int fileLength, String name, int cmCrc32, Connection conn) throws SQLException {
/*  364 */     PreparedStatement ps = null;
/*  365 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  368 */       byte[] content = null;
/*      */       
/*  370 */       ps = conn.prepareStatement("SELECT VM_FILE_DATA FROM GRCP_VOICE_MESSAGES_REPO WHERE UPPER(NAME) = ? AND VM_FILE_CRC32 = ? AND VM_FILE_LENGTH = ?");
/*  371 */       ps.setString(1, name.toUpperCase());
/*  372 */       ps.setInt(2, cmCrc32);
/*  373 */       ps.setInt(3, fileLength);
/*  374 */       rs = ps.executeQuery();
/*  375 */       if (rs.next()) {
/*  376 */         content = rs.getBytes("VM_FILE_DATA");
/*      */       }
/*  378 */       rs.close();
/*  379 */       ps.close();
/*      */       
/*  381 */       return content;
/*      */     }
/*  383 */     catch (SQLException ex) {
/*  384 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  385 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  387 */       throw ex;
/*      */     } finally {
/*  389 */       if (rs != null) {
/*  390 */         rs.close();
/*      */       }
/*  392 */       if (ps != null) {
/*  393 */         ps.close();
/*      */       }
/*  395 */       if (conn != null) {
/*  396 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void saveParsedCFGData(GriffonModule gModule, FAI_Validation faiValidation, Connection conn) throws Exception {
/*  411 */     PreparedStatement ps1 = null;
/*  412 */     PreparedStatement ps2 = null;
/*  413 */     Statement st = null;
/*      */     
/*      */     try {
/*  416 */       st = conn.createStatement();
/*  417 */       st.addBatch("DELETE FROM GRCP_FAI_VALIDATIONS WHERE ID_MODULE=" + gModule.getId_Module());
/*  418 */       st.addBatch("DELETE FROM GRCP_EXPANSION_MODULES WHERE ID_MODULE=" + gModule.getId_Module());
/*  419 */       st.addBatch("DELETE FROM GRCP_EM_KP_FW WHERE ID_MODULE=" + gModule.getId_Module());
/*  420 */       st.addBatch("DELETE FROM GRCP_PARTITIONS WHERE ID_MODULE=" + gModule.getId_Module());
/*  421 */       st.addBatch("DELETE FROM GRCP_PGMS WHERE ID_MODULE=" + gModule.getId_Module());
/*  422 */       st.addBatch("DELETE FROM GRCP_ZONES WHERE ID_MODULE=" + gModule.getId_Module());
/*  423 */       st.addBatch("DELETE FROM GRCP_AUDIO_FILES WHERE ID_MODULE=" + gModule.getId_Module() + " AND TYPE = 1");
/*  424 */       st.addBatch("DELETE FROM GRCP_SCHEDULES WHERE ID_MODULE=" + gModule.getId_Module());
/*  425 */       st.addBatch("DELETE FROM GRCP_PARTITION_GROUPS WHERE ID_MODULE=" + gModule.getId_Module());
/*  426 */       st.addBatch("DELETE FROM GRCP_ZONE_GROUPS WHERE ID_MODULE=" + gModule.getId_Module());
/*  427 */       st.addBatch("DELETE FROM GRCP_USER_GROUPS WHERE ID_MODULE=" + gModule.getId_Module());
/*  428 */       st.addBatch("DELETE FROM GRCP_GRIFFON_USERS WHERE ID_MODULE=" + gModule.getId_Module());
/*  429 */       st.addBatch("DELETE FROM GRCP_USERS WHERE USER_TYPE = 63 AND ID_CLIENT =" + gModule.getId_Client());
/*  430 */       if (gModule.getVmList() == null || gModule.getVmList().isEmpty()) {
/*  431 */         st.addBatch("DELETE FROM GRCP_VOICE_MESSAGES WHERE ID_MODULE=" + gModule.getId_Module());
/*  432 */         st.executeBatch();
/*  433 */         st.close();
/*      */       } else {
/*  435 */         List<VoiceMessage> vmList = getVoiceMessageInfoByIdModule(gModule.getId_Module(), false, conn);
/*  436 */         for (VoiceMessage vm : vmList) {
/*  437 */           if (!gModule.getVmList().contains(vm)) {
/*  438 */             st.addBatch("DELETE FROM GRCP_VOICE_MESSAGES WHERE UPPER(NAME) = '" + vm.getVmName().toUpperCase() + "' AND ID_MODULE=" + gModule.getId_Module());
/*      */           }
/*      */         } 
/*  441 */         st.executeBatch();
/*  442 */         st.close();
/*      */ 
/*      */         
/*  445 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_VOICE_MESSAGES (ID_CLIENT, ID_MODULE, VM_FILE_INDEX, VM_FILE_LENGTH, VM_FILE_CRC32, NAME) VALUES (?,?,?,?,?,?)");
/*  446 */         boolean vmpsFlag = false;
/*  447 */         for (VoiceMessage vm : gModule.getVmList()) {
/*  448 */           if (!vmList.contains(vm)) {
/*  449 */             vmpsFlag = true;
/*  450 */             ps1.setInt(1, gModule.getId_Client());
/*  451 */             ps1.setInt(2, gModule.getId_Module());
/*  452 */             ps1.setInt(3, vm.getVmIndex());
/*  453 */             ps1.setInt(4, vm.getVmLength());
/*  454 */             ps1.setInt(5, vm.getVmCRC32());
/*  455 */             ps1.setString(6, vm.getVmName());
/*  456 */             ps1.addBatch();
/*      */           } 
/*      */         } 
/*  459 */         if (vmpsFlag) {
/*  460 */           ps1.executeBatch();
/*      */         }
/*  462 */         ps1.close();
/*      */       } 
/*      */       
/*  465 */       if (gModule.getEmList() != null) {
/*  466 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_EXPANSION_MODULES (ID_CLIENT, ID_MODULE, EM_INDEX, SN, NAME, EM_TYPE, ENABLED) VALUES (?,?,?,?,?,?,?)");
/*  467 */         for (ExpansionModule eModule : gModule.getEmList()) {
/*  468 */           ps1.setInt(1, gModule.getId_Client());
/*  469 */           ps1.setInt(2, gModule.getId_Module());
/*  470 */           ps1.setInt(3, eModule.getEmIndex());
/*  471 */           ps1.setString(4, eModule.getSn());
/*  472 */           ps1.setString(5, eModule.getName());
/*  473 */           ps1.setInt(6, eModule.getEmType());
/*  474 */           ps1.setInt(7, eModule.getEnabled());
/*  475 */           ps1.addBatch();
/*      */         } 
/*  477 */         ps1.executeBatch();
/*  478 */         ps1.close();
/*      */       } 
/*      */       
/*  481 */       if (gModule.getpList() != null) {
/*  482 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_PARTITIONS (ID_CLIENT, ID_MODULE, PARTITION_INDEX, NAME, ACCOUNT, INSTANT_ENABLE, ASSIGNED_ZONES ) VALUES (?,?,?,?,?,?,?)");
/*  483 */         for (Partition p : gModule.getpList()) {
/*  484 */           ps1.setInt(1, gModule.getId_Client());
/*  485 */           ps1.setInt(2, gModule.getId_Module());
/*  486 */           ps1.setInt(3, p.getPartitionIndex());
/*  487 */           ps1.setString(4, p.getName());
/*  488 */           ps1.setString(5, p.getAccount());
/*  489 */           ps1.setInt(6, p.getInstantEnable());
/*  490 */           ps1.setString(7, p.getAssignedZones());
/*  491 */           ps1.addBatch();
/*      */         } 
/*  493 */         ps1.executeBatch();
/*  494 */         ps1.close();
/*      */       } 
/*      */       
/*  497 */       if (gModule.getzList() != null) {
/*  498 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_ZONES (ID_CLIENT, ID_MODULE, ZONE_INDEX, NAME, ASSIGNED_PARTITIONS, ZONE_TYPE, BYPASSABLE, ANALOG_VOLTAGE) VALUES (?,?,?,?,?,?,?,?)");
/*  499 */         for (Zone zone : gModule.getzList()) {
/*  500 */           ps1.setInt(1, gModule.getId_Client());
/*  501 */           ps1.setInt(2, gModule.getId_Module());
/*  502 */           ps1.setInt(3, zone.getZoneIndex());
/*  503 */           ps1.setString(4, zone.getName());
/*  504 */           ps1.setString(5, zone.getAssignedParitions());
/*  505 */           ps1.setInt(6, zone.getType());
/*  506 */           ps1.setInt(7, zone.getBypassable());
/*  507 */           ps1.setInt(8, zone.getAnalogVoltage());
/*  508 */           ps1.addBatch();
/*      */         } 
/*  510 */         ps1.executeBatch();
/*  511 */         ps1.close();
/*      */       } 
/*      */       
/*  514 */       if (gModule.getPgmList() != null) {
/*  515 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_PGMS (ID_CLIENT, ID_MODULE, PGM_INDEX, NAME, PGM_TYPE, ADDRESS) VALUES (?,?,?,?,?,?)");
/*  516 */         for (PGM pgm : gModule.getPgmList()) {
/*  517 */           ps1.setInt(1, gModule.getId_Client());
/*  518 */           ps1.setInt(2, gModule.getId_Module());
/*  519 */           ps1.setInt(3, pgm.getPgmIndex());
/*  520 */           ps1.setString(4, pgm.getName());
/*  521 */           ps1.setInt(5, pgm.getPgmType());
/*  522 */           ps1.setInt(6, pgm.getAddress());
/*  523 */           ps1.addBatch();
/*      */         } 
/*  525 */         ps1.executeBatch();
/*  526 */         ps1.close();
/*      */       } 
/*      */       
/*  529 */       if (gModule.getSchList() != null) {
/*  530 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_SCHEDULES (ID_CLIENT, ID_MODULE, SCHEDULE_INDEX, NAME) VALUES (?,?,?,?)");
/*  531 */         for (Schedular sch : gModule.getSchList()) {
/*  532 */           ps1.setInt(1, gModule.getId_Client());
/*  533 */           ps1.setInt(2, gModule.getId_Module());
/*  534 */           ps1.setInt(3, sch.getSchedularIndex());
/*  535 */           ps1.setString(4, sch.getSchedularName());
/*  536 */           ps1.addBatch();
/*      */         } 
/*  538 */         ps1.executeBatch();
/*  539 */         ps1.close();
/*      */       } 
/*      */       
/*  542 */       if (gModule.getAfdList() != null) {
/*  543 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_AUDIO_FILES (ID_CLIENT, ID_MODULE, AUDIO_FILE_INDEX, NAME, TYPE) VALUES (?,?,?,?,?)");
/*  544 */         for (AudioFileDetails afd : gModule.getAfdList()) {
/*  545 */           ps1.setInt(1, gModule.getId_Client());
/*  546 */           ps1.setInt(2, gModule.getId_Module());
/*  547 */           ps1.setInt(3, afd.getAudioFileIndex());
/*  548 */           ps1.setString(4, afd.getAudioFileName());
/*  549 */           ps1.setInt(5, afd.getType());
/*  550 */           ps1.addBatch();
/*      */         } 
/*  552 */         ps1.executeBatch();
/*  553 */         ps1.close();
/*      */       } 
/*      */       
/*  556 */       if (gModule.getPgList() != null) {
/*  557 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_PARTITION_GROUPS (ID_CLIENT, ID_MODULE, PARTITION_GROUP_INDEX, NAME, ASSIGNED_PARTITIONS) VALUES (?,?,?,?,?)");
/*  558 */         for (PartitionGroup pg : gModule.getPgList()) {
/*  559 */           ps1.setInt(1, gModule.getId_Client());
/*  560 */           ps1.setInt(2, gModule.getId_Module());
/*  561 */           ps1.setInt(3, pg.getPartitionGroupIndex());
/*  562 */           ps1.setString(4, pg.getPartitionGroupName());
/*  563 */           ps1.setString(5, pg.getAssignedPartitions());
/*  564 */           ps1.addBatch();
/*      */         } 
/*  566 */         ps1.executeBatch();
/*  567 */         ps1.close();
/*      */       } 
/*      */       
/*  570 */       if (gModule.getZgList() != null) {
/*  571 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_ZONE_GROUPS (ID_CLIENT, ID_MODULE, ZONE_GROUP_INDEX, NAME, BYPASSABLE, ASSIGNED_ZONES ) VALUES (?,?,?,?,?,?)");
/*  572 */         for (ZoneGroup zg : gModule.getZgList()) {
/*  573 */           ps1.setInt(1, gModule.getId_Client());
/*  574 */           ps1.setInt(2, gModule.getId_Module());
/*  575 */           ps1.setInt(3, zg.getZoneGroupIndex());
/*  576 */           ps1.setString(4, zg.getZoneGroupName());
/*  577 */           ps1.setInt(5, zg.getBypassable());
/*  578 */           ps1.setString(6, zg.getAssignedZones());
/*  579 */           ps1.addBatch();
/*      */         } 
/*  581 */         ps1.executeBatch();
/*  582 */         ps1.close();
/*      */       } 
/*      */       
/*  585 */       if (gModule.getUgList() != null) {
/*  586 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_USER_GROUPS (ID_CLIENT, ID_MODULE, USER_GROUP_INDEX, NAME, FORCE_ARM,ONLY_ARM,AWAY_ARM,SLEEP_ARM,STAY_ARM,BYPASS_ZONES,BYPASS_PARTITIONS,BYPASS_ZONE_GROUPS, ENABLE ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
/*  587 */         for (UserGroup ug : gModule.getUgList()) {
/*  588 */           ps1.setInt(1, gModule.getId_Client());
/*  589 */           ps1.setInt(2, gModule.getId_Module());
/*  590 */           ps1.setInt(3, ug.getUserGroupIndex());
/*  591 */           ps1.setString(4, ug.getName());
/*  592 */           ps1.setInt(5, ug.getForceARM());
/*  593 */           ps1.setInt(6, ug.getOnlyARM());
/*  594 */           ps1.setInt(7, ug.getAwayARM());
/*  595 */           ps1.setInt(8, ug.getSleepARM());
/*  596 */           ps1.setInt(9, ug.getStatyARM());
/*  597 */           ps1.setInt(10, ug.getBypassZones());
/*  598 */           ps1.setInt(11, ug.getBypassPartitions());
/*  599 */           ps1.setInt(12, ug.getBypassZoneGroups());
/*  600 */           ps1.setInt(13, ug.getEnable());
/*  601 */           ps1.addBatch();
/*      */         } 
/*  603 */         ps1.executeBatch();
/*  604 */         ps1.close();
/*      */       } 
/*      */       
/*  607 */       if (gModule.getGuList() != null) {
/*  608 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_GRIFFON_USERS (ID_CLIENT, ID_MODULE, USER_INDEX, NAME, ASSIGEND_USER_GROUP_INDEX, USER_TYPE, ACCESS_CODE, FORCE_ARM, ONLY_ARM, AWAY_ARM, SLEEP_ARM, STAY_ARM, BYPASS_ZONES, BYPASS_PARTITIONS, BYPASS_ZONE_GROUPS, ASSIGNED_PARTITIONS, ASSIGNED_ZONE_GROUPS, LAST_PWD_CHANGED, MAX_PWD_AGE, ENABLE, APP_USER, RPT_CODE, ASSIGNED_PGMS ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
/*  609 */         ps2 = conn.prepareStatement("INSERT INTO GRCP_USERS (ID_CLIENT, PERMISSIONS, USERID, PASSW, NAME, USER_TYPE) VALUES (?,?,?,?,?,?)");
/*  610 */         for (GriffonUser gu : gModule.getGuList()) {
/*  611 */           String per = "-1";
/*  612 */           int p = 0;
/*  613 */           ps1.setInt(1, gModule.getId_Client());
/*  614 */           ps1.setInt(2, gModule.getId_Module());
/*  615 */           ps1.setInt(3, gu.getUserIndex());
/*  616 */           ps1.setString(4, gu.getName());
/*  617 */           ps1.setInt(5, gu.getUserGroupIndex());
/*  618 */           ps1.setInt(6, gu.getUserType());
/*  619 */           ps1.setString(7, gu.getPasscode());
/*  620 */           ps1.setInt(8, gu.getForceARM());
/*  621 */           ps1.setInt(9, gu.getOnlyARM());
/*  622 */           ps1.setInt(10, gu.getAwayARM());
/*  623 */           ps1.setInt(11, gu.getSleepARM());
/*  624 */           ps1.setInt(12, gu.getStatyARM());
/*  625 */           ps1.setInt(13, gu.getBypassZones());
/*  626 */           ps1.setInt(14, gu.getBypassPartitions());
/*  627 */           ps1.setInt(15, gu.getBypassZoneGroups());
/*  628 */           ps1.setString(16, gu.getAssingedParitions());
/*  629 */           ps1.setString(17, gu.getAssignedZoneGroups());
/*  630 */           ps1.setTimestamp(18, new Timestamp(gu.getLastPwdChanged().getTime()));
/*  631 */           ps1.setInt(19, gu.getMaxAge());
/*  632 */           ps1.setInt(20, gu.getEnable());
/*  633 */           ps1.setInt(21, gu.getAppUser());
/*  634 */           ps1.setString(22, gu.getRptCode());
/*  635 */           ps1.setString(23, gu.getAssignedPGMS());
/*  636 */           ps1.addBatch();
/*  637 */           if (gu.getOnlyARM() == 0) {
/*  638 */             p++;
/*      */           }
/*  640 */           if (gu.getAwayARM() == 1) {
/*  641 */             p += 2;
/*      */           }
/*  643 */           if (gu.getStatyARM() == 1) {
/*  644 */             p += 4;
/*      */           }
/*  646 */           if (gu.getSleepARM() == 1) {
/*  647 */             p += 8;
/*      */           }
/*  649 */           if (gu.getForceARM() == 1) {
/*  650 */             p += 16;
/*      */           }
/*  652 */           if (gu.getBypassZones() == 1) {
/*  653 */             p += 32;
/*      */           }
/*  655 */           if (gu.getBypassZoneGroups() == 1) {
/*  656 */             p += 64;
/*      */           }
/*  658 */           if (gu.getBypassPartitions() == 1) {
/*  659 */             p += 128;
/*      */           }
/*  661 */           per = (p > 0) ? String.valueOf(p) : per;
/*  662 */           if (gu.getAppUser() == 1) {
/*  663 */             ps2.setInt(1, gModule.getId_Client());
/*  664 */             ps2.setString(2, per);
/*  665 */             ps2.setString(3, gModule.getSn() + "_" + gu.getName());
/*  666 */             ps2.setString(4, Rijndael.encryptString(gu.getPasscode(), Rijndael.dbKeyBytes).trim());
/*  667 */             ps2.setString(5, gModule.getSn() + "_" + gu.getName());
/*  668 */             ps2.setInt(6, 63);
/*  669 */             ps2.addBatch();
/*      */           } 
/*      */         } 
/*  672 */         ps1.executeBatch();
/*  673 */         ps1.close();
/*  674 */         ps2.executeBatch();
/*  675 */         ps2.close();
/*      */       } 
/*      */       
/*  678 */       if (faiValidation != null) {
/*  679 */         ps1 = conn.prepareStatement("INSERT INTO GRCP_FAI_VALIDATIONS (ID_CLIENT, ID_MODULE, ETH_ENABLE, GPRS_1_1_ENABLE, GPRS_1_2_ENABLE, GPRS_2_1_ENABLE, GPRS_2_2_ENABLE, WIFI_AP_1_ENABLE, WIFI_AP_2_ENABLE, TELEPHONE_ENABLE, USER_WALK_TEST_ENABLE) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
/*  680 */         ps1.setInt(1, gModule.getId_Client());
/*  681 */         ps1.setInt(2, gModule.getId_Module());
/*  682 */         ps1.setInt(3, faiValidation.getEthEnable());
/*  683 */         ps1.setInt(4, faiValidation.getGprs_1_1_Enable());
/*  684 */         ps1.setInt(5, faiValidation.getGprs_1_2_Enable());
/*  685 */         ps1.setInt(6, faiValidation.getGprs_2_1_Enable());
/*  686 */         ps1.setInt(7, faiValidation.getGprs_2_2_Enable());
/*  687 */         ps1.setInt(8, faiValidation.getWifi_AP_1_Enable());
/*  688 */         ps1.setInt(9, faiValidation.getWifi_AP_2_Enable());
/*  689 */         ps1.setInt(10, faiValidation.getTelephoneEnable());
/*  690 */         ps1.setInt(11, faiValidation.getUserWalkTestEnable());
/*  691 */         ps1.executeUpdate();
/*  692 */         ps1.close();
/*      */       }
/*      */     
/*  695 */     } catch (Exception ex) {
/*  696 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  697 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  699 */       throw ex;
/*      */     } finally {
/*  701 */       if (st != null) {
/*  702 */         st.close();
/*      */       }
/*  704 */       if (ps1 != null) {
/*  705 */         ps1.close();
/*      */       }
/*  707 */       if (ps2 != null) {
/*  708 */         ps2.close();
/*      */       }
/*  710 */       if (conn != null) {
/*  711 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static List<VoiceMessage> getMissingVoiceMessagesInfo(List<VoiceMessage> vmList, Connection conn) throws SQLException {
/*  718 */     PreparedStatement ps = null;
/*  719 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  722 */       List<VoiceMessage> reqVMList = new ArrayList<>();
/*      */       
/*  724 */       ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM GRCP_VOICE_MESSAGES_REPO WHERE NAME = ? AND VM_FILE_CRC32 = ? AND VM_FILE_LENGTH = ?");
/*  725 */       for (VoiceMessage vm : vmList) {
/*  726 */         ps.setString(1, vm.getVmName());
/*  727 */         ps.setInt(2, vm.getVmCRC32());
/*  728 */         ps.setInt(3, vm.getVmLength());
/*  729 */         rs = ps.executeQuery();
/*  730 */         if (rs.next() && 
/*  731 */           rs.getInt("CNT") == 0) {
/*  732 */           reqVMList.add(vm);
/*      */         }
/*      */         
/*  735 */         rs.close();
/*      */       } 
/*  737 */       ps.close();
/*      */       
/*  739 */       return reqVMList;
/*      */     }
/*  741 */     catch (SQLException ex) {
/*  742 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  743 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  745 */       throw ex;
/*      */     } finally {
/*  747 */       if (rs != null) {
/*  748 */         rs.close();
/*      */       }
/*  750 */       if (ps != null) {
/*  751 */         ps.close();
/*      */       }
/*  753 */       if (conn != null) {
/*  754 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void saveVoiceMessage(byte[] fileContent, int fileLength, String fileName, int crc32, Connection conn) throws SQLException {
/*  761 */     PreparedStatement ps = null;
/*  762 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  765 */       ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM GRCP_VOICE_MESSAGES_REPO WHERE UPPER(NAME) = ? AND VM_FILE_CRC32 = ? AND VM_FILE_LENGTH = ?");
/*  766 */       ps.setString(1, fileName.toUpperCase());
/*  767 */       ps.setInt(2, crc32);
/*  768 */       ps.setInt(3, fileLength);
/*  769 */       rs = ps.executeQuery();
/*  770 */       if (rs.next() && 
/*  771 */         rs.getInt("CNT") == 0) {
/*  772 */         rs.close();
/*  773 */         ps.close();
/*      */         
/*  775 */         ps = conn.prepareStatement("INSERT INTO GRCP_VOICE_MESSAGES_REPO (VM_FILE_LENGTH, VM_FILE_CRC32, NAME, VM_FILE_DATA) VALUES (?,?,?,?)");
/*  776 */         ps.setInt(1, fileLength);
/*  777 */         ps.setInt(2, crc32);
/*  778 */         ps.setString(3, fileName);
/*  779 */         ps.setBlob(4, new ByteArrayInputStream(fileContent));
/*  780 */         ps.executeUpdate();
/*  781 */         ps.close();
/*      */       }
/*      */     
/*      */     }
/*  785 */     catch (SQLException ex) {
/*  786 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  787 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  789 */       throw ex;
/*      */     } finally {
/*  791 */       if (rs != null) {
/*  792 */         rs.close();
/*      */       }
/*  794 */       if (ps != null) {
/*  795 */         ps.close();
/*      */       }
/*  797 */       if (conn != null) {
/*  798 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void updateEBFWVersionData(List<ExpansionModule> emFWList, List<ExpansionModule> kpLangFWList, int idClient, int idModule, int ebFWCRC32, boolean flag, Connection conn) throws SQLException {
/*  805 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/*  808 */       ps = conn.prepareStatement("UPDATE GRCP_EXPANSION_MODULES SET FW_VERSION = ?, LAST_RESET_STATUS = ? WHERE EM_INDEX = ? AND ID_MODULE = ?");
/*  809 */       for (ExpansionModule em : emFWList) {
/*  810 */         ps.setString(1, em.getFwVersion());
/*  811 */         ps.setInt(2, em.getLastResetStatus());
/*  812 */         ps.setInt(3, em.getEmIndex());
/*  813 */         ps.setInt(4, idModule);
/*  814 */         ps.addBatch();
/*      */       } 
/*  816 */       ps.executeBatch();
/*  817 */       ps.close();
/*      */       
/*  819 */       ps = conn.prepareStatement("DELETE FROM GRCP_EM_KP_FW WHERE ID_MODULE = ?");
/*  820 */       ps.setInt(1, idModule);
/*  821 */       ps.executeUpdate();
/*  822 */       ps.close();
/*      */       
/*  824 */       if (kpLangFWList != null) {
/*  825 */         ps = conn.prepareStatement("INSERT INTO GRCP_EM_KP_FW (ID_CLIENT, ID_MODULE, EM_INDEX, LANGUAGE_ID, LANGUAGE_FW_VERSION) VALUES(?,?,?,?,?)");
/*  826 */         for (ExpansionModule em : kpLangFWList) {
/*  827 */           ps.setInt(1, idClient);
/*  828 */           ps.setInt(2, idModule);
/*  829 */           ps.setInt(3, em.getEmIndex());
/*  830 */           ps.setInt(4, em.getLastResetStatus());
/*  831 */           ps.setString(5, em.getFwVersion());
/*  832 */           ps.addBatch();
/*      */         } 
/*  834 */         ps.executeBatch();
/*  835 */         ps.close();
/*      */       } 
/*      */       
/*  838 */       ps = conn.prepareStatement("UPDATE GRCP_MODULE SET EB_FW_CRC32 = ? WHERE ID_MODULE = ?");
/*  839 */       ps.setString(1, String.valueOf(ebFWCRC32));
/*  840 */       ps.setInt(2, idModule);
/*  841 */       ps.executeUpdate();
/*  842 */       ps.close();
/*      */     }
/*  844 */     catch (SQLException ex) {
/*  845 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  846 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  848 */       throw ex;
/*      */     } finally {
/*  850 */       if (ps != null) {
/*  851 */         ps.close();
/*      */       }
/*  853 */       if (conn != null) {
/*  854 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void updateRecordedFileLookupData(List<AudioFileDetails> afdList, int idClient, int idModule, int recAudioLookupCRC32, Connection conn) throws SQLException {
/*  861 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/*  864 */       ps = conn.prepareStatement("DELETE FROM GRCP_AUDIO_FILES WHERE ID_MODULE = ? AND TYPE = 2");
/*  865 */       ps.setInt(1, idModule);
/*  866 */       ps.executeUpdate();
/*  867 */       ps.close();
/*      */       
/*  869 */       if (afdList != null) {
/*  870 */         ps = conn.prepareStatement("INSERT INTO GRCP_AUDIO_FILES (ID_CLIENT, ID_MODULE, NAME, TYPE, LENGTH) VALUES(?,?,?,?,?)");
/*  871 */         for (AudioFileDetails afd : afdList) {
/*  872 */           ps.setInt(1, idClient);
/*  873 */           ps.setInt(2, idModule);
/*  874 */           ps.setString(3, afd.getAudioFileName());
/*  875 */           ps.setInt(4, afd.getType());
/*  876 */           ps.setInt(5, afd.getLength());
/*  877 */           ps.addBatch();
/*      */         } 
/*  879 */         ps.executeBatch();
/*  880 */         ps.close();
/*      */       } 
/*      */       
/*  883 */       ps = conn.prepareStatement("UPDATE GRCP_MODULE SET REC_AUDIO_LOOKUP_CRC32 = ? WHERE ID_MODULE = ?");
/*  884 */       ps.setString(1, String.valueOf(recAudioLookupCRC32));
/*  885 */       ps.setInt(2, idModule);
/*  886 */       ps.executeUpdate();
/*  887 */       ps.close();
/*      */     }
/*  889 */     catch (SQLException ex) {
/*  890 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  891 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  893 */       throw ex;
/*      */     } finally {
/*  895 */       if (ps != null) {
/*  896 */         ps.close();
/*      */       }
/*  898 */       if (conn != null) {
/*  899 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static List<Integer> getAnalogPGMIndexByModuleID(int idModule, Connection conn) throws SQLException {
/*  913 */     PreparedStatement ps = null;
/*  914 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  917 */       List<Integer> aPgmList = new ArrayList<>(8);
/*      */       
/*  919 */       ps = conn.prepareStatement("SELECT PGM_INDEX FROM GRCP_PGMS WHERE ID_MODULE = ? AND PGM_TYPE = 2 ORDER BY PGM_INDEX ASC");
/*  920 */       ps.setInt(1, idModule);
/*  921 */       rs = ps.executeQuery();
/*  922 */       while (rs.next()) {
/*  923 */         aPgmList.add(Integer.valueOf(rs.getInt("PGM_INDEX")));
/*      */       }
/*  925 */       rs.close();
/*  926 */       ps.close();
/*      */       
/*  928 */       return aPgmList;
/*      */     }
/*  930 */     catch (SQLException ex) {
/*  931 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  932 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  934 */       throw ex;
/*      */     } finally {
/*  936 */       if (rs != null) {
/*  937 */         rs.close();
/*      */       }
/*  939 */       if (ps != null) {
/*  940 */         ps.close();
/*      */       }
/*  942 */       if (conn != null) {
/*  943 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static Map<String, List<Integer>> getAnalogZoneIndexByModuleID(int idModule, Connection conn) throws SQLException {
/*  957 */     PreparedStatement ps = null;
/*  958 */     ResultSet rs = null;
/*      */     
/*      */     try {
/*  961 */       Map<String, List<Integer>> map = new HashMap<>(2);
/*  962 */       List<Integer> aZoneList = new ArrayList<>(8);
/*  963 */       List<Integer> temp24HrZoneList = new ArrayList<>(8);
/*      */       
/*  965 */       ps = conn.prepareStatement("SELECT ZONE_INDEX, ZONE_TYPE  FROM GRCP_ZONES WHERE ID_MODULE = ? AND (ZONE_TYPE = 11 OR ZONE_TYPE = 417)   ORDER BY ZONE_INDEX ASC");
/*  966 */       ps.setInt(1, idModule);
/*  967 */       rs = ps.executeQuery();
/*  968 */       while (rs.next()) {
/*  969 */         switch (rs.getInt("ZONE_TYPE")) {
/*      */           case 11:
/*  971 */             aZoneList.add(Integer.valueOf(rs.getInt("ZONE_INDEX")));
/*      */           
/*      */           case 417:
/*  974 */             temp24HrZoneList.add(Integer.valueOf(rs.getInt("ZONE_INDEX")));
/*      */         } 
/*      */       
/*      */       } 
/*  978 */       rs.close();
/*  979 */       ps.close();
/*      */       
/*  981 */       map.put("ANALOG", aZoneList);
/*  982 */       map.put("TEMPERATURE", temp24HrZoneList);
/*  983 */       return map;
/*      */     }
/*  985 */     catch (SQLException ex) {
/*  986 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  987 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/*  989 */       throw ex;
/*      */     } finally {
/*  991 */       if (rs != null) {
/*  992 */         rs.close();
/*      */       }
/*  994 */       if (ps != null) {
/*  995 */         ps.close();
/*      */       }
/*  997 */       if (conn != null) {
/*  998 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void disableCommunicationLog(int idModule, Connection conn) throws SQLException {
/* 1005 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/* 1008 */       ps = conn.prepareStatement("UPDATE GRCP_MODULE SET COMM_LOG = ? WHERE ID_MODULE = ?");
/* 1009 */       ps.setInt(1, 0);
/* 1010 */       ps.setInt(2, idModule);
/* 1011 */       ps.executeUpdate();
/* 1012 */       ps.close();
/*      */     }
/* 1014 */     catch (SQLException ex) {
/* 1015 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 1016 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/* 1018 */       throw ex;
/*      */     } finally {
/* 1020 */       if (ps != null) {
/* 1021 */         ps.close();
/*      */       }
/* 1023 */       if (conn != null) {
/* 1024 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void insertEvents(int idModule, int idGroup, int eProtocol, int eventDesc, String account, String rptCode, String partition, String zone, Connection conn) throws SQLException {
/* 1031 */     PreparedStatement ps = null;
/*      */     
/*      */     try {
/* 1034 */       ps = conn.prepareStatement("INSERT INTO GRCP_EVENT (ID_MODULE, RECEIVER_GROUP, EVENT_PROTOCOL, EVENT_TYPE, PARTITION, REPORT_CODE, ZONE_USER, ACCOUNT, OCCURRED, RECEIVED) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
/* 1035 */       ps.setInt(1, idModule);
/* 1036 */       ps.setInt(2, idGroup);
/* 1037 */       ps.setInt(3, eProtocol);
/* 1038 */       ps.setInt(4, eventDesc);
/* 1039 */       ps.setString(5, partition);
/* 1040 */       ps.setString(6, rptCode);
/* 1041 */       ps.setString(7, zone);
/* 1042 */       ps.setString(8, account);
/* 1043 */       ps.executeUpdate();
/* 1044 */       ps.close();
/*      */     }
/* 1046 */     catch (SQLException ex) {
/* 1047 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 1048 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/* 1050 */       throw ex;
/*      */     } finally {
/* 1052 */       if (ps != null) {
/* 1053 */         ps.close();
/*      */       }
/* 1055 */       if (conn != null) {
/* 1056 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static String getSnFromGriffonModule(int idClient, Connection conn) throws SQLException {
/* 1063 */     PreparedStatement ps = null;
/* 1064 */     ResultSet rs = null;
/*      */     
/*      */     try {
/* 1067 */       String iClient = "";
/*      */       
/* 1069 */       ps = conn.prepareStatement("SELECT SN FROM GRCP_MODULE  WHERE ID_CLIENT = ? WITH UR");
/* 1070 */       ps.setInt(1, idClient);
/* 1071 */       rs = ps.executeQuery();
/* 1072 */       if (rs.next()) {
/* 1073 */         iClient = rs.getString("SN");
/*      */       }
/* 1075 */       rs.close();
/* 1076 */       ps.close();
/*      */       
/* 1078 */       return iClient;
/*      */     }
/* 1080 */     catch (SQLException ex) {
/* 1081 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 1082 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/* 1084 */       throw ex;
/*      */     } finally {
/* 1086 */       if (rs != null) {
/* 1087 */         rs.close();
/*      */       }
/* 1089 */       if (ps != null) {
/* 1090 */         ps.close();
/*      */       }
/* 1092 */       if (conn != null) {
/* 1093 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void clearKeyfobReceiverOnUsbHwFail(int idModule, Connection conn) throws SQLException {
/* 1100 */     PreparedStatement ps = null;
/* 1101 */     ResultSet rs = null;
/*      */     
/*      */     try {
/* 1104 */       int hwFail = 0;
/*      */       
/* 1106 */       ps = conn.prepareStatement("SELECT HW_FAIL_STATUS FROM GRCP_MODULE WHERE ID_MODULE = ?");
/* 1107 */       ps.setInt(1, idModule);
/* 1108 */       rs = ps.executeQuery();
/* 1109 */       if (rs.next()) {
/* 1110 */         hwFail = rs.getInt("HW_FAIL_STATUS");
/*      */       }
/* 1112 */       rs.close();
/* 1113 */       ps.close();
/*      */       
/* 1115 */       if ((hwFail & 0x2000) > 0) {
/* 1116 */         ps = conn.prepareStatement("UPDATE GRCP_MODULE SET HW_FAIL_STATUS = ? WHERE ID_MODULE = ?");
/* 1117 */         ps.setInt(1, hwFail & 0xFFFFDFFF);
/* 1118 */         ps.setInt(2, idModule);
/* 1119 */         ps.executeUpdate();
/* 1120 */         ps.close();
/*      */       }
/*      */     
/* 1123 */     } catch (SQLException ex) {
/* 1124 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 1125 */         Logger.getLogger(GriffonQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       }
/* 1127 */       throw ex;
/*      */     } finally {
/* 1129 */       if (rs != null) {
/* 1130 */         rs.close();
/*      */       }
/* 1132 */       if (ps != null) {
/* 1133 */         ps.close();
/*      */       }
/* 1135 */       if (conn != null) {
/* 1136 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void updateGriffonModuleCfg(ModuleCFG moduleCFG, Connection conn) throws SQLException {
/* 1143 */     PreparedStatement ps = null;
/* 1144 */     ResultSet rs = null;
/*      */     
/*      */     try {
/* 1147 */       boolean exists = false;
/*      */       
/* 1149 */       ps = conn.prepareStatement("SELECT COUNT(ID_MODULE) AS CNT FROM GRCP_MODULE_CFG WHERE ID_MODULE = ?");
/* 1150 */       ps.setInt(1, moduleCFG.getIdModule());
/* 1151 */       rs = ps.executeQuery();
/* 1152 */       if (rs.next()) {
/* 1153 */         exists = (rs.getInt("CNT") > 0);
/*      */       }
/* 1155 */       rs.close();
/* 1156 */       ps.close();
/*      */       
/* 1158 */       byte[] cfgData = moduleCFG.getFileData();
/* 1159 */       moduleCFG.setFileData(null);
/* 1160 */       ps = conn.prepareStatement(exists ? "UPDATE GRCP_MODULE_CFG SET MODULE_CFG_DATA = ?, CFG_DATA = ? WHERE ID_MODULE = ?" : "INSERT INTO GRCP_MODULE_CFG(MODULE_CFG_DATA, CFG_DATA, ID_MODULE) VALUES (?,?,?)");
/* 1161 */       ps.setObject(1, moduleCFG);
/* 1162 */       ps.setBinaryStream(2, new ByteArrayInputStream(cfgData));
/* 1163 */       ps.setInt(3, moduleCFG.getIdModule());
/* 1164 */       ps.executeUpdate();
/* 1165 */       ps.close();
/* 1166 */       moduleCFG.setFileData(cfgData);
/*      */       
/* 1168 */       ps = conn.prepareStatement("UPDATE GRCP_MODULE SET CFG_CRC32 = ? WHERE ID_MODULE = ?");
/* 1169 */       ps.setString(1, String.valueOf(moduleCFG.getCrc32()));
/* 1170 */       ps.setInt(2, moduleCFG.getIdModule());
/* 1171 */       ps.executeUpdate();
/* 1172 */       ps.close();
/*      */     } finally {
/*      */       
/* 1175 */       if (rs != null) {
/* 1176 */         rs.close();
/*      */       }
/* 1178 */       if (ps != null) {
/* 1179 */         ps.close();
/*      */       }
/* 1181 */       if (conn != null) {
/* 1182 */         conn.close();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static ModuleCFG readGriffonModuleCfg(int idModule, Connection conn) throws SQLException {
/* 1189 */     PreparedStatement ps = null;
/* 1190 */     ResultSet rs = null;
/*      */     
/*      */     try {
/* 1193 */       ModuleCFG mCFG = null;
/*      */       
/* 1195 */       ps = conn.prepareStatement("SELECT MODULE_CFG_DATA, CFG_DATA FROM GRCP_MODULE_CFG WHERE ID_MODULE = ?");
/* 1196 */       ps.setInt(1, idModule);
/* 1197 */       rs = ps.executeQuery();
/* 1198 */       if (rs.next()) {
/* 1199 */         mCFG = (ModuleCFG)rs.getObject("MODULE_CFG_DATA");
/* 1200 */         mCFG.setFileData(rs.getBytes("CFG_DATA"));
/*      */       } 
/* 1202 */       rs.close();
/* 1203 */       ps.close();
/*      */       
/* 1205 */       return mCFG;
/*      */     } finally {
/*      */       
/* 1208 */       if (rs != null) {
/* 1209 */         rs.close();
/*      */       }
/* 1211 */       if (ps != null) {
/* 1212 */         ps.close();
/*      */       }
/* 1214 */       if (conn != null)
/* 1215 */         conn.close(); 
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\griffon\GriffonQueryHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */