/*     */ package com.zeusServer.mercurius;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*     */ import com.zeusServer.dto.GeofenceBean;
/*     */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.InfoModule;
/*     */ import java.sql.SQLException;
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
/*     */ public class MercuriusAVLHandlerHelper
/*     */ {
/*  28 */   public static int POLYGON_TYPE_LENGTH = 12;
/*  29 */   public static int CIRCLE_TYPE_LENGTH = 8;
/*  30 */   public static short CIRCLE_TYPE = 1;
/*  31 */   public static short POLYGON_TYPE = 2;
/*  32 */   public static short AVL_FILE_HEADER_SIZE = 12;
/*     */   
/*     */   public static void parseGeofenceData(byte[] geofenceData, int flen, int idModule, int idClient, int geoCRC32) throws SQLException, InterruptedException, Exception {
/*  35 */     byte[] tmp2 = new byte[2];
/*     */     
/*  37 */     int idx = 16;
/*  38 */     if ((geofenceData[idx++] & 0xFF) == 12 && (
/*  39 */       geofenceData[idx++] & 0xFF) == 40) {
/*  40 */       tmp2[1] = geofenceData[idx++];
/*  41 */       tmp2[0] = geofenceData[idx++];
/*  42 */       int cnt = Functions.getIntFrom2ByteArray(tmp2);
/*  43 */       if (cnt > 0) {
/*  44 */         List<GeofenceBean> gList = new ArrayList<>(cnt);
/*  45 */         byte[] tmp4 = new byte[4];
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  52 */         int gCnt = 0;
/*  53 */         while (gCnt++ < cnt) {
/*  54 */           GeofenceBean gBean = new GeofenceBean();
/*  55 */           tmp2[1] = geofenceData[idx++];
/*  56 */           tmp2[0] = geofenceData[idx++];
/*  57 */           gBean.setGeofenceIndex(Functions.getIntFrom2ByteArray(tmp2));
/*  58 */           tmp2[1] = geofenceData[idx++];
/*  59 */           tmp2[0] = geofenceData[idx++];
/*  60 */           int noGP = Functions.getIntFrom2ByteArray(tmp2);
/*  61 */           List<Float> latList = new ArrayList<>(noGP);
/*  62 */           List<Float> longList = new ArrayList<>(noGP);
/*     */           
/*  64 */           if (noGP == 1) {
/*  65 */             gBean.setType(CIRCLE_TYPE);
/*     */             
/*  67 */             tmp4[3] = geofenceData[idx++];
/*  68 */             tmp4[2] = geofenceData[idx++];
/*  69 */             tmp4[1] = geofenceData[idx++];
/*  70 */             tmp4[0] = geofenceData[idx++];
/*     */             
/*  72 */             long dValue = Functions.getIntFrom4ByteArray(tmp4);
/*  73 */             latList.add(Float.valueOf((float)(dValue / Math.pow(10.0D, 7.0D))));
/*     */             
/*  75 */             tmp4[3] = geofenceData[idx++];
/*  76 */             tmp4[2] = geofenceData[idx++];
/*  77 */             tmp4[1] = geofenceData[idx++];
/*  78 */             tmp4[0] = geofenceData[idx++];
/*     */             
/*  80 */             dValue = Functions.getIntFrom4ByteArray(tmp4);
/*  81 */             longList.add(Float.valueOf((float)(dValue / Math.pow(10.0D, 7.0D))));
/*     */             
/*  83 */             tmp4[3] = geofenceData[idx++];
/*  84 */             tmp4[2] = geofenceData[idx++];
/*  85 */             tmp4[1] = geofenceData[idx++];
/*  86 */             tmp4[0] = geofenceData[idx++];
/*  87 */             dValue = Functions.getIntFrom4ByteArray(tmp4);
/*  88 */             gBean.setRadius((float)dValue);
/*     */           } else {
/*  90 */             int pcnt = 0;
/*  91 */             while (pcnt++ < noGP) {
/*  92 */               gBean.setType(POLYGON_TYPE);
/*  93 */               tmp4[3] = geofenceData[idx++];
/*  94 */               tmp4[2] = geofenceData[idx++];
/*  95 */               tmp4[1] = geofenceData[idx++];
/*  96 */               tmp4[0] = geofenceData[idx++];
/*     */               
/*  98 */               long dValue = Functions.getIntFrom4ByteArray(tmp4);
/*  99 */               latList.add(Float.valueOf((float)(dValue / Math.pow(10.0D, 7.0D))));
/*     */               
/* 101 */               tmp4[3] = geofenceData[idx++];
/* 102 */               tmp4[2] = geofenceData[idx++];
/* 103 */               tmp4[1] = geofenceData[idx++];
/* 104 */               tmp4[0] = geofenceData[idx++];
/*     */               
/* 106 */               dValue = Functions.getIntFrom4ByteArray(tmp4);
/* 107 */               longList.add(Float.valueOf((float)(dValue / Math.pow(10.0D, 7.0D))));
/*     */               
/* 109 */               idx += 4;
/*     */             } 
/*     */           } 
/*     */           
/* 113 */           idx += 3;
/* 114 */           byte[] tmp = new byte[20];
/* 115 */           System.arraycopy(geofenceData, idx, tmp, 0, 20);
/* 116 */           idx += 21;
/* 117 */           String name = (new String(tmp, "ISO-8859-1")).trim();
/* 118 */           gBean.setGeofenceName((name == null || name.isEmpty()) ? ("Geofence #" + gBean.getGeofenceIndex()) : name);
/* 119 */           gBean.setLatList(latList);
/* 120 */           gBean.setLongList(longList);
/* 121 */           gList.add(gBean);
/*     */         } 
/* 123 */         MercuriusDBManager.saveGeofenceData(idModule, idClient, geoCRC32, gList);
/*     */       } else {
/* 125 */         MercuriusDBManager.updateGeofenceCRC32(idModule, geoCRC32);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void endCommand(int id_Module, short exec_Retries) throws SQLException, InterruptedException, Exception {
/* 132 */     MercuriusDBManager.executeSP_027(id_Module, (short)(exec_Retries + 1));
/*     */   }
/*     */   
/*     */   public static void updateGPSFWVersion(int id_Module, String custVersion, String sirfVersion) throws SQLException, InterruptedException, Exception {
/* 136 */     MercuriusDBManager.updateGPSFWVersion(id_Module, custVersion, sirfVersion);
/*     */   }
/*     */   
/*     */   public static void registerFailureSendCommand(String sn, String msg, short exec_Retries, int id_Command) throws SQLException, InterruptedException {
/* 140 */     if (msg != null && msg.length() > 0) {
/* 141 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, msg, Enums.EnumMessagePriority.HIGH, sn, null);
/*     */     }
/* 143 */     if (exec_Retries + 1 >= 3) {
/* 144 */       MercuriusDBManager.executeSP_025(id_Command, (short)(exec_Retries + 1));
/*     */     } else {
/* 146 */       MercuriusDBManager.executeSP_026(id_Command, (short)(exec_Retries + 1));
/*     */     } 
/*     */   }
/*     */   
/*     */   public static String getFileNameByCommandData(String cData) {
/* 151 */     String fileName = "";
/* 152 */     switch (Integer.parseInt(cData)) {
/*     */       case 1:
/* 154 */         fileName = "Configuration File";
/*     */         break;
/*     */       case 2:
/* 157 */         fileName = "Firmware File";
/*     */         break;
/*     */       case 3:
/* 160 */         fileName = "Modem Firmware File";
/*     */         break;
/*     */     } 
/* 163 */     return fileName;
/*     */   }
/*     */   
/*     */   public static void updateLastCommunicationModuleData(String sn, short lastCommIface, short currentSIM) throws SQLException, InterruptedException {
/* 167 */     MercuriusDBManager.executeSP_028(((InfoModule)TblMercuriusActiveConnections.getInstance().get(sn)).idModule, lastCommIface, currentSIM);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\MercuriusAVLHandlerHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */