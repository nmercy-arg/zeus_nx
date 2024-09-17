/*      */ package com.zeusServer.mercurius;
/*      */ 
/*      */ import com.zeus.mercuriusAVL.derby.beans.AVLOccurrence;
/*      */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLEnums;
/*      */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*      */ import com.zeusServer.dto.PendingDataHolder;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class ProcessAlivePackets
/*      */   implements Runnable
/*      */ {
/*   33 */   private final int TIME_BETWEEN_EXECUTIONS_THREAD_PROCESSING_ALIVE_PACKETS = 10000;
/*      */   public static Long wdt;
/*      */   public boolean flag;
/*      */   
/*      */   public ProcessAlivePackets() {
/*   38 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*   39 */     this.flag = true;
/*      */   }
/*      */ 
/*      */   
/*      */   public void run() {
/*      */     try {
/*   45 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_processing_of_ALIVE_packets_started"), Enums.EnumMessagePriority.LOW, null, null);
/*   46 */       while (this.flag) {
/*      */         
/*   48 */         try { if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM)
/*   49 */           { wdt = Functions.updateWatchdog(wdt, 100L);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*   75 */             wdt = Functions.updateWatchdog(wdt, 10000L); continue; }  List<PendingDataHolder> dataList = MercuriusDBManager.getAllPendingAlive(); if (dataList != null) { List<MercuriusAVLModule> mBeanList = new ArrayList<>(); for (PendingDataHolder pdh : dataList) { MercuriusAVLModule avlModule = new MercuriusAVLModule(); avlModule.setDefaults(); avlModule.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0)); avlModule.setId_Module(pdh.getIdModule()); avlModule.setId_Client(pdh.getIdClient()); avlModule.setIdPendingAlive(pdh.getIdPendingAlive()); avlModule.setLastCommunication(pdh.getReceived()); avlModule.setCurrentInterface(pdh.getLastCommInterface()); mBeanList.add(parseAVLAlivePacket(avlModule, pdh.getContent(), pdh.getTimezone())); }  if (mBeanList.size() > 0) MercuriusDBManager.executeSPM_002(mBeanList);  }  } catch (Exception exception) {  } finally { wdt = Functions.updateWatchdog(wdt, 10000L); }
/*      */       
/*      */       } 
/*   78 */     } catch (Exception ex) {
/*   79 */       ex.printStackTrace();
/*   80 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_processing_of_ALIVE_packets"), Enums.EnumMessagePriority.HIGH, null, ex);
/*   81 */       GlobalVariables.buzzerActivated = true;
/*      */     } finally {
/*   83 */       dispose();
/*      */     } 
/*      */   }
/*      */   
/*      */   private MercuriusAVLModule parseAVLAlivePacket(MercuriusAVLModule module, byte[] buffer, int timezone) throws ParseException {
/*   88 */     int index = 0;
/*   89 */     byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*   97 */     List<AVLOccurrence> avlOccurrenceList = null;
/*   98 */     List<AVLOccurrence> hwOccurrenceList = null;
/*      */     
/*  100 */     AVLOccurrence ao = null;
/*      */     
/*  102 */     byte[] tmp2 = new byte[2];
/*  103 */     byte[] tmp4 = new byte[4];
/*      */ 
/*      */     
/*  106 */     while (index < buffer.length && 
/*  107 */       index + 2 <= buffer.length) {
/*      */       short tmpShort, apn; long dValue; char[] bin; AVLOccurrence avlOccurrence; int tmp;
/*      */       StringBuilder sb;
/*  110 */       System.arraycopy(buffer, index, fid, 0, 2);
/*  111 */       index += 2;
/*  112 */       fid = Functions.swapLSB2MSB(fid);
/*  113 */       int fidVal = Functions.getIntFrom2ByteArray(fid);
/*  114 */       if (fidVal <= 0) {
/*      */         break;
/*      */       }
/*  117 */       short flen = (short)Functions.getIntFromHexByte(buffer[index]);
/*  118 */       byte[] fcon = new byte[flen];
/*  119 */       System.arraycopy(buffer, ++index, fcon, 0, flen);
/*  120 */       index += flen;
/*      */       
/*  122 */       switch (fidVal) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         case 9:
/*  148 */           if (Functions.getIntFromHexByte(fcon[0]) == 1 && Functions.getIntFromHexByte(fcon[1]) == 1) {
/*  149 */             module.setModemIfaceStatus_0((short)Functions.getIntFromHexByte(fcon[2])); continue;
/*  150 */           }  if (Functions.getIntFromHexByte(fcon[0]) == 1 && Functions.getIntFromHexByte(fcon[1]) == 2) {
/*  151 */             module.setModemIfaceStatus_1((short)Functions.getIntFromHexByte(fcon[2])); continue;
/*  152 */           }  if (Functions.getIntFromHexByte(fcon[0]) == 2 && Functions.getIntFromHexByte(fcon[1]) == 1) {
/*  153 */             module.setModemIfaceStatus_2((short)Functions.getIntFromHexByte(fcon[2])); continue;
/*  154 */           }  if (Functions.getIntFromHexByte(fcon[0]) == 2 && Functions.getIntFromHexByte(fcon[1]) == 2) {
/*  155 */             module.setModemIfaceStatus_3((short)Functions.getIntFromHexByte(fcon[2]));
/*      */           }
/*      */ 
/*      */         
/*      */         case 10:
/*  160 */           module.setCurrentInterface((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         case 12:
/*  167 */           module.setGsmSignalLevel(Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */         
/*      */         case 20:
/*  171 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[0]);
/*  172 */           tmp = tmpShort >> 7 & 0x1;
/*  173 */           if (tmp == 1) {
/*  174 */             tmpShort = (short)(tmpShort ^ 0x80);
/*      */           }
/*  176 */           module.setSatelliteCount(tmpShort);
/*      */ 
/*      */         
/*      */         case 13:
/*  180 */           module.setBatteryVoltage(Float.parseFloat(fcon[0] + "." + fcon[1]));
/*  181 */           tmp2[1] = fcon[2];
/*  182 */           tmp2[0] = fcon[3];
/*  183 */           module.setBatteryStatus(Functions.getIntFrom2ByteArray(tmp2));
/*      */ 
/*      */         
/*      */         case 14:
/*  187 */           module.setGprsDataVolume(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*      */ 
/*      */         
/*      */         case 18:
/*  191 */           tmp2 = Functions.getHighLowBytes(fcon[0]);
/*  192 */           module.setDo_1(tmp2[0]);
/*  193 */           module.setDo_2(tmp2[1]);
/*  194 */           tmp2 = Functions.getHighLowBytes(fcon[1]);
/*  195 */           module.setDo_3(tmp2[0]);
/*  196 */           module.setDo_4(tmp2[1]);
/*  197 */           tmp2 = Functions.getHighLowBytes(fcon[2]);
/*  198 */           module.setDo_5(tmp2[0]);
/*      */ 
/*      */         
/*      */         case 15:
/*  202 */           bin = Functions.getBinaryFromByte(fcon[0]);
/*  203 */           sb = new StringBuilder();
/*  204 */           sb.append(bin[1]).append(bin[2]).append(bin[3]);
/*  205 */           tmpShort = (short)Integer.parseInt(sb.toString(), 2);
/*  206 */           sb = new StringBuilder();
/*  207 */           sb.append(bin[4]).append(bin[5]);
/*  208 */           tmp = (short)Integer.parseInt(sb.toString(), 2);
/*  209 */           sb = new StringBuilder();
/*  210 */           sb.append(bin[6]).append(bin[7]);
/*  211 */           apn = (short)Integer.parseInt(sb.toString(), 2);
/*  212 */           if (bin[0] == '0') {
/*  213 */             module.setSimCard1Status(tmpShort);
/*  214 */             module.setSimCard1OperativeStatus((short)tmp);
/*  215 */             module.setSimCard1JDRStatus(apn); continue;
/*  216 */           }  if (bin[0] == '1') {
/*  217 */             module.setSimCard2Status(tmpShort);
/*  218 */             module.setSimCard2OperativeStatus((short)tmp);
/*  219 */             module.setSimCard2JDRStatus(apn);
/*      */           } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         case 101:
/*  227 */           tmp4[0] = fcon[0];
/*  228 */           tmp4[1] = fcon[1];
/*  229 */           tmp4[2] = fcon[2];
/*  230 */           tmp4[3] = fcon[3];
/*  231 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  232 */           avlOccurrence = new AVLOccurrence();
/*  233 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  234 */           tmp2 = Functions.getHighLowBytes((short)Functions.getIntFromHexByte(fcon[4]));
/*  235 */           if (tmp2[1] == 0) {
/*  236 */             module.setAn_1_value((short)tmp2[0]);
/*  237 */           } else if (tmp2[1] == 1) {
/*  238 */             module.setAn_2_value((short)tmp2[0]);
/*      */           } 
/*  240 */           avlOccurrence.setOccQualifier((short)1);
/*  241 */           switch (tmp2[0]) {
/*      */             case 0:
/*  243 */               if (tmp2[1] == 0) {
/*  244 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_1_ALARM.getOccurrence()); break;
/*  245 */               }  if (tmp2[1] == 1) {
/*  246 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_1_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 1:
/*  250 */               if (tmp2[1] == 0) {
/*  251 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_2_ALARM.getOccurrence()); break;
/*  252 */               }  if (tmp2[1] == 1) {
/*  253 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_2_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 2:
/*  257 */               if (tmp2[1] == 0) {
/*  258 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_3_ALARM.getOccurrence()); break;
/*  259 */               }  if (tmp2[1] == 1) {
/*  260 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_3_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 3:
/*  264 */               if (tmp2[1] == 0) {
/*  265 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_4_ALARM.getOccurrence()); break;
/*  266 */               }  if (tmp2[1] == 1) {
/*  267 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_4_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 4:
/*  271 */               if (tmp2[1] == 0) {
/*  272 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_5_ALARM.getOccurrence()); break;
/*  273 */               }  if (tmp2[1] == 1) {
/*  274 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_5_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 5:
/*  278 */               if (tmp2[1] == 0) {
/*  279 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_6_ALARM.getOccurrence()); break;
/*  280 */               }  if (tmp2[1] == 1) {
/*  281 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_6_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 6:
/*  285 */               if (tmp2[1] == 0) {
/*  286 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_7_ALARM.getOccurrence()); break;
/*  287 */               }  if (tmp2[1] == 1) {
/*  288 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_7_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 7:
/*  292 */               if (tmp2[1] == 0) {
/*  293 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_8_ALARM.getOccurrence()); break;
/*  294 */               }  if (tmp2[1] == 1) {
/*  295 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_8_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 8:
/*  299 */               if (tmp2[1] == 0) {
/*  300 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_9_ALARM.getOccurrence()); break;
/*  301 */               }  if (tmp2[1] == 1) {
/*  302 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_9_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */             case 9:
/*  306 */               if (tmp2[1] == 0) {
/*  307 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_1_TRIGGER_10_ALARM.getOccurrence()); break;
/*  308 */               }  if (tmp2[1] == 1) {
/*  309 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ANALOG_2_TRIGGER_10_ALARM.getOccurrence());
/*      */               }
/*      */               break;
/*      */           } 
/*  313 */           if (flen == 17) {
/*  314 */             byte[] gpsData = new byte[12];
/*  315 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  316 */             Functions.parseGpsData(gpsData, module);
/*  317 */             avlOccurrence.setAltitude(module.getAltitude());
/*  318 */             avlOccurrence.setLattitude(module.getLattitude());
/*  319 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  320 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  322 */           if (avlOccurrenceList == null) {
/*  323 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  325 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 22:
/*  329 */           tmp2[0] = fcon[0];
/*  330 */           tmp2[1] = fcon[1];
/*  331 */           tmpShort = (short)Functions.getIntFrom2ByteArray(tmp2);
/*  332 */           module.setDi_1(tmpShort & 0x3);
/*  333 */           module.setDi_2(tmpShort >> 2 & 0x3);
/*  334 */           module.setDi_3(tmpShort >> 4 & 0x3);
/*  335 */           module.setDi_4(tmpShort >> 6 & 0x3);
/*  336 */           module.setDi_5(tmpShort >> 8 & 0x3);
/*  337 */           module.setDi_6(tmpShort >> 10 & 0x3);
/*  338 */           module.setDi_7(tmpShort >> 12 & 0x3);
/*  339 */           module.setDi_8(tmpShort >> 14 & 0x3);
/*      */ 
/*      */         
/*      */         case 102:
/*  343 */           tmp4[0] = fcon[0];
/*  344 */           tmp4[1] = fcon[1];
/*  345 */           tmp4[2] = fcon[2];
/*  346 */           tmp4[3] = fcon[3];
/*  347 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  348 */           avlOccurrence = new AVLOccurrence();
/*  349 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  350 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*  351 */           apn = (short)(tmpShort >> 7 & 0x1);
/*  352 */           tmpShort = (short)(tmpShort & 0xFFFFFF7F);
/*  353 */           switch (tmpShort) {
/*      */             case 0:
/*  355 */               module.setDi_1(apn);
/*  356 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_1_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 1:
/*  359 */               module.setDi_2(apn);
/*  360 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_2_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 2:
/*  363 */               module.setDi_3(apn);
/*  364 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_3_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 3:
/*  367 */               module.setDi_4(apn);
/*  368 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_4_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 4:
/*  371 */               module.setDi_5(apn);
/*  372 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_5_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 5:
/*  375 */               module.setDi_6(apn);
/*  376 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_6_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 6:
/*  379 */               module.setDi_7(apn);
/*  380 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_7_ACTIVE.getOccurrence());
/*      */               break;
/*      */             case 7:
/*  383 */               module.setDi_8(apn);
/*  384 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DIGITAL_INPUT_8_ACTIVE.getOccurrence());
/*      */               break;
/*      */           } 
/*  387 */           if (apn == 0) {
/*  388 */             avlOccurrence.setOccQualifier((short)1);
/*  389 */           } else if (apn == 1) {
/*  390 */             avlOccurrence.setOccQualifier((short)3);
/*      */           } 
/*  392 */           if (flen == 17) {
/*  393 */             byte[] gpsData = new byte[12];
/*  394 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  395 */             Functions.parseGpsData(gpsData, module);
/*  396 */             avlOccurrence.setAltitude(module.getAltitude());
/*  397 */             avlOccurrence.setLattitude(module.getLattitude());
/*  398 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  399 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  401 */           if (avlOccurrenceList == null) {
/*  402 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  404 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 103:
/*  408 */           tmp4[0] = fcon[0];
/*  409 */           tmp4[1] = fcon[1];
/*  410 */           tmp4[2] = fcon[2];
/*  411 */           tmp4[3] = fcon[3];
/*  412 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  413 */           avlOccurrence = new AVLOccurrence();
/*  414 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  415 */           tmp2 = Functions.getHighLowBytes(fcon[4]);
/*  416 */           avlOccurrence.setOccQualifier((short)1);
/*  417 */           switch (tmp2[0]) {
/*      */             case 0:
/*  419 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_TRACKING_BY_DISTANCE.getOccurrence());
/*      */               break;
/*      */             case 1:
/*  422 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_TRACK_BY_TIME_INTERVAL_IG_ON.getOccurrence());
/*      */               break;
/*      */             case 2:
/*  425 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_TRACK_BY_TIME_INTERVAL_IG_OFF.getOccurrence());
/*      */               break;
/*      */             case 3:
/*  428 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_TRACKING_BY_DEMAND.getOccurrence());
/*      */               break;
/*      */             case 4:
/*  431 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_MAX_SMS_SENT_IG_ON.getOccurrence());
/*      */               break;
/*      */             case 5:
/*  434 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_MAX_SMS_SENT_IG_OFF.getOccurrence());
/*      */               break;
/*      */             case 6:
/*  437 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_TRACKING_LOG_OVERFLOW.getOccurrence());
/*      */               break;
/*      */             case 7:
/*  440 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.MAX_GPRS_RPTS_PER_CYCLE_IG_ON.getOccurrence());
/*      */               break;
/*      */             case 8:
/*  443 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.MAX_GPRS_RPTS_PER_CYCLE_IG_OFF.getOccurrence());
/*      */               break;
/*      */             case 9:
/*  446 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.MAX_SMS_RPTS_PER_CYCLE_IG_ON.getOccurrence());
/*      */               break;
/*      */             case 10:
/*  449 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.MAX_SMS_RPTS_PER_CYCLE_IG_OFF.getOccurrence());
/*      */               break;
/*      */           } 
/*  452 */           if (flen == 17) {
/*  453 */             byte[] gpsData = new byte[12];
/*  454 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  455 */             Functions.parseGpsData(gpsData, module);
/*  456 */             avlOccurrence.setAltitude(module.getAltitude());
/*  457 */             avlOccurrence.setLattitude(module.getLattitude());
/*  458 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  459 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  461 */           if (avlOccurrenceList == null) {
/*  462 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  464 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 104:
/*  468 */           tmp4[0] = fcon[0];
/*  469 */           tmp4[1] = fcon[1];
/*  470 */           tmp4[2] = fcon[2];
/*  471 */           tmp4[3] = fcon[3];
/*  472 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  473 */           avlOccurrence = new AVLOccurrence();
/*  474 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  475 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*  476 */           avlOccurrence.setOccQualifier((short)1);
/*  477 */           switch (tmpShort) {
/*      */             case 0:
/*  479 */               avlOccurrence.setOccQualifier((short)1);
/*  480 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_SPEED_LIMIT_VIOLATION.getOccurrence());
/*      */               break;
/*      */             case 1:
/*  483 */               avlOccurrence.setOccQualifier((short)3);
/*  484 */               avlOccurrence.setResotreNow(false);
/*  485 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_SPEED_LIMIT_VIOLATION.getOccurrence());
/*      */               break;
/*      */             case 2:
/*  488 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_TOWING_ALERT.getOccurrence());
/*      */               break;
/*      */             case 3:
/*  491 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_HEADING_CHANGE_ALERT.getOccurrence());
/*      */               break;
/*      */             case 4:
/*  494 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_IDLE_ALERT.getOccurrence());
/*      */               break;
/*      */             case 5:
/*  497 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_FUEL_THEFT_ALERT.getOccurrence());
/*      */               break;
/*      */             case 6:
/*  500 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_PARKING_ALERT.getOccurrence());
/*      */               break;
/*      */             case 7:
/*  503 */               avlOccurrence.setOccQualifier((short)3);
/*  504 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_IGNITION_ON.getOccurrence());
/*      */               break;
/*      */             case 8:
/*  507 */               avlOccurrence.setOccQualifier((short)1);
/*  508 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_IGNITION_ON.getOccurrence());
/*      */               break;
/*      */             case 9:
/*  511 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_HIGH_DECELERATION_ALERT.getOccurrence());
/*      */               break;
/*      */             case 10:
/*  514 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_HIGH_ACCELERATION_ALERT.getOccurrence());
/*      */               break;
/*      */             case 11:
/*  517 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_SHOCK_DETECTION.getOccurrence());
/*      */               break;
/*      */             case 12:
/*  520 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_MAINTENANCE_ALERT_ON_DISTANCE.getOccurrence());
/*      */               break;
/*      */             case 13:
/*  523 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_MAINTENANCE_ALERT_ON_RUNNING.getOccurrence());
/*      */               break;
/*      */             case 14:
/*  526 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_FATIGUE_DRIVING_ALARM.getOccurrence());
/*      */               break;
/*      */             case 15:
/*  529 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_OVER_REST_AFTER_FDA.getOccurrence());
/*      */               break;
/*      */             case 16:
/*  532 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DRIVING_STATUS_ALERT.getOccurrence());
/*      */               break;
/*      */             case 17:
/*  535 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_COLLISION_DETECTION_ALERT.getOccurrence());
/*      */               break;
/*      */             case 18:
/*  538 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.LOW_FUEL_ALERT.getOccurrence()); break;
/*      */           } 
/*  540 */           if (flen == 17) {
/*  541 */             byte[] gpsData = new byte[12];
/*  542 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  543 */             Functions.parseGpsData(gpsData, module);
/*  544 */             avlOccurrence.setAltitude(module.getAltitude());
/*  545 */             avlOccurrence.setLattitude(module.getLattitude());
/*  546 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  547 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  549 */           if (avlOccurrenceList == null) {
/*  550 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  552 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 105:
/*  556 */           tmp4[0] = fcon[0];
/*  557 */           tmp4[1] = fcon[1];
/*  558 */           tmp4[2] = fcon[2];
/*  559 */           tmp4[3] = fcon[3];
/*  560 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  561 */           avlOccurrence = new AVLOccurrence();
/*  562 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  563 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*  564 */           switch (tmpShort) {
/*      */             case 0:
/*  566 */               avlOccurrence.setOccQualifier((short)1);
/*  567 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.ROUTE_1_IN_OUT.getOccurrence());
/*      */               break;
/*      */             case 1:
/*  570 */               avlOccurrence.setOccQualifier((short)3);
/*  571 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.ROUTE_1_IN_OUT.getOccurrence());
/*      */               break;
/*      */             case 2:
/*  574 */               avlOccurrence.setOccQualifier((short)1);
/*  575 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_MONITOR_STARTED.getOccurrence());
/*      */               break;
/*      */             case 3:
/*  578 */               avlOccurrence.setOccQualifier((short)3);
/*  579 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_MONITOR_STARTED.getOccurrence());
/*  580 */               ao = new AVLOccurrence();
/*  581 */               ao.setOccQualifier((short)1);
/*  582 */               ao.setResotreNow(true);
/*  583 */               ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.ROUTE_PATH_1_MONITOR_ABORTED.getOccurrence());
/*      */               break;
/*      */             case 4:
/*  586 */               avlOccurrence.setOccQualifier((short)3);
/*  587 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_MONITOR_STARTED.getOccurrence());
/*      */               break;
/*      */             case 5:
/*  590 */               avlOccurrence.setOccQualifier((short)1);
/*  591 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_EARLY_REACH.getOccurrence());
/*  592 */               avlOccurrence.setResotreNow(true);
/*      */               break;
/*      */             case 6:
/*  595 */               avlOccurrence.setOccQualifier((short)1);
/*  596 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_TIMELY_REACH.getOccurrence());
/*  597 */               avlOccurrence.setResotreNow(true);
/*      */               break;
/*      */             case 7:
/*  600 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_DELAYED_REACH.getOccurrence());
/*  601 */               avlOccurrence.setOccQualifier((short)1);
/*  602 */               avlOccurrence.setResotreNow(true);
/*      */               break;
/*      */             case 8:
/*  605 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_VIOLATION.getOccurrence());
/*  606 */               avlOccurrence.setOccQualifier((short)1);
/*  607 */               avlOccurrence.setResotreNow(true);
/*  608 */               ao = new AVLOccurrence();
/*  609 */               ao.setOccQualifier((short)3);
/*  610 */               ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ROUTE_1_MONITOR_STARTED.getOccurrence());
/*      */               break;
/*      */             case 9:
/*  613 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.ROUTE_PATH_1_CP_NOT_VERIFIED.getOccurrence());
/*  614 */               avlOccurrence.setOccQualifier((short)1);
/*  615 */               avlOccurrence.setResotreNow(true);
/*      */               break;
/*      */           } 
/*  618 */           tmp = Functions.getIntFromHexByte(fcon[5]);
/*  619 */           tmp2[0] = fcon[7];
/*  620 */           tmp2[1] = fcon[6];
/*  621 */           tmpShort = (short)Functions.getIntFrom2ByteArray(tmp2);
/*  622 */           avlOccurrence.setOccurrenceType(avlOccurrence.getOccurrenceType() + tmp - 1);
/*  623 */           avlOccurrence.setOccData(tmpShort);
/*  624 */           if (flen == 20) {
/*  625 */             byte[] gpsData = new byte[12];
/*  626 */             System.arraycopy(fcon, 8, gpsData, 0, 12);
/*  627 */             Functions.parseGpsData(gpsData, module);
/*  628 */             avlOccurrence.setAltitude(module.getAltitude());
/*  629 */             avlOccurrence.setLattitude(module.getLattitude());
/*  630 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  631 */             avlOccurrence.setSpeed(module.getSpeed());
/*  632 */             if (ao != null) {
/*  633 */               ao.setOccurrenceType(ao.getOccurrenceType() + tmp - 1);
/*  634 */               ao.setAltitude(module.getAltitude());
/*  635 */               ao.setLattitude(module.getLattitude());
/*  636 */               ao.setLongitude(module.getLongtitude());
/*  637 */               ao.setSpeed(module.getSpeed());
/*      */             } 
/*      */           } 
/*  640 */           if (avlOccurrenceList == null) {
/*  641 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  643 */           avlOccurrenceList.add(avlOccurrence);
/*  644 */           if (ao != null) {
/*  645 */             avlOccurrenceList.add(ao);
/*      */           }
/*      */ 
/*      */         
/*      */         case 106:
/*  650 */           tmp4[0] = fcon[0];
/*  651 */           tmp4[1] = fcon[1];
/*  652 */           tmp4[2] = fcon[2];
/*  653 */           tmp4[3] = fcon[3];
/*  654 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  655 */           avlOccurrence = new AVLOccurrence();
/*  656 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  657 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*  658 */           tmp = tmpShort >> 7 & 0x1;
/*  659 */           if (tmp == 1) {
/*  660 */             tmpShort = (short)(tmpShort ^ 0x80);
/*      */           }
/*  662 */           ao = new AVLOccurrence();
/*  663 */           ao.setOccurred(avlOccurrence.getOccurred());
/*  664 */           module.setCurrentGeofence((short)(tmpShort + 1));
/*      */           
/*  666 */           switch (tmpShort) {
/*      */             case 0:
/*  668 */               if (tmp == 0) {
/*  669 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_1_OUT_ALARM.getOccurrence());
/*  670 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_1_IN_ALARM.getOccurrence()); break;
/*  671 */               }  if (tmp == 1) {
/*  672 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_1_OUT_ALARM.getOccurrence());
/*  673 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_1_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 1:
/*  677 */               if (tmp == 0) {
/*  678 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_2_IN_ALARM.getOccurrence());
/*  679 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_2_OUT_ALARM.getOccurrence()); break;
/*  680 */               }  if (tmp == 1) {
/*  681 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_2_OUT_ALARM.getOccurrence());
/*  682 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_2_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 2:
/*  686 */               if (tmp == 0) {
/*  687 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_3_IN_ALARM.getOccurrence());
/*  688 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_3_OUT_ALARM.getOccurrence()); break;
/*  689 */               }  if (tmp == 1) {
/*  690 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_3_OUT_ALARM.getOccurrence());
/*  691 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_3_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 3:
/*  695 */               if (tmp == 0) {
/*  696 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_4_IN_ALARM.getOccurrence());
/*  697 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_4_OUT_ALARM.getOccurrence()); break;
/*  698 */               }  if (tmp == 1) {
/*  699 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_4_OUT_ALARM.getOccurrence());
/*  700 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_4_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 4:
/*  704 */               if (tmp == 0) {
/*  705 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_5_IN_ALARM.getOccurrence());
/*  706 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_5_OUT_ALARM.getOccurrence()); break;
/*  707 */               }  if (tmp == 1) {
/*  708 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_5_OUT_ALARM.getOccurrence());
/*  709 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_5_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 5:
/*  713 */               if (tmp == 0) {
/*  714 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_6_IN_ALARM.getOccurrence());
/*  715 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_6_OUT_ALARM.getOccurrence()); break;
/*  716 */               }  if (tmp == 1) {
/*  717 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_6_OUT_ALARM.getOccurrence());
/*  718 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_6_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 6:
/*  722 */               if (tmp == 0) {
/*  723 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_7_IN_ALARM.getOccurrence());
/*  724 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_7_OUT_ALARM.getOccurrence()); break;
/*  725 */               }  if (tmp == 1) {
/*  726 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_7_OUT_ALARM.getOccurrence());
/*  727 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_7_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 7:
/*  731 */               if (tmp == 0) {
/*  732 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_8_IN_ALARM.getOccurrence());
/*  733 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_8_OUT_ALARM.getOccurrence()); break;
/*  734 */               }  if (tmp == 1) {
/*  735 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_8_OUT_ALARM.getOccurrence());
/*  736 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_8_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 8:
/*  740 */               if (tmp == 0) {
/*  741 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_9_IN_ALARM.getOccurrence());
/*  742 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_9_OUT_ALARM.getOccurrence()); break;
/*  743 */               }  if (tmp == 1) {
/*  744 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_9_OUT_ALARM.getOccurrence());
/*  745 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_9_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */             case 9:
/*  749 */               if (tmp == 0) {
/*  750 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_10_IN_ALARM.getOccurrence());
/*  751 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_10_OUT_ALARM.getOccurrence()); break;
/*  752 */               }  if (tmp == 1) {
/*  753 */                 ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_10_OUT_ALARM.getOccurrence());
/*  754 */                 avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GEOFENCE_10_IN_ALARM.getOccurrence());
/*      */               } 
/*      */               break;
/*      */           } 
/*  758 */           avlOccurrence.setOccQualifier((short)1);
/*  759 */           ao.setOccQualifier((short)3);
/*  760 */           if (tmp == 0) {
/*  761 */             module.setGeofenceViolation((short)0);
/*  762 */           } else if (tmp == 1) {
/*  763 */             module.setGeofenceViolation((short)1);
/*      */           } 
/*  765 */           if (flen == 17) {
/*  766 */             byte[] gpsData = new byte[12];
/*  767 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  768 */             Functions.parseGpsData(gpsData, module);
/*  769 */             avlOccurrence.setAltitude(module.getAltitude());
/*  770 */             avlOccurrence.setLattitude(module.getLattitude());
/*  771 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  772 */             avlOccurrence.setSpeed(module.getSpeed());
/*  773 */             ao.setAltitude(module.getAltitude());
/*  774 */             ao.setLattitude(module.getLattitude());
/*  775 */             ao.setLongitude(module.getLongtitude());
/*  776 */             ao.setSpeed(module.getSpeed());
/*      */           } 
/*  778 */           if (avlOccurrenceList == null) {
/*  779 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  781 */           avlOccurrenceList.add(avlOccurrence);
/*  782 */           avlOccurrenceList.add(ao);
/*      */ 
/*      */         
/*      */         case 107:
/*  786 */           tmp4[0] = fcon[0];
/*  787 */           tmp4[1] = fcon[1];
/*  788 */           tmp4[2] = fcon[2];
/*  789 */           tmp4[3] = fcon[3];
/*  790 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  791 */           avlOccurrence = new AVLOccurrence();
/*  792 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  793 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*  794 */           module.setHwFailure(tmpShort);
/*  795 */           tmp2 = Functions.getHighLowBytes(fcon[4]);
/*  796 */           tmp = tmp2[0] & 0xFF;
/*  797 */           switch (tmp) {
/*      */             case 0:
/*  799 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GPS_MODEM_FAILURE.getOccurrence());
/*      */               break;
/*      */             case 1:
/*  802 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GSM_MODEM_FAILURE.getOccurrence());
/*      */               break;
/*      */             case 2:
/*  805 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_FUEL_GUAGE_FAILURE.getOccurrence());
/*      */               break;
/*      */             case 3:
/*  808 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_ACCELEROMETER_FAILURE.getOccurrence());
/*      */               break;
/*      */             case 4:
/*  811 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_SIM_1_FAILURE.getOccurrence());
/*      */               break;
/*      */             case 5:
/*  814 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_SIM_2_FAILURE.getOccurrence());
/*      */               break;
/*      */             case 6:
/*  817 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_SD_CARD_FAILURE.getOccurrence());
/*      */               break;
/*      */           } 
/*      */           
/*  821 */           switch (tmp2[1] & 0xFF) {
/*      */             case 0:
/*  823 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 1:
/*  826 */               avlOccurrence.setOccQualifier((short)3);
/*      */               break;
/*      */             
/*      */             case 2:
/*  830 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.GPS_ANTENNA_CUT.getOccurrence());
/*  831 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             
/*      */             case 3:
/*  835 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.GPS_ANTENNA_SHORT.getOccurrence());
/*  836 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 4:
/*  839 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.GPS_ANTENNA_SHORT.getOccurrence());
/*  840 */               avlOccurrence.setOccQualifier((short)3);
/*      */               break;
/*      */           } 
/*      */           
/*  844 */           if (flen == 17) {
/*  845 */             byte[] gpsData = new byte[12];
/*  846 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  847 */             Functions.parseGpsData(gpsData, module);
/*  848 */             avlOccurrence.setAltitude(module.getAltitude());
/*  849 */             avlOccurrence.setLattitude(module.getLattitude());
/*  850 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  851 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  853 */           if (hwOccurrenceList == null) {
/*  854 */             hwOccurrenceList = new ArrayList<>();
/*      */           }
/*  856 */           hwOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 108:
/*  860 */           tmp4[0] = fcon[0];
/*  861 */           tmp4[1] = fcon[1];
/*  862 */           tmp4[2] = fcon[2];
/*  863 */           tmp4[3] = fcon[3];
/*  864 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  865 */           avlOccurrence = new AVLOccurrence();
/*  866 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  867 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*      */           
/*  869 */           switch (tmpShort) {
/*      */             case 0:
/*  871 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_BATTERY.getOccurrence());
/*  872 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 1:
/*  875 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_BATTERY.getOccurrence());
/*  876 */               avlOccurrence.setOccQualifier((short)3);
/*      */               break;
/*      */             case 2:
/*  879 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_POWER_FAILURE.getOccurrence());
/*  880 */               avlOccurrence.setOccQualifier((short)1);
/*  881 */               module.setMainPwrStatus((short)0);
/*      */               break;
/*      */             case 3:
/*  884 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_POWER_SUPPLY_INPUT_VOLTAGE.getOccurrence());
/*  885 */               avlOccurrence.setOccQualifier((short)1);
/*  886 */               module.setMainPwrStatus((short)2);
/*      */               break;
/*      */             case 4:
/*  889 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_POWER_FAILURE.getOccurrence());
/*  890 */               avlOccurrence.setOccQualifier((short)3);
/*  891 */               module.setMainPwrStatus((short)1);
/*      */               break;
/*      */           } 
/*  894 */           if (flen == 17) {
/*  895 */             byte[] gpsData = new byte[12];
/*  896 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  897 */             Functions.parseGpsData(gpsData, module);
/*  898 */             avlOccurrence.setAltitude(module.getAltitude());
/*  899 */             avlOccurrence.setLattitude(module.getLattitude());
/*  900 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  901 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  903 */           if (avlOccurrenceList == null) {
/*  904 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  906 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  915 */           if (avlOccurrence.getOccurrenceType() == MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_POWER_SUPPLY_INPUT_VOLTAGE.getOccurrence() && avlOccurrence.getOccQualifier() == 1) {
/*  916 */             ao = new AVLOccurrence();
/*  917 */             ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_POWER_FAILURE.getOccurrence());
/*  918 */             ao.setOccQualifier((short)3);
/*  919 */             ao.setAltitude(avlOccurrence.getAltitude());
/*  920 */             ao.setLattitude(avlOccurrence.getLattitude());
/*  921 */             ao.setLongitude(avlOccurrence.getLongitude());
/*  922 */             ao.setSpeed(avlOccurrence.getSpeed());
/*  923 */             avlOccurrenceList.add(ao); continue;
/*  924 */           }  if (avlOccurrence.getOccurrenceType() == MercuriusAVLEnums.EnumAVLOccurrences.E_POWER_FAILURE.getOccurrence() && avlOccurrence.getOccQualifier() == 1) {
/*  925 */             ao = new AVLOccurrence();
/*  926 */             ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_POWER_SUPPLY_INPUT_VOLTAGE.getOccurrence());
/*  927 */             ao.setOccQualifier((short)3);
/*  928 */             ao.setAltitude(avlOccurrence.getAltitude());
/*  929 */             ao.setLattitude(avlOccurrence.getLattitude());
/*  930 */             ao.setLongitude(avlOccurrence.getLongitude());
/*  931 */             ao.setSpeed(avlOccurrence.getSpeed());
/*  932 */             avlOccurrenceList.add(ao);
/*      */           } 
/*      */ 
/*      */         
/*      */         case 109:
/*  937 */           tmp4[0] = fcon[0];
/*  938 */           tmp4[1] = fcon[1];
/*  939 */           tmp4[2] = fcon[2];
/*  940 */           tmp4[3] = fcon[3];
/*  941 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  942 */           avlOccurrence = new AVLOccurrence();
/*  943 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  944 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*  945 */           switch (tmpShort) {
/*      */             case 0:
/*  947 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GPS_POSITION_ERROR_VIOLATION.getOccurrence());
/*  948 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 1:
/*  951 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_GPS_POSITION_ERROR_VIOLATION.getOccurrence());
/*  952 */               avlOccurrence.setOccQualifier((short)3);
/*      */               break;
/*      */             case 2:
/*  955 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_NO_GPS_SIGNAL.getOccurrence());
/*  956 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 3:
/*  959 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_NO_GPS_SIGNAL.getOccurrence());
/*  960 */               avlOccurrence.setOccQualifier((short)3);
/*      */               break;
/*      */           } 
/*  963 */           if (flen == 17) {
/*  964 */             byte[] gpsData = new byte[12];
/*  965 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  966 */             Functions.parseGpsData(gpsData, module);
/*  967 */             avlOccurrence.setAltitude(module.getAltitude());
/*  968 */             avlOccurrence.setLattitude(module.getLattitude());
/*  969 */             avlOccurrence.setLongitude(module.getLongtitude());
/*  970 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/*  972 */           if (avlOccurrenceList == null) {
/*  973 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/*  975 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 110:
/*  979 */           tmp4[0] = fcon[0];
/*  980 */           tmp4[1] = fcon[1];
/*  981 */           tmp4[2] = fcon[2];
/*  982 */           tmp4[3] = fcon[3];
/*  983 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  984 */           avlOccurrence = new AVLOccurrence();
/*  985 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/*  986 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/*      */           
/*  988 */           module.setTamperStatus(tmpShort);
/*  989 */           if (tmpShort == 0) {
/*  990 */             avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DEVICE_TAMPERED.getOccurrence());
/*  991 */             avlOccurrence.setOccQualifier((short)1);
/*  992 */           } else if (tmpShort == 1) {
/*  993 */             avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_DEVICE_TAMPERED.getOccurrence());
/*  994 */             avlOccurrence.setOccQualifier((short)3);
/*      */           } 
/*  996 */           if (flen == 17) {
/*  997 */             byte[] gpsData = new byte[12];
/*  998 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  999 */             Functions.parseGpsData(gpsData, module);
/* 1000 */             avlOccurrence.setAltitude(module.getAltitude());
/* 1001 */             avlOccurrence.setLattitude(module.getLattitude());
/* 1002 */             avlOccurrence.setLongitude(module.getLongtitude());
/* 1003 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/* 1005 */           if (avlOccurrenceList == null) {
/* 1006 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/* 1008 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 112:
/* 1012 */           tmp4[0] = fcon[0];
/* 1013 */           tmp4[1] = fcon[1];
/* 1014 */           tmp4[2] = fcon[2];
/* 1015 */           tmp4[3] = fcon[3];
/* 1016 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1017 */           avlOccurrence = new AVLOccurrence();
/* 1018 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/* 1019 */           tmpShort = (short)Functions.getIntFromHexByte(fcon[4]);
/* 1020 */           switch (tmpShort) {
/*      */             case 0:
/* 1022 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_GSM_SIGNAL.getOccurrence());
/* 1023 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 1:
/* 1026 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.GSM_SIGNAL_JAMMED.getOccurrence());
/* 1027 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 2:
/* 1030 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_GSM_SIGNAL.getOccurrence());
/* 1031 */               avlOccurrence.setOccQualifier((short)3);
/*      */               break;
/*      */           } 
/* 1034 */           if (flen == 19) {
/* 1035 */             byte[] gpsData = new byte[12];
/* 1036 */             System.arraycopy(fcon, 7, gpsData, 0, 12);
/* 1037 */             Functions.parseGpsData(gpsData, module);
/* 1038 */             avlOccurrence.setAltitude(module.getAltitude());
/* 1039 */             avlOccurrence.setLattitude(module.getLattitude());
/* 1040 */             avlOccurrence.setLongitude(module.getLongtitude());
/* 1041 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/* 1043 */           if (avlOccurrenceList == null) {
/* 1044 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/* 1046 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/* 1054 */           if (avlOccurrence.getOccurrenceType() == MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_GSM_SIGNAL.getOccurrence() && avlOccurrence.getOccQualifier() == 1) {
/* 1055 */             ao = new AVLOccurrence();
/* 1056 */             ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.GSM_SIGNAL_JAMMED.getOccurrence());
/* 1057 */             ao.setOccQualifier((short)3);
/* 1058 */             ao.setAltitude(avlOccurrence.getAltitude());
/* 1059 */             ao.setLattitude(avlOccurrence.getLattitude());
/* 1060 */             ao.setLongitude(avlOccurrence.getLongitude());
/* 1061 */             ao.setSpeed(avlOccurrence.getSpeed());
/*      */             
/* 1063 */             avlOccurrenceList.add(ao); continue;
/* 1064 */           }  if (avlOccurrence.getOccurrenceType() == MercuriusAVLEnums.EnumAVLOccurrences.GSM_SIGNAL_JAMMED.getOccurrence() && avlOccurrence.getOccQualifier() == 1) {
/* 1065 */             ao = new AVLOccurrence();
/* 1066 */             ao.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_LOW_GSM_SIGNAL.getOccurrence());
/* 1067 */             ao.setOccQualifier((short)3);
/* 1068 */             ao.setAltitude(avlOccurrence.getAltitude());
/* 1069 */             ao.setLattitude(avlOccurrence.getLattitude());
/* 1070 */             ao.setLongitude(avlOccurrence.getLongitude());
/* 1071 */             ao.setSpeed(avlOccurrence.getSpeed());
/*      */             
/* 1073 */             avlOccurrenceList.add(ao);
/*      */           } 
/*      */ 
/*      */         
/*      */         case 113:
/* 1078 */           tmp4[0] = fcon[0];
/* 1079 */           tmp4[1] = fcon[1];
/* 1080 */           tmp4[2] = fcon[2];
/* 1081 */           tmp4[3] = fcon[3];
/* 1082 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1083 */           avlOccurrence = new AVLOccurrence();
/* 1084 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/* 1085 */           switch (Functions.getIntFromHexByte(fcon[4])) {
/*      */             case 0:
/* 1087 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_INCOMING_CALL.getOccurrence());
/* 1088 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 1:
/* 1091 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_REJECT_INCOMING_CALL.getOccurrence());
/* 1092 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 2:
/* 1095 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.CALL_AUTO_ANSWERED.getOccurrence());
/* 1096 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 3:
/* 1099 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.CALL_MANUAL_ANSWERED.getOccurrence());
/* 1100 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 4:
/* 1103 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.CALL_TWO_WAY_COMMUNICATION.getOccurrence());
/* 1104 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 5:
/* 1107 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.CALL_DISCRETE_MODE.getOccurrence());
/* 1108 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 6:
/* 1111 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.CALL_DISC_TERMINATED.getOccurrence());
/* 1112 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */           } 
/* 1115 */           if (flen == 17) {
/* 1116 */             byte[] gpsData = new byte[12];
/* 1117 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 1118 */             Functions.parseGpsData(gpsData, module);
/* 1119 */             avlOccurrence.setAltitude(module.getAltitude());
/* 1120 */             avlOccurrence.setLattitude(module.getLattitude());
/* 1121 */             avlOccurrence.setLongitude(module.getLongtitude());
/* 1122 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/* 1124 */           if (avlOccurrenceList == null) {
/* 1125 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/* 1127 */           avlOccurrenceList.add(avlOccurrence);
/*      */ 
/*      */         
/*      */         case 114:
/* 1131 */           tmp4[0] = fcon[0];
/* 1132 */           tmp4[1] = fcon[1];
/* 1133 */           tmp4[2] = fcon[2];
/* 1134 */           tmp4[3] = fcon[3];
/* 1135 */           dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1136 */           avlOccurrence = new AVLOccurrence();
/* 1137 */           avlOccurrence.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(dValue), timezone));
/* 1138 */           switch (Functions.getIntFromHexByte(fcon[4])) {
/*      */             case 0:
/* 1140 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.E_INCOMING_SMS.getOccurrence());
/* 1141 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 1:
/* 1144 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.SMS_REJECTED.getOccurrence());
/* 1145 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 2:
/* 1148 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.SMS_CONFIGURATION.getOccurrence());
/* 1149 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 3:
/* 1152 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.SMS_COMMAND.getOccurrence());
/* 1153 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */             case 4:
/* 1156 */               avlOccurrence.setOccurrenceType((short)MercuriusAVLEnums.EnumAVLOccurrences.SMS_ECHO.getOccurrence());
/* 1157 */               avlOccurrence.setOccQualifier((short)1);
/*      */               break;
/*      */           } 
/* 1160 */           if (flen == 17) {
/* 1161 */             byte[] gpsData = new byte[12];
/* 1162 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 1163 */             Functions.parseGpsData(gpsData, module);
/* 1164 */             avlOccurrence.setAltitude(module.getAltitude());
/* 1165 */             avlOccurrence.setLattitude(module.getLattitude());
/* 1166 */             avlOccurrence.setLongitude(module.getLongtitude());
/* 1167 */             avlOccurrence.setSpeed(module.getSpeed());
/*      */           } 
/* 1169 */           if (avlOccurrenceList == null) {
/* 1170 */             avlOccurrenceList = new ArrayList<>();
/*      */           }
/* 1172 */           avlOccurrenceList.add(avlOccurrence);
/*      */       } 
/*      */     
/*      */     } 
/* 1176 */     module.setAvlOccurrenceList(avlOccurrenceList);
/* 1177 */     module.setHwOccurrenceList(hwOccurrenceList);
/* 1178 */     return module;
/*      */   }
/*      */ 
/*      */   
/*      */   private void dispose() {
/* 1183 */     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Task_for_processing_of_ALIVE_packets_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\ProcessAlivePackets.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */