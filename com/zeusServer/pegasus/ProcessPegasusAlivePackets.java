/*     */ package com.zeusServer.pegasus;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.socket.communication.TCPMessageServer;
/*     */ import com.zeusServer.tblConnections.TblActivePegasusMobileConnections;
/*     */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import com.zeuscc.pegasus.derby.beans.ModuleBean;
/*     */ import java.io.IOException;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
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
/*     */ public class ProcessPegasusAlivePackets
/*     */   implements Runnable
/*     */ {
/*  38 */   private final int TIME_BETWEEN_EXECUTIONS_THREAD_PROCESSING_ALIVE_PACKETS = 1000;
/*  39 */   private final float V1_BATTERY_VOLTAGE_MINIMUM_LEVEL = 10.0F;
/*  40 */   private final float V2_BATTERY_VOLTAGE_MINIMUM_LEVEL = 3.6F;
/*  41 */   private final int PEGASUS_1_5_FIXED_BIT = 128;
/*     */   
/*     */   private static long nextPrintTimeParsePendingAlivePackets;
/*     */   public static Long wdt;
/*     */   public boolean flag;
/*     */   
/*     */   public ProcessPegasusAlivePackets() {
/*  48 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  49 */     this.flag = true;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  55 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Task_for_processing_of_ALIVE_packets_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  56 */       while (this.flag) {
/*     */         
/*  58 */         try { if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM)
/*  59 */           { wdt = Functions.updateWatchdog(wdt, 100L);
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
/*     */             
/* 157 */             wdt = Functions.updateWatchdog(wdt, 1000L);
/* 158 */             Thread.sleep(10L); continue; }  List<PendingDataHolder> dataList = PegasusDBManager.getAllPendingAlive(); if (dataList != null) { long startCurrentTimeMillis = System.currentTimeMillis(); List<ModuleBean> mBeanList = new ArrayList<>(); List<ModuleBean> v2mBeanList = new ArrayList<>(); for (PendingDataHolder pdh : dataList) { ModuleBean mBean = new ModuleBean(); mBean.setIdModule(pdh.getIdModule()); mBean.setIdClient(pdh.getIdClient()); mBean.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0)); mBean.setId_Pending_Alive(pdh.getIdPendingAlive()); mBean.setLastCommunication(pdh.getReceived()); mBean.setLastCommInterface(pdh.getLastCommInterface()); mBean.setMinGSMSignalLevel(pdh.getMinGsmSignalLevel()); mBean.setAlarmPanelCommTestStatus((short)-1); if (pdh.getVersion() == 1) { byte[] buffer = pdh.getContent(); mBean.setBattery_Voltage_Min_Level(10.0F); mBean.setAlarmPanelConnectionStatus((short)((buffer[0] & 0x30) / 16)); mBean.setDualMonitoringStatus((short)((buffer[0] & 0x8) / 8)); mBean.setPhoneLineStatus((short)((buffer[0] & 0x80) / 128)); mBean.setLastGSMSignalLevel((short)buffer[2]); mBean.setBattery_Level((buffer[3] & 0xFF) * 0.075F); mBean.setAlarmPanelCommStatus((short)(buffer[1] & 0x1)); mBean.setMainPwrSupplyStatus((short)((buffer[1] & 0x2) / 2)); mBean.setAlarmPaenlReturnStatus((short)((buffer[1] & 0x4) / 4)); if ((buffer[7] & 0xFF) == 128) { char[] zoneInfo12 = Functions.getBinaryFromByte(buffer[8] & 0xFF); char[] zoneInfo34 = Functions.getBinaryFromByte(buffer[9] & 0xFF); mBean.setZone1Status((short)Functions.getIntFrom4CharBinaryBits(zoneInfo12[4], zoneInfo12[5], zoneInfo12[6], zoneInfo12[7])); mBean.setZone2Status((short)Functions.getIntFrom4CharBinaryBits(zoneInfo12[0], zoneInfo12[1], zoneInfo12[2], zoneInfo12[3])); mBean.setZone3Status((short)Functions.getIntFrom4CharBinaryBits(zoneInfo34[4], zoneInfo34[5], zoneInfo34[6], zoneInfo34[7])); mBean.setZone4Status((short)Functions.getIntFrom4CharBinaryBits(zoneInfo34[0], zoneInfo34[1], zoneInfo34[2], zoneInfo34[3])); mBean.setCurentBattCharge((short)buffer[10]); mBean.setAlarmPanelCommTestStatus((short)Functions.getIntFromHexByte(buffer[11])); mBean.setNewv1(true); } else { mBean.setZone1Status((short)((buffer[1] & 0x8) / 8)); mBean.setZone2Status((short)((buffer[1] & 0x10) / 16)); mBean.setZone3Status((short)((buffer[1] & 0x20) / 32)); mBean.setZone4Status((short)((buffer[1] & 0x40) / 64)); }  mBean.setLastNWProtocol(pdh.getLastNWProtocol()); mBeanList.add(mBean); continue; }  if (pdh.getVersion() == 2) v2mBeanList.add(parsePegasusV2AlivePacket(mBean, pdh.getContent()));  }  if (nextPrintTimeParsePendingAlivePackets < System.currentTimeMillis()) { Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Time_parse_Pending_Alive_Packets") + (System.currentTimeMillis() - startCurrentTimeMillis) + "ms", Enums.EnumMessagePriority.HIGH, null, null); nextPrintTimeParsePendingAlivePackets = System.currentTimeMillis() + 5000L; }  if (mBeanList.size() > 0) { startCurrentTimeMillis = System.currentTimeMillis(); try { TblPegasusActiveConnections.semaphoreAlivePacketsReceived.acquire(); PegasusDBManager.executeSP_059(mBeanList); } finally { TblPegasusActiveConnections.semaphoreAlivePacketsReceived.release(); }  Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Time_process_Pending_Alive_Packets_V1") + (System.currentTimeMillis() - startCurrentTimeMillis) + "ms", Enums.EnumMessagePriority.HIGH, null, null); }  if (v2mBeanList.size() > 0) { startCurrentTimeMillis = System.currentTimeMillis(); try { TblPegasusActiveConnections.semaphoreAlivePacketsReceived.acquire(); PegasusDBManager.executeSP_V2_002(v2mBeanList); } finally { TblPegasusActiveConnections.semaphoreAlivePacketsReceived.release(); }  Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Time_process_Pending_Alive_Packets_V2") + (System.currentTimeMillis() - startCurrentTimeMillis) + "ms", Enums.EnumMessagePriority.HIGH, null, null); }  }  } catch (Exception exception) {  } finally { wdt = Functions.updateWatchdog(wdt, 1000L); Thread.sleep(10L); }
/*     */       
/*     */       } 
/* 161 */     } catch (InterruptedException ex) {
/* 162 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_processing_of_ALIVE_packets"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 163 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/* 165 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private ModuleBean parsePegasusV2AlivePacket(ModuleBean mBean, byte[] buffer) throws InterruptedException, UnsupportedEncodingException, IOException {
/* 170 */     boolean appDataReceived = false;
/* 171 */     byte[] tmp4 = new byte[4];
/* 172 */     byte[] tmp2 = new byte[4];
/* 173 */     byte[] fid = new byte[2];
/* 174 */     int index = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 182 */     mBean.setLastGSMSignalLevel((short)-1);
/* 183 */     mBean.setZone1Status((short)-1);
/* 184 */     mBean.setZone2Status((short)-1);
/* 185 */     mBean.setZone3Status((short)-1);
/* 186 */     mBean.setZone4Status((short)-1);
/* 187 */     mBean.setSystemStatus((short)-1);
/* 188 */     mBean.setHwFailStatus((short)-1);
/* 189 */     mBean.setPhoneLineStatus((short)-1);
/* 190 */     mBean.setAlarmPaenlReturnStatus((short)-1);
/* 191 */     mBean.setDualMonitoringStatus((short)-1);
/* 192 */     mBean.setMainPwrSupplyStatus((short)-1);
/* 193 */     mBean.setAlarmPanelCommStatus((short)-1);
/* 194 */     mBean.setGsmJammerStatus((short)-1);
/* 195 */     mBean.setGsmJammerJDRStatus((short)-1);
/* 196 */     mBean.setCurrentBattVoltage(-1.0F);
/* 197 */     mBean.setCurentBattCharge((short)-1);
/* 198 */     mBean.setBatteryChargerStatus((short)-1);
/* 199 */     mBean.setAlarmPanelConnectionStatus((short)-1);
/* 200 */     mBean.setAlarmPanelConnectionOperationMode((short)-1);
/* 201 */     mBean.setAlarmPanelCommTestStatus((short)-1);
/* 202 */     mBean.setPhoneLineCommTestStatus((short)-1);
/* 203 */     mBean.setEthCommTestStatus((short)-1);
/* 204 */     mBean.setModemIfaceStatus_0((short)-1);
/* 205 */     mBean.setModemIfaceStatus_1((short)-1);
/* 206 */     mBean.setModemIfaceStatus_2((short)-1);
/* 207 */     mBean.setModemIfaceStatus_3((short)-1);
/* 208 */     mBean.setTamperStatus((short)-1);
/* 209 */     mBean.setWifiCommTestStatus_AP_1((short)-1);
/* 210 */     mBean.setWifiCommTestStatus_AP_2((short)-1);
/* 211 */     mBean.setSimcard1Status((short)-1);
/* 212 */     mBean.setSimcard1JDRStatus((short)-1);
/* 213 */     mBean.setSimcard1OperativeStatus((short)-1);
/* 214 */     mBean.setSimcard2Status((short)-1);
/* 215 */     mBean.setSimcard2JDRStatus((short)-1);
/* 216 */     mBean.setSimcard2OperativeStatus((short)-1);
/* 217 */     mBean.setGprsDataCounter(-1);
/* 218 */     mBean.setBatteryOverTemprature((short)-1);
/* 219 */     mBean.setBatteryOverTimeCharge((short)-1);
/* 220 */     mBean.setBatteryConnectedButNotConfigured((short)-1);
/* 221 */     mBean.setLastOtaStatus((short)-1);
/* 222 */     mBean.setRelayStatus((short)-1);
/* 223 */     mBean.setPgm_1_status((short)-1);
/* 224 */     mBean.setPgm_2_status((short)-1);
/* 225 */     mBean.setPgm_3_status((short)-1);
/* 226 */     mBean.setAltitude(-1.0F);
/* 227 */     mBean.setLongitude(-1.0F);
/* 228 */     mBean.setLattitude(-1.0F);
/* 229 */     mBean.setBattery_Voltage_Min_Level(3.6F);
/* 230 */     mBean.setTimezone(-1);
/* 231 */     mBean.setKeyfobLowBattery(-1);
/* 232 */     mBean.setSystemInAlarm((short)-1);
/* 233 */     mBean.setIpic((short)-1);
/* 234 */     mBean.setGsmDataCarrier((short)-1);
/* 235 */     mBean.setBatteryConnectionStatus((short)-1);
/*     */ 
/*     */ 
/*     */     
/* 239 */     while (index < buffer.length && 
/* 240 */       index + 2 <= buffer.length) {
/*     */       long dValue; int tmp; StringBuilder sb; short simNum, apn;
/*     */       char[] pgm;
/* 243 */       System.arraycopy(buffer, index, fid, 0, 2);
/* 244 */       index += 2;
/* 245 */       fid = Functions.swapLSB2MSB(fid);
/* 246 */       int fidVal = Functions.getIntFrom2ByteArray(fid);
/* 247 */       if (fidVal <= 0) {
/*     */         break;
/*     */       }
/* 250 */       short flen = (short)Functions.getIntFromHexByte(buffer[index]);
/* 251 */       byte[] fcon = new byte[flen];
/* 252 */       System.arraycopy(buffer, ++index, fcon, 0, flen);
/* 253 */       index += flen;
/*     */       
/* 255 */       switch (fidVal) {
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
/*     */         case 7:
/* 275 */           mBean.setHwFailStatus((short)Functions.getIntFromHexByte(fcon[0]));
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
/*     */         case 13:
/* 294 */           mBean.setOperationMode((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 14:
/* 298 */           mBean.setPhoneLineStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 15:
/* 302 */           mBean.setAlarmPaenlReturnStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 16:
/* 306 */           mBean.setDualMonitoringStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 17:
/* 310 */           mBean.setZone1Status((short)Functions.getIntFromHexByte(fcon[0]));
/* 311 */           mBean.setZone2Status((short)Functions.getIntFromHexByte(fcon[1]));
/* 312 */           mBean.setZone3Status((short)Functions.getIntFromHexByte(fcon[2]));
/* 313 */           mBean.setZone4Status((short)Functions.getIntFromHexByte(fcon[3]));
/*     */ 
/*     */         
/*     */         case 18:
/* 317 */           mBean.setMainPwrSupplyStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 19:
/* 321 */           mBean.setAlarmPanelCommStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 20:
/* 325 */           mBean.setGsmJammerStatus((short)Functions.getIntFromHexByte(fcon[0]));
/* 326 */           mBean.setGsmJammerJDRStatus((short)Functions.getIntFromHexByte(fcon[1]));
/*     */ 
/*     */         
/*     */         case 21:
/* 330 */           mBean.setLastGSMSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 22:
/* 334 */           sb = new StringBuilder();
/* 335 */           sb.append((short)Functions.getIntFromHexByte(fcon[0])).append('.');
/* 336 */           sb.append((short)Functions.getIntFromHexByte(fcon[1]));
/* 337 */           mBean.setCurrentBattVoltage(Float.parseFloat(sb.toString()));
/*     */ 
/*     */         
/*     */         case 23:
/* 341 */           mBean.setCurentBattCharge((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 24:
/* 345 */           mBean.setBatteryChargerStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 25:
/* 349 */           mBean.setAlarmPanelConnectionOperationMode((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 26:
/* 353 */           mBean.setAlarmPanelConnectionStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 27:
/* 357 */           mBean.setAlarmPanelCommTestStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 28:
/* 361 */           mBean.setPhoneLineCommTestStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 29:
/* 365 */           mBean.setEthCommTestStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 30:
/* 369 */           simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 370 */           apn = (short)Functions.getIntFromHexByte(fcon[1]);
/* 371 */           if (simNum == 1 && apn == 1) {
/* 372 */             mBean.setModemIfaceStatus_0((short)Functions.getIntFromHexByte(fcon[2])); continue;
/* 373 */           }  if (simNum == 1 && apn == 2) {
/* 374 */             mBean.setModemIfaceStatus_1((short)Functions.getIntFromHexByte(fcon[2])); continue;
/* 375 */           }  if (simNum == 2 && apn == 1) {
/* 376 */             mBean.setModemIfaceStatus_2((short)Functions.getIntFromHexByte(fcon[2])); continue;
/* 377 */           }  if (simNum == 2 && apn == 2) {
/* 378 */             mBean.setModemIfaceStatus_3((short)Functions.getIntFromHexByte(fcon[2]));
/*     */           }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         case 32:
/* 386 */           mBean.setTamperStatus((short)Functions.getIntFromHexByte(fcon[0]));
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
/*     */         case 36:
/* 399 */           if ((short)Functions.getIntFromHexByte(fcon[0]) == 0) {
/* 400 */             mBean.setWifiCommTestStatus_AP_1((short)(fcon[1] & 0xFF)); continue;
/* 401 */           }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 402 */             mBean.setWifiCommTestStatus_AP_2((short)(fcon[1] & 0xFF));
/*     */           }
/*     */ 
/*     */         
/*     */         case 37:
/* 407 */           mBean.setLastWIFISignalLevel(Functions.covertDigit2Percent((short)fcon[0]));
/*     */ 
/*     */         
/*     */         case 38:
/* 411 */           mBean.setSimcard1Status((short)Functions.getIntFromHexByte(fcon[0]));
/* 412 */           mBean.setSimcard1OperativeStatus((short)Functions.getIntFromHexByte(fcon[1]));
/* 413 */           mBean.setSimcard1JDRStatus((short)Functions.getIntFromHexByte(fcon[2]));
/*     */ 
/*     */         
/*     */         case 39:
/* 417 */           mBean.setSimcard2Status((short)Functions.getIntFromHexByte(fcon[0]));
/* 418 */           mBean.setSimcard2OperativeStatus((short)Functions.getIntFromHexByte(fcon[1]));
/* 419 */           mBean.setSimcard2JDRStatus((short)Functions.getIntFromHexByte(fcon[2]));
/*     */ 
/*     */         
/*     */         case 40:
/* 423 */           mBean.setGprsDataCounter(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*     */ 
/*     */         
/*     */         case 41:
/* 427 */           mBean.setLastOtaStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 42:
/* 431 */           mBean.setBatteryOverTemprature((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 43:
/* 435 */           mBean.setBatteryOverTimeCharge((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 62:
/* 439 */           mBean.setBatteryConnectedButNotConfigured((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 64:
/* 443 */           mBean.setBatteryConnectionStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 46:
/* 447 */           pgm = Functions.getBinaryFromByte((short)Functions.getIntFromHexByte(fcon[0]));
/* 448 */           mBean.setRelayStatus((short)Character.digit(pgm[7], 10));
/* 449 */           mBean.setPgm_1_status((short)Character.digit(pgm[6], 10));
/* 450 */           mBean.setPgm_2_status((short)Character.digit(pgm[5], 10));
/* 451 */           mBean.setPgm_3_status((short)Character.digit(pgm[4], 10));
/* 452 */           appDataReceived = true;
/*     */ 
/*     */         
/*     */         case 48:
/* 456 */           tmp4[0] = fcon[0];
/* 457 */           tmp4[1] = fcon[1];
/* 458 */           tmp4[2] = fcon[2];
/* 459 */           tmp4[3] = fcon[3];
/* 460 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 461 */           mBean.setLattitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*     */ 
/*     */         
/*     */         case 47:
/* 465 */           tmp4[0] = fcon[0];
/* 466 */           tmp4[1] = fcon[1];
/* 467 */           tmp4[2] = fcon[2];
/* 468 */           tmp4[3] = fcon[3];
/* 469 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 470 */           mBean.setLongitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*     */ 
/*     */         
/*     */         case 49:
/* 474 */           tmp4[0] = fcon[0];
/* 475 */           tmp4[1] = fcon[1];
/* 476 */           tmp4[2] = fcon[2];
/* 477 */           tmp4[3] = fcon[3];
/* 478 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 479 */           mBean.setAltitude((float)(dValue / 100.0D));
/*     */ 
/*     */         
/*     */         case 50:
/* 483 */           tmp4[0] = fcon[0];
/* 484 */           tmp4[1] = fcon[1];
/* 485 */           tmp4[2] = fcon[2];
/* 486 */           tmp4[3] = fcon[3];
/* 487 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 488 */           mBean.setLattitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/* 489 */           tmp4[0] = fcon[4];
/* 490 */           tmp4[1] = fcon[5];
/* 491 */           tmp4[2] = fcon[6];
/* 492 */           tmp4[3] = fcon[7];
/* 493 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 494 */           mBean.setLongitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/* 495 */           tmp4[0] = fcon[8];
/* 496 */           tmp4[1] = fcon[9];
/* 497 */           tmp4[2] = fcon[10];
/* 498 */           tmp4[3] = fcon[11];
/* 499 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 500 */           mBean.setAltitude((float)(dValue / 100.0D));
/*     */ 
/*     */         
/*     */         case 51:
/* 504 */           tmp2[0] = fcon[0];
/* 505 */           tmp2[1] = fcon[1];
/* 506 */           mBean.setTimezone(Functions.getSignedIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2)));
/*     */ 
/*     */         
/*     */         case 52:
/* 510 */           mBean.setZone1Status((short)fcon[0]);
/* 511 */           appDataReceived = true;
/*     */ 
/*     */         
/*     */         case 53:
/* 515 */           mBean.setZone2Status((short)fcon[0]);
/* 516 */           appDataReceived = true;
/*     */ 
/*     */         
/*     */         case 54:
/* 520 */           mBean.setZone3Status((short)fcon[0]);
/* 521 */           appDataReceived = true;
/*     */ 
/*     */         
/*     */         case 55:
/* 525 */           mBean.setZone4Status((short)fcon[0]);
/* 526 */           appDataReceived = true;
/*     */ 
/*     */         
/*     */         case 56:
/* 530 */           mBean.setSystemStatus((short)fcon[0]);
/* 531 */           appDataReceived = true;
/*     */ 
/*     */         
/*     */         case 57:
/* 535 */           tmp2[0] = fcon[1];
/* 536 */           tmp2[1] = fcon[0];
/* 537 */           tmp = Functions.getIntFrom2ByteArray(tmp2);
/* 538 */           mBean.setbKeyfobPanic(Functions.getBinaryFromInt(tmp));
/*     */ 
/*     */         
/*     */         case 58:
/* 542 */           tmp2[0] = fcon[1];
/* 543 */           tmp2[1] = fcon[0];
/* 544 */           tmp = Functions.getIntFrom2ByteArray(tmp2);
/* 545 */           mBean.setbKeyfobCommTest(Functions.getBinaryFromInt(tmp));
/*     */ 
/*     */         
/*     */         case 59:
/* 549 */           tmp2[0] = fcon[1];
/* 550 */           tmp2[1] = fcon[0];
/* 551 */           tmp = Functions.getIntFrom2ByteArray(tmp2);
/* 552 */           mBean.setKeyfobLowBattery(tmp);
/* 553 */           mBean.setbKeyfobLowBattery(Functions.getBinaryFromInt(tmp));
/*     */ 
/*     */         
/*     */         case 60:
/* 557 */           mBean.setSystemInAlarm((short)fcon[0]);
/*     */ 
/*     */         
/*     */         case 61:
/* 561 */           mBean.setIpic((short)fcon[0]);
/*     */ 
/*     */         
/*     */         case 63:
/* 565 */           mBean.setGsmDataCarrier((short)fcon[0]);
/*     */       } 
/*     */     
/*     */     } 
/* 569 */     if (appDataReceived) {
/* 570 */       pushAppDataReceived(mBean.getIdClient());
/*     */     }
/* 572 */     return mBean;
/*     */   }
/*     */ 
/*     */   
/*     */   private void dispose() {
/* 577 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Task_for_processing_of_ALIVE_packets_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void pushAppDataReceived(int idClient) throws UnsupportedEncodingException, IOException {
/* 587 */     if (TblActivePegasusMobileConnections.getInstance().containsKey(Integer.valueOf(idClient)) && 
/* 588 */       TCPMessageServer.pegamobileAppDataUpdater != null && TCPMessageServer.pegamobileAppDataUpdater.clientSocket != null) {
/* 589 */       String ic = String.valueOf(idClient);
/* 590 */       byte[] data = new byte[4 + ic.length()];
/* 591 */       data[0] = 117;
/* 592 */       data[1] = (byte)ic.length();
/* 593 */       System.arraycopy(ic.getBytes("ASCII"), 0, data, 2, ic.length());
/* 594 */       int crcCalc = CRC16.calculate(data, 0, ic.length() + 2, 65535);
/* 595 */       data[ic.length() + 2] = (byte)(crcCalc & 0xFF);
/* 596 */       data[ic.length() + 2 + 1] = (byte)((crcCalc & 0xFF00) / 256);
/* 597 */       TCPMessageServer.pegamobileAppDataUpdater.sendData(data);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\ProcessPegasusAlivePackets.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */