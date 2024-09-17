/*      */ package com.zeusServer.griffon;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.GriffonDBManager;
/*      */ import com.zeusServer.dto.FAI_Validation;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.socket.communication.TCPMessageServer;
/*      */ import com.zeusServer.tblConnections.TblActiveGriffonMobileConnections;
/*      */ import com.zeusServer.util.CRC16;
/*      */ import com.zeusServer.util.CRC32;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.Rijndael;
/*      */ import com.zeuscc.griffon.derby.beans.AudioFileDetails;
/*      */ import com.zeuscc.griffon.derby.beans.ExpansionModule;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonEnums;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonModule;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonUser;
/*      */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
/*      */ import com.zeuscc.griffon.derby.beans.PGM;
/*      */ import com.zeuscc.griffon.derby.beans.Partition;
/*      */ import com.zeuscc.griffon.derby.beans.PartitionGroup;
/*      */ import com.zeuscc.griffon.derby.beans.Schedular;
/*      */ import com.zeuscc.griffon.derby.beans.UserGroup;
/*      */ import com.zeuscc.griffon.derby.beans.VoiceMessage;
/*      */ import com.zeuscc.griffon.derby.beans.Zone;
/*      */ import com.zeuscc.griffon.derby.beans.ZoneGroup;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.security.InvalidAlgorithmParameterException;
/*      */ import java.security.InvalidKeyException;
/*      */ import java.security.NoSuchAlgorithmException;
/*      */ import java.sql.SQLException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ import javax.crypto.BadPaddingException;
/*      */ import javax.crypto.IllegalBlockSizeException;
/*      */ import javax.crypto.NoSuchPaddingException;
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
/*      */ public class GriffonHandlerHelper
/*      */ {
/*      */   public static void parseConfigurationData(ModuleCFG mCFG, String sn, int idClient) throws SQLException, InterruptedException, Exception {
/*   69 */     byte[] tmp4 = new byte[4];
/*   70 */     byte[] tmp2 = new byte[2];
/*   71 */     byte[] name = new byte[16];
/*   72 */     byte[] pass = new byte[6];
/*   73 */     byte[] snn = new byte[10];
/*      */ 
/*      */     
/*   76 */     int SN_LENGTH = 10;
/*   77 */     int NAME_LENGTH = 16;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*   84 */     List<ExpansionModule> emList = null;
/*   85 */     List<Partition> pList = null;
/*   86 */     List<Zone> zList = null;
/*   87 */     List<PGM> pgmList = null;
/*   88 */     List<GriffonUser> guList = null;
/*   89 */     List<UserGroup> ugList = null;
/*   90 */     List<ZoneGroup> zgList = null;
/*   91 */     List<PartitionGroup> pgList = null;
/*   92 */     List<Schedular> schList = null;
/*   93 */     List<AudioFileDetails> afdList = null;
/*   94 */     List<VoiceMessage> vmList = null;
/*      */ 
/*      */     
/*   97 */     System.arraycopy(mCFG.getIndexStructure(), 56, tmp4, 0, 4);
/*   98 */     int start = Functions.getIntFrom4ByteArray(tmp4);
/*   99 */     System.arraycopy(mCFG.getIndexStructure(), 60, tmp4, 0, 4);
/*  100 */     int end = Functions.getIntFrom4ByteArray(tmp4);
/*  101 */     if (end - start > 3) {
/*  102 */       emList = new ArrayList<>();
/*  103 */       byte[] emData = new byte[end - start];
/*  104 */       System.arraycopy(mCFG.getFileData(), start, emData, 0, end - start);
/*      */       
/*  106 */       for (int i = 43; i < emData.length; ) {
/*  107 */         ExpansionModule em = new ExpansionModule();
/*  108 */         tmp2[0] = emData[i + 1];
/*  109 */         tmp2[1] = emData[i];
/*  110 */         i += 2;
/*  111 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*      */         
/*  113 */         em.setEmIndex(idx);
/*  114 */         tmp2[0] = emData[i + 1];
/*  115 */         tmp2[1] = emData[i];
/*  116 */         i += 2;
/*  117 */         idx = Functions.getIntFrom2ByteArray(tmp2);
/*  118 */         em.setEmType(idx);
/*  119 */         int enable = emData[i];
/*  120 */         i++;
/*  121 */         System.arraycopy(emData, i, snn, 0, SN_LENGTH);
/*  122 */         em.setSn((new String(snn)).trim());
/*  123 */         i += SN_LENGTH;
/*  124 */         System.arraycopy(emData, i, name, 0, NAME_LENGTH);
/*  125 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  126 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("EM #" + em.getEmIndex()) : nameVal;
/*  127 */         em.setName(nameVal);
/*  128 */         i += NAME_LENGTH + 1 + 1 + 4;
/*  129 */         em.setEnabled(enable);
/*  130 */         emList.add(em);
/*      */       } 
/*      */     } 
/*      */     
/*  134 */     System.arraycopy(mCFG.getIndexStructure(), 24, tmp4, 0, 4);
/*  135 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  136 */     System.arraycopy(mCFG.getIndexStructure(), 28, tmp4, 0, 4);
/*  137 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  138 */     int pgmEnd = start;
/*  139 */     if (end - start > 3) {
/*  140 */       pList = new ArrayList<>();
/*      */       
/*  142 */       byte[] partitionData = new byte[end - start];
/*  143 */       System.arraycopy(mCFG.getFileData(), start, partitionData, 0, end - start);
/*      */       
/*  145 */       for (int i = 3; i < partitionData.length; ) {
/*  146 */         Partition par = new Partition();
/*  147 */         tmp2[0] = partitionData[i + 1];
/*  148 */         tmp2[1] = partitionData[i];
/*  149 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  150 */         par.setPartitionIndex(idx);
/*  151 */         tmp2[0] = partitionData[i + 2];
/*  152 */         tmp2[1] = partitionData[i + 3];
/*  153 */         par.setAccount(String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0').toUpperCase());
/*  154 */         i += 32;
/*  155 */         System.arraycopy(partitionData, i, name, 0, NAME_LENGTH);
/*  156 */         i += 50;
/*  157 */         par.setInstantEnable(partitionData[i]);
/*  158 */         i += 26;
/*  159 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  160 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("Partition #" + idx) : nameVal;
/*  161 */         par.setName(nameVal);
/*  162 */         pList.add(par);
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  167 */     System.arraycopy(mCFG.getIndexStructure(), 28, tmp4, 0, 4);
/*  168 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  169 */     System.arraycopy(mCFG.getIndexStructure(), 32, tmp4, 0, 4);
/*  170 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*      */     
/*  172 */     if (end - start > 3) {
/*  173 */       pgList = new ArrayList<>();
/*      */       
/*  175 */       byte[] partitionGroupData = new byte[end - start];
/*  176 */       System.arraycopy(mCFG.getFileData(), start, partitionGroupData, 0, end - start);
/*      */       int i;
/*  178 */       for (i = 82; i < partitionGroupData.length; ) {
/*  179 */         PartitionGroup pg = new PartitionGroup();
/*  180 */         tmp2[0] = partitionGroupData[i + 1];
/*  181 */         tmp2[1] = partitionGroupData[i];
/*  182 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  183 */         pg.setPartitionGroupIndex(idx);
/*  184 */         i += 2;
/*  185 */         tmp2[0] = partitionGroupData[i + 1];
/*  186 */         tmp2[1] = partitionGroupData[i];
/*  187 */         StringBuilder sb = new StringBuilder(); int ii;
/*  188 */         for (ii = 0; ii < 8; ii++) {
/*  189 */           if (((tmp2[1] & 0xFF) >> ii & 0x1) == 1) {
/*  190 */             sb.append(ii + 1).append(',');
/*      */           }
/*      */         } 
/*  193 */         for (ii = 0; ii < 8; ii++) {
/*  194 */           if (((tmp2[0] & 0xFF) >> ii & 0x1) == 1) {
/*  195 */             sb.append(ii + 9).append(',');
/*      */           }
/*      */         } 
/*  198 */         sb.deleteCharAt(sb.length() - 1);
/*  199 */         pg.setAssignedPartitions(sb.toString());
/*  200 */         i += 4;
/*  201 */         System.arraycopy(partitionGroupData, i, name, 0, NAME_LENGTH);
/*  202 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*      */         
/*  204 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("PG #" + idx) : nameVal;
/*  205 */         pg.setPartitionGroupName(nameVal);
/*  206 */         pgList.add(pg);
/*  207 */         i += NAME_LENGTH;
/*      */       } 
/*      */     } 
/*      */     
/*  211 */     HashMap<Integer, String> assignedZonesPerZG = null;
/*  212 */     HashMap<Integer, String> assignedZonesPerPT = null;
/*  213 */     System.arraycopy(mCFG.getIndexStructure(), 0, tmp4, 0, 4);
/*  214 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  215 */     System.arraycopy(mCFG.getIndexStructure(), 4, tmp4, 0, 4);
/*  216 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  217 */     if (end - start > 3) {
/*  218 */       zList = new ArrayList<>();
/*  219 */       assignedZonesPerZG = new HashMap<>();
/*  220 */       assignedZonesPerPT = new HashMap<>();
/*      */       
/*  222 */       byte[] zoneData = new byte[end - start];
/*      */       
/*  224 */       System.arraycopy(mCFG.getFileData(), start, zoneData, 0, end - start);
/*  225 */       for (int i = 3; i < zoneData.length; ) {
/*  226 */         Zone zone = new Zone();
/*  227 */         tmp2[0] = zoneData[i + 1];
/*  228 */         tmp2[1] = zoneData[i];
/*  229 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  230 */         zone.setZoneIndex(idx);
/*  231 */         tmp2[0] = zoneData[i + 3];
/*  232 */         tmp2[1] = zoneData[i + 2];
/*  233 */         char[] parMap = Functions.get16BitBinaryFromInt(Functions.getIntFrom2ByteArray(tmp2));
/*  234 */         tmp2[0] = zoneData[i + 5];
/*  235 */         tmp2[1] = zoneData[i + 4]; int ii;
/*  236 */         for (ii = 0; ii < 8; ii++) {
/*  237 */           if (((tmp2[1] & 0xFF) >> ii & 0x1) == 1) {
/*  238 */             if (assignedZonesPerZG.containsKey(Integer.valueOf(ii + 1))) {
/*  239 */               assignedZonesPerZG.put(Integer.valueOf(ii + 1), ((String)assignedZonesPerZG.get(Integer.valueOf(ii + 1))).concat(",").concat(String.valueOf(idx)));
/*      */             } else {
/*  241 */               assignedZonesPerZG.put(Integer.valueOf(ii + 1), String.valueOf(idx));
/*      */             } 
/*      */           }
/*      */         } 
/*  245 */         for (ii = 0; ii < 8; ii++) {
/*  246 */           if (((tmp2[0] & 0xFF) >> ii & 0x1) == 1) {
/*  247 */             if (assignedZonesPerZG.containsKey(Integer.valueOf(ii + 9))) {
/*  248 */               assignedZonesPerZG.put(Integer.valueOf(ii + 9), ((String)assignedZonesPerZG.get(Integer.valueOf(ii + 9))).concat(",").concat(String.valueOf(idx)));
/*      */             } else {
/*  250 */               assignedZonesPerZG.put(Integer.valueOf(ii + 9), String.valueOf(idx));
/*      */             } 
/*      */           }
/*      */         } 
/*      */         
/*  255 */         tmp2[0] = zoneData[i + 7];
/*  256 */         tmp2[1] = zoneData[i + 6];
/*      */         
/*  258 */         int zone24Sbutype = Functions.getIntFrom2ByteArray(tmp2);
/*      */         
/*  260 */         tmp2[0] = zoneData[i + 9];
/*  261 */         tmp2[1] = zoneData[i + 8];
/*      */         
/*  263 */         i += 25;
/*  264 */         System.arraycopy(zoneData, i, name, 0, 16);
/*  265 */         i += 39;
/*  266 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  267 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("Zone #" + idx) : nameVal;
/*  268 */         if (zone24Sbutype == 18) {
/*  269 */           nameVal = nameVal + " (*" + Functions.getIntFrom2ByteArray(tmp2) + ")";
/*      */         }
/*  271 */         zone.setName(nameVal);
/*      */         
/*  273 */         zone.setType(zoneData[i - 22] & 0xFF);
/*  274 */         switch (zone.getType()) {
/*      */           case 4:
/*  276 */             if (zone24Sbutype >= 1 && zone24Sbutype <= 19) {
/*  277 */               zone.setType(zone.getType() * 100 + zone24Sbutype);
/*      */             }
/*      */             break;
/*      */           case 11:
/*  281 */             zone.setAnalogVoltage(Functions.getIntFrom2ByteArray(tmp2));
/*      */             break;
/*      */         } 
/*  284 */         zone.setBypassable(zoneData[i - 19] & 0xFF);
/*      */         
/*  286 */         StringBuilder sb = new StringBuilder();
/*  287 */         for (int j = 15; j >= 0; j--) {
/*  288 */           if (parMap[j] == '1') {
/*  289 */             int pno = 15 - j + 1;
/*  290 */             sb.append(pno).append(',');
/*  291 */             if (assignedZonesPerPT.containsKey(Integer.valueOf(pno))) {
/*  292 */               assignedZonesPerPT.put(Integer.valueOf(pno), ((String)assignedZonesPerPT.get(Integer.valueOf(pno))).concat(",").concat(String.valueOf(idx)));
/*      */             } else {
/*  294 */               assignedZonesPerPT.put(Integer.valueOf(pno), String.valueOf(idx));
/*      */             } 
/*      */           } 
/*      */         } 
/*  298 */         if (sb.length() > 1) {
/*  299 */           sb.deleteCharAt(sb.length() - 1);
/*      */         }
/*  301 */         zone.setAssignedParitions(sb.toString());
/*  302 */         zList.add(zone);
/*      */       } 
/*      */     } 
/*      */     
/*  306 */     if (assignedZonesPerPT != null && pList != null && assignedZonesPerPT.size() > 0) {
/*  307 */       for (Partition p : pList) {
/*  308 */         if (assignedZonesPerPT.containsKey(Integer.valueOf(p.getPartitionIndex()))) {
/*  309 */           p.setAssignedZones(assignedZonesPerPT.get(Integer.valueOf(p.getPartitionIndex())));
/*      */         }
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*  315 */     start = end;
/*  316 */     System.arraycopy(mCFG.getIndexStructure(), 8, tmp4, 0, 4);
/*  317 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  318 */     if (end - start > 3) {
/*  319 */       zgList = new ArrayList<>();
/*      */       
/*  321 */       byte[] zoneGroupData = new byte[end - start];
/*  322 */       System.arraycopy(mCFG.getFileData(), start, zoneGroupData, 0, end - start);
/*  323 */       for (int i = 15; i < zoneGroupData.length; ) {
/*  324 */         ZoneGroup zg = new ZoneGroup();
/*  325 */         tmp2[0] = zoneGroupData[i + 1];
/*  326 */         tmp2[1] = zoneGroupData[i];
/*  327 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  328 */         zg.setZoneGroupIndex(idx);
/*  329 */         i += 2;
/*      */         
/*  331 */         System.arraycopy(zoneGroupData, i, name, 0, NAME_LENGTH);
/*  332 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*      */         
/*  334 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("ZoneGroup #" + idx) : nameVal;
/*  335 */         zg.setZoneGroupName(nameVal);
/*  336 */         i += NAME_LENGTH;
/*  337 */         zg.setBypassable(zoneGroupData[i]);
/*  338 */         if (assignedZonesPerZG != null && assignedZonesPerZG.containsKey(Integer.valueOf(idx))) {
/*  339 */           zg.setAssignedZones(assignedZonesPerZG.get(Integer.valueOf(idx)));
/*      */         }
/*  341 */         zgList.add(zg);
/*  342 */         i += 5;
/*      */       } 
/*      */     } 
/*      */     
/*  346 */     System.arraycopy(mCFG.getIndexStructure(), 20, tmp4, 0, 4);
/*  347 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  348 */     Map<Integer, String> pgmGroup = null;
/*  349 */     byte[] tmp8 = new byte[8];
/*  350 */     if (pgmEnd - start > 3) {
/*  351 */       pgmGroup = new HashMap<>(8);
/*  352 */       pgmList = new ArrayList<>();
/*      */       
/*  354 */       byte[] pgmData = new byte[pgmEnd - start];
/*  355 */       System.arraycopy(mCFG.getFileData(), start, pgmData, 0, pgmEnd - start);
/*      */       
/*  357 */       for (int i = 3; i < pgmData.length; ) {
/*  358 */         tmp2[0] = pgmData[i + 1];
/*  359 */         tmp2[1] = pgmData[i];
/*  360 */         PGM pgm = new PGM();
/*  361 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  362 */         pgm.setPgmIndex(idx);
/*      */         
/*  364 */         System.arraycopy(pgmData, i + 2, tmp8, 0, 8);
/*  365 */         i += 34;
/*  366 */         pgm.setPgmType(pgmData[i - 10]);
/*  367 */         pgm.setAddress(pgmData[i - 11] & 0xFF);
/*  368 */         System.arraycopy(pgmData, i, name, 0, NAME_LENGTH);
/*      */         
/*  370 */         System.arraycopy(pgmData, i + NAME_LENGTH, tmp4, 0, 4);
/*  371 */         byte assignedPGMGroups = 0;
/*  372 */         if (tmp4[0] == 24) {
/*  373 */           assignedPGMGroups = tmp8[0];
/*  374 */         } else if (tmp4[1] == 24) {
/*  375 */           assignedPGMGroups = tmp8[2];
/*  376 */         } else if (tmp4[2] == 24) {
/*  377 */           assignedPGMGroups = tmp8[4];
/*  378 */         } else if (tmp4[3] == 24) {
/*  379 */           assignedPGMGroups = tmp8[6];
/*      */         } 
/*  381 */         for (int k = 0; k < 8; k++) {
/*  382 */           if ((assignedPGMGroups >> k & 0x1) == 1) {
/*  383 */             if (pgmGroup.containsKey(Integer.valueOf(k + 1))) {
/*  384 */               pgmGroup.put(Integer.valueOf(k + 1), ((String)pgmGroup.get(Integer.valueOf(k + 1))).concat(",") + idx);
/*      */             } else {
/*  386 */               pgmGroup.put(Integer.valueOf(k + 1), String.valueOf(idx));
/*      */             } 
/*      */           }
/*      */         } 
/*  390 */         i += 35;
/*  391 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  392 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("PGM #" + idx) : nameVal;
/*  393 */         pgm.setName(nameVal);
/*  394 */         pgmList.add(pgm);
/*      */       } 
/*      */     } 
/*      */     
/*  398 */     System.arraycopy(mCFG.getIndexStructure(), 52, tmp4, 0, 4);
/*  399 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  400 */     System.arraycopy(mCFG.getIndexStructure(), 56, tmp4, 0, 4);
/*  401 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  402 */     Map<Integer, Integer> userRCMap = null;
/*  403 */     if (end - start > 3) {
/*  404 */       byte[] userRCData = new byte[end - start];
/*  405 */       System.arraycopy(mCFG.getFileData(), start, userRCData, 0, end - start);
/*  406 */       userRCMap = new HashMap<>();
/*  407 */       for (int i = 3; i < userRCData.length; ) {
/*  408 */         tmp2[0] = userRCData[i + 1];
/*  409 */         tmp2[1] = userRCData[i];
/*  410 */         i += 2;
/*  411 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  412 */         tmp2[0] = userRCData[i + 1];
/*  413 */         tmp2[1] = userRCData[i];
/*  414 */         i += 2;
/*  415 */         int tmp = Functions.getIntFrom2ByteArray(tmp2);
/*  416 */         userRCMap.put(Integer.valueOf(idx), Integer.valueOf(tmp));
/*      */       } 
/*      */     } 
/*      */     
/*  420 */     System.arraycopy(mCFG.getIndexStructure(), 44, tmp4, 0, 4);
/*  421 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  422 */     System.arraycopy(mCFG.getIndexStructure(), 48, tmp4, 0, 4);
/*  423 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  424 */     if (end - start > 3) {
/*  425 */       guList = new ArrayList<>();
/*      */       
/*  427 */       byte[] userData = new byte[end - start];
/*  428 */       System.arraycopy(mCFG.getFileData(), start, userData, 0, end - start);
/*  429 */       for (int i = 3; i < userData.length; ) {
/*  430 */         GriffonUser guser = new GriffonUser();
/*  431 */         tmp2[0] = userData[i + 1];
/*  432 */         tmp2[1] = userData[i];
/*  433 */         int idx = Functions.getIntFrom2ByteArray(tmp2) + 10;
/*  434 */         System.arraycopy(userData, i + 2, tmp4, 0, 4);
/*  435 */         guser.setLastPwdChanged(Functions.getDateFromInt(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4))));
/*  436 */         i += 10;
/*  437 */         guser.setUserIndex(idx);
/*  438 */         System.arraycopy(userData, i, tmp4, 0, 4);
/*  439 */         i += 5;
/*  440 */         guser.setUserGroupIndex(userData[i] & 0xFF);
/*  441 */         guser.setUserType(userData[i + 1] & 0xFF);
/*  442 */         i += 2;
/*  443 */         System.arraycopy(userData, i, pass, 0, 6);
/*  444 */         String nameVal = (new String(pass)).trim();
/*  445 */         guser.setPasscode(nameVal);
/*  446 */         i += 46;
/*  447 */         System.arraycopy(userData, i, name, 0, NAME_LENGTH);
/*  448 */         nameVal = (new String(name, "ISO-8859-1")).trim();
/*  449 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("User #" + idx) : nameVal;
/*  450 */         guser.setName(nameVal);
/*  451 */         i += NAME_LENGTH;
/*  452 */         i++;
/*      */         
/*  454 */         byte assignedPGMGroups = userData[i];
/*  455 */         Set<Integer> pgmset = new HashSet<>();
/*  456 */         for (int k = 0; k < 8; k++) {
/*  457 */           if ((assignedPGMGroups >> k & 0x1) == 1 && 
/*  458 */             pgmGroup != null && pgmGroup.containsKey(Integer.valueOf(k + 1))) {
/*  459 */             for (String pt : ((String)pgmGroup.get(Integer.valueOf(k + 1))).split(",")) {
/*  460 */               pgmset.add(Integer.valueOf(Integer.parseInt(pt)));
/*      */             }
/*      */           }
/*      */         } 
/*      */         
/*  465 */         StringBuilder sb = new StringBuilder();
/*  466 */         for (Integer pgmI : pgmset) {
/*  467 */           sb.append(pgmI).append(',');
/*      */         }
/*  469 */         if (sb.length() > 1) {
/*  470 */           sb.deleteCharAt(sb.length() - 1);
/*      */         }
/*  472 */         guser.setAssignedPGMS(sb.toString());
/*  473 */         i++;
/*  474 */         byte[] per = new byte[18];
/*  475 */         System.arraycopy(userData, i, per, 0, 18);
/*  476 */         i += 29;
/*      */         
/*  478 */         guser.setEnable(per[0]);
/*  479 */         guser.setBypassZones(per[1]);
/*  480 */         guser.setBypassZoneGroups(per[2]);
/*  481 */         guser.setBypassPartitions(per[3]);
/*  482 */         guser.setForceARM(per[4]);
/*  483 */         guser.setOnlyARM(per[5]);
/*  484 */         guser.setAwayARM(per[11]);
/*  485 */         guser.setSleepARM(per[12]);
/*  486 */         guser.setStatyARM(per[13]);
/*  487 */         guser.setAppUser(per[17]);
/*      */         
/*  489 */         sb = new StringBuilder(); int ii;
/*  490 */         for (ii = 0; ii < 8; ii++) {
/*  491 */           if (((tmp4[0] & 0xFF) >> ii & 0x1) == 1) {
/*  492 */             sb.append(ii + 1).append(',');
/*      */           }
/*      */         } 
/*  495 */         for (ii = 0; ii < 8; ii++) {
/*  496 */           if (((tmp4[1] & 0xFF) >> ii & 0x1) == 1) {
/*  497 */             sb.append(ii + 9).append(',');
/*      */           }
/*      */         } 
/*  500 */         if (sb.length() > 1) {
/*  501 */           sb.deleteCharAt(sb.length() - 1);
/*      */         }
/*  503 */         guser.setAssingedParitions(sb.toString());
/*      */         
/*  505 */         sb = new StringBuilder();
/*  506 */         for (ii = 0; ii < 8; ii++) {
/*  507 */           if (((tmp4[2] & 0xFF) >> ii & 0x1) == 1) {
/*  508 */             sb.append(ii + 1).append(',');
/*      */           }
/*      */         } 
/*  511 */         for (ii = 0; ii < 8; ii++) {
/*  512 */           if (((tmp4[3] & 0xFF) >> ii & 0x1) == 1) {
/*  513 */             sb.append(ii + 9).append(',');
/*      */           }
/*      */         } 
/*  516 */         if (sb.length() > 1) {
/*  517 */           sb.deleteCharAt(sb.length() - 1);
/*      */         }
/*  519 */         guser.setAssignedZoneGroups(sb.toString());
/*  520 */         if (userRCMap != null && userRCMap.containsKey(Integer.valueOf(idx))) {
/*  521 */           guser.setRptCode(String.format("%3s", new Object[] { String.valueOf(userRCMap.get(Integer.valueOf(idx))) }).replace(' ', '0'));
/*      */         }
/*      */         
/*  524 */         guList.add(guser);
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  529 */     System.arraycopy(mCFG.getIndexStructure(), 48, tmp4, 0, 4);
/*  530 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  531 */     System.arraycopy(mCFG.getIndexStructure(), 52, tmp4, 0, 4);
/*  532 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*      */     
/*  534 */     if (end - start > 3) {
/*  535 */       ugList = new ArrayList<>();
/*      */       
/*  537 */       byte[] userGroupData = new byte[end - start];
/*  538 */       System.arraycopy(mCFG.getFileData(), start, userGroupData, 0, end - start);
/*  539 */       int maxAge = userGroupData[5] & 0xFF;
/*  540 */       if (guList != null) {
/*  541 */         for (GriffonUser gu : guList) {
/*  542 */           gu.setMaxAge(maxAge);
/*      */         }
/*      */       }
/*  545 */       for (int i = 14; i < userGroupData.length; ) {
/*  546 */         UserGroup ug = new UserGroup();
/*  547 */         tmp2[0] = userGroupData[i + 1];
/*  548 */         tmp2[1] = userGroupData[i];
/*  549 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  550 */         ug.setUserGroupIndex(idx);
/*  551 */         i += 2;
/*  552 */         System.arraycopy(userGroupData, i, name, 0, NAME_LENGTH);
/*  553 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  554 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("UserGroup #" + idx) : nameVal;
/*  555 */         ug.setName(nameVal);
/*  556 */         i += NAME_LENGTH;
/*  557 */         byte[] per = new byte[18];
/*  558 */         System.arraycopy(userGroupData, i, per, 0, 18);
/*  559 */         ug.setEnable(per[0]);
/*  560 */         ug.setBypassZones(per[1]);
/*  561 */         ug.setBypassZoneGroups(per[2]);
/*  562 */         ug.setBypassPartitions(per[3]);
/*  563 */         ug.setForceARM(per[4]);
/*  564 */         ug.setOnlyARM(per[5]);
/*  565 */         ug.setAwayARM(per[11]);
/*  566 */         ug.setSleepARM(per[12]);
/*  567 */         ug.setStatyARM(per[13]);
/*  568 */         ugList.add(ug);
/*  569 */         i += 22;
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  574 */     System.arraycopy(mCFG.getIndexStructure(), 116, tmp4, 0, 4);
/*  575 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  576 */     System.arraycopy(mCFG.getIndexStructure(), 120, tmp4, 0, 4);
/*  577 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  578 */     if (end - start > 3) {
/*  579 */       schList = new ArrayList<>();
/*      */       
/*  581 */       byte[] schData = new byte[end - start];
/*  582 */       System.arraycopy(mCFG.getFileData(), start, schData, 0, end - start);
/*  583 */       for (int i = 3; i < schData.length; ) {
/*  584 */         tmp2[0] = schData[i + 1];
/*  585 */         tmp2[1] = schData[i];
/*  586 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  587 */         i += 14;
/*  588 */         System.arraycopy(schData, i, name, 0, NAME_LENGTH);
/*  589 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  590 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("Schedular #" + idx) : nameVal;
/*  591 */         i += NAME_LENGTH;
/*  592 */         if (schData[i] == 1) {
/*  593 */           Schedular sch = new Schedular(idx, nameVal);
/*  594 */           schList.add(sch);
/*      */         } 
/*  596 */         i += 21;
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  601 */     System.arraycopy(mCFG.getIndexStructure(), 76, tmp4, 0, 4);
/*  602 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  603 */     System.arraycopy(mCFG.getIndexStructure(), 80, tmp4, 0, 4);
/*  604 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  605 */     if (end - start > 3) {
/*  606 */       afdList = new ArrayList<>();
/*      */       
/*  608 */       byte[] voiceData = new byte[end - start];
/*  609 */       System.arraycopy(mCFG.getFileData(), start, voiceData, 0, end - start);
/*  610 */       tmp2[0] = voiceData[2];
/*  611 */       tmp2[1] = voiceData[1];
/*  612 */       int noOfAudioFiles = Functions.getIntFrom2ByteArray(tmp2);
/*  613 */       int counter = 0; int i;
/*  614 */       for (i = 3; counter++ < noOfAudioFiles && i < voiceData.length; ) {
/*  615 */         tmp2[0] = voiceData[i + 1];
/*  616 */         tmp2[1] = voiceData[i];
/*  617 */         int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  618 */         i += 3;
/*  619 */         System.arraycopy(voiceData, i, name, 0, NAME_LENGTH);
/*  620 */         String nameVal = (new String(name, "ISO-8859-1")).trim();
/*  621 */         nameVal = (nameVal == null || nameVal.isEmpty()) ? ("Audio File #" + idx) : nameVal;
/*  622 */         AudioFileDetails afd = new AudioFileDetails(idx, nameVal);
/*  623 */         afd.setType(1);
/*  624 */         afdList.add(afd);
/*  625 */         i += NAME_LENGTH + 17;
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  630 */     start = end;
/*  631 */     System.arraycopy(mCFG.getIndexStructure(), 84, tmp4, 0, 4);
/*  632 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  633 */     if (end - start > 3) {
/*  634 */       byte[] voiceData = new byte[end - start];
/*  635 */       System.arraycopy(mCFG.getFileData(), start, voiceData, 0, end - start);
/*  636 */       if (voiceData[0] == 34) {
/*  637 */         tmp2[0] = voiceData[2];
/*  638 */         tmp2[1] = voiceData[1];
/*  639 */         int noOfAudioFiles = Functions.getIntFrom2ByteArray(tmp2);
/*  640 */         vmList = new ArrayList<>(noOfAudioFiles);
/*      */         
/*  642 */         int counter = 0;
/*  643 */         for (int i = 3; counter++ < noOfAudioFiles && i < voiceData.length; ) {
/*  644 */           tmp2[0] = voiceData[i + 1];
/*  645 */           tmp2[1] = voiceData[i];
/*  646 */           int idx = Functions.getIntFrom2ByteArray(tmp2);
/*  647 */           VoiceMessage vm = new VoiceMessage();
/*  648 */           vm.setVmIndex(idx);
/*  649 */           i += 2;
/*  650 */           System.arraycopy(voiceData, i, tmp4, 0, 4);
/*      */           
/*  652 */           vm.setVmLength(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4)));
/*  653 */           i += 4;
/*  654 */           System.arraycopy(voiceData, i, tmp4, 0, 4);
/*  655 */           vm.setVmCRC32(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4)));
/*  656 */           i += 4;
/*  657 */           byte[] audioFName = new byte[12];
/*  658 */           System.arraycopy(voiceData, i, audioFName, 0, 12);
/*  659 */           vm.setVmName((new String(audioFName, "ISO-8859-1")).trim());
/*  660 */           i += 12;
/*  661 */           vmList.add(vm);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  667 */     FAI_Validation val = null;
/*  668 */     System.arraycopy(mCFG.getIndexStructure(), 132, tmp4, 0, 4);
/*  669 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  670 */     System.arraycopy(mCFG.getIndexStructure(), 136, tmp4, 0, 4);
/*  671 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  672 */     if (end - start > 3) {
/*  673 */       byte[] commData = new byte[end - start];
/*  674 */       System.arraycopy(mCFG.getFileData(), start, commData, 0, end - start);
/*      */       
/*  676 */       val = new FAI_Validation();
/*  677 */       int i = 5;
/*  678 */       i += 16;
/*  679 */       val.setTelephoneEnable(commData[i]);
/*  680 */       i += 61;
/*      */       
/*  682 */       i += 5;
/*  683 */       i += 2;
/*  684 */       val.setEthEnable(commData[i]);
/*  685 */       i += 152;
/*      */       
/*  687 */       i += 5;
/*      */       
/*  689 */       i += 153;
/*  690 */       byte[] apn = new byte[64];
/*  691 */       System.arraycopy(commData, i, apn, 0, 64);
/*  692 */       String ap = (new String(apn, "ISO-8859-1")).trim();
/*  693 */       if (ap == null || ap.isEmpty()) {
/*  694 */         val.setGprs_1_1_Enable(0);
/*      */       } else {
/*  696 */         val.setGprs_1_1_Enable(1);
/*      */       } 
/*  698 */       i += 128;
/*      */       
/*  700 */       System.arraycopy(commData, i, apn, 0, 64);
/*  701 */       ap = (new String(apn, "ISO-8859-1")).trim();
/*  702 */       if (ap == null || ap.isEmpty()) {
/*  703 */         val.setGprs_1_2_Enable(0);
/*      */       } else {
/*  705 */         val.setGprs_1_2_Enable(1);
/*      */       } 
/*  707 */       i += 140;
/*      */       
/*  709 */       i += 153;
/*      */       
/*  711 */       System.arraycopy(commData, i, apn, 0, 64);
/*  712 */       ap = (new String(apn, "ISO-8859-1")).trim();
/*  713 */       if (ap == null || ap.isEmpty()) {
/*  714 */         val.setGprs_2_1_Enable(0);
/*      */       } else {
/*  716 */         val.setGprs_2_1_Enable(1);
/*      */       } 
/*  718 */       i += 128;
/*      */       
/*  720 */       System.arraycopy(commData, i, apn, 0, 64);
/*  721 */       ap = (new String(apn, "ISO-8859-1")).trim();
/*  722 */       if (ap == null || ap.isEmpty()) {
/*  723 */         val.setGprs_2_2_Enable(0);
/*      */       } else {
/*  725 */         val.setGprs_2_2_Enable(1);
/*      */       } 
/*  727 */       i += 138;
/*      */       
/*  729 */       i += 5;
/*      */       
/*  731 */       i += 2;
/*  732 */       if (commData[i] == 0) {
/*  733 */         val.setGprs_1_1_Enable(0);
/*  734 */         val.setGprs_1_2_Enable(0);
/*  735 */         val.setGprs_2_1_Enable(0);
/*  736 */         val.setGprs_2_2_Enable(0);
/*      */       } 
/*      */       
/*  739 */       i += 75;
/*      */       
/*  741 */       i += 5;
/*  742 */       if (commData[i] == 1) {
/*  743 */         i += 8;
/*  744 */         apn = new byte[32];
/*  745 */         System.arraycopy(commData, i, apn, 0, 32);
/*  746 */         ap = (new String(apn, "ISO-8859-1")).trim();
/*  747 */         if (ap == null || ap.isEmpty()) {
/*  748 */           val.setWifi_AP_1_Enable(0);
/*      */         } else {
/*  750 */           val.setWifi_AP_1_Enable(1);
/*      */         } 
/*  752 */         i += 114;
/*      */         
/*  754 */         System.arraycopy(commData, i, apn, 0, 32);
/*  755 */         ap = (new String(apn, "ISO-8859-1")).trim();
/*  756 */         if (ap == null || ap.isEmpty()) {
/*  757 */           val.setWifi_AP_2_Enable(0);
/*      */         } else {
/*  759 */           val.setWifi_AP_2_Enable(1);
/*      */         } 
/*  761 */         i += 120;
/*      */       } else {
/*  763 */         val.setWifi_AP_1_Enable(0);
/*  764 */         val.setWifi_AP_2_Enable(0);
/*      */       } 
/*      */     } 
/*      */     
/*  768 */     System.arraycopy(mCFG.getIndexStructure(), 36, tmp4, 0, 4);
/*  769 */     start = Functions.getIntFrom4ByteArray(tmp4);
/*  770 */     System.arraycopy(mCFG.getIndexStructure(), 40, tmp4, 0, 4);
/*  771 */     end = Functions.getIntFrom4ByteArray(tmp4);
/*  772 */     if (end - start > 3) {
/*  773 */       byte[] systemData = new byte[end - start];
/*  774 */       System.arraycopy(mCFG.getFileData(), start, systemData, 0, end - start);
/*  775 */       if (val == null) {
/*  776 */         val = new FAI_Validation();
/*      */       }
/*  778 */       val.setUserWalkTestEnable(systemData[36]);
/*      */ 
/*      */       
/*  781 */       if (systemData[38] == 0) {
/*  782 */         GriffonDBManager.clearKeyfobReceiverOnUsbHwFail(mCFG.getIdModule());
/*      */       }
/*      */     } 
/*      */     
/*  786 */     GriffonModule gm = new GriffonModule();
/*  787 */     gm.setSn(sn);
/*  788 */     gm.setEmList(emList);
/*  789 */     gm.setpList(pList);
/*  790 */     gm.setzList(zList);
/*  791 */     gm.setPgmList(pgmList);
/*  792 */     gm.setSchList(schList);
/*  793 */     gm.setAfdList(afdList);
/*  794 */     gm.setPgList(pgList);
/*  795 */     gm.setZgList(zgList);
/*  796 */     gm.setUgList(ugList);
/*  797 */     gm.setGuList(guList);
/*  798 */     gm.setVmList(vmList);
/*  799 */     gm.setId_Module(mCFG.getIdModule());
/*  800 */     gm.setId_Client(idClient);
/*      */     
/*  802 */     GriffonDBManager.saveParsedCFGData(gm, val);
/*      */   }
/*      */ 
/*      */   
/*      */   public static void parseEBFWData(byte[] ebFWData, int idClient, int idModule, int ebFWCRC32) throws SQLException, InterruptedException, Exception {
/*  807 */     int indx = 0;
/*  808 */     int nos = ebFWData[indx++];
/*  809 */     int EACH_EM_FW_DATA_SIZE = 4;
/*  810 */     indx += EACH_EM_FW_DATA_SIZE;
/*  811 */     List<ExpansionModule> emList = new ArrayList<>(nos);
/*      */ 
/*      */     
/*  814 */     while (indx < nos * EACH_EM_FW_DATA_SIZE) {
/*  815 */       ExpansionModule em = new ExpansionModule();
/*  816 */       em.setEmIndex(ebFWData[indx++]);
/*  817 */       em.setFwVersion(ebFWData[indx++] + "." + ebFWData[indx++]);
/*  818 */       em.setLastResetStatus(ebFWData[indx++]);
/*  819 */       emList.add(em);
/*      */     } 
/*  821 */     List<ExpansionModule> kpLangList = null;
/*  822 */     int len = ebFWData.length;
/*  823 */     if (indx < len) {
/*  824 */       nos = ebFWData[indx++];
/*  825 */       kpLangList = new ArrayList<>(nos * 5);
/*      */ 
/*      */       
/*  828 */       byte[] tmp2 = new byte[2];
/*  829 */       while (indx < len) {
/*  830 */         int ebIndex = ebFWData[indx++];
/*  831 */         int eachKPLangSize = ebFWData[indx++];
/*  832 */         for (int i = 0; i < eachKPLangSize / 5; i++) {
/*  833 */           ExpansionModule em = new ExpansionModule();
/*  834 */           em.setEmIndex(ebIndex);
/*  835 */           tmp2[1] = ebFWData[indx++];
/*  836 */           tmp2[0] = ebFWData[indx++];
/*  837 */           em.setLastResetStatus(Functions.getIntFrom2ByteArray(tmp2));
/*  838 */           em.setFwVersion(ebFWData[indx++] + "." + ebFWData[indx++] + "." + ebFWData[indx++]);
/*  839 */           kpLangList.add(em);
/*      */         } 
/*      */       } 
/*      */     } 
/*  843 */     GriffonDBManager.updateEBFWVersionData(emList, kpLangList, idClient, idModule, ebFWCRC32, false);
/*      */   }
/*      */ 
/*      */   
/*      */   public static void parseRecordedFileLookupData(byte[] lookupData, int idClient, int idModule, int cnt, int recAudioLookupCRC32) throws UnsupportedEncodingException, SQLException, InterruptedException, Exception {
/*  848 */     byte[] tmp4 = new byte[4];
/*  849 */     byte[] name = new byte[12];
/*  850 */     int len = lookupData.length;
/*      */     
/*  852 */     List<AudioFileDetails> afdList = new ArrayList<>(cnt);
/*  853 */     for (int i = 0; i < len; ) {
/*  854 */       AudioFileDetails afd = new AudioFileDetails();
/*  855 */       afd.setType(2);
/*  856 */       System.arraycopy(lookupData, i, tmp4, 0, 4);
/*  857 */       i += 4;
/*  858 */       afd.setLength(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4)));
/*  859 */       System.arraycopy(lookupData, i, name, 0, 12);
/*  860 */       i += 12;
/*  861 */       afd.setAudioFileName((new String(name, "ISO-8859-1")).trim());
/*  862 */       afdList.add(afd);
/*      */     } 
/*  864 */     GriffonDBManager.updateRecordedFileLookupData(afdList, idClient, idModule, recAudioLookupCRC32);
/*      */   }
/*      */ 
/*      */   
/*      */   public static List<PGM> getPGMStatusFromDigitalBuffer(byte[] buffer) {
/*  869 */     List<PGM> pgmList = new ArrayList<>(16);
/*      */     
/*  871 */     int cnt = 1;
/*  872 */     for (int i = 0; i < buffer.length; i++) {
/*  873 */       byte[] tmp = Functions.getHighLowBytes(buffer[i]);
/*  874 */       if ((tmp[0] & 0xFF) >= 0) {
/*  875 */         PGM pgm = new PGM();
/*  876 */         pgm.setPgmIndex(cnt);
/*  877 */         pgm.setPgmStatus(getPGMStatusFromDashBoardBuffer(tmp[0], false));
/*  878 */         pgm.setUpdateOnly(true);
/*  879 */         pgmList.add(pgm);
/*      */       } 
/*  881 */       cnt++;
/*  882 */       if ((tmp[1] & 0xFF) >= 0) {
/*  883 */         PGM pgm = new PGM();
/*  884 */         pgm.setPgmIndex(cnt);
/*  885 */         pgm.setPgmStatus(getPGMStatusFromDashBoardBuffer(tmp[1], false));
/*  886 */         pgm.setUpdateOnly(true);
/*  887 */         pgmList.add(pgm);
/*      */       } 
/*  889 */       cnt++;
/*      */     } 
/*  891 */     return pgmList;
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte getPGMStatusFromDashBoardBuffer(byte pgmByte, boolean isAnalog) {
/*  896 */     byte pgmState = (byte)(pgmByte & 0x1);
/*  897 */     byte overrideState = (byte)(pgmByte >> 1 & 0x1);
/*  898 */     byte releaseState = (byte)(pgmByte >> 2 & 0x1);
/*  899 */     byte pgmStatus = 0;
/*  900 */     if (!isAnalog) {
/*  901 */       if (pgmState == 1 && overrideState == 1) {
/*  902 */         pgmStatus = 3;
/*  903 */       } else if (pgmState == 0 && overrideState == 1) {
/*  904 */         pgmStatus = 1;
/*  905 */       } else if (pgmState == 1 && releaseState == 1) {
/*  906 */         pgmStatus = 4;
/*  907 */       } else if (pgmState == 0 && releaseState == 1) {
/*  908 */         pgmStatus = 2;
/*  909 */       } else if (pgmState == 1) {
/*  910 */         pgmStatus = 5;
/*      */       }
/*      */     
/*  913 */     } else if (overrideState == 1) {
/*  914 */       pgmStatus = 3;
/*  915 */     } else if (releaseState == 1) {
/*  916 */       pgmStatus = 4;
/*  917 */     } else if (pgmState == 1) {
/*  918 */       pgmStatus = 1;
/*      */     } else {
/*  920 */       pgmState = 0;
/*      */     } 
/*      */     
/*  923 */     return pgmStatus;
/*      */   }
/*      */ 
/*      */   
/*      */   private static void getPartitionStatusFromDashBoardBuffer(Partition partition, byte partitionByte) {
/*  928 */     byte awayArmState = (byte)(partitionByte & 0x3);
/*  929 */     byte forceArmState = (byte)(partitionByte >> 2 & 0x1);
/*  930 */     byte tempLatchState = (byte)(partitionByte >> 3 & 0x1);
/*  931 */     byte fireAlarmState = (byte)(partitionByte >> 4 & 0x1);
/*  932 */     byte auxAlarmState = (byte)(partitionByte >> 5 & 0x1);
/*  933 */     byte panicAlarmState = (byte)(partitionByte >> 6 & 0x1);
/*  934 */     byte alarmState = (byte)(partitionByte >> 7 & 0x1);
/*  935 */     byte partitionStatus = -1;
/*  936 */     if (fireAlarmState == 1) {
/*  937 */       partitionStatus = 16;
/*  938 */     } else if (auxAlarmState == 1) {
/*  939 */       partitionStatus = 17;
/*  940 */     } else if (panicAlarmState == 1) {
/*  941 */       partitionStatus = 18;
/*  942 */     } else if (alarmState == 1) {
/*  943 */       partitionStatus = 51;
/*      */     } 
/*  945 */     partition.setAlarmStatus(partitionStatus);
/*  946 */     partitionStatus = 0;
/*      */     
/*  948 */     if (forceArmState == 1 && awayArmState == 1) {
/*  949 */       partitionStatus = 53;
/*  950 */     } else if (forceArmState == 1 && awayArmState == 2) {
/*  951 */       partitionStatus = 54;
/*  952 */     } else if (forceArmState == 1 && awayArmState == 3) {
/*  953 */       partitionStatus = 55;
/*  954 */     } else if (awayArmState == 0) {
/*  955 */       partitionStatus = 1;
/*  956 */     } else if (awayArmState == 1) {
/*  957 */       partitionStatus = 7;
/*  958 */     } else if (awayArmState == 2) {
/*  959 */       partitionStatus = 8;
/*  960 */     } else if (awayArmState == 3) {
/*  961 */       partitionStatus = 9;
/*      */     } 
/*  963 */     partition.setPartitionStatus(partitionStatus);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static int getZoneStatusFromDashBoardBuffer(List<Integer> analogZones, List<Integer> temp24HrZoneList, int idModule, int zoneIndex, byte zoneByte) throws SQLException, InterruptedException, Exception {
/*      */     int zoneStatus;
/*  974 */     if (analogZones != null && analogZones.contains(Integer.valueOf(zoneIndex))) {
/*  975 */       zoneStatus = zoneByte & 0xFF;
/*  976 */     } else if (temp24HrZoneList != null && temp24HrZoneList.contains(Integer.valueOf(zoneIndex))) {
/*  977 */       zoneStatus = zoneByte & 0xFF;
/*      */     } else {
/*  979 */       byte zoneState = (byte)(zoneByte & 0x3);
/*  980 */       byte armState = (byte)(zoneByte >> 2 & 0x3);
/*  981 */       byte alarmState = (byte)(zoneByte >> 4 & 0x1);
/*  982 */       byte masked = (byte)(zoneByte >> 5 & 0x1);
/*  983 */       byte offline = (byte)(zoneByte >> 6 & 0x1);
/*  984 */       zoneStatus = 50;
/*  985 */       if (offline == 1) {
/*  986 */         zoneStatus = 13;
/*  987 */       } else if (armState == 1) {
/*  988 */         zoneStatus = 4;
/*  989 */       } else if (masked == 1) {
/*  990 */         zoneStatus = 12;
/*      */       }
/*  992 */       else if (zoneState == 2) {
/*  993 */         zoneStatus = 3;
/*  994 */       } else if (zoneState == 3) {
/*  995 */         zoneStatus = 2;
/*  996 */       } else if (armState == 2) {
/*  997 */         zoneStatus = 6;
/*  998 */       } else if (alarmState == 1 && zoneState == 0) {
/*  999 */         zoneStatus = 51;
/* 1000 */       } else if (alarmState == 1) {
/* 1001 */         zoneStatus = 1;
/* 1002 */       } else if (armState == 3) {
/* 1003 */         zoneStatus = 52;
/* 1004 */       } else if (zoneState == 1) {
/* 1005 */         zoneStatus = 53;
/* 1006 */       } else if (zoneState == 0 || armState == 0 || alarmState == 0) {
/* 1007 */         zoneStatus = 50;
/*      */       } 
/*      */     } 
/*      */     
/* 1011 */     return zoneStatus;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public static GriffonModule handleDashboardBuffer(byte[] buffer, GriffonModule module, String sn, int idClient, int idModule) throws SQLException, InterruptedException, Exception {
/* 1017 */     List<ExpansionModule> emList = new ArrayList<>(16);
/* 1018 */     List<Partition> pList = new ArrayList<>(16);
/* 1019 */     List<Zone> zList = new ArrayList<>(128);
/* 1020 */     List<PGM> pgmList = new ArrayList<>(32);
/* 1021 */     int len = buffer.length;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1027 */     if (module == null) {
/* 1028 */       module = new GriffonModule();
/*      */     }
/* 1030 */     List<Integer> analogPGMIndex = GriffonDBManager.getAnalogPGMIndexByModuleID(idModule);
/*      */     
/* 1032 */     byte[] analogPGMBuffer = new byte[8];
/* 1033 */     System.arraycopy(buffer, 159, analogPGMBuffer, 0, 8);
/* 1034 */     int counter = 0;
/* 1035 */     Map<String, List<Integer>> map = GriffonDBManager.getAnalogZoneIndexByModuleID(idModule);
/* 1036 */     List<Integer> analogZones = map.get("ANALOG");
/* 1037 */     for (int idx = 0; idx < len; idx++) {
/* 1038 */       if (idx < 127 && (buffer[idx] & 0xFF) >= 0) {
/* 1039 */         Zone zone = new Zone(idx + 1, getZoneStatusFromDashBoardBuffer(analogZones, map.get("TEMPERATURE"), idModule, idx + 1, buffer[idx]));
/* 1040 */         if (analogZones.contains(Integer.valueOf(zone.getZoneIndex()))) {
/* 1041 */           zone.setAnalogVoltageValue(zone.getZoneStatus());
/*      */         }
/* 1043 */         zList.add(zone);
/* 1044 */       } else if (idx >= 127 && idx <= 142 && (buffer[idx] & 0xFF) >= 0) {
/* 1045 */         Partition partition = new Partition();
/* 1046 */         partition.setPartitionIndex(idx - 127 + 1);
/* 1047 */         getPartitionStatusFromDashBoardBuffer(partition, buffer[idx]);
/* 1048 */         pList.add(partition);
/* 1049 */       } else if (idx >= 143 && idx <= 166 && (buffer[idx] & 0xFF) >= 0) {
/* 1050 */         if (idx == 159) {
/* 1051 */           idx += 7;
/*      */         }
/* 1053 */         if (idx <= 159) {
/* 1054 */           byte[] tmp = Functions.getHighLowBytes(buffer[idx]);
/* 1055 */           if ((tmp[0] & 0xFF) >= 0) {
/* 1056 */             PGM pgm = new PGM();
/*      */             
/* 1058 */             pgm.setUpdateOnly(true);
/* 1059 */             pgm.setPgmIndex((idx - 143) * 2 + 1);
/* 1060 */             if (analogPGMIndex != null && analogPGMIndex.contains(Integer.valueOf(pgm.getPgmIndex()))) {
/* 1061 */               pgm.setPgmStatus(getPGMStatusFromDashBoardBuffer(tmp[0], true));
/* 1062 */               pgm.setPgmType(2);
/* 1063 */               pgm.setAnalogValue(analogPGMBuffer[counter] & 0xFF);
/* 1064 */               counter++;
/*      */             } else {
/* 1066 */               pgm.setPgmStatus(getPGMStatusFromDashBoardBuffer(tmp[0], false));
/*      */             } 
/* 1068 */             pgmList.add(pgm);
/*      */           } 
/* 1070 */           if ((tmp[1] & 0xFF) >= 0) {
/* 1071 */             PGM pgm = new PGM();
/* 1072 */             pgm.setUpdateOnly(true);
/* 1073 */             pgm.setPgmIndex((idx - 143) * 2 + 2);
/* 1074 */             if (analogPGMIndex != null && analogPGMIndex.contains(Integer.valueOf(pgm.getPgmIndex()))) {
/* 1075 */               pgm.setPgmType(2);
/* 1076 */               pgm.setPgmStatus(getPGMStatusFromDashBoardBuffer(tmp[1], true));
/* 1077 */               pgm.setAnalogValue(analogPGMBuffer[counter] & 0xFF);
/* 1078 */               counter++;
/*      */             } else {
/* 1080 */               pgm.setPgmStatus(getPGMStatusFromDashBoardBuffer(tmp[1], false));
/*      */             } 
/* 1082 */             pgmList.add(pgm);
/*      */           } 
/*      */         } 
/* 1085 */       } else if (idx >= 167 && idx <= 170) {
/* 1086 */         byte[] tmp4 = new byte[4];
/* 1087 */         System.arraycopy(buffer, 167, tmp4, 0, 4);
/* 1088 */         module.setMainPwrStatus(((tmp4[0] & 0x1) == 1) ? 0 : 1);
/* 1089 */         module.setBatteryStatus(((tmp4[0] >> 1 & 0x1) == 1) ? 0 : -1);
/* 1090 */         module.setBatteryStatus(((tmp4[0] >> 2 & 0x1) == 1) ? 1 : -1);
/* 1091 */         module.setAuxOutputStatus(((tmp4[0] >> 3 & 0x1) == 1) ? 1 : 0);
/* 1092 */         module.setBellStatus((short)GriffonEnums.EnumBellStatus.NORMAL.getStatus());
/* 1093 */         module.setBellStatus((short)(((tmp4[0] >> 4 & 0x1) == 1) ? GriffonEnums.EnumBellStatus.OVERLOADED.getStatus() : module.getBellStatus()));
/* 1094 */         module.setBellStatus((short)(((tmp4[0] >> 5 & 0x1) == 1) ? GriffonEnums.EnumBellStatus.DISCONNECTED.getStatus() : module.getBellStatus()));
/* 1095 */         module.setPhoneLineStatus(((tmp4[1] >> 3 & 0x1) == 1) ? 0 : 1);
/* 1096 */         module.setTamperStatus(((tmp4[1] & 0x1) == 1) ? 1 : 0);
/* 1097 */         module.setDualMonitoringStatus(((tmp4[1] >> 2 & 0x1) == 1) ? 1 : 0);
/* 1098 */         idx += 3;
/* 1099 */       } else if (idx >= 171 && idx <= 186 && (buffer[idx] & 0xFF) >= 0) {
/* 1100 */         int st = buffer[idx];
/* 1101 */         byte[] tmp = Functions.getHighLowBytes(st);
/* 1102 */         if ((tmp[0] & 0xFF) >= 0) {
/* 1103 */           ExpansionModule em = new ExpansionModule(idx - 171 + 1 + idx - 171, tmp[0] & 0xFF);
/* 1104 */           emList.add(em);
/*      */         } 
/* 1106 */         if ((tmp[1] & 0xFF) >= 0) {
/* 1107 */           ExpansionModule em = new ExpansionModule(idx - 171 + 2 + idx - 171, tmp[1] & 0xFF);
/* 1108 */           emList.add(em);
/*      */         } 
/* 1110 */       } else if (idx >= 187 && idx < 203) {
/* 1111 */         byte[] zs = new byte[16];
/* 1112 */         System.arraycopy(buffer, idx, zs, 0, 16);
/* 1113 */         idx += 16;
/* 1114 */         int zcnt = 0;
/* 1115 */         for (int k = 0; k < 16; k++) {
/* 1116 */           for (int j = 0; j < 8; j++) {
/* 1117 */             byte status = (byte)(zs[k] >> j & 0x1);
/* 1118 */             if (analogZones.contains(Integer.valueOf(zcnt + 1))) {
/* 1119 */               ((Zone)zList.get(zcnt)).setZoneStatus((status == 1) ? 4 : 50);
/*      */             }
/* 1121 */             zcnt++;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 1127 */     module.setSn(sn);
/* 1128 */     module.setId_Module(idModule);
/* 1129 */     module.setId_Client(idClient);
/* 1130 */     module.setEmList(emList);
/* 1131 */     module.setpList(pList);
/* 1132 */     module.setzList(zList);
/* 1133 */     module.setPgmList(pgmList);
/*      */     
/* 1135 */     return module;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static List<Zone> handleZoneStatusBuffer(int idModule, List<Zone> zList, byte[] buffer, boolean allZones) throws SQLException, InterruptedException, Exception {
/* 1142 */     Map<String, List<Integer>> map = GriffonDBManager.getAnalogZoneIndexByModuleID(idModule);
/* 1143 */     if (allZones) {
/* 1144 */       int len = buffer.length;
/* 1145 */       for (int idx = 0; idx < len; idx++) {
/* 1146 */         Zone zone = new Zone(idx + 1, getZoneStatusFromDashBoardBuffer(map.get("ANALOG"), map.get("TEMPERATURE"), idModule, idx + 1, buffer[idx]));
/* 1147 */         zList.add(zone);
/*      */       } 
/*      */     } else {
/* 1150 */       int len = (buffer[0] & 0xFF) * 2;
/* 1151 */       List<Integer> analogZones = map.get("ANALOG");
/* 1152 */       for (int idx = 1; idx < len; idx += 2) {
/* 1153 */         Zone zone = new Zone();
/* 1154 */         zone.setZoneIndex(buffer[idx]);
/* 1155 */         if (analogZones.contains(Integer.valueOf(zone.getZoneIndex()))) {
/* 1156 */           zone.setAnalogVoltageValue(getZoneStatusFromDashBoardBuffer(analogZones, map.get("TEMPERATURE"), idModule, buffer[idx], buffer[idx + 1]));
/*      */         } else {
/* 1158 */           zone.setZoneStatus(getZoneStatusFromDashBoardBuffer(analogZones, map.get("TEMPERATURE"), idModule, buffer[idx], buffer[idx + 1]));
/*      */         } 
/* 1160 */         zList.add(zone);
/*      */       } 
/*      */     } 
/* 1163 */     return zList;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void handleEBBuffer(List<ExpansionModule> emList, byte[] buffer) {
/* 1170 */     int len = (buffer[0] & 0xFF) * 2;
/* 1171 */     for (int idx = 1; idx < len; idx += 2) {
/* 1172 */       ExpansionModule eb = new ExpansionModule(buffer[idx], buffer[idx + 1]);
/* 1173 */       emList.add(eb);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void registerFailureSendCommand(String sn, String msg, short exec_Retries, int id_Command) throws SQLException, InterruptedException {
/* 1179 */     if (msg != null && msg.length() > 0) {
/* 1180 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, msg, Enums.EnumMessagePriority.HIGH, sn, null);
/*      */     }
/* 1182 */     if (exec_Retries + 1 >= 3) {
/* 1183 */       GriffonDBManager.executeSP_025(id_Command, (short)(exec_Retries + 1));
/*      */     } else {
/* 1185 */       GriffonDBManager.executeSP_026(id_Command, (short)(exec_Retries + 1));
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static String getFileNameByCommandData(int cData, int cmdType) {
/* 1191 */     String fileName = "";
/* 1192 */     if (cmdType == 32773) {
/* 1193 */       switch (cData) {
/*      */         case 1:
/* 1195 */           fileName = LocaleMessage.getLocaleMessage("Configuration_file");
/*      */           break;
/*      */         case 2:
/* 1198 */           fileName = LocaleMessage.getLocaleMessage("Firmware_File");
/*      */           break;
/*      */         case 3:
/* 1201 */           fileName = LocaleMessage.getLocaleMessage("Modem_Firmware_File");
/*      */           break;
/*      */         case 4:
/* 1204 */           fileName = LocaleMessage.getLocaleMessage("WiFi_Firmware_File");
/*      */           break;
/*      */         case 5:
/* 1207 */           fileName = LocaleMessage.getLocaleMessage("Keeloq_Firmware_File");
/*      */           break;
/*      */         case 6:
/* 1210 */           fileName = LocaleMessage.getLocaleMessage("Expansion_Board_Firmware_File");
/*      */           break;
/*      */         case 8:
/* 1213 */           fileName = LocaleMessage.getLocaleMessage("Keypad_Firmware");
/*      */           break;
/*      */       } 
/*      */     } else {
/* 1217 */       switch (cData) {
/*      */         case 1:
/*      */         case 5:
/* 1220 */           fileName = LocaleMessage.getLocaleMessage("Configuration_file");
/*      */           break;
/*      */         case 2:
/* 1223 */           fileName = LocaleMessage.getLocaleMessage("Events_File");
/*      */           break;
/*      */         case 3:
/* 1226 */           fileName = LocaleMessage.getLocaleMessage("Log_File");
/*      */           break;
/*      */         case 4:
/* 1229 */           fileName = LocaleMessage.getLocaleMessage("Access_log_file");
/*      */           break;
/*      */         case 8:
/* 1232 */           fileName = LocaleMessage.getLocaleMessage("Recorded_Audio_File");
/*      */           break;
/*      */       } 
/*      */     
/*      */     } 
/* 1237 */     return fileName;
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte[] prepareCommandPacket(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/* 1242 */     int plen = data.length;
/*      */     
/* 1244 */     int lpad = plen % 16;
/* 1245 */     if (lpad > 0) {
/* 1246 */       lpad = 16 - lpad;
/*      */     }
/* 1248 */     byte[] packet = new byte[plen + lpad + 4];
/* 1249 */     byte[] toEnc = new byte[plen + lpad];
/* 1250 */     System.arraycopy(data, 0, toEnc, 0, plen);
/* 1251 */     if (lpad > 0) {
/* 1252 */       for (int j = plen; j < plen + lpad; j++) {
/* 1253 */         toEnc[j] = 0;
/*      */       }
/*      */     }
/*      */ 
/*      */     
/* 1258 */     for (int i = 0; i < toEnc.length; i += 16) {
/* 1259 */       byte[] block = new byte[16];
/* 1260 */       System.arraycopy(toEnc, i, block, 0, 16);
/* 1261 */       byte[] decBlock = Rijndael.encryptBytes(block, Rijndael.aes_256, false);
/*      */       
/* 1263 */       System.arraycopy(decBlock, 0, toEnc, i, 16);
/*      */     } 
/*      */     
/* 1266 */     byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(plen + lpad + 2));
/* 1267 */     System.arraycopy(tmp, 0, packet, 0, 2);
/* 1268 */     System.arraycopy(toEnc, 0, packet, 2, plen + lpad);
/*      */     
/* 1270 */     int crcCalc = CRC16.calculate(packet, 0, plen + lpad + 2, 65535);
/* 1271 */     tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1272 */     System.arraycopy(tmp, 0, packet, plen + lpad + 2, 2);
/* 1273 */     return packet;
/*      */   }
/*      */ 
/*      */   
/*      */   public static String getReceiveLogFileName(int fileNo) {
/* 1278 */     switch (fileNo) {
/*      */       case 2:
/* 1280 */         return LocaleMessage.getLocaleMessage("Events_File");
/*      */       case 3:
/* 1282 */         return LocaleMessage.getLocaleMessage("Log_File");
/*      */       case 4:
/* 1284 */         return LocaleMessage.getLocaleMessage("Access_log_file");
/*      */     } 
/* 1286 */     return "";
/*      */   }
/*      */ 
/*      */   
/*      */   public static void endCommand(int id_Module, short exec_Retries) throws SQLException, InterruptedException {
/* 1291 */     GriffonDBManager.executeSP_027(id_Module, (short)(exec_Retries + 1));
/*      */   }
/*      */ 
/*      */   
/*      */   public static ModuleCFG rearrangeServerCFGCopy(short productID, byte[] fileContent, ModuleCFG mCFG, int flen, byte[] fileIDData) {
/* 1296 */     byte[] tmp4 = new byte[4];
/* 1297 */     int fileIndex = 0;
/*      */ 
/*      */ 
/*      */     
/* 1301 */     ModuleCFG incomingCFG = ConfigFileParser.arrangeCFGFile(productID, fileContent, flen);
/* 1302 */     List<Integer> newDataFiles = new ArrayList<>(fileIDData.length);
/* 1303 */     for (int i = 1; i < fileIDData.length; i++) {
/* 1304 */       newDataFiles.add(Integer.valueOf(fileIDData[i] & 0xFF));
/*      */     }
/* 1306 */     int newFileCount = 16;
/* 1307 */     for (int j = 1; j <= ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); j++) {
/* 1308 */       int startPos, nextStartPos; if (newDataFiles.contains(Integer.valueOf(j))) {
/* 1309 */         System.arraycopy(incomingCFG.getIndexStructure(), j * 4 - 4, tmp4, 0, 4);
/* 1310 */         startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1311 */         System.arraycopy(incomingCFG.getIndexStructure(), j * 4, tmp4, 0, 4);
/* 1312 */         nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1313 */         if (nextStartPos <= startPos) {
/* 1314 */           nextStartPos = flen;
/*      */         }
/*      */       } else {
/* 1317 */         System.arraycopy(mCFG.getIndexStructure(), j * 4 - 4, tmp4, 0, 4);
/* 1318 */         startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1319 */         System.arraycopy(mCFG.getIndexStructure(), j * 4, tmp4, 0, 4);
/* 1320 */         nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1321 */         if (nextStartPos <= startPos) {
/* 1322 */           nextStartPos = (mCFG.getFileData()).length;
/*      */         }
/*      */       } 
/* 1325 */       newFileCount += nextStartPos - startPos;
/*      */     } 
/* 1327 */     byte[] newFileContent = new byte[newFileCount];
/* 1328 */     System.arraycopy(incomingCFG.getFileData(), fileIndex, newFileContent, 0, 16);
/* 1329 */     fileIndex += 16;
/*      */     
/* 1331 */     for (int k = 1; k <= ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); k++) {
/* 1332 */       int startPos, nextStartPos; if (newDataFiles.contains(Integer.valueOf(k))) {
/* 1333 */         System.arraycopy(incomingCFG.getIndexStructure(), k * 4 - 4, tmp4, 0, 4);
/* 1334 */         startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1335 */         System.arraycopy(incomingCFG.getIndexStructure(), k * 4, tmp4, 0, 4);
/* 1336 */         nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1337 */         if (nextStartPos <= startPos) {
/* 1338 */           nextStartPos = flen;
/*      */         }
/* 1340 */         System.arraycopy(incomingCFG.getFileData(), startPos, newFileContent, fileIndex, nextStartPos - startPos);
/*      */       } else {
/* 1342 */         System.arraycopy(mCFG.getIndexStructure(), k * 4 - 4, tmp4, 0, 4);
/* 1343 */         startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1344 */         System.arraycopy(mCFG.getIndexStructure(), k * 4, tmp4, 0, 4);
/* 1345 */         nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1346 */         if (nextStartPos <= startPos) {
/* 1347 */           nextStartPos = (mCFG.getFileData()).length;
/*      */         }
/* 1349 */         System.arraycopy(mCFG.getFileData(), startPos, newFileContent, fileIndex, nextStartPos - startPos);
/*      */       } 
/* 1351 */       fileIndex += nextStartPos - startPos;
/*      */     } 
/* 1353 */     ModuleCFG newModuleCFG = ConfigFileParser.arrangeCFGFile(productID, newFileContent, newFileCount);
/* 1354 */     int calcCfgCrc32 = CRC32.getCRC32(newFileContent);
/* 1355 */     newModuleCFG.setCrc32(calcCfgCrc32);
/* 1356 */     return newModuleCFG;
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte[] prepareRequiredFileDataForDeviceByCRCMismatch(short productID, ModuleCFG uploadedMCFG, byte[] fileIDData) throws Exception {
/* 1361 */     int flen = (uploadedMCFG.getFileData()).length;
/*      */     
/* 1363 */     byte[] encBlock = new byte[16];
/* 1364 */     byte[] tmp4 = new byte[4];
/*      */ 
/*      */ 
/*      */     
/* 1368 */     int requiredFileLength = 16;
/* 1369 */     for (int i = 1; i < fileIDData.length; i++) {
/* 1370 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[i] * 4 - 4, tmp4, 0, 4);
/* 1371 */       int startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1372 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[i] * 4, tmp4, 0, 4);
/* 1373 */       int nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1374 */       if (nextStartPos <= startPos) {
/* 1375 */         nextStartPos = flen;
/*      */       }
/* 1377 */       requiredFileLength += nextStartPos - startPos;
/*      */     } 
/* 1379 */     byte[] requiredFileContent = new byte[requiredFileLength];
/* 1380 */     int idx = 16;
/* 1381 */     for (int j = 1; j < fileIDData.length; j++) {
/* 1382 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[j] * 4 - 4, tmp4, 0, 4);
/* 1383 */       int startPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1384 */       System.arraycopy(uploadedMCFG.getIndexStructure(), fileIDData[j] * 4, tmp4, 0, 4);
/* 1385 */       int nextStartPos = Functions.getIntFrom4ByteArray(tmp4);
/* 1386 */       if (nextStartPos <= startPos) {
/* 1387 */         nextStartPos = flen;
/*      */       }
/* 1389 */       System.arraycopy(uploadedMCFG.getFileData(), startPos, requiredFileContent, idx, nextStartPos - startPos);
/* 1390 */       idx += nextStartPos - startPos;
/*      */     } 
/* 1392 */     System.arraycopy(uploadedMCFG.getFileData(), 0, requiredFileContent, 0, 16);
/* 1393 */     byte[] header = new byte[10];
/* 1394 */     header[0] = (byte)(((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40) + 4);
/* 1395 */     header[1] = 0;
/* 1396 */     tmp4 = Functions.get4ByteArrayFromInt(requiredFileLength);
/* 1397 */     header[2] = tmp4[3];
/* 1398 */     header[3] = tmp4[2];
/* 1399 */     header[4] = tmp4[1];
/* 1400 */     header[5] = tmp4[0];
/* 1401 */     int crc32 = CRC32.getCRC32(requiredFileContent);
/* 1402 */     tmp4 = Functions.get4ByteArrayFromInt(crc32);
/* 1403 */     header[6] = tmp4[3];
/* 1404 */     header[7] = tmp4[2];
/* 1405 */     header[8] = tmp4[1];
/* 1406 */     header[9] = tmp4[0];
/* 1407 */     int lpad = (requiredFileLength + 10) % 16;
/* 1408 */     if (lpad > 0) {
/* 1409 */       lpad = 16 - lpad;
/*      */     }
/* 1411 */     byte[] decData = new byte[requiredFileLength + 10 + lpad];
/* 1412 */     System.arraycopy(header, 0, decData, 0, 10);
/* 1413 */     System.arraycopy(requiredFileContent, 0, decData, 10, requiredFileLength);
/* 1414 */     flen = requiredFileLength + 10 + lpad;
/* 1415 */     for (int k = 0; k < flen; ) {
/* 1416 */       System.arraycopy(decData, k, encBlock, 0, 16);
/* 1417 */       byte[] decBlock = Rijndael.encryptBytes(encBlock, Rijndael.aes_256, false);
/* 1418 */       System.arraycopy(decBlock, 0, decData, k, 16);
/* 1419 */       k += 16;
/*      */     } 
/* 1421 */     return decData;
/*      */   }
/*      */   
/*      */   public static void finalizeReceiveCFGFileCommand(int idCommand, int idModule, ModuleCFG mCFG, List<VoiceMessage> vmList) throws Exception {
/* 1425 */     int size = (mCFG.getFileData()).length;
/* 1426 */     int lPad = (size + 10) % 16;
/* 1427 */     if (lPad > 0) {
/* 1428 */       lPad = 16 - lPad;
/*      */     }
/* 1430 */     byte[] b = new byte[size + lPad + 10];
/* 1431 */     b[0] = 40;
/* 1432 */     b[1] = 0;
/*      */     
/* 1434 */     byte[] tmp4 = Functions.get4ByteArrayFromInt(size);
/* 1435 */     b[2] = tmp4[3];
/* 1436 */     b[3] = tmp4[2];
/* 1437 */     b[4] = tmp4[1];
/* 1438 */     b[5] = tmp4[0];
/*      */     
/* 1440 */     tmp4 = Functions.get4ByteArrayFromInt(mCFG.getCrc32());
/* 1441 */     b[6] = tmp4[3];
/* 1442 */     b[7] = tmp4[2];
/* 1443 */     b[8] = tmp4[1];
/* 1444 */     b[9] = tmp4[0];
/*      */     
/* 1446 */     System.arraycopy(mCFG.getFileData(), 0, b, 10, size);
/* 1447 */     size = b.length;
/* 1448 */     byte[] encBlock = new byte[16];
/*      */     
/* 1450 */     for (int i = 0; i < size; ) {
/* 1451 */       System.arraycopy(b, i, encBlock, 0, 16);
/* 1452 */       byte[] decBlock = Rijndael.encryptBytes(encBlock, Rijndael.aes_256, false);
/* 1453 */       System.arraycopy(decBlock, 0, b, i, 16);
/* 1454 */       i += 16;
/*      */     } 
/*      */ 
/*      */     
/* 1458 */     if (vmList != null) {
/*      */       
/* 1460 */       File file = Functions.writeByteArrayToFile("GRCP_" + idModule, b);
/* 1461 */       long cfgLen = file.length();
/* 1462 */       RandomAccessFile fc = new RandomAccessFile(file, "rw");
/* 1463 */       fc.seek(cfgLen);
/*      */       try {
/* 1465 */         for (VoiceMessage vm : vmList) {
/* 1466 */           byte[] content = GriffonDBManager.getVoiceMessagesByName(vm.getVmLength(), vm.getVmName(), vm.getVmCRC32());
/* 1467 */           if (content != null) {
/* 1468 */             byte[] vmHeader = new byte[22];
/* 1469 */             byte[] tmp2 = Functions.get2ByteArrayFromInt(vm.getVmIndex());
/* 1470 */             vmHeader[0] = tmp2[1];
/* 1471 */             vmHeader[1] = tmp2[0];
/* 1472 */             System.arraycopy(vm.getVmName().getBytes("ISO-8859-1"), 0, vmHeader, 2, vm.getVmName().length());
/* 1473 */             tmp4 = Functions.get4ByteArrayFromInt(vm.getVmLength());
/* 1474 */             vmHeader[14] = tmp4[3];
/* 1475 */             vmHeader[15] = tmp4[2];
/* 1476 */             vmHeader[16] = tmp4[1];
/* 1477 */             vmHeader[17] = tmp4[0];
/* 1478 */             tmp4 = Functions.get4ByteArrayFromInt(vm.getVmCRC32());
/* 1479 */             vmHeader[18] = tmp4[3];
/* 1480 */             vmHeader[19] = tmp4[2];
/* 1481 */             vmHeader[20] = tmp4[1];
/* 1482 */             vmHeader[21] = tmp4[0];
/* 1483 */             fc.write(vmHeader);
/* 1484 */             cfgLen += vmHeader.length;
/* 1485 */             fc.seek(cfgLen);
/* 1486 */             fc.write(content);
/* 1487 */             cfgLen += content.length;
/* 1488 */             fc.seek(cfgLen);
/*      */           } 
/*      */         } 
/*      */       } finally {
/*      */ 
/*      */         
/*      */         try {
/* 1495 */           fc.close();
/* 1496 */         } catch (IOException iOException) {}
/*      */       } 
/*      */ 
/*      */       
/* 1500 */       GriffonDBManager.updateCommandFileData(idCommand, new FileInputStream(file));
/* 1501 */       if (file.exists()) {
/* 1502 */         file.delete();
/*      */       }
/*      */     } else {
/* 1505 */       GriffonDBManager.updateCommandFileData(idCommand, new ByteArrayInputStream(b));
/*      */     } 
/*      */   }
/*      */   
/*      */   private static List<Integer> buildEmptyCRCList(int filesCount) {
/* 1510 */     List<Integer> deviceCRC32 = new ArrayList<>(filesCount);
/* 1511 */     for (int i = 0; i < filesCount; i++) {
/* 1512 */       deviceCRC32.add(Integer.valueOf(-1));
/*      */     }
/* 1514 */     return deviceCRC32;
/*      */   }
/*      */   
/*      */   public static List<Integer> buildCRC32FromReceivedBuffer(short productID, List<Integer> deviceCRC32, byte[] buffer, boolean isFirst, int fileCount) {
/*      */     int noOfFiles;
/* 1519 */     if (isFirst) {
/* 1520 */       noOfFiles = buffer[0] & 0xFF;
/*      */     } else {
/* 1522 */       noOfFiles = fileCount;
/*      */     } 
/*      */     
/* 1525 */     byte[] tmp4 = new byte[4];
/* 1526 */     deviceCRC32 = buildEmptyCRCList((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40);
/* 1527 */     for (int i = isFirst ? 1 : 0; i < noOfFiles * 5; i += 5) {
/* 1528 */       int fIdx = buffer[i];
/* 1529 */       if (fIdx > 0) {
/* 1530 */         System.arraycopy(buffer, i + 1, tmp4, 0, 4);
/* 1531 */         deviceCRC32.set(fIdx - 1, Integer.valueOf(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4))));
/*      */       } 
/*      */     } 
/* 1534 */     return deviceCRC32;
/*      */   }
/*      */   
/*      */   public static byte[] prepareFileDataByCRC32Mismatch(short productID, List<Integer> deviceCRC32, ModuleCFG mCFG, boolean readMode) {
/* 1538 */     List<Integer> requiredFileIDs = new ArrayList<>();
/*      */     
/* 1540 */     if (mCFG == null && readMode) {
/* 1541 */       for (int i = 0; i < ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); i++) {
/* 1542 */         requiredFileIDs.add(Integer.valueOf(i + 1));
/*      */       }
/* 1544 */     } else if (readMode) {
/* 1545 */       if (mCFG.getCrc32List() == null) {
/* 1546 */         for (int i = 0; i < ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); i++) {
/* 1547 */           requiredFileIDs.add(Integer.valueOf(i + 1));
/*      */         }
/*      */       } else {
/* 1550 */         for (int i = 0; i < ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); i++) {
/* 1551 */           if (((Integer)deviceCRC32.get(i)).intValue() != ((Integer)mCFG.getCrc32List().get(i)).intValue()) {
/* 1552 */             requiredFileIDs.add(Integer.valueOf(i + 1));
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } else {
/* 1557 */       for (int i = 0; i < ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); i++) {
/* 1558 */         if (((Integer)deviceCRC32.get(i)).intValue() != ((Integer)mCFG.getCrc32List().get(i)).intValue()) {
/* 1559 */           requiredFileIDs.add(Integer.valueOf(i + 1));
/*      */         }
/*      */       } 
/*      */     } 
/* 1563 */     int rFCount = requiredFileIDs.size();
/* 1564 */     if (rFCount <= ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40)) {
/* 1565 */       byte[] rFiles = new byte[rFCount + 1];
/* 1566 */       rFiles[0] = (byte)rFCount;
/* 1567 */       byte b = 1;
/* 1568 */       for (Integer rFID : requiredFileIDs) {
/* 1569 */         rFiles[b++] = rFID.byteValue();
/*      */       }
/* 1571 */       return rFiles;
/*      */     } 
/* 1573 */     return null;
/*      */   }
/*      */   
/*      */   public static ModuleCFG getModuleCFGFromUploadedFile(short productID, int idModule, byte[] fileContent) throws Exception {
/* 1577 */     byte[] tm, encBlock = new byte[16];
/*      */ 
/*      */     
/* 1580 */     System.arraycopy(fileContent, 0, encBlock, 0, 16);
/* 1581 */     byte[] decBlock = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1582 */     System.arraycopy(decBlock, 0, fileContent, 0, 16);
/*      */     
/* 1584 */     byte[] tmp4 = new byte[4];
/* 1585 */     tmp4[3] = fileContent[2];
/* 1586 */     tmp4[2] = fileContent[3];
/* 1587 */     tmp4[1] = fileContent[4];
/* 1588 */     tmp4[0] = fileContent[5];
/* 1589 */     int upFileLen = Functions.getIntFrom4ByteArray(tmp4);
/*      */     
/* 1591 */     int lPad = (upFileLen + 10) % 16;
/* 1592 */     if (lPad > 0) {
/* 1593 */       lPad = 16 - lPad;
/*      */     }
/*      */     
/* 1596 */     int tmpUPFileLen = upFileLen + lPad + 10;
/* 1597 */     if (fileContent.length > tmpUPFileLen) {
/* 1598 */       byte[] vmc = new byte[fileContent.length - tmpUPFileLen];
/* 1599 */       System.arraycopy(fileContent, tmpUPFileLen, vmc, 0, fileContent.length - tmpUPFileLen);
/* 1600 */       Functions.writeByteArrayToFile("GRCP_VMC_" + idModule, vmc);
/* 1601 */       tm = new byte[tmpUPFileLen];
/* 1602 */       System.arraycopy(fileContent, 0, tm, 0, tmpUPFileLen);
/*      */     } else {
/* 1604 */       tm = fileContent;
/*      */     } 
/*      */     
/* 1607 */     if (tm.length >= 16 && tmpUPFileLen % 16 == 0) {
/* 1608 */       for (int i = 16; i < tmpUPFileLen; ) {
/* 1609 */         System.arraycopy(tm, i, encBlock, 0, 16);
/* 1610 */         decBlock = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1611 */         System.arraycopy(decBlock, 0, tm, i, 16);
/* 1612 */         i += 16;
/*      */       } 
/*      */       
/* 1615 */       byte[] fileData = new byte[upFileLen];
/* 1616 */       System.arraycopy(tm, 10, fileData, 0, upFileLen);
/* 1617 */       ModuleCFG uploadedMCFG = ConfigFileParser.arrangeCFGFile(productID, fileData, upFileLen);
/* 1618 */       tmp4[3] = tm[6];
/* 1619 */       tmp4[2] = tm[7];
/* 1620 */       tmp4[1] = tm[8];
/* 1621 */       tmp4[0] = tm[9];
/*      */       
/* 1623 */       uploadedMCFG.setCrc32(Functions.getIntFrom4ByteArray(tmp4));
/* 1624 */       return uploadedMCFG;
/*      */     } 
/* 1626 */     return null;
/*      */   }
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
/*      */   public static List<VoiceMessage> getMismatchedVoiceMessageList(int idModule, ModuleCFG mCFG) throws Exception {
/* 1645 */     byte[] tmp4 = new byte[4];
/* 1646 */     byte[] tmp2 = new byte[2];
/*      */     
/* 1648 */     List<VoiceMessage> reqvmList = null;
/*      */     
/* 1650 */     System.arraycopy(mCFG.getIndexStructure(), 80, tmp4, 0, 4);
/* 1651 */     int start = Functions.getIntFrom4ByteArray(tmp4);
/* 1652 */     System.arraycopy(mCFG.getIndexStructure(), 84, tmp4, 0, 4);
/* 1653 */     int end = Functions.getIntFrom4ByteArray(tmp4);
/*      */     
/* 1655 */     if (end - start > 3) {
/* 1656 */       byte[] voiceData = new byte[end - start];
/*      */       
/* 1658 */       System.arraycopy(mCFG.getFileData(), start, voiceData, 0, end - start);
/* 1659 */       if (voiceData[0] == 34) {
/* 1660 */         tmp2[0] = voiceData[2];
/* 1661 */         tmp2[1] = voiceData[1];
/* 1662 */         int noOfAudioFiles = Functions.getIntFrom2ByteArray(tmp2);
/* 1663 */         List<VoiceMessage> vmList = new ArrayList<>(noOfAudioFiles);
/*      */         
/* 1665 */         int counter = 0;
/* 1666 */         for (int i = 3; counter++ < noOfAudioFiles && i < voiceData.length; ) {
/* 1667 */           tmp2[0] = voiceData[i + 1];
/* 1668 */           tmp2[1] = voiceData[i];
/* 1669 */           int idx = Functions.getIntFrom2ByteArray(tmp2);
/* 1670 */           VoiceMessage vm = new VoiceMessage();
/* 1671 */           vm.setVmIndex(idx);
/* 1672 */           i += 2;
/* 1673 */           System.arraycopy(voiceData, i, tmp4, 0, 4);
/*      */           
/* 1675 */           vm.setVmLength(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4)));
/* 1676 */           i += 4;
/* 1677 */           System.arraycopy(voiceData, i, tmp4, 0, 4);
/* 1678 */           vm.setVmCRC32(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4)));
/* 1679 */           i += 4;
/* 1680 */           byte[] audioFName = new byte[12];
/* 1681 */           System.arraycopy(voiceData, i, audioFName, 0, 12);
/* 1682 */           vm.setVmName((new String(audioFName, "ISO-8859-1")).trim());
/* 1683 */           i += 12;
/* 1684 */           vmList.add(vm);
/*      */         } 
/* 1686 */         reqvmList = new ArrayList<>();
/* 1687 */         List<VoiceMessage> dbVMList = GriffonDBManager.getVoiceMessageInfoByIdModule(idModule);
/* 1688 */         for (VoiceMessage vmm : vmList) {
/* 1689 */           if (!dbVMList.contains(vmm)) {
/* 1690 */             reqvmList.add(vmm);
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/* 1695 */     return reqvmList;
/*      */   }
/*      */   
/*      */   public static boolean pushAppDataReceived(int idClient) throws UnsupportedEncodingException, IOException {
/* 1699 */     if (TblActiveGriffonMobileConnections.getInstance().containsKey(Integer.valueOf(idClient))) {
/* 1700 */       if (TCPMessageServer.mobileAppDataUpdater != null && TCPMessageServer.mobileAppDataUpdater.clientSocket != null) {
/* 1701 */         String ic = String.valueOf(idClient);
/* 1702 */         byte[] data = new byte[4 + ic.length()];
/* 1703 */         data[0] = -107;
/* 1704 */         data[1] = (byte)ic.length();
/* 1705 */         System.arraycopy(ic.getBytes("ASCII"), 0, data, 2, ic.length());
/* 1706 */         int crcCalc = CRC16.calculate(data, 0, ic.length() + 2, 65535);
/* 1707 */         data[ic.length() + 2] = (byte)(crcCalc & 0xFF);
/* 1708 */         data[ic.length() + 2 + 1] = (byte)((crcCalc & 0xFF00) / 256);
/* 1709 */         TCPMessageServer.mobileAppDataUpdater.sendData(data);
/*      */       } 
/* 1711 */       return true;
/*      */     } 
/* 1713 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void updateCommandFailureStatus2App(int idClient, SP_024DataHolder sp24DH) {
/*      */     String[] cData;
/* 1725 */     boolean updateRequired = false;
/* 1726 */     int uIndex = 0;
/* 1727 */     int operation = 0;
/* 1728 */     switch (sp24DH.getCommand_Type()) {
/*      */       case 32780:
/* 1730 */         cData = sp24DH.getCommandData().split(";");
/* 1731 */         if (cData != null && cData.length == 3) {
/* 1732 */           operation = Byte.parseByte(cData[1]);
/* 1733 */           uIndex = Integer.parseInt(cData[2]);
/* 1734 */           if (uIndex > 0 && uIndex <= 999) {
/* 1735 */             updateRequired = true;
/*      */           }
/*      */         } 
/*      */         break;
/*      */     } 
/*      */     
/* 1741 */     if (updateRequired && 
/* 1742 */       TblActiveGriffonMobileConnections.getInstance().containsKey(Integer.valueOf(idClient)) && 
/* 1743 */       TCPMessageServer.mobileAppDataUpdater != null && TCPMessageServer.mobileAppDataUpdater.clientSocket != null) {
/* 1744 */       String ic = String.valueOf(idClient);
/* 1745 */       byte[] data = new byte[7 + ic.length()];
/* 1746 */       data[0] = -106;
/* 1747 */       data[1] = (byte)(3 + ic.length());
/* 1748 */       data[2] = (byte)operation;
/* 1749 */       byte[] tmp = Functions.get2ByteArrayFromInt(uIndex);
/* 1750 */       data[3] = tmp[1];
/* 1751 */       data[4] = tmp[0];
/* 1752 */       System.arraycopy(ic.getBytes(), 0, data, 5, ic.length());
/* 1753 */       int crcCalc = CRC16.calculate(data, 0, ic.length() + 5, 65535);
/* 1754 */       data[ic.length() + 5] = (byte)(crcCalc & 0xFF);
/* 1755 */       data[ic.length() + 5 + 1] = (byte)((crcCalc & 0xFF00) / 256);
/*      */       try {
/* 1757 */         TCPMessageServer.mobileAppDataUpdater.sendData(data);
/* 1758 */       } catch (IOException ex) {
/* 1759 */         Logger.getLogger(GriffonHandlerHelper.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\GriffonHandlerHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */