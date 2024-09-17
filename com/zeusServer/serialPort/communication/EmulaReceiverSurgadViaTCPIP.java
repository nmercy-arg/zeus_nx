/*     */ package com.zeusServer.serialPort.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*     */ import com.zeusServer.ui.UILogInitiator;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.MonitoringGroupInfo;
/*     */ import com.zeusServer.util.MonitoringInfo;
/*     */ import com.zeusServer.util.SocketFunctions;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.PriorityBlockingQueue;
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
/*     */ public class EmulaReceiverSurgadViaTCPIP
/*     */   extends EmulateReceiver
/*     */ {
/*  44 */   private Socket sck = null;
/*  45 */   private byte[] bufferRx = new byte[1];
/*     */   
/*     */   public EmulaReceiverSurgadViaTCPIP(MonitoringInfo mInfo) {
/*  48 */     super(mInfo);
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  53 */     long nextExecutionThread = 0L;
/*  54 */     long nextDispatchHeartBeat = 0L;
/*     */     
/*  56 */     Thread.currentThread().setPriority(10);
/*  57 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("SUR-GARD_(via_TCP/IP)_emulation_task_started") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/*     */     try {
/*  59 */       while (this.flag) {
/*     */         try {
/*  61 */           if (nextExecutionThread < System.currentTimeMillis()) {
/*  62 */             fetchNonProcessedEventsFromQueue();
/*  63 */             nextExecutionThread = System.currentTimeMillis() + 100L;
/*     */           } 
/*  65 */           if (nextDispatchHeartBeat < System.currentTimeMillis() && this.onlineFlag) {
/*  66 */             boolean heartBeatResponseStatus = sendHeartBeat_Surgard_TcpIp();
/*  67 */             changeOnline(heartBeatResponseStatus);
/*  68 */             UILogInitiator.toggleImageById((short)1, heartBeatResponseStatus, this.port);
/*  69 */             nextDispatchHeartBeat = System.currentTimeMillis() + 30000L;
/*     */           } 
/*  71 */         } catch (InterruptedException|SQLException ex) {
/*  72 */           ex.printStackTrace();
/*  73 */           if (!DerbyDBBackup.backupModeActivated) {
/*  74 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, ex);
/*  75 */             GlobalVariables.buzzerActivated = true;
/*     */           } 
/*     */         } 
/*  78 */         this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */       } 
/*  80 */     } catch (Exception ex) {
/*  81 */       if (!DerbyDBBackup.backupModeActivated) {
/*  82 */         ex.printStackTrace();
/*  83 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, ex);
/*  84 */         GlobalVariables.buzzerActivated = true;
/*     */       } 
/*     */     } finally {
/*  87 */       dispose();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected boolean processReplyMonitoringSoftware(int intRC, EventDataHolder edh) throws SQLException, InterruptedException {
/*  93 */     switch (intRC) {
/*     */       case 6:
/*  95 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Received_ACK_from_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/*  96 */         if (edh.getProductId() == Util.EnumProductIDs.ZEUS.getProductId()) {
/*  97 */           ZeusSettingsDBManager.executeSP_S007(edh.getId_Event());
/*     */         }
/*  99 */         else if (this.mInfo.getDeleteEventAfterTransmission()) {
/* 100 */           GenericDBManager.executeSP_008(edh.getId_Event(), edh.getProductId());
/*     */         } else {
/* 102 */           GenericDBManager.executeSP_016(edh.getId_Event(), edh.getProductId());
/*     */         } 
/*     */         
/* 105 */         if (this.mInfo.getBeepAfterEventTransmission()) {
/* 106 */           Functions.eventTransmissionBeep();
/*     */         }
/* 108 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Event_sent_successfully_to_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(edh.getEventBuffer(), null) }), Enums.EnumMessagePriority.LOW, null, null);
/* 109 */         changeOnline(true);
/* 110 */         UILogInitiator.toggleImageById((short)1, true, this.port);
/* 111 */         return true;
/*     */       case 21:
/* 113 */         if (edh.getProductId() == Util.EnumProductIDs.ZEUS.getProductId()) {
/* 114 */           if (edh.getTransmission_Retries() + 1 >= 3) {
/* 115 */             ZeusSettingsDBManager.cancelZeusEventTransmission(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1));
/*     */           } else {
/* 117 */             ZeusSettingsDBManager.updateZeusEventTransmissionRetries(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1));
/*     */           }
/*     */         
/* 120 */         } else if (edh.getTransmission_Retries() + 1 >= 3) {
/* 121 */           GenericDBManager.executeSP_005(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1), edh.getProductId());
/*     */         } else {
/* 123 */           GenericDBManager.executeSP_018(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1), edh.getProductId());
/*     */         } 
/*     */         
/* 126 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Event_was_not_recognized_by_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(edh.getEventBuffer(), null) }), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 127 */         changeOnline(false);
/* 128 */         return true;
/*     */     } 
/* 130 */     if (this.onlineFlag) {
/* 131 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("The_Zeus_Server_lost_communication_with_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/* 132 */       GlobalVariables.buzzerActivated = true;
/*     */     } 
/* 134 */     changeOnline(false);
/* 135 */     UILogInitiator.toggleImageById((short)1, false, this.port);
/* 136 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void fetchNonProcessedEventsFromQueue() throws SQLException, InterruptedException {
/* 142 */     this.newEvent = false;
/*     */     
/* 144 */     if (this.mInfo != null && this.mInfo.getAssignedGroupsByProduct() != null) {
/* 145 */       this.edhList = new ArrayList<>();
/* 146 */       List<EventDataHolder> dummyList = new ArrayList<>();
/* 147 */       for (Map.Entry<String, List<MonitoringGroupInfo>> entry : (Iterable<Map.Entry<String, List<MonitoringGroupInfo>>>)this.mInfo.getAssignedGroupsByProduct().entrySet()) {
/* 148 */         if (EventLoader.nonProcessedEventData.containsKey(entry.getKey())) {
/* 149 */           ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> peData = EventLoader.nonProcessedEventData.get(entry.getKey());
/* 150 */           for (MonitoringGroupInfo mgi : entry.getValue()) {
/* 151 */             if (peData.containsKey(Integer.valueOf(mgi.getGroupId()))) {
/* 152 */               int size = (((PriorityBlockingQueue)peData.get(Integer.valueOf(mgi.getGroupId()))).size() > 1000) ? 1000 : ((PriorityBlockingQueue)peData.get(Integer.valueOf(mgi.getGroupId()))).size();
/* 153 */               PriorityBlockingQueue<EventDataHolder> dummy = peData.get(Integer.valueOf(mgi.getGroupId()));
/* 154 */               Iterator<EventDataHolder> itr = dummy.iterator();
/* 155 */               int count = 0;
/* 156 */               while (itr.hasNext() && count < size) {
/* 157 */                 EventDataHolder event = itr.next();
/* 158 */                 this.edhList.add(event);
/* 159 */                 count++;
/* 160 */                 ((PriorityBlockingQueue<EventDataHolder>)peData.get(Integer.valueOf(mgi.getGroupId()))).drainTo(dummyList, 1);
/*     */               } 
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 167 */       if (EventLoader.nonProcessedEventData.containsKey("ZEUSSETTINGS")) {
/* 168 */         ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> peData = EventLoader.nonProcessedEventData.get("ZEUSSETTINGS");
/* 169 */         if (peData.containsKey(Integer.valueOf(0))) {
/* 170 */           int size = (((PriorityBlockingQueue)peData.get(Integer.valueOf(0))).size() > 1000) ? 1000 : ((PriorityBlockingQueue)peData.get(Integer.valueOf(0))).size();
/* 171 */           PriorityBlockingQueue<EventDataHolder> dummy = peData.get(Integer.valueOf(0));
/* 172 */           Iterator<EventDataHolder> itr = dummy.iterator();
/* 173 */           int count = 0;
/* 174 */           while (itr.hasNext() && count < size) {
/* 175 */             EventDataHolder event = itr.next();
/* 176 */             this.edhList.add(event);
/* 177 */             count++;
/* 178 */             ((PriorityBlockingQueue<EventDataHolder>)peData.get(Integer.valueOf(0))).drainTo(dummyList, 1);
/*     */           } 
/*     */         } 
/*     */       } 
/* 182 */       if (this.edhList != null) {
/* 183 */         if (this.edhList.isEmpty()) {
/* 184 */           this.onlineFlag = true;
/*     */         }
/* 186 */         for (EventDataHolder edh : this.edhList) {
/*     */           
/* 188 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_PEGASUS_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_GRCP_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_AVL_EVENT_TABLE) {
/* 189 */             this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */             
/*     */             continue;
/*     */           } 
/* 193 */           if (!processReplyMonitoringSoftware(sendEvent_Surgard_TcpIp(edh.getEvent_Protocol(), edh.getEventBuffer()), edh)) {
/*     */             break;
/*     */           }
/* 196 */           this.wdt = Functions.updateWatchdog(this.wdt, this.mInfo.getEventsTimeGap().intValue());
/*     */         } 
/*     */       } else {
/* 199 */         this.onlineFlag = true;
/*     */       } 
/*     */     } else {
/* 202 */       this.edhList = null;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean sendHeartBeat_Surgard_TcpIp() {
/* 210 */     int retries = 0;
/* 211 */     boolean connectionFlag = false;
/* 212 */     while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/*     */       try {
/* 214 */         if (establishConnectionMonitoringSoftware(retries, "Heartbeat")) {
/* 215 */           connectionFlag = true;
/* 216 */           SocketFunctions.send(this.sck, "1011           @    \024");
/* 217 */           if (this.mInfo.getWaitEventAck()) {
/* 218 */             this.bufferRx = SocketFunctions.receive(this.sck, 0, 1);
/* 219 */             if (this.bufferRx.length > 0) {
/* 220 */               UILogInitiator.toggleImageById((short)1, true, this.port);
/* 221 */               if (this.bufferRx[0] == this.mInfo.getAckByte()) {
/* 222 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 223 */                 return true;
/*     */               } 
/* 225 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + this.bufferRx[0], Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */             } 
/*     */           } else {
/*     */             
/* 229 */             UILogInitiator.toggleImageById((short)1, true, this.port);
/* 230 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 231 */             return true;
/*     */           } 
/*     */         } 
/* 234 */       } catch (SocketTimeoutException socketTimeoutException) {
/*     */       
/* 236 */       } catch (IOException|InterruptedException ex) {
/* 237 */         UILogInitiator.toggleImageById((short)1, false, this.port);
/* 238 */         if (!DerbyDBBackup.backupModeActivated) {
/* 239 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_while_sending_HEARTBEAT_to_the_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 240 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/* 242 */         SocketFunctions.closeSocket(this.sck);
/* 243 */         this.sck = null;
/*     */       } finally {}
/*     */       
/* 246 */       retries++;
/*     */     } 
/* 248 */     if (connectionFlag) {
/* 249 */       UILogInitiator.toggleImageById((short)1, false, this.port);
/* 250 */       if (!DerbyDBBackup.backupModeActivated) {
/* 251 */         GlobalVariables.buzzerActivated = true;
/* 252 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/*     */       } 
/*     */     } 
/* 255 */     return false;
/*     */   }
/*     */   
/*     */   private int sendEvent_Surgard_TcpIp(short event_Protocol, byte[] eventData) throws SQLException {
/* 259 */     int intRC = 27;
/* 260 */     int retries = 0;
/*     */     
/* 262 */     if (formatEvent_Surgard(event_Protocol, eventData)) {
/* 263 */       while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/*     */         try {
/* 265 */           if (establishConnectionMonitoringSoftware(retries, "EVent")) {
/* 266 */             this.onlineFlag = true;
/* 267 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Sending_event_to_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(eventData, null) }), Enums.EnumMessagePriority.LOW, null, null);
/* 268 */             SocketFunctions.send(this.sck, this.formattedEvent);
/* 269 */             if (this.mInfo.getWaitEventAck()) {
/* 270 */               this.bufferRx = SocketFunctions.receive(this.sck, 0, 1);
/* 271 */               if (this.bufferRx.length > 0) {
/* 272 */                 if (this.bufferRx[0] == this.mInfo.getAckByte()) {
/* 273 */                   return 6;
/*     */                 }
/* 275 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_an_event"), new Object[] { Functions.formatEventForPrinting(eventData, null) }) + Integer.toHexString(this.bufferRx[0]) + LocaleMessage.getLocaleMessage("(hexadecimal)"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 276 */                 intRC = 21;
/*     */               } else {
/*     */                 
/* 279 */                 intRC = 27;
/*     */               } 
/*     */             } else {
/* 282 */               return 6;
/*     */             } 
/* 284 */             UILogInitiator.toggleImageById((short)1, true, this.port);
/*     */           } else {
/* 286 */             this.onlineFlag = false;
/*     */           } 
/* 288 */         } catch (IOException|InterruptedException ex) {
/* 289 */           if (!DerbyDBBackup.backupModeActivated) {
/* 290 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Exception_while_sending_event_to_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(eventData, null) }), Enums.EnumMessagePriority.HIGH, null, ex);
/* 291 */             GlobalVariables.buzzerActivated = true;
/*     */           } 
/* 293 */           UILogInitiator.toggleImageById((short)1, false, this.port);
/* 294 */           SocketFunctions.closeSocket(this.sck);
/* 295 */           this.sck = null;
/*     */         } finally {}
/*     */         
/* 298 */         retries++;
/*     */       } 
/* 300 */       return intRC;
/*     */     } 
/* 302 */     return 21;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean establishConnectionMonitoringSoftware(int retries, String dd) throws SocketException {
/* 307 */     if (this.sck == null) {
/*     */       try {
/* 309 */         openSocket();
/* 310 */         SocketFunctions.connect(this.sck, this.mInfo.getMonitoringSoftwareIP(), this.mInfo.getMonitoringSoftwarePort().intValue(), this.mInfo.getReceiverTimeout().intValue());
/* 311 */         return true;
/* 312 */       } catch (Exception ex) {
/* 313 */         if (retries + 1 == this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE && 
/* 314 */           !DerbyDBBackup.backupModeActivated) {
/* 315 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("SUR_GARD_(via_TCP/IP)_receiver_could_not_communicate_with_the_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, null);
/* 316 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/*     */         
/* 319 */         UILogInitiator.toggleImageById((short)1, false, this.port);
/* 320 */         return false;
/*     */       } 
/*     */     }
/* 323 */     return true;
/*     */   }
/*     */   
/*     */   private void openSocket() throws SocketException {
/* 327 */     if (this.sck == null) {
/* 328 */       this.sck = new Socket();
/* 329 */       this.sck.setTcpNoDelay(true);
/* 330 */       this.sck.setSoTimeout(this.mInfo.getReceiverTimeout().intValue());
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EmulaReceiverSurgadViaTCPIP.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */