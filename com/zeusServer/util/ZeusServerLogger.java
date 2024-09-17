/*     */ package com.zeusServer.util;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import org.apache.log4j.Appender;
/*     */ import org.apache.log4j.Layout;
/*     */ import org.apache.log4j.LogManager;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.apache.log4j.PatternLayout;
/*     */ import org.apache.log4j.PropertyConfigurator;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ZeusServerLogger
/*     */ {
/*  25 */   private static final Logger ZEUS_MSG_LOGGER = Logger.getLogger("ZeusMessages");
/*  26 */   private static final Logger PEGASUS_MSG_LOGGER = Logger.getLogger("PegasusMessages");
/*  27 */   private static final Logger GRIFFON_MSG_LOGGER = Logger.getLogger("GriffonMessages");
/*  28 */   private static final Logger IGUARD_MSG_LOGGER = Logger.getLogger("iGuardMessages");
/*  29 */   private static final Logger MERCURIUS_MSG_LOGGER = Logger.getLogger("MercuriusMessages");
/*     */   
/*  31 */   private static final Logger ZEUS_EVT_LOGGER = Logger.getLogger("ZeusEvents");
/*  32 */   private static final Logger PEGASUS_EVT_LOGGER = Logger.getLogger("PegasusEvents");
/*  33 */   private static final Logger GRIFFON_EVT_LOGGER = Logger.getLogger("GriffonEvents");
/*  34 */   private static final Logger IGUARD_EVT_LOGGER = Logger.getLogger("iGuardEvents");
/*  35 */   private static final Logger MERCURIUS_EVT_LOGGER = Logger.getLogger("MercuriusEvents");
/*     */   
/*  37 */   private static final Logger SMS_ALL_LOGGER = Logger.getLogger("SMSALL");
/*  38 */   private static final Logger SMS_BAD_LOGGER = Logger.getLogger("SMSBAD");
/*     */ 
/*     */   
/*     */   static {
/*  42 */     PropertyConfigurator.configure("log4j.properties");
/*     */   }
/*     */ 
/*     */   
/*     */   public static void logMessage(Util.EnumProductIDs productId, String message) {
/*  47 */     switch (productId) {
/*     */       case ZEUS:
/*  49 */         ZEUS_MSG_LOGGER.info(message);
/*     */         return;
/*     */       case PEGASUS:
/*  52 */         PEGASUS_MSG_LOGGER.info(message);
/*     */         return;
/*     */       case GRIFFON_V1:
/*     */       case GRIFFON_V2:
/*  56 */         GRIFFON_MSG_LOGGER.info(message);
/*     */         return;
/*     */       case MERCURIUS:
/*  59 */         MERCURIUS_MSG_LOGGER.info(message);
/*     */         return;
/*     */       case IGUARD:
/*  62 */         IGUARD_MSG_LOGGER.info(message);
/*     */         return;
/*     */     } 
/*  65 */     ZEUS_MSG_LOGGER.info(message);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void logEvent(Util.EnumProductIDs productId, String event) {
/*  71 */     switch (productId) {
/*     */       case ZEUS:
/*  73 */         ZEUS_EVT_LOGGER.info(event);
/*     */         return;
/*     */       case PEGASUS:
/*  76 */         PEGASUS_EVT_LOGGER.info(event);
/*     */         return;
/*     */       case GRIFFON_V1:
/*     */       case GRIFFON_V2:
/*  80 */         GRIFFON_EVT_LOGGER.info(event);
/*     */         return;
/*     */       case MERCURIUS:
/*  83 */         MERCURIUS_EVT_LOGGER.info(event);
/*     */         return;
/*     */       case IGUARD:
/*  86 */         IGUARD_EVT_LOGGER.info(event);
/*     */         return;
/*     */     } 
/*  89 */     ZEUS_EVT_LOGGER.info(event);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static void logSMSAll(InfoSms infoSMS) {
/*  95 */     SMS_ALL_LOGGER.info(infoSMS);
/*     */   }
/*     */   
/*     */   public static void logSMSBAD(InfoSms infoSMS) {
/*  99 */     SMS_BAD_LOGGER.info(infoSMS);
/*     */   }
/*     */   
/*     */   public static void logErrorMessage(Util.EnumProductIDs productId, String message, Throwable t) {
/* 103 */     switch (productId) {
/*     */       case ZEUS:
/* 105 */         ZEUS_MSG_LOGGER.debug(message, t);
/*     */         return;
/*     */       case PEGASUS:
/* 108 */         PEGASUS_MSG_LOGGER.debug(message, t);
/*     */         return;
/*     */       case GRIFFON_V1:
/*     */       case GRIFFON_V2:
/* 112 */         GRIFFON_MSG_LOGGER.debug(message, t);
/*     */         return;
/*     */       case MERCURIUS:
/* 115 */         MERCURIUS_MSG_LOGGER.debug(message, t);
/*     */         return;
/*     */       case IGUARD:
/* 118 */         IGUARD_MSG_LOGGER.debug(message, t);
/*     */         return;
/*     */     } 
/* 121 */     ZEUS_MSG_LOGGER.debug(message, t);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isDebugMessageLogger(Util.EnumProductIDs productId) {
/* 128 */     switch (productId)
/*     */     { case ZEUS:
/* 130 */         enabled = ZEUS_MSG_LOGGER.isDebugEnabled();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 149 */         return enabled;case PEGASUS: enabled = PEGASUS_MSG_LOGGER.isDebugEnabled(); return enabled;case GRIFFON_V1: case GRIFFON_V2: enabled = GRIFFON_MSG_LOGGER.isDebugEnabled(); return enabled;case MERCURIUS: enabled = MERCURIUS_MSG_LOGGER.isDebugEnabled(); return enabled;case IGUARD: enabled = IGUARD_MSG_LOGGER.isDebugEnabled(); return enabled; }  boolean enabled = ZEUS_MSG_LOGGER.isDebugEnabled(); return enabled;
/*     */   }
/*     */   
/*     */   public static Logger getDeviceLogger(String productDir, String sn) {
/* 153 */     if (LogManager.exists(sn) == null) {
/* 154 */       DatedFileAppender newDFA = new DatedFileAppender();
/* 155 */       newDFA.setDirectory("messages/" + productDir + sn);
/* 156 */       newDFA.setPrefix(sn + "_");
/* 157 */       newDFA.setName(sn);
/* 158 */       newDFA.setLayout((Layout)new PatternLayout("%d{HH:mm:ss} >> %m%n"));
/* 159 */       newDFA.activateOptions();
/* 160 */       Logger newLogger = Logger.getLogger(sn);
/* 161 */       newLogger.addAppender((Appender)newDFA);
/* 162 */       newLogger.setAdditivity(false);
/* 163 */       return newLogger;
/*     */     } 
/* 165 */     return LogManager.exists(sn);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\ZeusServerLogger.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */