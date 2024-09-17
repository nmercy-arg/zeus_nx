/*     */ package com.zeusServer.DBManagers;
/*     */ 
/*     */ import com.zeusServer.box.ZeusBoxEvents;
/*     */ import com.zeusServer.dao.ZeusSettings.ZeusSettingsQueryHandler;
/*     */ import com.zeusServer.dao.ZeusSettings.ZeusSettingsSPHandler;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import com.zeusServer.util.LoggedInUser;
/*     */ import com.zeusbox.nativeLibrary.ZeusBoxDashBoard;
/*     */ import java.sql.SQLException;
/*     */ import java.util.List;
/*     */ import java.util.Set;
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
/*     */ public class ZeusSettingsDBManager
/*     */ {
/*     */   public static LoggedInUser executeSP_S004(String userName, String password) throws SQLException, InterruptedException {
/*  28 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  30 */         return ZeusSettingsSPHandler.executeSP_S004(userName, password, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*  31 */       } catch (SQLException ex) {
/*  32 */         if (retries == 3) {
/*  33 */           throw ex;
/*     */         }
/*  35 */         GenericDBManager.checkDerbyService();
/*  36 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/*  40 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_S006(int idModule, int rcvrGroup, String rcvrCOMPort, short protocol, String clientCode, byte[] eventData, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface, int isMonitorEvent) throws SQLException, InterruptedException {
/*  45 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  47 */         ZeusSettingsSPHandler.executeSP_S006(idModule, rcvrGroup, rcvrCOMPort, protocol, clientCode, eventData, lastMProtocolRcvd, nwProtocol, lastCommInterface, isMonitorEvent, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*     */         break;
/*  49 */       } catch (SQLException ex) {
/*  50 */         if (retries == 3) {
/*  51 */           throw ex;
/*     */         }
/*  53 */         GenericDBManager.checkDerbyService();
/*  54 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void executeSP_S007(int idEvent) throws SQLException, InterruptedException {
/*  62 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  64 */         ZeusSettingsSPHandler.executeSP_S007(idEvent, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*     */         break;
/*  66 */       } catch (SQLException ex) {
/*  67 */         if (retries == 3) {
/*  68 */           throw ex;
/*     */         }
/*  70 */         GenericDBManager.checkDerbyService();
/*  71 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void insertZeusBoxEvents(List<ZeusBoxEvents> events) throws SQLException, InterruptedException {
/*  79 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  81 */         ZeusSettingsQueryHandler.insertZeusBoxEvents(events, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*     */         break;
/*  83 */       } catch (SQLException ex) {
/*  84 */         if (retries == 3) {
/*  85 */           throw ex;
/*     */         }
/*  87 */         GenericDBManager.checkDerbyService();
/*  88 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateZeusSettings(int settingsId, Set<String> portSet, ZeusBoxDashBoard dashboard) throws SQLException, InterruptedException {
/*  96 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  98 */         ZeusSettingsQueryHandler.updateZeusSettings(settingsId, portSet, dashboard, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*     */         break;
/* 100 */       } catch (SQLException ex) {
/* 101 */         if (retries == 3) {
/* 102 */           throw ex;
/*     */         }
/* 104 */         GenericDBManager.checkDerbyService();
/* 105 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static int getProductIdByPhoneNumber(String phoneNumber) throws SQLException, InterruptedException {
/* 113 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 115 */         return ZeusSettingsQueryHandler.getProductIdByPhoneNumber(phoneNumber, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/* 116 */       } catch (SQLException ex) {
/* 117 */         if (retries == 3) {
/* 118 */           throw ex;
/*     */         }
/* 120 */         GenericDBManager.checkDerbyService();
/* 121 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 125 */     return 0;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void updateZeusLiveData(int productId, int regDevices, int disDevices, int connDevices, int conTCPDevices, int conUDPDevices, int pendingEvents, int pendingAlives) throws SQLException, InterruptedException {
/* 130 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 132 */         ZeusSettingsQueryHandler.updateZeusLiveData(GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false), productId, regDevices, disDevices, connDevices, conTCPDevices, conUDPDevices, pendingEvents, pendingAlives);
/* 133 */       } catch (SQLException ex) {
/* 134 */         if (retries == 3) {
/* 135 */           throw ex;
/*     */         }
/* 137 */         GenericDBManager.checkDerbyService();
/* 138 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static List<ZeusBoxEvents> getZeusBoxEvents() throws SQLException, InterruptedException {
/* 146 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 148 */         return ZeusSettingsQueryHandler.getZeusBoxEvents(GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/* 149 */       } catch (SQLException ex) {
/* 150 */         if (retries == 3) {
/* 151 */           throw ex;
/*     */         }
/* 153 */         GenericDBManager.checkDerbyService();
/* 154 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 158 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void clearZeusLiveData() throws SQLException, InterruptedException {
/* 163 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 165 */         ZeusSettingsQueryHandler.clearZeusLiveData(GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/* 166 */       } catch (SQLException ex) {
/* 167 */         if (retries == 3) {
/* 168 */           throw ex;
/*     */         }
/* 170 */         GenericDBManager.checkDerbyService();
/* 171 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void cancelZeusEventTransmission(int eventId, short transmissionRetries) throws SQLException, InterruptedException {
/* 179 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 181 */         ZeusSettingsQueryHandler.cancelZeusEventTransmission(eventId, transmissionRetries, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*     */         break;
/* 183 */       } catch (SQLException ex) {
/* 184 */         if (retries == 3) {
/* 185 */           throw ex;
/*     */         }
/* 187 */         GenericDBManager.checkDerbyService();
/* 188 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateZeusEventTransmissionRetries(int eventId, short transmissionRetries) throws SQLException, InterruptedException {
/* 196 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 198 */         ZeusSettingsQueryHandler.updateZeusEventTransmissionRetries(eventId, transmissionRetries, GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/*     */         break;
/* 200 */       } catch (SQLException ex) {
/* 201 */         if (retries == 3) {
/* 202 */           throw ex;
/*     */         }
/* 204 */         GenericDBManager.checkDerbyService();
/* 205 */         Thread.sleep(5000L);
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
/*     */   public static List<EventDataHolder> getNonProcessedZeusEvents() throws SQLException, InterruptedException {
/* 218 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 220 */         return ZeusSettingsQueryHandler.getNonProcessedZeusEvents(GenericDBManager.getConnectionBySchemaName("ZEUSSETTINGS", false));
/* 221 */       } catch (SQLException ex) {
/* 222 */         if (retries == 3) {
/* 223 */           throw ex;
/*     */         }
/* 225 */         GenericDBManager.checkDerbyService();
/* 226 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 230 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBManagers\ZeusSettingsDBManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */