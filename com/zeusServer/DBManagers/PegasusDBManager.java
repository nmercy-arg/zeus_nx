/*     */ package com.zeusServer.DBManagers;
/*     */ 
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusQueryHandler;
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusSPHandler01_10;
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusSPHandler11_20;
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusSPHandler21_30;
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusSPHandler31_40;
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusSPHandler51_63;
/*     */ import com.zeusServer.dao.pegasus.v2.PegasusV2QueryHandler;
/*     */ import com.zeusServer.dto.PendingDataHolder;
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.dto.SP_022DataHolder;
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.serialPort.communication.EventDataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.ModuleBean;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_003DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_003_VO;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_015DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_030DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_V2_001_VO;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PegasusDBManager
/*     */ {
/*     */   public static void executeSP_001(int idClient, int occuranceType) throws SQLException, InterruptedException {
/*  54 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  56 */         PegasusSPHandler01_10.executeSP_001(idClient, occuranceType, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/*  58 */       } catch (SQLException ex) {
/*  59 */         if (retries == 3) {
/*  60 */           throw ex;
/*     */         }
/*  62 */         GenericDBManager.checkDerbyService();
/*  63 */         Thread.sleep(5000L);
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
/*     */   public static SP_003DataHolder executeSP_003(List<SP_003_VO> list) throws SQLException, InterruptedException {
/*  79 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  81 */         return PegasusSPHandler01_10.executeSP_003(list, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*  82 */       } catch (SQLException ex) {
/*  83 */         if (retries == 3) {
/*  84 */           throw ex;
/*     */         }
/*  86 */         GenericDBManager.checkDerbyService();
/*  87 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/*  91 */     return null;
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
/*     */   public static void executeSP_004(int idClient, String newClientCode) throws SQLException, InterruptedException {
/* 103 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 105 */         PegasusSPHandler01_10.executeSP_004(idClient, newClientCode, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 107 */       } catch (SQLException ex) {
/* 108 */         if (retries == 3) {
/* 109 */           throw ex;
/*     */         }
/* 111 */         GenericDBManager.checkDerbyService();
/* 112 */         Thread.sleep(5000L);
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
/* 126 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 128 */         return PegasusSPHandler01_10.executeSP_006(GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 129 */       } catch (SQLException ex) {
/* 130 */         if (retries == 3) {
/* 131 */           throw ex;
/*     */         }
/* 133 */         GenericDBManager.checkDerbyService();
/* 134 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 138 */     return null;
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
/* 149 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 151 */         return PegasusSPHandler01_10.executeSP_007(GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 152 */       } catch (SQLException ex) {
/* 153 */         if (retries == 3) {
/* 154 */           throw ex;
/*     */         }
/* 156 */         GenericDBManager.checkDerbyService();
/* 157 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 161 */     return null;
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
/* 173 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 175 */         PegasusSPHandler01_10.executeSP_009(idClient, occuranceType, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 177 */       } catch (SQLException ex) {
/* 178 */         if (retries == 3) {
/* 179 */           throw ex;
/*     */         }
/* 181 */         GenericDBManager.checkDerbyService();
/* 182 */         Thread.sleep(5000L);
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
/* 196 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 198 */         return PegasusSPHandler11_20.executeSP_011(GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 199 */       } catch (SQLException ex) {
/* 200 */         if (retries == 3) {
/* 201 */           throw ex;
/*     */         }
/* 203 */         GenericDBManager.checkDerbyService();
/* 204 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 208 */     return null;
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
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void executeSP_014(int idModule, int rcvrGroup, String rcvrCOMPort, short protocol, String clientCode, byte[] eventData, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface) throws SQLException, InterruptedException {
/* 227 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 229 */         PegasusSPHandler11_20.executeSP_014(idModule, rcvrGroup, rcvrCOMPort, protocol, clientCode, eventData, lastMProtocolRcvd, nwProtocol, lastCommInterface, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 231 */       } catch (SQLException ex) {
/* 232 */         if (retries == 3) {
/* 233 */           throw ex;
/*     */         }
/* 235 */         GenericDBManager.checkDerbyService();
/* 236 */         Thread.sleep(5000L);
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
/*     */   public static SP_015DataHolder executeSP_015(SP_015DataHolder sp15DH) throws SQLException, InterruptedException {
/* 251 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 253 */         return PegasusSPHandler11_20.executeSP_015(sp15DH, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 254 */       } catch (SQLException ex) {
/* 255 */         if (retries == 3) {
/* 256 */           throw ex;
/*     */         }
/* 258 */         GenericDBManager.checkDerbyService();
/* 259 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 263 */     return null;
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
/* 274 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 276 */         PegasusSPHandler11_20.executeSP_017(idOccurance, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 278 */       } catch (SQLException ex) {
/* 279 */         if (retries == 3) {
/* 280 */           throw ex;
/*     */         }
/* 282 */         GenericDBManager.checkDerbyService();
/* 283 */         Thread.sleep(5000L);
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
/* 297 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 299 */         PegasusSPHandler11_20.executeSP_020(idOccurance, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 301 */       } catch (SQLException ex) {
/* 302 */         if (retries == 3) {
/* 303 */           throw ex;
/*     */         }
/* 305 */         GenericDBManager.checkDerbyService();
/* 306 */         Thread.sleep(5000L);
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
/*     */   public static SP_022DataHolder executeSP_022(int idClient, int tstPacket) throws SQLException, InterruptedException {
/* 322 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 324 */         return PegasusSPHandler21_30.executeSP_022(idClient, tstPacket, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 325 */       } catch (SQLException ex) {
/* 326 */         if (retries == 3) {
/* 327 */           throw ex;
/*     */         }
/* 329 */         GenericDBManager.checkDerbyService();
/* 330 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 334 */     return null;
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
/*     */   public static List<SP_024DataHolder> executeSP_024(int idModule) throws SQLException, InterruptedException, IOException {
/* 347 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 349 */         return PegasusSPHandler21_30.executeSP_024(idModule, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 350 */       } catch (IOException|SQLException ex) {
/* 351 */         if (retries == 3) {
/* 352 */           throw ex;
/*     */         }
/* 354 */         GenericDBManager.checkDerbyService();
/* 355 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 359 */     return null;
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
/* 371 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 373 */         PegasusSPHandler21_30.executeSP_025(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 375 */       } catch (SQLException ex) {
/* 376 */         if (retries == 3) {
/* 377 */           throw ex;
/*     */         }
/* 379 */         GenericDBManager.checkDerbyService();
/* 380 */         Thread.sleep(5000L);
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
/* 395 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 397 */         PegasusSPHandler21_30.executeSP_026(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 399 */       } catch (SQLException ex) {
/* 400 */         if (retries == 3) {
/* 401 */           throw ex;
/*     */         }
/* 403 */         GenericDBManager.checkDerbyService();
/* 404 */         Thread.sleep(5000L);
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
/* 419 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 421 */         PegasusSPHandler21_30.executeSP_027(id_Command, num_Exec_Retries, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 423 */       } catch (SQLException ex) {
/* 424 */         if (retries == 3) {
/* 425 */           throw ex;
/*     */         }
/* 427 */         GenericDBManager.checkDerbyService();
/* 428 */         Thread.sleep(5000L);
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
/* 444 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 446 */         PegasusSPHandler21_30.executeSP_028(idModule, lastCommInterface, currentSIM, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 448 */       } catch (SQLException ex) {
/* 449 */         if (retries == 3) {
/* 450 */           throw ex;
/*     */         }
/* 452 */         GenericDBManager.checkDerbyService();
/* 453 */         Thread.sleep(5000L);
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
/*     */   public static SP_030DataHolder executeSP_030(int idModule) throws SQLException, InterruptedException {
/* 468 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 470 */         return PegasusSPHandler21_30.executeSP_030(idModule, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 471 */       } catch (SQLException ex) {
/* 472 */         if (retries == 3) {
/* 473 */           throw ex;
/*     */         }
/* 475 */         GenericDBManager.checkDerbyService();
/* 476 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 480 */     return null;
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
/*     */   public static List<String> executeSP_031(int idClient) throws SQLException, InterruptedException {
/* 492 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 494 */         return PegasusSPHandler31_40.executeSP_031(idClient, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 495 */       } catch (SQLException ex) {
/* 496 */         if (retries == 3) {
/* 497 */           throw ex;
/*     */         }
/* 499 */         GenericDBManager.checkDerbyService();
/* 500 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 504 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_059(List<ModuleBean> mdBean) throws SQLException, InterruptedException {
/* 509 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 511 */         PegasusSPHandler51_63.executeSP_059(mdBean, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 513 */       } catch (SQLException ex) {
/* 514 */         if (retries == 3) {
/* 515 */           throw ex;
/*     */         }
/* 517 */         GenericDBManager.checkDerbyService();
/* 518 */         Thread.sleep(5000L);
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
/*     */   public static void executeSP_065(int idCommand, InputStream commandFileData) throws SQLException, InterruptedException {
/* 533 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 535 */         PegasusSPHandler51_63.execute_065Query(idCommand, commandFileData, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 537 */       } catch (SQLException ex) {
/* 538 */         if (retries == 3) {
/* 539 */           throw ex;
/*     */         }
/* 541 */         GenericDBManager.checkDerbyService();
/* 542 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static SP_V2_001_VO executeSP_V2_001(SP_V2_001_VO spV2_001_VO) throws SQLException, InterruptedException {
/* 550 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 552 */         return PegasusV2QueryHandler.executeSP_V2_001(spV2_001_VO, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 553 */       } catch (SQLException ex) {
/* 554 */         if (retries == 3) {
/* 555 */           throw ex;
/*     */         }
/* 557 */         GenericDBManager.checkDerbyService();
/* 558 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 562 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_V2_002(List<ModuleBean> mdBean) throws SQLException, InterruptedException {
/* 567 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 569 */         PegasusV2QueryHandler.executeSP_V2_002(mdBean, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 571 */       } catch (SQLException ex) {
/* 572 */         if (retries == 3) {
/* 573 */           throw ex;
/*     */         }
/* 575 */         GenericDBManager.checkDerbyService();
/* 576 */         Thread.sleep(5000L);
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
/* 590 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 592 */         return PegasusQueryHandler.getAllPendingAlive(GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 593 */       } catch (SQLException ex) {
/* 594 */         if (retries == 3) {
/* 595 */           throw ex;
/*     */         }
/* 597 */         GenericDBManager.checkDerbyService();
/* 598 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 602 */     return null;
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
/* 616 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 618 */         return PegasusQueryHandler.isCommandCancelled(commandId, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 619 */       } catch (SQLException ex) {
/* 620 */         if (retries == 3) {
/* 621 */           throw ex;
/*     */         }
/* 623 */         GenericDBManager.checkDerbyService();
/* 624 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 628 */     return false;
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
/* 640 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 642 */         PegasusQueryHandler.updateCommandStatus(commandId, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 644 */       } catch (SQLException ex) {
/* 645 */         if (retries == 3) {
/* 646 */           throw ex;
/*     */         }
/* 648 */         GenericDBManager.checkDerbyService();
/* 649 */         Thread.sleep(5000L);
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
/* 666 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 668 */         PegasusQueryHandler.updateCommandFailureStatus(commandId, eRetries, content, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 670 */       } catch (SQLException ex) {
/* 671 */         if (retries == 3) {
/* 672 */           throw ex;
/*     */         }
/* 674 */         GenericDBManager.checkDerbyService();
/* 675 */         Thread.sleep(5000L);
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
/*     */   public static void updateSIMCardICCID(int idModule, String iccid1, String iccid2) throws SQLException, InterruptedException {
/* 692 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 694 */         PegasusQueryHandler.updateSIMCardICCID(idModule, iccid1, iccid2, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 696 */       } catch (SQLException ex) {
/* 697 */         if (retries == 3) {
/* 698 */           throw ex;
/*     */         }
/* 700 */         GenericDBManager.checkDerbyService();
/* 701 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getSnFromPegausModule(int idClient) throws SQLException, InterruptedException {
/* 709 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 711 */         return PegasusQueryHandler.getSnFromPegausModule(idClient, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 712 */       } catch (SQLException ex) {
/* 713 */         if (retries == 3) {
/* 714 */           throw ex;
/*     */         }
/* 716 */         GenericDBManager.checkDerbyService();
/* 717 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 721 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static List<EventDataHolder> getNonProcessedEvents() throws SQLException, InterruptedException {
/* 732 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 734 */         return PegasusQueryHandler.getNonProcessedEvents(GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 735 */       } catch (SQLException ex) {
/* 736 */         if (retries == 3) {
/* 737 */           throw ex;
/*     */         }
/* 739 */         GenericDBManager.checkDerbyService();
/* 740 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 744 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Integer> getGroupIds() throws SQLException, InterruptedException {
/* 749 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 751 */         return PegasusQueryHandler.getGroupIds(GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/* 752 */       } catch (SQLException ex) {
/* 753 */         if (retries == 3) {
/* 754 */           throw ex;
/*     */         }
/* 756 */         GenericDBManager.checkDerbyService();
/* 757 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 761 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void disableCommunicationLog(int idModule) throws SQLException, InterruptedException {
/* 766 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 768 */         PegasusQueryHandler.disableCommunicationLog(idModule, GenericDBManager.getConnectionBySchemaName("PEGASUS", false));
/*     */         break;
/* 770 */       } catch (SQLException ex) {
/* 771 */         if (retries == 3) {
/* 772 */           throw ex;
/*     */         }
/* 774 */         GenericDBManager.checkDerbyService();
/* 775 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBManagers\PegasusDBManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */