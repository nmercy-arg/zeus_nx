/*     */ package com.zeusServer.DBManagers;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DBCleaner;
/*     */ import com.zeusServer.DBPools.GriffonPool;
/*     */ import com.zeusServer.DBPools.MercuriusPool;
/*     */ import com.zeusServer.DBPools.PegasusPool;
/*     */ import com.zeusServer.DBPools.SysPool;
/*     */ import com.zeusServer.DBPools.ZeusSettingsPool;
/*     */ import com.zeusServer.dao.generic.GenericQueryHandler;
/*     */ import com.zeusServer.dao.generic.GenericSPHandler;
/*     */ import com.zeusServer.dao.griffon.GriffonSPHandler51_60;
/*     */ import com.zeusServer.dao.mercurius.MercuriusSPHandler51_60;
/*     */ import com.zeusServer.dao.pegasus.v1.PegasusSPHandler51_63;
/*     */ import com.zeusServer.dto.SP_013DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LoggedInUser;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_029DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_062DataHolder;
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GenericDBManager
/*     */ {
/*  46 */   private static ScheduledExecutorService dailyDbAutoCleanupService = null;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void executeSP_005(int idEvent, short transmissionRetries, int productId) throws SQLException, InterruptedException {
/*  58 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  60 */         GenericSPHandler.executeSP_005(idEvent, transmissionRetries, getConnectionByProductId(productId, false));
/*     */         break;
/*  62 */       } catch (SQLException ex) {
/*  63 */         if (retries == 3) {
/*  64 */           throw ex;
/*     */         }
/*  66 */         checkDerbyService();
/*  67 */         Thread.sleep(5000L);
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
/*     */   public static void executeSP_008(int idEvent, int productId) throws SQLException, InterruptedException {
/*  82 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/*  84 */         GenericSPHandler.executeSP_008(idEvent, getConnectionByProductId(productId, false));
/*     */         break;
/*  86 */       } catch (SQLException ex) {
/*  87 */         if (retries == 3) {
/*  88 */           throw ex;
/*     */         }
/*  90 */         checkDerbyService();
/*  91 */         Thread.sleep(5000L);
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
/*     */   public static SP_013DataHolder executeSP_013(String schemaName) throws SQLException, InterruptedException {
/* 106 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 108 */         return GenericSPHandler.executeSP_013(getConnectionBySchemaName(schemaName, false));
/*     */       }
/* 110 */       catch (SQLException ex) {
/* 111 */         if (retries == 3) {
/* 112 */           throw ex;
/*     */         }
/* 114 */         checkDerbyService();
/* 115 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 119 */     return null;
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
/*     */   public static void executeSP_016(int idEvent, int productId) throws SQLException, InterruptedException {
/* 131 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 133 */         GenericSPHandler.executeSP_016(idEvent, getConnectionByProductId(productId, false));
/*     */         break;
/* 135 */       } catch (SQLException ex) {
/* 136 */         if (retries == 3) {
/* 137 */           throw ex;
/*     */         }
/* 139 */         checkDerbyService();
/* 140 */         Thread.sleep(5000L);
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
/*     */   public static void executeSP_018(int idEvent, short transmissionRetries, int productId) throws SQLException, InterruptedException {
/* 156 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 158 */         GenericSPHandler.executeSP_018(idEvent, transmissionRetries, getConnectionByProductId(productId, false));
/*     */         break;
/* 160 */       } catch (SQLException ex) {
/* 161 */         if (retries == 3) {
/* 162 */           throw ex;
/*     */         }
/* 164 */         checkDerbyService();
/* 165 */         Thread.sleep(5000L);
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
/*     */   public static void executeSP_019(int idClient, String schemaName) throws SQLException, InterruptedException {
/* 180 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 182 */         GenericSPHandler.executeSP_019(idClient, getConnectionBySchemaName(schemaName, false));
/*     */         break;
/* 184 */       } catch (SQLException ex) {
/* 185 */         if (retries == 3) {
/* 186 */           throw ex;
/*     */         }
/* 188 */         checkDerbyService();
/* 189 */         Thread.sleep(5000L);
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
/*     */   public static SP_029DataHolder executeSP_029(String phone, int productId) throws SQLException, InterruptedException {
/* 205 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 207 */         return GenericSPHandler.executeSP_029(phone, getConnectionByProductId(productId, false));
/* 208 */       } catch (SQLException ex) {
/* 209 */         if (retries == 3) {
/* 210 */           throw ex;
/*     */         }
/* 212 */         checkDerbyService();
/* 213 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 217 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static LoggedInUser executeSP_055(String userName, String password, String schemaName) throws SQLException, InterruptedException {
/* 222 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 224 */         switch (schemaName) {
/*     */           case "PEGASUS":
/* 226 */             return PegasusSPHandler51_63.executeSP_055(userName, password, getConnectionBySchemaName("PEGASUS", false));
/*     */           case "GRIFFON":
/* 228 */             return GriffonSPHandler51_60.executeSP_055(userName, password, getConnectionBySchemaName("GRIFFON", false));
/*     */           case "MERCURIUS":
/* 230 */             return MercuriusSPHandler51_60.executeSP_055(userName, password, getConnectionBySchemaName("MERCURIUS", false));
/*     */         } 
/* 232 */       } catch (SQLException ex) {
/* 233 */         if (retries == 3) {
/* 234 */           throw ex;
/*     */         }
/* 236 */         checkDerbyService();
/* 237 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 241 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_062DataHolder executeSP_062(String phone, int productId) throws SQLException, InterruptedException {
/* 246 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 248 */         return GenericSPHandler.executeSP_062(phone, getConnectionByProductId(productId, false));
/* 249 */       } catch (SQLException ex) {
/* 250 */         if (retries == 3) {
/* 251 */           throw ex;
/*     */         }
/* 253 */         checkDerbyService();
/* 254 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 258 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void compressTable(String schemaName, String tableName) throws SQLException, InterruptedException {
/* 263 */     for (int retries = 1; retries <= 6; retries++) {
/*     */       try {
/* 265 */         GenericQueryHandler.compressTable(schemaName, tableName, getConnectionBySchemaName(schemaName, true));
/*     */         break;
/* 267 */       } catch (SQLException ex) {
/* 268 */         if (retries == 6) {
/* 269 */           throw ex;
/*     */         }
/* 271 */         checkDerbyService();
/* 272 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void setDatabaseProperty(String property, String value, String schemaName) throws SQLException, InterruptedException {
/* 280 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 282 */         GenericQueryHandler.setDatabaseProperty(property, value, getConnectionBySchemaName(schemaName, false));
/*     */         break;
/* 284 */       } catch (SQLException ex) {
/* 285 */         if (retries == 3) {
/* 286 */           throw ex;
/*     */         }
/* 288 */         checkDerbyService();
/* 289 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static int getNumberRecordsToBeDeleted(String schemaName, Enums.EnumDbTableIDs dbTableID, Timestamp limitTimestamp) throws SQLException, InterruptedException {
/* 297 */     for (int retries = 1; retries <= 3; retries++) {
/*     */       try {
/* 299 */         return GenericQueryHandler.getNumberRecordsToBeDeleted(schemaName, dbTableID, limitTimestamp);
/* 300 */       } catch (SQLException ex) {
/* 301 */         if (retries == 3) {
/* 302 */           throw ex;
/*     */         }
/* 304 */         checkDerbyService();
/* 305 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */     
/* 309 */     return 0;
/*     */   }
/*     */ 
/*     */   
/*     */   public static void deleteRecords(String schemaName, Enums.EnumDbTableIDs dbTableID, Timestamp limitTimestamp, int numRecordsToBeDeleted) throws SQLException, InterruptedException {
/* 314 */     for (int retries = 1; retries <= 6; retries++) {
/*     */       try {
/* 316 */         GenericQueryHandler.deleteRecords(schemaName, dbTableID, limitTimestamp, numRecordsToBeDeleted);
/*     */         break;
/* 318 */       } catch (SQLException ex) {
/* 319 */         if (retries == 6) {
/* 320 */           throw ex;
/*     */         }
/* 322 */         checkDerbyService();
/* 323 */         Thread.sleep(5000L);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void stopDbCleanupDailyTimer() {
/* 331 */     if (dailyDbAutoCleanupService != null) {
/* 332 */       dailyDbAutoCleanupService.shutdownNow();
/* 333 */       dailyDbAutoCleanupService = null;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Connection getConnectionBySchemaName(String schema, boolean getConnectionEvenDuringDbSpaceReclaim) throws SQLException {
/* 341 */     while (!getConnectionEvenDuringDbSpaceReclaim && GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/* 342 */       Thread.yield();
/*     */     }
/* 344 */     Connection con = null;
/* 345 */     switch (schema) {
/*     */       case "SYS":
/* 347 */         con = SysPool.getSysConnectionPool().getConnection();
/*     */         break;
/*     */       case "ZEUSSETTINGS":
/* 350 */         con = ZeusSettingsPool.getZeusSettingsConnectionPool().getConnection();
/* 351 */         con.setSchema("ZEUSSETTINGS");
/*     */         break;
/*     */       case "PEGASUS":
/* 354 */         con = PegasusPool.getPegasusConnectionPool().getConnection();
/* 355 */         con.setSchema("PEGASUS");
/*     */         break;
/*     */       case "GRIFFON":
/* 358 */         con = GriffonPool.getGriffonConnectionPool().getConnection();
/* 359 */         con.setSchema("GRIFFON");
/*     */         break;
/*     */       case "MERCURIUS":
/* 362 */         con = MercuriusPool.getMercuriusAVLPool().getConnection();
/* 363 */         con.setSchema("MERCURIUS");
/*     */         break;
/*     */     } 
/* 366 */     return con;
/*     */   }
/*     */ 
/*     */   
/*     */   public static Connection getConnectionByProductId(int productId, boolean getConnectionEvenDuringDbSpaceReclaim) throws SQLException {
/* 371 */     switch (Util.EnumProductIDs.getProductID(productId)) {
/*     */       case ZEUS:
/* 373 */         return getConnectionBySchemaName("ZEUSSETTINGS", getConnectionEvenDuringDbSpaceReclaim);
/*     */       case PEGASUS:
/* 375 */         return getConnectionBySchemaName("PEGASUS", getConnectionEvenDuringDbSpaceReclaim);
/*     */       case GRIFFON_V1:
/*     */       case GRIFFON_V2:
/* 378 */         return getConnectionBySchemaName("GRIFFON", getConnectionEvenDuringDbSpaceReclaim);
/*     */       case MERCURIUS:
/* 380 */         return getConnectionBySchemaName("MERCURIUS", getConnectionEvenDuringDbSpaceReclaim);
/*     */     } 
/* 382 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getSchemaNameByProductId(int productId) {
/* 388 */     switch (Util.EnumProductIDs.getProductID(productId)) {
/*     */       case ZEUS:
/* 390 */         return "ZEUSSETTINGS";
/*     */       case PEGASUS:
/* 392 */         return "PEGASUS";
/*     */       case GRIFFON_V1:
/*     */       case GRIFFON_V2:
/* 395 */         return "GRIFFON";
/*     */       case MERCURIUS:
/* 397 */         return "MERCURIUS";
/*     */     } 
/* 399 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static int getProductIdBySchemaName(String schemaName) {
/* 405 */     switch (schemaName) {
/*     */       case "ZEUSSETTINGS":
/* 407 */         return Util.EnumProductIDs.ZEUS.getProductId();
/*     */       case "PEGASUS":
/* 409 */         return Util.EnumProductIDs.PEGASUS.getProductId();
/*     */       case "GRIFFON":
/* 411 */         return Util.EnumProductIDs.GRIFFON_V1.getProductId();
/*     */       case "MERCURIUS":
/* 413 */         return Util.EnumProductIDs.MERCURIUS.getProductId();
/*     */     } 
/* 415 */     return -1;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void dbAutoCleanupDaily() {
/*     */     try {
/* 422 */       if (dailyDbAutoCleanupService == null) {
/* 423 */         GlobalVariables.lastDbCleanup = System.currentTimeMillis();
/* 424 */         dailyDbAutoCleanupService = Executors.newSingleThreadScheduledExecutor();
/* 425 */         dailyDbAutoCleanupService.scheduleAtFixedRate(new Runnable()
/*     */             {
/*     */               public void run() {
/* 428 */                 if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.NORMAL && Functions.isLastCleanupWasBefore24Hr()) {
/*     */                   try {
/* 430 */                     GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.CLEANUP;
/* 431 */                     GlobalVariables.dbCleaner = new DBCleaner(new Timestamp(System.currentTimeMillis()));
/* 432 */                     GlobalVariables.cleanupThread = new Thread((Runnable)GlobalVariables.dbCleaner);
/* 433 */                     GlobalVariables.cleanupThread.setDaemon(true);
/* 434 */                     GlobalVariables.cleanupThread.start();
/* 435 */                     GlobalVariables.lastDbCleanup = System.currentTimeMillis();
/* 436 */                   } catch (Exception ex) {
/* 437 */                     GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/*     */                   } 
/*     */                 }
/*     */               }
/*     */             },  1L, 1L, TimeUnit.HOURS);
/*     */       } 
/* 443 */     } catch (Exception ex) {
/* 444 */       Logger.getLogger(GenericDBManager.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void checkDerbyService() {
/*     */     // Byte code:
/*     */     //   0: getstatic com/zeusServer/util/GlobalVariables.dbCurrentStatus : Lcom/zeusServer/util/Enums$enumDbStatus;
/*     */     //   3: getstatic com/zeusServer/util/Enums$enumDbStatus.NORMAL : Lcom/zeusServer/util/Enums$enumDbStatus;
/*     */     //   6: if_acmpeq -> 15
/*     */     //   9: invokestatic isDbInCleanupState : ()Z
/*     */     //   12: ifeq -> 123
/*     */     //   15: ldc 'ZeusDerby'
/*     */     //   17: invokestatic getServiceControllerByName : (Ljava/lang/String;)Lcom/zeusServer/service/controller/ServiceController;
/*     */     //   20: invokeinterface getServiceStatus : ()I
/*     */     //   25: iconst_4
/*     */     //   26: if_icmpne -> 32
/*     */     //   29: goto -> 103
/*     */     //   32: ldc 'ZeusDerby'
/*     */     //   34: invokestatic getServiceControllerByName : (Ljava/lang/String;)Lcom/zeusServer/service/controller/ServiceController;
/*     */     //   37: invokeinterface startService : ()Z
/*     */     //   42: pop
/*     */     //   43: ldc2_w 4000
/*     */     //   46: invokestatic sleep : (J)V
/*     */     //   49: goto -> 69
/*     */     //   52: astore_0
/*     */     //   53: ldc com/zeusServer/DBManagers/GenericDBManager
/*     */     //   55: invokevirtual getName : ()Ljava/lang/String;
/*     */     //   58: invokestatic getLogger : (Ljava/lang/String;)Ljava/util/logging/Logger;
/*     */     //   61: getstatic java/util/logging/Level.SEVERE : Ljava/util/logging/Level;
/*     */     //   64: aconst_null
/*     */     //   65: aload_0
/*     */     //   66: invokevirtual log : (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   69: goto -> 89
/*     */     //   72: astore_0
/*     */     //   73: ldc com/zeusServer/DBManagers/GenericDBManager
/*     */     //   75: invokevirtual getName : ()Ljava/lang/String;
/*     */     //   78: invokestatic getLogger : (Ljava/lang/String;)Ljava/util/logging/Logger;
/*     */     //   81: getstatic java/util/logging/Level.SEVERE : Ljava/util/logging/Level;
/*     */     //   84: aconst_null
/*     */     //   85: aload_0
/*     */     //   86: invokevirtual log : (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   89: ldc 'ZeusDerby'
/*     */     //   91: invokestatic getServiceControllerByName : (Ljava/lang/String;)Lcom/zeusServer/service/controller/ServiceController;
/*     */     //   94: invokeinterface getServiceStatus : ()I
/*     */     //   99: iconst_4
/*     */     //   100: if_icmpne -> 15
/*     */     //   103: goto -> 123
/*     */     //   106: astore_0
/*     */     //   107: ldc com/zeusServer/DBManagers/GenericDBManager
/*     */     //   109: invokevirtual getName : ()Ljava/lang/String;
/*     */     //   112: invokestatic getLogger : (Ljava/lang/String;)Ljava/util/logging/Logger;
/*     */     //   115: getstatic java/util/logging/Level.SEVERE : Ljava/util/logging/Level;
/*     */     //   118: aconst_null
/*     */     //   119: aload_0
/*     */     //   120: invokevirtual log : (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   123: return
/*     */     // Line number table:
/*     */     //   Java source line number -> byte code offset
/*     */     //   #450	-> 0
/*     */     //   #455	-> 15
/*     */     //   #456	-> 29
/*     */     //   #458	-> 32
/*     */     //   #460	-> 43
/*     */     //   #463	-> 49
/*     */     //   #461	-> 52
/*     */     //   #462	-> 53
/*     */     //   #467	-> 69
/*     */     //   #465	-> 72
/*     */     //   #466	-> 73
/*     */     //   #468	-> 89
/*     */     //   #471	-> 103
/*     */     //   #469	-> 106
/*     */     //   #470	-> 107
/*     */     //   #473	-> 123
/*     */     // Local variable table:
/*     */     //   start	length	slot	name	descriptor
/*     */     //   53	16	0	ex	Ljava/lang/InterruptedException;
/*     */     //   73	16	0	ex	Ljava/lang/Exception;
/*     */     //   107	16	0	ex	Ljava/lang/Exception;
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   15	29	72	java/lang/Exception
/*     */     //   15	103	106	java/lang/Exception
/*     */     //   32	69	72	java/lang/Exception
/*     */     //   43	49	52	java/lang/InterruptedException
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBManagers\GenericDBManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */