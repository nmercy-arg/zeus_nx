/*      */ package com.zeusServer.mercurius;
/*      */ 
/*      */ import com.zeus.mercuriusAVL.derby.beans.AudioNJSFileInfo;
/*      */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.griffon.GriffonHandlerHelper;
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
/*      */ import com.zeusServer.util.SocketFunctions;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import com.zeusServer.util.ZeusServerLogger;
/*      */ import com.zeuscc.griffon.derby.beans.ModuleCFG;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
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
/*      */ 
/*      */ 
/*      */ public class MercuriusHandler
/*      */   extends MercuriusAVLRoutines
/*      */   implements Runnable
/*      */ {
/*      */   private Socket clientSocket;
/*      */   private short encType;
/*   66 */   private String myThreadGuid = UUID.randomUUID().toString();
/*   67 */   private String sn = "";
/*      */   private String remoteIP;
/*      */   private boolean initialMsgFlag;
/*      */   private boolean initiatedGeofenceRequest = false;
/*      */   private byte[] prod;
/*      */   private int geofenceCRC32;
/*   73 */   private Logger ownLogger = null;
/*      */ 
/*      */   
/*      */   public MercuriusHandler(Socket clientSocket, short encType, byte[] prod) throws SocketException {
/*   77 */     this.clientSocket = clientSocket;
/*   78 */     this.encType = encType;
/*   79 */     this.clientSocket.setSoTimeout(30000);
/*   80 */     this.clientSocket.setTcpNoDelay(false);
/*   81 */     this.prod = prod;
/*   82 */     this.initialMsgFlag = true;
/*   83 */     this.remoteIP = this.clientSocket.getRemoteSocketAddress().toString();
/*   84 */     this.remoteIP = this.remoteIP.substring(1);
/*   85 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*   86 */     Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ 
/*      */   
/*      */   public void run() {
/*      */     
/*   92 */     try { int bytesReceived = 0;
/*      */       
/*   94 */       byte[] decBlock = null;
/*      */       
/*   96 */       while (isSocketConnected() && keepThreadRunning() && this.idleTimeout > System.currentTimeMillis()) {
/*   97 */         if (this.clientSocket.getInputStream().available() > 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*   98 */           if (this.clientSocket.getInputStream().available() == 1) {
/*   99 */             byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  100 */             if (buffer.length == 1) {
/*  101 */               if ((buffer[0] & 0x7) == 4) {
/*  102 */                 if (this.initiatedGeofenceRequest ? 
/*  103 */                   !initiateGeofenceDataRequest() : 
/*      */ 
/*      */ 
/*      */                   
/*  107 */                   !processCommandPacket()) {
/*      */                   break;
/*      */                 }
/*      */               } else {
/*      */                 
/*  112 */                 bytesReceived++;
/*      */               } 
/*      */             }
/*  115 */           } else if (this.clientSocket.getInputStream().available() > 1 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  116 */             if (!this.initialMsgFlag) {
/*  117 */               this.prod = Functions.swapLSB2MSB(SocketFunctions.receive(this.clientSocket, 0, 2));
/*  118 */               String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.prod[0] & 0xFF) }).replace(' ', '0');
/*  119 */               this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  120 */               prodBin = prodBin.substring(2);
/*  121 */               prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.prod[1] & 0xFF) }).replace(' ', '0'));
/*  122 */               short prodI = Short.parseShort(prodBin, 2);
/*  123 */               if (this.prod[0] == 43 && this.prod[1] == 43) {
/*  124 */                 dispose();
/*      */                 break;
/*      */               } 
/*  127 */               if (prodI != Util.EnumProductIDs.MERCURIUS.getProductId() && this.prod[0] != 43 && this.prod[1] != 43) {
/*      */                 
/*  129 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.sn + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_TCP"), Enums.EnumMessagePriority.HIGH, null, null);
/*      */                 try {
/*  131 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  132 */                 } catch (IOException ex) {}
/*      */                 
/*      */                 break;
/*      */               } 
/*      */             } 
/*      */             
/*  138 */             if (this.encType != 1) {
/*  139 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Module_Connected_with_Invalid_Encryption"), Enums.EnumMessagePriority.AVERAGE, this.remoteIP, null);
/*      */               break;
/*      */             } 
/*  142 */             byte[] length = Functions.swapLSB2MSB(SocketFunctions.receive(this.clientSocket, 0, 2));
/*  143 */             int msgLen = Functions.getIntFrom2ByteArray(length);
/*  144 */             byte[] data = SocketFunctions.receive(this.clientSocket, 0, msgLen);
/*  145 */             byte[] cbits = new byte[msgLen + 2];
/*  146 */             cbits[0] = this.prod[1];
/*  147 */             cbits[1] = this.prod[0];
/*  148 */             cbits[2] = length[1];
/*  149 */             cbits[3] = length[0];
/*  150 */             System.arraycopy(data, 0, cbits, 4, msgLen - 2);
/*  151 */             int crcCalc = CRC16.calculate(cbits, 0, msgLen + 2, 65535);
/*  152 */             byte[] crcbits = new byte[2];
/*  153 */             crcbits[0] = data[msgLen - 2];
/*  154 */             crcbits[1] = data[msgLen - 1];
/*  155 */             crcbits = Functions.swapLSB2MSB(crcbits);
/*  156 */             int crcRecv = Functions.getIntFrom2ByteArray(crcbits);
/*  157 */             if (crcCalc == crcRecv) {
/*      */               
/*  159 */               byte[] encData = new byte[msgLen - 2];
/*  160 */               System.arraycopy(data, 0, encData, 0, msgLen - 2);
/*  161 */               byte[] decData = new byte[msgLen - 2];
/*  162 */               byte[] block = new byte[16];
/*  163 */               if (encData.length >= 16 && encData.length % 16 == 0) {
/*  164 */                 for (int i = 0; i < encData.length; ) {
/*  165 */                   System.arraycopy(encData, i, block, 0, 16);
/*  166 */                   if (this.encType == 1) {
/*  167 */                     decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */                   }
/*  169 */                   System.arraycopy(decBlock, 0, decData, i, 16);
/*  170 */                   i += 16;
/*      */                 } 
/*      */               }
/*  173 */               if (!parseM2SPacket(decData)) {
/*      */                 break;
/*      */               }
/*      */             } else {
/*  177 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*      */               try {
/*  179 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  180 */               } catch (IOException ex) {
/*      */                 break;
/*      */               } 
/*      */             } 
/*  184 */             bytesReceived = 0;
/*  185 */             if (this.initialMsgFlag) {
/*  186 */               this.initialMsgFlag = false;
/*      */             }
/*      */           } 
/*  189 */         } else if (bytesReceived == 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && 
/*  190 */           TblMercuriusActiveConnections.getInstance().containsKey(this.sn) && 
/*  191 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).newCommand) {
/*  192 */           byte[] newCmd = Functions.intToByteArray(128, 1);
/*  193 */           Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */           try {
/*  195 */             SocketFunctions.send(this.clientSocket, newCmd);
/*  196 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  197 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*  198 */           } catch (IOException ex) {
/*      */             break;
/*      */           } 
/*      */         } 
/*      */ 
/*      */         
/*  204 */         sleepThread();
/*      */       }  }
/*  206 */     catch (InterruptedException interruptedException)
/*      */     
/*      */     { 
/*      */       
/*      */       try { 
/*      */ 
/*      */         
/*  213 */         if (this.clientSocket != null) {
/*  214 */           this.clientSocket.close();
/*      */         }
/*  216 */         dispose(); }
/*  217 */       catch (IOException ex)
/*  218 */       { Logger.getLogger(MercuriusHandler.class.getName()).log(Level.SEVERE, (String)null, ex); }  } catch (IOException|NumberFormatException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) { ex.printStackTrace(); Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_in_the_Data_Server_task"), Enums.EnumMessagePriority.HIGH, this.sn, ex); } finally { try { if (this.clientSocket != null) this.clientSocket.close();  dispose(); } catch (IOException ex) { Logger.getLogger(MercuriusHandler.class.getName()).log(Level.SEVERE, (String)null, ex); }
/*      */        }
/*      */   
/*      */   }
/*      */   
/*      */   private boolean isSocketConnected() {
/*  224 */     if (this.clientSocket.isConnected()) {
/*  225 */       if (TblMercuriusActiveConnections.getInstance().containsKey(this.sn)) {
/*  226 */         return !((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).fecharConexao;
/*      */       }
/*  228 */       return true;
/*      */     } 
/*      */     
/*  231 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean keepThreadRunning() {
/*  236 */     if (TblMercuriusActiveConnections.getInstance().containsKey(this.sn)) {
/*  237 */       return this.myThreadGuid.equals(((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).ownerThreadGuid);
/*      */     }
/*  239 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   private void monitorThread() throws InterruptedException, IOException {
/*  244 */     for (byte ii = 0; ii < 30; ii = (byte)(ii + 1)) {
/*  245 */       Thread.sleep(1000L);
/*  246 */       if (this.clientSocket.getInputStream().available() > 0) {
/*      */         break;
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void sleepThread() throws InterruptedException, IOException {
/*  253 */     for (byte ii = 0; ii < 2; ii = (byte)(ii + 1)) {
/*  254 */       Thread.sleep(2500L);
/*  255 */       if (this.clientSocket.getInputStream().available() > 0) {
/*      */         break;
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void dispose() {
/*  262 */     this.clientSocket = SocketFunctions.closeSocket(this.clientSocket);
/*  263 */     if (TblMercuriusActiveConnections.getInstance().containsKey(this.sn) && 
/*  264 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).ownerThreadGuid)) {
/*  265 */       TblMercuriusActiveConnections.removeConnection(this.sn);
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean parseM2SPacket(byte[] decData) {
/*      */     try {
/*  272 */       TblMercuriusActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/*  273 */       this.module = new MercuriusAVLModule();
/*  274 */       this.module.setDefaults();
/*  275 */       this.module.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0));
/*  276 */       this.module.setM2sData(decData);
/*  277 */       this.module.setLastNWProtocol(Enums.EnumNWProtocol.TCP.name());
/*      */       
/*  279 */       if (!this.initialMsgFlag) {
/*  280 */         this.module.setSn(this.sn);
/*      */       }
/*      */       
/*  283 */       int index = 0;
/*  284 */       byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  292 */       byte[] tmp2 = new byte[2];
/*      */ 
/*      */       
/*  295 */       while (index < decData.length && 
/*  296 */         index + 2 <= decData.length) {
/*      */         byte[] fcon; short simNum, apn, tmp; char[] bin; StringBuilder sb; int i;
/*      */         byte[] oper;
/*  299 */         System.arraycopy(decData, index, fid, 0, 2);
/*  300 */         index += 2;
/*  301 */         fid = Functions.swapLSB2MSB(fid);
/*  302 */         int fidVal = Functions.getIntFrom2ByteArray(fid);
/*  303 */         if (fidVal <= 0) {
/*      */           break;
/*      */         }
/*  306 */         short flen = (short)Functions.getIntFromHexByte(decData[index]);
/*      */         try {
/*  308 */           fcon = new byte[flen];
/*  309 */           System.arraycopy(decData, ++index, fcon, 0, flen);
/*  310 */           index += flen;
/*  311 */         } catch (ArrayIndexOutOfBoundsException ace) {
/*  312 */           Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "M2S Packet received with wrong length information ", Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  313 */           StringBuilder stringBuilder = new StringBuilder();
/*  314 */           for (byte bb : decData) {
/*  315 */             stringBuilder.append(bb).append(" ");
/*      */           }
/*  317 */           Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "Packet: " + stringBuilder.toString(), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  318 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  319 */           return true;
/*      */         } 
/*  321 */         switch (fidVal) {
/*      */           case 1:
/*  323 */             this.sn = Functions.getASCIIFromByteArray(fcon);
/*  324 */             this.module.setSn(this.sn);
/*  325 */             if (this.sn.equals("0000000000")) {
/*  326 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  327 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  328 */               return false;
/*  329 */             }  if (this.sn.equals("0000000001")) {
/*  330 */               String ip = this.remoteIP.substring(0, this.remoteIP.indexOf(":"));
/*  331 */               if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  332 */                 if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/*  333 */                   Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  334 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  335 */                   return false;
/*      */                 }  continue;
/*      */               } 
/*  338 */               if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/*  339 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  340 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  341 */                 return false;
/*      */               } 
/*      */             } 
/*      */ 
/*      */ 
/*      */           
/*      */           case 2:
/*  348 */             if ((fcon[0] & 0xFF) == 1) {
/*  349 */               this.module.setModemModel(fcon[2] & 0xFF); continue;
/*  350 */             }  if ((fcon[1] & 0xFF) == 1) {
/*  351 */               this.module.setGpsModel(fcon[3] & 0xFF);
/*      */             }
/*      */ 
/*      */           
/*      */           case 3:
/*  356 */             sb = new StringBuilder();
/*  357 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1])).append('.').append(Functions.getIntFromHexByte(fcon[2]));
/*  358 */             this.module.setModuleFWVersion(sb.toString());
/*      */ 
/*      */           
/*      */           case 4:
/*  362 */             sb = new StringBuilder();
/*  363 */             tmp2[0] = fcon[2];
/*  364 */             tmp2[1] = fcon[3];
/*  365 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFrom2ByteArray(tmp2));
/*  366 */             this.module.setModemFWVersion(sb.toString());
/*      */ 
/*      */           
/*      */           case 5:
/*  370 */             sb = new StringBuilder();
/*  371 */             for (i = 0; i < flen - 1; i++) {
/*  372 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  374 */             sb.append(fcon[7] / 10);
/*  375 */             this.module.setModemIMEI(sb.toString());
/*      */ 
/*      */           
/*      */           case 6:
/*  379 */             sb = new StringBuilder();
/*  380 */             for (i = 1; i < flen; i++) {
/*  381 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  383 */             if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  384 */               this.module.setSim_iccid_1(sb.toString()); continue;
/*  385 */             }  if (Functions.getIntFromHexByte(fcon[0]) == 2) {
/*  386 */               this.module.setSim_iccid_2(sb.toString());
/*      */             }
/*      */ 
/*      */           
/*      */           case 7:
/*  391 */             sb = new StringBuilder();
/*  392 */             for (i = 1; i < flen - 1; i++) {
/*  393 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  395 */             sb.append(fcon[8] / 10);
/*  396 */             if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  397 */               this.module.setSim_imsi_1(sb.toString()); continue;
/*  398 */             }  if (Functions.getIntFromHexByte(fcon[0]) == 2) {
/*  399 */               this.module.setSim_imsi_2(sb.toString());
/*      */             }
/*      */ 
/*      */           
/*      */           case 8:
/*  404 */             oper = new byte[flen - 1];
/*  405 */             System.arraycopy(fcon, 1, oper, 0, flen - 1);
/*  406 */             if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  407 */               this.module.setSim_operator_1(Functions.getASCIIFromByteArray(oper)); continue;
/*  408 */             }  if (Functions.getIntFromHexByte(fcon[0]) == 2) {
/*  409 */               this.module.setSim_operator_2(Functions.getASCIIFromByteArray(oper));
/*      */             }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 10:
/*  417 */             this.lastCommIface = (short)Functions.getIntFromHexByte(fcon[0]);
/*  418 */             this.module.setCurrentInterface(this.lastCommIface);
/*      */ 
/*      */           
/*      */           case 11:
/*  422 */             this.module.setCurrentSIM(Functions.getIntFromHexByte(fcon[0]));
/*  423 */             this.module.setCurrentAPN(Functions.getIntFromHexByte(fcon[1]));
/*      */ 
/*      */           
/*      */           case 12:
/*  427 */             this.module.setGsmSignalLevel(Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */           
/*      */           case 14:
/*  431 */             this.module.setGprsDataVolume(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*      */ 
/*      */           
/*      */           case 15:
/*  435 */             bin = Functions.getBinaryFromByte(fcon[0]);
/*  436 */             sb = new StringBuilder();
/*  437 */             sb.append(bin[1]).append(bin[2]).append(bin[3]);
/*  438 */             simNum = (short)Integer.parseInt(sb.toString(), 2);
/*  439 */             sb = new StringBuilder();
/*  440 */             sb.append(bin[4]).append(bin[5]);
/*  441 */             apn = (short)Integer.parseInt(sb.toString(), 2);
/*  442 */             sb = new StringBuilder();
/*  443 */             sb.append(bin[6]).append(bin[7]);
/*  444 */             tmp = (short)Integer.parseInt(sb.toString(), 2);
/*  445 */             if (bin[0] == '0') {
/*  446 */               this.module.setSimCard1Status(simNum);
/*  447 */               this.module.setSimCard1OperativeStatus(apn);
/*  448 */               this.module.setSimCard1JDRStatus(tmp); continue;
/*  449 */             }  if (bin[0] == '1') {
/*  450 */               this.module.setSimCard2Status(simNum);
/*  451 */               this.module.setSimCard2OperativeStatus(apn);
/*  452 */               this.module.setSimCard2JDRStatus(tmp);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 16:
/*  457 */             tmp2[0] = fcon[1];
/*  458 */             tmp2[1] = fcon[0];
/*  459 */             this.timezone = Functions.getSignedIntFrom2ByteArray(tmp2);
/*  460 */             this.module.setTimezone(this.timezone);
/*  461 */             if ((fcon[2] & 0xFF) == 1) {
/*  462 */               this.module.setTimeSync(true);
/*      */             }
/*      */ 
/*      */           
/*      */           case 17:
/*  467 */             this.module.setInitialPacket(true);
/*  468 */             this.module.setGeofencecrc32(Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(fcon)));
/*  469 */             this.geofenceCRC32 = this.module.getGeofencecrc32();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 20:
/*  476 */             this.module.setSatelliteCount(Functions.getIntFromHexByte(fcon[0]));
/*      */ 
/*      */           
/*      */           case 21:
/*  480 */             tmp2[0] = fcon[1];
/*  481 */             tmp2[1] = fcon[0];
/*  482 */             this.module.setModuleHWDtls(Functions.getIntFrom2ByteArray(tmp2));
/*      */ 
/*      */           
/*      */           case 100:
/*  486 */             this.module.setCurrentSchedule((short)(Functions.getIntFromHexByte(fcon[4]) + 1));
/*  487 */             if (flen == 17) {
/*  488 */               byte[] gpsData = new byte[12];
/*  489 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  490 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 101:
/*  495 */             tmp2 = Functions.getHighLowBytes((short)Functions.getIntFromHexByte(fcon[4]));
/*  496 */             if (flen == 17) {
/*  497 */               byte[] gpsData = new byte[12];
/*  498 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  499 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 102:
/*  504 */             if (flen == 17) {
/*  505 */               byte[] gpsData = new byte[12];
/*  506 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  507 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 103:
/*  512 */             if (flen == 17) {
/*  513 */               byte[] gpsData = new byte[12];
/*  514 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  515 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 104:
/*  520 */             if (flen == 17) {
/*  521 */               byte[] gpsData = new byte[12];
/*  522 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  523 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 105:
/*  528 */             if (flen == 20) {
/*  529 */               byte[] gpsData = new byte[12];
/*  530 */               System.arraycopy(fcon, 8, gpsData, 0, 12);
/*  531 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 106:
/*  536 */             if (flen == 17) {
/*  537 */               byte[] gpsData = new byte[12];
/*  538 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  539 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 107:
/*  544 */             if (flen == 17) {
/*  545 */               byte[] gpsData = new byte[12];
/*  546 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  547 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 108:
/*  552 */             if (flen == 17) {
/*  553 */               byte[] gpsData = new byte[12];
/*  554 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  555 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */ 
/*      */           
/*      */           case 109:
/*  560 */             if (flen == 17) {
/*  561 */               byte[] gpsData = new byte[12];
/*  562 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  563 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */           
/*      */           case 110:
/*  567 */             if (flen == 17) {
/*  568 */               byte[] gpsData = new byte[12];
/*  569 */               System.arraycopy(fcon, 5, gpsData, 0, 12);
/*  570 */               Functions.parseGpsData(gpsData, this.module);
/*      */             } 
/*      */         } 
/*      */ 
/*      */       
/*      */       } 
/*  576 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.sn + LocaleMessage.getLocaleMessage("]_M2S_packet_received") + Functions.getMercuriusIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/*  577 */       this.module.setTimezone(this.timezone);
/*  578 */       this.module.setAutoRegistration((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/*  579 */       this.module.setIp(this.remoteIP.substring(0, this.remoteIP.indexOf(":")));
/*      */       try {
/*  581 */         TblMercuriusActiveConnections.semaphoreAlivePacketsReceived.acquire();
/*  582 */         this.module = MercuriusDBManager.executeSPM_001(this.module);
/*      */       } finally {
/*  584 */         TblMercuriusActiveConnections.semaphoreAlivePacketsReceived.release();
/*      */       } 
/*  586 */       if (this.module != null) {
/*  587 */         if (this.module.getAuto_Registration_Executed() == 1) {
/*  588 */           if (this.module.getRegistered() == 0) {
/*  589 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + this.sn, Enums.EnumMessagePriority.HIGH, null, null);
/*  590 */             SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/*  591 */             return false;
/*      */           } 
/*  593 */         } else if (this.module.getRegistered() == 1) {
/*  594 */           if (this.module.getEnabled() == 0) {
/*  595 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.sn + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  596 */             SocketFunctions.send(this.clientSocket, new byte[] { -31 });
/*  597 */             return false;
/*      */           } 
/*      */         } else {
/*  600 */           Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.sn + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  601 */           SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/*  602 */           return false;
/*      */         } 
/*      */         
/*  605 */         Functions.generateEventReceptionAlivePacket(Util.EnumProductIDs.MERCURIUS.getProductId(), this.module.getId_Client(), this.module.getId_Module(), this.module.getId_Group(), this.module.getClientCode(), this.module.getE_Alive_Received(), this.module.getF_Alive_Received(), this.module.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.TCP.name(), this.lastCommIface, -1);
/*  606 */         if (this.initialMsgFlag) {
/*  607 */           if (TblMercuriusActiveConnections.getInstance().containsKey(this.sn)) {
/*  608 */             TblMercuriusActiveConnections.removeConnection(this.sn);
/*      */           }
/*  610 */           TblMercuriusActiveConnections.addConnection(this.sn, this.myThreadGuid);
/*  611 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idClient = this.module.getId_Client();
/*  612 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idModule = this.module.getId_Module();
/*      */         } 
/*  614 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idGroup = this.module.getId_Group();
/*  615 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).clientName = this.module.getName();
/*  616 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).communicationDebug = (this.module.getCommDebug() == 1);
/*  617 */         ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).communicationTimeout = this.module.getComm_Timeout() * 1000;
/*  618 */         this.idleTimeout = System.currentTimeMillis() + (this.module.getComm_Timeout() * 1000);
/*      */         
/*  620 */         if (this.module.getCommLog() == 1 && this.module.getCommLogEnabledDate() != null) {
/*  621 */           Calendar sys = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/*  622 */           if (sys.get(5) - this.module.getCommLogEnabledDate().get(5) > 30) {
/*  623 */             MercuriusDBManager.disableCommunicationLog(this.module.getId_Module());
/*  624 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).commLog = false;
/*  625 */             if (this.ownLogger != null) {
/*  626 */               this.ownLogger = null;
/*      */             }
/*      */           } else {
/*  629 */             ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).commLog = true;
/*  630 */             if (this.ownLogger == null && this.sn != null) {
/*  631 */               this.ownLogger = ZeusServerLogger.getDeviceLogger("Mercurius/", this.sn);
/*      */             }
/*  633 */             if (this.ownLogger != null) {
/*  634 */               Functions.logMercuriusIncomingPacket(this.ownLogger, decData);
/*      */             }
/*      */           } 
/*      */         } else {
/*  638 */           ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).commLog = false;
/*  639 */           if (this.ownLogger != null) {
/*  640 */             this.ownLogger = null;
/*      */           }
/*      */         } 
/*      */         
/*      */         try {
/*  645 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  646 */           if (this.module.isInitialPacket() && !this.module.isCrc32Matched()) {
/*  647 */             Thread.sleep(100L);
/*  648 */             SocketFunctions.send(this.clientSocket, new byte[] { Byte.MIN_VALUE });
/*  649 */             this.initiatedGeofenceRequest = true;
/*      */           } 
/*  651 */           return true;
/*  652 */         } catch (IOException|InterruptedException ex) {
/*  653 */           return false;
/*      */         } 
/*      */       } 
/*  656 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Error_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  657 */       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  658 */       return false;
/*      */     }
/*  660 */     catch (Exception ex) {
/*  661 */       ex.printStackTrace();
/*  662 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */       try {
/*  664 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  665 */       } catch (IOException iOException) {}
/*      */       
/*  667 */       return false;
/*      */     } finally {
/*  669 */       TblMercuriusActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean processCommandPacket() {
/*  674 */     SP_024DataHolder sp24DH = null;
/*      */     try {
/*  676 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  677 */       List<SP_024DataHolder> cmdsList = MercuriusDBManager.executeSP_024(((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idModule);
/*  678 */       int count = 0;
/*  679 */       int cmdsSize = cmdsList.size();
/*      */       
/*  681 */       while (isSocketConnected() && count < cmdsSize) {
/*  682 */         sp24DH = cmdsList.get(count++);
/*  683 */         if (MercuriusDBManager.isCommandCancelled(sp24DH.getId_Command())) {
/*      */           continue;
/*      */         }
/*  686 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Sending_command") + sp24DH.getCommand_Type() + ":" + sp24DH.getCommandData(), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  687 */         MercuriusDBManager.updateCommandStatus(sp24DH.getId_Command());
/*      */ 
/*      */         
/*  690 */         if ((sp24DH.getCommand_Type() == 32769 || sp24DH.getCommand_Type() == 32770) && 
/*  691 */           sp24DH.getCommandData().charAt(0) == '1') {
/*  692 */           byte[] encBlock = new byte[16];
/*      */ 
/*      */           
/*  695 */           byte[] fileContent = sp24DH.getCommandFileData();
/*  696 */           System.arraycopy(fileContent, 0, encBlock, 0, 16);
/*  697 */           byte[] decBlock = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/*  698 */           System.arraycopy(decBlock, 0, fileContent, 0, 16);
/*      */           
/*  700 */           byte[] tmp4 = new byte[4];
/*  701 */           tmp4[3] = fileContent[4];
/*  702 */           tmp4[2] = fileContent[5];
/*  703 */           tmp4[1] = fileContent[6];
/*  704 */           tmp4[0] = fileContent[7];
/*  705 */           int upFileLen = Functions.getIntFrom4ByteArray(tmp4);
/*      */           
/*  707 */           int lPad = (upFileLen + 12) % 16;
/*  708 */           if (lPad > 0) {
/*  709 */             lPad = 16 - lPad;
/*      */           }
/*      */           
/*  712 */           int tmpUPFileLen = upFileLen + lPad + 12;
/*      */           
/*  714 */           byte[] tm = new byte[tmpUPFileLen];
/*  715 */           System.arraycopy(fileContent, 0, tm, 0, tmpUPFileLen);
/*      */           
/*  717 */           if (tm.length >= 16 && tmpUPFileLen % 16 == 0) {
/*  718 */             for (int i = 16; i < tmpUPFileLen; ) {
/*  719 */               System.arraycopy(tm, i, encBlock, 0, 16);
/*  720 */               decBlock = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/*  721 */               System.arraycopy(decBlock, 0, tm, i, 16);
/*  722 */               i += 16;
/*      */             } 
/*      */           }
/*  725 */           ModuleCFG mCFG = ConfigFileParser.arrangeCFGFile(tm, tmpUPFileLen - lPad, true);
/*      */           
/*  727 */           if (fileContent.length > tmpUPFileLen) {
/*  728 */             byte[] vmc = new byte[fileContent.length - tmpUPFileLen];
/*  729 */             System.arraycopy(fileContent, tmpUPFileLen, vmc, 0, fileContent.length - tmpUPFileLen);
/*  730 */             File ajsFile = Functions.writeByteArrayToFile("MRCS_STRD_VMJS_" + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idModule, vmc);
/*  731 */             int uploadLookupCRC32 = ((Integer)mCFG.getCrc32List().get(2)).intValue();
/*  732 */             if (requestAudioJSInfo(32790)) {
/*  733 */               byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  734 */               if (buffer.length == 1 && 
/*  735 */                 buffer[0] == 6) {
/*  736 */                 byte[] scrc32 = SocketFunctions.receive(this.clientSocket, 0, 9);
/*  737 */                 int crcCalc = CRC16.calculate(scrc32, 0, 7, 65535);
/*  738 */                 buffer = new byte[2];
/*  739 */                 buffer[0] = scrc32[8];
/*  740 */                 buffer[1] = scrc32[7];
/*  741 */                 int crcRecv = Functions.getIntFrom2ByteArray(buffer);
/*  742 */                 if (crcCalc == crcRecv) {
/*  743 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  744 */                   tmp4[3] = scrc32[3];
/*  745 */                   tmp4[2] = scrc32[4];
/*  746 */                   tmp4[1] = scrc32[5];
/*  747 */                   tmp4[0] = scrc32[6];
/*  748 */                   int rcvLookupCRC32 = Functions.getIntFrom4ByteArray(tmp4);
/*  749 */                   if (rcvLookupCRC32 != uploadLookupCRC32) {
/*  750 */                     byte[] fName = new byte[12];
/*  751 */                     List<AudioNJSFileInfo> uploadedJSFileList = ConfigFileParser.getAudioScriptLookupDataFromCFG(null, 0, mCFG);
/*      */                     
/*  753 */                     if (requestAudioJSInfo(32773)) {
/*  754 */                       buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  755 */                       if (buffer.length == 1 && 
/*  756 */                         buffer[0] == 6) {
/*      */                         
/*  758 */                         byte[] arrayOfByte = new byte[2];
/*  759 */                         arrayOfByte[0] = 1;
/*  760 */                         arrayOfByte[1] = 3;
/*  761 */                         SocketFunctions.send(this.clientSocket, arrayOfByte);
/*  762 */                         monitorThread();
/*  763 */                         List<AudioNJSFileInfo> lookupFileList = receiveAudioJSLookUPData();
/*  764 */                         List<AudioNJSFileInfo> reqajsList = null;
/*  765 */                         if (lookupFileList == null) {
/*  766 */                           reqajsList = uploadedJSFileList;
/*  767 */                         } else if (uploadedJSFileList != null) {
/*  768 */                           reqajsList = new ArrayList<>();
/*  769 */                           for (AudioNJSFileInfo ajs : uploadedJSFileList) {
/*  770 */                             if (!lookupFileList.contains(ajs)) {
/*  771 */                               reqajsList.add(ajs);
/*      */                             }
/*      */                           } 
/*      */                         } 
/*  775 */                         if (reqajsList != null && reqajsList.size() > 0) {
/*  776 */                           RandomAccessFile raf = new RandomAccessFile(ajsFile, "r");
/*  777 */                           for (AudioNJSFileInfo rajs : reqajsList) {
/*  778 */                             int fpos = 0;
/*  779 */                             while (fpos < ajsFile.length()) {
/*  780 */                               raf.seek(fpos);
/*  781 */                               byte[] vmHeader = new byte[22];
/*  782 */                               raf.read(vmHeader);
/*  783 */                               fpos += 22;
/*  784 */                               System.arraycopy(vmHeader, 2, fName, 0, 12);
/*  785 */                               System.arraycopy(vmHeader, 14, tmp4, 0, 4);
/*  786 */                               int fle = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  787 */                               System.arraycopy(vmHeader, 18, tmp4, 0, 4);
/*  788 */                               int crc32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/*  789 */                               if ((new String(fName, "ISO-8859-1")).trim().toLowerCase().equals(rajs.getName().toLowerCase()) && 
/*  790 */                                 sendAudioJSCMD((rajs.getDir() == 1) ? 32792 : 32793)) {
/*  791 */                                 buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  792 */                                 if (buffer.length == 1 && 
/*  793 */                                   buffer[0] == 6) {
/*  794 */                                   byte[] vmcmd = new byte[19];
/*  795 */                                   vmcmd[0] = (byte)rajs.getDir();
/*  796 */                                   System.arraycopy(fName, 0, vmcmd, 1, fName.length);
/*  797 */                                   System.arraycopy(vmHeader, 14, vmcmd, 13, 4);
/*  798 */                                   crcCalc = CRC16.calculate(vmcmd, 0, 17, 65535);
/*  799 */                                   byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/*  800 */                                   System.arraycopy(tmp2, 0, vmcmd, 17, 2);
/*  801 */                                   SocketFunctions.send(this.clientSocket, vmcmd);
/*  802 */                                   monitorThread();
/*  803 */                                   buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  804 */                                   if (buffer != null && buffer[0] == 6) {
/*  805 */                                     raf.seek(fpos);
/*  806 */                                     sendAudioFile2Module(raf, fpos, fle, rajs.getCrc32(), rajs.getDir(), rajs.getName()); break;
/*      */                                   } 
/*  808 */                                   if (buffer == null || buffer[0] == 25);
/*      */                                 } 
/*      */                               } 
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
/*  822 */                               fpos += fle;
/*      */                             }
/*      */                           
/*      */                           }
/*      */                         
/*      */                         }
/*      */                       
/*      */                       } 
/*      */                     } 
/*      */                   } 
/*      */                 } else {
/*      */                   
/*  834 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                 } 
/*      */               } 
/*      */             } 
/*      */           } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  845 */           byte[] fileIDData = findFileIDsByCRCMismatch(mCFG);
/*  846 */           if (fileIDData == null || fileIDData.length == 0) {
/*  847 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  848 */             MercuriusAVLHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */             continue;
/*      */           } 
/*  851 */           sp24DH.setCommandFileData(ConfigFileParser.prepareRequiredFileDataForDeviceByCRCMismatch(mCFG, fileIDData));
/*      */         } 
/*      */         
/*  854 */         if (sendRemoteCommand(sp24DH)) {
/*  855 */           byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  856 */           if (buffer.length == 1) {
/*  857 */             if (buffer[0] == 6) {
/*  858 */               if (sp24DH.getCommand_Type() == 32769 || sp24DH.getCommand_Type() == 32770) {
/*  859 */                 if (sp24DH.getCommandData().charAt(0) == '3') {
/*  860 */                   byte[] fwLen = Functions.swapLSB2MSB4ByteArray(Functions.get4ByteArrayFromInt((sp24DH.getCommandFileData()).length));
/*  861 */                   int crcFw = CRC16.calculate(fwLen, 0, 4, 65535);
/*  862 */                   byte[] fwLenData = new byte[6];
/*  863 */                   System.arraycopy(fwLen, 0, fwLenData, 0, 4);
/*  864 */                   fwLen = Functions.get2ByteArrayFromInt(crcFw);
/*  865 */                   fwLenData[4] = fwLen[1];
/*  866 */                   fwLenData[5] = fwLen[0];
/*  867 */                   SocketFunctions.send(this.clientSocket, fwLenData);
/*  868 */                   monitorThread();
/*  869 */                   byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  870 */                   if (tmp[0] == 21) {
/*  871 */                     MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("CRC_Not_Matched_for_Firmware_file_(response") + tmp[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */                     continue;
/*      */                   } 
/*      */                 } 
/*  875 */                 sendFile2Module(sp24DH);
/*  876 */               } else if (sp24DH.getCommand_Type() == 32773) {
/*  877 */                 byte[] fileIDData = new byte[14];
/*  878 */                 fileIDData[0] = 13;
/*  879 */                 for (int kk = 1; kk <= 13; kk++) {
/*  880 */                   fileIDData[kk] = (byte)kk;
/*      */                 }
/*  882 */                 SocketFunctions.send(this.clientSocket, fileIDData);
/*  883 */                 monitorThread();
/*  884 */                 receiveFile4MModule(sp24DH);
/*  885 */               } else if (sp24DH.getCommand_Type() == 32787) {
/*  886 */                 buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  887 */                 if (buffer != null && buffer.length == 1) {
/*  888 */                   int fwLen = buffer[0] & 0xFF;
/*  889 */                   buffer = SocketFunctions.receive(this.clientSocket, 0, fwLen);
/*  890 */                   int custVersionLen = buffer[1] & 0xFF;
/*  891 */                   int sirfVersionLen = buffer[0] & 0xFF;
/*  892 */                   byte[] tmp = new byte[sirfVersionLen];
/*  893 */                   System.arraycopy(buffer, 2, tmp, 0, sirfVersionLen);
/*  894 */                   String sirfVersion = Functions.getASCIIFromByteArray(tmp);
/*  895 */                   tmp = new byte[custVersionLen];
/*  896 */                   System.arraycopy(buffer, sirfVersionLen + 2, tmp, 0, custVersionLen);
/*  897 */                   String custVersion = Functions.getASCIIFromByteArray(tmp);
/*  898 */                   MercuriusAVLHandlerHelper.updateGPSFWVersion(((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idModule, custVersion, sirfVersion);
/*  899 */                   MercuriusAVLHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */                 } 
/*      */               } else {
/*  902 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  903 */                 MercuriusAVLHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */               }
/*      */             
/*  906 */             } else if (buffer[0] == 21) {
/*  907 */               MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             } else {
/*  909 */               if (buffer[0] == 4) {
/*      */                 continue;
/*      */               }
/*  912 */               MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             }
/*      */           
/*      */           } else {
/*      */             
/*  917 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           } 
/*      */         } else {
/*  920 */           MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         } 
/*  922 */         Thread.sleep(100L);
/*      */       } 
/*  924 */       ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).newCommand = false;
/*  925 */       ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = 0L;
/*  926 */       this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).communicationTimeout;
/*      */       try {
/*  928 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  929 */         return true;
/*  930 */       } catch (IOException ex) {
/*  931 */         return false;
/*      */       } 
/*  933 */     } catch (Exception ex) {
/*  934 */       if (sp24DH != null) {
/*      */         try {
/*  936 */           MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, "", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*  937 */         } catch (SQLException|InterruptedException ex1) {
/*  938 */           Logger.getLogger(MercuriusHandler.class.getName()).log(Level.SEVERE, (String)null, ex1);
/*      */         } 
/*      */       }
/*  941 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_while_processing_the_COMMAND_packet"), Enums.EnumMessagePriority.HIGH, this.sn, ex);
/*      */       try {
/*  943 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  944 */       } catch (IOException iOException) {}
/*      */       
/*  946 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private byte[] findFileIDsByCRCMismatch(ModuleCFG mCFG) throws Exception {
/*  951 */     byte[] data = new byte[3];
/*  952 */     byte[] tmp = Functions.get2ByteArrayFromInt(32796);
/*  953 */     tmp = Functions.swapLSB2MSB(tmp);
/*  954 */     System.arraycopy(tmp, 0, data, 0, 2);
/*  955 */     data[2] = 0;
/*      */ 
/*      */     
/*  958 */     tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/*  959 */     SocketFunctions.send(this.clientSocket, tmp);
/*  960 */     monitorThread();
/*  961 */     byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  962 */     if (buffer != null && buffer[0] == 6) {
/*  963 */       monitorThread();
/*  964 */       buffer = SocketFunctions.receive(this.clientSocket, 0, 54);
/*  965 */       int crcCalc = CRC16.calculate(buffer, 0, 52, 65535);
/*  966 */       tmp = new byte[2];
/*  967 */       tmp[0] = buffer[53];
/*  968 */       tmp[1] = buffer[52];
/*  969 */       int crcRecv = Functions.getIntFrom2ByteArray(tmp);
/*  970 */       if (crcCalc == crcRecv) {
/*  971 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  972 */         List<Integer> devCRC32List = ConfigFileParser.buildCRC32FromReceivedBuffer(buffer);
/*  973 */         return ConfigFileParser.prepareFileIDSByCRC32Mismatch(mCFG, devCRC32List);
/*      */       } 
/*      */     } 
/*  976 */     return null;
/*      */   }
/*      */   
/*      */   private void sendAudioFile2Module(RandomAccessFile raf, int currPos, int vmLen, int crc32, int dir, String vmFileName) throws IOException, InterruptedException, SQLException, Exception {
/*  980 */     long flen = vmLen;
/*      */     
/*  982 */     int blockIndex = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  987 */     short maxRetries = 3;
/*  988 */     short retry = 0;
/*  989 */     FileChannel fc = raf.getChannel();
/*      */     
/*  991 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */     
/*  993 */     label50: while (isSocketConnected() && fc.position() < currPos + flen) {
/*      */       
/*  995 */       for (retry = (short)(retry + 1); retry < maxRetries; ) {
/*  996 */         int blockLength = (int)((currPos + flen - fc.position() > 240L) ? 240L : (currPos + flen - fc.position()));
/*  997 */         ByteBuffer blockBuf = ByteBuffer.allocate(blockLength);
/*  998 */         if (fc.read(blockBuf) == blockLength) {
/*  999 */           byte[] block = blockBuf.array();
/* 1000 */           byte[] packet = new byte[blockLength + 5];
/* 1001 */           byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(blockIndex));
/* 1002 */           System.arraycopy(tmp, 0, packet, 0, 2);
/* 1003 */           packet[2] = (byte)Integer.parseInt(Integer.toHexString(blockLength), 16);
/* 1004 */           System.arraycopy(block, 0, packet, 3, blockLength);
/* 1005 */           int crcCalc = CRC16.calculate(packet, 0, blockLength + 3, 65535);
/* 1006 */           tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1007 */           System.arraycopy(tmp, 0, packet, blockLength + 3, 2);
/*      */           try {
/* 1009 */             SocketFunctions.send(this.clientSocket, packet);
/* 1010 */             Thread.sleep(50L);
/*      */             try {
/* 1012 */               if (retry - 1 == 0) {
/* 1013 */                 this.clientSocket.setSoTimeout(120000);
/* 1014 */               } else if (retry - 1 == 1) {
/* 1015 */                 this.clientSocket.setSoTimeout(210000);
/* 1016 */               } else if (retry - 1 == 2) {
/* 1017 */                 this.clientSocket.setSoTimeout(300000);
/*      */               } 
/*      */               
/* 1020 */               if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1021 */                 MercuriusAVLHandlerHelper.updateLastCommunicationModuleData(this.sn, this.lastCommIface, this.currentSIM);
/* 1022 */                 nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 1024 */               tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1025 */               if (tmp[0] == 6) {
/* 1026 */                 retry = 0;
/* 1027 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + vmFileName + " (" + (currPos + flen - fc.position()) + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1028 */                 blockIndex++; break;
/*      */               } 
/* 1030 */               if (tmp[0] == 21) {
/*      */                 
/* 1032 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Invalid_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + vmFileName + " :" + tmp[0], Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1033 */                 fc.position(fc.position() - blockLength); break;
/*      */               } 
/* 1035 */               if ((tmp[0] & 0xFF) == 4) {
/*      */                 break label50;
/*      */               }
/* 1038 */             } catch (SocketException ex) {
/*      */ 
/*      */               
/* 1041 */               fc.position(fc.position() - blockLength);
/* 1042 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + vmFileName, Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/* 1044 */           } catch (IOException|InterruptedException|SQLException ex) {
/* 1045 */             ex.printStackTrace();
/*      */             
/*      */             break label50;
/*      */           } 
/* 1049 */           Thread.sleep(100L);
/*      */           continue;
/*      */         } 
/*      */         break label50;
/*      */       } 
/*      */     } 
/* 1055 */     if (fc.position() >= currPos + flen) {
/* 1056 */       monitorThread();
/* 1057 */       byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1058 */       if (tmp[0] == 6) {
/* 1059 */         raf.seek(currPos);
/* 1060 */         byte[] vmContent = new byte[vmLen];
/* 1061 */         raf.read(vmContent);
/* 1062 */         MercuriusDBManager.saveVoiceMessage(vmContent, vmLen, vmFileName, crc32, dir);
/* 1063 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("The_file_[") + vmFileName + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   private List<AudioNJSFileInfo> receiveAudioJSLookUPData() throws Exception {
/* 1069 */     List<AudioNJSFileInfo> uploadedJSFileList = null;
/* 1070 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1071 */     short retry = 0;
/*      */     
/* 1073 */     byte[] tmp2 = new byte[2];
/* 1074 */     byte[] tmp4 = new byte[4];
/*      */ 
/*      */     
/* 1077 */     byte[] fileContent = null;
/* 1078 */     int fileContentIndex = 0;
/*      */ 
/*      */     
/* 1081 */     int expBlockIndex = 0;
/* 1082 */     int flen = 0;
/* 1083 */     int lPad = 0;
/* 1084 */     int recvCfgCRC32 = 0;
/*      */     
/*      */     try {
/* 1087 */       if (isSocketConnected()) {
/*      */         
/* 1089 */         for (retry = (short)(retry + 1); retry < 3; ) {
/*      */           try {
/* 1091 */             byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1092 */             int blockLength = tmp[2] & 0xFF;
/* 1093 */             tmp2[0] = tmp[1];
/* 1094 */             tmp2[1] = tmp[0];
/* 1095 */             int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1096 */             if (expBlockIndex == rcvdBlockIndex) {
/* 1097 */               byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1098 */               byte[] packet = new byte[blockLength + 3];
/* 1099 */               System.arraycopy(tmp, 0, packet, 0, 3);
/* 1100 */               System.arraycopy(block, 0, packet, 3, blockLength);
/* 1101 */               int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1102 */               System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1103 */               int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1104 */               if (crcCalc == crcRecv) {
/* 1105 */                 if (expBlockIndex == 0) {
/* 1106 */                   byte[] first16 = new byte[16];
/* 1107 */                   System.arraycopy(block, 0, first16, 0, 16);
/* 1108 */                   first16 = Rijndael.decryptBytes(first16, Rijndael.aes_256, false);
/* 1109 */                   System.arraycopy(first16, 4, tmp4, 0, 4);
/* 1110 */                   flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1111 */                   lPad = (flen + 12) % 16;
/* 1112 */                   if (lPad > 0) {
/* 1113 */                     lPad = 16 - lPad;
/*      */                   }
/* 1115 */                   System.arraycopy(first16, 8, tmp4, 0, 4);
/* 1116 */                   recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1117 */                   fileContent = new byte[flen + lPad + 12];
/* 1118 */                   fileContentIndex = 0;
/*      */                 } 
/* 1120 */                 System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1121 */                 fileContentIndex += blockLength;
/* 1122 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1123 */                 retry = 0;
/* 1124 */                 if (fileContentIndex >= flen + lPad + 12 && flen > 0) {
/*      */                   break;
/*      */                 }
/* 1127 */                 expBlockIndex++;
/*      */               } else {
/* 1129 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               } 
/*      */             } else {
/*      */               
/* 1133 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */             } 
/* 1135 */           } catch (SocketException ex) {
/* 1136 */             ex.printStackTrace();
/*      */           } 
/*      */ 
/*      */           
/* 1140 */           if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1141 */             MercuriusAVLHandlerHelper.updateLastCommunicationModuleData(this.sn, this.lastCommIface, this.currentSIM);
/* 1142 */             nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/*      */         
/* 1146 */         if (fileContentIndex >= flen + lPad + 12 && flen > 0) {
/* 1147 */           byte[] decData = new byte[flen + lPad + 12];
/*      */           
/* 1149 */           byte[] encBlock = new byte[16];
/* 1150 */           if (fileContent.length >= 16 && fileContent.length % 16 == 0) {
/* 1151 */             for (int i = 0; i < fileContent.length; ) {
/* 1152 */               System.arraycopy(fileContent, i, encBlock, 0, 16);
/* 1153 */               byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1154 */               System.arraycopy(arrayOfByte, 0, decData, i, 16);
/* 1155 */               i += 16;
/*      */             } 
/*      */           }
/* 1158 */           byte[] decBlock = new byte[flen];
/* 1159 */           System.arraycopy(decData, MercuriusAVLHandlerHelper.AVL_FILE_HEADER_SIZE, decBlock, 0, flen);
/* 1160 */           int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/* 1161 */           if (recvCfgCRC32 == calcCfgCrc32) {
/* 1162 */             uploadedJSFileList = ConfigFileParser.getAudioScriptLookupData(decBlock);
/*      */           }
/*      */         }
/*      */       
/*      */       } 
/* 1167 */     } catch (IOException|InterruptedException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|SQLException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 1168 */       ex.printStackTrace();
/*      */     } 
/*      */     
/* 1171 */     return uploadedJSFileList;
/*      */   }
/*      */   
/*      */   private void receiveFile4MModule(SP_024DataHolder sp24DH) throws SQLException, InterruptedException {
/* 1175 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1176 */     short retry = 0;
/*      */     
/* 1178 */     byte[] fid = new byte[2];
/* 1179 */     byte[] tmp2 = new byte[2];
/* 1180 */     byte[] tmp4 = new byte[4];
/* 1181 */     byte[] first16 = new byte[16];
/*      */ 
/*      */     
/* 1184 */     byte[] fileContent = null;
/* 1185 */     int fileContentIndex = 0;
/*      */ 
/*      */     
/* 1188 */     int expBlockIndex = 0;
/* 1189 */     int flen = 0;
/* 1190 */     int recvCfgCRC32 = 0;
/* 1191 */     int lPad = 0;
/* 1192 */     File file = null;
/*      */     
/*      */     try {
/* 1195 */       while (isSocketConnected()) {
/*      */         
/* 1197 */         for (retry = (short)(retry + 1); retry < 3; ) {
/*      */           try {
/* 1199 */             byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1200 */             int blockLength = tmp[2] & 0xFF;
/* 1201 */             tmp2[0] = tmp[1];
/* 1202 */             tmp2[1] = tmp[0];
/* 1203 */             int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1204 */             if (expBlockIndex == rcvdBlockIndex) {
/* 1205 */               byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1206 */               byte[] packet = new byte[blockLength + 3];
/* 1207 */               System.arraycopy(tmp, 0, packet, 0, 3);
/* 1208 */               System.arraycopy(block, 0, packet, 3, blockLength);
/* 1209 */               int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1210 */               System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1211 */               int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1212 */               if (crcCalc == crcRecv) {
/* 1213 */                 if (expBlockIndex == 0) {
/* 1214 */                   fid[0] = block[0];
/* 1215 */                   fid[1] = block[1];
/* 1216 */                   System.arraycopy(block, 0, first16, 0, 16);
/* 1217 */                   first16 = Rijndael.decryptBytes(first16, Rijndael.aes_256, false);
/* 1218 */                   System.arraycopy(first16, 4, tmp4, 0, 4);
/* 1219 */                   flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1220 */                   lPad = (flen + 12) % 16;
/* 1221 */                   if (lPad > 0) {
/* 1222 */                     lPad = 16 - lPad;
/*      */                   }
/* 1224 */                   System.arraycopy(first16, 8, tmp4, 0, 4);
/* 1225 */                   recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1226 */                   fileContent = new byte[flen + lPad + 12];
/* 1227 */                   fileContentIndex = 0;
/*      */                 } 
/* 1229 */                 System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1230 */                 fileContentIndex += blockLength;
/* 1231 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1232 */                 retry = 0;
/* 1233 */                 if (fileContentIndex >= flen + lPad + 12 && flen > 0) {
/*      */                   break;
/*      */                 }
/* 1236 */                 expBlockIndex++;
/*      */               } else {
/* 1238 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               } 
/*      */             } else {
/*      */               
/* 1242 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */             } 
/* 1244 */           } catch (SocketException ex) {
/* 1245 */             if (sp24DH != null) {
/* 1246 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sp24DH.getCommandData(), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             }
/*      */           } 
/*      */ 
/*      */           
/* 1251 */           if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1252 */             MercuriusAVLHandlerHelper.updateLastCommunicationModuleData(this.sn, this.lastCommIface, this.currentSIM);
/* 1253 */             nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/*      */         } 
/* 1256 */         if (fileContentIndex >= flen + lPad + 12 && flen > 0) {
/* 1257 */           byte[] decData = new byte[flen + lPad + 12];
/*      */           
/* 1259 */           byte[] encBlock = new byte[16];
/* 1260 */           if (fileContent.length >= 16 && fileContent.length % 16 == 0) {
/* 1261 */             for (int i = 0; i < fileContent.length; ) {
/* 1262 */               System.arraycopy(fileContent, i, encBlock, 0, 16);
/* 1263 */               byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1264 */               System.arraycopy(arrayOfByte, 0, decData, i, 16);
/* 1265 */               i += 16;
/*      */             } 
/*      */           }
/* 1268 */           byte[] decBlock = new byte[flen];
/* 1269 */           System.arraycopy(decData, 12, decBlock, 0, flen);
/* 1270 */           int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/* 1271 */           if (recvCfgCRC32 == calcCfgCrc32) {
/* 1272 */             SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1273 */             if (sp24DH.getCommandData().charAt(0) == '2') {
/* 1274 */               List<AudioNJSFileInfo> reqiredJSFileList = ConfigFileParser.getAudioScriptLookupDataFromCFG(decData, flen, null);
/* 1275 */               if (reqiredJSFileList != null && reqiredJSFileList.size() > 0) {
/* 1276 */                 file = Functions.writeByteArrayToFile(this.sn + "_READ_FULL_CFG" + sp24DH.getCommandData(), fileContent);
/* 1277 */                 List<AudioNJSFileInfo> requriedVMList = MercuriusDBManager.getMissingVoiceMessagesInfo(reqiredJSFileList);
/* 1278 */                 List<AudioNJSFileInfo> repoFilesList = new ArrayList<>();
/* 1279 */                 if (requriedVMList == null) {
/* 1280 */                   repoFilesList.addAll(reqiredJSFileList);
/*      */                 } else {
/* 1282 */                   for (AudioNJSFileInfo ajsF : reqiredJSFileList) {
/* 1283 */                     if (!requriedVMList.contains(ajsF)) {
/* 1284 */                       repoFilesList.add(ajsF);
/*      */                     }
/*      */                   } 
/*      */                 } 
/* 1288 */                 long cfgLen = file.length();
/* 1289 */                 int audioCounter = 100;
/* 1290 */                 int jsCounter = 300;
/*      */                 
/* 1292 */                 RandomAccessFile rac = new RandomAccessFile(file, "rw");
/*      */                 try {
/* 1294 */                   for (AudioNJSFileInfo ajsF : repoFilesList) {
/* 1295 */                     byte[] ajsContent = MercuriusDBManager.getVoiceMessagesByName(ajsF.getLength(), ajsF.getName(), ajsF.getCrc32(), ajsF.getDir());
/* 1296 */                     if (ajsContent != null) {
/* 1297 */                       byte[] ajsheader = new byte[22];
/* 1298 */                       tmp2 = Functions.get2ByteArrayFromInt((ajsF.getDir() == 1) ? audioCounter++ : jsCounter++);
/* 1299 */                       ajsheader[0] = tmp2[1];
/* 1300 */                       ajsheader[1] = tmp2[0];
/* 1301 */                       System.arraycopy(ajsF.getName().getBytes("ISO-8859-1"), 0, ajsheader, 2, ajsF.getName().length());
/* 1302 */                       tmp4 = Functions.get4ByteArrayFromInt(ajsF.getLength());
/* 1303 */                       ajsheader[14] = tmp4[3];
/* 1304 */                       ajsheader[15] = tmp4[2];
/* 1305 */                       ajsheader[16] = tmp4[1];
/* 1306 */                       ajsheader[17] = tmp4[0];
/* 1307 */                       tmp4 = Functions.get4ByteArrayFromInt(ajsF.getCrc32());
/* 1308 */                       ajsheader[18] = tmp4[3];
/* 1309 */                       ajsheader[19] = tmp4[2];
/* 1310 */                       ajsheader[20] = tmp4[1];
/* 1311 */                       ajsheader[21] = tmp4[0];
/* 1312 */                       rac.seek(cfgLen);
/* 1313 */                       rac.write(ajsheader);
/* 1314 */                       cfgLen += ajsheader.length;
/* 1315 */                       rac.seek(cfgLen);
/* 1316 */                       rac.write(ajsContent);
/* 1317 */                       cfgLen += ajsContent.length;
/*      */                     } 
/*      */                   } 
/*      */                 } finally {
/* 1321 */                   rac.close();
/*      */                 } 
/*      */                 
/* 1324 */                 if (requriedVMList != null && !requriedVMList.isEmpty()) {
/* 1325 */                   readAudioandJSFiles(requriedVMList, file, audioCounter, jsCounter);
/*      */                 }
/* 1327 */                 MercuriusDBManager.updateCommandData(sp24DH.getId_Command(), new FileInputStream(file));
/*      */               } 
/*      */             } else {
/* 1330 */               MercuriusDBManager.updateCommandData(sp24DH.getId_Command(), new ByteArrayInputStream(fileContent));
/*      */             } 
/* 1332 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("The_file_[") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1333 */             MercuriusAVLHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); break;
/*      */           } 
/* 1335 */           MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("invalid"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           
/*      */           break;
/*      */         } 
/*      */       } 
/* 1340 */     } catch (Exception ex) {
/* 1341 */       ex.printStackTrace();
/* 1342 */       if (sp24DH != null) {
/* 1343 */         MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file") + sp24DH.getCommandData() + ex.getMessage(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */       } else {
/* 1345 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */       } 
/*      */     } finally {
/* 1348 */       if (file != null && file.exists()) {
/* 1349 */         file.delete();
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private void readAudioandJSFiles(List<AudioNJSFileInfo> reqiredJSFileList, File file, int audioCounter, int jsCounter) throws SQLException, InterruptedException, IOException, Exception {
/* 1356 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1357 */     RandomAccessFile rac = new RandomAccessFile(file, "rw");
/*      */     
/* 1359 */     long cfgLen = file.length();
/*      */     try {
/* 1361 */       for (AudioNJSFileInfo ajs : reqiredJSFileList) {
/* 1362 */         if (requestAudioJSInfo((ajs.getDir() == 1) ? 32794 : 32795)) {
/* 1363 */           monitorThread();
/* 1364 */           byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1365 */           if (resp != null && resp[0] == 6) {
/* 1366 */             byte[] vmcmd = new byte[15];
/* 1367 */             vmcmd[0] = (byte)ajs.getDir();
/* 1368 */             System.arraycopy(ajs.getName().getBytes("ISO-8859-1"), 0, vmcmd, 1, ajs.getName().length());
/* 1369 */             int crcCalc = CRC16.calculate(vmcmd, 0, 13, 65535);
/* 1370 */             byte[] tmp2 = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1371 */             System.arraycopy(tmp2, 0, vmcmd, 13, 2);
/* 1372 */             SocketFunctions.send(this.clientSocket, vmcmd);
/* 1373 */             monitorThread();
/* 1374 */             resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1375 */             if (resp != null && resp[0] == 6) {
/* 1376 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Reading_audio_file_name_[") + ajs.getName() + " ]", Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1377 */               short retry = 0;
/*      */ 
/*      */ 
/*      */               
/* 1381 */               byte[] fileContent = null;
/* 1382 */               int fileContentIndex = 0;
/*      */ 
/*      */               
/* 1385 */               int expBlockIndex = 0;
/* 1386 */               if (isSocketConnected()) {
/*      */                 
/* 1388 */                 for (retry = (short)(retry + 1); retry < 3; ) {
/*      */                   try {
/* 1390 */                     byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1391 */                     int blockLength = tmp[2] & 0xFF;
/* 1392 */                     tmp2[0] = tmp[1];
/* 1393 */                     tmp2[1] = tmp[0];
/* 1394 */                     int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1395 */                     if (expBlockIndex == rcvdBlockIndex) {
/* 1396 */                       byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1397 */                       byte[] packet = new byte[blockLength + 3];
/* 1398 */                       System.arraycopy(tmp, 0, packet, 0, 3);
/* 1399 */                       System.arraycopy(block, 0, packet, 3, blockLength);
/* 1400 */                       crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1401 */                       System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1402 */                       int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1403 */                       if (crcCalc == crcRecv) {
/* 1404 */                         if (expBlockIndex == 0) {
/* 1405 */                           fileContent = new byte[ajs.getLength()];
/* 1406 */                           System.arraycopy(block, 4, fileContent, fileContentIndex, blockLength - 4);
/* 1407 */                           fileContentIndex += blockLength - 4;
/*      */                         } else {
/* 1409 */                           System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1410 */                           fileContentIndex += blockLength;
/*      */                         } 
/* 1412 */                         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1413 */                         retry = 0;
/* 1414 */                         if (fileContentIndex >= ajs.getLength() && ajs.getLength() > 0) {
/*      */                           break;
/*      */                         }
/* 1417 */                         expBlockIndex++;
/*      */                       } else {
/* 1419 */                         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                       } 
/*      */                     } else {
/*      */                       
/* 1423 */                       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                     } 
/* 1425 */                   } catch (IOException|InterruptedException ex) {
/* 1426 */                     ex.printStackTrace();
/*      */                   } 
/*      */ 
/*      */ 
/*      */                   
/* 1431 */                   if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1432 */                     MercuriusAVLHandlerHelper.updateLastCommunicationModuleData(this.sn, this.lastCommIface, this.currentSIM);
/* 1433 */                     nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */                   } 
/*      */                 } 
/* 1436 */                 if (fileContentIndex >= ajs.getLength() && ajs.getLength() > 0) {
/* 1437 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*      */                   
/* 1439 */                   byte[] ajsheader = new byte[22];
/* 1440 */                   tmp2 = Functions.get2ByteArrayFromInt((ajs.getDir() == 1) ? audioCounter++ : jsCounter++);
/* 1441 */                   ajsheader[0] = tmp2[1];
/* 1442 */                   ajsheader[1] = tmp2[0];
/* 1443 */                   byte[] tmp4 = Functions.get4ByteArrayFromInt(ajs.getCrc32());
/* 1444 */                   ajsheader[2] = tmp4[3];
/* 1445 */                   ajsheader[3] = tmp4[2];
/* 1446 */                   ajsheader[4] = tmp4[1];
/* 1447 */                   ajsheader[5] = tmp4[0];
/*      */                   
/* 1449 */                   tmp4 = Functions.get4ByteArrayFromInt(ajs.getLength());
/* 1450 */                   ajsheader[6] = tmp4[3];
/* 1451 */                   ajsheader[7] = tmp4[2];
/* 1452 */                   ajsheader[8] = tmp4[1];
/* 1453 */                   ajsheader[9] = tmp4[0];
/*      */                   
/* 1455 */                   System.arraycopy(ajs.getName().getBytes("ISO-8859-1"), 0, ajsheader, 10, ajs.getName().length());
/*      */                   
/* 1457 */                   rac.seek(cfgLen);
/* 1458 */                   rac.write(ajsheader);
/* 1459 */                   cfgLen += ajsheader.length;
/* 1460 */                   rac.seek(cfgLen);
/* 1461 */                   rac.write(fileContent);
/* 1462 */                   cfgLen += fileContent.length;
/* 1463 */                   MercuriusDBManager.saveVoiceMessage(fileContent, ajs.getLength(), ajs.getName(), ajs.getCrc32(), ajs.getDir());
/*      */                 } 
/*      */               } 
/*      */             } 
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     } finally {
/*      */ 
/*      */       
/*      */       try {
/*      */         
/* 1475 */         rac.close();
/* 1476 */       } catch (IOException iOException) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendFile2Module(SP_024DataHolder sp24DH) throws IOException, InterruptedException, SQLException {
/* 1484 */     int blockIndex = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1489 */     short maxRetries = 3;
/* 1490 */     short retry = 0;
/*      */ 
/*      */ 
/*      */     
/* 1494 */     String fileName = MercuriusAVLHandlerHelper.getFileNameByCommandData(sp24DH.getCommandData());
/* 1495 */     File file = Functions.writeByteArrayToFile(this.sn + "_" + sp24DH.getCommandData(), sp24DH.getCommandFileData());
/* 1496 */     FileChannel fc = (new RandomAccessFile(file, "r")).getChannel();
/* 1497 */     fc.position(0L);
/* 1498 */     long flen = fc.size();
/* 1499 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */ 
/*      */     
/* 1502 */     label92: while (isSocketConnected() && fc.position() < flen) {
/*      */       
/* 1504 */       for (retry = (short)(retry + 1); retry < maxRetries; ) {
/* 1505 */         int blockLength = (int)((flen - fc.position() > 240L) ? 240L : (flen - fc.position()));
/* 1506 */         ByteBuffer blockBuf = ByteBuffer.allocate(blockLength);
/* 1507 */         if (fc.read(blockBuf) == blockLength) {
/* 1508 */           byte[] block = blockBuf.array();
/* 1509 */           byte[] packet = new byte[blockLength + 5];
/* 1510 */           byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(blockIndex));
/* 1511 */           System.arraycopy(tmp, 0, packet, 0, 2);
/* 1512 */           packet[2] = (byte)Integer.parseInt(Integer.toHexString(blockLength), 16);
/* 1513 */           System.arraycopy(block, 0, packet, 3, blockLength);
/* 1514 */           int crcCalc = CRC16.calculate(packet, 0, blockLength + 3, 65535);
/* 1515 */           tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1516 */           System.arraycopy(tmp, 0, packet, blockLength + 3, 2);
/*      */           try {
/* 1518 */             SocketFunctions.send(this.clientSocket, packet);
/* 1519 */             Thread.sleep(50L);
/*      */             try {
/* 1521 */               if (retry - 1 == 0) {
/* 1522 */                 this.clientSocket.setSoTimeout(120000);
/* 1523 */               } else if (retry - 1 == 1) {
/* 1524 */                 this.clientSocket.setSoTimeout(210000);
/* 1525 */               } else if (retry - 1 == 2) {
/* 1526 */                 this.clientSocket.setSoTimeout(300000);
/*      */               } 
/*      */               
/* 1529 */               if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1530 */                 MercuriusAVLHandlerHelper.updateLastCommunicationModuleData(this.sn, this.lastCommIface, this.currentSIM);
/* 1531 */                 nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 1533 */               tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1534 */               if (tmp[0] == 6) {
/* 1535 */                 retry = 0;
/* 1536 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + fileName + " (" + fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1537 */                 blockIndex++; break;
/*      */               } 
/* 1539 */               if (tmp[0] == 21) {
/* 1540 */                 if (blockIndex > 0) {
/* 1541 */                   blockIndex--;
/*      */                 }
/*      */                 
/* 1544 */                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Invalid_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + fileName + " :" + tmp[0], Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1545 */                 fc.position(fc.position() - blockLength); break;
/*      */               } 
/* 1547 */               if ((tmp[0] & 0xFF) == 4) {
/* 1548 */                 MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, "COMMAND Request Received in between file processing " + fileName, sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */                 break label92;
/*      */               } 
/* 1551 */             } catch (SocketException ex) {
/*      */ 
/*      */               
/* 1554 */               if (blockIndex > 0) {
/* 1555 */                 blockIndex--;
/*      */               }
/* 1557 */               fc.position(fc.position() - blockLength);
/* 1558 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + fileName, Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/* 1560 */           } catch (IOException|InterruptedException|SQLException ex) {
/*      */             
/* 1562 */             if (blockIndex > 0) {
/* 1563 */               blockIndex--;
/*      */             }
/* 1565 */             fc.position(fc.position() - blockLength);
/* 1566 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + fileName, Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */           } 
/* 1568 */           Thread.sleep(10L); continue;
/*      */         } 
/* 1570 */         MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_reading_data_of_the_file") + fileName, sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         
/*      */         break label92;
/*      */       } 
/* 1574 */       if (retry >= maxRetries) {
/* 1575 */         MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_a_data_block_of_the_file") + fileName + LocaleMessage.getLocaleMessage("to_the_module"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         
/*      */         break;
/*      */       } 
/*      */     } 
/* 1580 */     boolean commandProcessed = (fc.position() >= flen);
/* 1581 */     fc.close();
/*      */     
/*      */     try {
/* 1584 */       if (commandProcessed) {
/* 1585 */         Thread.sleep(100L);
/* 1586 */         if (sp24DH.getCommandData().charAt(0) == '1') {
/* 1587 */           byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1588 */           if (tmp[0] == 6) {
/* 1589 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("The_file_[") + fileName + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1590 */             MercuriusAVLHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/* 1591 */           } else if (tmp[0] == 22) {
/* 1592 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("The_module_informed_that_CRC-32_is_not_matching_for_the_file_[") + fileName + "]", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           } else {
/* 1594 */             MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Failure_while_sending_the_file") + fileName + LocaleMessage.getLocaleMessage("(response") + tmp[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           } 
/*      */         } else {
/* 1597 */           MercuriusAVLHandlerHelper.endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */         } 
/*      */       } else {
/* 1600 */         MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, "", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */       } 
/* 1602 */     } catch (Exception ex) {
/* 1603 */       MercuriusAVLHandlerHelper.registerFailureSendCommand(this.sn, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + fileName + LocaleMessage.getLocaleMessage("was_sent_successfully"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */     } finally {
/* 1605 */       if (file != null && file.exists())
/* 1606 */         file.delete(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean sendRemoteCommand(SP_024DataHolder sp24DH) {
/*      */     byte[] tmp, ascii;
/*      */     String[] cData, date, dData, hData;
/* 1613 */     byte[] data = null;
/*      */ 
/*      */ 
/*      */     
/* 1617 */     switch (sp24DH.getCommand_Type()) {
/*      */       case 32774:
/* 1619 */         data = new byte[3];
/* 1620 */         tmp = Functions.get2ByteArrayFromInt(32774);
/* 1621 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1622 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1623 */         data[2] = 0;
/*      */         break;
/*      */       
/*      */       case 32773:
/* 1627 */         data = new byte[4];
/* 1628 */         tmp = Functions.get2ByteArrayFromInt(32773);
/* 1629 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1630 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1631 */         data[2] = 1;
/* 1632 */         data[3] = (byte)Character.digit(sp24DH.getCommandData().charAt(0), 10);
/*      */         break;
/*      */       
/*      */       case 32769:
/* 1636 */         data = new byte[4];
/* 1637 */         tmp = Functions.get2ByteArrayFromInt(32769);
/* 1638 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1639 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1640 */         data[2] = 1;
/* 1641 */         data[3] = (byte)Character.digit(sp24DH.getCommandData().charAt(0), 10);
/*      */         break;
/*      */       
/*      */       case 32770:
/* 1645 */         data = new byte[4];
/* 1646 */         tmp = Functions.get2ByteArrayFromInt(32770);
/* 1647 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1648 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1649 */         data[2] = 1;
/* 1650 */         data[3] = (byte)Character.digit(sp24DH.getCommandData().charAt(0), 10);
/*      */         break;
/*      */       
/*      */       case 32775:
/* 1654 */         tmp = Functions.get2ByteArrayFromInt(32775);
/* 1655 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1656 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/* 1657 */         data = new byte[ascii.length + 3];
/* 1658 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1659 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/* 1660 */         System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */         break;
/*      */       
/*      */       case 32776:
/* 1664 */         cData = sp24DH.getCommandData().split(";");
/* 1665 */         data = new byte[5];
/* 1666 */         data[2] = 2;
/* 1667 */         data[3] = (byte)Integer.parseInt(cData[1]);
/* 1668 */         data[4] = (byte)Integer.parseInt(cData[2]);
/* 1669 */         tmp = Functions.get2ByteArrayFromInt(32776);
/* 1670 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1671 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       
/*      */       case 32777:
/* 1675 */         tmp = Functions.get2ByteArrayFromInt(32777);
/* 1676 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1677 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/* 1678 */         data = new byte[ascii.length + 4];
/* 1679 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1680 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length + 2), 16).byteValue();
/* 1681 */         data[3] = (byte)((sp24DH.getCommandData().charAt(0) == '1') ? 1 : 2);
/* 1682 */         System.arraycopy(ascii, 0, data, 4, ascii.length);
/*      */         break;
/*      */       
/*      */       case 32778:
/* 1686 */         data = new byte[3];
/* 1687 */         tmp = Functions.get2ByteArrayFromInt(32778);
/* 1688 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1689 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1690 */         data[2] = 0;
/*      */         break;
/*      */       
/*      */       case 32780:
/* 1694 */         tmp = Functions.get2ByteArrayFromInt(32780);
/* 1695 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1696 */         cData = sp24DH.getCommandData().split(";");
/* 1697 */         if (cData.length == 1) {
/* 1698 */           if (cData[0].equals("1")) {
/* 1699 */             data = new byte[4];
/* 1700 */             System.arraycopy(tmp, 0, data, 0, 2);
/* 1701 */             data[2] = 1;
/* 1702 */             data[3] = 1; break;
/* 1703 */           }  if (cData[0].equals("2")) {
/* 1704 */             data = new byte[11];
/* 1705 */             System.arraycopy(tmp, 0, data, 0, 2);
/* 1706 */             String[] zones = TimeZone.getAvailableIDs(this.timezone * 60 * 1000);
/* 1707 */             this.df.setTimeZone(TimeZone.getTimeZone((String)Defines.TIMEZONE_NAMES.get(Integer.valueOf(this.timezone))));
/* 1708 */             String ddd = this.df.format(new Date());
/* 1709 */             data[2] = 8;
/* 1710 */             data[3] = 2;
/* 1711 */             data[4] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(0, 2))), 16).byteValue();
/* 1712 */             data[5] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(3, 5))), 16).byteValue();
/* 1713 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(ddd.substring(6, 10)));
/* 1714 */             data[6] = tmp[1];
/* 1715 */             data[7] = tmp[0];
/* 1716 */             data[8] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(11, 13))), 16).byteValue();
/* 1717 */             data[9] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(14, 16))), 16).byteValue();
/* 1718 */             data[10] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(17))), 16).byteValue();
/*      */           }  break;
/*      */         } 
/* 1721 */         data = new byte[11];
/* 1722 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         
/* 1724 */         date = cData[1].split(" ");
/* 1725 */         dData = date[0].split("-");
/* 1726 */         hData = date[1].split(":");
/* 1727 */         data[2] = 8;
/* 1728 */         data[3] = 3;
/* 1729 */         data[4] = Byte.valueOf(dData[2]).byteValue();
/* 1730 */         data[5] = Byte.valueOf(dData[1]).byteValue();
/* 1731 */         tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(dData[0]));
/* 1732 */         data[6] = tmp[1];
/* 1733 */         data[7] = tmp[0];
/* 1734 */         data[8] = Byte.valueOf(hData[0]).byteValue();
/* 1735 */         data[9] = Byte.valueOf(hData[1]).byteValue();
/* 1736 */         data[10] = Byte.valueOf(hData[2]).byteValue();
/*      */         break;
/*      */ 
/*      */       
/*      */       case 32781:
/* 1741 */         data = new byte[4];
/* 1742 */         tmp = Functions.get2ByteArrayFromInt(32781);
/* 1743 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1744 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1745 */         data[2] = 0;
/* 1746 */         data[3] = (byte)Integer.parseInt(sp24DH.getCommandData());
/*      */         break;
/*      */       
/*      */       case 32782:
/* 1750 */         data = new byte[6];
/* 1751 */         tmp = Functions.get2ByteArrayFromInt(32782);
/* 1752 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1753 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1754 */         cData = sp24DH.getCommandData().split(";");
/* 1755 */         data[2] = 3;
/* 1756 */         data[3] = (byte)Integer.parseInt(cData[0]);
/* 1757 */         data[4] = (byte)Integer.parseInt(cData[1]);
/* 1758 */         data[5] = (byte)Integer.parseInt(cData[2]);
/*      */         break;
/*      */       
/*      */       case 32783:
/* 1762 */         data = new byte[3];
/* 1763 */         tmp = Functions.get2ByteArrayFromInt(32783);
/* 1764 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1765 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1766 */         data[2] = 0;
/*      */         break;
/*      */       
/*      */       case 32784:
/* 1770 */         data = new byte[4];
/* 1771 */         tmp = Functions.get2ByteArrayFromInt(32784);
/* 1772 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1773 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1774 */         data[2] = 1;
/* 1775 */         data[3] = (byte)Integer.parseInt(sp24DH.getCommandData());
/*      */         break;
/*      */       
/*      */       case 32785:
/* 1779 */         data = new byte[4];
/* 1780 */         tmp = Functions.get2ByteArrayFromInt(32785);
/* 1781 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1782 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1783 */         data[2] = 1;
/* 1784 */         data[3] = (byte)Integer.parseInt(sp24DH.getCommandData());
/*      */         break;
/*      */       
/*      */       case 32786:
/* 1788 */         data = new byte[4];
/* 1789 */         tmp = Functions.get2ByteArrayFromInt(32786);
/* 1790 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1791 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1792 */         data[2] = 1;
/* 1793 */         data[3] = (byte)Integer.parseInt(sp24DH.getCommandData());
/*      */         break;
/*      */       
/*      */       case 32787:
/* 1797 */         data = new byte[3];
/* 1798 */         tmp = Functions.get2ByteArrayFromInt(32787);
/* 1799 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1800 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1801 */         data[2] = 0;
/*      */         break;
/*      */     } 
/*      */     
/*      */     try {
/* 1806 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 1807 */       SocketFunctions.send(this.clientSocket, tmp);
/* 1808 */       return true;
/* 1809 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 1810 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1811 */       ex.printStackTrace();
/* 1812 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean initiateGeofenceDataRequest() throws InterruptedException, IOException {
/* 1817 */     byte[] data = new byte[3];
/* 1818 */     byte[] tmp = Functions.get2ByteArrayFromInt(32773);
/* 1819 */     tmp = Functions.swapLSB2MSB(tmp);
/* 1820 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 1821 */     data[2] = 0;
/*      */     
/*      */     try {
/* 1824 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 1825 */       SocketFunctions.send(this.clientSocket, tmp);
/* 1826 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 1827 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1828 */       ex.printStackTrace();
/* 1829 */       return false;
/*      */     } 
/*      */     
/* 1832 */     monitorThread();
/* 1833 */     byte[] resp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1834 */     this.initiatedGeofenceRequest = false;
/*      */     
/* 1836 */     if (resp != null && resp[0] == 6) {
/*      */       
/* 1838 */       byte[] fileIDData = new byte[2];
/* 1839 */       fileIDData[0] = 1;
/* 1840 */       fileIDData[1] = 12;
/* 1841 */       SocketFunctions.send(this.clientSocket, fileIDData);
/* 1842 */       monitorThread();
/* 1843 */       long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/* 1844 */       short retry = 0;
/* 1845 */       byte[] tmp2 = new byte[2];
/* 1846 */       byte[] tmp4 = new byte[4];
/* 1847 */       byte[] first16 = new byte[16];
/*      */ 
/*      */       
/* 1850 */       byte[] fileContent = null;
/* 1851 */       int fileContentIndex = 0;
/*      */ 
/*      */       
/* 1854 */       int expBlockIndex = 0;
/* 1855 */       int flen = 0;
/* 1856 */       int recvCfgCRC32 = 0;
/* 1857 */       int lPad = 0;
/*      */       
/*      */       try {
/* 1860 */         while (isSocketConnected()) {
/*      */           
/* 1862 */           for (retry = (short)(retry + 1); retry < 3; ) {
/*      */             try {
/* 1864 */               tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1865 */               int blockLength = tmp[2] & 0xFF;
/* 1866 */               tmp2[0] = tmp[1];
/* 1867 */               tmp2[1] = tmp[0];
/* 1868 */               int rcvdBlockIndex = Functions.getIntFrom2ByteArray(tmp2);
/* 1869 */               if (expBlockIndex == rcvdBlockIndex) {
/* 1870 */                 byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1871 */                 byte[] packet = new byte[blockLength + 3];
/* 1872 */                 System.arraycopy(tmp, 0, packet, 0, 3);
/* 1873 */                 System.arraycopy(block, 0, packet, 3, blockLength);
/* 1874 */                 int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1875 */                 System.arraycopy(block, blockLength, tmp2, 0, 2);
/* 1876 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp2));
/* 1877 */                 if (crcCalc == crcRecv) {
/* 1878 */                   if (expBlockIndex == 0) {
/* 1879 */                     System.arraycopy(block, 0, first16, 0, 16);
/* 1880 */                     first16 = Rijndael.decryptBytes(first16, Rijndael.aes_256, false);
/* 1881 */                     System.arraycopy(first16, 4, tmp4, 0, 4);
/* 1882 */                     flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1883 */                     lPad = (flen + 12) % 16;
/* 1884 */                     if (lPad > 0) {
/* 1885 */                       lPad = 16 - lPad;
/*      */                     }
/* 1887 */                     System.arraycopy(first16, 8, tmp4, 0, 4);
/* 1888 */                     recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp4));
/* 1889 */                     fileContent = new byte[flen + lPad + 12];
/* 1890 */                     fileContentIndex = 0;
/*      */                   } 
/* 1892 */                   System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1893 */                   fileContentIndex += blockLength;
/* 1894 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1895 */                   retry = 0;
/* 1896 */                   if (fileContentIndex >= flen + lPad + 12 && flen > 0) {
/*      */                     break;
/*      */                   }
/* 1899 */                   expBlockIndex++;
/*      */                 } else {
/* 1901 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                 } 
/*      */               } else {
/*      */                 
/* 1905 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               } 
/* 1907 */             } catch (SocketException ex) {
/* 1908 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/*      */             
/* 1911 */             if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1912 */               MercuriusAVLHandlerHelper.updateLastCommunicationModuleData(this.sn, this.lastCommIface, this.currentSIM);
/* 1913 */               nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */             } 
/*      */           } 
/*      */           
/* 1917 */           if (fileContentIndex >= flen + lPad + 12 && flen > 0) {
/* 1918 */             byte[] decData = new byte[flen + lPad + 12];
/*      */             
/* 1920 */             byte[] encBlock = new byte[16];
/* 1921 */             if (fileContent.length >= 16 && fileContent.length % 16 == 0) {
/* 1922 */               for (int i = 0; i < fileContent.length; ) {
/* 1923 */                 System.arraycopy(fileContent, i, encBlock, 0, 16);
/* 1924 */                 byte[] arrayOfByte = Rijndael.decryptBytes(encBlock, Rijndael.aes_256, false);
/* 1925 */                 System.arraycopy(arrayOfByte, 0, decData, i, 16);
/* 1926 */                 i += 16;
/*      */               } 
/*      */             }
/* 1929 */             byte[] decBlock = new byte[flen];
/* 1930 */             System.arraycopy(decData, MercuriusAVLHandlerHelper.AVL_FILE_HEADER_SIZE, decBlock, 0, flen);
/* 1931 */             int calcCfgCrc32 = CRC32.getCRC32(decBlock);
/* 1932 */             if (recvCfgCRC32 == calcCfgCrc32) {
/* 1933 */               MercuriusAVLHandlerHelper.parseGeofenceData(decBlock, flen, ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idModule, ((InfoModule)TblMercuriusActiveConnections.getInstance().get(this.sn)).idClient, this.geofenceCRC32);
/* 1934 */               SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1935 */               Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Geofence_data_sync_done_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1936 */               return true;
/*      */             } 
/* 1938 */             SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 1939 */             Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("CRC_32_of_the_received_configuration_file_is_invalid"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             
/*      */             break;
/*      */           } 
/*      */         } 
/* 1944 */       } catch (Exception ex) {
/* 1945 */         ex.printStackTrace();
/* 1946 */         Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */       } 
/*      */     } 
/* 1949 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean requestAudioJSInfo(int cmd) {
/* 1954 */     byte[] data = new byte[3];
/* 1955 */     byte[] tmp = Functions.get2ByteArrayFromInt(cmd);
/* 1956 */     tmp = Functions.swapLSB2MSB(tmp);
/* 1957 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 1958 */     data[2] = 0;
/*      */     
/*      */     try {
/* 1961 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 1962 */       SocketFunctions.send(this.clientSocket, tmp);
/* 1963 */       return true;
/* 1964 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 1965 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1966 */       ex.printStackTrace();
/* 1967 */       return false;
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean sendAudioJSCMD(int cmd) {
/* 1973 */     byte[] data = new byte[3];
/* 1974 */     byte[] tmp = Functions.get2ByteArrayFromInt(cmd);
/* 1975 */     tmp = Functions.swapLSB2MSB(tmp);
/* 1976 */     System.arraycopy(tmp, 0, data, 0, 2);
/* 1977 */     data[2] = 0;
/*      */     
/*      */     try {
/* 1980 */       tmp = GriffonHandlerHelper.prepareCommandPacket(data);
/* 1981 */       SocketFunctions.send(this.clientSocket, tmp);
/* 1982 */       return true;
/* 1983 */     } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 1984 */       Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1985 */       ex.printStackTrace();
/* 1986 */       return false;
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\MercuriusHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */