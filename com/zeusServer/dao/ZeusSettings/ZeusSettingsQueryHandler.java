/*     */ package com.zeusServer.dao.ZeusSettings;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.box.ZeusBoxEvents;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusbox.nativeLibrary.ZeusBoxDashBoard;
/*     */ import java.sql.Connection;
/*     */ import java.sql.Date;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Set;
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
/*     */ public class ZeusSettingsQueryHandler
/*     */ {
/*     */   public static void updateZeusEventTransmissionRetries(int eventId, short transmissionRetries, Connection conn) throws SQLException {
/*  34 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/*  37 */       ps = conn.prepareStatement("UPDATE ZEUS_EVENTS SET TRANSMISSION_RETRIES=? WHERE ID_EVENT=?");
/*  38 */       ps.setShort(1, transmissionRetries);
/*  39 */       ps.setInt(2, eventId);
/*  40 */       ps.executeUpdate();
/*  41 */       ps.close();
/*     */     }
/*  43 */     catch (SQLException e) {
/*  44 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  45 */         Logger.getLogger(ZeusSettingsQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  47 */       throw e;
/*     */     } finally {
/*  49 */       if (ps != null) {
/*  50 */         ps.close();
/*     */       }
/*  52 */       if (conn != null) {
/*  53 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void cancelZeusEventTransmission(int eventId, short transmissionRetries, Connection conn) throws SQLException {
/*  60 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/*  63 */       ps = conn.prepareStatement("UPDATE ZEUS_EVENTS SET TRANSMITTED=CURRENT_TIMESTAMP, TRANSMISSION_RETRIES=?, TRANSMISSION_CANCELLED=1 WHERE ID_EVENT=?");
/*  64 */       ps.setShort(1, transmissionRetries);
/*  65 */       ps.setInt(2, eventId);
/*  66 */       ps.executeUpdate();
/*  67 */       ps.close();
/*     */     }
/*  69 */     catch (SQLException e) {
/*  70 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  71 */         Logger.getLogger(ZeusSettingsQueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  73 */       throw e;
/*     */     } finally {
/*  75 */       if (ps != null) {
/*  76 */         ps.close();
/*     */       }
/*  78 */       if (conn != null) {
/*  79 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void insertZeusBoxEvents(List<ZeusBoxEvents> events, Connection conn) throws SQLException {
/*  85 */     PreparedStatement ps = null;
/*  86 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  89 */       int tcount = 0;
/*  90 */       ps = conn.prepareStatement("SELECT COUNT(*) FROM ZEUSBOX_EVENTS");
/*  91 */       rs = ps.executeQuery();
/*  92 */       if (rs.next()) {
/*  93 */         tcount = rs.getInt(1);
/*     */       }
/*  95 */       rs.close();
/*  96 */       ps.close();
/*     */       
/*  98 */       if (tcount + events.size() > 200) {
/*  99 */         int delRecords = tcount + events.size() - 200;
/* 100 */         if (delRecords > 0) {
/* 101 */           ps = conn.prepareStatement("DELETE FROM ZEUSBOX_EVENTS WHERE ID_EVENT IN(SELECT ID_EVENT FROM ZEUSBOX_EVENTS ORDER BY ID_EVENT ASC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY)");
/* 102 */           ps.setInt(1, delRecords);
/* 103 */           ps.executeUpdate();
/* 104 */           ps.close();
/*     */         } 
/*     */       } 
/*     */       
/* 108 */       ps = conn.prepareStatement("INSERT INTO ZEUSBOX_EVENTS (EVENT_DATA, EVENT_RCVD_DATE) VALUES(?,?)");
/* 109 */       for (ZeusBoxEvents event : events) {
/* 110 */         ps.setObject(1, event.getEventData());
/* 111 */         ps.setDate(2, new Date(event.getReceivedDate().getTime()));
/* 112 */         ps.addBatch();
/*     */       } 
/* 114 */       ps.executeBatch();
/* 115 */       ps.close();
/*     */     } finally {
/*     */       
/* 118 */       if (rs != null) {
/* 119 */         rs.close();
/*     */       }
/* 121 */       if (ps != null) {
/* 122 */         ps.close();
/*     */       }
/* 124 */       if (conn != null) {
/* 125 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public static int getProductIdByPhoneNumber(String phoneNumber, Connection conn) throws SQLException {
/* 131 */     PreparedStatement ps = null;
/* 132 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 135 */       int productId = 0;
/*     */       
/* 137 */       ps = conn.prepareStatement("SELECT PRODUCT_ID FROM ALL_PHONE_NUMBERS WHERE PHONE_NUMBER LIKE('%' || ?)");
/* 138 */       ps.setString(1, phoneNumber);
/* 139 */       rs = ps.executeQuery();
/* 140 */       if (rs.next()) {
/* 141 */         productId = rs.getInt("PRODUCT_ID");
/*     */       }
/* 143 */       rs.close();
/* 144 */       ps.close();
/*     */       
/* 146 */       return productId;
/*     */     } finally {
/*     */       
/* 149 */       if (rs != null) {
/* 150 */         rs.close();
/*     */       }
/* 152 */       if (ps != null) {
/* 153 */         ps.close();
/*     */       }
/* 155 */       if (conn != null) {
/* 156 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void clearZeusLiveData(Connection conn) throws SQLException {
/* 163 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 166 */       ps = conn.prepareStatement("UPDATE ZEUS_LIVE_DATA SET CONNECTED_DEVICES = ?, CON_TCP_DEVICES = ?, CON_UDP_DEVICES = ?, PENDING_EVNETS = ?");
/* 167 */       ps.setInt(1, 0);
/* 168 */       ps.setInt(2, 0);
/* 169 */       ps.setInt(3, 0);
/* 170 */       ps.setInt(4, 0);
/* 171 */       ps.executeUpdate();
/* 172 */       ps.close();
/*     */     } finally {
/*     */       
/* 175 */       if (ps != null) {
/* 176 */         ps.close();
/*     */       }
/* 178 */       if (conn != null) {
/* 179 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateZeusLiveData(Connection conn, int productId, int regDevices, int disDevices, int connDevices, int conTCPDevices, int conUDPDevices, int pendingEvents, int pendingAlives) throws SQLException {
/* 186 */     PreparedStatement ps = null;
/* 187 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 190 */       boolean flag = false;
/*     */       
/* 192 */       ps = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM ZEUS_LIVE_DATA WHERE PRODUCT_ID = ?");
/* 193 */       ps.setInt(1, productId);
/* 194 */       rs = ps.executeQuery();
/* 195 */       if (rs.next() && 
/* 196 */         rs.getInt("CNT") > 0) {
/* 197 */         flag = true;
/*     */       }
/*     */       
/* 200 */       rs.close();
/* 201 */       ps.close();
/*     */       
/* 203 */       if (flag) {
/* 204 */         ps = conn.prepareStatement("UPDATE ZEUS_LIVE_DATA SET REG_DEVICES = ?, DISABLE_DEVICES = ?, CONNECTED_DEVICES = ?, CON_TCP_DEVICES = ?, CON_UDP_DEVICES = ?, PENDING_EVNETS = ?, PENDING_ALIVES = ? WHERE PRODUCT_ID = ?");
/* 205 */         ps.setInt(1, regDevices);
/* 206 */         ps.setInt(2, disDevices);
/* 207 */         ps.setInt(3, connDevices);
/* 208 */         ps.setInt(4, conTCPDevices);
/* 209 */         ps.setInt(5, conUDPDevices);
/* 210 */         ps.setInt(6, pendingEvents);
/* 211 */         ps.setInt(7, pendingAlives);
/* 212 */         ps.setInt(8, productId);
/*     */       } else {
/* 214 */         ps = conn.prepareStatement("INSERT INTO ZEUS_LIVE_DATA (REG_DEVICES, DISABLE_DEVICES, CONNECTED_DEVICES, CON_TCP_DEVICES, CON_UDP_DEVICES, PENDING_EVNETS, PENDING_ALIVES, PRODUCT_ID) VALUES (?,?,?,?,?,?,?,?)");
/* 215 */         ps.setInt(1, regDevices);
/* 216 */         ps.setInt(2, disDevices);
/* 217 */         ps.setInt(3, connDevices);
/* 218 */         ps.setInt(4, conTCPDevices);
/* 219 */         ps.setInt(5, conUDPDevices);
/* 220 */         ps.setInt(6, pendingEvents);
/* 221 */         ps.setInt(7, pendingAlives);
/* 222 */         ps.setInt(8, productId);
/*     */       } 
/* 224 */       ps.executeUpdate();
/* 225 */       ps.close();
/*     */     } finally {
/*     */       
/* 228 */       if (rs != null) {
/* 229 */         rs.close();
/*     */       }
/* 231 */       if (ps != null) {
/* 232 */         ps.close();
/*     */       }
/* 234 */       if (conn != null) {
/* 235 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public static List<ZeusBoxEvents> getZeusBoxEvents(Connection conn) throws SQLException {
/* 241 */     PreparedStatement ps = null;
/* 242 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 245 */       List<ZeusBoxEvents> events = new ArrayList<>();
/*     */       
/* 247 */       ps = conn.prepareStatement("SELECT * FROM ZEUSBOX_EVENTS ORDER BY EVENT_RCVD_DATE ASC");
/* 248 */       rs = ps.executeQuery();
/* 249 */       while (rs.next()) {
/* 250 */         events.add(new ZeusBoxEvents((List)rs.getObject("EVENT_DATA"), new Date(rs.getDate("EVENT_RCVD_DATE").getTime()), true));
/*     */       }
/* 252 */       rs.close();
/* 253 */       ps.close();
/*     */       
/* 255 */       return events;
/*     */     } finally {
/*     */       
/* 258 */       if (rs != null) {
/* 259 */         rs.close();
/*     */       }
/* 261 */       if (ps != null) {
/* 262 */         ps.close();
/*     */       }
/* 264 */       if (conn != null) {
/* 265 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void updateZeusSettings(int settingsId, Set<String> portSet, ZeusBoxDashBoard dashboard, Connection conn) throws SQLException {
/* 271 */     PreparedStatement ps = null;
/*     */     
/*     */     try {
/* 274 */       if (settingsId == 0 && dashboard != null) {
/*     */         
/* 276 */         ps = conn.prepareStatement("DELETE FROM PROPERTIES WHERE PROP_NAME IN (" + Util.EnumZeusSettingsPropNames.ZEUSBOX_CPU_USAGE.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_RAM_USAGE.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_TOTAL_RAM.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_TOTAL_HD_SPACE.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_HD_USAGE.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_ETH_0.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_ETH_1.getId() + "," + Util.EnumZeusSettingsPropNames.ZEUSBOX_TEMPERATURE.getId() + ")");
/* 277 */         ps.executeUpdate();
/* 278 */         ps.close();
/*     */         
/* 280 */         ps = conn.prepareStatement("INSERT INTO PROPERTIES(PROP_NAME, PROP_VALUE) VALUES(?,?)");
/* 281 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_CPU_USAGE.getId());
/* 282 */         ps.setString(2, String.valueOf(dashboard.getCpuUsage()));
/* 283 */         ps.addBatch();
/* 284 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_RAM_USAGE.getId());
/* 285 */         ps.setString(2, String.valueOf(dashboard.getRamUsage()));
/* 286 */         ps.addBatch();
/* 287 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_TOTAL_RAM.getId());
/* 288 */         ps.setString(2, String.valueOf(dashboard.getTotalRAM()));
/* 289 */         ps.addBatch();
/* 290 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_TOTAL_HD_SPACE.getId());
/* 291 */         ps.setString(2, String.valueOf(dashboard.getTotalHDSpace()));
/* 292 */         ps.addBatch();
/* 293 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_HD_USAGE.getId());
/* 294 */         ps.setString(2, String.valueOf(dashboard.getHdUsage()));
/* 295 */         ps.addBatch();
/* 296 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_ETH_0.getId());
/* 297 */         ps.setString(2, String.valueOf(dashboard.getEth0()));
/* 298 */         ps.addBatch();
/* 299 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_ETH_1.getId());
/* 300 */         ps.setString(2, String.valueOf(dashboard.getEth1()));
/* 301 */         ps.addBatch();
/* 302 */         ps.setInt(1, Util.EnumZeusSettingsPropNames.ZEUSBOX_TEMPERATURE.getId());
/* 303 */         ps.setString(2, String.valueOf(dashboard.getBoxTemprature()));
/* 304 */         ps.addBatch();
/* 305 */         ps.executeBatch();
/* 306 */         ps.close();
/* 307 */       } else if (settingsId == Util.EnumZeusSettingsPropNames.AVAILABLE_SERIALPORTS.getId()) {
/* 308 */         ps = conn.prepareStatement("DELETE FROM PROPERTIES WHERE PROP_NAME = ?");
/* 309 */         ps.setInt(1, settingsId);
/* 310 */         ps.executeUpdate();
/* 311 */         ps.close();
/*     */         
/* 313 */         ps = conn.prepareStatement("INSERT INTO PROPERTIES(PROP_NAME, PROP_VALUE) VALUES(?,?)");
/* 314 */         for (String port : portSet) {
/* 315 */           ps.setInt(1, settingsId);
/* 316 */           ps.setString(2, port);
/* 317 */           ps.addBatch();
/*     */         } 
/* 319 */         if (portSet.size() > 0) {
/* 320 */           ps.executeBatch();
/*     */         }
/*     */       } 
/* 323 */     } catch (SQLException ex) {
/* 324 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 325 */         Logger.getLogger(ZeusSettingsQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 327 */       throw ex;
/*     */     } finally {
/* 329 */       if (ps != null) {
/* 330 */         ps.close();
/*     */       }
/* 332 */       if (conn != null) {
/* 333 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<EventDataHolder> getNonProcessedZeusEvents(Connection conn) throws SQLException {
/* 340 */     PreparedStatement ps = null;
/* 341 */     ResultSet rs = null;
/*     */     
/*     */     try {
/* 344 */       List<EventDataHolder> edhList = new ArrayList<>();
/*     */       
/* 346 */       ps = conn.prepareStatement(" SELECT ID_EVENT, RECEIVER_GROUP, EVENT_PROTOCOL,EVENT_DATA,TRANSMISSION_RETRIES FROM ZEUS_EVENTS WHERE ((TRANSMITTED is NULL) AND (TRANSMISSION_CANCELLED = 0)) ORDER BY RECEIVED ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY WITH UR");
/* 347 */       ps.setInt(1, 0);
/* 348 */       ps.setInt(2, 1000);
/* 349 */       rs = ps.executeQuery();
/* 350 */       while (rs.next()) {
/* 351 */         EventDataHolder edh = new EventDataHolder();
/* 352 */         edh.setId_Event(rs.getInt("ID_EVENT"));
/* 353 */         edh.setIdGroup(rs.getInt("RECEIVER_GROUP"));
/* 354 */         edh.setEvent_Protocol(rs.getShort("EVENT_PROTOCOL"));
/* 355 */         edh.setEventBuffer(rs.getBytes("EVENT_DATA"));
/* 356 */         edh.setTransmission_Retries(rs.getShort("TRANSMISSION_RETRIES"));
/* 357 */         edh.setProductId(Util.EnumProductIDs.ZEUS.getProductId());
/* 358 */         edhList.add(edh);
/*     */       } 
/* 360 */       rs.close();
/* 361 */       ps.close();
/*     */       
/* 363 */       return edhList;
/*     */     }
/* 365 */     catch (SQLException ex) {
/* 366 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 367 */         Logger.getLogger(ZeusSettingsQueryHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       }
/* 369 */       throw ex;
/*     */     } finally {
/* 371 */       if (rs != null) {
/* 372 */         rs.close();
/*     */       }
/* 374 */       if (ps != null) {
/* 375 */         ps.close();
/*     */       }
/* 377 */       if (conn != null)
/* 378 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\ZeusSettings\ZeusSettingsQueryHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */