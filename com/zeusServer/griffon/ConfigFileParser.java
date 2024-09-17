/*     */ package com.zeusServer.griffon;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.util.CRC32;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ConfigFileParser
/*     */ {
/*     */   private static final int GRIFFON_SIGNATURE_16 = 16;
/*     */   public static final int GRIFFON_ZONE_FILE_ID = 1;
/*     */   public static final int GRIFFON_ZONE_GROUP_FILE_ID = 2;
/*     */   public static final int GRIFFON_COUNTER_FILE_ID = 3;
/*     */   public static final int GRIFFON_TRIGGER_FILE_ID = 4;
/*     */   public static final int GRIFFON_ZONE_REPORT_FILE_ID = 5;
/*     */   public static final int GRIFFON_PGM_FILE_ID = 6;
/*     */   public static final int GRIFFON_PARTITION_FILE_ID = 7;
/*     */   public static final int GRIFFON_PARTITION_GROUP_FILE_ID = 8;
/*     */   public static final int GRIFFON_ACTION_FILE_ID = 9;
/*     */   public static final int GRIFFON_SYSTEM_FILE_ID = 10;
/*     */   public static final int GRIFFON_INSTALLER_FILE_ID = 11;
/*     */   public static final int GRIFFON_USER_FILE_ID = 12;
/*     */   public static final int GRIFFON_USER_GROUP_FILE_ID = 13;
/*     */   public static final int GRIFFON_USER_REPORT_CODES_FILE_ID = 14;
/*     */   public static final int GRIFFON_EXPANSION_BOARD_FILE_ID = 15;
/*     */   public static final int GRIFFON_KEYPAD_MACRO_FILE_ID = 16;
/*     */   public static final int GRIFFON_KEYPAD_FILE_ID = 17;
/*     */   public static final int GRIFFON_EM_ACCESS_FILE_ID = 18;
/*     */   public static final int GRIFFON_ACCESS_TRIGGER_FILE_ID = 19;
/*     */   public static final int GRIFFON_VOICE_FILE_ID = 20;
/*     */   public static final int GRIFFON_VOICE_MSG_LIST_FILE_ID = 21;
/*     */   public static final int GRIFFON_SMS_FILE_ID = 22;
/*     */   public static final int GRIFFON_BLUETOOTH_FILE_ID = 23;
/*     */   public static final int GRIFFON_BLUETOOTH_DEVICE_FILE_ID = 24;
/*     */   public static final int GRIFFON_ZIGBEE_FILE_ID = 25;
/*     */   public static final int GRIFFON_ETH_IP_MASTER_FILE_ID = 26;
/*     */   public static final int GRIFFON_WIFI_IP_MASTER_FILE_ID = 27;
/*     */   public static final int GRIFFON_TIME_FILE_ID = 28;
/*     */   public static final int GRIFFON_HOLIDAY_FILE_ID = 29;
/*     */   public static final int GRIFFON_SCHEDULE_FILE_ID = 30;
/*     */   public static final int GRIFFON_ARM_DISARM_SCHEDULE_FILE_ID = 31;
/*     */   public static final int GRIFFON_PERIPHERAL_FILE_ID = 32;
/*     */   public static final int GRIFFON_MS_FILE_ID = 33;
/*     */   public static final int GRIFFON_COMM_FILE_ID = 34;
/*     */   public static final int GRIFFON_SYSTEM_REPORT_FILE_ID = 35;
/*     */   public static final int GRIFFON_KEYFOB_TEMPLATE_FILE_ID = 36;
/*     */   public static final int GRIFFON_WSN_FILE_ID = 37;
/*     */   public static final int GRIFFON_KEYFOBS_FILE_ID = 38;
/*     */   public static final int GRIFFON_ACCESS_CARDS_FILE_ID = 39;
/*     */   public static final int GRIFFON_ACCESS_GROUPS_FILE_ID = 40;
/*     */   public static final int GRIFFON_ZONE_OBJECT_ID = 1;
/*     */   public static final int GRIFFON_ZONE_GLOBAL_OBJECT_ID = 2;
/*     */   public static final int GRIFFON_ZONE_GROUP_OBJECT_ID = 3;
/*     */   public static final int GRIFFON_COUNTER_OBJECT_ID = 4;
/*     */   public static final int GRIFFON_ACCESS_COUNTER_TRIGGER_OBJECT_ID = 5;
/*     */   public static final int GRIFFON_ZONE_REPORT_CODE_OBJECT_ID = 6;
/*     */   public static final int GRIFFON_PGM_OBJECT_ID = 7;
/*     */   public static final int GRIFFON_PARTITION_OBJECT_ID = 8;
/*     */   public static final int GRIFFON_PARTITION_GLOBAL_OBJECT_ID = 9;
/*     */   public static final int GRIFFON_RESTRICT_ARM_OBJECT_ID = 10;
/*     */   public static final int GRIFFON_PARTITION_GROUP_OBJECT_ID = 11;
/*     */   public static final int GRIFFON_ACTION_OBJECT_ID = 12;
/*     */   public static final int GRIFFON_TROUBLE_OBJECT_ID = 13;
/*     */   public static final int GRIFFON_TAMPER_SUPERVISION_OBJECT_ID = 14;
/*     */   public static final int GRIFFON_SYSTEM_SETTINGS_OBJECT_ID = 15;
/*     */   public static final int GRIFFON_BELL_TROUBLE_OBJECT_ID = 16;
/*     */   public static final int GRIFFON_3I_LOCATE_OBJECT_ID = 17;
/*     */   public static final int GRIFFON_CFG_TOOL_AUTH_OBJECT_ID = 18;
/*     */   public static final int GRIFFON_INSTALLER_OBJECT_ID = 19;
/*     */   public static final int GRIFFON_USER_OBJECT_ID = 20;
/*     */   public static final int GRIFFON_USER_GLOBAL_PASSWORD_OBJECT_ID = 21;
/*     */   public static final int GRIFFON_USER_GROUP_OBJECT_ID = 22;
/*     */   public static final int GRIFFON_USER_REPORT_CODE_OBJECT_ID = 23;
/*     */   public static final int GRIFFON_ALARM_PANEL_OBJECT_ID = 24;
/*     */   public static final int GRIFFON_EXPANSION_BOARD_OBJECT_ID = 25;
/*     */   public static final int GRIFFON_KEYPAD_MACRO_OBJECT_ID = 26;
/*     */   public static final int GRIFFON_GLOBAL_KEYPAD_OBJECT_ID = 27;
/*     */   public static final int GRIFFON_KEYPAD_OBJECT_ID = 28;
/*     */   public static final int GRIFFON_EM_ACCESS_GLOBAL_OBJECT_ID = 29;
/*     */   public static final int GRIFFON_EM_ACCESS_OBJECT_ID = 30;
/*     */   public static final int GRIFFON_ACCESS_TRIGGER_OBJECT_ID = 31;
/*     */   public static final int GRIFFON_VOICE_MSG_OBJECT_ID = 32;
/*     */   public static final int GRIFFON_PHONE_BOOK_OBJECT_ID = 33;
/*     */   public static final int GRIFFON_VOICE_FILE_LIST_OBJECT_ID = 34;
/*     */   public static final int GRIFFON_INCOMING_SMS_OBJECT_ID = 35;
/*     */   public static final int GRIFFON_SMS_TAG_MSG_OBJECT_ID = 36;
/*     */   public static final int GRIFFON_SMS_MSG_OBJECT_ID = 37;
/*     */   public static final int GRIFFON_BLUETOOTH_OBJECT_ID = 38;
/*     */   public static final int GRIFFON_BLUETOOTH_DEVICE_OBJECT_ID = 39;
/*     */   public static final int GRIFFON_ZIGBEE_OBJECT_ID = 40;
/*     */   public static final int GRIFFON_ETHERNET_MASTER_OBJECT_ID = 41;
/*     */   public static final int GRIFFON_WIFI_MASTER_OBJECT_ID = 42;
/*     */   public static final int GRIFFON_TIME_SYNC_OBJECT_ID = 43;
/*     */   public static final int GRIFFON_TEST_REPORT_SETTINGS_OBJECT_ID = 44;
/*     */   public static final int GRIFFON_DAYLIGHT_SAVING_OBJECT_ID = 45;
/*     */   public static final int GRIFFON_TEST_REPORT_SCHEDULE_OBJECT_ID = 46;
/*     */   public static final int GRIFFON_HOLIDAY_OBJECT_ID = 47;
/*     */   public static final int GRIFFON_SCHEDULE_OBJECT_ID = 48;
/*     */   public static final int GRIFFON_AUTO_ARM_SCHEDULE_OBJECT_ID = 49;
/*     */   public static final int GRIFFON_AUTO_DISARM_SCHEDULE_OBJECT_ID = 50;
/*     */   public static final int GRIFFON_BATTERY_CHARGER_OBJECT_ID = 51;
/*     */   public static final int GRIFFON_BELL_OBJECT_ID = 52;
/*     */   public static final int GRIFFON_ACCELEROMETER_OBJECT_ID = 53;
/*     */   public static final int GRIFFON_MS_GENERAL_OBJECT_ID = 54;
/*     */   public static final int GRIFFON_MONITORING_STATION_OBJECT_ID = 55;
/*     */   public static final int GRIFFON_TELEPHONE_OBJECT_ID = 56;
/*     */   public static final int GRIFFON_ETHERNET_OBJECT_ID = 57;
/*     */   public static final int GRIFFON_SIMCARD_OBJECT_ID = 58;
/*     */   public static final int GRIFFON_GSM_MODEM_OBJECT_ID = 59;
/*     */   public static final int GRIFFON_WIFI_OBJECT_ID = 60;
/*     */   public static final int GRIFFON_LOCAL_SERVER_OBJECT_ID = 61;
/*     */   public static final int GRIFFON_SYSTEM_REPORT_CODE_OBJECT_ID = 62;
/*     */   public static final int GRIFFON_KEYFOB_TEMPLATE_OBJECT_ID = 63;
/*     */   public static final int GRIFFON_WIRELESS_SENSOR_OBJECT_ID = 64;
/*     */   public static final int GRIFFON_KEYFOB_OBJECT_ID = 65;
/*     */   public static final int GRIFFON_ACCESS_CARD_OBJECT_ID = 66;
/*     */   public static final int GRIFFON_ACCESS_GROUP_OBJECT_ID = 67;
/*     */   private static final int GRIFFON_EACH_ZONE_SIZE = 62;
/*     */   private static final int GRIFFON_EACH_ZONE_GLOBAL_SIZE = 7;
/*     */   private static final int GRIFFON_EACH_ZONE_GROUP_SIZE = 21;
/*     */   private static final int GRIFFON_EACH_COUNTER_SIZE = 12;
/*     */   private static final int GRIFFON_EACH_ANALOG_COUNTER_TRIGGER_SIZE = 9;
/*     */   private static final int GRIFFON_EACH_ZONE_REPORT_CODE_SIZE = 6;
/*     */   private static final int GRIFFON_EACH_PGM_SIZE = 67;
/*     */   private static final int GRIFFON_EACH_PARTITION_SIZE = 106;
/*     */   private static final int GRIFFON_EACH_PARTITION_GLOBAL_SIZE = 45;
/*     */   private static final int GRIFFON_EACH_RESTRICT_ARMING_SIZE = 24;
/*     */   private static final int GRIFFON_EACH_PARTITION_GROUP_SIZE = 20;
/*     */   private static final int GRIFFON_EACH_ACTION_SIZE = 36;
/*     */   private static final int GRIFFON_EACH_TROUBLE_SIZE = 6;
/*     */   private static final int GRIFFON_EACH_TAMPER_SUPERVISION_SIZE = 9;
/*     */   private static final int GRIFFON_EACH_SYS_SETTING_SIZE = 13;
/*     */   private static final int GRIFFON_EACH_BELL_TROUBLE_CFG_SIZE = 4;
/*     */   private static final int GRIFFON_EACH_3I_LOCATE_CFG_SIZE = 6;
/*     */   private static final int GRIFFON_EACH_CFG_TOOL_AUTH_SIZE = 20;
/*     */   private static final int GRIFFON_EACH_INSTALLER_SIZE = 48;
/*     */   private static final int GRIFFON_EACH_USER_CFG_SIZE = 108;
/*     */   private static final int GRIFFON_EACH_USER_GLOBAL_PWD_SIZE = 6;
/*     */   private static final int GRIFFON_EACH_USER_GROUP_SIZE = 38;
/*     */   private static final int GRIFFON_EACH_USER_REPORT_CODE_SIZE = 2;
/*     */   private static final int GRIFFON_EACH_CONTROL_PANEL_CFG_SIZE = 35;
/*     */   private static final int GRIFFON_EACH_EB_CFG_SIZE = 35;
/*     */   private static final int GRIFFON_EACH_FUNCTION_KEY_MACRO_SIZE = 22;
/*     */   private static final int GRIFFON_EACH_GLOBAL_KEYPAD_SIZE = 25;
/*     */   private static final int GRIFFON_EACH_KEYPAD_CFG_SIZE = 49;
/*     */   private static final int GRIFFON_EACH_ACCESS_BOARD_GLOBAL_CFG_SIZE = 5;
/*     */   private static final int GRIFFON_EACH_ACCESS_CARD_CFG_SIZE = 15;
/*     */   private static final int GRIFFON_EACH_ACCESS_TRIGGER_CFG_SIZE = 28;
/*     */   private static final int GRIFFON_EACH_VOICE_MSG_CFG_SIZE = 34;
/*     */   private static final int GRIFFON_EACH_PHONE_BOOK_CFG_SIZE = 38;
/*     */   private static final int GRIFFON_EACH_VOICE_FILE_LIST_1_SIZE = 20;
/*     */   private static final int GRIFFON_EACH_INCOMING_SMS_CFG_SIZE = 207;
/*     */   private static final int GRIFFON_EACH_SMS_CUSTOM_TAG_CFG_SIZE = 40;
/*     */   private static final int GRIFFON_EACH_SMS_MESSAGE_SIZE = 181;
/*     */   private static final int GRIFFON_EACH_BLUE_TOOTH_TXR_CFG_SIZE = 44;
/*     */   private static final int GRIFFON_EACH_BLUE_TOOTH_DEVICE_CFG_SIZE = 38;
/*     */   private static final int GRIFFON_EACH_ZIGBEE_NW_CFG_SIZE = 32;
/*     */   private static final int GRIFFON_EACH_ETH_IPEB_CFG_SIZE = 257;
/*     */   private static final int GRIFFON_EACH_WIFI_IPEB_SIZE = 345;
/*     */   private static final int GRIFFON_EACH_TIME_SYNC_CFG_SIZE = 8;
/*     */   private static final int GRIFFON_EACH_TEST_REPORT_CFG_SIZE = 4;
/*     */   private static final int GRIFFON_EACH_DAY_LIGHT_SAVING_SIZE = 104;
/*     */   private static final int GRIFFON_EACH_TEST_REPORT_SCHEDULE_CFG_SIZE = 49;
/*     */   private static final int GRIFFON_EACH_HOLIDAY_CFG_SIZE = 25;
/*     */   private static final int GRIFFON_EACH_SCHEDULE_CFG_SIZE = 49;
/*     */   private static final int GRIFFON_EACH_AUTO_ARM_SCHEDULES_SIZE = 49;
/*     */   private static final int GRIFFON_EACH_AUTO_DISARM_SCHEDULES_SIZE = 49;
/*     */   private static final int GRIFFON_EACH_BATTERY_CHARGER_CFG_SIZE = 4;
/*     */   private static final int GRIFFON_EACH_BELL_CFG_SIZE = 24;
/*     */   private static final int GRIFFON_EACH_ACCELEROMETER_CFG_SIZE = 8;
/*     */   private static final int GRIFFON_EACH_COI_CFG_SIZE = 199;
/*     */   private static final int GRIFFON_EACH_MONITORING_STATION_CFG_SIZE = 462;
/*     */   private static final int GRIFFON_EACH_TELEPHONE_CFG_SIZE = 77;
/*     */   private static final int GRIFFON_EACH_ETHERNET_CFG_SIZE = 154;
/*     */   private static final int GRIFFON_EACH_SIMCARD_CFG_SIZE = 419;
/*     */   private static final int GRIFFON_EACH_GSM_MODEM_CFG_SIZE = 77;
/*     */   private static final int GRIFFON_EACH_WIFI_CFG_SIZE = 242;
/*     */   private static final int GRIFFON_EACH_LOCAL_SERVER_CFG_SIZE = 6;
/*     */   private static final int GRIFFON_EACH_REPORT_CODE_SIZE = 4;
/*     */   private static final int GRIFFON_EACH_KEYFOB_TEMPLATE_SIZE = 33;
/*     */   private static final int GRIFFON_EACH_WIRELESS_DEVICE_SIZE = 27;
/*     */   private static final int GRIFFON_EACH_KEYFOB_SIZE = 40;
/*     */   private static final int GRIFFON_EACH_ACCESS_CARD_SIZE = 80;
/*     */   private static final int GRIFFON_EACH_ACCESS_GROUP_SIZE = 22;
/*     */   
/*     */   private static List<Integer> buildEmptyCRCList(int filesCount) {
/* 210 */     List<Integer> deviceCRC32 = new ArrayList<>(filesCount);
/* 211 */     for (int i = 0; i < filesCount; i++) {
/* 212 */       deviceCRC32.add(Integer.valueOf(-1));
/*     */     }
/* 214 */     return deviceCRC32;
/*     */   }
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
/*     */   public static ModuleCFG arrangeCFGFile(short productID, byte[] fileContent, int fileSize) {
/* 228 */     int fPos = 0;
/* 229 */     byte[] indexStructure = new byte[200];
/* 230 */     byte[] tmp4 = new byte[4];
/* 231 */     byte[] tmp2 = new byte[2];
/*     */ 
/*     */ 
/*     */     
/* 235 */     fPos += 16;
/*     */ 
/*     */ 
/*     */     
/* 239 */     while (fPos < fileSize) {
/* 240 */       byte tmpObjectID = fileContent[fPos];
/* 241 */       tmp2[0] = fileContent[fPos + 2];
/* 242 */       tmp2[1] = fileContent[fPos + 1];
/* 243 */       int count = Functions.getIntFrom2ByteArray(tmp2);
/* 244 */       int fileId = getFileIDByObjectID(tmpObjectID & 0xFF);
/* 245 */       if (fileId > 0) {
/* 246 */         tmp4 = Functions.get4ByteArrayFromInt(fPos);
/* 247 */         System.arraycopy(tmp4, 0, indexStructure, fileId * 4 - 4, 4);
/* 248 */         int size = getSizeByFileID(fileId, tmpObjectID & 0xFF, count, fileContent, fPos);
/* 249 */         fPos += size;
/* 250 */         tmp4 = Functions.get4ByteArrayFromInt(fPos);
/* 251 */         System.arraycopy(tmp4, 0, indexStructure, (fileId + 1) * 4 - 4, 4);
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 257 */     List<Integer> crc32List = buildEmptyCRCList((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40);
/*     */ 
/*     */ 
/*     */     
/* 261 */     for (int i = 1; i <= ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); i++) {
/* 262 */       int nextStartPos; System.arraycopy(indexStructure, i * 4 - 4, tmp4, 0, 4);
/* 263 */       int startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 264 */       if (i < ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40)) {
/* 265 */         System.arraycopy(indexStructure, (i + 1) * 4 - 4, tmp4, 0, 4);
/* 266 */         nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/*     */       } else {
/* 268 */         nextStartPos = fileSize;
/*     */       } 
/* 270 */       if (nextStartPos - startPos >= 3) {
/*     */ 
/*     */         
/* 273 */         byte[] idata = new byte[nextStartPos - startPos];
/* 274 */         System.arraycopy(fileContent, startPos, idata, 0, nextStartPos - startPos);
/* 275 */         if (idata.length != 3)
/*     */         
/*     */         { 
/* 278 */           int crc32 = CRC32.getCRC32(idata);
/* 279 */           crc32List.set(i - 1, Integer.valueOf(crc32)); } 
/*     */       } 
/* 281 */     }  ModuleCFG incomingCFG = new ModuleCFG();
/* 282 */     incomingCFG.setFileData(fileContent);
/* 283 */     incomingCFG.setIndexStructure(indexStructure);
/* 284 */     incomingCFG.setCrc32List(crc32List);
/* 285 */     return incomingCFG;
/*     */   }
/*     */ 
/*     */   
/*     */   private static int getSizeByFileID(int fileId, int objectId, int count, byte[] decData, int fPos) {
/* 290 */     int size = 0;
/* 291 */     byte[] tmp2 = new byte[2];
/* 292 */     int[] remainingObjectID = null;
/*     */ 
/*     */ 
/*     */     
/* 296 */     boolean flag1 = false;
/* 297 */     boolean flag2 = false;
/*     */     
/* 299 */     switch (fileId) {
/*     */       case 1:
/* 301 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 2:
/* 304 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 305 */         flag1 = true;
/* 306 */         flag2 = true;
/*     */         break;
/*     */       case 3:
/* 309 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 4:
/* 312 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 5:
/* 315 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 6:
/* 318 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 7:
/* 321 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 8:
/* 324 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 325 */         flag1 = true;
/* 326 */         flag2 = true;
/*     */         break;
/*     */       case 9:
/* 329 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 10:
/* 332 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 333 */         flag1 = true;
/* 334 */         flag2 = true;
/*     */         break;
/*     */       case 11:
/* 337 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 338 */         flag1 = true;
/* 339 */         flag2 = false;
/*     */         break;
/*     */       case 12:
/* 342 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 13:
/* 345 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 346 */         flag1 = true;
/* 347 */         flag2 = false;
/*     */         break;
/*     */       case 14:
/* 350 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 15:
/* 353 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 354 */         flag1 = true;
/* 355 */         flag2 = false;
/*     */         break;
/*     */       case 16:
/* 358 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 17:
/* 361 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 362 */         flag1 = true;
/* 363 */         flag2 = false;
/*     */         break;
/*     */       case 18:
/* 366 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 367 */         flag1 = true;
/* 368 */         flag2 = false;
/*     */         break;
/*     */       case 19:
/* 371 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 20:
/* 374 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 375 */         flag1 = true;
/* 376 */         flag2 = true;
/*     */         break;
/*     */       case 21:
/* 379 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 22:
/* 382 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 383 */         flag1 = true;
/* 384 */         flag2 = true;
/*     */         break;
/*     */       case 23:
/* 387 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 24:
/* 390 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 25:
/* 393 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 26:
/* 396 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 27:
/* 399 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 28:
/* 402 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 403 */         flag1 = true;
/* 404 */         flag2 = true;
/*     */         break;
/*     */       case 29:
/* 407 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 30:
/* 410 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 31:
/* 413 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 414 */         flag1 = true;
/* 415 */         flag2 = false;
/*     */         break;
/*     */       case 32:
/* 418 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 419 */         flag1 = true;
/* 420 */         flag2 = true;
/*     */         break;
/*     */       case 33:
/* 423 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 424 */         flag1 = true;
/* 425 */         flag2 = false;
/*     */         break;
/*     */       case 34:
/* 428 */         remainingObjectID = getRemainingObjectIDSByFileID(fileId, objectId);
/* 429 */         flag1 = true;
/* 430 */         flag2 = true;
/*     */         break;
/*     */       case 35:
/* 433 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 36:
/* 436 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 37:
/* 439 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 38:
/* 442 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 39:
/* 445 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       case 40:
/* 448 */         size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/*     */         break;
/*     */       default:
/* 451 */         size = 1; break;
/*     */     } 
/* 453 */     if (flag1) {
/* 454 */       size = getSizeByObjectIDAndCount(objectId, count) + 3 + count * 2;
/* 455 */       if (flag2) {
/* 456 */         int cnt = 0;
/* 457 */         while (cnt++ < remainingObjectID.length) {
/* 458 */           byte tempObjectId = decData[fPos + size];
/* 459 */           tmp2[0] = decData[fPos + size + 2];
/* 460 */           tmp2[1] = decData[fPos + size + 1];
/* 461 */           int tmpCount = Functions.getIntFrom2ByteArray(tmp2);
/* 462 */           if (isExistsByteInArray(remainingObjectID, tempObjectId & 0xFF)) {
/* 463 */             size += getSizeByObjectIDAndCount(tempObjectId & 0xFF, tmpCount) + 3 + tmpCount * 2; continue;
/*     */           } 
/* 465 */           size += 3;
/*     */         } 
/*     */       } else {
/*     */         
/* 469 */         byte tempObjectId = decData[fPos + size];
/* 470 */         tmp2[0] = decData[fPos + size + 2];
/* 471 */         tmp2[1] = decData[fPos + size + 1];
/* 472 */         int tmpCount = Functions.getIntFrom2ByteArray(tmp2);
/* 473 */         if (tempObjectId == remainingObjectID[0]) {
/* 474 */           size += getSizeByObjectIDAndCount(tempObjectId & 0xFF, tmpCount) + 3 + tmpCount * 2;
/*     */         }
/*     */       } 
/*     */     } 
/* 478 */     return size;
/*     */   }
/*     */ 
/*     */   
/*     */   private static boolean isExistsByteInArray(int[] objectIds, int objectId) {
/* 483 */     boolean exists = false;
/* 484 */     for (int b : objectIds) {
/* 485 */       if (b == objectId) {
/* 486 */         exists = true;
/*     */         break;
/*     */       } 
/*     */     } 
/* 490 */     return exists;
/*     */   }
/*     */ 
/*     */   
/*     */   private static int[] getRemainingObjectIDSByFileID(int fileId, int objectId) {
/* 495 */     int[] tmpIds, remainingObjectIds = null;
/*     */ 
/*     */     
/* 498 */     switch (fileId) {
/*     */       case 2:
/* 500 */         if (objectId == 2) {
/* 501 */           remainingObjectIds = new int[] { 3 }; break;
/* 502 */         }  if (objectId == 3) {
/* 503 */           remainingObjectIds = new int[] { 2 };
/*     */         }
/*     */         break;
/*     */       case 8:
/* 507 */         tmpIds = new int[] { 9, 10, 11 };
/* 508 */         remainingObjectIds = getRemainingIDs(tmpIds, objectId, tmpIds.length - 1);
/*     */         break;
/*     */       case 10:
/* 511 */         tmpIds = new int[] { 13, 14, 15, 16, 17 };
/* 512 */         remainingObjectIds = getRemainingIDs(tmpIds, objectId, tmpIds.length - 1);
/*     */         break;
/*     */       case 11:
/* 515 */         if (objectId == 18) {
/* 516 */           remainingObjectIds = new int[] { 19 }; break;
/* 517 */         }  if (objectId == 19) {
/* 518 */           remainingObjectIds = new int[] { 18 };
/*     */         }
/*     */         break;
/*     */       case 13:
/* 522 */         if (objectId == 21) {
/* 523 */           remainingObjectIds = new int[] { 22 }; break;
/* 524 */         }  if (objectId == 22) {
/* 525 */           remainingObjectIds = new int[] { 21 };
/*     */         }
/*     */         break;
/*     */       case 15:
/* 529 */         if (objectId == 24) {
/* 530 */           remainingObjectIds = new int[] { 25 }; break;
/* 531 */         }  if (objectId == 25) {
/* 532 */           remainingObjectIds = new int[] { 24 };
/*     */         }
/*     */         break;
/*     */       case 17:
/* 536 */         if (objectId == 27) {
/* 537 */           remainingObjectIds = new int[] { 28 }; break;
/* 538 */         }  if (objectId == 28) {
/* 539 */           remainingObjectIds = new int[] { 27 };
/*     */         }
/*     */         break;
/*     */       case 18:
/* 543 */         if (objectId == 29) {
/* 544 */           remainingObjectIds = new int[] { 30 }; break;
/* 545 */         }  if (objectId == 30) {
/* 546 */           remainingObjectIds = new int[] { 29 };
/*     */         }
/*     */         break;
/*     */       case 20:
/* 550 */         if (objectId == 32) {
/* 551 */           remainingObjectIds = new int[] { 33 }; break;
/* 552 */         }  if (objectId == 33) {
/* 553 */           remainingObjectIds = new int[] { 32 };
/*     */         }
/*     */         break;
/*     */       case 22:
/* 557 */         tmpIds = new int[] { 35, 36, 37 };
/* 558 */         remainingObjectIds = getRemainingIDs(tmpIds, objectId, tmpIds.length - 1);
/*     */         break;
/*     */       case 28:
/* 561 */         tmpIds = new int[] { 43, 44, 45, 46 };
/* 562 */         remainingObjectIds = getRemainingIDs(tmpIds, objectId, tmpIds.length - 1);
/*     */         break;
/*     */       case 31:
/* 565 */         if (objectId == 49) {
/* 566 */           remainingObjectIds = new int[] { 50 }; break;
/* 567 */         }  if (objectId == 50) {
/* 568 */           remainingObjectIds = new int[] { 49 };
/*     */         }
/*     */         break;
/*     */       case 32:
/* 572 */         tmpIds = new int[] { 51, 52, 53 };
/* 573 */         remainingObjectIds = getRemainingIDs(tmpIds, objectId, tmpIds.length - 1);
/*     */         break;
/*     */       case 33:
/* 576 */         if (objectId == 54) {
/* 577 */           remainingObjectIds = new int[] { 55 }; break;
/* 578 */         }  if (objectId == 55) {
/* 579 */           remainingObjectIds = new int[] { 54 };
/*     */         }
/*     */         break;
/*     */       case 34:
/* 583 */         tmpIds = new int[] { 56, 57, 58, 59, 60, 61 };
/* 584 */         remainingObjectIds = getRemainingIDs(tmpIds, objectId, tmpIds.length - 1);
/*     */         break;
/*     */     } 
/* 587 */     return remainingObjectIds;
/*     */   }
/*     */ 
/*     */   
/*     */   private static int[] getRemainingIDs(int[] tmpIds, int objectId, int size) {
/* 592 */     int[] remainingObjectIds = new int[size];
/* 593 */     int idx = 0;
/* 594 */     for (int b : tmpIds) {
/* 595 */       if (b != objectId) {
/* 596 */         remainingObjectIds[idx++] = b;
/*     */       }
/*     */     } 
/* 599 */     return remainingObjectIds;
/*     */   }
/*     */ 
/*     */   
/*     */   private static int getFileIDByObjectID(int objectID) {
/* 604 */     int fileId = 0;
/* 605 */     switch (objectID) {
/*     */       case 1:
/* 607 */         fileId = 1;
/*     */         break;
/*     */       case 2:
/*     */       case 3:
/* 611 */         fileId = 2;
/*     */         break;
/*     */       case 4:
/* 614 */         fileId = 3;
/*     */         break;
/*     */       case 5:
/* 617 */         fileId = 4;
/*     */         break;
/*     */       case 6:
/* 620 */         fileId = 5;
/*     */         break;
/*     */       case 7:
/* 623 */         fileId = 6;
/*     */         break;
/*     */       case 8:
/* 626 */         fileId = 7;
/*     */         break;
/*     */       case 9:
/*     */       case 10:
/*     */       case 11:
/* 631 */         fileId = 8;
/*     */         break;
/*     */       case 12:
/* 634 */         fileId = 9;
/*     */         break;
/*     */       case 13:
/*     */       case 14:
/*     */       case 15:
/*     */       case 16:
/*     */       case 17:
/* 641 */         fileId = 10;
/*     */         break;
/*     */       case 18:
/*     */       case 19:
/* 645 */         fileId = 11;
/*     */         break;
/*     */       case 20:
/* 648 */         fileId = 12;
/*     */         break;
/*     */       case 21:
/*     */       case 22:
/* 652 */         fileId = 13;
/*     */         break;
/*     */       case 23:
/* 655 */         fileId = 14;
/*     */         break;
/*     */       case 24:
/*     */       case 25:
/* 659 */         fileId = 15;
/*     */         break;
/*     */       case 26:
/* 662 */         fileId = 16;
/*     */         break;
/*     */       case 27:
/*     */       case 28:
/* 666 */         fileId = 17;
/*     */         break;
/*     */       case 29:
/*     */       case 30:
/* 670 */         fileId = 18;
/*     */         break;
/*     */       case 31:
/* 673 */         fileId = 19;
/*     */         break;
/*     */       case 32:
/*     */       case 33:
/* 677 */         fileId = 20;
/*     */         break;
/*     */       case 34:
/* 680 */         fileId = 21;
/*     */         break;
/*     */       case 35:
/*     */       case 36:
/*     */       case 37:
/* 685 */         fileId = 22;
/*     */         break;
/*     */       case 38:
/* 688 */         fileId = 23;
/*     */         break;
/*     */       case 39:
/* 691 */         fileId = 24;
/*     */         break;
/*     */       case 40:
/* 694 */         fileId = 25;
/*     */         break;
/*     */       case 41:
/* 697 */         fileId = 26;
/*     */         break;
/*     */       case 42:
/* 700 */         fileId = 27;
/*     */         break;
/*     */       case 43:
/*     */       case 44:
/*     */       case 45:
/*     */       case 46:
/* 706 */         fileId = 28;
/*     */         break;
/*     */       case 47:
/* 709 */         fileId = 29;
/*     */         break;
/*     */       case 48:
/* 712 */         fileId = 30;
/*     */         break;
/*     */       case 49:
/*     */       case 50:
/* 716 */         fileId = 31;
/*     */         break;
/*     */       case 51:
/*     */       case 52:
/*     */       case 53:
/* 721 */         fileId = 32;
/*     */         break;
/*     */       case 54:
/*     */       case 55:
/* 725 */         fileId = 33;
/*     */         break;
/*     */       case 56:
/*     */       case 57:
/*     */       case 58:
/*     */       case 59:
/*     */       case 60:
/*     */       case 61:
/* 733 */         fileId = 34;
/*     */         break;
/*     */       case 62:
/* 736 */         fileId = 35;
/*     */         break;
/*     */       case 63:
/* 739 */         fileId = 36;
/*     */         break;
/*     */       case 64:
/* 742 */         fileId = 37;
/*     */         break;
/*     */       case 65:
/* 745 */         fileId = 38;
/*     */         break;
/*     */       case 66:
/* 748 */         fileId = 39;
/*     */         break;
/*     */       case 67:
/* 751 */         fileId = 40;
/*     */         break;
/*     */     } 
/*     */ 
/*     */     
/* 756 */     return fileId;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static int getSizeByObjectIDAndCount(int objectID, int count) {
/* 762 */     switch (objectID)
/*     */     { case 1:
/* 764 */         size = 62 * count;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 968 */         return size;case 2: size = 7 * count; return size;case 3: size = 21 * count; return size;case 4: size = 12 * count; return size;case 5: size = 9 * count; return size;case 6: size = 6 * count; return size;case 7: size = 67 * count; return size;case 8: size = 106 * count; return size;case 9: size = 45 * count; return size;case 10: size = 24 * count; return size;case 11: size = 20 * count; return size;case 12: size = 36 * count; return size;case 13: size = 6 * count; return size;case 14: size = 9 * count; return size;case 15: size = 13 * count; return size;case 16: size = 4 * count; return size;case 17: size = 6 * count; return size;case 18: size = 20 * count; return size;case 19: size = 48 * count; return size;case 20: size = 108 * count; return size;case 21: size = 6 * count; return size;case 22: size = 38 * count; return size;case 23: size = 2 * count; return size;case 24: size = 35 * count; return size;case 25: size = 35 * count; return size;case 26: size = 22 * count; return size;case 27: size = 25 * count; return size;case 28: size = 49 * count; return size;case 29: size = 5 * count; return size;case 30: size = 15 * count; return size;case 31: size = 28 * count; return size;case 32: size = 34 * count; return size;case 33: size = 38 * count; return size;case 34: size = 20 * count; return size;case 35: size = 207 * count; return size;case 36: size = 40 * count; return size;case 37: size = 181 * count; return size;case 38: size = 44 * count; return size;case 39: size = 38 * count; return size;case 40: size = 32 * count; return size;case 41: size = 257 * count; return size;case 42: size = 345 * count; return size;case 43: size = 8 * count; return size;case 44: size = 4 * count; return size;case 45: size = 104 * count; return size;case 46: size = 49 * count; return size;case 47: size = 25 * count; return size;case 48: size = 49 * count; return size;case 49: size = 49 * count; return size;case 50: size = 49 * count; return size;case 51: size = 4 * count; return size;case 52: size = 24 * count; return size;case 53: size = 8 * count; return size;case 54: size = 199 * count; return size;case 55: size = 462 * count; return size;case 56: size = 77 * count; return size;case 57: size = 154 * count; return size;case 58: size = 419 * count; return size;case 59: size = 77 * count; return size;case 60: size = 242 * count; return size;case 61: size = 6 * count; return size;case 62: size = 4 * count; return size;case 63: size = 33 * count; return size;case 64: size = 27 * count; return size;case 65: size = 40 * count; return size;case 66: size = 80 * count; return size;case 67: size = 22 * count; return size; }  int size = 1; return size;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\ConfigFileParser.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */