/*     */ package com.zeusServer.griffon;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GriffonDBManager;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import com.zeuscc.griffon.derby.beans.GriffonEnums;
/*     */ import com.zeuscc.griffon.derby.beans.GriffonModule;
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
/*     */ public class ProcessAlivePackets
/*     */   implements Runnable
/*     */ {
/*  33 */   private final int TIME_BETWEEN_EXECUTIONS_THREAD_PROCESSING_ALIVE_PACKETS = 10000;
/*     */   public static Long wdt;
/*     */   public boolean flag;
/*     */   
/*     */   public ProcessAlivePackets() {
/*  38 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  39 */     this.flag = true;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  45 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_processing_of_ALIVE_packets_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  46 */       while (this.flag) {
/*     */         
/*  48 */         try { if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM)
/*  49 */           { wdt = Functions.updateWatchdog(wdt, 100L);
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
/*  79 */             wdt = Functions.updateWatchdog(wdt, 10000L);
/*  80 */             Thread.sleep(10L); continue; }  List<PendingDataHolder> dataList = GriffonDBManager.getAllPendingAlive(); if (dataList != null) { List<GriffonModule> mBeanList = new ArrayList<>(); for (PendingDataHolder pdh : dataList) { GriffonModule gModule = new GriffonModule(); gModule.setDefaults(); gModule.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0)); gModule.setId_Module(pdh.getIdModule()); gModule.setId_Client(pdh.getIdClient()); gModule.setIdPendingAlive(pdh.getIdPendingAlive()); gModule.setLastCommunication(pdh.getReceived()); gModule.setCurrentInterface(pdh.getLastCommInterface()); mBeanList.add(parseGriffonAlivePacket(gModule, pdh.getContent())); }  if (mBeanList.size() > 0) try { TblGriffonActiveConnections.semaphoreAlivePacketsReceived.acquire(); GriffonDBManager.executeSPG_003(mBeanList); } finally { TblGriffonActiveConnections.semaphoreAlivePacketsReceived.release(); }   }  } catch (InterruptedException|java.sql.SQLException interruptedException) {  } finally { wdt = Functions.updateWatchdog(wdt, 10000L); Thread.sleep(10L); }
/*     */       
/*     */       } 
/*  83 */     } catch (InterruptedException ex) {
/*  84 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_processing_of_ALIVE_packets"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  85 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/*  87 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private GriffonModule parseGriffonAlivePacket(GriffonModule gModule, byte[] buffer) {
/*  92 */     int index = 0;
/*  93 */     byte[] fid = new byte[2];
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  99 */     byte[] tmp2 = new byte[2];
/* 100 */     byte[] tmp4 = new byte[4];
/*     */     
/* 102 */     gModule.setModemIfaceStatus_0((short)-1);
/* 103 */     gModule.setModemIfaceStatus_1((short)-1);
/* 104 */     gModule.setModemIfaceStatus_2((short)-1);
/* 105 */     gModule.setModemIfaceStatus_3((short)-1);
/* 106 */     gModule.setWifiCommTestStatus_AP_1((short)-1);
/* 107 */     gModule.setWifiCommTestStatus_AP_2((short)-1);
/* 108 */     gModule.setBatteryStatus(-1);
/*     */     
/* 110 */     while (index < buffer.length && 
/* 111 */       index + 2 <= buffer.length) {
/*     */       long dValue;
/*     */       int eventQualifier;
/* 114 */       System.arraycopy(buffer, index, fid, 0, 2);
/* 115 */       index += 2;
/* 116 */       fid = Functions.swapLSB2MSB(fid);
/* 117 */       int fidVal = Functions.getIntFrom2ByteArray(fid);
/* 118 */       if (fidVal <= 0) {
/*     */         break;
/*     */       }
/* 121 */       short flen = (short)Functions.getIntFromHexByte(buffer[index]);
/* 122 */       byte[] fcon = new byte[flen];
/* 123 */       System.arraycopy(buffer, ++index, fcon, 0, flen);
/* 124 */       index += flen;
/*     */       
/* 126 */       switch (fidVal) {
/*     */         case 3:
/* 128 */           eventQualifier = fcon[8] & 0xFF;
/* 129 */           switch (fcon[11] & 0xFF) {
/*     */             case 1:
/* 131 */               switch (fcon[12] & 0xFF) {
/*     */                 case 1:
/* 133 */                   if (eventQualifier == 1) {
/* 134 */                     gModule.setMainPwrStatus((short)0); break;
/* 135 */                   }  if (eventQualifier == 3) {
/* 136 */                     gModule.setMainPwrStatus((short)1);
/*     */                   }
/*     */                   break;
/*     */                 
/*     */                 case 2:
/*     */                 case 3:
/*     */                   break;
/*     */                 case 4:
/* 144 */                   if (eventQualifier == 1) {
/* 145 */                     gModule.setAuxOutputStatus((short)1); break;
/* 146 */                   }  if (eventQualifier == 3) {
/* 147 */                     gModule.setAuxOutputStatus((short)0);
/*     */                   }
/*     */                   break;
/*     */                 case 5:
/* 151 */                   if (eventQualifier == 1) {
/* 152 */                     gModule.setBellStatus((short)GriffonEnums.EnumBellStatus.OVERLOADED.getStatus()); break;
/* 153 */                   }  if (eventQualifier == 3) {
/* 154 */                     gModule.setBellStatus((short)GriffonEnums.EnumBellStatus.NORMAL.getStatus());
/*     */                   }
/*     */                   break;
/*     */                 case 6:
/* 158 */                   if (eventQualifier == 1) {
/* 159 */                     gModule.setBellStatus((short)GriffonEnums.EnumBellStatus.DISCONNECTED.getStatus()); break;
/* 160 */                   }  if (eventQualifier == 3) {
/* 161 */                     gModule.setBellStatus((short)GriffonEnums.EnumBellStatus.NORMAL.getStatus());
/*     */                   }
/*     */                   break;
/*     */                 
/*     */                 case 7:
/*     */                 case 8:
/*     */                   break;
/*     */                 case 9:
/* 169 */                   if (eventQualifier == 1) {
/* 170 */                     gModule.setTamperStatus((short)1); break;
/* 171 */                   }  if (eventQualifier == 3) {
/* 172 */                     gModule.setTamperStatus((short)0);
/*     */                   }
/*     */                   break;
/*     */                 case 10:
/*     */                   break;
/*     */                 case 11:
/* 178 */                   if (eventQualifier == 1) {
/* 179 */                     gModule.setDualMonitoringStatus((short)1); break;
/* 180 */                   }  if (eventQualifier == 3) {
/* 181 */                     gModule.setDualMonitoringStatus((short)0);
/*     */                   }
/*     */                   break;
/*     */                 case 12:
/* 185 */                   if (eventQualifier == 1) {
/* 186 */                     gModule.setPhoneLineStatus((short)0); break;
/* 187 */                   }  if (eventQualifier == 3) {
/* 188 */                     gModule.setPhoneLineStatus((short)1);
/*     */                   }
/*     */                   break;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */                 
/*     */                 case 13:
/*     */                 case 14:
/*     */                 case 15:
/*     */                 case 16:
/*     */                 case 17:
/*     */                 case 18:
/*     */                 case 19:
/*     */                   break;
/*     */               } 
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
/*     */             case 11:
/* 228 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 1, eventQualifier));
/*     */             
/*     */             case 12:
/* 231 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 2, eventQualifier));
/*     */             
/*     */             case 13:
/* 234 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 3, eventQualifier));
/*     */             
/*     */             case 14:
/* 237 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 4, eventQualifier));
/*     */             
/*     */             case 15:
/* 240 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 5, eventQualifier));
/*     */             
/*     */             case 16:
/* 243 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 6, eventQualifier));
/*     */             
/*     */             case 17:
/* 246 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 7, eventQualifier));
/*     */             
/*     */             case 18:
/* 249 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 8, eventQualifier));
/*     */             
/*     */             case 19:
/* 252 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 9, eventQualifier));
/*     */             
/*     */             case 20:
/* 255 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 10, eventQualifier));
/*     */             
/*     */             case 21:
/* 258 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 11, eventQualifier));
/*     */             
/*     */             case 22:
/* 261 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 12, eventQualifier));
/*     */             
/*     */             case 23:
/* 264 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 13, eventQualifier));
/*     */             
/*     */             case 24:
/* 267 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 14, eventQualifier));
/*     */             
/*     */             case 25:
/* 270 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 15, eventQualifier));
/*     */ 
/*     */ 
/*     */             
/*     */             case 27:
/* 275 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 16, eventQualifier));
/*     */             
/*     */             case 28:
/* 278 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 17, eventQualifier));
/*     */             
/*     */             case 29:
/* 281 */               gModule.setHwFailure(getHwFailStatus(gModule.getHwFailure(), 18, eventQualifier));
/*     */           } 
/*     */         
/*     */         
/*     */         
/*     */         case 83:
/* 287 */           gModule.setBatteryStatus(fcon[0] & 0xFF);
/* 288 */           gModule.setCurrentBatteryVoltage((new Float((fcon[1] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[2] & 0xFF) }))).floatValue());
/* 289 */           gModule.setBatteryInputCurrent(Float.parseFloat((fcon[3] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[4] & 0xFF) })));
/*     */ 
/*     */         
/*     */         case 89:
/* 293 */           gModule.setGsmSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 90:
/* 297 */           gModule.setModemIfaceStatusSelf_EB((short)(fcon[0] & 0xFF));
/* 298 */           if (Functions.getIntFromHexByte(fcon[1]) == 1 && Functions.getIntFromHexByte(fcon[2]) == 1) {
/* 299 */             gModule.setModemIfaceStatus_0((short)Functions.getIntFromHexByte(fcon[3])); continue;
/* 300 */           }  if (Functions.getIntFromHexByte(fcon[1]) == 1 && Functions.getIntFromHexByte(fcon[2]) == 2) {
/* 301 */             gModule.setModemIfaceStatus_1((short)Functions.getIntFromHexByte(fcon[3])); continue;
/* 302 */           }  if (Functions.getIntFromHexByte(fcon[1]) == 2 && Functions.getIntFromHexByte(fcon[2]) == 1) {
/* 303 */             gModule.setModemIfaceStatus_2((short)Functions.getIntFromHexByte(fcon[3])); continue;
/* 304 */           }  if (Functions.getIntFromHexByte(fcon[1]) == 2 && Functions.getIntFromHexByte(fcon[2]) == 2) {
/* 305 */             gModule.setModemIfaceStatus_3((short)Functions.getIntFromHexByte(fcon[3]));
/*     */           }
/*     */ 
/*     */         
/*     */         case 93:
/* 310 */           gModule.setGprsDataVolume(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*     */ 
/*     */         
/*     */         case 92:
/* 314 */           gModule.setOtaStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 91:
/* 318 */           gModule.setGsmJammerStatus((short)Functions.getIntFromHexByte(fcon[0]));
/* 319 */           gModule.setGsmJDRStatus((short)Functions.getIntFromHexByte(fcon[1]));
/*     */ 
/*     */         
/*     */         case 98:
/* 323 */           if ((fcon[0] & 0xFF) == 0) {
/* 324 */             gModule.setSimCard1Status((short)Functions.getIntFromHexByte(fcon[1]));
/* 325 */             gModule.setSimCard1OperativeStatus((short)Functions.getIntFromHexByte(fcon[2]));
/* 326 */             gModule.setSimCard1JDRStatus((short)Functions.getIntFromHexByte(fcon[3])); continue;
/* 327 */           }  if ((fcon[0] & 0xFF) == 1) {
/* 328 */             gModule.setSimCard2Status((short)Functions.getIntFromHexByte(fcon[1]));
/* 329 */             gModule.setSimCard2OperativeStatus((short)Functions.getIntFromHexByte(fcon[2]));
/* 330 */             gModule.setSimCard2JDRStatus((short)Functions.getIntFromHexByte(fcon[3]));
/*     */           } 
/*     */ 
/*     */         
/*     */         case 99:
/* 335 */           tmp4[0] = fcon[0];
/* 336 */           tmp4[1] = fcon[1];
/* 337 */           tmp4[2] = fcon[2];
/* 338 */           tmp4[3] = fcon[3];
/* 339 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 340 */           gModule.setLongitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*     */           
/* 342 */           tmp4[0] = fcon[4];
/* 343 */           tmp4[1] = fcon[5];
/* 344 */           tmp4[2] = fcon[6];
/* 345 */           tmp4[3] = fcon[7];
/* 346 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 347 */           gModule.setLattitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*     */           
/* 349 */           tmp4[0] = fcon[8];
/* 350 */           tmp4[1] = fcon[9];
/* 351 */           tmp4[2] = fcon[10];
/* 352 */           tmp4[3] = fcon[11];
/* 353 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 354 */           gModule.setAltitude((float)(dValue / 100.0D));
/*     */ 
/*     */         
/*     */         case 100:
/* 358 */           gModule.setTelephoneTestStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 101:
/* 362 */           gModule.setEthIfaceSelf_EB((short)Functions.getIntFromHexByte(fcon[0]));
/* 363 */           gModule.setEthCommTestStatus((short)Functions.getIntFromHexByte(fcon[1]));
/*     */ 
/*     */         
/*     */         case 105:
/* 367 */           gModule.setWifiIfaceStatusSelf_EB((short)Functions.getIntFromHexByte(fcon[0]));
/* 368 */           if ((fcon[1] & 0xFF) == 0) {
/* 369 */             gModule.setWifiCommTestStatus_AP_1((short)(fcon[2] & 0xFF)); continue;
/* 370 */           }  if ((fcon[1] & 0xFF) == 1) {
/* 371 */             gModule.setWifiCommTestStatus_AP_2((short)(fcon[2] & 0xFF));
/*     */           }
/*     */ 
/*     */         
/*     */         case 106:
/* 376 */           gModule.setWifiSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*     */ 
/*     */         
/*     */         case 107:
/* 380 */           tmp2[0] = fcon[2];
/* 381 */           tmp2[1] = fcon[1];
/* 382 */           gModule.setTimezone(Functions.getSignedIntFrom2ByteArray(tmp2));
/*     */ 
/*     */         
/*     */         case 113:
/* 386 */           gModule.setAuxOutputStatus((short)fcon[0]);
/* 387 */           gModule.setAuxVoltage(Float.parseFloat(fcon[1] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[2]) })));
/* 388 */           gModule.setAuxCurrent(Float.parseFloat(fcon[3] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[4]) })));
/*     */ 
/*     */         
/*     */         case 115:
/* 392 */           gModule.setTemparature((fcon[0] & 0xFF) - 40);
/*     */       } 
/*     */     
/*     */     } 
/* 396 */     return gModule;
/*     */   }
/*     */   
/*     */   private int getHwFailStatus(int hwFailStatus, int hwIdx, int eQualifier) {
/* 400 */     hwFailStatus = (hwFailStatus == -1) ? 0 : hwFailStatus;
/* 401 */     hwFailStatus = (eQualifier == 1) ? (hwFailStatus |= 1 << hwIdx) : (hwFailStatus &= 1 << hwIdx ^ 0xFFFFFFFF);
/* 402 */     return (short)hwFailStatus;
/*     */   }
/*     */   
/*     */   private void dispose() {
/* 406 */     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Task_for_processing_of_ALIVE_packets_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\ProcessAlivePackets.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */