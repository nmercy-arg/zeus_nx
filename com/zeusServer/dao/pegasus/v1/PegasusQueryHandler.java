/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
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
/*     */ public class PegasusQueryHandler
/*     */ {
/*     */   private static long nextPrintTimeFetchingPendingAlivePackets;
/*     */   
/*     */   public static List<PendingDataHolder> getAllPendingAlive(Connection conn) throws SQLException {
/*  50 */     PreparedStatement ps = null;
/*  51 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  54 */       int limit = (GlobalVariables.currentPlatform == Enums.Platform.ARM) ? 100 : 250;
/*  55 */       List<PendingDataHolder> dataList = new ArrayList<>(limit);
/*  56 */       long startFetchingPendingAlivePackets = System.currentTimeMillis();
/*     */       
/*  58 */       ps = conn.prepareStatement("SELECT ID_CLIENT, ID_MODULE, ID_PENDING_DATA_FIELD, RECEIVED, VERSION, LAST_COMM_INTERFACE, MIN_GSM_SIGNAL_LEVEL, LAST_NW_PROTOCOL, CONTENT  FROM PENDING_DATA_FIELDS ORDER BY RECEIVED ASC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
/*  59 */       ps.setInt(1, limit);
/*  60 */       rs = ps.executeQuery();
/*  61 */       while (rs.next()) {
/*  62 */         PendingDataHolder pdh = new PendingDataHolder();
/*  63 */         pdh.setIdClient(rs.getInt("ID_CLIENT"));
/*  64 */         pdh.setIdModule(rs.getInt("ID_MODULE"));
/*  65 */         pdh.setIdPendingAlive(rs.getInt("ID_PENDING_DATA_FIELD"));
/*  66 */         pdh.setReceived(rs.getTimestamp("RECEIVED", Calendar.getInstance(TimeZone.getTimeZone("GMT"))));
/*  67 */         pdh.setContent(rs.getBytes("CONTENT"));
/*  68 */         pdh.setVersion(rs.getShort("VERSION"));
/*  69 */         pdh.setLastCommInterface(rs.getShort("LAST_COMM_INTERFACE"));
/*  70 */         pdh.setMinGsmSignalLevel(rs.getShort("MIN_GSM_SIGNAL_LEVEL"));
/*  71 */         pdh.setLastNWProtocol(rs.getString("LAST_NW_PROTOCOL"));
/*  72 */         dataList.add(pdh);
/*     */       } 
/*  74 */       rs.close();
/*  75 */       ps.close();
/*     */       
/*  77 */       if (nextPrintTimeFetchingPendingAlivePackets < System.currentTimeMillis()) {
/*  78 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Time_fetch_Pending_Alive_Packets") + (System.currentTimeMillis() - startFetchingPendingAlivePackets) + "ms", Enums.EnumMessagePriority.HIGH, null, null);
/*  79 */         nextPrintTimeFetchingPendingAlivePackets = System.currentTimeMillis() + 5000L;
/*     */       } 
/*     */       
/*  82 */       return dataList;
/*     */     }
/*  84 */     catch (SQLException ex) {
/*  85 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  86 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/*  88 */       throw ex;
/*     */     } finally {
/*  90 */       if (rs != null) {
/*  91 */         rs.close();
/*     */       }
/*  93 */       if (ps != null) {
/*  94 */         ps.close();
/*     */       }
/*  96 */       if (conn != null) {
/*  97 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean isCommandCancelled(int commandId, Connection conn) throws SQLException {
/* 104 */     PreparedStatement ps = null;
/* 105 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 108 */       boolean isCancel = false;
/*     */       
/* 110 */       ps = conn.prepareStatement("SELECT CANCELLED FROM COMMAND WHERE ID_COMMAND=?");
/* 111 */       ps.setInt(1, commandId);
/* 112 */       rs = ps.executeQuery();
/* 113 */       if (rs.next()) {
/* 114 */         isCancel = (rs.getTimestamp("CANCELLED") != null);
/*     */       }
/* 116 */       rs.close();
/* 117 */       ps.close();
/*     */       
/* 119 */       return isCancel;
/*     */     }
/* 121 */     catch (SQLException ex) {
/* 122 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 123 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 125 */       throw ex;
/*     */     } finally {
/* 127 */       if (rs != null) {
/* 128 */         rs.close();
/*     */       }
/* 130 */       if (ps != null) {
/* 131 */         ps.close();
/*     */       }
/* 133 */       if (conn != null) {
/* 134 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateCommandStatus(int commandId, Connection conn) throws SQLException {
/* 141 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 144 */       ps = conn.prepareStatement("UPDATE COMMAND SET IN_PROGRESS = 1 WHERE ID_COMMAND = ?");
/* 145 */       ps.setInt(1, commandId);
/* 146 */       ps.executeUpdate();
/* 147 */       ps.close();
/*     */     }
/* 149 */     catch (SQLException ex) {
/* 150 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 151 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 153 */       throw ex;
/*     */     } finally {
/* 155 */       if (ps != null) {
/* 156 */         ps.close();
/*     */       }
/* 158 */       if (conn != null) {
/* 159 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateCommandFailureStatus(int commandId, int eRetries, String content, Connection conn) throws SQLException {
/* 166 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 169 */       ps = conn.prepareStatement("UPDATE COMMAND SET TRANSMITTED=CURRENT_TIMESTAMP, EXEC_RETRIES =?, EXEC_CANCELLED=1, IN_PROGRESS = 0, COMMAND_DATA =? WHERE ID_COMMAND=?");
/* 170 */       ps.setInt(1, eRetries);
/* 171 */       ps.setBinaryStream(2, new ByteArrayInputStream(content.getBytes()));
/* 172 */       ps.setInt(3, commandId);
/* 173 */       ps.executeUpdate();
/* 174 */       ps.close();
/*     */     }
/* 176 */     catch (SQLException ex) {
/* 177 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 178 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 180 */       throw ex;
/*     */     } finally {
/* 182 */       if (ps != null) {
/* 183 */         ps.close();
/*     */       }
/* 185 */       if (conn != null) {
/* 186 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateSIMCardICCID(int idModule, String iccid1, String iccid2, Connection conn) throws SQLException {
/* 193 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 196 */       iccid1 = (iccid1 == null) ? "00000000000000000000" : ((iccid1.trim().length() > 0) ? iccid1 : "00000000000000000000");
/* 197 */       iccid2 = (iccid2 == null) ? "00000000000000000000" : ((iccid2.trim().length() > 0) ? iccid2 : "00000000000000000000");
/* 198 */       ps = conn.prepareStatement("UPDATE MODULE SET SIMCARD1_ICCID = ?, SIMCARD2_ICCID = ? WHERE ID_MODULE = ?");
/* 199 */       ps.setString(1, iccid1);
/* 200 */       ps.setString(2, iccid2);
/* 201 */       ps.setInt(3, idModule);
/* 202 */       ps.executeUpdate();
/* 203 */       ps.close();
/*     */     }
/* 205 */     catch (SQLException ex) {
/* 206 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 207 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 209 */       throw ex;
/*     */     } finally {
/* 211 */       if (ps != null) {
/* 212 */         ps.close();
/*     */       }
/* 214 */       if (conn != null) {
/* 215 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void disableCommunicationLog(int idModule, Connection conn) throws SQLException {
/* 222 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 225 */       ps = conn.prepareStatement("UPDATE MODULE SET COMM_LOG=0 WHERE ID_MODULE=?");
/* 226 */       ps.setInt(1, idModule);
/* 227 */       ps.executeUpdate();
/* 228 */       ps.close();
/*     */     }
/* 230 */     catch (SQLException ex) {
/* 231 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 232 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 234 */       throw ex;
/*     */     } finally {
/* 236 */       if (ps != null) {
/* 237 */         ps.close();
/*     */       }
/* 239 */       if (conn != null) {
/* 240 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<EventDataHolder> getNonProcessedEvents(Connection conn) throws SQLException {
/* 247 */     PreparedStatement ps = null;
/* 248 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 251 */       List<EventDataHolder> edhList = new ArrayList<>();
/*     */       
/* 253 */       ps = conn.prepareStatement("SELECT ID_EVENT, RECEIVER_GROUP, EVENT_PROTOCOL,EVENT_DATA,TRANSMISSION_RETRIES FROM EVENT WHERE ((TRANSMITTED is NULL) AND (TRANSMISSION_CANCELLED = 0)) ORDER BY RECEIVED ASC OFFSET 0 ROWS FETCH NEXT 1000 ROWS ONLY WITH UR");
/* 254 */       rs = ps.executeQuery();
/* 255 */       while (rs.next()) {
/* 256 */         EventDataHolder edh = new EventDataHolder();
/* 257 */         edh.setId_Event(rs.getInt("ID_EVENT"));
/* 258 */         edh.setIdGroup(rs.getInt("RECEIVER_GROUP"));
/* 259 */         edh.setEvent_Protocol(rs.getShort("EVENT_PROTOCOL"));
/* 260 */         edh.setEventBuffer(rs.getBytes("EVENT_DATA"));
/* 261 */         edh.setTransmission_Retries(rs.getShort("TRANSMISSION_RETRIES"));
/* 262 */         edh.setProductId(Util.EnumProductIDs.PEGASUS.getProductId());
/* 263 */         edhList.add(edh);
/*     */       } 
/* 265 */       rs.close();
/* 266 */       ps.close();
/*     */       
/* 268 */       return edhList;
/*     */     }
/* 270 */     catch (SQLException ex) {
/* 271 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 272 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 274 */       throw ex;
/*     */     } finally {
/* 276 */       if (rs != null) {
/* 277 */         rs.close();
/*     */       }
/* 279 */       if (ps != null) {
/* 280 */         ps.close();
/*     */       }
/* 282 */       if (conn != null) {
/* 283 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> getGroupIds(Connection conn) throws SQLException {
/* 290 */     PreparedStatement ps = null;
/* 291 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 294 */       List<Integer> gList = new ArrayList<>();
/*     */       
/* 296 */       ps = conn.prepareStatement("SELECT ID_CLIENT FROM CLIENT C1 WHERE C1.CLIENT_TYPE = 3");
/* 297 */       rs = ps.executeQuery();
/* 298 */       while (rs.next()) {
/* 299 */         gList.add(Integer.valueOf(rs.getInt("ID_CLIENT")));
/*     */       }
/* 301 */       rs.close();
/* 302 */       ps.close();
/*     */       
/* 304 */       return gList;
/*     */     }
/* 306 */     catch (SQLException ex) {
/* 307 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 308 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 310 */       throw ex;
/*     */     } finally {
/* 312 */       if (rs != null) {
/* 313 */         rs.close();
/*     */       }
/* 315 */       if (ps != null) {
/* 316 */         ps.close();
/*     */       }
/* 318 */       if (conn != null) {
/* 319 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static String getSnFromPegausModule(int idClient, Connection conn) throws SQLException {
/* 326 */     PreparedStatement ps = null;
/* 327 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 330 */       String sn = "";
/*     */       
/* 332 */       ps = conn.prepareStatement("SELECT SN FROM MODULE  WHERE ID_CLIENT = ? WITH UR");
/* 333 */       ps.setInt(1, idClient);
/* 334 */       rs = ps.executeQuery();
/* 335 */       if (rs.next()) {
/* 336 */         sn = rs.getString("SN");
/*     */       }
/* 338 */       rs.close();
/* 339 */       ps.close();
/*     */       
/* 341 */       return sn;
/*     */     }
/* 343 */     catch (SQLException ex) {
/* 344 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 345 */         Logger.getLogger(PegasusQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 347 */       throw ex;
/*     */     } finally {
/* 349 */       if (rs != null) {
/* 350 */         rs.close();
/*     */       }
/* 352 */       if (ps != null) {
/* 353 */         ps.close();
/*     */       }
/* 355 */       if (conn != null)
/* 356 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusQueryHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */