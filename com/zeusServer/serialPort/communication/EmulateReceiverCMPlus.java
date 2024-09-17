/*    */ package com.zeusServer.serialPort.communication;
/*    */ 
/*    */ import com.zeus.settings.beans.Util;
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.Functions;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import com.zeusServer.util.LocaleMessage;
/*    */ import com.zeusServer.util.MonitoringInfo;
/*    */ import com.zeusServer.util.ZeusServerCfg;
/*    */ import java.util.Calendar;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class EmulateReceiverCMPlus
/*    */   extends EmulateReceiver
/*    */   implements Runnable
/*    */ {
/*    */   public EmulateReceiverCMPlus(MonitoringInfo mInfo) {
/* 30 */     super(mInfo);
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 35 */     long nextExecutionThread = 0L;
/*    */     
/* 37 */     Thread.currentThread().setPriority(10);
/* 38 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("CM-PLUS_emulation_task_started"), Enums.EnumMessagePriority.LOW, null, null);
/* 39 */     if (openReceiverSerialPort()) {
/*    */       try {
/* 41 */         while (this.flag) {
/*    */           try {
/* 43 */             if ((nextExecutionThread < System.currentTimeMillis() || this.newEvent) && SerialMux.lastCommunication + 5000L < System.currentTimeMillis()) {
/* 44 */               readEventFromDB_Surgard_Ademco685_CmPlus_ITI_Radionics();
/* 45 */               nextExecutionThread = System.currentTimeMillis() + 100L;
/* 46 */             } else if (ZeusServerCfg.getInstance().getEnableSerialMux()) {
/* 47 */               redirectSerialMuxPackets();
/*    */             } 
/* 49 */           } catch (Exception ex) {
/* 50 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 51 */             GlobalVariables.buzzerActivated = true;
/*    */           } 
/* 53 */           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*    */         } 
/* 55 */       } catch (Exception ex) {
/* 56 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 57 */         GlobalVariables.buzzerActivated = true;
/*    */       } finally {
/* 59 */         dispose();
/*    */       } 
/*    */     }
/*    */   }
/*    */ 
/*    */   
/*    */   protected boolean formatEvent(short eventProtocol, byte[] eventData, Calendar received) {
/* 66 */     if (eventProtocol == Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId()) {
/* 67 */       StringBuilder sb = new StringBuilder();
/* 68 */       eventData = Functions.convertBufferContactIdInBufferHex(eventData);
/* 69 */       sb.append('$');
/* 70 */       sb.append('5');
/* 71 */       sb.append(Functions.formatToHex(eventData[0] & 0xFF, 2));
/* 72 */       sb.append(Functions.formatToHex(eventData[1] & 0xFF, 2));
/* 73 */       sb.append("00");
/* 74 */       sb.append(Functions.convertInt2Hex((eventData[3] & 0xFF) / 16 & 0xF));
/* 75 */       sb.append(Functions.convertInt2Hex(eventData[3] & 0xFF & 0xF));
/* 76 */       sb.append(Functions.formatToHex(eventData[4] & 0xFF, 2));
/* 77 */       sb.append(Functions.formatToHex(eventData[5] & 0xFF, 2));
/* 78 */       sb.append(Functions.formatToHex(eventData[6] & 0xFF, 2));
/* 79 */       sb.append(Functions.convertInt2Hex((eventData[7] & 0xFF) / 16 & 0xF));
/* 80 */       sb.append("0000");
/* 81 */       int chkSum = sb.charAt(1);
/* 82 */       for (int i = 2; i < sb.length(); i++) {
/* 83 */         chkSum ^= sb.charAt(i);
/*    */       }
/* 85 */       sb.append((char)chkSum);
/* 86 */       this.formattedEvent = sb.toString().toUpperCase();
/* 87 */       return true;
/*    */     } 
/* 89 */     return false;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EmulateReceiverCMPlus.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */