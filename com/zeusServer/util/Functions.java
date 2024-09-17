/*      */ package com.zeusServer.util;
/*      */ 
/*      */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.GenericDBManager;
/*      */ import com.zeusServer.DBManagers.GriffonDBManager;
/*      */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*      */ import com.zeusServer.DBManagers.PegasusDBManager;
/*      */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*      */ import com.zeusServer.ddns.client.DDNSUpdater;
/*      */ import com.zeusServer.ddns.client.DNSMadeEasyUpdater;
/*      */ import com.zeusServer.ddns.client.DynDnsUpdater;
/*      */ import com.zeusServer.ddns.client.NoIPUpdater;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiver;
/*      */ import com.zeusServer.service.controller.LinuxServiceController;
/*      */ import com.zeusServer.service.controller.MacServiceController;
/*      */ import com.zeusServer.service.controller.ServiceController;
/*      */ import com.zeusServer.service.controller.WindowsServiceController;
/*      */ import com.zeusServer.tblConnections.TblActiveUserLogins;
/*      */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblPrinterSpool;
/*      */ import com.zeusServer.tblConnections.TblThreadsEmulaReceivers;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.net.InetAddress;
/*      */ import java.net.NetworkInterface;
/*      */ import java.net.SocketException;
/*      */ import java.sql.CallableStatement;
/*      */ import java.sql.Connection;
/*      */ import java.sql.DriverManager;
/*      */ import java.sql.SQLException;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Calendar;
/*      */ import java.util.Collections;
/*      */ import java.util.Comparator;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.TreeSet;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ import java.util.concurrent.ExecutorService;
/*      */ import java.util.concurrent.Executors;
/*      */ import java.util.concurrent.ScheduledFuture;
/*      */ import java.util.concurrent.ScheduledThreadPoolExecutor;
/*      */ import java.util.concurrent.ThreadFactory;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ import java.util.zip.ZipEntry;
/*      */ import java.util.zip.ZipInputStream;
/*      */ import java.util.zip.ZipOutputStream;
/*      */ import javax.xml.parsers.DocumentBuilder;
/*      */ import javax.xml.parsers.DocumentBuilderFactory;
/*      */ import javax.xml.transform.Transformer;
/*      */ import javax.xml.transform.TransformerFactory;
/*      */ import javax.xml.transform.dom.DOMSource;
/*      */ import javax.xml.transform.stream.StreamResult;
/*      */ import jcifs.smb.SmbException;
/*      */ import jcifs.smb.SmbFile;
/*      */ import jcifs.smb.SmbFileOutputStream;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.hyperic.sigar.win32.Win32Exception;
/*      */ import org.w3c.dom.Attr;
/*      */ import org.w3c.dom.Document;
/*      */ import org.w3c.dom.Element;
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
/*      */ public class Functions
/*      */ {
/*  107 */   private static DateFormat printerFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss >> ");
/*      */ 
/*      */   
/*  110 */   public static ConcurrentHashMap<String, String> zeusBoxSerialPortNames = new ConcurrentHashMap<>(3); private static final int NUM_THREADS_LOG_PUMPER = 50;
/*      */   static {
/*  112 */     zeusBoxSerialPortNames.put("/dev/ttyHS1", "2 (ttyHS1)");
/*  113 */     zeusBoxSerialPortNames.put("/dev/ttyHS2", "3 (ttyHS2)");
/*  114 */     zeusBoxSerialPortNames.put("/dev/ttyHS3", "4 (ttyHS3)");
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*  119 */   public static final ExecutorService logPumperHandler = Executors.newFixedThreadPool(50, new ThreadFactory()
/*      */       {
/*      */         public Thread newThread(Runnable r) {
/*  122 */           Thread t = new Thread(r);
/*  123 */           t.setName("MessagePumper");
/*  124 */           t.setPriority(1);
/*  125 */           t.setDaemon(true);
/*  126 */           return t;
/*      */         }
/*      */       });
/*      */ 
/*      */   
/*  131 */   private static ScheduledThreadPoolExecutor stpe = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(6, new ThreadFactory()
/*      */       {
/*      */         public Thread newThread(Runnable r) {
/*  134 */           Thread t = new Thread(r);
/*  135 */           t.setName("ScheduledThreadPoolExecutor");
/*  136 */           return t;
/*      */         }
/*      */       });
/*      */   
/*      */   public static ScheduledFuture addRunnable2ScheduleExecutor(Runnable task, long initialDelay, long repeatDelay, TimeUnit unit) {
/*  141 */     return stpe.scheduleWithFixedDelay(task, initialDelay, repeatDelay, unit);
/*      */   }
/*      */ 
/*      */   
/*      */   public static DDNSUpdater getDDNSServiceProviderClientByID(int id, String[] settings) {
/*  146 */     DDNSUpdater updater = null;
/*  147 */     if (id == 1) {
/*  148 */       NoIPUpdater noIPUpdater = new NoIPUpdater(settings);
/*  149 */     } else if (id == 2) {
/*  150 */       DNSMadeEasyUpdater dNSMadeEasyUpdater = new DNSMadeEasyUpdater(settings);
/*  151 */     } else if (id == 3) {
/*  152 */       DynDnsUpdater dynDnsUpdater = new DynDnsUpdater(settings);
/*      */     } else {
/*  154 */       updater = null;
/*      */     } 
/*  156 */     return updater;
/*      */   }
/*      */ 
/*      */   
/*      */   public static void applicationExit() {
/*  161 */     System.exit(1);
/*      */   }
/*      */ 
/*      */   
/*      */   public static void printMessage(Util.EnumProductIDs productId, String message, Enums.EnumMessagePriority priority, String iccid, Throwable th) {
/*  166 */     boolean showMessage = true;
/*      */     try {
/*  168 */       if (iccid != null && iccid.length() > 0) {
/*  169 */         switch (productId) {
/*      */           case WINDOWS:
/*  171 */             if (TblPegasusActiveConnections.getInstance().containsKey(iccid)) {
/*  172 */               message = "[" + ((InfoModule)TblPegasusActiveConnections.getInstance().get(iccid)).clientName + "] " + message;
/*  173 */               showMessage = ((InfoModule)TblPegasusActiveConnections.getInstance().get(iccid)).communicationDebug;
/*      */             } 
/*      */             break;
/*      */           case LINUX:
/*      */           case ARM:
/*  178 */             if (TblGriffonActiveConnections.getInstance().containsKey(iccid)) {
/*  179 */               message = "[" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(iccid)).clientName + "] " + message;
/*  180 */               showMessage = ((InfoModule)TblGriffonActiveConnections.getInstance().get(iccid)).communicationDebug;
/*      */             } 
/*      */             break;
/*      */           case MAC:
/*  184 */             if (TblMercuriusActiveConnections.getInstance().containsKey(iccid)) {
/*  185 */               message = "[" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(iccid)).clientName + "] " + message;
/*  186 */               showMessage = ((InfoModule)TblMercuriusActiveConnections.getInstance().get(iccid)).communicationDebug;
/*      */             } 
/*      */             break;
/*      */         } 
/*      */       }
/*  191 */       if (showMessage) {
/*  192 */         String errorMsg = message;
/*  193 */         if (th != null) {
/*  194 */           errorMsg = message.concat(th.toString());
/*      */         }
/*  196 */         int msgLen = errorMsg.length();
/*  197 */         byte[] log = new byte[msgLen + 4];
/*  198 */         log[0] = -112;
/*  199 */         byte[] len = get2ByteArrayFromInt(msgLen);
/*  200 */         log[1] = len[0];
/*  201 */         log[2] = len[1];
/*  202 */         log[3] = (byte)productId.getProductId();
/*      */         try {
/*  204 */           System.arraycopy(errorMsg.getBytes("ISO-8859-1"), 0, log, 4, msgLen);
/*  205 */         } catch (UnsupportedEncodingException ex) {
/*  206 */           System.arraycopy(errorMsg.getBytes(), 0, log, 4, msgLen);
/*      */         } 
/*  208 */         pumpMessage2RemoteUI(productId.getProductId(), log, iccid, 144, 0, 0);
/*      */         
/*  210 */         if (ZeusServerLogger.isDebugMessageLogger(productId)) {
/*  211 */           ZeusServerLogger.logErrorMessage(productId, message, th);
/*      */         } else {
/*  213 */           ZeusServerLogger.logMessage(productId, errorMsg);
/*      */         }
/*      */       
/*      */       } 
/*  217 */     } catch (RuntimeException runtimeException) {}
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String printEvent(int productId, byte[] bufferEvent, int idGroup, int idClient) throws SQLException, Exception {
/*  224 */     String formattedEventForPrinting = null;
/*  225 */     String sn = "";
/*      */     try {
/*  227 */       switch (Util.EnumProductIDs.getProductID(productId)) {
/*      */         case WINDOWS:
/*  229 */           sn = PegasusDBManager.getSnFromPegausModule(idClient);
/*      */           break;
/*      */         case LINUX:
/*      */         case ARM:
/*  233 */           sn = GriffonDBManager.getSnFromGriffonModule(idClient);
/*      */           break;
/*      */         case MAC:
/*  236 */           sn = MercuriusDBManager.getSnFromMercuriusModule(idClient);
/*      */           break;
/*      */         default:
/*  239 */           sn = null;
/*      */           break;
/*      */       } 
/*      */       
/*  243 */       formattedEventForPrinting = formatEventForPrinting(bufferEvent, sn);
/*  244 */       if (ZeusServerCfg.getInstance().getEnableSerialPrinter()) {
/*  245 */         TblPrinterSpool.addPrintJob(printerFormat.format(new Date()) + formattedEventForPrinting);
/*      */       }
/*  247 */       ZeusServerLogger.logEvent(Util.EnumProductIDs.getProductID(productId), formattedEventForPrinting);
/*  248 */       int msgLen = formattedEventForPrinting.length();
/*  249 */       byte[] log = new byte[msgLen + 4];
/*  250 */       log[0] = -111;
/*  251 */       byte[] len = get2ByteArrayFromInt(msgLen);
/*  252 */       log[1] = len[0];
/*  253 */       log[2] = len[1];
/*  254 */       log[3] = (byte)productId;
/*  255 */       System.arraycopy(formattedEventForPrinting.getBytes(), 0, log, 4, msgLen);
/*  256 */       pumpMessage2RemoteUI(productId, log, null, 145, idGroup, idClient);
/*  257 */       return formattedEventForPrinting;
/*  258 */     } catch (RuntimeException re) {
/*      */       
/*  260 */       return formattedEventForPrinting;
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static void pumpMessage2RemoteUI(int productId, byte[] message, String iccid, int packetIdentifier, int groupId, int clientId) {
/*  266 */     if (packetIdentifier == 146 && GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  267 */       Runnable runnable = new RemoteUILogPumper("localhost", message, 8888);
/*  268 */       logPumperHandler.execute(runnable);
/*      */     } 
/*  270 */     if (TblActiveUserLogins.getInstance().size() > 0) {
/*  271 */       List<String> removalList = new ArrayList<>();
/*  272 */       if (TblActiveUserLogins.getInstance().containsKey("SUPER_ADMIN")) {
/*  273 */         for (Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>> entry : (Iterable<Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>>>)((ConcurrentHashMap)TblActiveUserLogins.getInstance().get("SUPER_ADMIN")).entrySet()) {
/*  274 */           for (Map.Entry<String, LoggedInUser> user : (Iterable<Map.Entry<String, LoggedInUser>>)((ConcurrentHashMap)entry.getValue()).entrySet()) {
/*  275 */             if (((LoggedInUser)user.getValue()).getLastAlivePacketReceived() > System.currentTimeMillis()) {
/*  276 */               Runnable runnable = new RemoteUILogPumper(((LoggedInUser)user.getValue()).getRemoteIp(), message, ((LoggedInUser)user.getValue()).getRemoteUdpPort());
/*  277 */               logPumperHandler.execute(runnable); continue;
/*      */             } 
/*  279 */             removalList.add(user.getKey());
/*      */           } 
/*      */         } 
/*      */       }
/*      */ 
/*      */       
/*  285 */       if (TblActiveUserLogins.getInstance().containsKey("ADMIN")) {
/*  286 */         for (Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>> entry : (Iterable<Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>>>)((ConcurrentHashMap)TblActiveUserLogins.getInstance().get("ADMIN")).entrySet()) {
/*  287 */           for (Map.Entry<String, LoggedInUser> user : (Iterable<Map.Entry<String, LoggedInUser>>)((ConcurrentHashMap)entry.getValue()).entrySet()) {
/*  288 */             if (((LoggedInUser)user.getValue()).getLastAlivePacketReceived() > System.currentTimeMillis()) {
/*  289 */               if (((LoggedInUser)user.getValue()).getAssignedProducts().contains(String.valueOf(productId)) || (message[0] & 0xFF) == 146 || (message[0] & 0xFF) == 148) {
/*  290 */                 Runnable runnable = new RemoteUILogPumper(((LoggedInUser)user.getValue()).getRemoteIp(), message, ((LoggedInUser)user.getValue()).getRemoteUdpPort());
/*  291 */                 logPumperHandler.execute(runnable);
/*      */               }  continue;
/*      */             } 
/*  294 */             removalList.add(user.getKey());
/*      */           } 
/*      */         } 
/*      */       }
/*      */ 
/*      */       
/*  300 */       if (TblActiveUserLogins.getInstance().containsKey("PRODUCT_USER")) {
/*  301 */         for (Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>> entry : (Iterable<Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>>>)((ConcurrentHashMap)TblActiveUserLogins.getInstance().get("PRODUCT_USER")).entrySet()) {
/*  302 */           for (Map.Entry<String, LoggedInUser> user : (Iterable<Map.Entry<String, LoggedInUser>>)((ConcurrentHashMap)entry.getValue()).entrySet()) {
/*  303 */             if (((LoggedInUser)user.getValue()).getLastAlivePacketReceived() > System.currentTimeMillis()) {
/*  304 */               if (((LoggedInUser)user.getValue()).getAssignedProducts().contains(String.valueOf(productId)) || (message[0] & 0xFF) == 146 || (message[0] & 0xFF) == 148) {
/*  305 */                 Runnable runnable = new RemoteUILogPumper(((LoggedInUser)user.getValue()).getRemoteIp(), message, ((LoggedInUser)user.getValue()).getRemoteUdpPort());
/*  306 */                 logPumperHandler.execute(runnable);
/*      */               }  continue;
/*      */             } 
/*  309 */             removalList.add(user.getKey());
/*      */           } 
/*      */         } 
/*      */       }
/*      */       
/*  314 */       if (removalList.size() > 0) {
/*  315 */         for (String removeIP : removalList) {
/*  316 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.NORMAL) {
/*  317 */             TblActiveUserLogins.removeConnection(removeIP);
/*      */           }
/*      */         } 
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static byte[] intToByteArray(int value, int sizeOfByte) {
/*  326 */     byte[] b = new byte[sizeOfByte];
/*  327 */     for (int i = 0; i < sizeOfByte; i++) {
/*  328 */       int offset = (b.length - 1 - i) * 8;
/*  329 */       b[i] = (byte)(value >>> offset & 0xFF);
/*      */     } 
/*  331 */     return b;
/*      */   }
/*      */   
/*      */   public static boolean isInteger(String value) {
/*      */     try {
/*  336 */       Integer.parseInt(value);
/*  337 */       return true;
/*  338 */     } catch (NumberFormatException nfe) {
/*  339 */       return false;
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static boolean isDate(String date) {
/*      */     try {
/*  346 */       SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
/*  347 */       sdf.setLenient(false);
/*  348 */       sdf.parse(date);
/*  349 */       return true;
/*  350 */     } catch (ParseException e) {
/*  351 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public static boolean isInteger(String value, int radix) {
/*      */     try {
/*  357 */       Integer.parseInt(value, radix);
/*  358 */       return true;
/*  359 */     } catch (NumberFormatException nfe) {
/*  360 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public static byte convertString2Byte(String value) {
/*  365 */     return Byte.parseByte(value);
/*      */   }
/*      */   
/*      */   public static Integer convertString2Integer(String value) {
/*  369 */     return Integer.valueOf(value);
/*      */   }
/*      */   
/*      */   public static Integer convertString2Integer(String value, int radix) {
/*  373 */     return Integer.valueOf(value, radix);
/*      */   }
/*      */   
/*      */   public static String getString(byte[] data) {
/*  377 */     StringBuilder sb = new StringBuilder();
/*  378 */     for (int i = 0; i < data.length; i++) {
/*  379 */       if (data[i] > 0) {
/*  380 */         sb.append((char)data[i]);
/*      */       }
/*      */     } 
/*  383 */     return sb.toString();
/*      */   }
/*      */   
/*      */   public static String getString4mByteArray(byte[] data, int length) {
/*  387 */     StringBuilder sb = new StringBuilder();
/*  388 */     for (int i = 0; i < length; i++) {
/*  389 */       sb.append((char)data[i]);
/*      */     }
/*  391 */     return sb.toString();
/*      */   }
/*      */   
/*      */   public static byte[] converthexString2ByteArray(String hex) {
/*  395 */     byte[] buff = new byte[hex.length() / 2];
/*  396 */     int temp = 0;
/*  397 */     for (int i = 0; i < buff.length; i++) {
/*  398 */       temp = i * 2;
/*  399 */       buff[i] = (byte)Integer.parseInt(hex.substring(temp, temp + 2), 16);
/*      */     } 
/*  401 */     return buff;
/*      */   }
/*      */ 
/*      */   
/*      */   public static String formatToHex(int v, int numberOfDigits) {
/*  406 */     StringBuilder tmp = new StringBuilder();
/*  407 */     tmp.append(Integer.toHexString(v));
/*  408 */     while (tmp.length() < numberOfDigits) {
/*  409 */       tmp.insert(0, 0);
/*      */     }
/*  411 */     return tmp.toString();
/*      */   }
/*      */   
/*      */   public static String convertInt2Hex(int number) {
/*  415 */     return Integer.toHexString(number);
/*      */   }
/*      */   
/*      */   public static String formatEventForPrinting(byte[] bufferEvent, String sn) {
/*  419 */     String evento = "";
/*  420 */     if (bufferEvent.length == 8) {
/*      */       
/*  422 */       for (byte numeroByte = 0; numeroByte <= 7; numeroByte = (byte)(numeroByte + 1)) {
/*  423 */         evento = evento + convertContactIdDigitToHex((bufferEvent[numeroByte] & 0xF0) / 16) + convertContactIdDigitToHex(bufferEvent[numeroByte] & 0xF);
/*      */       }
/*  425 */       if (sn == null || sn.equals("")) {
/*  426 */         return evento.substring(0, 4) + "-" + evento.substring(4, 6) + "-" + evento.substring(6, 7) + "-" + evento.substring(7, 10) + "-" + evento.substring(10, 12) + "-" + evento.substring(12, 15) + "-" + evento.substring(15, 16);
/*      */       }
/*  428 */       return "[" + sn + "] " + evento.substring(0, 4) + "-" + evento.substring(4, 6) + "-" + evento.substring(6, 7) + "-" + evento.substring(7, 10) + "-" + evento.substring(10, 12) + "-" + evento.substring(12, 15) + "-" + evento.substring(15, 16);
/*      */     } 
/*      */     
/*  431 */     return LocaleMessage.getLocaleMessage("[Invalid_Format]");
/*      */   }
/*      */ 
/*      */   
/*      */   public static String convertContactIdDigitToHex(int v) {
/*  436 */     return (v == 10) ? "0" : Integer.toHexString(v).toUpperCase();
/*      */   }
/*      */   
/*      */   public static Enums.Platform detectPlatform() {
/*  440 */     String osName = System.getProperty("os.name").toLowerCase();
/*  441 */     if (osName.contains("win"))
/*  442 */       return Enums.Platform.WINDOWS; 
/*  443 */     if (osName.contains("mac"))
/*  444 */       return Enums.Platform.MAC; 
/*  445 */     if (osName.contains("nix") || osName.contains("nux"))
/*  446 */       return Enums.Platform.LINUX; 
/*  447 */     if (osName.contains("sunos")) {
/*  448 */       return Enums.Platform.SOLARIS;
/*      */     }
/*  450 */     return Enums.Platform.EMPTY;
/*      */   }
/*      */   
/*      */   public static ServiceController getServiceControllerByName(String sName) {
/*      */     MacServiceController macServiceController;
/*  455 */     ServiceController sController = null; try {
/*      */       WindowsServiceController windowsServiceController;
/*      */       LinuxServiceController linuxServiceController;
/*  458 */       switch (GlobalVariables.currentPlatform) {
/*      */         case WINDOWS:
/*  460 */           windowsServiceController = new WindowsServiceController(sName);
/*      */           break;
/*      */         case LINUX:
/*      */         case ARM:
/*  464 */           linuxServiceController = new LinuxServiceController(sName);
/*      */           break;
/*      */         case MAC:
/*  467 */           macServiceController = new MacServiceController(sName);
/*      */           break;
/*      */       } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  474 */     } catch (Win32Exception ex) {
/*  475 */       ex.printStackTrace();
/*      */     } 
/*      */     
/*  478 */     return (ServiceController)macServiceController;
/*      */   }
/*      */   
/*      */   public static Long updateWatchdog(Long wdt, long sleep) {
/*  482 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*      */     try {
/*  484 */       Thread.sleep(sleep);
/*  485 */     } catch (InterruptedException interruptedException) {}
/*      */     
/*  487 */     return wdt;
/*      */   }
/*      */   
/*      */   public static void eventTransmissionBeep() {
/*  491 */     byte[] log = new byte[4];
/*  492 */     log[0] = -108;
/*  493 */     byte[] len = get2ByteArrayFromInt(1);
/*  494 */     log[1] = len[0];
/*  495 */     log[2] = len[1];
/*  496 */     log[3] = 1;
/*  497 */     pumpMessage2RemoteUI(0, log, null, 148, 0, 0);
/*      */   }
/*      */ 
/*      */   
/*      */   public static long getOldestKey(Set<? extends Long> keys) {
/*  502 */     TreeSet<Long> ts = new TreeSet<>(keys);
/*  503 */     long old = ((Long)ts.first()).longValue();
/*  504 */     return old;
/*      */   }
/*      */   
/*      */   public static Integer getOldestInfoSMSByDate(Hashtable<Integer, InfoSms> tblSMS) {
/*  508 */     int key = 0, i = 0;
/*  509 */     Date tmpDate = null;
/*  510 */     for (Map.Entry<Integer, InfoSms> iSMS : tblSMS.entrySet()) {
/*  511 */       if (i == 0) {
/*  512 */         key = ((Integer)iSMS.getKey()).intValue();
/*  513 */         tmpDate = ((InfoSms)iSMS.getValue()).gettimeStamp();
/*      */       } 
/*  515 */       if (i++ > 0 && 
/*  516 */         tmpDate.after(((InfoSms)iSMS.getValue()).gettimeStamp())) {
/*  517 */         key = ((Integer)iSMS.getKey()).intValue();
/*  518 */         tmpDate = ((InfoSms)iSMS.getValue()).gettimeStamp();
/*      */       } 
/*      */     } 
/*      */     
/*  522 */     return Integer.valueOf(key);
/*      */   }
/*      */   
/*      */   public static byte[] convertBufferContactIdInBufferHex(byte[] buffer) {
/*  526 */     for (byte numeroDigito = 0; numeroDigito < 8; numeroDigito = (byte)(numeroDigito + 1)) {
/*  527 */       if ((buffer[numeroDigito] & 0xFF & 0xF) == 10) {
/*  528 */         buffer[numeroDigito] = (byte)(buffer[numeroDigito] & 0xFF & 0xF0);
/*      */       }
/*      */       
/*  531 */       if ((buffer[numeroDigito] & 0xFF & 0xF0) == 160) {
/*  532 */         buffer[numeroDigito] = (byte)(buffer[numeroDigito] & 0xFF & 0xF);
/*      */       }
/*      */     } 
/*  535 */     return buffer;
/*      */   }
/*      */   
/*      */   public static Date format2DateTime(String date) throws ParseException {
/*  539 */     DateFormat df = new SimpleDateFormat("yy/MM/dd,HH:mm:ss");
/*  540 */     return df.parse(date);
/*      */   }
/*      */   
/*      */   public static String formatDate2String(Date date) {
/*  544 */     DateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
/*  545 */     if (date == null) {
/*  546 */       return format.format(new Date());
/*      */     }
/*  548 */     return format.format(date);
/*      */   }
/*      */ 
/*      */   
/*      */   public static void saveEvent(int productId, int idModule, int idGroup, int idClient, String comPort, String clientCode, String infoEvent, Enums.EnumEventQualifier enumEventQualifier, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface, int eventDesc, int isMonitorEvent) throws SQLException, InterruptedException, Exception {
/*  553 */     byte[] bufferEvent = new byte[8];
/*  554 */     clientCode = clientCode.replaceAll("0", "A");
/*  555 */     bufferEvent[0] = (byte)Integer.parseInt(clientCode.substring(0, 2), 16);
/*  556 */     bufferEvent[1] = (byte)Integer.parseInt(clientCode.substring(2, 4), 16);
/*  557 */     bufferEvent[2] = 24;
/*      */     
/*  559 */     infoEvent = infoEvent.replaceAll("0", "A");
/*  560 */     bufferEvent[3] = (byte)Integer.parseInt(String.valueOf(enumEventQualifier.getEvent()) + infoEvent.charAt(0), 16);
/*  561 */     bufferEvent[4] = (byte)Integer.parseInt(infoEvent.substring(1, 3), 16);
/*  562 */     bufferEvent[5] = (byte)Integer.parseInt(infoEvent.substring(3, 5), 16);
/*  563 */     bufferEvent[6] = (byte)Integer.parseInt(infoEvent.substring(5, 7), 16);
/*  564 */     bufferEvent[7] = (byte)Integer.parseInt(infoEvent.substring(7, 8) + '0', 16);
/*      */     
/*  566 */     int checksum = 0;
/*      */     byte numByte;
/*  568 */     for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/*  569 */       byte tmp = (byte)(((numByte & 0x1) == 0) ? (bufferEvent[numByte / 2] / 16) : bufferEvent[numByte / 2]);
/*  570 */       checksum += tmp & 0xF;
/*      */     } 
/*  572 */     bufferEvent[7] = (byte)(bufferEvent[7] + (byte)((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  578 */     comPort = (comPort == null) ? getReceiverCOMPortByGroupID(idGroup, GenericDBManager.getSchemaNameByProductId(productId)) : comPort;
/*      */     
/*  580 */     insertEvent(productId, idModule, idGroup, idClient, comPort, (byte)Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId(), bufferEvent, lastMProtocolRcvd, nwProtocol, lastCommInterface, eventDesc, isMonitorEvent);
/*      */     
/*  582 */     if (TblThreadsEmulaReceivers.getInstance().get(comPort) != null && TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/*  583 */       ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */     }
/*      */   }
/*      */   
/*      */   public static Calendar addTime2Calendar(Calendar cal, int key, int value) {
/*  588 */     cal.set(key, cal.get(key) + value);
/*  589 */     return cal;
/*      */   }
/*      */   
/*      */   public static void insertEvent(int productId, int idModule, int idGroup, int idClient, String comPort, byte alarmPanelProtocol, byte[] bufferEvent, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface, int eventDesc, int isMonitorEvent) throws SQLException, InterruptedException, Exception {
/*  593 */     if (bufferEvent.length == 8) {
/*  594 */       StringBuilder originalEvent = new StringBuilder();
/*      */       byte numByte;
/*  596 */       for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/*  597 */         originalEvent.append(convertContactIdDigitToHex((bufferEvent[numByte] & 0xF0) / 16)).append(convertContactIdDigitToHex(bufferEvent[numByte] & 0xF));
/*      */       }
/*  599 */       String sEvent = originalEvent.toString();
/*      */ 
/*      */       
/*  602 */       if (ZeusServerCfg.getInstance().getMonitoringInfo() == null || ZeusServerCfg.getInstance().getMonitoringInfo().isEmpty()) {
/*  603 */         comPort = "NONE";
/*      */       }
/*  605 */       comPort = (comPort == null) ? getReceiverCOMPortByGroupID(idGroup, GenericDBManager.getSchemaNameByProductId(productId)) : comPort;
/*  606 */       if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().containsKey(comPort)) {
/*  607 */         if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.ADICIONAR.getPartitionScheme()) {
/*  608 */           int partition = Integer.parseInt(sEvent.substring(10, 12), 16);
/*  609 */           partition = (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue() > 99) ? 99 : (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue());
/*  610 */           sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { Integer.valueOf(partition) }) + sEvent.substring(12, 16);
/*  611 */         } else if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.SUBSTITUIR.getPartitionScheme()) {
/*  612 */           sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme() }) + sEvent.substring(12, 16);
/*      */         } 
/*      */       }
/*      */       
/*  616 */       if (!sEvent.equals(originalEvent.toString())) {
/*  617 */         byte checksum = 0;
/*  618 */         sEvent = sEvent.substring(0, 15).replaceAll("0", "A");
/*  619 */         for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/*  620 */           checksum = (byte)(checksum + Integer.parseInt(sEvent.substring(numByte, numByte + 1), 16));
/*      */         }
/*  622 */         sEvent = sEvent.concat(convertInt2Hex((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/*  623 */         for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/*  624 */           bufferEvent[numByte] = (byte)Integer.parseInt(sEvent.substring(numByte * 2, numByte * 2 + 2), 16);
/*      */         }
/*  626 */         sEvent = sEvent.substring(0, 15).replaceAll("A", "0");
/*      */       } 
/*      */ 
/*      */       
/*  630 */       printEvent(productId, bufferEvent, idGroup, idClient);
/*      */ 
/*      */       
/*  633 */       switch (Util.EnumProductIDs.getProductID(productId)) {
/*      */         case SOLARIS:
/*  635 */           ZeusSettingsDBManager.executeSP_S006(idModule, idGroup, comPort, (short)alarmPanelProtocol, sEvent.substring(0, 4), bufferEvent, lastMProtocolRcvd, nwProtocol, lastCommInterface, isMonitorEvent);
/*      */           break;
/*      */         case WINDOWS:
/*  638 */           PegasusDBManager.executeSP_014(idModule, idGroup, comPort, (short)alarmPanelProtocol, sEvent.substring(0, 4), bufferEvent, lastMProtocolRcvd, nwProtocol, lastCommInterface);
/*      */           break;
/*      */         case LINUX:
/*      */         case ARM:
/*  642 */           GriffonDBManager.insertEvents(idModule, idGroup, alarmPanelProtocol, eventDesc, sEvent.substring(0, 4), (sEvent.charAt(6) == '1') ? "E".concat(sEvent.substring(7, 10)) : "R".concat(sEvent.substring(7, 10)), sEvent.substring(10, 12), sEvent.substring(12, 15));
/*      */           break;
/*      */         case MAC:
/*  645 */           MercuriusDBManager.executeSP_014(idModule, idGroup, (short)alarmPanelProtocol, bufferEvent);
/*      */           break;
/*      */       } 
/*      */       
/*  649 */       if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/*  650 */         ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */       }
/*      */     } else {
/*  653 */       printMessage(Util.EnumProductIDs.getProductID(productId), LocaleMessage.getLocaleMessage("An_event_was_received/generated_with_incorrect_size"), Enums.EnumMessagePriority.HIGH, null, null);
/*  654 */       GlobalVariables.buzzerActivated = true;
/*      */     } 
/*      */   }
/*      */   
/*      */   public static boolean isDbInCleanupState() {
/*  659 */     return (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEANUP || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_PEGASUS_CMD_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_PEGASUS_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_PEGASUS_OCCUR_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_GRCP_CMD_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_GRCP_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_GRCP_OCCUR_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_AVL_CMD_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_AVL_EVENT_TABLE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.CLEAN_AVL_OCCUR_TABLE);
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
/*      */   public static File writeByteArrayToFile(String fileName, byte[] data) {
/*  679 */     File file = null;
/*      */ 
/*      */     
/*  682 */     try { file = new File(fileName);
/*  683 */       FileOutputStream foStream = new FileOutputStream(file);
/*  684 */       foStream.write(data);
/*  685 */       foStream.close(); }
/*  686 */     catch (FileNotFoundException fileNotFoundException) {  }
/*  687 */     catch (IOException io)
/*  688 */     { io.printStackTrace(); }
/*      */     
/*  690 */     return file;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] getByteArrayFromFile(String fileName) {
/*  700 */     File file = new File(fileName);
/*  701 */     byte[] filecontent = null;
/*      */ 
/*      */     
/*  704 */     try { FileInputStream fin = new FileInputStream(file);
/*  705 */       filecontent = new byte[(int)file.length()];
/*  706 */       int i = fin.read(filecontent);
/*  707 */       fin.close(); }
/*  708 */     catch (FileNotFoundException fileNotFoundException) {  }
/*  709 */     catch (IOException iOException) {}
/*      */ 
/*      */     
/*  712 */     return filecontent;
/*      */   }
/*      */ 
/*      */   
/*      */   public static File unZipIt(File zipFile) throws FileNotFoundException, IOException {
/*  717 */     byte[] buffer = new byte[1024];
/*  718 */     String outputFolder = zipFile.getParent() + "/" + zipFile.getName();
/*  719 */     outputFolder = outputFolder.substring(0, outputFolder.length() - 4);
/*  720 */     File folder = new File(outputFolder);
/*  721 */     if (!folder.exists()) {
/*  722 */       folder.mkdir();
/*      */     }
/*      */     
/*  725 */     ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
/*  726 */     ZipEntry ze = zis.getNextEntry();
/*      */     
/*  728 */     while (ze != null) {
/*  729 */       String fileName = ze.getName();
/*  730 */       File newFile = new File(outputFolder + File.separator + fileName);
/*  731 */       (new File(newFile.getParent())).mkdirs();
/*  732 */       FileOutputStream fos = new FileOutputStream(newFile);
/*      */       int len;
/*  734 */       while ((len = zis.read(buffer)) > 0) {
/*  735 */         fos.write(buffer, 0, len);
/*      */       }
/*  737 */       fos.close();
/*  738 */       ze = zis.getNextEntry();
/*      */     } 
/*      */     
/*  741 */     zis.closeEntry();
/*  742 */     zis.close();
/*  743 */     return folder;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void getAllFiles(File file, List<File> fileList) {
/*  753 */     File[] files = file.listFiles();
/*  754 */     if (files != null) {
/*  755 */       for (File f : files) {
/*  756 */         fileList.add(f);
/*  757 */         if (f.isDirectory()) {
/*  758 */           getAllFiles(f, fileList);
/*      */         }
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
/*      */   public static void writeZipFile(File directoryToZip, List<File> fileList) throws FileNotFoundException, IOException {
/*  773 */     FileOutputStream fos = new FileOutputStream(directoryToZip.getParent() + "/" + directoryToZip.getName() + ".zip");
/*  774 */     ZipOutputStream zos = new ZipOutputStream(fos);
/*      */     
/*  776 */     for (File file : fileList) {
/*  777 */       if (!file.isDirectory()) {
/*  778 */         addToZip(directoryToZip, file, zos);
/*      */       }
/*      */     } 
/*  781 */     zos.close();
/*  782 */     fos.close();
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
/*      */   public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
/*  795 */     FileInputStream fis = new FileInputStream(file);
/*      */ 
/*      */ 
/*      */     
/*  799 */     String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
/*  800 */     ZipEntry zipEntry = new ZipEntry(zipFilePath);
/*  801 */     zos.putNextEntry(zipEntry);
/*      */     
/*  803 */     byte[] bytes = new byte[1024];
/*      */     int length;
/*  805 */     while ((length = fis.read(bytes)) >= 0) {
/*  806 */       zos.write(bytes, 0, length);
/*      */     }
/*      */     
/*  809 */     zos.closeEntry();
/*  810 */     fis.close();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void deleteDirectory(File dir) {
/*  819 */     if (dir != null && dir.isDirectory() && dir.exists()) {
/*  820 */       File[] list = dir.listFiles();
/*  821 */       for (File file : list) {
/*  822 */         if (file.isDirectory()) {
/*  823 */           deleteDirectory(file);
/*      */         } else {
/*  825 */           file.delete();
/*      */         } 
/*      */       } 
/*  828 */       dir.delete();
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] swapLSB2MSB(byte[] data) {
/*  839 */     byte msb = data[0];
/*  840 */     data[0] = data[1];
/*  841 */     data[1] = msb;
/*  842 */     return data;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int getIntFrom2ByteArray(byte[] data) {
/*  852 */     int i = 0;
/*  853 */     i |= data[0] & 0xFF;
/*  854 */     i <<= 8;
/*  855 */     i |= data[1] & 0xFF;
/*  856 */     return i;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int getSignedIntFrom2ByteArray(byte[] data) {
/*  866 */     int i = 0;
/*  867 */     i |= data[0];
/*  868 */     i <<= 8;
/*  869 */     i |= data[1] & 0xFF;
/*  870 */     return i;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int getIntFromHexByte(byte b) {
/*  880 */     return Integer.parseInt(Integer.toHexString(b & 0xFF), 16);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String getFormatIntFromHexByte(byte b) {
/*  891 */     int valueToBeConverted = Integer.parseInt(Integer.toHexString(b & 0xFF), 16);
/*  892 */     if (valueToBeConverted > 99) {
/*  893 */       return String.format("%02x", new Object[] { Integer.valueOf(valueToBeConverted) });
/*      */     }
/*  895 */     return String.format("%02d", new Object[] { Integer.valueOf(valueToBeConverted) });
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String getASCIIFromByteArray(byte[] data) {
/*  906 */     StringBuilder sb = new StringBuilder();
/*  907 */     for (int i = 0; i < data.length; i++) {
/*  908 */       sb.append((char)(data[i] & 0xFF));
/*      */     }
/*  910 */     return sb.toString();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int getIntFrom4ByteArray(byte[] data) {
/*  920 */     return (data[0] & 0xFF) << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | data[3] & 0xFF;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static char[] getBinaryFromByte(byte b) {
/*  930 */     return String.format("%8s", new Object[] { Integer.toBinaryString(b) }).replace(' ', '0').toCharArray();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static char[] getBinaryFromByte(int b) {
/*  940 */     return String.format("%8s", new Object[] { Integer.toBinaryString(b) }).replace(' ', '0').toCharArray();
/*      */   }
/*      */   
/*      */   public static char[] get16BitBinaryFromInt(int b) {
/*  944 */     return String.format("%16s", new Object[] { Integer.toBinaryString(b) }).replace(' ', '0').toCharArray();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String getHexStringFromByteArray(byte[] data) {
/*  955 */     StringBuilder sb = new StringBuilder();
/*  956 */     for (int i = data.length - 1; i >= 0; i--) {
/*  957 */       sb.append(String.format("%02x", new Object[] { Byte.valueOf(data[i]) }));
/*      */     } 
/*  959 */     return sb.toString();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] swapLSB2MSB4ByteArray(byte[] data) {
/*  969 */     byte[] rev = new byte[data.length];
/*  970 */     int j = 0;
/*  971 */     for (int i = data.length - 1; i >= 0; i--) {
/*  972 */       rev[j++] = data[i];
/*      */     }
/*  974 */     return rev;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] get4ByteArrayFromInt(int i) {
/*  984 */     byte[] data = new byte[4];
/*  985 */     data[3] = (byte)(i & 0xFF);
/*  986 */     data[2] = (byte)(i >> 8 & 0xFF);
/*  987 */     data[1] = (byte)(i >> 16 & 0xFF);
/*  988 */     data[0] = (byte)(i >> 24 & 0xFF);
/*  989 */     return data;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] get2ByteArrayFromInt(int i) {
/*  999 */     byte[] data = new byte[2];
/* 1000 */     data[1] = (byte)(i & 0xFF);
/* 1001 */     data[0] = (byte)(i >> 8 & 0xFF);
/* 1002 */     return data;
/*      */   }
/*      */   
/*      */   public static int getIntegerFromSelectedParitions(String selPartition) {
/* 1006 */     String[] c = selPartition.split(",");
/* 1007 */     int i = 0;
/* 1008 */     for (String ch : c) {
/* 1009 */       i |= 1 << Integer.parseInt(ch) - 1;
/*      */     }
/*      */     
/* 1012 */     return i;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static byte[] getASCII4mString(String s) {
/* 1022 */     char[] cdata = s.toCharArray();
/* 1023 */     byte[] ascii = new byte[cdata.length];
/* 1024 */     int i = 0;
/* 1025 */     for (char c : cdata) {
/* 1026 */       ascii[i++] = (byte)(c & 0xFF);
/*      */     }
/* 1028 */     return ascii;
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
/*      */   public static int getIntFrom4CharBinaryBits(char one, char two, char three, char four) {
/* 1042 */     return Integer.parseInt("" + one + two + three + four, 2);
/*      */   }
/*      */   
/*      */   public static byte[] getHighLowBytes(int i) {
/* 1046 */     byte[] b = new byte[2];
/* 1047 */     b[0] = (byte)(i & 0xF);
/* 1048 */     b[1] = (byte)(i >> 4 & 0xF);
/* 1049 */     return b;
/*      */   }
/*      */   
/*      */   public static char[] getBinaryFromInt(int b) {
/* 1053 */     return String.format("%16s", new Object[] { Integer.toBinaryString(b) }).replace(' ', '0').toCharArray();
/*      */   }
/*      */   
/*      */   public static Date getDateFromInt(long val) throws ParseException {
/* 1057 */     DateFormat dd = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
/* 1058 */     dd.setTimeZone(TimeZone.getTimeZone("GMT"));
/* 1059 */     return dd.parse(dd.format(new Date(val * 1000L)));
/*      */   }
/*      */   
/*      */   public static Date convert2GMT(Date date, int timeZone) {
/* 1063 */     TimeZone received = TimeZone.getTimeZone(Defines.TIMEZONE_NAMES.get(Integer.valueOf(timeZone)));
/* 1064 */     Date gmtDate = new Date(date.getTime() - received.getRawOffset());
/*      */     
/* 1066 */     if (received.inDaylightTime(gmtDate)) {
/* 1067 */       gmtDate = new Date(gmtDate.getTime() - received.getDSTSavings());
/*      */     }
/* 1069 */     return gmtDate;
/*      */   }
/*      */   
/*      */   public static void generateEvent(int productId, String account, int newRestore, String rptCode, String partition, String zoneCode, int idGroup, int idClient) throws Exception {
/* 1073 */     byte[] bufferEvent = new byte[8];
/* 1074 */     account = account.replaceAll("0", "A");
/* 1075 */     bufferEvent[0] = (byte)Integer.parseInt(account.substring(0, 2), 16);
/* 1076 */     bufferEvent[1] = (byte)Integer.parseInt(account.substring(2, 4), 16);
/* 1077 */     bufferEvent[2] = 24;
/* 1078 */     String infoEvent = rptCode.concat(partition).concat(zoneCode);
/* 1079 */     infoEvent = infoEvent.replaceAll("0", "A");
/* 1080 */     bufferEvent[3] = (byte)Integer.parseInt(String.valueOf(newRestore) + infoEvent.charAt(0), 16);
/* 1081 */     bufferEvent[4] = (byte)Integer.parseInt(infoEvent.substring(1, 3), 16);
/* 1082 */     bufferEvent[5] = (byte)Integer.parseInt(infoEvent.substring(3, 5), 16);
/* 1083 */     bufferEvent[6] = (byte)Integer.parseInt(infoEvent.substring(5, 7), 16);
/* 1084 */     bufferEvent[7] = (byte)Integer.parseInt(infoEvent.substring(7, 8) + '0', 16);
/* 1085 */     int checksum = 0;
/*      */     byte numByte;
/* 1087 */     for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/* 1088 */       byte tmp = (byte)(((numByte & 0x1) == 0) ? (bufferEvent[numByte / 2] / 16) : bufferEvent[numByte / 2]);
/* 1089 */       checksum += tmp & 0xF;
/*      */     } 
/* 1091 */     bufferEvent[7] = (byte)(bufferEvent[7] + (byte)((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/* 1092 */     printEvent(productId, bufferEvent, idGroup, idClient);
/*      */   }
/*      */   
/*      */   public static byte[] getContactIDBuffer(String account, String rptCode, String partition, String zoneCode) {
/* 1096 */     byte[] bufferEvent = new byte[8];
/* 1097 */     account = account.replaceAll("0", "A");
/* 1098 */     bufferEvent[0] = (byte)Integer.parseInt(account.substring(0, 2), 16);
/* 1099 */     bufferEvent[1] = (byte)Integer.parseInt(account.substring(2, 4), 16);
/* 1100 */     bufferEvent[2] = 24;
/* 1101 */     int newRestore = (rptCode.charAt(0) == 'E') ? 1 : 3;
/* 1102 */     rptCode = rptCode.substring(1);
/* 1103 */     String infoEvent = rptCode.concat(partition).concat(zoneCode);
/* 1104 */     infoEvent = infoEvent.replaceAll("0", "A");
/* 1105 */     bufferEvent[3] = (byte)Integer.parseInt(String.valueOf(newRestore) + infoEvent.charAt(0), 16);
/* 1106 */     bufferEvent[4] = (byte)Integer.parseInt(infoEvent.substring(1, 3), 16);
/* 1107 */     bufferEvent[5] = (byte)Integer.parseInt(infoEvent.substring(3, 5), 16);
/* 1108 */     bufferEvent[6] = (byte)Integer.parseInt(infoEvent.substring(5, 7), 16);
/* 1109 */     bufferEvent[7] = (byte)Integer.parseInt(infoEvent.substring(7, 8) + '0', 16);
/* 1110 */     int checksum = 0;
/*      */     byte numByte;
/* 1112 */     for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/* 1113 */       byte tmp = (byte)(((numByte & 0x1) == 0) ? (bufferEvent[numByte / 2] / 16) : bufferEvent[numByte / 2]);
/* 1114 */       checksum += tmp & 0xF;
/*      */     } 
/* 1116 */     bufferEvent[7] = (byte)(bufferEvent[7] + (byte)((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/* 1117 */     return bufferEvent;
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
/*      */   public static void generateEventReceptionAlivePacket(int productId, int idClient, int idModule, int idGroup, String clientCode, String eAliveReceived, int fAliveReceived, Calendar lastRcvdEvent, int lastMProtocolRcvd, String nwProtocol, int lastCommInterface, int eventDesc) throws SQLException, InterruptedException, UnsupportedEncodingException, Exception {
/* 1141 */     if (clientCode != null && clientCode.length() == 4 && 
/* 1142 */       eAliveReceived != null && eAliveReceived.length() == 8) {
/*      */       boolean generateEvent;
/* 1144 */       if (lastRcvdEvent != null && fAliveReceived >= 0) {
/* 1145 */         lastRcvdEvent = addTime2Calendar(lastRcvdEvent, 12, fAliveReceived);
/* 1146 */         generateEvent = (lastRcvdEvent.getTimeInMillis() < System.currentTimeMillis());
/*      */       } else {
/* 1148 */         generateEvent = true;
/*      */       } 
/* 1150 */       if (generateEvent) {
/* 1151 */         switch (Util.EnumProductIDs.getProductID(productId)) {
/*      */           case WINDOWS:
/* 1153 */             saveEvent(productId, idModule, idGroup, idClient, null, clientCode, eAliveReceived, Enums.EnumEventQualifier.NEW_EVENT, lastMProtocolRcvd, nwProtocol, lastCommInterface, 0, 1);
/* 1154 */             GenericDBManager.executeSP_019(idClient, "PEGASUS");
/*      */             break;
/*      */           case LINUX:
/*      */           case ARM:
/* 1158 */             saveEvent(productId, idModule, idGroup, idClient, null, clientCode, eAliveReceived, Enums.EnumEventQualifier.NEW_EVENT, lastMProtocolRcvd, nwProtocol, lastCommInterface, eventDesc, 1);
/* 1159 */             GenericDBManager.executeSP_019(idClient, "GRIFFON");
/*      */             break;
/*      */           case MAC:
/* 1162 */             saveEvent(productId, idModule, idGroup, idClient, null, clientCode, eAliveReceived, Enums.EnumEventQualifier.NEW_EVENT, lastMProtocolRcvd, nwProtocol, lastCommInterface, 0, 1);
/* 1163 */             GenericDBManager.executeSP_019(idClient, "MERCURIUS");
/*      */             break;
/*      */         } 
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
/*      */   public static String getReceiverCOMPortByGroupID(int groupId, String prodName) {
/* 1181 */     String comPort = null;
/* 1182 */     if (ZeusServerCfg.getInstance().getMonitoringInfo() == null || ZeusServerCfg.getInstance().getMonitoringInfo().isEmpty()) {
/* 1183 */       return "NONE";
/*      */     }
/* 1185 */     if (ZeusServerCfg.getInstance().getMonitoringInfo() != null)
/*      */     {
/* 1187 */       for (Map.Entry<String, MonitoringInfo> receiver : ZeusServerCfg.getInstance().getMonitoringInfo().entrySet()) {
/* 1188 */         for (Map.Entry<String, List<MonitoringGroupInfo>> mi : ((MonitoringInfo)receiver.getValue()).getAssignedGroupsByProduct().entrySet()) {
/* 1189 */           if (((String)mi.getKey()).equalsIgnoreCase(prodName)) {
/* 1190 */             for (MonitoringGroupInfo mgi : mi.getValue()) {
/* 1191 */               if (mgi.getGroupId() == groupId) {
/* 1192 */                 comPort = receiver.getKey();
/*      */                 // Byte code: goto -> 203
/*      */               } 
/*      */             } 
/*      */           }
/*      */         } 
/*      */       } 
/*      */     }
/* 1200 */     if (comPort == null) {
/* 1201 */       comPort = ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort();
/*      */     }
/* 1203 */     return comPort;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String getIface(short lastCommIface) {
/* 1214 */     switch (lastCommIface)
/*      */     { case 1:
/* 1216 */         iface = " " + LocaleMessage.getLocaleMessage("via_GPRS");
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
/* 1231 */         return iface;case 2: iface = " " + LocaleMessage.getLocaleMessage("via_CSD"); return iface;case 3: iface = " " + LocaleMessage.getLocaleMessage("via_ETH"); return iface;case 6: iface = " " + LocaleMessage.getLocaleMessage("via_WiFi"); return iface; }  String iface = ""; return iface;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String getGriffonIface(short lastCommIface) {
/* 1242 */     switch (lastCommIface)
/*      */     { case 1:
/* 1244 */         iface = " " + LocaleMessage.getLocaleMessage("via_GPRS");
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
/* 1259 */         return iface;case 2: iface = " " + LocaleMessage.getLocaleMessage("via_CSD"); return iface;case 3: iface = " " + LocaleMessage.getLocaleMessage("via_ETH"); return iface;case 4: iface = " " + LocaleMessage.getLocaleMessage("via_WiFi"); return iface; }  String iface = ""; return iface;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String getMercuriusIface(short lastCommIface) {
/* 1270 */     switch (lastCommIface)
/*      */     { case 1:
/* 1272 */         iface = " " + LocaleMessage.getLocaleMessage("via_GPRS");
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
/* 1285 */         return iface;case 2: iface = " " + LocaleMessage.getLocaleMessage("via_CSD"); return iface;case 3: iface = " " + LocaleMessage.getLocaleMessage("via_SMS"); return iface; }  String iface = ""; return iface;
/*      */   }
/*      */   
/*      */   public static String getCurrentDateFolder() {
/* 1289 */     return Defines.DTFORMAT.format(new Date());
/*      */   }
/*      */   
/*      */   public static Thread dbBackup(final File backupDirFile, final SmbFile nwRootFolder) throws Exception {
/*      */     Thread thread;
/* 1294 */     if (ZeusServerCfg.getInstance().isNetworkAuth() && nwRootFolder != null) {
/* 1295 */       final File tmpFile = new File("dbbckuptmp");
/* 1296 */       tmpFile.mkdirs();
/* 1297 */       Connection con = null;
/*      */       try {
/* 1299 */         Class.forName("org.apache.derby.jdbc.ClientDriver");
/* 1300 */         con = DriverManager.getConnection("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/" + ZeusServerCfg.getInstance().getDbFile() + ";upgrade=true", "SYS", "SYS");
/* 1301 */         CallableStatement cst = con.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
/* 1302 */         cst.setString(1, tmpFile.getAbsolutePath() + "/" + backupDirFile.getName());
/* 1303 */         cst.execute();
/* 1304 */         cst.close();
/* 1305 */       } catch (ClassNotFoundException|SQLException ex) {
/* 1306 */         printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_executing_the_backup_of_the_original_database"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1307 */         throw ex;
/*      */       } finally {
/*      */         try {
/* 1310 */           if (con != null) {
/* 1311 */             con.close();
/*      */           }
/* 1313 */         } catch (SQLException sQLException) {}
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 1318 */       Thread zipThread = new Thread(new Runnable()
/*      */           {
/*      */             public void run() {
/*      */               try {
/* 1322 */                 File backupFile = new File(tmpFile.getAbsolutePath() + "/" + backupDirFile.getName());
/* 1323 */                 List<File> fileList = new ArrayList<>();
/* 1324 */                 Functions.getAllFiles(backupFile, fileList);
/* 1325 */                 Functions.writeZipFile(backupFile, fileList);
/* 1326 */                 SmbFile newFile = new SmbFile(nwRootFolder, backupDirFile.getName() + ".zip");
/* 1327 */                 SmbFileOutputStream smbFOS = new SmbFileOutputStream(newFile);
/* 1328 */                 FileInputStream fis = new FileInputStream(tmpFile.getAbsolutePath() + "/" + backupDirFile.getName() + ".zip");
/* 1329 */                 byte[] data = new byte[4096];
/*      */                 try {
/*      */                   int bytesRead;
/* 1332 */                   while ((bytesRead = fis.read(data)) != -1) {
/* 1333 */                     smbFOS.write(data, 0, bytesRead);
/*      */                   }
/*      */                 } finally {
/* 1336 */                   smbFOS.close();
/* 1337 */                   fis.close();
/*      */                 } 
/* 1339 */                 Functions.deleteDirectory(tmpFile);
/*      */                 
/* 1341 */                 NetworkZipFilter nwZipFilter = new NetworkZipFilter();
/* 1342 */                 SmbFile[] backupFiles = nwRootFolder.listFiles(nwZipFilter);
/* 1343 */                 while (ZeusServerCfg.getInstance().getDBBackupMaxSize() < backupFiles.length) {
/* 1344 */                   Arrays.sort(backupFiles, new Comparator<SmbFile>() {
/*      */                         public int compare(SmbFile o1, SmbFile o2) {
/*      */                           try {
/* 1347 */                             return Long.compare(o1.lastModified(), o2.lastModified());
/* 1348 */                           } catch (SmbException ex) {
/* 1349 */                             Logger.getLogger(Functions.class
/* 1350 */                                 .getName()).log(Level.SEVERE, (String)null, (Throwable)ex);
/*      */                             
/* 1352 */                             return -1;
/*      */                           }  }
/*      */                       });
/* 1355 */                   backupFiles[0].delete();
/* 1356 */                   backupFiles = nwRootFolder.listFiles(nwZipFilter);
/*      */                 } 
/* 1358 */               } catch (IOException ex) {
/* 1359 */                 ex.printStackTrace();
/*      */               } 
/*      */             }
/*      */           });
/* 1363 */       zipThread.setName("zipThread");
/* 1364 */       zipThread.setDaemon(true);
/* 1365 */       zipThread.start();
/* 1366 */       thread = zipThread;
/*      */     } else {
/* 1368 */       if (!backupDirFile.exists() && !backupDirFile.isDirectory()) {
/* 1369 */         backupDirFile.mkdirs();
/*      */       }
/* 1371 */       Connection con = null;
/*      */       try {
/* 1373 */         Class.forName("org.apache.derby.jdbc.ClientDriver");
/* 1374 */         con = DriverManager.getConnection("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/" + ZeusServerCfg.getInstance().getDbFile() + ";upgrade=true", "SYS", "SYS");
/* 1375 */         CallableStatement cst = con.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
/* 1376 */         cst.setString(1, backupDirFile.getAbsolutePath());
/* 1377 */         cst.execute();
/* 1378 */         cst.close();
/* 1379 */       } catch (ClassNotFoundException|SQLException ex) {
/* 1380 */         printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_executing_the_backup_of_the_original_database"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1381 */         throw ex;
/*      */       } finally {
/*      */         try {
/* 1384 */           if (con != null) {
/* 1385 */             con.close();
/*      */           }
/* 1387 */         } catch (SQLException sQLException) {}
/*      */       } 
/*      */ 
/*      */       
/* 1391 */       Thread zipThread = new Thread(new Runnable()
/*      */           {
/*      */             public void run() {
/*      */               try {
/* 1395 */                 List<File> fileList = new ArrayList<>();
/* 1396 */                 Functions.getAllFiles(backupDirFile, fileList);
/* 1397 */                 Functions.writeZipFile(backupDirFile, fileList);
/* 1398 */                 Functions.deleteDirectory(backupDirFile);
/*      */                 
/* 1400 */                 ZipFilter zipFilter = new ZipFilter();
/* 1401 */                 File[] backupFiles = backupDirFile.getParentFile().listFiles(zipFilter);
/* 1402 */                 while (ZeusServerCfg.getInstance().getDBBackupMaxSize() < backupFiles.length) {
/* 1403 */                   Arrays.sort(backupFiles, new Comparator<File>() {
/*      */                         public int compare(File o1, File o2) {
/* 1405 */                           return Long.compare(o1.lastModified(), o2.lastModified());
/*      */                         }
/*      */                       });
/* 1408 */                   backupFiles[0].delete();
/* 1409 */                   backupFiles = backupDirFile.getParentFile().listFiles(zipFilter);
/*      */                 } 
/* 1411 */               } catch (IOException ex) {
/* 1412 */                 ex.printStackTrace();
/*      */               } 
/*      */             }
/*      */           });
/* 1416 */       zipThread.setDaemon(true);
/* 1417 */       zipThread.start();
/* 1418 */       thread = zipThread;
/*      */     } 
/* 1420 */     return thread;
/*      */   }
/*      */   
/*      */   public static void logPegasusIncomingPacket(Logger ownLogger, byte[] buffer) {
/* 1424 */     int index = 0;
/* 1425 */     byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1433 */     byte[] tmp4 = new byte[4];
/* 1434 */     byte[] tmp2 = new byte[4];
/*      */     
/* 1436 */     ownLogger.info("***** PACKET RECEIVED *****");
/* 1437 */     StringBuilder sb = new StringBuilder(buffer.length);
/* 1438 */     for (byte b : buffer) {
/* 1439 */       sb.append(b & 0xFF).append(' ');
/*      */     }
/* 1441 */     ownLogger.info(sb.toString());
/*      */     
/* 1443 */     while (index < buffer.length && 
/* 1444 */       index + 2 <= buffer.length) {
/*      */       short tmpShort; long dValue; int tmp, i; byte[] oper; int k, j; char[] moduleHW; short apn; int m, gprsCount;
/*      */       char[] pgm;
/* 1447 */       System.arraycopy(buffer, index, fid, 0, 2);
/* 1448 */       index += 2;
/* 1449 */       fid = swapLSB2MSB(fid);
/* 1450 */       int fidVal = getIntFrom2ByteArray(fid);
/* 1451 */       if (fidVal <= 0) {
/*      */         break;
/*      */       }
/* 1454 */       short flen = (short)getIntFromHexByte(buffer[index]);
/* 1455 */       byte[] fcon = new byte[flen];
/* 1456 */       System.arraycopy(buffer, ++index, fcon, 0, flen);
/* 1457 */       index += flen;
/* 1458 */       switch (fidVal) {
/*      */         case 1:
/* 1460 */           sb = new StringBuilder();
/* 1461 */           for (i = 0; i < flen; i++) {
/* 1462 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/* 1464 */           ownLogger.info("MODULE ID :" + sb.toString());
/*      */         
/*      */         case 2:
/* 1467 */           sb = new StringBuilder();
/* 1468 */           for (i = 0; i < flen - 1; i++) {
/* 1469 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/* 1471 */           sb.append(fcon[7] / 10);
/* 1472 */           ownLogger.info("MODEM IMEI :" + sb.toString());
/*      */         
/*      */         case 45:
/* 1475 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1476 */           sb = new StringBuilder();
/*      */           
/* 1478 */           for (i = 1; i < flen - 1; i++) {
/* 1479 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/* 1481 */           sb.append(fcon[8] / 10);
/*      */           
/* 1483 */           if (tmpShort == 1) {
/* 1484 */             ownLogger.info("Sim #1 IMSI :" + sb.toString()); continue;
/* 1485 */           }  if (tmpShort == 2) {
/* 1486 */             ownLogger.info("Sim #2 IMSI :" + sb.toString());
/*      */           }
/*      */         
/*      */         case 3:
/* 1490 */           if (fcon[0] == 1) {
/* 1491 */             ownLogger.info("MODEM_MODEL : TELIT_GE865");
/*      */           }
/*      */         
/*      */         case 4:
/* 1495 */           sb = new StringBuilder();
/* 1496 */           sb.append(getIntFromHexByte(fcon[0])).append(".").append(getIntFromHexByte(fcon[1])).append(".").append(getIntFromHexByte(fcon[2])).append(".").append(getIntFromHexByte(fcon[3]));
/* 1497 */           ownLogger.info("MODEM FIRMWARE VERSION :" + sb.toString());
/*      */         
/*      */         case 5:
/* 1500 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1501 */           sb = new StringBuilder();
/*      */           
/* 1503 */           for (i = 1; i < flen; i++) {
/* 1504 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/*      */           
/* 1507 */           if (tmpShort == 1) {
/* 1508 */             ownLogger.info("Sim #1 ICCID :" + sb.toString()); continue;
/* 1509 */           }  if (tmpShort == 2) {
/* 1510 */             ownLogger.info("Sim #2 ICCID :" + sb.toString());
/*      */           }
/*      */         
/*      */         case 6:
/* 1514 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1515 */           oper = new byte[flen - 1];
/* 1516 */           System.arraycopy(fcon, 1, oper, 0, flen - 1);
/*      */           
/* 1518 */           if (tmpShort == 1) {
/* 1519 */             ownLogger.info("Sim #1 Operator :" + getASCIIFromByteArray(oper)); continue;
/* 1520 */           }  if (tmpShort == 2) {
/* 1521 */             ownLogger.info("Sim #2 Operator :" + getASCIIFromByteArray(oper));
/*      */           }
/*      */         
/*      */         case 7:
/* 1525 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/*      */           
/* 1527 */           if (tmpShort == 1) {
/* 1528 */             ownLogger.info("HARDWARE_FAILURE # SIM 1"); continue;
/* 1529 */           }  if (tmpShort == 2) {
/* 1530 */             ownLogger.info("HARDWARE_FAILURE # SIM 2"); continue;
/* 1531 */           }  if (tmpShort == 3) {
/* 1532 */             ownLogger.info("HARDWARE_FAILURE # GSM MODEM"); continue;
/* 1533 */           }  if (tmpShort == 4) {
/* 1534 */             ownLogger.info("HARDWARE_FAILURE # Ethernet"); continue;
/* 1535 */           }  if (tmpShort == 5) {
/* 1536 */             ownLogger.info("HARDWARE_FAILURE # Wi-Fi");
/*      */           }
/*      */         
/*      */         case 8:
/* 1540 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1541 */           ownLogger.info("CURRENT SIMCARD : " + tmpShort);
/* 1542 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 1543 */           ownLogger.info("CURRENT APN : " + tmpShort);
/*      */         
/*      */         case 9:
/* 1546 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1547 */           if (tmpShort == 1) {
/* 1548 */             ownLogger.info("CURRENT_INTERFACE # GPRS"); continue;
/* 1549 */           }  if (tmpShort == 2) {
/* 1550 */             ownLogger.info("CURRENT_INTERFACE # CSD/SMS"); continue;
/* 1551 */           }  if (tmpShort == 3) {
/* 1552 */             ownLogger.info("CURRENT_INTERFACE # ETHERNET"); continue;
/* 1553 */           }  if (tmpShort == 6) {
/* 1554 */             ownLogger.info("CURRENT_INTERFACE # Wi-Fi");
/*      */           }
/*      */         
/*      */         case 10:
/* 1558 */           k = 3;
/* 1559 */           for (j = 0; j < 4; j++) {
/* 1560 */             tmp4[k--] = fcon[j];
/*      */           }
/* 1562 */           ownLogger.info("CRC32 of CFG File : " + getIntFrom4ByteArray(tmp4));
/*      */         
/*      */         case 11:
/* 1565 */           moduleHW = getBinaryFromByte(fcon[0]);
/* 1566 */           if (moduleHW[7] == '0') {
/* 1567 */             ownLogger.info("MODULE_HW_DETAILS # NO Internal Battery ");
/* 1568 */           } else if (moduleHW[7] == '1') {
/* 1569 */             ownLogger.info("MODULE_HW_DETAILS # Internal Battery Available ");
/*      */           } 
/* 1571 */           if (moduleHW[6] == '0') {
/* 1572 */             ownLogger.info("MODULE_HW_DETAILS # NO GPRS Interface ");
/* 1573 */           } else if (moduleHW[6] == '1') {
/* 1574 */             ownLogger.info("MODULE_HW_DETAILS # GPRS Interface Available ");
/*      */           } 
/* 1576 */           if (moduleHW[5] == '0') {
/* 1577 */             ownLogger.info("MODULE_HW_DETAILS # NO Ethernet Interface ");
/* 1578 */           } else if (moduleHW[5] == '1') {
/* 1579 */             ownLogger.info("MODULE_HW_DETAILS # Ethernet Interface Available ");
/*      */           } 
/* 1581 */           if (moduleHW[4] == '0') {
/* 1582 */             ownLogger.info("MODULE_HW_DETAILS # NO Digital Inputs ");
/* 1583 */           } else if (moduleHW[4] == '1') {
/* 1584 */             ownLogger.info("MODULE_HW_DETAILS # Digital Inputs Available ");
/*      */           } 
/* 1586 */           if (moduleHW[3] == '0') {
/* 1587 */             ownLogger.info("MODULE_HW_DETAILS # NO Digital Outputs ");
/* 1588 */           } else if (moduleHW[3] == '1') {
/* 1589 */             ownLogger.info("MODULE_HW_DETAILS # Digital Outputs Available ");
/*      */           } 
/* 1591 */           if (moduleHW[2] == '0') {
/* 1592 */             ownLogger.info("MODULE_HW_DETAILS # NO Wi-Fi Interface "); continue;
/* 1593 */           }  if (moduleHW[2] == '1') {
/* 1594 */             ownLogger.info("MODULE_HW_DETAILS # Wi-Fi Interface Available ");
/*      */           }
/*      */         
/*      */         case 12:
/* 1598 */           sb = new StringBuilder();
/* 1599 */           sb.append(getIntFromHexByte(fcon[0])).append('.').append(getIntFromHexByte(fcon[1])).append('.').append(getIntFromHexByte(fcon[2]));
/* 1600 */           ownLogger.info("MODULE FIRMWARE VERSION : " + sb.toString());
/*      */         
/*      */         case 13:
/* 1603 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1604 */           if (tmpShort == 0) {
/* 1605 */             ownLogger.info("MODULE_OPERATION_MODE # First communication option for the alarm panel"); continue;
/* 1606 */           }  if (tmpShort == 1) {
/* 1607 */             ownLogger.info("MODULE_OPERATION_MODE # Second communication option (backup) for the alarm panel");
/*      */           }
/*      */         
/*      */         case 14:
/* 1611 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1612 */           if (tmpShort == 0) {
/* 1613 */             ownLogger.info("PHONE_LINE_STATUS # Not Detected"); continue;
/* 1614 */           }  if (tmpShort == 1) {
/* 1615 */             ownLogger.info("PHONE_LINE_STATUS # Detected");
/*      */           }
/*      */         
/*      */         case 15:
/* 1619 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1620 */           if (tmpShort == 0) {
/* 1621 */             ownLogger.info("ALARM_PANEL_RETURN_STATUS # Not Detected"); continue;
/* 1622 */           }  if (tmpShort == 1) {
/* 1623 */             ownLogger.info("ALARM_PANEL_RETURN_STATUS # Detected");
/*      */           }
/*      */         
/*      */         case 16:
/* 1627 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1628 */           if (tmpShort == 0) {
/* 1629 */             ownLogger.info("DUAL_MONITORING_STATUS #  Normal Operation"); continue;
/* 1630 */           }  if (tmpShort == 1) {
/* 1631 */             ownLogger.info("DUAL_MONITORING_STATUS # Failure");
/*      */           }
/*      */         
/*      */         case 17:
/* 1635 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1636 */           ownLogger.info("DIGITAL_INPUT_STATUS # ZONE 1 Status : " + tmpShort);
/* 1637 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 1638 */           ownLogger.info("DIGITAL_INPUT_STATUS # ZONE 2 Status : " + tmpShort);
/* 1639 */           tmpShort = (short)getIntFromHexByte(fcon[2]);
/* 1640 */           ownLogger.info("DIGITAL_INPUT_STATUS # ZONE 3 Status : " + tmpShort);
/* 1641 */           tmpShort = (short)getIntFromHexByte(fcon[3]);
/* 1642 */           ownLogger.info("DIGITAL_INPUT_STATUS # ZONE 4 Status : " + tmpShort);
/*      */         
/*      */         case 18:
/* 1645 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1646 */           if (tmpShort == 0) {
/* 1647 */             ownLogger.info("MAIN_POWER_SUPPLY_STATUS # Not Detected"); continue;
/* 1648 */           }  if (tmpShort == 1) {
/* 1649 */             ownLogger.info("MAIN_POWER_SUPPLY_STATUS # Detected");
/*      */           }
/*      */         
/*      */         case 19:
/* 1653 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1654 */           if (tmpShort == 0) {
/* 1655 */             ownLogger.info("ALARM_PANEL_COMM_STATUS # Normal Operation"); continue;
/* 1656 */           }  if (tmpShort == 1) {
/* 1657 */             ownLogger.info("ALARM_PANEL_COMM_STATUS # Failure");
/*      */           }
/*      */         
/*      */         case 20:
/* 1661 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1662 */           ownLogger.info("GSM Jammer Status :" + tmpShort);
/* 1663 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 1664 */           ownLogger.info("GSM Jammer JDR Status :" + tmpShort);
/*      */         
/*      */         case 21:
/* 1667 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1668 */           ownLogger.info("GSM Signal Level :" + tmpShort);
/*      */         
/*      */         case 22:
/* 1671 */           sb = new StringBuilder();
/* 1672 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1673 */           sb.append(tmpShort).append('.');
/* 1674 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 1675 */           sb.append(tmpShort);
/* 1676 */           ownLogger.info("Battery Voltage Level : " + Float.parseFloat(sb.toString()));
/*      */         
/*      */         case 23:
/* 1679 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1680 */           ownLogger.info("Battery Charge Percentage : " + tmpShort);
/*      */         
/*      */         case 24:
/* 1683 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1684 */           ownLogger.info("Battery Low Occurrence : " + tmpShort);
/*      */         
/*      */         case 25:
/* 1687 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1688 */           if (tmpShort == 0) {
/* 1689 */             ownLogger.info("ALARM_PANEL_CONNECTION_OPERATION_MODE # Automatic "); continue;
/* 1690 */           }  if (tmpShort == 1) {
/* 1691 */             ownLogger.info("ALARM_PANEL_CONNECTION_OPERATION_MODE # Manual (remotely controlled) ");
/*      */           }
/*      */         
/*      */         case 26:
/* 1695 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1696 */           if (tmpShort == 0) {
/* 1697 */             ownLogger.info("ALARM_PANEL_CONNECTION_STATUS # Connected to the phone line "); continue;
/* 1698 */           }  if (tmpShort == 1) {
/* 1699 */             ownLogger.info("ALARM_PANEL_CONNECTION_STATUS # Connected to the virtual line ");
/*      */           }
/*      */         
/*      */         case 27:
/* 1703 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1704 */           if (tmpShort == 0) {
/* 1705 */             ownLogger.info("ALARM_PANEL_COMM_TEST_STATUS # Failure "); continue;
/* 1706 */           }  if (tmpShort == 1) {
/* 1707 */             ownLogger.info("ALARM_PANEL_COMM_TEST_STATUS # Success ");
/*      */           }
/*      */         
/*      */         case 28:
/* 1711 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1712 */           if (tmpShort == 0) {
/* 1713 */             ownLogger.info("TELEPHONE_LINE_COMM_TEST_STATUS # Failure "); continue;
/* 1714 */           }  if (tmpShort == 1) {
/* 1715 */             ownLogger.info("TELEPHONE_LINE_COMM_TEST_STATUS # Success ");
/*      */           }
/*      */         
/*      */         case 29:
/* 1719 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1720 */           if (tmpShort == 0) {
/* 1721 */             ownLogger.info("ETHERNET_INTERFACE_TEST_STATUS # Failure "); continue;
/* 1722 */           }  if (tmpShort == 1) {
/* 1723 */             ownLogger.info("ETHERNET_INTERFACE_TEST_STATUS # Success ");
/*      */           }
/*      */         
/*      */         case 30:
/* 1727 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1728 */           apn = (short)getIntFromHexByte(fcon[1]);
/*      */           
/* 1730 */           if (tmpShort == 1 && apn == 1) {
/* 1731 */             ownLogger.info(" MODEM INTERFACE TEST STATUS SIM #1 & APN #1 : " + getIntFromHexByte(fcon[2])); continue;
/* 1732 */           }  if (tmpShort == 1 && apn == 2) {
/* 1733 */             ownLogger.info(" MODEM INTERFACE TEST STATUS SIM #1 & APN #2  " + getIntFromHexByte(fcon[2])); continue;
/* 1734 */           }  if (tmpShort == 2 && apn == 1) {
/* 1735 */             ownLogger.info(" MODEM INTERFACE TEST STATUS SIM #2 & APN #1 : " + getIntFromHexByte(fcon[2])); continue;
/* 1736 */           }  if (tmpShort == 2 && apn == 2) {
/* 1737 */             ownLogger.info(" MODEM INTERFACE TEST STATUS SIM #2 & APN #2 : " + getIntFromHexByte(fcon[2]));
/*      */           }
/*      */ 
/*      */         
/*      */         case 31:
/* 1742 */           sb = new StringBuilder();
/* 1743 */           for (m = 0; m <= 7; m++) {
/* 1744 */             sb.append(convertContactIdDigitToHex((fcon[m] & 0xF0) / 16)).append(convertContactIdDigitToHex(fcon[m] & 0xF));
/*      */           }
/* 1746 */           ownLogger.info("Contact-ID Event : " + sb.toString());
/*      */         
/*      */         case 32:
/* 1749 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1750 */           if (tmpShort == 0) {
/* 1751 */             ownLogger.info("TAMPER_DETECTION # Tamper Not Detected "); continue;
/* 1752 */           }  if (tmpShort == 1) {
/* 1753 */             ownLogger.info("TAMPER_DETECTION # Tamper Detected ");
/*      */           }
/*      */         
/*      */         case 33:
/* 1757 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1758 */           if (tmpShort == 1) {
/* 1759 */             ownLogger.info("WIFI_MODEL # CC3000 ....");
/*      */           }
/*      */         
/*      */         case 34:
/* 1763 */           ownLogger.info("Wi-Fi Firmware Version : " + fcon[0] + "." + fcon[1]);
/*      */         
/*      */         case 35:
/* 1766 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1767 */           if (tmpShort == 0) {
/* 1768 */             ownLogger.info("WIFI_ACCESS_POINT # Primary Access Point "); continue;
/* 1769 */           }  if (tmpShort == 1) {
/* 1770 */             ownLogger.info("WIFI_ACCESS_POINT # Secondary Access Point ");
/*      */           }
/*      */         
/*      */         case 36:
/* 1774 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1775 */           if (tmpShort == 0) {
/* 1776 */             ownLogger.info("Wi-Fi Access Point #1 Test Status : " + (short)(fcon[1] & 0xFF)); continue;
/* 1777 */           }  if (tmpShort == 1) {
/* 1778 */             ownLogger.info("Wi-Fi Access Point #2 Test Status : " + (short)(fcon[1] & 0xFF));
/*      */           }
/*      */         
/*      */         case 37:
/* 1782 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1783 */           ownLogger.info("Wi-Fi Signal Level : " + tmpShort);
/*      */         
/*      */         case 38:
/* 1786 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1787 */           if (tmpShort == 0) {
/* 1788 */             ownLogger.info("SIM_CARD_0_STATUS # Sim Card Failure ");
/* 1789 */           } else if (tmpShort == 1) {
/* 1790 */             ownLogger.info("SIM_CARD_0_STATUS # Sim Card Present ");
/*      */           } 
/* 1792 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 1793 */           if (tmpShort == 0) {
/* 1794 */             ownLogger.info("SIM_CARD_0_STATUS # Sim Card Reg. Failure ");
/* 1795 */           } else if (tmpShort == 1) {
/* 1796 */             ownLogger.info("SIM_CARD_0_STATUS # Sim Card Reg. Success ");
/* 1797 */           } else if (tmpShort == 2) {
/* 1798 */             ownLogger.info("SIM_CARD_0_STATUS # Registration Status Not Tested ");
/*      */           } 
/*      */           
/* 1801 */           tmpShort = (short)getIntFromHexByte(fcon[2]);
/*      */           
/* 1803 */           if (tmpShort == 0) {
/* 1804 */             ownLogger.info("SIM_CARD_0_STATUS # Operatiive "); continue;
/* 1805 */           }  if (tmpShort == 1) {
/* 1806 */             ownLogger.info("SIM_CARD_0_STATUS # Jammed "); continue;
/* 1807 */           }  if (tmpShort == 2) {
/* 1808 */             ownLogger.info("SIM_CARD_0_STATUS # Operative Status Not Tested ");
/*      */           }
/*      */         
/*      */         case 39:
/* 1812 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1813 */           if (tmpShort == 0) {
/* 1814 */             ownLogger.info("SIM_CARD_1_STATUS # Sim Card Failure ");
/* 1815 */           } else if (tmpShort == 1) {
/* 1816 */             ownLogger.info("SIM_CARD_1_STATUS # Sim Card Present ");
/*      */           } 
/* 1818 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 1819 */           if (tmpShort == 0) {
/* 1820 */             ownLogger.info("SIM_CARD_1_STATUS # Sim Card Reg. Failure ");
/* 1821 */           } else if (tmpShort == 1) {
/* 1822 */             ownLogger.info("SIM_CARD_1_STATUS # Sim Card Reg. Success ");
/* 1823 */           } else if (tmpShort == 2) {
/* 1824 */             ownLogger.info("SIM_CARD_1_STATUS # Registration Status Not Tested ");
/*      */           } 
/* 1826 */           tmpShort = (short)getIntFromHexByte(fcon[2]);
/* 1827 */           if (tmpShort == 0) {
/* 1828 */             ownLogger.info("SIM_CARD_1_STATUS # Operatiive "); continue;
/* 1829 */           }  if (tmpShort == 1) {
/* 1830 */             ownLogger.info("SIM_CARD_1_STATUS # Jammed "); continue;
/* 1831 */           }  if (tmpShort == 2) {
/* 1832 */             ownLogger.info("SIM_CARD_1_STATUS # Operative Status Not Tested ");
/*      */           }
/*      */         
/*      */         case 40:
/* 1836 */           gprsCount = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(fcon));
/* 1837 */           ownLogger.info("KBPS_DATA_TRANSFER_VIA_GPRS : " + gprsCount);
/*      */         
/*      */         case 41:
/* 1840 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1841 */           ownLogger.info("OTA STATUS : " + tmpShort);
/*      */         
/*      */         case 42:
/* 1844 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1845 */           ownLogger.info("Battery Over Temprature : " + tmpShort);
/*      */         
/*      */         case 43:
/* 1848 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1849 */           ownLogger.info("Battery Over Time Charge : " + tmpShort);
/*      */         case 64:
/* 1851 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1852 */           ownLogger.info("Battery DisConnected : " + tmpShort);
/*      */         
/*      */         case 46:
/* 1855 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 1856 */           pgm = getBinaryFromByte(tmpShort);
/* 1857 */           ownLogger.info("RELAY STATUS : " + Character.digit(pgm[7], 10));
/* 1858 */           ownLogger.info("PGM #1 STATUS : " + Character.digit(pgm[6], 10));
/* 1859 */           ownLogger.info("PGM #2 STATUS : " + Character.digit(pgm[5], 10));
/* 1860 */           ownLogger.info("PGM #3 STATUS : " + Character.digit(pgm[4], 10));
/*      */         
/*      */         case 48:
/* 1863 */           tmp4[0] = fcon[0];
/* 1864 */           tmp4[1] = fcon[1];
/* 1865 */           tmp4[2] = fcon[2];
/* 1866 */           tmp4[3] = fcon[3];
/* 1867 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 1868 */           ownLogger.info("GPS_DATA : Received Latitude : " + dValue + " Actual Value : " + (dValue / Math.pow(10.0D, 7.0D)));
/*      */         
/*      */         case 47:
/* 1871 */           tmp4[0] = fcon[0];
/* 1872 */           tmp4[1] = fcon[1];
/* 1873 */           tmp4[2] = fcon[2];
/* 1874 */           tmp4[3] = fcon[3];
/* 1875 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 1876 */           ownLogger.info("GPS_DATA : Received Longitude : " + dValue + " Actual Value : " + (dValue / Math.pow(10.0D, 7.0D)));
/*      */         
/*      */         case 49:
/* 1879 */           tmp4[0] = fcon[0];
/* 1880 */           tmp4[1] = fcon[1];
/* 1881 */           tmp4[2] = fcon[2];
/* 1882 */           tmp4[3] = fcon[3];
/* 1883 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 1884 */           ownLogger.info("GPS_DATA : Received Altitude : " + dValue + " Actual Value : " + (float)(dValue / 100.0D));
/*      */         
/*      */         case 50:
/* 1887 */           tmp4[0] = fcon[0];
/* 1888 */           tmp4[1] = fcon[1];
/* 1889 */           tmp4[2] = fcon[2];
/* 1890 */           tmp4[3] = fcon[3];
/* 1891 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 1892 */           ownLogger.info("V2_3I_LOCATE : Received Latitude : " + dValue + " Actual Value : " + (dValue / Math.pow(10.0D, 7.0D)));
/*      */           
/* 1894 */           tmp4[0] = fcon[4];
/* 1895 */           tmp4[1] = fcon[5];
/* 1896 */           tmp4[2] = fcon[6];
/* 1897 */           tmp4[3] = fcon[7];
/* 1898 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 1899 */           ownLogger.info("V2_3I_LOCATE : Received Longitude : " + dValue + " Actual Value : " + (dValue / Math.pow(10.0D, 7.0D)));
/*      */           
/* 1901 */           tmp4[0] = fcon[8];
/* 1902 */           tmp4[1] = fcon[9];
/* 1903 */           tmp4[2] = fcon[10];
/* 1904 */           tmp4[3] = fcon[11];
/* 1905 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 1906 */           ownLogger.info("V2_3I_LOCATE : Received Altitude : " + dValue + " Actual Value : " + (float)(dValue / 100.0D));
/*      */         
/*      */         case 51:
/* 1909 */           tmp2[0] = fcon[1];
/* 1910 */           tmp2[1] = fcon[0];
/* 1911 */           ownLogger.info("V2_TIMEZONE : " + getSignedIntFrom2ByteArray(tmp2));
/*      */         
/*      */         case 52:
/* 1914 */           printZoneStatus(1, fcon[0], ownLogger);
/*      */         
/*      */         case 53:
/* 1917 */           printZoneStatus(2, fcon[0], ownLogger);
/*      */         
/*      */         case 54:
/* 1920 */           printZoneStatus(3, fcon[0], ownLogger);
/*      */         
/*      */         case 55:
/* 1923 */           printZoneStatus(4, fcon[0], ownLogger);
/*      */         
/*      */         case 56:
/* 1926 */           ownLogger.info("SYSTEM STATUS : " + fcon[0]);
/*      */         
/*      */         case 57:
/* 1929 */           tmp2[0] = fcon[1];
/* 1930 */           tmp2[1] = fcon[0];
/* 1931 */           tmp = getIntFrom2ByteArray(tmp2);
/* 1932 */           ownLogger.info("V2_KEYFOB_PANIC : " + tmp);
/*      */         
/*      */         case 58:
/* 1935 */           tmp2[0] = fcon[1];
/* 1936 */           tmp2[1] = fcon[0];
/* 1937 */           tmp = getIntFrom2ByteArray(tmp2);
/* 1938 */           ownLogger.info("V2_KEYFOB_COMM_TEST : " + tmp);
/*      */         
/*      */         case 59:
/* 1941 */           tmp2[0] = fcon[1];
/* 1942 */           tmp2[1] = fcon[0];
/* 1943 */           tmp = getIntFrom2ByteArray(tmp2);
/* 1944 */           ownLogger.info("V2_KEYFOB_LOW_BATTERY : " + getBinaryFromInt(tmp));
/*      */         
/*      */         case 60:
/* 1947 */           ownLogger.info("SYSTEM IN ALARM : " + fcon[0]);
/*      */       } 
/*      */ 
/*      */     
/*      */     } 
/* 1952 */     ownLogger.info("****************************************************************************");
/*      */   }
/*      */   
/*      */   public static void printZoneStatus(int zone, int status, Logger ownLogger) {
/* 1956 */     switch (status) {
/*      */       case 0:
/* 1958 */         ownLogger.info("Zone #" + zone + " DISABLED");
/*      */         break;
/*      */       case 1:
/* 1961 */         ownLogger.info("Zone #" + zone + " NORMAL");
/*      */         break;
/*      */       case 2:
/* 1964 */         ownLogger.info("Zone #" + zone + " ACTIVE");
/*      */         break;
/*      */       case 3:
/* 1967 */         ownLogger.info("Zone #" + zone + " WIREFAULT");
/*      */         break;
/*      */       case 4:
/* 1970 */         ownLogger.info("Zone #" + zone + " TAMPER");
/*      */         break;
/*      */       case 5:
/* 1973 */         ownLogger.info("Zone #" + zone + " ALARM");
/*      */         break;
/*      */       case 6:
/* 1976 */         ownLogger.info("Zone #" + zone + " BYPASS");
/*      */         break;
/*      */       case 7:
/* 1979 */         ownLogger.info("Zone #" + zone + " UNBYPASS");
/*      */         break;
/*      */       case 8:
/* 1982 */         ownLogger.info("Zone #" + zone + " FORCE ARMED");
/*      */         break;
/*      */       case 9:
/* 1985 */         ownLogger.info("Zone #" + zone + " FORCE RESTORE");
/*      */         break;
/*      */     } 
/*      */   }
/*      */   
/*      */   public static void logGriffonIncomingPacket(Logger ownLogger, byte[] buffer) throws ParseException {
/* 1991 */     int index = 0;
/* 1992 */     byte[] fid = new byte[2];
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
/* 2007 */     byte[] tmp2 = new byte[2];
/* 2008 */     byte[] tmp4 = new byte[4];
/* 2009 */     int idx = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2014 */     ownLogger.info("***** PACKET RECEIVED *****");
/* 2015 */     StringBuilder sb = new StringBuilder(buffer.length);
/* 2016 */     for (byte b : buffer) {
/* 2017 */       sb.append(b & 0xFF).append(' ');
/*      */     }
/* 2019 */     ownLogger.info(sb.toString());
/*      */     
/* 2021 */     while (index < buffer.length && 
/* 2022 */       index + 2 <= buffer.length) {
/*      */       short tmpShort; long dValue; String account; long timestamp; int evntQulifier; String rptCode, partition, zoneCode; int eventIndex, zoneStatus, tmp, sysVal1, sysVal2, i; byte[] oper; char[] st; int j; short apn;
/*      */       int k, eblen, ix;
/* 2025 */       System.arraycopy(buffer, index, fid, 0, 2);
/* 2026 */       index += 2;
/* 2027 */       fid = swapLSB2MSB(fid);
/* 2028 */       int fidVal = getIntFrom2ByteArray(fid);
/* 2029 */       if (fidVal <= 0) {
/*      */         break;
/*      */       }
/* 2032 */       short flen = (short)getIntFromHexByte(buffer[index]);
/* 2033 */       byte[] fcon = new byte[flen];
/* 2034 */       System.arraycopy(buffer, ++index, fcon, 0, flen);
/* 2035 */       ownLogger.info("***** SUB PACKET PACKET  *****");
/* 2036 */       sb = new StringBuilder(fcon.length);
/* 2037 */       for (byte b : fcon) {
/* 2038 */         sb.append(b & 0xFF).append(' ');
/*      */       }
/* 2040 */       ownLogger.info(sb.toString());
/*      */       
/* 2042 */       index += flen;
/* 2043 */       switch (fidVal) {
/*      */         case 1:
/* 2045 */           idx = 0;
/* 2046 */           ownLogger.info(" M2S Packet Type : ZONE REPORT ");
/* 2047 */           tmp2[1] = fcon[idx++];
/* 2048 */           tmp2[0] = fcon[idx++];
/* 2049 */           eventIndex = getIntFrom2ByteArray(tmp2);
/* 2050 */           ownLogger.info(" Zone Event Index : " + eventIndex);
/* 2051 */           tmp2[0] = fcon[idx++];
/* 2052 */           tmp2[1] = fcon[idx++];
/* 2053 */           account = String.format("%4s", new Object[] { getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2054 */           ownLogger.info(" Account : " + account);
/*      */           
/* 2056 */           tmp4[0] = fcon[idx++];
/* 2057 */           tmp4[1] = fcon[idx++];
/* 2058 */           tmp4[2] = fcon[idx++];
/* 2059 */           tmp4[3] = fcon[idx++];
/* 2060 */           timestamp = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 2061 */           ownLogger.info(" Event Timestamp : " + getDateFromInt(timestamp));
/* 2062 */           evntQulifier = fcon[idx++] & 0xFF;
/* 2063 */           if (evntQulifier == 1) {
/* 2064 */             ownLogger.info(" Event Qualifier : New Event");
/* 2065 */           } else if (evntQulifier == 3) {
/* 2066 */             ownLogger.info(" Event Qualifier : Restore Event ");
/*      */           } 
/*      */           
/* 2069 */           tmp2[0] = fcon[idx++];
/* 2070 */           tmp2[1] = fcon[idx++];
/* 2071 */           rptCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2072 */           ownLogger.info(" Contact-ID Report Code : " + rptCode);
/* 2073 */           zoneStatus = fcon[idx++] & 0xFF;
/* 2074 */           switch (zoneStatus) {
/*      */             case 1:
/* 2076 */               if (evntQulifier == 1) {
/* 2077 */                 ownLogger.info(" Zone In Alarm "); break;
/* 2078 */               }  if (evntQulifier == 3) {
/* 2079 */                 ownLogger.info(" Alarm Restored ");
/*      */               }
/*      */               break;
/*      */             case 2:
/* 2083 */               if (evntQulifier == 1) {
/* 2084 */                 ownLogger.info(" Zone Tamper "); break;
/* 2085 */               }  if (evntQulifier == 3) {
/* 2086 */                 ownLogger.info(" Tamper Restore ");
/*      */               }
/*      */               break;
/*      */             case 3:
/* 2090 */               if (evntQulifier == 1) {
/* 2091 */                 ownLogger.info(" WireFault "); break;
/* 2092 */               }  if (evntQulifier == 3) {
/* 2093 */                 ownLogger.info(" WireFault Restore  ");
/*      */               }
/*      */               break;
/*      */             case 4:
/* 2097 */               if (evntQulifier == 1) {
/* 2098 */                 ownLogger.info(" Bypass "); break;
/* 2099 */               }  if (evntQulifier == 3) {
/* 2100 */                 ownLogger.info(" UnBypassed ");
/*      */               }
/*      */               break;
/*      */             case 5:
/* 2104 */               if (evntQulifier == 1) {
/* 2105 */                 ownLogger.info(" Group Bypassed "); break;
/* 2106 */               }  if (evntQulifier == 3) {
/* 2107 */                 ownLogger.info("  Group UnBypassed ");
/*      */               }
/*      */               break;
/*      */             case 6:
/* 2111 */               if (evntQulifier == 1) {
/* 2112 */                 ownLogger.info(" Forced "); break;
/* 2113 */               }  if (evntQulifier == 3) {
/* 2114 */                 ownLogger.info(" Force Restore ");
/*      */               }
/*      */               break;
/*      */             case 7:
/* 2118 */               ownLogger.info(" Alarm Not Verified ");
/*      */               break;
/*      */             case 8:
/* 2121 */               ownLogger.info(" Cross Zone Alarm ");
/*      */               break;
/*      */             case 9:
/* 2124 */               ownLogger.info(" Zone Shutdown ");
/*      */               break;
/*      */             case 10:
/* 2127 */               ownLogger.info(" Exit Error ");
/*      */               break;
/*      */             case 11:
/* 2130 */               ownLogger.info(" Recent Close ");
/*      */               break;
/*      */             case 12:
/* 2133 */               ownLogger.info("Zone Masked");
/*      */               break;
/*      */           } 
/* 2136 */           tmp = fcon[idx++] & 0xFF;
/* 2137 */           zoneCode = String.format("%3s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2138 */           tmp = fcon[idx++] & 0xFF;
/* 2139 */           partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2140 */           ownLogger.info(" Partition Number : " + partition);
/* 2141 */           if (zoneStatus == 5) {
/* 2142 */             ownLogger.info(" Zone Group Number : " + zoneCode); break;
/*      */           } 
/* 2144 */           ownLogger.info(" Zone Number : " + zoneCode);
/*      */           break;
/*      */         
/*      */         case 2:
/* 2148 */           idx = 0;
/* 2149 */           ownLogger.info(" M2S Packet Type : PARTITION_EVENTS ");
/* 2150 */           tmp2[1] = fcon[idx++];
/* 2151 */           tmp2[0] = fcon[idx++];
/* 2152 */           eventIndex = getIntFrom2ByteArray(tmp2);
/* 2153 */           ownLogger.info(" Partition Event Index : " + eventIndex);
/* 2154 */           tmp2[0] = fcon[idx++];
/* 2155 */           tmp2[1] = fcon[idx++];
/* 2156 */           account = String.format("%4s", new Object[] { getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2157 */           ownLogger.info(" Account : " + account);
/*      */           
/* 2159 */           tmp4[0] = fcon[idx++];
/* 2160 */           tmp4[1] = fcon[idx++];
/* 2161 */           tmp4[2] = fcon[idx++];
/* 2162 */           tmp4[3] = fcon[idx++];
/* 2163 */           timestamp = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 2164 */           ownLogger.info(" Event Timestamp : " + getDateFromInt(timestamp));
/*      */           
/* 2166 */           evntQulifier = fcon[idx++] & 0xFF;
/* 2167 */           if (evntQulifier == 1) {
/* 2168 */             ownLogger.info(" Event Qualifier : New Event");
/* 2169 */           } else if (evntQulifier == 3) {
/* 2170 */             ownLogger.info(" Event Qualifier : Restore Event ");
/*      */           } 
/*      */           
/* 2173 */           tmp2[0] = fcon[idx++];
/* 2174 */           tmp2[1] = fcon[idx++];
/* 2175 */           rptCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2176 */           ownLogger.info(" Contact-ID Report Code : " + rptCode);
/* 2177 */           tmpShort = (short)(fcon[idx++] & 0xFF);
/* 2178 */           switch (tmpShort) {
/*      */             case 1:
/* 2180 */               ownLogger.info(" Disarm ");
/*      */               break;
/*      */             case 2:
/* 2183 */               ownLogger.info(" Disarm Alarm");
/*      */               break;
/*      */             case 3:
/* 2186 */               ownLogger.info(" Alarm Cancelled ");
/*      */               break;
/*      */             case 4:
/* 2189 */               ownLogger.info(" Partial/Force Away Arm ");
/*      */               break;
/*      */             case 5:
/* 2192 */               ownLogger.info(" Partial/Force Stay Arm ");
/*      */               break;
/*      */             case 6:
/* 2195 */               ownLogger.info(" Partial/Force Sleep Arm ");
/*      */               break;
/*      */             case 7:
/* 2198 */               ownLogger.info(" Away ARM ");
/*      */               break;
/*      */             case 8:
/* 2201 */               ownLogger.info(" Stay ARM  ");
/*      */               break;
/*      */             case 9:
/* 2204 */               ownLogger.info(" Sleep ARM ");
/*      */               break;
/*      */             case 10:
/* 2207 */               ownLogger.info(" Arm Failed ");
/*      */               break;
/*      */             case 11:
/* 2210 */               ownLogger.info(" Auto Arm Canceled");
/*      */               break;
/*      */             case 12:
/* 2213 */               ownLogger.info(" Late To Open/Close ");
/*      */               break;
/*      */             case 13:
/* 2216 */               ownLogger.info(" Arm Delinquency ");
/*      */               break;
/*      */             case 14:
/* 2219 */               ownLogger.info(" No Activity ");
/*      */               break;
/*      */             case 15:
/* 2222 */               ownLogger.info(" Partition Bypassed ");
/*      */               break;
/*      */             case 16:
/* 2225 */               ownLogger.info(" Fire Alarm ");
/*      */               break;
/*      */             case 17:
/* 2228 */               ownLogger.info(" Medical Alarm ");
/*      */               break;
/*      */             case 18:
/* 2231 */               ownLogger.info(" Panic Alarm ");
/*      */               break;
/*      */           } 
/* 2234 */           tmp = fcon[idx++] & 0xFF;
/* 2235 */           partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/*      */           
/* 2237 */           ownLogger.info(" Partition Number : " + partition);
/* 2238 */           tmp = fcon[idx++] & 0xFF;
/* 2239 */           switch (tmp) {
/*      */ 
/*      */             
/*      */             case 1:
/* 2243 */               ownLogger.info(" Source : RFID ");
/*      */               break;
/*      */             case 2:
/* 2246 */               ownLogger.info(" Source : NFC ");
/*      */               break;
/*      */             case 3:
/* 2249 */               ownLogger.info(" Source : Finger Print ");
/*      */               break;
/*      */             case 4:
/* 2252 */               ownLogger.info(" Source : IRIS ");
/*      */               break;
/*      */             case 5:
/* 2255 */               ownLogger.info(" Source : FU1 ");
/*      */               break;
/*      */             case 6:
/* 2258 */               ownLogger.info(" Source : FU2  ");
/*      */               break;
/*      */             case 7:
/* 2261 */               ownLogger.info(" Source : Keeloq ");
/*      */               break;
/*      */             case 8:
/* 2264 */               ownLogger.info(" Source : Keypad ");
/*      */               break;
/*      */             case 9:
/* 2267 */               ownLogger.info(" Source : Auto ARM ");
/*      */               break;
/*      */             case 10:
/* 2270 */               ownLogger.info(" Source : No Movement Arming ");
/*      */               break;
/*      */             case 11:
/* 2273 */               ownLogger.info(" Source : Key Switch ");
/*      */               break;
/*      */             case 12:
/* 2276 */               ownLogger.info(" Source : Remote ");
/*      */               break;
/*      */             case 13:
/* 2279 */               ownLogger.info(" Source : Wireless ");
/*      */               break;
/*      */             case 14:
/* 2282 */               ownLogger.info(" Quick ARM ");
/*      */               break;
/*      */           } 
/* 2285 */           tmp2[0] = fcon[idx++];
/* 2286 */           tmp2[1] = fcon[idx++];
/* 2287 */           zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2288 */           ownLogger.info(" User Report Code : " + zoneCode);
/*      */           break;
/*      */         
/*      */         case 3:
/* 2292 */           idx = 0;
/* 2293 */           ownLogger.info(" M2S Packet Type : SYSTEM ");
/* 2294 */           tmp2[1] = fcon[idx++];
/* 2295 */           tmp2[0] = fcon[idx++];
/* 2296 */           eventIndex = getIntFrom2ByteArray(tmp2);
/* 2297 */           ownLogger.info(" System Event Index : " + eventIndex);
/* 2298 */           tmp2[0] = fcon[idx++];
/* 2299 */           tmp2[1] = fcon[idx++];
/* 2300 */           account = String.format("%4s", new Object[] { getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2301 */           ownLogger.info(" Account : " + account);
/*      */           
/* 2303 */           tmp4[0] = fcon[idx++];
/* 2304 */           tmp4[1] = fcon[idx++];
/* 2305 */           tmp4[2] = fcon[idx++];
/* 2306 */           tmp4[3] = fcon[idx++];
/* 2307 */           timestamp = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 2308 */           ownLogger.info(" Event Timestamp : " + getDateFromInt(timestamp));
/*      */           
/* 2310 */           evntQulifier = fcon[idx++] & 0xFF;
/* 2311 */           if (evntQulifier == 1) {
/* 2312 */             ownLogger.info(" Event Qualifier : New Event");
/* 2313 */           } else if (evntQulifier == 3) {
/* 2314 */             ownLogger.info(" Event Qualifier : Restore Event ");
/*      */           } 
/*      */           
/* 2317 */           tmp2[0] = fcon[idx++];
/* 2318 */           tmp2[1] = fcon[idx++];
/* 2319 */           rptCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2320 */           ownLogger.info(" Contact-ID Report Code : " + rptCode);
/*      */           
/* 2322 */           sysVal1 = fcon[idx++] & 0xFF;
/* 2323 */           switch (sysVal1) {
/*      */             case 1:
/* 2325 */               ownLogger.info(" Trouble ");
/* 2326 */               sysVal2 = fcon[idx++] & 0xFF;
/* 2327 */               switch (sysVal2) {
/*      */                 case 1:
/* 2329 */                   if (evntQulifier == 1) {
/* 2330 */                     ownLogger.info(" Trouble : AC Fail "); break;
/* 2331 */                   }  if (evntQulifier == 3) {
/* 2332 */                     ownLogger.info(" Trouble :  AC Fail Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 2:
/* 2336 */                   if (evntQulifier == 1) {
/* 2337 */                     ownLogger.info(" Trouble : Battery Fail "); break;
/* 2338 */                   }  if (evntQulifier == 3) {
/* 2339 */                     ownLogger.info(" Trouble :  Battery Fail Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 3:
/* 2343 */                   if (evntQulifier == 1) {
/* 2344 */                     ownLogger.info(" Trouble  : Battery Low "); break;
/* 2345 */                   }  if (evntQulifier == 3) {
/* 2346 */                     ownLogger.info(" Trouble : Battery Low Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 4:
/* 2350 */                   if (evntQulifier == 1) {
/* 2351 */                     ownLogger.info(" Trouble : Aux CUR Overload "); break;
/* 2352 */                   }  if (evntQulifier == 3) {
/* 2353 */                     ownLogger.info(" Trouble  : Aux Cur Overload Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 5:
/* 2357 */                   if (evntQulifier == 1) {
/* 2358 */                     ownLogger.info(" Trouble : Bell Overload "); break;
/* 2359 */                   }  if (evntQulifier == 3) {
/* 2360 */                     ownLogger.info(" Trouble : Bell Overload Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 6:
/* 2364 */                   if (evntQulifier == 1) {
/* 2365 */                     ownLogger.info(" Trouble  : Bell Disconnect  "); break;
/* 2366 */                   }  if (evntQulifier == 3) {
/* 2367 */                     ownLogger.info(" Trouble : Bell Disconnect Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 7:
/* 2371 */                   if (evntQulifier == 1) {
/* 2372 */                     ownLogger.info(" Trouble : EB Lost  ");
/* 2373 */                     ownLogger.info(" Trouble: EB Lost : Expansion Board Index  " + (fcon[13] & 0xFF)); break;
/* 2374 */                   }  if (evntQulifier == 3) {
/* 2375 */                     ownLogger.info(" Trouble : EB Lost Restore ");
/* 2376 */                     ownLogger.info(" Trouble : EB Lost Restore : Expansion Board Index  " + (fcon[13] & 0xFF));
/*      */                   } 
/*      */                   break;
/*      */                 case 8:
/* 2380 */                   if (evntQulifier == 1) {
/* 2381 */                     if ((fcon[13] & 0xFF) > 0) {
/* 2382 */                       ownLogger.info(" Trouble : EB Tamper ");
/* 2383 */                       ownLogger.info(" Trouble : EB Tamper : Expansion Board Index  " + (fcon[13] & 0xFF)); break;
/*      */                     } 
/* 2385 */                     ownLogger.info(" Trouble : Control Panel Tamper "); break;
/*      */                   } 
/* 2387 */                   if (evntQulifier == 3) {
/* 2388 */                     if ((fcon[13] & 0xFF) > 0) {
/* 2389 */                       ownLogger.info(" Trouble  : EB Tamper Restore ");
/* 2390 */                       ownLogger.info(" Trouble : EB Tamper Restore : Expansion Board Index  " + (fcon[13] & 0xFF)); break;
/*      */                     } 
/* 2392 */                     ownLogger.info(" Trouble  : Control Panel Tamper Restore ");
/*      */                   } 
/*      */                   break;
/*      */                 
/*      */                 case 9:
/* 2397 */                   if (evntQulifier == 1) {
/* 2398 */                     ownLogger.info(" Trouble : Control Panel Tamper  "); break;
/* 2399 */                   }  if (evntQulifier == 3) {
/* 2400 */                     ownLogger.info(" Trouble : Control Panel Tamper Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 10:
/* 2404 */                   if (evntQulifier == 1) {
/* 2405 */                     ownLogger.info(" Trouble  : Comm Fail "); break;
/* 2406 */                   }  if (evntQulifier == 3) {
/* 2407 */                     ownLogger.info(" Trouble  : Comm Fail Resotre  ");
/*      */                   }
/*      */                   break;
/*      */                 case 11:
/* 2411 */                   if (evntQulifier == 1) {
/* 2412 */                     ownLogger.info(" Trouble : Dual Monitoring  "); break;
/* 2413 */                   }  if (evntQulifier == 3) {
/* 2414 */                     ownLogger.info(" Trouble : Dual Monitoring Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 12:
/* 2418 */                   if (evntQulifier == 1) {
/* 2419 */                     ownLogger.info(" Trouble : Telephone Disconnect "); break;
/* 2420 */                   }  if (evntQulifier == 3) {
/* 2421 */                     ownLogger.info(" Trouble : Telephone Disconnect Restore  ");
/*      */                   }
/*      */                   break;
/*      */                 case 13:
/* 2425 */                   if (evntQulifier == 1) {
/* 2426 */                     ownLogger.info(" Trouble : GSM RF Jamming ");
/* 2427 */                     ownLogger.info(" Trouble : GSM RF Jamming  SIM Index(1- SIM1, 2- SIM2, 3 - Both) : " + (fcon[13] & 0xFF)); break;
/* 2428 */                   }  if (evntQulifier == 3) {
/* 2429 */                     ownLogger.info(" Trouble : GSM RF Jamming Restore ");
/* 2430 */                     ownLogger.info(" Trouble : GSM RF Jamming  SIM Index(1- SIM1, 2- SIM2, 3 - Both) :  " + (fcon[13] & 0xFF));
/*      */                   } 
/*      */                   break;
/*      */                 case 14:
/* 2434 */                   if (evntQulifier == 1) {
/* 2435 */                     ownLogger.info(" Trouble : GSM No Service "); break;
/* 2436 */                   }  if (evntQulifier == 3) {
/* 2437 */                     ownLogger.info(" Trouble : GSM No Service Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 15:
/* 2441 */                   if (evntQulifier == 1) {
/* 2442 */                     ownLogger.info(" Trouble : IP COMM Fail "); break;
/* 2443 */                   }  if (evntQulifier == 3) {
/* 2444 */                     ownLogger.info(" Trouble  : IP Comm Fail Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 16:
/* 2448 */                   if (evntQulifier == 1) {
/* 2449 */                     ownLogger.info(" Trouble  : GPRS COMM Fail "); break;
/* 2450 */                   }  if (evntQulifier == 3) {
/* 2451 */                     ownLogger.info(" Trouble : GPRS Comm Fail Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 17:
/* 2455 */                   if (evntQulifier == 1) {
/* 2456 */                     ownLogger.info(" Trouble : Ethernet COMM Fail "); break;
/* 2457 */                   }  if (evntQulifier == 3) {
/* 2458 */                     ownLogger.info(" Trouble : Ethernet Comm Fail Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 18:
/* 2462 */                   if (evntQulifier == 1) {
/* 2463 */                     ownLogger.info(" Trouble  : Wi-Fi COMM Fail  "); break;
/* 2464 */                   }  if (evntQulifier == 3) {
/* 2465 */                     ownLogger.info(" Trouble : WiFi Comm Fail Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 19:
/* 2469 */                   if (evntQulifier == 1) {
/* 2470 */                     ownLogger.info(" Trouble : Time Loss "); break;
/* 2471 */                   }  if (evntQulifier == 3) {
/* 2472 */                     ownLogger.info(" Trouble : Timer Loss Restore ");
/*      */                   }
/*      */                   break;
/*      */                 case 20:
/* 2476 */                   if (evntQulifier == 1) {
/* 2477 */                     ownLogger.info(" Trouble : Fire Loop Trouble "); break;
/* 2478 */                   }  if (evntQulifier == 3) {
/* 2479 */                     ownLogger.info(" Trouble : Fire Loop Trouble Restore ");
/*      */                   }
/*      */                   break;
/*      */               } 
/*      */               break;
/*      */             case 2:
/* 2485 */               tmp2[0] = fcon[idx++];
/* 2486 */               tmp2[1] = fcon[idx++];
/* 2487 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2488 */               if (evntQulifier == 1) {
/* 2489 */                 ownLogger.info(" Installer Mode Entered ");
/* 2490 */                 ownLogger.info(" Installer Mode Entered : User Report Code : " + zoneCode); break;
/* 2491 */               }  if (evntQulifier == 3) {
/* 2492 */                 ownLogger.info(" Installer Mode Exit ");
/* 2493 */                 ownLogger.info(" Installer Mode Exit : User Report Code : " + zoneCode);
/*      */               } 
/*      */               break;
/*      */             case 3:
/* 2497 */               tmp2[0] = fcon[idx++];
/* 2498 */               tmp2[1] = fcon[idx++];
/* 2499 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2500 */               if (evntQulifier == 1) {
/* 2501 */                 ownLogger.info(" Maintainence Mode Entered ");
/* 2502 */                 ownLogger.info(" Maintainence Mode Entered : User Report Code : " + zoneCode); break;
/* 2503 */               }  if (evntQulifier == 3) {
/* 2504 */                 ownLogger.info(" Maintainence Mode Exit ");
/* 2505 */                 ownLogger.info(" Maintainence Mode Exit : User Report Code : " + zoneCode);
/*      */               } 
/*      */               break;
/*      */             case 4:
/* 2509 */               tmp2[0] = fcon[idx++];
/* 2510 */               tmp2[1] = fcon[idx++];
/* 2511 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2512 */               if (evntQulifier == 1) {
/* 2513 */                 ownLogger.info(" Master Config Mode Entered  ");
/* 2514 */                 ownLogger.info(" Master Config Mode Entered : User Report Code : " + zoneCode); break;
/* 2515 */               }  if (evntQulifier == 3) {
/* 2516 */                 ownLogger.info(" Master Config Mode Exited  ");
/* 2517 */                 ownLogger.info(" Master Config Mode Exited : User Report Code : " + zoneCode);
/*      */               } 
/*      */               break;
/*      */             case 5:
/* 2521 */               tmp2[0] = fcon[idx++];
/* 2522 */               tmp2[1] = fcon[idx++];
/* 2523 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2524 */               if (evntQulifier == 1) {
/* 2525 */                 ownLogger.info(" User Config Mode Entered ");
/* 2526 */                 ownLogger.info(" User Config Mode Entered  : User Report Code : " + zoneCode); break;
/* 2527 */               }  if (evntQulifier == 3) {
/* 2528 */                 ownLogger.info(" User Config Mode Exited ");
/* 2529 */                 ownLogger.info(" User Config Mode Exited : User Report Code : " + zoneCode);
/*      */               } 
/*      */               break;
/*      */             case 6:
/* 2533 */               if (evntQulifier == 1) {
/* 2534 */                 ownLogger.info(" Day Light Saving Started "); break;
/* 2535 */               }  if (evntQulifier == 3) {
/* 2536 */                 ownLogger.info(" Day Light Saving End ");
/*      */               }
/*      */               break;
/*      */             case 7:
/* 2540 */               ownLogger.info(" Time Changed ");
/*      */               break;
/*      */             case 8:
/* 2543 */               tmp2[0] = fcon[idx++];
/* 2544 */               tmp2[1] = fcon[idx++];
/* 2545 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2546 */               ownLogger.info(" User Report Code : " + zoneCode);
/* 2547 */               if (getIntFrom2ByteArray(swapLSB2MSB(tmp2)) == 0) {
/* 2548 */                 ownLogger.info(" Test Stopped By Systemd Automatically  ");
/*      */               }
/* 2550 */               if (evntQulifier == 1) {
/* 2551 */                 ownLogger.info(" Installer Walk Test Start "); break;
/* 2552 */               }  if (evntQulifier == 3) {
/* 2553 */                 ownLogger.info(" Installer Walk Test End ");
/*      */               }
/*      */               break;
/*      */             case 9:
/* 2557 */               tmp2[0] = fcon[idx++];
/* 2558 */               tmp2[1] = fcon[idx++];
/* 2559 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2560 */               ownLogger.info(" User Report Code : " + zoneCode);
/* 2561 */               if (getIntFrom2ByteArray(swapLSB2MSB(tmp2)) == 0) {
/* 2562 */                 ownLogger.info(" Test Stopped By Systemd Automatically  ");
/*      */               }
/* 2564 */               if (evntQulifier == 1) {
/* 2565 */                 ownLogger.info(" User Walk Test Start "); break;
/* 2566 */               }  if (evntQulifier == 3) {
/* 2567 */                 ownLogger.info(" User Walk Test End ");
/*      */               }
/*      */               break;
/*      */             case 10:
/* 2571 */               tmp2[0] = fcon[idx++];
/* 2572 */               tmp2[1] = fcon[idx++];
/* 2573 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2574 */               ownLogger.info(" User Report Code : " + zoneCode);
/* 2575 */               ownLogger.info(" Automatic Test Report ");
/*      */               break;
/*      */             case 11:
/* 2578 */               if (evntQulifier == 1) {
/* 2579 */                 ownLogger.info(" CAN - Trobule"); break;
/* 2580 */               }  if (evntQulifier == 3) {
/* 2581 */                 ownLogger.info(" CAN - Restore");
/*      */               }
/*      */               break;
/*      */             case 12:
/* 2585 */               if (evntQulifier == 1) {
/* 2586 */                 ownLogger.info(" EEPROM  - Trouble"); break;
/* 2587 */               }  if (evntQulifier == 3) {
/* 2588 */                 ownLogger.info(" EEPROM  - Restore");
/*      */               }
/*      */               break;
/*      */             case 13:
/* 2592 */               if (evntQulifier == 1) {
/* 2593 */                 ownLogger.info(" SD Card (ONLY EB) - Trouble "); break;
/* 2594 */               }  if (evntQulifier == 3) {
/* 2595 */                 ownLogger.info(" SD Card (ONLY EB) - Restore ");
/*      */               }
/*      */               break;
/*      */             case 14:
/* 2599 */               if (evntQulifier == 1) {
/* 2600 */                 ownLogger.info(" GSM - Trouble "); break;
/* 2601 */               }  if (evntQulifier == 3) {
/* 2602 */                 ownLogger.info(" GSM - Restore ");
/*      */               }
/*      */               break;
/*      */             case 15:
/* 2606 */               if (evntQulifier == 1) {
/* 2607 */                 ownLogger.info(" Wi-Fi - Trouble "); break;
/* 2608 */               }  if (evntQulifier == 3) {
/* 2609 */                 ownLogger.info(" Wi-Fi - Restore ");
/*      */               }
/*      */               break;
/*      */             case 16:
/* 2613 */               if (evntQulifier == 1) {
/* 2614 */                 ownLogger.info(" Ethernet  -  Trouble "); break;
/* 2615 */               }  if (evntQulifier == 3) {
/* 2616 */                 ownLogger.info(" Ethernet  -  Restore ");
/*      */               }
/*      */               break;
/*      */             case 17:
/* 2620 */               if (evntQulifier == 1) {
/* 2621 */                 ownLogger.info(" Telephone   - Trouble"); break;
/* 2622 */               }  if (evntQulifier == 3) {
/* 2623 */                 ownLogger.info(" Telephone   - Restore");
/*      */               }
/*      */               break;
/*      */             case 18:
/* 2627 */               if (evntQulifier == 1) {
/* 2628 */                 ownLogger.info(" BlueTooth  - Trouble"); break;
/* 2629 */               }  if (evntQulifier == 3) {
/* 2630 */                 ownLogger.info(" BlueTooth  - Restore");
/*      */               }
/*      */               break;
/*      */             case 19:
/* 2634 */               if (evntQulifier == 1) {
/* 2635 */                 ownLogger.info(" Zigbee  - Trouble"); break;
/* 2636 */               }  if (evntQulifier == 3) {
/* 2637 */                 ownLogger.info(" Zigbee  - Restore");
/*      */               }
/*      */               break;
/*      */             case 20:
/* 2641 */               if (evntQulifier == 1) {
/* 2642 */                 ownLogger.info(" Weigand1 - Trouble "); break;
/* 2643 */               }  if (evntQulifier == 3) {
/* 2644 */                 ownLogger.info(" Weigand1 - Restore ");
/*      */               }
/*      */               break;
/*      */             case 21:
/* 2648 */               if (evntQulifier == 1) {
/* 2649 */                 ownLogger.info(" Weigand2 - Trouble "); break;
/* 2650 */               }  if (evntQulifier == 3) {
/* 2651 */                 ownLogger.info(" Weigand2 - Restore ");
/*      */               }
/*      */               break;
/*      */             case 22:
/* 2655 */               if (evntQulifier == 1) {
/* 2656 */                 ownLogger.info(" UART CRC  - Trouble"); break;
/* 2657 */               }  if (evntQulifier == 3) {
/* 2658 */                 ownLogger.info(" UART CRC  - Restore");
/*      */               }
/*      */               break;
/*      */             case 23:
/* 2662 */               tmp = fcon[idx + 1];
/* 2663 */               if (evntQulifier == 1 && fcon[idx] == 1) {
/* 2664 */                 ownLogger.info(" Keefob  - Low Battery For Keyfob with Index : " + tmp); break;
/* 2665 */               }  if (evntQulifier == 3 && fcon[idx] == 1) {
/* 2666 */                 ownLogger.info(" Keefob  - Low Battery restore For Keyfob with Index : " + tmp); break;
/* 2667 */               }  if (evntQulifier == 1 && fcon[idx] == 2) {
/* 2668 */                 ownLogger.info(" Keefob  - Communication Lost "); break;
/* 2669 */               }  if (evntQulifier == 3 && fcon[idx] == 2) {
/* 2670 */                 ownLogger.info(" Keefob  - Communication Lost Restore ");
/*      */               }
/*      */               break;
/*      */             case 24:
/* 2674 */               if (evntQulifier == 1) {
/* 2675 */                 ownLogger.info(" Accelerometer - Trouble "); break;
/* 2676 */               }  if (evntQulifier == 3) {
/* 2677 */                 ownLogger.info(" Accelerometer - Restore ");
/*      */               }
/*      */               break;
/*      */             case 25:
/* 2681 */               if (evntQulifier == 1) {
/* 2682 */                 ownLogger.info(" Proximity  - Trouble"); break;
/* 2683 */               }  if (evntQulifier == 3) {
/* 2684 */                 ownLogger.info(" Proximity  - Restore");
/*      */               }
/*      */               break;
/*      */             case 26:
/* 2688 */               ownLogger.info(" Watch Dog Reset ");
/*      */               break;
/*      */             case 27:
/* 2691 */               if (evntQulifier == 1) {
/* 2692 */                 ownLogger.info(" SIM #1 - Trouble ");
/* 2693 */               } else if (evntQulifier == 3) {
/* 2694 */                 ownLogger.info(" SIM #1 - Restore ");
/*      */               } 
/* 2696 */               if (fcon[10] == 1) {
/* 2697 */                 ownLogger.info("SIM Failure"); break;
/* 2698 */               }  if (fcon[10] == 2) {
/* 2699 */                 ownLogger.info("SIM Jammed");
/*      */               }
/*      */               break;
/*      */             case 28:
/* 2703 */               if (evntQulifier == 1) {
/* 2704 */                 ownLogger.info(" SIM #2  - Trouble");
/* 2705 */               } else if (evntQulifier == 3) {
/* 2706 */                 ownLogger.info(" SIM #2  - Restore");
/*      */               } 
/* 2708 */               if (fcon[10] == 1) {
/* 2709 */                 ownLogger.info("SIM Failure"); break;
/* 2710 */               }  if (fcon[10] == 2) {
/* 2711 */                 ownLogger.info("SIM Jammed");
/*      */               }
/*      */               break;
/*      */             case 29:
/* 2715 */               if (evntQulifier == 1) {
/* 2716 */                 ownLogger.info(" CAN Bus Unstable - Trouble "); break;
/* 2717 */               }  if (evntQulifier == 3) {
/* 2718 */                 ownLogger.info(" CAN Bus Unstable - Restore ");
/*      */               }
/*      */               break;
/*      */           } 
/*      */           
/*      */           break;
/*      */         
/*      */         case 4:
/* 2726 */           idx = 0;
/* 2727 */           ownLogger.info(" M2S Packet Type : MISCELLANEOUS ");
/* 2728 */           tmp2[1] = fcon[idx++];
/* 2729 */           tmp2[0] = fcon[idx++];
/* 2730 */           eventIndex = getIntFrom2ByteArray(tmp2);
/* 2731 */           ownLogger.info(" Miscellaneous Event Index : " + eventIndex);
/*      */           
/* 2733 */           tmp2[0] = fcon[idx++];
/* 2734 */           tmp2[1] = fcon[idx++];
/* 2735 */           account = String.format("%4s", new Object[] { getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2736 */           ownLogger.info(" Account : " + account);
/*      */           
/* 2738 */           tmp4[0] = fcon[idx++];
/* 2739 */           tmp4[1] = fcon[idx++];
/* 2740 */           tmp4[2] = fcon[idx++];
/* 2741 */           tmp4[3] = fcon[idx++];
/* 2742 */           timestamp = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 2743 */           ownLogger.info(" Event Timestamp : " + getDateFromInt(timestamp));
/*      */           
/* 2745 */           evntQulifier = fcon[idx++] & 0xFF;
/* 2746 */           if (evntQulifier == 1) {
/* 2747 */             ownLogger.info(" Event Qualifier : New Event");
/* 2748 */           } else if (evntQulifier == 3) {
/* 2749 */             ownLogger.info(" Event Qualifier : Restore Event ");
/*      */           } 
/*      */           
/* 2752 */           tmp2[0] = fcon[idx++];
/* 2753 */           tmp2[1] = fcon[idx++];
/* 2754 */           rptCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2755 */           ownLogger.info(" Contact-ID Report Code : " + rptCode);
/* 2756 */           tmpShort = (short)(fcon[idx++] & 0xFF);
/* 2757 */           switch (tmpShort) {
/*      */             case 1:
/* 2759 */               ownLogger.info(" Event Log 90% ");
/*      */               break;
/*      */             case 2:
/* 2762 */               ownLogger.info(" Event Log Overflow ");
/*      */               break;
/*      */             case 3:
/* 2765 */               ownLogger.info(" Call Back Request ");
/*      */               break;
/*      */             case 4:
/* 2768 */               ownLogger.info(" Successful Upload ");
/*      */               break;
/*      */             case 5:
/* 2771 */               ownLogger.info(" Remote Access Failed ");
/*      */               break;
/*      */             case 6:
/* 2774 */               ownLogger.info(" Successful Download ");
/*      */               break;
/*      */             case 7:
/* 2777 */               ownLogger.info(" Cold Start ( power on reset of control panel ) ");
/*      */               break;
/*      */             case 8:
/* 2780 */               tmp = fcon[idx++] & 0xFF;
/* 2781 */               partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2782 */               ownLogger.info(" Wrong Code Entry : Partition Number : " + partition);
/*      */               break;
/*      */             case 9:
/* 2785 */               tmp = fcon[idx++] & 0xFF;
/* 2786 */               partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2787 */               ownLogger.info(" Keypad Lockout : Partition Number : " + partition);
/* 2788 */               tmp = fcon[idx++] & 0xFF;
/* 2789 */               ownLogger.info(" Keypad Lockout : Expansion Board Index  " + tmp);
/*      */               break;
/*      */             case 10:
/* 2792 */               tmp = fcon[idx++] & 0xFF;
/* 2793 */               partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2794 */               ownLogger.info(" Legal Code Entry : Partition Number : " + partition);
/* 2795 */               tmp2[0] = fcon[idx++];
/* 2796 */               tmp2[1] = fcon[idx++];
/* 2797 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2798 */               ownLogger.info(" Legal Code Entry : User Report Code : " + zoneCode);
/*      */               break;
/*      */             case 11:
/* 2801 */               tmp = fcon[idx++] & 0xFF;
/* 2802 */               partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2803 */               ownLogger.info(" Duress Code Entry : Partition Number : " + partition);
/* 2804 */               tmp2[0] = fcon[idx++];
/* 2805 */               tmp2[1] = fcon[idx++];
/* 2806 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2807 */               ownLogger.info(" Duress Code Entry : User Report Code : " + zoneCode);
/*      */               break;
/*      */             
/*      */             case 12:
/* 2811 */               tmp = fcon[idx++] & 0xFF;
/* 2812 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2813 */               ownLogger.info(" PGM Activated : PGM Number : " + zoneCode);
/* 2814 */               tmp = fcon[idx++] & 0xFF;
/* 2815 */               ownLogger.info("  PGM Activated : Expansion Board Index  " + tmp);
/*      */               break;
/*      */           } 
/*      */           
/*      */           break;
/*      */         case 5:
/* 2821 */           idx = 0;
/* 2822 */           ownLogger.info(" M2S Packet Type : ACCESS ");
/* 2823 */           tmp2[1] = fcon[idx++];
/* 2824 */           tmp2[0] = fcon[idx++];
/* 2825 */           eventIndex = getIntFrom2ByteArray(tmp2);
/* 2826 */           ownLogger.info(" Access Event Index : " + eventIndex);
/*      */           
/* 2828 */           tmp2[0] = fcon[idx++];
/* 2829 */           tmp2[1] = fcon[idx++];
/* 2830 */           account = String.format("%4s", new Object[] { getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2831 */           ownLogger.info(" Account : " + account);
/*      */           
/* 2833 */           tmp4[0] = fcon[idx++];
/* 2834 */           tmp4[1] = fcon[idx++];
/* 2835 */           tmp4[2] = fcon[idx++];
/* 2836 */           tmp4[3] = fcon[idx++];
/* 2837 */           timestamp = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 2838 */           ownLogger.info(" Event Timestamp : " + getDateFromInt(timestamp));
/*      */           
/* 2840 */           evntQulifier = fcon[idx++] & 0xFF;
/* 2841 */           if (evntQulifier == 1) {
/* 2842 */             ownLogger.info(" Event Qualifier : New Event");
/* 2843 */           } else if (evntQulifier == 3) {
/* 2844 */             ownLogger.info(" Event Qualifier : Restore Event ");
/*      */           } 
/*      */           
/* 2847 */           tmp2[0] = fcon[idx++];
/* 2848 */           tmp2[1] = fcon[idx++];
/* 2849 */           rptCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2850 */           ownLogger.info(" Contact-ID Report Code : " + rptCode);
/*      */           
/* 2852 */           tmp2[0] = fcon[idx++];
/* 2853 */           ownLogger.info(" Access/Keypad EB Index : " + (tmp2[0] & 0xFF));
/*      */           
/* 2855 */           tmp2 = getHighLowBytes(fcon[idx++] & 0xFF);
/* 2856 */           if ((tmp2[0] & 0xFF) == 1) {
/* 2857 */             ownLogger.info(" Interface Type : Serial ");
/* 2858 */           } else if ((tmp2[0] & 0xFF) == 2) {
/* 2859 */             ownLogger.info(" Interface Type : Weigand #1 ");
/* 2860 */           } else if ((tmp2[0] & 0xFF) == 3) {
/* 2861 */             ownLogger.info(" Interface Type : Weigand #2 ");
/*      */           } 
/*      */           
/* 2864 */           tmp2 = getHighLowBytes(fcon[idx++] & 0xFF);
/* 2865 */           if ((tmp2[0] & 0xFF) == 0) {
/* 2866 */             ownLogger.info(" Invalid ");
/* 2867 */           } else if ((tmp2[0] & 0xFF) == 1) {
/* 2868 */             ownLogger.info(" Access Granted ");
/* 2869 */           } else if ((tmp2[0] & 0xFF) == 2) {
/* 2870 */             ownLogger.info(" Egress Granted ");
/* 2871 */           } else if ((tmp2[0] & 0xFF) == 3) {
/* 2872 */             ownLogger.info(" Access Denied ");
/* 2873 */             if ((tmp2[1] & 0xFF) == 0) {
/* 2874 */               ownLogger.info(" Invalid ");
/* 2875 */             } else if ((tmp2[1] & 0xFF) == 1) {
/* 2876 */               ownLogger.info(" Anti Passback ");
/* 2877 */             } else if ((tmp2[1] & 0xFF) == 2) {
/* 2878 */               ownLogger.info(" Time not matched ");
/* 2879 */             } else if ((tmp2[1] & 0xFF) == 3) {
/* 2880 */               ownLogger.info(" Eb index not matched ");
/* 2881 */             } else if ((tmp2[1] & 0xFF) == 4) {
/* 2882 */               ownLogger.info(" Interface Type Not matched ");
/*      */             } 
/* 2884 */           } else if ((tmp2[0] & 0xFF) == 4) {
/* 2885 */             ownLogger.info(" Egress Denied ");
/* 2886 */             if ((tmp2[1] & 0xFF) == 0) {
/* 2887 */               ownLogger.info(" Invalid ");
/* 2888 */             } else if ((tmp2[1] & 0xFF) == 1) {
/* 2889 */               ownLogger.info(" Anti Passback ");
/* 2890 */             } else if ((tmp2[1] & 0xFF) == 2) {
/* 2891 */               ownLogger.info(" Time not matched ");
/* 2892 */             } else if ((tmp2[1] & 0xFF) == 3) {
/* 2893 */               ownLogger.info(" Eb index not matched ");
/* 2894 */             } else if ((tmp2[1] & 0xFF) == 4) {
/* 2895 */               ownLogger.info(" Interface Type Not matched ");
/*      */             } 
/*      */           } 
/*      */           
/* 2899 */           tmp2[0] = fcon[idx++];
/* 2900 */           tmp2[1] = fcon[idx++];
/* 2901 */           zoneCode = String.format("%3s", new Object[] { String.valueOf(getIntFrom2ByteArray(swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2902 */           ownLogger.info(" User Index : " + zoneCode);
/*      */           break;
/*      */         
/*      */         case 80:
/* 2906 */           idx = 0;
/* 2907 */           sb = new StringBuilder();
/* 2908 */           for (i = 0; i < flen; i++) {
/* 2909 */             sb.append(String.format(" %x ", new Object[] { Integer.valueOf(fcon[i] & 0xFF) }));
/*      */           } 
/* 2911 */           ownLogger.info("Data Received For MODULE_ID :");
/* 2912 */           ownLogger.info(sb.toString());
/*      */           
/* 2914 */           tmp2[0] = fcon[idx++];
/* 2915 */           tmp2[1] = fcon[idx++];
/* 2916 */           account = getHexStringFromByteArray(tmp2);
/* 2917 */           account = String.format("%4s", new Object[] { account }).replace(' ', '0');
/* 2918 */           ownLogger.info("MODULE_ID : Account : " + account);
/*      */           
/* 2920 */           tmp4[0] = fcon[idx++];
/* 2921 */           tmp4[1] = fcon[idx++];
/* 2922 */           tmp4[2] = fcon[idx++];
/* 2923 */           tmp4[3] = fcon[idx++];
/* 2924 */           timestamp = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 2925 */           ownLogger.info("MODULE_ID :  Timestamp : " + getDateFromInt(timestamp));
/*      */           
/* 2927 */           tmp2[0] = fcon[idx++];
/* 2928 */           tmp2[1] = fcon[idx++];
/* 2929 */           ownLogger.info("Product ID : " + getIntFrom2ByteArray(swapLSB2MSB(tmp2)));
/* 2930 */           oper = new byte[flen - idx];
/* 2931 */           System.arraycopy(fcon, idx, oper, 0, flen - idx);
/* 2932 */           ownLogger.info("MODULE_ID :   " + getASCIIFromByteArray(oper));
/*      */           break;
/*      */         
/*      */         case 81:
/* 2936 */           idx = 0;
/* 2937 */           sb = new StringBuilder();
/* 2938 */           sb.append(getIntFromHexByte(fcon[0])).append('.').append(getIntFromHexByte(fcon[1]));
/* 2939 */           ownLogger.info(" MODULE_FW_VERSION :" + sb.toString());
/*      */           break;
/*      */         
/*      */         case 82:
/* 2943 */           idx = 0;
/* 2944 */           tmp2[0] = fcon[idx++];
/* 2945 */           tmp2[1] = fcon[idx++];
/* 2946 */           st = getBinaryFromInt(getIntFrom2ByteArray(tmp2));
/* 2947 */           if (st[7] == '1') {
/* 2948 */             ownLogger.info("MODULE_HW_DTLS :   GPRS ");
/*      */           } else {
/* 2950 */             ownLogger.info("MODULE_HW_DTLS :  NO GPRS ");
/*      */           } 
/* 2952 */           if (st[6] == '1') {
/* 2953 */             ownLogger.info("MODULE_HW_DTLS :   ETHERNET ");
/*      */           } else {
/* 2955 */             ownLogger.info("MODULE_HW_DTLS :  NO ETHERNET ");
/*      */           } 
/* 2957 */           if (st[5] == '1') {
/* 2958 */             ownLogger.info("MODULE_HW_DTLS :   WIFI ");
/*      */           } else {
/* 2960 */             ownLogger.info("MODULE_HW_DTLS :  NO WIFI ");
/*      */           } 
/* 2962 */           if (st[4] == '1') {
/* 2963 */             ownLogger.info("MODULE_HW_DTLS :   TELEPHONE LINE ");
/*      */           } else {
/* 2965 */             ownLogger.info("MODULE_HW_DTLS :  NO TELEPHONE LINE ");
/*      */           } 
/* 2967 */           if (st[3] == '1') {
/* 2968 */             ownLogger.info("MODULE_HW_DTLS :   ZIGBEE "); break;
/*      */           } 
/* 2970 */           ownLogger.info("MODULE_HW_DTLS :  NO ZIGBEE ");
/*      */           break;
/*      */ 
/*      */         
/*      */         case 83:
/* 2975 */           switch (fcon[0] & 0xFF) {
/*      */             case 0:
/* 2977 */               ownLogger.info("BATTERY_STATUS :  Battery Absent ");
/*      */               break;
/*      */             case 1:
/* 2980 */               ownLogger.info("BATTERY_STATUS :  Battery Low ( Only if ac mains is absent) ");
/*      */               break;
/*      */             case 2:
/* 2983 */               ownLogger.info("BATTERY_STATUS :  Battery Normal ");
/*      */               break;
/*      */             case 3:
/* 2986 */               ownLogger.info("BATTERY_STATUS :  Bulk Charging ");
/*      */               break;
/*      */             case 4:
/* 2989 */               ownLogger.info("BATTERY_STATUS :  Absorption Charging ");
/*      */               break;
/*      */             case 5:
/* 2992 */               ownLogger.info("BATTERY_STATUS :  FLOAT CHARGING ");
/*      */               break;
/*      */             case 6:
/* 2995 */               ownLogger.info("BATTERY_STATUS :  CHARGER OFF ");
/*      */               break;
/*      */           } 
/* 2998 */           ownLogger.info("BATTERY_STATUS :  Battery Voltage:  " + (fcon[1] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[2] & 0xFF) }));
/* 2999 */           ownLogger.info("BATTERY_STATUS :  Battery Input current : " + Float.parseFloat((fcon[3] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[4] & 0xFF) })));
/*      */           break;
/*      */         
/*      */         case 84:
/* 3003 */           switch (fcon[0] & 0xFF) {
/*      */             case 1:
/* 3005 */               ownLogger.info("CURRENT_INTERFACE :  GPRS ");
/*      */               break;
/*      */             case 2:
/* 3008 */               ownLogger.info("CURRENT_INTERFACE :  CSD ");
/*      */               break;
/*      */             case 3:
/* 3011 */               ownLogger.info("CURRENT_INTERFACE :  ETHERNET ");
/*      */               break;
/*      */             case 4:
/* 3014 */               ownLogger.info("CURRENT_INTERFACE :  WIFI ");
/*      */               break;
/*      */             case 5:
/* 3017 */               ownLogger.info("CURRENT_INTERFACE :  TELEPHONE LINE ");
/*      */               break;
/*      */             case 6:
/* 3020 */               ownLogger.info("CURRENT_INTERFACE :  SMS ");
/*      */               break;
/*      */           } 
/*      */           
/*      */           break;
/*      */         case 86:
/* 3026 */           sb = new StringBuilder();
/* 3027 */           for (j = 0; j < flen - 1; j++) {
/* 3028 */             sb.append(getFormatIntFromHexByte(fcon[j]));
/*      */           }
/* 3030 */           sb.append(fcon[7] / 10);
/* 3031 */           ownLogger.info("MODEM_IMEI : " + sb.toString());
/*      */           break;
/*      */         case 87:
/* 3034 */           if (fcon[0] == 1) {
/* 3035 */             ownLogger.info("MODEM_MODEL : TELIT_GE865"); break;
/* 3036 */           }  if (fcon[0] == 2) {
/* 3037 */             ownLogger.info("MODEM_MODEL : TELIT_GE910"); break;
/* 3038 */           }  if (fcon[0] == 3) {
/* 3039 */             ownLogger.info("MODEM_MODEL : TELIT_HE910");
/*      */           }
/*      */           break;
/*      */         case 88:
/* 3043 */           sb = new StringBuilder();
/* 3044 */           tmp2[0] = fcon[2];
/* 3045 */           tmp2[1] = fcon[3];
/* 3046 */           sb.append(getIntFromHexByte(fcon[0])).append(".").append(getIntFromHexByte(fcon[1])).append(".").append(getIntFrom2ByteArray(tmp2));
/* 3047 */           ownLogger.info("GSM_MODEM_FW_VERSION : " + sb.toString());
/*      */           break;
/*      */         case 89:
/* 3050 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3051 */           ownLogger.info("GSM_SIGNAL_LEVEL   : " + tmpShort);
/*      */           break;
/*      */ 
/*      */         
/*      */         case 90:
/* 3056 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 3057 */           apn = (short)getIntFromHexByte(fcon[2]);
/* 3058 */           ownLogger.info("MODEM_INTERFACE_TEST_STATUS  # SIM # : " + tmpShort);
/*      */           
/* 3060 */           ownLogger.info("MODEM_INTERFACE_TEST_STATUS  # APN  # : " + apn);
/*      */           
/* 3062 */           apn = (short)getIntFromHexByte(fcon[3]);
/* 3063 */           if ((fcon[0] & 0xFF) == 0) {
/* 3064 */             if (apn == 0) {
/* 3065 */               ownLogger.info("MODEM_INTERFACE_TEST_STATUS # ON_BOARD  # SUCCESS"); break;
/* 3066 */             }  if (apn == 1)
/* 3067 */               ownLogger.info("MODEM_INTERFACE_TEST_STATUS  # ON_BOARD # FAILURE");  break;
/*      */           } 
/* 3069 */           if ((fcon[0] & 0xFF) == 1) {
/* 3070 */             if (apn == 0) {
/* 3071 */               ownLogger.info("MODEM_INTERFACE_TEST_STATUS  # EB COMM # SUCCESS"); break;
/* 3072 */             }  if (apn == 1) {
/* 3073 */               ownLogger.info("MODEM_INTERFACE_TEST_STATUS  # EB COMM # FAILURE");
/*      */             }
/*      */           } 
/*      */           break;
/*      */         case 95:
/* 3078 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3079 */           sb = new StringBuilder();
/* 3080 */           for (k = 1; k < flen; k++) {
/* 3081 */             sb.append(getFormatIntFromHexByte(fcon[k]));
/*      */           }
/* 3083 */           if (tmpShort == 1) {
/* 3084 */             ownLogger.info("SIMCARD_ICCID  SIM #1: " + sb.toString()); break;
/* 3085 */           }  if (tmpShort == 2) {
/* 3086 */             ownLogger.info("SIMCARD_ICCID SIM #2: " + sb.toString());
/*      */           }
/*      */           break;
/*      */         case 96:
/* 3090 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3091 */           oper = new byte[flen - 1];
/* 3092 */           System.arraycopy(fcon, 1, oper, 0, flen - 1);
/*      */           
/* 3094 */           if (tmpShort == 1) {
/* 3095 */             ownLogger.info("SIMCARD_OPERATOR  SIM #1: " + getASCIIFromByteArray(oper)); break;
/*      */           } 
/* 3097 */           if (tmpShort == 2) {
/* 3098 */             ownLogger.info("SIMCARD_OPERATOR  SIM #2: " + getASCIIFromByteArray(oper));
/*      */           }
/*      */           break;
/*      */         case 94:
/* 3102 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3103 */           ownLogger.info("CURRENT_SIM_APN  SIM : " + tmpShort);
/* 3104 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 3105 */           ownLogger.info("CURRENT_SIM_APN  APN : " + tmpShort);
/*      */           break;
/*      */         
/*      */         case 93:
/* 3109 */           ownLogger.info("DATA_TRANSFERED_GPRS : " + getIntFrom4ByteArray(swapLSB2MSB4ByteArray(fcon)));
/*      */           break;
/*      */         
/*      */         case 92:
/* 3113 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3114 */           if (tmpShort == 0) {
/* 3115 */             ownLogger.info("OTA_STATUS  #  SUCCESS"); break;
/* 3116 */           }  if (tmpShort == 1) {
/* 3117 */             ownLogger.info("OTA_STATUS  # FAILURE");
/*      */           }
/*      */           break;
/*      */         case 97:
/* 3121 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3122 */           sb = new StringBuilder();
/*      */           
/* 3124 */           for (k = 1; k < flen - 1; k++) {
/* 3125 */             sb.append(getFormatIntFromHexByte(fcon[k]));
/*      */           }
/* 3127 */           sb.append(fcon[8] / 10);
/*      */           
/* 3129 */           if (tmpShort == 1) {
/* 3130 */             ownLogger.info("SIM_IMSI  SIM #1: " + sb.toString()); break;
/* 3131 */           }  if (tmpShort == 2) {
/* 3132 */             ownLogger.info("SIM_IMSI SIM #2: " + sb.toString());
/*      */           }
/*      */           break;
/*      */         case 91:
/* 3136 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3137 */           if (tmpShort == 0) {
/* 3138 */             ownLogger.info("GSM_JAMMER_STATUS  #  Operative");
/* 3139 */           } else if (tmpShort == 1) {
/* 3140 */             ownLogger.info("GSM_JAMMER_STATUS  # JAMMED ");
/*      */           } 
/* 3142 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 3143 */           if (tmpShort == 0) {
/* 3144 */             ownLogger.info("GSM_JAMMER_STATUS  #  JDR LOW "); break;
/* 3145 */           }  if (tmpShort == 1) {
/* 3146 */             ownLogger.info("GSM_JAMMER_STATUS  # JDR HIGH ");
/*      */           }
/*      */           break;
/*      */         
/*      */         case 98:
/* 3151 */           ownLogger.info("SIM_CARD_STATUS  #  SIM # " + (fcon[0] & 0xFF));
/* 3152 */           tmpShort = (short)getIntFromHexByte(fcon[1]);
/* 3153 */           if (tmpShort == 0) {
/* 3154 */             ownLogger.info("SIM_CARD_STATUS  #  SIM CARD Present");
/* 3155 */           } else if (tmpShort == 1) {
/* 3156 */             ownLogger.info("SIM_CARD_STATUS  # SIM CARD Failure  ");
/* 3157 */           } else if (tmpShort == 2) {
/* 3158 */             ownLogger.info("SIM_CARD_STATUS  # NOT TESTED  ");
/*      */           } 
/*      */           
/* 3161 */           tmpShort = (short)getIntFromHexByte(fcon[2]);
/* 3162 */           if (tmpShort == 0) {
/* 3163 */             ownLogger.info("SIM_CARD_STATUS  #  Operative");
/* 3164 */           } else if (tmpShort == 1) {
/* 3165 */             ownLogger.info("SIM_CARD_STATUS  # Jammed  ");
/*      */           } 
/*      */           
/* 3168 */           tmpShort = (short)getIntFromHexByte(fcon[3]);
/* 3169 */           if (tmpShort == 0) {
/* 3170 */             ownLogger.info("SIM_CARD_STATUS  #  JDR LOW "); break;
/* 3171 */           }  if (tmpShort == 1) {
/* 3172 */             ownLogger.info("SIM_CARD_STATUS  # JDR HIGH  ");
/*      */           }
/*      */           break;
/*      */         case 99:
/* 3176 */           tmp4[0] = fcon[0];
/* 3177 */           tmp4[1] = fcon[1];
/* 3178 */           tmp4[2] = fcon[2];
/* 3179 */           tmp4[3] = fcon[3];
/* 3180 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 3181 */           ownLogger.info("3i Locate : Received LONGTITUDE : " + dValue + " Actual Value : " + (float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */           
/* 3183 */           tmp4[0] = fcon[4];
/* 3184 */           tmp4[1] = fcon[5];
/* 3185 */           tmp4[2] = fcon[6];
/* 3186 */           tmp4[3] = fcon[7];
/* 3187 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 3188 */           ownLogger.info("3i Locate : Received LATITUDE : " + dValue + " Actual Value : " + (float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */           
/* 3190 */           tmp4[0] = fcon[8];
/* 3191 */           tmp4[1] = fcon[9];
/* 3192 */           tmp4[2] = fcon[10];
/* 3193 */           tmp4[3] = fcon[11];
/* 3194 */           dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 3195 */           ownLogger.info("3i Locate : Received Altitude : " + dValue + " Actual Value : " + (float)(dValue / 100.0D));
/*      */           break;
/*      */         
/*      */         case 100:
/* 3199 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3200 */           if (tmpShort == 0) {
/* 3201 */             ownLogger.info("TELEPHONE_LINE_COMM_TEST_STATUS  #  SUCCESS"); break;
/* 3202 */           }  if (tmpShort == 1) {
/* 3203 */             ownLogger.info("TELEPHONE_LINE_COMM_TEST_STATUS  # FAILURE");
/*      */           }
/*      */           break;
/*      */         
/*      */         case 101:
/* 3208 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3209 */           apn = (short)getIntFromHexByte(fcon[1]);
/* 3210 */           if (tmpShort == 0) {
/* 3211 */             if (apn == 0) {
/* 3212 */               ownLogger.info("ETH_COMM_TEST_STATUS  # ON BOARD #  SUCCESS"); break;
/* 3213 */             }  if (apn == 1)
/* 3214 */               ownLogger.info("ETH_COMM_TEST_STATUS  # ON BOARD #  FAILURE ");  break;
/*      */           } 
/* 3216 */           if (tmpShort == 1) {
/* 3217 */             if (apn == 0) {
/* 3218 */               ownLogger.info("ETH_COMM_TEST_STATUS  # EB COMM #  SUCCESS"); break;
/* 3219 */             }  if (apn == 1) {
/* 3220 */               ownLogger.info("ETH_COMM_TEST_STATUS  # EB COMM #  FAILURE");
/*      */             }
/*      */           } 
/*      */           break;
/*      */         case 102:
/* 3225 */           if (fcon[0] == 1) {
/* 3226 */             ownLogger.info("WIFI_MODEL : CC3000");
/*      */           }
/*      */           break;
/*      */         case 103:
/* 3230 */           ownLogger.info(" WIFI_FW :" + fcon[0] + "." + fcon[1]);
/*      */           break;
/*      */         case 104:
/* 3233 */           if (fcon[0] == 0) {
/* 3234 */             ownLogger.info("WIFI_ACCESS_POINT : Primary Access point"); break;
/* 3235 */           }  if (fcon[0] == 1) {
/* 3236 */             ownLogger.info("WIFI_ACCESS_POINT : Secondary Access Point");
/*      */           }
/*      */           break;
/*      */         
/*      */         case 105:
/* 3241 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3242 */           apn = (short)getIntFromHexByte(fcon[1]);
/* 3243 */           tmp = getIntFromHexByte(fcon[2]);
/* 3244 */           if (tmpShort == 0) {
/* 3245 */             if (tmp == 0) {
/* 3246 */               ownLogger.info("WIFI_IFACE_COMM_TEST_STATUS  # ON BOARD WITH APN :" + apn + " #  SUCCESS"); break;
/* 3247 */             }  if (tmp == 1)
/* 3248 */               ownLogger.info("WIFI_IFACE_COMM_TEST_STATUS  # ON BOARD  WITH APN :" + apn + "#  FAILURE ");  break;
/*      */           } 
/* 3250 */           if (tmpShort == 1) {
/* 3251 */             if (tmp == 0) {
/* 3252 */               ownLogger.info("WIFI_IFACE_COMM_TEST_STATUS  # EB COMM  WITH APN :" + apn + "#  SUCCESS"); break;
/* 3253 */             }  if (tmp == 1) {
/* 3254 */               ownLogger.info("WIFI_IFACE_COMM_TEST_STATUS  # EB COMM WITH APN :" + apn + " #  FAILURE");
/*      */             }
/*      */           } 
/*      */           break;
/*      */         case 106:
/* 3259 */           tmpShort = (short)getIntFromHexByte(fcon[0]);
/* 3260 */           ownLogger.info("WIFI_SIGNAL_LEVEL   : " + tmpShort);
/*      */           break;
/*      */         case 107:
/* 3263 */           tmp2[0] = fcon[2];
/* 3264 */           tmp2[1] = fcon[1];
/* 3265 */           ownLogger.info("Device timezone : " + getSignedIntFrom2ByteArray(tmp2));
/* 3266 */           ownLogger.info("Device requested Zeus Time Sync (1- Yes; 0 - No) : " + fcon[0]);
/*      */           break;
/*      */         case 109:
/* 3269 */           ownLogger.info("Dash board Buffer");
/*      */           break;
/*      */         case 110:
/* 3272 */           ownLogger.info("GRIFFON_EVENTS_LOG_UPLOAD : " + fcon[0]);
/* 3273 */           if (fcon[0] == 1) {
/* 3274 */             ownLogger.info("Events Upload Request Received"); break;
/* 3275 */           }  if (fcon[0] == 2) {
/* 3276 */             ownLogger.info("Log Upload Request Received");
/*      */           }
/*      */           break;
/*      */         case 108:
/* 3280 */           ownLogger.info("GRIFFON ALL CFG CRC32 Received");
/*      */           break;
/*      */         case 85:
/* 3283 */           tmp4[0] = fcon[3];
/* 3284 */           tmp4[1] = fcon[2];
/* 3285 */           tmp4[2] = fcon[1];
/* 3286 */           tmp4[3] = fcon[0];
/* 3287 */           ownLogger.info("Configuration File CRC-32 ::: " + getIntFrom4ByteArray(tmp4));
/*      */           break;
/*      */         
/*      */         case 111:
/* 3291 */           sb = new StringBuilder();
/* 3292 */           for (byte bb : fcon) {
/* 3293 */             sb.append(bb & 0xFF).append(' ');
/*      */           }
/* 3295 */           ownLogger.info("ZONE STATUS BUFFER : " + sb.toString());
/*      */           break;
/*      */         
/*      */         case 112:
/* 3299 */           ownLogger.info("PGM Index : " + (fcon[0] & 0xFF));
/* 3300 */           ownLogger.info("PGM Analog Value : " + (fcon[1] & 0xFF));
/* 3301 */           ownLogger.info("PGM Analog Status : " + (fcon[2] & 0xFF));
/*      */           break;
/*      */         
/*      */         case 113:
/* 3305 */           ownLogger.info("Griffon AUX Status ");
/* 3306 */           switch (fcon[0]) {
/*      */             case 1:
/* 3308 */               ownLogger.info("Aux Normal");
/*      */               break;
/*      */             case 2:
/* 3311 */               ownLogger.info("Aux Overload");
/*      */               break;
/*      */             case 3:
/* 3314 */               ownLogger.info("Aux Cut-Off");
/*      */               break;
/*      */           } 
/* 3317 */           ownLogger.info("Griffon AUX Status : Voltage : " + fcon[1] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[2]) }));
/* 3318 */           ownLogger.info("Griffon AUX Status : Charging Current : " + fcon[3] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[4]) }));
/*      */           break;
/*      */         
/*      */         case 114:
/* 3322 */           eblen = (fcon[0] & 0xFF) * 2;
/* 3323 */           for (ix = 1; ix < eblen; ix += 2) {
/* 3324 */             ownLogger.info("EB STATUS RECIVED : EB INDEX : " + fcon[idx] + " EB Status Received : " + fcon[idx + 1]);
/*      */           }
/*      */           break;
/*      */         case 115:
/* 3328 */           ownLogger.info("Griffon Temparature :" + ((fcon[0] & 0xFF) - 40));
/*      */           break;
/*      */         case 118:
/* 3331 */           ownLogger.info("GRIFFON DIGITAL PGM BUFFER Received");
/*      */           break;
/*      */         case 120:
/* 3334 */           tmp4[0] = fcon[3];
/* 3335 */           tmp4[1] = fcon[2];
/* 3336 */           tmp4[2] = fcon[1];
/* 3337 */           tmp4[3] = fcon[0];
/* 3338 */           ownLogger.info(" GRIFFON RECORDED FILES LOOKUP CRC32 Receied: " + getIntFrom4ByteArray(tmp4));
/*      */           break;
/*      */       } 
/* 3341 */       ownLogger.info("***** END OF SUB PACKET BUFFER **********");
/*      */     } 
/*      */     
/* 3344 */     ownLogger.info("****************************************************************************");
/*      */   }
/*      */ 
/*      */   
/*      */   public static void logMercuriusIncomingPacket(Logger ownLogger, byte[] buffer) throws ParseException {
/* 3349 */     ownLogger.info("***** PACKET RECEIVED *****");
/* 3350 */     int index = 0;
/* 3351 */     byte[] fid = new byte[2];
/* 3352 */     byte[] tmp2 = new byte[2];
/* 3353 */     byte[] tmp4 = new byte[4];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3362 */     StringBuilder sb = new StringBuilder(buffer.length);
/* 3363 */     for (byte b : buffer) {
/* 3364 */       sb.append(b & 0xFF).append(' ');
/*      */     }
/* 3366 */     ownLogger.info(sb.toString());
/*      */     
/* 3368 */     while (index < buffer.length && 
/* 3369 */       index + 2 <= buffer.length) {
/*      */       int hwDtls; short simNum, tmp; char[] bin; short apn; int i;
/*      */       byte[] oper;
/* 3372 */       System.arraycopy(buffer, index, fid, 0, 2);
/* 3373 */       index += 2;
/* 3374 */       fid = swapLSB2MSB(fid);
/* 3375 */       int fidVal = getIntFrom2ByteArray(fid);
/* 3376 */       if (fidVal <= 0) {
/*      */         break;
/*      */       }
/* 3379 */       short flen = (short)getIntFromHexByte(buffer[index]);
/* 3380 */       byte[] fcon = new byte[flen];
/* 3381 */       System.arraycopy(buffer, ++index, fcon, 0, flen);
/* 3382 */       ownLogger.info("***** SUB PACKET PACKET  *****");
/* 3383 */       sb = new StringBuilder(fcon.length);
/* 3384 */       for (byte b : fcon) {
/* 3385 */         sb.append(b & 0xFF).append(' ');
/*      */       }
/* 3387 */       ownLogger.info(sb.toString());
/*      */       
/* 3389 */       index += flen;
/* 3390 */       switch (fidVal) {
/*      */         case 1:
/* 3392 */           ownLogger.info("DEVICE_ID : " + getASCIIFromByteArray(fcon));
/*      */           break;
/*      */         case 2:
/* 3395 */           if ((fcon[0] & 0xFF) == 1) {
/* 3396 */             if ((fcon[2] & 0xFF) == 1) {
/* 3397 */               ownLogger.info("THIRD_PARTY_MODULES_DETAILS : GSM : Telit GE-685 - GSM Module "); break;
/* 3398 */             }  if ((fcon[2] & 0xFF) == 2) {
/* 3399 */               ownLogger.info("THIRD_PARTY_MODULES_DETAILS : GSM : Telit GE-910 - GSM Module "); break;
/* 3400 */             }  if ((fcon[2] & 0xFF) == 3) {
/* 3401 */               ownLogger.info("THIRD_PARTY_MODULES_DETAILS : GSM : Telit HE-910 - GSM Module "); break;
/* 3402 */             }  if ((fcon[2] & 0xFF) == 0)
/* 3403 */               ownLogger.info("THIRD_PARTY_MODULES_DETAILS : GSM : NONE ");  break;
/*      */           } 
/* 3405 */           if ((fcon[1] & 0xFF) == 1 && (
/* 3406 */             fcon[3] & 0xFF) == 1) {
/* 3407 */             ownLogger.info("THIRD_PARTY_MODULES_DETAILS : GPS : TELIT_JF2 - GPS Module ");
/*      */           }
/*      */           break;
/*      */         
/*      */         case 3:
/* 3412 */           sb = new StringBuilder();
/* 3413 */           sb.append(getIntFromHexByte(fcon[0])).append('.').append(getIntFromHexByte(fcon[1])).append('.').append(getIntFromHexByte(fcon[2]));
/* 3414 */           ownLogger.info("DEVICE_FIRMWARE_VERSION : " + sb.toString());
/*      */           break;
/*      */         case 4:
/* 3417 */           sb = new StringBuilder();
/* 3418 */           tmp2[0] = fcon[2];
/* 3419 */           tmp2[1] = fcon[3];
/* 3420 */           sb.append(getIntFromHexByte(fcon[0])).append(".").append(getIntFromHexByte(fcon[1])).append(".").append(getIntFrom2ByteArray(tmp2));
/* 3421 */           ownLogger.info("GSM_MODEM_FW_VERSION : " + sb.toString());
/*      */           break;
/*      */         case 5:
/* 3424 */           sb = new StringBuilder();
/* 3425 */           for (i = 0; i < flen - 1; i++) {
/* 3426 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/* 3428 */           sb.append(fcon[7] / 10);
/* 3429 */           ownLogger.info("GSM_MODULE_IMEI : " + sb.toString());
/*      */           break;
/*      */         case 6:
/* 3432 */           simNum = (short)getIntFromHexByte(fcon[0]);
/* 3433 */           sb = new StringBuilder();
/*      */           
/* 3435 */           for (i = 1; i < flen; i++) {
/* 3436 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/* 3438 */           if (simNum == 1) {
/* 3439 */             ownLogger.info("SIMCARD_ICCID For SIM #  1  ICCID : " + sb.toString()); break;
/* 3440 */           }  if (simNum == 2) {
/* 3441 */             ownLogger.info("SIMCARD_ICCID For SIM #  2  ICCID : " + sb.toString());
/*      */           }
/*      */           break;
/*      */         case 7:
/* 3445 */           simNum = (short)getIntFromHexByte(fcon[0]);
/* 3446 */           ownLogger.info("SIM_IMSI # " + simNum);
/* 3447 */           sb = new StringBuilder();
/*      */           
/* 3449 */           for (i = 1; i < flen - 1; i++) {
/* 3450 */             sb.append(getFormatIntFromHexByte(fcon[i]));
/*      */           }
/* 3452 */           sb.append(fcon[8] / 10);
/*      */           
/* 3454 */           if (simNum == 1) {
/* 3455 */             ownLogger.info("SIM_IMSI : SIM #1 :  " + sb.toString()); break;
/* 3456 */           }  if (simNum == 2) {
/* 3457 */             ownLogger.info("SIM_IMSI : SIM #2 :  " + sb.toString());
/*      */           }
/*      */           break;
/*      */         case 8:
/* 3461 */           simNum = (short)getIntFromHexByte(fcon[0]);
/* 3462 */           oper = new byte[flen - 1];
/* 3463 */           System.arraycopy(fcon, 1, oper, 0, flen - 1);
/* 3464 */           if (simNum == 1) {
/* 3465 */             ownLogger.info("SIMCARD_OPERATOR_NAME For SIM # 1: " + getASCIIFromByteArray(oper)); break;
/* 3466 */           }  if (simNum == 2) {
/* 3467 */             ownLogger.info("SIMCARD_OPERATOR_NAME For SIM # 2: " + getASCIIFromByteArray(oper));
/*      */           }
/*      */           break;
/*      */         case 9:
/* 3471 */           simNum = (short)getIntFromHexByte(fcon[0]);
/* 3472 */           ownLogger.info("GPRS_INTERFACE_TEST_STATUS # No of Sim-Card " + simNum);
/*      */           
/* 3474 */           simNum = (short)getIntFromHexByte(fcon[1]);
/* 3475 */           ownLogger.info("GPRS_INTERFACE_TEST_STATUS # No of APN " + simNum);
/*      */           
/* 3477 */           simNum = (short)getIntFromHexByte(fcon[2]);
/* 3478 */           ownLogger.info("GPRS_INTERFACE_TEST_STATUS # Status " + simNum);
/* 3479 */           if (simNum == 0) {
/* 3480 */             ownLogger.info("GPRS_INTERFACE_TEST_STATUS : Failure "); break;
/* 3481 */           }  if (simNum == 1) {
/* 3482 */             ownLogger.info("GPRS_INTERFACE_TEST_STATUS : Success ");
/*      */           }
/*      */           break;
/*      */         case 10:
/* 3486 */           simNum = (short)getIntFromHexByte(fcon[0]);
/* 3487 */           if (simNum == 1) {
/* 3488 */             ownLogger.info("CURRENT_INTERFACE # GPRS "); break;
/* 3489 */           }  if (simNum == 2) {
/* 3490 */             ownLogger.info("CURRENT_INTERFACE # CSD "); break;
/* 3491 */           }  if (simNum == 3) {
/* 3492 */             ownLogger.info("CURRENT_INTERFACE # SMS ");
/*      */           }
/*      */           break;
/*      */         
/*      */         case 11:
/* 3497 */           ownLogger.info(" CURRENT_SIMCARD_AND_APN # Number of SIM-Card Used : " + getIntFromHexByte(fcon[0]));
/* 3498 */           ownLogger.info(" CURRENT_SIMCARD_AND_APN # Number of APN Currently Used : " + getIntFromHexByte(fcon[1]));
/*      */           break;
/*      */         case 12:
/* 3501 */           ownLogger.info("GSM_SIGNAL_LEVEL # " + getIntFromHexByte(fcon[0]));
/*      */           break;
/*      */         case 14:
/* 3504 */           ownLogger.info("GPRS_TOTAL_DATA_VOLUME : " + getIntFrom4ByteArray(swapLSB2MSB4ByteArray(fcon)));
/*      */           break;
/*      */         case 15:
/* 3507 */           bin = getBinaryFromByte(fcon[0]);
/* 3508 */           if (bin[0] == '0') {
/* 3509 */             ownLogger.info("SIMCARD_STATUS : Received For SIM #0 ");
/* 3510 */           } else if (bin[0] == '1') {
/* 3511 */             ownLogger.info("SIMCARD_STATUS : Received For SIM #1 ");
/*      */           } 
/* 3513 */           sb = new StringBuilder();
/* 3514 */           sb.append(bin[1]).append(bin[2]).append(bin[3]);
/* 3515 */           simNum = (short)Integer.parseInt(sb.toString(), 2);
/*      */           
/* 3517 */           if (simNum == 0) {
/* 3518 */             ownLogger.info("SIM_CARD_STATUS # Sim Card Present ");
/* 3519 */           } else if (simNum == 1) {
/* 3520 */             ownLogger.info("SIM_CARD_STATUS # Sim Card Failure ");
/* 3521 */           } else if (simNum == 2) {
/* 3522 */             ownLogger.info("SIM_CARD_STATUS : Status # Sim Card Not Tested ");
/* 3523 */           } else if (simNum == 3) {
/* 3524 */             ownLogger.info("SIM_CARD_STATUS : Status # Registration Success ");
/*      */           } 
/*      */           
/* 3527 */           sb = new StringBuilder();
/* 3528 */           sb.append(bin[4]).append(bin[5]);
/* 3529 */           apn = (short)Integer.parseInt(sb.toString(), 2);
/*      */           
/* 3531 */           if (apn == 0) {
/* 3532 */             ownLogger.info("SIM_CARD_STATUS  # Operative ");
/* 3533 */           } else if (apn == 1) {
/* 3534 */             ownLogger.info("SIM_CARD_STATUS  # JAMMED ");
/*      */           } 
/*      */           
/* 3537 */           sb = new StringBuilder();
/* 3538 */           sb.append(bin[6]).append(bin[7]);
/* 3539 */           tmp = (short)Integer.parseInt(sb.toString(), 2);
/*      */           
/* 3541 */           if (tmp == 0) {
/* 3542 */             ownLogger.info("SIM_CARD_STATUS # JAMMER GPIO Low "); break;
/* 3543 */           }  if (tmp == 1) {
/* 3544 */             ownLogger.info("SIM_CARD_STATUS JDR  # JAMMER GPIO HIGH ");
/*      */           }
/*      */           break;
/*      */         case 16:
/* 3548 */           tmp2[0] = fcon[1];
/* 3549 */           tmp2[1] = fcon[0];
/* 3550 */           ownLogger.info("Received Time Zone : " + getSignedIntFrom2ByteArray(tmp2));
/* 3551 */           ownLogger.info(" TIMEZONE REQUIRED UPDATE: " + (fcon[2] & 0xFF));
/*      */           break;
/*      */         case 17:
/* 3554 */           ownLogger.info("GEOFENCE_CRC32 : " + getIntFrom4ByteArray(swapLSB2MSB4ByteArray(fcon)));
/*      */           break;
/*      */ 
/*      */         
/*      */         case 20:
/* 3559 */           ownLogger.info("CURRENT_SATILITES_COUNT # " + (fcon[0] & 0xFF));
/*      */           break;
/*      */         case 21:
/* 3562 */           tmp2[0] = fcon[1];
/* 3563 */           tmp2[1] = fcon[0];
/* 3564 */           hwDtls = getIntFrom2ByteArray(tmp2);
/* 3565 */           if ((hwDtls & 0x1) == 1) {
/* 3566 */             ownLogger.info("HW_DETAILS: No Battery");
/*      */           } else {
/* 3568 */             ownLogger.info("HW_DETAILS: Battery Avaliable");
/*      */           } 
/*      */           
/* 3571 */           if ((hwDtls >> 1 & 0x1) == 1) {
/* 3572 */             ownLogger.info("HW_DETAILS: No GSM");
/*      */           } else {
/* 3574 */             ownLogger.info("HW_DETAILS: GSM Available");
/*      */           } 
/*      */           
/* 3577 */           if ((hwDtls >> 2 & 0x1) == 1) {
/* 3578 */             ownLogger.info("HW_DETAILS: No GPS");
/*      */           } else {
/* 3580 */             ownLogger.info("HW_DETAILS: GPS Avaliable");
/*      */           } 
/*      */           
/* 3583 */           if ((hwDtls >> 3 & 0x1) == 1) {
/* 3584 */             ownLogger.info("HW_DETAILS: No Accelerometer");
/*      */           } else {
/* 3586 */             ownLogger.info("HW_DETAILS: Accelerometer Avaliable");
/*      */           } 
/* 3588 */           if ((hwDtls >> 4 & 0x1) == 1) {
/* 3589 */             ownLogger.info("HW_DETAILS: No Analog Input Outputs");
/*      */           } else {
/* 3591 */             ownLogger.info("HW_DETAILS: Analog Input Outputs Avaliable");
/*      */           } 
/* 3593 */           if ((hwDtls >> 5 & 0x1) == 1) {
/* 3594 */             ownLogger.info("HW_DETAILS: No Digital Inputs");
/*      */           } else {
/* 3596 */             ownLogger.info("HW_DETAILS: Digital Inputs Avaliable");
/*      */           } 
/* 3598 */           if ((hwDtls >> 6 & 0x1) == 1) {
/* 3599 */             ownLogger.info("HW_DETAILS: No Digital Outputs");
/*      */           } else {
/* 3601 */             ownLogger.info("HW_DETAILS: Digital Outputs Avaliable");
/*      */           } 
/* 3603 */           if ((hwDtls >> 7 & 0x1) == 1) {
/* 3604 */             ownLogger.info("HW_DETAILS: No Audio Outputs");
/*      */           } else {
/* 3606 */             ownLogger.info("HW_DETAILS: Audio Outputs Avaliable");
/*      */           } 
/* 3608 */           if ((hwDtls >> 8 & 0x1) == 1) {
/* 3609 */             ownLogger.info("HW_DETAILS: No Audio Inputs"); break;
/*      */           } 
/* 3611 */           ownLogger.info("HW_DETAILS: Audio Inputs Avaliable");
/*      */           break;
/*      */         
/*      */         case 100:
/* 3615 */           tmp4[0] = fcon[0];
/* 3616 */           tmp4[1] = fcon[1];
/* 3617 */           tmp4[2] = fcon[2];
/* 3618 */           tmp4[3] = fcon[3];
/* 3619 */           ownLogger.info("SCHEDULER_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3620 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 3621 */           ownLogger.info("SCHEDULER_EVENT : Received SCHEDULE : " + (simNum + 1));
/* 3622 */           if (flen == 17) {
/* 3623 */             byte[] gpsData = new byte[12];
/* 3624 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 3625 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 101:
/* 3629 */           tmp4[0] = fcon[0];
/* 3630 */           tmp4[1] = fcon[1];
/* 3631 */           tmp4[2] = fcon[2];
/* 3632 */           tmp4[3] = fcon[3];
/* 3633 */           ownLogger.info("ANALOG_INPUT_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3634 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 3635 */           ownLogger.info("ANALOG_INPUT_EVENT : Received  : " + simNum);
/* 3636 */           tmp2 = getHighLowBytes(simNum);
/* 3637 */           if (tmp2[1] == 0) {
/* 3638 */             ownLogger.info("ANALOG_INPUT_EVENT : Analog Input CH 01 ");
/* 3639 */           } else if (tmp2[1] == 1) {
/* 3640 */             ownLogger.info("ANALOG_INPUT_EVENT : Analog Input CH 02 ");
/*      */           } 
/* 3642 */           switch (tmp2[0]) {
/*      */             case 0:
/* 3644 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 01 ");
/*      */               break;
/*      */             case 1:
/* 3647 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 02 ");
/*      */               break;
/*      */             case 2:
/* 3650 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 03 ");
/*      */               break;
/*      */             case 3:
/* 3653 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 04 ");
/*      */               break;
/*      */             case 4:
/* 3656 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 05 ");
/*      */               break;
/*      */             case 5:
/* 3659 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 06 ");
/*      */               break;
/*      */             case 6:
/* 3662 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 07 ");
/*      */               break;
/*      */             case 7:
/* 3665 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 08 ");
/*      */               break;
/*      */             case 8:
/* 3668 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 09 ");
/*      */               break;
/*      */             case 9:
/* 3671 */               ownLogger.info("ANALOG_INPUT_EVENT : Analog Input 10 ");
/*      */               break;
/*      */           } 
/* 3674 */           if (flen == 17) {
/* 3675 */             byte[] gpsData = new byte[12];
/* 3676 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 3677 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 102:
/* 3681 */           tmp4[0] = fcon[0];
/* 3682 */           tmp4[1] = fcon[1];
/* 3683 */           tmp4[2] = fcon[2];
/* 3684 */           tmp4[3] = fcon[3];
/* 3685 */           ownLogger.info("DIGITAL_INPUT_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/*      */           
/* 3687 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 3688 */           ownLogger.info("DIGITAL_INPUT_EVENT : Received  : " + simNum);
/* 3689 */           apn = (short)(simNum >> 7 & 0x1);
/* 3690 */           simNum = (short)(simNum & 0xFFFFFF7F);
/*      */           
/* 3692 */           ownLogger.info("DI Value : " + simNum);
/* 3693 */           switch (simNum) {
/*      */             case 0:
/* 3695 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 01 " + apn);
/*      */               break;
/*      */             case 1:
/* 3698 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 02 " + apn);
/*      */               break;
/*      */             case 2:
/* 3701 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 03 " + apn);
/*      */               break;
/*      */             case 3:
/* 3704 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 04 " + apn);
/*      */               break;
/*      */             case 4:
/* 3707 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 05 " + apn);
/*      */               break;
/*      */             case 5:
/* 3710 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 06 " + apn);
/*      */               break;
/*      */             case 6:
/* 3713 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 07 " + apn);
/*      */               break;
/*      */             case 7:
/* 3716 */               ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input 08 ");
/*      */               break;
/*      */           } 
/*      */           
/* 3720 */           if (apn == 0) {
/* 3721 */             ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input Active ");
/* 3722 */           } else if (apn == 1) {
/* 3723 */             ownLogger.info("DIGITAL_INPUT_EVENT : Digital Input Inactive ");
/*      */           } 
/* 3725 */           if (flen == 17) {
/* 3726 */             byte[] gpsData = new byte[12];
/* 3727 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 3728 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 22:
/* 3732 */           tmp2[0] = fcon[0];
/* 3733 */           tmp2[1] = fcon[1];
/* 3734 */           simNum = (short)getIntFrom2ByteArray(tmp2);
/* 3735 */           ownLogger.info(" DI_DATA :: " + simNum);
/* 3736 */           ownLogger.info(" DI 1 : " + (simNum & 0x3));
/* 3737 */           ownLogger.info(" DI 2 : " + (simNum >> 2 & 0x3));
/* 3738 */           ownLogger.info(" DI 3 : " + (simNum >> 4 & 0x3));
/* 3739 */           ownLogger.info(" DI 4 : " + (simNum >> 6 & 0x3));
/* 3740 */           ownLogger.info(" DI 5 : " + (simNum >> 8 & 0x3));
/* 3741 */           ownLogger.info(" DI 6 : " + (simNum >> 10 & 0x3));
/* 3742 */           ownLogger.info(" DI 7 : " + (simNum >> 12 & 0x3));
/* 3743 */           ownLogger.info(" DI 8 : " + (simNum >> 14 & 0x3));
/*      */           break;
/*      */         case 103:
/* 3746 */           tmp4[0] = fcon[0];
/* 3747 */           tmp4[1] = fcon[1];
/* 3748 */           tmp4[2] = fcon[2];
/* 3749 */           tmp4[3] = fcon[3];
/* 3750 */           ownLogger.info("TRACKING_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3751 */           tmp2 = getHighLowBytes(fcon[4]);
/*      */           
/* 3753 */           if (tmp2[1] == 0) {
/* 3754 */             ownLogger.info("TRACKING_EVENT : For Tracking Server : ");
/* 3755 */           } else if (tmp2[1] == 1) {
/* 3756 */             ownLogger.info("TRACKING_EVENT : For Zeus Server : ");
/*      */           } 
/* 3758 */           switch (tmp2[0]) {
/*      */             case 0:
/* 3760 */               ownLogger.info("TRACKING_EVENT : Tracking by Distance ");
/*      */               break;
/*      */             case 1:
/* 3763 */               ownLogger.info("TRACKING_EVENT : Tracking by Time Interval Ignition ON");
/*      */               break;
/*      */             case 2:
/* 3766 */               ownLogger.info("TRACKING_EVENT : Tracking by Time Interval Ignition OFF  ");
/*      */               break;
/*      */             case 3:
/* 3769 */               ownLogger.info("TRACKING_EVENT : Tracking by Demand ");
/*      */               break;
/*      */             case 4:
/* 3772 */               ownLogger.info("TRACKING_EVENT : Max SMS Sent on Ignition ON ");
/*      */               break;
/*      */             case 5:
/* 3775 */               ownLogger.info("TRACKING_EVENT : Max SMS Sent on Ignition OFF");
/*      */               break;
/*      */             case 6:
/* 3778 */               ownLogger.info("TRACKING_EVENT :  Log Over Flow ");
/*      */               break;
/*      */             case 7:
/* 3781 */               ownLogger.info("TRACKING_EVENT : Max GPRS Reports per cycle  for Ignition On");
/*      */               break;
/*      */             case 8:
/* 3784 */               ownLogger.info("TRACKING_EVENT : Max GPRS Reports per cycle  for Ignition Off");
/*      */               break;
/*      */             case 9:
/* 3787 */               ownLogger.info("TRACKING_EVENT : Max SMS Reports per cycle  for Ignition On");
/*      */               break;
/*      */             case 10:
/* 3790 */               ownLogger.info("TRACKING_EVENT : Max SMS Reports per cycle  for Ignition Off");
/*      */               break;
/*      */           } 
/* 3793 */           if (flen == 17) {
/* 3794 */             byte[] gpsData = new byte[12];
/* 3795 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 3796 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 104:
/* 3800 */           tmp4[0] = fcon[0];
/* 3801 */           tmp4[1] = fcon[1];
/* 3802 */           tmp4[2] = fcon[2];
/* 3803 */           tmp4[3] = fcon[3];
/*      */           
/* 3805 */           ownLogger.info("VEHICLE_SAFETY_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3806 */           simNum = (short)getIntFromHexByte(fcon[4]);
/*      */           
/* 3808 */           switch (simNum) {
/*      */             case 0:
/* 3810 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Speed Limit Violation");
/*      */               break;
/*      */             case 1:
/* 3813 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Speed Limit Restore");
/*      */               break;
/*      */             case 2:
/* 3816 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Towing Alert ");
/*      */               break;
/*      */             case 3:
/* 3819 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Heading Change Alert");
/*      */               break;
/*      */             case 4:
/* 3822 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Idle Alert");
/*      */               break;
/*      */             case 5:
/* 3825 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Fuel theft Alert");
/*      */               break;
/*      */             case 6:
/* 3828 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Parking Alert");
/*      */               break;
/*      */             case 7:
/* 3831 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Ignition ON");
/*      */               break;
/*      */             case 8:
/* 3834 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Ignition OFF");
/*      */               break;
/*      */             case 9:
/* 3837 */               ownLogger.info("VEHICLE_SAFETY_EVENT : High Deceleration");
/*      */               break;
/*      */             case 10:
/* 3840 */               ownLogger.info("VEHICLE_SAFETY_EVENT : High Acceleration");
/*      */               break;
/*      */             case 11:
/* 3843 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Shock Detection");
/*      */               break;
/*      */             case 12:
/* 3846 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Maintenance Alert on Distance");
/*      */               break;
/*      */             case 13:
/* 3849 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Maintenance Alert on Running Time");
/*      */               break;
/*      */             case 14:
/* 3852 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Fatigue Driving Alarm");
/*      */               break;
/*      */             case 15:
/* 3855 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Over Rest After Fatigue Driving Alarm");
/*      */               break;
/*      */             case 16:
/* 3858 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Driving Status Alert");
/*      */               break;
/*      */             case 17:
/* 3861 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Collision Detection Alert");
/*      */               break;
/*      */             case 18:
/* 3864 */               ownLogger.info("VEHICLE_SAFETY_EVENT : Low Fuel Alert"); break;
/*      */           } 
/* 3866 */           if (flen == 17) {
/* 3867 */             byte[] gpsData = new byte[12];
/* 3868 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 3869 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 105:
/* 3873 */           tmp4[0] = fcon[0];
/* 3874 */           tmp4[1] = fcon[1];
/* 3875 */           tmp4[2] = fcon[2];
/* 3876 */           tmp4[3] = fcon[3];
/*      */           
/* 3878 */           ownLogger.info("ROUTE_PATH_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3879 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 3880 */           ownLogger.info("Received Route SUB ID  Data : " + simNum);
/* 3881 */           switch (simNum) {
/*      */             case 0:
/* 3883 */               ownLogger.info("ROUTE_PATH_EVENT : Route IN");
/*      */               break;
/*      */             case 1:
/* 3886 */               ownLogger.info("ROUTE_PATH_EVENT : Route OUT");
/*      */               break;
/*      */             case 2:
/* 3889 */               ownLogger.info("ROUTE_PATH_EVENT : Route Path Monitor Started");
/*      */               break;
/*      */             case 3:
/* 3892 */               ownLogger.info("ROUTE_PATH_EVENT : Route Path Monitor Aborted");
/*      */               break;
/*      */             case 4:
/* 3895 */               ownLogger.info("ROUTE_PATH_EVENT : Route Completed");
/*      */               break;
/*      */             case 5:
/* 3898 */               ownLogger.info("ROUTE_PATH_EVENT : Route Path Early Reach");
/*      */               break;
/*      */             case 6:
/* 3901 */               ownLogger.info("ROUTE_PATH_EVENT : Route Path Timely Reach");
/*      */               break;
/*      */             case 7:
/* 3904 */               ownLogger.info("ROUTE_PATH_EVENT : Route Path Delayed Reach");
/*      */               break;
/*      */             case 8:
/* 3907 */               ownLogger.info("ROUTE_PATH_EVENT : Route Path Violation");
/*      */               break;
/*      */             case 9:
/* 3910 */               ownLogger.info("ROUTE_PATH_EVENT : Check Point Not Verified");
/*      */               break;
/*      */           } 
/* 3913 */           tmp = (short)getIntFromHexByte(fcon[5]);
/* 3914 */           ownLogger.info("ROUTE_PATH_EVENT : Route Path No: " + tmp);
/* 3915 */           tmp2[0] = fcon[7];
/* 3916 */           tmp2[1] = fcon[6];
/* 3917 */           simNum = (short)getIntFrom2ByteArray(tmp2);
/* 3918 */           ownLogger.info("ROUTE_PATH_EVENT : Route Point No: " + simNum);
/* 3919 */           if (flen == 20) {
/* 3920 */             byte[] gpsData = new byte[12];
/* 3921 */             System.arraycopy(fcon, 8, gpsData, 0, 12);
/* 3922 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 106:
/* 3926 */           tmp4[0] = fcon[0];
/* 3927 */           tmp4[1] = fcon[1];
/* 3928 */           tmp4[2] = fcon[2];
/* 3929 */           tmp4[3] = fcon[3];
/* 3930 */           ownLogger.info("GEOFENCE_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3931 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 3932 */           ownLogger.info("GEOFENCE_EVENT : Received  : " + simNum);
/* 3933 */           tmp = (short)(simNum >> 7 & 0x1);
/* 3934 */           if (tmp == 1) {
/* 3935 */             simNum = (short)(simNum ^ 0x80);
/*      */           }
/* 3937 */           switch (simNum) {
/*      */             case 0:
/* 3939 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 01 ");
/*      */               break;
/*      */             case 1:
/* 3942 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 02 ");
/*      */               break;
/*      */             case 2:
/* 3945 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 03 ");
/*      */               break;
/*      */             case 3:
/* 3948 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 04 ");
/*      */               break;
/*      */             case 4:
/* 3951 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 05 ");
/*      */               break;
/*      */             case 5:
/* 3954 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 06 ");
/*      */               break;
/*      */             case 6:
/* 3957 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 07 ");
/*      */               break;
/*      */             case 7:
/* 3960 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 08 ");
/*      */               break;
/*      */             case 8:
/* 3963 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 09 ");
/*      */               break;
/*      */             case 9:
/* 3966 */               ownLogger.info("GEOFENCE_EVENT : Geo Fence 10 ");
/*      */               break;
/*      */           } 
/* 3969 */           if (tmp == 0) {
/* 3970 */             ownLogger.info("GEOFENCE_EVENT : Geo Fence Violation ");
/* 3971 */           } else if (tmp == 1) {
/* 3972 */             ownLogger.info("GEOFENCE_EVENT : Geo Fence Restore ");
/*      */           } 
/* 3974 */           if (flen == 17) {
/* 3975 */             byte[] gpsData = new byte[12];
/* 3976 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 3977 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 107:
/* 3981 */           tmp4[0] = fcon[0];
/* 3982 */           tmp4[1] = fcon[1];
/* 3983 */           tmp4[2] = fcon[2];
/* 3984 */           tmp4[3] = fcon[3];
/*      */           
/* 3986 */           ownLogger.info("HARDWARE_FAILURE_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 3987 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 3988 */           ownLogger.info("HARDWARE_FAILURE_EVENT : Received  Hardware Failure: " + simNum);
/* 3989 */           tmp2 = getHighLowBytes(fcon[4]);
/* 3990 */           tmp = (short)(tmp2[0] & 0xFF);
/*      */           
/* 3992 */           switch (tmp) {
/*      */             case 0:
/* 3994 */               ownLogger.info("HARDWARE_FAILURE_EVENT : GPS ");
/*      */               break;
/*      */             case 1:
/* 3997 */               ownLogger.info("HARDWARE_FAILURE_EVENT : GSM ");
/*      */               break;
/*      */             case 2:
/* 4000 */               ownLogger.info("HARDWARE_FAILURE_EVENT : Fuel Gauge  ");
/*      */               break;
/*      */             case 3:
/* 4003 */               ownLogger.info("HARDWARE_FAILURE_EVENT : Accelerometer ");
/*      */               break;
/*      */             case 4:
/* 4006 */               ownLogger.info("HARDWARE_FAILURE_EVENT : SIM 1");
/*      */               break;
/*      */             case 5:
/* 4009 */               ownLogger.info("HARDWARE_FAILURE_EVENT : SIM 2 ");
/*      */               break;
/*      */             case 6:
/* 4012 */               ownLogger.info("HARDWARE_FAILURE_EVENT : SD Card ");
/*      */               break;
/*      */           } 
/*      */           
/* 4016 */           switch (tmp2[1] & 0xFF) {
/*      */             case 0:
/* 4018 */               ownLogger.info("Failure Detected ");
/*      */               break;
/*      */             case 1:
/* 4021 */               ownLogger.info("Failure Restored ");
/*      */               break;
/*      */             case 2:
/* 4024 */               ownLogger.info("Antenna Cut Detected (GPS)  ");
/*      */               break;
/*      */             case 3:
/* 4027 */               ownLogger.info("Antenna Sort Detected(GPS)");
/*      */               break;
/*      */             case 4:
/* 4030 */               ownLogger.info("Antenna Normal (GPS)");
/*      */               break;
/*      */           } 
/*      */           
/* 4034 */           if (flen == 17) {
/* 4035 */             byte[] gpsData = new byte[12];
/* 4036 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 4037 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */ 
/*      */         
/*      */         case 108:
/* 4043 */           tmp4[0] = fcon[0];
/* 4044 */           tmp4[1] = fcon[1];
/* 4045 */           tmp4[2] = fcon[2];
/* 4046 */           tmp4[3] = fcon[3];
/* 4047 */           ownLogger.info("POWER_STATUS_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 4048 */           simNum = (short)getIntFromHexByte(fcon[4]);
/*      */           
/* 4050 */           switch (simNum) {
/*      */             case 0:
/* 4052 */               ownLogger.info("POWER_STATUS_EVENT : Low battery Alert");
/*      */               break;
/*      */             case 1:
/* 4055 */               ownLogger.info("POWER_STATUS_EVENT : Low battery Restore");
/*      */               break;
/*      */             case 2:
/* 4058 */               ownLogger.info("POWER_STATUS_EVENT : Input Power Disconnected");
/*      */               break;
/*      */             case 3:
/* 4061 */               ownLogger.info("POWER_STATUS_EVENT : Input Power Low");
/*      */               break;
/*      */             case 4:
/* 4064 */               ownLogger.info("POWER_STATUS_EVENT : Input Power Normal");
/*      */               break;
/*      */           } 
/* 4067 */           if (flen == 17) {
/* 4068 */             byte[] gpsData = new byte[12];
/* 4069 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 4070 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 109:
/* 4074 */           tmp4[0] = fcon[0];
/* 4075 */           tmp4[1] = fcon[1];
/* 4076 */           tmp4[2] = fcon[2];
/* 4077 */           tmp4[3] = fcon[3];
/*      */           
/* 4079 */           ownLogger.info("GPS_STATUS_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 4080 */           simNum = (short)getIntFromHexByte(fcon[4]);
/*      */           
/* 4082 */           switch (simNum) {
/*      */             case 0:
/* 4084 */               ownLogger.info("GPS_STATUS_EVENT : GPS Position Error Violation");
/*      */               break;
/*      */             case 1:
/* 4087 */               ownLogger.info("GPS_STATUS_EVENT : GPS Position Error Restored");
/*      */               break;
/*      */             case 2:
/* 4090 */               ownLogger.info("GPS_STATUS_EVENT : GPS Signal Lost ");
/*      */               break;
/*      */             case 3:
/* 4093 */               ownLogger.info("GPS_STATUS_EVENT : GPS Restored");
/*      */               break;
/*      */           } 
/* 4096 */           if (flen == 17) {
/* 4097 */             byte[] gpsData = new byte[12];
/* 4098 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 4099 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 110:
/* 4103 */           tmp4[0] = fcon[0];
/* 4104 */           tmp4[1] = fcon[1];
/* 4105 */           tmp4[2] = fcon[2];
/* 4106 */           tmp4[3] = fcon[3];
/*      */           
/* 4108 */           ownLogger.info("TAMPER_STATUS_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 4109 */           simNum = (short)getIntFromHexByte(fcon[4]);
/*      */           
/* 4111 */           if (simNum == 0) {
/* 4112 */             ownLogger.info("TAMPER_STATUS_EVENT : Tamper Alert Detected");
/* 4113 */           } else if (simNum == 1) {
/* 4114 */             ownLogger.info("TAMPER_STATUS_EVENT : Tamper Alert Restored");
/*      */           } 
/* 4116 */           if (flen == 17) {
/* 4117 */             byte[] gpsData = new byte[12];
/* 4118 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 4119 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 18:
/* 4123 */           ownLogger.info("Received PGM Status : Byte 1 : " + fcon[0]);
/* 4124 */           ownLogger.info("Received PGM status : Byte 2 : " + fcon[1]);
/* 4125 */           ownLogger.info("Received PGM status : Byte 3 : " + fcon[2]);
/* 4126 */           tmp2 = getHighLowBytes(fcon[0]);
/* 4127 */           ownLogger.info("DO 1 : " + tmp2[0]);
/* 4128 */           ownLogger.info("DO 2 : " + tmp2[1]);
/* 4129 */           tmp2 = getHighLowBytes(fcon[1]);
/* 4130 */           ownLogger.info("DO 3 : " + tmp2[0]);
/* 4131 */           ownLogger.info("DO 4 : " + tmp2[1]);
/* 4132 */           tmp2 = getHighLowBytes(fcon[2]);
/* 4133 */           ownLogger.info("DO 5 : " + tmp2[0]);
/*      */           break;
/*      */         case 112:
/* 4136 */           tmp4[0] = fcon[0];
/* 4137 */           tmp4[1] = fcon[1];
/* 4138 */           tmp4[2] = fcon[2];
/* 4139 */           tmp4[3] = fcon[3];
/* 4140 */           ownLogger.info("JAMMER_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/*      */           
/* 4142 */           simNum = (short)getIntFromHexByte(fcon[4]);
/* 4143 */           ownLogger.info("GSM SIGNAL STATUS : Received Data : " + simNum);
/*      */           
/* 4145 */           switch (simNum) {
/*      */             case 0:
/* 4147 */               ownLogger.info("GSM SIGNAL STATUS : : Low Gsm signal Alert");
/*      */               break;
/*      */             case 1:
/* 4150 */               ownLogger.info("GSM SIGNAL STATUS : : gsm signal jammed alert ");
/*      */               break;
/*      */             case 2:
/* 4153 */               ownLogger.info("GSM SIGNAL STATUS : : gsm signal normal ");
/*      */               break;
/*      */           } 
/*      */           
/* 4157 */           if (flen == 19) {
/* 4158 */             byte[] gpsData = new byte[12];
/* 4159 */             System.arraycopy(fcon, 7, gpsData, 0, 12);
/* 4160 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 113:
/* 4164 */           tmp4[0] = fcon[0];
/* 4165 */           tmp4[1] = fcon[1];
/* 4166 */           tmp4[2] = fcon[2];
/* 4167 */           tmp4[3] = fcon[3];
/* 4168 */           ownLogger.info("INCOMING_CALL_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/*      */           
/* 4170 */           switch (getIntFromHexByte(fcon[4])) {
/*      */             case 0:
/* 4172 */               ownLogger.info("INCOMING_CALL_EVENT : E_INCOMING_CALL ");
/*      */               break;
/*      */             case 1:
/* 4175 */               ownLogger.info("INCOMING_CALL_EVENT : E_REJECT_INCOMING_CALL ");
/*      */               break;
/*      */             case 2:
/* 4178 */               ownLogger.info("INCOMING_CALL_EVENT : CALL_AUTO_ANSWERED ");
/*      */               break;
/*      */             case 3:
/* 4181 */               ownLogger.info("INCOMING_CALL_EVENT : CALL_MANUAL_ANSWERED ");
/*      */               break;
/*      */             case 4:
/* 4184 */               ownLogger.info("INCOMING_CALL_EVENT : CALL_TWO_WAY_COMMUNICATION ");
/*      */               break;
/*      */             case 5:
/* 4187 */               ownLogger.info("INCOMING_CALL_EVENT : CALL_DISCRETE_MODE ");
/*      */               break;
/*      */             case 6:
/* 4190 */               ownLogger.info("INCOMING_CALL_EVENT : CALL_DISC_TERMINATED ");
/*      */               break;
/*      */           } 
/*      */           
/* 4194 */           if (flen == 17) {
/* 4195 */             byte[] gpsData = new byte[12];
/* 4196 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 4197 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */         case 114:
/* 4201 */           tmp4[0] = fcon[0];
/* 4202 */           tmp4[1] = fcon[1];
/* 4203 */           tmp4[2] = fcon[2];
/* 4204 */           tmp4[3] = fcon[3];
/*      */           
/* 4206 */           ownLogger.info("INCOMING_SMS_EVENT : Received Date&Time : " + getDateFromInt(getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4))));
/* 4207 */           switch (getIntFromHexByte(fcon[4])) {
/*      */             case 0:
/* 4209 */               ownLogger.info("INCOMING_CALL_EVENT : INCOMING SMS ");
/*      */               break;
/*      */             case 1:
/* 4212 */               ownLogger.info("INCOMING_CALL_EVENT : SMS_REJECTED ");
/*      */               break;
/*      */             case 2:
/* 4215 */               ownLogger.info("INCOMING_CALL_EVENT : SMS_CONFIGURATION ");
/*      */               break;
/*      */             case 3:
/* 4218 */               ownLogger.info("INCOMING_CALL_EVENT : SMS_COMMAND ");
/*      */               break;
/*      */             case 4:
/* 4221 */               ownLogger.info("INCOMING_CALL_EVENT : SMS_ECHO ");
/*      */               break;
/*      */           } 
/*      */           
/* 4225 */           if (flen == 17) {
/* 4226 */             byte[] gpsData = new byte[12];
/* 4227 */             System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 4228 */             parseGpsData(gpsData, ownLogger);
/*      */           } 
/*      */           break;
/*      */       } 
/* 4232 */       ownLogger.info("***** END OF SUB PACKET BUFFER **********");
/*      */     } 
/*      */     
/* 4235 */     ownLogger.info("****************************************************************************");
/*      */   }
/*      */ 
/*      */   
/*      */   public static void parseGpsData(byte[] gpsData, Logger ownLogger) {
/* 4240 */     byte[] tmp4 = new byte[4];
/* 4241 */     byte[] tmp2 = new byte[2];
/*      */     
/* 4243 */     ownLogger.info("   *********************** Start of GPS Data ******************************   ");
/* 4244 */     tmp4[0] = gpsData[0];
/* 4245 */     tmp4[1] = gpsData[1];
/* 4246 */     tmp4[2] = gpsData[2];
/* 4247 */     tmp4[3] = gpsData[3];
/* 4248 */     long dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/*      */     
/* 4250 */     ownLogger.info(" Received Latitude : " + dValue + " Actual Value : " + (float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */     
/* 4252 */     tmp4[0] = gpsData[4];
/* 4253 */     tmp4[1] = gpsData[5];
/* 4254 */     tmp4[2] = gpsData[6];
/* 4255 */     tmp4[3] = gpsData[7];
/* 4256 */     dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 4257 */     ownLogger.info(" Received Longitude : " + dValue + " Actual Value : " + (float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */     
/* 4259 */     tmp2[1] = gpsData[8];
/* 4260 */     tmp2[0] = gpsData[9];
/* 4261 */     dValue = getIntFrom2ByteArray(tmp2);
/* 4262 */     ownLogger.info(" Received Altitude : " + dValue + " Actual Value : " + (float)(dValue / 100.0D));
/*      */     
/* 4264 */     tmp2[1] = gpsData[10];
/* 4265 */     tmp2[0] = gpsData[11];
/* 4266 */     dValue = getIntFrom2ByteArray(tmp2);
/* 4267 */     ownLogger.info(" Received Speed : " + dValue + " Speed : " + (float)(dValue / 100.0D));
/*      */   }
/*      */   
/*      */   public static void parseGpsData(byte[] gpsData, MercuriusAVLModule module) {
/* 4271 */     byte[] tmp4 = new byte[4];
/* 4272 */     byte[] tmp2 = new byte[2];
/*      */     
/* 4274 */     module.setIsGpsDataReceived(true);
/* 4275 */     tmp4[0] = gpsData[0];
/* 4276 */     tmp4[1] = gpsData[1];
/* 4277 */     tmp4[2] = gpsData[2];
/* 4278 */     tmp4[3] = gpsData[3];
/* 4279 */     long dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 4280 */     module.setLattitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */     
/* 4282 */     tmp4[0] = gpsData[4];
/* 4283 */     tmp4[1] = gpsData[5];
/* 4284 */     tmp4[2] = gpsData[6];
/* 4285 */     tmp4[3] = gpsData[7];
/* 4286 */     dValue = getIntFrom4ByteArray(swapLSB2MSB4ByteArray(tmp4));
/* 4287 */     module.setLongtitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */     
/* 4289 */     tmp2[1] = gpsData[8];
/* 4290 */     tmp2[0] = gpsData[9];
/* 4291 */     dValue = getIntFrom2ByteArray(tmp2);
/* 4292 */     module.setAltitude((float)(dValue / 100.0D));
/*      */     
/* 4294 */     tmp2[1] = gpsData[10];
/* 4295 */     tmp2[0] = gpsData[11];
/* 4296 */     dValue = getIntFrom2ByteArray(tmp2);
/* 4297 */     module.setSpeed((float)(dValue / 100.0D));
/*      */   }
/*      */   
/*      */   public static String getDataServerIP() {
/*      */     try {
/* 4302 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 4303 */         if (ZeusServerCfg.getInstance().getDataServerIP().equalsIgnoreCase("any")) {
/* 4304 */           return "127.0.0.1";
/*      */         }
/* 4306 */         Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
/* 4307 */         for (NetworkInterface ni : Collections.<NetworkInterface>list(e)) {
/* 4308 */           if (ni.getName().equalsIgnoreCase(ZeusServerCfg.getInstance().getDataServerIP())) {
/* 4309 */             Enumeration<InetAddress> iddrs = ni.getInetAddresses();
/* 4310 */             for (InetAddress addr : Collections.<InetAddress>list(iddrs)) {
/* 4311 */               if (addr instanceof java.net.Inet4Address) {
/* 4312 */                 return addr.toString().substring(1);
/*      */               }
/*      */             } 
/*      */           } 
/*      */         } 
/*      */         
/* 4318 */         return "127.0.0.1";
/*      */       } 
/*      */       
/* 4321 */       return ZeusServerCfg.getInstance().getDataServerIP();
/*      */     }
/* 4323 */     catch (SocketException ex) {
/* 4324 */       ex.printStackTrace();
/*      */       
/* 4326 */       return ZeusServerCfg.getInstance().getDataServerIP();
/*      */     } 
/*      */   }
/*      */   public static String getMsgServerIP() {
/*      */     try {
/* 4331 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 4332 */         if (ZeusServerCfg.getInstance().getMsgServerIP().equalsIgnoreCase("any")) {
/* 4333 */           return "0.0.0.0";
/*      */         }
/* 4335 */         Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
/* 4336 */         for (NetworkInterface ni : Collections.<NetworkInterface>list(e)) {
/* 4337 */           if (ni.getName().equalsIgnoreCase(ZeusServerCfg.getInstance().getMsgServerIP())) {
/* 4338 */             Enumeration<InetAddress> iddrs = ni.getInetAddresses();
/* 4339 */             for (InetAddress addr : Collections.<InetAddress>list(iddrs)) {
/* 4340 */               if (addr instanceof java.net.Inet4Address) {
/* 4341 */                 return addr.toString().substring(1);
/*      */               }
/*      */             } 
/*      */           } 
/*      */         } 
/*      */         
/* 4347 */         return "0.0.0.0";
/*      */       } 
/*      */       
/* 4350 */       return ZeusServerCfg.getInstance().getMsgServerIP();
/*      */     }
/* 4352 */     catch (SocketException ex) {
/* 4353 */       ex.printStackTrace();
/*      */       
/* 4355 */       return ZeusServerCfg.getInstance().getMsgServerIP();
/*      */     } 
/*      */   }
/*      */   public static String getUdpServerIP() {
/*      */     try {
/* 4360 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 4361 */         if (ZeusServerCfg.getInstance().getUdpDataServerIP().equalsIgnoreCase("any")) {
/* 4362 */           return "127.0.0.1";
/*      */         }
/* 4364 */         Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
/* 4365 */         for (NetworkInterface ni : Collections.<NetworkInterface>list(e)) {
/* 4366 */           if (ni.getName().equalsIgnoreCase(ZeusServerCfg.getInstance().getUdpDataServerIP())) {
/* 4367 */             Enumeration<InetAddress> iddrs = ni.getInetAddresses();
/* 4368 */             for (InetAddress addr : Collections.<InetAddress>list(iddrs)) {
/* 4369 */               if (addr instanceof java.net.Inet4Address) {
/* 4370 */                 return addr.toString().substring(1);
/*      */               }
/*      */             } 
/*      */           } 
/*      */         } 
/*      */         
/* 4376 */         return "127.0.0.1";
/*      */       } 
/*      */       
/* 4379 */       return ZeusServerCfg.getInstance().getUdpDataServerIP();
/*      */     }
/* 4381 */     catch (SocketException ex) {
/* 4382 */       ex.printStackTrace();
/*      */       
/* 4384 */       return ZeusServerCfg.getInstance().getUdpDataServerIP();
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static short covertDigit2Percent(short value) {
/* 4394 */     int maxval = -50;
/* 4395 */     int minval = -100;
/* 4396 */     int percentage = 0;
/* 4397 */     if (value > maxval) {
/* 4398 */       percentage = 100;
/* 4399 */     } else if (value < minval) {
/* 4400 */       percentage = 0;
/*      */     } else {
/* 4402 */       percentage = 200 - value * 100 / maxval;
/*      */     } 
/* 4404 */     return (short)percentage;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public static void copyFolder(File src, File dest) throws IOException {
/* 4410 */     if (src.isDirectory()) {
/*      */       
/* 4412 */       if (!dest.exists()) {
/* 4413 */         dest.mkdir();
/*      */       }
/*      */ 
/*      */       
/* 4417 */       String[] files = src.list();
/*      */       
/* 4419 */       for (String file : files)
/*      */       {
/* 4421 */         File srcFile = new File(src, file);
/* 4422 */         File destFile = new File(dest, file);
/*      */         
/* 4424 */         copyFolder(srcFile, destFile);
/*      */       }
/*      */     
/*      */     }
/*      */     else {
/*      */       
/* 4430 */       InputStream in = new FileInputStream(src);
/* 4431 */       OutputStream out = new FileOutputStream(dest);
/*      */       
/* 4433 */       byte[] buffer = new byte[1024];
/*      */       
/*      */       int length;
/*      */       
/* 4437 */       while ((length = in.read(buffer)) > 0) {
/* 4438 */         out.write(buffer, 0, length);
/*      */       }
/*      */       
/* 4441 */       in.close();
/* 4442 */       out.close();
/*      */     } 
/*      */   }
/*      */   
/*      */   public static void deleteFolder(File file) throws IOException {
/* 4447 */     if (file.isDirectory()) {
/*      */       
/* 4449 */       if ((file.list()).length == 0) {
/* 4450 */         file.delete();
/*      */       } else {
/*      */         
/* 4453 */         String[] files = file.list();
/* 4454 */         for (String temp : files) {
/*      */           
/* 4456 */           File fileDelete = new File(file, temp);
/*      */ 
/*      */           
/* 4459 */           deleteFolder(fileDelete);
/*      */         } 
/*      */ 
/*      */         
/* 4463 */         if ((file.list()).length == 0) {
/* 4464 */           file.delete();
/*      */         }
/*      */       }
/*      */     
/*      */     } else {
/*      */       
/* 4470 */       file.delete();
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public static boolean createFolder(String path) throws IOException {
/* 4476 */     File theDir = new File(path);
/* 4477 */     if (!theDir.exists())
/*      */       try {
/* 4479 */         theDir.mkdirs();
/* 4480 */         return true;
/*      */       }
/* 4482 */       catch (SecurityException se) {
/* 4483 */         Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, (String)null, se);
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 4488 */         return false;
/*      */       }  
/*      */     return true;
/*      */   } public static long hoursToMillis(long hrs) {
/* 4492 */     return TimeUnit.MILLISECONDS.convert(hrs, TimeUnit.HOURS);
/*      */   }
/*      */   
/*      */   public static boolean isLastCleanupWasBefore24Hr() {
/* 4496 */     return (System.currentTimeMillis() - GlobalVariables.lastDbCleanup > hoursToMillis(24L));
/*      */   }
/*      */   
/*      */   public static void updateRefDate(String fName, String newRefDate) {
/*      */     try {
/* 4501 */       DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
/* 4502 */       Document doc = db.parse(new File(fName));
/* 4503 */       Element element = doc.getDocumentElement();
/* 4504 */       Attr attr = doc.createAttribute("refDate");
/* 4505 */       attr.setValue(newRefDate);
/* 4506 */       element.setAttributeNode(attr);
/* 4507 */       Transformer trans = TransformerFactory.newInstance().newTransformer();
/* 4508 */       trans.setOutputProperty("indent", "yes");
/* 4509 */       trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
/* 4510 */       trans.setOutputProperty("omit-xml-declaration", "yes");
/* 4511 */       trans.transform(new DOMSource(doc), new StreamResult(fName));
/*      */     }
/* 4513 */     catch (IOException|IllegalArgumentException|javax.xml.parsers.ParserConfigurationException|javax.xml.transform.TransformerException|org.w3c.dom.DOMException|org.xml.sax.SAXException ex) {
/* 4514 */       ex.printStackTrace();
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\Functions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */