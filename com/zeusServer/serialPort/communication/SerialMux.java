/*     */ package com.zeusServer.serialPort.communication;
/*     */ 
/*     */ import Serialio.SerInputStream;
/*     */ import Serialio.SerOutputStream;
/*     */ import Serialio.SerialPort;
/*     */ import Serialio.SerialPortLocal;
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*     */ import com.zeusServer.tblConnections.TblThreadsEmulaReceivers;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.MonitoringInfo;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.io.IOException;
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
/*     */ public class SerialMux
/*     */   implements Runnable
/*     */ {
/*     */   public static long lastCommunication;
/*     */   public static SerialPortLocal muxCommPort;
/*     */   protected static SerOutputStream sos;
/*     */   protected SerInputStream sis;
/*     */   public MonitoringInfo mInfo;
/*     */   public static Long wdt;
/*     */   public boolean flag;
/*  42 */   String port = null;
/*     */   
/*     */   public SerialMux() {
/*  45 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  46 */     lastCommunication = 0L;
/*  47 */     this.flag = true;
/*  48 */     this.port = ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort();
/*  49 */     this.mInfo = (MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(this.port);
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  54 */     int numberOfBytesPending = 0;
/*  55 */     byte[] bufferComm = null;
/*     */     
/*  57 */     label41: while (this.flag) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  64 */       muxCommPort = SerialPortFunctions.openSPL(getClass(), ZeusServerCfg.getInstance().getMuxSerialPort(), this.mInfo.getReceiverBaudrate().intValue(), this.mInfo.getReceiverDatabits().intValue(), this.mInfo.getReceiverStopbits().intValue(), this.mInfo.getReceiverParity().intValue());
/*     */       
/*  66 */       if (muxCommPort == null) {
/*  67 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_not_possible_to_open_the_serial_port_[") + ZeusServerCfg.getInstance().getMuxSerialPort() + LocaleMessage.getLocaleMessage("]_for_use_of_the_serial_multiplexer"), Enums.EnumMessagePriority.HIGH, null, null);
/*  68 */         GlobalVariables.buzzerActivated = true;
/*  69 */         wdt = Functions.updateWatchdog(wdt, 15000L);
/*     */       } else {
/*     */         
/*     */         try {
/*  73 */           muxCommPort.setDTR(true);
/*  74 */           muxCommPort.setRTS(true);
/*  75 */           sos = new SerOutputStream((SerialPort)muxCommPort);
/*  76 */           this.sis = new SerInputStream((SerialPort)muxCommPort);
/*     */           break label41;
/*  78 */         } catch (IOException ex) {
/*  79 */           if (muxCommPort != null) {
/*     */             try {
/*  81 */               muxCommPort.close();
/*  82 */             } catch (IOException iOException) {}
/*     */           }
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/*  88 */       wdt = Functions.updateWatchdog(wdt, 100L);
/*     */     } 
/*     */     
/*     */     try {
/*  92 */       while (this.flag) {
/*  93 */         if (this.sis.available() > 0) {
/*  94 */           byte[] buffer = SerialPortFunctions.readSPL(this.sis, 0, this.sis.available(), 2500);
/*  95 */           if (buffer != null && buffer.length > 0) {
/*  96 */             bufferComm = new byte[numberOfBytesPending + buffer.length];
/*  97 */             System.arraycopy(buffer, 0, bufferComm, numberOfBytesPending, buffer.length);
/*  98 */             numberOfBytesPending += buffer.length;
/*  99 */             lastCommunication = System.currentTimeMillis();
/*     */           } 
/*     */         } 
/* 102 */         if (numberOfBytesPending > 0 && TblThreadsEmulaReceivers.getInstance().get(this.port) != null && !((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(this.port)).serialPortInUse) {
/* 103 */           ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(this.port)).write2ReceiverCommPort(bufferComm);
/* 104 */           lastCommunication = System.currentTimeMillis();
/* 105 */           numberOfBytesPending = 0;
/*     */         } 
/* 107 */         wdt = Functions.updateWatchdog(wdt, 100L);
/*     */       } 
/* 109 */     } catch (IOException ex) {
/* 110 */       if (!DerbyDBBackup.backupModeActivated) {
/* 111 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_serial_multiplexer_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 112 */         GlobalVariables.buzzerActivated = true;
/*     */       } 
/*     */     } finally {
/* 115 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void write2MuxCommPort(byte[] buffer) {
/*     */     try {
/* 121 */       if (sos != null) {
/* 122 */         sos.write(buffer, 0, buffer.length);
/*     */       }
/* 124 */     } catch (IOException iOException) {}
/*     */   }
/*     */ 
/*     */   
/*     */   protected void dispose() {
/*     */     try {
/* 130 */       if (muxCommPort != null) {
/* 131 */         muxCommPort.close();
/*     */       }
/* 133 */       sos = null;
/* 134 */       this.sis = null;
/* 135 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Serial_multiplexer_task_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/* 136 */     } catch (IOException iOException) {}
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\SerialMux.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */