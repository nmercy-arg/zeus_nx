/*     */ package com.zeusServer.serialPort.communication;
/*     */ 
/*     */ import Serialio.SerInputStream;
/*     */ import Serialio.SerOutputStream;
/*     */ import Serialio.SerialPort;
/*     */ import Serialio.SerialPortLocal;
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.DBManagers.GriffonDBManager;
/*     */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*     */ import com.zeusServer.ui.UILogInitiator;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.MonitoringGroupInfo;
/*     */ import com.zeusServer.util.MonitoringInfo;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.io.IOException;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
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
/*     */ public abstract class EmulateReceiver
/*     */   implements Runnable
/*     */ {
/*  43 */   protected final int TIME_BETWEEN_CHECK_NEW_EVENTS_IN_THE_QUEUE = 100;
/*  44 */   protected final int TIME_BETWEEN_HEARTBEATS_SURGARD = 30000;
/*  45 */   protected final int MINIMUM_TIME_SINCE_RECEPTION_LAST_BYTE_SERIAL_MUX = 5000;
/*  46 */   protected final int MAXIMUM_RETRIES_EVENT_TRANSMISSION = 3;
/*  47 */   protected int MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE = 3;
/*     */   protected byte[] serialInputBuffer;
/*     */   public boolean online = true;
/*     */   public boolean serialPortInUse;
/*     */   public boolean newEvent;
/*     */   public boolean flag;
/*     */   public Long wdt;
/*     */   public Thread myThread;
/*  55 */   public SerialPortLocal receiverCommPort = null;
/*     */   protected SerOutputStream sos;
/*     */   protected SerInputStream sis;
/*  58 */   protected int scc = 65;
/*  59 */   protected int referenceNumber = 1;
/*     */   protected List<EventDataHolder> edhList;
/*  61 */   protected String formattedEvent = null;
/*     */   
/*     */   protected boolean onlineFlag = false;
/*     */   protected String port;
/*     */   public MonitoringInfo mInfo;
/*     */   
/*     */   public EmulateReceiver(MonitoringInfo mInfo) {
/*  68 */     this.serialPortInUse = false;
/*  69 */     this.online = true;
/*  70 */     this.newEvent = false;
/*  71 */     this.wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*     */     
/*  73 */     this.mInfo = mInfo;
/*  74 */     this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE = mInfo.getCommRetries().intValue();
/*  75 */     this.port = mInfo.getReceiverSerialPort();
/*  76 */     if (ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort() != null && this.port != null && this.port.equalsIgnoreCase(ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort())) {
/*  77 */       if (mInfo.getAssignedGroupsByProduct() == null) {
/*  78 */         ConcurrentHashMap<String, List<MonitoringGroupInfo>> assignedGroupsByProduct = new ConcurrentHashMap<>();
/*  79 */         mInfo.setAssignedGroupsByProduct(assignedGroupsByProduct);
/*     */       } 
/*  81 */       for (String product : EventLoader.availableProducts) {
/*  82 */         MonitoringGroupInfo mgi = new MonitoringGroupInfo();
/*  83 */         mgi.setGroupId(0);
/*  84 */         if (mInfo.getAssignedGroupsByProduct().containsKey(product)) {
/*  85 */           ((List<MonitoringGroupInfo>)mInfo.getAssignedGroupsByProduct().get(product)).add(mgi); continue;
/*     */         } 
/*  87 */         List<MonitoringGroupInfo> mgiList = new ArrayList<>();
/*  88 */         mgiList.add(mgi);
/*  89 */         mInfo.getAssignedGroupsByProduct().put(product, mgiList);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected boolean openReceiverSerialPort() {
/*  97 */     while (this.flag) {
/*  98 */       this.receiverCommPort = SerialPortFunctions.openSPL(getClass(), this.port, this.mInfo.getReceiverBaudrate().intValue(), this.mInfo.getReceiverDatabits().intValue(), this.mInfo.getReceiverStopbits().intValue(), this.mInfo.getReceiverParity().intValue());
/*  99 */       if (this.receiverCommPort == null) {
/* 100 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, this.port + " " + LocaleMessage.getLocaleMessage("cannot_be_opened_for_communication_with_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, null);
/* 101 */         UILogInitiator.toggleImageById((short)1, false, this.port);
/* 102 */         changeOnline(false);
/* 103 */         GlobalVariables.buzzerActivated = true;
/* 104 */         this.wdt = Functions.updateWatchdog(this.wdt, 15000L);
/*     */       } else {
/*     */         try {
/* 107 */           this.receiverCommPort.setDTR(true);
/* 108 */           this.receiverCommPort.setRTS(true);
/* 109 */           if (this.sos == null) {
/* 110 */             this.sos = new SerOutputStream((SerialPort)this.receiverCommPort);
/*     */           }
/* 112 */           if (this.sis == null) {
/* 113 */             this.sis = new SerInputStream((SerialPort)this.receiverCommPort);
/*     */           }
/* 115 */           UILogInitiator.toggleImageById((short)1, true, this.port);
/*     */           break;
/* 117 */         } catch (IOException ex) {
/* 118 */           if (this.receiverCommPort != null) {
/*     */             try {
/* 120 */               this.receiverCommPort.close();
/* 121 */             } catch (IOException iOException) {}
/*     */           }
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 127 */       this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */     } 
/*     */     
/* 130 */     return true;
/*     */   }
/*     */   
/*     */   protected void redirectSerialMuxPackets() throws IOException {
/* 134 */     if (this.sis.available() > 0) {
/* 135 */       byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, this.sis.available(), 2500);
/* 136 */       if (buffer != null && buffer.length > 0) {
/* 137 */         SerialMux.write2MuxCommPort(buffer);
/* 138 */         SerialMux.lastCommunication = System.currentTimeMillis();
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   protected void changeOnline(boolean state) {
/* 144 */     if (!state && this.online) {
/* 145 */       GlobalVariables.buzzerActivated = true;
/*     */     }
/* 147 */     this.online = state;
/*     */   }
/*     */   
/*     */   protected void readEventFromDB_Surgard_Ademco685_CmPlus_ITI_Radionics() throws InterruptedException, Exception {
/*     */     try {
/* 152 */       this.newEvent = false;
/* 153 */       this.serialPortInUse = true;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 159 */       if (ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort() != null && this.port != null && this.port.equalsIgnoreCase(ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort())) {
/* 160 */         if (this.mInfo.getAssignedGroupsByProduct() == null) {
/* 161 */           ConcurrentHashMap<String, List<MonitoringGroupInfo>> assignedGroupsByProduct = new ConcurrentHashMap<>();
/* 162 */           this.mInfo.setAssignedGroupsByProduct(assignedGroupsByProduct);
/*     */         } 
/* 164 */         for (String product : EventLoader.availableProducts) {
/* 165 */           List<Integer> dbGrpIds = null;
/* 166 */           List<Integer> assignedGrpIds = null;
/* 167 */           switch (product) {
/*     */             case "PEGASUS":
/* 169 */               dbGrpIds = PegasusDBManager.getGroupIds();
/* 170 */               assignedGrpIds = ZeusServerCfg.getAssignedGroupIdsByProduct(product);
/*     */               break;
/*     */             case "GRIFFON":
/* 173 */               dbGrpIds = GriffonDBManager.getGroupIds();
/* 174 */               assignedGrpIds = ZeusServerCfg.getAssignedGroupIdsByProduct(product);
/*     */               break;
/*     */             case "MERCURIUS":
/* 177 */               dbGrpIds = MercuriusDBManager.getGroupIds();
/* 178 */               assignedGrpIds = ZeusServerCfg.getAssignedGroupIdsByProduct(product);
/*     */               break;
/*     */           } 
/* 181 */           if (dbGrpIds != null) {
/* 182 */             List<Integer> missedGrpsList = new ArrayList<>();
/* 183 */             if (assignedGrpIds == null) {
/* 184 */               missedGrpsList.addAll(dbGrpIds);
/*     */             }
/* 186 */             if (assignedGrpIds.isEmpty()) {
/* 187 */               missedGrpsList.addAll(dbGrpIds);
/*     */             }
/* 189 */             for (Integer gid : dbGrpIds) {
/* 190 */               if (!assignedGrpIds.contains(gid)) {
/* 191 */                 missedGrpsList.add(gid);
/*     */               }
/*     */             } 
/* 194 */             for (Integer gid : missedGrpsList) {
/* 195 */               MonitoringGroupInfo mgi = new MonitoringGroupInfo();
/* 196 */               mgi.setGroupId(gid.intValue());
/* 197 */               if (this.mInfo.getAssignedGroupsByProduct().containsKey(product)) {
/* 198 */                 ((List<MonitoringGroupInfo>)this.mInfo.getAssignedGroupsByProduct().get(product)).add(mgi); continue;
/*     */               } 
/* 200 */               List<MonitoringGroupInfo> mgiList = new ArrayList<>();
/* 201 */               mgiList.add(mgi);
/* 202 */               this.mInfo.getAssignedGroupsByProduct().put(product, mgiList);
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 208 */       fetchNonProcessedEventsFromQueue();
/*     */       
/* 210 */       if (this.edhList != null)
/*     */       {
/* 212 */         for (EventDataHolder edh : this.edhList) {
/* 213 */           while (SerialMux.lastCommunication + 5000L < System.currentTimeMillis()) {
/* 214 */             if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_PEGASUS_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_GRCP_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_AVL_EVENT_TABLE) {
/* 215 */               this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */               
/*     */               continue;
/*     */             } 
/* 219 */             boolean result = processReplyMonitoringSoftware(sendEvent(edh.getEvent_Protocol(), edh.getEventBuffer(), edh.getReceived()), edh);
/* 220 */             if (!result) {
/*     */               // Byte code: goto -> 665
/*     */             }
/* 223 */             this.wdt = Functions.updateWatchdog(this.wdt, this.mInfo.getEventsTimeGap().intValue());
/*     */             
/* 225 */             if (result) {
/*     */               break;
/*     */             }
/*     */           } 
/*     */         } 
/*     */       }
/*     */     } finally {
/* 232 */       this.serialPortInUse = false;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void fetchNonProcessedEventsFromQueue() {
/* 237 */     if (this.mInfo != null && this.mInfo.getAssignedGroupsByProduct() != null) {
/* 238 */       this.edhList = new ArrayList<>();
/* 239 */       List<EventDataHolder> dummyList = new ArrayList<>();
/* 240 */       for (Map.Entry<String, List<MonitoringGroupInfo>> entry : (Iterable<Map.Entry<String, List<MonitoringGroupInfo>>>)this.mInfo.getAssignedGroupsByProduct().entrySet()) {
/* 241 */         if (EventLoader.nonProcessedEventData.containsKey(entry.getKey())) {
/* 242 */           ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> peData = EventLoader.nonProcessedEventData.get(entry.getKey());
/* 243 */           for (MonitoringGroupInfo mgi : entry.getValue()) {
/* 244 */             if (peData.containsKey(Integer.valueOf(mgi.getGroupId()))) {
/* 245 */               int size = (((PriorityBlockingQueue)peData.get(Integer.valueOf(mgi.getGroupId()))).size() > 1000) ? 1000 : ((PriorityBlockingQueue)peData.get(Integer.valueOf(mgi.getGroupId()))).size();
/* 246 */               Iterator<EventDataHolder> itr = ((PriorityBlockingQueue)peData.get(Integer.valueOf(mgi.getGroupId()))).iterator();
/* 247 */               int count = 0;
/* 248 */               while (itr.hasNext() && count < size) {
/* 249 */                 EventDataHolder event = itr.next();
/* 250 */                 this.edhList.add(event);
/* 251 */                 count++;
/* 252 */                 ((PriorityBlockingQueue<EventDataHolder>)peData.get(Integer.valueOf(mgi.getGroupId()))).drainTo(dummyList, 1);
/*     */               } 
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       } 
/* 258 */       if (EventLoader.nonProcessedEventData.containsKey("ZEUSSETTINGS")) {
/* 259 */         ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> peData = EventLoader.nonProcessedEventData.get("ZEUSSETTINGS");
/* 260 */         if (peData.containsKey(Integer.valueOf(0))) {
/* 261 */           int size = (((PriorityBlockingQueue)peData.get(Integer.valueOf(0))).size() > 1000) ? 1000 : ((PriorityBlockingQueue)peData.get(Integer.valueOf(0))).size();
/* 262 */           Iterator<EventDataHolder> itr = ((PriorityBlockingQueue)peData.get(Integer.valueOf(0))).iterator();
/* 263 */           int count = 0;
/* 264 */           while (itr.hasNext() && count < size) {
/* 265 */             EventDataHolder event = itr.next();
/* 266 */             this.edhList.add(event);
/* 267 */             count++;
/* 268 */             ((PriorityBlockingQueue<EventDataHolder>)peData.get(Integer.valueOf(0))).drainTo(dummyList, 1);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } else {
/* 273 */       this.edhList = null;
/*     */     } 
/*     */   }
/*     */   
/*     */   protected boolean processReplyMonitoringSoftware(int intRC, EventDataHolder edh) throws SQLException, InterruptedException {
/* 278 */     switch (intRC) {
/*     */       case 6:
/* 280 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Received_ACK_from_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 281 */         if (edh.getProductId() == Util.EnumProductIDs.ZEUS.getProductId()) {
/* 282 */           ZeusSettingsDBManager.executeSP_S007(edh.getId_Event());
/*     */         }
/* 284 */         else if (this.mInfo.getDeleteEventAfterTransmission()) {
/* 285 */           GenericDBManager.executeSP_008(edh.getId_Event(), edh.getProductId());
/*     */         } else {
/* 287 */           GenericDBManager.executeSP_016(edh.getId_Event(), edh.getProductId());
/*     */         } 
/*     */         
/* 290 */         if (this.mInfo.getBeepAfterEventTransmission()) {
/* 291 */           Functions.eventTransmissionBeep();
/*     */         }
/* 293 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Event_sent_successfully_to_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(edh.getEventBuffer(), null) }), Enums.EnumMessagePriority.LOW, null, null);
/* 294 */         changeOnline(true);
/* 295 */         UILogInitiator.toggleImageById((short)1, true, this.port);
/* 296 */         return true;
/*     */       
/*     */       case 21:
/* 299 */         if (edh.getProductId() == Util.EnumProductIDs.ZEUS.getProductId()) {
/* 300 */           if (edh.getTransmission_Retries() + 1 >= 3) {
/* 301 */             ZeusSettingsDBManager.cancelZeusEventTransmission(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1));
/*     */           } else {
/* 303 */             ZeusSettingsDBManager.updateZeusEventTransmissionRetries(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1));
/*     */           }
/*     */         
/* 306 */         } else if (edh.getTransmission_Retries() + 1 >= 3) {
/* 307 */           GenericDBManager.executeSP_005(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1), edh.getProductId());
/*     */         } else {
/* 309 */           GenericDBManager.executeSP_018(edh.getId_Event(), (short)(edh.getTransmission_Retries() + 1), edh.getProductId());
/*     */         } 
/*     */         
/* 312 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Event_was_not_recognized_by_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(edh.getEventBuffer(), null) }), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 313 */         changeOnline(false);
/* 314 */         return true;
/*     */     } 
/*     */     
/* 317 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("The_Zeus_Server_lost_communication_with_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/* 318 */     changeOnline(false);
/* 319 */     GlobalVariables.buzzerActivated = true;
/* 320 */     UILogInitiator.toggleImageById((short)1, false, this.port);
/* 321 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private int sendEvent(short eventProtocol, byte[] eventData, Calendar receivedTime) {
/* 326 */     int intRc = 27;
/* 327 */     int retries = 0;
/*     */     
/*     */     try {
/* 330 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Sending_event_to_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(eventData, null) }), Enums.EnumMessagePriority.LOW, null, null);
/* 331 */       if (formatEvent(eventProtocol, eventData, receivedTime)) {
/* 332 */         while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/* 333 */           this.sis.skip(this.sis.available());
/* 334 */           this.sos.write(this.formattedEvent.getBytes("ISO-8859-1"));
/* 335 */           if (this.mInfo.getWaitEventAck()) {
/* 336 */             byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, 1, this.mInfo.getReceiverTimeout().intValue());
/* 337 */             if (buffer != null && buffer.length > 0) {
/*     */               
/* 339 */               if (buffer[0] == this.mInfo.getAckByte()) {
/* 340 */                 return 6;
/*     */               }
/* 342 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_an_event"), new Object[] { Functions.formatEventForPrinting(eventData, null) }) + buffer[0] + LocaleMessage.getLocaleMessage("(hexadecimal)"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 343 */               intRc = 21;
/*     */             } else {
/*     */               
/* 346 */               return 27;
/*     */             } 
/*     */           } else {
/* 349 */             return 6;
/*     */           } 
/* 351 */           retries++;
/*     */         } 
/* 353 */         return intRc;
/*     */       } 
/* 355 */       return 21;
/*     */     }
/* 357 */     catch (IOException ex) {
/* 358 */       if (!DerbyDBBackup.backupModeActivated) {
/* 359 */         GlobalVariables.buzzerActivated = true;
/* 360 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.port + "] " + String.format(LocaleMessage.getLocaleMessage("Exception_while_sending_event_to_the_monitoring_software"), new Object[] { Functions.formatEventForPrinting(eventData, null) }), Enums.EnumMessagePriority.HIGH, null, ex);
/*     */       } 
/* 362 */       return 0;
/*     */     } 
/*     */   }
/*     */   
/*     */   protected boolean formatEvent(short eventProtocol, byte[] eventData, Calendar received) {
/* 367 */     return false;
/*     */   }
/*     */   
/*     */   protected boolean formatEvent_Surgard(short eventProtocol, byte[] eventData) {
/* 371 */     if (eventProtocol == Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId()) {
/* 372 */       StringBuilder sb = new StringBuilder();
/* 373 */       eventData = Functions.convertBufferContactIdInBufferHex(eventData);
/* 374 */       sb.append(5);
/* 375 */       sb.append(Functions.convertInt2Hex(this.mInfo.getReceiverNumber().intValue()));
/* 376 */       sb.append(Functions.convertInt2Hex(this.mInfo.getReceiverGroup().intValue()));
/* 377 */       sb.append(Functions.convertInt2Hex(this.mInfo.getReceiverLine().intValue()));
/* 378 */       sb.append(" ");
/* 379 */       sb.append("18");
/* 380 */       sb.append(Functions.formatToHex(eventData[0] & 0xFF, 2));
/* 381 */       sb.append(Functions.formatToHex(eventData[1] & 0xFF, 2));
/* 382 */       int val = (eventData[3] & 0xFF) / 16 & 0xF;
/* 383 */       if (val == Enums.EnumEventQualifier.NEW_EVENT.getEvent()) {
/* 384 */         sb.append('E');
/* 385 */       } else if (val == Enums.EnumEventQualifier.NEW_RESTORE.getEvent()) {
/* 386 */         sb.append('R');
/* 387 */       } else if (val == Enums.EnumEventQualifier.PREVIOUS_EVENT.getEvent()) {
/* 388 */         sb.append('P');
/*     */       } else {
/* 390 */         return false;
/*     */       } 
/* 392 */       sb.append(Integer.toHexString(eventData[3] & 0xFF & 0xF));
/* 393 */       sb.append(Functions.formatToHex(eventData[4] & 0xFF, 2));
/* 394 */       sb.append(Functions.formatToHex(eventData[5] & 0xFF, 2));
/* 395 */       sb.append(Functions.formatToHex(eventData[6] & 0xFF, 2));
/* 396 */       sb.append(Integer.toHexString((eventData[7] & 0xFF) / 16 & 0xF));
/* 397 */       sb.append('\024');
/* 398 */       this.formattedEvent = sb.toString().toUpperCase();
/* 399 */       return true;
/*     */     } 
/* 401 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   public void write2ReceiverCommPort(byte[] buffer) {
/*     */     try {
/* 407 */       this.sos.write(buffer, 0, buffer.length);
/* 408 */     } catch (IOException iOException) {}
/*     */   }
/*     */ 
/*     */   
/*     */   protected void dispose() {
/* 413 */     if (this.sis != null) {
/*     */       try {
/* 415 */         this.sis.close();
/*     */       }
/* 417 */       catch (IOException iOException) {}
/*     */     }
/*     */     
/* 420 */     this.sis = null;
/* 421 */     if (this.sos != null) {
/*     */       try {
/* 423 */         this.sos.close();
/* 424 */       } catch (IOException iOException) {}
/*     */     }
/*     */     
/* 427 */     this.sos = null;
/*     */     try {
/* 429 */       if (this.receiverCommPort != null) {
/* 430 */         this.receiverCommPort.close();
/*     */       }
/* 432 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Receiver_emulation_task_finalized") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 433 */     } catch (IOException iOException) {}
/*     */   }
/*     */   
/*     */   public abstract void run();
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EmulateReceiver.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */