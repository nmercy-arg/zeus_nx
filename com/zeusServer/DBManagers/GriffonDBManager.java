/*     */ package com.zeusServer.DBManagers;
/*     */ 
/*     */ import com.zeusServer.dao.griffon.GriffonQueryHandler;
/*     */ import com.zeusServer.dao.griffon.GriffonSPGHandler01_10;
/*     */ import com.zeusServer.dao.griffon.GriffonSPHandler01_10;
/*     */ import com.zeusServer.dao.griffon.GriffonSPHandler11_20;
/*     */ import com.zeusServer.dao.griffon.GriffonSPHandler21_30;
/*     */ import com.zeusServer.dao.griffon.GriffonSPHandler31_40;
/*     */ import com.zeusServer.dto.FAI_Validation;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import com.zeuscc.griffon.derby.beans.AudioFileDetails;
/*     */ import com.zeuscc.griffon.derby.beans.ExpansionModule;
/*     */ import com.zeuscc.griffon.derby.beans.GriffonModule;
/*     */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
/*     */ import com.zeuscc.griffon.derby.beans.VoiceMessage;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.sql.SQLException;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GriffonDBManager
/*     */ {
/*     */   public static void executeSP_001(int idClient, int occuranceType) throws SQLException, InterruptedException {
/*  50 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  52 */         GriffonSPHandler01_10.executeSP_001(idClient, occuranceType, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/*  54 */       } catch (SQLException ex) {
/*  55 */         if (retries == 3) {
/*  56 */           throw ex;
/*     */         }
/*  58 */         GenericDBManager.checkDerbyService();
/*  59 */         Thread.sleep(5000L);
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
/*  73 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  75 */         return GriffonSPHandler01_10.executeSP_006(GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*  76 */       } catch (SQLException ex) {
/*  77 */         if (retries == 3) {
/*  78 */           throw ex;
/*     */         }
/*  80 */         GenericDBManager.checkDerbyService();
/*  81 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/*  85 */     return null;
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
/*  96 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  98 */         return GriffonSPHandler01_10.executeSP_007(GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*  99 */       } catch (SQLException ex) {
/* 100 */         if (retries == 3) {
/* 101 */           throw ex;
/*     */         }
/* 103 */         GenericDBManager.checkDerbyService();
/* 104 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 108 */     return null;
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
/* 120 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 122 */         GriffonSPHandler01_10.executeSP_009(idClient, occuranceType, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 124 */       } catch (SQLException ex) {
/* 125 */         if (retries == 3) {
/* 126 */           throw ex;
/*     */         }
/* 128 */         GenericDBManager.checkDerbyService();
/* 129 */         Thread.sleep(5000L);
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
/* 143 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 145 */         return GriffonSPHandler11_20.executeSP_011(GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 146 */       } catch (SQLException ex) {
/* 147 */         if (retries == 3) {
/* 148 */           throw ex;
/*     */         }
/* 150 */         GenericDBManager.checkDerbyService();
/* 151 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 155 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void executeSP_017(int idOccurance) throws SQLException, InterruptedException {
/* 166 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 168 */         GriffonSPHandler11_20.executeSP_017(idOccurance, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 170 */       } catch (SQLException ex) {
/* 171 */         if (retries == 3) {
/* 172 */           throw ex;
/*     */         }
/* 174 */         GenericDBManager.checkDerbyService();
/* 175 */         Thread.sleep(5000L);
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
/* 189 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 191 */         GriffonSPHandler11_20.executeSP_020(idOccurance, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 193 */       } catch (SQLException ex) {
/* 194 */         if (retries == 3) {
/* 195 */           throw ex;
/*     */         }
/* 197 */         GenericDBManager.checkDerbyService();
/* 198 */         Thread.sleep(5000L);
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
/*     */   public static List<SP_024DataHolder> executeSP_024(int idModule) throws SQLException, IOException, InterruptedException {
/* 214 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 216 */         return GriffonSPHandler21_30.executeSP_024(idModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 217 */       } catch (IOException|SQLException ex) {
/* 218 */         if (retries == 3) {
/* 219 */           throw ex;
/*     */         }
/* 221 */         GenericDBManager.checkDerbyService();
/* 222 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 226 */     return null;
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
/* 238 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 240 */         GriffonSPHandler21_30.executeSP_025(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 242 */       } catch (SQLException ex) {
/* 243 */         if (retries == 3) {
/* 244 */           throw ex;
/*     */         }
/* 246 */         GenericDBManager.checkDerbyService();
/* 247 */         Thread.sleep(5000L);
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
/* 262 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 264 */         GriffonSPHandler21_30.executeSP_026(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 266 */       } catch (SQLException ex) {
/* 267 */         if (retries == 3) {
/* 268 */           throw ex;
/*     */         }
/* 270 */         GenericDBManager.checkDerbyService();
/* 271 */         Thread.sleep(5000L);
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
/* 286 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 288 */         GriffonSPHandler21_30.executeSP_027(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 290 */       } catch (SQLException ex) {
/* 291 */         if (retries == 3) {
/* 292 */           throw ex;
/*     */         }
/* 294 */         GenericDBManager.checkDerbyService();
/* 295 */         Thread.sleep(5000L);
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
/* 311 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 313 */         GriffonSPHandler21_30.executeSP_028(idModule, lastCommInterface, currentSIM, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 315 */       } catch (SQLException ex) {
/* 316 */         if (retries == 3) {
/* 317 */           throw ex;
/*     */         }
/* 319 */         GenericDBManager.checkDerbyService();
/* 320 */         Thread.sleep(5000L);
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
/* 335 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 337 */         return GriffonSPHandler31_40.executeSP_031(idClient, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 338 */       } catch (SQLException ex) {
/* 339 */         if (retries == 3) {
/* 340 */           throw ex;
/*     */         }
/* 342 */         GenericDBManager.checkDerbyService();
/* 343 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 347 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static GriffonModule executeSPG_001(GriffonModule gModule) throws SQLException, InterruptedException {
/* 352 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 354 */         return GriffonSPGHandler01_10.executeSPG_001(gModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 355 */       } catch (SQLException ex) {
/* 356 */         if (retries == 3) {
/* 357 */           throw ex;
/*     */         }
/* 359 */         GenericDBManager.checkDerbyService();
/* 360 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 364 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSPG_003(List<GriffonModule> gModuleList) throws SQLException, InterruptedException {
/* 369 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 371 */         GriffonSPGHandler01_10.executeSPG_003(gModuleList, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 373 */       } catch (SQLException ex) {
/* 374 */         if (retries == 3) {
/* 375 */           throw ex;
/*     */         }
/* 377 */         GenericDBManager.checkDerbyService();
/* 378 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void executeSPG_005(GriffonModule gModule) throws SQLException, InterruptedException {
/* 386 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 388 */         GriffonSPGHandler01_10.executeSPG_005(gModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 390 */       } catch (SQLException ex) {
/* 391 */         if (retries == 3) {
/* 392 */           throw ex;
/*     */         }
/* 394 */         GenericDBManager.checkDerbyService();
/* 395 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateGriffonModuleCfg(ModuleCFG moduleCFG) throws SQLException, InterruptedException {
/* 403 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 405 */         GriffonQueryHandler.updateGriffonModuleCfg(moduleCFG, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 407 */       } catch (SQLException ex) {
/* 408 */         if (retries == 3) {
/* 409 */           throw ex;
/*     */         }
/* 411 */         GenericDBManager.checkDerbyService();
/* 412 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static ModuleCFG readGriffonModuleCfg(int idModule) throws SQLException, InterruptedException {
/* 420 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 422 */         return GriffonQueryHandler.readGriffonModuleCfg(idModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 423 */       } catch (SQLException ex) {
/* 424 */         if (retries == 3) {
/* 425 */           throw ex;
/*     */         }
/* 427 */         GenericDBManager.checkDerbyService();
/* 428 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 432 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void saveParsedCFGData(GriffonModule gModule, FAI_Validation val) throws Exception {
/* 437 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 439 */         GriffonQueryHandler.saveParsedCFGData(gModule, val, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 441 */       } catch (Exception ex) {
/* 442 */         if (retries == 3) {
/* 443 */           throw ex;
/*     */         }
/* 445 */         GenericDBManager.checkDerbyService();
/* 446 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static List<VoiceMessage> getVoiceMessageInfoByIdModule(int idModule) throws SQLException, InterruptedException {
/* 454 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 456 */         return GriffonQueryHandler.getVoiceMessageInfoByIdModule(idModule, true, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 457 */       } catch (SQLException ex) {
/* 458 */         if (retries == 3) {
/* 459 */           throw ex;
/*     */         }
/* 461 */         GenericDBManager.checkDerbyService();
/* 462 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 466 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<VoiceMessage> getMissingVoiceMessagesInfo(List<VoiceMessage> vmList) throws SQLException, InterruptedException {
/* 471 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 473 */         return GriffonQueryHandler.getMissingVoiceMessagesInfo(vmList, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 474 */       } catch (SQLException ex) {
/* 475 */         if (retries == 3) {
/* 476 */           throw ex;
/*     */         }
/* 478 */         GenericDBManager.checkDerbyService();
/* 479 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 483 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] getVoiceMessagesByName(int fileLength, String vmName, int vmCrc32) throws SQLException, InterruptedException {
/* 488 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 490 */         return GriffonQueryHandler.getVoiceMessagesByName(fileLength, vmName, vmCrc32, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 491 */       } catch (SQLException ex) {
/* 492 */         if (retries == 3) {
/* 493 */           throw ex;
/*     */         }
/* 495 */         GenericDBManager.checkDerbyService();
/* 496 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 500 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void saveVoiceMessage(byte[] fileContent, int fileLength, String fileName, int crc32) throws SQLException, InterruptedException {
/* 505 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 507 */         GriffonQueryHandler.saveVoiceMessage(fileContent, fileLength, fileName, crc32, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 509 */       } catch (SQLException ex) {
/* 510 */         if (retries == 3) {
/* 511 */           throw ex;
/*     */         }
/* 513 */         GenericDBManager.checkDerbyService();
/* 514 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateEBFWVersionData(List<ExpansionModule> emFWList, List<ExpansionModule> kpLangFWList, int idClient, int idModule, int ebFWCRC32, boolean flag) throws SQLException, InterruptedException {
/* 522 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 524 */         GriffonQueryHandler.updateEBFWVersionData(emFWList, kpLangFWList, idClient, idModule, ebFWCRC32, flag, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 526 */       } catch (SQLException ex) {
/* 527 */         if (retries == 3) {
/* 528 */           throw ex;
/*     */         }
/* 530 */         GenericDBManager.checkDerbyService();
/* 531 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void updateRecordedFileLookupData(List<AudioFileDetails> afdList, int idClient, int idModule, int recAudioLookupCRC32) throws SQLException, InterruptedException {
/* 539 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 541 */         GriffonQueryHandler.updateRecordedFileLookupData(afdList, idClient, idModule, recAudioLookupCRC32, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 543 */       } catch (SQLException ex) {
/* 544 */         if (retries == 3) {
/* 545 */           throw ex;
/*     */         }
/* 547 */         GenericDBManager.checkDerbyService();
/* 548 */         Thread.sleep(5000L);
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
/* 562 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 564 */         return GriffonQueryHandler.getAllPendingAlive(GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 565 */       } catch (SQLException ex) {
/* 566 */         if (retries == 3) {
/* 567 */           throw ex;
/*     */         }
/* 569 */         GenericDBManager.checkDerbyService();
/* 570 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 574 */     return null;
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
/* 588 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 590 */         return GriffonQueryHandler.isCommandCancelled(commandId, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 591 */       } catch (SQLException ex) {
/* 592 */         if (retries == 3) {
/* 593 */           throw ex;
/*     */         }
/* 595 */         GenericDBManager.checkDerbyService();
/* 596 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 600 */     return false;
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
/*     */   public static void updateCommandStatus(int commandId, int progressStatus) throws SQLException, InterruptedException {
/* 613 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 615 */         GriffonQueryHandler.updateCommandStatus(commandId, progressStatus, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 617 */       } catch (SQLException ex) {
/* 618 */         if (retries == 3) {
/* 619 */           throw ex;
/*     */         }
/* 621 */         GenericDBManager.checkDerbyService();
/* 622 */         Thread.sleep(5000L);
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
/*     */   
/*     */   public static void updateCommandFailureStatus(int commandId, int eRetries, String content) throws SQLException, InterruptedException {
/* 639 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 641 */         GriffonQueryHandler.updateCommandFailureStatus(commandId, eRetries, content, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 643 */       } catch (SQLException ex) {
/* 644 */         if (retries == 3) {
/* 645 */           throw ex;
/*     */         }
/* 647 */         GenericDBManager.checkDerbyService();
/* 648 */         Thread.sleep(5000L);
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
/*     */   public static void updateCommandFileData(int idCommand, InputStream commandFileData) throws SQLException, InterruptedException {
/* 663 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 665 */         GriffonQueryHandler.updateCommandFileData(idCommand, commandFileData, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 667 */       } catch (SQLException ex) {
/* 668 */         if (retries == 3) {
/* 669 */           throw ex;
/*     */         }
/* 671 */         GenericDBManager.checkDerbyService();
/* 672 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void insertEvents(int idModule, int idGroup, int eProtocol, int eventDesc, String account, String rptCode, String partition, String zone) throws SQLException, InterruptedException {
/* 680 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 682 */         GriffonQueryHandler.insertEvents(idModule, idGroup, eProtocol, eventDesc, account, rptCode, partition, zone, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 684 */       } catch (SQLException ex) {
/* 685 */         if (retries == 3) {
/* 686 */           throw ex;
/*     */         }
/* 688 */         GenericDBManager.checkDerbyService();
/* 689 */         Thread.sleep(5000L);
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
/* 703 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 705 */         return GriffonQueryHandler.getNonProcessedEvents(GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 706 */       } catch (SQLException ex) {
/* 707 */         if (retries == 3) {
/* 708 */           throw ex;
/*     */         }
/* 710 */         GenericDBManager.checkDerbyService();
/* 711 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 715 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> getGroupIds() throws SQLException, InterruptedException {
/* 720 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 722 */         return GriffonQueryHandler.getGroupIds(GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 723 */       } catch (SQLException ex) {
/* 724 */         if (retries == 3) {
/* 725 */           throw ex;
/*     */         }
/* 727 */         GenericDBManager.checkDerbyService();
/* 728 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 732 */     return null;
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
/*     */   public static List<Integer> getAnalogPGMIndexByModuleID(int idModule) throws SQLException, InterruptedException {
/* 744 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 746 */         return GriffonQueryHandler.getAnalogPGMIndexByModuleID(idModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 747 */       } catch (SQLException ex) {
/* 748 */         if (retries == 3) {
/* 749 */           throw ex;
/*     */         }
/* 751 */         GenericDBManager.checkDerbyService();
/* 752 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 756 */     return null;
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
/*     */   public static Map<String, List<Integer>> getAnalogZoneIndexByModuleID(int idModule) throws SQLException, InterruptedException {
/* 768 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 770 */         return GriffonQueryHandler.getAnalogZoneIndexByModuleID(idModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 771 */       } catch (SQLException ex) {
/* 772 */         if (retries == 3) {
/* 773 */           throw ex;
/*     */         }
/* 775 */         GenericDBManager.checkDerbyService();
/* 776 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 780 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void disableCommunicationLog(int idModule) throws SQLException, InterruptedException {
/* 785 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 787 */         GriffonQueryHandler.disableCommunicationLog(idModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/*     */         break;
/* 789 */       } catch (SQLException ex) {
/* 790 */         if (retries == 3) {
/* 791 */           throw ex;
/*     */         }
/* 793 */         GenericDBManager.checkDerbyService();
/* 794 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getSnFromGriffonModule(int idClient) throws SQLException, InterruptedException {
/* 802 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 804 */         return GriffonQueryHandler.getSnFromGriffonModule(idClient, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 805 */       } catch (SQLException ex) {
/* 806 */         if (retries == 3) {
/* 807 */           throw ex;
/*     */         }
/* 809 */         GenericDBManager.checkDerbyService();
/* 810 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 814 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void clearKeyfobReceiverOnUsbHwFail(int idModule) throws SQLException, InterruptedException {
/* 825 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 827 */         GriffonQueryHandler.clearKeyfobReceiverOnUsbHwFail(idModule, GenericDBManager.getConnectionBySchemaName("GRIFFON", false));
/* 828 */       } catch (SQLException ex) {
/* 829 */         if (retries == 3) {
/* 830 */           throw ex;
/*     */         }
/* 832 */         GenericDBManager.checkDerbyService();
/* 833 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBManagers\GriffonDBManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */