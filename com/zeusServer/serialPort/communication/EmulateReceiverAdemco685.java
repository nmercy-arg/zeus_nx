/*     */ package com.zeusServer.serialPort.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*     */ import com.zeusServer.ui.UILogInitiator;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.MonitoringInfo;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.io.IOException;
/*     */ import java.util.Calendar;
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
/*     */ public class EmulateReceiverAdemco685
/*     */   extends EmulateReceiver
/*     */   implements Runnable
/*     */ {
/*  31 */   private final byte HEARTBEAT = 83;
/*  32 */   private long lastHeartBeatSent = 0L;
/*     */   
/*     */   public EmulateReceiverAdemco685(MonitoringInfo mInfo) {
/*  35 */     super(mInfo);
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  40 */     long nextExecutionThread = 0L;
/*     */     
/*  42 */     Thread.currentThread().setPriority(10);
/*  43 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("ADEMCO-685_receiver_emulation_task_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  44 */     if (openReceiverSerialPort()) {
/*     */       try {
/*  46 */         while (this.flag) {
/*     */           try {
/*  48 */             if (this.mInfo.getEnableHeartbeat() && 
/*  49 */               this.lastHeartBeatSent < System.currentTimeMillis()) {
/*     */               try {
/*  51 */                 int retries = 0;
/*  52 */                 boolean ackReceived = false;
/*  53 */                 while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/*  54 */                   this.sis.skip(this.sis.available());
/*  55 */                   String tm = "\n" + this.mInfo.getHeartBeatData() + "\r";
/*  56 */                   this.sos.write(tm.getBytes("ISO-8859-1"));
/*  57 */                   byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, 1, this.mInfo.getReceiverTimeout().intValue());
/*  58 */                   if (buffer != null && buffer.length > 0) {
/*  59 */                     if (buffer[0] == this.mInfo.getAckByte()) {
/*  60 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/*  61 */                       ackReceived = true;
/*     */                       break;
/*     */                     } 
/*  64 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + buffer[0], Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */                     
/*  66 */                     UILogInitiator.toggleImageById((short)1, true, this.port);
/*     */                   } 
/*  68 */                   retries++;
/*     */                 } 
/*  70 */                 if (!ackReceived) {
/*  71 */                   GlobalVariables.buzzerActivated = true;
/*  72 */                   UILogInitiator.toggleImageById((short)1, false, this.port);
/*  73 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                 } 
/*  75 */               } catch (IOException ex) {
/*  76 */                 UILogInitiator.toggleImageById((short)1, false, this.port);
/*  77 */                 if (!DerbyDBBackup.backupModeActivated) {
/*  78 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_while_sending_HEARTBEAT_to_the_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  79 */                   GlobalVariables.buzzerActivated = true;
/*     */                 } 
/*     */               } 
/*  82 */               this.lastHeartBeatSent = System.currentTimeMillis() + (this.mInfo.getHeartBeatFrequency().intValue() * 1000);
/*     */             } 
/*     */ 
/*     */             
/*  86 */             if ((nextExecutionThread < System.currentTimeMillis() || this.newEvent) && SerialMux.lastCommunication + 5000L < System.currentTimeMillis()) {
/*  87 */               readEventFromDB_Surgard_Ademco685_CmPlus_ITI_Radionics();
/*  88 */               nextExecutionThread = System.currentTimeMillis() + 100L;
/*  89 */             } else if (ZeusServerCfg.getInstance().getEnableSerialMux()) {
/*  90 */               redirectSerialMuxPackets();
/*  91 */             } else if (this.sis.available() > 0) {
/*  92 */               byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, 1, this.mInfo.getReceiverTimeout().intValue());
/*  93 */               if (buffer != null && buffer.length > 0 && 
/*  94 */                 buffer[0] == 83) {
/*  95 */                 changeOnline(sendHeartBeatReply());
/*     */               }
/*     */             }
/*     */           
/*  99 */           } catch (Exception ex) {
/* 100 */             if (!DerbyDBBackup.backupModeActivated) {
/* 101 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 102 */               GlobalVariables.buzzerActivated = true;
/*     */             } 
/*     */           } 
/* 105 */           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */         } 
/* 107 */       } catch (Exception ex) {
/* 108 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 109 */         GlobalVariables.buzzerActivated = true;
/*     */       } finally {
/* 111 */         dispose();
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   private boolean sendHeartBeatReply() {
/* 117 */     int retries = 0;
/*     */     try {
/* 119 */       while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/* 120 */         this.sis.skip(this.sis.available());
/* 121 */         this.sos.write("\n00 OKAY @\r".getBytes("ISO-8859-1"));
/* 122 */         if (this.mInfo.getWaitEventAck()) {
/* 123 */           byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, 1, this.mInfo.getReceiverTimeout().intValue());
/* 124 */           if (buffer != null && buffer.length > 0) {
/* 125 */             UILogInitiator.toggleImageById((short)1, true, this.port);
/* 126 */             if (buffer[0] == this.mInfo.getAckByte()) {
/* 127 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 128 */               return true;
/*     */             } 
/* 130 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + buffer[0], Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */           } 
/*     */         } else {
/*     */           
/* 134 */           UILogInitiator.toggleImageById((short)1, true, this.port);
/* 135 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 136 */           return true;
/*     */         } 
/*     */         
/* 139 */         retries++;
/*     */       } 
/* 141 */       UILogInitiator.toggleImageById((short)1, false, this.port);
/* 142 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/* 143 */       GlobalVariables.buzzerActivated = true;
/* 144 */       return false;
/* 145 */     } catch (IOException ex) {
/* 146 */       if (!DerbyDBBackup.backupModeActivated) {
/* 147 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_while_sending_HEARTBEAT_to_the_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 148 */         GlobalVariables.buzzerActivated = true;
/*     */       } 
/*     */ 
/*     */       
/* 152 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   protected boolean formatEvent(short eventProtocol, byte[] eventData, Calendar received) {
/* 157 */     if (eventProtocol == Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId()) {
/* 158 */       StringBuilder sb = new StringBuilder();
/* 159 */       eventData = Functions.convertBufferContactIdInBufferHex(eventData);
/* 160 */       sb.append(Functions.convertInt2Hex(this.mInfo.getReceiverNumber().intValue()));
/* 161 */       sb.append(Functions.convertInt2Hex(this.mInfo.getReceiverGroup().intValue()));
/* 162 */       sb.append(" ");
/* 163 */       sb.append(Functions.formatToHex(eventData[0] & 0xFF, 2));
/* 164 */       sb.append(Functions.formatToHex(eventData[1] & 0xFF, 2));
/* 165 */       sb.append(" ");
/* 166 */       sb.append(18);
/* 167 */       sb.append(" ");
/* 168 */       int val = (eventData[3] & 0xFF) / 16 & 0xF;
/* 169 */       if (val == Enums.EnumEventQualifier.NEW_EVENT.getEvent()) {
/* 170 */         sb.append('E');
/* 171 */       } else if (val == Enums.EnumEventQualifier.NEW_RESTORE.getEvent()) {
/* 172 */         sb.append('R');
/* 173 */       } else if (val == Enums.EnumEventQualifier.PREVIOUS_EVENT.getEvent()) {
/* 174 */         sb.append('P');
/*     */       } else {
/* 176 */         return false;
/*     */       } 
/* 178 */       String eventCode = Integer.toHexString(eventData[3] & 0xFF & 0xF) + Functions.formatToHex(eventData[4] & 0xFF, 2);
/* 179 */       sb.append(eventCode);
/* 180 */       sb.append(" ");
/* 181 */       sb.append(Functions.formatToHex(eventData[5] & 0xFF, 2));
/* 182 */       sb.append(" ");
/* 183 */       sb.append(isContactOrUser(eventCode));
/* 184 */       sb.append(Functions.formatToHex(eventData[6] & 0xFF, 2));
/* 185 */       sb.append(Integer.toHexString((eventData[7] & 0xFF) / 16 & 0xF));
/* 186 */       sb.append(" ");
/* 187 */       sb.append('\r');
/* 188 */       String tmp = sb.toString().toUpperCase();
/* 189 */       this.formattedEvent = "\n" + tmp + "\r";
/* 190 */       return true;
/*     */     } 
/* 192 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private char isContactOrUser(String hex) {
/* 197 */     switch (hex) {
/*     */       case "121":
/*     */       case "313":
/*     */       case "400":
/*     */       case "401":
/*     */       case "402":
/*     */       case "403":
/*     */       case "404":
/*     */       case "405":
/*     */       case "406":
/*     */       case "407":
/*     */       case "408":
/*     */       case "409":
/*     */       case "441":
/*     */       case "442":
/*     */       case "450":
/*     */       case "451":
/*     */       case "452":
/*     */       case "453":
/*     */       case "454":
/*     */       case "455":
/*     */       case "456":
/*     */       case "457":
/*     */       case "458":
/*     */       case "459":
/*     */       case "462":
/*     */       case "463":
/*     */       case "464":
/*     */       case "466":
/*     */       case "411":
/*     */       case "412":
/*     */       case "413":
/*     */       case "414":
/*     */       case "415":
/*     */       case "421":
/*     */       case "422":
/*     */       case "424":
/*     */       case "425":
/*     */       case "429":
/*     */       case "430":
/*     */       case "431":
/*     */       case "574":
/*     */       case "604":
/*     */       case "607":
/*     */       case "625":
/*     */       case "642":
/*     */       case "652":
/*     */       case "653":
/* 245 */         return 'U';
/*     */     } 
/* 247 */     return 'C';
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EmulateReceiverAdemco685.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */