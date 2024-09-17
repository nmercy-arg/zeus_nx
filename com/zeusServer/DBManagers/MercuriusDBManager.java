/*     */ package com.zeusServer.DBManagers;
/*     */ 
/*     */ import com.zeus.mercuriusAVL.derby.beans.AudioNJSFileInfo;
/*     */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*     */ import com.zeusServer.dao.mercurius.MercuriusQueryHandler;
/*     */ import com.zeusServer.dao.mercurius.MercuriusSPHandler01_10;
/*     */ import com.zeusServer.dao.mercurius.MercuriusSPHandler11_20;
/*     */ import com.zeusServer.dao.mercurius.MercuriusSPHandler21_30;
/*     */ import com.zeusServer.dao.mercurius.MercuriusSPHandler31_40;
/*     */ import com.zeusServer.dto.GeofenceBean;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.sql.SQLException;
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
/*     */ public class MercuriusDBManager
/*     */ {
/*     */   public static void executeSP_001(int idClient, int occuranceType) throws SQLException, InterruptedException {
/*  45 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  47 */         MercuriusSPHandler01_10.executeSP_001(idClient, occuranceType, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/*  49 */       } catch (SQLException ex) {
/*  50 */         if (retries == 3) {
/*  51 */           throw ex;
/*     */         }
/*  53 */         GenericDBManager.checkDerbyService();
/*  54 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static List<Integer> executeSP_006() throws SQLException, InterruptedException {
/*  68 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  70 */         return MercuriusSPHandler01_10.executeSP_006(GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*  71 */       } catch (SQLException ex) {
/*  72 */         if (retries == 3) {
/*  73 */           throw ex;
/*     */         }
/*  75 */         GenericDBManager.checkDerbyService();
/*  76 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/*  80 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static List<SP_007DataHolder> executeSP_007() throws SQLException, InterruptedException {
/*  91 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  93 */         return MercuriusSPHandler01_10.executeSP_007(GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*  94 */       } catch (SQLException ex) {
/*  95 */         if (retries == 3) {
/*  96 */           throw ex;
/*     */         }
/*  98 */         GenericDBManager.checkDerbyService();
/*  99 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 103 */     return null;
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
/*     */   public static void executeSP_009(int idClient, short occuranceType) throws SQLException, InterruptedException {
/* 115 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 117 */         MercuriusSPHandler01_10.executeSP_009(idClient, occuranceType, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 119 */       } catch (SQLException ex) {
/* 120 */         if (retries == 3) {
/* 121 */           throw ex;
/*     */         }
/* 123 */         GenericDBManager.checkDerbyService();
/* 124 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static List<SP_011DataHolder> executeSP_011() throws SQLException, InterruptedException {
/* 138 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 140 */         return MercuriusSPHandler11_20.executeSP_011(GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 141 */       } catch (SQLException ex) {
/* 142 */         if (retries == 3) {
/* 143 */           throw ex;
/*     */         }
/* 145 */         GenericDBManager.checkDerbyService();
/* 146 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 150 */     return null;
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
/*     */   public static void executeSP_014(int idModule, int rcvrGroup, short protocol, byte[] eventData) throws SQLException, InterruptedException {
/* 164 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 166 */         MercuriusSPHandler11_20.executeSP_014(idModule, rcvrGroup, protocol, eventData, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 168 */       } catch (SQLException ex) {
/* 169 */         if (retries == 3) {
/* 170 */           throw ex;
/*     */         }
/* 172 */         GenericDBManager.checkDerbyService();
/* 173 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static void executeSP_017(int idOccurance) throws SQLException, InterruptedException {
/* 187 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 189 */         MercuriusSPHandler11_20.executeSP_017(idOccurance, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 191 */       } catch (SQLException ex) {
/* 192 */         if (retries == 3) {
/* 193 */           throw ex;
/*     */         }
/* 195 */         GenericDBManager.checkDerbyService();
/* 196 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static void executeSP_020(int idOccurance) throws SQLException, InterruptedException {
/* 210 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 212 */         MercuriusSPHandler11_20.executeSP_020(idOccurance, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 214 */       } catch (SQLException ex) {
/* 215 */         if (retries == 3) {
/* 216 */           throw ex;
/*     */         }
/* 218 */         GenericDBManager.checkDerbyService();
/* 219 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static List<SP_024DataHolder> executeSP_024(int idModule) throws SQLException, InterruptedException, IOException {
/* 235 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 237 */         return MercuriusSPHandler21_30.executeSP_024(idModule, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 238 */       } catch (IOException|SQLException ex) {
/* 239 */         if (retries == 3) {
/* 240 */           throw ex;
/*     */         }
/* 242 */         GenericDBManager.checkDerbyService();
/* 243 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 247 */     return null;
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
/*     */   public static void executeSP_025(int id_Command, short num_Exec_Retries) throws SQLException, InterruptedException {
/* 259 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 261 */         MercuriusSPHandler21_30.executeSP_025(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 263 */       } catch (SQLException ex) {
/* 264 */         if (retries == 3) {
/* 265 */           throw ex;
/*     */         }
/* 267 */         GenericDBManager.checkDerbyService();
/* 268 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static void executeSP_026(int id_Command, short num_Exec_Retries) throws SQLException, InterruptedException {
/* 283 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 285 */         MercuriusSPHandler21_30.executeSP_026(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 287 */       } catch (SQLException ex) {
/* 288 */         if (retries == 3) {
/* 289 */           throw ex;
/*     */         }
/* 291 */         GenericDBManager.checkDerbyService();
/* 292 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static void executeSP_027(int id_Command, short num_Exec_Retries) throws SQLException, InterruptedException {
/* 307 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 309 */         MercuriusSPHandler21_30.executeSP_027(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 311 */       } catch (SQLException ex) {
/* 312 */         if (retries == 3) {
/* 313 */           throw ex;
/*     */         }
/* 315 */         GenericDBManager.checkDerbyService();
/* 316 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static void executeSP_028(int idModule, int lastCommInterface, int currentSIM) throws SQLException, InterruptedException {
/* 332 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 334 */         MercuriusSPHandler21_30.executeSP_028(idModule, lastCommInterface, currentSIM, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 336 */       } catch (SQLException ex) {
/* 337 */         if (retries == 3) {
/* 338 */           throw ex;
/*     */         }
/* 340 */         GenericDBManager.checkDerbyService();
/* 341 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static List<String> executeSP_031(int idClient) throws SQLException, InterruptedException {
/* 356 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 358 */         return MercuriusSPHandler31_40.executeSP_031(idClient, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 359 */       } catch (SQLException ex) {
/* 360 */         if (retries == 3) {
/* 361 */           throw ex;
/*     */         }
/* 363 */         GenericDBManager.checkDerbyService();
/* 364 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 368 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static MercuriusAVLModule executeSPM_001(MercuriusAVLModule gModule) throws SQLException, InterruptedException {
/* 373 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 375 */         return MercuriusQueryHandler.executeSPM_001(gModule, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 376 */       } catch (SQLException ex) {
/* 377 */         if (retries == 3) {
/* 378 */           throw ex;
/*     */         }
/* 380 */         GenericDBManager.checkDerbyService();
/* 381 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 385 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSPM_002(List<MercuriusAVLModule> avlList) throws SQLException, InterruptedException {
/* 390 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 392 */         MercuriusQueryHandler.executeSPM_002(avlList, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 394 */       } catch (SQLException ex) {
/* 395 */         if (retries == 3) {
/* 396 */           throw ex;
/*     */         }
/* 398 */         GenericDBManager.checkDerbyService();
/* 399 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static List<PendingDataHolder> getAllPendingAlive() throws SQLException, InterruptedException {
/* 413 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 415 */         return MercuriusQueryHandler.getAllPendingAlive(GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 416 */       } catch (SQLException ex) {
/* 417 */         if (retries == 3) {
/* 418 */           throw ex;
/*     */         }
/* 420 */         GenericDBManager.checkDerbyService();
/* 421 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 425 */     return null;
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
/*     */   public static boolean isCommandCancelled(int commandId) throws SQLException, InterruptedException {
/* 439 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 441 */         return MercuriusQueryHandler.isCommandCancelled(commandId, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 442 */       } catch (SQLException ex) {
/* 443 */         if (retries == 3) {
/* 444 */           throw ex;
/*     */         }
/* 446 */         GenericDBManager.checkDerbyService();
/* 447 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 451 */     return false;
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
/*     */   public static void updateCommandStatus(int commandId) throws SQLException, InterruptedException {
/* 463 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 465 */         MercuriusQueryHandler.updateCommandStatus(commandId, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 467 */       } catch (SQLException ex) {
/* 468 */         if (retries == 3) {
/* 469 */           throw ex;
/*     */         }
/* 471 */         GenericDBManager.checkDerbyService();
/* 472 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static void updateCommandData(int idCommand, InputStream commandFileData) throws SQLException, InterruptedException {
/* 487 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 489 */         MercuriusQueryHandler.updateCommandData(idCommand, commandFileData, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 491 */       } catch (SQLException ex) {
/* 492 */         if (retries == 3) {
/* 493 */           throw ex;
/*     */         }
/* 495 */         GenericDBManager.checkDerbyService();
/* 496 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
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
/*     */   public static List<EventDataHolder> getNonProcessedEvents() throws SQLException, InterruptedException {
/* 510 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 512 */         return MercuriusQueryHandler.getNonProcessedEvents(GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 513 */       } catch (SQLException ex) {
/* 514 */         if (retries == 3) {
/* 515 */           throw ex;
/*     */         }
/* 517 */         GenericDBManager.checkDerbyService();
/* 518 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 522 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> getGroupIds() throws SQLException, InterruptedException {
/* 527 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 529 */         return MercuriusQueryHandler.getGroupIds(GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 530 */       } catch (SQLException ex) {
/* 531 */         if (retries == 3) {
/* 532 */           throw ex;
/*     */         }
/* 534 */         GenericDBManager.checkDerbyService();
/* 535 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 539 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void saveVoiceMessage(byte[] fileContent, int fileLength, String fileName, int crc32, int dir) throws SQLException, InterruptedException {
/* 544 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 546 */         MercuriusQueryHandler.saveVoiceMessage(fileContent, fileLength, fileName, crc32, dir, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 548 */       } catch (SQLException ex) {
/* 549 */         if (retries == 3) {
/* 550 */           throw ex;
/*     */         }
/* 552 */         GenericDBManager.checkDerbyService();
/* 553 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static List<AudioNJSFileInfo> getMissingVoiceMessagesInfo(List<AudioNJSFileInfo> vmList) throws SQLException, InterruptedException {
/* 561 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 563 */         return MercuriusQueryHandler.getMissingVoiceMessagesInfo(vmList, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 564 */       } catch (SQLException ex) {
/* 565 */         if (retries == 3) {
/* 566 */           throw ex;
/*     */         }
/* 568 */         GenericDBManager.checkDerbyService();
/* 569 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 573 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] getVoiceMessagesByName(int fileLength, String vmName, int vmCrc32, int dir) throws SQLException, InterruptedException {
/* 578 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 580 */         return MercuriusQueryHandler.getVoiceMessagesByName(fileLength, vmName, vmCrc32, dir, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 581 */       } catch (SQLException ex) {
/* 582 */         if (retries == 3) {
/* 583 */           throw ex;
/*     */         }
/* 585 */         GenericDBManager.checkDerbyService();
/* 586 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 590 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void disableCommunicationLog(int idModule) throws SQLException, InterruptedException {
/* 595 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 597 */         MercuriusQueryHandler.disableCommunicationLog(idModule, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 599 */       } catch (SQLException ex) {
/* 600 */         if (retries == 3) {
/* 601 */           throw ex;
/*     */         }
/* 603 */         GenericDBManager.checkDerbyService();
/* 604 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void saveGeofenceData(int idModule, int idClient, int geoCRC32, List<GeofenceBean> gList) throws SQLException, InterruptedException {
/* 612 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 614 */         MercuriusQueryHandler.saveGeofenceData(idModule, idClient, geoCRC32, gList, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 616 */       } catch (SQLException ex) {
/* 617 */         if (retries == 3) {
/* 618 */           throw ex;
/*     */         }
/* 620 */         GenericDBManager.checkDerbyService();
/* 621 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateGeofenceCRC32(int idModule, int geoCRC32) throws SQLException, InterruptedException {
/* 629 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 631 */         MercuriusQueryHandler.updateGeofenceCRC32(idModule, geoCRC32, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 633 */       } catch (SQLException ex) {
/* 634 */         if (retries == 3) {
/* 635 */           throw ex;
/*     */         }
/* 637 */         GenericDBManager.checkDerbyService();
/* 638 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateGPSFWVersion(int idModule, String custVersion, String sirfVersion) throws SQLException, InterruptedException {
/* 646 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 648 */         MercuriusQueryHandler.updateGPSFWVersion(idModule, custVersion, sirfVersion, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/*     */         break;
/* 650 */       } catch (SQLException ex) {
/* 651 */         if (retries == 3) {
/* 652 */           throw ex;
/*     */         }
/* 654 */         GenericDBManager.checkDerbyService();
/* 655 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getSnFromMercuriusModule(int idClient) throws SQLException, InterruptedException {
/* 663 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 665 */         return MercuriusQueryHandler.getSnFromMercuriusModule(idClient, GenericDBManager.getConnectionBySchemaName("MERCURIUS", false));
/* 666 */       } catch (SQLException ex) {
/* 667 */         if (retries == 3) {
/* 668 */           throw ex;
/*     */         }
/* 670 */         GenericDBManager.checkDerbyService();
/* 671 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 675 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBManagers\MercuriusDBManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */