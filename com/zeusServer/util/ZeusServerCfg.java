/*      */ package com.zeusServer.util;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import java.io.File;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ import javax.xml.parsers.DocumentBuilder;
/*      */ import javax.xml.parsers.DocumentBuilderFactory;
/*      */ import org.w3c.dom.Document;
/*      */ import org.w3c.dom.Element;
/*      */ import org.w3c.dom.NamedNodeMap;
/*      */ import org.w3c.dom.Node;
/*      */ import org.w3c.dom.NodeList;
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
/*      */ public class ZeusServerCfg
/*      */ {
/*      */   private static ZeusServerCfg instance;
/*   38 */   private static int _dbType = 2;
/*      */   private static String _dataServerIP;
/*      */   private static Integer _dataServerPort;
/*      */   private static String _udpDataServerIP;
/*      */   private static Integer _udpDataServerPort;
/*      */   private static String _msgServerIP;
/*      */   private static Integer _msgServerPort;
/*      */   private static byte _smtpServerRequiresAuth;
/*      */   private static String _mailAccount;
/*      */   private static String _smtpServer;
/*      */   private static String _smtpPort;
/*      */   private static String _pop3User;
/*      */   private static String _pop3Pass;
/*      */   private static String _nameSender;
/*      */   private static String _muxSerialPort;
/*      */   private static byte _enableSerialPrinter;
/*      */   private static byte _enableSerialMux;
/*      */   private static String _printerSerialPort;
/*      */   private static Integer _printerBaudrate;
/*      */   private static Integer _printerDatabits;
/*      */   private static Integer _printerStopbits;
/*      */   private static Integer _printerParity;
/*      */   private static String _clientCode;
/*      */   private static String _internetOfflineEvent;
/*      */   private static Integer _internetOfflineFrequency;
/*      */   private static String _eventGsmReceiverSignalLevelBelowMin;
/*      */   private static Integer _freqGenerationEventGsmReceiverSignalLevelBelowMin;
/*      */   private static String _eventLostCommGsmReceiver;
/*      */   private static Integer _freqGenerationEventLostCommGsmReceiver;
/*      */   private static String _serversTestInternet;
/*      */   private static Integer _frequenceTestInternet;
/*      */   private static Integer _timeoutTestInternet;
/*      */   private static Integer _retriesTestInternet;
/*      */   private static String _recipientsCSD;
/*      */   private static Integer _minimumReceivingSignalLevel;
/*      */   private static byte _autoRegistration;
/*   74 */   private static byte _deleteOccAfterFinalization = 0;
/*   75 */   private static String _refDate = "";
/*      */   
/*      */   private static String _languageID;
/*      */   
/*      */   private static String _securityPass;
/*      */   
/*      */   private static String _defaultMonitoringCOMPort;
/*      */   private static int _dbBackupSize;
/*      */   private static String _dbServer;
/*      */   private static int _dbServerPort;
/*      */   private static String _dbFile;
/*      */   private static String _dbBackupFrequency;
/*      */   private static String _dbBackupDirectory;
/*      */   private static byte _enableAutomaticBackup;
/*      */   private static byte _cleanDataBaseAfterBackup;
/*      */   private static Integer _webserverPort;
/*      */   private static String _ddnsClientSettings;
/*      */   private static String _eventETH_1_disconnect;
/*      */   private static Integer _freqETH_1_disconnect;
/*      */   private static String _eventETH_2_disconnect;
/*      */   private static Integer _freqETH_2_disconnect;
/*      */   private static String _eventLowBattery;
/*      */   private static Integer _freqLowBattery;
/*      */   private static String _eventDeviceTampered;
/*      */   private static Integer _freqDeviceTampered;
/*      */   private static String _eventACPower;
/*      */   private static Integer _freqACPower;
/*      */   private static String _eventSDCardFailure;
/*      */   private static Integer _freqSDCardFailure;
/*      */   private static String _eventLowDiskSpace;
/*      */   private static Integer _freqLowDiskSpace;
/*      */   private static String _eventOverTemperature;
/*      */   private static Integer _freqOverTemperature;
/*      */   private static String _dbUser;
/*      */   private static String _dbPass;
/*      */   private static byte _enableNetworkAuth;
/*      */   private static String _networkDomain;
/*      */   private static String _networkUser;
/*      */   private static String _networkPassword;
/*      */   private static String _ethInterface0;
/*      */   private static String _ethInterface1;
/*      */   private static ConcurrentHashMap<String, MonitoringInfo> _monitoringInfo;
/*      */   
/*      */   public static ZeusServerCfg getInstance() {
/*  119 */     if (instance == null) {
/*      */       try {
/*  121 */         instance = new ZeusServerCfg();
/*  122 */         loadZeusConfiguration();
/*  123 */       } catch (Exception ex) {
/*  124 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Error while loading the configuration file", Enums.EnumMessagePriority.HIGH, null, ex);
/*  125 */         Functions.applicationExit();
/*      */       } 
/*      */     }
/*  128 */     return instance;
/*      */   }
/*      */   
/*      */   public static void nullfyInstance() {
/*  132 */     instance = null;
/*      */   }
/*      */   
/*      */   public final int getDbType() {
/*  136 */     return _dbType;
/*      */   }
/*      */   
/*      */   public final String getDataServerIP() {
/*  140 */     return _dataServerIP;
/*      */   }
/*      */   
/*      */   public final Integer getDataServerPort() {
/*  144 */     return _dataServerPort;
/*      */   }
/*      */   
/*      */   public final String getUdpDataServerIP() {
/*  148 */     return _udpDataServerIP;
/*      */   }
/*      */   
/*      */   public final Integer getUdpDataServerPort() {
/*  152 */     return _udpDataServerPort;
/*      */   }
/*      */   
/*      */   public final String getMsgServerIP() {
/*  156 */     return _msgServerIP;
/*      */   }
/*      */   
/*      */   public final Integer getMsgServerPort() {
/*  160 */     return _msgServerPort;
/*      */   }
/*      */   
/*      */   public final String getDbUser() {
/*  164 */     return _dbUser;
/*      */   }
/*      */   
/*      */   public final String getDbPass() {
/*  168 */     return _dbPass;
/*      */   }
/*      */   
/*      */   public final boolean getSmtpServerRequiresAuth() {
/*  172 */     return (_smtpServerRequiresAuth > 0);
/*      */   }
/*      */   
/*      */   public final String getMailAccount() {
/*  176 */     return _mailAccount;
/*      */   }
/*      */   
/*      */   public final String getSmtpServer() {
/*  180 */     return _smtpServer;
/*      */   }
/*      */   
/*      */   public final String getSmtpPort() {
/*  184 */     return _smtpPort;
/*      */   }
/*      */   
/*      */   public final String getPop3User() {
/*  188 */     return _pop3User;
/*      */   }
/*      */   
/*      */   public final String getPop3Pass() {
/*  192 */     return _pop3Pass;
/*      */   }
/*      */   
/*      */   public final String getNameSender() {
/*  196 */     return _nameSender;
/*      */   }
/*      */   
/*      */   public final String getMuxSerialPort() {
/*  200 */     return _muxSerialPort;
/*      */   }
/*      */   
/*      */   public final boolean getEnableSerialPrinter() {
/*  204 */     return (_enableSerialPrinter > 0);
/*      */   }
/*      */   
/*      */   public final boolean getEnableSerialMux() {
/*  208 */     return (_enableSerialMux > 0);
/*      */   }
/*      */   
/*      */   public final String getPrinterSerialPort() {
/*  212 */     return _printerSerialPort;
/*      */   }
/*      */   
/*      */   public final Integer getPrinterBaudrate() {
/*  216 */     return _printerBaudrate;
/*      */   }
/*      */   
/*      */   public final Integer getPrinterDatabits() {
/*  220 */     return _printerDatabits;
/*      */   }
/*      */   
/*      */   public final Integer getPrinterStopbits() {
/*  224 */     return _printerStopbits;
/*      */   }
/*      */   
/*      */   public final Integer getPrinterParity() {
/*  228 */     return _printerParity;
/*      */   }
/*      */   
/*      */   public final String getClientCode() {
/*  232 */     return _clientCode;
/*      */   }
/*      */   
/*      */   public final String getInternetOfflineEvent() {
/*  236 */     return _internetOfflineEvent;
/*      */   }
/*      */   
/*      */   public final Integer getInternetOfflineFrequency() {
/*  240 */     return _internetOfflineFrequency;
/*      */   }
/*      */   
/*      */   public final String getEventGsmReceiverSignalLevelBelowMin() {
/*  244 */     return _eventGsmReceiverSignalLevelBelowMin;
/*      */   }
/*      */   
/*      */   public final Integer getFreqGenerationEventGsmReceiverSignalLevelBelowMin() {
/*  248 */     return _freqGenerationEventGsmReceiverSignalLevelBelowMin;
/*      */   }
/*      */   
/*      */   public final String getEventLostCommGsmReceiver() {
/*  252 */     return _eventLostCommGsmReceiver;
/*      */   }
/*      */   
/*      */   public final Integer getFreqGenerationEventLostCommGsmReceiver() {
/*  256 */     return _freqGenerationEventLostCommGsmReceiver;
/*      */   }
/*      */   
/*      */   public final String getServersTestInternet() {
/*  260 */     return _serversTestInternet;
/*      */   }
/*      */   
/*      */   public final Integer getFrequenceTestInternet() {
/*  264 */     return _frequenceTestInternet;
/*      */   }
/*      */   
/*      */   public final Integer getTimeoutTestInternet() {
/*  268 */     return _timeoutTestInternet;
/*      */   }
/*      */   
/*      */   public final Integer getRetriesTestInternet() {
/*  272 */     return _retriesTestInternet;
/*      */   }
/*      */   
/*      */   public final String getRecipientsCSD() {
/*  276 */     return _recipientsCSD;
/*      */   }
/*      */   
/*      */   public final Integer getMinimumReceivingSignalLevel() {
/*  280 */     return _minimumReceivingSignalLevel;
/*      */   }
/*      */   
/*      */   public final boolean getAutoRegistration() {
/*  284 */     return (_autoRegistration > 0);
/*      */   }
/*      */   
/*      */   public final boolean getDeleteOccAfterFinalization() {
/*  288 */     return (_deleteOccAfterFinalization > 0);
/*      */   }
/*      */   
/*      */   public final String getRefDate() {
/*  292 */     return _refDate;
/*      */   }
/*      */   
/*      */   public final String getLanguageID() {
/*  296 */     return _languageID;
/*      */   }
/*      */   
/*      */   public final String getSecurityPass() {
/*  300 */     return _securityPass;
/*      */   }
/*      */   
/*      */   public final String getDefaultMonitoringCOMPort() {
/*  304 */     return _defaultMonitoringCOMPort;
/*      */   }
/*      */   
/*      */   public final ConcurrentHashMap<String, MonitoringInfo> getMonitoringInfo() {
/*  308 */     return _monitoringInfo;
/*      */   }
/*      */   
/*      */   public final int getDBBackupMaxSize() {
/*  312 */     return _dbBackupSize;
/*      */   }
/*      */   
/*      */   public final String getDbServer() {
/*  316 */     return _dbServer;
/*      */   }
/*      */   
/*      */   public final int getDbServerPort() {
/*  320 */     return _dbServerPort;
/*      */   }
/*      */   
/*      */   public final String getDbFile() {
/*  324 */     return _dbFile;
/*      */   }
/*      */   
/*      */   public final String getDbBackupDirectory() {
/*  328 */     return _dbBackupDirectory;
/*      */   }
/*      */   
/*      */   public final String getDbBackupFrequency() {
/*  332 */     return _dbBackupFrequency;
/*      */   }
/*      */   
/*      */   public final boolean getEnableAutomaticBackup() {
/*  336 */     return (_enableAutomaticBackup > 0);
/*      */   }
/*      */   
/*      */   public final boolean getCleanDataBaseAfterBackup() {
/*  340 */     return (_cleanDataBaseAfterBackup > 0);
/*      */   }
/*      */   
/*      */   public final Integer getWebserverPort() {
/*  344 */     return _webserverPort;
/*      */   }
/*      */   
/*      */   public final String getDDNSClientSettings() {
/*  348 */     return _ddnsClientSettings;
/*      */   }
/*      */   
/*      */   public final String getEventETH_1_disconnect() {
/*  352 */     return _eventETH_1_disconnect;
/*      */   }
/*      */   
/*      */   public final Integer getFreqETH_1_disconnect() {
/*  356 */     return _freqETH_1_disconnect;
/*      */   }
/*      */   
/*      */   public final String getEventETH_2_disconnect() {
/*  360 */     return _eventETH_2_disconnect;
/*      */   }
/*      */   
/*      */   public final Integer getFreqETH_2_disconnect() {
/*  364 */     return _freqETH_2_disconnect;
/*      */   }
/*      */   
/*      */   public final String getEventLowBattery() {
/*  368 */     return _eventLowBattery;
/*      */   }
/*      */   
/*      */   public final Integer getFreqLowBattery() {
/*  372 */     return _freqLowBattery;
/*      */   }
/*      */   
/*      */   public final String getEventDeviceTampered() {
/*  376 */     return _eventDeviceTampered;
/*      */   }
/*      */   
/*      */   public final String getEventACPower() {
/*  380 */     return _eventACPower;
/*      */   }
/*      */   
/*      */   public final Integer getFreqACPower() {
/*  384 */     return _freqACPower;
/*      */   }
/*      */   
/*      */   public final String getEventSDCardFailure() {
/*  388 */     return _eventSDCardFailure;
/*      */   }
/*      */   
/*      */   public final Integer getFreqSDCardFailure() {
/*  392 */     return _freqSDCardFailure;
/*      */   }
/*      */   
/*      */   public final String getEventLowDiskSpace() {
/*  396 */     return _eventLowDiskSpace;
/*      */   }
/*      */   
/*      */   public final Integer getFreqLowDiskSpace() {
/*  400 */     return _freqLowDiskSpace;
/*      */   }
/*      */   
/*      */   public final String getEventOverTemperature() {
/*  404 */     return _eventOverTemperature;
/*      */   }
/*      */   
/*      */   public final Integer getFreqOverTemperature() {
/*  408 */     return _freqOverTemperature;
/*      */   }
/*      */   
/*      */   public final Integer getFreqDeviceTampered() {
/*  412 */     return _freqDeviceTampered;
/*      */   }
/*      */   
/*      */   public final String getNetworkDomain() {
/*  416 */     return _networkDomain;
/*      */   }
/*      */   
/*      */   public final String getNetworkUser() {
/*  420 */     return _networkUser;
/*      */   }
/*      */   
/*      */   public final String getNetworkPassword() {
/*  424 */     return _networkPassword;
/*      */   }
/*      */   
/*      */   public final boolean isNetworkAuth() {
/*  428 */     return (_enableNetworkAuth > 0);
/*      */   }
/*      */   
/*      */   public final String getEth_0_IP() {
/*  432 */     return _ethInterface0;
/*      */   }
/*      */   
/*      */   public final String getEth_1_IP() {
/*  436 */     return _ethInterface1;
/*      */   }
/*      */   
/*      */   private static void loadZeusConfiguration() throws Exception {
/*  440 */     DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
/*  441 */     String cfgFile = ZeusPathCfgUtil.getZeusCfgPath();
/*  442 */     File file = new File(cfgFile);
/*  443 */     if (!file.exists()) {
/*  444 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to locate the Zeus Configuration File " + cfgFile, Enums.EnumMessagePriority.HIGH, null, null);
/*  445 */       Functions.applicationExit();
/*      */     } 
/*  447 */     Document doc = db.parse(file);
/*  448 */     NodeList nl = doc.getElementsByTagName("general");
/*  449 */     if (nl.getLength() == 0) {
/*  450 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to locate the element 'general' in the configuration file (ZeusServer.xml)", Enums.EnumMessagePriority.HIGH, null, null);
/*  451 */       Functions.applicationExit();
/*  452 */     } else if (nl.getLength() > 1) {
/*  453 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was located more than one element 'general' in the configuration file (ZeusServer.xml)", Enums.EnumMessagePriority.HIGH, null, null);
/*  454 */       Functions.applicationExit();
/*      */     } else {
/*  456 */       NamedNodeMap nnm = nl.item(0).getAttributes();
/*      */       
/*  458 */       Node node = nnm.getNamedItem("enableSerialPrinter");
/*  459 */       if (node == null) {
/*  460 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'enableSerialPrinter' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  461 */         Functions.applicationExit();
/*  462 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  463 */         _enableSerialPrinter = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  465 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'enableSerialPrinter' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  466 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  469 */       node = nnm.getNamedItem("enableSerialMux");
/*  470 */       if (node == null) {
/*  471 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'enableSerialMux' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  472 */         Functions.applicationExit();
/*  473 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  474 */         _enableSerialMux = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  476 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'enableSerialMux' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  477 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  480 */       node = nnm.getNamedItem("enableNetworkFolder");
/*  481 */       if (node == null) {
/*  482 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'enableNetworkFolder' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  483 */         Functions.applicationExit();
/*  484 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  485 */         _enableNetworkAuth = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  487 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'enableNetworkFolder' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  488 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  491 */       node = nnm.getNamedItem("NetworkDomain");
/*  492 */       if (node == null) {
/*  493 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'NetworkDomain' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  494 */         Functions.applicationExit();
/*      */       } else {
/*  496 */         _networkDomain = node.getNodeValue();
/*      */       } 
/*      */       
/*  499 */       node = nnm.getNamedItem("NetworkUserName");
/*  500 */       if (node == null) {
/*  501 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'NetworkUserName' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  502 */         Functions.applicationExit();
/*      */       } else {
/*  504 */         _networkUser = node.getNodeValue();
/*      */       } 
/*      */       
/*  507 */       node = nnm.getNamedItem("NetworkPassword");
/*  508 */       if (node == null) {
/*  509 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'NetworkPassword' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  510 */         Functions.applicationExit();
/*      */       } else {
/*  512 */         _networkPassword = Rijndael.decryptString(node.getNodeValue(), Rijndael.cfgKey, true);
/*      */       } 
/*      */       
/*  515 */       node = nnm.getNamedItem("smtpServerRequiresAuth");
/*  516 */       if (node == null) {
/*  517 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'smtpServerRequiresAuth' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  518 */         Functions.applicationExit();
/*  519 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  520 */         _smtpServerRequiresAuth = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  522 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'smtpServerRequiresAuth' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  523 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  526 */       node = nnm.getNamedItem("autoRegistration");
/*  527 */       if (node == null) {
/*  528 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'autoRegistration' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  529 */         Functions.applicationExit();
/*  530 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  531 */         _autoRegistration = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  533 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'autoRegistration' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  534 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  537 */       node = nnm.getNamedItem("deleteOccAfterFinalization");
/*  538 */       if (node == null) {
/*  539 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'deleteOccAfterFinalization' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  540 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  541 */         _deleteOccAfterFinalization = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  543 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'deleteOccAfterFinalization' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*      */       } 
/*      */       
/*  546 */       node = nnm.getNamedItem("refDate");
/*  547 */       if (node == null) {
/*  548 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'refDate' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  549 */       } else if (Functions.isDate(node.getNodeValue())) {
/*  550 */         _refDate = node.getNodeValue();
/*      */       } else {
/*  552 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'refDate' of the configuration file (ZeusServer.xml): " + node.getNodeValue() + "\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*      */       } 
/*  554 */       if (_refDate.isEmpty()) {
/*  555 */         _refDate = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
/*  556 */         Functions.updateRefDate(ZeusPathCfgUtil.getZeusCfgPath(), _refDate);
/*      */       } 
/*      */       
/*  559 */       node = nnm.getNamedItem("DataServerIP");
/*  560 */       if (node == null) {
/*  561 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DataServerIP' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  562 */         Functions.applicationExit();
/*      */       } else {
/*  564 */         _dataServerIP = node.getNodeValue();
/*      */       } 
/*      */       
/*  567 */       node = nnm.getNamedItem("UdpDataServerIP");
/*  568 */       if (node == null) {
/*  569 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'UdpDataServerIP' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  570 */         Functions.applicationExit();
/*      */       } else {
/*  572 */         _udpDataServerIP = node.getNodeValue();
/*      */       } 
/*      */       
/*  575 */       node = nnm.getNamedItem("UdpDataServerPort");
/*  576 */       if (node == null) {
/*  577 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'UdpDataServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  578 */         Functions.applicationExit();
/*  579 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  580 */         _udpDataServerPort = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  582 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'UdpDataServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  583 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  586 */       node = nnm.getNamedItem("DataServerPort");
/*  587 */       if (node == null) {
/*  588 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DataServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  589 */         Functions.applicationExit();
/*  590 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  591 */         _dataServerPort = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  593 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'DataServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  594 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  597 */       node = nnm.getNamedItem("MsgServerIP");
/*  598 */       if (node == null) {
/*  599 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MsgServerIP' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  600 */         Functions.applicationExit();
/*      */       } else {
/*  602 */         _msgServerIP = node.getNodeValue();
/*      */       } 
/*      */       
/*  605 */       node = nnm.getNamedItem("MsgServerPort");
/*  606 */       if (node == null) {
/*  607 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MsgServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  608 */         Functions.applicationExit();
/*  609 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  610 */         _msgServerPort = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  612 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'MsgServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  613 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  616 */       node = nnm.getNamedItem("MailAccount");
/*  617 */       if (node == null) {
/*  618 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MailAccount' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  619 */         Functions.applicationExit();
/*      */       } else {
/*  621 */         _mailAccount = node.getNodeValue();
/*      */       } 
/*      */       
/*  624 */       node = nnm.getNamedItem("SmtpServer");
/*  625 */       if (node == null) {
/*  626 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'SmtpServer' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  627 */         Functions.applicationExit();
/*      */       } else {
/*  629 */         _smtpServer = node.getNodeValue();
/*      */       } 
/*      */       
/*  632 */       node = nnm.getNamedItem("SmtpPort");
/*  633 */       if (node == null) {
/*  634 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'SmtpPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  635 */         Functions.applicationExit();
/*      */       } else {
/*  637 */         _smtpPort = node.getNodeValue();
/*      */       } 
/*      */       
/*  640 */       node = nnm.getNamedItem("Pop3User");
/*  641 */       if (node == null) {
/*  642 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'Pop3User' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  643 */         Functions.applicationExit();
/*      */       } else {
/*  645 */         _pop3User = node.getNodeValue();
/*      */       } 
/*      */       
/*  648 */       node = nnm.getNamedItem("Pop3Pass");
/*  649 */       if (node == null) {
/*  650 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'Pop3Pass' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  651 */         Functions.applicationExit();
/*      */       } else {
/*  653 */         _pop3Pass = Rijndael.decryptString(node.getNodeValue(), Rijndael.cfgKey, true);
/*      */       } 
/*      */       
/*  656 */       node = nnm.getNamedItem("NameSender");
/*  657 */       if (node == null) {
/*  658 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'NameSender' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  659 */         Functions.applicationExit();
/*      */       } else {
/*  661 */         _nameSender = node.getNodeValue();
/*      */       } 
/*      */       
/*  664 */       node = nnm.getNamedItem("MuxSerialPort");
/*  665 */       if (node == null) {
/*  666 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MuxSerialPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  667 */         Functions.applicationExit();
/*      */       } else {
/*  669 */         _muxSerialPort = node.getNodeValue();
/*      */       } 
/*      */       
/*  672 */       node = nnm.getNamedItem("PrinterSerialPort");
/*  673 */       if (node == null) {
/*  674 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'PrinterSerialPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  675 */         Functions.applicationExit();
/*      */       } else {
/*  677 */         _printerSerialPort = node.getNodeValue();
/*      */       } 
/*      */       
/*  680 */       node = nnm.getNamedItem("PrinterBaudrate");
/*  681 */       if (node == null) {
/*  682 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'PrinterBaudrate' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  683 */         Functions.applicationExit();
/*  684 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  685 */         _printerBaudrate = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  687 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'PrinterBaudrate' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  688 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  691 */       node = nnm.getNamedItem("PrinterDatabits");
/*  692 */       if (node == null) {
/*  693 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'PrinterDatabits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  694 */         Functions.applicationExit();
/*  695 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  696 */         _printerDatabits = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  698 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'PrinterDatabits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  699 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  702 */       node = nnm.getNamedItem("PrinterStopbits");
/*  703 */       if (node == null) {
/*  704 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'PrinterStopbits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  705 */         Functions.applicationExit();
/*  706 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  707 */         _printerStopbits = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  709 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'PrinterStopbits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  710 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  713 */       node = nnm.getNamedItem("PrinterParity");
/*  714 */       if (node == null) {
/*  715 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'PrinterParity' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  716 */         Functions.applicationExit();
/*  717 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  718 */         _printerParity = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  720 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'PrinterParity' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  721 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  724 */       node = nnm.getNamedItem("ClientCode");
/*  725 */       if (node == null) {
/*  726 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ClientCode' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  727 */         Functions.applicationExit();
/*      */       } else {
/*  729 */         _clientCode = node.getNodeValue();
/*      */       } 
/*      */       
/*  732 */       node = nnm.getNamedItem("InternetOfflineEvent");
/*  733 */       if (node == null) {
/*  734 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'InternetOfflineEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  735 */         Functions.applicationExit();
/*      */       } else {
/*  737 */         _internetOfflineEvent = node.getNodeValue();
/*      */       } 
/*      */       
/*  740 */       node = nnm.getNamedItem("InternetOfflineFrequency");
/*  741 */       if (node == null) {
/*  742 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'InternetOfflineFrequency' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  743 */         Functions.applicationExit();
/*  744 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  745 */         _internetOfflineFrequency = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  747 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'InternetOfflineFrequency' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  748 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  751 */       node = nnm.getNamedItem("EventGsmReceiverSignalLevelBelowMin");
/*  752 */       if (node == null) {
/*  753 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EventGsmReceiverSignalLevelBelowMin' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  754 */         Functions.applicationExit();
/*      */       } else {
/*  756 */         _eventGsmReceiverSignalLevelBelowMin = node.getNodeValue();
/*      */       } 
/*      */       
/*  759 */       node = nnm.getNamedItem("FreqGenerationEventGsmReceiverSignalLevelBelowMin");
/*  760 */       if (node == null) {
/*  761 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'FreqGenerationEventGsmReceiverSignalLevelBelowMin' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  762 */         Functions.applicationExit();
/*  763 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  764 */         _freqGenerationEventGsmReceiverSignalLevelBelowMin = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  766 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'FreqGenerationEventGsmReceiverSignalLevelBelowMin' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  767 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  770 */       node = nnm.getNamedItem("EventLostCommGsmReceiver");
/*  771 */       if (node == null) {
/*  772 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EventLostCommGsmReceiver' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  773 */         Functions.applicationExit();
/*      */       } else {
/*  775 */         _eventLostCommGsmReceiver = node.getNodeValue();
/*      */       } 
/*      */       
/*  778 */       node = nnm.getNamedItem("FreqGenerationEventLostCommGsmReceiver");
/*  779 */       if (node == null) {
/*  780 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'FreqGenerationEventLostCommGsmReceiver' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  781 */         Functions.applicationExit();
/*  782 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  783 */         _freqGenerationEventLostCommGsmReceiver = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  785 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'FreqGenerationEventLostCommGsmReceiver' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  786 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  789 */       node = nnm.getNamedItem("ServersTestInternet");
/*  790 */       if (node == null) {
/*  791 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ServersTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  792 */         Functions.applicationExit();
/*      */       } else {
/*  794 */         _serversTestInternet = node.getNodeValue();
/*      */       } 
/*      */       
/*  797 */       node = nnm.getNamedItem("FrequenceTestInternet");
/*  798 */       if (node == null) {
/*  799 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'FrequenceTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  800 */         Functions.applicationExit();
/*  801 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  802 */         _frequenceTestInternet = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  804 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'FrequenceTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  805 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  808 */       node = nnm.getNamedItem("TimeoutTestInternet");
/*  809 */       if (node == null) {
/*  810 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'TimeoutTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  811 */         Functions.applicationExit();
/*  812 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  813 */         _timeoutTestInternet = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  815 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'TimeoutTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  816 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  819 */       node = nnm.getNamedItem("RetriesTestInternet");
/*  820 */       if (node == null) {
/*  821 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'RetriesTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  822 */         Functions.applicationExit();
/*  823 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  824 */         _retriesTestInternet = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  826 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'RetriesTestInternet' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  827 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  830 */       node = nnm.getNamedItem("RecipientsCSD");
/*  831 */       if (node == null) {
/*  832 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'RecipientsCSD' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  833 */         Functions.applicationExit();
/*      */       } else {
/*  835 */         _recipientsCSD = node.getNodeValue();
/*      */       } 
/*      */       
/*  838 */       node = nnm.getNamedItem("MinimumReceivingSignalLevel");
/*  839 */       if (node == null) {
/*  840 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MinimumReceivingSignalLevel' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  841 */         Functions.applicationExit();
/*  842 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  843 */         _minimumReceivingSignalLevel = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  845 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'MinimumReceivingSignalLevel' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  846 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  849 */       node = nnm.getNamedItem("LanguageID");
/*  850 */       if (node == null) {
/*  851 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'LanguageID' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  852 */         Functions.applicationExit();
/*      */       } else {
/*  854 */         _languageID = node.getNodeValue();
/*      */       } 
/*      */       
/*  857 */       node = nnm.getNamedItem("securityPass");
/*  858 */       if (node == null) {
/*  859 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'securityPass' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  860 */         Functions.applicationExit();
/*      */       } else {
/*  862 */         _securityPass = Rijndael.decryptString(node.getNodeValue(), Rijndael.cfgKey, true);
/*      */       } 
/*      */       
/*  865 */       node = nnm.getNamedItem("DefaultMonitoringCOMPort");
/*  866 */       if (node == null) {
/*  867 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DefaultMonitoringCOMPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  868 */         Functions.applicationExit();
/*      */       } else {
/*  870 */         _defaultMonitoringCOMPort = node.getNodeValue();
/*      */       } 
/*      */       
/*  873 */       node = nnm.getNamedItem("DbBackupSize");
/*  874 */       if (node == null) {
/*  875 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DbBackupSize' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  876 */         Functions.applicationExit();
/*  877 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  878 */         _dbBackupSize = Functions.convertString2Integer(node.getNodeValue()).intValue();
/*      */       } else {
/*  880 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'DbBackupSize' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  881 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  884 */       node = nnm.getNamedItem("DbServer");
/*  885 */       if (node == null) {
/*  886 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DbServer' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  887 */         Functions.applicationExit();
/*      */       } else {
/*  889 */         _dbServer = node.getNodeValue().toLowerCase();
/*      */       } 
/*      */       
/*  892 */       node = nnm.getNamedItem("DbServerPort");
/*  893 */       if (node == null) {
/*  894 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DbServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  895 */         Functions.applicationExit();
/*  896 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  897 */         _dbServerPort = Functions.convertString2Integer(node.getNodeValue()).intValue();
/*      */       } else {
/*  899 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'DbServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  900 */         Functions.applicationExit();
/*      */       } 
/*  902 */       node = nnm.getNamedItem("DbSchema");
/*  903 */       if (node == null) {
/*  904 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DbSchema' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  905 */         Functions.applicationExit();
/*      */       } else {
/*  907 */         _dbFile = node.getNodeValue().toUpperCase();
/*      */       } 
/*  909 */       node = nnm.getNamedItem("DbBackupDirectory");
/*  910 */       if (node == null) {
/*  911 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DbBackupDirectory' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  912 */         Functions.applicationExit();
/*      */       } else {
/*  914 */         _dbBackupDirectory = node.getNodeValue();
/*      */       } 
/*  916 */       node = nnm.getNamedItem("DbBackupFrequency");
/*  917 */       if (node == null) {
/*  918 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DbBackupFrequency' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  919 */         Functions.applicationExit();
/*      */       } else {
/*  921 */         _dbBackupFrequency = node.getNodeValue().toLowerCase();
/*      */       } 
/*      */       
/*  924 */       node = nnm.getNamedItem("enableAutomaticBackup");
/*  925 */       if (node == null) {
/*  926 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'enableAutomaticBackup' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  927 */         Functions.applicationExit();
/*  928 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  929 */         _enableAutomaticBackup = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  931 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'enableAutomaticBackup' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  932 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  935 */       node = nnm.getNamedItem("cleanDataBaseAfterBackup");
/*  936 */       if (node == null) {
/*  937 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'cleanDataBaseAfterBackup' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  938 */         Functions.applicationExit();
/*  939 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  940 */         _cleanDataBaseAfterBackup = Functions.convertString2Byte(node.getNodeValue());
/*      */       } else {
/*  942 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'cleanDataBaseAfterBackup' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  943 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  946 */       node = nnm.getNamedItem("WebServerPort");
/*  947 */       if (node == null) {
/*  948 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'WebServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  949 */         Functions.applicationExit();
/*  950 */       } else if (Functions.isInteger(node.getNodeValue())) {
/*  951 */         _webserverPort = Functions.convertString2Integer(node.getNodeValue());
/*      */       } else {
/*  953 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'WebServerPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  954 */         Functions.applicationExit();
/*      */       } 
/*      */       
/*  957 */       node = nnm.getNamedItem("DDNSSettings");
/*  958 */       if (node == null) {
/*  959 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DDNSSettings' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  960 */         Functions.applicationExit();
/*      */       } else {
/*  962 */         _ddnsClientSettings = node.getNodeValue();
/*  963 */         if (_ddnsClientSettings != null && _ddnsClientSettings.length() > 0) {
/*  964 */           String[] settings = _ddnsClientSettings.split(";");
/*  965 */           if (settings != null && settings.length == 5) {
/*  966 */             _ddnsClientSettings = settings[0] + ";" + settings[1] + ";" + Rijndael.decryptString(settings[2], Rijndael.cfgKey, true) + ";" + settings[3] + ";" + settings[4];
/*      */           }
/*      */         } 
/*      */       } 
/*      */       
/*  971 */       node = nnm.getNamedItem("DBUser");
/*  972 */       if (node == null) {
/*  973 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DBUser' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  974 */         Functions.applicationExit();
/*      */       } else {
/*  976 */         _dbUser = node.getNodeValue();
/*      */       } 
/*      */       
/*  979 */       node = nnm.getNamedItem("DBPass");
/*  980 */       if (node == null) {
/*  981 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DBPass' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  982 */         Functions.applicationExit();
/*      */       } else {
/*  984 */         _dbPass = Rijndael.decryptString(node.getNodeValue(), Rijndael.cfgKey, true);
/*      */       } 
/*      */       
/*  987 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  988 */         node = nnm.getNamedItem("EthPort1DisconnectedEvent");
/*  989 */         if (node == null) {
/*  990 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EthPort1DisconnectedEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  991 */           Functions.applicationExit();
/*      */         } else {
/*  993 */           _eventETH_1_disconnect = node.getNodeValue();
/*      */         } 
/*      */         
/*  996 */         node = nnm.getNamedItem("EthPort1DisconnectedFreq");
/*  997 */         if (node == null) {
/*  998 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EthPort1DisconnectedFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/*  999 */           Functions.applicationExit();
/* 1000 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1001 */           _freqETH_1_disconnect = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1003 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'EthPort1DisconnectedFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1004 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1007 */         node = nnm.getNamedItem("EthPort2DisconnectedEvent");
/* 1008 */         if (node == null) {
/* 1009 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EthPort2DisconnectedEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1010 */           Functions.applicationExit();
/*      */         } else {
/* 1012 */           _eventETH_2_disconnect = node.getNodeValue();
/*      */         } 
/*      */         
/* 1015 */         node = nnm.getNamedItem("EthPort2DisconnectedFreq");
/* 1016 */         if (node == null) {
/* 1017 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EthPort2DisconnectedFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1018 */           Functions.applicationExit();
/* 1019 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1020 */           _freqETH_2_disconnect = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1022 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'EthPort2DisconnectedFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1023 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1026 */         node = nnm.getNamedItem("LowBatteryEvent");
/* 1027 */         if (node == null) {
/* 1028 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'LowBatteryEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1029 */           Functions.applicationExit();
/*      */         } else {
/* 1031 */           _eventLowBattery = node.getNodeValue();
/*      */         } 
/*      */         
/* 1034 */         node = nnm.getNamedItem("LowBatteryFreq");
/* 1035 */         if (node == null) {
/* 1036 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'LowBatteryFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1037 */           Functions.applicationExit();
/* 1038 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1039 */           _freqLowBattery = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1041 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'LowBatteryFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1042 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1045 */         node = nnm.getNamedItem("DeviceTamperEvent");
/* 1046 */         if (node == null) {
/* 1047 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DeviceTamperEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1048 */           Functions.applicationExit();
/*      */         } else {
/* 1050 */           _eventDeviceTampered = node.getNodeValue();
/*      */         } 
/*      */         
/* 1053 */         node = nnm.getNamedItem("DeviceTamperFreq");
/* 1054 */         if (node == null) {
/* 1055 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DeviceTamperFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1056 */           Functions.applicationExit();
/* 1057 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1058 */           _freqDeviceTampered = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1060 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'DeviceTamperFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1061 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1064 */         node = nnm.getNamedItem("AcPowerEvent");
/* 1065 */         if (node == null) {
/* 1066 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'AcPowerEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1067 */           Functions.applicationExit();
/*      */         } else {
/* 1069 */           _eventACPower = node.getNodeValue();
/*      */         } 
/*      */         
/* 1072 */         node = nnm.getNamedItem("AcPowerFreq");
/* 1073 */         if (node == null) {
/* 1074 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'AcPowerFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1075 */           Functions.applicationExit();
/* 1076 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1077 */           _freqACPower = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1079 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'AcPowerFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1080 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1083 */         node = nnm.getNamedItem("SDCardFailureEvent");
/* 1084 */         if (node == null) {
/* 1085 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'SDCardFailureEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1086 */           Functions.applicationExit();
/*      */         } else {
/* 1088 */           _eventSDCardFailure = node.getNodeValue();
/*      */         } 
/*      */         
/* 1091 */         node = nnm.getNamedItem("SDCardFailureFreq");
/* 1092 */         if (node == null) {
/* 1093 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'SDCardFailureFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1094 */           Functions.applicationExit();
/* 1095 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1096 */           _freqSDCardFailure = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1098 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'SDCardFailureFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1099 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1102 */         node = nnm.getNamedItem("LowDiskSpaceEvent");
/* 1103 */         if (node == null) {
/* 1104 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'LowDiskSpaceEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1105 */           Functions.applicationExit();
/*      */         } else {
/* 1107 */           _eventLowDiskSpace = node.getNodeValue();
/*      */         } 
/*      */         
/* 1110 */         node = nnm.getNamedItem("LowDiskSpaceFreq");
/* 1111 */         if (node == null) {
/* 1112 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'LowDiskSpaceFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1113 */           Functions.applicationExit();
/* 1114 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1115 */           _freqLowDiskSpace = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1117 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'LowDiskSpaceFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1118 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1121 */         node = nnm.getNamedItem("ZeusBoxOverTemperatureEvent");
/* 1122 */         if (node == null) {
/* 1123 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ZeusBoxOverTemperatureEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1124 */           Functions.applicationExit();
/*      */         } else {
/* 1126 */           _eventOverTemperature = node.getNodeValue();
/*      */         } 
/*      */         
/* 1129 */         node = nnm.getNamedItem("ZeusBoxOverTemperatureFreq");
/* 1130 */         if (node == null) {
/* 1131 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ZeusBoxOverTemperatureFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1132 */           Functions.applicationExit();
/* 1133 */         } else if (Functions.isInteger(node.getNodeValue())) {
/* 1134 */           _freqOverTemperature = Functions.convertString2Integer(node.getNodeValue());
/*      */         } else {
/* 1136 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ZeusBoxOverTemperatureFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1137 */           Functions.applicationExit();
/*      */         } 
/*      */         
/* 1140 */         node = nnm.getNamedItem("EthPort1IP");
/* 1141 */         if (node == null) {
/* 1142 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EthPort1IP' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1143 */           Functions.applicationExit();
/*      */         } else {
/* 1145 */           _ethInterface0 = node.getNodeValue();
/*      */         } 
/*      */         
/* 1148 */         node = nnm.getNamedItem("EthPort2IP");
/* 1149 */         if (node == null) {
/* 1150 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EthPort2IP' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1151 */           Functions.applicationExit();
/*      */         } else {
/* 1153 */           _ethInterface1 = node.getNodeValue();
/*      */         } 
/*      */       } 
/*      */       
/* 1157 */       NodeList comPorts = doc.getElementsByTagName("MonitoringCOMPort");
/* 1158 */       int portSize = comPorts.getLength();
/* 1159 */       if (portSize > 0) {
/* 1160 */         HashMap<String, List<Integer>> tmpMap = new HashMap<>(portSize);
/* 1161 */         _monitoringInfo = new ConcurrentHashMap<>();
/* 1162 */         boolean defaultIDMismatch = true;
/*      */         
/* 1164 */         for (int i = 0; i < portSize; i++) {
/* 1165 */           MonitoringInfo mi = new MonitoringInfo();
/* 1166 */           Node childNode = comPorts.item(i);
/* 1167 */           NamedNodeMap childNodeMap = childNode.getAttributes();
/*      */           
/* 1169 */           node = childNodeMap.getNamedItem("MonitoringSoftwareIP");
/* 1170 */           if (node == null) {
/* 1171 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MonitoringSoftwareIP' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1172 */             Functions.applicationExit();
/*      */           } else {
/* 1174 */             mi.setMonitoringSoftwareIP(node.getNodeValue());
/*      */           } 
/*      */           
/* 1177 */           node = childNodeMap.getNamedItem("MonitoringSoftwarePort");
/* 1178 */           if (node == null) {
/* 1179 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'MonitoringSoftwarePort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1180 */             Functions.applicationExit();
/* 1181 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1182 */             mi.setMonitoringSoftwarePort(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1184 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'MonitoringSoftwarePort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1185 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1188 */           node = childNodeMap.getNamedItem("ReceiverType");
/* 1189 */           if (node == null) {
/* 1190 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverType' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1191 */             Functions.applicationExit();
/* 1192 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1193 */             mi.setReceiverType(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1195 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverType' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1196 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1199 */           node = childNodeMap.getNamedItem("ValueAddedPartition");
/* 1200 */           if (node == null) {
/* 1201 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ValueAddedPartition' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1202 */             Functions.applicationExit();
/* 1203 */           } else if (Functions.isInteger(node.getNodeValue(), 16)) {
/* 1204 */             mi.setValueAddedPartition(Functions.convertString2Integer(node.getNodeValue(), 16));
/*      */           } else {
/* 1206 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ValueAddedPartition' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1207 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1210 */           node = childNodeMap.getNamedItem("PartitionScheme");
/* 1211 */           if (node == null) {
/* 1212 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'PartitionScheme' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1213 */             Functions.applicationExit();
/* 1214 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1215 */             mi.setPartitionScheme(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1217 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'PartitionScheme' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1218 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1221 */           node = childNodeMap.getNamedItem("ReceiverNumber");
/* 1222 */           if (node == null) {
/* 1223 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverNumber' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1224 */             Functions.applicationExit();
/* 1225 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1226 */             mi.setReceiverNumber(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1228 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverNumber' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1229 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1232 */           node = childNodeMap.getNamedItem("ReceiverGroup");
/* 1233 */           if (node == null) {
/* 1234 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverGroup' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1235 */             Functions.applicationExit();
/* 1236 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1237 */             mi.setReceiverGroup(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1239 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverGroup' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1240 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1243 */           node = childNodeMap.getNamedItem("ReceiverLine");
/* 1244 */           if (node == null) {
/* 1245 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverLine' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1246 */             Functions.applicationExit();
/* 1247 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1248 */             mi.setReceiverLine(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1250 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverLine' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1251 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1254 */           node = childNodeMap.getNamedItem("ReceiverSerialPort");
/* 1255 */           if (node == null) {
/* 1256 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverSerialPort' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1257 */             Functions.applicationExit();
/*      */           } else {
/* 1259 */             mi.setReceiverSerialPort(node.getNodeValue());
/*      */           } 
/*      */           
/* 1262 */           node = childNodeMap.getNamedItem("ReceiverTimeout");
/* 1263 */           if (node == null) {
/* 1264 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverTimeout' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1265 */             Functions.applicationExit();
/* 1266 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1267 */             mi.setReceiverTimeout(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1269 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverTimeout' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1270 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1273 */           node = childNodeMap.getNamedItem("ReceiverBaudrate");
/* 1274 */           if (node == null) {
/* 1275 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverBaudrate' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1276 */             Functions.applicationExit();
/* 1277 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1278 */             mi.setReceiverBaudrate(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1280 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverBaudrate' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1281 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1284 */           node = childNodeMap.getNamedItem("ReceiverDatabits");
/* 1285 */           if (node == null) {
/* 1286 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverDatabits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1287 */             Functions.applicationExit();
/* 1288 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1289 */             mi.setReceiverDatabits(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1291 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverDatabits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1292 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1295 */           node = childNodeMap.getNamedItem("ReceiverStopbits");
/* 1296 */           if (node == null) {
/* 1297 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverStopbits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1298 */             Functions.applicationExit();
/* 1299 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1300 */             mi.setReceiverStopbits(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1302 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverStopbits' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1303 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1306 */           node = childNodeMap.getNamedItem("ReceiverParity");
/* 1307 */           if (node == null) {
/* 1308 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'ReceiverParity' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1309 */             Functions.applicationExit();
/* 1310 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1311 */             mi.setReceiverParity(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1313 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'ReceiverParity' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1314 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1317 */           node = childNodeMap.getNamedItem("EventsTimeGap");
/* 1318 */           if (node == null) {
/* 1319 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'EventsTimeGap' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1320 */             Functions.applicationExit();
/* 1321 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1322 */             mi.setEventsTimeGap(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1324 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'EventsTimeGap' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1325 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1328 */           node = childNodeMap.getNamedItem("WaitEventAck");
/* 1329 */           if (node == null) {
/* 1330 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'WaitEventAck' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1331 */             Functions.applicationExit();
/* 1332 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1333 */             mi.setWaitEventAck(Functions.convertString2Byte(node.getNodeValue()));
/*      */           } else {
/* 1335 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'WaitEventAck' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1336 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1339 */           node = childNodeMap.getNamedItem("AckByte");
/* 1340 */           if (node == null) {
/* 1341 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'AckByte' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1342 */             Functions.applicationExit();
/* 1343 */           } else if (Functions.isInteger(node.getNodeValue(), 16)) {
/* 1344 */             mi.setAckByte(Functions.convertString2Integer(node.getNodeValue(), 16).intValue());
/*      */           } else {
/*      */             
/* 1347 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'AckByte' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1348 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1351 */           node = childNodeMap.getNamedItem("DeleteEventAfterTransmission");
/* 1352 */           if (node == null) {
/* 1353 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'DeleteEventAfterTransmission' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1354 */             Functions.applicationExit();
/* 1355 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1356 */             mi.setDeleteEventAfterTransmission(Functions.convertString2Byte(node.getNodeValue()));
/*      */           } else {
/* 1358 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'DeleteEventAfterTransmission' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1359 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1362 */           node = childNodeMap.getNamedItem("BeepAfterEventTransmission");
/* 1363 */           if (node == null) {
/* 1364 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'BeepAfterEventTransmission' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1365 */             Functions.applicationExit();
/* 1366 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1367 */             mi.setBeepAfterEventTransmission(Functions.convertString2Byte(node.getNodeValue()));
/*      */           } else {
/* 1369 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'BeepAfterEventTransmission' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1370 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1373 */           node = childNodeMap.getNamedItem("SelfTestEvent");
/* 1374 */           if (node == null) {
/* 1375 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'SelfTestEvent' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1376 */             Functions.applicationExit();
/*      */           } else {
/* 1378 */             mi.setSelfTestEvent(node.getNodeValue());
/*      */           } 
/*      */           
/* 1381 */           node = childNodeMap.getNamedItem("SelfTestFrequency");
/* 1382 */           if (node == null) {
/* 1383 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'SelfTestFrequency' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1384 */             Functions.applicationExit();
/* 1385 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1386 */             mi.setSelfTestFrequency(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1388 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'SelfTestFrequency' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1389 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1392 */           node = childNodeMap.getNamedItem("enableHeartBeat");
/* 1393 */           if (node == null) {
/* 1394 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'enableHeartBeat' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1395 */             Functions.applicationExit();
/* 1396 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1397 */             mi.setEnableHeartbeat(Functions.convertString2Byte(node.getNodeValue()));
/*      */           } else {
/* 1399 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'enableHeartBeat' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1400 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1403 */           node = childNodeMap.getNamedItem("CommunicationRetries");
/* 1404 */           if (node == null) {
/* 1405 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'CommunicationRetries' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1406 */             Functions.applicationExit();
/* 1407 */           } else if (Functions.isInteger(node.getNodeValue())) {
/* 1408 */             mi.setCommRetries(Functions.convertString2Integer(node.getNodeValue()));
/*      */           } else {
/* 1410 */             Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'CommunicationRetries' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1411 */             Functions.applicationExit();
/*      */           } 
/*      */           
/* 1414 */           if (mi.getEnableHeartbeat()) {
/* 1415 */             node = childNodeMap.getNamedItem("HeartBeatData");
/* 1416 */             if (node == null) {
/* 1417 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'HeartBeatData' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1418 */               Functions.applicationExit();
/*      */             } else {
/* 1420 */               mi.setHeartBeatData(node.getNodeValue());
/*      */             } 
/*      */             
/* 1423 */             node = childNodeMap.getNamedItem("HeartBeatFreq");
/* 1424 */             if (node == null) {
/* 1425 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'HeartBeatFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1426 */               Functions.applicationExit();
/* 1427 */             } else if (Functions.isInteger(node.getNodeValue())) {
/* 1428 */               mi.setHeartBeatFrequency(Functions.convertString2Integer(node.getNodeValue()));
/*      */             } else {
/* 1430 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'HeartBeatFreq' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1431 */               Functions.applicationExit();
/*      */             } 
/*      */           } 
/*      */           
/* 1435 */           Element childElement = (Element)childNode;
/*      */           
/* 1437 */           NodeList moduleTypeList = childElement.getElementsByTagName("Product");
/* 1438 */           int size = moduleTypeList.getLength();
/* 1439 */           if (size > 0) {
/* 1440 */             ConcurrentHashMap<String, List<MonitoringGroupInfo>> mgiTable = new ConcurrentHashMap<>();
/*      */             
/* 1442 */             for (int j = 0; j < size; j++) {
/* 1443 */               Node mtNode = moduleTypeList.item(j);
/* 1444 */               childNodeMap = mtNode.getAttributes();
/* 1445 */               node = childNodeMap.getNamedItem("name");
/* 1446 */               String prodName = null;
/* 1447 */               if (node == null) {
/* 1448 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'name' under ModuleType Tag of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1449 */                 Functions.applicationExit();
/*      */               } else {
/* 1451 */                 prodName = node.getNodeValue();
/*      */               } 
/* 1453 */               if (prodName != null) {
/* 1454 */                 Element prodElement = (Element)mtNode;
/*      */                 
/* 1456 */                 NodeList prodList = prodElement.getElementsByTagName("Group");
/* 1457 */                 int prodSize = prodList.getLength();
/* 1458 */                 if (prodSize > 0) {
/*      */                   
/* 1460 */                   List<MonitoringGroupInfo> mgiList = new ArrayList<>();
/* 1461 */                   for (int k = 0; k < prodSize; k++) {
/* 1462 */                     MonitoringGroupInfo mgi = new MonitoringGroupInfo();
/* 1463 */                     NamedNodeMap prodNodeMap = prodList.item(k).getAttributes();
/*      */                     
/* 1465 */                     node = prodNodeMap.getNamedItem("GroupName");
/* 1466 */                     if (node == null) {
/* 1467 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'GroupName' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1468 */                       Functions.applicationExit();
/*      */                     } else {
/* 1470 */                       mgi.setGroupName(node.getNodeValue());
/*      */                     } 
/*      */                     
/* 1473 */                     node = prodNodeMap.getNamedItem("GroupId");
/* 1474 */                     if (node == null) {
/* 1475 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, "It was not possible to read the parameter 'GroupId' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1476 */                       Functions.applicationExit();
/* 1477 */                     } else if (Functions.isInteger(node.getNodeValue())) {
/* 1478 */                       mgi.setGroupId(Functions.convertString2Integer(node.getNodeValue()).intValue());
/*      */                     } else {
/* 1480 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, "Invalid format for parameter 'GroupId' of the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1481 */                       Functions.applicationExit();
/*      */                     } 
/*      */                     
/* 1484 */                     String prodLowcase = prodName.toLowerCase();
/* 1485 */                     if (tmpMap.containsKey(prodLowcase)) {
/* 1486 */                       if (((List)tmpMap.get(prodLowcase)).contains(Integer.valueOf(mgi.getGroupId()))) {
/* 1487 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Group ID (" + mgi.getGroupId() + ") Configured in More than one Monitoring Stations in the configuration file (ZeusServer.xml)\r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1488 */                         Functions.applicationExit();
/*      */                       } else {
/* 1490 */                         ((List<Integer>)tmpMap.get(prodLowcase)).add(Integer.valueOf(mgi.getGroupId()));
/*      */                       } 
/*      */                     } else {
/* 1493 */                       List<Integer> tmpGrpList = new ArrayList<>();
/* 1494 */                       tmpGrpList.add(Integer.valueOf(mgi.getGroupId()));
/* 1495 */                       tmpMap.put(prodLowcase, tmpGrpList);
/*      */                     } 
/* 1497 */                     mgiList.add(mgi);
/*      */                   } 
/* 1499 */                   mgiTable.put(prodName.toUpperCase(), mgiList);
/*      */                 } 
/*      */               } 
/*      */             } 
/* 1503 */             mi.setAssignedGroupsByProduct(mgiTable);
/*      */           } 
/*      */           
/* 1506 */           if (defaultIDMismatch && mi.getReceiverSerialPort().equalsIgnoreCase(getInstance().getDefaultMonitoringCOMPort())) {
/* 1507 */             defaultIDMismatch = false;
/*      */           }
/*      */           
/* 1510 */           _monitoringInfo.put(mi.getReceiverSerialPort(), mi);
/*      */         } 
/*      */         
/* 1513 */         if (defaultIDMismatch) {
/* 1514 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "'DefaultMonitoringCOMPort' is not Matching with Any MonitoringCOMPort - ReceiverSerialPort in the configuration file (ZeusServer.xml) \r\n", Enums.EnumMessagePriority.HIGH, null, null);
/* 1515 */           Functions.applicationExit();
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static List<Integer> getAssignedGroupIdsByProduct(String product) {
/* 1523 */     List<Integer> asList = new ArrayList<>();
/* 1524 */     if (getInstance().getMonitoringInfo() != null) {
/* 1525 */       for (Map.Entry<String, MonitoringInfo> receiver : getInstance().getMonitoringInfo().entrySet()) {
/* 1526 */         for (Map.Entry<String, List<MonitoringGroupInfo>> pdata : ((MonitoringInfo)receiver.getValue()).getAssignedGroupsByProduct().entrySet()) {
/* 1527 */           if (((String)pdata.getKey()).equalsIgnoreCase(product)) {
/* 1528 */             for (MonitoringGroupInfo mgi : pdata.getValue()) {
/* 1529 */               asList.add(Integer.valueOf(mgi.getGroupId()));
/*      */             }
/*      */           }
/*      */         } 
/*      */       } 
/*      */     }
/* 1535 */     return asList;
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\ZeusServerCfg.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */