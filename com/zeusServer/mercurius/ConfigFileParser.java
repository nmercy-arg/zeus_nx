/*     */ package com.zeusServer.mercurius;
/*     */ 
/*     */ import com.zeus.mercuriusAVL.derby.beans.AudioNJSFileInfo;
/*     */ import com.zeusServer.util.CRC32;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.Rijndael;
/*     */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
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
/*     */   private static final int MERCURIUS_SECTION_HEADER_LEN = 5;
/*     */   private static final int AVL_1_1_GNRL_CFG = 14;
/*     */   private static final int AVL_1_2_GPS_CFG = 7;
/*     */   private static final int AVL_1_3_VEHICLE_SAFETY_CFG = 53;
/*     */   private static final int AVL_1_4_TRACKING_CFG = 391;
/*     */   private static final int AVL_1_5_DI_CFG = 544;
/*     */   private static final int AVL_1_6_AI_CFG = 59;
/*     */   private static final int AVL_1_7_PGM_CFG = 29;
/*     */   private static final int AVL_1_8_AUDIO_CFG = 4;
/*     */   private static final int AVL_2_9_PHONE_CONTACTS_CFG = 32;
/*     */   private static final int AVL_2_10_MSG_CFG = 176;
/*     */   private static final int AVL_2_11_GSM_CFG = 836;
/*     */   private static final int AVL_2_12_MODEM_CFG = 74;
/*     */   private static final int AVL_2_13_GSM_RECEIVER_CFG = 77;
/*     */   private static final int AVL_2_14_INCOMING_SMS_CFG = 21;
/*     */   private static final int AVL_2_15_INCOMING_CALL_CFG = 25;
/*     */   private static final int AVL_2_16_ZEUS_SERVER_CFG = 313;
/*     */   private static final int AVL_3_17_AUDIO_LOOKUP_CFG = 20;
/*     */   private static final int AVL_3_18_SCRIPT_LOOKUP_CFG = 20;
/*     */   private static final int AVL_3_19_RECORD_LOOKUP_CFG = 20;
/*     */   private static final int AVL_4_20_SCHEDULER_CFG = 45;
/*     */   private static final int AVL_4_21_DAYLIGHT_CFG = 92;
/*     */   private static final int AVL_4_22_HOLIDAY_CFG = 11;
/*     */   private static final int AVL_5_23_SCHEDULE_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_24_INPUT_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_25_ANALOG_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_26_TRACKING_SERVER_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_27_ZEUS_SERVER_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_28_VEHICLE_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_29_GSM_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_30_GPS_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_31_SMS_CALL_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_32_HARDWARE_EVENTS_CFG = 95;
/*     */   private static final int AVL_5_33_MISLANEOUS_EVENTS_CFG = 95;
/*     */   private static final int AVL_6_34_GEOFENCE_EVENTS_CFG = 95;
/*     */   private static final int AVL_7_35_ROUTE_1_10_EVENTS_CFG = 95;
/*     */   private static final int AVL_8_36_ROUTE_11_20_EVENTS_CFG = 95;
/*     */   private static final int AVL_9_37_ROUTE_21_30_EVENTS_CFG = 95;
/*     */   private static final int AVL_10_38_ROUTE_31_40_EVENTS_CFG = 95;
/*     */   private static final int AVL_11_39_ROUTE_41_50_EVENTS_CFG = 95;
/*     */   private static final int AVL_12_40_GEOFENCE_EACH_POINTS_CFG = 12;
/*     */   private static final int AVL_12_GEOFENCE_EACH_STATIC_CFG = 24;
/*     */   private static final int AVL_13_ROUTE_EACH_CHECK_POINT_CFG = 20;
/*     */   private static final int AVL_13_ROUTE_EACH_POLYGON_POINT_CFG = 8;
/*     */   private static final int AVL_13_ROUTE_EACH_STATIC_CFG = 20;
/*     */   public static final int AVL_FILE_1 = 1;
/*     */   public static final int AVL_FILE_2 = 2;
/*     */   public static final int AVL_FILE_3 = 3;
/*     */   public static final int AVL_FILE_4 = 4;
/*     */   public static final int AVL_FILE_5 = 5;
/*     */   public static final int AVL_FILE_6 = 6;
/*     */   public static final int AVL_FILE_7 = 7;
/*     */   public static final int AVL_FILE_8 = 8;
/*     */   public static final int AVL_FILE_9 = 9;
/*     */   public static final int AVL_FILE_10 = 10;
/*     */   public static final int AVL_FILE_11 = 11;
/*     */   public static final int AVL_FILE_12 = 12;
/*     */   public static final int AVL_FILE_13 = 13;
/*     */   public static final int AVL_SECTION_17 = 17;
/*     */   public static final int AVL_SECTION_18 = 18;
/*     */   public static final int AVL_SECTION_40 = 40;
/*     */   
/*     */   public static List<AudioNJSFileInfo> getAudioScriptLookupDataFromCFG(byte[] fileContent, int fileSize, ModuleCFG mCFG1) throws UnsupportedEncodingException {
/*     */     ModuleCFG mCFG;
/* 117 */     if (mCFG1 == null) {
/* 118 */       mCFG = arrangeCFGFile(fileContent, fileSize, false);
/*     */     } else {
/* 120 */       mCFG = mCFG1;
/*     */     } 
/* 122 */     byte[] tmp4 = new byte[4];
/* 123 */     System.arraycopy(mCFG.getIndexStructure(), 8, tmp4, 0, 4);
/* 124 */     int start = Functions.getIntFrom4ByteArray(tmp4);
/* 125 */     System.arraycopy(mCFG.getIndexStructure(), 12, tmp4, 0, 4);
/* 126 */     int end = Functions.getIntFrom4ByteArray(tmp4);
/* 127 */     byte[] lookupData = new byte[end - start];
/* 128 */     System.arraycopy(mCFG.getFileData(), start, lookupData, 0, end - start);
/*     */     
/* 130 */     return getAudioScriptLookupData(lookupData);
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<AudioNJSFileInfo> getAudioScriptLookupData(byte[] lookupData) throws UnsupportedEncodingException {
/* 135 */     List<AudioNJSFileInfo> reqiredJSFileList = null;
/* 136 */     byte[] tmp4 = new byte[4];
/* 137 */     if (lookupData != null && lookupData.length > 4) {
/*     */       
/* 139 */       int idx = 0;
/* 140 */       byte[] tmp2 = new byte[2];
/*     */       
/* 142 */       int counter = 0;
/* 143 */       byte[] fName = new byte[12];
/* 144 */       if ((lookupData[idx++] & 0xFF) == 3) {
/* 145 */         reqiredJSFileList = new ArrayList<>();
/* 146 */         if ((lookupData[idx++] & 0xFF) == 17) {
/* 147 */           tmp2[1] = lookupData[idx++];
/* 148 */           tmp2[0] = lookupData[idx++];
/* 149 */           int cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 150 */           while (counter++ < cnt) {
/* 151 */             idx += 2;
/* 152 */             System.arraycopy(lookupData, idx, tmp4, 0, 4);
/* 153 */             int crc32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 154 */             idx += 4;
/* 155 */             System.arraycopy(lookupData, idx, tmp4, 0, 4);
/* 156 */             int fle = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 157 */             idx += 4;
/* 158 */             System.arraycopy(lookupData, idx, fName, 0, 12);
/* 159 */             String name = (new String(fName, "ISO-8859-1")).trim();
/* 160 */             if (name != null && (name.endsWith(".raw") || name.endsWith(".RAW"))) {
/* 161 */               AudioNJSFileInfo aJSFile = new AudioNJSFileInfo();
/* 162 */               aJSFile.setName(name);
/* 163 */               aJSFile.setCrc32(crc32);
/* 164 */               aJSFile.setLength(fle);
/* 165 */               aJSFile.setDir(1);
/* 166 */               reqiredJSFileList.add(aJSFile);
/* 167 */               idx += 12;
/*     */             } 
/*     */           } 
/*     */         } 
/*     */         
/* 172 */         if ((lookupData[idx++] & 0xFF) == 18) {
/* 173 */           tmp2[1] = lookupData[idx++];
/* 174 */           tmp2[0] = lookupData[idx++];
/* 175 */           int cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 176 */           counter = 0;
/* 177 */           while (counter++ < cnt) {
/* 178 */             idx += 2;
/* 179 */             System.arraycopy(lookupData, idx, tmp4, 0, 4);
/* 180 */             int crc32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 181 */             idx += 4;
/* 182 */             System.arraycopy(lookupData, idx, tmp4, 0, 4);
/* 183 */             int fle = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 184 */             idx += 4;
/* 185 */             System.arraycopy(lookupData, idx, fName, 0, 12);
/* 186 */             String name = (new String(fName, "ISO-8859-1")).trim();
/* 187 */             if (name != null && (name.endsWith(".JS") || name.endsWith(".js"))) {
/* 188 */               AudioNJSFileInfo aJSFile = new AudioNJSFileInfo();
/* 189 */               aJSFile.setName(name);
/* 190 */               aJSFile.setCrc32(crc32);
/* 191 */               aJSFile.setLength(fle);
/* 192 */               aJSFile.setDir(2);
/* 193 */               reqiredJSFileList.add(aJSFile);
/* 194 */               idx += 12;
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 201 */     return reqiredJSFileList;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] prepareRequiredFileDataForDeviceByCRCMismatch(ModuleCFG uploadedMCFG, byte[] fileIDData) throws Exception {
/* 206 */     int flen = (uploadedMCFG.getFileData()).length;
/*     */     
/* 208 */     byte[] encBlock = new byte[16];
/* 209 */     byte[] tmp4 = new byte[4];
/*     */ 
/*     */ 
/*     */     
/* 213 */     int requiredFileLength = 16;
/* 214 */     for (int i = 0; i < fileIDData.length; i++) {
/* 215 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[i] * 4 - 4, tmp4, 0, 4);
/* 216 */       int startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 217 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[i] * 4, tmp4, 0, 4);
/* 218 */       int nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 219 */       if (nextStartPos <= startPos) {
/* 220 */         nextStartPos = flen;
/*     */       }
/* 222 */       requiredFileLength += nextStartPos - startPos;
/*     */     } 
/* 224 */     byte[] requiredFileContent = new byte[requiredFileLength];
/* 225 */     int idx = 16;
/* 226 */     for (int j = 0; j < fileIDData.length; j++) {
/* 227 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[j] * 4 - 4, tmp4, 0, 4);
/* 228 */       int startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 229 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[j] * 4, tmp4, 0, 4);
/* 230 */       int nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 231 */       if (nextStartPos <= startPos) {
/* 232 */         nextStartPos = flen;
/*     */       }
/* 234 */       System.arraycopy(uploadedMCFG.getFileData(), startPos, requiredFileContent, idx, nextStartPos - startPos);
/*     */       
/* 236 */       idx += nextStartPos - startPos;
/*     */     } 
/* 238 */     System.arraycopy(uploadedMCFG.getFileData(), 0, requiredFileContent, 0, 16);
/* 239 */     byte[] header = new byte[12];
/*     */     
/* 241 */     tmp4 = Functions.get4ByteArrayFromInt(requiredFileLength);
/* 242 */     header[4] = tmp4[3];
/* 243 */     header[5] = tmp4[2];
/* 244 */     header[6] = tmp4[1];
/* 245 */     header[7] = tmp4[0];
/* 246 */     int crc32 = CRC32.getCRC32(requiredFileContent);
/* 247 */     tmp4 = Functions.get4ByteArrayFromInt(crc32);
/* 248 */     header[8] = tmp4[3];
/* 249 */     header[9] = tmp4[2];
/* 250 */     header[10] = tmp4[1];
/* 251 */     header[11] = tmp4[0];
/* 252 */     int lpad = (requiredFileLength + 12) % 16;
/* 253 */     if (lpad > 0) {
/* 254 */       lpad = 16 - lpad;
/*     */     }
/* 256 */     byte[] decData = new byte[requiredFileLength + 12 + lpad];
/* 257 */     System.arraycopy(header, 0, decData, 0, 12);
/* 258 */     System.arraycopy(requiredFileContent, 0, decData, 12, requiredFileLength);
/* 259 */     flen = requiredFileLength + 12 + lpad;
/* 260 */     for (int k = 0; k < flen; ) {
/* 261 */       System.arraycopy(decData, k, encBlock, 0, 16);
/* 262 */       byte[] decBlock = Rijndael.encryptBytes(encBlock, Rijndael.aes_256, false);
/* 263 */       System.arraycopy(decBlock, 0, decData, k, 16);
/* 264 */       k += 16;
/*     */     } 
/* 266 */     return decData;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] prepareFileIDSByCRC32Mismatch(ModuleCFG mCFG, List<Integer> devCRC32) {
/* 271 */     List<Integer> requiredFileIDs = new ArrayList<>();
/* 272 */     for (int i = 0; i < 13; i++) {
/* 273 */       if (((Integer)devCRC32.get(i)).intValue() != ((Integer)mCFG.getCrc32List().get(i)).intValue()) {
/* 274 */         requiredFileIDs.add(Integer.valueOf(i));
/*     */       }
/*     */     } 
/* 277 */     byte[] ids = new byte[requiredFileIDs.size()];
/* 278 */     for (int j = 0; j < requiredFileIDs.size(); j++) {
/* 279 */       ids[j] = (byte)(((Integer)requiredFileIDs.get(j)).byteValue() + 1);
/*     */     }
/* 281 */     return ids;
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> buildCRC32FromReceivedBuffer(byte[] buffer) {
/* 286 */     byte[] tmp4 = new byte[4];
/* 287 */     List<Integer> deviceCRC32 = buildEmptyCRCList(13);
/* 288 */     int k = 0;
/* 289 */     for (int i = 0; i < 52; i += 4) {
/* 290 */       System.arraycopy(buffer, i, tmp4, 0, 4);
/* 291 */       deviceCRC32.set(k++, Integer.valueOf(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4))));
/*     */     } 
/* 293 */     return deviceCRC32;
/*     */   }
/*     */ 
/*     */   
/*     */   private static List<Integer> buildEmptyCRCList(int filesCount) {
/* 298 */     List<Integer> deviceCRC32 = new ArrayList<>(filesCount);
/* 299 */     for (int i = 0; i < filesCount; i++) {
/* 300 */       deviceCRC32.add(Integer.valueOf(-1));
/*     */     }
/* 302 */     return deviceCRC32;
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
/*     */   
/*     */   public static ModuleCFG arrangeCFGFile(byte[] fileContent, int fileSize, boolean buildCRC) {
/* 317 */     int fPos = 0;
/* 318 */     byte[] indexStructure = new byte[200];
/* 319 */     byte[] tmp4 = new byte[4];
/*     */ 
/*     */     
/* 322 */     fPos += 28;
/*     */ 
/*     */ 
/*     */     
/* 326 */     while (fPos < fileSize) {
/* 327 */       int fileId = fileContent[fPos];
/* 328 */       if (fileId > 0 && fileId <= 13) {
/* 329 */         tmp4 = Functions.get4ByteArrayFromInt(fPos);
/* 330 */         System.arraycopy(tmp4, 0, indexStructure, fileId * 4 - 4, 4);
/* 331 */         int size = getSizeByFileID(fileId, fileContent, fPos);
/* 332 */         fPos += size;
/* 333 */         tmp4 = Functions.get4ByteArrayFromInt(fPos);
/* 334 */         System.arraycopy(tmp4, 0, indexStructure, (fileId + 1) * 4 - 4, 4); continue;
/*     */       } 
/* 336 */       fPos++;
/*     */     } 
/*     */     
/* 339 */     List<Integer> crc32List = null;
/* 340 */     if (buildCRC) {
/* 341 */       crc32List = buildEmptyCRCList(13);
/*     */ 
/*     */ 
/*     */       
/* 345 */       for (int i = 1; i <= 13; i++) {
/* 346 */         int nextStartPos; System.arraycopy(indexStructure, i * 4 - 4, tmp4, 0, 4);
/* 347 */         int startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 348 */         if (i < 13) {
/* 349 */           System.arraycopy(indexStructure, (i + 1) * 4 - 4, tmp4, 0, 4);
/* 350 */           nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/*     */         } else {
/* 352 */           nextStartPos = fileSize;
/*     */         } 
/* 354 */         if (nextStartPos - startPos >= 4) {
/*     */ 
/*     */           
/* 357 */           byte[] idata = new byte[nextStartPos - startPos];
/*     */           
/* 359 */           System.arraycopy(fileContent, startPos, idata, 0, nextStartPos - startPos);
/* 360 */           int crc32 = CRC32.getCRC32(idata);
/* 361 */           crc32List.set(i - 1, Integer.valueOf(crc32));
/*     */         } 
/*     */       } 
/* 364 */     }  ModuleCFG incomingCFG = new ModuleCFG();
/* 365 */     incomingCFG.setFileData(fileContent);
/* 366 */     incomingCFG.setIndexStructure(indexStructure);
/* 367 */     incomingCFG.setCrc32List(crc32List);
/* 368 */     return incomingCFG;
/*     */   }
/*     */ 
/*     */   
/*     */   public static int getSizeByFileID(int fileID, byte[] fileContent, int fPos) {
/* 373 */     int sectionID, cnt, size = 1;
/*     */ 
/*     */     
/* 376 */     byte[] tmp2 = new byte[2];
/* 377 */     switch (fileID) {
/*     */       case 1:
/* 379 */         size += 19;
/* 380 */         size += 12;
/* 381 */         size += 58;
/* 382 */         size += 396;
/* 383 */         size += 549;
/*     */         
/* 385 */         sectionID = fileContent[fPos + size];
/* 386 */         tmp2[0] = fileContent[fPos + size + 2];
/* 387 */         tmp2[1] = fileContent[fPos + size + 1];
/* 388 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 389 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 391 */         sectionID = fileContent[fPos + size];
/* 392 */         tmp2[0] = fileContent[fPos + size + 2];
/* 393 */         tmp2[1] = fileContent[fPos + size + 1];
/* 394 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 395 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 397 */         size += 9;
/*     */         break;
/*     */       
/*     */       case 2:
/* 401 */         sectionID = fileContent[fPos + size];
/* 402 */         tmp2[0] = fileContent[fPos + size + 2];
/* 403 */         tmp2[1] = fileContent[fPos + size + 1];
/* 404 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 405 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 407 */         sectionID = fileContent[fPos + size];
/* 408 */         tmp2[0] = fileContent[fPos + size + 2];
/* 409 */         tmp2[1] = fileContent[fPos + size + 1];
/* 410 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 411 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 413 */         size += 841;
/* 414 */         size += 79;
/* 415 */         size += 82;
/* 416 */         size += 26;
/* 417 */         size += 30;
/* 418 */         size += 318;
/*     */         break;
/*     */       
/*     */       case 3:
/* 422 */         sectionID = fileContent[fPos + size];
/* 423 */         tmp2[0] = fileContent[fPos + size + 2];
/* 424 */         tmp2[1] = fileContent[fPos + size + 1];
/* 425 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 426 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 428 */         sectionID = fileContent[fPos + size];
/* 429 */         tmp2[0] = fileContent[fPos + size + 2];
/* 430 */         tmp2[1] = fileContent[fPos + size + 1];
/* 431 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 432 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 434 */         sectionID = fileContent[fPos + size];
/* 435 */         tmp2[0] = fileContent[fPos + size + 2];
/* 436 */         tmp2[1] = fileContent[fPos + size + 1];
/* 437 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 438 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       
/*     */       case 4:
/* 442 */         sectionID = fileContent[fPos + size];
/* 443 */         tmp2[0] = fileContent[fPos + size + 2];
/* 444 */         tmp2[1] = fileContent[fPos + size + 1];
/* 445 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 446 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 448 */         size += 97;
/*     */         
/* 450 */         sectionID = fileContent[fPos + size];
/* 451 */         tmp2[0] = fileContent[fPos + size + 2];
/* 452 */         tmp2[1] = fileContent[fPos + size + 1];
/* 453 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 454 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       
/*     */       case 5:
/* 458 */         sectionID = fileContent[fPos + size];
/* 459 */         tmp2[0] = fileContent[fPos + size + 2];
/* 460 */         tmp2[1] = fileContent[fPos + size + 1];
/* 461 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 462 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 464 */         sectionID = fileContent[fPos + size];
/* 465 */         tmp2[0] = fileContent[fPos + size + 2];
/* 466 */         tmp2[1] = fileContent[fPos + size + 1];
/* 467 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 468 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 470 */         sectionID = fileContent[fPos + size];
/* 471 */         tmp2[0] = fileContent[fPos + size + 2];
/* 472 */         tmp2[1] = fileContent[fPos + size + 1];
/* 473 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 474 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 476 */         sectionID = fileContent[fPos + size];
/* 477 */         tmp2[0] = fileContent[fPos + size + 2];
/* 478 */         tmp2[1] = fileContent[fPos + size + 1];
/* 479 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 480 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 482 */         sectionID = fileContent[fPos + size];
/* 483 */         tmp2[0] = fileContent[fPos + size + 2];
/* 484 */         tmp2[1] = fileContent[fPos + size + 1];
/* 485 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 486 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 488 */         sectionID = fileContent[fPos + size];
/* 489 */         tmp2[0] = fileContent[fPos + size + 2];
/* 490 */         tmp2[1] = fileContent[fPos + size + 1];
/* 491 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 492 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 494 */         sectionID = fileContent[fPos + size];
/* 495 */         tmp2[0] = fileContent[fPos + size + 2];
/* 496 */         tmp2[1] = fileContent[fPos + size + 1];
/* 497 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 498 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 500 */         sectionID = fileContent[fPos + size];
/* 501 */         tmp2[0] = fileContent[fPos + size + 2];
/* 502 */         tmp2[1] = fileContent[fPos + size + 1];
/* 503 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 504 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 506 */         sectionID = fileContent[fPos + size];
/* 507 */         tmp2[0] = fileContent[fPos + size + 2];
/* 508 */         tmp2[1] = fileContent[fPos + size + 1];
/* 509 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 510 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 512 */         sectionID = fileContent[fPos + size];
/* 513 */         tmp2[0] = fileContent[fPos + size + 2];
/* 514 */         tmp2[1] = fileContent[fPos + size + 1];
/* 515 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 516 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         
/* 518 */         sectionID = fileContent[fPos + size];
/* 519 */         tmp2[0] = fileContent[fPos + size + 2];
/* 520 */         tmp2[1] = fileContent[fPos + size + 1];
/* 521 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 522 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 6:
/* 525 */         sectionID = fileContent[fPos + size];
/* 526 */         tmp2[0] = fileContent[fPos + size + 2];
/* 527 */         tmp2[1] = fileContent[fPos + size + 1];
/* 528 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 529 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 7:
/* 532 */         sectionID = fileContent[fPos + size];
/* 533 */         tmp2[0] = fileContent[fPos + size + 2];
/* 534 */         tmp2[1] = fileContent[fPos + size + 1];
/* 535 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 536 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 8:
/* 539 */         sectionID = fileContent[fPos + size];
/* 540 */         tmp2[0] = fileContent[fPos + size + 2];
/* 541 */         tmp2[1] = fileContent[fPos + size + 1];
/* 542 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 543 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 9:
/* 546 */         sectionID = fileContent[fPos + size];
/* 547 */         tmp2[0] = fileContent[fPos + size + 2];
/* 548 */         tmp2[1] = fileContent[fPos + size + 1];
/* 549 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 550 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 10:
/* 553 */         sectionID = fileContent[fPos + size];
/* 554 */         tmp2[0] = fileContent[fPos + size + 2];
/* 555 */         tmp2[1] = fileContent[fPos + size + 1];
/* 556 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 557 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 11:
/* 560 */         sectionID = fileContent[fPos + size];
/* 561 */         tmp2[0] = fileContent[fPos + size + 2];
/* 562 */         tmp2[1] = fileContent[fPos + size + 1];
/* 563 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 564 */         size += (cnt == 0) ? 3 : (getSizeByObjectIDAndCount(sectionID, cnt) + 2 * cnt + 3);
/*     */         break;
/*     */       case 12:
/* 567 */         sectionID = fileContent[fPos + size++];
/* 568 */         tmp2[1] = fileContent[fPos + size++];
/* 569 */         tmp2[0] = fileContent[fPos + size++];
/* 570 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 571 */         if (cnt > 0) {
/* 572 */           int idx = 0;
/* 573 */           int noGP = 0;
/* 574 */           while (idx++ < cnt) {
/* 575 */             size++;
/* 576 */             size++;
/* 577 */             tmp2[1] = fileContent[fPos + size++];
/* 578 */             tmp2[0] = fileContent[fPos + size++];
/* 579 */             noGP = Functions.getIntFrom2ByteArray(tmp2);
/* 580 */             size += 24 + 12 * noGP;
/*     */           } 
/*     */         } 
/*     */         break;
/*     */       case 13:
/* 585 */         sectionID = fileContent[fPos + size++];
/* 586 */         tmp2[1] = fileContent[fPos + size++];
/* 587 */         tmp2[0] = fileContent[fPos + size++];
/* 588 */         cnt = Functions.getIntFrom2ByteArray(tmp2);
/* 589 */         if (cnt > 0) {
/* 590 */           int idx = 0;
/*     */ 
/*     */           
/* 593 */           while (idx++ < cnt) {
/* 594 */             size++;
/* 595 */             size++;
/* 596 */             tmp2[1] = fileContent[fPos + size++];
/* 597 */             tmp2[0] = fileContent[fPos + size++];
/* 598 */             int polyPoints = Functions.getIntFrom2ByteArray(tmp2);
/* 599 */             if (polyPoints > 0) {
/* 600 */               size += 8 * polyPoints;
/*     */             }
/* 602 */             tmp2[1] = fileContent[fPos + size++];
/* 603 */             tmp2[0] = fileContent[fPos + size++];
/* 604 */             int checkPoints = Functions.getIntFrom2ByteArray(tmp2);
/* 605 */             if (checkPoints > 0) {
/* 606 */               size += 20 * checkPoints;
/*     */             }
/*     */             
/* 609 */             size += 20;
/*     */           } 
/*     */         } 
/*     */         break;
/*     */     } 
/* 614 */     return size;
/*     */   }
/*     */ 
/*     */   
/*     */   private static int getSizeByObjectIDAndCount(int sectionID, int count) {
/* 619 */     int size = 0;
/* 620 */     switch (sectionID) {
/*     */       case 1:
/* 622 */         size = 14 * count;
/*     */         break;
/*     */       case 2:
/* 625 */         size = 7 * count;
/*     */         break;
/*     */       case 3:
/* 628 */         size = 53 * count;
/*     */         break;
/*     */       case 4:
/* 631 */         size = 391 * count;
/*     */         break;
/*     */       case 5:
/* 634 */         size = 544 * count;
/*     */         break;
/*     */       case 6:
/* 637 */         size = 59 * count;
/*     */         break;
/*     */       case 7:
/* 640 */         size = 29 * count;
/*     */         break;
/*     */       case 8:
/* 643 */         size = 4 * count;
/*     */         break;
/*     */       case 9:
/* 646 */         size = 32 * count;
/*     */         break;
/*     */       case 10:
/* 649 */         size = 176 * count;
/*     */         break;
/*     */       case 11:
/* 652 */         size = 836 * count;
/*     */         break;
/*     */       case 12:
/* 655 */         size = 74 * count;
/*     */         break;
/*     */       case 13:
/* 658 */         size = 77 * count;
/*     */         break;
/*     */       case 14:
/* 661 */         size = 21 * count;
/*     */         break;
/*     */       case 15:
/* 664 */         size = 25 * count;
/*     */         break;
/*     */       case 16:
/* 667 */         size = 313 * count;
/*     */         break;
/*     */       case 17:
/* 670 */         size = 20 * count;
/*     */         break;
/*     */       case 18:
/* 673 */         size = 20 * count;
/*     */         break;
/*     */       case 19:
/* 676 */         size = 20 * count;
/*     */         break;
/*     */       case 20:
/* 679 */         size = 45 * count;
/*     */         break;
/*     */       case 21:
/* 682 */         size = 92 * count;
/*     */         break;
/*     */       case 22:
/* 685 */         size = 11 * count;
/*     */         break;
/*     */       case 23:
/* 688 */         size = 95 * count;
/*     */         break;
/*     */       case 24:
/* 691 */         size = 95 * count;
/*     */         break;
/*     */       case 25:
/* 694 */         size = 95 * count;
/*     */         break;
/*     */       case 26:
/* 697 */         size = 95 * count;
/*     */         break;
/*     */       case 27:
/* 700 */         size = 95 * count;
/*     */         break;
/*     */       case 28:
/* 703 */         size = 95 * count;
/*     */         break;
/*     */       case 29:
/* 706 */         size = 95 * count;
/*     */         break;
/*     */       case 30:
/* 709 */         size = 95 * count;
/*     */         break;
/*     */       case 31:
/* 712 */         size = 95 * count;
/*     */         break;
/*     */       case 32:
/* 715 */         size = 95 * count;
/*     */         break;
/*     */       case 33:
/* 718 */         size = 95 * count;
/*     */         break;
/*     */       case 34:
/* 721 */         size = 95 * count;
/*     */         break;
/*     */       case 35:
/* 724 */         size = 95 * count;
/*     */         break;
/*     */       case 36:
/* 727 */         size = 95 * count;
/*     */         break;
/*     */       case 37:
/* 730 */         size = 95 * count;
/*     */         break;
/*     */       case 38:
/* 733 */         size = 95 * count;
/*     */         break;
/*     */       case 39:
/* 736 */         size = 95 * count;
/*     */         break;
/*     */       case 40:
/* 739 */         size = 12 * count;
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 746 */     return size;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\ConfigFileParser.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */