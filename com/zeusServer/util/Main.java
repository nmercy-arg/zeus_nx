/*      */ package com.zeusServer.util;
/*      */ 
/*      */ import Serialio.SerialPortLocal;
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*      */ import com.zeusServer.DBManagers.GenericDBManager;
/*      */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*      */ import com.zeusServer.DBPools.GriffonPool;
/*      */ import com.zeusServer.DBPools.MercuriusPool;
/*      */ import com.zeusServer.DBPools.PegasusPool;
/*      */ import com.zeusServer.ddns.client.DDNSClient;
/*      */ import com.zeusServer.griffon.ChkNewCommands;
/*      */ import com.zeusServer.griffon.ChkOccurrences;
/*      */ import com.zeusServer.griffon.GenerateEvents;
/*      */ import com.zeusServer.griffon.ProcessAlivePackets;
/*      */ import com.zeusServer.mercurius.ChkNewCommands;
/*      */ import com.zeusServer.mercurius.ChkOccurrences;
/*      */ import com.zeusServer.mercurius.GenerateEvents;
/*      */ import com.zeusServer.mercurius.ProcessAlivePackets;
/*      */ import com.zeusServer.pegasus.ChkNewCommands;
/*      */ import com.zeusServer.pegasus.ChkOccurrences;
/*      */ import com.zeusServer.pegasus.GenerateEvents;
/*      */ import com.zeusServer.pegasus.ProcessPegasusAlivePackets;
/*      */ import com.zeusServer.serialPort.communication.CommReceiverCSD;
/*      */ import com.zeusServer.serialPort.communication.EmulaReceiverSurgadViaTCPIP;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiver;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiverAdemco685;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiverRadionics;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiverSurgard;
/*      */ import com.zeusServer.serialPort.communication.EventLoader;
/*      */ import com.zeusServer.serialPort.communication.PrinterFunctions;
/*      */ import com.zeusServer.serialPort.communication.SerialMux;
/*      */ import com.zeusServer.socket.communication.TCPDataServer;
/*      */ import com.zeusServer.socket.communication.TCPMessageServer;
/*      */ import com.zeusServer.socket.communication.UDPDataServer;
/*      */ import com.zeusServer.socket.communication.UdpV2Handler;
/*      */ import com.zeusServer.tblConnections.TblActiveUdpConnections;
/*      */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblGriffonActiveUdpConnections;
/*      */ import com.zeusServer.tblConnections.TblMercuriusAVLActiveUdpConnections;
/*      */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblPrinterSpool;
/*      */ import com.zeusServer.tblConnections.TblThreadsCommReceiversCSD;
/*      */ import com.zeusServer.tblConnections.TblThreadsEmulaReceivers;
/*      */ import com.zeusServer.ui.UILogInitiator;
/*      */ import com.zeusbox.nativeLibrary.ZeusBoxDashBoard;
/*      */ import com.zeusbox.nativeLibrary.ZeusBoxNativeLibrary;
/*      */ import gnu.io.CommPortIdentifier;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.sql.SQLException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.Arrays;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.TreeSet;
/*      */ import java.util.concurrent.ScheduledFuture;
/*      */ import java.util.concurrent.ThreadLocalRandom;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ import org.hyperic.sigar.Sigar;
/*      */ import org.hyperic.sigar.SigarException;
/*      */ import org.joda.time.Days;
/*      */ import org.joda.time.LocalDate;
/*      */ import org.joda.time.ReadablePartial;
/*      */ import org.joda.time.format.DateTimeFormat;
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
/*      */ public class Main
/*      */ {
/*      */   private static ChkOccurrences checkOccurrences;
/*      */   private static ChkInternetLink chkInternetLink;
/*      */   private static DDNSClient ddnsClient;
/*      */   private static ProcessPegasusAlivePackets processAlivePackets;
/*      */   private static DerbyDBBackup dbBackup;
/*      */   private static SerialMux serialMux;
/*      */   private static GenerateEvents generateEvents;
/*      */   private static ChkNewCommands checkNewCommands;
/*      */   private static Thread threadDBBackup;
/*      */   private static Thread threadMultiplexdorSerial;
/*      */   private static Thread threadCheckOccurrences;
/*      */   private static Thread threadProcessAlivePackets;
/*      */   private static Thread threadGenerateEvents;
/*      */   private static Thread threadChkInternetLink;
/*      */   private static Thread threadDDNSClient;
/*      */   private static Thread threadChkNewCommands;
/*      */   private static Thread threadDataServer;
/*      */   private static Thread threadUDPDataServer;
/*      */   private static Thread threadMessageServer;
/*      */   private static EventLoader eventLoader;
/*      */   private static Thread threadEventLoader;
/*      */   private static TCPDataServer tds;
/*      */   private static UDPDataServer uds;
/*      */   private static TCPMessageServer tms;
/*      */   private static final long TEMP_MAXIMUM_SHUTDOWN_SOCKETS = 15000L;
/*      */   private static final long TEMP_MAXIMUM_SERVER_ERRORS_BETWEEN_REPLICATION = 300000L;
/*      */   private static final int MAXIMUM_NUMBER_SERVER_ERRORS = 3;
/*  117 */   private static int noOfServerErrors = 0;
/*  118 */   private static long lastTimeServerError = 0L;
/*      */   
/*      */   private static ThreadGroup zeusThreadGroup;
/*      */   
/*      */   private static Sigar sigar;
/*      */   private static ScheduledFuture closeCheckSF;
/*      */   private static Thread threadGriffonCheckOccurrences;
/*      */   private static Thread threadGriffonProcessAlivePackets;
/*      */   private static Thread threadGriffonGenerateEvents;
/*      */   private static Thread threadGriffonChkNewCommands;
/*      */   private static ChkOccurrences checkGriffonOccurrences;
/*      */   private static GenerateEvents generateGriffonEvents;
/*      */   private static ChkNewCommands checkGriffonNewCommands;
/*      */   private static ProcessAlivePackets processGriffonAlivePackets;
/*      */   private static Thread threadMercuriusCheckOccurrences;
/*      */   private static Thread threadMercuriusProcessAlivePackets;
/*      */   private static Thread threadMercuriusGenerateEvents;
/*      */   private static Thread threadMercuriusChkNewCommands;
/*      */   private static ChkOccurrences checkMercuriusOccurrences;
/*      */   private static GenerateEvents generateMercuriusEvents;
/*      */   private static ChkNewCommands checkMercuriusNewCommands;
/*      */   private static ProcessAlivePackets processMercuriusAlivePackets;
/*  140 */   public static String platformFilePath = "../Configuration/platform";
/*  141 */   private static final int RANDOM_DAY = ThreadLocalRandom.current().nextInt(180, 270);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static {
/*  149 */     Runnable closeCheckTask = new Runnable()
/*      */       {
/*      */         public void run() {
/*      */           try {
/*  153 */             Main.checkForClosedThreads();
/*  154 */           } catch (Exception ex) {
/*  155 */             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */           } 
/*      */         }
/*      */       };
/*  159 */     closeCheckSF = Functions.addRunnable2ScheduleExecutor(closeCheckTask, 15000L, 15000L, TimeUnit.MILLISECONDS);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void updateZeusSettings() throws SQLException, InterruptedException {
/*  170 */     Set<String> portSet = new TreeSet<>();
/*      */     
/*  172 */     Enumeration<CommPortIdentifier> en = CommPortIdentifier.getPortIdentifiers();
/*  173 */     while (en.hasMoreElements()) {
/*  174 */       CommPortIdentifier cpi = en.nextElement();
/*  175 */       if (cpi.getPortType() == 1) {
/*  176 */         portSet.add(cpi.getName());
/*      */       }
/*      */     } 
/*      */ 
/*      */     
/*  181 */     String[] prs = null;
/*      */     try {
/*  183 */       prs = SerialPortLocal.getPortList();
/*  184 */     } catch (IOException ex) {
/*  185 */       Logger.getLogger(Main.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */     } 
/*  187 */     if (prs != null && prs.length > 0) {
/*  188 */       List<String> portList = Arrays.asList(prs);
/*  189 */       if (portList != null && !portList.isEmpty()) {
/*  190 */         portSet.addAll(portList);
/*      */       }
/*      */     } 
/*  193 */     ZeusSettingsDBManager.updateZeusSettings(Util.EnumZeusSettingsPropNames.AVAILABLE_SERIALPORTS.getId(), portSet, null);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static void checkForClosedThreads() throws InterruptedException, SigarException, Exception {
/*  203 */     if (!GlobalVariables.mainTimerStopped) {
/*  204 */       GlobalVariables.mainTimerRoutineRunning = true;
/*      */ 
/*      */       
/*  207 */       LocalDate refDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(ZeusServerCfg.getInstance().getRefDate());
/*  208 */       int daysSinceLastUpdateRefDate = Days.daysBetween((ReadablePartial)refDate, (ReadablePartial)LocalDate.now()).getDays();
/*  209 */       if (daysSinceLastUpdateRefDate > RANDOM_DAY) {
/*  210 */         Calendar cal = Calendar.getInstance();
/*  211 */         cal.setTime(new Date());
/*  212 */         int weekDay = cal.get(7);
/*  213 */         if (weekDay >= 2 && weekDay <= 5) {
/*  214 */           int hours = cal.get(11);
/*  215 */           if (hours >= 8 && hours <= 16) {
/*  216 */             File oldFile = new File(ZeusPathCfgUtil.getZeusCfgPath());
/*  217 */             File newFile = new File("Zeus.log");
/*  218 */             if (newFile.exists()) {
/*  219 */               newFile.delete();
/*      */             }
/*  221 */             if (oldFile.renameTo(newFile)) {
/*  222 */               Functions.updateRefDate(newFile.getName(), (new SimpleDateFormat("dd-MM-yyyy")).format(new Date()));
/*  223 */               Functions.applicationExit();
/*      */             } 
/*      */           } 
/*      */         } 
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/*      */       try {
/*  232 */         if (threadDataServer != null && tds != null && (
/*  233 */           !threadDataServer.isAlive() || !tds.flag)) {
/*  234 */           tds.flag = false;
/*  235 */           threadDataServer.interrupt();
/*  236 */           tds = null;
/*  237 */           startThreadTCPDataServer();
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/*  242 */         if (threadUDPDataServer != null && uds != null && (
/*  243 */           !threadUDPDataServer.isAlive() || !uds.flag)) {
/*  244 */           uds.flag = false;
/*  245 */           threadUDPDataServer.interrupt();
/*  246 */           uds = null;
/*  247 */           startThreadUDPDataServer();
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/*  252 */         if (threadMessageServer != null && tms != null && (
/*  253 */           !threadMessageServer.isAlive() || !tms.flag)) {
/*  254 */           tms.flag = false;
/*  255 */           threadMessageServer.interrupt();
/*  256 */           tms = null;
/*  257 */           startThreadTCPMessageServer();
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/*  262 */         if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().size() > 0) {
/*  263 */           if (threadEventLoader != null && eventLoader != null && (
/*  264 */             !threadEventLoader.isAlive() || !eventLoader.flag)) {
/*  265 */             eventLoader.flag = false;
/*  266 */             threadEventLoader.interrupt();
/*  267 */             eventLoader = null;
/*  268 */             startEventLoader();
/*      */           } 
/*      */ 
/*      */           
/*  272 */           for (Map.Entry<String, EmulateReceiver> receiver : (Iterable<Map.Entry<String, EmulateReceiver>>)TblThreadsEmulaReceivers.getInstance().entrySet()) {
/*  273 */             if (!((EmulateReceiver)receiver.getValue()).myThread.isAlive() || ((EmulateReceiver)receiver.getValue()).wdt.longValue() < System.currentTimeMillis()) {
/*  274 */               ((EmulateReceiver)receiver.getValue()).flag = false;
/*  275 */               UILogInitiator.toggleImageById((short)1, false, receiver.getKey());
/*  276 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_receiver_emulation_task_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  277 */               GlobalVariables.buzzerActivated = true;
/*  278 */               if (isZeusWatchDogRunning()) {
/*  279 */                 resetTasks(null);
/*      */                 
/*      */                 return;
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         } 
/*      */         
/*  287 */         if (ZeusServerCfg.getInstance().getEnableSerialMux()) {
/*  288 */           if (threadMultiplexdorSerial != null) {
/*  289 */             if (!threadMultiplexdorSerial.isAlive() || SerialMux.wdt.longValue() < System.currentTimeMillis()) {
/*  290 */               serialMux.flag = false;
/*  291 */               threadMultiplexdorSerial.interrupt();
/*  292 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_serial_multiplexer_task_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  293 */               GlobalVariables.buzzerActivated = true;
/*  294 */               if (isZeusWatchDogRunning()) {
/*  295 */                 resetTasks(null);
/*      */                 return;
/*      */               } 
/*  298 */               startThreadMultiplexdorSerail();
/*      */             } 
/*      */           } else {
/*  301 */             if (isZeusWatchDogRunning()) {
/*  302 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  305 */             startThreadMultiplexdorSerail();
/*      */           } 
/*      */         }
/*      */ 
/*      */         
/*  310 */         if (ZeusServerCfg.getInstance().getEnableAutomaticBackup()) {
/*  311 */           if (threadDBBackup != null) {
/*  312 */             if (!DerbyDBBackup.backupModeActivated && (
/*  313 */               !threadDBBackup.isAlive() || DerbyDBBackup.wdt < System.currentTimeMillis())) {
/*  314 */               dbBackup.flag = false;
/*  315 */               threadDBBackup.interrupt();
/*  316 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_database_backup_task_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  317 */               GlobalVariables.buzzerActivated = true;
/*  318 */               if (isZeusWatchDogRunning()) {
/*  319 */                 resetTasks(null);
/*      */                 return;
/*      */               } 
/*  322 */               startThreadDBBackup(false);
/*      */             }
/*      */           
/*      */           } else {
/*      */             
/*  327 */             if (isZeusWatchDogRunning()) {
/*  328 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  331 */             startThreadDBBackup(false);
/*      */           } 
/*      */         }
/*      */ 
/*      */         
/*  336 */         if (threadCheckOccurrences != null) {
/*  337 */           if (!threadCheckOccurrences.isAlive() || ChkOccurrences.wdt.longValue() < System.currentTimeMillis()) {
/*  338 */             checkOccurrences.flag = false;
/*  339 */             threadCheckOccurrences.interrupt();
/*  340 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_verification_of_the_occurrences_stopped") + " (Pegasus)", Enums.EnumMessagePriority.HIGH, null, null);
/*  341 */             GlobalVariables.buzzerActivated = true;
/*  342 */             if (isZeusWatchDogRunning()) {
/*  343 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  346 */             startThreadCheckOccurances();
/*      */           } 
/*      */         } else {
/*  349 */           if (isZeusWatchDogRunning()) {
/*  350 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  353 */           startThreadCheckOccurances();
/*      */         } 
/*      */ 
/*      */         
/*  357 */         if (threadProcessAlivePackets != null) {
/*  358 */           if (!threadProcessAlivePackets.isAlive() || ProcessPegasusAlivePackets.wdt.longValue() < System.currentTimeMillis()) {
/*  359 */             processAlivePackets.flag = false;
/*  360 */             threadProcessAlivePackets.interrupt();
/*  361 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_tasks_for_processing_of_ALIVE_packets_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  362 */             GlobalVariables.buzzerActivated = true;
/*  363 */             if (isZeusWatchDogRunning()) {
/*  364 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  367 */             startThreadProcessPacketsAlive();
/*      */           } 
/*      */         } else {
/*  370 */           if (isZeusWatchDogRunning()) {
/*  371 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  374 */           startThreadProcessPacketsAlive();
/*      */         } 
/*      */ 
/*      */         
/*  378 */         if (threadGenerateEvents != null) {
/*  379 */           if (!threadGenerateEvents.isAlive() || GenerateEvents.wdt.longValue() < System.currentTimeMillis()) {
/*  380 */             generateEvents.flag = false;
/*  381 */             threadGenerateEvents.interrupt();
/*  382 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_event_generation_task_and_transmission_of_e-mails_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  383 */             GlobalVariables.buzzerActivated = true;
/*  384 */             if (isZeusWatchDogRunning()) {
/*  385 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  388 */             startThread2GenerateEventsAndMails();
/*      */           } 
/*      */         } else {
/*  391 */           if (isZeusWatchDogRunning()) {
/*  392 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  395 */           startThread2GenerateEventsAndMails();
/*      */         } 
/*      */ 
/*      */         
/*  399 */         if (ZeusServerCfg.getInstance().getServersTestInternet() != null && ZeusServerCfg.getInstance().getServersTestInternet().length() > 0) {
/*  400 */           if (threadChkInternetLink != null) {
/*  401 */             if (!threadChkInternetLink.isAlive() || ChkInternetLink.wdt.longValue() < System.currentTimeMillis()) {
/*  402 */               chkInternetLink.flag = false;
/*  403 */               threadChkInternetLink.interrupt();
/*  404 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_testing_of_the_internet_connection_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  405 */               GlobalVariables.buzzerActivated = true;
/*  406 */               if (isZeusWatchDogRunning()) {
/*  407 */                 resetTasks(null);
/*      */                 return;
/*      */               } 
/*  410 */               startThreadCheckInternetLink();
/*      */             } 
/*      */           } else {
/*  413 */             if (isZeusWatchDogRunning()) {
/*  414 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  417 */             startThreadCheckInternetLink();
/*      */           } 
/*      */         }
/*      */ 
/*      */         
/*  422 */         if (ZeusServerCfg.getInstance().getDDNSClientSettings() != null && ZeusServerCfg.getInstance().getDDNSClientSettings().length() > 0) {
/*  423 */           if (threadDDNSClient != null) {
/*  424 */             if (!threadDDNSClient.isAlive() || DDNSClient.wdt.longValue() < System.currentTimeMillis()) {
/*  425 */               ddnsClient.flag = false;
/*  426 */               threadDDNSClient.interrupt();
/*  427 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_DDNS_client_task_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  428 */               GlobalVariables.buzzerActivated = true;
/*  429 */               if (isZeusWatchDogRunning()) {
/*  430 */                 resetTasks(null);
/*      */                 return;
/*      */               } 
/*  433 */               startThreadDDNSClient();
/*      */             } 
/*      */           } else {
/*  436 */             if (isZeusWatchDogRunning()) {
/*  437 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  440 */             startThreadDDNSClient();
/*      */           } 
/*      */         }
/*      */ 
/*      */         
/*  445 */         if (threadChkNewCommands != null) {
/*  446 */           if (!threadChkNewCommands.isAlive() || ChkNewCommands.wdt.longValue() < System.currentTimeMillis()) {
/*  447 */             checkNewCommands.flag = false;
/*  448 */             threadChkNewCommands.interrupt();
/*  449 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_verification_of_the_NEW_COMMANDS_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  450 */             GlobalVariables.buzzerActivated = true;
/*  451 */             if (isZeusWatchDogRunning()) {
/*  452 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  455 */             startThread2CheckNewCommands();
/*      */           } 
/*      */         } else {
/*  458 */           if (isZeusWatchDogRunning()) {
/*  459 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  462 */           startThread2CheckNewCommands();
/*      */         } 
/*      */ 
/*      */         
/*  466 */         if (ZeusServerCfg.getInstance().getRecipientsCSD() != null && ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/*  467 */           synchronized (TblThreadsCommReceiversCSD.getInstance()) {
/*  468 */             for (Map.Entry<String, CommReceiverCSD> receiver : (Iterable<Map.Entry<String, CommReceiverCSD>>)TblThreadsCommReceiversCSD.getInstance().entrySet()) {
/*  469 */               if (!((CommReceiverCSD)receiver.getValue()).myThread.isAlive() || ((CommReceiverCSD)receiver.getValue()).wdt.longValue() < System.currentTimeMillis()) {
/*  470 */                 ((CommReceiverCSD)receiver.getValue()).flag = false;
/*  471 */                 ((CommReceiverCSD)receiver.getValue()).myThread.interrupt();
/*  472 */                 UILogInitiator.toggleImageById((short)3, false, ((CommReceiverCSD)receiver.getValue()).cfgReceiverCSD.getSerialPort());
/*  473 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_one_of_the_communication_tasks_with_the_CSD/SMS_receivers_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  474 */                 GlobalVariables.buzzerActivated = true;
/*  475 */                 if (isZeusWatchDogRunning()) {
/*  476 */                   resetTasks(null);
/*      */ 
/*      */                   
/*      */                   return;
/*      */                 } 
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         }
/*      */         
/*  486 */         if (threadGriffonProcessAlivePackets != null) {
/*  487 */           if (!threadGriffonProcessAlivePackets.isAlive() || ProcessAlivePackets.wdt.longValue() < System.currentTimeMillis()) {
/*  488 */             processGriffonAlivePackets.flag = false;
/*  489 */             threadGriffonProcessAlivePackets.interrupt();
/*  490 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_tasks_for_processing_of_ALIVE_packets_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  491 */             GlobalVariables.buzzerActivated = true;
/*  492 */             if (isZeusWatchDogRunning()) {
/*  493 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  496 */             startThreadGriffonProcessPacketsAlive();
/*      */           } 
/*      */         } else {
/*  499 */           if (isZeusWatchDogRunning()) {
/*  500 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  503 */           startThreadGriffonProcessPacketsAlive();
/*      */         } 
/*      */         
/*  506 */         if (threadGriffonGenerateEvents != null) {
/*  507 */           if (!threadGriffonGenerateEvents.isAlive() || GenerateEvents.wdt.longValue() < System.currentTimeMillis()) {
/*  508 */             generateGriffonEvents.flag = false;
/*  509 */             threadGriffonGenerateEvents.interrupt();
/*  510 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_event_generation_task_and_transmission_of_e-mails_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  511 */             GlobalVariables.buzzerActivated = true;
/*  512 */             if (isZeusWatchDogRunning()) {
/*  513 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  516 */             startThreadGriffonGenerateEventsAndMails();
/*      */           } 
/*      */         } else {
/*  519 */           if (isZeusWatchDogRunning()) {
/*  520 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  523 */           startThreadGriffonGenerateEventsAndMails();
/*      */         } 
/*      */ 
/*      */         
/*  527 */         if (threadGriffonChkNewCommands != null) {
/*  528 */           if (!threadGriffonChkNewCommands.isAlive() || ChkNewCommands.wdt.longValue() < System.currentTimeMillis()) {
/*  529 */             checkGriffonNewCommands.flag = false;
/*  530 */             threadGriffonChkNewCommands.interrupt();
/*  531 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_verification_of_the_NEW_COMMANDS_stopped"), Enums.EnumMessagePriority.HIGH, null, null);
/*  532 */             GlobalVariables.buzzerActivated = true;
/*  533 */             if (isZeusWatchDogRunning()) {
/*  534 */               resetTasks(null);
/*      */               return;
/*      */             } 
/*  537 */             startThreadGriffonCheckNewCommands();
/*      */           } 
/*      */         } else {
/*  540 */           if (isZeusWatchDogRunning()) {
/*  541 */             resetTasks(null);
/*      */             return;
/*      */           } 
/*  544 */           startThreadGriffonCheckNewCommands();
/*      */         } 
/*      */         
/*  547 */         if (threadGriffonCheckOccurrences != null) {
/*  548 */           if (!threadGriffonCheckOccurrences.isAlive() || ChkOccurrences.wdt.longValue() < System.currentTimeMillis()) {
/*  549 */             checkGriffonOccurrences.flag = false;
/*  550 */             threadGriffonCheckOccurrences.interrupt();
/*  551 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("ATTENTION_It_was_detected_that_the_task_for_verification_of_the_occurrences_stopped") + " (Griffon)", Enums.EnumMessagePriority.HIGH, null, null);
/*  552 */             GlobalVariables.buzzerActivated = true;
/*  553 */             if (isZeusWatchDogRunning()) {
/*  554 */               resetTasks(null);
/*      */             } else {
/*  556 */               startThreadGriffonCheckOccurances();
/*      */             } 
/*      */           } 
/*  559 */         } else if (isZeusWatchDogRunning()) {
/*  560 */           resetTasks(null);
/*      */         } else {
/*  562 */           startThreadGriffonCheckOccurances();
/*      */         } 
/*      */       } finally {
/*      */         
/*  566 */         GlobalVariables.mainTimerRoutineRunning = false;
/*      */       } 
/*      */     } 
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
/*      */   public static void main(String[] args) {
/*      */     // Byte code:
/*      */     //   0: new java/io/File
/*      */     //   3: dup
/*      */     //   4: getstatic com/zeusServer/util/Main.platformFilePath : Ljava/lang/String;
/*      */     //   7: invokespecial <init> : (Ljava/lang/String;)V
/*      */     //   10: astore_1
/*      */     //   11: aload_1
/*      */     //   12: invokevirtual exists : ()Z
/*      */     //   15: ifeq -> 64
/*      */     //   18: new java/io/BufferedReader
/*      */     //   21: dup
/*      */     //   22: new java/io/FileReader
/*      */     //   25: dup
/*      */     //   26: aload_1
/*      */     //   27: invokespecial <init> : (Ljava/io/File;)V
/*      */     //   30: invokespecial <init> : (Ljava/io/Reader;)V
/*      */     //   33: astore_2
/*      */     //   34: aload_2
/*      */     //   35: invokevirtual readLine : ()Ljava/lang/String;
/*      */     //   38: dup
/*      */     //   39: astore_3
/*      */     //   40: ifnull -> 61
/*      */     //   43: aload_3
/*      */     //   44: ldc 'ARMv7'
/*      */     //   46: invokevirtual contains : (Ljava/lang/CharSequence;)Z
/*      */     //   49: ifeq -> 34
/*      */     //   52: getstatic com/zeusServer/util/Enums$Platform.ARM : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   55: putstatic com/zeusServer/util/GlobalVariables.currentPlatform : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   58: goto -> 61
/*      */     //   61: goto -> 70
/*      */     //   64: invokestatic detectPlatform : ()Lcom/zeusServer/util/Enums$Platform;
/*      */     //   67: putstatic com/zeusServer/util/GlobalVariables.currentPlatform : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   70: getstatic com/zeus/settings/beans/Util$EnumProductIDs.ZEUS : Lcom/zeus/settings/beans/Util$EnumProductIDs;
/*      */     //   73: new java/lang/StringBuilder
/*      */     //   76: dup
/*      */     //   77: invokespecial <init> : ()V
/*      */     //   80: ldc 'Starting'
/*      */     //   82: invokestatic getLocaleMessage : (Ljava/lang/String;)Ljava/lang/String;
/*      */     //   85: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   88: ldc 'full_version'
/*      */     //   90: invokestatic getLocaleMessage : (Ljava/lang/String;)Ljava/lang/String;
/*      */     //   93: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   96: ldc ' ...'
/*      */     //   98: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   101: invokevirtual toString : ()Ljava/lang/String;
/*      */     //   104: getstatic com/zeusServer/util/Enums$EnumMessagePriority.HIGH : Lcom/zeusServer/util/Enums$EnumMessagePriority;
/*      */     //   107: aconst_null
/*      */     //   108: aconst_null
/*      */     //   109: invokestatic printMessage : (Lcom/zeus/settings/beans/Util$EnumProductIDs;Ljava/lang/String;Lcom/zeusServer/util/Enums$EnumMessagePriority;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   112: ldc2_w 30000
/*      */     //   115: invokestatic sleep : (J)V
/*      */     //   118: iconst_0
/*      */     //   119: istore_2
/*      */     //   120: ldc 'ZeusDerby'
/*      */     //   122: invokestatic getServiceControllerByName : (Ljava/lang/String;)Lcom/zeusServer/service/controller/ServiceController;
/*      */     //   125: invokeinterface getServiceStatus : ()I
/*      */     //   130: iconst_4
/*      */     //   131: if_icmpne -> 137
/*      */     //   134: goto -> 223
/*      */     //   137: iinc #2, 1
/*      */     //   140: iload_2
/*      */     //   141: bipush #10
/*      */     //   143: if_icmplt -> 183
/*      */     //   146: ldc 'ZeusDerby'
/*      */     //   148: invokestatic getServiceControllerByName : (Ljava/lang/String;)Lcom/zeusServer/service/controller/ServiceController;
/*      */     //   151: invokeinterface startService : ()Z
/*      */     //   156: pop
/*      */     //   157: ldc2_w 30000
/*      */     //   160: invokestatic sleep : (J)V
/*      */     //   163: goto -> 183
/*      */     //   166: astore_3
/*      */     //   167: ldc com/zeusServer/util/Main
/*      */     //   169: invokevirtual getName : ()Ljava/lang/String;
/*      */     //   172: invokestatic getLogger : (Ljava/lang/String;)Ljava/util/logging/Logger;
/*      */     //   175: getstatic java/util/logging/Level.SEVERE : Ljava/util/logging/Level;
/*      */     //   178: aconst_null
/*      */     //   179: aload_3
/*      */     //   180: invokevirtual log : (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   183: ldc2_w 1000
/*      */     //   186: invokestatic sleep : (J)V
/*      */     //   189: goto -> 209
/*      */     //   192: astore_3
/*      */     //   193: ldc com/zeusServer/util/Main
/*      */     //   195: invokevirtual getName : ()Ljava/lang/String;
/*      */     //   198: invokestatic getLogger : (Ljava/lang/String;)Ljava/util/logging/Logger;
/*      */     //   201: getstatic java/util/logging/Level.SEVERE : Ljava/util/logging/Level;
/*      */     //   204: aconst_null
/*      */     //   205: aload_3
/*      */     //   206: invokevirtual log : (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   209: ldc 'ZeusDerby'
/*      */     //   211: invokestatic getServiceControllerByName : (Ljava/lang/String;)Lcom/zeusServer/service/controller/ServiceController;
/*      */     //   214: invokeinterface getServiceStatus : ()I
/*      */     //   219: iconst_4
/*      */     //   220: if_icmpne -> 120
/*      */     //   223: ldc 'org.apache.derby.jdbc.ClientDriver'
/*      */     //   225: invokestatic forName : (Ljava/lang/String;)Ljava/lang/Class;
/*      */     //   228: pop
/*      */     //   229: new java/lang/StringBuilder
/*      */     //   232: dup
/*      */     //   233: invokespecial <init> : ()V
/*      */     //   236: ldc 'jdbc:derby://'
/*      */     //   238: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   241: invokestatic getInstance : ()Lcom/zeusServer/util/ZeusServerCfg;
/*      */     //   244: invokevirtual getDbServer : ()Ljava/lang/String;
/*      */     //   247: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   250: ldc ':'
/*      */     //   252: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   255: invokestatic getInstance : ()Lcom/zeusServer/util/ZeusServerCfg;
/*      */     //   258: invokevirtual getDbServerPort : ()I
/*      */     //   261: invokevirtual append : (I)Ljava/lang/StringBuilder;
/*      */     //   264: ldc '/'
/*      */     //   266: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   269: invokestatic getInstance : ()Lcom/zeusServer/util/ZeusServerCfg;
/*      */     //   272: invokevirtual getDbFile : ()Ljava/lang/String;
/*      */     //   275: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   278: ldc ';upgrade=true'
/*      */     //   280: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   283: invokevirtual toString : ()Ljava/lang/String;
/*      */     //   286: invokestatic getConnection : (Ljava/lang/String;)Ljava/sql/Connection;
/*      */     //   289: astore_3
/*      */     //   290: aload_3
/*      */     //   291: invokestatic updateDB : (Ljava/sql/Connection;)V
/*      */     //   294: iconst_0
/*      */     //   295: istore #4
/*      */     //   297: iconst_0
/*      */     //   298: istore #5
/*      */     //   300: iload #5
/*      */     //   302: getstatic com/zeusServer/util/Defines.SCHEMA_NAMES : [Ljava/lang/String;
/*      */     //   305: arraylength
/*      */     //   306: if_icmpge -> 427
/*      */     //   309: getstatic com/zeusServer/util/Defines.SCHEMA_NAMES : [Ljava/lang/String;
/*      */     //   312: iload #5
/*      */     //   314: aaload
/*      */     //   315: astore #6
/*      */     //   317: aload #6
/*      */     //   319: getstatic com/zeusServer/util/Defines.T_COUNT : [I
/*      */     //   322: iload #5
/*      */     //   324: iaload
/*      */     //   325: getstatic com/zeusServer/util/Defines.P_COUNT : [I
/*      */     //   328: iload #5
/*      */     //   330: iaload
/*      */     //   331: invokestatic validateDB : (Ljava/lang/String;II)Z
/*      */     //   334: istore #7
/*      */     //   336: iinc #5, 1
/*      */     //   339: iload #7
/*      */     //   341: ifne -> 356
/*      */     //   344: iinc #4, 1
/*      */     //   347: iinc #5, -1
/*      */     //   350: ldc2_w 5000
/*      */     //   353: invokestatic sleep : (J)V
/*      */     //   356: iload #4
/*      */     //   358: getstatic com/zeusServer/util/GlobalVariables.currentPlatform : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   361: getstatic com/zeusServer/util/Enums$Platform.ARM : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   364: if_acmpne -> 372
/*      */     //   367: bipush #25
/*      */     //   369: goto -> 374
/*      */     //   372: bipush #10
/*      */     //   374: if_icmplt -> 412
/*      */     //   377: getstatic com/zeus/settings/beans/Util$EnumProductIDs.ZEUS : Lcom/zeus/settings/beans/Util$EnumProductIDs;
/*      */     //   380: new java/lang/StringBuilder
/*      */     //   383: dup
/*      */     //   384: invokespecial <init> : ()V
/*      */     //   387: ldc 'DB SCHEMA ARE NOT VALID : '
/*      */     //   389: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   392: aload #6
/*      */     //   394: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   397: invokevirtual toString : ()Ljava/lang/String;
/*      */     //   400: getstatic com/zeusServer/util/Enums$EnumMessagePriority.HIGH : Lcom/zeusServer/util/Enums$EnumMessagePriority;
/*      */     //   403: aconst_null
/*      */     //   404: aconst_null
/*      */     //   405: invokestatic printMessage : (Lcom/zeus/settings/beans/Util$EnumProductIDs;Ljava/lang/String;Lcom/zeusServer/util/Enums$EnumMessagePriority;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   408: iconst_1
/*      */     //   409: invokestatic exit : (I)V
/*      */     //   412: iload #5
/*      */     //   414: getstatic com/zeusServer/util/Defines.SCHEMA_NAMES : [Ljava/lang/String;
/*      */     //   417: arraylength
/*      */     //   418: if_icmplt -> 424
/*      */     //   421: goto -> 440
/*      */     //   424: goto -> 300
/*      */     //   427: goto -> 297
/*      */     //   430: astore #5
/*      */     //   432: aload #5
/*      */     //   434: invokevirtual printStackTrace : ()V
/*      */     //   437: goto -> 297
/*      */     //   440: ldc 'derby.locks.monitor'
/*      */     //   442: ldc 'true'
/*      */     //   444: ldc 'PEGASUS'
/*      */     //   446: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   449: ldc 'derby.locks.monitor'
/*      */     //   451: ldc 'true'
/*      */     //   453: ldc 'GRIFFON'
/*      */     //   455: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   458: ldc 'derby.locks.monitor'
/*      */     //   460: ldc 'true'
/*      */     //   462: ldc 'MERCURIUS'
/*      */     //   464: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   467: ldc 'derby.locks.monitor'
/*      */     //   469: ldc 'true'
/*      */     //   471: ldc 'ZEUSSETTINGS'
/*      */     //   473: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   476: ldc 'derby.locks.deadlockTrace'
/*      */     //   478: ldc 'true'
/*      */     //   480: ldc 'PEGASUS'
/*      */     //   482: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   485: ldc 'derby.locks.deadlockTrace'
/*      */     //   487: ldc 'true'
/*      */     //   489: ldc 'GRIFFON'
/*      */     //   491: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   494: ldc 'derby.locks.deadlockTrace'
/*      */     //   496: ldc 'true'
/*      */     //   498: ldc 'MERCURIUS'
/*      */     //   500: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   503: ldc 'derby.locks.deadlockTrace'
/*      */     //   505: ldc 'true'
/*      */     //   507: ldc 'ZEUSSETTINGS'
/*      */     //   509: invokestatic setDatabaseProperty : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   512: invokestatic currentThread : ()Ljava/lang/Thread;
/*      */     //   515: invokevirtual getThreadGroup : ()Ljava/lang/ThreadGroup;
/*      */     //   518: putstatic com/zeusServer/util/Main.zeusThreadGroup : Ljava/lang/ThreadGroup;
/*      */     //   521: invokestatic getInstance : ()Lcom/zeusServer/util/ZeusServerCfg;
/*      */     //   524: invokevirtual getLanguageID : ()Ljava/lang/String;
/*      */     //   527: invokestatic createInstance : (Ljava/lang/String;)V
/*      */     //   530: new com/zeusServer/util/Main$2
/*      */     //   533: dup
/*      */     //   534: invokespecial <init> : ()V
/*      */     //   537: invokestatic invokeLater : (Ljava/lang/Runnable;)V
/*      */     //   540: iconst_1
/*      */     //   541: iconst_1
/*      */     //   542: invokestatic startTasks : (ZZ)V
/*      */     //   545: getstatic com/zeusServer/util/GlobalVariables.currentPlatform : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   548: getstatic com/zeusServer/util/Enums$Platform.ARM : Lcom/zeusServer/util/Enums$Platform;
/*      */     //   551: if_acmpne -> 578
/*      */     //   554: new com/zeusServer/util/Main$3
/*      */     //   557: dup
/*      */     //   558: invokespecial <init> : ()V
/*      */     //   561: astore #5
/*      */     //   563: aload #5
/*      */     //   565: ldc2_w 15000
/*      */     //   568: ldc2_w 30000
/*      */     //   571: getstatic java/util/concurrent/TimeUnit.MILLISECONDS : Ljava/util/concurrent/TimeUnit;
/*      */     //   574: invokestatic addRunnable2ScheduleExecutor : (Ljava/lang/Runnable;JJLjava/util/concurrent/TimeUnit;)Ljava/util/concurrent/ScheduledFuture;
/*      */     //   577: pop
/*      */     //   578: goto -> 606
/*      */     //   581: astore_1
/*      */     //   582: ldc com/zeusServer/util/Main
/*      */     //   584: invokevirtual getName : ()Ljava/lang/String;
/*      */     //   587: invokestatic getLogger : (Ljava/lang/String;)Ljava/util/logging/Logger;
/*      */     //   590: getstatic java/util/logging/Level.SEVERE : Ljava/util/logging/Level;
/*      */     //   593: aconst_null
/*      */     //   594: aload_1
/*      */     //   595: invokevirtual log : (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   598: goto -> 606
/*      */     //   601: astore_1
/*      */     //   602: aload_1
/*      */     //   603: invokevirtual printStackTrace : ()V
/*      */     //   606: return
/*      */     // Line number table:
/*      */     //   Java source line number -> byte code offset
/*      */     //   #576	-> 0
/*      */     //   #577	-> 11
/*      */     //   #578	-> 18
/*      */     //   #580	-> 34
/*      */     //   #581	-> 43
/*      */     //   #582	-> 52
/*      */     //   #583	-> 58
/*      */     //   #586	-> 61
/*      */     //   #587	-> 64
/*      */     //   #590	-> 70
/*      */     //   #593	-> 112
/*      */     //   #595	-> 118
/*      */     //   #597	-> 120
/*      */     //   #598	-> 134
/*      */     //   #600	-> 137
/*      */     //   #601	-> 140
/*      */     //   #602	-> 146
/*      */     //   #604	-> 157
/*      */     //   #607	-> 163
/*      */     //   #605	-> 166
/*      */     //   #606	-> 167
/*      */     //   #610	-> 183
/*      */     //   #613	-> 189
/*      */     //   #611	-> 192
/*      */     //   #612	-> 193
/*      */     //   #615	-> 209
/*      */     //   #619	-> 223
/*      */     //   #620	-> 229
/*      */     //   #621	-> 290
/*      */     //   #624	-> 294
/*      */     //   #628	-> 297
/*      */     //   #629	-> 309
/*      */     //   #630	-> 317
/*      */     //   #631	-> 336
/*      */     //   #632	-> 339
/*      */     //   #633	-> 344
/*      */     //   #634	-> 347
/*      */     //   #635	-> 350
/*      */     //   #638	-> 356
/*      */     //   #639	-> 377
/*      */     //   #640	-> 408
/*      */     //   #642	-> 412
/*      */     //   #643	-> 421
/*      */     //   #645	-> 424
/*      */     //   #648	-> 427
/*      */     //   #646	-> 430
/*      */     //   #647	-> 432
/*      */     //   #648	-> 437
/*      */     //   #652	-> 440
/*      */     //   #653	-> 449
/*      */     //   #654	-> 458
/*      */     //   #655	-> 467
/*      */     //   #656	-> 476
/*      */     //   #657	-> 485
/*      */     //   #658	-> 494
/*      */     //   #659	-> 503
/*      */     //   #661	-> 512
/*      */     //   #664	-> 521
/*      */     //   #666	-> 530
/*      */     //   #673	-> 540
/*      */     //   #675	-> 545
/*      */     //   #676	-> 554
/*      */     //   #687	-> 563
/*      */     //   #694	-> 578
/*      */     //   #690	-> 581
/*      */     //   #691	-> 582
/*      */     //   #694	-> 598
/*      */     //   #692	-> 601
/*      */     //   #693	-> 602
/*      */     //   #695	-> 606
/*      */     // Local variable table:
/*      */     //   start	length	slot	name	descriptor
/*      */     //   34	27	2	br	Ljava/io/BufferedReader;
/*      */     //   40	21	3	line	Ljava/lang/String;
/*      */     //   167	16	3	ex	Ljava/lang/InterruptedException;
/*      */     //   193	16	3	ex	Ljava/lang/InterruptedException;
/*      */     //   317	107	6	schemaName	Ljava/lang/String;
/*      */     //   336	88	7	valid	Z
/*      */     //   300	127	5	index	I
/*      */     //   432	5	5	ex	Ljava/lang/Exception;
/*      */     //   563	15	5	boxDashBoardLoader	Ljava/lang/Runnable;
/*      */     //   11	567	1	platform	Ljava/io/File;
/*      */     //   120	458	2	dbRetires	I
/*      */     //   290	288	3	con	Ljava/sql/Connection;
/*      */     //   297	281	4	dbValidateCount	I
/*      */     //   582	16	1	ex	Lorg/hyperic/sigar/SigarException;
/*      */     //   602	4	1	ex	Ljava/lang/Exception;
/*      */     //   0	607	0	args	[Ljava/lang/String;
/*      */     // Exception table:
/*      */     //   from	to	target	type
/*      */     //   0	578	581	org/hyperic/sigar/SigarException
/*      */     //   0	578	601	java/lang/Exception
/*      */     //   157	163	166	java/lang/InterruptedException
/*      */     //   183	189	192	java/lang/InterruptedException
/*      */     //   297	421	430	java/lang/ClassNotFoundException
/*      */     //   297	421	430	java/lang/InterruptedException
/*      */     //   297	421	430	java/sql/SQLException
/*      */     //   424	427	430	java/lang/ClassNotFoundException
/*      */     //   424	427	430	java/lang/InterruptedException
/*      */     //   424	427	430	java/sql/SQLException
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
/*      */   private static ZeusBoxDashBoard readZeusBoxDashBoard() {
/*  698 */     ZeusBoxDashBoard dashboard = new ZeusBoxDashBoard();
/*  699 */     ZeusBoxNativeLibrary.nGetZeusBoxDashBoardData(dashboard);
/*  700 */     return dashboard;
/*      */   }
/*      */   
/*      */   private static void startEventLoader() {
/*      */     try {
/*  705 */       if (eventLoader == null) {
/*  706 */         eventLoader = new EventLoader();
/*  707 */         eventLoader.flag = true;
/*  708 */         threadEventLoader = new Thread((Runnable)eventLoader);
/*  709 */         threadEventLoader.setName("EventLoader");
/*  710 */         threadEventLoader.setDaemon(true);
/*  711 */         threadEventLoader.start();
/*      */       }
/*      */     
/*  714 */     } catch (Exception ex) {
/*  715 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_Event_Loader_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  716 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadTCPDataServer() {
/*      */     try {
/*  722 */       if (tds == null) {
/*  723 */         tds = new TCPDataServer(ZeusServerCfg.getInstance().getDataServerIP(), ZeusServerCfg.getInstance().getDataServerPort().intValue(), true);
/*  724 */         threadDataServer = new Thread((Runnable)tds);
/*  725 */         threadDataServer.setName("TCPDataServer");
/*  726 */         threadDataServer.setDaemon(true);
/*  727 */         threadDataServer.start();
/*      */       } 
/*  729 */       tds.flag = true;
/*  730 */       tds.dataServerRunning = true;
/*  731 */     } catch (Exception ex) {
/*  732 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_Data_Server_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  733 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadUDPDataServer() {
/*      */     try {
/*  739 */       if (uds == null) {
/*  740 */         uds = new UDPDataServer(ZeusServerCfg.getInstance().getUdpDataServerIP(), ZeusServerCfg.getInstance().getUdpDataServerPort().intValue(), true);
/*  741 */         threadUDPDataServer = new Thread((Runnable)uds);
/*  742 */         threadUDPDataServer.setName("UDPDataServer");
/*  743 */         threadUDPDataServer.setDaemon(true);
/*  744 */         threadUDPDataServer.start();
/*      */       } 
/*  746 */       uds.flag = true;
/*  747 */       uds.udpServerRunning = true;
/*  748 */     } catch (Exception ex) {
/*  749 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_UDP_Data_Server_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  750 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadTCPMessageServer() {
/*      */     try {
/*  756 */       if (tms == null) {
/*  757 */         tms = new TCPMessageServer(ZeusServerCfg.getInstance().getMsgServerIP(), ZeusServerCfg.getInstance().getMsgServerPort().intValue(), true);
/*  758 */         threadMessageServer = new Thread((Runnable)tms);
/*  759 */         threadMessageServer.setName("TCPMessageServer");
/*  760 */         threadMessageServer.setDaemon(true);
/*  761 */         threadMessageServer.start();
/*      */       } 
/*  763 */       tms.flag = true;
/*  764 */       tms.msgServerRunning = true;
/*  765 */     } catch (Exception ex) {
/*  766 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_Message_Server_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  767 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadCheckOccurances() {
/*      */     try {
/*  773 */       if (checkOccurrences == null || !checkOccurrences.flag) {
/*  774 */         checkOccurrences = new ChkOccurrences();
/*  775 */         threadCheckOccurrences = new Thread((Runnable)checkOccurrences);
/*  776 */         threadCheckOccurrences.setName("ChkPegasusOccurrences");
/*  777 */         threadCheckOccurrences.setDaemon(true);
/*  778 */         threadCheckOccurrences.start();
/*      */       } 
/*  780 */     } catch (Exception ex) {
/*  781 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_verification_of_the_occurrences"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  782 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadCheckInternetLink() {
/*      */     try {
/*  788 */       if (chkInternetLink == null || !chkInternetLink.flag) {
/*  789 */         chkInternetLink = new ChkInternetLink();
/*  790 */         threadChkInternetLink = new Thread(chkInternetLink);
/*  791 */         threadChkInternetLink.setName("ChkInternetLink");
/*  792 */         threadChkInternetLink.setDaemon(true);
/*  793 */         threadChkInternetLink.start();
/*      */       } 
/*  795 */     } catch (Exception ex) {
/*  796 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_testing_of_the_Internet_connection"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  797 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadDDNSClient() {
/*      */     try {
/*  803 */       if (ddnsClient == null || !ddnsClient.flag) {
/*  804 */         ddnsClient = new DDNSClient(ZeusServerCfg.getInstance().getDDNSClientSettings());
/*  805 */         threadDDNSClient = new Thread((Runnable)ddnsClient);
/*  806 */         threadDDNSClient.setName("DDNSClient");
/*  807 */         threadDDNSClient.setDaemon(true);
/*  808 */         threadDDNSClient.start();
/*      */       } 
/*  810 */     } catch (Exception ex) {
/*  811 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_DDNS_client_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  812 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadDBBackup(boolean firstTime) {
/*      */     try {
/*  818 */       dbBackup = new DerbyDBBackup(true, firstTime);
/*  819 */       threadDBBackup = new Thread((Runnable)dbBackup);
/*  820 */       threadDBBackup.setName("DerbyDBBackup");
/*  821 */       threadDBBackup.setDaemon(true);
/*  822 */       threadDBBackup.start();
/*  823 */     } catch (Exception ex) {
/*  824 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_backup_of_the_database"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  825 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadMultiplexdorSerail() {
/*      */     try {
/*  831 */       if (serialMux == null) {
/*  832 */         serialMux = new SerialMux();
/*  833 */         threadMultiplexdorSerial = new Thread((Runnable)serialMux);
/*  834 */         threadMultiplexdorSerial.setName("SerialMux");
/*  835 */         threadMultiplexdorSerial.setDaemon(true);
/*  836 */         threadMultiplexdorSerial.start();
/*      */       } 
/*  838 */     } catch (Exception ex) {
/*  839 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_serial_multiplexer_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  840 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadProcessPacketsAlive() {
/*      */     try {
/*  846 */       if (processAlivePackets == null || !processAlivePackets.flag) {
/*  847 */         processAlivePackets = new ProcessPegasusAlivePackets();
/*  848 */         threadProcessAlivePackets = new Thread((Runnable)processAlivePackets);
/*  849 */         threadProcessAlivePackets.setName("ProcessPegasusAlivePackets");
/*  850 */         threadProcessAlivePackets.setDaemon(true);
/*  851 */         threadProcessAlivePackets.start();
/*      */       } 
/*  853 */     } catch (Exception ex) {
/*  854 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_processing_of_ALIVE_packets"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  855 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThread2GenerateEventsAndMails() {
/*      */     try {
/*  861 */       if (generateEvents == null || !generateEvents.flag) {
/*  862 */         generateEvents = new GenerateEvents();
/*  863 */         threadGenerateEvents = new Thread((Runnable)generateEvents);
/*  864 */         threadGenerateEvents.setName("GeneratePegasusEvents");
/*  865 */         threadGenerateEvents.setDaemon(true);
/*  866 */         threadGenerateEvents.start();
/*      */       } 
/*  868 */     } catch (Exception ex) {
/*  869 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_event_generation_and_transmission_of_e-mails"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  870 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThread2CheckNewCommands() {
/*      */     try {
/*  876 */       if (checkNewCommands == null || !checkNewCommands.flag) {
/*  877 */         checkNewCommands = new ChkNewCommands();
/*  878 */         threadChkNewCommands = new Thread((Runnable)checkNewCommands);
/*  879 */         threadChkNewCommands.setName("ChkPegasusNewCommands");
/*  880 */         threadChkNewCommands.setDaemon(true);
/*  881 */         threadChkNewCommands.start();
/*      */       } 
/*  883 */     } catch (Exception ex) {
/*  884 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_verification_of_the_NEW_COMMANDS"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  885 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadGriffonProcessPacketsAlive() {
/*      */     try {
/*  891 */       if (processGriffonAlivePackets == null || !processGriffonAlivePackets.flag) {
/*  892 */         processGriffonAlivePackets = new ProcessAlivePackets();
/*  893 */         threadGriffonProcessAlivePackets = new Thread((Runnable)processGriffonAlivePackets);
/*  894 */         threadGriffonProcessAlivePackets.setName("ProcessGriffonAlivePackets");
/*  895 */         threadGriffonProcessAlivePackets.setDaemon(true);
/*  896 */         threadGriffonProcessAlivePackets.start();
/*      */       } 
/*  898 */     } catch (Exception ex) {
/*  899 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_processing_of_ALIVE_packets"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  900 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadGriffonGenerateEventsAndMails() {
/*      */     try {
/*  906 */       if (generateGriffonEvents == null || !generateGriffonEvents.flag) {
/*  907 */         generateGriffonEvents = new GenerateEvents();
/*  908 */         threadGriffonGenerateEvents = new Thread((Runnable)generateGriffonEvents);
/*  909 */         threadGriffonGenerateEvents.setName("GenerateGriffonEvents");
/*  910 */         threadGriffonGenerateEvents.setDaemon(true);
/*  911 */         threadGriffonGenerateEvents.start();
/*      */       } 
/*  913 */     } catch (Exception ex) {
/*  914 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_event_generation_and_transmission_of_e-mails"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  915 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadGriffonCheckNewCommands() {
/*      */     try {
/*  921 */       if (checkGriffonNewCommands == null || !checkGriffonNewCommands.flag) {
/*  922 */         checkGriffonNewCommands = new ChkNewCommands();
/*  923 */         threadGriffonChkNewCommands = new Thread((Runnable)checkGriffonNewCommands);
/*  924 */         threadGriffonChkNewCommands.setName("ChkGriffonNewCommands");
/*  925 */         threadGriffonChkNewCommands.setDaemon(true);
/*  926 */         threadGriffonChkNewCommands.start();
/*      */       } 
/*  928 */     } catch (Exception ex) {
/*  929 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_verification_of_the_NEW_COMMANDS"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  930 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadGriffonCheckOccurances() {
/*      */     try {
/*  936 */       if (checkGriffonOccurrences == null || !checkGriffonOccurrences.flag) {
/*  937 */         checkGriffonOccurrences = new ChkOccurrences();
/*  938 */         threadGriffonCheckOccurrences = new Thread((Runnable)checkGriffonOccurrences);
/*  939 */         threadGriffonCheckOccurrences.setName("ChkGriffonOccurrences");
/*  940 */         threadGriffonCheckOccurrences.setDaemon(true);
/*  941 */         threadGriffonCheckOccurrences.start();
/*      */       } 
/*  943 */     } catch (Exception ex) {
/*  944 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_verification_of_the_occurrences"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  945 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadMercuriusAVLProcessPacketsAlive() {
/*      */     try {
/*  951 */       if (processMercuriusAlivePackets == null || !processMercuriusAlivePackets.flag) {
/*  952 */         processMercuriusAlivePackets = new ProcessAlivePackets();
/*  953 */         threadMercuriusProcessAlivePackets = new Thread((Runnable)processMercuriusAlivePackets);
/*  954 */         threadMercuriusProcessAlivePackets.setName("ProcessMercuriusAlivePackets");
/*  955 */         threadMercuriusProcessAlivePackets.setDaemon(true);
/*  956 */         threadMercuriusProcessAlivePackets.start();
/*      */       } 
/*  958 */     } catch (Exception ex) {
/*  959 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_processing_of_ALIVE_packets"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  960 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadMercuriusAVLGenerateEventsAndMails() {
/*      */     try {
/*  966 */       if (generateMercuriusEvents == null || !generateMercuriusEvents.flag) {
/*  967 */         generateMercuriusEvents = new GenerateEvents();
/*  968 */         threadMercuriusGenerateEvents = new Thread((Runnable)generateMercuriusEvents);
/*  969 */         threadMercuriusGenerateEvents.setName("GenerateMercuriusEvents");
/*  970 */         threadMercuriusGenerateEvents.setDaemon(true);
/*  971 */         threadMercuriusGenerateEvents.start();
/*      */       } 
/*  973 */     } catch (Exception ex) {
/*  974 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_event_generation_and_transmission_of_e-mails"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  975 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadMercuriusAVLCheckNewCommands() {
/*      */     try {
/*  981 */       if (checkMercuriusNewCommands == null || !checkMercuriusNewCommands.flag) {
/*  982 */         checkMercuriusNewCommands = new ChkNewCommands();
/*  983 */         threadMercuriusChkNewCommands = new Thread((Runnable)checkMercuriusNewCommands);
/*  984 */         threadMercuriusChkNewCommands.setName("ChkMercuriusNewCommands");
/*  985 */         threadMercuriusChkNewCommands.setDaemon(true);
/*  986 */         threadMercuriusChkNewCommands.start();
/*      */       } 
/*  988 */     } catch (Exception ex) {
/*  989 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_verification_of_the_NEW_COMMANDS"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  990 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   private static void startThreadMercuriusAVLCheckOccurances() {
/*      */     try {
/*  996 */       if (checkMercuriusOccurrences == null || !checkMercuriusOccurrences.flag) {
/*  997 */         checkMercuriusOccurrences = new ChkOccurrences();
/*  998 */         threadMercuriusCheckOccurrences = new Thread((Runnable)checkMercuriusOccurrences);
/*  999 */         threadMercuriusCheckOccurrences.setName("ChkMercuriusOccurrences");
/* 1000 */         threadMercuriusCheckOccurrences.setDaemon(true);
/* 1001 */         threadMercuriusCheckOccurrences.start();
/*      */       } 
/* 1003 */     } catch (Exception ex) {
/* 1004 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_task_for_verification_of_the_occurrences"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1005 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void startTasks(boolean startTaskBackupDB, boolean stopServices) throws SigarException, Exception {
/* 1015 */     while (Functions.getServiceControllerByName("ZeusDerby").getServiceStatus() != 4) {
/*      */ 
/*      */       
/* 1018 */       Functions.getServiceControllerByName("ZeusDerby").startService();
/*      */       try {
/* 1020 */         Thread.sleep(4000L);
/* 1021 */       } catch (InterruptedException ex) {
/* 1022 */         Logger.getLogger(Main.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/*      */       
/* 1025 */       if (Functions.getServiceControllerByName("ZeusDerby").getServiceStatus() == 4)
/*      */         break; 
/*      */     } 
/* 1028 */     if (startTaskBackupDB) {
/*      */       
/* 1030 */       startThreadTCPDataServer();
/*      */ 
/*      */       
/* 1033 */       startThreadUDPDataServer();
/*      */ 
/*      */       
/* 1036 */       startThreadTCPMessageServer();
/*      */     } else {
/*      */       
/* 1039 */       if (tds != null) {
/* 1040 */         tds.dataServerRunning = true;
/*      */       }
/* 1042 */       if (uds != null) {
/* 1043 */         uds.udpServerRunning = true;
/*      */       }
/* 1045 */       if (tms != null) {
/* 1046 */         tms.msgServerRunning = true;
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1055 */     while (Functions.getServiceControllerByName("ZeusServerWatchdog").getServiceStatus() != 4) {
/*      */ 
/*      */       
/*      */       try {
/* 1059 */         Functions.getServiceControllerByName("ZeusServerWatchdog").startService();
/* 1060 */         Thread.sleep(4000L);
/* 1061 */       } catch (InterruptedException ex) {
/* 1062 */         Logger.getLogger(Main.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/*      */       
/* 1065 */       if (Functions.getServiceControllerByName("ZeusServerWatchdog").getServiceStatus() == 4) {
/*      */         break;
/*      */       }
/*      */     } 
/* 1069 */     if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && 
/* 1070 */       ZeusServerCfg.getInstance().getMonitoringInfo().size() > 0 && eventLoader == null) {
/*      */       
/* 1072 */       startEventLoader();
/* 1073 */       EmulateReceiver eReceiver = null;
/*      */       
/* 1075 */       for (Map.Entry<String, MonitoringInfo> receiver : ZeusServerCfg.getInstance().getMonitoringInfo().entrySet()) {
/*      */         try {
/* 1077 */           EmulateReceiverRadionics emulateReceiverRadionics; int receiverType = ((MonitoringInfo)receiver.getValue()).getReceiverType().intValue();
/*      */           
/* 1079 */           if (receiverType == Enums.EnumTipoReceptora.ADEMCO_685.getType()) {
/* 1080 */             EmulateReceiverAdemco685 emulateReceiverAdemco685 = new EmulateReceiverAdemco685(receiver.getValue());
/* 1081 */           } else if (receiverType == Enums.EnumTipoReceptora.SURGARD.getType()) {
/* 1082 */             EmulateReceiverSurgard emulateReceiverSurgard = new EmulateReceiverSurgard(receiver.getValue());
/*      */           }
/* 1084 */           else if (receiverType == Enums.EnumTipoReceptora.SURGARD_VIA_TCPIP.getType()) {
/* 1085 */             EmulaReceiverSurgadViaTCPIP emulaReceiverSurgadViaTCPIP = new EmulaReceiverSurgadViaTCPIP(receiver.getValue());
/* 1086 */           } else if (receiverType == Enums.EnumTipoReceptora.RADIONICS_D6600.getType()) {
/* 1087 */             emulateReceiverRadionics = new EmulateReceiverRadionics(receiver.getValue());
/*      */           } 
/*      */           
/* 1090 */           if (emulateReceiverRadionics == null) {
/* 1091 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Type_of_the_receiver_to_be_emulated_is_invalid"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1092 */             Functions.applicationExit();
/*      */           } 
/* 1094 */           ((EmulateReceiver)emulateReceiverRadionics).flag = true;
/* 1095 */           Thread emRcvr = new Thread((Runnable)emulateReceiverRadionics);
/* 1096 */           emRcvr.setName("EmulateReceiver");
/* 1097 */           ((EmulateReceiver)emulateReceiverRadionics).myThread = emRcvr;
/* 1098 */           emRcvr.setDaemon(true);
/* 1099 */           emRcvr.start();
/* 1100 */           TblThreadsEmulaReceivers.addThread(receiver.getKey(), (EmulateReceiver)emulateReceiverRadionics, emRcvr);
/* 1101 */         } catch (Exception ex) {
/* 1102 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_receiver_emulation_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1103 */           GlobalVariables.buzzerActivated = true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/* 1109 */     if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().size() > 0 && ZeusServerCfg.getInstance().getEnableSerialMux()) {
/* 1110 */       startThreadMultiplexdorSerail();
/*      */     }
/*      */ 
/*      */     
/* 1114 */     if (ZeusServerCfg.getInstance().getEnableSerialPrinter()) {
/* 1115 */       PrinterFunctions.openPrinterSerialPort();
/*      */     }
/*      */ 
/*      */     
/* 1119 */     if (ZeusServerCfg.getInstance().getServersTestInternet() != null && ZeusServerCfg.getInstance().getServersTestInternet().length() > 0) {
/* 1120 */       startThreadCheckInternetLink();
/*      */     }
/*      */ 
/*      */     
/* 1124 */     if (ZeusServerCfg.getInstance().getDDNSClientSettings() != null && ZeusServerCfg.getInstance().getDDNSClientSettings().length() > 0) {
/* 1125 */       startThreadDDNSClient();
/*      */     }
/*      */ 
/*      */     
/* 1129 */     startPegasusTasks();
/*      */ 
/*      */     
/* 1132 */     startGriffonTasks();
/*      */ 
/*      */     
/* 1135 */     startMercuriusAVLTasks();
/*      */ 
/*      */     
/* 1138 */     if (ZeusServerCfg.getInstance().getRecipientsCSD() != null && ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/*      */       
/* 1140 */       String[] rcvrCSD = ZeusServerCfg.getInstance().getRecipientsCSD().split(";");
/* 1141 */       for (String rcvr : rcvrCSD) {
/*      */         try {
/* 1143 */           CommReceiverCSD commRcvrCSD = new CommReceiverCSD(true);
/* 1144 */           commRcvrCSD.cfgReceiverCSD = new CfgReceiverCSD(rcvr);
/* 1145 */           Thread csdRcvr = new Thread((Runnable)commRcvrCSD);
/* 1146 */           csdRcvr.setName("CSDReceiver");
/* 1147 */           commRcvrCSD.myThread = csdRcvr;
/* 1148 */           csdRcvr.setDaemon(true);
/* 1149 */           csdRcvr.start();
/* 1150 */           TblThreadsCommReceiversCSD.addThread(commRcvrCSD);
/* 1151 */         } catch (Exception ex) {
/* 1152 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_starting_communication_task_with_the_CSD/SMS_receiver"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1153 */           GlobalVariables.buzzerActivated = true;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/* 1159 */     if (startTaskBackupDB && ZeusServerCfg.getInstance().getEnableAutomaticBackup()) {
/* 1160 */       startThreadDBBackup(stopServices);
/*      */     }
/*      */     
/* 1163 */     GlobalVariables.mainTimerStopped = false;
/*      */     
/* 1165 */     ZeusSettingsDBManager.clearZeusLiveData();
/* 1166 */     GenericDBManager.dbAutoCleanupDaily();
/*      */     
/* 1168 */     if (startTaskBackupDB) {
/* 1169 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Initialization_of_the_Zeus_Server_tasks_completed_successfully"), Enums.EnumMessagePriority.LOW, null, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   private static void startPegasusTasks() {
/* 1175 */     startThreadCheckOccurances();
/*      */ 
/*      */     
/* 1178 */     startThreadProcessPacketsAlive();
/*      */ 
/*      */     
/* 1181 */     startThread2GenerateEventsAndMails();
/*      */ 
/*      */     
/* 1184 */     startThread2CheckNewCommands();
/*      */   }
/*      */   
/*      */   private static void startGriffonTasks() {
/* 1188 */     startThreadGriffonProcessPacketsAlive();
/*      */     
/* 1190 */     startThreadGriffonCheckNewCommands();
/*      */     
/* 1192 */     startThreadGriffonCheckOccurances();
/*      */     
/* 1194 */     startThreadGriffonGenerateEventsAndMails();
/*      */   }
/*      */   
/*      */   private static void startMercuriusAVLTasks() {
/* 1198 */     startThreadMercuriusAVLProcessPacketsAlive();
/*      */     
/* 1200 */     startThreadMercuriusAVLCheckNewCommands();
/*      */     
/* 1202 */     startThreadMercuriusAVLCheckOccurances();
/*      */     
/* 1204 */     startThreadMercuriusAVLGenerateEventsAndMails();
/*      */   }
/*      */   
/*      */   private static void resetTasks(Boolean closeApp) throws InterruptedException, SigarException, Exception {
/* 1208 */     closeApp = Boolean.valueOf((closeApp != null));
/* 1209 */     noOfServerErrors = (lastTimeServerError + 300000L < System.currentTimeMillis()) ? 1 : (noOfServerErrors + 1);
/* 1210 */     lastTimeServerError = System.currentTimeMillis();
/* 1211 */     finishTasks(true);
/* 1212 */     GenericDBManager.stopDbCleanupDailyTimer();
/* 1213 */     if (noOfServerErrors > 3 || closeApp.booleanValue()) {
/*      */       
/* 1215 */       Functions.applicationExit();
/*      */     } else {
/*      */       
/* 1218 */       startTasks(true, false);
/*      */     } 
/*      */   }
/*      */   
/*      */   public static void finishTasks(boolean finishTaskBackupDB) throws InterruptedException {
/* 1223 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Terminating_Zeus_Server_tasks"), Enums.EnumMessagePriority.LOW, null, null);
/*      */     
/* 1225 */     if (finishTaskBackupDB) {
/* 1226 */       tds.flag = false;
/* 1227 */       if (threadDataServer != null) {
/* 1228 */         threadDataServer.interrupt();
/*      */       }
/* 1230 */       uds.flag = false;
/* 1231 */       if (threadUDPDataServer != null) {
/* 1232 */         threadUDPDataServer.interrupt();
/*      */       }
/* 1234 */       tms.flag = false;
/* 1235 */       if (threadMessageServer != null) {
/* 1236 */         threadMessageServer.interrupt();
/*      */       }
/*      */     } else {
/*      */       
/* 1240 */       tds.dataServerRunning = false;
/*      */ 
/*      */       
/* 1243 */       uds.udpServerRunning = false;
/*      */ 
/*      */       
/* 1246 */       tms.msgServerRunning = false;
/*      */     } 
/*      */     
/* 1249 */     disconnectAllConnections();
/*      */     
/*      */     try {
/* 1252 */       ZeusSettingsDBManager.clearZeusLiveData();
/* 1253 */     } catch (SQLException sQLException) {}
/*      */ 
/*      */ 
/*      */     
/* 1257 */     if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().size() > 0) {
/*      */       
/* 1259 */       if (eventLoader != null) {
/* 1260 */         eventLoader.flag = false;
/* 1261 */         if (threadEventLoader != null) {
/* 1262 */           threadEventLoader.interrupt();
/*      */         }
/* 1264 */         eventLoader = null;
/*      */       } 
/* 1266 */       for (Map.Entry<String, EmulateReceiver> receiver : (Iterable<Map.Entry<String, EmulateReceiver>>)TblThreadsEmulaReceivers.getInstance().entrySet()) {
/* 1267 */         ((EmulateReceiver)receiver.getValue()).flag = false;
/*      */         try {
/* 1269 */           if (((EmulateReceiver)receiver.getValue()).receiverCommPort != null) {
/* 1270 */             ((EmulateReceiver)receiver.getValue()).receiverCommPort.close();
/*      */           }
/* 1272 */         } catch (IOException iOException) {}
/*      */         
/* 1274 */         Thread t = TblThreadsEmulaReceivers.getThread4Receiver(receiver.getKey());
/* 1275 */         if (t != null) {
/* 1276 */           t.interrupt();
/*      */         }
/*      */       } 
/* 1279 */       TblThreadsEmulaReceivers.getInstance().clear();
/* 1280 */       TblThreadsEmulaReceivers.clearThreads();
/*      */     } 
/*      */ 
/*      */     
/* 1284 */     if (ZeusServerCfg.getInstance().getEnableSerialMux()) {
/* 1285 */       serialMux.flag = false;
/* 1286 */       if (threadMultiplexdorSerial != null) {
/* 1287 */         threadMultiplexdorSerial.interrupt();
/*      */       }
/*      */     } 
/*      */ 
/*      */     
/* 1292 */     if (finishTaskBackupDB && ZeusServerCfg.getInstance().getEnableAutomaticBackup()) {
/* 1293 */       dbBackup.flag = false;
/* 1294 */       if (threadDBBackup != null) {
/* 1295 */         threadDBBackup.interrupt();
/*      */       }
/*      */     } 
/*      */ 
/*      */     
/* 1300 */     stopPegasusTasks();
/*      */ 
/*      */     
/* 1303 */     stopGriffonTasks();
/*      */ 
/*      */     
/* 1306 */     stopMercuriusTasks();
/*      */ 
/*      */     
/* 1309 */     if (ZeusServerCfg.getInstance().getServersTestInternet() != null && ZeusServerCfg.getInstance().getServersTestInternet().length() > 0) {
/* 1310 */       chkInternetLink.flag = false;
/* 1311 */       if (threadChkInternetLink != null) {
/* 1312 */         threadChkInternetLink.interrupt();
/*      */       }
/*      */     } 
/*      */     
/* 1316 */     if (ZeusServerCfg.getInstance().getDDNSClientSettings() != null && ZeusServerCfg.getInstance().getDDNSClientSettings().length() > 0) {
/* 1317 */       ddnsClient.flag = false;
/* 1318 */       if (threadDDNSClient != null) {
/* 1319 */         threadDDNSClient.interrupt();
/*      */       }
/*      */     } 
/*      */ 
/*      */     
/* 1324 */     if (ZeusServerCfg.getInstance().getRecipientsCSD() != null && ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/* 1325 */       synchronized (TblThreadsCommReceiversCSD.getInstance()) {
/* 1326 */         for (Map.Entry<String, CommReceiverCSD> receiver : (Iterable<Map.Entry<String, CommReceiverCSD>>)TblThreadsCommReceiversCSD.getInstance().entrySet()) {
/* 1327 */           ((CommReceiverCSD)receiver.getValue()).flag = false;
/* 1328 */           if (((CommReceiverCSD)receiver.getValue()).myThread != null) {
/* 1329 */             ((CommReceiverCSD)receiver.getValue()).myThread.interrupt();
/*      */           }
/*      */         } 
/*      */       } 
/* 1333 */       TblThreadsCommReceiversCSD.getInstance().clear();
/*      */     } 
/*      */ 
/*      */     
/* 1337 */     if (ZeusServerCfg.getInstance().getEnableSerialPrinter()) {
/* 1338 */       while (TblPrinterSpool.getInstance().size() > 0 && PrinterFunctions.printerCommPort != null) {
/* 1339 */         Thread.sleep(100L);
/*      */       }
/* 1341 */       PrinterFunctions.closePrinterSerialPort();
/*      */     } 
/*      */     
/* 1344 */     PegasusPool.closeConnectionPool();
/* 1345 */     GriffonPool.closeConnectionPool();
/* 1346 */     MercuriusPool.closeConnectionPool();
/*      */     
/* 1348 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Termination_of_the_Zeus_Server_tasks_completed_successfully"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */   
/*      */   private static void stopPegasusTasks() {
/* 1352 */     checkOccurrences.flag = false;
/* 1353 */     if (threadCheckOccurrences != null) {
/* 1354 */       threadCheckOccurrences.interrupt();
/*      */     }
/* 1356 */     processAlivePackets.flag = false;
/* 1357 */     if (threadProcessAlivePackets != null) {
/* 1358 */       threadProcessAlivePackets.interrupt();
/*      */     }
/* 1360 */     generateEvents.flag = false;
/* 1361 */     if (threadGenerateEvents != null) {
/* 1362 */       threadGenerateEvents.interrupt();
/*      */     }
/* 1364 */     checkNewCommands.flag = false;
/* 1365 */     if (threadChkNewCommands != null) {
/* 1366 */       threadChkNewCommands.interrupt();
/*      */     }
/*      */   }
/*      */   
/*      */   private static void stopGriffonTasks() {
/* 1371 */     checkGriffonOccurrences.flag = false;
/* 1372 */     if (threadGriffonCheckOccurrences != null) {
/* 1373 */       threadGriffonCheckOccurrences.interrupt();
/*      */     }
/* 1375 */     processGriffonAlivePackets.flag = false;
/* 1376 */     if (threadGriffonProcessAlivePackets != null) {
/* 1377 */       threadGriffonProcessAlivePackets.interrupt();
/*      */     }
/* 1379 */     generateGriffonEvents.flag = false;
/* 1380 */     if (threadGriffonGenerateEvents != null) {
/* 1381 */       threadGriffonGenerateEvents.interrupt();
/*      */     }
/* 1383 */     checkGriffonNewCommands.flag = false;
/* 1384 */     if (threadGriffonChkNewCommands != null) {
/* 1385 */       threadGriffonChkNewCommands.interrupt();
/*      */     }
/*      */   }
/*      */   
/*      */   private static void stopMercuriusTasks() {
/* 1390 */     checkMercuriusOccurrences.flag = false;
/* 1391 */     if (threadMercuriusCheckOccurrences != null) {
/* 1392 */       threadMercuriusCheckOccurrences.interrupt();
/*      */     }
/* 1394 */     processMercuriusAlivePackets.flag = false;
/* 1395 */     if (threadMercuriusProcessAlivePackets != null) {
/* 1396 */       threadMercuriusProcessAlivePackets.interrupt();
/*      */     }
/* 1398 */     generateMercuriusEvents.flag = false;
/* 1399 */     if (threadMercuriusGenerateEvents != null) {
/* 1400 */       threadMercuriusGenerateEvents.interrupt();
/*      */     }
/* 1402 */     checkMercuriusNewCommands.flag = false;
/* 1403 */     if (threadMercuriusChkNewCommands != null) {
/* 1404 */       threadMercuriusChkNewCommands.interrupt();
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void disconnectAllConnections() throws InterruptedException {
/* 1412 */     synchronized (TblActiveUdpConnections.getInstance()) {
/* 1413 */       for (Map.Entry<String, UdpV2Handler> connection : (Iterable<Map.Entry<String, UdpV2Handler>>)TblActiveUdpConnections.getInstance().entrySet()) {
/* 1414 */         ((UdpV2Handler)connection.getValue()).dispose();
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1421 */     synchronized (TblPegasusActiveConnections.getInstance()) {
/* 1422 */       for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblPegasusActiveConnections.getInstance().entrySet()) {
/* 1423 */         ((InfoModule)connection.getValue()).fecharConexao = true;
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1430 */     synchronized (TblGriffonActiveUdpConnections.getInstance()) {
/* 1431 */       for (Map.Entry<String, UdpV2Handler> connection : (Iterable<Map.Entry<String, UdpV2Handler>>)TblGriffonActiveUdpConnections.getInstance().entrySet()) {
/* 1432 */         ((UdpV2Handler)connection.getValue()).dispose();
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1439 */     synchronized (TblGriffonActiveConnections.getInstance()) {
/* 1440 */       for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblGriffonActiveConnections.getInstance().entrySet()) {
/* 1441 */         ((InfoModule)connection.getValue()).fecharConexao = true;
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1448 */     synchronized (TblMercuriusAVLActiveUdpConnections.getInstance()) {
/* 1449 */       for (Map.Entry<String, UdpV2Handler> connection : (Iterable<Map.Entry<String, UdpV2Handler>>)TblMercuriusAVLActiveUdpConnections.getInstance().entrySet()) {
/* 1450 */         ((UdpV2Handler)connection.getValue()).dispose();
/*      */       }
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 1456 */     synchronized (TblMercuriusActiveConnections.getInstance()) {
/* 1457 */       for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblMercuriusActiveConnections.getInstance().entrySet()) {
/* 1458 */         ((InfoModule)connection.getValue()).fecharConexao = true;
/*      */       }
/*      */     } 
/* 1461 */     long tt = System.currentTimeMillis() + 15000L;
/*      */     
/* 1463 */     while ((TblPegasusActiveConnections.getInstance().size() > 0 || TblGriffonActiveConnections.getInstance().size() > 0) && tt > System.currentTimeMillis())
/*      */     {
/* 1465 */       Thread.sleep(100L);
/*      */     }
/*      */   }
/*      */   
/*      */   private static boolean isZeusWatchDogRunning() throws Exception {
/* 1470 */     return (Functions.getServiceControllerByName("ZeusServerWatchdog").getServiceStatus() == 4);
/*      */   }
/*      */   
/*      */   public static Sigar getSigar() {
/* 1474 */     if (sigar == null) {
/* 1475 */       sigar = new Sigar();
/*      */     }
/* 1477 */     return sigar;
/*      */   }
/*      */   
/*      */   public static int getCurrentThreadCount() {
/* 1481 */     return zeusThreadGroup.activeCount();
/*      */   }
/*      */   
/*      */   public static TCPDataServer getTCPDataServer() {
/* 1485 */     return tds;
/*      */   }
/*      */   
/*      */   public static ChkOccurrences getCheckOccurrences() {
/* 1489 */     return checkOccurrences;
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\Main.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */