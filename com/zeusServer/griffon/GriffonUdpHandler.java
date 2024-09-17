/*      */ package com.zeusServer.griffon;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.GriffonDBManager;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.socket.communication.UDPDataServer;
/*      */ import com.zeusServer.socket.communication.UdpV2Handler;
/*      */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblGriffonActiveUdpConnections;
/*      */ import com.zeusServer.util.CRC16;
/*      */ import com.zeusServer.util.CRC32;
/*      */ import com.zeusServer.util.Defines;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.InfoModule;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.Rijndael;
/*      */ import com.zeusServer.util.UDPFunctions;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import com.zeusServer.util.ZeusServerLogger;
/*      */ import com.zeuscc.griffon.derby.beans.Access;
/*      */ import com.zeuscc.griffon.derby.beans.Event;
/*      */ import com.zeuscc.griffon.derby.beans.ExpansionModule;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonEnums;
/*      */ import com.zeuscc.griffon.derby.beans.GriffonModule;
/*      */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
/*      */ import com.zeuscc.griffon.derby.beans.PGM;
/*      */ import com.zeuscc.griffon.derby.beans.Partition;
/*      */ import com.zeuscc.griffon.derby.beans.VoiceMessage;
/*      */ import com.zeuscc.griffon.derby.beans.Zone;
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
/*      */ import java.util.Calendar;
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
/*      */ import org.apache.log4j.Logger;
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
/*      */ public class GriffonUdpHandler
/*      */   extends UdpV2Handler
/*      */ {
/*   73 */   DatagramPacket outPacket = null;
/*      */   private DatagramSocket socket;
/*      */   private DatagramPacket inPacket;
/*   76 */   private String myThreadGuid = UUID.randomUUID().toString();
/*      */   private String remoteIP;
/*      */   private UdpV2Handler currInsance;
/*      */   protected int timezone;
/*   80 */   private short encType = 2;
/*      */   public GriffonEnums.UDPExceutionsStates nextState;
/*      */   private List<SP_024DataHolder> cmdsList;
/*      */   private int cmdIndex;
/*      */   private boolean commandModeActivated;
/*      */   private int sentCommand;
/*   86 */   int blockIndex = 0;
/*      */   boolean fileSendingFlag;
/*   88 */   int maxReadLength = 240;
/*      */   int blockLength;
/*   90 */   short maxRetries = 3;
/*   91 */   short retry = 0;
/*   92 */   int flen = 0;
/*      */   int rcvBlockIndex;
/*   94 */   int expBlockIndex = 0;
/*   95 */   int recvCfgCRC32 = 0;
/*   96 */   int fileContentIndex = 0;
/*   97 */   byte[] block = null;
/*   98 */   byte[] bid = new byte[2];
/*      */   byte[] packet;
/*      */   byte[] ftmp;
/*      */   byte[] tmp;
/*  102 */   byte[] fileContent = null;
/*      */   String filePath;
/*  104 */   FileChannel fc = null;
/*      */   ByteBuffer blockBuf;
/*      */   SP_024DataHolder sp24DH;
/*  107 */   File file = null;
/*  108 */   long nextUpdateFieldLastCommunication = 0L;
/*  109 */   int responseTimeout = 120000;
/*      */   long packetSentTime;
/*      */   boolean waitingForResponse;
/*      */   short lCommIface;
/*      */   long iTimeout;
/*      */   long lastPacketReceivedTime;
/*      */   int newCMDCheck;
/*      */   int m2sPacketReceived;
/*      */   int recevedTimeZone;
/*      */   boolean runtimeCommandsPending = false;
/*  119 */   long last_80_sent_time = 0L;
/*  120 */   private Logger ownLogger = null;
/*      */   boolean firstPacketWithOutCfgCRC32 = false;
/*  122 */   private List<Integer> deviceCRC32 = null;
/*  123 */   List<VoiceMessage> vmList = null;
/*  124 */   List<VoiceMessage> requiredVMList = null;
/*      */   private int requiredVMIndex;
/*      */   private long currentVMPosition;
/*  127 */   RandomAccessFile raf = null;
/*      */   VoiceMessage currentVM;
/*      */   private int idGroup;
/*      */   private boolean crc32MatchedNUpdateStatusNotSent = false;
/*      */   protected boolean requestedAllFilesCRC32 = false;
/*      */   protected boolean ebFWRequested = false;
/*      */   protected boolean fileIDsSent = false;
/*      */   boolean fileSync_80_Sent = false;
/*  135 */   byte[] fid = new byte[2];
/*  136 */   byte[] first16 = new byte[16];
/*  137 */   byte[] tmp4 = new byte[4];
/*  138 */   int lPad = 0;
/*  139 */   ModuleCFG syncCFG = null;
/*  140 */   byte[] fileIDData = null;
/*      */   private int recAudioLookupCRC32;
/*  142 */   int recvLogCRC32 = 0;
/*  143 */   int recordedAudioFilesCount = 0;
/*  144 */   int recordedAudioFilesCountInPrevPack = 0;
/*      */   boolean recordedLookupRequest = false;
/*      */   boolean recordListLookupRequest2 = false;
/*      */   boolean recordedLookupRequestSent = false;
/*      */   boolean digitalPGMBufferReceived = false;
/*      */   boolean disableDigitalPGMBuffer = false;
/*      */   boolean lastRecvCfgFailed = false;
/*      */   byte[] oldNonProcessedData;
/*      */   boolean waitForSecondAck = false;
/*  153 */   private short productID = 0;
/*      */ 
/*      */   
/*      */   public GriffonUdpHandler(DatagramSocket socket, DatagramPacket inPacket, short productID, short encType) {
/*  157 */     this.socket = socket;
/*  158 */     this.inPacket = inPacket;
/*  159 */     this.productID = productID;
/*  160 */     this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*  161 */     this.encType = encType;
/*  162 */     this.remoteIP = this.inPacket.getAddress().toString() + ":" + this.inPacket.getPort();
/*  163 */     this.remoteIP = this.remoteIP.substring(1);
/*  164 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  165 */     this.iTimeout = this.idleTimeout;
/*  166 */     this.currInsance = this;
/*  167 */     this.firstPacketWithOutCfgCRC32 = true;
/*  168 */     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void processM2SPacket(byte[] data) {}
/*      */ 
/*      */ 
/*      */   
/*      */   public void processM2SPacket(byte[] data, int actualDataSize) {
/*  178 */     this.waitingForResponse = false;
/*  179 */     this.lastCommunicationTime = System.currentTimeMillis();
/*  180 */     if (this.oldNonProcessedData != null) {
/*  181 */       if (this.oldNonProcessedData.length > 2) {
/*  182 */         int chunkSize = this.oldNonProcessedData[2];
/*      */ 
/*      */         
/*  185 */         if (this.oldNonProcessedData.length - 3 + data.length == chunkSize + 2) {
/*  186 */           byte[] tmp = new byte[chunkSize + 5];
/*  187 */           System.arraycopy(this.oldNonProcessedData, 0, tmp, 0, this.oldNonProcessedData.length);
/*  188 */           System.arraycopy(data, 0, tmp, this.oldNonProcessedData.length, data.length);
/*  189 */           data = new byte[tmp.length];
/*  190 */           System.arraycopy(tmp, 0, data, 0, tmp.length);
/*      */         } 
/*      */       } 
/*  193 */       this.oldNonProcessedData = null;
/*      */     } 
/*  195 */     UDPDataServer.clientHelper.execute(new GriffonUdpClientHandler(data, actualDataSize));
/*      */   }
/*      */ 
/*      */   
/*      */   public void removeIdleConnections() {
/*  200 */     if (this.iTimeout < System.currentTimeMillis()) {
/*  201 */       dispose();
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   public void updateRemoteIP(String newIP, DatagramSocket newSocket, DatagramPacket newPacket) {
/*  207 */     this.remoteIP = newIP;
/*  208 */     this.socket = newSocket;
/*  209 */     this.inPacket = newPacket;
/*      */   }
/*      */ 
/*      */   
/*      */   public DatagramSocket getCurrentSocket() {
/*  214 */     return this.socket;
/*      */   }
/*      */ 
/*      */   
/*      */   public DatagramPacket getCurrentPacket() {
/*  219 */     return this.inPacket;
/*      */   }
/*      */ 
/*      */   
/*      */   public void sendNewCMDAtInActiveTime() {
/*  224 */     if (TblGriffonActiveConnections.getInstance().containsKey(this.sn)) {
/*  225 */       boolean condn1 = (((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).newCommand && this.newCMDCheck++ < 3 && !this.commandModeActivated);
/*  226 */       if (condn1 || this.runtimeCommandsPending || (this.recordListLookupRequest2 && this.newCMDCheck++ < 3 && !this.commandModeActivated)) {
/*  227 */         if (this.recordListLookupRequest2 && !condn1 && !this.runtimeCommandsPending) {
/*  228 */           this.lastPacketReceivedTime = 0L;
/*      */         }
/*      */         
/*  231 */         if (this.lastPacketReceivedTime + 20000L < System.currentTimeMillis()) {
/*  232 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/*  233 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + '', Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */           try {
/*  235 */             UDPFunctions.send(this.socket, this.inPacket, newCmd);
/*  236 */             this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*  237 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  238 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*      */           }
/*  240 */           catch (IOException iOException) {}
/*      */         }
/*  242 */         else if (this.runtimeCommandsPending && this.last_80_sent_time + 30000L < System.currentTimeMillis()) {
/*  243 */           this.last_80_sent_time = System.currentTimeMillis();
/*  244 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/*      */           try {
/*  246 */             UDPFunctions.send(this.socket, this.inPacket, newCmd);
/*  247 */           } catch (IOException iOException) {}
/*      */           
/*  249 */           this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public void dispose() {
/*  257 */     if (this.sn != null && TblGriffonActiveConnections.getInstance().containsKey(this.sn) && 
/*  258 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).ownerThreadGuid)) {
/*  259 */       TblGriffonActiveConnections.removeConnection(this.sn);
/*      */     }
/*      */     
/*  262 */     if (TblGriffonActiveUdpConnections.getInstance().containsKey(this.remoteIP))
/*  263 */       TblGriffonActiveUdpConnections.getInstance().remove(this.remoteIP); 
/*      */   }
/*      */   
/*      */   public class GriffonUdpClientHandler
/*      */     extends GriffonRoutines implements Runnable {
/*      */     private byte[] data;
/*  269 */     private ModuleCFG newModuleCFG = null;
/*  270 */     int remainingBytes = 0;
/*  271 */     int actualDataSize = 0;
/*      */     boolean running = true;
/*      */     
/*      */     private GriffonUdpClientHandler(byte[] data, int actualDataSize) {
/*  275 */       this.data = data;
/*  276 */       this.actualDataSize = actualDataSize;
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     public void run() {
/*      */       try {
/*  283 */         Thread.sleep(10L);
/*  284 */       } catch (InterruptedException interruptedException) {}
/*      */ 
/*      */       
/*  287 */       while (this.running) {
/*  288 */         this.remainingBytes = this.actualDataSize;
/*  289 */         processData();
/*  290 */         if (this.remainingBytes > 0) {
/*  291 */           byte[] tmp = new byte[this.remainingBytes];
/*  292 */           System.arraycopy(this.data, this.actualDataSize - this.remainingBytes, tmp, 0, this.remainingBytes);
/*  293 */           this.data = new byte[tmp.length];
/*  294 */           System.arraycopy(tmp, 0, this.data, 0, tmp.length);
/*  295 */           this.actualDataSize = this.remainingBytes; continue;
/*      */         } 
/*  297 */         this.remainingBytes = 0;
/*      */       } 
/*      */     } private void processData() {
/*      */       try {
/*      */         String prodBin;
/*      */         int crc32Recv;
/*      */         int noOfFiles;
/*      */         boolean sendVMFiles;
/*  305 */         switch (GriffonUdpHandler.this.nextState) {
/*      */           
/*      */           case EM_FW_REQUESTED:
/*  308 */             if (this.data[0] == 6) {
/*  309 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.READ_EM_FW_DATA;
/*  310 */               if (this.data.length < 4) {
/*  311 */                 waitGriffonResponse();
/*      */               }
/*  313 */               this.remainingBytes--; break;
/*      */             } 
/*  315 */             prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  316 */             GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  317 */             prodBin = prodBin.substring(2);
/*  318 */             prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  319 */             GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/*  320 */             if ((GriffonUdpHandler.this.productID != Util.EnumProductIDs.GRIFFON_V1.getProductId() && GriffonUdpHandler.this.productID != Util.EnumProductIDs.GRIFFON_V2.getProductId()) || 
/*  321 */               !processM2SPacket(true));
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case READ_EM_FW_DATA:
/*  330 */             if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/*  331 */               GriffonUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  332 */               System.arraycopy(this.data, 0, GriffonUdpHandler.this.bid, 0, 2);
/*  333 */               GriffonUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.bid));
/*  334 */               this.remainingBytes -= 3;
/*      */               
/*  336 */               if (GriffonUdpHandler.this.blockLength > this.data.length) {
/*  337 */                 GriffonUdpHandler.this.oldNonProcessedData = new byte[this.data.length];
/*  338 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.oldNonProcessedData, 0, this.data.length);
/*  339 */                 this.remainingBytes = 0;
/*      */                 break;
/*      */               } 
/*  342 */               GriffonUdpHandler.this.oldNonProcessedData = null;
/*      */               
/*  344 */               if (GriffonUdpHandler.this.expBlockIndex == GriffonUdpHandler.this.rcvBlockIndex) {
/*  345 */                 GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 3];
/*  346 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.packet, 0, 3);
/*  347 */                 System.arraycopy(this.data, 3, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/*  348 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, 65535);
/*  349 */                 GriffonUdpHandler.this.tmp = new byte[2];
/*  350 */                 System.arraycopy(this.data, GriffonUdpHandler.this.blockLength + 3, GriffonUdpHandler.this.tmp, 0, 2);
/*  351 */                 this.remainingBytes -= GriffonUdpHandler.this.blockLength + 2;
/*  352 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp));
/*  353 */                 if (crcCalc == crcRecv) {
/*  354 */                   if (GriffonUdpHandler.this.expBlockIndex == 0 && GriffonUdpHandler.this.blockLength <= 10) {
/*  355 */                     UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  356 */                     if (sendUpdateStatusCommand()) {
/*  357 */                       GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*  358 */                       GriffonUdpHandler.this.fileSync_80_Sent = false;
/*  359 */                       waitGriffonResponse();
/*      */                     } 
/*      */                   } 
/*  362 */                   if (GriffonUdpHandler.this.expBlockIndex == 0) {
/*  363 */                     System.arraycopy(this.data, 3, GriffonUdpHandler.this.first16, 0, 16);
/*  364 */                     System.arraycopy(GriffonUdpHandler.this.first16, 2, GriffonUdpHandler.this.tmp4, 0, 4);
/*  365 */                     GriffonUdpHandler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/*  366 */                     System.arraycopy(GriffonUdpHandler.this.first16, 6, GriffonUdpHandler.this.tmp4, 0, 4);
/*  367 */                     GriffonUdpHandler.this.recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/*  368 */                     GriffonUdpHandler.this.fileContent = new byte[GriffonUdpHandler.this.flen + 10];
/*  369 */                     GriffonUdpHandler.this.fileContentIndex = 0;
/*      */                   } 
/*      */                   
/*  372 */                   System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/*  373 */                   GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength;
/*  374 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  375 */                   GriffonUdpHandler.this.retry = 0;
/*  376 */                   GriffonUdpHandler.this.expBlockIndex++;
/*  377 */                   if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  378 */                     updateLastCommunicationModuleData();
/*  379 */                     GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*  382 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  384 */                 if (GriffonUdpHandler.this.fileContentIndex < GriffonUdpHandler.this.flen + 10 && GriffonUdpHandler.this.flen > 0) {
/*  385 */                   waitGriffonResponse();
/*      */                 }
/*      */               } else {
/*  388 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  389 */                 GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  390 */                 prodBin = prodBin.substring(2);
/*  391 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  392 */                 GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/*  393 */                 if (GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) {
/*  394 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*  397 */                 } else if (GriffonUdpHandler.this.rcvBlockIndex < GriffonUdpHandler.this.expBlockIndex) {
/*  398 */                   GriffonUdpHandler.this.retry = 0;
/*  399 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  400 */                   this.remainingBytes = 0;
/*      */                 } else {
/*  402 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*  403 */                   this.remainingBytes = 0;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  408 */               GriffonUdpHandler.this.retry = 0;
/*  409 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  410 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*  411 */               if (sendUpdateStatusCommand()) {
/*  412 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*  413 */                 GriffonUdpHandler.this.fileSync_80_Sent = false;
/*  414 */                 waitGriffonResponse();
/*      */               } 
/*      */             } 
/*  417 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.flen + 10 && GriffonUdpHandler.this.flen > 0) {
/*  418 */               byte[] decBlock = null;
/*      */               
/*  420 */               GriffonUdpHandler.this.retry = 0;
/*  421 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  422 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*  423 */               decBlock = new byte[GriffonUdpHandler.this.flen];
/*  424 */               System.arraycopy(GriffonUdpHandler.this.fileContent, 10, decBlock, 0, GriffonUdpHandler.this.flen);
/*  425 */               int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/*  426 */               if (GriffonUdpHandler.this.recvCfgCRC32 == calcCfgCrc32) {
/*  427 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exp_Boards_FW_version_sync_done_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/*  428 */                 GriffonHandlerHelper.parseEBFWData(decBlock, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.recvCfgCRC32);
/*      */               } else {
/*  430 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("CRC_32_of_the_received_configuration_file_is_invalid"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/*      */               } 
/*  432 */               if (sendUpdateStatusCommand()) {
/*  433 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*  434 */                 GriffonUdpHandler.this.fileSync_80_Sent = false;
/*  435 */                 waitGriffonResponse();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case VM_SEND_INITIATED:
/*  442 */             this.remainingBytes--;
/*  443 */             if (this.data[0] == 6 && 
/*  444 */               GriffonUdpHandler.this.currentVM != null) {
/*  445 */               GriffonUdpHandler.this.file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*      */               
/*  447 */               GriffonUdpHandler.this.raf = new RandomAccessFile(GriffonUdpHandler.this.file, "r");
/*  448 */               int fpos = 0;
/*  449 */               boolean fileFound = false;
/*  450 */               byte[] tm = new byte[12];
/*  451 */               byte[] vmHeader = new byte[22];
/*      */               
/*  453 */               while (fpos < GriffonUdpHandler.this.file.length()) {
/*  454 */                 GriffonUdpHandler.this.raf.seek(fpos);
/*      */                 
/*  456 */                 GriffonUdpHandler.this.raf.read(vmHeader);
/*  457 */                 fpos += 22;
/*  458 */                 GriffonUdpHandler.this.currentVMPosition = fpos;
/*  459 */                 System.arraycopy(vmHeader, 2, tm, 0, 12);
/*      */                 
/*  461 */                 byte[] tmlen = new byte[4];
/*  462 */                 tmlen[3] = vmHeader[14];
/*  463 */                 tmlen[2] = vmHeader[15];
/*  464 */                 tmlen[1] = vmHeader[16];
/*  465 */                 tmlen[0] = vmHeader[17];
/*  466 */                 int vmLen = Functions.getIntFrom4ByteArray(tmlen);
/*  467 */                 if ((new String(tm, "ISO-8859-1")).trim().toLowerCase().equals(GriffonUdpHandler.this.currentVM.getVmName().toLowerCase())) {
/*  468 */                   fileFound = true;
/*      */                   break;
/*      */                 } 
/*  471 */                 fpos += vmLen;
/*      */               } 
/*      */ 
/*      */ 
/*      */               
/*  476 */               if (fileFound && (
/*  477 */                 new String(tm, "ISO-8859-1")).trim().toLowerCase().equals(GriffonUdpHandler.this.currentVM.getVmName().toLowerCase())) {
/*  478 */                 byte[] vmcmd = new byte[19];
/*  479 */                 vmcmd[0] = 1;
/*  480 */                 System.arraycopy(tm, 0, vmcmd, 1, tm.length);
/*  481 */                 System.arraycopy(vmHeader, 14, vmcmd, 13, 4);
/*  482 */                 int crcCalc = CRC16.calculate(vmcmd, 0, 17, 65535);
/*  483 */                 byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/*  484 */                 System.arraycopy(tmp2, 0, vmcmd, 17, 2);
/*  485 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, vmcmd);
/*  486 */                 GriffonUdpHandler.this.flen = GriffonUdpHandler.this.currentVM.getVmLength();
/*  487 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.VM_SEND_IN_PROGRESS;
/*  488 */                 GriffonUdpHandler.this.blockIndex = 0;
/*  489 */                 GriffonUdpHandler.this.retry = 0;
/*  490 */                 if (GriffonUdpHandler.this.file == null) {
/*  491 */                   GriffonUdpHandler.this.file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*      */                 }
/*  493 */                 if (GriffonUdpHandler.this.raf == null) {
/*  494 */                   GriffonUdpHandler.this.raf = new RandomAccessFile(GriffonUdpHandler.this.file, "r");
/*      */                 }
/*  496 */                 GriffonUdpHandler.this.raf.getChannel().position(GriffonUdpHandler.this.currentVMPosition);
/*  497 */                 waitGriffonResponse();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case VM_SEND_IN_PROGRESS:
/*  506 */             if (GriffonUdpHandler.this.file == null) {
/*  507 */               GriffonUdpHandler.this.file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*      */             }
/*  509 */             if (GriffonUdpHandler.this.raf == null) {
/*  510 */               GriffonUdpHandler.this.raf = new RandomAccessFile(GriffonUdpHandler.this.file, "r");
/*  511 */               GriffonUdpHandler.this.raf.getChannel().position(GriffonUdpHandler.this.currentVMPosition);
/*      */             } 
/*  513 */             this.remainingBytes--;
/*  514 */             if (this.data[0] == 6) {
/*  515 */               if (GriffonUdpHandler.this.raf.getChannel().position() < GriffonUdpHandler.this.currentVMPosition + GriffonUdpHandler.this.flen) {
/*  516 */                 GriffonUdpHandler.this.waitForSecondAck = false;
/*  517 */                 GriffonUdpHandler.this.retry = 0;
/*  518 */                 this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout;
/*  519 */                 GriffonUdpHandler.this.iTimeout = this.idleTimeout;
/*  520 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + GriffonUdpHandler.this.currentVM.getVmName() + " (" + (GriffonUdpHandler.this.currentVMPosition + GriffonUdpHandler.this.flen - GriffonUdpHandler.this.raf.getChannel().position()) + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/*  521 */                 sendVoiceMessageFile2Module();
/*  522 */                 GriffonUdpHandler.this.fileSendingFlag = true;
/*  523 */                 GriffonUdpHandler.this.blockIndex++;
/*  524 */                 GriffonUdpHandler.this.responseTimeout = 120000;
/*  525 */                 waitGriffonResponse(); break;
/*      */               } 
/*  527 */               if (this.remainingBytes > 0) {
/*  528 */                 this.remainingBytes--;
/*  529 */                 if (this.data[1] == 6) {
/*  530 */                   GriffonUdpHandler.this.blockIndex = 0;
/*  531 */                   GriffonUdpHandler.this.flen = 0;
/*  532 */                   saveVoiceMessageIntoRepo();
/*  533 */                   sendNextVoiceMessageFromQueue();
/*      */                 }  break;
/*      */               } 
/*  536 */               if (GriffonUdpHandler.this.waitForSecondAck) {
/*  537 */                 GriffonUdpHandler.this.waitForSecondAck = false;
/*  538 */                 GriffonUdpHandler.this.blockIndex = 0;
/*  539 */                 GriffonUdpHandler.this.flen = 0;
/*  540 */                 saveVoiceMessageIntoRepo();
/*  541 */                 sendNextVoiceMessageFromQueue(); break;
/*      */               } 
/*  543 */               GriffonUdpHandler.this.waitForSecondAck = true;
/*  544 */               GriffonUdpHandler.this.responseTimeout = 120000;
/*  545 */               waitGriffonResponse();
/*      */               
/*      */               break;
/*      */             } 
/*  549 */             if (this.data[0] == 21 && 
/*  550 */               GriffonUdpHandler.this.raf.getChannel().position() < GriffonUdpHandler.this.currentVMPosition + GriffonUdpHandler.this.flen) {
/*  551 */               if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/*  552 */                 if (GriffonUdpHandler.this.lCommIface == 1) {
/*  553 */                   if (GriffonUdpHandler.this.retry == 0) {
/*  554 */                     GriffonUdpHandler.this.responseTimeout = 120000;
/*  555 */                   } else if (GriffonUdpHandler.this.retry == 1) {
/*  556 */                     GriffonUdpHandler.this.responseTimeout = 210000;
/*  557 */                   } else if (GriffonUdpHandler.this.retry == 2) {
/*  558 */                     GriffonUdpHandler.this.responseTimeout = 300000;
/*      */                   } 
/*      */                 } else {
/*  561 */                   GriffonUdpHandler.this.responseTimeout = 120000;
/*      */                 } 
/*  563 */                 GriffonUdpHandler.this.blockIndex--;
/*  564 */                 if (GriffonUdpHandler.this.blockIndex < 0) {
/*  565 */                   GriffonUdpHandler.this.blockIndex = 0;
/*      */                 }
/*  567 */                 GriffonUdpHandler.this.raf.getChannel().position(GriffonUdpHandler.this.raf.getChannel().position() - GriffonUdpHandler.this.blockLength);
/*  568 */                 sendVoiceMessageFile2Module();
/*  569 */                 GriffonUdpHandler.this.blockIndex++;
/*  570 */                 waitGriffonResponse(); break;
/*      */               } 
/*  572 */               GriffonUdpHandler.this.retry = 0;
/*  573 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case VM_READ_INITIATED:
/*  581 */             this.remainingBytes--;
/*  582 */             if (this.data[0] == 6 && 
/*  583 */               GriffonUdpHandler.this.currentVM != null) {
/*  584 */               byte[] vmcmd = new byte[15];
/*  585 */               vmcmd[0] = 1;
/*  586 */               System.arraycopy(GriffonUdpHandler.this.currentVM.getVmName().getBytes("ISO-8859-1"), 0, vmcmd, 1, GriffonUdpHandler.this.currentVM.getVmName().length());
/*  587 */               int crcCalc = CRC16.calculate(vmcmd, 0, 13, 65535);
/*  588 */               byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/*  589 */               System.arraycopy(tmp2, 0, vmcmd, 13, 2);
/*  590 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, vmcmd);
/*  591 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.VM_READ_COMMAND;
/*  592 */               waitGriffonResponse();
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case VM_READ_COMMAND:
/*  599 */             this.remainingBytes--;
/*  600 */             if (this.data[0] == 6 && 
/*  601 */               GriffonUdpHandler.this.currentVM != null) {
/*  602 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  603 */               GriffonUdpHandler.this.retry = 0;
/*  604 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*  605 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.VM_READ_IN_PROGRESS;
/*  606 */               waitGriffonResponse();
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case VM_READ_IN_PROGRESS:
/*  613 */             if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/*  614 */               GriffonUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  615 */               System.arraycopy(this.data, 0, GriffonUdpHandler.this.bid, 0, 2);
/*  616 */               GriffonUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.bid));
/*  617 */               if (GriffonUdpHandler.this.blockLength > this.data.length) {
/*  618 */                 GriffonUdpHandler.this.oldNonProcessedData = new byte[this.data.length];
/*  619 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.oldNonProcessedData, 0, this.data.length);
/*  620 */                 this.remainingBytes = 0;
/*      */                 break;
/*      */               } 
/*  623 */               GriffonUdpHandler.this.oldNonProcessedData = null;
/*      */               
/*  625 */               if (GriffonUdpHandler.this.expBlockIndex == GriffonUdpHandler.this.rcvBlockIndex) {
/*  626 */                 GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 3];
/*  627 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.packet, 0, 3);
/*  628 */                 System.arraycopy(this.data, 3, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/*  629 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, 65535);
/*  630 */                 GriffonUdpHandler.this.tmp = new byte[2];
/*  631 */                 System.arraycopy(this.data, GriffonUdpHandler.this.blockLength + 3, GriffonUdpHandler.this.tmp, 0, 2);
/*  632 */                 this.remainingBytes -= GriffonUdpHandler.this.blockLength + 5;
/*  633 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp));
/*  634 */                 if (crcCalc == crcRecv) {
/*  635 */                   if (GriffonUdpHandler.this.expBlockIndex == 0) {
/*  636 */                     GriffonUdpHandler.this.fileContent = new byte[GriffonUdpHandler.this.currentVM.getVmLength()];
/*  637 */                     System.arraycopy(this.data, 7, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength - 4);
/*  638 */                     GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength - 4;
/*      */                   } else {
/*  640 */                     System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/*  641 */                     GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength;
/*      */                   } 
/*      */                   
/*  644 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  645 */                   GriffonUdpHandler.this.retry = 0;
/*  646 */                   GriffonUdpHandler.this.expBlockIndex++;
/*  647 */                   if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  648 */                     updateLastCommunicationModuleData();
/*  649 */                     GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/*  653 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  655 */                 if (GriffonUdpHandler.this.fileContentIndex < GriffonUdpHandler.this.currentVM.getVmLength() && GriffonUdpHandler.this.currentVM.getVmLength() > 0) {
/*  656 */                   waitGriffonResponse();
/*      */                 }
/*      */               } else {
/*  659 */                 this.remainingBytes -= 2;
/*  660 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  661 */                 GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  662 */                 prodBin = prodBin.substring(2);
/*  663 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  664 */                 GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/*  665 */                 if (GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) {
/*  666 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*  669 */                 } else if (GriffonUdpHandler.this.rcvBlockIndex < GriffonUdpHandler.this.expBlockIndex) {
/*  670 */                   GriffonUdpHandler.this.retry = 0;
/*  671 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  672 */                   this.remainingBytes = 0;
/*      */                 } else {
/*  674 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*  675 */                   this.remainingBytes = 0;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  680 */               GriffonUdpHandler.this.retry = 0;
/*  681 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  682 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*      */               
/*  684 */               requestNextVoiceMessageFromQueue();
/*      */             } 
/*  686 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.currentVM.getVmLength() && GriffonUdpHandler.this.currentVM.getVmLength() > 0) {
/*  687 */               GriffonDBManager.saveVoiceMessage(GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.currentVM.getVmLength(), GriffonUdpHandler.this.currentVM.getVmName(), GriffonUdpHandler.this.currentVM.getVmCRC32());
/*  688 */               requestNextVoiceMessageFromQueue();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case EXPECTED_REPLY:
/*  694 */             this.remainingBytes--;
/*  695 */             if (this.data[0] == 4) {
/*  696 */               GriffonUdpHandler.this.newCMDCheck = 0;
/*  697 */               if (!GriffonUdpHandler.this.commandModeActivated) {
/*  698 */                 GriffonUdpHandler.this.commandModeActivated = true;
/*      */               }
/*  700 */               if (GriffonUdpHandler.this.runtimeCommandsPending) {
/*  701 */                 GriffonUdpHandler.this.runtimeCommandsPending = false;
/*  702 */                 GriffonUdpHandler.this.last_80_sent_time = 0L;
/*      */               } 
/*  704 */               if (GriffonUdpHandler.this.crc32MatchedNUpdateStatusNotSent) {
/*  705 */                 if (sendUpdateStatusCommand())
/*  706 */                   waitGriffonResponse();  break;
/*      */               } 
/*  708 */               if (GriffonUdpHandler.this.requestedAllFilesCRC32 && GriffonUdpHandler.this.deviceCRC32 != null) {
/*  709 */                 if (sendFileSyncCommand(1))
/*  710 */                   waitGriffonResponse();  break;
/*      */               } 
/*  712 */               if (GriffonUdpHandler.this.disableDigitalPGMBuffer) {
/*  713 */                 if (sendEnableDisablePGMDigitalBuffer(0))
/*  714 */                   waitGriffonResponse();  break;
/*      */               } 
/*  716 */               if (GriffonUdpHandler.this.recordedLookupRequest) {
/*  717 */                 if (sendFileSyncCommand(7))
/*  718 */                   waitGriffonResponse(); 
/*      */                 break;
/*      */               } 
/*  721 */               if (processCommandPacket())
/*  722 */                 waitGriffonResponse(); 
/*      */               break;
/*      */             } 
/*  725 */             if (this.data[0] == 6) {
/*  726 */               GriffonUdpHandler.this.recordedAudioFilesCountInPrevPack = 0;
/*  727 */               if (GriffonUdpHandler.this.crc32MatchedNUpdateStatusNotSent || GriffonUdpHandler.this.sentCommand == 32785) {
/*  728 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.HANDLE_DASH_BOARD_BUFFER; break;
/*  729 */               }  if (GriffonUdpHandler.this.requestedAllFilesCRC32 && GriffonUdpHandler.this.sentCommand == 32774) {
/*  730 */                 if (GriffonUdpHandler.this.fileIDData == null) {
/*  731 */                   GriffonUdpHandler.this.fileIDData = new byte[((GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40) + 1];
/*  732 */                   for (int k = 1; k <= ((GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40); k++) {
/*  733 */                     GriffonUdpHandler.this.fileIDData[k] = (byte)k;
/*      */                   }
/*  735 */                   GriffonUdpHandler.this.fileIDData[0] = (byte)((GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40);
/*      */                 } 
/*  737 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.fileIDData, 0, GriffonUdpHandler.this.fileIDData.length, 65535);
/*  738 */                 byte[] fileIDDataCRC16 = new byte[GriffonUdpHandler.this.fileIDData.length + 2];
/*  739 */                 byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/*  740 */                 System.arraycopy(GriffonUdpHandler.this.fileIDData, 0, fileIDDataCRC16, 0, GriffonUdpHandler.this.fileIDData.length);
/*  741 */                 System.arraycopy(tmp2, 0, fileIDDataCRC16, GriffonUdpHandler.this.fileIDData.length, 2);
/*  742 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, fileIDDataCRC16);
/*  743 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Entered_into_configuration_sync_mode"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/*  744 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*  745 */                 GriffonUdpHandler.this.requestedAllFilesCRC32 = false;
/*  746 */                 GriffonUdpHandler.this.fileIDsSent = true;
/*  747 */                 waitGriffonResponse(); break;
/*  748 */               }  if (GriffonUdpHandler.this.fileIDsSent) {
/*  749 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.CFG_FILE_RECEIVING;
/*  750 */                 GriffonUdpHandler.this.fileIDsSent = false; break;
/*  751 */               }  if (GriffonUdpHandler.this.disableDigitalPGMBuffer) {
/*  752 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  753 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*  754 */                 GriffonUdpHandler.this.disableDigitalPGMBuffer = false; break;
/*  755 */               }  if (GriffonUdpHandler.this.recordedLookupRequest || GriffonUdpHandler.this.recordListLookupRequest2) {
/*  756 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.RECORDED_AUDIO_FILE_COUNT_READ;
/*  757 */                 GriffonUdpHandler.this.recordedLookupRequest = false;
/*  758 */                 GriffonUdpHandler.this.recordListLookupRequest2 = false;
/*      */                 
/*  760 */                 this.remainingBytes--;
/*  761 */                 if (this.data[1] > 0)
/*  762 */                   GriffonUdpHandler.this.recordedAudioFilesCountInPrevPack = this.data[1]; 
/*      */               }  break;
/*      */             } 
/*  765 */             if (this.data[0] == 21) {
/*  766 */               this.running = false; break;
/*  767 */             }  if (this.data.length >= 2) {
/*  768 */               this.remainingBytes--;
/*  769 */               prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  770 */               GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  771 */               prodBin = prodBin.substring(2);
/*  772 */               prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  773 */               GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/*  774 */               if ((GriffonUdpHandler.this.productID != Util.EnumProductIDs.GRIFFON_V1.getProductId() && GriffonUdpHandler.this.productID != Util.EnumProductIDs.GRIFFON_V2.getProductId()) || 
/*  775 */                 !processM2SPacket(true));
/*      */               
/*      */               break;
/*      */             } 
/*      */             
/*  780 */             this.running = false;
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case RECORDED_AUDIO_FILE_COUNT_READ:
/*  786 */             this.remainingBytes--;
/*  787 */             GriffonUdpHandler.this.recordedAudioFilesCount = this.data[0] & 0xFF;
/*  788 */             if (GriffonUdpHandler.this.recordedAudioFilesCount <= 0) {
/*  789 */               GriffonUdpHandler.this.recordedAudioFilesCount = GriffonUdpHandler.this.recordedAudioFilesCountInPrevPack;
/*      */             }
/*  791 */             if (GriffonUdpHandler.this.recordedAudioFilesCount > 0) {
/*  792 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*  793 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  794 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.RECORDED_AUDIO_FILE_LOOKUP_RECEIVING;
/*  795 */               waitGriffonResponse(); break;
/*      */             } 
/*  797 */             GriffonDBManager.updateRecordedFileLookupData(null, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.recAudioLookupCRC32);
/*  798 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  799 */             GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case RECORDED_AUDIO_FILE_LOOKUP_RECEIVING:
/*  805 */             if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/*  806 */               GriffonUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  807 */               System.arraycopy(this.data, 0, GriffonUdpHandler.this.bid, 0, 2);
/*  808 */               GriffonUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.bid));
/*  809 */               if (GriffonUdpHandler.this.blockLength > this.data.length) {
/*  810 */                 GriffonUdpHandler.this.oldNonProcessedData = new byte[this.data.length];
/*  811 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.oldNonProcessedData, 0, this.data.length);
/*  812 */                 this.remainingBytes = 0;
/*      */                 break;
/*      */               } 
/*  815 */               GriffonUdpHandler.this.oldNonProcessedData = null;
/*      */               
/*  817 */               if (GriffonUdpHandler.this.expBlockIndex == GriffonUdpHandler.this.rcvBlockIndex) {
/*  818 */                 GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 3];
/*  819 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.packet, 0, 3);
/*  820 */                 System.arraycopy(this.data, 3, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/*  821 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, 65535);
/*  822 */                 GriffonUdpHandler.this.tmp = new byte[2];
/*  823 */                 System.arraycopy(this.data, GriffonUdpHandler.this.blockLength + 3, GriffonUdpHandler.this.tmp, 0, 2);
/*  824 */                 this.remainingBytes -= GriffonUdpHandler.this.blockLength + 5;
/*  825 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp));
/*  826 */                 if (crcCalc == crcRecv) {
/*  827 */                   if (GriffonUdpHandler.this.expBlockIndex == 0) {
/*  828 */                     GriffonUdpHandler.this.fileContent = new byte[GriffonUdpHandler.this.recordedAudioFilesCount * 16];
/*  829 */                     GriffonUdpHandler.this.flen = GriffonUdpHandler.this.recordedAudioFilesCount * 16;
/*      */                   } 
/*  831 */                   System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/*  832 */                   GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength;
/*  833 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  834 */                   GriffonUdpHandler.this.retry = 0;
/*  835 */                   GriffonUdpHandler.this.expBlockIndex++;
/*  836 */                   if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  837 */                     updateLastCommunicationModuleData();
/*  838 */                     GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/*  842 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  844 */                 if (GriffonUdpHandler.this.fileContentIndex < GriffonUdpHandler.this.flen && GriffonUdpHandler.this.flen > 0) {
/*  845 */                   waitGriffonResponse();
/*      */                 }
/*      */               } else {
/*  848 */                 this.remainingBytes -= 2;
/*  849 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  850 */                 GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  851 */                 prodBin = prodBin.substring(2);
/*  852 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  853 */                 GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/*  854 */                 if (GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) {
/*  855 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*  858 */                 } else if (GriffonUdpHandler.this.rcvBlockIndex < GriffonUdpHandler.this.expBlockIndex) {
/*  859 */                   GriffonUdpHandler.this.retry = 0;
/*  860 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  861 */                   this.remainingBytes = 0;
/*      */                 } else {
/*  863 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*  864 */                   this.remainingBytes = 0;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  869 */               GriffonUdpHandler.this.retry = 0;
/*  870 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  871 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*      */             } 
/*  873 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.flen && GriffonUdpHandler.this.flen > 0) {
/*  874 */               GriffonUdpHandler.this.retry = 0;
/*  875 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  876 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*  877 */               GriffonHandlerHelper.parseRecordedFileLookupData(GriffonUdpHandler.this.fileContent, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.recordedAudioFilesCount, GriffonUdpHandler.this.recAudioLookupCRC32);
/*      */             } 
/*  879 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  880 */             this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout;
/*  881 */             GriffonUdpHandler.this.iTimeout = this.idleTimeout;
/*  882 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.flen && GriffonUdpHandler.this.flen > 0) {
/*  883 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*      */               
/*  885 */               GriffonUdpHandler.this.recordListLookupRequest2 = false;
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case CFG_FILE_RECEIVING:
/*  891 */             if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/*  892 */               GriffonUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  893 */               System.arraycopy(this.data, 0, GriffonUdpHandler.this.bid, 0, 2);
/*  894 */               GriffonUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.bid));
/*  895 */               if (GriffonUdpHandler.this.blockLength > this.data.length) {
/*  896 */                 GriffonUdpHandler.this.oldNonProcessedData = new byte[this.data.length];
/*  897 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.oldNonProcessedData, 0, this.data.length);
/*  898 */                 this.remainingBytes = 0;
/*      */                 break;
/*      */               } 
/*  901 */               GriffonUdpHandler.this.oldNonProcessedData = null;
/*  902 */               if (GriffonUdpHandler.this.expBlockIndex == GriffonUdpHandler.this.rcvBlockIndex) {
/*  903 */                 GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 3];
/*  904 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.packet, 0, 3);
/*  905 */                 System.arraycopy(this.data, 3, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/*  906 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, 65535);
/*  907 */                 GriffonUdpHandler.this.tmp = new byte[2];
/*  908 */                 System.arraycopy(this.data, GriffonUdpHandler.this.blockLength + 3, GriffonUdpHandler.this.tmp, 0, 2);
/*  909 */                 this.remainingBytes -= GriffonUdpHandler.this.blockLength + 5;
/*  910 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp));
/*  911 */                 if (crcCalc == crcRecv) {
/*  912 */                   if (GriffonUdpHandler.this.expBlockIndex == 0) {
/*  913 */                     GriffonUdpHandler.this.fid[0] = this.data[0];
/*  914 */                     GriffonUdpHandler.this.fid[1] = this.data[1];
/*  915 */                     System.arraycopy(this.data, 3, GriffonUdpHandler.this.first16, 0, 16);
/*  916 */                     GriffonUdpHandler.this.first16 = Rijndael.decryptBytes(GriffonUdpHandler.this.first16, Rijndael.aes_256, false);
/*  917 */                     System.arraycopy(GriffonUdpHandler.this.first16, 2, GriffonUdpHandler.this.tmp4, 0, 4);
/*  918 */                     GriffonUdpHandler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/*  919 */                     GriffonUdpHandler.this.lPad = (GriffonUdpHandler.this.flen + 10) % 16;
/*  920 */                     if (GriffonUdpHandler.this.lPad > 0) {
/*  921 */                       GriffonUdpHandler.this.lPad = 16 - GriffonUdpHandler.this.lPad;
/*      */                     }
/*  923 */                     System.arraycopy(GriffonUdpHandler.this.first16, 6, GriffonUdpHandler.this.tmp4, 0, 4);
/*  924 */                     GriffonUdpHandler.this.recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/*  925 */                     GriffonUdpHandler.this.fileContent = new byte[GriffonUdpHandler.this.flen + GriffonUdpHandler.this.lPad + 10];
/*  926 */                     GriffonUdpHandler.this.fileContentIndex = 0;
/*      */                   } 
/*  928 */                   System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/*  929 */                   GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength;
/*  930 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  931 */                   GriffonUdpHandler.this.retry = 0;
/*  932 */                   GriffonUdpHandler.this.expBlockIndex++;
/*  933 */                   if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  934 */                     updateLastCommunicationModuleData();
/*  935 */                     this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout;
/*  936 */                     GriffonUdpHandler.this.iTimeout = this.idleTimeout;
/*  937 */                     GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/*  941 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  943 */                 if (GriffonUdpHandler.this.fileContentIndex < GriffonUdpHandler.this.flen + GriffonUdpHandler.this.lPad + 10 && GriffonUdpHandler.this.flen > 0) {
/*  944 */                   waitGriffonResponse();
/*      */                 }
/*      */               } else {
/*  947 */                 this.remainingBytes -= 2;
/*  948 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  949 */                 GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  950 */                 prodBin = prodBin.substring(2);
/*  951 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  952 */                 GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/*  953 */                 if (GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) {
/*  954 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*  957 */                 } else if (GriffonUdpHandler.this.rcvBlockIndex < GriffonUdpHandler.this.expBlockIndex) {
/*  958 */                   GriffonUdpHandler.this.retry = 0;
/*  959 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/*  960 */                   this.remainingBytes = 0;
/*      */                 } else {
/*  962 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*  963 */                   this.remainingBytes = 0;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  968 */               GriffonUdpHandler.this.retry = 0;
/*  969 */               GriffonUdpHandler.this.fileContentIndex = 0;
/*  970 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*      */               
/*  972 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*  973 */               GriffonUdpHandler.this.requestedAllFilesCRC32 = false;
/*      */             } 
/*  975 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.flen + GriffonUdpHandler.this.lPad + 10 && GriffonUdpHandler.this.flen > 0) {
/*  976 */               byte[] decData = new byte[GriffonUdpHandler.this.flen + GriffonUdpHandler.this.lPad + 10];
/*      */               
/*  978 */               byte[] encBlock = new byte[16];
/*  979 */               if (GriffonUdpHandler.this.fileContent.length >= 16 && GriffonUdpHandler.this.fileContent.length % 16 == 0) {
/*  980 */                 for (int i = 0; i < GriffonUdpHandler.this.fileContent.length; ) {
/*  981 */                   System.arraycopy(GriffonUdpHandler.this.fileContent, i, encBlock, 0, 16);
/*  982 */                   byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/*  983 */                   System.arraycopy(arrayOfByte, 0, decData, i, 16);
/*  984 */                   i += 16;
/*      */                 } 
/*      */               }
/*  987 */               byte[] decBlock = new byte[GriffonUdpHandler.this.flen];
/*  988 */               System.arraycopy(decData, 10, decBlock, 0, GriffonUdpHandler.this.flen);
/*  989 */               int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/*  990 */               if (GriffonUdpHandler.this.recvCfgCRC32 == calcCfgCrc32) {
/*  991 */                 this.newModuleCFG = GriffonHandlerHelper.rearrangeServerCFGCopy(GriffonUdpHandler.this.productID, decBlock, GriffonUdpHandler.this.syncCFG, GriffonUdpHandler.this.flen, GriffonUdpHandler.this.fileIDData);
/*  992 */                 this.newModuleCFG.setIdModule(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*  993 */                 if (GriffonUdpHandler.this.sp24DH != null) {
/*  994 */                   GriffonHandlerHelper.finalizeReceiveCFGFileCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, this.newModuleCFG, null);
/*  995 */                   if (GriffonUdpHandler.this.deviceCRC32 != null) {
/*  996 */                     this.newModuleCFG.setCrc32List(GriffonUdpHandler.this.deviceCRC32);
/*      */                   }
/*  998 */                   GriffonDBManager.updateGriffonModuleCfg(this.newModuleCFG);
/*  999 */                   Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonUdpHandler.this.sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1000 */                   endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/*      */                 } else {
/* 1002 */                   Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Configuration_sync_done_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1003 */                   if (GriffonUdpHandler.this.deviceCRC32 != null) {
/* 1004 */                     this.newModuleCFG.setCrc32List(GriffonUdpHandler.this.deviceCRC32);
/*      */                   }
/* 1006 */                   GriffonDBManager.updateGriffonModuleCfg(this.newModuleCFG);
/*      */                 } 
/* 1008 */                 GriffonHandlerHelper.parseConfigurationData(this.newModuleCFG, GriffonUdpHandler.this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient);
/* 1009 */                 GriffonUdpHandler.this.fileContentIndex = 0;
/* 1010 */                 GriffonUdpHandler.this.expBlockIndex = 0;
/* 1011 */                 if (GriffonUdpHandler.this.sp24DH == null && GriffonUdpHandler.this.fileSync_80_Sent) {
/* 1012 */                   if (sendEMFWCommand()) {
/* 1013 */                     waitGriffonResponse();
/*      */                   }
/*      */                 } else {
/* 1016 */                   GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*      */                 } 
/*      */               } else {
/* 1019 */                 if (GriffonUdpHandler.this.sp24DH != null) {
/* 1020 */                   GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + GriffonUdpHandler.this.sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("invalid"), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */                 } else {
/* 1022 */                   GriffonUdpHandler.this.fileSync_80_Sent = false;
/* 1023 */                   Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("CRC_32_of_the_received_configuration_file_is_invalid"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/*      */                 } 
/* 1025 */                 GriffonUdpHandler.this.fileContentIndex = 0;
/* 1026 */                 GriffonUdpHandler.this.expBlockIndex = 0;
/*      */               } 
/*      */               
/* 1029 */               if (GriffonUdpHandler.this.sp24DH != null) {
/* 1030 */                 sendNextCommandFromQueue();
/*      */               }
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case FILE_SENDING_INITIATED:
/* 1037 */             this.remainingBytes--;
/* 1038 */             if (this.data[0] == 6) {
/* 1039 */               if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773) {
/* 1040 */                 if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1' && GriffonUdpHandler.this.fileIDData != null && GriffonUdpHandler.this.fileIDData.length > 1) {
/* 1041 */                   GriffonUdpHandler.this.sp24DH.setCommandFileData(GriffonHandlerHelper.prepareRequiredFileDataForDeviceByCRCMismatch(GriffonUdpHandler.this.productID, GriffonUdpHandler.this.syncCFG, GriffonUdpHandler.this.fileIDData));
/* 1042 */                   sendFile2Module(true);
/* 1043 */                   GriffonUdpHandler.this.blockIndex++;
/* 1044 */                   GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.FILE_SENDING;
/* 1045 */                   waitGriffonResponse(); break;
/*      */                 } 
/* 1047 */                 if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '3' || GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '4') {
/* 1048 */                   byte[] fwLen = Functions.swapLSB2MSB4ByteArray(Functions.get4ByteArrayFromInt((GriffonUdpHandler.this.sp24DH.getCommandFileData()).length));
/* 1049 */                   int crcFw = CRC16.calculate(fwLen, 0, 4, 65535);
/* 1050 */                   byte[] fwLenData = new byte[6];
/* 1051 */                   System.arraycopy(fwLen, 0, fwLenData, 0, 4);
/* 1052 */                   fwLen = Functions.get2ByteArrayFromInt(crcFw);
/* 1053 */                   fwLenData[4] = fwLen[1];
/* 1054 */                   fwLenData[5] = fwLen[0];
/* 1055 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, fwLenData);
/* 1056 */                   GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.FIRMWARE_CRC_RESPONSE;
/* 1057 */                   waitGriffonResponse(); break;
/*      */                 } 
/* 1059 */                 sendFile2Module(true);
/* 1060 */                 GriffonUdpHandler.this.blockIndex++;
/* 1061 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.FILE_SENDING;
/* 1062 */                 waitGriffonResponse();
/*      */               } 
/*      */               break;
/*      */             } 
/* 1066 */             if (this.data[0] == 21) {
/* 1067 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/* 1068 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case FILE_SENDING:
/* 1074 */             this.remainingBytes--;
/* 1075 */             if (this.data[0] == 6 && GriffonUdpHandler.this.fc != null) {
/* 1076 */               if (GriffonUdpHandler.this.fc.position() < GriffonUdpHandler.this.flen) {
/* 1077 */                 GriffonUdpHandler.this.waitForSecondAck = false;
/* 1078 */                 GriffonUdpHandler.this.retry = 0;
/* 1079 */                 this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout;
/* 1080 */                 GriffonUdpHandler.this.iTimeout = this.idleTimeout;
/* 1081 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + GriffonHandlerHelper.getFileNameByCommandData(Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10), GriffonUdpHandler.this.sp24DH.getCommand_Type()) + " (" + GriffonUdpHandler.this.fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1082 */                 sendFile2Module(false);
/* 1083 */                 GriffonUdpHandler.this.blockIndex++;
/* 1084 */                 GriffonUdpHandler.this.responseTimeout = 120000;
/* 1085 */                 waitGriffonResponse(); break;
/*      */               } 
/* 1087 */               if (this.remainingBytes > 0) {
/* 1088 */                 this.remainingBytes--;
/* 1089 */                 if (this.data[1] == 6) {
/* 1090 */                   GriffonUdpHandler.this.fc.close();
/* 1091 */                   GriffonUdpHandler.this.fc = null;
/* 1092 */                   GriffonUdpHandler.this.blockIndex = 0;
/* 1093 */                   GriffonUdpHandler.this.flen = 0;
/* 1094 */                   endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1095 */                   if (GriffonUdpHandler.this.file != null && GriffonUdpHandler.this.file.exists()) {
/* 1096 */                     GriffonUdpHandler.this.file.delete();
/*      */                   }
/* 1098 */                   if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*      */                     
/* 1100 */                     sendLogoutCommand();
/*      */                     break;
/*      */                   } 
/* 1103 */                   sendNextCommandFromQueue();
/*      */                 } 
/*      */                 break;
/*      */               } 
/* 1107 */               if (GriffonUdpHandler.this.waitForSecondAck) {
/* 1108 */                 GriffonUdpHandler.this.waitForSecondAck = false;
/* 1109 */                 GriffonUdpHandler.this.fc.close();
/* 1110 */                 GriffonUdpHandler.this.fc = null;
/* 1111 */                 GriffonUdpHandler.this.blockIndex = 0;
/* 1112 */                 GriffonUdpHandler.this.flen = 0;
/* 1113 */                 endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1114 */                 if (GriffonUdpHandler.this.file != null && GriffonUdpHandler.this.file.exists()) {
/* 1115 */                   GriffonUdpHandler.this.file.delete();
/*      */                 }
/* 1117 */                 if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*      */                   
/* 1119 */                   sendLogoutCommand();
/*      */                   break;
/*      */                 } 
/* 1122 */                 sendNextCommandFromQueue();
/*      */                 break;
/*      */               } 
/* 1125 */               GriffonUdpHandler.this.waitForSecondAck = true;
/* 1126 */               GriffonUdpHandler.this.responseTimeout = 120000;
/* 1127 */               waitGriffonResponse();
/*      */               
/*      */               break;
/*      */             } 
/* 1131 */             if (this.data[0] == 21) {
/* 1132 */               if (GriffonUdpHandler.this.fc.position() < GriffonUdpHandler.this.flen) {
/* 1133 */                 if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/* 1134 */                   if (GriffonUdpHandler.this.lCommIface == 1) {
/* 1135 */                     if (GriffonUdpHandler.this.retry == 0) {
/* 1136 */                       GriffonUdpHandler.this.responseTimeout = 120000;
/* 1137 */                     } else if (GriffonUdpHandler.this.retry == 1) {
/* 1138 */                       GriffonUdpHandler.this.responseTimeout = 210000;
/* 1139 */                     } else if (GriffonUdpHandler.this.retry == 2) {
/* 1140 */                       GriffonUdpHandler.this.responseTimeout = 300000;
/*      */                     } 
/*      */                   } else {
/* 1143 */                     GriffonUdpHandler.this.responseTimeout = 120000;
/*      */                   } 
/* 1145 */                   GriffonUdpHandler.this.blockIndex--;
/* 1146 */                   if (GriffonUdpHandler.this.blockIndex < 0) {
/* 1147 */                     GriffonUdpHandler.this.blockIndex = 0;
/*      */                   }
/* 1149 */                   GriffonUdpHandler.this.fc.position(GriffonUdpHandler.this.fc.position() - GriffonUdpHandler.this.blockLength);
/* 1150 */                   sendFile2Module(false);
/* 1151 */                   GriffonUdpHandler.this.blockIndex++;
/* 1152 */                   waitGriffonResponse(); break;
/*      */                 } 
/* 1154 */                 GriffonUdpHandler.this.retry = 0;
/* 1155 */                 GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */                 break;
/*      */               } 
/* 1158 */               GriffonUdpHandler.this.fc.close();
/* 1159 */               GriffonUdpHandler.this.fc = null;
/* 1160 */               GriffonUdpHandler.this.blockIndex = 0;
/* 1161 */               GriffonUdpHandler.this.flen = 0;
/* 1162 */               if (this.data[0] == 20) {
/* 1163 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_module_informed_that_CRC-32_is_not_matching_for_the_file_[") + GriffonUdpHandler.this.sp24DH.getCommandData() + "]", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1164 */                 GriffonHandlerHelper.endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1165 */               } else if (this.data[0] == 18) {
/* 1166 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_module_informed_that_received_configuration_file_is_invalid_[") + GriffonUdpHandler.this.sp24DH.getCommandData() + "]", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1167 */                 GriffonHandlerHelper.endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/*      */               } 
/* 1169 */               if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1170 */                 updateLastCommunicationModuleData();
/* 1171 */                 GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 1173 */               if (GriffonUdpHandler.this.file != null && GriffonUdpHandler.this.file.exists()) {
/* 1174 */                 GriffonUdpHandler.this.file.delete();
/*      */               }
/* 1176 */               if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') {
/*      */                 
/* 1178 */                 sendLogoutCommand();
/*      */                 break;
/*      */               } 
/* 1181 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case NEW_COMMAND_RESPONSE:
/* 1189 */             this.remainingBytes--;
/* 1190 */             if (this.data[0] == 4) {
/* 1191 */               GriffonUdpHandler.this.newCMDCheck = 0;
/* 1192 */               if (!GriffonUdpHandler.this.commandModeActivated) {
/* 1193 */                 GriffonUdpHandler.this.commandModeActivated = true;
/*      */               }
/* 1195 */               if (processCommandPacket()) {
/* 1196 */                 waitGriffonResponse();
/*      */               }
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case COMMAND_PACKET_REPSONE:
/* 1203 */             this.remainingBytes--;
/* 1204 */             if (this.data[0] == 6) {
/* 1205 */               endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1206 */               sendNextCommandFromQueue(); break;
/* 1207 */             }  if (this.data[0] == 21) {
/* 1208 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/* 1209 */               sendNextCommandFromQueue(); break;
/*      */             } 
/* 1211 */             if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32781 && this.data[0] >= 21 && this.data[0] <= 30) {
/* 1212 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1213 */               GriffonDBManager.updateCommandFailureStatus(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getCommandData() + ";" + (this.data[0] & 0xFF)); break;
/* 1214 */             }  if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32780 && this.data[0] >= 21 && this.data[0] <= 54) {
/* 1215 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1216 */               GriffonDBManager.updateCommandFailureStatus(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getCommandData() + ";" + (this.data[0] & 0xFF));
/* 1217 */               GriffonHandlerHelper.updateCommandFailureStatus2App(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient, GriffonUdpHandler.this.sp24DH); break;
/* 1218 */             }  if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32770 && this.data[0] >= 21 && this.data[0] <= 22) {
/* 1219 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1220 */               GriffonDBManager.updateCommandFailureStatus(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getCommandData()); break;
/* 1221 */             }  if (this.data[0] == 4) {
/*      */               break;
/*      */             }
/* 1224 */             GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case M2S_PACKET_PARSING:
/* 1231 */             processM2SPacket(true);
/*      */             break;
/*      */ 
/*      */           
/*      */           case HANDLE_DASH_BOARD_BUFFER:
/* 1236 */             this.module = GriffonHandlerHelper.handleDashboardBuffer(this.data, this.module, GriffonUdpHandler.this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/* 1237 */             GriffonDBManager.executeSPG_005(this.module);
/* 1238 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1239 */             GriffonUdpHandler.this.crc32MatchedNUpdateStatusNotSent = false;
/* 1240 */             this.remainingBytes -= this.data.length;
/* 1241 */             if (GriffonUdpHandler.this.recordedLookupRequest) {
/* 1242 */               if (sendFileSyncCommand(7)) {
/* 1243 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/* 1244 */                 waitGriffonResponse();
/* 1245 */                 GriffonUdpHandler.this.recordedLookupRequest = false;
/*      */               }  break;
/*      */             } 
/* 1248 */             GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case RECORD_AUDIO_FILE_READ_INITIATED:
/* 1254 */             this.remainingBytes--;
/* 1255 */             if (this.data[0] == 6) {
/* 1256 */               String[] rad = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 1257 */               if (rad != null && rad.length == 3) {
/* 1258 */                 byte[] vmcmd = new byte[15];
/* 1259 */                 vmcmd[0] = 0;
/* 1260 */                 System.arraycopy(rad[2].getBytes("ISO-8859-1"), 0, vmcmd, 1, rad[2].length());
/* 1261 */                 int crcCalc = CRC16.calculate(vmcmd, 0, 13, 65535);
/* 1262 */                 byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1263 */                 System.arraycopy(tmp2, 0, vmcmd, 13, 2);
/* 1264 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, vmcmd);
/* 1265 */                 GriffonUdpHandler.this.currentVM = new VoiceMessage();
/* 1266 */                 GriffonUdpHandler.this.currentVM.setVmLength(Integer.parseInt(rad[1]));
/* 1267 */                 GriffonUdpHandler.this.currentVM.setVmName(rad[2]);
/* 1268 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.RECORD_AUDIO_FILE_READ_COMMAND;
/* 1269 */                 waitGriffonResponse();
/*      */               }  break;
/* 1271 */             }  if (this.data[0] == 21);
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case RECORD_AUDIO_FILE_READ_COMMAND:
/* 1278 */             this.remainingBytes--;
/* 1279 */             if (this.data[0] == 6) {
/* 1280 */               if (GriffonUdpHandler.this.currentVM != null) {
/* 1281 */                 GriffonUdpHandler.this.fileContentIndex = 0;
/* 1282 */                 GriffonUdpHandler.this.retry = 0;
/* 1283 */                 GriffonUdpHandler.this.expBlockIndex = 0;
/* 1284 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.RECORD_AUDIO_FILE_READ_IN_PROGRESS;
/* 1285 */                 waitGriffonResponse();
/*      */               }  break;
/* 1287 */             }  if (this.data[0] == 21) {
/* 1288 */               GriffonDBManager.updateCommandFailureStatus(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getCommandData() + ";" + (this.data[0] & 0xFF));
/* 1289 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case RECORD_AUDIO_FILE_READ_IN_PROGRESS:
/* 1295 */             if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/* 1296 */               GriffonUdpHandler.this.blockLength = this.data[2] & 0xFF;
/* 1297 */               System.arraycopy(this.data, 0, GriffonUdpHandler.this.bid, 0, 2);
/* 1298 */               GriffonUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.bid));
/* 1299 */               if (GriffonUdpHandler.this.blockLength > this.data.length) {
/* 1300 */                 GriffonUdpHandler.this.oldNonProcessedData = new byte[this.data.length];
/* 1301 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.oldNonProcessedData, 0, this.data.length);
/* 1302 */                 this.remainingBytes = 0;
/*      */                 break;
/*      */               } 
/* 1305 */               GriffonUdpHandler.this.oldNonProcessedData = null;
/* 1306 */               if (GriffonUdpHandler.this.expBlockIndex == GriffonUdpHandler.this.rcvBlockIndex) {
/* 1307 */                 GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 3];
/* 1308 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.packet, 0, 3);
/* 1309 */                 System.arraycopy(this.data, 3, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/* 1310 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, 65535);
/* 1311 */                 GriffonUdpHandler.this.tmp = new byte[2];
/* 1312 */                 System.arraycopy(this.data, GriffonUdpHandler.this.blockLength + 3, GriffonUdpHandler.this.tmp, 0, 2);
/* 1313 */                 this.remainingBytes -= GriffonUdpHandler.this.blockLength + 5;
/* 1314 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp));
/* 1315 */                 if (crcCalc == crcRecv) {
/* 1316 */                   if (GriffonUdpHandler.this.expBlockIndex == 0) {
/* 1317 */                     GriffonUdpHandler.this.fileContent = new byte[GriffonUdpHandler.this.currentVM.getVmLength()];
/* 1318 */                     System.arraycopy(this.data, 7, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength - 4);
/* 1319 */                     GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength - 4;
/*      */                   } else {
/* 1321 */                     System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/* 1322 */                     GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength;
/*      */                   } 
/*      */                   
/* 1325 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1326 */                   GriffonUdpHandler.this.retry = 0;
/*      */                   
/* 1328 */                   GriffonUdpHandler.this.expBlockIndex++;
/* 1329 */                   if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1330 */                     updateLastCommunicationModuleData();
/* 1331 */                     GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/* 1335 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/* 1337 */                 if (GriffonUdpHandler.this.fileContentIndex < GriffonUdpHandler.this.currentVM.getVmLength() && GriffonUdpHandler.this.currentVM.getVmLength() > 0) {
/* 1338 */                   waitGriffonResponse();
/*      */                 }
/*      */               } else {
/* 1341 */                 this.remainingBytes -= 2;
/* 1342 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/* 1343 */                 GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1344 */                 prodBin = prodBin.substring(2);
/* 1345 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/* 1346 */                 GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/* 1347 */                 if (GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) {
/* 1348 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/* 1351 */                 } else if (GriffonUdpHandler.this.rcvBlockIndex < GriffonUdpHandler.this.expBlockIndex) {
/* 1352 */                   GriffonUdpHandler.this.retry = 0;
/* 1353 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1354 */                   this.remainingBytes = 0;
/*      */                 } else {
/* 1356 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 1357 */                   this.remainingBytes = 0;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/* 1362 */               GriffonUdpHandler.this.retry = 0;
/* 1363 */               GriffonUdpHandler.this.fileContentIndex = 0;
/* 1364 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*      */               
/* 1366 */               requestNextVoiceMessageFromQueue();
/*      */             } 
/* 1368 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.currentVM.getVmLength() && GriffonUdpHandler.this.currentVM.getVmLength() > 0) {
/*      */               
/* 1370 */               GriffonUdpHandler.this.retry = 0;
/* 1371 */               GriffonUdpHandler.this.fileContentIndex = 0;
/* 1372 */               GriffonUdpHandler.this.expBlockIndex = 0;
/* 1373 */               GriffonDBManager.updateCommandFileData(GriffonUdpHandler.this.sp24DH.getId_Command(), new ByteArrayInputStream(GriffonUdpHandler.this.fileContent));
/* 1374 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonUdpHandler.this.currentVM.getVmName() + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1375 */               GriffonHandlerHelper.endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1376 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case EVENT_LOG_FILE_INITIATED:
/* 1382 */             this.remainingBytes--;
/* 1383 */             if (this.data[0] == 6) {
/* 1384 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EVENT_LOG_FILE_RECEIVING;
/* 1385 */               GriffonUdpHandler.this.retry = 0;
/* 1386 */               GriffonUdpHandler.this.flen = 0;
/* 1387 */               GriffonUdpHandler.this.expBlockIndex = 0;
/* 1388 */               GriffonUdpHandler.this.fileContent = null;
/* 1389 */               waitGriffonResponse(); break;
/* 1390 */             }  if (this.data[0] == 21);
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case EVENT_LOG_FILE_RECEIVING:
/* 1397 */             if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/* 1398 */               GriffonUdpHandler.this.blockLength = this.data[2] & 0xFF;
/* 1399 */               System.arraycopy(this.data, 0, GriffonUdpHandler.this.bid, 0, 2);
/* 1400 */               GriffonUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.bid));
/* 1401 */               if (GriffonUdpHandler.this.blockLength > this.data.length) {
/* 1402 */                 GriffonUdpHandler.this.oldNonProcessedData = new byte[this.data.length];
/* 1403 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.oldNonProcessedData, 0, this.data.length);
/* 1404 */                 this.remainingBytes = 0;
/*      */                 break;
/*      */               } 
/* 1407 */               GriffonUdpHandler.this.oldNonProcessedData = null;
/*      */               
/* 1409 */               if (GriffonUdpHandler.this.expBlockIndex == GriffonUdpHandler.this.rcvBlockIndex) {
/* 1410 */                 GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 3];
/* 1411 */                 System.arraycopy(this.data, 0, GriffonUdpHandler.this.packet, 0, 3);
/* 1412 */                 System.arraycopy(this.data, 3, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/* 1413 */                 int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, 65535);
/* 1414 */                 GriffonUdpHandler.this.tmp = new byte[2];
/* 1415 */                 System.arraycopy(this.data, GriffonUdpHandler.this.blockLength + 3, GriffonUdpHandler.this.tmp, 0, 2);
/* 1416 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp));
/* 1417 */                 this.remainingBytes -= GriffonUdpHandler.this.blockLength + 5;
/* 1418 */                 if (crcCalc == crcRecv) {
/* 1419 */                   if (GriffonUdpHandler.this.expBlockIndex == 0) {
/* 1420 */                     System.arraycopy(this.data, 5, GriffonUdpHandler.this.tmp4, 0, 4);
/* 1421 */                     GriffonUdpHandler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/* 1422 */                     System.arraycopy(this.data, 9, GriffonUdpHandler.this.tmp4, 0, 4);
/* 1423 */                     GriffonUdpHandler.this.recvLogCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/* 1424 */                     GriffonUdpHandler.this.fileContent = new byte[GriffonUdpHandler.this.flen + 10];
/* 1425 */                     GriffonUdpHandler.this.fileContentIndex = 0;
/* 1426 */                     if (GriffonUdpHandler.this.flen == 0) {
/* 1427 */                       UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1428 */                       System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/* 1429 */                       GriffonDBManager.updateCommandFileData(GriffonUdpHandler.this.sp24DH.getId_Command(), new ByteArrayInputStream(GriffonUdpHandler.this.fileContent));
/* 1430 */                       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonHandlerHelper.getReceiveLogFileName(Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10)) + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1431 */                       GriffonHandlerHelper.endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1432 */                       sendNextCommandFromQueue();
/*      */                     } 
/*      */                   } 
/* 1435 */                   System.arraycopy(this.data, 3, GriffonUdpHandler.this.fileContent, GriffonUdpHandler.this.fileContentIndex, GriffonUdpHandler.this.blockLength);
/* 1436 */                   GriffonUdpHandler.this.fileContentIndex += GriffonUdpHandler.this.blockLength;
/* 1437 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1438 */                   GriffonUdpHandler.this.retry = 0;
/* 1439 */                   GriffonUdpHandler.this.expBlockIndex++;
/* 1440 */                   if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1441 */                     updateLastCommunicationModuleData();
/* 1442 */                     GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/* 1445 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/* 1447 */                 if (GriffonUdpHandler.this.fileContentIndex < GriffonUdpHandler.this.flen + 10 && GriffonUdpHandler.this.flen > 0) {
/* 1448 */                   waitGriffonResponse();
/*      */                 }
/*      */               } else {
/* 1451 */                 this.remainingBytes -= 2;
/* 1452 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/* 1453 */                 GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1454 */                 prodBin = prodBin.substring(2);
/* 1455 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/* 1456 */                 GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/* 1457 */                 if (GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) {
/* 1458 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/* 1461 */                 } else if (GriffonUdpHandler.this.rcvBlockIndex < GriffonUdpHandler.this.expBlockIndex) {
/* 1462 */                   GriffonUdpHandler.this.retry = 0;
/* 1463 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1464 */                   this.remainingBytes = 0;
/*      */                 } else {
/* 1466 */                   UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 1467 */                   this.remainingBytes = 0;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/* 1472 */               GriffonUdpHandler.this.retry = 0;
/* 1473 */               GriffonUdpHandler.this.fileContentIndex = 0;
/* 1474 */               GriffonUdpHandler.this.expBlockIndex = 0;
/*      */             } 
/* 1476 */             if (GriffonUdpHandler.this.fileContentIndex >= GriffonUdpHandler.this.flen + 10 && GriffonUdpHandler.this.flen > 0) {
/*      */               
/* 1478 */               GriffonUdpHandler.this.retry = 0;
/* 1479 */               GriffonUdpHandler.this.fileContentIndex = 0;
/* 1480 */               GriffonUdpHandler.this.expBlockIndex = 0;
/* 1481 */               byte[] decData = new byte[GriffonUdpHandler.this.flen];
/* 1482 */               System.arraycopy(GriffonUdpHandler.this.fileContent, 10, decData, 0, GriffonUdpHandler.this.flen);
/* 1483 */               int calcLogCrc32 = CRC32.getCRC32(decData);
/* 1484 */               if (GriffonUdpHandler.this.recvLogCRC32 == calcLogCrc32) {
/* 1485 */                 GriffonDBManager.updateCommandFileData(GriffonUdpHandler.this.sp24DH.getId_Command(), new ByteArrayInputStream(GriffonUdpHandler.this.fileContent));
/* 1486 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonHandlerHelper.getReceiveLogFileName(Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10)) + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1487 */                 endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/*      */               } else {
/*      */                 
/* 1490 */                 GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + GriffonUdpHandler.this.sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("invalid"), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */               } 
/* 1492 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case REQUESTED_CRC32:
/* 1498 */             GriffonUdpHandler.this.tmp4[0] = this.data[0];
/* 1499 */             GriffonUdpHandler.this.tmp4[1] = this.data[1];
/* 1500 */             GriffonUdpHandler.this.tmp4[2] = this.data[2];
/* 1501 */             GriffonUdpHandler.this.tmp4[3] = this.data[3];
/* 1502 */             this.remainingBytes -= 4;
/* 1503 */             crc32Recv = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(GriffonUdpHandler.this.tmp4));
/* 1504 */             if (GriffonUdpHandler.this.syncCFG.getCrc32() != crc32Recv) {
/* 1505 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 8 });
/* 1506 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.REQUESTED_ALL_FILES_CRC32;
/* 1507 */               waitGriffonResponse(); break;
/*      */             } 
/* 1509 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 1510 */             if (GriffonUdpHandler.this.sp24DH != null && GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774) {
/* 1511 */               if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '5') {
/* 1512 */                 GriffonUdpHandler.this.vmList = GriffonDBManager.getVoiceMessageInfoByIdModule(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/* 1513 */                 if (GriffonUdpHandler.this.vmList != null) {
/* 1514 */                   GriffonUdpHandler.this.requiredVMList = GriffonDBManager.getMissingVoiceMessagesInfo(GriffonUdpHandler.this.vmList);
/* 1515 */                   if (GriffonUdpHandler.this.requiredVMList != null && !GriffonUdpHandler.this.requiredVMList.isEmpty()) {
/* 1516 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Zeus_identified_missing_or_added_voice_messages"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1517 */                     requestNextVoiceMessageFromQueue(); break;
/*      */                   } 
/* 1519 */                   closeReadCFGCommand();
/*      */                   break;
/*      */                 } 
/* 1522 */                 closeReadCFGCommand();
/*      */                 break;
/*      */               } 
/* 1525 */               closeReadCFGCommand(); break;
/*      */             } 
/* 1527 */             if (GriffonUdpHandler.this.sp24DH != null && GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773) {
/* 1528 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1529 */               endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1530 */               sendNextCommandFromQueue(); break;
/*      */             } 
/* 1532 */             sendNextCommandFromQueue();
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case REQUESTED_ALL_FILES_CRC32:
/* 1539 */             noOfFiles = this.data[0] & 0xFF;
/* 1540 */             GriffonUdpHandler.this.tmp = new byte[noOfFiles * 5];
/* 1541 */             System.arraycopy(this.data, 1, GriffonUdpHandler.this.tmp, 0, noOfFiles * 5);
/* 1542 */             this.remainingBytes -= 1 + noOfFiles * 5;
/* 1543 */             GriffonUdpHandler.this.deviceCRC32 = GriffonHandlerHelper.buildCRC32FromReceivedBuffer(GriffonUdpHandler.this.productID, GriffonUdpHandler.this.deviceCRC32, GriffonUdpHandler.this.tmp, false, noOfFiles);
/* 1544 */             GriffonUdpHandler.this.fileIDData = GriffonHandlerHelper.prepareFileDataByCRC32Mismatch(GriffonUdpHandler.this.productID, GriffonUdpHandler.this.deviceCRC32, GriffonUdpHandler.this.syncCFG, (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774));
/* 1545 */             if (GriffonUdpHandler.this.fileIDData == null) {
/* 1546 */               if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774) {
/* 1547 */                 if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '5') {
/* 1548 */                   GriffonUdpHandler.this.vmList = GriffonDBManager.getVoiceMessageInfoByIdModule(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/* 1549 */                   if (GriffonUdpHandler.this.vmList != null) {
/* 1550 */                     GriffonUdpHandler.this.requiredVMList = GriffonDBManager.getMissingVoiceMessagesInfo(GriffonUdpHandler.this.vmList);
/* 1551 */                     if (GriffonUdpHandler.this.requiredVMList != null && !GriffonUdpHandler.this.requiredVMList.isEmpty()) {
/* 1552 */                       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Zeus_identified_missing_or_added_voice_messages"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1553 */                       requestNextVoiceMessageFromQueue(); break;
/*      */                     } 
/* 1555 */                     closeReadCFGCommand();
/*      */                     break;
/*      */                   } 
/* 1558 */                   closeReadCFGCommand();
/*      */                   break;
/*      */                 } 
/* 1561 */                 closeReadCFGCommand();
/*      */                 break;
/*      */               } 
/* 1564 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1565 */               endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1566 */               sendNextCommandFromQueue();
/*      */               break;
/*      */             } 
/* 1569 */             sendVMFiles = false;
/* 1570 */             if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773) {
/* 1571 */               for (int i = 1; i < GriffonUdpHandler.this.fileIDData.length; i++) {
/* 1572 */                 if (GriffonUdpHandler.this.fileIDData[i] == 21) {
/* 1573 */                   sendVMFiles = true;
/*      */                   break;
/*      */                 } 
/*      */               } 
/* 1577 */               if (sendVMFiles) {
/* 1578 */                 GriffonUdpHandler.this.requiredVMList = null;
/* 1579 */                 GriffonUdpHandler.this.requiredVMIndex = 0;
/* 1580 */                 GriffonUdpHandler.this.requiredVMList = GriffonHandlerHelper.getMismatchedVoiceMessageList(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.syncCFG);
/* 1581 */                 if (GriffonUdpHandler.this.requiredVMList != null && GriffonUdpHandler.this.requiredVMList.size() > 0) {
/* 1582 */                   sendNextVoiceMessageFromQueue();
/*      */                 } else {
/* 1584 */                   sendVMFiles = false;
/*      */                 } 
/*      */               } 
/*      */             } 
/* 1588 */             if (!sendVMFiles) {
/* 1589 */               byte[] data = new byte[4];
/* 1590 */               GriffonUdpHandler.this.tmp = Functions.get2ByteArrayFromInt(GriffonUdpHandler.this.sp24DH.getCommand_Type());
/* 1591 */               GriffonUdpHandler.this.tmp = Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp);
/* 1592 */               System.arraycopy(GriffonUdpHandler.this.tmp, 0, data, 0, 2);
/* 1593 */               data[2] = 1;
/* 1594 */               data[3] = (byte)Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */               try {
/* 1596 */                 GriffonUdpHandler.this.tmp = prepareCommandPacket(data);
/* 1597 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, GriffonUdpHandler.this.tmp);
/* 1598 */                 GriffonUdpHandler.this.nextState = (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773) ? GriffonEnums.UDPExceutionsStates.FILE_SENDING_INITIATED : GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/* 1599 */                 GriffonUdpHandler.this.sentCommand = GriffonUdpHandler.this.sp24DH.getCommand_Type();
/* 1600 */                 if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774) {
/* 1601 */                   GriffonUdpHandler.this.requestedAllFilesCRC32 = true;
/*      */                 }
/* 1603 */                 waitGriffonResponse();
/* 1604 */               } catch (IOException|InterruptedException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|SQLException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1605 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1606 */                 ex.printStackTrace();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case FIRMWARE_CRC_RESPONSE:
/* 1614 */             this.remainingBytes--;
/* 1615 */             if (this.data[0] == 21) {
/* 1616 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("CRC_Not_Matched_for_Firmware_file_(response") + GriffonUdpHandler.this.tmp[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/* 1617 */               sendNextCommandFromQueue(); break;
/* 1618 */             }  if (this.data[0] == 6) {
/* 1619 */               sendFile2Module(true);
/* 1620 */               GriffonUdpHandler.this.blockIndex++;
/* 1621 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.FILE_SENDING;
/* 1622 */               waitGriffonResponse();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case LOGOUT_SENT:
/* 1628 */             this.remainingBytes--;
/* 1629 */             if (this.data != null && this.data[0] == 6) {
/* 1630 */               this.idleTimeout = 0L;
/*      */             }
/* 1632 */             GriffonUdpHandler.this.dispose();
/*      */             break;
/*      */         } 
/*      */ 
/*      */ 
/*      */       
/* 1638 */       } catch (Exception ex) {
/* 1639 */         ex.printStackTrace();
/* 1640 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_on_the_UDP_Server_task"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, ex);
/* 1641 */         this.remainingBytes = 0;
/*      */ 
/*      */         
/* 1644 */         if (this.newModuleCFG != null) {
/*      */           try {
/* 1646 */             this.newModuleCFG.setCrc32(-1);
/* 1647 */             GriffonDBManager.updateGriffonModuleCfg(this.newModuleCFG);
/* 1648 */             GriffonUdpHandler.this.lastRecvCfgFailed = true;
/* 1649 */           } catch (Exception ex1) {
/* 1650 */             ex1.printStackTrace();
/*      */           } 
/*      */         }
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     public void closeReadCFGCommand() throws Exception {
/* 1658 */       GriffonHandlerHelper.finalizeReceiveCFGFileCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.syncCFG, GriffonUdpHandler.this.vmList);
/* 1659 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1660 */       endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 1661 */       sendNextCommandFromQueue();
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     public void reinitialize() {
/* 1667 */       GriffonUdpHandler.this.cmdsList = null;
/* 1668 */       GriffonUdpHandler.this.cmdIndex = 0;
/* 1669 */       GriffonUdpHandler.this.vmList = null;
/* 1670 */       GriffonUdpHandler.this.requiredVMList = null;
/* 1671 */       GriffonUdpHandler.this.requiredVMIndex = 0;
/* 1672 */       GriffonUdpHandler.this.currentVM = null;
/* 1673 */       if (GriffonUdpHandler.this.raf != null) {
/*      */         try {
/* 1675 */           GriffonUdpHandler.this.raf.close();
/* 1676 */         } catch (IOException iOException) {}
/*      */       }
/*      */       
/* 1679 */       GriffonUdpHandler.this.currentVMPosition = 0L;
/* 1680 */       GriffonUdpHandler.this.raf = null;
/*      */       
/* 1682 */       GriffonUdpHandler.this.requestedAllFilesCRC32 = false;
/* 1683 */       GriffonUdpHandler.this.commandModeActivated = false;
/* 1684 */       GriffonUdpHandler.this.sentCommand = 0;
/* 1685 */       GriffonUdpHandler.this.blockIndex = 0;
/* 1686 */       GriffonUdpHandler.this.blockLength = 0;
/* 1687 */       GriffonUdpHandler.this.retry = 0;
/* 1688 */       GriffonUdpHandler.this.flen = 0;
/* 1689 */       GriffonUdpHandler.this.rcvBlockIndex = 0;
/* 1690 */       GriffonUdpHandler.this.expBlockIndex = 0;
/* 1691 */       GriffonUdpHandler.this.recvCfgCRC32 = 0;
/* 1692 */       GriffonUdpHandler.this.fileContentIndex = 0;
/* 1693 */       GriffonUdpHandler.this.block = null;
/* 1694 */       GriffonUdpHandler.this.fileContent = null;
/* 1695 */       GriffonUdpHandler.this.filePath = null;
/* 1696 */       GriffonUdpHandler.this.fc = null;
/* 1697 */       GriffonUdpHandler.this.blockBuf = null;
/* 1698 */       GriffonUdpHandler.this.sp24DH = null;
/* 1699 */       if (GriffonUdpHandler.this.file != null && GriffonUdpHandler.this.file.exists()) {
/* 1700 */         GriffonUdpHandler.this.file.delete();
/*      */       }
/* 1702 */       GriffonUdpHandler.this.fileSendingFlag = false;
/* 1703 */       GriffonUdpHandler.this.file = null;
/* 1704 */       GriffonUdpHandler.this.nextUpdateFieldLastCommunication = 0L;
/* 1705 */       GriffonUdpHandler.this.packetSentTime = 0L;
/* 1706 */       GriffonUdpHandler.this.waitingForResponse = false;
/* 1707 */       GriffonUdpHandler.this.newCMDCheck = 0;
/* 1708 */       GriffonUdpHandler.this.m2sPacketReceived = 0;
/* 1709 */       GriffonUdpHandler.this.crc32MatchedNUpdateStatusNotSent = false;
/* 1710 */       GriffonUdpHandler.this.fileSync_80_Sent = false;
/* 1711 */       GriffonUdpHandler.this.fileIDsSent = false;
/* 1712 */       GriffonUdpHandler.this.fileIDData = null;
/* 1713 */       GriffonUdpHandler.this.disableDigitalPGMBuffer = false;
/* 1714 */       GriffonUdpHandler.this.disableDigitalPGMBuffer = false;
/* 1715 */       GriffonUdpHandler.this.recordedLookupRequest = false;
/* 1716 */       GriffonUdpHandler.this.ebFWRequested = false;
/* 1717 */       GriffonUdpHandler.this.recordedLookupRequestSent = false;
/* 1718 */       GriffonUdpHandler.this.recordedAudioFilesCount = 0;
/* 1719 */       GriffonUdpHandler.this.runtimeCommandsPending = false;
/* 1720 */       GriffonUdpHandler.this.last_80_sent_time = 0L;
/* 1721 */       GriffonUdpHandler.this.firstPacketWithOutCfgCRC32 = false;
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
/*      */     private boolean processM2SPacket(boolean flag) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
/* 1740 */       GriffonUdpHandler.this.responseTimeout = 120000;
/* 1741 */       byte[] prod = new byte[2];
/* 1742 */       prod[0] = this.data[1];
/* 1743 */       prod[1] = this.data[0];
/*      */       
/* 1745 */       if (this.data[0] == 6) {
/* 1746 */         this.remainingBytes--;
/* 1747 */         return false;
/*      */       } 
/* 1749 */       if (prod[0] == 43 && prod[1] == 43) {
/* 1750 */         GriffonUdpHandler.this.dispose();
/* 1751 */         this.remainingBytes -= 3;
/* 1752 */         return false;
/*      */       } 
/*      */       
/* 1755 */       String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/* 1756 */       GriffonUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1757 */       prodBin = prodBin.substring(2);
/* 1758 */       prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/* 1759 */       GriffonUdpHandler.this.productID = Short.parseShort(prodBin, 2);
/* 1760 */       if (GriffonUdpHandler.this.productID != Util.EnumProductIDs.GRIFFON_V1.getProductId() && GriffonUdpHandler.this.productID != Util.EnumProductIDs.GRIFFON_V2.getProductId() && prod[0] != 43 && prod[1] != 43) {
/* 1761 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.sn + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_UDP"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1762 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 1763 */         this.remainingBytes = 0;
/* 1764 */         return false;
/*      */       } 
/*      */       
/* 1767 */       if (GriffonUdpHandler.this.encType != 1 && GriffonUdpHandler.this.encType != 2) {
/* 1768 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Module_Connected_with_Invalid_Encryption"), Enums.EnumMessagePriority.AVERAGE, GriffonUdpHandler.this.remoteIP, null);
/* 1769 */         this.remainingBytes = 0;
/* 1770 */         return false;
/*      */       } 
/*      */       
/* 1773 */       if (this.data.length < 4) {
/* 1774 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Invalid_M2S_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1775 */         this.remainingBytes = 0;
/* 1776 */         return false;
/*      */       } 
/*      */       
/* 1779 */       byte[] len = new byte[2];
/* 1780 */       len[0] = this.data[3];
/* 1781 */       len[1] = this.data[2];
/* 1782 */       int msgLen = Functions.getIntFrom2ByteArray(len);
/* 1783 */       this.remainingBytes -= 4;
/*      */ 
/*      */       
/* 1786 */       if (this.data.length - 2 < msgLen) {
/* 1787 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Invalid_M2S_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1788 */         this.remainingBytes = 0;
/* 1789 */         return false;
/*      */       } 
/* 1791 */       int crcCalc = CRC16.calculate(this.data, 0, msgLen + 2, 65535);
/* 1792 */       len[1] = this.data[msgLen + 2];
/* 1793 */       len[0] = this.data[msgLen + 2 + 1];
/* 1794 */       this.remainingBytes -= msgLen + 2;
/* 1795 */       int crcRecv = Functions.getIntFrom2ByteArray(len);
/* 1796 */       if (crcCalc == crcRecv) {
/* 1797 */         byte[] encData = new byte[msgLen - 2];
/* 1798 */         System.arraycopy(this.data, 4, encData, 0, msgLen - 2);
/* 1799 */         byte[] decData = new byte[msgLen - 2];
/* 1800 */         byte[] decBlock = null;
/* 1801 */         if (encData.length >= 17 && encData.length % 16 == 0) {
/* 1802 */           for (int i = 0; i < encData.length; ) {
/* 1803 */             byte[] block = new byte[16];
/* 1804 */             System.arraycopy(encData, i, block, 0, 16);
/* 1805 */             if (GriffonUdpHandler.this.encType == 1) {
/* 1806 */               decBlock = Rijndael.decryptBytes(block, Rijndael.dataKeyBytes, false);
/* 1807 */             } else if (GriffonUdpHandler.this.encType == 2) {
/* 1808 */               decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */             } 
/* 1810 */             System.arraycopy(decBlock, 0, decData, i, 16);
/* 1811 */             i += 16;
/*      */           } 
/*      */         }
/*      */         
/* 1815 */         if (!parseM2SPacket(decData)) {
/* 1816 */           return false;
/*      */         }
/*      */         
/* 1819 */         if (GriffonUdpHandler.this.newCMDCheck >= 3 && !flag) {
/* 1820 */           flag = true;
/*      */         }
/*      */         
/* 1823 */         if (GriffonUdpHandler.this.commandModeActivated && 
/* 1824 */           ++GriffonUdpHandler.this.m2sPacketReceived > 1) {
/* 1825 */           GriffonUdpHandler.this.commandModeActivated = false;
/* 1826 */           GriffonUdpHandler.this.m2sPacketReceived = 0;
/*      */         } 
/*      */         
/* 1829 */         if (TblGriffonActiveConnections.getInstance().containsKey(GriffonUdpHandler.this.sn) && flag && 
/* 1830 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).newCommand && !GriffonUdpHandler.this.commandModeActivated) {
/*      */           
/* 1832 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/* 1833 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/*      */           try {
/* 1835 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, newCmd);
/* 1836 */             GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/* 1837 */             GriffonUdpHandler.this.commandModeActivated = true;
/* 1838 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1839 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/* 1840 */             waitGriffonResponse();
/* 1841 */             GriffonUdpHandler.this.newCMDCheck = 0;
/* 1842 */           } catch (IOException|InterruptedException|SQLException ex) {
/* 1843 */             return false;
/*      */           } 
/*      */         } 
/*      */       } else {
/*      */         
/* 1848 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1849 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/*      */       } 
/* 1851 */       return true;
/*      */     }
/*      */     
/*      */     private boolean processCommandPacket() {
/*      */       try {
/* 1856 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1857 */         GriffonUdpHandler.this.cmdsList = GriffonDBManager.executeSP_024(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/* 1858 */         GriffonUdpHandler.this.cmdIndex = 0;
/* 1859 */         if (GriffonUdpHandler.this.cmdsList.size() > 0) {
/* 1860 */           GriffonUdpHandler.this.sp24DH = GriffonUdpHandler.this.cmdsList.get(GriffonUdpHandler.this.cmdIndex);
/* 1861 */           return sendCommandPacket();
/*      */         } 
/* 1863 */       } catch (Exception ex) {
/* 1864 */         Logger.getLogger(GriffonUdpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/* 1866 */       return false; } private boolean sendCommandPacket() throws SQLException, InterruptedException, Exception { byte[] tmp, ascii; String[] cData, zData; int temp, idx; String[] date, dData;
/*      */       int i;
/*      */       String[] hData;
/*      */       byte b;
/* 1870 */       if (GriffonDBManager.isCommandCancelled(GriffonUdpHandler.this.sp24DH.getId_Command())) {
/* 1871 */         return false;
/*      */       }
/* 1873 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Sending_command") + GriffonUdpHandler.this.sp24DH.getCommand_Type() + ":" + GriffonUdpHandler.this.sp24DH.getCommandData(), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 1874 */       GriffonDBManager.updateCommandStatus(GriffonUdpHandler.this.sp24DH.getId_Command(), 1);
/*      */       
/* 1876 */       if ((GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774 && GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') || (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774 && GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '5') || (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773 && GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1')) {
/* 1877 */         GriffonUdpHandler.this.syncCFG = (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774) ? GriffonDBManager.readGriffonModuleCfg(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule) : GriffonHandlerHelper.getModuleCFGFromUploadedFile(GriffonUdpHandler.this.productID, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.sp24DH.getCommandFileData());
/* 1878 */         byte[] arrayOfByte1 = new byte[3];
/* 1879 */         byte[] arrayOfByte2 = Functions.get2ByteArrayFromInt(32799);
/* 1880 */         arrayOfByte2 = Functions.swapLSB2MSB(arrayOfByte2);
/* 1881 */         System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 0, 2);
/* 1882 */         arrayOfByte1[2] = 0;
/*      */         try {
/* 1884 */           arrayOfByte2 = prepareCommandPacket(arrayOfByte1);
/* 1885 */           UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, arrayOfByte2);
/* 1886 */           GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.REQUESTED_CRC32;
/* 1887 */           return true;
/* 1888 */         } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1889 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 1890 */           ex.printStackTrace();
/* 1891 */           return false;
/*      */         } 
/*      */       } 
/*      */       
/* 1895 */       byte[] data = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 1901 */       switch (GriffonUdpHandler.this.sp24DH.getCommand_Type()) {
/*      */         case 32769:
/* 1903 */           data = new byte[3];
/* 1904 */           tmp = Functions.get2ByteArrayFromInt(32769);
/* 1905 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1906 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1907 */           data[2] = 0;
/*      */           break;
/*      */         case 32774:
/* 1910 */           data = new byte[4];
/* 1911 */           tmp = Functions.get2ByteArrayFromInt(32774);
/* 1912 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1913 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1914 */           data[2] = 1;
/* 1915 */           data[3] = (byte)Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */           break;
/*      */         case 32773:
/* 1918 */           data = new byte[4];
/* 1919 */           tmp = Functions.get2ByteArrayFromInt(32773);
/* 1920 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1921 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1922 */           data[2] = 1;
/* 1923 */           data[3] = (byte)Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */           break;
/*      */         case 32770:
/* 1926 */           tmp = Functions.get2ByteArrayFromInt(32770);
/* 1927 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1928 */           ascii = Functions.getASCII4mString(GriffonUdpHandler.this.sp24DH.getCommandData());
/*      */           
/* 1930 */           data = new byte[ascii.length + 3];
/* 1931 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1932 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/* 1933 */           System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */           break;
/*      */         case 32772:
/* 1936 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 1937 */           temp = Integer.parseInt(cData[0]);
/* 1938 */           if (temp == 1 || temp == 4) {
/* 1939 */             data = new byte[4];
/* 1940 */             data[2] = 1;
/* 1941 */             data[3] = (byte)temp;
/* 1942 */           } else if (temp == 3) {
/* 1943 */             data = new byte[5];
/* 1944 */             data[2] = 2;
/* 1945 */             data[3] = (byte)temp;
/* 1946 */             data[4] = (byte)Integer.parseInt(cData[1]);
/* 1947 */           } else if (temp == 2) {
/* 1948 */             data = new byte[6];
/* 1949 */             data[2] = 3;
/* 1950 */             data[3] = (byte)temp;
/* 1951 */             data[4] = (byte)Integer.parseInt(cData[1]);
/* 1952 */             data[5] = (byte)Integer.parseInt(cData[2]);
/*      */           } 
/* 1954 */           tmp = Functions.get2ByteArrayFromInt(32772);
/* 1955 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1956 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32771:
/* 1959 */           tmp = Functions.get2ByteArrayFromInt(32771);
/* 1960 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1961 */           ascii = Functions.getASCII4mString(GriffonUdpHandler.this.sp24DH.getCommandData().substring(2));
/* 1962 */           data = new byte[ascii.length + 4];
/* 1963 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1964 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length + 2), 16).byteValue();
/* 1965 */           data[3] = (byte)((GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') ? 1 : 2);
/* 1966 */           System.arraycopy(ascii, 0, data, 4, ascii.length);
/*      */           break;
/*      */         case 32775:
/* 1969 */           data = new byte[3];
/* 1970 */           tmp = Functions.get2ByteArrayFromInt(32775);
/* 1971 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1972 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1973 */           data[2] = 0;
/*      */           break;
/*      */         case 32777:
/* 1976 */           tmp = Functions.get2ByteArrayFromInt(32777);
/* 1977 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1978 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 1979 */           if (cData.length == 1) {
/* 1980 */             if (cData[0].equals("1")) {
/* 1981 */               data = new byte[4];
/* 1982 */               System.arraycopy(tmp, 0, data, 0, 2);
/* 1983 */               data[2] = 1;
/* 1984 */               data[3] = 1; break;
/* 1985 */             }  if (cData[0].equals("2")) {
/* 1986 */               data = new byte[11];
/* 1987 */               System.arraycopy(tmp, 0, data, 0, 2);
/* 1988 */               String[] zones = TimeZone.getAvailableIDs(GriffonUdpHandler.this.timezone * 60 * 1000);
/* 1989 */               this.df.setTimeZone(TimeZone.getTimeZone((String)Defines.TIMEZONE_NAMES.get(Integer.valueOf(GriffonUdpHandler.this.timezone))));
/* 1990 */               String ddd = this.df.format(new Date());
/* 1991 */               data[2] = 8;
/* 1992 */               data[3] = 2;
/* 1993 */               data[4] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(0, 2))), 16).byteValue();
/* 1994 */               data[5] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(3, 5))), 16).byteValue();
/* 1995 */               tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(ddd.substring(6, 10)));
/* 1996 */               data[6] = tmp[1];
/* 1997 */               data[7] = tmp[0];
/* 1998 */               data[8] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(11, 13))), 16).byteValue();
/* 1999 */               data[9] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(14, 16))), 16).byteValue();
/* 2000 */               data[10] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(17))), 16).byteValue();
/*      */             }  break;
/*      */           } 
/* 2003 */           data = new byte[11];
/* 2004 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           
/* 2006 */           date = cData[1].split(" ");
/* 2007 */           dData = date[0].split("-");
/* 2008 */           hData = date[1].split(":");
/* 2009 */           data[2] = 8;
/* 2010 */           data[3] = 3;
/* 2011 */           data[4] = Byte.valueOf(dData[2]).byteValue();
/* 2012 */           data[5] = Byte.valueOf(dData[1]).byteValue();
/* 2013 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(dData[0]));
/* 2014 */           data[6] = tmp[1];
/* 2015 */           data[7] = tmp[0];
/* 2016 */           data[8] = Byte.valueOf(hData[0]).byteValue();
/* 2017 */           data[9] = Byte.valueOf(hData[1]).byteValue();
/* 2018 */           data[10] = Byte.valueOf(hData[2]).byteValue();
/*      */           break;
/*      */         
/*      */         case 32778:
/* 2022 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 2023 */           zData = cData[2].split(",");
/* 2024 */           data = new byte[8 + zData.length];
/* 2025 */           data[2] = (byte)(5 + zData.length);
/* 2026 */           if (cData.length == 4) {
/* 2027 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[3]));
/* 2028 */             data[3] = tmp[1];
/* 2029 */             data[4] = tmp[0];
/*      */           } else {
/* 2031 */             data[3] = 0;
/* 2032 */             data[4] = 0;
/*      */           } 
/* 2034 */           data[5] = Byte.parseByte(cData[0]);
/* 2035 */           data[6] = Byte.parseByte(cData[1]);
/* 2036 */           data[7] = (byte)zData.length;
/* 2037 */           idx = 8;
/* 2038 */           for (date = zData, i = date.length, b = 0; b < i; ) { String s = date[b];
/* 2039 */             data[idx++] = (byte)Integer.parseInt(s); b++; }
/*      */           
/* 2041 */           tmp = Functions.get2ByteArrayFromInt(32778);
/* 2042 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2043 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32779:
/* 2046 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 2047 */           data = new byte[8];
/* 2048 */           data[2] = 5;
/* 2049 */           if (cData.length == 5) {
/* 2050 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[4]));
/* 2051 */             data[3] = tmp[1];
/* 2052 */             data[4] = tmp[0];
/*      */           } else {
/* 2054 */             data[3] = 0;
/* 2055 */             data[4] = 0;
/*      */           } 
/* 2057 */           data[5] = (byte)Integer.parseInt(cData[0]);
/* 2058 */           data[6] = Byte.parseByte(cData[2]);
/* 2059 */           data[7] = (byte)Integer.parseInt(cData[3]);
/* 2060 */           tmp = Functions.get2ByteArrayFromInt(32779);
/* 2061 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2062 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32780:
/* 2065 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 2066 */           data = new byte[8];
/* 2067 */           data[2] = 5;
/* 2068 */           if (cData.length == 3) {
/* 2069 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[2]));
/* 2070 */             data[3] = tmp[1];
/* 2071 */             data[4] = tmp[0];
/*      */           } else {
/* 2073 */             data[3] = 0;
/* 2074 */             data[4] = 0;
/*      */           } 
/* 2076 */           tmp = Functions.get2ByteArrayFromInt(Functions.getIntegerFromSelectedParitions(cData[0]));
/* 2077 */           data[5] = tmp[1];
/* 2078 */           data[6] = tmp[0];
/* 2079 */           data[7] = Byte.parseByte(cData[1]);
/* 2080 */           tmp = Functions.get2ByteArrayFromInt(32780);
/* 2081 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2082 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32781:
/* 2085 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 2086 */           data = new byte[4];
/* 2087 */           temp = Byte.parseByte(cData[0]);
/* 2088 */           data[2] = (byte)((temp == 1) ? 1 : 0);
/* 2089 */           data[3] = (temp == 1) ? Byte.parseByte(cData[1]) : 0;
/*      */           
/* 2091 */           tmp = Functions.get2ByteArrayFromInt(32781);
/* 2092 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2093 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32782:
/* 2096 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 2097 */           data = new byte[4];
/* 2098 */           data[2] = 1;
/* 2099 */           data[3] = (byte)Integer.parseInt(cData[0]);
/*      */           
/* 2101 */           tmp = Functions.get2ByteArrayFromInt(32782);
/* 2102 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2103 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32783:
/* 2106 */           cData = GriffonUdpHandler.this.sp24DH.getCommandData().split(";");
/* 2107 */           data = new byte[5];
/* 2108 */           data[2] = 2;
/* 2109 */           data[3] = (byte)Integer.parseInt(cData[0]);
/* 2110 */           data[4] = (byte)Integer.parseInt(cData[1]);
/* 2111 */           tmp = Functions.get2ByteArrayFromInt(32783);
/* 2112 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2113 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         case 32784:
/* 2116 */           data = new byte[3];
/* 2117 */           tmp = Functions.get2ByteArrayFromInt(32784);
/* 2118 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2119 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 2120 */           data[2] = 0;
/*      */           break;
/*      */         case 32785:
/* 2123 */           data = new byte[3];
/* 2124 */           tmp = Functions.get2ByteArrayFromInt(32785);
/* 2125 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2126 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 2127 */           data[2] = 0;
/*      */           break;
/*      */         case 32799:
/* 2130 */           data = new byte[3];
/* 2131 */           tmp = Functions.get2ByteArrayFromInt(32799);
/* 2132 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2133 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 2134 */           data[2] = 0;
/*      */           break;
/*      */         case 32787:
/* 2137 */           data = new byte[3];
/* 2138 */           tmp = Functions.get2ByteArrayFromInt(32787);
/* 2139 */           tmp = Functions.swapLSB2MSB(tmp);
/* 2140 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 2141 */           data[2] = 0;
/*      */           break;
/*      */       } 
/*      */       
/*      */       try {
/* 2146 */         tmp = prepareCommandPacket(data);
/* 2147 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, tmp);
/* 2148 */         if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773) {
/* 2149 */           GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.FILE_SENDING_INITIATED;
/* 2150 */         } else if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774) {
/* 2151 */           if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '2' || GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '3' || GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '4') {
/* 2152 */             GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EVENT_LOG_FILE_INITIATED;
/* 2153 */           } else if (GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0) == '8') {
/* 2154 */             GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.RECORD_AUDIO_FILE_READ_INITIATED;
/*      */           } 
/*      */         } else {
/* 2157 */           GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.COMMAND_PACKET_REPSONE;
/*      */         } 
/* 2159 */         return true;
/* 2160 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 2161 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 2162 */         ex.printStackTrace();
/* 2163 */         return false;
/*      */       }  }
/*      */ 
/*      */     
/*      */     public byte[] prepareCommandPacket(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/* 2168 */       int plen = data.length;
/*      */       
/* 2170 */       int lpad = plen % 16;
/* 2171 */       if (lpad > 0) {
/* 2172 */         lpad = 16 - lpad;
/*      */       }
/* 2174 */       byte[] packet = new byte[plen + lpad + 4];
/* 2175 */       byte[] toEnc = new byte[plen + lpad];
/* 2176 */       System.arraycopy(data, 0, toEnc, 0, plen);
/* 2177 */       if (lpad > 0) {
/* 2178 */         for (int j = plen; j < plen + lpad; j++) {
/* 2179 */           toEnc[j] = 0;
/*      */         }
/*      */       }
/*      */ 
/*      */       
/* 2184 */       for (int i = 0; i < toEnc.length; i += 16) {
/* 2185 */         byte[] block = new byte[16];
/* 2186 */         System.arraycopy(toEnc, i, block, 0, 16);
/* 2187 */         byte[] decBlock = null;
/* 2188 */         decBlock = Rijndael.encryptBytes(block, Rijndael.aes_256, false);
/* 2189 */         System.arraycopy(decBlock, 0, toEnc, i, 16);
/*      */       } 
/*      */       
/* 2192 */       byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(plen + lpad + 2));
/* 2193 */       System.arraycopy(tmp, 0, packet, 0, 2);
/* 2194 */       System.arraycopy(toEnc, 0, packet, 2, plen + lpad);
/*      */       
/* 2196 */       int crcCalc = CRC16.calculate(packet, 0, plen + lpad + 2, 65535);
/* 2197 */       tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 2198 */       System.arraycopy(tmp, 0, packet, plen + lpad + 2, 2);
/* 2199 */       return packet;
/*      */     }
/*      */     
/*      */     private boolean parseM2SPacket(byte[] decData) {
/*      */       try {
/* 2204 */         TblGriffonActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/* 2205 */         this.module = new GriffonModule();
/* 2206 */         this.module.setDefaults();
/* 2207 */         this.module.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0));
/* 2208 */         this.module.setM2sData(decData);
/* 2209 */         this.module.setLastNWProtocol(Enums.EnumNWProtocol.UDP.name());
/* 2210 */         boolean appDataReceived = false;
/* 2211 */         boolean pendingAlive = false;
/* 2212 */         List<Event> eList = null;
/* 2213 */         List<ExpansionModule> emList = null;
/* 2214 */         List<Partition> pList = null;
/* 2215 */         List<Zone> zList = null;
/* 2216 */         List<PGM> pgmList = null;
/* 2217 */         List<Access> acList = null;
/* 2218 */         GriffonUdpHandler.this.digitalPGMBufferReceived = false;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 2225 */         int index = 0;
/* 2226 */         byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 2236 */         String partition = null;
/* 2237 */         String zoneCode = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 2243 */         int eType = 0;
/*      */         
/* 2245 */         byte[] tmp2 = new byte[2];
/* 2246 */         byte[] tmp4 = new byte[4];
/*      */ 
/*      */         
/* 2249 */         int sysVal2 = 0;
/*      */ 
/*      */ 
/*      */         
/* 2253 */         while (index < decData.length && 
/* 2254 */           index + 2 <= decData.length) {
/*      */           Event event; ExpansionModule eModule; Access access; byte[] fcon; long dValue; String account; long timestamp; int eventIndex, evntQulifier; String rptCode; int readerType, access_iface, emIndex, zoneIndex, zoneStatus, tmp, idx, sysVal1, sysVal3; StringBuilder sb; int partitionIndex; short partitionStatus; int source, pno, eventType, i; byte[] moduleSn; int j; byte[] simCardOperator; int k;
/*      */           short numRepPgms, numPgm;
/* 2257 */           System.arraycopy(decData, index, fid, 0, 2);
/* 2258 */           index += 2;
/* 2259 */           fid = Functions.swapLSB2MSB(fid);
/* 2260 */           int fidVal = Functions.getIntFrom2ByteArray(fid);
/* 2261 */           if (fidVal <= 0) {
/*      */             break;
/*      */           }
/* 2264 */           short flen = (short)Functions.getIntFromHexByte(decData[index]);
/*      */           try {
/* 2266 */             fcon = new byte[flen];
/* 2267 */             System.arraycopy(decData, ++index, fcon, 0, flen);
/* 2268 */             index += flen;
/* 2269 */           } catch (ArrayIndexOutOfBoundsException ace) {
/* 2270 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "M2S Packet received with wrong length information ", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 2271 */             StringBuilder stringBuilder = new StringBuilder();
/* 2272 */             for (byte bb : decData) {
/* 2273 */               stringBuilder.append(bb).append(" ");
/*      */             }
/* 2275 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Packet: " + stringBuilder.toString(), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 2276 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 2277 */             return true;
/*      */           } 
/*      */           
/* 2280 */           switch (fidVal) {
/*      */             case 1:
/* 2282 */               idx = 0;
/* 2283 */               if (eList == null) {
/* 2284 */                 eList = new ArrayList<>();
/*      */               }
/* 2286 */               if (zList == null) {
/* 2287 */                 zList = new ArrayList<>();
/*      */               }
/* 2289 */               tmp2[1] = fcon[idx++];
/* 2290 */               tmp2[0] = fcon[idx++];
/* 2291 */               eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2292 */               tmp2[0] = fcon[idx++];
/* 2293 */               tmp2[1] = fcon[idx++];
/* 2294 */               account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2295 */               tmp4[0] = fcon[idx++];
/* 2296 */               tmp4[1] = fcon[idx++];
/* 2297 */               tmp4[2] = fcon[idx++];
/* 2298 */               tmp4[3] = fcon[idx++];
/* 2299 */               timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2300 */               evntQulifier = fcon[idx++] & 0xFF;
/* 2301 */               tmp2[0] = fcon[idx++];
/* 2302 */               tmp2[1] = fcon[idx++];
/* 2303 */               rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2304 */               zoneStatus = fcon[idx++] & 0xFF;
/* 2305 */               switch (zoneStatus) {
/*      */                 case 4:
/*      */                 case 6:
/* 2308 */                   appDataReceived = true;
/*      */                   break;
/*      */               } 
/* 2311 */               zoneIndex = fcon[idx++] & 0xFF;
/* 2312 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(zoneIndex) }).replace(' ', '0');
/* 2313 */               partitionIndex = fcon[idx++] & 0xFF;
/* 2314 */               partition = String.format("%2s", new Object[] { String.valueOf(partitionIndex) }).replace(' ', '0');
/* 2315 */               if (zoneStatus != 5 && 
/* 2316 */                 zoneStatus == 1 && evntQulifier == 1 && partitionIndex > 0 && partitionIndex <= 16) {
/* 2317 */                 if (pList == null) {
/* 2318 */                   pList = new ArrayList<>();
/*      */                 }
/* 2320 */                 Partition part = new Partition();
/* 2321 */                 part.setPartitionIndex(partitionIndex);
/* 2322 */                 part.setAlarmStatus(20);
/* 2323 */                 part.setOccurred(Functions.getDateFromInt(timestamp));
/* 2324 */                 part.setSource(zoneIndex);
/* 2325 */                 part.setAccount(account);
/* 2326 */                 pList.add(part);
/*      */               } 
/*      */ 
/*      */               
/* 2330 */               Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, GriffonUdpHandler.this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient);
/* 2331 */               event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, 100 + zoneStatus);
/* 2332 */               event.setOccurred(Functions.convert2GMT(event.getOccurred(), GriffonUdpHandler.this.timezone));
/* 2333 */               event.setEventIndex(eventIndex);
/* 2334 */               eList.add(event);
/* 2335 */               if (zoneStatus == 5 || zoneStatus == 12) {
/*      */                 continue;
/*      */               }
/*      */               
/* 2339 */               if (zoneStatus < 7 || zoneStatus > 11) {
/* 2340 */                 zoneStatus = (zoneStatus == 1 && evntQulifier == 3) ? 51 : zoneStatus;
/* 2341 */                 zoneStatus = (zoneStatus == 6 && evntQulifier == 3) ? 52 : zoneStatus;
/* 2342 */                 if ((zoneStatus == 2 || zoneStatus == 3 || zoneStatus == 4) && evntQulifier == 3) {
/*      */                   
/* 2344 */                   if ((fcon[idx++] & 0xFF) == 11) {
/* 2345 */                     Zone zone = new Zone(zoneIndex, 50, evntQulifier);
/* 2346 */                     zone.setOccurred(event.getOccurred());
/* 2347 */                     zList.add(zone);
/*      */                   }  continue;
/*      */                 } 
/* 2350 */                 if ((fcon[idx++] & 0xFF) != 417) {
/* 2351 */                   Zone zone = new Zone(zoneIndex, zoneStatus, evntQulifier);
/* 2352 */                   zone.setOccurred(event.getOccurred());
/* 2353 */                   zList.add(zone);
/*      */                 } 
/*      */               } 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*      */             case 2:
/* 2361 */               idx = 0;
/* 2362 */               if (eList == null) {
/* 2363 */                 eList = new ArrayList<>();
/*      */               }
/* 2365 */               if (pList == null) {
/* 2366 */                 pList = new ArrayList<>();
/*      */               }
/* 2368 */               tmp2[1] = fcon[idx++];
/* 2369 */               tmp2[0] = fcon[idx++];
/* 2370 */               eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2371 */               tmp2[0] = fcon[idx++];
/* 2372 */               tmp2[1] = fcon[idx++];
/* 2373 */               account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2374 */               tmp4[0] = fcon[idx++];
/* 2375 */               tmp4[1] = fcon[idx++];
/* 2376 */               tmp4[2] = fcon[idx++];
/* 2377 */               tmp4[3] = fcon[idx++];
/* 2378 */               timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2379 */               evntQulifier = fcon[idx++] & 0xFF;
/* 2380 */               tmp2[0] = fcon[idx++];
/* 2381 */               tmp2[1] = fcon[idx++];
/* 2382 */               rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2383 */               partitionStatus = (short)(fcon[idx++] & 0xFF);
/* 2384 */               switch (partitionStatus) {
/*      */                 case 1:
/*      */                 case 2:
/*      */                 case 4:
/*      */                 case 5:
/*      */                 case 6:
/*      */                 case 7:
/*      */                 case 8:
/*      */                 case 9:
/*      */                 case 16:
/*      */                 case 17:
/*      */                 case 18:
/* 2396 */                   appDataReceived = true;
/*      */                   break;
/*      */               } 
/* 2399 */               partition = String.format("%2s", new Object[] { String.valueOf(fcon[idx++] & 0xFF) }).replace(' ', '0');
/* 2400 */               source = fcon[idx++] & 0xFF;
/* 2401 */               tmp2[0] = fcon[idx++];
/* 2402 */               tmp2[1] = fcon[idx++];
/* 2403 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2404 */               Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, GriffonUdpHandler.this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient);
/* 2405 */               event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, 200 + partitionStatus);
/* 2406 */               event.setOccurred(Functions.convert2GMT(event.getOccurred(), GriffonUdpHandler.this.timezone));
/* 2407 */               event.setEventIndex(eventIndex);
/* 2408 */               eList.add(event);
/* 2409 */               pno = Integer.parseInt(partition, 10);
/* 2410 */               if (pno > 0 && (partitionStatus < 10 || partitionStatus > 15)) {
/* 2411 */                 Partition part; if (partitionStatus >= 16 && partitionStatus <= 18) {
/* 2412 */                   part = new Partition();
/* 2413 */                   part.setPartitionIndex(pno);
/* 2414 */                   part.setAlarmStatus(partitionStatus);
/* 2415 */                   part.setUserIndex(zoneCode);
/* 2416 */                   part.setSource(source);
/*      */                 } else {
/* 2418 */                   part = new Partition(pno, (partitionStatus == 2) ? 1 : partitionStatus, zoneCode, source);
/*      */                 } 
/* 2420 */                 part.setOccurred(event.getOccurred());
/* 2421 */                 part.setAccount(account);
/* 2422 */                 pList.add(part);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 3:
/* 2427 */               pendingAlive = true;
/* 2428 */               idx = 0;
/* 2429 */               if (eList == null) {
/* 2430 */                 eList = new ArrayList<>();
/*      */               }
/* 2432 */               tmp2[1] = fcon[idx++];
/* 2433 */               tmp2[0] = fcon[idx++];
/* 2434 */               eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2435 */               tmp2[0] = fcon[idx++];
/* 2436 */               tmp2[1] = fcon[idx++];
/* 2437 */               account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2438 */               tmp4[0] = fcon[idx++];
/* 2439 */               tmp4[1] = fcon[idx++];
/* 2440 */               tmp4[2] = fcon[idx++];
/* 2441 */               tmp4[3] = fcon[idx++];
/* 2442 */               timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2443 */               evntQulifier = fcon[idx++] & 0xFF;
/* 2444 */               tmp2[0] = fcon[idx++];
/* 2445 */               tmp2[1] = fcon[idx++];
/* 2446 */               rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2447 */               sysVal1 = fcon[idx++] & 0xFF;
/* 2448 */               partition = "00";
/* 2449 */               zoneCode = "000";
/* 2450 */               switch (sysVal1) {
/*      */                 case 1:
/* 2452 */                   appDataReceived = true;
/* 2453 */                   zoneCode = "000";
/* 2454 */                   sysVal2 = fcon[idx++] & 0xFF;
/* 2455 */                   switch (sysVal2) {
/*      */                     case 7:
/* 2457 */                       eModule = new ExpansionModule();
/* 2458 */                       if (evntQulifier == 1) {
/* 2459 */                         eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.COMMMUNICATION_TROUBLE.getStatus());
/* 2460 */                         eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/* 2461 */                       } else if (evntQulifier == 3) {
/* 2462 */                         eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.NORMAL_OPERATION.getStatus());
/* 2463 */                         eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*      */                       } 
/* 2465 */                       if (emList == null) {
/* 2466 */                         emList = new ArrayList<>();
/*      */                       }
/* 2468 */                       emList.add(eModule);
/* 2469 */                       zoneCode = String.format("%3s", new Object[] { String.valueOf(fcon[13] & 0xFF) }).replace(' ', '0');
/*      */                       break;
/*      */                     case 8:
/* 2472 */                       eModule = new ExpansionModule();
/* 2473 */                       if (evntQulifier == 1) {
/* 2474 */                         if ((fcon[13] & 0xFF) > 0) {
/* 2475 */                           eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.TAMPERED.getStatus());
/* 2476 */                           eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*      */                         }
/*      */                       
/* 2479 */                       } else if (evntQulifier == 3 && (
/* 2480 */                         fcon[13] & 0xFF) > 0) {
/* 2481 */                         eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.NORMAL_OPERATION.getStatus());
/* 2482 */                         eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*      */                       } 
/*      */ 
/*      */                       
/* 2486 */                       if (emList == null) {
/* 2487 */                         emList = new ArrayList<>();
/*      */                       }
/* 2489 */                       emList.add(eModule);
/* 2490 */                       zoneCode = String.format("%3s", new Object[] { String.valueOf(fcon[13] & 0xFF) }).replace(' ', '0');
/*      */                       break;
/*      */                   } 
/*      */                   break;
/*      */                 case 2:
/*      */                 case 3:
/*      */                 case 4:
/*      */                 case 5:
/* 2498 */                   tmp2[0] = fcon[idx++];
/* 2499 */                   tmp2[1] = fcon[idx++];
/* 2500 */                   zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*      */                   break;
/*      */                 case 6:
/*      */                 case 7:
/* 2504 */                   zoneCode = "000";
/*      */                   break;
/*      */                 case 8:
/*      */                 case 9:
/* 2508 */                   tmp2[0] = fcon[idx++];
/* 2509 */                   tmp2[1] = fcon[idx++];
/* 2510 */                   zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2511 */                   if (Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2)) == 0) {
/* 2512 */                     zoneCode = "000";
/*      */                   }
/*      */                   break;
/*      */                 case 10:
/* 2516 */                   tmp2[0] = fcon[idx++];
/* 2517 */                   tmp2[1] = fcon[idx++];
/* 2518 */                   zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*      */                   break;
/*      */                 case 11:
/*      */                 case 12:
/*      */                 case 13:
/*      */                 case 14:
/*      */                 case 15:
/*      */                 case 16:
/*      */                 case 17:
/*      */                 case 18:
/*      */                 case 19:
/*      */                 case 20:
/*      */                 case 21:
/*      */                 case 22:
/*      */                 case 24:
/*      */                 case 25:
/*      */                 case 26:
/*      */                 case 29:
/* 2536 */                   zoneCode = String.format("%3s", new Object[] { String.valueOf(fcon[13] & 0xFF) }).replace(' ', '0');
/*      */                   break;
/*      */                 case 23:
/* 2539 */                   sysVal2 = fcon[idx++] & 0xFF;
/* 2540 */                   sysVal3 = fcon[idx++] & 0xFF;
/* 2541 */                   if (sysVal2 == 1) {
/* 2542 */                     eType = 350;
/* 2543 */                     zoneCode = String.format("%3s", new Object[] { String.valueOf(sysVal3) }).replace(' ', '0'); break;
/* 2544 */                   }  if (sysVal2 == 2) {
/* 2545 */                     eType = 351;
/* 2546 */                     zoneCode = "000";
/*      */                   } 
/*      */                   break;
/*      */                 case 27:
/* 2550 */                   sysVal2 = fcon[idx++] & 0xFF;
/* 2551 */                   if (sysVal2 == 1) {
/* 2552 */                     eType = 352;
/* 2553 */                   } else if (sysVal2 == 2) {
/* 2554 */                     eType = 353;
/*      */                   } 
/* 2556 */                   zoneCode = "001";
/*      */                   break;
/*      */                 case 28:
/* 2559 */                   sysVal2 = fcon[idx++] & 0xFF;
/* 2560 */                   if (sysVal2 == 1) {
/* 2561 */                     eType = 354;
/* 2562 */                   } else if (sysVal2 == 2) {
/* 2563 */                     eType = 355;
/*      */                   } 
/* 2565 */                   zoneCode = "002";
/*      */                   break;
/*      */               } 
/*      */               
/* 2569 */               Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, GriffonUdpHandler.this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient);
/* 2570 */               event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, (sysVal1 == 23 || sysVal1 == 27 || sysVal1 == 28) ? eType : ((sysVal1 == 1) ? (3000 + sysVal2) : (300 + sysVal1)));
/* 2571 */               event.setOccurred(Functions.convert2GMT(event.getOccurred(), GriffonUdpHandler.this.timezone));
/* 2572 */               event.setEventIndex(eventIndex);
/* 2573 */               eList.add(event);
/*      */ 
/*      */             
/*      */             case 4:
/* 2577 */               idx = 0;
/* 2578 */               if (eList == null) {
/* 2579 */                 eList = new ArrayList<>();
/*      */               }
/* 2581 */               tmp2[1] = fcon[idx++];
/* 2582 */               tmp2[0] = fcon[idx++];
/* 2583 */               eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2584 */               tmp2[0] = fcon[idx++];
/* 2585 */               tmp2[1] = fcon[idx++];
/* 2586 */               account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2587 */               tmp4[0] = fcon[idx++];
/* 2588 */               tmp4[1] = fcon[idx++];
/* 2589 */               tmp4[2] = fcon[idx++];
/* 2590 */               tmp4[3] = fcon[idx++];
/* 2591 */               timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2592 */               evntQulifier = fcon[idx++] & 0xFF;
/* 2593 */               tmp2[0] = fcon[idx++];
/* 2594 */               tmp2[1] = fcon[idx++];
/* 2595 */               rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2596 */               eventType = (short)(fcon[idx++] & 0xFF);
/* 2597 */               switch (eventType) {
/*      */                 case 1:
/*      */                 case 2:
/*      */                 case 3:
/*      */                 case 4:
/*      */                 case 5:
/*      */                 case 6:
/*      */                 case 7:
/* 2605 */                   partition = "00";
/* 2606 */                   zoneCode = "000";
/*      */                   break;
/*      */                 case 8:
/* 2609 */                   tmp = fcon[idx++] & 0xFF;
/* 2610 */                   partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2611 */                   zoneCode = "000";
/*      */                   break;
/*      */                 case 9:
/* 2614 */                   tmp = fcon[idx++] & 0xFF;
/* 2615 */                   partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2616 */                   zoneCode = "000";
/*      */                   break;
/*      */                 case 10:
/*      */                 case 11:
/* 2620 */                   tmp = fcon[idx++] & 0xFF;
/* 2621 */                   partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/* 2622 */                   tmp2[0] = fcon[idx++];
/* 2623 */                   tmp2[1] = fcon[idx++];
/* 2624 */                   zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*      */                   break;
/*      */                 case 12:
/* 2627 */                   if (TblGriffonActiveConnections.getInstance().containsKey(GriffonUdpHandler.this.sn)) {
/* 2628 */                     List<Integer> analogPGMIndex = GriffonDBManager.getAnalogPGMIndexByModuleID(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/* 2629 */                     if (pgmList == null) {
/* 2630 */                       pgmList = new ArrayList<>();
/*      */                     }
/* 2632 */                     PGM pgm = new PGM();
/* 2633 */                     partition = "00";
/* 2634 */                     tmp = fcon[idx++] & 0xFF;
/* 2635 */                     pgm.setPgmIndex(tmp);
/* 2636 */                     tmp = fcon[fcon.length - 1];
/* 2637 */                     if (analogPGMIndex != null && analogPGMIndex.contains(Integer.valueOf(pgm.getPgmIndex()))) {
/* 2638 */                       switch (tmp) {
/*      */                         case 0:
/* 2640 */                           tmp = (evntQulifier == 1) ? 1 : 0;
/*      */                           break;
/*      */                         case 1:
/* 2643 */                           tmp = (evntQulifier == 1) ? 3 : 0;
/*      */                           break;
/*      */                         case 2:
/* 2646 */                           tmp = (evntQulifier == 1) ? 4 : 0;
/*      */                           break;
/*      */                       } 
/*      */                     } else {
/* 2650 */                       switch (tmp) {
/*      */                         case 0:
/* 2652 */                           tmp = (evntQulifier == 1) ? 5 : 0;
/*      */                           break;
/*      */                         case 1:
/* 2655 */                           tmp = (evntQulifier == 1) ? 3 : 1;
/*      */                           break;
/*      */                         case 2:
/* 2658 */                           tmp = (evntQulifier == 1) ? 4 : 2;
/*      */                           break;
/*      */                       } 
/*      */                     } 
/* 2662 */                     pgm.setPgmStatus(tmp);
/* 2663 */                     pgm.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(timestamp), GriffonUdpHandler.this.timezone));
/* 2664 */                     pgmList.add(pgm);
/* 2665 */                     zoneCode = String.format("%3s", new Object[] { String.valueOf(pgm.getPgmIndex()) }).replace(' ', '0');
/*      */                   } 
/* 2667 */                   appDataReceived = true;
/*      */                   break;
/*      */               } 
/* 2670 */               Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, GriffonUdpHandler.this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient);
/* 2671 */               event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, 400 + eventType);
/* 2672 */               event.setOccurred(Functions.convert2GMT(event.getOccurred(), GriffonUdpHandler.this.timezone));
/* 2673 */               event.setEventIndex(eventIndex);
/* 2674 */               eList.add(event);
/*      */ 
/*      */             
/*      */             case 5:
/* 2678 */               idx = 0;
/* 2679 */               if (eList == null) {
/* 2680 */                 eList = new ArrayList<>();
/*      */               }
/* 2682 */               if (acList == null) {
/* 2683 */                 acList = new ArrayList<>();
/*      */               }
/* 2685 */               tmp2[1] = fcon[idx++];
/* 2686 */               tmp2[0] = fcon[idx++];
/* 2687 */               eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2688 */               tmp2[0] = fcon[idx++];
/* 2689 */               tmp2[1] = fcon[idx++];
/* 2690 */               account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/* 2691 */               tmp4[0] = fcon[idx++];
/* 2692 */               tmp4[1] = fcon[idx++];
/* 2693 */               tmp4[2] = fcon[idx++];
/* 2694 */               tmp4[3] = fcon[idx++];
/* 2695 */               timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2696 */               evntQulifier = fcon[idx++] & 0xFF;
/* 2697 */               tmp2[0] = fcon[idx++];
/* 2698 */               tmp2[1] = fcon[idx++];
/* 2699 */               rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2700 */               tmp2[0] = fcon[idx++];
/* 2701 */               emIndex = tmp2[0] & 0xFF;
/* 2702 */               tmp2 = Functions.getHighLowBytes(fcon[idx++] & 0xFF);
/* 2703 */               access_iface = tmp2[0] & 0xFF;
/* 2704 */               readerType = tmp2[1] & 0xFF;
/* 2705 */               tmp2 = Functions.getHighLowBytes(fcon[idx++] & 0xFF);
/* 2706 */               tmp = GriffonEnums.EnumAccessDescription.INVALID.getDescription();
/* 2707 */               if ((tmp2[0] & 0xFF) == 0) {
/* 2708 */                 tmp = GriffonEnums.EnumAccessDescription.INVALID.getDescription();
/* 2709 */               } else if ((tmp2[0] & 0xFF) == 1) {
/* 2710 */                 tmp = GriffonEnums.EnumAccessDescription.ACCESS_GRANTED.getDescription();
/* 2711 */               } else if ((tmp2[0] & 0xFF) == 2) {
/* 2712 */                 tmp = GriffonEnums.EnumAccessDescription.EGRESS_GRANTED.getDescription();
/* 2713 */               } else if ((tmp2[0] & 0xFF) == 3) {
/* 2714 */                 if ((tmp2[1] & 0xFF) == 0) {
/* 2715 */                   tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_INVALID.getDescription();
/* 2716 */                 } else if ((tmp2[1] & 0xFF) == 1) {
/* 2717 */                   tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_ANTI_PASSBACK.getDescription();
/* 2718 */                 } else if ((tmp2[1] & 0xFF) == 2) {
/* 2719 */                   tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_TIME_NOT_MATCHED.getDescription();
/* 2720 */                 } else if ((tmp2[1] & 0xFF) == 3) {
/* 2721 */                   tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_EB_INDEX_NOT_MATCHED.getDescription();
/* 2722 */                 } else if ((tmp2[1] & 0xFF) == 4) {
/* 2723 */                   tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_INTERFACE_TYPE_NOT_MATCHED.getDescription();
/*      */                 } 
/* 2725 */               } else if ((tmp2[0] & 0xFF) == 4) {
/* 2726 */                 if ((tmp2[1] & 0xFF) == 0) {
/* 2727 */                   tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_INVALID.getDescription();
/* 2728 */                 } else if ((tmp2[1] & 0xFF) == 1) {
/* 2729 */                   tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_ANTI_PASSBACK.getDescription();
/* 2730 */                 } else if ((tmp2[1] & 0xFF) == 2) {
/* 2731 */                   tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_TIME_NOT_MATCHED.getDescription();
/* 2732 */                 } else if ((tmp2[1] & 0xFF) == 3) {
/* 2733 */                   tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_EB_INDEX_NOT_MATCHED.getDescription();
/* 2734 */                 } else if ((tmp2[1] & 0xFF) == 4) {
/* 2735 */                   tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_INTERFACE_TYPE_NOT_MATCHED.getDescription();
/*      */                 } 
/*      */               } 
/* 2738 */               tmp2[0] = fcon[idx++];
/* 2739 */               tmp2[1] = fcon[idx++];
/* 2740 */               zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/* 2741 */               partition = "00";
/* 2742 */               Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, GriffonUdpHandler.this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient);
/* 2743 */               event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, tmp);
/* 2744 */               event.setOccurred(Functions.convert2GMT(event.getOccurred(), GriffonUdpHandler.this.timezone));
/* 2745 */               event.setEventIndex(eventIndex);
/* 2746 */               eList.add(event);
/* 2747 */               access = new Access(event.getOccurred(), new Date(), account, rptCode, emIndex, access_iface, readerType, zoneCode, evntQulifier, tmp);
/* 2748 */               acList.add(access);
/*      */ 
/*      */             
/*      */             case 80:
/* 2752 */               idx = 0;
/* 2753 */               sb = new StringBuilder();
/* 2754 */               for (i = 0; i < flen; i++) {
/* 2755 */                 sb.append(String.format(" %x ", new Object[] { Integer.valueOf(fcon[i] & 0xFF) }));
/*      */               } 
/* 2757 */               tmp2[0] = fcon[idx++];
/* 2758 */               tmp2[1] = fcon[idx++];
/* 2759 */               account = Functions.getHexStringFromByteArray(tmp2);
/* 2760 */               account = String.format("%4s", new Object[] { account }).replace(' ', '0');
/* 2761 */               tmp4[0] = fcon[idx++];
/* 2762 */               tmp4[1] = fcon[idx++];
/* 2763 */               tmp4[2] = fcon[idx++];
/* 2764 */               tmp4[3] = fcon[idx++];
/* 2765 */               timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2766 */               tmp2[0] = fcon[idx++];
/* 2767 */               tmp2[1] = fcon[idx++];
/* 2768 */               this.module.setProductId(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2)));
/* 2769 */               moduleSn = new byte[flen - idx];
/* 2770 */               System.arraycopy(fcon, idx, moduleSn, 0, flen - idx);
/* 2771 */               this.module.setSn(Functions.getASCIIFromByteArray(moduleSn));
/* 2772 */               this.module.setClientCode(account);
/* 2773 */               GriffonUdpHandler.this.sn = this.module.getSn();
/* 2774 */               if (GriffonUdpHandler.this.sn.equals("00000000000000000000")) {
/* 2775 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, GriffonUdpHandler.this.sn, null);
/* 2776 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 2777 */                 return false;
/* 2778 */               }  if (GriffonUdpHandler.this.sn.equals("00000000000000000001")) {
/* 2779 */                 String ip = GriffonUdpHandler.this.remoteIP.substring(0, GriffonUdpHandler.this.remoteIP.indexOf(":"));
/* 2780 */                 if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 2781 */                   if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/* 2782 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, GriffonUdpHandler.this.sn, null);
/* 2783 */                     UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 2784 */                     return false;
/*      */                   } 
/* 2786 */                   this.module.setProductId(21);
/*      */                 } else {
/*      */                   
/* 2789 */                   if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/* 2790 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, GriffonUdpHandler.this.sn, null);
/* 2791 */                     UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 2792 */                     return false;
/*      */                   } 
/* 2794 */                   this.module.setProductId(21);
/*      */                 } 
/*      */               } 
/*      */               
/* 2798 */               if (TblGriffonActiveConnections.getInstance().containsKey(GriffonUdpHandler.this.sn)) {
/* 2799 */                 String oldIP = null;
/* 2800 */                 UdpV2Handler oldHandler = null;
/* 2801 */                 synchronized (TblGriffonActiveConnections.getInstance()) {
/* 2802 */                   for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblGriffonActiveUdpConnections.getInstance().entrySet()) {
/* 2803 */                     if (GriffonUdpHandler.this.sn.equals(((UdpV2Handler)handler.getValue()).sn) && ((UdpV2Handler)handler.getValue()).lastCommunicationTime + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout > System.currentTimeMillis() && 
/* 2804 */                       !((String)handler.getKey()).equalsIgnoreCase(GriffonUdpHandler.this.remoteIP)) {
/* 2805 */                       oldIP = handler.getKey();
/* 2806 */                       oldHandler = handler.getValue();
/*      */                     } 
/*      */                   } 
/*      */                 } 
/*      */                 
/* 2811 */                 if (oldIP != null && oldHandler != null) {
/* 2812 */                   TblGriffonActiveUdpConnections.removeConnection(oldIP);
/* 2813 */                   UdpV2Handler cHandler = (UdpV2Handler)TblGriffonActiveUdpConnections.getInstance().get(GriffonUdpHandler.this.remoteIP);
/* 2814 */                   TblGriffonActiveUdpConnections.addConnection(GriffonUdpHandler.this.remoteIP, oldHandler);
/* 2815 */                   this.remainingBytes = this.actualDataSize;
/* 2816 */                   this.remainingBytes = this.actualDataSize;
/* 2817 */                   ((UdpV2Handler)TblGriffonActiveUdpConnections.getInstance().get(GriffonUdpHandler.this.remoteIP)).processM2SPacket(this.data, this.actualDataSize);
/* 2818 */                   oldHandler.updateRemoteIP(GriffonUdpHandler.this.remoteIP, cHandler.getCurrentSocket(), cHandler.getCurrentPacket());
/* 2819 */                   cHandler = null;
/* 2820 */                   return false;
/*      */                 } 
/*      */               } 
/*      */ 
/*      */             
/*      */             case 81:
/* 2826 */               sb = new StringBuilder();
/* 2827 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1]));
/* 2828 */               this.module.setModuleFWVersion(sb.toString());
/*      */ 
/*      */             
/*      */             case 82:
/* 2832 */               tmp2[0] = fcon[0];
/* 2833 */               tmp2[1] = fcon[1];
/* 2834 */               this.module.setModuleHWDtls(Functions.getIntFrom2ByteArray(tmp2));
/*      */ 
/*      */             
/*      */             case 83:
/* 2838 */               pendingAlive = true;
/* 2839 */               this.module.setBatteryStatus(fcon[0] & 0xFF);
/* 2840 */               this.module.setCurrentBatteryVoltage((new Float((fcon[1] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[2] & 0xFF) }))).floatValue());
/* 2841 */               this.module.setBatteryInputCurrent(Float.parseFloat((fcon[3] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[4] & 0xFF) })));
/*      */ 
/*      */             
/*      */             case 84:
/* 2845 */               this.module.setCurrentInterface(fcon[0] & 0xFF);
/* 2846 */               this.lastCommIface = (short)this.module.getCurrentInterface();
/*      */ 
/*      */             
/*      */             case 86:
/* 2850 */               sb = new StringBuilder();
/* 2851 */               for (j = 0; j < flen - 1; j++) {
/* 2852 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[j]));
/*      */               }
/* 2854 */               sb.append(fcon[7] / 10);
/* 2855 */               this.module.setModemIMEI(sb.toString());
/*      */ 
/*      */             
/*      */             case 87:
/* 2859 */               this.module.setModemModel(fcon[0]);
/*      */ 
/*      */             
/*      */             case 88:
/* 2863 */               sb = new StringBuilder();
/* 2864 */               tmp2[0] = fcon[2];
/* 2865 */               tmp2[1] = fcon[3];
/* 2866 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFrom2ByteArray(tmp2));
/* 2867 */               this.module.setModemFWVersion(sb.toString());
/*      */ 
/*      */             
/*      */             case 89:
/* 2871 */               pendingAlive = true;
/* 2872 */               this.module.setGsmSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */             
/*      */             case 90:
/* 2876 */               pendingAlive = true;
/*      */ 
/*      */             
/*      */             case 95:
/* 2880 */               sb = new StringBuilder();
/* 2881 */               for (j = 1; j < flen; j++) {
/* 2882 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[j]));
/*      */               }
/* 2884 */               if (fcon[0] == 1) {
/* 2885 */                 this.module.setSim_iccid_1(sb.toString()); continue;
/* 2886 */               }  if (fcon[0] == 2) {
/* 2887 */                 this.module.setSim_iccid_2(sb.toString());
/*      */               }
/*      */ 
/*      */             
/*      */             case 96:
/* 2892 */               simCardOperator = new byte[flen - 1];
/* 2893 */               System.arraycopy(fcon, 1, simCardOperator, 0, flen - 1);
/* 2894 */               if (fcon[0] == 1) {
/* 2895 */                 this.module.setSim_operator_1(Functions.getASCIIFromByteArray(simCardOperator)); continue;
/* 2896 */               }  if (fcon[0] == 2) {
/* 2897 */                 this.module.setSim_operator_2(Functions.getASCIIFromByteArray(simCardOperator));
/*      */               }
/*      */ 
/*      */             
/*      */             case 94:
/* 2902 */               this.module.setCurrentSIM((short)Functions.getIntFromHexByte(fcon[0]));
/* 2903 */               this.module.setCurrentAPN((short)Functions.getIntFromHexByte(fcon[1]));
/*      */ 
/*      */             
/*      */             case 93:
/* 2907 */               pendingAlive = true;
/* 2908 */               this.module.setGprsDataVolume(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*      */ 
/*      */             
/*      */             case 92:
/* 2912 */               pendingAlive = true;
/* 2913 */               this.module.setOtaStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */             
/*      */             case 97:
/* 2917 */               sb = new StringBuilder();
/* 2918 */               for (k = 1; k < flen - 1; k++) {
/* 2919 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[k]));
/*      */               }
/* 2921 */               sb.append(fcon[8] / 10);
/* 2922 */               if (fcon[0] == 1) {
/* 2923 */                 this.module.setSim_imsi_1(sb.toString()); continue;
/* 2924 */               }  if (fcon[0] == 2) {
/* 2925 */                 this.module.setSim_imsi_2(sb.toString());
/*      */               }
/*      */ 
/*      */             
/*      */             case 91:
/* 2930 */               pendingAlive = true;
/* 2931 */               this.module.setGsmJammerStatus((short)Functions.getIntFromHexByte(fcon[0]));
/* 2932 */               this.module.setGsmJDRStatus((short)Functions.getIntFromHexByte(fcon[1]));
/*      */ 
/*      */             
/*      */             case 98:
/* 2936 */               pendingAlive = true;
/*      */ 
/*      */             
/*      */             case 99:
/* 2940 */               pendingAlive = true;
/* 2941 */               tmp4[0] = fcon[0];
/* 2942 */               tmp4[1] = fcon[1];
/* 2943 */               tmp4[2] = fcon[2];
/* 2944 */               tmp4[3] = fcon[3];
/* 2945 */               dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2946 */               this.module.setLongitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */               
/* 2948 */               tmp4[0] = fcon[4];
/* 2949 */               tmp4[1] = fcon[5];
/* 2950 */               tmp4[2] = fcon[6];
/* 2951 */               tmp4[3] = fcon[7];
/* 2952 */               dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2953 */               this.module.setLattitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */               
/* 2955 */               tmp4[0] = fcon[8];
/* 2956 */               tmp4[1] = fcon[9];
/* 2957 */               tmp4[2] = fcon[10];
/* 2958 */               tmp4[3] = fcon[11];
/* 2959 */               dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2960 */               this.module.setAltitude((float)(dValue / 100.0D));
/*      */ 
/*      */             
/*      */             case 100:
/* 2964 */               pendingAlive = true;
/*      */ 
/*      */             
/*      */             case 101:
/* 2968 */               pendingAlive = true;
/*      */ 
/*      */             
/*      */             case 102:
/* 2972 */               this.module.setWifiModel(fcon[0]);
/*      */ 
/*      */             
/*      */             case 103:
/* 2976 */               sb = new StringBuilder();
/* 2977 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1]));
/* 2978 */               this.module.setWifiFWVersion(sb.toString());
/*      */ 
/*      */             
/*      */             case 104:
/* 2982 */               this.module.setWifiAccessPoint(fcon[0]);
/*      */ 
/*      */             
/*      */             case 105:
/* 2986 */               pendingAlive = true;
/*      */ 
/*      */             
/*      */             case 106:
/* 2990 */               pendingAlive = true;
/* 2991 */               this.module.setWifiSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */             
/*      */             case 107:
/* 2995 */               this.module.setTimeSync(true);
/* 2996 */               tmp2[0] = fcon[2];
/* 2997 */               tmp2[1] = fcon[1];
/* 2998 */               GriffonUdpHandler.this.timezone = Functions.getSignedIntFrom2ByteArray(tmp2);
/* 2999 */               this.module.setTimezone(GriffonUdpHandler.this.timezone);
/*      */ 
/*      */             
/*      */             case 109:
/* 3003 */               this.module.setUpdateStatusReceived(true);
/* 3004 */               this.module = GriffonHandlerHelper.handleDashboardBuffer(fcon, this.module, GriffonUdpHandler.this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*      */ 
/*      */             
/*      */             case 110:
/* 3008 */               if (fcon[0] == 1) {
/* 3009 */                 this.module.setEventUpload(1); continue;
/* 3010 */               }  if (fcon[0] == 2) {
/* 3011 */                 this.module.setLogUpload(2);
/*      */               }
/*      */ 
/*      */             
/*      */             case 108:
/* 3016 */               GriffonUdpHandler.this.deviceCRC32 = null;
/* 3017 */               GriffonUdpHandler.this.deviceCRC32 = GriffonHandlerHelper.buildCRC32FromReceivedBuffer(GriffonUdpHandler.this.productID, GriffonUdpHandler.this.deviceCRC32, fcon, true, 0);
/* 3018 */               GriffonUdpHandler.this.requestedAllFilesCRC32 = true;
/*      */ 
/*      */             
/*      */             case 85:
/* 3022 */               GriffonUdpHandler.this.deviceCRC32 = null;
/* 3023 */               tmp4[0] = fcon[3];
/* 3024 */               tmp4[1] = fcon[2];
/* 3025 */               tmp4[2] = fcon[1];
/* 3026 */               tmp4[3] = fcon[0];
/* 3027 */               this.module.setCrc32(Functions.getIntFrom4ByteArray(tmp4));
/* 3028 */               this.module.setInitialPacket(true);
/* 3029 */               GriffonUdpHandler.this.firstPacketWithOutCfgCRC32 = false;
/* 3030 */               reinitialize();
/*      */ 
/*      */             
/*      */             case 111:
/* 3034 */               if (zList == null) {
/* 3035 */                 zList = new ArrayList<>();
/*      */               }
/* 3037 */               GriffonHandlerHelper.handleZoneStatusBuffer(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, zList, fcon, false);
/* 3038 */               appDataReceived = true;
/*      */ 
/*      */ 
/*      */             
/*      */             case 112:
/* 3043 */               if (pgmList == null) {
/* 3044 */                 pgmList = new ArrayList<>();
/*      */               }
/* 3046 */               tmp = 0;
/* 3047 */               numRepPgms = (short)(fcon[tmp++] & 0xFF);
/* 3048 */               for (numPgm = 0; numPgm < numRepPgms; numPgm = (short)(numPgm + 1)) {
/* 3049 */                 PGM pgm = new PGM();
/* 3050 */                 pgm.setPgmIndex(fcon[tmp++] & 0xFF);
/* 3051 */                 pgm.setPgmType(2);
/* 3052 */                 pgm.setAnalogValue(fcon[tmp++] & 0xFF);
/* 3053 */                 pgm.setPgmStatus(GriffonHandlerHelper.getPGMStatusFromDashBoardBuffer(fcon[tmp++], true));
/* 3054 */                 pgmList.add(pgm);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 113:
/* 3059 */               pendingAlive = true;
/* 3060 */               this.module.setAuxOutputStatus((short)fcon[0]);
/* 3061 */               this.module.setAuxVoltage(Float.parseFloat(fcon[1] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[2]) })));
/* 3062 */               this.module.setAuxCurrent(Float.parseFloat(fcon[3] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[4]) })));
/*      */ 
/*      */             
/*      */             case 114:
/* 3066 */               if (emList == null) {
/* 3067 */                 emList = new ArrayList<>();
/*      */               }
/* 3069 */               GriffonHandlerHelper.handleEBBuffer(emList, fcon);
/*      */ 
/*      */             
/*      */             case 115:
/* 3073 */               pendingAlive = true;
/* 3074 */               this.module.setTemparature((fcon[0] & 0xFF) - 40);
/*      */ 
/*      */             
/*      */             case 118:
/* 3078 */               GriffonUdpHandler.this.digitalPGMBufferReceived = true;
/* 3079 */               appDataReceived = true;
/* 3080 */               if (pgmList == null) {
/* 3081 */                 pgmList = new ArrayList<>();
/*      */               }
/* 3083 */               pgmList.addAll(GriffonHandlerHelper.getPGMStatusFromDigitalBuffer(fcon));
/*      */ 
/*      */             
/*      */             case 120:
/* 3087 */               tmp4[0] = fcon[3];
/* 3088 */               tmp4[1] = fcon[2];
/* 3089 */               tmp4[2] = fcon[1];
/* 3090 */               tmp4[3] = fcon[0];
/* 3091 */               this.module.setRecAudioLookupCRC32(Functions.getIntFrom4ByteArray(tmp4));
/* 3092 */               this.module.setRecAudioLookupCRC32Received(true);
/*      */           } 
/*      */         
/*      */         } 
/* 3096 */         this.module.setAcList(acList);
/* 3097 */         this.module.seteList(eList);
/* 3098 */         this.module.setEmList(emList);
/* 3099 */         this.module.setpList(pList);
/* 3100 */         this.module.setzList(zList);
/* 3101 */         this.module.setPgmList(pgmList);
/*      */         
/* 3103 */         if (!pendingAlive) {
/* 3104 */           this.module.setM2sData(null);
/*      */         }
/* 3106 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.sn + LocaleMessage.getLocaleMessage("]_M2S_packet_received") + Functions.getGriffonIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/* 3107 */         this.module.setAutoRegistration((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/* 3108 */         this.module.setIp(GriffonUdpHandler.this.remoteIP.substring(0, GriffonUdpHandler.this.remoteIP.indexOf(":")));
/*      */         try {
/* 3110 */           TblGriffonActiveConnections.semaphoreAlivePacketsReceived.acquire();
/* 3111 */           this.module = GriffonDBManager.executeSPG_001(this.module);
/*      */         } finally {
/* 3113 */           TblGriffonActiveConnections.semaphoreAlivePacketsReceived.release();
/*      */         } 
/* 3115 */         if (this.module != null) {
/* 3116 */           GriffonUdpHandler.this.lastPacketReceivedTime = System.currentTimeMillis();
/* 3117 */           this.idleTimeout = GriffonUdpHandler.this.lastPacketReceivedTime + (this.module.getComm_Timeout() * 1000);
/* 3118 */           GriffonUdpHandler.this.iTimeout = this.idleTimeout;
/* 3119 */           if (this.module.getAuto_Registration_Executed() == 1) {
/* 3120 */             if (this.module.getRegistered() == 0) {
/* 3121 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + GriffonUdpHandler.this.sn, Enums.EnumMessagePriority.HIGH, null, null);
/* 3122 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { -32 });
/* 3123 */               return false;
/*      */             } 
/* 3125 */           } else if (this.module.getRegistered() == 1) {
/* 3126 */             if (this.module.getEnabled() == 0) {
/* 3127 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + GriffonUdpHandler.this.sn + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 3128 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { -31 });
/* 3129 */               return false;
/*      */             } 
/*      */           } else {
/* 3132 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + GriffonUdpHandler.this.sn + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 3133 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { -32 });
/* 3134 */             return false;
/*      */           } 
/*      */           
/* 3137 */           Functions.generateEventReceptionAlivePacket(2, this.module.getId_Client(), this.module.getId_Module(), this.module.getId_Group(), this.module.getClientCode(), this.module.getE_Alive_Received(), this.module.getF_Alive_Received(), this.module.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.UDP.name(), this.lastCommIface, 611);
/*      */           
/* 3139 */           if (this.module.isInitialPacket()) {
/* 3140 */             if (TblGriffonActiveConnections.getInstance().containsKey(GriffonUdpHandler.this.sn)) {
/* 3141 */               synchronized (TblGriffonActiveUdpConnections.getInstance()) {
/* 3142 */                 for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblGriffonActiveUdpConnections.getInstance().entrySet()) {
/* 3143 */                   if (GriffonUdpHandler.this.sn.equals(((UdpV2Handler)handler.getValue()).sn)) {
/* 3144 */                     ((UdpV2Handler)handler.getValue()).dispose();
/*      */                   }
/*      */                 } 
/*      */               } 
/* 3148 */               TblGriffonActiveConnections.removeConnection(GriffonUdpHandler.this.sn);
/*      */             } 
/* 3150 */             TblGriffonActiveConnections.addConnection(GriffonUdpHandler.this.sn, GriffonUdpHandler.this.myThreadGuid);
/* 3151 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).sn = GriffonUdpHandler.this.sn;
/* 3152 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient = this.module.getId_Client();
/* 3153 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule = this.module.getId_Module();
/* 3154 */             TblGriffonActiveUdpConnections.getInstance().put(GriffonUdpHandler.this.remoteIP, GriffonUdpHandler.this.currInsance);
/* 3155 */           } else if (!TblGriffonActiveConnections.getInstance().containsKey(GriffonUdpHandler.this.sn)) {
/* 3156 */             TblGriffonActiveConnections.addConnection(GriffonUdpHandler.this.sn, GriffonUdpHandler.this.myThreadGuid);
/* 3157 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).sn = GriffonUdpHandler.this.sn;
/* 3158 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idClient = this.module.getId_Client();
/* 3159 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule = this.module.getId_Module();
/* 3160 */             TblGriffonActiveUdpConnections.getInstance().put(GriffonUdpHandler.this.remoteIP, GriffonUdpHandler.this.currInsance);
/*      */           } 
/* 3162 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).clientName = this.module.getName();
/* 3163 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idGroup = this.module.getId_Group();
/* 3164 */           if (this.module.getCommDebug() == 1) {
/* 3165 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationDebug = true;
/*      */           } else {
/* 3167 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationDebug = false;
/*      */           } 
/* 3169 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout = this.module.getComm_Timeout() * 1000;
/*      */           
/* 3171 */           if (this.module.isRecAudioLookupCRC32Received() && !this.module.isRecAudioLookupCRC32NotMacthed() && 
/* 3172 */             !GriffonUdpHandler.this.recordedLookupRequest) {
/* 3173 */             GriffonUdpHandler.this.recordedLookupRequest = true;
/* 3174 */             GriffonUdpHandler.this.recordListLookupRequest2 = true;
/* 3175 */             GriffonUdpHandler.this.recAudioLookupCRC32 = this.module.getRecAudioLookupCRC32();
/*      */           } 
/*      */ 
/*      */           
/* 3179 */           if (this.module.getCommLog() == 1 && this.module.getCommLogEnabledDate() != null) {
/* 3180 */             Calendar sys = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/* 3181 */             if (sys.get(5) - this.module.getCommLogEnabledDate().get(5) > 30) {
/* 3182 */               GriffonDBManager.disableCommunicationLog(this.module.getId_Module());
/* 3183 */               ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).commLog = false;
/* 3184 */               if (GriffonUdpHandler.this.ownLogger != null) {
/* 3185 */                 GriffonUdpHandler.this.ownLogger = null;
/*      */               }
/*      */             } else {
/* 3188 */               ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).commLog = true;
/* 3189 */               if (GriffonUdpHandler.this.ownLogger == null && GriffonUdpHandler.this.sn != null) {
/* 3190 */                 GriffonUdpHandler.this.ownLogger = ZeusServerLogger.getDeviceLogger("Griffon/", GriffonUdpHandler.this.sn);
/*      */               }
/* 3192 */               if (GriffonUdpHandler.this.ownLogger != null) {
/* 3193 */                 Functions.logGriffonIncomingPacket(GriffonUdpHandler.this.ownLogger, decData);
/*      */               }
/*      */             } 
/*      */           } else {
/* 3197 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).commLog = false;
/* 3198 */             if (GriffonUdpHandler.this.ownLogger != null) {
/* 3199 */               GriffonUdpHandler.this.ownLogger = null;
/*      */             }
/*      */           } 
/*      */           
/*      */           try {
/* 3204 */             if ((this.module.isInitialPacket() && !this.module.isCrc32Matched()) || (this.module.getAuto_Registration_Executed() == 1 && GriffonUdpHandler.this.firstPacketWithOutCfgCRC32) || GriffonUdpHandler.this.lastRecvCfgFailed) {
/*      */               
/* 3206 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 8 });
/*      */             }
/* 3208 */             else if (GriffonUdpHandler.this.firstPacketWithOutCfgCRC32) {
/* 3209 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 8 });
/*      */             } else {
/* 3211 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 3212 */               Thread.sleep(100L);
/* 3213 */               if ((this.module.isInitialPacket() && this.module.isCrc32Matched()) || GriffonUdpHandler.this.firstPacketWithOutCfgCRC32) {
/* 3214 */                 UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { Byte.MIN_VALUE });
/* 3215 */                 GriffonUdpHandler.this.firstPacketWithOutCfgCRC32 = false;
/* 3216 */                 GriffonUdpHandler.this.last_80_sent_time = System.currentTimeMillis();
/* 3217 */                 GriffonUdpHandler.this.runtimeCommandsPending = true;
/* 3218 */                 GriffonUdpHandler.this.crc32MatchedNUpdateStatusNotSent = true;
/* 3219 */                 GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/* 3220 */               } else if (!GriffonUdpHandler.this.fileSync_80_Sent && GriffonUdpHandler.this.requestedAllFilesCRC32 && GriffonUdpHandler.this.deviceCRC32 != null) {
/* 3221 */                 GriffonUdpHandler.this.syncCFG = GriffonDBManager.readGriffonModuleCfg(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/* 3222 */                 if (GriffonUdpHandler.this.syncCFG != null) {
/* 3223 */                   GriffonUdpHandler.this.fileIDData = GriffonHandlerHelper.prepareFileDataByCRC32Mismatch(GriffonUdpHandler.this.productID, GriffonUdpHandler.this.deviceCRC32, GriffonUdpHandler.this.syncCFG, true);
/*      */                   
/* 3225 */                   if (GriffonUdpHandler.this.fileIDData != null && GriffonUdpHandler.this.fileIDData.length > 1 && GriffonUdpHandler.this.fileIDData[0] > 0 && GriffonUdpHandler.this.fileIDData[0] <= ((GriffonUdpHandler.this.productID == Util.EnumProductIDs.GRIFFON_V1.getProductId()) ? 37 : 40)) {
/* 3226 */                     UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { Byte.MIN_VALUE });
/* 3227 */                     GriffonUdpHandler.this.last_80_sent_time = System.currentTimeMillis();
/* 3228 */                     GriffonUdpHandler.this.runtimeCommandsPending = true;
/* 3229 */                     GriffonUdpHandler.this.crc32MatchedNUpdateStatusNotSent = false;
/* 3230 */                     GriffonUdpHandler.this.fileSync_80_Sent = true;
/* 3231 */                     GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*      */                   } else {
/* 3233 */                     GriffonUdpHandler.this.requestedAllFilesCRC32 = false;
/* 3234 */                     GriffonUdpHandler.this.deviceCRC32 = null;
/*      */                   } 
/*      */                 } 
/*      */               } 
/*      */             } 
/*      */ 
/*      */             
/* 3241 */             if (appDataReceived && 
/* 3242 */               !GriffonHandlerHelper.pushAppDataReceived(this.module.getId_Client()) && 
/* 3243 */               GriffonUdpHandler.this.digitalPGMBufferReceived) {
/* 3244 */               GriffonUdpHandler.this.disableDigitalPGMBuffer = true;
/*      */             }
/*      */ 
/*      */             
/* 3248 */             if (GriffonUdpHandler.this.disableDigitalPGMBuffer || (GriffonUdpHandler.this.recordedLookupRequest && GriffonUdpHandler.this.recordedLookupRequestSent)) {
/* 3249 */               GriffonUdpHandler.this.last_80_sent_time = System.currentTimeMillis();
/* 3250 */               GriffonUdpHandler.this.runtimeCommandsPending = true;
/* 3251 */               UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { Byte.MIN_VALUE });
/* 3252 */               if (GriffonUdpHandler.this.recordedLookupRequest) {
/* 3253 */                 GriffonUdpHandler.this.recordedLookupRequestSent = false;
/*      */               }
/* 3255 */               GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/*      */             } 
/*      */             
/* 3258 */             return true;
/* 3259 */           } catch (Exception ex) {
/* 3260 */             return false;
/*      */           } 
/*      */         } 
/*      */         
/* 3264 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Error_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.AVERAGE, GriffonUdpHandler.this.sn, null);
/* 3265 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 3266 */         return false;
/*      */       }
/* 3268 */       catch (Exception ex) {
/* 3269 */         ex.printStackTrace();
/* 3270 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + GriffonUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */         try {
/* 3272 */           UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 21 });
/* 3273 */         } catch (IOException iOException) {}
/*      */ 
/*      */         
/* 3276 */         return false;
/*      */       } finally {
/* 3278 */         TblGriffonActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*      */       } 
/*      */     }
/*      */     
/*      */     public boolean sendEnableDisablePGMDigitalBuffer(int operation) {
/* 3283 */       byte[] data = new byte[4];
/* 3284 */       byte[] tmp = Functions.get2ByteArrayFromInt(32788);
/* 3285 */       tmp = Functions.swapLSB2MSB(tmp);
/* 3286 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 3287 */       data[2] = 1;
/* 3288 */       data[3] = (byte)operation;
/*      */       
/*      */       try {
/* 3291 */         tmp = prepareCommandPacket(data);
/* 3292 */         GriffonUdpHandler.this.sentCommand = 32788;
/* 3293 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, tmp);
/* 3294 */         GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/* 3295 */         return true;
/* 3296 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3297 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3298 */         ex.printStackTrace();
/* 3299 */         return false;
/*      */       } 
/*      */     }
/*      */     
/*      */     public boolean sendFileSyncCommand(int operation) {
/* 3304 */       byte[] data = new byte[4];
/* 3305 */       byte[] tmp = Functions.get2ByteArrayFromInt(32774);
/* 3306 */       tmp = Functions.swapLSB2MSB(tmp);
/* 3307 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 3308 */       data[2] = 1;
/* 3309 */       data[3] = (byte)operation;
/*      */       
/*      */       try {
/* 3312 */         tmp = prepareCommandPacket(data);
/* 3313 */         GriffonUdpHandler.this.sentCommand = 32774;
/* 3314 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, tmp);
/* 3315 */         if (operation == 6) {
/* 3316 */           GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.VM_READ_INITIATED;
/*      */         }
/* 3318 */         return true;
/* 3319 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3320 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3321 */         ex.printStackTrace();
/* 3322 */         return false;
/*      */       } 
/*      */     }
/*      */     
/*      */     public boolean sendEMFWCommand() {
/* 3327 */       byte[] data = new byte[4];
/* 3328 */       byte[] tmp = Functions.get2ByteArrayFromInt(32774);
/* 3329 */       tmp = Functions.swapLSB2MSB(tmp);
/* 3330 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 3331 */       data[2] = 1;
/* 3332 */       data[3] = 5;
/*      */       try {
/* 3334 */         tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 3335 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, tmp);
/* 3336 */         GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.EM_FW_REQUESTED;
/* 3337 */         return true;
/* 3338 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3339 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3340 */         ex.printStackTrace();
/* 3341 */         return false;
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     public boolean sendUpdateStatusCommand() {
/* 3347 */       byte[] data = new byte[3];
/* 3348 */       byte[] tmp = Functions.get2ByteArrayFromInt(32785);
/* 3349 */       tmp = Functions.swapLSB2MSB(tmp);
/* 3350 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 3351 */       data[2] = 0;
/*      */       try {
/* 3353 */         tmp = prepareCommandPacket(data);
/* 3354 */         GriffonUdpHandler.this.sentCommand = 32785;
/* 3355 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, tmp);
/* 3356 */         return true;
/* 3357 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3358 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3359 */         ex.printStackTrace();
/* 3360 */         return false;
/*      */       } 
/*      */     }
/*      */     
/*      */     public boolean sendLogoutCommand() throws InterruptedException {
/* 3365 */       byte[] data = new byte[3];
/*      */       try {
/* 3367 */         Thread.sleep(1000L);
/* 3368 */         GriffonUdpHandler.this.sentCommand = 32800;
/* 3369 */         System.arraycopy(Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(32800)), 0, data, 0, 2);
/* 3370 */         data[2] = 0;
/* 3371 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, prepareCommandPacket(data));
/* 3372 */         GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.LOGOUT_SENT;
/* 3373 */         return true;
/* 3374 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3375 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3376 */         ex.printStackTrace();
/* 3377 */         return false;
/*      */       } 
/*      */     }
/*      */     
/*      */     private void sendFile2Module(boolean firstChunk) throws SQLException, InterruptedException, FileNotFoundException, IOException {
/* 3382 */       if (firstChunk) {
/* 3383 */         GriffonUdpHandler.this.file = Functions.writeByteArrayToFile(GriffonUdpHandler.this.sn + "_" + GriffonUdpHandler.this.sp24DH.getCommandData(), GriffonUdpHandler.this.sp24DH.getCommandFileData());
/* 3384 */         GriffonUdpHandler.this.fc = (new RandomAccessFile(GriffonUdpHandler.this.file, "r")).getChannel();
/* 3385 */         GriffonUdpHandler.this.flen = (int)GriffonUdpHandler.this.fc.size();
/* 3386 */         GriffonUdpHandler.this.fc.position(0L);
/*      */       } 
/* 3388 */       if (GriffonUdpHandler.this.fc.position() < GriffonUdpHandler.this.flen) {
/* 3389 */         GriffonUdpHandler.this.blockLength = (int)((GriffonUdpHandler.this.flen - GriffonUdpHandler.this.fc.position() > GriffonUdpHandler.this.maxReadLength) ? GriffonUdpHandler.this.maxReadLength : (GriffonUdpHandler.this.flen - GriffonUdpHandler.this.fc.position()));
/* 3390 */         GriffonUdpHandler.this.blockBuf = ByteBuffer.allocate(GriffonUdpHandler.this.blockLength);
/* 3391 */         if (GriffonUdpHandler.this.fc.read(GriffonUdpHandler.this.blockBuf) == GriffonUdpHandler.this.blockLength) {
/* 3392 */           GriffonUdpHandler.this.block = GriffonUdpHandler.this.blockBuf.array();
/* 3393 */           GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 5];
/* 3394 */           GriffonUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(GriffonUdpHandler.this.blockIndex));
/* 3395 */           System.arraycopy(GriffonUdpHandler.this.ftmp, 0, GriffonUdpHandler.this.packet, 0, 2);
/* 3396 */           GriffonUdpHandler.this.packet[2] = (byte)Integer.parseInt(Integer.toHexString(GriffonUdpHandler.this.blockLength), 16);
/* 3397 */           System.arraycopy(GriffonUdpHandler.this.block, 0, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/* 3398 */           int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.blockLength + 3, 65535);
/* 3399 */           GriffonUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 3400 */           System.arraycopy(GriffonUdpHandler.this.ftmp, 0, GriffonUdpHandler.this.packet, GriffonUdpHandler.this.blockLength + 3, 2);
/* 3401 */           GriffonUdpHandler.this.outPacket = new DatagramPacket(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, GriffonUdpHandler.this.inPacket.getAddress(), GriffonUdpHandler.this.inPacket.getPort());
/* 3402 */           GriffonUdpHandler.this.socket.send(GriffonUdpHandler.this.outPacket);
/* 3403 */           if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 3404 */             updateLastCommunicationModuleData();
/* 3405 */             GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     private void sendVoiceMessageFile2Module() throws SQLException, InterruptedException, FileNotFoundException, IOException {
/* 3412 */       if (GriffonUdpHandler.this.file == null) {
/* 3413 */         GriffonUdpHandler.this.file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*      */       }
/* 3415 */       if (GriffonUdpHandler.this.raf == null) {
/* 3416 */         GriffonUdpHandler.this.raf = new RandomAccessFile(GriffonUdpHandler.this.file, "r");
/*      */       }
/* 3418 */       if (GriffonUdpHandler.this.raf.getChannel().position() < GriffonUdpHandler.this.currentVMPosition + GriffonUdpHandler.this.flen) {
/* 3419 */         GriffonUdpHandler.this.blockLength = (int)((GriffonUdpHandler.this.currentVMPosition + GriffonUdpHandler.this.flen - GriffonUdpHandler.this.raf.getChannel().position() > 240L) ? 240L : (GriffonUdpHandler.this.currentVMPosition + GriffonUdpHandler.this.flen - GriffonUdpHandler.this.raf.getChannel().position()));
/* 3420 */         GriffonUdpHandler.this.blockBuf = ByteBuffer.allocate(GriffonUdpHandler.this.blockLength);
/* 3421 */         if (GriffonUdpHandler.this.raf.getChannel().read(GriffonUdpHandler.this.blockBuf) == GriffonUdpHandler.this.blockLength) {
/* 3422 */           GriffonUdpHandler.this.block = GriffonUdpHandler.this.blockBuf.array();
/* 3423 */           GriffonUdpHandler.this.packet = new byte[GriffonUdpHandler.this.blockLength + 5];
/* 3424 */           GriffonUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(GriffonUdpHandler.this.blockIndex));
/* 3425 */           System.arraycopy(GriffonUdpHandler.this.ftmp, 0, GriffonUdpHandler.this.packet, 0, 2);
/* 3426 */           GriffonUdpHandler.this.packet[2] = (byte)Integer.parseInt(Integer.toHexString(GriffonUdpHandler.this.blockLength), 16);
/* 3427 */           System.arraycopy(GriffonUdpHandler.this.block, 0, GriffonUdpHandler.this.packet, 3, GriffonUdpHandler.this.blockLength);
/* 3428 */           int crcCalc = CRC16.calculate(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.blockLength + 3, 65535);
/* 3429 */           GriffonUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 3430 */           System.arraycopy(GriffonUdpHandler.this.ftmp, 0, GriffonUdpHandler.this.packet, GriffonUdpHandler.this.blockLength + 3, 2);
/* 3431 */           GriffonUdpHandler.this.outPacket = new DatagramPacket(GriffonUdpHandler.this.packet, 0, GriffonUdpHandler.this.packet.length, GriffonUdpHandler.this.inPacket.getAddress(), GriffonUdpHandler.this.inPacket.getPort());
/* 3432 */           GriffonUdpHandler.this.socket.send(GriffonUdpHandler.this.outPacket);
/* 3433 */           if (GriffonUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 3434 */             updateLastCommunicationModuleData();
/* 3435 */             GriffonUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     private void endCommand(int idCommand, short exec_Retries) throws SQLException, InterruptedException {
/* 3442 */       GriffonDBManager.executeSP_027(idCommand, (short)(exec_Retries + 1));
/*      */     }
/*      */     
/*      */     private void updateLastCommunicationModuleData() throws SQLException, InterruptedException {
/* 3446 */       if (TblGriffonActiveConnections.getInstance().containsKey(GriffonUdpHandler.this.sn)) {
/* 3447 */         GriffonDBManager.executeSP_028(((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, this.lastCommIface, this.currentSIM);
/*      */       }
/*      */     }
/*      */     
/*      */     public boolean sendVoiceFileCommand(int operation) {
/* 3452 */       byte[] data = new byte[4];
/* 3453 */       byte[] tmp = Functions.get2ByteArrayFromInt(32773);
/* 3454 */       tmp = Functions.swapLSB2MSB(tmp);
/* 3455 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 3456 */       data[2] = 1;
/* 3457 */       data[3] = (byte)operation;
/*      */       
/*      */       try {
/* 3460 */         tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 3461 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, tmp);
/* 3462 */         GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.VM_SEND_INITIATED;
/* 3463 */         return true;
/*      */       }
/* 3465 */       catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3466 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3467 */         ex.printStackTrace();
/* 3468 */         return false;
/*      */       } 
/*      */     }
/*      */     
/*      */     private void sendNextCommandFromQueue() throws SQLException, InterruptedException, Exception {
/* 3473 */       if (GriffonUdpHandler.this.cmdsList != null && ++GriffonUdpHandler.this.cmdIndex < GriffonUdpHandler.this.cmdsList.size()) {
/* 3474 */         GriffonUdpHandler.this.sp24DH = GriffonUdpHandler.this.cmdsList.get(GriffonUdpHandler.this.cmdIndex);
/* 3475 */         if (sendCommandPacket()) {
/* 3476 */           waitGriffonResponse();
/*      */         }
/*      */       } else {
/* 3479 */         UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, new byte[] { 6 });
/* 3480 */         ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).newCommand = false;
/* 3481 */         ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).nextSendingSolicitationReadingCommand = 0L;
/* 3482 */         GriffonUdpHandler.this.commandModeActivated = false;
/* 3483 */         this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).communicationTimeout;
/* 3484 */         GriffonUdpHandler.this.iTimeout = this.idleTimeout;
/* 3485 */         GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*      */       } 
/*      */     }
/*      */     
/*      */     private void requestNextVoiceMessageFromQueue() throws SQLException, InterruptedException, Exception {
/* 3490 */       if (GriffonUdpHandler.this.requiredVMList != null && GriffonUdpHandler.this.requiredVMIndex < GriffonUdpHandler.this.requiredVMList.size()) {
/* 3491 */         GriffonUdpHandler.this.currentVM = GriffonUdpHandler.this.requiredVMList.get(GriffonUdpHandler.this.requiredVMIndex);
/* 3492 */         GriffonUdpHandler.this.requiredVMIndex++;
/* 3493 */         if (sendFileSyncCommand(6)) {
/* 3494 */           waitGriffonResponse();
/*      */         }
/*      */       }
/* 3497 */       else if (GriffonUdpHandler.this.sp24DH != null) {
/* 3498 */         GriffonUdpHandler.this.requiredVMIndex = 0;
/* 3499 */         GriffonHandlerHelper.finalizeReceiveCFGFileCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule, GriffonUdpHandler.this.syncCFG, GriffonUdpHandler.this.vmList);
/* 3500 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 3501 */         endCommand(GriffonUdpHandler.this.sp24DH.getId_Command(), GriffonUdpHandler.this.sp24DH.getExec_Retries());
/* 3502 */         sendNextCommandFromQueue();
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private void saveVoiceMessageIntoRepo() throws IOException, SQLException, InterruptedException, Exception {
/* 3508 */       if (GriffonUdpHandler.this.currentVM != null) {
/* 3509 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonUdpHandler.this.currentVM.getVmName() + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, GriffonUdpHandler.this.sn, null);
/* 3510 */         GriffonUdpHandler.this.raf.seek(GriffonUdpHandler.this.currentVMPosition);
/* 3511 */         byte[] vmContent = new byte[GriffonUdpHandler.this.currentVM.getVmLength()];
/* 3512 */         GriffonUdpHandler.this.raf.read(vmContent);
/* 3513 */         GriffonDBManager.saveVoiceMessage(vmContent, GriffonUdpHandler.this.currentVM.getVmLength(), GriffonUdpHandler.this.currentVM.getVmName(), GriffonUdpHandler.this.currentVM.getVmCRC32());
/*      */       } 
/*      */     }
/*      */     
/*      */     private void sendNextVoiceMessageFromQueue() throws SQLException, InterruptedException, Exception {
/* 3518 */       if (GriffonUdpHandler.this.requiredVMList != null && GriffonUdpHandler.this.requiredVMIndex < GriffonUdpHandler.this.requiredVMList.size()) {
/* 3519 */         GriffonUdpHandler.this.currentVM = GriffonUdpHandler.this.requiredVMList.get(GriffonUdpHandler.this.requiredVMIndex);
/* 3520 */         GriffonUdpHandler.this.requiredVMIndex++;
/* 3521 */         if (sendVoiceFileCommand(7)) {
/* 3522 */           waitGriffonResponse();
/*      */         }
/*      */       } else {
/* 3525 */         if (GriffonUdpHandler.this.file == null) {
/* 3526 */           GriffonUdpHandler.this.file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(GriffonUdpHandler.this.sn)).idModule);
/*      */         }
/* 3528 */         if (GriffonUdpHandler.this.raf == null) {
/* 3529 */           GriffonUdpHandler.this.raf = new RandomAccessFile(GriffonUdpHandler.this.file, "r");
/*      */         }
/* 3531 */         GriffonUdpHandler.this.raf.close();
/*      */         
/* 3533 */         if (GriffonUdpHandler.this.file != null && GriffonUdpHandler.this.file.exists()) {
/* 3534 */           GriffonUdpHandler.this.file.delete();
/*      */         }
/* 3536 */         if (GriffonUdpHandler.this.sp24DH != null) {
/* 3537 */           GriffonUdpHandler.this.requiredVMIndex = 0;
/* 3538 */           byte[] data = new byte[4];
/* 3539 */           GriffonUdpHandler.this.tmp = Functions.get2ByteArrayFromInt(GriffonUdpHandler.this.sp24DH.getCommand_Type());
/* 3540 */           GriffonUdpHandler.this.tmp = Functions.swapLSB2MSB(GriffonUdpHandler.this.tmp);
/* 3541 */           System.arraycopy(GriffonUdpHandler.this.tmp, 0, data, 0, 2);
/* 3542 */           data[2] = 1;
/* 3543 */           data[3] = (byte)Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */           try {
/* 3545 */             GriffonUdpHandler.this.tmp = prepareCommandPacket(data);
/* 3546 */             UDPFunctions.send(GriffonUdpHandler.this.socket, GriffonUdpHandler.this.inPacket, GriffonUdpHandler.this.tmp);
/* 3547 */             GriffonUdpHandler.this.nextState = (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32773) ? GriffonEnums.UDPExceutionsStates.FILE_SENDING_INITIATED : GriffonEnums.UDPExceutionsStates.EXPECTED_REPLY;
/* 3548 */             GriffonUdpHandler.this.sentCommand = GriffonUdpHandler.this.sp24DH.getCommand_Type();
/* 3549 */             if (GriffonUdpHandler.this.sp24DH.getCommand_Type() == 32774) {
/* 3550 */               GriffonUdpHandler.this.requestedAllFilesCRC32 = true;
/*      */             }
/* 3552 */             waitGriffonResponse();
/* 3553 */           } catch (IOException|InterruptedException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|SQLException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 3554 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3555 */             ex.printStackTrace();
/*      */           } 
/*      */         } 
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
/*      */     private void waitGriffonResponse() throws InterruptedException, SQLException, IOException {
/* 3570 */       GriffonUdpHandler.this.waitingForResponse = true;
/* 3571 */       GriffonUdpHandler.this.packetSentTime = System.currentTimeMillis();
/* 3572 */       while (GriffonUdpHandler.this.waitingForResponse && GriffonUdpHandler.this.packetSentTime + GriffonUdpHandler.this.responseTimeout > System.currentTimeMillis()) {
/* 3573 */         Thread.sleep(5L);
/*      */       }
/* 3575 */       if (GriffonUdpHandler.this.waitingForResponse) {
/* 3576 */         switch (GriffonUdpHandler.this.nextState) {
/*      */           case EXPECTED_REPLY:
/* 3578 */             GriffonUdpHandler.this.commandModeActivated = false;
/* 3579 */             GriffonUdpHandler.this.nextState = GriffonEnums.UDPExceutionsStates.M2S_PACKET_PARSING;
/*      */             break;
/*      */           
/*      */           case COMMAND_PACKET_REPSONE:
/* 3583 */             GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           
/*      */           case CFG_FILE_RECEIVING:
/* 3587 */             if (GriffonUdpHandler.this.sp24DH != null) {
/* 3588 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + GriffonUdpHandler.this.sp24DH.getCommandData() + "]", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command()); break;
/*      */             } 
/* 3590 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module"), Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/*      */             break;
/*      */ 
/*      */           
/*      */           case FILE_SENDING:
/* 3595 */             while (GriffonUdpHandler.this.waitingForResponse) {
/* 3596 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + "[" + GriffonHandlerHelper.getFileNameByCommandData(Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10), GriffonUdpHandler.this.sp24DH.getCommand_Type()) + "]", Enums.EnumMessagePriority.HIGH, GriffonUdpHandler.this.sn, null);
/* 3597 */               if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/* 3598 */                 if (--GriffonUdpHandler.this.blockIndex < 0) {
/* 3599 */                   GriffonUdpHandler.this.blockIndex = 0;
/*      */                 }
/* 3601 */                 GriffonUdpHandler.this.fc.position(GriffonUdpHandler.this.fc.position() - GriffonUdpHandler.this.blockLength);
/* 3602 */                 sendFile2Module(false);
/* 3603 */                 GriffonUdpHandler.this.blockIndex++;
/* 3604 */                 GriffonUdpHandler.this.packetSentTime = System.currentTimeMillis();
/* 3605 */                 while (GriffonUdpHandler.this.waitingForResponse && GriffonUdpHandler.this.packetSentTime + GriffonUdpHandler.this.responseTimeout > System.currentTimeMillis())
/* 3606 */                   Thread.sleep(5L); 
/*      */                 continue;
/*      */               } 
/* 3609 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/* 3610 */               GriffonUdpHandler.this.retry = 0;
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case VM_SEND_IN_PROGRESS:
/* 3617 */             while (GriffonUdpHandler.this.waitingForResponse) {
/* 3618 */               if ((GriffonUdpHandler.this.retry = (short)(GriffonUdpHandler.this.retry + 1)) < GriffonUdpHandler.this.maxRetries) {
/* 3619 */                 if (--GriffonUdpHandler.this.blockIndex < 0) {
/* 3620 */                   GriffonUdpHandler.this.blockIndex = 0;
/*      */                 }
/* 3622 */                 GriffonUdpHandler.this.raf.getChannel().position(GriffonUdpHandler.this.raf.getChannel().position() - GriffonUdpHandler.this.blockLength);
/* 3623 */                 sendVoiceMessageFile2Module();
/* 3624 */                 GriffonUdpHandler.this.blockIndex++;
/* 3625 */                 GriffonUdpHandler.this.packetSentTime = System.currentTimeMillis();
/* 3626 */                 while (GriffonUdpHandler.this.waitingForResponse && GriffonUdpHandler.this.packetSentTime + GriffonUdpHandler.this.responseTimeout > System.currentTimeMillis())
/* 3627 */                   Thread.sleep(5L); 
/*      */                 continue;
/*      */               } 
/* 3630 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/* 3631 */               GriffonUdpHandler.this.retry = 0;
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case READ_EM_FW_DATA:
/*      */           case FILE_SENDING_INITIATED:
/*      */           case HANDLE_DASH_BOARD_BUFFER:
/*      */           case REQUESTED_CRC32:
/*      */           case REQUESTED_ALL_FILES_CRC32:
/*      */           case FIRMWARE_CRC_RESPONSE:
/*      */             break;
/*      */ 
/*      */           
/*      */           case VM_READ_INITIATED:
/*      */           case VM_READ_COMMAND:
/*      */           case VM_READ_IN_PROGRESS:
/*      */           case EVENT_LOG_FILE_RECEIVING:
/* 3649 */             GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + GriffonUdpHandler.this.sp24DH.getCommandData() + "]", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           
/*      */           case VM_SEND_INITIATED:
/*      */           case VM_SEND_COMMAND:
/* 3654 */             GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           
/*      */           default:
/* 3658 */             if (GriffonUdpHandler.this.sp24DH != null)
/*      */             {
/* 3660 */               GriffonHandlerHelper.registerFailureSendCommand(GriffonUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + "[" + GriffonHandlerHelper.getFileNameByCommandData(Character.digit(GriffonUdpHandler.this.sp24DH.getCommandData().charAt(0), 10), GriffonUdpHandler.this.sp24DH.getCommand_Type()) + "]" + LocaleMessage.getLocaleMessage("was_sent_successfully"), GriffonUdpHandler.this.sp24DH.getExec_Retries(), GriffonUdpHandler.this.sp24DH.getId_Command());
/*      */             }
/*      */             break;
/*      */         } 
/*      */       }
/* 3665 */       this.running = false;
/*      */     }
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\GriffonUdpHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */