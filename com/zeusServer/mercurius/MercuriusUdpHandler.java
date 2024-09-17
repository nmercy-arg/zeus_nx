/*      */ package com.zeusServer.mercurius;
/*      */ 
/*      */ import com.zeus.mercuriusAVL.derby.beans.AudioNJSFileInfo;
/*      */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.socket.communication.UDPDataServer;
/*      */ import com.zeusServer.socket.communication.UdpV2Handler;
/*      */ import com.zeusServer.tblConnections.TblMercuriusAVLActiveUdpConnections;
/*      */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
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
/*      */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
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
/*      */ 
/*      */ 
/*      */ public class MercuriusUdpHandler
/*      */   extends UdpV2Handler
/*      */ {
/*   66 */   DatagramPacket outPacket = null;
/*      */   private DatagramSocket socket;
/*      */   private DatagramPacket inPacket;
/*   69 */   private String myThreadGuid = UUID.randomUUID().toString();
/*      */   private String remoteIP;
/*      */   private UdpV2Handler currInsance;
/*   72 */   private short encType = 1;
/*      */   
/*      */   private boolean initiatedGeofenceRequest = false;
/*      */   
/*      */   public Enums.MercuriusUDPCommStates nextState;
/*      */   private List<SP_024DataHolder> cmdsList;
/*      */   private int cmdIndex;
/*      */   private boolean commandModeActivated;
/*      */   private int sentCommand;
/*   81 */   int blockIndex = 0;
/*   82 */   int maxReadLength = 240;
/*      */   int blockLength;
/*   84 */   short maxRetries = 3;
/*      */   int noOfGeofenceReceived;
/*   86 */   short retry = 0;
/*   87 */   int flen = 0;
/*      */   int rcvBlockIndex;
/*   89 */   int expBlockIndex = 0;
/*   90 */   int recvCfgCRC32 = 0;
/*   91 */   int fileContentIndex = 0;
/*   92 */   byte[] block = null;
/*   93 */   byte[] bid = new byte[2];
/*      */   byte[] packet;
/*      */   byte[] ftmp;
/*      */   byte[] tmp;
/*   97 */   byte[] fileContent = null;
/*      */   String filePath;
/*   99 */   FileChannel fc = null;
/*      */   ByteBuffer blockBuf;
/*      */   SP_024DataHolder sp24DH;
/*  102 */   List<AudioNJSFileInfo> requriedVMList = null;
/*      */   boolean fileSendingFlag;
/*  104 */   AudioNJSFileInfo currentAJS = null;
/*      */   private long currentVMPosition;
/*  106 */   int requiredVMIndex = 0;
/*  107 */   int audioCounter = 100;
/*  108 */   int jsCounter = 300;
/*      */   int uploadLookupCRC32;
/*  110 */   List<AudioNJSFileInfo> uploadedJSFileList = null;
/*  111 */   List<AudioNJSFileInfo> reqajsList = null;
/*  112 */   RandomAccessFile raf = null;
/*  113 */   File file = null;
/*  114 */   long nextUpdateFieldLastCommunication = 0L;
/*  115 */   int responseTimeout = 120000;
/*      */   long packetSentTime;
/*      */   boolean waitingForResponse;
/*      */   short lCommIface;
/*      */   long iTimeout;
/*      */   int newCMDCheck;
/*      */   int m2sPacketReceived;
/*      */   int recevedTimeZone;
/*  123 */   byte[] fileIDData = null;
/*  124 */   ModuleCFG mCFG = null;
/*      */   boolean fileSync_80_Sent = false;
/*  126 */   byte[] first16 = new byte[16];
/*  127 */   byte[] tmp4 = new byte[4];
/*  128 */   int lPad = 0;
/*      */ 
/*      */   
/*      */   public MercuriusUdpHandler(DatagramSocket socket, DatagramPacket inPacket, short encType) {
/*  132 */     this.socket = socket;
/*  133 */     this.inPacket = inPacket;
/*  134 */     this.nextState = Enums.MercuriusUDPCommStates.M2S_PACKET_PARSING;
/*  135 */     this.encType = encType;
/*  136 */     this.remoteIP = this.inPacket.getAddress().toString() + ":" + this.inPacket.getPort();
/*  137 */     this.remoteIP = this.remoteIP.substring(1);
/*  138 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  139 */     this.iTimeout = this.idleTimeout;
/*  140 */     this.currInsance = this;
/*  141 */     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ 
/*      */   
/*      */   public void processM2SPacket(byte[] data) {
/*  146 */     this.waitingForResponse = false;
/*  147 */     this.lastCommunicationTime = System.currentTimeMillis();
/*  148 */     UDPDataServer.clientHelper.execute(new MercuriusAVLUdpClientHandler(data));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void processM2SPacket(byte[] data, int actualDataSize) {}
/*      */ 
/*      */ 
/*      */   
/*      */   public void removeIdleConnections() {
/*  158 */     if (this.iTimeout < System.currentTimeMillis()) {
/*  159 */       dispose();
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   public void updateRemoteIP(String newIP, DatagramSocket newSocket, DatagramPacket newPacket) {
/*  165 */     this.remoteIP = newIP;
/*  166 */     this.socket = newSocket;
/*  167 */     this.inPacket = newPacket;
/*      */   }
/*      */ 
/*      */   
/*      */   public DatagramSocket getCurrentSocket() {
/*  172 */     return this.socket;
/*      */   }
/*      */ 
/*      */   
/*      */   public DatagramPacket getCurrentPacket() {
/*  177 */     return this.inPacket;
/*      */   }
/*      */ 
/*      */   
/*      */   public void sendNewCMDAtInActiveTime() {
/*  182 */     if (TblMercuriusActiveConnections.getInstance().containsKey(this.sn) && 
/*  183 */       ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).newCommand && this.newCMDCheck++ < 3 && !this.commandModeActivated) {
/*      */       
/*  185 */       byte[] newCmd = Functions.intToByteArray(128, 1);
/*  186 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + '', Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */       try {
/*  188 */         UDPFunctions.send(this.socket, this.inPacket, newCmd);
/*  189 */         this.nextState = Enums.MercuriusUDPCommStates.EXPECTED_REPLY;
/*  190 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  191 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*      */       }
/*  193 */       catch (IOException iOException) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void dispose() {
/*  201 */     if (this.sn != null && TblMercuriusActiveConnections.getInstance().containsKey(this.sn) && 
/*  202 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).ownerThreadGuid)) {
/*  203 */       TblMercuriusActiveConnections.removeConnection(this.sn);
/*      */     }
/*      */     
/*  206 */     if (TblMercuriusAVLActiveUdpConnections.getInstance().containsKey(this.remoteIP))
/*  207 */       TblMercuriusAVLActiveUdpConnections.getInstance().remove(this.remoteIP); 
/*      */   }
/*      */   
/*      */   public class MercuriusAVLUdpClientHandler
/*      */     extends MercuriusAVLRoutines implements Runnable {
/*      */     private byte[] data;
/*      */     
/*      */     private MercuriusAVLUdpClientHandler(byte[] data) {
/*  215 */       this.data = data; } public void run() { try {
/*      */         int gpsfwLen; int custVersionLen; int sirfVersionLen; byte[] tmp; String sirfVersion;
/*      */         String custVersion;
/*      */         int crcCalc;
/*      */         int crcRecv;
/*      */         String prodBin;
/*      */         short prodI;
/*  222 */         switch (MercuriusUdpHandler.this.nextState) {
/*      */           case GPS_FW_RESPONSE:
/*  224 */             gpsfwLen = this.data[0] & 0xFF;
/*  225 */             custVersionLen = this.data[1] & 0xFF;
/*  226 */             sirfVersionLen = this.data[0] & 0xFF;
/*  227 */             tmp = new byte[sirfVersionLen];
/*  228 */             System.arraycopy(this.data, 2, tmp, 0, sirfVersionLen);
/*  229 */             sirfVersion = Functions.getASCIIFromByteArray(tmp);
/*  230 */             tmp = new byte[custVersionLen];
/*  231 */             System.arraycopy(this.data, sirfVersionLen + 2, tmp, 0, custVersionLen);
/*  232 */             custVersion = Functions.getASCIIFromByteArray(tmp);
/*  233 */             MercuriusAVLHandlerHelper.updateGPSFWVersion(((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule, custVersion, sirfVersion);
/*  234 */             MercuriusAVLHandlerHelper.endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/*  235 */             sendNextCommandFromQueue();
/*      */             break;
/*      */           case AJS_SINGLE_CRC32_REQUESTED:
/*  238 */             if (this.data[0] == 6) {
/*  239 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_SINGLE_CRC32_READING;
/*  240 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */           case AJS_SINGLE_CRC32_READING:
/*  244 */             if (MercuriusUdpHandler.this.sp24DH != null) {
/*  245 */               int i = CRC16.calculate(this.data, 0, 7, 65535);
/*  246 */               byte[] buffer = new byte[2];
/*  247 */               buffer[0] = this.data[8];
/*  248 */               buffer[1] = this.data[7];
/*  249 */               int j = Functions.getIntFrom2ByteArray(buffer);
/*  250 */               if (i == j) {
/*  251 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  252 */                 MercuriusUdpHandler.this.tmp4[3] = this.data[3];
/*  253 */                 MercuriusUdpHandler.this.tmp4[2] = this.data[4];
/*  254 */                 MercuriusUdpHandler.this.tmp4[1] = this.data[5];
/*  255 */                 MercuriusUdpHandler.this.tmp4[0] = this.data[6];
/*  256 */                 int rcvLookupCRC32 = Functions.getIntFrom4ByteArray(MercuriusUdpHandler.this.tmp4);
/*  257 */                 if (rcvLookupCRC32 != MercuriusUdpHandler.this.uploadLookupCRC32) {
/*  258 */                   MercuriusUdpHandler.this.uploadedJSFileList = ConfigFileParser.getAudioScriptLookupDataFromCFG(null, 0, MercuriusUdpHandler.this.mCFG);
/*      */                   
/*  260 */                   if (requestAudioJSInfo(32773)) {
/*  261 */                     MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_LOOKUP_DATA_INITIATED;
/*  262 */                     registerResponseTimeOut();
/*      */                   }  break;
/*      */                 } 
/*  265 */                 initiateReadingAllFilesCRC32();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */           
/*      */           case AJS_LOOKUP_DATA_INITIATED:
/*  271 */             if (this.data[0] == 6) {
/*  272 */               byte[] fileIDData = new byte[2];
/*  273 */               fileIDData[0] = 1;
/*  274 */               fileIDData[1] = 3;
/*  275 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, fileIDData);
/*  276 */               MercuriusUdpHandler.this.blockIndex = 0;
/*  277 */               MercuriusUdpHandler.this.flen = 0;
/*  278 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  279 */               MercuriusUdpHandler.this.fileContent = null;
/*  280 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_LOOKUP_DATA_READ_IN_PROGRESS;
/*  281 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */           case AJS_LOOKUP_DATA_READ_IN_PROGRESS:
/*  285 */             if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/*  286 */               MercuriusUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  287 */               System.arraycopy(this.data, 0, MercuriusUdpHandler.this.bid, 0, 2);
/*  288 */               MercuriusUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(MercuriusUdpHandler.this.bid));
/*  289 */               if (MercuriusUdpHandler.this.expBlockIndex == MercuriusUdpHandler.this.rcvBlockIndex) {
/*  290 */                 MercuriusUdpHandler.this.packet = new byte[MercuriusUdpHandler.this.blockLength + 3];
/*  291 */                 System.arraycopy(this.data, 0, MercuriusUdpHandler.this.packet, 0, 3);
/*  292 */                 System.arraycopy(this.data, 3, MercuriusUdpHandler.this.packet, 3, MercuriusUdpHandler.this.blockLength);
/*  293 */                 int i = CRC16.calculate(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.packet.length, 65535);
/*  294 */                 tmp = new byte[2];
/*  295 */                 System.arraycopy(this.data, MercuriusUdpHandler.this.blockLength + 3, tmp, 0, 2);
/*  296 */                 int j = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp));
/*  297 */                 if (i == j) {
/*  298 */                   if (MercuriusUdpHandler.this.expBlockIndex == 0) {
/*  299 */                     System.arraycopy(this.data, 3, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  300 */                     MercuriusUdpHandler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*  301 */                     MercuriusUdpHandler.this.lPad = (MercuriusUdpHandler.this.flen + 12) % 16;
/*  302 */                     if (MercuriusUdpHandler.this.lPad > 0) {
/*  303 */                       MercuriusUdpHandler.this.lPad = 16 - MercuriusUdpHandler.this.lPad;
/*      */                     }
/*  305 */                     System.arraycopy(MercuriusUdpHandler.this.first16, 8, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  306 */                     MercuriusUdpHandler.this.recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*  307 */                     MercuriusUdpHandler.this.fileContent = new byte[MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12];
/*      */                   } 
/*  309 */                   System.arraycopy(this.data, 3, MercuriusUdpHandler.this.fileContent, MercuriusUdpHandler.this.fileContentIndex, MercuriusUdpHandler.this.blockLength);
/*  310 */                   MercuriusUdpHandler.this.fileContentIndex += MercuriusUdpHandler.this.blockLength;
/*  311 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  312 */                   MercuriusUdpHandler.this.retry = 0;
/*  313 */                   MercuriusUdpHandler.this.expBlockIndex++;
/*  314 */                   if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  315 */                     updateLastCommunicationModuleData();
/*  316 */                     MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*  319 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  321 */                 if (MercuriusUdpHandler.this.fileContentIndex < MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12 && MercuriusUdpHandler.this.flen > 0) {
/*  322 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  325 */                 String str = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  326 */                 MercuriusUdpHandler.this.encType = Short.parseShort(str.substring(0, 2), 2);
/*  327 */                 str = str.substring(2);
/*  328 */                 str = str.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  329 */                 short s = Short.parseShort(str, 2);
/*  330 */                 if (s == Util.EnumProductIDs.MERCURIUS.getProductId()) {
/*  331 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*      */                 } else {
/*  335 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  340 */               MercuriusUdpHandler.this.retry = 0;
/*  341 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  342 */               MercuriusUdpHandler.this.expBlockIndex = 0;
/*      */             } 
/*      */             
/*  345 */             if (MercuriusUdpHandler.this.fileContentIndex >= MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12 && MercuriusUdpHandler.this.flen > 0) {
/*  346 */               List<AudioNJSFileInfo> lookupFileList = null;
/*  347 */               byte[] decData = new byte[MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12];
/*      */               
/*  349 */               byte[] encBlock = new byte[16];
/*  350 */               if (MercuriusUdpHandler.this.fileContent.length >= 16 && MercuriusUdpHandler.this.fileContent.length % 16 == 0) {
/*  351 */                 for (int i = 0; i < MercuriusUdpHandler.this.fileContent.length; ) {
/*  352 */                   System.arraycopy(MercuriusUdpHandler.this.fileContent, i, encBlock, 0, 16);
/*  353 */                   byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/*  354 */                   System.arraycopy(arrayOfByte, 0, decData, i, 16);
/*  355 */                   i += 16;
/*      */                 } 
/*      */               }
/*  358 */               byte[] decBlock = new byte[MercuriusUdpHandler.this.flen];
/*  359 */               System.arraycopy(decData, MercuriusAVLHandlerHelper.AVL_FILE_HEADER_SIZE, decBlock, 0, MercuriusUdpHandler.this.flen);
/*  360 */               int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/*  361 */               if (MercuriusUdpHandler.this.recvCfgCRC32 == calcCfgCrc32) {
/*  362 */                 lookupFileList = ConfigFileParser.getAudioScriptLookupData(decBlock);
/*      */               }
/*  364 */               if (lookupFileList == null) {
/*  365 */                 MercuriusUdpHandler.this.reqajsList = MercuriusUdpHandler.this.uploadedJSFileList;
/*  366 */               } else if (MercuriusUdpHandler.this.uploadedJSFileList != null && lookupFileList != null) {
/*  367 */                 MercuriusUdpHandler.this.reqajsList = new ArrayList<>();
/*  368 */                 for (AudioNJSFileInfo ajs : MercuriusUdpHandler.this.uploadedJSFileList) {
/*  369 */                   if (!lookupFileList.contains(ajs)) {
/*  370 */                     MercuriusUdpHandler.this.reqajsList.add(ajs);
/*      */                   }
/*      */                 } 
/*      */               } 
/*  374 */               if (MercuriusUdpHandler.this.reqajsList != null && MercuriusUdpHandler.this.reqajsList.size() > 0) {
/*  375 */                 MercuriusUdpHandler.this.requiredVMIndex = 0;
/*  376 */                 sendNextVoiceMessageFromQueue();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */           case AJS_SEND_INITIATED:
/*  381 */             if (this.data[0] == 6 && 
/*  382 */               MercuriusUdpHandler.this.currentAJS != null) {
/*  383 */               MercuriusUdpHandler.this.file = new File("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule);
/*  384 */               RandomAccessFile raf = new RandomAccessFile(MercuriusUdpHandler.this.file, "r");
/*  385 */               int fpos = 0;
/*  386 */               boolean fileFound = false;
/*  387 */               byte[] tm = new byte[12];
/*  388 */               byte[] vmHeader = new byte[22];
/*      */               try {
/*  390 */                 while (fpos < MercuriusUdpHandler.this.file.length()) {
/*  391 */                   raf.seek(fpos);
/*  392 */                   raf.read(vmHeader);
/*  393 */                   fpos += 22;
/*  394 */                   MercuriusUdpHandler.this.currentVMPosition = fpos;
/*  395 */                   System.arraycopy(vmHeader, 2, tm, 0, 12);
/*      */                   
/*  397 */                   System.arraycopy(vmHeader, 14, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  398 */                   int fle = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*      */                   
/*  400 */                   System.arraycopy(vmHeader, 18, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  401 */                   int crc32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*      */                   
/*  403 */                   if ((new String(tm, "ISO-8859-1")).trim().toLowerCase().equals(MercuriusUdpHandler.this.currentAJS.getName().toLowerCase())) {
/*  404 */                     fileFound = true;
/*      */                     break;
/*      */                   } 
/*  407 */                   fpos += fle;
/*      */                 } 
/*      */               } finally {
/*      */                 
/*  411 */                 raf.close();
/*  412 */                 raf = null;
/*      */               } 
/*  414 */               if (fileFound && (
/*  415 */                 new String(tm, "ISO-8859-1")).trim().toLowerCase().equals(MercuriusUdpHandler.this.currentAJS.getName().toLowerCase())) {
/*  416 */                 byte[] vmcmd = new byte[19];
/*  417 */                 vmcmd[0] = (byte)MercuriusUdpHandler.this.currentAJS.getDir();
/*  418 */                 System.arraycopy(tm, 0, vmcmd, 1, tm.length);
/*  419 */                 System.arraycopy(vmHeader, 14, vmcmd, 13, 4);
/*  420 */                 int i = CRC16.calculate(vmcmd, 0, 17, 65535);
/*  421 */                 byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(i));
/*  422 */                 System.arraycopy(tmp2, 0, vmcmd, 17, 2);
/*  423 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, vmcmd);
/*      */                 
/*  425 */                 MercuriusUdpHandler.this.flen = MercuriusUdpHandler.this.currentAJS.getLength();
/*  426 */                 MercuriusUdpHandler.this.blockIndex = 0;
/*  427 */                 MercuriusUdpHandler.this.retry = 0;
/*  428 */                 if (MercuriusUdpHandler.this.file == null) {
/*  429 */                   MercuriusUdpHandler.this.file = new File("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule);
/*      */                 }
/*  431 */                 if (raf == null) {
/*  432 */                   raf = new RandomAccessFile(MercuriusUdpHandler.this.file, "r");
/*      */                 }
/*      */                 
/*  435 */                 raf.getChannel().position(MercuriusUdpHandler.this.currentVMPosition);
/*  436 */                 MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_SEND_IN_PROGRESS;
/*  437 */                 registerResponseTimeOut();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case AJS_SEND_IN_PROGRESS:
/*  445 */             if (MercuriusUdpHandler.this.file == null) {
/*  446 */               MercuriusUdpHandler.this.file = new File("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule);
/*      */             }
/*  448 */             if (MercuriusUdpHandler.this.raf == null) {
/*  449 */               MercuriusUdpHandler.this.raf = new RandomAccessFile(MercuriusUdpHandler.this.file, "r");
/*  450 */               MercuriusUdpHandler.this.raf.getChannel().position(MercuriusUdpHandler.this.currentVMPosition);
/*      */             } 
/*  452 */             if (this.data[0] == 6) {
/*  453 */               if (MercuriusUdpHandler.this.currentVMPosition > 0L && MercuriusUdpHandler.this.raf.getChannel().position() < MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen) {
/*  454 */                 if (MercuriusUdpHandler.this.raf.getChannel().position() < MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen) {
/*  455 */                   MercuriusUdpHandler.this.retry = 0;
/*  456 */                   this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationTimeout;
/*  457 */                   MercuriusUdpHandler.this.iTimeout = this.idleTimeout;
/*  458 */                   Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + MercuriusUdpHandler.this.currentAJS.getName() + " (" + (MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen - MercuriusUdpHandler.this.raf.getChannel().position()) + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/*  459 */                   MercuriusUdpHandler.this.responseTimeout = 120000;
/*  460 */                   sendVoiceMessageFile2Module();
/*  461 */                   MercuriusUdpHandler.this.fileSendingFlag = true;
/*  462 */                   MercuriusUdpHandler.this.blockIndex++;
/*  463 */                   registerResponseTimeOut(); break;
/*      */                 } 
/*  465 */                 if (this.data[1] == 6) {
/*  466 */                   MercuriusUdpHandler.this.blockIndex = 0;
/*  467 */                   MercuriusUdpHandler.this.flen = 0;
/*  468 */                   saveAJSFileIntoRepo();
/*  469 */                   sendNextVoiceMessageFromQueue();
/*      */                 } 
/*      */                 break;
/*      */               } 
/*  473 */               if (MercuriusUdpHandler.this.fileSendingFlag) {
/*  474 */                 registerResponseTimeOut();
/*  475 */                 MercuriusUdpHandler.this.fileSendingFlag = false; break;
/*      */               } 
/*  477 */               MercuriusUdpHandler.this.blockIndex = 0;
/*  478 */               MercuriusUdpHandler.this.flen = 0;
/*  479 */               saveAJSFileIntoRepo();
/*  480 */               sendNextVoiceMessageFromQueue();
/*      */               
/*      */               break;
/*      */             } 
/*  484 */             if (this.data[0] == 21 && 
/*  485 */               MercuriusUdpHandler.this.raf.getChannel().position() < MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen) {
/*  486 */               if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/*  487 */                 if (MercuriusUdpHandler.this.lCommIface == 1) {
/*  488 */                   if (MercuriusUdpHandler.this.retry == 0) {
/*  489 */                     MercuriusUdpHandler.this.responseTimeout = 120000;
/*  490 */                   } else if (MercuriusUdpHandler.this.retry == 1) {
/*  491 */                     MercuriusUdpHandler.this.responseTimeout = 210000;
/*  492 */                   } else if (MercuriusUdpHandler.this.retry == 2) {
/*  493 */                     MercuriusUdpHandler.this.responseTimeout = 300000;
/*      */                   } 
/*      */                 } else {
/*  496 */                   MercuriusUdpHandler.this.responseTimeout = 120000;
/*      */                 } 
/*  498 */                 MercuriusUdpHandler.this.blockIndex--;
/*  499 */                 MercuriusUdpHandler.this.raf.getChannel().position(MercuriusUdpHandler.this.raf.getChannel().position() - MercuriusUdpHandler.this.blockLength);
/*  500 */                 sendVoiceMessageFile2Module();
/*  501 */                 MercuriusUdpHandler.this.blockIndex++;
/*  502 */                 registerResponseTimeOut(); break;
/*      */               } 
/*  504 */               MercuriusUdpHandler.this.retry = 0;
/*  505 */               MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + MercuriusAVLHandlerHelper.getFileNameByCommandData(MercuriusUdpHandler.this.sp24DH.getCommandData()) + LocaleMessage.getLocaleMessage("invalid"), MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case AJS_READ_INITIATED:
/*  511 */             if (this.data[0] == 6 && 
/*  512 */               MercuriusUdpHandler.this.currentAJS != null) {
/*  513 */               byte[] vmcmd = new byte[15];
/*  514 */               vmcmd[0] = (byte)MercuriusUdpHandler.this.currentAJS.getDir();
/*  515 */               System.arraycopy(MercuriusUdpHandler.this.currentAJS.getName().getBytes("ISO-8859-1"), 0, vmcmd, 1, MercuriusUdpHandler.this.currentAJS.getName().length());
/*  516 */               int i = CRC16.calculate(vmcmd, 0, 13, 65535);
/*  517 */               byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(i));
/*  518 */               System.arraycopy(tmp2, 0, vmcmd, 13, 2);
/*  519 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, vmcmd);
/*  520 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_READ_COMMAND;
/*  521 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */           
/*      */           case AJS_READ_COMMAND:
/*  526 */             if (this.data[0] == 6 && 
/*  527 */               MercuriusUdpHandler.this.currentAJS != null) {
/*  528 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  529 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_READ_IN_PROGRESS;
/*  530 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */           
/*      */           case AJS_READ_IN_PROGRESS:
/*  535 */             if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/*  536 */               MercuriusUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  537 */               System.arraycopy(this.data, 0, MercuriusUdpHandler.this.bid, 0, 2);
/*  538 */               MercuriusUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(MercuriusUdpHandler.this.bid));
/*  539 */               if (MercuriusUdpHandler.this.expBlockIndex == MercuriusUdpHandler.this.rcvBlockIndex) {
/*  540 */                 MercuriusUdpHandler.this.packet = new byte[MercuriusUdpHandler.this.blockLength + 3];
/*  541 */                 System.arraycopy(this.data, 0, MercuriusUdpHandler.this.packet, 0, 3);
/*  542 */                 System.arraycopy(this.data, 3, MercuriusUdpHandler.this.packet, 3, MercuriusUdpHandler.this.blockLength);
/*  543 */                 int i = CRC16.calculate(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.packet.length, 65535);
/*  544 */                 tmp = new byte[2];
/*  545 */                 System.arraycopy(this.data, MercuriusUdpHandler.this.blockLength + 3, tmp, 0, 2);
/*  546 */                 int j = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp));
/*  547 */                 if (i == j) {
/*  548 */                   if (MercuriusUdpHandler.this.expBlockIndex == 0) {
/*  549 */                     MercuriusUdpHandler.this.fileContent = new byte[MercuriusUdpHandler.this.currentAJS.getLength()];
/*  550 */                     System.arraycopy(this.data, 7, MercuriusUdpHandler.this.fileContent, MercuriusUdpHandler.this.fileContentIndex, MercuriusUdpHandler.this.blockLength - 4);
/*  551 */                     MercuriusUdpHandler.this.fileContentIndex += MercuriusUdpHandler.this.blockLength - 4;
/*      */                   } else {
/*  553 */                     System.arraycopy(this.data, 3, MercuriusUdpHandler.this.fileContent, MercuriusUdpHandler.this.fileContentIndex, MercuriusUdpHandler.this.blockLength);
/*  554 */                     MercuriusUdpHandler.this.fileContentIndex += MercuriusUdpHandler.this.blockLength;
/*      */                   } 
/*  556 */                   if (MercuriusUdpHandler.this.expBlockIndex == 1 && (this.data[2] & 0xFF) < 250) {
/*  557 */                     String str = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  558 */                     MercuriusUdpHandler.this.encType = Short.parseShort(str.substring(0, 2), 2);
/*  559 */                     str = str.substring(2);
/*  560 */                     str = str.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  561 */                     short s = Short.parseShort(str, 2);
/*  562 */                     if (s == Util.EnumProductIDs.MERCURIUS.getProductId() && 
/*  563 */                       !processM2SPacket(true)) {
/*      */                       break;
/*      */                     }
/*      */                   } 
/*      */ 
/*      */                   
/*  569 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  570 */                   MercuriusUdpHandler.this.retry = 0;
/*  571 */                   MercuriusUdpHandler.this.expBlockIndex++;
/*  572 */                   if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  573 */                     updateLastCommunicationModuleData();
/*  574 */                     MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/*  578 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  580 */                 if (MercuriusUdpHandler.this.fileContentIndex <= MercuriusUdpHandler.this.currentAJS.getLength() && MercuriusUdpHandler.this.currentAJS.getLength() > 0) {
/*  581 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  584 */                 String str = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  585 */                 MercuriusUdpHandler.this.encType = Short.parseShort(str.substring(0, 2), 2);
/*  586 */                 str = str.substring(2);
/*  587 */                 str = str.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  588 */                 short s = Short.parseShort(str, 2);
/*  589 */                 if (s == Util.EnumProductIDs.MERCURIUS.getProductId()) {
/*  590 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*      */                 } else {
/*  594 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/*  599 */               MercuriusUdpHandler.this.retry = 0;
/*  600 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  601 */               MercuriusUdpHandler.this.expBlockIndex = 0;
/*  602 */               requestNextVoiceMessageFromQueue();
/*      */             } 
/*  604 */             if (MercuriusUdpHandler.this.fileContentIndex >= MercuriusUdpHandler.this.currentAJS.getLength() && MercuriusUdpHandler.this.currentAJS.getLength() > 0) {
/*      */               
/*  606 */               byte[] ajsheader = new byte[22];
/*  607 */               byte[] tmp2 = Functions.get2ByteArrayFromInt((MercuriusUdpHandler.this.currentAJS.getDir() == 1) ? MercuriusUdpHandler.this.audioCounter++ : MercuriusUdpHandler.this.jsCounter++);
/*  608 */               ajsheader[0] = tmp2[1];
/*  609 */               ajsheader[1] = tmp2[0];
/*  610 */               byte[] tmp4 = Functions.get4ByteArrayFromInt(MercuriusUdpHandler.this.currentAJS.getCrc32());
/*  611 */               ajsheader[2] = tmp4[3];
/*  612 */               ajsheader[3] = tmp4[2];
/*  613 */               ajsheader[4] = tmp4[1];
/*  614 */               ajsheader[5] = tmp4[0];
/*      */               
/*  616 */               tmp4 = Functions.get4ByteArrayFromInt(MercuriusUdpHandler.this.currentAJS.getLength());
/*  617 */               ajsheader[6] = tmp4[3];
/*  618 */               ajsheader[7] = tmp4[2];
/*  619 */               ajsheader[8] = tmp4[1];
/*  620 */               ajsheader[9] = tmp4[0];
/*      */               
/*  622 */               System.arraycopy(MercuriusUdpHandler.this.currentAJS.getName().getBytes("ISO-8859-1"), 0, ajsheader, 10, MercuriusUdpHandler.this.currentAJS.getName().length());
/*  623 */               if (MercuriusUdpHandler.this.file == null) {
/*  624 */                 MercuriusUdpHandler.this.file = new File(MercuriusUdpHandler.this.sn + "_READ_FULL_CFG" + MercuriusUdpHandler.this.sp24DH.getCommandData());
/*      */               }
/*  626 */               if (MercuriusUdpHandler.this.file != null && MercuriusUdpHandler.this.file.exists()) {
/*  627 */                 long len = MercuriusUdpHandler.this.file.length();
/*  628 */                 RandomAccessFile rac = null;
/*      */                 try {
/*  630 */                   rac = new RandomAccessFile(MercuriusUdpHandler.this.file, "rw");
/*  631 */                   rac.seek(len);
/*  632 */                   rac.write(ajsheader);
/*  633 */                   len += ajsheader.length;
/*  634 */                   rac.seek(len);
/*  635 */                   rac.write(MercuriusUdpHandler.this.fileContent);
/*      */                 } finally {
/*  637 */                   if (rac != null) {
/*      */                     try {
/*  639 */                       rac.close();
/*  640 */                     } catch (IOException iOException) {}
/*      */                   }
/*      */                 } 
/*      */               } 
/*      */ 
/*      */               
/*  646 */               MercuriusDBManager.saveVoiceMessage(MercuriusUdpHandler.this.fileContent, MercuriusUdpHandler.this.currentAJS.getLength(), MercuriusUdpHandler.this.currentAJS.getName(), MercuriusUdpHandler.this.currentAJS.getCrc32(), MercuriusUdpHandler.this.currentAJS.getDir());
/*  647 */               requestNextVoiceMessageFromQueue();
/*      */             } 
/*      */             break;
/*      */           case ALL_CFG_CRC32_REQUESTED:
/*  651 */             if (this.data[0] == 6) {
/*  652 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.ALL_CFG_CRC32_DATA_RECIEVING;
/*  653 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case ALL_CFG_CRC32_DATA_RECIEVING:
/*  659 */             crcCalc = CRC16.calculate(this.data, 0, 52, 65535);
/*  660 */             tmp = new byte[2];
/*  661 */             tmp[0] = this.data[53];
/*  662 */             tmp[1] = this.data[52];
/*  663 */             crcRecv = Functions.getIntFrom2ByteArray(tmp);
/*  664 */             if (crcCalc == crcRecv) {
/*  665 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  666 */               List<Integer> devCRC32List = ConfigFileParser.buildCRC32FromReceivedBuffer(this.data);
/*  667 */               MercuriusUdpHandler.this.fileIDData = ConfigFileParser.prepareFileIDSByCRC32Mismatch(MercuriusUdpHandler.this.mCFG, devCRC32List);
/*  668 */               if (MercuriusUdpHandler.this.fileIDData == null || MercuriusUdpHandler.this.fileIDData.length == 0) {
/*  669 */                 if (MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32769 || MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32770) {
/*  670 */                   Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/*  671 */                   MercuriusAVLHandlerHelper.endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/*  672 */                   sendNextCommandFromQueue();
/*      */                 }  break;
/*      */               } 
/*  675 */               MercuriusUdpHandler.this.sp24DH.setCommandFileData(ConfigFileParser.prepareRequiredFileDataForDeviceByCRCMismatch(MercuriusUdpHandler.this.mCFG, MercuriusUdpHandler.this.fileIDData));
/*  676 */               byte[] data = new byte[4];
/*  677 */               tmp = Functions.get2ByteArrayFromInt(MercuriusUdpHandler.this.sp24DH.getCommand_Type());
/*  678 */               tmp = Functions.swapLSB2MSB(tmp);
/*  679 */               System.arraycopy(tmp, 0, data, 0, 2);
/*  680 */               data[2] = 1;
/*  681 */               data[3] = (byte)Character.digit(MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */               try {
/*  683 */                 tmp = prepareCommandPacket(data);
/*  684 */                 MercuriusUdpHandler.this.sentCommand = MercuriusUdpHandler.this.sp24DH.getCommand_Type();
/*  685 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, tmp);
/*  686 */                 MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.FILE_SENDING_INITIATED;
/*  687 */                 registerResponseTimeOut();
/*  688 */               } catch (IOException|InterruptedException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|SQLException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/*  689 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/*  690 */                 ex.printStackTrace();
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case EXPECTED_REPLY:
/*  698 */             if (this.data[0] == 4) {
/*  699 */               String str = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  700 */               MercuriusUdpHandler.this.encType = Short.parseShort(str.substring(0, 2), 2);
/*  701 */               str = str.substring(2);
/*  702 */               str = str.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  703 */               short s = Short.parseShort(str, 2);
/*  704 */               if (s == Util.EnumProductIDs.MERCURIUS.getProductId() && MercuriusUdpHandler.this.encType == 1) {
/*  705 */                 if (!processM2SPacket(true));
/*      */                 
/*      */                 break;
/*      */               } 
/*  709 */               MercuriusUdpHandler.this.newCMDCheck = 0;
/*  710 */               if (!MercuriusUdpHandler.this.commandModeActivated) {
/*  711 */                 MercuriusUdpHandler.this.commandModeActivated = true;
/*      */               }
/*  713 */               if (MercuriusUdpHandler.this.initiatedGeofenceRequest) {
/*  714 */                 if (requestGeofenceData())
/*  715 */                   registerResponseTimeOut(); 
/*      */                 break;
/*      */               } 
/*  718 */               if (processCommandPacket()) {
/*  719 */                 registerResponseTimeOut();
/*      */               }
/*      */               break;
/*      */             } 
/*  723 */             if (this.data[0] == 6) {
/*  724 */               if (MercuriusUdpHandler.this.initiatedGeofenceRequest && MercuriusUdpHandler.this.sentCommand == 32773) {
/*  725 */                 byte[] fileIDData = new byte[2];
/*  726 */                 fileIDData[0] = 1;
/*  727 */                 fileIDData[1] = 12;
/*  728 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, fileIDData);
/*  729 */                 MercuriusUdpHandler.this.expBlockIndex = 0;
/*  730 */                 MercuriusUdpHandler.this.flen = 0;
/*  731 */                 MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.GEOFENCE_FILE_RECEIVING;
/*  732 */                 registerResponseTimeOut();
/*      */               }  break;
/*  734 */             }  if (this.data[0] == 21) {
/*      */               break;
/*      */             }
/*  737 */             prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  738 */             MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  739 */             prodBin = prodBin.substring(2);
/*  740 */             prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  741 */             prodI = Short.parseShort(prodBin, 2);
/*  742 */             if (prodI != Util.EnumProductIDs.MERCURIUS.getProductId() || MercuriusUdpHandler.this.encType != 1 || 
/*  743 */               !processM2SPacket(true));
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case CFG_FILE_RECEIVE_INITIATED:
/*  750 */             if (this.data[0] == 6) {
/*  751 */               byte[] fileIDData = new byte[14];
/*  752 */               fileIDData[0] = 13;
/*  753 */               for (int kk = 1; kk <= 13; kk++) {
/*  754 */                 fileIDData[kk] = (byte)kk;
/*      */               }
/*  756 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, fileIDData);
/*  757 */               MercuriusUdpHandler.this.expBlockIndex = 0;
/*  758 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  759 */               MercuriusUdpHandler.this.retry = 0;
/*  760 */               MercuriusUdpHandler.this.flen = 0;
/*  761 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.CFG_FILE_RECEIVING;
/*  762 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */           
/*      */           case CFG_FILE_RECEIVING:
/*  767 */             if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/*  768 */               MercuriusUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  769 */               System.arraycopy(this.data, 0, MercuriusUdpHandler.this.bid, 0, 2);
/*  770 */               MercuriusUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(MercuriusUdpHandler.this.bid));
/*  771 */               if (MercuriusUdpHandler.this.expBlockIndex == MercuriusUdpHandler.this.rcvBlockIndex) {
/*  772 */                 MercuriusUdpHandler.this.packet = new byte[MercuriusUdpHandler.this.blockLength + 3];
/*  773 */                 System.arraycopy(this.data, 0, MercuriusUdpHandler.this.packet, 0, 3);
/*  774 */                 System.arraycopy(this.data, 3, MercuriusUdpHandler.this.packet, 3, MercuriusUdpHandler.this.blockLength);
/*  775 */                 crcCalc = CRC16.calculate(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.packet.length, 65535);
/*  776 */                 tmp = new byte[2];
/*  777 */                 System.arraycopy(this.data, MercuriusUdpHandler.this.blockLength + 3, tmp, 0, 2);
/*  778 */                 crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp));
/*  779 */                 if (crcCalc == crcRecv) {
/*  780 */                   if (MercuriusUdpHandler.this.expBlockIndex == 0) {
/*  781 */                     System.arraycopy(this.data, 3, MercuriusUdpHandler.this.first16, 0, 16);
/*  782 */                     MercuriusUdpHandler.this.first16 = Rijndael.decryptBytes(MercuriusUdpHandler.this.first16, Rijndael.aes_256, false);
/*  783 */                     System.arraycopy(MercuriusUdpHandler.this.first16, 4, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  784 */                     MercuriusUdpHandler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*  785 */                     MercuriusUdpHandler.this.lPad = (MercuriusUdpHandler.this.flen + 12) % 16;
/*  786 */                     if (MercuriusUdpHandler.this.lPad > 0) {
/*  787 */                       MercuriusUdpHandler.this.lPad = 16 - MercuriusUdpHandler.this.lPad;
/*      */                     }
/*  789 */                     System.arraycopy(MercuriusUdpHandler.this.first16, 8, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  790 */                     MercuriusUdpHandler.this.recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*  791 */                     MercuriusUdpHandler.this.fileContent = new byte[MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12];
/*  792 */                     MercuriusUdpHandler.this.fileContentIndex = 0;
/*      */                   } 
/*  794 */                   if (MercuriusUdpHandler.this.expBlockIndex == 1 && (this.data[2] & 0xFF) < 250) {
/*  795 */                     prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  796 */                     MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  797 */                     prodBin = prodBin.substring(2);
/*  798 */                     prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  799 */                     prodI = Short.parseShort(prodBin, 2);
/*  800 */                     if (prodI == Util.EnumProductIDs.MERCURIUS.getProductId() && MercuriusUdpHandler.this.encType == 1 && 
/*  801 */                       !processM2SPacket(true)) {
/*      */                       break;
/*      */                     }
/*      */                   } 
/*      */ 
/*      */                   
/*  807 */                   System.arraycopy(this.data, 3, MercuriusUdpHandler.this.fileContent, MercuriusUdpHandler.this.fileContentIndex, MercuriusUdpHandler.this.blockLength);
/*  808 */                   MercuriusUdpHandler.this.fileContentIndex += MercuriusUdpHandler.this.blockLength;
/*  809 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  810 */                   MercuriusUdpHandler.this.retry = 0;
/*  811 */                   MercuriusUdpHandler.this.expBlockIndex++;
/*  812 */                   if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  813 */                     updateLastCommunicationModuleData();
/*  814 */                     this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationTimeout;
/*  815 */                     MercuriusUdpHandler.this.iTimeout = this.idleTimeout;
/*  816 */                     MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/*  820 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*  822 */                 if (MercuriusUdpHandler.this.fileContentIndex < MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12 && MercuriusUdpHandler.this.flen > 0) {
/*  823 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/*  826 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  827 */                 MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  828 */                 prodBin = prodBin.substring(2);
/*  829 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  830 */                 prodI = Short.parseShort(prodBin, 2);
/*  831 */                 if (prodI == Util.EnumProductIDs.MERCURIUS.getProductId() && MercuriusUdpHandler.this.encType == 1) {
/*  832 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*      */                 }
/*  836 */                 else if (MercuriusUdpHandler.this.rcvBlockIndex < MercuriusUdpHandler.this.expBlockIndex) {
/*  837 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*      */                 } else {
/*  839 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 }
/*      */               
/*      */               } 
/*      */             } else {
/*      */               
/*  845 */               MercuriusUdpHandler.this.retry = 0;
/*  846 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  847 */               MercuriusUdpHandler.this.expBlockIndex = 0;
/*      */               
/*  849 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.M2S_PACKET_PARSING;
/*      */             } 
/*      */             
/*  852 */             if (MercuriusUdpHandler.this.fileContentIndex >= MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12 && MercuriusUdpHandler.this.flen > 0) {
/*  853 */               byte[] decData = new byte[MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12];
/*      */               
/*  855 */               byte[] encBlock = new byte[16];
/*  856 */               if (MercuriusUdpHandler.this.fileContent.length >= 16 && MercuriusUdpHandler.this.fileContent.length % 16 == 0) {
/*  857 */                 for (int i = 0; i < MercuriusUdpHandler.this.fileContent.length; ) {
/*  858 */                   System.arraycopy(MercuriusUdpHandler.this.fileContent, i, encBlock, 0, 16);
/*  859 */                   byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/*  860 */                   System.arraycopy(arrayOfByte, 0, decData, i, 16);
/*  861 */                   i += 16;
/*      */                 } 
/*      */               }
/*  864 */               byte[] decBlock = new byte[MercuriusUdpHandler.this.flen];
/*  865 */               System.arraycopy(decData, 12, decBlock, 0, MercuriusUdpHandler.this.flen);
/*  866 */               int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/*  867 */               decBlock = null;
/*  868 */               if (MercuriusUdpHandler.this.recvCfgCRC32 == calcCfgCrc32) {
/*  869 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  870 */                 if (MercuriusUdpHandler.this.sp24DH != null) {
/*  871 */                   if (MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0) == '2') {
/*  872 */                     List<AudioNJSFileInfo> reqiredJSFileList = ConfigFileParser.getAudioScriptLookupDataFromCFG(decData, MercuriusUdpHandler.this.flen, null);
/*      */                     
/*  874 */                     if (reqiredJSFileList != null && reqiredJSFileList.size() > 0) {
/*  875 */                       MercuriusUdpHandler.this.file = Functions.writeByteArrayToFile(MercuriusUdpHandler.this.sn + "_READ_FULL_CFG" + MercuriusUdpHandler.this.sp24DH.getCommandData(), MercuriusUdpHandler.this.fileContent);
/*  876 */                       MercuriusUdpHandler.this.requriedVMList = MercuriusDBManager.getMissingVoiceMessagesInfo(reqiredJSFileList);
/*  877 */                       List<AudioNJSFileInfo> repoFilesList = new ArrayList<>();
/*  878 */                       if (MercuriusUdpHandler.this.requriedVMList == null) {
/*  879 */                         repoFilesList.addAll(reqiredJSFileList);
/*      */                       } else {
/*  881 */                         for (AudioNJSFileInfo ajsF : reqiredJSFileList) {
/*  882 */                           if (!MercuriusUdpHandler.this.requriedVMList.contains(ajsF)) {
/*  883 */                             repoFilesList.add(ajsF);
/*      */                           }
/*      */                         } 
/*      */                       } 
/*  887 */                       long cfgLen = MercuriusUdpHandler.this.file.length();
/*      */                       
/*  889 */                       RandomAccessFile rac = new RandomAccessFile(MercuriusUdpHandler.this.file, "rw");
/*      */                       try {
/*  891 */                         for (AudioNJSFileInfo ajsF : repoFilesList) {
/*  892 */                           byte[] ajsContent = MercuriusDBManager.getVoiceMessagesByName(ajsF.getLength(), ajsF.getName(), ajsF.getCrc32(), ajsF.getDir());
/*  893 */                           if (ajsContent != null) {
/*  894 */                             byte[] ajsheader = new byte[22];
/*  895 */                             byte[] tmp2 = Functions.get2ByteArrayFromInt((ajsF.getDir() == 1) ? MercuriusUdpHandler.this.audioCounter++ : MercuriusUdpHandler.this.jsCounter++);
/*  896 */                             ajsheader[0] = tmp2[1];
/*  897 */                             ajsheader[1] = tmp2[0];
/*  898 */                             MercuriusUdpHandler.this.tmp4 = Functions.get4ByteArrayFromInt(ajsF.getCrc32());
/*  899 */                             ajsheader[2] = MercuriusUdpHandler.this.tmp4[3];
/*  900 */                             ajsheader[3] = MercuriusUdpHandler.this.tmp4[2];
/*  901 */                             ajsheader[4] = MercuriusUdpHandler.this.tmp4[1];
/*  902 */                             ajsheader[5] = MercuriusUdpHandler.this.tmp4[0];
/*      */                             
/*  904 */                             MercuriusUdpHandler.this.tmp4 = Functions.get4ByteArrayFromInt(ajsF.getLength());
/*  905 */                             ajsheader[6] = MercuriusUdpHandler.this.tmp4[3];
/*  906 */                             ajsheader[7] = MercuriusUdpHandler.this.tmp4[2];
/*  907 */                             ajsheader[8] = MercuriusUdpHandler.this.tmp4[1];
/*  908 */                             ajsheader[9] = MercuriusUdpHandler.this.tmp4[0];
/*      */                             
/*  910 */                             System.arraycopy(ajsF.getName().getBytes("ISO-8859-1"), 0, ajsheader, 10, ajsF.getName().length());
/*      */                             
/*  912 */                             rac.seek(cfgLen);
/*  913 */                             rac.write(ajsheader);
/*  914 */                             cfgLen += ajsheader.length;
/*  915 */                             rac.seek(cfgLen);
/*  916 */                             rac.write(ajsContent);
/*  917 */                             cfgLen += ajsContent.length;
/*      */                           } 
/*      */                         } 
/*      */                       } finally {
/*  921 */                         rac.close();
/*      */                       } 
/*      */                       
/*  924 */                       if (MercuriusUdpHandler.this.requriedVMList != null && !MercuriusUdpHandler.this.requriedVMList.isEmpty()) {
/*  925 */                         requestNextVoiceMessageFromQueue();
/*      */                       } else {
/*  927 */                         MercuriusDBManager.updateCommandData(MercuriusUdpHandler.this.sp24DH.getId_Command(), new FileInputStream(MercuriusUdpHandler.this.file));
/*  928 */                         if (MercuriusUdpHandler.this.file != null) {
/*  929 */                           MercuriusUdpHandler.this.file.delete();
/*      */                         }
/*      */                       } 
/*      */                     } 
/*      */                   } else {
/*  934 */                     MercuriusDBManager.updateCommandData(MercuriusUdpHandler.this.sp24DH.getId_Command(), new ByteArrayInputStream(MercuriusUdpHandler.this.fileContent));
/*      */                   } 
/*  936 */                   Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("The_file_[") + MercuriusAVLHandlerHelper.getFileNameByCommandData(MercuriusUdpHandler.this.sp24DH.getCommandData()) + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/*  937 */                   endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/*      */                 }
/*      */               
/*  940 */               } else if (MercuriusUdpHandler.this.sp24DH != null) {
/*  941 */                 MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + MercuriusAVLHandlerHelper.getFileNameByCommandData(MercuriusUdpHandler.this.sp24DH.getCommandData()) + LocaleMessage.getLocaleMessage("invalid"), MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */               } 
/*      */               
/*  944 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/*  945 */               MercuriusUdpHandler.this.expBlockIndex = 0;
/*  946 */               if (MercuriusUdpHandler.this.sp24DH != null) {
/*  947 */                 sendNextCommandFromQueue();
/*      */               }
/*      */             } 
/*      */             break;
/*      */           case GEOFENCE_FILE_RECEIVING:
/*  952 */             if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/*  953 */               MercuriusUdpHandler.this.blockLength = this.data[2] & 0xFF;
/*  954 */               System.arraycopy(this.data, 0, MercuriusUdpHandler.this.bid, 0, 2);
/*  955 */               MercuriusUdpHandler.this.rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(MercuriusUdpHandler.this.bid));
/*  956 */               if (MercuriusUdpHandler.this.expBlockIndex == MercuriusUdpHandler.this.rcvBlockIndex) {
/*  957 */                 MercuriusUdpHandler.this.packet = new byte[MercuriusUdpHandler.this.blockLength + 3];
/*  958 */                 System.arraycopy(this.data, 0, MercuriusUdpHandler.this.packet, 0, 3);
/*  959 */                 System.arraycopy(this.data, 3, MercuriusUdpHandler.this.packet, 3, MercuriusUdpHandler.this.blockLength);
/*  960 */                 crcCalc = CRC16.calculate(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.packet.length, 65535);
/*  961 */                 tmp = new byte[2];
/*  962 */                 System.arraycopy(this.data, MercuriusUdpHandler.this.blockLength + 3, tmp, 0, 2);
/*  963 */                 crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp));
/*  964 */                 if (crcCalc == crcRecv) {
/*  965 */                   if (MercuriusUdpHandler.this.expBlockIndex == 0) {
/*  966 */                     System.arraycopy(this.data, 3, MercuriusUdpHandler.this.first16, 0, 16);
/*  967 */                     MercuriusUdpHandler.this.first16 = Rijndael.decryptBytes(MercuriusUdpHandler.this.first16, Rijndael.aes_256, false);
/*  968 */                     System.arraycopy(MercuriusUdpHandler.this.first16, 4, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  969 */                     MercuriusUdpHandler.this.flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*  970 */                     MercuriusUdpHandler.this.lPad = (MercuriusUdpHandler.this.flen + 12) % 16;
/*  971 */                     if (MercuriusUdpHandler.this.lPad > 0) {
/*  972 */                       MercuriusUdpHandler.this.lPad = 16 - MercuriusUdpHandler.this.lPad;
/*      */                     }
/*  974 */                     System.arraycopy(MercuriusUdpHandler.this.first16, 8, MercuriusUdpHandler.this.tmp4, 0, 4);
/*  975 */                     MercuriusUdpHandler.this.recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(MercuriusUdpHandler.this.tmp4));
/*  976 */                     MercuriusUdpHandler.this.fileContent = new byte[MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12];
/*  977 */                     MercuriusUdpHandler.this.fileContentIndex = 0;
/*      */                   } 
/*  979 */                   if (MercuriusUdpHandler.this.expBlockIndex == 1 && (this.data[2] & 0xFF) < 250) {
/*  980 */                     prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/*  981 */                     MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  982 */                     prodBin = prodBin.substring(2);
/*  983 */                     prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/*  984 */                     prodI = Short.parseShort(prodBin, 2);
/*  985 */                     if (prodI == Util.EnumProductIDs.MERCURIUS.getProductId() && MercuriusUdpHandler.this.encType == 1 && 
/*  986 */                       !processM2SPacket(true)) {
/*      */                       break;
/*      */                     }
/*      */                   } 
/*      */ 
/*      */                   
/*  992 */                   System.arraycopy(this.data, 3, MercuriusUdpHandler.this.fileContent, MercuriusUdpHandler.this.fileContentIndex, MercuriusUdpHandler.this.blockLength);
/*  993 */                   MercuriusUdpHandler.this.fileContentIndex += MercuriusUdpHandler.this.blockLength;
/*  994 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/*  995 */                   MercuriusUdpHandler.this.retry = 0;
/*  996 */                   MercuriusUdpHandler.this.expBlockIndex++;
/*  997 */                   if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/*  998 */                     updateLastCommunicationModuleData();
/*  999 */                     MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } else {
/*      */                   
/* 1003 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/* 1005 */                 if (MercuriusUdpHandler.this.fileContentIndex < MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12 && MercuriusUdpHandler.this.flen > 0) {
/* 1006 */                   registerResponseTimeOut();
/*      */                 }
/*      */               } else {
/* 1009 */                 prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/* 1010 */                 MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1011 */                 prodBin = prodBin.substring(2);
/* 1012 */                 prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/* 1013 */                 prodI = Short.parseShort(prodBin, 2);
/* 1014 */                 if (prodI == Util.EnumProductIDs.MERCURIUS.getProductId() && MercuriusUdpHandler.this.encType == 1) {
/* 1015 */                   if (!processM2SPacket(true)) {
/*      */                     break;
/*      */                   }
/*      */                 } else {
/* 1019 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */                 } 
/*      */               } 
/*      */             } else {
/*      */               
/* 1024 */               MercuriusUdpHandler.this.retry = 0;
/* 1025 */               MercuriusUdpHandler.this.fileContentIndex = 0;
/* 1026 */               MercuriusUdpHandler.this.expBlockIndex = 0;
/* 1027 */               MercuriusUdpHandler.this.initiatedGeofenceRequest = false;
/*      */               
/* 1029 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.M2S_PACKET_PARSING;
/*      */             } 
/*      */             
/* 1032 */             if (MercuriusUdpHandler.this.fileContentIndex >= MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12 && MercuriusUdpHandler.this.flen > 0) {
/* 1033 */               byte[] decData = new byte[MercuriusUdpHandler.this.flen + MercuriusUdpHandler.this.lPad + 12];
/*      */               
/* 1035 */               byte[] encBlock = new byte[16];
/* 1036 */               if (MercuriusUdpHandler.this.fileContent.length >= 16 && MercuriusUdpHandler.this.fileContent.length % 16 == 0) {
/* 1037 */                 for (int i = 0; i < MercuriusUdpHandler.this.fileContent.length; ) {
/* 1038 */                   System.arraycopy(MercuriusUdpHandler.this.fileContent, i, encBlock, 0, 16);
/* 1039 */                   byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1040 */                   System.arraycopy(arrayOfByte, 0, decData, i, 16);
/* 1041 */                   i += 16;
/*      */                 } 
/*      */               }
/* 1044 */               byte[] decBlock = new byte[MercuriusUdpHandler.this.flen];
/* 1045 */               System.arraycopy(decData, MercuriusAVLHandlerHelper.AVL_FILE_HEADER_SIZE, decBlock, 0, MercuriusUdpHandler.this.flen);
/* 1046 */               int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/* 1047 */               if (MercuriusUdpHandler.this.recvCfgCRC32 == calcCfgCrc32) {
/* 1048 */                 MercuriusAVLHandlerHelper.parseGeofenceData(decBlock, MercuriusUdpHandler.this.flen, ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule, ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idClient, MercuriusUdpHandler.this.recvCfgCRC32);
/* 1049 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/* 1050 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Geofence_data_sync_done_successfully"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/*      */               } else {
/* 1052 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("CRC_32_of_the_received_configuration_file_is_invalid"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/*      */               } 
/* 1054 */               MercuriusUdpHandler.this.initiatedGeofenceRequest = false;
/* 1055 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.M2S_PACKET_PARSING;
/*      */             } 
/*      */             break;
/*      */           case FILE_SENDING_INITIATED:
/* 1059 */             if (this.data[0] == 6) {
/* 1060 */               if (MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32769 || MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32770) {
/* 1061 */                 if (MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1' || MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0) == '2') {
/* 1062 */                   sendFile2Module(true);
/* 1063 */                   MercuriusUdpHandler.this.blockIndex++;
/* 1064 */                   MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.FILE_SENDING;
/* 1065 */                   registerResponseTimeOut(); break;
/*      */                 } 
/* 1067 */                 if (MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0) == '3') {
/* 1068 */                   byte[] fwLen = Functions.swapLSB2MSB4ByteArray(Functions.get4ByteArrayFromInt((MercuriusUdpHandler.this.sp24DH.getCommandFileData()).length));
/* 1069 */                   int crcFw = CRC16.calculate(fwLen, 0, 4, 65535);
/* 1070 */                   byte[] fwLenData = new byte[6];
/* 1071 */                   System.arraycopy(fwLen, 0, fwLenData, 0, 4);
/* 1072 */                   fwLen = Functions.get2ByteArrayFromInt(crcFw);
/* 1073 */                   fwLenData[4] = fwLen[1];
/* 1074 */                   fwLenData[5] = fwLen[0];
/* 1075 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, fwLenData);
/* 1076 */                   MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.FIRMWARE_CRC_RESPONSE;
/* 1077 */                   registerResponseTimeOut();
/*      */                 } 
/*      */               }  break;
/*      */             } 
/* 1081 */             if (this.data[0] == 21) {
/* 1082 */               MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/* 1083 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */           
/*      */           case FILE_SENDING:
/* 1088 */             if (this.data[0] == 6 && MercuriusUdpHandler.this.fc.position() < MercuriusUdpHandler.this.flen) {
/* 1089 */               if (MercuriusUdpHandler.this.fc.position() < MercuriusUdpHandler.this.flen) {
/* 1090 */                 MercuriusUdpHandler.this.retry = 0;
/* 1091 */                 this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationTimeout;
/* 1092 */                 MercuriusUdpHandler.this.iTimeout = this.idleTimeout;
/* 1093 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + MercuriusAVLHandlerHelper.getFileNameByCommandData(MercuriusUdpHandler.this.sp24DH.getCommandData()) + " (" + MercuriusUdpHandler.this.fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/* 1094 */                 MercuriusUdpHandler.this.responseTimeout = 120000;
/* 1095 */                 sendFile2Module(false);
/* 1096 */                 MercuriusUdpHandler.this.blockIndex++;
/* 1097 */                 registerResponseTimeOut(); break;
/*      */               } 
/* 1099 */               if (this.data[1] == 6) {
/* 1100 */                 MercuriusUdpHandler.this.fc.close();
/* 1101 */                 MercuriusUdpHandler.this.blockIndex = 0;
/* 1102 */                 MercuriusUdpHandler.this.flen = 0;
/* 1103 */                 endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/* 1104 */                 if (MercuriusUdpHandler.this.file != null && MercuriusUdpHandler.this.file.exists()) {
/* 1105 */                   MercuriusUdpHandler.this.file.delete();
/*      */                 }
/*      */                 
/* 1108 */                 sendNextCommandFromQueue(); break;
/*      */               } 
/* 1110 */               registerResponseTimeOut();
/*      */               break;
/*      */             } 
/* 1113 */             if (this.data[0] == 6) {
/* 1114 */               MercuriusUdpHandler.this.fc.close();
/* 1115 */               MercuriusUdpHandler.this.blockIndex = 0;
/* 1116 */               MercuriusUdpHandler.this.flen = 0;
/* 1117 */               endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/* 1118 */               if (MercuriusUdpHandler.this.file != null && MercuriusUdpHandler.this.file.exists()) {
/* 1119 */                 MercuriusUdpHandler.this.file.delete();
/*      */               }
/*      */               
/* 1122 */               sendNextCommandFromQueue(); break;
/* 1123 */             }  if (this.data[0] == 21) {
/* 1124 */               if (MercuriusUdpHandler.this.fc.position() < MercuriusUdpHandler.this.flen) {
/* 1125 */                 if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/* 1126 */                   if (MercuriusUdpHandler.this.lCommIface == 1) {
/* 1127 */                     if (MercuriusUdpHandler.this.retry == 0) {
/* 1128 */                       MercuriusUdpHandler.this.responseTimeout = 120000;
/* 1129 */                     } else if (MercuriusUdpHandler.this.retry == 1) {
/* 1130 */                       MercuriusUdpHandler.this.responseTimeout = 210000;
/* 1131 */                     } else if (MercuriusUdpHandler.this.retry == 2) {
/* 1132 */                       MercuriusUdpHandler.this.responseTimeout = 300000;
/*      */                     } 
/*      */                   } else {
/* 1135 */                     MercuriusUdpHandler.this.responseTimeout = 120000;
/*      */                   } 
/* 1137 */                   MercuriusUdpHandler.this.blockIndex--;
/* 1138 */                   MercuriusUdpHandler.this.fc.position(MercuriusUdpHandler.this.fc.position() - MercuriusUdpHandler.this.blockLength);
/* 1139 */                   sendFile2Module(false);
/* 1140 */                   MercuriusUdpHandler.this.blockIndex++;
/* 1141 */                   registerResponseTimeOut(); break;
/*      */                 } 
/* 1143 */                 MercuriusUdpHandler.this.retry = 0;
/*      */                 break;
/*      */               } 
/* 1146 */               MercuriusUdpHandler.this.fc.close();
/* 1147 */               MercuriusUdpHandler.this.blockIndex = 0;
/* 1148 */               MercuriusUdpHandler.this.flen = 0;
/* 1149 */               if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1150 */                 updateLastCommunicationModuleData();
/* 1151 */                 MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 1153 */               if (MercuriusUdpHandler.this.file != null && MercuriusUdpHandler.this.file.exists()) {
/* 1154 */                 MercuriusUdpHandler.this.file.delete();
/*      */               }
/* 1156 */               sendNextCommandFromQueue();
/*      */             } 
/*      */             break;
/*      */           
/*      */           case NEW_COMMAND_RESPONSE:
/* 1161 */             if (this.data[0] == 4) {
/* 1162 */               prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/* 1163 */               MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1164 */               prodBin = prodBin.substring(2);
/* 1165 */               prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/* 1166 */               prodI = Short.parseShort(prodBin, 2);
/* 1167 */               if (prodI == Util.EnumProductIDs.MERCURIUS.getProductId() && MercuriusUdpHandler.this.encType == 1) {
/* 1168 */                 if (!processM2SPacket(true));
/*      */                 
/*      */                 break;
/*      */               } 
/* 1172 */               MercuriusUdpHandler.this.newCMDCheck = 0;
/* 1173 */               if (!MercuriusUdpHandler.this.commandModeActivated) {
/* 1174 */                 MercuriusUdpHandler.this.commandModeActivated = true;
/*      */               }
/* 1176 */               if (processCommandPacket()) {
/* 1177 */                 registerResponseTimeOut();
/*      */               }
/*      */             } 
/*      */             break;
/*      */           
/*      */           case COMMAND_PACKET_REPSONE:
/* 1183 */             if (this.data[0] == 6) {
/* 1184 */               endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/* 1185 */               sendNextCommandFromQueue(); break;
/* 1186 */             }  if (this.data[0] == 21) {
/* 1187 */               sendNextCommandFromQueue(); break;
/*      */             } 
/* 1189 */             if (this.data[0] == 4) {
/* 1190 */               prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.data[1] & 0xFF) }).replace(' ', '0');
/* 1191 */               MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1192 */               prodBin = prodBin.substring(2);
/* 1193 */               prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.data[0] & 0xFF) }).replace(' ', '0'));
/* 1194 */               prodI = Short.parseShort(prodBin, 2);
/* 1195 */               if (prodI != Util.EnumProductIDs.MERCURIUS.getProductId() || MercuriusUdpHandler.this.encType != 1 || 
/* 1196 */                 !processM2SPacket(true));
/*      */ 
/*      */               
/*      */               break;
/*      */             } 
/*      */ 
/*      */             
/* 1203 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + this.data[0] + ")", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */ 
/*      */           
/*      */           case M2S_PACKET_PARSING:
/* 1208 */             processM2SPacket(true);
/*      */             break;
/*      */           
/*      */           case FIRMWARE_CRC_RESPONSE:
/* 1212 */             if (this.data[0] == 21) {
/* 1213 */               sendNextCommandFromQueue(); break;
/* 1214 */             }  if (this.data[0] == 6) {
/* 1215 */               sendFile2Module(true);
/* 1216 */               MercuriusUdpHandler.this.blockIndex++;
/* 1217 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.FILE_SENDING;
/* 1218 */               registerResponseTimeOut();
/*      */             } 
/*      */             break;
/*      */         } 
/*      */ 
/*      */       
/* 1224 */       } catch (Exception ex) {
/* 1225 */         ex.printStackTrace();
/* 1226 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_on_the_UDP_Server_task"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, ex);
/*      */       }  }
/*      */ 
/*      */ 
/*      */     
/*      */     public void reinitialize() {
/* 1232 */       MercuriusUdpHandler.this.cmdsList = null;
/* 1233 */       MercuriusUdpHandler.this.cmdIndex = 0;
/*      */       
/* 1235 */       MercuriusUdpHandler.this.commandModeActivated = false;
/* 1236 */       MercuriusUdpHandler.this.sentCommand = 0;
/* 1237 */       MercuriusUdpHandler.this.blockIndex = 0;
/* 1238 */       MercuriusUdpHandler.this.blockLength = 0;
/* 1239 */       MercuriusUdpHandler.this.retry = 0;
/* 1240 */       MercuriusUdpHandler.this.flen = 0;
/* 1241 */       MercuriusUdpHandler.this.rcvBlockIndex = 0;
/* 1242 */       MercuriusUdpHandler.this.expBlockIndex = 0;
/* 1243 */       MercuriusUdpHandler.this.recvCfgCRC32 = 0;
/* 1244 */       MercuriusUdpHandler.this.fileContentIndex = 0;
/* 1245 */       MercuriusUdpHandler.this.block = null;
/* 1246 */       MercuriusUdpHandler.this.fileContent = null;
/* 1247 */       MercuriusUdpHandler.this.filePath = null;
/* 1248 */       MercuriusUdpHandler.this.fc = null;
/* 1249 */       MercuriusUdpHandler.this.blockBuf = null;
/* 1250 */       MercuriusUdpHandler.this.sp24DH = null;
/* 1251 */       MercuriusUdpHandler.this.file = null;
/* 1252 */       MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = 0L;
/* 1253 */       MercuriusUdpHandler.this.packetSentTime = 0L;
/* 1254 */       MercuriusUdpHandler.this.waitingForResponse = false;
/* 1255 */       MercuriusUdpHandler.this.newCMDCheck = 0;
/* 1256 */       MercuriusUdpHandler.this.m2sPacketReceived = 0;
/* 1257 */       MercuriusUdpHandler.this.initiatedGeofenceRequest = false;
/* 1258 */       MercuriusUdpHandler.this.fileSync_80_Sent = false;
/* 1259 */       MercuriusUdpHandler.this.requriedVMList = null;
/* 1260 */       MercuriusUdpHandler.this.currentAJS = null;
/* 1261 */       MercuriusUdpHandler.this.requiredVMIndex = 0;
/* 1262 */       MercuriusUdpHandler.this.audioCounter = 100;
/* 1263 */       MercuriusUdpHandler.this.jsCounter = 300;
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
/* 1282 */       MercuriusUdpHandler.this.responseTimeout = 120000;
/* 1283 */       byte[] prod = new byte[2];
/* 1284 */       prod[0] = this.data[1];
/* 1285 */       prod[1] = this.data[0];
/*      */       
/* 1287 */       if (this.data[0] == 6) {
/* 1288 */         return false;
/*      */       }
/* 1290 */       if (prod[0] == 43 && prod[1] == 43) {
/* 1291 */         MercuriusUdpHandler.this.dispose();
/* 1292 */         return false;
/*      */       } 
/*      */       
/* 1295 */       String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/* 1296 */       MercuriusUdpHandler.this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/* 1297 */       prodBin = prodBin.substring(2);
/* 1298 */       prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/* 1299 */       short prodI = Short.parseShort(prodBin, 2);
/*      */       
/* 1301 */       if (prodI != Util.EnumProductIDs.MERCURIUS.getProductId() && prod[0] != 43 && prod[1] != 43) {
/* 1302 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.sn + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_UDP"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1303 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/* 1304 */         return false;
/*      */       } 
/*      */       
/* 1307 */       if (MercuriusUdpHandler.this.encType != 1) {
/* 1308 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Module_Connected_with_Invalid_Encryption"), Enums.EnumMessagePriority.AVERAGE, MercuriusUdpHandler.this.remoteIP, null);
/* 1309 */         return false;
/*      */       } 
/* 1311 */       byte[] len = new byte[2];
/* 1312 */       len[0] = this.data[3];
/* 1313 */       len[1] = this.data[2];
/* 1314 */       int msgLen = Functions.getIntFrom2ByteArray(len);
/* 1315 */       int crcCalc = CRC16.calculate(this.data, 0, msgLen + 2, 65535);
/* 1316 */       len[1] = this.data[msgLen + 2];
/* 1317 */       len[0] = this.data[msgLen + 2 + 1];
/* 1318 */       int crcRecv = Functions.getIntFrom2ByteArray(len);
/* 1319 */       if (crcCalc == crcRecv) {
/* 1320 */         byte[] encData = new byte[msgLen - 2];
/* 1321 */         System.arraycopy(this.data, 4, encData, 0, msgLen - 2);
/* 1322 */         byte[] decData = new byte[msgLen - 2];
/* 1323 */         byte[] decBlock = null;
/* 1324 */         if (encData.length >= 17 && encData.length % 16 == 0) {
/* 1325 */           for (int i = 0; i < encData.length; ) {
/* 1326 */             byte[] block = new byte[16];
/* 1327 */             System.arraycopy(encData, i, block, 0, 16);
/* 1328 */             if (MercuriusUdpHandler.this.encType == 1) {
/* 1329 */               decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */             }
/* 1331 */             System.arraycopy(decBlock, 0, decData, i, 16);
/* 1332 */             i += 16;
/*      */           } 
/*      */         }
/*      */         
/* 1336 */         if (!parseM2SPacket(decData)) {
/* 1337 */           return false;
/*      */         }
/*      */         
/* 1340 */         if (MercuriusUdpHandler.this.newCMDCheck >= 3 && !flag) {
/* 1341 */           flag = true;
/*      */         }
/* 1343 */         if (MercuriusUdpHandler.this.commandModeActivated && 
/* 1344 */           ++MercuriusUdpHandler.this.m2sPacketReceived > 1) {
/* 1345 */           MercuriusUdpHandler.this.commandModeActivated = false;
/* 1346 */           MercuriusUdpHandler.this.m2sPacketReceived = 0;
/*      */         } 
/*      */         
/* 1349 */         if (TblMercuriusActiveConnections.getInstance().containsKey(MercuriusUdpHandler.this.sn) && flag && 
/* 1350 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).newCommand && !MercuriusUdpHandler.this.commandModeActivated) {
/*      */           
/* 1352 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/* 1353 */           Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/*      */           try {
/* 1355 */             UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, newCmd);
/* 1356 */             MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.EXPECTED_REPLY;
/* 1357 */             MercuriusUdpHandler.this.commandModeActivated = true;
/* 1358 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/* 1359 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/* 1360 */             registerResponseTimeOut();
/* 1361 */             MercuriusUdpHandler.this.newCMDCheck = 0;
/* 1362 */           } catch (IOException|InterruptedException|SQLException ex) {
/* 1363 */             return false;
/*      */           }
/*      */         
/*      */         } 
/*      */       } else {
/*      */         
/* 1369 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1370 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/*      */       } 
/* 1372 */       return true;
/*      */     }
/*      */ 
/*      */     
/*      */     private boolean processCommandPacket() {
/*      */       try {
/* 1378 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/* 1379 */         MercuriusUdpHandler.this.cmdsList = MercuriusDBManager.executeSP_024(((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule);
/* 1380 */         MercuriusUdpHandler.this.cmdIndex = 0;
/* 1381 */         if (MercuriusUdpHandler.this.cmdsList.size() > 0) {
/* 1382 */           MercuriusUdpHandler.this.sp24DH = MercuriusUdpHandler.this.cmdsList.get(MercuriusUdpHandler.this.cmdIndex);
/* 1383 */           return sendCommandPacket();
/*      */         } 
/* 1385 */       } catch (Exception ex) {
/* 1386 */         Logger.getLogger(MercuriusUdpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/* 1388 */       return false;
/*      */     }
/*      */ 
/*      */     
/*      */     private boolean initiateReadingAllFilesCRC32() {
/* 1393 */       byte[] data = new byte[3];
/* 1394 */       byte[] tmp = Functions.get2ByteArrayFromInt(32796);
/* 1395 */       tmp = Functions.swapLSB2MSB(tmp);
/* 1396 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 1397 */       data[2] = 0;
/*      */       try {
/* 1399 */         tmp = prepareCommandPacket(data);
/* 1400 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, tmp);
/* 1401 */         MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.ALL_CFG_CRC32_REQUESTED;
/* 1402 */         return true;
/* 1403 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1404 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1405 */         ex.printStackTrace();
/* 1406 */         return false;
/*      */       } 
/*      */     }
/*      */     private boolean sendCommandPacket() throws SQLException, InterruptedException, Exception {
/*      */       byte[] tmp, ascii;
/*      */       String[] cData, date, dData, hData;
/* 1412 */       if (MercuriusDBManager.isCommandCancelled(MercuriusUdpHandler.this.sp24DH.getId_Command())) {
/* 1413 */         return false;
/*      */       }
/* 1415 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Sending_command") + MercuriusUdpHandler.this.sp24DH.getCommand_Type() + ":" + MercuriusUdpHandler.this.sp24DH.getCommandData(), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/* 1416 */       MercuriusDBManager.updateCommandStatus(MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */ 
/*      */       
/* 1419 */       if ((MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32769 || MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32770) && 
/* 1420 */         MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') {
/* 1421 */         byte[] encBlock = new byte[16];
/*      */ 
/*      */         
/* 1424 */         byte[] fileContent = MercuriusUdpHandler.this.sp24DH.getCommandFileData();
/* 1425 */         System.arraycopy(fileContent, 0, encBlock, 0, 16);
/* 1426 */         byte[] decBlock = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1427 */         System.arraycopy(decBlock, 0, fileContent, 0, 16);
/*      */         
/* 1429 */         byte[] tmp4 = new byte[4];
/* 1430 */         tmp4[3] = fileContent[4];
/* 1431 */         tmp4[2] = fileContent[5];
/* 1432 */         tmp4[1] = fileContent[6];
/* 1433 */         tmp4[0] = fileContent[7];
/* 1434 */         int upFileLen = Functions.getIntFrom4ByteArray(tmp4);
/*      */         
/* 1436 */         int lPad = (upFileLen + 12) % 16;
/* 1437 */         if (lPad > 0) {
/* 1438 */           lPad = 16 - lPad;
/*      */         }
/*      */         
/* 1441 */         int tmpUPFileLen = upFileLen + lPad + 12;
/*      */         
/* 1443 */         byte[] tm = new byte[tmpUPFileLen];
/* 1444 */         System.arraycopy(fileContent, 0, tm, 0, tmpUPFileLen);
/*      */         
/* 1446 */         if (tm.length >= 16 && tmpUPFileLen % 16 == 0) {
/* 1447 */           for (int i = 16; i < tmpUPFileLen; ) {
/* 1448 */             System.arraycopy(tm, i, encBlock, 0, 16);
/* 1449 */             decBlock = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1450 */             System.arraycopy(decBlock, 0, tm, i, 16);
/* 1451 */             i += 16;
/*      */           } 
/*      */         }
/* 1454 */         MercuriusUdpHandler.this.mCFG = ConfigFileParser.arrangeCFGFile(tm, tmpUPFileLen - lPad, true);
/*      */         
/* 1456 */         if (fileContent.length > tmpUPFileLen) {
/* 1457 */           byte[] vmc = new byte[fileContent.length - tmpUPFileLen];
/* 1458 */           System.arraycopy(fileContent, tmpUPFileLen, vmc, 0, fileContent.length - tmpUPFileLen);
/* 1459 */           File ajsFile = Functions.writeByteArrayToFile("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule, vmc);
/* 1460 */           MercuriusUdpHandler.this.uploadLookupCRC32 = ((Integer)MercuriusUdpHandler.this.mCFG.getCrc32List().get(2)).intValue();
/* 1461 */           MercuriusUdpHandler.this.sp24DH.setCommandFileData(tm);
/* 1462 */           if (requestAudioJSInfo(32790)) {
/* 1463 */             MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_SINGLE_CRC32_REQUESTED;
/* 1464 */             return true;
/*      */           } 
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 1470 */         initiateReadingAllFilesCRC32();
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 1475 */       byte[] data = null;
/*      */ 
/*      */ 
/*      */       
/* 1479 */       switch (MercuriusUdpHandler.this.sp24DH.getCommand_Type()) {
/*      */         case 32774:
/* 1481 */           data = new byte[3];
/* 1482 */           tmp = Functions.get2ByteArrayFromInt(32774);
/* 1483 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1484 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1485 */           data[2] = 0;
/*      */           break;
/*      */         
/*      */         case 32773:
/* 1489 */           data = new byte[4];
/* 1490 */           tmp = Functions.get2ByteArrayFromInt(32773);
/* 1491 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1492 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1493 */           data[2] = 1;
/* 1494 */           data[3] = (byte)Character.digit(MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */           break;
/*      */         
/*      */         case 32769:
/* 1498 */           data = new byte[4];
/* 1499 */           tmp = Functions.get2ByteArrayFromInt(32769);
/* 1500 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1501 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1502 */           data[2] = 1;
/* 1503 */           data[3] = (byte)Character.digit(MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */           break;
/*      */         
/*      */         case 32770:
/* 1507 */           data = new byte[4];
/* 1508 */           tmp = Functions.get2ByteArrayFromInt(32770);
/* 1509 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1510 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1511 */           data[2] = 1;
/* 1512 */           data[3] = (byte)Character.digit(MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0), 10);
/*      */           break;
/*      */         
/*      */         case 32775:
/* 1516 */           tmp = Functions.get2ByteArrayFromInt(32775);
/* 1517 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1518 */           ascii = Functions.getASCII4mString(MercuriusUdpHandler.this.sp24DH.getCommandData());
/* 1519 */           data = new byte[ascii.length + 3];
/* 1520 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1521 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/* 1522 */           System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */           break;
/*      */         
/*      */         case 32776:
/* 1526 */           cData = MercuriusUdpHandler.this.sp24DH.getCommandData().split(";");
/* 1527 */           data = new byte[5];
/* 1528 */           data[2] = 2;
/* 1529 */           data[3] = (byte)Integer.parseInt(cData[1]);
/* 1530 */           data[4] = (byte)Integer.parseInt(cData[2]);
/* 1531 */           tmp = Functions.get2ByteArrayFromInt(32776);
/* 1532 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1533 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           break;
/*      */         
/*      */         case 32777:
/* 1537 */           tmp = Functions.get2ByteArrayFromInt(32777);
/* 1538 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1539 */           ascii = Functions.getASCII4mString(MercuriusUdpHandler.this.sp24DH.getCommandData());
/* 1540 */           data = new byte[ascii.length + 4];
/* 1541 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1542 */           data[2] = Byte.valueOf(Integer.toHexString(ascii.length + 2), 16).byteValue();
/* 1543 */           data[3] = (byte)((MercuriusUdpHandler.this.sp24DH.getCommandData().charAt(0) == '1') ? 1 : 2);
/* 1544 */           System.arraycopy(ascii, 0, data, 4, ascii.length);
/*      */           break;
/*      */         
/*      */         case 32778:
/* 1548 */           data = new byte[3];
/* 1549 */           tmp = Functions.get2ByteArrayFromInt(32778);
/* 1550 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1551 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1552 */           data[2] = 0;
/*      */           break;
/*      */         
/*      */         case 32780:
/* 1556 */           tmp = Functions.get2ByteArrayFromInt(32780);
/* 1557 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1558 */           cData = MercuriusUdpHandler.this.sp24DH.getCommandData().split(";");
/* 1559 */           if (cData.length == 1) {
/* 1560 */             if (cData[0].equals("1")) {
/* 1561 */               data = new byte[4];
/* 1562 */               System.arraycopy(tmp, 0, data, 0, 2);
/* 1563 */               data[2] = 1;
/* 1564 */               data[3] = 1; break;
/* 1565 */             }  if (cData[0].equals("2")) {
/* 1566 */               data = new byte[11];
/* 1567 */               System.arraycopy(tmp, 0, data, 0, 2);
/* 1568 */               String[] zones = TimeZone.getAvailableIDs(this.timezone * 60 * 1000);
/* 1569 */               this.df.setTimeZone(TimeZone.getTimeZone((String)Defines.TIMEZONE_NAMES.get(Integer.valueOf(this.timezone))));
/* 1570 */               String ddd = this.df.format(new Date());
/* 1571 */               data[2] = 8;
/* 1572 */               data[3] = 2;
/* 1573 */               data[4] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(0, 2))), 16).byteValue();
/* 1574 */               data[5] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(3, 5))), 16).byteValue();
/* 1575 */               tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(ddd.substring(6, 10)));
/* 1576 */               data[6] = tmp[1];
/* 1577 */               data[7] = tmp[0];
/* 1578 */               data[8] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(11, 13))), 16).byteValue();
/* 1579 */               data[9] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(14, 16))), 16).byteValue();
/* 1580 */               data[10] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(17))), 16).byteValue();
/*      */             }  break;
/*      */           } 
/* 1583 */           data = new byte[11];
/* 1584 */           System.arraycopy(tmp, 0, data, 0, 2);
/*      */           
/* 1586 */           date = cData[1].split(" ");
/* 1587 */           dData = date[0].split("-");
/* 1588 */           hData = date[1].split(":");
/* 1589 */           data[2] = 8;
/* 1590 */           data[3] = 3;
/* 1591 */           data[4] = Byte.valueOf(dData[2]).byteValue();
/* 1592 */           data[5] = Byte.valueOf(dData[1]).byteValue();
/* 1593 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(dData[0]));
/* 1594 */           data[6] = tmp[1];
/* 1595 */           data[7] = tmp[0];
/* 1596 */           data[8] = Byte.valueOf(hData[0]).byteValue();
/* 1597 */           data[9] = Byte.valueOf(hData[1]).byteValue();
/* 1598 */           data[10] = Byte.valueOf(hData[2]).byteValue();
/*      */           break;
/*      */ 
/*      */         
/*      */         case 32781:
/* 1603 */           data = new byte[4];
/* 1604 */           tmp = Functions.get2ByteArrayFromInt(32781);
/* 1605 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1606 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1607 */           data[2] = 0;
/* 1608 */           data[3] = (byte)Integer.parseInt(MercuriusUdpHandler.this.sp24DH.getCommandData());
/*      */           break;
/*      */         
/*      */         case 32782:
/* 1612 */           data = new byte[6];
/* 1613 */           tmp = Functions.get2ByteArrayFromInt(32782);
/* 1614 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1615 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1616 */           cData = MercuriusUdpHandler.this.sp24DH.getCommandData().split(";");
/* 1617 */           data[2] = 3;
/* 1618 */           data[3] = (byte)Integer.parseInt(cData[0]);
/* 1619 */           data[4] = (byte)Integer.parseInt(cData[1]);
/* 1620 */           data[5] = (byte)Integer.parseInt(cData[2]);
/*      */           break;
/*      */         
/*      */         case 32783:
/* 1624 */           data = new byte[3];
/* 1625 */           tmp = Functions.get2ByteArrayFromInt(32783);
/* 1626 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1627 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1628 */           data[2] = 0;
/*      */           break;
/*      */         
/*      */         case 32784:
/* 1632 */           data = new byte[4];
/* 1633 */           tmp = Functions.get2ByteArrayFromInt(32784);
/* 1634 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1635 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1636 */           data[2] = 1;
/* 1637 */           data[3] = (byte)Integer.parseInt(MercuriusUdpHandler.this.sp24DH.getCommandData());
/*      */           break;
/*      */         
/*      */         case 32785:
/* 1641 */           data = new byte[4];
/* 1642 */           tmp = Functions.get2ByteArrayFromInt(32785);
/* 1643 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1644 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1645 */           data[2] = 1;
/* 1646 */           data[3] = (byte)Integer.parseInt(MercuriusUdpHandler.this.sp24DH.getCommandData());
/*      */           break;
/*      */         
/*      */         case 32786:
/* 1650 */           data = new byte[4];
/* 1651 */           tmp = Functions.get2ByteArrayFromInt(32786);
/* 1652 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1653 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1654 */           data[2] = 1;
/* 1655 */           data[3] = (byte)Integer.parseInt(MercuriusUdpHandler.this.sp24DH.getCommandData());
/*      */           break;
/*      */         
/*      */         case 32787:
/* 1659 */           data = new byte[3];
/* 1660 */           tmp = Functions.get2ByteArrayFromInt(32787);
/* 1661 */           tmp = Functions.swapLSB2MSB(tmp);
/* 1662 */           System.arraycopy(tmp, 0, data, 0, 2);
/* 1663 */           data[2] = 0;
/*      */           break;
/*      */       } 
/*      */       
/*      */       try {
/* 1668 */         tmp = prepareCommandPacket(data);
/* 1669 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, tmp);
/* 1670 */         if (MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32769 || MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32770) {
/* 1671 */           MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.FILE_SENDING_INITIATED;
/* 1672 */         } else if (MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32773) {
/* 1673 */           MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.CFG_FILE_RECEIVE_INITIATED;
/* 1674 */         } else if (MercuriusUdpHandler.this.sp24DH.getCommand_Type() == 32787) {
/* 1675 */           MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.GPS_FW_RESPONSE;
/*      */         } else {
/* 1677 */           MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.COMMAND_PACKET_REPSONE;
/*      */         } 
/* 1679 */         return true;
/* 1680 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1681 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1682 */         ex.printStackTrace();
/* 1683 */         return false;
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     public byte[] prepareCommandPacket(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/* 1689 */       int plen = data.length;
/*      */       
/* 1691 */       int lpad = plen % 16;
/* 1692 */       if (lpad > 0) {
/* 1693 */         lpad = 16 - lpad;
/*      */       }
/* 1695 */       byte[] packet = new byte[plen + lpad + 4];
/* 1696 */       byte[] toEnc = new byte[plen + lpad];
/* 1697 */       System.arraycopy(data, 0, toEnc, 0, plen);
/* 1698 */       if (lpad > 0) {
/* 1699 */         for (int j = plen; j < plen + lpad; j++) {
/* 1700 */           toEnc[j] = 0;
/*      */         }
/*      */       }
/*      */ 
/*      */       
/* 1705 */       for (int i = 0; i < toEnc.length; i += 16) {
/* 1706 */         byte[] block = new byte[16];
/* 1707 */         System.arraycopy(toEnc, i, block, 0, 16);
/*      */         
/* 1709 */         byte[] decBlock = Rijndael.encryptBytes(block, Rijndael.aes_256, false);
/* 1710 */         System.arraycopy(decBlock, 0, toEnc, i, 16);
/*      */       } 
/*      */       
/* 1713 */       byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(plen + lpad + 2));
/* 1714 */       System.arraycopy(tmp, 0, packet, 0, 2);
/* 1715 */       System.arraycopy(toEnc, 0, packet, 2, plen + lpad);
/*      */       
/* 1717 */       int crcCalc = CRC16.calculate(packet, 0, plen + lpad + 2, 65535);
/* 1718 */       tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1719 */       System.arraycopy(tmp, 0, packet, plen + lpad + 2, 2);
/* 1720 */       return packet;
/*      */     }
/*      */ 
/*      */     
/*      */     public boolean requestGeofenceData() {
/* 1725 */       byte[] data = new byte[3];
/* 1726 */       byte[] tmp = Functions.get2ByteArrayFromInt(32773);
/* 1727 */       tmp = Functions.swapLSB2MSB(tmp);
/* 1728 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 1729 */       data[2] = 0;
/*      */       try {
/* 1731 */         tmp = prepareCommandPacket(data);
/* 1732 */         MercuriusUdpHandler.this.sentCommand = 32773;
/* 1733 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, tmp);
/* 1734 */         return true;
/* 1735 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1736 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1737 */         ex.printStackTrace();
/* 1738 */         return false;
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private void saveAJSFileIntoRepo() throws IOException, SQLException, InterruptedException, Exception {
/* 1744 */       if (MercuriusUdpHandler.this.currentAJS != null) {
/* 1745 */         MercuriusUdpHandler.this.raf.seek(MercuriusUdpHandler.this.currentVMPosition);
/* 1746 */         byte[] vmContent = new byte[MercuriusUdpHandler.this.currentAJS.getLength()];
/* 1747 */         MercuriusUdpHandler.this.raf.read(vmContent);
/* 1748 */         MercuriusDBManager.saveVoiceMessage(vmContent, MercuriusUdpHandler.this.currentAJS.getLength(), MercuriusUdpHandler.this.currentAJS.getName(), MercuriusUdpHandler.this.currentAJS.getCrc32(), MercuriusUdpHandler.this.currentAJS.getDir());
/* 1749 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("The_file_[") + MercuriusUdpHandler.this.currentAJS.getName() + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private void sendNextVoiceMessageFromQueue() throws SQLException, InterruptedException, Exception {
/* 1755 */       if (MercuriusUdpHandler.this.reqajsList != null && ++MercuriusUdpHandler.this.requiredVMIndex < MercuriusUdpHandler.this.reqajsList.size()) {
/* 1756 */         MercuriusUdpHandler.this.currentAJS = MercuriusUdpHandler.this.reqajsList.get(MercuriusUdpHandler.this.requiredVMIndex);
/* 1757 */         if (sendAudioJSCMD((MercuriusUdpHandler.this.currentAJS.getDir() == 1) ? 32792 : 32793)) {
/* 1758 */           MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_SEND_INITIATED;
/* 1759 */           registerResponseTimeOut();
/*      */         }
/*      */       
/* 1762 */       } else if (MercuriusUdpHandler.this.sp24DH != null) {
/* 1763 */         if (MercuriusUdpHandler.this.file == null) {
/* 1764 */           MercuriusUdpHandler.this.file = new File("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule);
/*      */         }
/* 1766 */         if (MercuriusUdpHandler.this.file != null) {
/* 1767 */           MercuriusUdpHandler.this.file.delete();
/*      */         }
/* 1769 */         initiateReadingAllFilesCRC32();
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     private void requestNextVoiceMessageFromQueue() throws SQLException, InterruptedException, Exception {
/* 1776 */       if (MercuriusUdpHandler.this.requriedVMList != null && ++MercuriusUdpHandler.this.requiredVMIndex < MercuriusUdpHandler.this.requriedVMList.size()) {
/* 1777 */         MercuriusUdpHandler.this.currentAJS = MercuriusUdpHandler.this.requriedVMList.get(MercuriusUdpHandler.this.requiredVMIndex);
/* 1778 */         if (requestAudioJSInfo((MercuriusUdpHandler.this.currentAJS.getDir() == 1) ? 32794 : 32795)) {
/* 1779 */           MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.AJS_READ_INITIATED;
/* 1780 */           registerResponseTimeOut();
/*      */         }
/*      */       
/* 1783 */       } else if (MercuriusUdpHandler.this.sp24DH != null) {
/* 1784 */         if (MercuriusUdpHandler.this.file == null) {
/* 1785 */           MercuriusUdpHandler.this.file = new File(MercuriusUdpHandler.this.sn + "_READ_FULL_CFG" + MercuriusUdpHandler.this.sp24DH.getCommandData());
/*      */         }
/* 1787 */         MercuriusDBManager.updateCommandData(MercuriusUdpHandler.this.sp24DH.getId_Command(), new FileInputStream(MercuriusUdpHandler.this.file));
/* 1788 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("The_file_[") + MercuriusAVLHandlerHelper.getFileNameByCommandData(MercuriusUdpHandler.this.sp24DH.getCommandData()) + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, MercuriusUdpHandler.this.sn, null);
/* 1789 */         endCommand(MercuriusUdpHandler.this.sp24DH.getId_Command(), MercuriusUdpHandler.this.sp24DH.getExec_Retries());
/* 1790 */         if (MercuriusUdpHandler.this.file != null) {
/* 1791 */           MercuriusUdpHandler.this.file.delete();
/*      */         }
/* 1793 */         sendNextCommandFromQueue();
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     public boolean requestAudioJSInfo(int cmd) {
/* 1800 */       byte[] data = new byte[3];
/* 1801 */       byte[] tmp = Functions.get2ByteArrayFromInt(cmd);
/* 1802 */       tmp = Functions.swapLSB2MSB(tmp);
/* 1803 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 1804 */       data[2] = 0;
/*      */       
/*      */       try {
/* 1807 */         tmp = prepareCommandPacket(data);
/* 1808 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, tmp);
/* 1809 */         return true;
/* 1810 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1811 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1812 */         ex.printStackTrace();
/* 1813 */         return false;
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     public boolean sendAudioJSCMD(int cmd) {
/* 1819 */       byte[] data = new byte[3];
/* 1820 */       byte[] tmp = Functions.get2ByteArrayFromInt(cmd);
/* 1821 */       tmp = Functions.swapLSB2MSB(tmp);
/* 1822 */       System.arraycopy(tmp, 0, data, 0, 2);
/* 1823 */       data[2] = 0;
/*      */       try {
/* 1825 */         tmp = prepareCommandPacket(data);
/* 1826 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, tmp);
/* 1827 */         return true;
/* 1828 */       } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1829 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1830 */         ex.printStackTrace();
/* 1831 */         return false;
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private boolean parseM2SPacket(byte[] decData) {
/*      */       try {
/* 1838 */         int index = 0;
/* 1839 */         byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 1844 */         byte[] tmp2 = new byte[2];
/*      */ 
/*      */         
/* 1847 */         TblMercuriusActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/* 1848 */         this.module = new MercuriusAVLModule();
/* 1849 */         this.module.setDefaults();
/* 1850 */         this.module.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0));
/* 1851 */         this.module.setM2sData(decData);
/* 1852 */         this.module.setLastNWProtocol(Enums.EnumNWProtocol.UDP.name());
/*      */         
/* 1854 */         while (index < decData.length && 
/* 1855 */           index + 2 <= decData.length) {
/*      */           byte[] fcon; char[] bin; StringBuilder sb; int i; byte[] oper;
/*      */           short simNum, apn;
/* 1858 */           System.arraycopy(decData, index, fid, 0, 2);
/* 1859 */           index += 2;
/* 1860 */           fid = Functions.swapLSB2MSB(fid);
/* 1861 */           int fidVal = Functions.getIntFrom2ByteArray(fid);
/* 1862 */           if (fidVal <= 0) {
/*      */             break;
/*      */           }
/* 1865 */           short flen = (short)Functions.getIntFromHexByte(decData[index]);
/*      */           try {
/* 1867 */             fcon = new byte[flen];
/* 1868 */             System.arraycopy(decData, ++index, fcon, 0, flen);
/* 1869 */             index += flen;
/* 1870 */           } catch (ArrayIndexOutOfBoundsException ace) {
/* 1871 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "M2S Packet received with wrong length information ", Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1872 */             StringBuilder stringBuilder = new StringBuilder();
/* 1873 */             for (byte bb : decData) {
/* 1874 */               stringBuilder.append(bb).append(" ");
/*      */             }
/* 1876 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "Packet: " + stringBuilder.toString(), Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 1877 */             UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/* 1878 */             return true;
/*      */           } 
/* 1880 */           switch (fidVal) {
/*      */             case 1:
/* 1882 */               MercuriusUdpHandler.this.sn = Functions.getASCIIFromByteArray(fcon);
/* 1883 */               this.module.setSn(MercuriusUdpHandler.this.sn);
/*      */               
/* 1885 */               if (MercuriusUdpHandler.this.sn.equals("0000000000")) {
/* 1886 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, MercuriusUdpHandler.this.sn, null);
/* 1887 */                 UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/* 1888 */                 return false;
/* 1889 */               }  if (MercuriusUdpHandler.this.sn.equals("0000000001")) {
/* 1890 */                 String ip = MercuriusUdpHandler.this.remoteIP.substring(0, MercuriusUdpHandler.this.remoteIP.indexOf(":"));
/* 1891 */                 if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 1892 */                   if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/* 1893 */                     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, MercuriusUdpHandler.this.sn, null);
/* 1894 */                     UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/* 1895 */                     return false;
/*      */                   }
/*      */                 
/* 1898 */                 } else if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/* 1899 */                   Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, MercuriusUdpHandler.this.sn, null);
/* 1900 */                   UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/* 1901 */                   return false;
/*      */                 } 
/*      */               } 
/*      */               
/* 1905 */               if (TblMercuriusActiveConnections.getInstance().containsKey(MercuriusUdpHandler.this.sn)) {
/* 1906 */                 String oldIP = null;
/* 1907 */                 UdpV2Handler oldHandler = null;
/* 1908 */                 synchronized (TblMercuriusActiveConnections.getInstance()) {
/* 1909 */                   for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblMercuriusAVLActiveUdpConnections.getInstance().entrySet()) {
/* 1910 */                     if (MercuriusUdpHandler.this.sn.equals(((UdpV2Handler)handler.getValue()).sn) && ((UdpV2Handler)handler.getValue()).lastCommunicationTime + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationTimeout > System.currentTimeMillis() && 
/* 1911 */                       !((String)handler.getKey()).equalsIgnoreCase(MercuriusUdpHandler.this.remoteIP)) {
/* 1912 */                       oldIP = handler.getKey();
/* 1913 */                       oldHandler = handler.getValue();
/*      */                     } 
/*      */                   } 
/*      */                 } 
/*      */ 
/*      */                 
/* 1919 */                 if (oldIP != null && oldHandler != null) {
/* 1920 */                   TblMercuriusAVLActiveUdpConnections.removeConnection(oldIP);
/* 1921 */                   UdpV2Handler cHandler = (UdpV2Handler)TblMercuriusAVLActiveUdpConnections.getInstance().get(MercuriusUdpHandler.this.remoteIP);
/* 1922 */                   TblMercuriusAVLActiveUdpConnections.addConnection(MercuriusUdpHandler.this.remoteIP, oldHandler);
/* 1923 */                   ((UdpV2Handler)TblMercuriusAVLActiveUdpConnections.getInstance().get(MercuriusUdpHandler.this.remoteIP)).processM2SPacket(this.data);
/* 1924 */                   oldHandler.updateRemoteIP(MercuriusUdpHandler.this.remoteIP, cHandler.getCurrentSocket(), cHandler.getCurrentPacket());
/* 1925 */                   return false;
/*      */                 } 
/*      */               } 
/*      */ 
/*      */             
/*      */             case 2:
/* 1931 */               if ((fcon[0] & 0xFF) == 1) {
/* 1932 */                 this.module.setModemModel(fcon[2] & 0xFF); continue;
/* 1933 */               }  if ((fcon[1] & 0xFF) == 1) {
/* 1934 */                 this.module.setGpsModel(fcon[3] & 0xFF);
/*      */               }
/*      */ 
/*      */             
/*      */             case 3:
/* 1939 */               sb = new StringBuilder();
/* 1940 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1])).append('.').append(Functions.getIntFromHexByte(fcon[2]));
/* 1941 */               this.module.setModuleFWVersion(sb.toString());
/*      */ 
/*      */             
/*      */             case 4:
/* 1945 */               sb = new StringBuilder();
/* 1946 */               tmp2[0] = fcon[2];
/* 1947 */               tmp2[1] = fcon[3];
/* 1948 */               sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFrom2ByteArray(tmp2));
/* 1949 */               this.module.setModemFWVersion(sb.toString());
/*      */ 
/*      */             
/*      */             case 5:
/* 1953 */               sb = new StringBuilder();
/* 1954 */               for (i = 0; i < flen - 1; i++) {
/* 1955 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1957 */               sb.append(fcon[7] / 10);
/* 1958 */               this.module.setModemIMEI(sb.toString());
/*      */ 
/*      */             
/*      */             case 6:
/* 1962 */               sb = new StringBuilder();
/* 1963 */               for (i = 1; i < flen; i++) {
/* 1964 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1966 */               if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1967 */                 this.module.setSim_iccid_1(sb.toString()); continue;
/* 1968 */               }  if (Functions.getIntFromHexByte(fcon[0]) == 2) {
/* 1969 */                 this.module.setSim_iccid_2(sb.toString());
/*      */               }
/*      */ 
/*      */             
/*      */             case 7:
/* 1974 */               sb = new StringBuilder();
/* 1975 */               for (i = 1; i < flen - 1; i++) {
/* 1976 */                 sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */               }
/* 1978 */               sb.append(fcon[8] / 10);
/* 1979 */               if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1980 */                 this.module.setSim_imsi_1(sb.toString()); continue;
/* 1981 */               }  if (Functions.getIntFromHexByte(fcon[0]) == 2) {
/* 1982 */                 this.module.setSim_imsi_2(sb.toString());
/*      */               }
/*      */ 
/*      */             
/*      */             case 8:
/* 1987 */               oper = new byte[flen - 1];
/* 1988 */               System.arraycopy(fcon, 1, oper, 0, flen - 1);
/* 1989 */               if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1990 */                 this.module.setSim_operator_1(Functions.getASCIIFromByteArray(oper)); continue;
/* 1991 */               }  if (Functions.getIntFromHexByte(fcon[0]) == 2) {
/* 1992 */                 this.module.setSim_operator_2(Functions.getASCIIFromByteArray(oper));
/*      */               }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*      */             case 10:
/* 2000 */               this.lastCommIface = (short)Functions.getIntFromHexByte(fcon[0]);
/* 2001 */               this.module.setCurrentInterface(this.lastCommIface);
/*      */ 
/*      */             
/*      */             case 11:
/* 2005 */               this.module.setCurrentSIM(Functions.getIntFromHexByte(fcon[0]));
/* 2006 */               this.module.setCurrentAPN(Functions.getIntFromHexByte(fcon[1]));
/*      */ 
/*      */             
/*      */             case 12:
/* 2010 */               this.module.setGsmSignalLevel(Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */             
/*      */             case 14:
/* 2014 */               this.module.setGprsDataVolume(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*      */ 
/*      */             
/*      */             case 15:
/* 2018 */               bin = Functions.getBinaryFromByte(fcon[0]);
/* 2019 */               sb = new StringBuilder();
/* 2020 */               sb.append(bin[1]).append(bin[2]).append(bin[3]);
/* 2021 */               simNum = (short)Integer.parseInt(sb.toString(), 2);
/* 2022 */               sb = new StringBuilder();
/* 2023 */               sb.append(bin[4]).append(bin[5]);
/* 2024 */               apn = (short)Integer.parseInt(sb.toString(), 2);
/* 2025 */               sb = new StringBuilder();
/* 2026 */               sb.append(bin[6]).append(bin[7]);
/* 2027 */               if (bin[0] == '0') {
/* 2028 */                 this.module.setSimCard1Status(simNum);
/* 2029 */                 this.module.setSimCard1OperativeStatus(apn);
/* 2030 */                 this.module.setSimCard1JDRStatus((short)Integer.parseInt(sb.toString(), 2)); continue;
/* 2031 */               }  if (bin[0] == '1') {
/* 2032 */                 this.module.setSimCard2Status(simNum);
/* 2033 */                 this.module.setSimCard2OperativeStatus(apn);
/* 2034 */                 this.module.setSimCard2JDRStatus((short)Integer.parseInt(sb.toString(), 2));
/*      */               } 
/*      */ 
/*      */             
/*      */             case 16:
/* 2039 */               tmp2[0] = fcon[1];
/* 2040 */               tmp2[1] = fcon[0];
/* 2041 */               this.timezone = Functions.getSignedIntFrom2ByteArray(tmp2);
/* 2042 */               if ((fcon[2] & 0xFF) == 1) {
/* 2043 */                 this.module.setTimeSync(true);
/*      */               }
/*      */ 
/*      */             
/*      */             case 17:
/* 2048 */               this.module.setInitialPacket(true);
/* 2049 */               this.module.setGeofencecrc32(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*      */             case 20:
/* 2056 */               this.module.setSatelliteCount(Functions.getIntFromHexByte(fcon[0]));
/*      */             
/*      */             case 21:
/* 2059 */               tmp2[0] = fcon[1];
/* 2060 */               tmp2[1] = fcon[0];
/* 2061 */               this.module.setModuleHWDtls(Functions.getIntFrom2ByteArray(tmp2));
/*      */ 
/*      */             
/*      */             case 100:
/* 2065 */               this.module.setCurrentSchedule((short)(Functions.getIntFromHexByte(fcon[4]) + 1));
/* 2066 */               if (flen == 17) {
/* 2067 */                 byte[] gpsData = new byte[12];
/* 2068 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2069 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 101:
/* 2074 */               if (flen == 17) {
/* 2075 */                 byte[] gpsData = new byte[12];
/* 2076 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2077 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 102:
/* 2082 */               if (flen == 17) {
/* 2083 */                 byte[] gpsData = new byte[12];
/* 2084 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2085 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 103:
/* 2090 */               if (flen == 17) {
/* 2091 */                 byte[] gpsData = new byte[12];
/* 2092 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2093 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 104:
/* 2098 */               if (flen == 17) {
/* 2099 */                 byte[] gpsData = new byte[12];
/* 2100 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2101 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 105:
/* 2106 */               if (flen == 20) {
/* 2107 */                 byte[] gpsData = new byte[12];
/* 2108 */                 System.arraycopy(fcon, 8, gpsData, 0, 12);
/* 2109 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 106:
/* 2114 */               if (flen == 17) {
/* 2115 */                 byte[] gpsData = new byte[12];
/* 2116 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2117 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 107:
/* 2122 */               if (flen == 17) {
/* 2123 */                 byte[] gpsData = new byte[12];
/* 2124 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2125 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 108:
/* 2130 */               if (flen == 17) {
/* 2131 */                 byte[] gpsData = new byte[12];
/* 2132 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2133 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */ 
/*      */             
/*      */             case 109:
/* 2138 */               if (flen == 17) {
/* 2139 */                 byte[] gpsData = new byte[12];
/* 2140 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2141 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */             
/*      */             case 110:
/* 2145 */               if (flen == 17) {
/* 2146 */                 byte[] gpsData = new byte[12];
/* 2147 */                 System.arraycopy(fcon, 5, gpsData, 0, 12);
/* 2148 */                 Functions.parseGpsData(gpsData, this.module);
/*      */               } 
/*      */           } 
/*      */ 
/*      */         
/*      */         } 
/* 2154 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.sn + LocaleMessage.getLocaleMessage("]_M2S_packet_received") + Functions.getMercuriusIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/* 2155 */         this.module.setAutoRegistration((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/* 2156 */         this.module.setIp(MercuriusUdpHandler.this.remoteIP.substring(0, MercuriusUdpHandler.this.remoteIP.indexOf(":")));
/*      */         try {
/* 2158 */           TblMercuriusActiveConnections.semaphoreAlivePacketsReceived.acquire();
/* 2159 */           this.module = MercuriusDBManager.executeSPM_001(this.module);
/*      */         } finally {
/* 2161 */           TblMercuriusActiveConnections.semaphoreAlivePacketsReceived.release();
/*      */         } 
/* 2163 */         if (this.module != null) {
/* 2164 */           this.idleTimeout = System.currentTimeMillis() + (this.module.getComm_Timeout() * 1000);
/* 2165 */           MercuriusUdpHandler.this.iTimeout = this.idleTimeout;
/* 2166 */           if (this.module.getAuto_Registration_Executed() == 1) {
/* 2167 */             if (this.module.getRegistered() == 0) {
/* 2168 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + MercuriusUdpHandler.this.sn, Enums.EnumMessagePriority.HIGH, null, null);
/* 2169 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { -32 });
/* 2170 */               return false;
/*      */             } 
/* 2172 */           } else if (this.module.getRegistered() == 1) {
/* 2173 */             if (this.module.getEnabled() == 0) {
/* 2174 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + MercuriusUdpHandler.this.sn + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 2175 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { -31 });
/* 2176 */               return false;
/*      */             } 
/*      */           } else {
/* 2179 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + MercuriusUdpHandler.this.sn + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 2180 */             UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { -32 });
/* 2181 */             return false;
/*      */           } 
/*      */           
/* 2184 */           Functions.generateEventReceptionAlivePacket(Util.EnumProductIDs.MERCURIUS.getProductId(), this.module.getId_Client(), this.module.getId_Module(), this.module.getId_Group(), this.module.getClientCode(), this.module.getE_Alive_Received(), this.module.getF_Alive_Received(), this.module.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.UDP.name(), this.lastCommIface, -1);
/*      */           
/* 2186 */           if (this.module.isInitialPacket()) {
/* 2187 */             if (TblMercuriusActiveConnections.getInstance().containsKey(MercuriusUdpHandler.this.sn)) {
/* 2188 */               synchronized (TblMercuriusAVLActiveUdpConnections.getInstance()) {
/* 2189 */                 for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblMercuriusAVLActiveUdpConnections.getInstance().entrySet()) {
/* 2190 */                   if (MercuriusUdpHandler.this.sn.equals(((UdpV2Handler)handler.getValue()).sn)) {
/* 2191 */                     ((UdpV2Handler)handler.getValue()).dispose();
/*      */                   }
/*      */                 } 
/*      */               } 
/* 2195 */               TblMercuriusActiveConnections.removeConnection(MercuriusUdpHandler.this.sn);
/*      */             } 
/* 2197 */             TblMercuriusActiveConnections.addConnection(MercuriusUdpHandler.this.sn, MercuriusUdpHandler.this.myThreadGuid);
/* 2198 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idClient = this.module.getId_Client();
/* 2199 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule = this.module.getId_Module();
/* 2200 */             TblMercuriusAVLActiveUdpConnections.getInstance().put(MercuriusUdpHandler.this.remoteIP, MercuriusUdpHandler.this.currInsance);
/* 2201 */           } else if (!TblMercuriusActiveConnections.getInstance().containsKey(MercuriusUdpHandler.this.sn)) {
/* 2202 */             TblMercuriusActiveConnections.addConnection(MercuriusUdpHandler.this.sn, MercuriusUdpHandler.this.myThreadGuid);
/* 2203 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idClient = this.module.getId_Client();
/* 2204 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule = this.module.getId_Module();
/* 2205 */             TblMercuriusAVLActiveUdpConnections.getInstance().put(MercuriusUdpHandler.this.remoteIP, MercuriusUdpHandler.this.currInsance);
/*      */           } 
/* 2207 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).clientName = this.module.getName();
/* 2208 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idGroup = this.module.getId_Group();
/* 2209 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationDebug = (this.module.getCommDebug() == 1);
/* 2210 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationTimeout = this.module.getComm_Timeout() * 1000;
/*      */           
/*      */           try {
/* 2213 */             UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/* 2214 */             if (this.module.isInitialPacket() && !this.module.isCrc32Matched()) {
/* 2215 */               Thread.sleep(100L);
/* 2216 */               UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { Byte.MIN_VALUE });
/* 2217 */               MercuriusUdpHandler.this.initiatedGeofenceRequest = true;
/* 2218 */               MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.EXPECTED_REPLY;
/*      */             } 
/* 2220 */             return true;
/* 2221 */           } catch (IOException|InterruptedException ex) {
/* 2222 */             return false;
/*      */           } 
/*      */         } 
/* 2225 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Error_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.AVERAGE, MercuriusUdpHandler.this.sn, null);
/* 2226 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/* 2227 */         return false;
/*      */       }
/* 2229 */       catch (Exception ex) {
/* 2230 */         ex.printStackTrace();
/* 2231 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + MercuriusUdpHandler.this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */         try {
/* 2233 */           UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 21 });
/* 2234 */         } catch (IOException iOException) {}
/*      */         
/* 2236 */         return false;
/*      */       } finally {
/* 2238 */         TblMercuriusActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*      */       } 
/*      */     }
/*      */     
/*      */     private void sendFile2Module(boolean firstChunk) throws SQLException, InterruptedException, FileNotFoundException, IOException {
/* 2243 */       if (firstChunk) {
/* 2244 */         MercuriusUdpHandler.this.file = Functions.writeByteArrayToFile(MercuriusUdpHandler.this.sn + "_" + MercuriusUdpHandler.this.sp24DH.getCommandData(), MercuriusUdpHandler.this.sp24DH.getCommandFileData());
/* 2245 */         MercuriusUdpHandler.this.fc = (new RandomAccessFile(MercuriusUdpHandler.this.file, "r")).getChannel();
/* 2246 */         MercuriusUdpHandler.this.flen = (int)MercuriusUdpHandler.this.fc.size();
/* 2247 */         MercuriusUdpHandler.this.fc.position(0L);
/*      */       } 
/* 2249 */       if (MercuriusUdpHandler.this.fc.position() < MercuriusUdpHandler.this.flen) {
/* 2250 */         MercuriusUdpHandler.this.blockLength = (int)((MercuriusUdpHandler.this.flen - MercuriusUdpHandler.this.fc.position() > MercuriusUdpHandler.this.maxReadLength) ? MercuriusUdpHandler.this.maxReadLength : (MercuriusUdpHandler.this.flen - MercuriusUdpHandler.this.fc.position()));
/* 2251 */         MercuriusUdpHandler.this.blockBuf = ByteBuffer.allocate(MercuriusUdpHandler.this.blockLength);
/* 2252 */         if (MercuriusUdpHandler.this.fc.read(MercuriusUdpHandler.this.blockBuf) == MercuriusUdpHandler.this.blockLength) {
/* 2253 */           MercuriusUdpHandler.this.block = MercuriusUdpHandler.this.blockBuf.array();
/* 2254 */           MercuriusUdpHandler.this.packet = new byte[MercuriusUdpHandler.this.blockLength + 5];
/* 2255 */           MercuriusUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(MercuriusUdpHandler.this.blockIndex));
/* 2256 */           System.arraycopy(MercuriusUdpHandler.this.ftmp, 0, MercuriusUdpHandler.this.packet, 0, 2);
/* 2257 */           MercuriusUdpHandler.this.packet[2] = (byte)Integer.parseInt(Integer.toHexString(MercuriusUdpHandler.this.blockLength), 16);
/* 2258 */           System.arraycopy(MercuriusUdpHandler.this.block, 0, MercuriusUdpHandler.this.packet, 3, MercuriusUdpHandler.this.blockLength);
/* 2259 */           int crcCalc = CRC16.calculate(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.blockLength + 3, 65535);
/* 2260 */           MercuriusUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 2261 */           System.arraycopy(MercuriusUdpHandler.this.ftmp, 0, MercuriusUdpHandler.this.packet, MercuriusUdpHandler.this.blockLength + 3, 2);
/* 2262 */           MercuriusUdpHandler.this.outPacket = new DatagramPacket(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.packet.length, MercuriusUdpHandler.this.inPacket.getAddress(), MercuriusUdpHandler.this.inPacket.getPort());
/* 2263 */           MercuriusUdpHandler.this.socket.send(MercuriusUdpHandler.this.outPacket);
/* 2264 */           if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2265 */             updateLastCommunicationModuleData();
/* 2266 */             MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     private void sendVoiceMessageFile2Module() throws SQLException, InterruptedException, FileNotFoundException, IOException {
/* 2273 */       if (MercuriusUdpHandler.this.file == null) {
/* 2274 */         MercuriusUdpHandler.this.file = new File("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule);
/*      */       }
/* 2276 */       if (MercuriusUdpHandler.this.raf == null) {
/* 2277 */         MercuriusUdpHandler.this.raf = new RandomAccessFile(MercuriusUdpHandler.this.file, "r");
/*      */       }
/* 2279 */       if (MercuriusUdpHandler.this.raf.getChannel().position() < MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen) {
/* 2280 */         MercuriusUdpHandler.this.blockLength = (int)((MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen - MercuriusUdpHandler.this.raf.getChannel().position() > 240L) ? 240L : (MercuriusUdpHandler.this.currentVMPosition + MercuriusUdpHandler.this.flen - MercuriusUdpHandler.this.raf.getChannel().position()));
/* 2281 */         MercuriusUdpHandler.this.blockBuf = ByteBuffer.allocate(MercuriusUdpHandler.this.blockLength);
/* 2282 */         if (MercuriusUdpHandler.this.raf.getChannel().read(MercuriusUdpHandler.this.blockBuf) == MercuriusUdpHandler.this.blockLength) {
/* 2283 */           MercuriusUdpHandler.this.block = MercuriusUdpHandler.this.blockBuf.array();
/* 2284 */           MercuriusUdpHandler.this.packet = new byte[MercuriusUdpHandler.this.blockLength + 5];
/* 2285 */           MercuriusUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(MercuriusUdpHandler.this.blockIndex));
/* 2286 */           System.arraycopy(MercuriusUdpHandler.this.ftmp, 0, MercuriusUdpHandler.this.packet, 0, 2);
/* 2287 */           MercuriusUdpHandler.this.packet[2] = (byte)Integer.parseInt(Integer.toHexString(MercuriusUdpHandler.this.blockLength), 16);
/* 2288 */           System.arraycopy(MercuriusUdpHandler.this.block, 0, MercuriusUdpHandler.this.packet, 3, MercuriusUdpHandler.this.blockLength);
/* 2289 */           int crcCalc = CRC16.calculate(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.blockLength + 3, 65535);
/* 2290 */           MercuriusUdpHandler.this.ftmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 2291 */           System.arraycopy(MercuriusUdpHandler.this.ftmp, 0, MercuriusUdpHandler.this.packet, MercuriusUdpHandler.this.blockLength + 3, 2);
/* 2292 */           MercuriusUdpHandler.this.outPacket = new DatagramPacket(MercuriusUdpHandler.this.packet, 0, MercuriusUdpHandler.this.packet.length, MercuriusUdpHandler.this.inPacket.getAddress(), MercuriusUdpHandler.this.inPacket.getPort());
/* 2293 */           MercuriusUdpHandler.this.socket.send(MercuriusUdpHandler.this.outPacket);
/* 2294 */           if (MercuriusUdpHandler.this.nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2295 */             updateLastCommunicationModuleData();
/* 2296 */             MercuriusUdpHandler.this.nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     private void endCommand(int idCommand, short exec_Retries) throws SQLException, InterruptedException, Exception {
/* 2303 */       MercuriusDBManager.executeSP_027(idCommand, (short)(exec_Retries + 1));
/*      */     }
/*      */     
/*      */     private void updateLastCommunicationModuleData() throws SQLException, InterruptedException {
/* 2307 */       if (TblMercuriusActiveConnections.getInstance().containsKey(MercuriusUdpHandler.this.sn)) {
/* 2308 */         MercuriusDBManager.executeSP_028(((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).idModule, this.lastCommIface, this.currentSIM);
/*      */       }
/*      */     }
/*      */     
/*      */     private void sendNextCommandFromQueue() throws SQLException, InterruptedException, Exception {
/* 2313 */       if (MercuriusUdpHandler.this.cmdsList != null && ++MercuriusUdpHandler.this.cmdIndex < MercuriusUdpHandler.this.cmdsList.size()) {
/* 2314 */         MercuriusUdpHandler.this.sp24DH = MercuriusUdpHandler.this.cmdsList.get(MercuriusUdpHandler.this.cmdIndex);
/* 2315 */         if (sendCommandPacket()) {
/* 2316 */           registerResponseTimeOut();
/*      */         }
/*      */       } else {
/* 2319 */         UDPFunctions.send(MercuriusUdpHandler.this.socket, MercuriusUdpHandler.this.inPacket, new byte[] { 6 });
/* 2320 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).newCommand = false;
/* 2321 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).nextSendingSolicitationReadingCommand = 0L;
/* 2322 */         MercuriusUdpHandler.this.commandModeActivated = false;
/* 2323 */         this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(MercuriusUdpHandler.this.sn)).communicationTimeout;
/* 2324 */         MercuriusUdpHandler.this.iTimeout = this.idleTimeout;
/* 2325 */         MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.M2S_PACKET_PARSING;
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
/* 2338 */       MercuriusUdpHandler.this.packetSentTime = System.currentTimeMillis();
/* 2339 */       MercuriusUdpHandler.this.waitingForResponse = true;
/* 2340 */       boolean failed = false;
/*      */       
/* 2342 */       while (MercuriusUdpHandler.this.waitingForResponse) {
/* 2343 */         if (MercuriusUdpHandler.this.packetSentTime + MercuriusUdpHandler.this.responseTimeout > System.currentTimeMillis()) {
/* 2344 */           Thread.sleep(5L); continue;
/*      */         } 
/* 2346 */         failed = true;
/*      */       } 
/*      */ 
/*      */       
/* 2350 */       boolean fileRetry = true;
/* 2351 */       if (failed && MercuriusUdpHandler.this.waitingForResponse) {
/* 2352 */         switch (MercuriusUdpHandler.this.nextState) {
/*      */           case EXPECTED_REPLY:
/* 2354 */             MercuriusUdpHandler.this.commandModeActivated = false;
/* 2355 */             fileRetry = false;
/* 2356 */             MercuriusUdpHandler.this.nextState = Enums.MercuriusUDPCommStates.M2S_PACKET_PARSING;
/*      */             break;
/*      */           case COMMAND_PACKET_REPSONE:
/* 2359 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           case CFG_FILE_RECEIVE_INITIATED:
/* 2362 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           case CFG_FILE_RECEIVING:
/* 2365 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           
/*      */           case FILE_SENDING:
/* 2369 */             if ((MercuriusUdpHandler.this.retry = (short)(MercuriusUdpHandler.this.retry + 1)) < MercuriusUdpHandler.this.maxRetries) {
/* 2370 */               MercuriusUdpHandler.this.blockIndex--;
/* 2371 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + "[" + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]", Enums.EnumMessagePriority.HIGH, MercuriusUdpHandler.this.sn, null);
/* 2372 */               MercuriusUdpHandler.this.fc.position(MercuriusUdpHandler.this.fc.position() - MercuriusUdpHandler.this.blockLength);
/* 2373 */               if (MercuriusUdpHandler.this.lCommIface == 1) {
/* 2374 */                 if (MercuriusUdpHandler.this.retry == 0) {
/* 2375 */                   MercuriusUdpHandler.this.responseTimeout = 120000;
/* 2376 */                 } else if (MercuriusUdpHandler.this.retry == 1) {
/* 2377 */                   MercuriusUdpHandler.this.responseTimeout = 210000;
/* 2378 */                 } else if (MercuriusUdpHandler.this.retry == 2) {
/* 2379 */                   MercuriusUdpHandler.this.responseTimeout = 300000;
/*      */                 } 
/*      */               } else {
/* 2382 */                 MercuriusUdpHandler.this.responseTimeout = 120000;
/*      */               } 
/* 2384 */               fileRetry = false;
/* 2385 */               sendFile2Module(false);
/* 2386 */               MercuriusUdpHandler.this.blockIndex++;
/* 2387 */               registerResponseTimeOut(); break;
/*      */             } 
/* 2389 */             MercuriusUdpHandler.this.retry = 0;
/* 2390 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Failed_to_send_File") + " (" + this.data[0] + ")", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           
/*      */           case FIRMWARE_CRC_RESPONSE:
/*      */           case HANDLE_DASH_BOARD_BUFFER:
/*      */             break;
/*      */           
/*      */           case GPS_FW_RESPONSE:
/* 2398 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case AJS_SINGLE_CRC32_REQUESTED:
/*      */           case AJS_SINGLE_CRC32_READING:
/*      */           case AJS_SEND_INITIATED:
/*      */           case AJS_SEND_IN_PROGRESS:
/*      */           case FILE_SENDING_INITIATED:
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case AJS_READ_INITIATED:
/* 2414 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           case AJS_READ_COMMAND:
/* 2417 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           case AJS_READ_IN_PROGRESS:
/* 2420 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_uploading_file_from_module") + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]", MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             break;
/*      */           default:
/* 2423 */             if (MercuriusUdpHandler.this.sp24DH != null)
/*      */             {
/* 2425 */               MercuriusAVLHandlerHelper.registerFailureSendCommand(MercuriusUdpHandler.this.sn, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + "[" + MercuriusUdpHandler.this.sp24DH.getCommandData() + "]" + LocaleMessage.getLocaleMessage("was_sent_successfully"), MercuriusUdpHandler.this.sp24DH.getExec_Retries(), MercuriusUdpHandler.this.sp24DH.getId_Command());
/*      */             }
/*      */             break;
/*      */         } 
/* 2429 */         if (fileRetry)
/* 2430 */           MercuriusUdpHandler.this.dispose(); 
/*      */       } 
/*      */     }
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\MercuriusUdpHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */