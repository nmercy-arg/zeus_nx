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
/*     */ public class EmulateReceiverSurgard
/*     */   extends EmulateReceiver
/*     */   implements Runnable
/*     */ {
/*     */   public EmulateReceiverSurgard(MonitoringInfo mInfo) {
/*  33 */     super(mInfo);
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  38 */     long nextExecutionThread = 0L;
/*  39 */     long nextDispatchHeartBeat = 0L;
/*     */     
/*  41 */     Thread.currentThread().setPriority(10);
/*  42 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("SUR-GARD_emulation_task_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  43 */     if (openReceiverSerialPort()) {
/*     */       try {
/*  45 */         while (this.flag) {
/*     */           try {
/*  47 */             if ((nextExecutionThread < System.currentTimeMillis() || this.newEvent) && SerialMux.lastCommunication + 5000L < System.currentTimeMillis()) {
/*  48 */               readEventFromDB_Surgard_Ademco685_CmPlus_ITI_Radionics();
/*  49 */               nextExecutionThread = System.currentTimeMillis() + 100L;
/*  50 */             } else if (ZeusServerCfg.getInstance().getEnableSerialMux()) {
/*  51 */               redirectSerialMuxPackets();
/*     */             } 
/*  53 */             if (nextDispatchHeartBeat < System.currentTimeMillis()) {
/*  54 */               changeOnline(sendHeartBeat());
/*  55 */               nextDispatchHeartBeat = System.currentTimeMillis() + 30000L;
/*     */             } 
/*  57 */           } catch (Exception ex) {
/*  58 */             if (!DerbyDBBackup.backupModeActivated) {
/*  59 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  60 */               GlobalVariables.buzzerActivated = true;
/*     */             } 
/*     */           } 
/*  63 */           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*     */         } 
/*  65 */       } catch (Exception ex) {
/*  66 */         if (!DerbyDBBackup.backupModeActivated) {
/*  67 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  68 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/*     */       } finally {
/*  71 */         dispose();
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   private boolean sendHeartBeat() {
/*  77 */     int retries = 0;
/*     */     try {
/*  79 */       while (retries < this.MAXIMUM_RETRIES_COMM_MONITORING_SOFTWARE) {
/*  80 */         this.sis.skip(this.sis.available());
/*  81 */         this.sos.write("1011           @    \024".getBytes());
/*  82 */         if (this.mInfo.getWaitEventAck()) {
/*  83 */           byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, 1, this.mInfo.getReceiverTimeout().intValue());
/*  84 */           if (buffer != null && buffer.length > 0) {
/*  85 */             UILogInitiator.toggleImageById((short)1, true, this.port);
/*  86 */             if (buffer[0] == this.mInfo.getAckByte()) {
/*  87 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/*  88 */               return true;
/*     */             } 
/*  90 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Invalid_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + buffer[0], Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */           } 
/*     */         } else {
/*     */           
/*  94 */           UILogInitiator.toggleImageById((short)1, true, this.port);
/*  95 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("HEARTBEAT_sent_successfully_to_the_monitoring_software") + " [" + this.port + "]", Enums.EnumMessagePriority.LOW, null, null);
/*  96 */           return true;
/*     */         } 
/*  98 */         retries++;
/*     */       } 
/* 100 */       UILogInitiator.toggleImageById((short)1, false, this.port);
/* 101 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_monitoring_software_to_the_transmission_of_a_HEARTBEAT") + " [" + this.port + "]", Enums.EnumMessagePriority.HIGH, null, null);
/* 102 */       GlobalVariables.buzzerActivated = true;
/* 103 */       return false;
/* 104 */     } catch (IOException ex) {
/* 105 */       if (!DerbyDBBackup.backupModeActivated) {
/* 106 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, " [" + this.port + "] " + LocaleMessage.getLocaleMessage("Exception_while_sending_HEARTBEAT_to_the_monitoring_software"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 107 */         GlobalVariables.buzzerActivated = true;
/*     */       } 
/*     */       
/* 110 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   protected boolean formatEvent(short eventProtocol, byte[] eventData, Calendar received) {
/* 115 */     return formatEvent_Surgard(eventProtocol, eventData);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EmulateReceiverSurgard.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */