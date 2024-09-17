/*      */ package com.zeusServer.pegasus;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.PegasusDBManager;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiver;
/*      */ import com.zeusServer.socket.communication.UDPDataServer;
/*      */ import com.zeusServer.socket.communication.UdpV2Handler;
/*      */ import com.zeusServer.tblConnections.TblActiveUdpConnections;
/*      */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblThreadsEmulaReceivers;
/*      */ import com.zeusServer.util.CRC16;
/*      */ import com.zeusServer.util.CRC32;
/*      */ import com.zeusServer.util.Defines;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.InfoModule;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.MonitoringInfo;
/*      */ import com.zeusServer.util.Rijndael;
/*      */ import com.zeusServer.util.UDPFunctions;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import com.zeuscc.pegasus.derby.beans.SP_V2_001_VO;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.net.DatagramPacket;
/*      */ import java.net.DatagramSocket;
/*      */ import java.net.InetAddress;
/*      */ import java.nio.ByteBuffer;
/*      */ import java.nio.channels.FileChannel;
/*      */ import java.security.InvalidAlgorithmParameterException;
/*      */ import java.security.InvalidKeyException;
/*      */ import java.security.NoSuchAlgorithmException;
/*      */ import java.sql.SQLException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.TimeZone;
/*      */ import java.util.UUID;
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
/*      */ public class PegasusUdpV2Handler
/*      */   extends UdpV2Handler
/*      */ {
/*   64 */   DatagramPacket outPacket = null;
/*      */   private DatagramSocket socket;
/*      */   private DatagramPacket inPacket;
/*   67 */   private String myThreadGuid = UUID.randomUUID().toString();
/*      */   
/*      */   private String remoteIP;
/*      */   
/*      */   private UdpV2Handler currInsance;
/*      */   
/*      */   private short encType;
/*      */   private List<SP_024DataHolder> cmdsList;
/*      */   private int cmdIndex;
/*      */   private boolean expectedByteFlag;
/*      */   private boolean readFileFlag;
/*      */   private boolean sendFileFlag;
/*      */   private boolean fileSendingFlag;
/*      */   private boolean waitingNewCommandReply;
/*      */   private boolean waitingCommandPacketResponse;
/*      */   private boolean commandModeActivated;
/*      */   private int sentCommand;
/*   84 */   int blockIndex = 0;
/*   85 */   int maxReadLength = 240;
/*      */   int blockLength;
/*   87 */   short maxRetries = 3;
/*   88 */   short retry = 0;
/*   89 */   int flen = 0;
/*      */   int rcvBlockIndex;
/*   91 */   int expBlockIndex = 0;
/*   92 */   int recvCfgCRC32 = 0;
/*   93 */   int fileContentIndex = 0;
/*   94 */   byte[] block = null;
/*   95 */   byte[] bid = new byte[2];
/*      */   byte[] packet;
/*      */   byte[] ftmp;
/*      */   byte[] tmp;
/*   99 */   byte[] fileContent = null;
/*      */   String filePath;
/*  101 */   FileChannel fc = null;
/*      */   ByteBuffer blockBuf;
/*      */   SP_024DataHolder sp24DH;
/*  104 */   File file = null;
/*  105 */   long nextUpdateFieldLastCommunication = 0L;
/*  106 */   int responseTimeout = 120000;
/*      */   
/*      */   long packetSentTime;
/*      */   boolean waitingForResponse;
/*      */   long iTimeout;
/*      */   int newCMDCheck;
/*      */   int m2sPacketReceived;
/*      */   int recevedTimeZone;
/*      */   
/*      */   public PegasusUdpV2Handler(DatagramSocket socket, DatagramPacket inPacket, short encType) {
/*  116 */     this.socket = socket;
/*  117 */     this.inPacket = inPacket;
/*  118 */     this.encType = encType;
/*  119 */     this.remoteIP = this.inPacket.getAddress().toString() + ":" + this.inPacket.getPort();
/*  120 */     this.remoteIP = this.remoteIP.substring(1);
/*  121 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  122 */     this.iTimeout = this.idleTimeout;
/*  123 */     this.currInsance = this;
/*  124 */     Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ 
/*      */   
/*      */   public void processM2SPacket(byte[] data) {
/*  129 */     this.waitingForResponse = false;
/*  130 */     this.lastCommunicationTime = System.currentTimeMillis();
/*  131 */     UDPDataServer.clientHelper.execute(new PegasusUdpClientHandler(data));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void processM2SPacket(byte[] data, int actualDataSize) {}
/*      */ 
/*      */ 
/*      */   
/*      */   public void removeIdleConnections() {
/*  141 */     if (this.iTimeout < System.currentTimeMillis()) {
/*  142 */       dispose();
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   public void updateRemoteIP(String newIP, DatagramSocket newSocket, DatagramPacket newPacket) {
/*  148 */     this.remoteIP = newIP;
/*  149 */     this.socket = newSocket;
/*  150 */     this.inPacket = newPacket;
/*      */   }
/*      */ 
/*      */   
/*      */   public DatagramSocket getCurrentSocket() {
/*  155 */     return this.socket;
/*      */   }
/*      */ 
/*      */   
/*      */   public DatagramPacket getCurrentPacket() {
/*  160 */     return this.inPacket;
/*      */   }
/*      */ 
/*      */   
/*      */   public void sendNewCMDAtInActiveTime() {
/*  165 */     if (TblPegasusActiveConnections.getInstance().containsKey(this.sn) && 
/*  166 */       ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).newCommand && this.newCMDCheck++ < 3 && !this.commandModeActivated) {
/*      */       
/*  168 */       byte[] newCmd = Functions.intToByteArray(128, 1);
/*  169 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + '', Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */       try {
/*  171 */         UDPFunctions.send(this.socket, this.inPacket, newCmd);
/*  172 */         this.expectedByteFlag = true;
/*  173 */         this.readFileFlag = false;
/*  174 */         this.waitingNewCommandReply = true;
/*  175 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  176 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*      */       }
/*  178 */       catch (IOException iOException) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void dispose() {
/*  186 */     if (this.sn != null && TblPegasusActiveConnections.getInstance().containsKey(this.sn) && 
/*  187 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).ownerThreadGuid)) {
/*  188 */       TblPegasusActiveConnections.removeConnection(this.sn);
/*      */     }
/*      */     
/*  191 */     if (TblActiveUdpConnections.getInstance().containsKey(this.remoteIP))
/*  192 */       TblActiveUdpConnections.getInstance().remove(this.remoteIP); 
/*      */   }
/*      */   
/*      */   public class PegasusUdpClientHandler
/*      */     extends PegasusV2Routines
/*      */     implements Runnable {
/*      */     private byte[] data;
/*      */     
/*      */     private PegasusUdpClientHandler(byte[] data) {
/*  201 */       this.data = data;
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     public void run() {
/*      */       try {
/*  208 */         if (PegasusUdpV2Handler.this.expectedByteFlag) {
/*  209 */           if (PegasusUdpV2Handler.this.readFileFlag) {
/*  210 */             if ((PegasusUdpV2Handler.this.retry = (short)(PegasusUdpV2Handler.this.retry + 1)) < PegasusUdpV2Handler.this.maxRetries) {
/*  211 */               PegasusUdpV2Handler.this.blockLength = this.data[2] & 0xFF;
/*  212 */               System.arraycopy(this.data, 0, PegasusUdpV2Handler.this.bid, 0, 2);
/*  213 */               PegasusUdpV2Handler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(PegasusUdpV2Handler.this.bid));
/*  214 */               if (PegasusUdpV2Handler.this.expBlockIndex == PegasusUdpV2Handler.this.rcvBlockIndex) {
/*  215 */                 PegasusUdpV2Handler.this.packet = new byte[PegasusUdpV2Handler.this.blockLength + 3];
/*  216 */                 System.arraycopy(this.data, 0, PegasusUdpV2Handler.this.packet, 0, 3);
/*  217 */                 System.arraycopy(this.data, 3, PegasusUdpV2Handler.this.packet, 3, PegasusUdpV2Handler.this.blockLength);
/*  218 */                 int crcCalc = CRC16.calculate(PegasusUdpV2Handler.this.packet, 0, PegasusUdpV2Handler.this.packet.length, 65535);
/*  219 */                 PegasusUdpV2Handler.this.tmp = new byte[2];
/*  220 */                 System.arraycopy(this.data, PegasusUdpV2Handler.this.blockLength + 3, PegasusUdpV2Handler.this.tmp, 0, 2);
/*  221 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(PegasusUdpV2Handler.this.tmp));
/*  222 */                 if (crcCalc == crcRecv) {
/*  223 */                   if (PegasusUdpV2Handler.this.expBlockIndex == 0) {
/*  224 */                     PegasusUdpV2Handler.this.tmp = new byte[4];
/*  225 */                     System.arraycopy(this.data, 3, PegasusUdpV2Handler.this.tmp, 0, 4);
/*  226 */                     PegasusUdpV2Handler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(PegasusUdpV2Handler.this.tmp));
/*  227 */                     System.arraycopy(this.data, 7, PegasusUdpV2Handler.this.tmp, 0, 4);
/*  228 */                     PegasusUdpV2Handler.this.recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(PegasusUdpV2Handler.this.tmp));
/*  229 */                     PegasusUdpV2Handler.this.fileContent = new byte[PegasusUdpV2Handler.this.flen + 8];
/*      */                   } 
/*  231 */                   if (PegasusUdpV2Handler.this.expBlockIndex == 1 && (this.data[2] & 0xFF) < 250) {
/*  232 */                     String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  233 */                     PegasusUdpV2Handler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  234 */                     prodBin = prodBin.substring(2);
/*  235 */                     prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  236 */                     short prodI = Short.parseShort(prodBin, 2);
/*  237 */                     if (prodI == Util.EnumProductIDs.PEGASUS.getProductId() && 
/*  238 */                       !processM2SPacket(true)) {
/*      */                       return;
/*      */                     }
/*      */                   } 
/*      */                   
/*  243 */                   System.arraycopy(this.data, 3, PegasusUdpV2Handler.this.fileContent, PegasusUdpV2Handler.this.fileContentIndex, PegasusUdpV2Handler.this.blockLength);
/*  244 */                   PegasusUdpV2Handler.this.fileContentIndex += PegasusUdpV2Handler.this.blockLength;
/*  245 */                   UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  246 */                   PegasusUdpV2Handler.this.retry = 0;
/*  247 */                   PegasusUdpV2Handler.this.expBlockIndex++;
/*      */                 } else {
/*  249 */                   UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  251 */                 if (PegasusUdpV2Handler.this.fileContentIndex < PegasusUdpV2Handler.this.flen + 8 && PegasusUdpV2Handler.this.flen > 0) {
/*  252 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  255 */                 String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  256 */                 PegasusUdpV2Handler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  257 */                 prodBin = prodBin.substring(2);
/*  258 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  259 */                 short prodI = Short.parseShort(prodBin, 2);
/*  260 */                 if (prodI == Util.EnumProductIDs.PEGASUS.getProductId()) {
/*  261 */                   if (!processM2SPacket(true)) {
/*      */                     return;
/*      */                   }
/*      */                 } else {
/*  265 */                   UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  270 */               PegasusUdpV2Handler.this.retry = 0;
/*  271 */               PegasusUdpV2Handler.this.readFileFlag = false;
/*  272 */               PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  273 */               PegasusUdpV2Handler.this.fileContentIndex = 0;
/*  274 */               PegasusUdpV2Handler.this.expBlockIndex = 0;
/*      */             } 
/*  276 */             if (PegasusUdpV2Handler.this.fileContentIndex >= PegasusUdpV2Handler.this.flen + 8 && PegasusUdpV2Handler.this.flen > 0) {
/*      */ 
/*      */               
/*  279 */               PegasusUdpV2Handler.this.tmp = new byte[PegasusUdpV2Handler.this.flen];
/*  280 */               System.arraycopy(PegasusUdpV2Handler.this.fileContent, 8, PegasusUdpV2Handler.this.tmp, 0, PegasusUdpV2Handler.this.flen);
/*  281 */               int calcCfgCrc32 = CRC32.calculateCRC32(PegasusUdpV2Handler.this.tmp, PegasusUdpV2Handler.this.flen, -1);
/*  282 */               if (calcCfgCrc32 == PegasusUdpV2Handler.this.recvCfgCRC32) {
/*  283 */                 PegasusDBManager.executeSP_065(PegasusUdpV2Handler.this.sp24DH.getId_Command(), new ByteArrayInputStream(PegasusUdpV2Handler.this.fileContent));
/*  284 */                 endCommand(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries());
/*  285 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("The_file_[") + PegasusUdpV2Handler.this.sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*  286 */                 UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  287 */                 PegasusUdpV2Handler.this.readFileFlag = false;
/*  288 */                 PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  289 */                 PegasusUdpV2Handler.this.readFileFlag = false;
/*  290 */                 PegasusUdpV2Handler.this.fileContentIndex = 0;
/*  291 */                 PegasusUdpV2Handler.this.expBlockIndex = 0;
/*      */               } else {
/*  293 */                 registerFailureSendCommand(LocaleMessage.getLocaleMessage("CRC_32_of_the_received_file_doesnt_match_[") + PegasusUdpV2Handler.this.sp24DH.getCommandData() + "]", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*  294 */                 UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/*  295 */                 PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  296 */                 PegasusUdpV2Handler.this.readFileFlag = false;
/*  297 */                 PegasusUdpV2Handler.this.fileContentIndex = 0;
/*  298 */                 PegasusUdpV2Handler.this.expBlockIndex = 0;
/*      */               } 
/*  300 */               if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  301 */                 PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  302 */                 if (sendCommandPacket()) {
/*  303 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  306 */                 UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  307 */                 ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).newCommand = false;
/*  308 */                 ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).nextSendingSolicitationReadingCommand = 0L;
/*  309 */                 this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout;
/*  310 */                 PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/*  311 */                 PegasusUdpV2Handler.this.commandModeActivated = false;
/*  312 */                 if (PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  313 */                   updateLastCommunicationModuleData();
/*  314 */                   PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                 }
/*      */               
/*      */               } 
/*      */             } 
/*  319 */           } else if (PegasusUdpV2Handler.this.waitingNewCommandReply && this.data[0] == 4) {
/*  320 */             PegasusUdpV2Handler.this.waitingNewCommandReply = false;
/*  321 */             PegasusUdpV2Handler.this.newCMDCheck = 0;
/*  322 */             if (!PegasusUdpV2Handler.this.commandModeActivated) {
/*  323 */               PegasusUdpV2Handler.this.commandModeActivated = true;
/*      */             }
/*  325 */             if (processCommandPacket()) {
/*  326 */               registerResponseTimeOut();
/*      */             }
/*  328 */           } else if (this.data[0] == 6) {
/*  329 */             if (PegasusUdpV2Handler.this.sentCommand == 32769 || PegasusUdpV2Handler.this.sentCommand == 32770) {
/*  330 */               if (PegasusUdpV2Handler.this.sendFileFlag) {
/*  331 */                 PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*  332 */                 PegasusUdpV2Handler.this.fileSendingFlag = false;
/*  333 */                 PegasusUdpV2Handler.this.sendFileFlag = false;
/*  334 */                 sendFile2Pegasus();
/*  335 */                 PegasusUdpV2Handler.this.blockIndex++;
/*  336 */                 registerResponseTimeOut();
/*      */               }
/*  338 */               else if (PegasusUdpV2Handler.this.fileSendingFlag) {
/*  339 */                 if (PegasusUdpV2Handler.this.fc.position() < PegasusUdpV2Handler.this.flen) {
/*  340 */                   PegasusUdpV2Handler.this.retry = 0;
/*  341 */                   this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout;
/*  342 */                   PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/*  343 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + PegasusUdpV2Handler.this.sp24DH.getCommandData() + " (" + PegasusUdpV2Handler.this.fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*  344 */                   PegasusUdpV2Handler.this.responseTimeout = 120000;
/*  345 */                   sendFile2Pegasus();
/*  346 */                   PegasusUdpV2Handler.this.blockIndex++;
/*  347 */                   registerResponseTimeOut();
/*      */                 } else {
/*  349 */                   PegasusUdpV2Handler.this.fileSendingFlag = false;
/*  350 */                   if (this.data[1] == 6) {
/*  351 */                     PegasusUdpV2Handler.this.fc.close();
/*  352 */                     PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  353 */                     PegasusUdpV2Handler.this.blockIndex = 0;
/*  354 */                     PegasusUdpV2Handler.this.flen = 0;
/*  355 */                     endCommand(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries());
/*  356 */                     if (PegasusUdpV2Handler.this.file != null && PegasusUdpV2Handler.this.file.exists()) {
/*  357 */                       PegasusUdpV2Handler.this.file.delete();
/*      */                     }
/*  359 */                     if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  360 */                       PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  361 */                       if (sendCommandPacket()) {
/*  362 */                         registerResponseTimeOut();
/*      */                       }
/*      */                     } else {
/*  365 */                       UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  366 */                       ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).newCommand = false;
/*  367 */                       ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).nextSendingSolicitationReadingCommand = 0L;
/*  368 */                       this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout;
/*  369 */                       PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/*  370 */                       PegasusUdpV2Handler.this.commandModeActivated = false;
/*  371 */                       if (PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  372 */                         updateLastCommunicationModuleData();
/*  373 */                         PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                       } 
/*      */                     } 
/*      */                   } else {
/*  377 */                     registerResponseTimeOut();
/*      */                   } 
/*      */                 } 
/*      */               } else {
/*  381 */                 PegasusUdpV2Handler.this.fc.close();
/*  382 */                 PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  383 */                 PegasusUdpV2Handler.this.blockIndex = 0;
/*  384 */                 PegasusUdpV2Handler.this.flen = 0;
/*  385 */                 endCommand(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries());
/*  386 */                 if (PegasusUdpV2Handler.this.file != null && PegasusUdpV2Handler.this.file.exists()) {
/*  387 */                   PegasusUdpV2Handler.this.file.delete();
/*      */                 }
/*  389 */                 if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  390 */                   PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  391 */                   if (sendCommandPacket()) {
/*  392 */                     registerResponseTimeOut();
/*      */                   }
/*      */                 } else {
/*  395 */                   UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  396 */                   ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).newCommand = false;
/*  397 */                   ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).nextSendingSolicitationReadingCommand = 0L;
/*  398 */                   this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout;
/*  399 */                   PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/*  400 */                   PegasusUdpV2Handler.this.commandModeActivated = false;
/*  401 */                   if (PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  402 */                     updateLastCommunicationModuleData();
/*  403 */                     PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   }
/*      */                 
/*      */                 } 
/*      */               } 
/*  408 */             } else if (PegasusUdpV2Handler.this.sentCommand == 32771) {
/*  409 */               if (!PegasusUdpV2Handler.this.readFileFlag) {
/*  410 */                 PegasusUdpV2Handler.this.readFileFlag = true;
/*  411 */                 PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*      */               } 
/*      */             } else {
/*  414 */               PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  415 */               PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*  416 */               endCommand(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries());
/*  417 */               if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  418 */                 PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  419 */                 if (sendCommandPacket()) {
/*  420 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  423 */                 UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  424 */                 ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).newCommand = false;
/*  425 */                 ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).nextSendingSolicitationReadingCommand = 0L;
/*  426 */                 PegasusUdpV2Handler.this.commandModeActivated = false;
/*  427 */                 this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout;
/*  428 */                 PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/*      */               } 
/*      */             } 
/*  431 */           } else if (this.data[0] == 20 && (PegasusUdpV2Handler.this.sentCommand == 32785 || PegasusUdpV2Handler.this.sentCommand == 32784 || PegasusUdpV2Handler.this.sp24DH
/*  432 */             .getCommand_Type() == 32775)) {
/*  433 */             if (PegasusUdpV2Handler.this.sentCommand == 32785) {
/*  434 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  435 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Armed_cant_bypass_a_zone"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  436 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  437 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Armed_cant_unbypass_a_zone"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*  439 */             } else if (PegasusUdpV2Handler.this.sentCommand == 32784) {
/*  440 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  441 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  442 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  443 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Disarmed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  444 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '3') {
/*  445 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  446 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '4') {
/*  447 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  448 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '5') {
/*  449 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*  451 */             } else if (PegasusUdpV2Handler.this.sp24DH.getCommand_Type() == 32775) {
/*  452 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  453 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Generate_Pulse_In_Relay"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } else {
/*  455 */                 int pgmIndex = Character.getNumericValue(PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0));
/*  456 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Generate_Pulse_Selected_output") + (pgmIndex - 1), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*      */             } 
/*  459 */             PegasusDBManager.updateCommandFailureStatus(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getCommandData() + ";" + '\024');
/*  460 */           } else if (this.data[0] == 19 && (PegasusUdpV2Handler.this.sentCommand == 32785 || PegasusUdpV2Handler.this.sentCommand == 32784)) {
/*  461 */             if (PegasusUdpV2Handler.this.sentCommand == 32785) {
/*  462 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  463 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_is_Already_Bypassed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  464 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  465 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_is_Already_Unbypassed"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*  467 */             } else if (PegasusUdpV2Handler.this.sentCommand == 32784) {
/*  468 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  469 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_arm"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  470 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  471 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_disarm"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  472 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '3') {
/*  473 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_forcearm"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  474 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '4') {
/*  475 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_stayarm"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  476 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '5') {
/*  477 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_forcestayarm"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*      */             } 
/*  480 */             PegasusDBManager.updateCommandFailureStatus(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getCommandData() + ";" + '\023');
/*  481 */           } else if (this.data[0] == 18 && (PegasusUdpV2Handler.this.sentCommand == 32785 || PegasusUdpV2Handler.this.sentCommand == 32784)) {
/*  482 */             if (PegasusUdpV2Handler.this.sentCommand == 32785) {
/*  483 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  484 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_cant_bypass"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  485 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  486 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_cant_unbypass"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*  488 */             } else if (PegasusUdpV2Handler.this.sentCommand == 32784) {
/*  489 */               if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*  490 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  491 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  492 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  493 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '3') {
/*  494 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  495 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '4') {
/*  496 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  497 */               } else if (PegasusUdpV2Handler.this.sp24DH.getCommandData().charAt(0) == '5') {
/*  498 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */               } 
/*      */             } 
/*  501 */             PegasusDBManager.updateCommandFailureStatus(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getCommandData() + ";" + '\022');
/*  502 */           } else if ((this.data[0] & 0xFF) == 17 && PegasusUdpV2Handler.this.sp24DH.getCommand_Type() == 32784) {
/*  503 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_In_Alarm"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*  504 */             PegasusDBManager.updateCommandFailureStatus(PegasusUdpV2Handler.this.sp24DH.getId_Command(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getCommandData() + ";" + '\021');
/*  505 */           } else if (this.data[0] == 21) {
/*  506 */             if (PegasusUdpV2Handler.this.sentCommand == 32769 || PegasusUdpV2Handler.this.sentCommand == 32770) {
/*  507 */               if (PegasusUdpV2Handler.this.sendFileFlag) {
/*  508 */                 PegasusUdpV2Handler.this.fileSendingFlag = true;
/*  509 */                 PegasusUdpV2Handler.this.sendFileFlag = false;
/*  510 */                 PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*  511 */                 registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*  512 */                 PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  513 */                 if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  514 */                   PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  515 */                   if (sendCommandPacket()) {
/*  516 */                     registerResponseTimeOut();
/*      */                   }
/*      */                 } 
/*      */               } 
/*  520 */               if (PegasusUdpV2Handler.this.fileSendingFlag) {
/*  521 */                 if ((PegasusUdpV2Handler.this.retry = (short)(PegasusUdpV2Handler.this.retry + 1)) < PegasusUdpV2Handler.this.maxRetries) {
/*  522 */                   if (this.lastCommIface == 1) {
/*  523 */                     if (PegasusUdpV2Handler.this.retry == 0) {
/*  524 */                       PegasusUdpV2Handler.this.responseTimeout = 120000;
/*  525 */                     } else if (PegasusUdpV2Handler.this.retry == 1) {
/*  526 */                       PegasusUdpV2Handler.this.responseTimeout = 210000;
/*  527 */                     } else if (PegasusUdpV2Handler.this.retry == 2) {
/*  528 */                       PegasusUdpV2Handler.this.responseTimeout = 300000;
/*      */                     } 
/*      */                   } else {
/*  531 */                     PegasusUdpV2Handler.this.responseTimeout = 120000;
/*      */                   } 
/*  533 */                   PegasusUdpV2Handler.this.blockIndex--;
/*  534 */                   PegasusUdpV2Handler.this.fc.position(PegasusUdpV2Handler.this.fc.position() - PegasusUdpV2Handler.this.blockLength);
/*  535 */                   sendFile2Pegasus();
/*  536 */                   PegasusUdpV2Handler.this.blockIndex++;
/*  537 */                   registerResponseTimeOut();
/*      */                 } else {
/*  539 */                   PegasusUdpV2Handler.this.retry = 0;
/*  540 */                   PegasusUdpV2Handler.this.fileSendingFlag = false;
/*  541 */                   PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  542 */                   registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*      */                 } 
/*  544 */               } else if (PegasusUdpV2Handler.this.fc.position() >= PegasusUdpV2Handler.this.flen) {
/*  545 */                 PegasusUdpV2Handler.this.fc.close();
/*  546 */                 PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  547 */                 PegasusUdpV2Handler.this.blockIndex = 0;
/*  548 */                 PegasusUdpV2Handler.this.flen = 0;
/*  549 */                 if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  550 */                   PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  551 */                   if (sendCommandPacket()) {
/*  552 */                     registerResponseTimeOut();
/*      */                   }
/*      */                 } else {
/*  555 */                   registerFailureSendCommand(LocaleMessage.getLocaleMessage("The_module_informed_that_CRC-32_is_not_matching_for_the_file_[") + PegasusUdpV2Handler.this.sp24DH.getCommandData() + "]", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*  556 */                   if (PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  557 */                     updateLastCommunicationModuleData();
/*  558 */                     PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*  560 */                   if (PegasusUdpV2Handler.this.file != null && PegasusUdpV2Handler.this.file.exists()) {
/*  561 */                     PegasusUdpV2Handler.this.file.delete();
/*      */                   }
/*  563 */                   UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  564 */                   PegasusUdpV2Handler.this.commandModeActivated = false;
/*      */                 } 
/*      */               } 
/*  567 */             } else if (PegasusUdpV2Handler.this.sentCommand == 32771) {
/*  568 */               PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  569 */               PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*  570 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*  571 */               if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  572 */                 PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  573 */                 if (sendCommandPacket()) {
/*  574 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } 
/*      */             } else {
/*  578 */               PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  579 */               PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*  580 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*  581 */               if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  582 */                 PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  583 */                 if (sendCommandPacket()) {
/*  584 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  587 */                 UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  588 */                 PegasusUdpV2Handler.this.commandModeActivated = false;
/*      */               } 
/*      */             } 
/*  591 */           } else if (this.data[0] == 22) {
/*  592 */             byte[] tmp2 = new byte[2];
/*  593 */             tmp2[0] = this.data[2];
/*  594 */             tmp2[1] = this.data[1];
/*  595 */             int rBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/*  596 */             if (PegasusUdpV2Handler.this.blockIndex - 1 == rBlockIndex) {
/*  597 */               PegasusUdpV2Handler.this.retry = 0;
/*  598 */               this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout;
/*  599 */               PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/*  600 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + PegasusUdpV2Handler.this.sp24DH.getCommandData() + " (" + PegasusUdpV2Handler.this.fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*  601 */               sendFile2Pegasus();
/*  602 */               PegasusUdpV2Handler.this.blockIndex++;
/*  603 */               registerResponseTimeOut();
/*      */             } 
/*  605 */           } else if (this.data[0] == 4) {
/*  606 */             PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  607 */             PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*      */             
/*  609 */             if (PegasusUdpV2Handler.this.sentCommand == 32769 || PegasusUdpV2Handler.this.sentCommand == 32770) {
/*  610 */               PegasusUdpV2Handler.this.sendFileFlag = false;
/*  611 */               PegasusUdpV2Handler.this.fileSendingFlag = false;
/*  612 */               PegasusUdpV2Handler.this.blockIndex = 0;
/*      */             } 
/*  614 */             registerFailureSendCommand("COMMAND Request Received in between file processing " + PegasusUdpV2Handler.this.sp24DH.getCommandData(), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*  615 */             if (++PegasusUdpV2Handler.this.cmdIndex < PegasusUdpV2Handler.this.cmdsList.size()) {
/*  616 */               PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  617 */               if (sendCommandPacket()) {
/*  618 */                 registerResponseTimeOut();
/*      */               }
/*      */             } else {
/*  621 */               UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/*  622 */               PegasusUdpV2Handler.this.commandModeActivated = false;
/*      */             }
/*      */           
/*  625 */           } else if (!processM2SPacket(false)) {
/*      */ 
/*      */           
/*      */           }
/*      */         
/*      */         }
/*  631 */         else if (!processM2SPacket(true)) {
/*      */ 
/*      */         
/*      */         } 
/*  635 */       } catch (Exception ex) {
/*  636 */         ex.printStackTrace();
/*  637 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_on_the_UDP_Server_task"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, ex);
/*      */       } finally {}
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public void reinitialize() {
/*  645 */       PegasusUdpV2Handler.this.cmdsList = null;
/*  646 */       PegasusUdpV2Handler.this.cmdIndex = 0;
/*      */       
/*  648 */       PegasusUdpV2Handler.this.expectedByteFlag = false;
/*  649 */       PegasusUdpV2Handler.this.readFileFlag = false;
/*  650 */       PegasusUdpV2Handler.this.sendFileFlag = false;
/*  651 */       PegasusUdpV2Handler.this.fileSendingFlag = false;
/*  652 */       PegasusUdpV2Handler.this.waitingNewCommandReply = false;
/*  653 */       PegasusUdpV2Handler.this.waitingCommandPacketResponse = false;
/*  654 */       PegasusUdpV2Handler.this.commandModeActivated = false;
/*  655 */       PegasusUdpV2Handler.this.sentCommand = 0;
/*  656 */       PegasusUdpV2Handler.this.blockIndex = 0;
/*  657 */       PegasusUdpV2Handler.this.blockLength = 0;
/*  658 */       PegasusUdpV2Handler.this.retry = 0;
/*  659 */       PegasusUdpV2Handler.this.flen = 0;
/*  660 */       PegasusUdpV2Handler.this.rcvBlockIndex = 0;
/*  661 */       PegasusUdpV2Handler.this.expBlockIndex = 0;
/*  662 */       PegasusUdpV2Handler.this.recvCfgCRC32 = 0;
/*  663 */       PegasusUdpV2Handler.this.fileContentIndex = 0;
/*  664 */       PegasusUdpV2Handler.this.block = null;
/*  665 */       PegasusUdpV2Handler.this.fileContent = null;
/*  666 */       PegasusUdpV2Handler.this.filePath = null;
/*  667 */       PegasusUdpV2Handler.this.fc = null;
/*  668 */       PegasusUdpV2Handler.this.blockBuf = null;
/*  669 */       PegasusUdpV2Handler.this.sp24DH = null;
/*  670 */       PegasusUdpV2Handler.this.file = null;
/*  671 */       PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = 0L;
/*  672 */       PegasusUdpV2Handler.this.packetSentTime = 0L;
/*  673 */       PegasusUdpV2Handler.this.waitingForResponse = false;
/*  674 */       PegasusUdpV2Handler.this.newCMDCheck = 0;
/*  675 */       PegasusUdpV2Handler.this.m2sPacketReceived = 0;
/*      */     }
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
/*      */     private boolean processM2SPacket(boolean flag) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
/*  695 */       PegasusUdpV2Handler.this.responseTimeout = 120000;
/*  696 */       byte[] prod = new byte[2];
/*  697 */       prod[0] = this.data[1];
/*  698 */       prod[1] = this.data[0];
/*      */       
/*  700 */       if (this.data[0] == 6) {
/*  701 */         return false;
/*      */       }
/*  703 */       if (prod[0] == 43 && prod[1] == 43) {
/*  704 */         PegasusUdpV2Handler.this.dispose();
/*  705 */         return false;
/*      */       } 
/*      */       
/*  708 */       String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/*  709 */       PegasusUdpV2Handler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  710 */       prodBin = prodBin.substring(2);
/*  711 */       prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/*  712 */       short prodI = Short.parseShort(prodBin, 2);
/*      */       
/*  714 */       if (prodI != Util.EnumProductIDs.PEGASUS.getProductId() && prod[0] != 43 && prod[1] != 43) {
/*  715 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.sn + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_UDP"), Enums.EnumMessagePriority.HIGH, null, null);
/*  716 */         UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/*  717 */         return false;
/*      */       } 
/*      */       
/*  720 */       if (PegasusUdpV2Handler.this.encType != 1 && PegasusUdpV2Handler.this.encType != 2) {
/*  721 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Module_Connected_with_Invalid_Encryption"), Enums.EnumMessagePriority.AVERAGE, PegasusUdpV2Handler.this.remoteIP, null);
/*  722 */         return false;
/*      */       } 
/*  724 */       byte[] len = new byte[2];
/*  725 */       len[0] = this.data[3];
/*  726 */       len[1] = this.data[2];
/*  727 */       int msgLen = Functions.getIntFrom2ByteArray(len);
/*  728 */       int crcCalc = CRC16.calculate(this.data, 0, msgLen + 2, 65535);
/*  729 */       len[1] = this.data[msgLen + 2];
/*  730 */       len[0] = this.data[msgLen + 2 + 1];
/*  731 */       int crcRecv = Functions.getIntFrom2ByteArray(len);
/*  732 */       if (crcCalc == crcRecv) {
/*  733 */         byte[] encData = new byte[msgLen - 2];
/*  734 */         System.arraycopy(this.data, 4, encData, 0, msgLen - 2);
/*  735 */         byte[] decData = new byte[msgLen - 2];
/*  736 */         byte[] decBlock = null;
/*  737 */         if (encData.length >= 17 && encData.length % 16 == 0) {
/*  738 */           for (int i = 0; i < encData.length; ) {
/*  739 */             byte[] block = new byte[16];
/*  740 */             System.arraycopy(encData, i, block, 0, 16);
/*  741 */             if (PegasusUdpV2Handler.this.encType == 1) {
/*  742 */               decBlock = Rijndael.decryptBytes(block, Rijndael.dataKeyBytes, false);
/*  743 */             } else if (PegasusUdpV2Handler.this.encType == 2) {
/*  744 */               decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */             } 
/*  746 */             System.arraycopy(decBlock, 0, decData, i, 16);
/*  747 */             i += 16;
/*      */           } 
/*      */         }
/*  750 */         if (!parseM2SPacket(decData)) {
/*  751 */           return false;
/*      */         }
/*  753 */         if (PegasusUdpV2Handler.this.newCMDCheck >= 3 && !flag) {
/*  754 */           flag = true;
/*      */         }
/*  756 */         if (++PegasusUdpV2Handler.this.m2sPacketReceived > 1 && PegasusUdpV2Handler.this.commandModeActivated) {
/*  757 */           PegasusUdpV2Handler.this.commandModeActivated = false;
/*  758 */           PegasusUdpV2Handler.this.m2sPacketReceived = 0;
/*      */         } 
/*  760 */         if (TblPegasusActiveConnections.getInstance().containsKey(PegasusUdpV2Handler.this.sn) && flag && 
/*  761 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).newCommand && !PegasusUdpV2Handler.this.commandModeActivated) {
/*      */           
/*  763 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/*  764 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*      */           try {
/*  766 */             UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, newCmd);
/*  767 */             PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  768 */             PegasusUdpV2Handler.this.readFileFlag = false;
/*  769 */             PegasusUdpV2Handler.this.waitingNewCommandReply = true;
/*  770 */             PegasusUdpV2Handler.this.commandModeActivated = true;
/*  771 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*  772 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*  773 */             registerResponseTimeOut();
/*  774 */             PegasusUdpV2Handler.this.newCMDCheck = 0;
/*  775 */           } catch (IOException|InterruptedException|SQLException ex) {
/*  776 */             return false;
/*      */           } 
/*      */         } 
/*      */       } else {
/*      */         
/*  781 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  782 */         UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/*      */       } 
/*  784 */       return true;
/*      */     }
/*      */     
/*      */     private boolean processCommandPacket() {
/*      */       try {
/*  789 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*  790 */         PegasusUdpV2Handler.this.cmdsList = PegasusDBManager.executeSP_024(((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idModule);
/*  791 */         PegasusUdpV2Handler.this.cmdIndex = 0;
/*  792 */         if (PegasusUdpV2Handler.this.cmdsList.size() > 0) {
/*  793 */           PegasusUdpV2Handler.this.sp24DH = PegasusUdpV2Handler.this.cmdsList.get(PegasusUdpV2Handler.this.cmdIndex);
/*  794 */           return sendCommandPacket();
/*      */         } 
/*  796 */       } catch (Exception ex) {
/*  797 */         Logger.getLogger(PegasusUdpV2Handler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/*  799 */       return false;
/*      */     } private boolean sendCommandPacket() throws SQLException, InterruptedException, Exception {
/*      */       byte[] tmp, ascii;
/*      */       String[] cData;
/*  803 */       if (PegasusDBManager.isCommandCancelled(PegasusUdpV2Handler.this.sp24DH.getId_Command())) {
/*  804 */         return false;
/*      */       }
/*  806 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Sending_command") + PegasusUdpV2Handler.this.sp24DH.getCommand_Type() + ":" + PegasusUdpV2Handler.this.sp24DH.getCommandData(), Enums.EnumMessagePriority.LOW, PegasusUdpV2Handler.this.sn, null);
/*  807 */       PegasusDBManager.updateCommandStatus(PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*      */       
/*  809 */       byte[] data = null;
/*      */ 
/*      */       
/*  812 */       switch (PegasusUdpV2Handler.this.sp24DH.getCommand_Type()) {
/*      */         case 32769:
/*  814 */           tmp = Functions.get2ByteArrayFromInt(32769);
/*  815 */           tmp = Functions.swapLSB2MSB(tmp);
/*  816 */           data = new byte[5];
/*  817 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  818 */           data[2] = Byte.valueOf(Integer.toHexString(2), 16).byteValue();
/*  819 */           ascii = Functions.getASCII4mString(PegasusUdpV2Handler.this.sp24DH.getCommandData());
/*  820 */           data = new byte[ascii.length + 3];
/*  821 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  822 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  823 */           System.arraycopy(ascii, 0, data, 3, ascii.length);
/*  824 */           PegasusUdpV2Handler.this.blockIndex = 0;
/*  825 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  826 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  827 */           PegasusUdpV2Handler.this.sendFileFlag = true;
/*  828 */           PegasusUdpV2Handler.this.sentCommand = 32769;
/*  829 */           PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           break;
/*      */         case 32770:
/*  832 */           tmp = Functions.get2ByteArrayFromInt(32770);
/*  833 */           tmp = Functions.swapLSB2MSB(tmp);
/*  834 */           data = new byte[5];
/*  835 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  836 */           data[2] = Byte.valueOf(Integer.toHexString(2), 16).byteValue();
/*  837 */           ascii = Functions.getASCII4mString(PegasusUdpV2Handler.this.sp24DH.getCommandData());
/*  838 */           data = new byte[ascii.length + 3];
/*  839 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  840 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  841 */           System.arraycopy(ascii, 0, data, 3, ascii.length);
/*  842 */           PegasusUdpV2Handler.this.blockIndex = 0;
/*  843 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  844 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  845 */           PegasusUdpV2Handler.this.sendFileFlag = true;
/*  846 */           PegasusUdpV2Handler.this.sentCommand = 32770;
/*  847 */           PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           break;
/*      */         case 32771:
/*  850 */           tmp = Functions.get2ByteArrayFromInt(32771);
/*  851 */           tmp = Functions.swapLSB2MSB(tmp);
/*  852 */           ascii = Functions.getASCII4mString(PegasusUdpV2Handler.this.sp24DH.getCommandData());
/*  853 */           data = new byte[ascii.length + 3];
/*  854 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  855 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  856 */           System.arraycopy(ascii, 0, data, 3, ascii.length);
/*  857 */           PegasusUdpV2Handler.this.expBlockIndex = 0;
/*  858 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  859 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  860 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  861 */           PegasusUdpV2Handler.this.sentCommand = 32771;
/*  862 */           PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           break;
/*      */         case 32772:
/*  865 */           data = new byte[3];
/*  866 */           tmp = Functions.get2ByteArrayFromInt(32772);
/*  867 */           tmp = Functions.swapLSB2MSB(tmp);
/*  868 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  869 */           data[2] = 0;
/*  870 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  871 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  872 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  873 */           PegasusUdpV2Handler.this.sentCommand = 32772;
/*      */           break;
/*      */         case 32773:
/*  876 */           tmp = Functions.get2ByteArrayFromInt(32773);
/*  877 */           tmp = Functions.swapLSB2MSB(tmp);
/*  878 */           data = new byte[7];
/*  879 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  880 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/*  881 */           data[2] = 4;
/*  882 */           data[3] = Byte.parseByte(cData[0]);
/*      */           
/*  884 */           data[4] = Byte.parseByte(cData[2]);
/*  885 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[1]));
/*  886 */           data[5] = tmp[1];
/*  887 */           data[6] = tmp[0];
/*  888 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  889 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  890 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  891 */           PegasusUdpV2Handler.this.sentCommand = 32773;
/*      */           break;
/*      */         case 32774:
/*  894 */           tmp = Functions.get2ByteArrayFromInt(32774);
/*  895 */           tmp = Functions.swapLSB2MSB(tmp);
/*  896 */           ascii = Functions.getASCII4mString(PegasusUdpV2Handler.this.sp24DH.getCommandData());
/*  897 */           data = new byte[ascii.length + 3];
/*  898 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  899 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  900 */           System.arraycopy(ascii, 0, data, 3, ascii.length);
/*  901 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  902 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  903 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  904 */           PegasusUdpV2Handler.this.sentCommand = 32774;
/*      */           break;
/*      */         case 32775:
/*  907 */           tmp = Functions.get2ByteArrayFromInt(32775);
/*  908 */           tmp = Functions.swapLSB2MSB(tmp);
/*  909 */           data = new byte[7];
/*  910 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  911 */           data[2] = 4;
/*  912 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/*  913 */           data[3] = Byte.parseByte(cData[0]);
/*  914 */           data[4] = (byte)(Byte.parseByte(cData[1]) - 1);
/*  915 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[2]));
/*  916 */           data[5] = tmp[1];
/*  917 */           data[6] = tmp[0];
/*  918 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  919 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  920 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  921 */           PegasusUdpV2Handler.this.sentCommand = 32775;
/*      */           break;
/*      */         case 32776:
/*  924 */           data = new byte[3];
/*  925 */           data[2] = 0;
/*  926 */           tmp = Functions.get2ByteArrayFromInt(32776);
/*  927 */           tmp = Functions.swapLSB2MSB(tmp);
/*  928 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  929 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  930 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  931 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  932 */           PegasusUdpV2Handler.this.sentCommand = 32776;
/*      */           break;
/*      */         case 32777:
/*  935 */           data = new byte[5];
/*  936 */           data[2] = 2;
/*  937 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/*  938 */           data[3] = Byte.parseByte(cData[0]);
/*  939 */           data[4] = Byte.parseByte(cData[1]);
/*  940 */           tmp = Functions.get2ByteArrayFromInt(32777);
/*  941 */           tmp = Functions.swapLSB2MSB(tmp);
/*  942 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  943 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  944 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  945 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  946 */           PegasusUdpV2Handler.this.sentCommand = 32777;
/*      */           break;
/*      */         case 32778:
/*  949 */           tmp = Functions.get2ByteArrayFromInt(32778);
/*  950 */           tmp = Functions.swapLSB2MSB(tmp);
/*  951 */           ascii = Functions.getASCII4mString(PegasusUdpV2Handler.this.sp24DH.getCommandData());
/*  952 */           data = new byte[ascii.length + 4];
/*  953 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  954 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length + 1), 16).byteValue();
/*  955 */           data[3] = 0;
/*  956 */           System.arraycopy(ascii, 0, data, 4, ascii.length);
/*  957 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  958 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  959 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  960 */           PegasusUdpV2Handler.this.sentCommand = 32778;
/*      */           break;
/*      */         case 32779:
/*  963 */           data = new byte[4];
/*  964 */           data[2] = 1;
/*  965 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/*  966 */           data[3] = Byte.parseByte(cData[0]);
/*  967 */           tmp = Functions.get2ByteArrayFromInt(32779);
/*  968 */           tmp = Functions.swapLSB2MSB(tmp);
/*  969 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  970 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  971 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  972 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  973 */           PegasusUdpV2Handler.this.sentCommand = 32779;
/*      */           break;
/*      */         case 32780:
/*  976 */           data = new byte[3];
/*  977 */           tmp = Functions.get2ByteArrayFromInt(32780);
/*  978 */           tmp = Functions.swapLSB2MSB(tmp);
/*  979 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  980 */           data[2] = 0;
/*  981 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/*  982 */           PegasusUdpV2Handler.this.readFileFlag = false;
/*  983 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/*  984 */           PegasusUdpV2Handler.this.sentCommand = 32780;
/*      */           break;
/*      */         case 32781:
/*  987 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/*  988 */           ascii = Functions.getASCII4mString(cData[2]);
/*  989 */           data = new byte[ascii.length + 5];
/*  990 */           data[2] = (byte)(ascii.length + 2);
/*  991 */           data[3] = Byte.parseByte(cData[0]);
/*  992 */           data[4] = Byte.parseByte(cData[1]);
/*  993 */           System.arraycopy(ascii, 0, data, 5, ascii.length);
/*  994 */           tmp = Functions.get2ByteArrayFromInt(32781);
/*  995 */           tmp = Functions.swapLSB2MSB(tmp);
/*  996 */           System.arraycopy(tmp, 0, data, 0, 2);
/*  997 */           data = new byte[5];
/*  998 */           data[2] = 2;
/*  999 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/* 1000 */           data[3] = Byte.parseByte(cData[0]);
/* 1001 */           data[4] = Byte.parseByte(cData[1]);
/* 1002 */           tmp = Functions.get2ByteArrayFromInt(32781);
/* 1003 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1004 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1005 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/* 1006 */           PegasusUdpV2Handler.this.readFileFlag = false;
/* 1007 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/* 1008 */           PegasusUdpV2Handler.this.sentCommand = 32781;
/*      */           break;
/*      */         case 32782:
/* 1011 */           tmp = Functions.get2ByteArrayFromInt(32782);
/* 1012 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1013 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/* 1014 */           if (cData.length == 1) {
/* 1015 */             if (cData[0].equals("1")) {
/* 1016 */               data = new byte[4];
/* 1017 */               System.arraycopy(tmp, 0, data, 0, 2);
/* 1018 */               data[2] = 1;
/* 1019 */               data[3] = 1;
/* 1020 */             } else if (cData[0].equals("2")) {
/* 1021 */               data = new byte[11];
/* 1022 */               System.arraycopy(tmp, 0, data, 0, 2);
/* 1023 */               this.df.setTimeZone(TimeZone.getTimeZone((String)Defines.TIMEZONE_NAMES.get(Integer.valueOf(PegasusUdpV2Handler.this.recevedTimeZone))));
/* 1024 */               String ddd = this.df.format(new Date());
/* 1025 */               data[2] = 8;
/* 1026 */               data[3] = 2;
/* 1027 */               data[4] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(0, 2))), 16).byteValue();
/* 1028 */               data[5] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(3, 5))), 16).byteValue();
/* 1029 */               tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(ddd.substring(6, 10)));
/* 1030 */               data[6] = tmp[0];
/* 1031 */               data[7] = tmp[1];
/* 1032 */               data[8] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(11, 13))), 16).byteValue();
/* 1033 */               data[9] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(14, 16))), 16).byteValue();
/* 1034 */               data[10] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(17))), 16).byteValue();
/*      */             } 
/*      */           } else {
/* 1037 */             data = new byte[11];
/* 1038 */             System.arraycopy(tmp, 0, data, 0, 2);
/*      */             
/* 1040 */             String[] date = cData[1].split(" ");
/* 1041 */             String[] dData = date[0].split("-");
/* 1042 */             String[] hData = date[1].split(":");
/* 1043 */             data[2] = 8;
/* 1044 */             data[3] = 3;
/* 1045 */             data[4] = Byte.valueOf(dData[2]).byteValue();
/* 1046 */             data[5] = Byte.valueOf(dData[1]).byteValue();
/* 1047 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(dData[0]));
/* 1048 */             data[6] = tmp[0];
/* 1049 */             data[7] = tmp[1];
/* 1050 */             data[8] = Byte.valueOf(hData[0]).byteValue();
/* 1051 */             data[9] = Byte.valueOf(hData[1]).byteValue();
/* 1052 */             data[10] = Byte.valueOf(hData[2]).byteValue();
/*      */           } 
/* 1054 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/* 1055 */           PegasusUdpV2Handler.this.readFileFlag = false;
/* 1056 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/* 1057 */           PegasusUdpV2Handler.this.sentCommand = 32782;
/*      */           break;
/*      */         case 32784:
/* 1060 */           data = new byte[4];
/* 1061 */           tmp = Functions.get2ByteArrayFromInt(32784);
/* 1062 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1063 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1064 */           data[2] = 1;
/* 1065 */           data[3] = Byte.parseByte(PegasusUdpV2Handler.this.sp24DH.getCommandData());
/* 1066 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/* 1067 */           PegasusUdpV2Handler.this.readFileFlag = false;
/* 1068 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/* 1069 */           PegasusUdpV2Handler.this.sentCommand = 32784;
/*      */           break;
/*      */         case 32785:
/* 1072 */           data = new byte[5];
/* 1073 */           tmp = Functions.get2ByteArrayFromInt(32785);
/* 1074 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1075 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1076 */           data[2] = 2;
/* 1077 */           cData = PegasusUdpV2Handler.this.sp24DH.getCommandData().split(";");
/* 1078 */           data[3] = Byte.parseByte(cData[0]);
/* 1079 */           data[4] = Byte.parseByte(cData[1]);
/* 1080 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/* 1081 */           PegasusUdpV2Handler.this.readFileFlag = false;
/* 1082 */           PegasusUdpV2Handler.this.sendFileFlag = false;
/* 1083 */           PegasusUdpV2Handler.this.sentCommand = 32785;
/*      */           break;
/*      */       } 
/*      */       
/*      */       try {
/* 1088 */         tmp = prepareCommandPacket(data);
/* 1089 */         UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, tmp);
/* 1090 */         PegasusUdpV2Handler.this.waitingCommandPacketResponse = true;
/* 1091 */         return true;
/* 1092 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1093 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/* 1094 */         ex.printStackTrace();
/* 1095 */         return false;
/*      */       } 
/*      */     }
/*      */     
/*      */     public byte[] prepareCommandPacket(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/* 1100 */       int plen = data.length;
/*      */       
/* 1102 */       int lpad = plen % 16;
/* 1103 */       if (lpad > 0) {
/* 1104 */         lpad = 16 - lpad;
/*      */       }
/* 1106 */       byte[] packet = new byte[plen + lpad + 4];
/* 1107 */       byte[] toEnc = new byte[plen + lpad];
/* 1108 */       System.arraycopy(data, 0, toEnc, 0, plen);
/* 1109 */       if (lpad > 0) {
/* 1110 */         for (int j = plen; j < plen + lpad; j++) {
/* 1111 */           toEnc[j] = 0;
/*      */         }
/*      */       }
/*      */ 
/*      */       
/* 1116 */       for (int i = 0; i < toEnc.length; i += 16) {
/* 1117 */         byte[] block = new byte[16];
/* 1118 */         System.arraycopy(toEnc, i, block, 0, 16);
/* 1119 */         byte[] decBlock = null;
/* 1120 */         if (PegasusUdpV2Handler.this.encType == 1) {
/* 1121 */           decBlock = Rijndael.encryptBytes(block, Rijndael.dataKeyBytes, false);
/* 1122 */         } else if (PegasusUdpV2Handler.this.encType == 2) {
/* 1123 */           decBlock = Rijndael.encryptBytes(block, Rijndael.aes_256, false);
/*      */         } 
/* 1125 */         System.arraycopy(decBlock, 0, toEnc, i, 16);
/*      */       } 
/*      */       
/* 1128 */       byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(plen + lpad + 2));
/* 1129 */       System.arraycopy(tmp, 0, packet, 0, 2);
/* 1130 */       System.arraycopy(toEnc, 0, packet, 2, plen + lpad);
/*      */       
/* 1132 */       int crcCalc = CRC16.calculate(packet, 0, plen + lpad + 2, 65535);
/* 1133 */       tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1134 */       System.arraycopy(tmp, 0, packet, plen + lpad + 2, 2);
/* 1135 */       return packet;
/*      */     }
/*      */     
/*      */     private boolean parseM2SPacket(byte[] decData) {
/* 1139 */       List<String> originalClientCodes = new ArrayList<>(6);
/* 1140 */       byte[] eventData = new byte[48];
/* 1141 */       int contactIdCounter = 0;
/* 1142 */       int index = 0;
/* 1143 */       byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*      */       try {
/* 1150 */         TblPegasusActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/* 1151 */         this.spV201VO = new SP_V2_001_VO();
/* 1152 */         this.spV201VO.setM2sData(decData);
/* 1153 */         this.spV201VO.setLastNWProtocol(Enums.EnumNWProtocol.UDP.name());
/* 1154 */         if (PegasusUdpV2Handler.this.encType == 1) {
/* 1155 */           this.spV201VO.setLastEncryption(Enums.EnumEncyption.AES128.getType());
/* 1156 */         } else if (PegasusUdpV2Handler.this.encType == 2) {
/* 1157 */           this.spV201VO.setLastEncryption(Enums.EnumEncyption.AES256.getType());
/*      */         } 
/*      */ 
/*      */         
/* 1161 */         while (index < decData.length && 
/* 1162 */           index + 2 <= decData.length) {
/*      */           StringBuilder sb; int i; byte[] oper; char[] moduleHW; byte numByte; StringBuilder originalEvent; String sEvent; int idGroup; String comPort;
/*      */           byte[] rawTimezone;
/* 1165 */           System.arraycopy(decData, index, fid, 0, 2);
/* 1166 */           index += 2;
/* 1167 */           fid = Functions.swapLSB2MSB(fid);
/* 1168 */           int fidVal = Functions.getIntFrom2ByteArray(fid);
/* 1169 */           if (fidVal <= 0) {
/*      */             break;
/*      */           }
/* 1172 */           short flen = (short)Functions.getIntFromHexByte(decData[index]);
/* 1173 */           byte[] fcon = new byte[flen];
/* 1174 */           System.arraycopy(decData, ++index, fcon, 0, flen);
/* 1175 */           index += flen;
/*      */           
/* 1177 */           switch (fidVal) {
/*      */             case 1:
/* 1179 */               sb = new StringBuilder();
/* 1180 */               for (i = 0; i < flen; i++) {
/* 1181 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1183 */               this.spV201VO.setSn(sb.toString());
/* 1184 */               PegasusUdpV2Handler.this.sn = this.spV201VO.getSn();
/* 1185 */               if (PegasusUdpV2Handler.this.sn.equals("00000000000000000000")) {
/* 1186 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, PegasusUdpV2Handler.this.sn, null);
/* 1187 */                 UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/* 1188 */                 i = 0; return i;
/* 1189 */               }  if (PegasusUdpV2Handler.this.sn.equals("00000000000000000001")) {
/* 1190 */                 String ip = PegasusUdpV2Handler.this.remoteIP.substring(0, PegasusUdpV2Handler.this.remoteIP.indexOf(":"));
/* 1191 */                 if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 1192 */                   if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/* 1193 */                     Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, PegasusUdpV2Handler.this.sn, null);
/* 1194 */                     UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/* 1195 */                     return false;
/*      */                   }
/*      */                 
/* 1198 */                 } else if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/* 1199 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, PegasusUdpV2Handler.this.sn, null);
/* 1200 */                   UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/* 1201 */                   return false;
/*      */                 } 
/*      */               } 
/*      */               
/* 1205 */               if (TblPegasusActiveConnections.getInstance().containsKey(PegasusUdpV2Handler.this.sn)) {
/* 1206 */                 String oldIP = null;
/* 1207 */                 UdpV2Handler oldHandler = null;
/* 1208 */                 synchronized (TblActiveUdpConnections.getInstance()) {
/* 1209 */                   for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblActiveUdpConnections.getInstance().entrySet()) {
/* 1210 */                     if (PegasusUdpV2Handler.this.sn.equals(((UdpV2Handler)handler.getValue()).sn) && ((UdpV2Handler)handler.getValue()).lastCommunicationTime + ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout > System.currentTimeMillis() && 
/* 1211 */                       !((String)handler.getKey()).equalsIgnoreCase(PegasusUdpV2Handler.this.remoteIP)) {
/* 1212 */                       oldIP = handler.getKey();
/* 1213 */                       oldHandler = handler.getValue();
/*      */                     } 
/*      */                   } 
/*      */                 } 
/*      */                 
/* 1218 */                 if (oldIP != null && oldHandler != null) {
/* 1219 */                   TblActiveUdpConnections.removeConnection(oldIP);
/* 1220 */                   UdpV2Handler cHandler = (UdpV2Handler)TblActiveUdpConnections.getInstance().get(PegasusUdpV2Handler.this.remoteIP);
/* 1221 */                   TblActiveUdpConnections.addConnection(PegasusUdpV2Handler.this.remoteIP, oldHandler);
/* 1222 */                   ((UdpV2Handler)TblActiveUdpConnections.getInstance().get(PegasusUdpV2Handler.this.remoteIP)).processM2SPacket(this.data);
/* 1223 */                   oldHandler.updateRemoteIP(PegasusUdpV2Handler.this.remoteIP, cHandler.getCurrentSocket(), cHandler.getCurrentPacket());
/* 1224 */                   cHandler = null;
/* 1225 */                   return false;
/*      */                 } 
/*      */                 continue;
/*      */               } 
/*      */               break;
/*      */             case 2:
/* 1231 */               sb = new StringBuilder();
/* 1232 */               for (i = 0; i < flen - 1; i++) {
/* 1233 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1235 */               sb.append(fcon[7] / 10);
/* 1236 */               this.spV201VO.setModemIMEI(sb.toString());
/*      */               break;
/*      */             
/*      */             case 45:
/* 1240 */               sb = new StringBuilder();
/* 1241 */               for (i = 1; i < flen - 1; i++) {
/* 1242 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1244 */               sb.append(fcon[8] / 10);
/* 1245 */               if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1246 */                 this.spV201VO.setSimcard1IMSI(sb.toString()); break;
/* 1247 */               }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 2) {
/* 1248 */                 this.spV201VO.setSimcard2IMSI(sb.toString());
/*      */               }
/*      */               break;
/*      */             
/*      */             case 3:
/* 1253 */               this.spV201VO.setModemModel((short)fcon[0]);
/*      */               break;
/*      */             
/*      */             case 4:
/* 1257 */               sb = new StringBuilder();
/* 1258 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFromHexByte(fcon[2])).append(".").append(Functions.getIntFromHexByte(fcon[3]));
/* 1259 */               this.spV201VO.setModemFWVersion(sb.toString());
/*      */               break;
/*      */             
/*      */             case 5:
/* 1263 */               sb = new StringBuilder();
/* 1264 */               for (i = 1; i < flen; i++) {
/* 1265 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1267 */               if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1268 */                 this.spV201VO.setSimcard1ICCID(sb.toString()); break;
/* 1269 */               }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 2) {
/* 1270 */                 this.spV201VO.setSimcard2ICCID(sb.toString());
/*      */               }
/*      */               break;
/*      */             
/*      */             case 6:
/* 1275 */               oper = new byte[flen - 1];
/* 1276 */               System.arraycopy(fcon, 1, oper, 0, flen - 1);
/* 1277 */               if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1278 */                 this.spV201VO.setSimcard1Operator(Functions.getASCIIFromByteArray(oper)); break;
/* 1279 */               }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 2) {
/* 1280 */                 this.spV201VO.setSimcard2Operator(Functions.getASCIIFromByteArray(oper));
/*      */               }
/*      */               break;
/*      */             
/*      */             case 7:
/*      */               break;
/*      */             
/*      */             case 8:
/* 1288 */               this.currentSIM = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1289 */               this.spV201VO.setCurrentSim(this.currentSIM);
/* 1290 */               this.spV201VO.setCurrentAPN((short)Functions.getIntFromHexByte(fcon[1]));
/*      */               break;
/*      */             
/*      */             case 9:
/* 1294 */               this.lastCommIface = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1295 */               this.spV201VO.setLast_Comm_Interface(this.lastCommIface);
/*      */               break;
/*      */             
/*      */             case 10:
/*      */               break;
/*      */             
/*      */             case 11:
/* 1302 */               this.spV201VO.setInitialPacket(true);
/* 1303 */               reinitialize();
/* 1304 */               moduleHW = Functions.getBinaryFromByte(fcon[0]);
/* 1305 */               this.spV201VO.setModuleHWDtls(Functions.getIntFrom2ByteArray(fcon));
/*      */               break;
/*      */             case 12:
/* 1308 */               sb = new StringBuilder();
/* 1309 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1])).append('.').append(Functions.getIntFromHexByte(fcon[2]));
/* 1310 */               this.spV201VO.setPegasus_Firmware_Version(sb.toString());
/*      */               break;
/*      */             
/*      */             case 13:
/* 1314 */               this.spV201VO.setOperation_Mode((short)Functions.getIntFromHexByte(fcon[0]));
/*      */               break;
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
/*      */             case 14:
/*      */             case 15:
/*      */             case 16:
/*      */             case 17:
/*      */             case 18:
/*      */             case 19:
/*      */             case 20:
/*      */             case 21:
/*      */             case 22:
/*      */             case 23:
/*      */             case 24:
/*      */             case 25:
/*      */             case 26:
/*      */             case 27:
/*      */             case 28:
/*      */             case 29:
/*      */             case 30:
/*      */               break;
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
/*      */             case 31:
/* 1369 */               if (flen != 8) {
/* 1370 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("An_event_was_received/generated_with_incorrect_size"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1371 */                 GlobalVariables.buzzerActivated = true;
/*      */               } 
/*      */               
/* 1374 */               originalEvent = new StringBuilder();
/* 1375 */               for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/* 1376 */                 originalEvent.append(Functions.convertContactIdDigitToHex((fcon[numByte] & 0xF0) / 16)).append(Functions.convertContactIdDigitToHex(fcon[numByte] & 0xF));
/*      */               }
/* 1378 */               sEvent = originalEvent.toString();
/* 1379 */               originalClientCodes.add(originalEvent.substring(0, 4));
/* 1380 */               idGroup = ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idGroup;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */               
/* 1386 */               comPort = Functions.getReceiverCOMPortByGroupID(idGroup, "PEGASUS");
/* 1387 */               this.spV201VO.setRcvrGroup(idGroup);
/* 1388 */               this.spV201VO.setRcvrCOMPort(comPort);
/* 1389 */               if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().containsKey(comPort)) {
/* 1390 */                 if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.ADICIONAR.getPartitionScheme()) {
/* 1391 */                   int partition = Integer.parseInt(sEvent.substring(10, 12), 16);
/* 1392 */                   partition = (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue() > 99) ? 99 : (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue());
/* 1393 */                   sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { Integer.valueOf(partition) }) + sEvent.substring(12, 16);
/* 1394 */                 } else if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.SUBSTITUIR.getPartitionScheme()) {
/* 1395 */                   sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme() }) + sEvent.substring(12, 16);
/*      */                 } 
/*      */               }
/* 1398 */               if (sEvent.equals(originalEvent.toString())) {
/* 1399 */                 byte checksum = 0;
/* 1400 */                 sEvent = sEvent.substring(0, 15).replaceAll("0", "A");
/* 1401 */                 for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/* 1402 */                   checksum = (byte)(checksum + Integer.parseInt(sEvent.substring(numByte, numByte + 1), 16));
/*      */                 }
/* 1404 */                 sEvent = sEvent.concat(Functions.convertInt2Hex((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/* 1405 */                 for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/* 1406 */                   fcon[numByte] = (byte)Integer.parseInt(sEvent.substring(numByte * 2, numByte * 2 + 2), 16);
/*      */                 }
/*      */               } 
/* 1409 */               Functions.printEvent(Util.EnumProductIDs.PEGASUS.getProductId(), fcon, idGroup, ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idClient);
/* 1410 */               System.arraycopy(fcon, 0, eventData, contactIdCounter, 8);
/* 1411 */               contactIdCounter += 8;
/* 1412 */               if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/* 1413 */                 ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */               }
/*      */               break;
/*      */             
/*      */             case 32:
/*      */               break;
/*      */             
/*      */             case 33:
/* 1421 */               this.spV201VO.setWifiModel((short)Functions.getIntFromHexByte(fcon[0]));
/*      */               break;
/*      */             
/*      */             case 34:
/* 1425 */               sb = new StringBuilder();
/* 1426 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1]));
/* 1427 */               this.spV201VO.setWifiFW(sb.toString());
/*      */               break;
/*      */             
/*      */             case 35:
/* 1431 */               this.spV201VO.setCurrentWifiAccessPoint((short)Functions.getIntFromHexByte(fcon[0]));
/*      */               break;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*      */             case 36:
/*      */             case 37:
/*      */             case 38:
/*      */             case 39:
/*      */             case 40:
/*      */             case 41:
/*      */             case 42:
/*      */             case 43:
/*      */             case 64:
/*      */               break;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*      */             case 44:
/* 1462 */               if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1463 */                 this.spV201VO.setNtpUpdateRequired(true);
/*      */               }
/*      */               break;
/*      */             
/*      */             case 46:
/*      */               break;
/*      */             
/*      */             case 51:
/* 1471 */               rawTimezone = new byte[2];
/* 1472 */               rawTimezone[0] = fcon[0];
/* 1473 */               rawTimezone[1] = fcon[1];
/* 1474 */               PegasusUdpV2Handler.this.recevedTimeZone = Functions.getSignedIntFrom2ByteArray(Functions.swapLSB2MSB(rawTimezone));
/*      */               break;
/*      */           } 
/*      */         
/*      */         } 
/* 1479 */         this.spV201VO.setOriginalClientCodes(originalClientCodes);
/* 1480 */         this.spV201VO.setEventData(eventData);
/* 1481 */         this.spV201VO.setAlarmPanelProtocol((short)1);
/*      */         
/* 1483 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.sn + LocaleMessage.getLocaleMessage("]_M2S_packet_received") + Functions.getIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/* 1484 */         this.spV201VO.setAuto_Registration_Enabled((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/* 1485 */         this.spV201VO.setModule_Ip_Addr(PegasusUdpV2Handler.this.remoteIP.substring(0, PegasusUdpV2Handler.this.remoteIP.indexOf(":")));
/*      */         try {
/* 1487 */           TblPegasusActiveConnections.semaphoreAlivePacketsReceived.acquire();
/* 1488 */           this.spV201VO = PegasusDBManager.executeSP_V2_001(this.spV201VO);
/*      */         } finally {
/* 1490 */           TblPegasusActiveConnections.semaphoreAlivePacketsReceived.release();
/*      */         } 
/* 1492 */         if (this.spV201VO != null) {
/* 1493 */           this.idleTimeout = System.currentTimeMillis() + (this.spV201VO.getComm_Timeout() * 1000);
/* 1494 */           PegasusUdpV2Handler.this.iTimeout = this.idleTimeout;
/* 1495 */           if (this.spV201VO.getAuto_Registration_Executed() == 1) {
/* 1496 */             if (this.spV201VO.getRegistered() == 0) {
/* 1497 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + PegasusUdpV2Handler.this.sn, Enums.EnumMessagePriority.HIGH, null, null);
/* 1498 */               UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { -32 });
/* 1499 */               return false;
/*      */             } 
/* 1501 */           } else if (this.spV201VO.getRegistered() == 1) {
/* 1502 */             if (this.spV201VO.getEnabled() == 0) {
/* 1503 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + PegasusUdpV2Handler.this.sn + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1504 */               UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { -31 });
/* 1505 */               return false;
/*      */             } 
/*      */           } else {
/* 1508 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + PegasusUdpV2Handler.this.sn + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1509 */             UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { -32 });
/* 1510 */             return false;
/*      */           } 
/*      */           
/* 1513 */           Functions.generateEventReceptionAlivePacket(1, this.spV201VO.getId_Client(), this.spV201VO.getId_Module(), this.spV201VO.getId_Group(), this.spV201VO.getClientCode(), this.spV201VO.getE_Alive_Received(), this.spV201VO.getF_Alive_Received(), this.spV201VO.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.UDP.name(), this.lastCommIface, -1);
/*      */           
/* 1515 */           if (this.spV201VO.isInitialPacket()) {
/* 1516 */             if (TblPegasusActiveConnections.getInstance().containsKey(PegasusUdpV2Handler.this.sn)) {
/* 1517 */               synchronized (TblActiveUdpConnections.getInstance()) {
/* 1518 */                 for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblActiveUdpConnections.getInstance().entrySet()) {
/* 1519 */                   if (PegasusUdpV2Handler.this.sn.equals(((UdpV2Handler)handler.getValue()).sn)) {
/* 1520 */                     ((UdpV2Handler)handler.getValue()).dispose();
/*      */                   }
/*      */                 } 
/*      */               } 
/* 1524 */               TblPegasusActiveConnections.removeConnection(PegasusUdpV2Handler.this.sn);
/*      */             } 
/* 1526 */             TblPegasusActiveConnections.addConnection(PegasusUdpV2Handler.this.sn, PegasusUdpV2Handler.this.myThreadGuid);
/* 1527 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idClient = this.spV201VO.getId_Client();
/* 1528 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idModule = this.spV201VO.getId_Module();
/* 1529 */             TblActiveUdpConnections.getInstance().put(PegasusUdpV2Handler.this.remoteIP, PegasusUdpV2Handler.this.currInsance);
/* 1530 */           } else if (!TblPegasusActiveConnections.getInstance().containsKey(PegasusUdpV2Handler.this.sn)) {
/* 1531 */             TblPegasusActiveConnections.addConnection(PegasusUdpV2Handler.this.sn, PegasusUdpV2Handler.this.myThreadGuid);
/* 1532 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idClient = this.spV201VO.getId_Client();
/* 1533 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idModule = this.spV201VO.getId_Module();
/* 1534 */             TblActiveUdpConnections.getInstance().put(PegasusUdpV2Handler.this.remoteIP, PegasusUdpV2Handler.this.currInsance);
/*      */           } 
/* 1536 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).clientName = this.spV201VO.getName();
/* 1537 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idGroup = this.spV201VO.getId_Group();
/* 1538 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationDebug = (this.spV201VO.getCommDebug() == 1);
/* 1539 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).communicationTimeout = this.spV201VO.getComm_Timeout() * 1000;
/* 1540 */           if (this.spV201VO.isNtpUpdateRequired()) {
/* 1541 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).newCommand = true;
/*      */           }
/*      */           try {
/* 1544 */             UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 6 });
/* 1545 */             return true;
/* 1546 */           } catch (IOException ex) {
/* 1547 */             return false;
/*      */           } 
/*      */         } 
/* 1550 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Error_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.AVERAGE, PegasusUdpV2Handler.this.sn, null);
/* 1551 */         UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/* 1552 */         return false;
/*      */       }
/* 1554 */       catch (Exception ex) {
/* 1555 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + PegasusUdpV2Handler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */         try {
/* 1557 */           UDPFunctions.send(PegasusUdpV2Handler.this.socket, PegasusUdpV2Handler.this.inPacket, new byte[] { 21 });
/* 1558 */         } catch (IOException iOException) {}
/*      */         
/* 1560 */         return false;
/*      */       } finally {
/* 1562 */         TblPegasusActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private void sendFile2Pegasus() throws SQLException, InterruptedException, FileNotFoundException, IOException {
/* 1568 */       if (!PegasusUdpV2Handler.this.fileSendingFlag) {
/* 1569 */         PegasusUdpV2Handler.this.file = Functions.writeByteArrayToFile(PegasusUdpV2Handler.this.sn + "_" + PegasusUdpV2Handler.this.sp24DH.getCommandData(), PegasusUdpV2Handler.this.sp24DH.getCommandFileData());
/* 1570 */         PegasusUdpV2Handler.this.fc = (new RandomAccessFile(PegasusUdpV2Handler.this.file, "r")).getChannel();
/* 1571 */         PegasusUdpV2Handler.this.flen = (int)PegasusUdpV2Handler.this.fc.size();
/* 1572 */         PegasusUdpV2Handler.this.fc.position(0L);
/*      */       } 
/* 1574 */       if (PegasusUdpV2Handler.this.fc.position() < PegasusUdpV2Handler.this.flen) {
/* 1575 */         PegasusUdpV2Handler.this.blockLength = (int)((PegasusUdpV2Handler.this.flen - PegasusUdpV2Handler.this.fc.position() > PegasusUdpV2Handler.this.maxReadLength) ? PegasusUdpV2Handler.this.maxReadLength : (PegasusUdpV2Handler.this.flen - PegasusUdpV2Handler.this.fc.position()));
/* 1576 */         PegasusUdpV2Handler.this.blockBuf = ByteBuffer.allocate(PegasusUdpV2Handler.this.blockLength);
/* 1577 */         if (PegasusUdpV2Handler.this.fc.read(PegasusUdpV2Handler.this.blockBuf) == PegasusUdpV2Handler.this.blockLength) {
/* 1578 */           PegasusUdpV2Handler.this.block = PegasusUdpV2Handler.this.blockBuf.array();
/* 1579 */           PegasusUdpV2Handler.this.packet = new byte[PegasusUdpV2Handler.this.blockLength + 5];
/* 1580 */           PegasusUdpV2Handler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(PegasusUdpV2Handler.this.blockIndex));
/* 1581 */           System.arraycopy(PegasusUdpV2Handler.this.ftmp, 0, PegasusUdpV2Handler.this.packet, 0, 2);
/* 1582 */           PegasusUdpV2Handler.this.packet[2] = (byte)Integer.parseInt(Integer.toHexString(PegasusUdpV2Handler.this.blockLength), 16);
/* 1583 */           System.arraycopy(PegasusUdpV2Handler.this.block, 0, PegasusUdpV2Handler.this.packet, 3, PegasusUdpV2Handler.this.blockLength);
/* 1584 */           int crcCalc = CRC16.calculate(PegasusUdpV2Handler.this.packet, 0, PegasusUdpV2Handler.this.blockLength + 3, 65535);
/* 1585 */           PegasusUdpV2Handler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1586 */           System.arraycopy(PegasusUdpV2Handler.this.ftmp, 0, PegasusUdpV2Handler.this.packet, PegasusUdpV2Handler.this.blockLength + 3, 2);
/* 1587 */           PegasusUdpV2Handler.this.outPacket = new DatagramPacket(PegasusUdpV2Handler.this.packet, 0, PegasusUdpV2Handler.this.packet.length, PegasusUdpV2Handler.this.inPacket.getAddress(), PegasusUdpV2Handler.this.inPacket.getPort());
/* 1588 */           PegasusUdpV2Handler.this.socket.send(PegasusUdpV2Handler.this.outPacket);
/* 1589 */           PegasusUdpV2Handler.this.fileSendingFlag = true;
/* 1590 */           PegasusUdpV2Handler.this.expectedByteFlag = true;
/* 1591 */           PegasusUdpV2Handler.this.readFileFlag = false;
/* 1592 */           if (PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1593 */             updateLastCommunicationModuleData();
/* 1594 */             PegasusUdpV2Handler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private void endCommand(int idCommand, short exec_Retries) throws SQLException, InterruptedException {
/* 1602 */       PegasusDBManager.executeSP_027(idCommand, (short)(exec_Retries + 1));
/*      */     }
/*      */     
/*      */     private void registerFailureSendCommand(String msg, short exec_Retries, int id_Command) throws SQLException, InterruptedException {
/* 1606 */       if (msg != null && msg.length() > 0) {
/* 1607 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, msg, Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/*      */       }
/* 1609 */       if (exec_Retries + 1 >= 3) {
/* 1610 */         PegasusDBManager.executeSP_025(id_Command, (short)(exec_Retries + 1));
/*      */       } else {
/* 1612 */         PegasusDBManager.executeSP_026(id_Command, (short)(exec_Retries + 1));
/*      */       } 
/*      */     }
/*      */     
/*      */     private void updateLastCommunicationModuleData() throws SQLException, InterruptedException {
/* 1617 */       if (TblPegasusActiveConnections.getInstance().containsKey(PegasusUdpV2Handler.this.sn)) {
/* 1618 */         PegasusDBManager.executeSP_028(((InfoModule)TblPegasusActiveConnections.getInstance().get(PegasusUdpV2Handler.this.sn)).idModule, this.lastCommIface, this.currentSIM);
/*      */       }
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     private void registerResponseTimeOut() throws InterruptedException, SQLException, IOException {
/* 1631 */       PegasusUdpV2Handler.this.packetSentTime = System.currentTimeMillis();
/* 1632 */       PegasusUdpV2Handler.this.waitingForResponse = true;
/* 1633 */       boolean failed = false;
/*      */       
/* 1635 */       while (PegasusUdpV2Handler.this.waitingForResponse) {
/* 1636 */         if (PegasusUdpV2Handler.this.packetSentTime + PegasusUdpV2Handler.this.responseTimeout > System.currentTimeMillis()) {
/* 1637 */           Thread.sleep(5L); continue;
/*      */         } 
/* 1639 */         failed = true;
/*      */       } 
/*      */ 
/*      */       
/* 1643 */       boolean fileRetry = true;
/* 1644 */       if (failed && PegasusUdpV2Handler.this.waitingForResponse) {
/* 1645 */         if (PegasusUdpV2Handler.this.expectedByteFlag) {
/* 1646 */           if (PegasusUdpV2Handler.this.readFileFlag) {
/* 1647 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + PegasusUdpV2Handler.this.sp24DH.getCommandData() + "]", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/* 1648 */           } else if (PegasusUdpV2Handler.this.waitingNewCommandReply) {
/* 1649 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_waiting_response_to_the_New_Command_Available_packet"), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/* 1650 */             PegasusUdpV2Handler.this.commandModeActivated = false;
/* 1651 */             PegasusUdpV2Handler.this.expectedByteFlag = false;
/* 1652 */           } else if (PegasusUdpV2Handler.this.waitingCommandPacketResponse) {
/* 1653 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/* 1654 */           } else if (PegasusUdpV2Handler.this.fileSendingFlag) {
/*      */             
/* 1656 */             if ((PegasusUdpV2Handler.this.retry = (short)(PegasusUdpV2Handler.this.retry + 1)) < PegasusUdpV2Handler.this.maxRetries) {
/* 1657 */               PegasusUdpV2Handler.this.blockIndex--;
/* 1658 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + "[" + PegasusUdpV2Handler.this.sp24DH.getCommandData() + "]", Enums.EnumMessagePriority.HIGH, PegasusUdpV2Handler.this.sn, null);
/* 1659 */               PegasusUdpV2Handler.this.fc.position(PegasusUdpV2Handler.this.fc.position() - PegasusUdpV2Handler.this.blockLength);
/* 1660 */               if (this.lastCommIface == 1) {
/* 1661 */                 if (PegasusUdpV2Handler.this.retry == 0) {
/* 1662 */                   PegasusUdpV2Handler.this.responseTimeout = 120000;
/* 1663 */                 } else if (PegasusUdpV2Handler.this.retry == 1) {
/* 1664 */                   PegasusUdpV2Handler.this.responseTimeout = 210000;
/* 1665 */                 } else if (PegasusUdpV2Handler.this.retry == 2) {
/* 1666 */                   PegasusUdpV2Handler.this.responseTimeout = 300000;
/*      */                 } 
/*      */               } else {
/* 1669 */                 PegasusUdpV2Handler.this.responseTimeout = 120000;
/*      */               } 
/* 1671 */               fileRetry = false;
/* 1672 */               sendFile2Pegasus();
/* 1673 */               PegasusUdpV2Handler.this.blockIndex++;
/* 1674 */               registerResponseTimeOut();
/*      */             } else {
/* 1676 */               PegasusUdpV2Handler.this.retry = 0;
/* 1677 */               PegasusUdpV2Handler.this.fileSendingFlag = false;
/* 1678 */               PegasusUdpV2Handler.this.expectedByteFlag = false;
/* 1679 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*      */             } 
/*      */           } else {
/* 1682 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + "[" + PegasusUdpV2Handler.this.sp24DH.getCommandData() + "]" + LocaleMessage.getLocaleMessage("was_sent_successfully"), PegasusUdpV2Handler.this.sp24DH.getExec_Retries(), PegasusUdpV2Handler.this.sp24DH.getId_Command());
/*      */           } 
/*      */         }
/* 1685 */         if (fileRetry)
/* 1686 */           PegasusUdpV2Handler.this.dispose(); 
/*      */       } 
/*      */     }
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\PegasusUdpV2Handler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */