/*      */ package com.zeusServer.griffon;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.GriffonDBManager;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*      */ import com.zeusServer.util.CRC16;
/*      */ import com.zeusServer.util.CRC32;
/*      */ import com.zeusServer.util.Defines;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.InfoModule;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.Rijndael;
/*      */ import com.zeusServer.util.SocketFunctions;
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
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.net.InetAddress;
/*      */ import java.net.Socket;
/*      */ import java.net.SocketException;
/*      */ import java.nio.ByteBuffer;
/*      */ import java.nio.channels.FileChannel;
/*      */ import java.sql.SQLException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.TimeZone;
/*      */ import java.util.UUID;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
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
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class GriffonHandler
/*      */   extends GriffonRoutines
/*      */   implements Runnable
/*      */ {
/*      */   private Socket clientSocket;
/*      */   private short encType;
/*   69 */   private String myThreadGuid = UUID.randomUUID().toString();
/*   70 */   private String sn = "";
/*      */   private String remoteIP;
/*      */   private boolean isSync = false;
/*      */   private boolean initialMsgFlag;
/*      */   protected boolean requestedAllFilesCRC32;
/*      */   private boolean crc32MatchedNUpdateStatusNotSent = false;
/*      */   private boolean ebFWRequested = false;
/*      */   private boolean ebFWCRC32MismatchOnInitialPacket = false;
/*      */   private byte[] prod;
/*      */   private int idGroup;
/*   80 */   private List<Integer> deviceCRC32 = null;
/*   81 */   private Logger ownLogger = null;
/*      */   boolean recordedLookupRequest = false;
/*      */   boolean digitalPGMBufferReceived = false;
/*      */   boolean disableDigitalPGMBuffer = false;
/*   85 */   long last_80_Sent = 0L;
/*      */   private int timezone;
/*      */   private int recAudioLookupCRC32;
/*   88 */   private short productID = 0;
/*      */ 
/*      */   
/*      */   public GriffonHandler(Socket clientSocket, short productID, short encType, byte[] prod) throws SocketException {
/*   92 */     this.clientSocket = clientSocket;
/*   93 */     this.productID = productID;
/*   94 */     this.encType = encType;
/*   95 */     this.clientSocket.setSoTimeout(30000);
/*   96 */     this.clientSocket.setTcpNoDelay(false);
/*   97 */     this.prod = prod;
/*   98 */     this.initialMsgFlag = true;
/*   99 */     this.remoteIP = this.clientSocket.getRemoteSocketAddress().toString();
/*  100 */     this.remoteIP = this.remoteIP.substring(1);
/*  101 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  102 */     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ 
/*      */   
/*      */   public void run() {
/*      */     
/*  108 */     try { int bytesReceived = 0;
/*      */       
/*  110 */       byte[] decBlock = null;
/*      */       
/*  112 */       while (isSocketConnected() && keepThreadRunning() && this.idleTimeout > System.currentTimeMillis()) {
/*  113 */         if (this.clientSocket.getInputStream().available() > 0) {
/*  114 */           if (this.clientSocket.getInputStream().available() == 1 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  115 */             byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  116 */             if (buffer.length == 1 && (
/*  117 */               buffer[0] & 0x7) == 4) {
/*  118 */               if (this.requestedAllFilesCRC32 && this.deviceCRC32 != null) {
/*  119 */                 ModuleCFG mCFG = GriffonDBManager.readGriffonModuleCfg(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*  120 */                 if (mCFG != null) {
/*  121 */                   byte[] fileIDData = GriffonHandlerHelper.prepareFileDataByCRC32Mismatch(this.productID, this.deviceCRC32, mCFG, true);
/*  122 */                   if (fileIDData != null && fileIDData.length > 1) {
/*  123 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Entered_into_configuration_sync_mode"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  124 */                     if (sendFileSyncCommand(1)) {
/*  125 */                       monitorThread();
/*  126 */                       byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  127 */                       if (resp != null && resp[0] == 6) {
/*  128 */                         int crcCalc = CRC16.calculate(fileIDData, 0, fileIDData.length, 65535);
/*  129 */                         byte[] fileIDDataCRC16 = new byte[fileIDData.length + 2];
/*  130 */                         byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/*  131 */                         System.arraycopy(fileIDData, 0, fileIDDataCRC16, 0, fileIDData.length);
/*  132 */                         System.arraycopy(tmp2, 0, fileIDDataCRC16, fileIDData.length, 2);
/*  133 */                         SocketFunctions.send(this.clientSocket, fileIDDataCRC16);
/*  134 */                         monitorThread();
/*  135 */                         resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  136 */                         if (resp != null && resp[0] == 6) {
/*  137 */                           receiveFile4MModule((SP_024DataHolder)null, mCFG, fileIDData);
/*  138 */                           this.isSync = false;
/*  139 */                           this.requestedAllFilesCRC32 = false;
/*  140 */                           if (sendEMFWCommand()) {
/*  141 */                             monitorThread();
/*  142 */                             resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  143 */                             if (resp != null && resp[0] == 6) {
/*  144 */                               receiveEMFWFileData();
/*      */                             }
/*      */                           } 
/*  147 */                           if (sendUpdateStatusCommand(true)) {
/*  148 */                             monitorThread();
/*  149 */                             resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  150 */                             if (resp != null && resp[0] == 6) {
/*  151 */                               byte[] buff = SocketFunctions.receive(this.clientSocket, 0, 203);
/*  152 */                               if (buff != null && buff.length >= 203) {
/*  153 */                                 this.module = GriffonHandlerHelper.handleDashboardBuffer(buff, this.module, this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*  154 */                                 GriffonDBManager.executeSPG_005(this.module);
/*  155 */                                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  156 */                                 Thread.sleep(0L);
/*  157 */                                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  158 */                                 this.crc32MatchedNUpdateStatusNotSent = false;
/*      */                               } 
/*      */                             } 
/*      */                           } 
/*      */                         } 
/*      */                       } 
/*      */                     } 
/*      */                   } else {
/*  166 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, " ENTERED TO SYNC MODE BUT NO FILES REQUIRED TO SYNC .....", Enums.EnumMessagePriority.LOW, this.sn, null);
/*  167 */                     this.requestedAllFilesCRC32 = false;
/*  168 */                     if (!runtimeCommandsProcessing()) {
/*      */                       break;
/*      */                     }
/*      */                   }
/*      */                 
/*      */                 } 
/*  174 */               } else if (!runtimeCommandsProcessing()) {
/*      */                 
/*      */                 break;
/*      */               }
/*      */             
/*      */             }
/*  180 */           } else if (this.clientSocket.getInputStream().available() > 1 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  181 */             if (!this.initialMsgFlag) {
/*  182 */               this.prod = Functions.swapLSB2MSB(SocketFunctions.receive(this.clientSocket, 0, 2));
/*  183 */               String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.prod[0] & 0xFF) }).replace(' ', '0');
/*  184 */               this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  185 */               prodBin = prodBin.substring(2);
/*  186 */               prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.prod[1] & 0xFF) }).replace(' ', '0'));
/*  187 */               this.productID = Short.parseShort(prodBin, 2);
/*  188 */               if (this.prod[0] == 43 && this.prod[1] == 43) {
/*  189 */                 dispose();
/*      */                 break;
/*      */               } 
/*  192 */               if (this.productID != Util.EnumProductIDs.GRIFFON_V1.getProductId() && this.productID != Util.EnumProductIDs.GRIFFON_V2.getProductId() && this.prod[0] != 43 && this.prod[1] != 43) {
/*  193 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.sn + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_TCP"), Enums.EnumMessagePriority.HIGH, null, null);
/*  194 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.sn + this.prod[0] + this.prod[1], Enums.EnumMessagePriority.HIGH, null, null);
/*      */                 try {
/*  196 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  197 */                 } catch (IOException ex) {}
/*      */                 
/*      */                 break;
/*      */               } 
/*      */             } 
/*      */             
/*  203 */             if (this.encType != 1 && this.encType != 2) {
/*  204 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Module_Connected_with_Invalid_Encryption"), Enums.EnumMessagePriority.AVERAGE, this.remoteIP, null);
/*      */               break;
/*      */             } 
/*  207 */             byte[] length = Functions.swapLSB2MSB(SocketFunctions.receive(this.clientSocket, 0, 2));
/*  208 */             int msgLen = Functions.getIntFrom2ByteArray(length);
/*  209 */             byte[] data = SocketFunctions.receive(this.clientSocket, 0, msgLen);
/*  210 */             byte[] cbits = new byte[msgLen + 2];
/*  211 */             cbits[0] = this.prod[1];
/*  212 */             cbits[1] = this.prod[0];
/*  213 */             cbits[2] = length[1];
/*  214 */             cbits[3] = length[0];
/*      */             
/*  216 */             System.arraycopy(data, 0, cbits, 4, msgLen - 2);
/*  217 */             int crcCalc = CRC16.calculate(cbits, 0, msgLen + 2, 65535);
/*  218 */             byte[] crcbits = new byte[2];
/*  219 */             crcbits[0] = data[msgLen - 2];
/*  220 */             crcbits[1] = data[msgLen - 1];
/*  221 */             crcbits = Functions.swapLSB2MSB(crcbits);
/*  222 */             int crcRecv = Functions.getIntFrom2ByteArray(crcbits);
/*  223 */             if (crcCalc == crcRecv) {
/*      */               
/*  225 */               byte[] encData = new byte[msgLen - 2];
/*  226 */               System.arraycopy(data, 0, encData, 0, msgLen - 2);
/*  227 */               byte[] decData = new byte[msgLen - 2];
/*  228 */               byte[] block = new byte[16];
/*  229 */               if (encData.length >= 16 && encData.length % 16 == 0) {
/*  230 */                 for (int i = 0; i < encData.length; ) {
/*  231 */                   System.arraycopy(encData, i, block, 0, 16);
/*  232 */                   if (this.encType == 2) {
/*  233 */                     decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*  234 */                   } else if (this.encType == 1) {
/*  235 */                     decBlock = Rijndael.decryptBytes(block, Rijndael.dataKeyBytes, false);
/*      */                   } 
/*  237 */                   System.arraycopy(decBlock, 0, decData, i, 16);
/*  238 */                   i += 16;
/*      */                 } 
/*      */               }
/*  241 */               if (!parseM2SPacket(decData)) {
/*      */                 break;
/*      */               }
/*      */             } else {
/*  245 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*      */               try {
/*  247 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  248 */               } catch (IOException ex) {
/*      */                 break;
/*      */               } 
/*      */             } 
/*  252 */             bytesReceived = 0;
/*  253 */             if (this.initialMsgFlag) {
/*  254 */               this.initialMsgFlag = false;
/*      */             }
/*      */           } 
/*  257 */         } else if (bytesReceived == 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && 
/*  258 */           TblGriffonActiveConnections.getInstance().containsKey(this.sn) && (
/*  259 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).newCommand || this.isSync || this.crc32MatchedNUpdateStatusNotSent || this.ebFWRequested || this.disableDigitalPGMBuffer || this.recordedLookupRequest) && 
/*  260 */           this.last_80_Sent + 30000L < System.currentTimeMillis()) {
/*  261 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/*  262 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */           try {
/*  264 */             SocketFunctions.sendWithOutSkip(this.clientSocket, newCmd);
/*  265 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  266 */             if (this.isSync || this.crc32MatchedNUpdateStatusNotSent || this.ebFWRequested || this.disableDigitalPGMBuffer || this.recordedLookupRequest) {
/*  267 */               this.last_80_Sent = System.currentTimeMillis();
/*      */             } else {
/*  269 */               this.last_80_Sent = 0L;
/*  270 */               ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*      */             } 
/*  272 */           } catch (IOException ex) {
/*      */             break;
/*      */           } 
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/*  279 */         sleepThread();
/*      */       }  }
/*  281 */     catch (InterruptedException interruptedException)
/*      */     
/*      */     { 
/*      */       
/*      */       try { 
/*      */ 
/*      */         
/*  288 */         if (this.clientSocket != null) {
/*  289 */           this.clientSocket.close();
/*      */         }
/*  291 */         dispose(); }
/*  292 */       catch (IOException ex)
/*  293 */       { Logger.getLogger(GriffonHandler.class.getName()).log(Level.SEVERE, (String)null, ex); }  } catch (Exception ex) { ex.printStackTrace(); Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_in_the_Data_Server_task"), Enums.EnumMessagePriority.HIGH, this.sn, ex); } finally { try { if (this.clientSocket != null) this.clientSocket.close();  dispose(); } catch (IOException ex) { Logger.getLogger(GriffonHandler.class.getName()).log(Level.SEVERE, (String)null, ex); }
/*      */        }
/*      */   
/*      */   }
/*      */   
/*      */   private boolean runtimeCommandsProcessing() throws Exception {
/*  299 */     if (this.crc32MatchedNUpdateStatusNotSent) {
/*  300 */       if (sendUpdateStatusCommand(false)) {
/*  301 */         monitorThread();
/*  302 */         byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  303 */         if (resp != null && resp[0] == 6) {
/*  304 */           byte[] buff = SocketFunctions.receive(this.clientSocket, 0, 203);
/*  305 */           if (buff != null && buff.length >= 203) {
/*  306 */             this.module = GriffonHandlerHelper.handleDashboardBuffer(buff, this.module, this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*  307 */             GriffonDBManager.executeSPG_005(this.module);
/*  308 */             SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  309 */             Thread.sleep(0L);
/*  310 */             SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  311 */             this.crc32MatchedNUpdateStatusNotSent = false;
/*      */           } 
/*      */         } 
/*      */       } else {
/*  315 */         return false;
/*      */       } 
/*  317 */     } else if (this.ebFWRequested) {
/*  318 */       if (sendEMFWCommand()) {
/*  319 */         monitorThread();
/*  320 */         byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  321 */         if (resp != null && resp[0] == 6) {
/*  322 */           receiveEMFWFileData();
/*  323 */           this.ebFWRequested = false;
/*  324 */           if (this.ebFWCRC32MismatchOnInitialPacket) {
/*  325 */             if (sendUpdateStatusCommand(false)) {
/*  326 */               monitorThread();
/*  327 */               resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  328 */               if (resp != null && resp[0] == 6) {
/*  329 */                 byte[] buff = SocketFunctions.receive(this.clientSocket, 0, 203);
/*  330 */                 if (buff != null && buff.length >= 203) {
/*  331 */                   this.module = GriffonHandlerHelper.handleDashboardBuffer(buff, this.module, this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*  332 */                   GriffonDBManager.executeSPG_005(this.module);
/*  333 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  334 */                   this.ebFWCRC32MismatchOnInitialPacket = false;
/*      */                 } 
/*      */               } 
/*      */             } else {
/*  338 */               return false;
/*      */             } 
/*      */           } else {
/*  341 */             SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*      */           } 
/*      */         } 
/*      */       } 
/*  345 */     } else if (this.disableDigitalPGMBuffer) {
/*  346 */       if (sendEnableDisablePGMDigitalBuffer(0)) {
/*  347 */         monitorThread();
/*  348 */         byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  349 */         if (resp != null && resp[0] == 6) {
/*  350 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  351 */           this.disableDigitalPGMBuffer = false;
/*      */         } 
/*      */       } 
/*  354 */     } else if (this.recordedLookupRequest) {
/*  355 */       requestRecordAudioLookupData();
/*  356 */       this.recordedLookupRequest = false;
/*      */     }
/*  358 */     else if (!processCommandPacket()) {
/*  359 */       return false;
/*      */     } 
/*      */     
/*  362 */     return true;
/*      */   }
/*      */   
/*      */   private boolean isSocketConnected() {
/*  366 */     if (this.clientSocket != null && this.clientSocket.isConnected()) {
/*  367 */       if (TblGriffonActiveConnections.getInstance().containsKey(this.sn)) {
/*  368 */         return !((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).fecharConexao;
/*      */       }
/*  370 */       return true;
/*      */     } 
/*      */     
/*  373 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean keepThreadRunning() {
/*  378 */     if (TblGriffonActiveConnections.getInstance().containsKey(this.sn)) {
/*  379 */       return this.myThreadGuid.equals(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).ownerThreadGuid);
/*      */     }
/*  381 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   private void monitorThread() throws InterruptedException, IOException {
/*  386 */     for (byte ii = 0; ii < 30; ii = (byte)(ii + 1)) {
/*  387 */       Thread.sleep(1000L);
/*  388 */       if (this.clientSocket.getInputStream().available() > 0) {
/*      */         break;
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void sleepThread() throws InterruptedException, IOException {
/*  395 */     for (byte ii = 0; ii < 2; ii = (byte)(ii + 1)) {
/*  396 */       Thread.sleep(2500L);
/*  397 */       if (this.clientSocket.getInputStream().available() > 0) {
/*      */         break;
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void dispose() {
/*  404 */     this.clientSocket = SocketFunctions.closeSocket(this.clientSocket);
/*  405 */     if (TblGriffonActiveConnections.getInstance().containsKey(this.sn) && 
/*  406 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).ownerThreadGuid)) {
/*  407 */       TblGriffonActiveConnections.removeConnection(this.sn);
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean parseM2SPacket(byte[] decData) {
/*      */     try {
/*  414 */       TblGriffonActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/*  415 */       this.module = new GriffonModule();
/*  416 */       this.module.setDefaults();
/*  417 */       this.module.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0));
/*  418 */       this.module.setM2sData(decData);
/*  419 */       this.module.setLastNWProtocol(Enums.EnumNWProtocol.TCP.name());
/*  420 */       if (!this.initialMsgFlag) {
/*  421 */         this.module.setSn(this.sn);
/*      */       }
/*  423 */       this.digitalPGMBufferReceived = false;
/*  424 */       boolean pendingAlive = false;
/*  425 */       boolean appDataReceived = false;
/*  426 */       List<Event> eList = null;
/*  427 */       List<ExpansionModule> emList = null;
/*  428 */       List<Partition> pList = null;
/*  429 */       List<Zone> zList = null;
/*  430 */       List<PGM> pgmList = null;
/*  431 */       List<Access> acList = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  438 */       int index = 0;
/*  439 */       byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  449 */       String partition = null;
/*  450 */       String zoneCode = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  457 */       byte[] tmp2 = new byte[2];
/*  458 */       byte[] tmp4 = new byte[4];
/*      */ 
/*      */       
/*  461 */       int sysVal2 = 0;
/*      */       
/*  463 */       int eType = 0;
/*      */ 
/*      */       
/*  466 */       while (index < decData.length && 
/*  467 */         index + 2 <= decData.length) {
/*      */         Event event; ExpansionModule eModule; PGM pgm; Access access; byte[] fcon; long dValue; String account; long timestamp; int evntQulifier, eventIndex; String rptCode; int readerType, access_iface, emIndex, zoneIndex, zoneStatus, tmp, idx, sysVal1, sysVal3; StringBuilder sb; int partitionIndex; short partitionStatus; int source, pno, eventType; List<Integer> analogPGMIndex; int i; byte[] moduleSN; int j; byte[] simCardOperator; int k;
/*      */         short numRepPgms, numPgm;
/*  470 */         System.arraycopy(decData, index, fid, 0, 2);
/*  471 */         index += 2;
/*  472 */         fid = Functions.swapLSB2MSB(fid);
/*  473 */         int fidVal = Functions.getIntFrom2ByteArray(fid);
/*  474 */         if (fidVal <= 0) {
/*      */           break;
/*      */         }
/*  477 */         short flen = (short)Functions.getIntFromHexByte(decData[index]);
/*      */         try {
/*  479 */           fcon = new byte[flen];
/*  480 */           System.arraycopy(decData, ++index, fcon, 0, flen);
/*  481 */           index += flen;
/*  482 */         } catch (ArrayIndexOutOfBoundsException ace) {
/*  483 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "M2S Packet received with wrong length information ", Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  484 */           StringBuilder stringBuilder = new StringBuilder();
/*  485 */           for (byte bb : decData) {
/*  486 */             stringBuilder.append(bb).append(" ");
/*      */           }
/*  488 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Packet: " + stringBuilder.toString(), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  489 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  490 */           return true;
/*      */         } 
/*      */         
/*  493 */         switch (fidVal) {
/*      */           case 1:
/*  495 */             idx = 0;
/*  496 */             if (eList == null) {
/*  497 */               eList = new ArrayList<>();
/*      */             }
/*  499 */             if (zList == null) {
/*  500 */               zList = new ArrayList<>();
/*      */             }
/*  502 */             tmp2[1] = fcon[idx++];
/*  503 */             tmp2[0] = fcon[idx++];
/*  504 */             eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/*  505 */             tmp2[0] = fcon[idx++];
/*  506 */             tmp2[1] = fcon[idx++];
/*  507 */             account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/*      */             
/*  509 */             tmp4[0] = fcon[idx++];
/*  510 */             tmp4[1] = fcon[idx++];
/*  511 */             tmp4[2] = fcon[idx++];
/*  512 */             tmp4[3] = fcon[idx++];
/*  513 */             timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  514 */             evntQulifier = fcon[idx++] & 0xFF;
/*      */             
/*  516 */             tmp2[0] = fcon[idx++];
/*  517 */             tmp2[1] = fcon[idx++];
/*  518 */             rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  519 */             zoneStatus = fcon[idx++] & 0xFF;
/*  520 */             appDataReceived = true;
/*  521 */             switch (zoneStatus) {
/*      */               case 4:
/*      */               case 6:
/*  524 */                 appDataReceived = true;
/*      */                 break;
/*      */             } 
/*  527 */             zoneIndex = fcon[idx++] & 0xFF;
/*  528 */             zoneCode = String.format("%3s", new Object[] { String.valueOf(zoneIndex) }).replace(' ', '0');
/*  529 */             partitionIndex = fcon[idx++] & 0xFF;
/*  530 */             partition = String.format("%2s", new Object[] { String.valueOf(partitionIndex) }).replace(' ', '0');
/*  531 */             if (zoneStatus != 5 && 
/*  532 */               zoneStatus == 1 && evntQulifier == 1 && partitionIndex > 0 && partitionIndex <= 16) {
/*  533 */               if (pList == null) {
/*  534 */                 pList = new ArrayList<>();
/*      */               }
/*  536 */               Partition part = new Partition();
/*  537 */               part.setPartitionIndex(partitionIndex);
/*  538 */               part.setAlarmStatus(20);
/*  539 */               part.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(timestamp), this.timezone));
/*  540 */               part.setSource(zoneIndex);
/*  541 */               part.setAccount(account);
/*  542 */               pList.add(part);
/*      */             } 
/*      */ 
/*      */             
/*  546 */             Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient);
/*  547 */             event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, 100 + zoneStatus);
/*  548 */             event.setOccurred(Functions.convert2GMT(event.getOccurred(), this.timezone));
/*  549 */             event.setEventIndex(eventIndex);
/*  550 */             eList.add(event);
/*      */             
/*  552 */             if (zoneStatus == 5 || zoneStatus == 12 || zoneStatus == 13) {
/*      */               continue;
/*      */             }
/*      */             
/*  556 */             if (zoneStatus < 7 || zoneStatus > 11) {
/*  557 */               zoneStatus = (zoneStatus == 1 && evntQulifier == 3) ? 51 : zoneStatus;
/*  558 */               zoneStatus = (zoneStatus == 13 && evntQulifier == 3) ? 51 : zoneStatus;
/*  559 */               zoneStatus = (zoneStatus == 6 && evntQulifier == 3) ? 52 : zoneStatus;
/*  560 */               if ((zoneStatus == 2 || zoneStatus == 3 || zoneStatus == 4) && evntQulifier == 3) {
/*      */                 
/*  562 */                 if ((fcon[idx++] & 0xFF) == 11) {
/*  563 */                   Zone zone = new Zone(zoneIndex, 50, evntQulifier);
/*  564 */                   zone.setOccurred(event.getOccurred());
/*  565 */                   zList.add(zone);
/*      */                 }  continue;
/*      */               } 
/*  568 */               if ((fcon[idx++] & 0xFF) != 417) {
/*  569 */                 Zone zone = new Zone(zoneIndex, zoneStatus, evntQulifier);
/*  570 */                 zone.setOccurred(event.getOccurred());
/*  571 */                 zList.add(zone);
/*      */               } 
/*      */             } 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 2:
/*  579 */             idx = 0;
/*  580 */             if (eList == null) {
/*  581 */               eList = new ArrayList<>();
/*      */             }
/*  583 */             if (pList == null) {
/*  584 */               pList = new ArrayList<>();
/*      */             }
/*  586 */             tmp2[1] = fcon[idx++];
/*  587 */             tmp2[0] = fcon[idx++];
/*  588 */             eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/*  589 */             tmp2[0] = fcon[idx++];
/*  590 */             tmp2[1] = fcon[idx++];
/*  591 */             account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/*  592 */             tmp4[0] = fcon[idx++];
/*  593 */             tmp4[1] = fcon[idx++];
/*  594 */             tmp4[2] = fcon[idx++];
/*  595 */             tmp4[3] = fcon[idx++];
/*  596 */             timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  597 */             evntQulifier = fcon[idx++] & 0xFF;
/*  598 */             tmp2[0] = fcon[idx++];
/*  599 */             tmp2[1] = fcon[idx++];
/*  600 */             rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  601 */             partitionStatus = (short)(fcon[idx++] & 0xFF);
/*  602 */             switch (partitionStatus) {
/*      */               case 1:
/*      */               case 2:
/*      */               case 4:
/*      */               case 5:
/*      */               case 6:
/*      */               case 7:
/*      */               case 8:
/*      */               case 9:
/*      */               case 16:
/*      */               case 17:
/*      */               case 18:
/*  614 */                 appDataReceived = true;
/*      */                 break;
/*      */             } 
/*  617 */             partition = String.format("%2s", new Object[] { String.valueOf(fcon[idx++] & 0xFF) }).replace(' ', '0');
/*  618 */             source = fcon[idx++] & 0xFF;
/*  619 */             tmp2[0] = fcon[idx++];
/*  620 */             tmp2[1] = fcon[idx++];
/*  621 */             zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  622 */             Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient);
/*  623 */             event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, 200 + partitionStatus);
/*  624 */             event.setOccurred(Functions.convert2GMT(event.getOccurred(), this.timezone));
/*  625 */             event.setEventIndex(eventIndex);
/*  626 */             eList.add(event);
/*  627 */             pno = Integer.parseInt(partition, 10);
/*  628 */             if (pno > 0 && (partitionStatus < 10 || partitionStatus > 15)) {
/*  629 */               Partition part; if (partitionStatus >= 16 && partitionStatus <= 18) {
/*  630 */                 part = new Partition();
/*  631 */                 part.setPartitionIndex(pno);
/*  632 */                 part.setAlarmStatus(partitionStatus);
/*  633 */                 part.setUserIndex(zoneCode);
/*  634 */                 part.setSource(source);
/*      */               } else {
/*  636 */                 part = new Partition(pno, (partitionStatus == 2) ? 1 : partitionStatus, zoneCode, source);
/*      */               } 
/*  638 */               part.setOccurred(event.getOccurred());
/*  639 */               part.setAccount(account);
/*  640 */               pList.add(part);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 3:
/*  645 */             pendingAlive = true;
/*  646 */             idx = 0;
/*  647 */             if (eList == null) {
/*  648 */               eList = new ArrayList<>();
/*      */             }
/*  650 */             tmp2[1] = fcon[idx++];
/*  651 */             tmp2[0] = fcon[idx++];
/*  652 */             eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/*  653 */             tmp2[0] = fcon[idx++];
/*  654 */             tmp2[1] = fcon[idx++];
/*  655 */             account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/*  656 */             tmp4[0] = fcon[idx++];
/*  657 */             tmp4[1] = fcon[idx++];
/*  658 */             tmp4[2] = fcon[idx++];
/*  659 */             tmp4[3] = fcon[idx++];
/*  660 */             timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  661 */             evntQulifier = fcon[idx++] & 0xFF;
/*  662 */             tmp2[0] = fcon[idx++];
/*  663 */             tmp2[1] = fcon[idx++];
/*  664 */             rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  665 */             sysVal1 = fcon[idx++] & 0xFF;
/*  666 */             partition = "00";
/*  667 */             zoneCode = "000";
/*  668 */             switch (sysVal1) {
/*      */               case 1:
/*  670 */                 appDataReceived = true;
/*  671 */                 zoneCode = "000";
/*  672 */                 sysVal2 = fcon[idx++] & 0xFF;
/*  673 */                 switch (sysVal2) {
/*      */                   case 7:
/*  675 */                     eModule = new ExpansionModule();
/*  676 */                     if (evntQulifier == 1) {
/*  677 */                       eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.COMMMUNICATION_TROUBLE.getStatus());
/*  678 */                       eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*  679 */                     } else if (evntQulifier == 3) {
/*  680 */                       eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.NORMAL_OPERATION.getStatus());
/*  681 */                       eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*      */                     } 
/*  683 */                     if (emList == null) {
/*  684 */                       emList = new ArrayList<>();
/*      */                     }
/*  686 */                     emList.add(eModule);
/*  687 */                     zoneCode = String.format("%3s", new Object[] { String.valueOf(fcon[13] & 0xFF) }).replace(' ', '0');
/*      */                     break;
/*      */                   case 8:
/*  690 */                     eModule = new ExpansionModule();
/*  691 */                     if (evntQulifier == 1) {
/*  692 */                       if ((fcon[13] & 0xFF) > 0) {
/*  693 */                         eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.TAMPERED.getStatus());
/*  694 */                         eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*      */                       }
/*      */                     
/*  697 */                     } else if (evntQulifier == 3 && (
/*  698 */                       fcon[13] & 0xFF) > 0) {
/*  699 */                       eModule.setEmStatus(GriffonEnums.EnumPartitionStatus.NORMAL_OPERATION.getStatus());
/*  700 */                       eModule.setEmIndex(Functions.getIntFromHexByte(fcon[13]));
/*      */                     } 
/*      */ 
/*      */                     
/*  704 */                     if (emList == null) {
/*  705 */                       emList = new ArrayList<>();
/*      */                     }
/*  707 */                     emList.add(eModule);
/*  708 */                     zoneCode = String.format("%3s", new Object[] { String.valueOf(fcon[13] & 0xFF) }).replace(' ', '0');
/*      */                     break;
/*      */                 } 
/*      */                 break;
/*      */               case 2:
/*      */               case 3:
/*      */               case 4:
/*      */               case 5:
/*  716 */                 tmp2[0] = fcon[idx++];
/*  717 */                 tmp2[1] = fcon[idx++];
/*  718 */                 zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*      */                 break;
/*      */               case 6:
/*      */               case 7:
/*  722 */                 zoneCode = "000";
/*      */                 break;
/*      */               case 8:
/*      */               case 9:
/*  726 */                 tmp2[0] = fcon[idx++];
/*  727 */                 tmp2[1] = fcon[idx++];
/*  728 */                 zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  729 */                 if (Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2)) == 0) {
/*  730 */                   zoneCode = "000";
/*      */                 }
/*      */                 break;
/*      */               case 10:
/*  734 */                 tmp2[0] = fcon[idx++];
/*  735 */                 tmp2[1] = fcon[idx++];
/*  736 */                 zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*      */                 break;
/*      */               case 11:
/*      */               case 12:
/*      */               case 13:
/*      */               case 14:
/*      */               case 15:
/*      */               case 16:
/*      */               case 17:
/*      */               case 18:
/*      */               case 19:
/*      */               case 20:
/*      */               case 21:
/*      */               case 22:
/*      */               case 24:
/*      */               case 25:
/*      */               case 26:
/*      */               case 29:
/*  754 */                 zoneCode = String.format("%3s", new Object[] { String.valueOf(fcon[13] & 0xFF) }).replace(' ', '0');
/*      */                 break;
/*      */               case 23:
/*  757 */                 sysVal2 = fcon[idx++] & 0xFF;
/*  758 */                 sysVal3 = fcon[idx++] & 0xFF;
/*  759 */                 if (sysVal2 == 1) {
/*  760 */                   eType = 350;
/*  761 */                   zoneCode = String.format("%3s", new Object[] { String.valueOf(sysVal3) }).replace(' ', '0'); break;
/*  762 */                 }  if (sysVal2 == 2) {
/*  763 */                   eType = 351;
/*  764 */                   zoneCode = "000";
/*      */                 } 
/*      */                 break;
/*      */               case 27:
/*  768 */                 sysVal2 = fcon[idx++] & 0xFF;
/*  769 */                 if (sysVal2 == 1) {
/*  770 */                   eType = 352;
/*  771 */                 } else if (sysVal2 == 2) {
/*  772 */                   eType = 353;
/*      */                 } 
/*  774 */                 zoneCode = "001";
/*      */                 break;
/*      */               case 28:
/*  777 */                 sysVal2 = fcon[idx++] & 0xFF;
/*  778 */                 if (sysVal2 == 1) {
/*  779 */                   eType = 354;
/*  780 */                 } else if (sysVal2 == 2) {
/*  781 */                   eType = 355;
/*      */                 } 
/*  783 */                 zoneCode = "002";
/*      */                 break;
/*      */             } 
/*  786 */             Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient);
/*  787 */             event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, (sysVal1 == 23 || sysVal1 == 27 || sysVal1 == 28) ? eType : ((sysVal1 == 1) ? (3000 + sysVal2) : (300 + sysVal1)));
/*  788 */             event.setOccurred(Functions.convert2GMT(event.getOccurred(), this.timezone));
/*  789 */             event.setEventIndex(eventIndex);
/*  790 */             eList.add(event);
/*      */ 
/*      */           
/*      */           case 4:
/*  794 */             idx = 0;
/*  795 */             if (eList == null) {
/*  796 */               eList = new ArrayList<>();
/*      */             }
/*  798 */             tmp2[1] = fcon[idx++];
/*  799 */             tmp2[0] = fcon[idx++];
/*  800 */             eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/*  801 */             tmp2[0] = fcon[idx++];
/*  802 */             tmp2[1] = fcon[idx++];
/*  803 */             account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/*  804 */             tmp4[0] = fcon[idx++];
/*  805 */             tmp4[1] = fcon[idx++];
/*  806 */             tmp4[2] = fcon[idx++];
/*  807 */             tmp4[3] = fcon[idx++];
/*  808 */             timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  809 */             evntQulifier = fcon[idx++] & 0xFF;
/*  810 */             tmp2[0] = fcon[idx++];
/*  811 */             tmp2[1] = fcon[idx++];
/*  812 */             rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  813 */             eventType = (short)(fcon[idx++] & 0xFF);
/*  814 */             switch (eventType) {
/*      */               case 1:
/*      */               case 2:
/*      */               case 3:
/*      */               case 4:
/*      */               case 5:
/*      */               case 6:
/*      */               case 7:
/*  822 */                 partition = "00";
/*  823 */                 zoneCode = "000";
/*      */                 break;
/*      */               case 8:
/*  826 */                 tmp = fcon[idx++] & 0xFF;
/*  827 */                 partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/*  828 */                 zoneCode = "000";
/*      */                 break;
/*      */               case 9:
/*  831 */                 tmp = fcon[idx++] & 0xFF;
/*  832 */                 partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/*  833 */                 zoneCode = "000";
/*      */                 break;
/*      */               case 10:
/*      */               case 11:
/*  837 */                 tmp = fcon[idx++] & 0xFF;
/*  838 */                 partition = String.format("%2s", new Object[] { String.valueOf(tmp) }).replace(' ', '0');
/*  839 */                 tmp2[0] = fcon[idx++];
/*  840 */                 tmp2[1] = fcon[idx++];
/*  841 */                 zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*      */                 break;
/*      */               case 12:
/*  844 */                 if (pgmList == null) {
/*  845 */                   pgmList = new ArrayList<>();
/*      */                 }
/*  847 */                 analogPGMIndex = GriffonDBManager.getAnalogPGMIndexByModuleID(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*  848 */                 pgm = new PGM();
/*  849 */                 partition = "00";
/*  850 */                 tmp = fcon[idx++] & 0xFF;
/*  851 */                 pgm.setPgmIndex(tmp);
/*  852 */                 tmp = fcon[fcon.length - 1];
/*  853 */                 if (analogPGMIndex != null && analogPGMIndex.contains(Integer.valueOf(pgm.getPgmIndex()))) {
/*  854 */                   switch (tmp) {
/*      */                     case 0:
/*  856 */                       tmp = (evntQulifier == 1) ? 1 : 0;
/*      */                       break;
/*      */                     case 1:
/*  859 */                       tmp = (evntQulifier == 1) ? 3 : 0;
/*      */                       break;
/*      */                     case 2:
/*  862 */                       tmp = (evntQulifier == 1) ? 4 : 0;
/*      */                       break;
/*      */                   } 
/*      */                 } else {
/*  866 */                   switch (tmp) {
/*      */                     case 0:
/*  868 */                       tmp = (evntQulifier == 1) ? 5 : 0;
/*      */                       break;
/*      */                     case 1:
/*  871 */                       tmp = (evntQulifier == 1) ? 3 : 1;
/*      */                       break;
/*      */                     case 2:
/*  874 */                       tmp = (evntQulifier == 1) ? 4 : 2;
/*      */                       break;
/*      */                   } 
/*      */                 } 
/*  878 */                 pgm.setPgmStatus(tmp);
/*  879 */                 pgm.setOccurred(Functions.convert2GMT(Functions.getDateFromInt(timestamp), this.timezone));
/*  880 */                 pgmList.add(pgm);
/*  881 */                 zoneCode = String.format("%3s", new Object[] { String.valueOf(pgm.getPgmIndex()) }).replace(' ', '0');
/*  882 */                 appDataReceived = true;
/*      */                 break;
/*      */             } 
/*  885 */             Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient);
/*  886 */             event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, 400 + eventType);
/*  887 */             event.setOccurred(Functions.convert2GMT(event.getOccurred(), this.timezone));
/*  888 */             event.setEventIndex(eventIndex);
/*  889 */             eList.add(event);
/*      */ 
/*      */           
/*      */           case 5:
/*  893 */             idx = 0;
/*  894 */             if (eList == null) {
/*  895 */               eList = new ArrayList<>();
/*      */             }
/*  897 */             if (acList == null) {
/*  898 */               acList = new ArrayList<>();
/*      */             }
/*  900 */             tmp2[1] = fcon[idx++];
/*  901 */             tmp2[0] = fcon[idx++];
/*  902 */             eventIndex = Functions.getIntFrom2ByteArray(tmp2);
/*  903 */             tmp2[0] = fcon[idx++];
/*  904 */             tmp2[1] = fcon[idx++];
/*  905 */             account = String.format("%4s", new Object[] { Functions.getHexStringFromByteArray(tmp2) }).replace(' ', '0');
/*  906 */             tmp4[0] = fcon[idx++];
/*  907 */             tmp4[1] = fcon[idx++];
/*  908 */             tmp4[2] = fcon[idx++];
/*  909 */             tmp4[3] = fcon[idx++];
/*  910 */             timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  911 */             evntQulifier = fcon[idx++] & 0xFF;
/*  912 */             tmp2[0] = fcon[idx++];
/*  913 */             tmp2[1] = fcon[idx++];
/*  914 */             rptCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  915 */             tmp2[0] = fcon[idx++];
/*  916 */             emIndex = tmp2[0] & 0xFF;
/*  917 */             tmp2 = Functions.getHighLowBytes(fcon[idx++] & 0xFF);
/*  918 */             access_iface = tmp2[0] & 0xFF;
/*  919 */             readerType = tmp2[1] & 0xFF;
/*      */             
/*  921 */             tmp2 = Functions.getHighLowBytes(fcon[idx++] & 0xFF);
/*  922 */             tmp = GriffonEnums.EnumAccessDescription.INVALID.getDescription();
/*  923 */             if ((tmp2[0] & 0xFF) == 0) {
/*  924 */               tmp = GriffonEnums.EnumAccessDescription.INVALID.getDescription();
/*  925 */             } else if ((tmp2[0] & 0xFF) == 1) {
/*  926 */               tmp = GriffonEnums.EnumAccessDescription.ACCESS_GRANTED.getDescription();
/*  927 */             } else if ((tmp2[0] & 0xFF) == 2) {
/*  928 */               tmp = GriffonEnums.EnumAccessDescription.EGRESS_GRANTED.getDescription();
/*  929 */             } else if ((tmp2[0] & 0xFF) == 3) {
/*  930 */               if ((tmp2[1] & 0xFF) == 0) {
/*  931 */                 tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_INVALID.getDescription();
/*  932 */               } else if ((tmp2[1] & 0xFF) == 1) {
/*  933 */                 tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_ANTI_PASSBACK.getDescription();
/*  934 */               } else if ((tmp2[1] & 0xFF) == 2) {
/*  935 */                 tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_TIME_NOT_MATCHED.getDescription();
/*  936 */               } else if ((tmp2[1] & 0xFF) == 3) {
/*  937 */                 tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_EB_INDEX_NOT_MATCHED.getDescription();
/*  938 */               } else if ((tmp2[1] & 0xFF) == 4) {
/*  939 */                 tmp = GriffonEnums.EnumAccessDescription.ACCESS_DENIED_INTERFACE_TYPE_NOT_MATCHED.getDescription();
/*      */               } 
/*  941 */             } else if ((tmp2[0] & 0xFF) == 4) {
/*  942 */               if ((tmp2[1] & 0xFF) == 0) {
/*  943 */                 tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_INVALID.getDescription();
/*  944 */               } else if ((tmp2[1] & 0xFF) == 1) {
/*  945 */                 tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_ANTI_PASSBACK.getDescription();
/*  946 */               } else if ((tmp2[1] & 0xFF) == 2) {
/*  947 */                 tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_TIME_NOT_MATCHED.getDescription();
/*  948 */               } else if ((tmp2[1] & 0xFF) == 3) {
/*  949 */                 tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_EB_INDEX_NOT_MATCHED.getDescription();
/*  950 */               } else if ((tmp2[1] & 0xFF) == 4) {
/*  951 */                 tmp = GriffonEnums.EnumAccessDescription.EGRESS_DENIED_INTERFACE_TYPE_NOT_MATCHED.getDescription();
/*      */               } 
/*  953 */             } else if ((tmp2[0] & 0xFF) == 6) {
/*  954 */               tmp = GriffonEnums.EnumAccessDescription.ACCESS_EGRESS_GRANTED_UNDER_DURESS.getDescription();
/*      */             } 
/*      */             
/*  957 */             tmp2[0] = fcon[idx++];
/*  958 */             tmp2[1] = fcon[idx++];
/*  959 */             zoneCode = String.format("%3s", new Object[] { String.valueOf(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2))) }).replace(' ', '0');
/*  960 */             partition = "00";
/*  961 */             Functions.generateEvent(2, account, evntQulifier, rptCode, partition, zoneCode, this.idGroup, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient);
/*  962 */             event = new Event(Functions.getDateFromInt(timestamp), new Date(), account, partition, rptCode, zoneCode, evntQulifier, tmp);
/*  963 */             event.setOccurred(Functions.convert2GMT(event.getOccurred(), this.timezone));
/*  964 */             event.setEventIndex(eventIndex);
/*  965 */             eList.add(event);
/*  966 */             access = new Access(event.getOccurred(), new Date(), account, rptCode, emIndex, access_iface, readerType, zoneCode, evntQulifier, tmp);
/*  967 */             acList.add(access);
/*      */ 
/*      */           
/*      */           case 80:
/*  971 */             idx = 0;
/*  972 */             sb = new StringBuilder();
/*  973 */             for (i = 0; i < flen; i++) {
/*  974 */               sb.append(String.format(" %x ", new Object[] { Integer.valueOf(fcon[i] & 0xFF) }));
/*      */             } 
/*  976 */             tmp2[0] = fcon[idx++];
/*  977 */             tmp2[1] = fcon[idx++];
/*  978 */             account = Functions.getHexStringFromByteArray(tmp2);
/*  979 */             account = String.format("%4s", new Object[] { account }).replace(' ', '0');
/*  980 */             tmp4[0] = fcon[idx++];
/*  981 */             tmp4[1] = fcon[idx++];
/*  982 */             tmp4[2] = fcon[idx++];
/*  983 */             tmp4[3] = fcon[idx++];
/*  984 */             timestamp = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  985 */             tmp2[0] = fcon[idx++];
/*  986 */             tmp2[1] = fcon[idx++];
/*  987 */             this.module.setProductId(Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2)));
/*  988 */             moduleSN = new byte[flen - idx];
/*  989 */             System.arraycopy(fcon, idx, moduleSN, 0, flen - idx);
/*  990 */             this.module.setSn(Functions.getASCIIFromByteArray(moduleSN));
/*  991 */             this.module.setClientCode(account);
/*  992 */             this.sn = this.module.getSn();
/*  993 */             if (this.sn.equals("0000000000")) {
/*  994 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  995 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  996 */               return false;
/*  997 */             }  if (this.sn.equals("0000000001")) {
/*  998 */               String ip = this.remoteIP.substring(0, this.remoteIP.indexOf(":"));
/*  999 */               if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 1000 */                 if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/* 1001 */                   Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/* 1002 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 1003 */                   return false;
/*      */                 } 
/* 1005 */                 this.module.setProductId(21);
/*      */                 continue;
/*      */               } 
/* 1008 */               if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/* 1009 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/* 1010 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 1011 */                 return false;
/*      */               } 
/* 1013 */               this.module.setProductId(21);
/*      */             } 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 81:
/* 1020 */             sb = new StringBuilder();
/* 1021 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1]));
/* 1022 */             this.module.setModuleFWVersion(sb.toString());
/*      */ 
/*      */           
/*      */           case 82:
/* 1026 */             tmp2[0] = fcon[0];
/* 1027 */             tmp2[1] = fcon[1];
/* 1028 */             this.module.setModuleHWDtls(Functions.getIntFrom2ByteArray(tmp2));
/*      */ 
/*      */           
/*      */           case 83:
/* 1032 */             pendingAlive = true;
/* 1033 */             this.module.setBatteryStatus(fcon[0] & 0xFF);
/* 1034 */             this.module.setCurrentBatteryVoltage((new Float((fcon[1] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[2] & 0xFF) }))).floatValue());
/* 1035 */             this.module.setBatteryInputCurrent(Float.parseFloat((fcon[3] & 0xFF) + "." + String.format("%02d", new Object[] { Integer.valueOf(fcon[4] & 0xFF) })));
/*      */ 
/*      */           
/*      */           case 84:
/* 1039 */             this.module.setCurrentInterface(fcon[0] & 0xFF);
/* 1040 */             this.lastCommIface = (short)this.module.getCurrentInterface();
/*      */ 
/*      */           
/*      */           case 86:
/* 1044 */             sb = new StringBuilder();
/* 1045 */             for (j = 0; j < flen - 1; j++) {
/* 1046 */               sb.append(Functions.getFormatIntFromHexByte(fcon[j]));
/*      */             }
/* 1048 */             sb.append(fcon[7] / 10);
/* 1049 */             this.module.setModemIMEI(sb.toString());
/*      */ 
/*      */           
/*      */           case 87:
/* 1053 */             this.module.setModemModel(fcon[0]);
/*      */ 
/*      */           
/*      */           case 88:
/* 1057 */             sb = new StringBuilder();
/* 1058 */             tmp2[0] = fcon[2];
/* 1059 */             tmp2[1] = fcon[3];
/* 1060 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFrom2ByteArray(tmp2));
/* 1061 */             this.module.setModemFWVersion(sb.toString());
/*      */ 
/*      */           
/*      */           case 89:
/* 1065 */             pendingAlive = true;
/* 1066 */             this.module.setGsmSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */           
/*      */           case 90:
/* 1070 */             pendingAlive = true;
/*      */ 
/*      */           
/*      */           case 95:
/* 1074 */             sb = new StringBuilder();
/* 1075 */             for (j = 1; j < flen; j++) {
/* 1076 */               sb.append(Functions.getFormatIntFromHexByte(fcon[j]));
/*      */             }
/* 1078 */             if (fcon[0] == 1) {
/* 1079 */               this.module.setSim_iccid_1(sb.toString()); continue;
/* 1080 */             }  if (fcon[0] == 2) {
/* 1081 */               this.module.setSim_iccid_2(sb.toString());
/*      */             }
/*      */ 
/*      */           
/*      */           case 96:
/* 1086 */             simCardOperator = new byte[flen - 1];
/* 1087 */             System.arraycopy(fcon, 1, simCardOperator, 0, flen - 1);
/* 1088 */             if (fcon[0] == 1) {
/* 1089 */               this.module.setSim_operator_1(Functions.getASCIIFromByteArray(simCardOperator)); continue;
/* 1090 */             }  if (fcon[0] == 2) {
/* 1091 */               this.module.setSim_operator_2(Functions.getASCIIFromByteArray(simCardOperator));
/*      */             }
/*      */ 
/*      */           
/*      */           case 94:
/* 1096 */             this.module.setCurrentSIM((short)Functions.getIntFromHexByte(fcon[0]));
/* 1097 */             this.module.setCurrentAPN((short)Functions.getIntFromHexByte(fcon[1]));
/*      */ 
/*      */           
/*      */           case 93:
/* 1101 */             pendingAlive = true;
/* 1102 */             this.module.setGprsDataVolume(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*      */ 
/*      */           
/*      */           case 92:
/* 1106 */             pendingAlive = true;
/* 1107 */             this.module.setOtaStatus((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */           
/*      */           case 97:
/* 1111 */             sb = new StringBuilder();
/* 1112 */             for (k = 1; k < flen - 1; k++) {
/* 1113 */               sb.append(Functions.getFormatIntFromHexByte(fcon[k]));
/*      */             }
/* 1115 */             sb.append(fcon[8] / 10);
/* 1116 */             if (fcon[0] == 1) {
/* 1117 */               this.module.setSim_imsi_1(sb.toString()); continue;
/* 1118 */             }  if (fcon[0] == 2) {
/* 1119 */               this.module.setSim_imsi_2(sb.toString());
/*      */             }
/*      */ 
/*      */           
/*      */           case 91:
/* 1124 */             pendingAlive = true;
/* 1125 */             this.module.setGsmJammerStatus((short)Functions.getIntFromHexByte(fcon[0]));
/* 1126 */             this.module.setGsmJDRStatus((short)Functions.getIntFromHexByte(fcon[1]));
/*      */ 
/*      */           
/*      */           case 98:
/* 1130 */             pendingAlive = true;
/*      */ 
/*      */           
/*      */           case 99:
/* 1134 */             pendingAlive = true;
/* 1135 */             tmp4[0] = fcon[0];
/* 1136 */             tmp4[1] = fcon[1];
/* 1137 */             tmp4[2] = fcon[2];
/* 1138 */             tmp4[3] = fcon[3];
/* 1139 */             dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1140 */             this.module.setLongitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */             
/* 1142 */             tmp4[0] = fcon[4];
/* 1143 */             tmp4[1] = fcon[5];
/* 1144 */             tmp4[2] = fcon[6];
/* 1145 */             tmp4[3] = fcon[7];
/* 1146 */             dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1147 */             this.module.setLattitude((float)(dValue / Math.pow(10.0D, 7.0D)));
/*      */             
/* 1149 */             tmp4[0] = fcon[8];
/* 1150 */             tmp4[1] = fcon[9];
/* 1151 */             tmp4[2] = fcon[10];
/* 1152 */             tmp4[3] = fcon[11];
/* 1153 */             dValue = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1154 */             this.module.setAltitude((float)(dValue / 100.0D));
/*      */ 
/*      */           
/*      */           case 100:
/* 1158 */             pendingAlive = true;
/*      */ 
/*      */           
/*      */           case 101:
/* 1162 */             pendingAlive = true;
/*      */ 
/*      */           
/*      */           case 102:
/* 1166 */             this.module.setWifiModel(fcon[0]);
/*      */ 
/*      */           
/*      */           case 103:
/* 1170 */             sb = new StringBuilder();
/* 1171 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1]));
/* 1172 */             this.module.setWifiFWVersion(sb.toString());
/*      */ 
/*      */           
/*      */           case 104:
/* 1176 */             this.module.setWifiAccessPoint(fcon[0]);
/*      */ 
/*      */           
/*      */           case 105:
/* 1180 */             pendingAlive = true;
/*      */ 
/*      */           
/*      */           case 106:
/* 1184 */             pendingAlive = true;
/* 1185 */             this.module.setWifiSignalLevel((short)Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */           
/*      */           case 107:
/* 1189 */             pendingAlive = true;
/* 1190 */             this.module.setTimeSync(true);
/* 1191 */             tmp2[0] = fcon[2];
/* 1192 */             tmp2[1] = fcon[1];
/* 1193 */             this.timezone = Functions.getSignedIntFrom2ByteArray(tmp2);
/* 1194 */             this.module.setTimezone(this.timezone);
/*      */ 
/*      */           
/*      */           case 109:
/* 1198 */             this.module.setUpdateStatusReceived(true);
/* 1199 */             this.module = GriffonHandlerHelper.handleDashboardBuffer(fcon, this.module, this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*      */ 
/*      */           
/*      */           case 110:
/* 1203 */             if (fcon[0] == 1) {
/* 1204 */               this.module.setEventUpload(1); continue;
/* 1205 */             }  if (fcon[0] == 2) {
/* 1206 */               this.module.setLogUpload(2);
/*      */             }
/*      */ 
/*      */           
/*      */           case 108:
/* 1211 */             this.deviceCRC32 = null;
/* 1212 */             this.deviceCRC32 = GriffonHandlerHelper.buildCRC32FromReceivedBuffer(this.productID, this.deviceCRC32, fcon, true, 0);
/* 1213 */             this.requestedAllFilesCRC32 = true;
/*      */ 
/*      */           
/*      */           case 85:
/* 1217 */             this.deviceCRC32 = null;
/* 1218 */             tmp4[0] = fcon[3];
/* 1219 */             tmp4[1] = fcon[2];
/* 1220 */             tmp4[2] = fcon[1];
/* 1221 */             tmp4[3] = fcon[0];
/* 1222 */             this.module.setCrc32(Functions.getIntFrom4ByteArray(tmp4));
/* 1223 */             this.module.setInitialPacket(true);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 116:
/* 1230 */             tmp4[0] = fcon[3];
/* 1231 */             tmp4[1] = fcon[2];
/* 1232 */             tmp4[2] = fcon[1];
/* 1233 */             tmp4[3] = fcon[0];
/* 1234 */             this.module.setEbFWCRC32(Functions.getIntFrom4ByteArray(tmp4));
/* 1235 */             this.module.setEbFWCRC32Received(true);
/*      */ 
/*      */           
/*      */           case 111:
/* 1239 */             if (zList == null) {
/* 1240 */               zList = new ArrayList<>();
/*      */             }
/* 1242 */             GriffonHandlerHelper.handleZoneStatusBuffer(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, zList, fcon, false);
/* 1243 */             appDataReceived = true;
/*      */ 
/*      */ 
/*      */           
/*      */           case 112:
/* 1248 */             if (pgmList == null) {
/* 1249 */               pgmList = new ArrayList<>();
/*      */             }
/* 1251 */             tmp = 0;
/* 1252 */             numRepPgms = (short)(fcon[tmp++] & 0xFF);
/* 1253 */             for (numPgm = 0; numPgm < numRepPgms; numPgm = (short)(numPgm + 1)) {
/* 1254 */               pgm = new PGM();
/* 1255 */               pgm.setPgmIndex(fcon[tmp++] & 0xFF);
/* 1256 */               pgm.setPgmType(2);
/* 1257 */               pgm.setAnalogValue(fcon[tmp++] & 0xFF);
/* 1258 */               pgm.setPgmStatus(GriffonHandlerHelper.getPGMStatusFromDashBoardBuffer(fcon[tmp++], true));
/* 1259 */               pgmList.add(pgm);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 113:
/* 1264 */             pendingAlive = true;
/* 1265 */             this.module.setAuxOutputStatus((short)fcon[0]);
/* 1266 */             this.module.setAuxVoltage(Float.parseFloat(fcon[1] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[2]) })));
/* 1267 */             this.module.setAuxCurrent(Float.parseFloat(fcon[3] + "." + String.format("%02d", new Object[] { Byte.valueOf(fcon[4]) })));
/*      */ 
/*      */           
/*      */           case 114:
/* 1271 */             if (emList == null) {
/* 1272 */               emList = new ArrayList<>();
/*      */             }
/* 1274 */             GriffonHandlerHelper.handleEBBuffer(emList, fcon);
/*      */ 
/*      */           
/*      */           case 115:
/* 1278 */             pendingAlive = true;
/* 1279 */             this.module.setTemparature((fcon[0] & 0xFF) - 40);
/*      */ 
/*      */           
/*      */           case 118:
/* 1283 */             appDataReceived = true;
/* 1284 */             this.digitalPGMBufferReceived = true;
/* 1285 */             if (pgmList == null) {
/* 1286 */               pgmList = new ArrayList<>();
/*      */             }
/* 1288 */             pgmList.addAll(GriffonHandlerHelper.getPGMStatusFromDigitalBuffer(fcon));
/*      */ 
/*      */           
/*      */           case 120:
/* 1292 */             tmp4[0] = fcon[3];
/* 1293 */             tmp4[1] = fcon[2];
/* 1294 */             tmp4[2] = fcon[1];
/* 1295 */             tmp4[3] = fcon[0];
/* 1296 */             this.module.setRecAudioLookupCRC32(Functions.getIntFrom4ByteArray(tmp4));
/* 1297 */             this.module.setRecAudioLookupCRC32Received(true);
/*      */         } 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*      */       } 
/* 1304 */       this.module.setAcList(acList);
/* 1305 */       this.module.seteList(eList);
/* 1306 */       this.module.setEmList(emList);
/* 1307 */       this.module.setpList(pList);
/* 1308 */       this.module.setzList(zList);
/* 1309 */       this.module.setPgmList(pgmList);
/*      */       
/* 1311 */       if (!pendingAlive) {
/* 1312 */         this.module.setM2sData(null);
/*      */       }
/*      */       
/* 1315 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.sn + LocaleMessage.getLocaleMessage("]_M2S_packet_received") + Functions.getGriffonIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/* 1316 */       this.module.setAutoRegistration((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/* 1317 */       this.module.setIp(this.remoteIP.substring(0, this.remoteIP.indexOf(":")));
/*      */       try {
/* 1319 */         TblGriffonActiveConnections.semaphoreAlivePacketsReceived.acquire();
/* 1320 */         this.module = GriffonDBManager.executeSPG_001(this.module);
/*      */       } finally {
/* 1322 */         TblGriffonActiveConnections.semaphoreAlivePacketsReceived.release();
/*      */       } 
/* 1324 */       if (this.module != null) {
/* 1325 */         if (this.module.getAuto_Registration_Executed() == 1) {
/* 1326 */           if (this.module.getRegistered() == 0) {
/* 1327 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + this.sn, Enums.EnumMessagePriority.HIGH, null, null);
/* 1328 */             SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/* 1329 */             return false;
/*      */           } 
/* 1331 */         } else if (this.module.getRegistered() == 1) {
/* 1332 */           if (this.module.getEnabled() == 0) {
/* 1333 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.sn + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1334 */             SocketFunctions.send(this.clientSocket, new byte[] { -31 });
/* 1335 */             return false;
/*      */           } 
/*      */         } else {
/* 1338 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.sn + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 1339 */           SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/* 1340 */           return false;
/*      */         } 
/* 1342 */         Functions.generateEventReceptionAlivePacket(2, this.module.getId_Client(), this.module.getId_Module(), this.module.getId_Group(), this.module.getClientCode(), this.module.getE_Alive_Received(), this.module.getF_Alive_Received(), this.module.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.TCP.name(), this.lastCommIface, 611);
/* 1343 */         if (this.initialMsgFlag) {
/* 1344 */           if (TblGriffonActiveConnections.getInstance().containsKey(this.sn)) {
/* 1345 */             TblGriffonActiveConnections.removeConnection(this.sn);
/*      */           }
/* 1347 */           TblGriffonActiveConnections.addConnection(this.sn, this.myThreadGuid);
/* 1348 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient = this.module.getId_Client();
/* 1349 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule = this.module.getId_Module();
/*      */         } 
/* 1351 */         ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idGroup = this.module.getId_Group();
/* 1352 */         ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).clientName = this.module.getName();
/* 1353 */         ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).communicationDebug = (this.module.getCommDebug() == 1);
/* 1354 */         ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).communicationTimeout = this.module.getComm_Timeout() * 1000;
/* 1355 */         this.idleTimeout = System.currentTimeMillis() + (this.module.getComm_Timeout() * 1000);
/*      */         
/* 1357 */         if (this.module.getCommLog() == 1 && this.module.getCommLogEnabledDate() != null) {
/* 1358 */           Calendar sys = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/* 1359 */           if (sys.get(5) - this.module.getCommLogEnabledDate().get(5) > 30) {
/* 1360 */             GriffonDBManager.disableCommunicationLog(this.module.getId_Module());
/* 1361 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).commLog = false;
/* 1362 */             if (this.ownLogger != null) {
/* 1363 */               this.ownLogger = null;
/*      */             }
/*      */           } else {
/* 1366 */             ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).commLog = true;
/* 1367 */             if (this.ownLogger == null && this.sn != null) {
/* 1368 */               this.ownLogger = ZeusServerLogger.getDeviceLogger("Griffon/", this.sn);
/*      */             }
/* 1370 */             if (this.ownLogger != null) {
/* 1371 */               Functions.logGriffonIncomingPacket(this.ownLogger, decData);
/*      */             }
/*      */           } 
/*      */         } else {
/* 1375 */           ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).commLog = false;
/* 1376 */           if (this.ownLogger != null) {
/* 1377 */             this.ownLogger = null;
/*      */           }
/*      */         } 
/*      */         
/* 1381 */         if (this.module.isRecAudioLookupCRC32Received() && !this.module.isRecAudioLookupCRC32NotMacthed() && 
/* 1382 */           !this.recordedLookupRequest) {
/* 1383 */           this.recordedLookupRequest = true;
/* 1384 */           this.recAudioLookupCRC32 = this.module.getRecAudioLookupCRC32();
/*      */         } 
/*      */ 
/*      */         
/*      */         try {
/* 1389 */           if (this.module.isInitialPacket() && !this.module.isCrc32Matched()) {
/* 1390 */             SocketFunctions.send(this.clientSocket, new byte[] { 8 });
/* 1391 */             Thread.sleep(0L);
/* 1392 */             if (this.last_80_Sent + 30000L < System.currentTimeMillis()) {
/* 1393 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Sending 0x80 For Configuration Sync on First Packet", Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1394 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { Byte.MIN_VALUE });
/* 1395 */               this.last_80_Sent = System.currentTimeMillis();
/* 1396 */               this.isSync = true;
/*      */             } 
/*      */           } else {
/* 1399 */             SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1400 */             if (this.module.isEbFWCRC32Received() && !this.module.isIsEBCrc32Matched()) {
/* 1401 */               Thread.sleep(0L);
/* 1402 */               if (this.last_80_Sent + 30000L < System.currentTimeMillis()) {
/* 1403 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Sending 0x80 For Exp Board FW CRC-32 Packet Received", Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1404 */                 SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { Byte.MIN_VALUE });
/* 1405 */                 this.last_80_Sent = System.currentTimeMillis();
/*      */               } 
/* 1407 */               this.ebFWRequested = true;
/* 1408 */               if (this.module.isInitialPacket()) {
/* 1409 */                 this.ebFWCRC32MismatchOnInitialPacket = true;
/*      */               }
/* 1411 */             } else if (this.module.isInitialPacket() && this.module.isCrc32Matched()) {
/* 1412 */               Thread.sleep(0L);
/* 1413 */               if (this.last_80_Sent + 30000L < System.currentTimeMillis()) {
/* 1414 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Sending 0x80 For DashBoard on First Packet", Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1415 */                 SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { Byte.MIN_VALUE });
/* 1416 */                 this.last_80_Sent = System.currentTimeMillis();
/*      */               } 
/* 1418 */               this.crc32MatchedNUpdateStatusNotSent = true;
/* 1419 */             } else if (this.recordedLookupRequest) {
/* 1420 */               Thread.sleep(0L);
/* 1421 */               if (this.last_80_Sent + 30000L < System.currentTimeMillis()) {
/* 1422 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Sending 0x80 For Recorded Audio Files Packet Received" + this.recordedLookupRequest, Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1423 */                 SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { Byte.MIN_VALUE });
/* 1424 */                 this.last_80_Sent = System.currentTimeMillis();
/*      */               } 
/*      */             } 
/*      */           } 
/* 1428 */           if (appDataReceived && 
/* 1429 */             !GriffonHandlerHelper.pushAppDataReceived(this.module.getId_Client()) && 
/* 1430 */             this.digitalPGMBufferReceived) {
/* 1431 */             this.disableDigitalPGMBuffer = true;
/*      */           }
/*      */ 
/*      */ 
/*      */           
/* 1436 */           if (this.disableDigitalPGMBuffer) {
/* 1437 */             Thread.sleep(0L);
/* 1438 */             if (this.last_80_Sent + 30000L < System.currentTimeMillis()) {
/* 1439 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "Sending 0x80 For Enable Disable PGM Buffer Packet Received", Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1440 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { Byte.MIN_VALUE });
/* 1441 */               this.last_80_Sent = System.currentTimeMillis();
/*      */             } 
/*      */           } 
/* 1444 */           return true;
/* 1445 */         } catch (IOException|InterruptedException ex) {
/* 1446 */           ex.printStackTrace();
/* 1447 */           return false;
/*      */         } 
/*      */       } 
/* 1450 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Error_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/* 1451 */       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 1452 */       return false;
/*      */     }
/* 1454 */     catch (Exception ex) {
/* 1455 */       ex.printStackTrace();
/* 1456 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */       try {
/* 1458 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 1459 */       } catch (IOException iOException) {}
/*      */       
/* 1461 */       return false;
/*      */     } finally {
/* 1463 */       TblGriffonActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean processCommandPacket() {
/* 1468 */     SP_024DataHolder sp24DH = null;
/*      */     try {
/* 1470 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1471 */       List<SP_024DataHolder> cmdsList = GriffonDBManager.executeSP_024(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/* 1472 */       int count = 0;
/* 1473 */       int cmdsSize = cmdsList.size();
/*      */       
/* 1475 */       while (isSocketConnected() && count < cmdsSize) {
/* 1476 */         sp24DH = cmdsList.get(count++);
/* 1477 */         if (GriffonDBManager.isCommandCancelled(sp24DH.getId_Command())) {
/*      */           continue;
/*      */         }
/* 1480 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Sending_command") + sp24DH.getCommand_Type() + ":" + sp24DH.getCommandData(), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1481 */         GriffonDBManager.updateCommandStatus(sp24DH.getId_Command(), 1);
/* 1482 */         byte[] fileIDData = null;
/* 1483 */         ModuleCFG mCFG = null;
/* 1484 */         if ((sp24DH.getCommand_Type() == 32774 && sp24DH.getCommandData().charAt(0) == '1') || (sp24DH.getCommand_Type() == 32774 && sp24DH.getCommandData().charAt(0) == '5') || (sp24DH.getCommand_Type() == 32773 && sp24DH.getCommandData().charAt(0) == '1')) {
/* 1485 */           mCFG = (sp24DH.getCommand_Type() == 32774) ? GriffonDBManager.readGriffonModuleCfg(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule) : GriffonHandlerHelper.getModuleCFGFromUploadedFile(this.productID, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, sp24DH.getCommandFileData());
/* 1486 */           fileIDData = findFileIDsByCRCMismatch(mCFG, (sp24DH.getCommand_Type() == 32774));
/* 1487 */           if (fileIDData == null) {
/* 1488 */             if (sp24DH.getCommand_Type() == 32774) {
/* 1489 */               List<VoiceMessage> vmList = null;
/* 1490 */               if (sp24DH.getCommandData().charAt(0) == '5') {
/* 1491 */                 vmList = GriffonDBManager.getVoiceMessageInfoByIdModule(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/* 1492 */                 if (vmList != null) {
/* 1493 */                   List<VoiceMessage> requriedVMList = GriffonDBManager.getMissingVoiceMessagesInfo(vmList);
/* 1494 */                   if (requriedVMList != null && !requriedVMList.isEmpty()) {
/* 1495 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Zeus_identified_missing_or_added_voice_messages"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1496 */                     readVoiceMessages(requriedVMList);
/*      */                   } 
/*      */                 } 
/*      */               } 
/* 1500 */               GriffonHandlerHelper.finalizeReceiveCFGFileCommand(sp24DH.getId_Command(), ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, mCFG, vmList);
/*      */             } 
/* 1502 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1503 */             GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); continue;
/*      */           } 
/* 1505 */           if (sp24DH.getCommand_Type() == 32773) {
/* 1506 */             File file = null;
/*      */             try {
/* 1508 */               for (int i = 1; i < fileIDData.length; i++) {
/* 1509 */                 if (fileIDData[i] == 21) {
/* 1510 */                   List<VoiceMessage> reqVMList = GriffonHandlerHelper.getMismatchedVoiceMessageList(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, mCFG);
/*      */                   
/* 1512 */                   if (reqVMList != null && reqVMList.size() > 0) {
/* 1513 */                     file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/* 1514 */                     RandomAccessFile raf = new RandomAccessFile(file, "r");
/* 1515 */                     for (VoiceMessage vm : reqVMList) {
/* 1516 */                       int fpos = 0;
/* 1517 */                       while (fpos < file.length()) {
/* 1518 */                         raf.seek(fpos);
/* 1519 */                         byte[] vmHeader = new byte[22];
/* 1520 */                         raf.read(vmHeader);
/* 1521 */                         fpos += 22;
/* 1522 */                         byte[] tm = new byte[12];
/* 1523 */                         System.arraycopy(vmHeader, 2, tm, 0, 12);
/*      */                         
/* 1525 */                         byte[] tmlen = new byte[4];
/* 1526 */                         tmlen[3] = vmHeader[14];
/* 1527 */                         tmlen[2] = vmHeader[15];
/* 1528 */                         tmlen[1] = vmHeader[16];
/* 1529 */                         tmlen[0] = vmHeader[17];
/* 1530 */                         int vmLen = Functions.getIntFrom4ByteArray(tmlen);
/* 1531 */                         if ((new String(tm, "ISO-8859-1")).trim().toLowerCase().equals(vm.getVmName().toLowerCase()) && 
/* 1532 */                           sendVoiceFileCommand(7)) {
/* 1533 */                           byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1534 */                           if (buffer.length == 1 && 
/* 1535 */                             buffer[0] == 6) {
/* 1536 */                             byte[] vmcmd = new byte[19];
/* 1537 */                             vmcmd[0] = 1;
/* 1538 */                             System.arraycopy(tm, 0, vmcmd, 1, tm.length);
/* 1539 */                             System.arraycopy(vmHeader, 14, vmcmd, 13, 4);
/* 1540 */                             int crcCalc = CRC16.calculate(vmcmd, 0, 17, 65535);
/* 1541 */                             byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1542 */                             System.arraycopy(tmp2, 0, vmcmd, 17, 2);
/* 1543 */                             SocketFunctions.send(this.clientSocket, vmcmd);
/* 1544 */                             monitorThread();
/* 1545 */                             buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1546 */                             if (buffer != null && buffer[0] == 6) {
/* 1547 */                               raf.seek(fpos);
/* 1548 */                               sendAudioFile2Module(raf, fpos, vmLen, vm.getVmCRC32(), vm.getVmName());
/*      */                               
/*      */                               break;
/*      */                             } 
/*      */                           } 
/*      */                         } 
/*      */                         
/* 1555 */                         fpos += vmLen;
/*      */                       } 
/*      */                     } 
/*      */                   } 
/*      */                   break;
/*      */                 } 
/*      */               } 
/* 1562 */               if (file == null) {
/* 1563 */                 file = new File("GRCP_VMC_" + ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/*      */               }
/*      */             } finally {
/* 1566 */               if (file != null && file.exists()) {
/* 1567 */                 file.delete();
/*      */               }
/*      */             } 
/*      */           } 
/*      */         } 
/* 1572 */         if (sendRemoteCommand(sp24DH)) {
/* 1573 */           byte[] buffer = new byte[1];
/* 1574 */           buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1575 */           if (buffer.length == 1) {
/* 1576 */             if (buffer[0] == 6)
/* 1577 */             { if (sp24DH.getCommand_Type() == 32773) {
/* 1578 */                 if (sp24DH.getCommandData().charAt(0) == '1' && fileIDData != null && fileIDData.length > 1) {
/* 1579 */                   sp24DH.setCommandFileData(GriffonHandlerHelper.prepareRequiredFileDataForDeviceByCRCMismatch(this.productID, mCFG, fileIDData));
/* 1580 */                   sendFile2Module(sp24DH);
/* 1581 */                   sendLogoutCommand();
/* 1582 */                   return false;
/*      */                 } 
/* 1584 */                 if (sp24DH.getCommandData().charAt(0) == '3' || sp24DH.getCommandData().charAt(0) == '4') {
/* 1585 */                   byte[] fwLen = Functions.swapLSB2MSB4ByteArray(Functions.get4ByteArrayFromInt((sp24DH.getCommandFileData()).length));
/* 1586 */                   int crcFw = CRC16.calculate(fwLen, 0, 4, 65535);
/* 1587 */                   byte[] fwLenData = new byte[6];
/* 1588 */                   System.arraycopy(fwLen, 0, fwLenData, 0, 4);
/* 1589 */                   fwLen = Functions.get2ByteArrayFromInt(crcFw);
/* 1590 */                   fwLenData[4] = fwLen[1];
/* 1591 */                   fwLenData[5] = fwLen[0];
/* 1592 */                   SocketFunctions.send(this.clientSocket, fwLenData);
/* 1593 */                   monitorThread();
/* 1594 */                   byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1595 */                   if (tmp[0] == 21) {
/* 1596 */                     GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("CRC_Not_Matched_for_Firmware_file_(response") + tmp[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */                     continue;
/*      */                   } 
/*      */                 } 
/* 1600 */                 sendFile2Module(sp24DH);
/*      */               }
/* 1602 */               else if (sp24DH.getCommand_Type() == 32774) {
/* 1603 */                 if ((sp24DH.getCommandData().charAt(0) == '1' || sp24DH.getCommandData().charAt(0) == '5') && fileIDData != null && fileIDData.length > 1) {
/* 1604 */                   int crcCalc = CRC16.calculate(fileIDData, 0, fileIDData.length, 65535);
/* 1605 */                   byte[] fileIDDataCRC16 = new byte[fileIDData.length + 2];
/* 1606 */                   byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1607 */                   System.arraycopy(fileIDData, 0, fileIDDataCRC16, 0, fileIDData.length);
/* 1608 */                   System.arraycopy(tmp2, 0, fileIDDataCRC16, fileIDData.length, 2);
/* 1609 */                   SocketFunctions.send(this.clientSocket, fileIDDataCRC16);
/* 1610 */                   monitorThread();
/* 1611 */                   byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1612 */                   if (resp != null && resp[0] == 6) {
/* 1613 */                     receiveFile4MModule((SP_024DataHolder)null, mCFG, fileIDData);
/*      */                   }
/* 1615 */                 } else if (sp24DH.getCommandData().charAt(0) == '2' || sp24DH.getCommandData().charAt(0) == '3' || sp24DH.getCommandData().charAt(0) == '4') {
/* 1616 */                   receiveLogFiles4MModule(sp24DH);
/* 1617 */                 } else if (sp24DH.getCommandData().charAt(0) == '8') {
/* 1618 */                   String[] rad = sp24DH.getCommandData().split(";");
/* 1619 */                   if (rad != null && rad.length == 3) {
/* 1620 */                     byte[] vmcmd = new byte[15];
/* 1621 */                     vmcmd[0] = 0;
/* 1622 */                     System.arraycopy(rad[2].getBytes("ISO-8859-1"), 0, vmcmd, 1, rad[2].length());
/* 1623 */                     int crcCalc = CRC16.calculate(vmcmd, 0, 13, 65535);
/* 1624 */                     byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1625 */                     System.arraycopy(tmp2, 0, vmcmd, 13, 2);
/* 1626 */                     SocketFunctions.send(this.clientSocket, vmcmd);
/* 1627 */                     monitorThread();
/* 1628 */                     buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1629 */                     if (buffer != null && buffer[0] == 6) {
/* 1630 */                       readRecordedAudioFileData(Integer.parseInt(rad[1]), rad[2], sp24DH);
/* 1631 */                     } else if (buffer != null && buffer[0] == 21) {
/* 1632 */                       GriffonDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + (buffer[0] & 0xFF));
/*      */                     } 
/*      */                   } 
/*      */                 } 
/* 1636 */               } else if (sp24DH.getCommand_Type() == 32785) {
/* 1637 */                 sleepThread();
/* 1638 */                 buffer = SocketFunctions.receive(this.clientSocket, 0, 203);
/* 1639 */                 if (buffer != null && buffer.length >= 203) {
/* 1640 */                   this.module = GriffonHandlerHelper.handleDashboardBuffer(buffer, this.module, this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/* 1641 */                   GriffonDBManager.executeSPG_005(this.module);
/*      */                 } 
/* 1643 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1644 */                 GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */               } else {
/* 1646 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1647 */                 GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */               }
/*      */                }
/* 1650 */             else if (buffer[0] == 21)
/* 1651 */             { GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command()); }
/*      */             
/* 1653 */             else if (sp24DH.getCommand_Type() == 32781 && buffer[0] >= 21 && buffer[0] <= 30)
/* 1654 */             { Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1655 */               GriffonDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + (buffer[0] & 0xFF)); }
/* 1656 */             else if (sp24DH.getCommand_Type() == 32780 && buffer[0] >= 21 && buffer[0] <= 54)
/* 1657 */             { Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1658 */               GriffonDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + (buffer[0] & 0xFF));
/* 1659 */               GriffonHandlerHelper.updateCommandFailureStatus2App(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, sp24DH); }
/* 1660 */             else if (sp24DH.getCommand_Type() == 32770 && buffer[0] >= 21 && buffer[0] <= 22)
/* 1661 */             { Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1662 */               GriffonDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData()); }
/* 1663 */             else { if (buffer[0] == 4) {
/*      */                 continue;
/*      */               }
/* 1666 */               GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command()); }
/*      */ 
/*      */           
/*      */           } else {
/*      */             
/* 1671 */             GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           } 
/*      */         } else {
/* 1674 */           GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         } 
/* 1676 */         Thread.sleep(100L);
/*      */       } 
/* 1678 */       ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).newCommand = false;
/* 1679 */       ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = 0L;
/* 1680 */       this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).communicationTimeout;
/*      */       try {
/* 1682 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1683 */         return true;
/* 1684 */       } catch (IOException ex) {
/* 1685 */         return false;
/*      */       } 
/* 1687 */     } catch (Exception ex) {
/* 1688 */       if (sp24DH != null) {
/*      */         try {
/* 1690 */           GriffonHandlerHelper.registerFailureSendCommand(this.sn, "", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/* 1691 */         } catch (SQLException|InterruptedException ex1) {
/* 1692 */           Logger.getLogger(GriffonHandler.class.getName()).log(Level.SEVERE, (String)null, ex1);
/*      */         } 
/*      */       }
/* 1695 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_while_processing_the_COMMAND_packet"), Enums.EnumMessagePriority.HIGH, this.sn, ex);
/*      */       try {
/* 1697 */         if (this.clientSocket != null) {
/* 1698 */           SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */         }
/* 1700 */       } catch (IOException iOException) {}
/*      */       
/* 1702 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void requestRecordAudioLookupData() throws InterruptedException, IOException, Exception {
/* 1707 */     if (sendFileSyncCommand(7)) {
/* 1708 */       monitorThread();
/* 1709 */       byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1710 */       if (resp != null && resp[0] == 6) {
/*      */         
/* 1712 */         resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1713 */         int cnt = resp[0] & 0xFF;
/* 1714 */         if (cnt > 0) {
/* 1715 */           long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1716 */           short retry = 0;
/*      */           
/* 1718 */           byte[] tmp2 = new byte[2];
/*      */ 
/*      */           
/* 1721 */           int fileContentIndex = 0;
/*      */ 
/*      */           
/* 1724 */           int expBlockIndex = 0;
/* 1725 */           int flen = cnt * 16;
/* 1726 */           byte[] fileContent = new byte[flen];
/*      */           try {
/* 1728 */             while (isSocketConnected()) {
/*      */               
/* 1730 */               for (retry = (short)(retry + 1); retry < 3; ) {
/*      */                 try {
/* 1732 */                   byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1733 */                   int blockLength = tmp[2] & 0xFF;
/* 1734 */                   tmp2[0] = tmp[1];
/* 1735 */                   tmp2[1] = tmp[0];
/* 1736 */                   int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1737 */                   if (expBlockIndex == rcvdBlockIndex) {
/* 1738 */                     byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1739 */                     byte[] packet = new byte[blockLength + 3];
/* 1740 */                     System.arraycopy(tmp, 0, packet, 0, 3);
/* 1741 */                     System.arraycopy(block, 0, packet, 3, blockLength);
/* 1742 */                     int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1743 */                     System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1744 */                     int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1745 */                     if (crcCalc == crcRecv) {
/* 1746 */                       System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1747 */                       fileContentIndex += blockLength;
/* 1748 */                       SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1749 */                       retry = 0;
/* 1750 */                       if (fileContentIndex >= cnt * 16 && flen > 0) {
/*      */                         break;
/*      */                       }
/* 1753 */                       expBlockIndex++;
/*      */                     } else {
/* 1755 */                       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                     }
/*      */                   
/*      */                   }
/* 1759 */                   else if (rcvdBlockIndex < expBlockIndex) {
/* 1760 */                     SocketFunctions.clearInputStream(this.clientSocket, blockLength);
/* 1761 */                     SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/*      */                   } else {
/* 1763 */                     SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                   }
/*      */                 
/* 1766 */                 } catch (SocketException socketException) {}
/*      */ 
/*      */                 
/* 1769 */                 if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1770 */                   updateLastCommunicationModuleData();
/* 1771 */                   nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                 } 
/*      */               } 
/*      */               
/* 1775 */               if (fileContentIndex >= cnt * 16 && flen > 0) {
/* 1776 */                 GriffonHandlerHelper.parseRecordedFileLookupData(fileContent, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, cnt, this.recAudioLookupCRC32);
/*      */                 break;
/*      */               } 
/*      */             } 
/* 1780 */           } catch (Exception ex) {
/* 1781 */             ex.printStackTrace();
/*      */           } 
/*      */         } else {
/* 1784 */           GriffonDBManager.updateRecordedFileLookupData(null, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, this.recAudioLookupCRC32);
/*      */         } 
/* 1786 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   private void receiveLogFiles4MModule(SP_024DataHolder sp24DH) throws SQLException, InterruptedException {
/* 1792 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1793 */     short retry = 0;
/*      */     
/* 1795 */     byte[] tmp2 = new byte[2];
/* 1796 */     byte[] tmp4 = new byte[4];
/*      */ 
/*      */     
/* 1799 */     byte[] fileContent = null;
/* 1800 */     int fileContentIndex = 0;
/*      */ 
/*      */     
/* 1803 */     int expBlockIndex = 0;
/* 1804 */     int flen = 0;
/* 1805 */     int recvLogCRC32 = 0;
/*      */ 
/*      */     
/*      */     try {
/* 1809 */       while (isSocketConnected()) {
/*      */         
/* 1811 */         for (retry = (short)(retry + 1); retry < 3;) {
/*      */           try {
/* 1813 */             byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1814 */             int blockLength = tmp[2] & 0xFF;
/* 1815 */             tmp2[0] = tmp[1];
/* 1816 */             tmp2[1] = tmp[0];
/* 1817 */             int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1818 */             if (expBlockIndex == rcvdBlockIndex) {
/* 1819 */               byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1820 */               byte[] packet = new byte[blockLength + 3];
/* 1821 */               System.arraycopy(tmp, 0, packet, 0, 3);
/* 1822 */               System.arraycopy(block, 0, packet, 3, blockLength);
/* 1823 */               int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1824 */               System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1825 */               int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1826 */               if (crcCalc == crcRecv) {
/* 1827 */                 if (expBlockIndex == 0) {
/* 1828 */                   System.arraycopy(block, 2, tmp4, 0, 4);
/* 1829 */                   flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1830 */                   System.arraycopy(block, 6, tmp4, 0, 4);
/* 1831 */                   recvLogCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1832 */                   fileContent = new byte[flen + 10];
/* 1833 */                   fileContentIndex = 0;
/* 1834 */                   if (flen == 0) {
/* 1835 */                     SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/* 1836 */                     System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1837 */                     GriffonDBManager.updateCommandFileData(sp24DH.getId_Command(), new ByteArrayInputStream(fileContent));
/* 1838 */                     Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonHandlerHelper.getReceiveLogFileName(Character.digit(sp24DH.getCommandData().charAt(0), 10)) + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1839 */                     GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */                     // Byte code: goto -> 818
/*      */                   } 
/*      */                 } 
/* 1843 */                 System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1844 */                 fileContentIndex += blockLength;
/* 1845 */                 SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/*      */                 
/* 1847 */                 if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1848 */                   updateLastCommunicationModuleData();
/* 1849 */                   nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                 } 
/* 1851 */                 retry = 0;
/* 1852 */                 if (fileContentIndex >= flen + 10 && flen > 0) {
/*      */                   break;
/*      */                 }
/* 1855 */                 expBlockIndex++; continue;
/*      */               } 
/* 1857 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 21 });
/*      */               
/*      */               continue;
/*      */             } 
/* 1861 */             if (rcvdBlockIndex < expBlockIndex) {
/* 1862 */               SocketFunctions.clearInputStream(this.clientSocket, blockLength);
/* 1863 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 }); continue;
/*      */             } 
/* 1865 */             SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 21 });
/*      */           
/*      */           }
/* 1868 */           catch (SocketException ex) {
/* 1869 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sp24DH.getCommandData(), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */           } 
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 1875 */         if (fileContentIndex >= flen + 10 && flen > 0) {
/* 1876 */           byte[] decData = new byte[flen];
/* 1877 */           System.arraycopy(fileContent, 10, decData, 0, flen);
/* 1878 */           int calcLogCrc32 = CRC32.getCRC32(decData);
/* 1879 */           if (recvLogCRC32 == calcLogCrc32) {
/* 1880 */             GriffonDBManager.updateCommandFileData(sp24DH.getId_Command(), new ByteArrayInputStream(fileContent));
/* 1881 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + GriffonHandlerHelper.getReceiveLogFileName(Character.digit(sp24DH.getCommandData().charAt(0), 10)) + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1882 */             GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); break;
/*      */           } 
/* 1884 */           GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("invalid"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           
/*      */           break;
/*      */         } 
/*      */       } 
/* 1889 */     } catch (IOException|InterruptedException|SQLException ex) {
/* 1890 */       ex.printStackTrace();
/* 1891 */       GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file") + sp24DH.getCommandData() + ex.getMessage(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */     } 
/*      */   }
/*      */   
/*      */   private void receiveEMFWFileData() {
/* 1896 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1897 */     short retry = 0;
/*      */     
/* 1899 */     byte[] fid = new byte[2];
/* 1900 */     byte[] tmp2 = new byte[2];
/* 1901 */     byte[] tmp4 = new byte[4];
/* 1902 */     byte[] first16 = new byte[16];
/*      */ 
/*      */     
/* 1905 */     byte[] fileContent = null;
/* 1906 */     int fileContentIndex = 0;
/*      */ 
/*      */     
/* 1909 */     int expBlockIndex = 0;
/* 1910 */     int flen = 0;
/* 1911 */     int recvCfgCRC32 = 0;
/*      */     
/*      */     try {
/* 1914 */       if (isSocketConnected()) {
/*      */         
/* 1916 */         for (retry = (short)(retry + 1); retry < 3; ) {
/*      */           try {
/* 1918 */             byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1919 */             int blockLength = tmp[2] & 0xFF;
/* 1920 */             tmp2[0] = tmp[1];
/* 1921 */             tmp2[1] = tmp[0];
/* 1922 */             int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1923 */             if (expBlockIndex == rcvdBlockIndex) {
/* 1924 */               byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1925 */               byte[] packet = new byte[blockLength + 3];
/* 1926 */               System.arraycopy(tmp, 0, packet, 0, 3);
/* 1927 */               System.arraycopy(block, 0, packet, 3, blockLength);
/* 1928 */               int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1929 */               System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1930 */               int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1931 */               if (crcCalc == crcRecv) {
/* 1932 */                 if (expBlockIndex == 0 && blockLength <= 10) {
/* 1933 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*      */                   break;
/*      */                 } 
/* 1936 */                 if (expBlockIndex == 0) {
/* 1937 */                   fid[0] = block[0];
/* 1938 */                   fid[1] = block[1];
/* 1939 */                   System.arraycopy(block, 0, first16, 0, 16);
/*      */                   
/* 1941 */                   System.arraycopy(first16, 2, tmp4, 0, 4);
/* 1942 */                   flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1943 */                   System.arraycopy(first16, 6, tmp4, 0, 4);
/* 1944 */                   recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1945 */                   fileContent = new byte[flen + 10];
/* 1946 */                   fileContentIndex = 0;
/*      */                 } 
/* 1948 */                 System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1949 */                 fileContentIndex += blockLength;
/*      */                 
/* 1951 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1952 */                 retry = 0;
/* 1953 */                 if (fileContentIndex >= flen + 10 && flen > 0) {
/*      */                   break;
/*      */                 }
/* 1956 */                 expBlockIndex++;
/*      */               } else {
/* 1958 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               }
/*      */             
/*      */             }
/* 1962 */             else if (rcvdBlockIndex < expBlockIndex) {
/* 1963 */               SocketFunctions.clearInputStream(this.clientSocket, blockLength);
/* 1964 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/*      */             } else {
/* 1966 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */             }
/*      */           
/* 1969 */           } catch (IOException|InterruptedException ex) {
/* 1970 */             SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 1971 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */           } 
/*      */ 
/*      */           
/* 1975 */           if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1976 */             updateLastCommunicationModuleData();
/* 1977 */             nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */         
/* 1981 */         if (fileContentIndex >= flen + 10 && flen > 0) {
/*      */           
/* 1983 */           byte[] decBlock = new byte[flen];
/* 1984 */           System.arraycopy(fileContent, 10, decBlock, 0, flen);
/* 1985 */           int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/* 1986 */           if (recvCfgCRC32 == calcCfgCrc32) {
/* 1987 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exp_Boards_FW_version_sync_done_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1988 */             GriffonHandlerHelper.parseEBFWData(decBlock, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, recvCfgCRC32);
/*      */           } else {
/* 1990 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("CRC_32_of_the_received_configuration_file_is_invalid"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */           }
/*      */         
/*      */         } 
/*      */       } 
/* 1995 */     } catch (Exception ex) {
/* 1996 */       ex.printStackTrace();
/* 1997 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */     } 
/*      */   }
/*      */   
/*      */   private void readRecordedAudioFileData(int flen, String name, SP_024DataHolder sp24DH) {
/* 2002 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 2003 */     short retry = 0;
/*      */     
/* 2005 */     byte[] tmp2 = new byte[2];
/*      */ 
/*      */     
/* 2008 */     byte[] fileContent = new byte[flen];
/* 2009 */     int fileContentIndex = 0;
/*      */ 
/*      */     
/* 2012 */     int expBlockIndex = 0;
/*      */     
/*      */     try {
/* 2015 */       while (isSocketConnected()) {
/*      */         
/* 2017 */         for (retry = (short)(retry + 1); retry < 3; ) {
/*      */           try {
/* 2019 */             byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 2020 */             int blockLength = tmp[2] & 0xFF;
/* 2021 */             tmp2[0] = tmp[1];
/* 2022 */             tmp2[1] = tmp[0];
/* 2023 */             int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2024 */             if (expBlockIndex == rcvdBlockIndex) {
/* 2025 */               byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 2026 */               byte[] packet = new byte[blockLength + 3];
/* 2027 */               System.arraycopy(tmp, 0, packet, 0, 3);
/* 2028 */               System.arraycopy(block, 0, packet, 3, blockLength);
/* 2029 */               int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 2030 */               System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 2031 */               int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 2032 */               if (crcCalc == crcRecv) {
/* 2033 */                 if (expBlockIndex == 0) {
/* 2034 */                   System.arraycopy(block, 4, fileContent, fileContentIndex, blockLength - 4);
/* 2035 */                   fileContentIndex += blockLength - 4;
/*      */                 } else {
/* 2037 */                   System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 2038 */                   fileContentIndex += blockLength;
/*      */                 } 
/* 2040 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 2041 */                 retry = 0;
/* 2042 */                 if (fileContentIndex >= flen && flen > 0) {
/*      */                   break;
/*      */                 }
/* 2045 */                 expBlockIndex++;
/*      */               } else {
/* 2047 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               }
/*      */             
/*      */             }
/* 2051 */             else if (rcvdBlockIndex < expBlockIndex) {
/* 2052 */               SocketFunctions.clearInputStream(this.clientSocket, blockLength);
/* 2053 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/*      */             } else {
/* 2055 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */             }
/*      */           
/* 2058 */           } catch (IOException|InterruptedException ex) {
/* 2059 */             ex.printStackTrace();
/* 2060 */             SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */           } 
/*      */ 
/*      */           
/* 2064 */           if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2065 */             updateLastCommunicationModuleData();
/* 2066 */             nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */         
/* 2070 */         if (fileContentIndex >= flen && flen > 0) {
/* 2071 */           GriffonDBManager.updateCommandFileData(sp24DH.getId_Command(), new ByteArrayInputStream(fileContent));
/* 2072 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + name + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2073 */           GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */           break;
/*      */         } 
/*      */       } 
/* 2077 */     } catch (IOException|InterruptedException|SQLException ex) {
/* 2078 */       ex.printStackTrace();
/*      */     } 
/*      */   }
/*      */   
/*      */   private void receiveFile4MModule(SP_024DataHolder sp24DH, ModuleCFG mCFG, byte[] fileIDData) throws SQLException, InterruptedException {
/* 2083 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 2084 */     short retry = 0;
/*      */     
/* 2086 */     byte[] fid = new byte[2];
/* 2087 */     byte[] tmp2 = new byte[2];
/* 2088 */     byte[] tmp4 = new byte[4];
/* 2089 */     byte[] first16 = new byte[16];
/*      */ 
/*      */     
/* 2092 */     byte[] fileContent = null;
/* 2093 */     int fileContentIndex = 0;
/*      */ 
/*      */     
/* 2096 */     int expBlockIndex = 0;
/* 2097 */     int flen = 0;
/* 2098 */     int recvCfgCRC32 = 0;
/* 2099 */     int lPad = 0;
/*      */     
/*      */     try {
/* 2102 */       while (isSocketConnected()) {
/*      */         
/* 2104 */         for (retry = (short)(retry + 1); retry < 3; ) {
/*      */           try {
/* 2106 */             byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 2107 */             int blockLength = tmp[2] & 0xFF;
/* 2108 */             tmp2[0] = tmp[1];
/* 2109 */             tmp2[1] = tmp[0];
/* 2110 */             int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2111 */             if (expBlockIndex == rcvdBlockIndex) {
/* 2112 */               byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 2113 */               byte[] packet = new byte[blockLength + 3];
/* 2114 */               System.arraycopy(tmp, 0, packet, 0, 3);
/* 2115 */               System.arraycopy(block, 0, packet, 3, blockLength);
/* 2116 */               int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 2117 */               System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 2118 */               int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 2119 */               if (crcCalc == crcRecv) {
/* 2120 */                 if (expBlockIndex == 0) {
/* 2121 */                   fid[0] = block[0];
/* 2122 */                   fid[1] = block[1];
/* 2123 */                   System.arraycopy(block, 0, first16, 0, 16);
/* 2124 */                   first16 = Rijndael.decryptBytes(first16, Rijndael.aes_256, false);
/* 2125 */                   System.arraycopy(first16, 2, tmp4, 0, 4);
/* 2126 */                   flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2127 */                   lPad = (flen + 10) % 16;
/* 2128 */                   if (lPad > 0) {
/* 2129 */                     lPad = 16 - lPad;
/*      */                   }
/* 2131 */                   System.arraycopy(first16, 6, tmp4, 0, 4);
/* 2132 */                   recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 2133 */                   fileContent = new byte[flen + lPad + 10];
/* 2134 */                   fileContentIndex = 0;
/*      */                 } 
/* 2136 */                 System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 2137 */                 fileContentIndex += blockLength;
/* 2138 */                 SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/* 2139 */                 retry = 0;
/* 2140 */                 if (fileContentIndex >= flen + lPad + 10 && flen > 0) {
/*      */                   break;
/*      */                 }
/* 2143 */                 expBlockIndex++;
/*      */               } else {
/* 2145 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               }
/*      */             
/*      */             }
/* 2149 */             else if (rcvdBlockIndex < expBlockIndex) {
/* 2150 */               SocketFunctions.clearInputStream(this.clientSocket, blockLength);
/* 2151 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/*      */             } else {
/* 2153 */               SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 21 });
/*      */             }
/*      */           
/* 2156 */           } catch (IOException|InterruptedException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2157 */             SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 2158 */             if (sp24DH != null) {
/* 2159 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sp24DH.getCommandData(), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } else {
/* 2161 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/*      */           } 
/*      */ 
/*      */           
/* 2166 */           if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2167 */             updateLastCommunicationModuleData();
/* 2168 */             nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */         
/* 2172 */         if (fileContentIndex >= flen + lPad + 10 && flen > 0) {
/* 2173 */           byte[] decData = new byte[flen + lPad + 10];
/*      */           
/* 2175 */           byte[] encBlock = new byte[16];
/* 2176 */           if (fileContent.length >= 16 && fileContent.length % 16 == 0) {
/* 2177 */             for (int i = 0; i < fileContent.length; ) {
/* 2178 */               System.arraycopy(fileContent, i, encBlock, 0, 16);
/* 2179 */               byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 2180 */               System.arraycopy(arrayOfByte, 0, decData, i, 16);
/* 2181 */               i += 16;
/*      */             } 
/*      */           }
/* 2184 */           byte[] decBlock = new byte[flen];
/* 2185 */           System.arraycopy(decData, 10, decBlock, 0, flen);
/* 2186 */           int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/* 2187 */           if (recvCfgCRC32 == calcCfgCrc32) {
/* 2188 */             ModuleCFG newModuleCFG = GriffonHandlerHelper.rearrangeServerCFGCopy(this.productID, decBlock, mCFG, flen, fileIDData);
/* 2189 */             newModuleCFG.setIdModule(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule);
/* 2190 */             if (sp24DH != null) {
/* 2191 */               GriffonHandlerHelper.finalizeReceiveCFGFileCommand(sp24DH.getId_Command(), ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, newModuleCFG, null);
/* 2192 */               if (this.deviceCRC32 != null) {
/* 2193 */                 newModuleCFG.setCrc32List(this.deviceCRC32);
/*      */               }
/* 2195 */               GriffonDBManager.updateGriffonModuleCfg(newModuleCFG);
/* 2196 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2197 */               GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); break;
/*      */             } 
/* 2199 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Configuration_sync_done_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2200 */             if (this.deviceCRC32 != null) {
/* 2201 */               newModuleCFG.setCrc32List(this.deviceCRC32);
/*      */             }
/* 2203 */             GriffonDBManager.updateGriffonModuleCfg(newModuleCFG);
/* 2204 */             GriffonHandlerHelper.parseConfigurationData(newModuleCFG, this.sn, ((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idClient);
/*      */             break;
/*      */           } 
/* 2207 */           if (sp24DH != null) {
/* 2208 */             GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("invalid"), sp24DH.getExec_Retries(), sp24DH.getId_Command()); break;
/*      */           } 
/* 2210 */           Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("CRC_32_of_the_received_configuration_file_is_invalid"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */ 
/*      */           
/*      */           break;
/*      */         } 
/*      */       } 
/* 2216 */     } catch (Exception ex) {
/* 2217 */       ex.printStackTrace();
/* 2218 */       if (sp24DH != null) {
/* 2219 */         GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file") + sp24DH.getCommandData() + ex.getMessage(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */       } else {
/* 2221 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   private void readVoiceMessages(List<VoiceMessage> requriedVMList) throws SQLException, InterruptedException, IOException, Exception {
/* 2227 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 2228 */     for (VoiceMessage vm : requriedVMList) {
/* 2229 */       if (sendFileSyncCommand(6)) {
/* 2230 */         monitorThread();
/* 2231 */         byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2232 */         if (resp != null && resp[0] == 6) {
/* 2233 */           byte[] vmcmd = new byte[15];
/* 2234 */           vmcmd[0] = 1;
/* 2235 */           System.arraycopy(vm.getVmName().getBytes("ISO-8859-1"), 0, vmcmd, 1, vm.getVmName().length());
/* 2236 */           int crcCalc = CRC16.calculate(vmcmd, 0, 13, 65535);
/* 2237 */           byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 2238 */           System.arraycopy(tmp2, 0, vmcmd, 13, 2);
/* 2239 */           SocketFunctions.send(this.clientSocket, vmcmd);
/* 2240 */           monitorThread();
/* 2241 */           resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2242 */           if (resp != null && resp[0] == 6) {
/* 2243 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Reading_audio_file_name_[") + vm.getVmName() + " ]", Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2244 */             short retry = 0;
/*      */ 
/*      */ 
/*      */             
/* 2248 */             byte[] fileContent = null;
/* 2249 */             int fileContentIndex = 0;
/*      */ 
/*      */             
/* 2252 */             int expBlockIndex = 0;
/* 2253 */             if (isSocketConnected()) {
/*      */               
/* 2255 */               for (retry = (short)(retry + 1); retry < 3; ) {
/*      */                 try {
/* 2257 */                   byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 2258 */                   int blockLength = tmp[2] & 0xFF;
/* 2259 */                   tmp2[0] = tmp[1];
/* 2260 */                   tmp2[1] = tmp[0];
/* 2261 */                   int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 2262 */                   if (expBlockIndex == rcvdBlockIndex) {
/* 2263 */                     byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 2264 */                     byte[] packet = new byte[blockLength + 3];
/* 2265 */                     System.arraycopy(tmp, 0, packet, 0, 3);
/* 2266 */                     System.arraycopy(block, 0, packet, 3, blockLength);
/* 2267 */                     crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 2268 */                     System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 2269 */                     int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 2270 */                     if (crcCalc == crcRecv) {
/* 2271 */                       if (expBlockIndex == 0) {
/* 2272 */                         fileContent = new byte[vm.getVmLength()];
/* 2273 */                         System.arraycopy(block, 4, fileContent, fileContentIndex, blockLength - 4);
/* 2274 */                         fileContentIndex += blockLength - 4;
/*      */                       } else {
/* 2276 */                         System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 2277 */                         fileContentIndex += blockLength;
/*      */                       } 
/* 2279 */                       SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 2280 */                       retry = 0;
/* 2281 */                       if (fileContentIndex >= vm.getVmLength() && vm.getVmLength() > 0) {
/*      */                         break;
/*      */                       }
/* 2284 */                       expBlockIndex++;
/*      */                     } else {
/* 2286 */                       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                     }
/*      */                   
/*      */                   }
/* 2290 */                   else if (rcvdBlockIndex < expBlockIndex) {
/* 2291 */                     SocketFunctions.clearInputStream(this.clientSocket, blockLength);
/* 2292 */                     SocketFunctions.sendWithOutSkip(this.clientSocket, new byte[] { 6 });
/*      */                   } else {
/* 2294 */                     SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                   }
/*      */                 
/* 2297 */                 } catch (IOException|InterruptedException ex) {
/* 2298 */                   ex.printStackTrace();
/*      */                 } 
/*      */ 
/*      */ 
/*      */                 
/* 2303 */                 if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2304 */                   updateLastCommunicationModuleData();
/* 2305 */                   nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                 } 
/*      */               } 
/* 2308 */               if (fileContentIndex >= vm.getVmLength() && vm.getVmLength() > 0) {
/* 2309 */                 GriffonDBManager.saveVoiceMessage(fileContent, vm.getVmLength(), vm.getVmName(), vm.getVmCRC32());
/*      */               }
/*      */             } 
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendAudioFile2Module(RandomAccessFile raf, int currPos, int vmLen, int crc32, String vmFileName) throws IOException, InterruptedException, SQLException, Exception {
/* 2322 */     long flen = vmLen;
/*      */     
/* 2324 */     int blockIndex = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2329 */     short maxRetries = 3;
/* 2330 */     short retry = 0;
/* 2331 */     FileChannel fc = raf.getChannel();
/*      */     
/* 2333 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */     
/* 2335 */     label50: while (isSocketConnected() && fc.position() < currPos + flen) {
/*      */       
/* 2337 */       for (retry = (short)(retry + 1); retry < maxRetries; ) {
/* 2338 */         int blockLength = (int)((currPos + flen - fc.position() > 240L) ? 240L : (currPos + flen - fc.position()));
/* 2339 */         ByteBuffer blockBuf = ByteBuffer.allocate(blockLength);
/* 2340 */         if (fc.read(blockBuf) == blockLength) {
/* 2341 */           byte[] block = blockBuf.array();
/* 2342 */           byte[] packet = new byte[blockLength + 5];
/* 2343 */           byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(blockIndex));
/* 2344 */           System.arraycopy(tmp, 0, packet, 0, 2);
/* 2345 */           packet[2] = (byte)Integer.parseInt(Integer.toHexString(blockLength), 16);
/* 2346 */           System.arraycopy(block, 0, packet, 3, blockLength);
/* 2347 */           int crcCalc = CRC16.calculate(packet, 0, blockLength + 3, 65535);
/* 2348 */           tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 2349 */           System.arraycopy(tmp, 0, packet, blockLength + 3, 2);
/*      */           try {
/* 2351 */             SocketFunctions.send(this.clientSocket, packet);
/* 2352 */             Thread.sleep(50L);
/*      */             try {
/* 2354 */               if (retry - 1 == 0) {
/* 2355 */                 this.clientSocket.setSoTimeout(120000);
/* 2356 */               } else if (retry - 1 == 1) {
/* 2357 */                 this.clientSocket.setSoTimeout(210000);
/* 2358 */               } else if (retry - 1 == 2) {
/* 2359 */                 this.clientSocket.setSoTimeout(300000);
/*      */               } 
/* 2361 */               if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2362 */                 updateLastCommunicationModuleData();
/* 2363 */                 nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 2365 */               tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2366 */               if (tmp[0] == 6) {
/* 2367 */                 retry = 0;
/* 2368 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + vmFileName + " (" + (currPos + flen - fc.position()) + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2369 */                 blockIndex++; break;
/*      */               } 
/* 2371 */               if (tmp[0] == 21) {
/*      */                 
/* 2373 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Invalid_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + vmFileName + " :" + tmp[0], Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2374 */                 fc.position(fc.position() - blockLength); break;
/*      */               } 
/* 2376 */               if ((tmp[0] & 0xFF) == 4) {
/*      */                 break label50;
/*      */               }
/* 2379 */             } catch (SocketException ex) {
/*      */ 
/*      */               
/* 2382 */               fc.position(fc.position() - blockLength);
/* 2383 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + vmFileName, Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/* 2385 */           } catch (IOException|InterruptedException|SQLException ex) {
/* 2386 */             ex.printStackTrace();
/*      */             
/*      */             break label50;
/*      */           } 
/* 2390 */           Thread.sleep(100L);
/*      */           continue;
/*      */         } 
/*      */         break label50;
/*      */       } 
/*      */     } 
/* 2396 */     if (fc.position() >= currPos + flen) {
/* 2397 */       monitorThread();
/* 2398 */       byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2399 */       if (tmp[0] == 6) {
/* 2400 */         Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + vmFileName + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2401 */         raf.seek(currPos);
/* 2402 */         byte[] vmContent = new byte[vmLen];
/* 2403 */         raf.read(vmContent);
/* 2404 */         GriffonDBManager.saveVoiceMessage(vmContent, vmLen, vmFileName, crc32);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendFile2Module(SP_024DataHolder sp24DH) throws IOException, InterruptedException, SQLException {
/* 2412 */     int blockIndex = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2417 */     short maxRetries = 3;
/* 2418 */     short retry = 0;
/*      */ 
/*      */ 
/*      */     
/* 2422 */     File file = Functions.writeByteArrayToFile(this.sn + "_" + sp24DH.getCommandData(), sp24DH.getCommandFileData());
/* 2423 */     FileChannel fc = (new RandomAccessFile(file, "r")).getChannel();
/* 2424 */     fc.position(0L);
/* 2425 */     long flen = fc.size();
/* 2426 */     String fileName = GriffonHandlerHelper.getFileNameByCommandData(Character.digit(sp24DH.getCommandData().charAt(0), 10), sp24DH.getCommand_Type());
/* 2427 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */     
/* 2429 */     label91: while (isSocketConnected() && fc.position() < flen) {
/*      */       
/* 2431 */       for (retry = (short)(retry + 1); retry < maxRetries; ) {
/* 2432 */         int blockLength = (int)((flen - fc.position() > 240L) ? 240L : (flen - fc.position()));
/* 2433 */         ByteBuffer blockBuf = ByteBuffer.allocate(blockLength);
/* 2434 */         if (fc.read(blockBuf) == blockLength) {
/* 2435 */           byte[] block = blockBuf.array();
/* 2436 */           byte[] packet = new byte[blockLength + 5];
/* 2437 */           byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(blockIndex));
/* 2438 */           System.arraycopy(tmp, 0, packet, 0, 2);
/* 2439 */           packet[2] = (byte)Integer.parseInt(Integer.toHexString(blockLength), 16);
/* 2440 */           System.arraycopy(block, 0, packet, 3, blockLength);
/* 2441 */           int crcCalc = CRC16.calculate(packet, 0, blockLength + 3, 65535);
/* 2442 */           tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 2443 */           System.arraycopy(tmp, 0, packet, blockLength + 3, 2);
/*      */           try {
/* 2445 */             SocketFunctions.send(this.clientSocket, packet);
/* 2446 */             Thread.sleep(50L);
/*      */             try {
/* 2448 */               if (retry - 1 == 0) {
/* 2449 */                 this.clientSocket.setSoTimeout(120000);
/* 2450 */               } else if (retry - 1 == 1) {
/* 2451 */                 this.clientSocket.setSoTimeout(210000);
/* 2452 */               } else if (retry - 1 == 2) {
/* 2453 */                 this.clientSocket.setSoTimeout(300000);
/*      */               } 
/* 2455 */               if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 2456 */                 updateLastCommunicationModuleData();
/* 2457 */                 nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 2459 */               tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2460 */               if (tmp[0] == 6) {
/* 2461 */                 retry = 0;
/* 2462 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + fileName + " (" + fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2463 */                 blockIndex++; break;
/*      */               } 
/* 2465 */               if (tmp[0] == 21) {
/* 2466 */                 if (blockIndex > 0) {
/* 2467 */                   blockIndex--;
/*      */                 }
/*      */                 
/* 2470 */                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Invalid_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + fileName + " :" + tmp[0], Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2471 */                 fc.position(fc.position() - blockLength); break;
/*      */               } 
/* 2473 */               if ((tmp[0] & 0xFF) == 4) {
/* 2474 */                 GriffonHandlerHelper.registerFailureSendCommand(this.sn, "COMMAND Request Received in between file processing " + fileName, sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */                 break label91;
/*      */               } 
/* 2477 */             } catch (SocketException ex) {
/*      */ 
/*      */               
/* 2480 */               if (blockIndex > 0) {
/* 2481 */                 blockIndex--;
/*      */               }
/* 2483 */               fc.position(fc.position() - blockLength);
/* 2484 */               Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + fileName, Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/* 2486 */           } catch (Exception ex) {
/*      */             
/* 2488 */             GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_a_data_block_of_the_file") + fileName + LocaleMessage.getLocaleMessage("to_the_module"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             break label91;
/*      */           } 
/* 2491 */           Thread.sleep(100L); continue;
/*      */         } 
/* 2493 */         GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_reading_data_of_the_file") + fileName, sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         
/*      */         break label91;
/*      */       } 
/*      */     } 
/*      */     
/* 2499 */     boolean commandProcessed = (fc.position() >= flen);
/* 2500 */     fc.close();
/*      */     
/*      */     try {
/* 2503 */       if (commandProcessed) {
/* 2504 */         Thread.sleep(100L);
/* 2505 */         if (sp24DH.getCommandData().charAt(0) == '1') {
/* 2506 */           monitorThread();
/* 2507 */           byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2508 */           if (tmp[0] == 6) {
/* 2509 */             Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("The_file_[") + fileName + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 2510 */             GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/* 2511 */           } else if (tmp[0] == 20) {
/* 2512 */             GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("The_module_informed_that_CRC-32_is_not_matching_for_the_file_[") + fileName + "]", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/* 2513 */             GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/* 2514 */           } else if (tmp[0] == 18) {
/* 2515 */             GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("The_module_informed_that_received_configuration_file_is_invalid_[") + fileName + "]", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/* 2516 */             GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */           } else {
/* 2518 */             GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_the_file") + fileName + LocaleMessage.getLocaleMessage("(response") + tmp[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           } 
/*      */         } else {
/* 2521 */           GriffonHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */         } 
/*      */       } else {
/* 2524 */         GriffonHandlerHelper.registerFailureSendCommand(this.sn, "", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */       } 
/* 2526 */     } catch (IOException|InterruptedException|SQLException ex) {
/* 2527 */       GriffonHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + fileName + LocaleMessage.getLocaleMessage("was_sent_successfully"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */     } finally {
/* 2529 */       if (file != null && file.exists()) {
/* 2530 */         file.delete();
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void updateLastCommunicationModuleData() throws SQLException, InterruptedException {
/* 2536 */     GriffonDBManager.executeSP_028(((InfoModule)TblGriffonActiveConnections.getInstance().get(this.sn)).idModule, this.lastCommIface, this.currentSIM); } private boolean sendRemoteCommand(SP_024DataHolder sp24DH) { byte[] tmp, ascii; String[] cData, zData; int idx, temp;
/*      */     String[] date, dData;
/*      */     int i;
/*      */     String[] hData;
/*      */     byte b;
/* 2541 */     byte[] data = null;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2547 */     switch (sp24DH.getCommand_Type()) {
/*      */       case 32769:
/* 2549 */         data = new byte[3];
/* 2550 */         tmp = Functions.get2ByteArrayFromInt(32769);
/* 2551 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2552 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2553 */         data[2] = 0;
/*      */         break;
/*      */       case 32774:
/* 2556 */         data = new byte[4];
/* 2557 */         tmp = Functions.get2ByteArrayFromInt(32774);
/* 2558 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2559 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2560 */         data[2] = 1;
/* 2561 */         data[3] = (byte)Character.digit(sp24DH.getCommandData().charAt(0), 10);
/*      */         break;
/*      */       case 32773:
/* 2564 */         data = new byte[4];
/* 2565 */         tmp = Functions.get2ByteArrayFromInt(32773);
/* 2566 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2567 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2568 */         data[2] = 1;
/* 2569 */         data[3] = (byte)Character.digit(sp24DH.getCommandData().charAt(0), 10);
/*      */         break;
/*      */       case 32770:
/* 2572 */         tmp = Functions.get2ByteArrayFromInt(32770);
/* 2573 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2574 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/* 2575 */         data = new byte[ascii.length + 3];
/* 2576 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2577 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/* 2578 */         System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */         break;
/*      */       case 32772:
/* 2581 */         cData = sp24DH.getCommandData().split(";");
/* 2582 */         temp = Integer.parseInt(cData[0]);
/* 2583 */         if (temp == 1 || temp == 4) {
/* 2584 */           data = new byte[4];
/* 2585 */           data[2] = 1;
/* 2586 */           data[3] = (byte)temp;
/* 2587 */         } else if (temp == 3) {
/* 2588 */           data = new byte[5];
/* 2589 */           data[2] = 2;
/* 2590 */           data[3] = (byte)temp;
/* 2591 */           data[4] = (byte)Integer.parseInt(cData[1]);
/* 2592 */         } else if (temp == 2) {
/* 2593 */           data = new byte[6];
/* 2594 */           data[2] = 3;
/* 2595 */           data[3] = (byte)temp;
/* 2596 */           data[4] = (byte)Integer.parseInt(cData[1]);
/* 2597 */           data[5] = (byte)Integer.parseInt(cData[2]);
/*      */         } 
/* 2599 */         tmp = Functions.get2ByteArrayFromInt(32772);
/* 2600 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2601 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32771:
/* 2604 */         tmp = Functions.get2ByteArrayFromInt(32771);
/* 2605 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2606 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData().substring(2));
/* 2607 */         data = new byte[ascii.length + 4];
/* 2608 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2609 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length + 2), 16).byteValue();
/* 2610 */         data[3] = (byte)((sp24DH.getCommandData().charAt(0) == '1') ? 1 : 2);
/* 2611 */         System.arraycopy(ascii, 0, data, 4, ascii.length);
/*      */         break;
/*      */       case 32775:
/* 2614 */         data = new byte[3];
/* 2615 */         tmp = Functions.get2ByteArrayFromInt(32775);
/* 2616 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2617 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2618 */         data[2] = 0;
/*      */         break;
/*      */       case 32777:
/* 2621 */         tmp = Functions.get2ByteArrayFromInt(32777);
/* 2622 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2623 */         cData = sp24DH.getCommandData().split(";");
/* 2624 */         if (cData.length == 1) {
/* 2625 */           if (cData[0].equals("1")) {
/* 2626 */             data = new byte[4];
/* 2627 */             System.arraycopy(tmp, 0, data, 0, 2);
/* 2628 */             data[2] = 1;
/* 2629 */             data[3] = 1; break;
/* 2630 */           }  if (cData[0].equals("2")) {
/* 2631 */             data = new byte[11];
/* 2632 */             System.arraycopy(tmp, 0, data, 0, 2);
/* 2633 */             String[] zones = TimeZone.getAvailableIDs(this.timezone * 60 * 1000);
/* 2634 */             this.df.setTimeZone(TimeZone.getTimeZone((String)Defines.TIMEZONE_NAMES.get(Integer.valueOf(this.timezone))));
/* 2635 */             String ddd = this.df.format(new Date());
/* 2636 */             data[2] = 8;
/* 2637 */             data[3] = 2;
/* 2638 */             data[4] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(0, 2))), 16).byteValue();
/* 2639 */             data[5] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(3, 5))), 16).byteValue();
/* 2640 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(ddd.substring(6, 10)));
/* 2641 */             data[6] = tmp[1];
/* 2642 */             data[7] = tmp[0];
/* 2643 */             data[8] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(11, 13))), 16).byteValue();
/* 2644 */             data[9] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(14, 16))), 16).byteValue();
/* 2645 */             data[10] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(17))), 16).byteValue();
/*      */           }  break;
/*      */         } 
/* 2648 */         data = new byte[11];
/* 2649 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         
/* 2651 */         date = cData[1].split(" ");
/* 2652 */         dData = date[0].split("-");
/* 2653 */         hData = date[1].split(":");
/* 2654 */         data[2] = 8;
/* 2655 */         data[3] = 3;
/* 2656 */         data[4] = Byte.valueOf(dData[2]).byteValue();
/* 2657 */         data[5] = Byte.valueOf(dData[1]).byteValue();
/* 2658 */         tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(dData[0]));
/* 2659 */         data[6] = tmp[1];
/* 2660 */         data[7] = tmp[0];
/* 2661 */         data[8] = Byte.valueOf(hData[0]).byteValue();
/* 2662 */         data[9] = Byte.valueOf(hData[1]).byteValue();
/* 2663 */         data[10] = Byte.valueOf(hData[2]).byteValue();
/*      */         break;
/*      */       
/*      */       case 32778:
/* 2667 */         cData = sp24DH.getCommandData().split(";");
/* 2668 */         zData = cData[2].split(",");
/* 2669 */         data = new byte[8 + zData.length];
/* 2670 */         data[2] = (byte)(5 + zData.length);
/* 2671 */         if (cData.length == 4) {
/* 2672 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[3]));
/* 2673 */           data[3] = tmp[1];
/* 2674 */           data[4] = tmp[0];
/*      */         } else {
/* 2676 */           data[3] = 0;
/* 2677 */           data[4] = 0;
/*      */         } 
/* 2679 */         data[5] = Byte.parseByte(cData[0]);
/* 2680 */         data[6] = Byte.parseByte(cData[1]);
/* 2681 */         data[7] = (byte)zData.length;
/* 2682 */         idx = 8;
/* 2683 */         for (date = zData, i = date.length, b = 0; b < i; ) { String s = date[b];
/* 2684 */           data[idx++] = (byte)Integer.parseInt(s); b++; }
/*      */         
/* 2686 */         tmp = Functions.get2ByteArrayFromInt(32778);
/* 2687 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2688 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32779:
/* 2691 */         cData = sp24DH.getCommandData().split(";");
/* 2692 */         data = new byte[8];
/* 2693 */         data[2] = 5;
/* 2694 */         if (cData.length == 5) {
/* 2695 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[4]));
/* 2696 */           data[3] = tmp[1];
/* 2697 */           data[4] = tmp[0];
/*      */         } else {
/* 2699 */           data[3] = 0;
/* 2700 */           data[4] = 0;
/*      */         } 
/* 2702 */         data[5] = (byte)Integer.parseInt(cData[0]);
/* 2703 */         data[6] = Byte.parseByte(cData[2]);
/* 2704 */         data[7] = (byte)Integer.parseInt(cData[3]);
/* 2705 */         tmp = Functions.get2ByteArrayFromInt(32779);
/* 2706 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2707 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32780:
/* 2710 */         cData = sp24DH.getCommandData().split(";");
/* 2711 */         data = new byte[8];
/* 2712 */         data[2] = 5;
/* 2713 */         if (cData.length == 3) {
/* 2714 */           tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[2]));
/* 2715 */           data[3] = tmp[1];
/* 2716 */           data[4] = tmp[0];
/*      */         } else {
/* 2718 */           data[3] = 0;
/* 2719 */           data[4] = 0;
/*      */         } 
/* 2721 */         tmp = Functions.get2ByteArrayFromInt(Functions.getIntegerFromSelectedParitions(cData[0]));
/* 2722 */         data[5] = tmp[1];
/* 2723 */         data[6] = tmp[0];
/* 2724 */         data[7] = Byte.parseByte(cData[1]);
/*      */         
/* 2726 */         tmp = Functions.get2ByteArrayFromInt(32780);
/* 2727 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2728 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32781:
/* 2731 */         cData = sp24DH.getCommandData().split(";");
/* 2732 */         data = new byte[4];
/* 2733 */         temp = Byte.parseByte(cData[0]);
/* 2734 */         data[2] = (byte)((temp == 1) ? 1 : 0);
/* 2735 */         data[3] = (temp == 1) ? Byte.parseByte(cData[1]) : 0;
/*      */         
/* 2737 */         tmp = Functions.get2ByteArrayFromInt(32781);
/* 2738 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2739 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32782:
/* 2742 */         cData = sp24DH.getCommandData().split(";");
/* 2743 */         data = new byte[4];
/* 2744 */         data[2] = 1;
/* 2745 */         data[3] = (byte)Integer.parseInt(cData[0]);
/*      */         
/* 2747 */         tmp = Functions.get2ByteArrayFromInt(32782);
/* 2748 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2749 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32783:
/* 2752 */         cData = sp24DH.getCommandData().split(";");
/* 2753 */         data = new byte[5];
/* 2754 */         data[2] = 2;
/* 2755 */         data[3] = (byte)Integer.parseInt(cData[0]);
/* 2756 */         data[4] = (byte)Integer.parseInt(cData[1]);
/* 2757 */         tmp = Functions.get2ByteArrayFromInt(32783);
/* 2758 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2759 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32784:
/* 2762 */         data = new byte[3];
/* 2763 */         tmp = Functions.get2ByteArrayFromInt(32784);
/* 2764 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2765 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2766 */         data[2] = 0;
/*      */         break;
/*      */       case 32785:
/* 2769 */         data = new byte[3];
/* 2770 */         tmp = Functions.get2ByteArrayFromInt(32785);
/* 2771 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2772 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2773 */         data[2] = 0;
/*      */         break;
/*      */       case 32799:
/* 2776 */         data = new byte[3];
/* 2777 */         tmp = Functions.get2ByteArrayFromInt(32799);
/* 2778 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2779 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2780 */         data[2] = 0;
/*      */         break;
/*      */       case 32787:
/* 2783 */         data = new byte[3];
/* 2784 */         tmp = Functions.get2ByteArrayFromInt(32787);
/* 2785 */         tmp = Functions.swapLSB2MSB(tmp);
/* 2786 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 2787 */         data[2] = 0;
/*      */         break;
/*      */     } 
/*      */     try {
/* 2791 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2792 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2793 */       return true;
/* 2794 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2795 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2796 */       ex.printStackTrace();
/* 2797 */       return false;
/*      */     }  }
/*      */ 
/*      */   
/*      */   public boolean sendFileSyncCommand(int operation) {
/* 2802 */     byte[] data = new byte[4];
/* 2803 */     byte[] tmp = Functions.get2ByteArrayFromInt(32774);
/* 2804 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2805 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2806 */     data[2] = 1;
/* 2807 */     data[3] = (byte)operation;
/*      */     
/*      */     try {
/* 2810 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2811 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2812 */       return true;
/* 2813 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2814 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2815 */       ex.printStackTrace();
/* 2816 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean sendVoiceFileCommand(int operation) {
/* 2821 */     byte[] data = new byte[4];
/* 2822 */     byte[] tmp = Functions.get2ByteArrayFromInt(32773);
/* 2823 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2824 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2825 */     data[2] = 1;
/* 2826 */     data[3] = (byte)operation;
/*      */     
/*      */     try {
/* 2829 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2830 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2831 */       return true;
/* 2832 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2833 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2834 */       ex.printStackTrace();
/* 2835 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean sendUpdateStatusCommand(boolean sequential) {
/* 2840 */     byte[] data = new byte[3];
/* 2841 */     byte[] tmp = Functions.get2ByteArrayFromInt(32785);
/* 2842 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2843 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2844 */     data[2] = 0;
/*      */     try {
/* 2846 */       if (sequential) {
/* 2847 */         tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2848 */         SocketFunctions.send(this.clientSocket, tmp);
/* 2849 */         return true;
/*      */       } 
/* 2851 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2852 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2853 */       return true;
/*      */     }
/* 2855 */     catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2856 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2857 */       ex.printStackTrace();
/* 2858 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean sendEnableDisablePGMDigitalBuffer(int flag) {
/* 2863 */     byte[] data = new byte[4];
/* 2864 */     byte[] tmp = Functions.get2ByteArrayFromInt(32788);
/* 2865 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2866 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2867 */     data[2] = 1;
/* 2868 */     data[3] = (byte)flag;
/*      */     try {
/* 2870 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2871 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2872 */       return true;
/* 2873 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2874 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2875 */       ex.printStackTrace();
/* 2876 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean sendEMFWCommand() {
/* 2881 */     byte[] data = new byte[4];
/* 2882 */     byte[] tmp = Functions.get2ByteArrayFromInt(32774);
/* 2883 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2884 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2885 */     data[2] = 1;
/* 2886 */     data[3] = 5;
/*      */     try {
/* 2888 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2889 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2890 */       return true;
/* 2891 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2892 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2893 */       ex.printStackTrace();
/* 2894 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public void sendLogoutCommand() {
/* 2899 */     byte[] data = new byte[3];
/* 2900 */     byte[] tmp = Functions.get2ByteArrayFromInt(32800);
/* 2901 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2902 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2903 */     data[2] = 0;
/*      */     try {
/* 2905 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2906 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2907 */       monitorThread();
/* 2908 */       byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2909 */       if (resp != null && resp[0] == 6) {
/* 2910 */         this.idleTimeout = 0L;
/*      */       }
/* 2912 */       dispose();
/* 2913 */     } catch (IOException|InterruptedException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2914 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2915 */       ex.printStackTrace();
/*      */     } 
/*      */   }
/*      */   
/*      */   public byte[] findFileIDsByCRCMismatch(ModuleCFG mCFG, boolean readMode) {
/* 2920 */     byte[] data = new byte[3];
/* 2921 */     byte[] tmp = Functions.get2ByteArrayFromInt(32799);
/* 2922 */     tmp = Functions.swapLSB2MSB(tmp);
/* 2923 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 2924 */     data[2] = 0;
/*      */     
/*      */     try {
/* 2927 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 2928 */       SocketFunctions.send(this.clientSocket, tmp);
/* 2929 */       monitorThread();
/* 2930 */       byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 4);
/* 2931 */       if (buffer.length == 4) {
/* 2932 */         int crc32Recv = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(buffer));
/* 2933 */         if (mCFG.getCrc32() != crc32Recv) {
/* 2934 */           SocketFunctions.send(this.clientSocket, new byte[] { 8 });
/* 2935 */           monitorThread();
/* 2936 */           buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 2937 */           int noOfFiles = buffer[0] & 0xFF;
/* 2938 */           buffer = SocketFunctions.receive(this.clientSocket, 0, noOfFiles * 5);
/* 2939 */           this.deviceCRC32 = GriffonHandlerHelper.buildCRC32FromReceivedBuffer(this.productID, this.deviceCRC32, buffer, false, noOfFiles);
/* 2940 */           byte[] fileIDData = GriffonHandlerHelper.prepareFileDataByCRC32Mismatch(this.productID, this.deviceCRC32, mCFG, readMode);
/* 2941 */           return fileIDData;
/*      */         } 
/* 2943 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*      */       }
/*      */     
/* 2946 */     } catch (IOException|InterruptedException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 2947 */       Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 2948 */       ex.printStackTrace();
/* 2949 */       return null;
/*      */     } 
/* 2951 */     return null;
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\GriffonHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */