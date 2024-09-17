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
/*     */ 
/*     */ public class EmulateReceiverRadionics
/*     */   extends EmulateReceiver
/*     */   implements Runnable
/*     */ {
/*  32 */   private final int TIME_BETWEEN_HEARTBEATS_RADIONICS = 30000;
/*     */   
/*     */   public EmulateReceiverRadionics(MonitoringInfo mInfo) {
/*  35 */     super(mInfo);
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  40 */     long nextExecutionThread = 0L;
/*  41 */     long nextDispatchHeartBeat = 0L;
/*     */     
/*  43 */     Thread.currentThread().setPriority(10);
/*  44 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("RADIONICS_D6600_emulation_task_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  45 */     if (openReceiverSerialPort()) {
/*     */       try {
/*  47 */         while (this.flag) {
/*     */           try {
/*  49 */             if ((nextExecutionThread < System.currentTimeMillis() || this.newEvent) && SerialMux.lastCommunication + 5000L < System.currentTimeMillis()) {
/*  50 */               readEventFromDB_Surgard_Ademco685_CmPlus_ITI_Radionics();
/*  51 */               nextExecutionThread = System.currentTimeMillis() + 100L;
/*  52 */             } else if (ZeusServerCfg.getInstance().getEnableSerialMux()) {
/*  53 */               redirectSerialMuxPackets();
/*     */             } 
/*  55 */             if (nextDispatchHeartBeat < System.currentTimeMillis()) {
/*  56 */               changeOnline(sendHeartBeat());
/*  57 */               nextDispatchHeartBeat = System.currentTimeMillis() + 30000L;
/*     */             } 
/*  59 */           } catch (Exception ex) {
/*  60 */             if (!DerbyDBBackup.backupModeActivated) {
/*  61 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  62 */               GlobalVariables.buzzerActivated = true;
/*     */             } 
/*     */           } 
/*  65 */           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */         } 
/*  67 */       } catch (Exception ex) {
/*  68 */         if (!DerbyDBBackup.backupModeActivated) {
/*  69 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  70 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/*     */       } finally {
/*  73 */         dispose();
/*     */       } 
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   protected boolean formatEvent(short eventProtocol, byte[] eventData, Calendar received) {
/*  80 */     if (eventProtocol == Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId()) {
/*  81 */       StringBuilder sb = new StringBuilder();
/*  82 */       eventData = Functions.convertBufferContactIdInBufferHex(eventData);
/*     */ 
/*     */       
/*  85 */       sb.append("a");
/*  86 */       sb.append(Functions.formatToHex(this.mInfo.getReceiverNumber().intValue(), 2));
/*  87 */       sb.append(Functions.convertInt2Hex(this.mInfo.getReceiverLine().intValue()));
/*  88 */       sb.append(" ");
/*  89 */       sb.append(Functions.formatToHex(eventData[0] & 0xFF, 2));
/*  90 */       sb.append(Functions.formatToHex(eventData[1] & 0xFF, 2));
/*  91 */       sb.append(18);
/*  92 */       sb.append(Functions.convertInt2Hex((eventData[3] & 0xFF) / 16 & 0xF));
/*  93 */       sb.append(Functions.convertInt2Hex(eventData[3] & 0xFF & 0xF));
/*  94 */       sb.append(Functions.formatToHex(eventData[4] & 0xFF, 2));
/*  95 */       sb.append(Functions.formatToHex(eventData[5] & 0xFF, 2));
/*  96 */       sb.append(Functions.formatToHex(eventData[6] & 0xFF, 2));
/*  97 */       sb.append(Functions.convertInt2Hex((eventData[7] & 0xFF) / 16 & 0xF));
/*  98 */       sb.append('\024');
/*  99 */       this.formattedEvent = sb.toString().toLowerCase();
/* 100 */       return true;
/*     */     } 
/* 102 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean sendHeartBeat() {
/* 107 */     int retries = 0;
/*     */     try {
/* 109 */       while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/* 110 */         this.sis.skip(this.sis.available());
/* 111 */         this.sos.write(("1" + Functions.formatToHex(this.mInfo.getReceiverNumber().intValue(), 2) + "0           @    " + '\024').getBytes());
/* 112 */         if (this.mInfo.getWaitEventAck()) {
/* 113 */           byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, 1, this.mInfo.getReceiverTimeout().intValue());
/* 114 */           if (buffer != null && buffer.length > 0) {
/* 115 */             UILogInitiator.toggleImageById((short)1, true, this.port);
/* 116 */             if (buffer[0] == this.mInfo.getAckByte()) {
/* 117 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 118 */               return true;
/*     */             } 
/* 120 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + buffer[0], Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */           } 
/*     */         } else {
/*     */           
/* 124 */           UILogInitiator.toggleImageById((short)1, true, this.port);
/* 125 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 126 */           return true;
/*     */         } 
/* 128 */         retries++;
/*     */       } 
/* 130 */       UILogInitiator.toggleImageById((short)1, false, this.port);
/* 131 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/* 132 */       GlobalVariables.buzzerActivated = true;
/* 133 */       return false;
/* 134 */     } catch (IOException ex) {
/* 135 */       if (!DerbyDBBackup.backupModeActivated) {
/* 136 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_while_sending_HEARTBEAT_to_the_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 137 */         GlobalVariables.buzzerActivated = true;
/*     */       } 
/*     */       
/* 140 */       return false;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EmulateReceiverRadionics.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */