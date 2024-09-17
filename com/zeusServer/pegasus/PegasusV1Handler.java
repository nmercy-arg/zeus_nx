/*     */ package com.zeusServer.pegasus;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.serialPort.communication.EmulateReceiver;
/*     */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblThreadsEmulaReceivers;
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.InfoModule;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.Rijndael;
/*     */ import com.zeusServer.util.SocketFunctions;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.net.InetAddress;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.sql.SQLException;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.UUID;
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
/*     */ 
/*     */ public class PegasusV1Handler
/*     */   extends PegasusRoutines
/*     */   implements Runnable
/*     */ {
/*     */   private Socket clientSocket;
/*     */   private File tmpDownloadFile;
/*     */   
/*     */   private enum EnumCommandType
/*     */   {
/*  60 */     SET_CLOCK(5);
/*     */     private int commandType;
/*     */     
/*     */     EnumCommandType(int commandType) {
/*  64 */       this.commandType = commandType;
/*     */     }
/*     */     
/*     */     public int getCommandType() {
/*  68 */       return this.commandType;
/*     */     }
/*     */   }
/*     */   
/*  72 */   private String myThreadGuid = UUID.randomUUID().toString();
/*  73 */   private String iccid = "";
/*     */   private String remoteIP;
/*  75 */   private byte[] bufferRx = new byte[32];
/*     */   private boolean initialMsgFlag;
/*  77 */   private DateFormat df = new SimpleDateFormat("yy/MM/dd,HH:mm:ss");
/*     */   
/*     */   String tName;
/*     */   private byte[] prod;
/*     */   
/*     */   public PegasusV1Handler(Socket sock, byte[] prod) throws SocketException, IOException {
/*  83 */     this.clientSocket = sock;
/*  84 */     this.clientSocket.setSoTimeout(30000);
/*  85 */     this.clientSocket.setTcpNoDelay(false);
/*  86 */     this.initialMsgFlag = true;
/*  87 */     this.prod = prod;
/*  88 */     this.remoteIP = this.clientSocket.getRemoteSocketAddress().toString();
/*  89 */     this.remoteIP = this.remoteIP.substring(1);
/*  90 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  91 */     Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*  97 */     int bytesReceived = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 102 */     try { while (isSocketConnected() && keepThreadRunning() && this.idleTimeout > System.currentTimeMillis()) {
/*     */ 
/*     */ 
/*     */         
/* 106 */         if (this.clientSocket.getInputStream().available() > 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/* 107 */           if (this.clientSocket.getInputStream().available() == 1) {
/* 108 */             byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 109 */             if (buffer.length == 1) {
/* 110 */               if ((buffer[0] & 0x7) == PegasusRoutines.EnumTipoPacote.COMMAND_PACKET.getPacket()) {
/* 111 */                 if (!processCommandPacket()) {
/*     */                   break;
/*     */                 }
/*     */               } else {
/* 115 */                 bytesReceived++;
/*     */               } 
/*     */             }
/* 118 */           } else if (this.clientSocket.getInputStream().available() >= 16 || this.initialMsgFlag) {
/* 119 */             byte[] buffer = new byte[17];
/* 120 */             if (this.initialMsgFlag) {
/* 121 */               this.initialMsgFlag = false;
/* 122 */               buffer[0] = this.prod[0];
/* 123 */               buffer[1] = this.prod[1];
/* 124 */               this.prod = SocketFunctions.receive(this.clientSocket, 0, 15);
/* 125 */               System.arraycopy(this.prod, 0, buffer, 2, 15);
/*     */             } else {
/* 127 */               buffer = SocketFunctions.receive(this.clientSocket, 0, 17);
/*     */             } 
/* 129 */             if (buffer.length != 17) {
/*     */               break;
/*     */             }
/*     */             
/* 133 */             byte[] eee = new byte[16];
/* 134 */             System.arraycopy(buffer, 1, eee, 0, 16);
/* 135 */             byte[] decrytedBuffer = Rijndael.decryptBytes(eee, Rijndael.dataKeyBytes, false);
/* 136 */             if (decrytedBuffer.length >= 16) {
/* 137 */               System.arraycopy(decrytedBuffer, 0, this.bufferRx, 1, 16);
/* 138 */               int crcReceived = this.bufferRx[16] & 0xFF;
/* 139 */               crcReceived = crcReceived * 256 + (this.bufferRx[15] & 0xFF);
/* 140 */               this.bufferRx[0] = buffer[0];
/* 141 */               int crccalc = CRC16.calculate(this.bufferRx, 0, 15, 65535);
/* 142 */               if (crcReceived == crccalc) {
/* 143 */                 if (((this.bufferRx[0] & 0x7) == PegasusRoutines.EnumTipoPacote.IDENTIFICATION_PACKET.getPacket()) ? 
/* 144 */                   !processIdentificationPacket() : (
/*     */ 
/*     */                   
/* 147 */                   ((this.bufferRx[0] & 0x7) == PegasusRoutines.EnumTipoPacote.EXTENDED_ALIVE_PACKET.getPacket()) ? 
/* 148 */                   !processAlivePacket() : ((
/*     */ 
/*     */                   
/* 151 */                   this.bufferRx[0] & 0x7) != PegasusRoutines.EnumTipoPacote.EVENT_PACKET.getPacket() || 
/* 152 */                   !processEventPacket())))
/*     */                 {
/*     */                   break;
/*     */                 }
/*     */               }
/*     */               else {
/*     */                 
/* 159 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/*     */                 try {
/* 161 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 162 */                 } catch (IOException ex) {
/*     */                   break;
/*     */                 } 
/*     */               } 
/*     */             } else {
/* 167 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Error_while_decrypting_the_data_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/*     */               try {
/* 169 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 170 */               } catch (IOException ex) {
/*     */                 break;
/*     */               } 
/*     */             } 
/* 174 */             bytesReceived = 0;
/*     */           } 
/* 176 */         } else if (bytesReceived == 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && 
/* 177 */           TblPegasusActiveConnections.getInstance().containsKey(this.iccid) && 
/* 178 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).newCommand) {
/*     */           
/* 180 */           if (this.idleTimeout - ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).communicationTimeout + 10000L < System.currentTimeMillis() && ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).nextSendingSolicitationReadingCommand < System.currentTimeMillis()) {
/* 181 */             byte[] newCmd = Functions.intToByteArray(128, 1);
/* 182 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, this.iccid, null);
/*     */             try {
/* 184 */               SocketFunctions.send(this.clientSocket, newCmd);
/* 185 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.iccid, null);
/* 186 */               ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/* 187 */             } catch (IOException ex) {
/*     */               break;
/*     */             } 
/*     */           } 
/*     */         } 
/*     */ 
/*     */         
/* 194 */         sleepThread();
/*     */       }  }
/* 196 */     catch (InterruptedException interruptedException)
/*     */     
/*     */     { 
/*     */       try {
/*     */ 
/*     */ 
/*     */         
/* 203 */         this.clientSocket.close();
/* 204 */         this.clientSocket = null;
/* 205 */         dispose();
/* 206 */       } catch (IOException iOException) {} } catch (IOException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) { ex.printStackTrace(); Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_in_the_Data_Server_task"), Enums.EnumMessagePriority.HIGH, this.iccid, ex); } finally { try { this.clientSocket.close(); this.clientSocket = null; dispose(); } catch (IOException iOException) {} }
/*     */   
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean isSocketConnected() {
/* 213 */     if (this.clientSocket.isConnected()) {
/* 214 */       if (TblPegasusActiveConnections.getInstance().containsKey(this.iccid)) {
/* 215 */         return !((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).fecharConexao;
/*     */       }
/* 217 */       return true;
/*     */     } 
/*     */     
/* 220 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean keepThreadRunning() {
/* 225 */     if (TblPegasusActiveConnections.getInstance().containsKey(this.iccid)) {
/* 226 */       return this.myThreadGuid.equals(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).ownerThreadGuid);
/*     */     }
/* 228 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   private void sleepCommandThread() throws InterruptedException, IOException {
/* 233 */     for (byte ii = 0; ii < 10; ii = (byte)(ii + 1)) {
/* 234 */       Thread.sleep(500L);
/* 235 */       if (this.clientSocket.getInputStream().available() > 0) {
/*     */         break;
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   private void sleepThread() throws InterruptedException, IOException {
/* 242 */     for (byte ii = 0; ii < 2; ii = (byte)(ii + 1)) {
/* 243 */       Thread.sleep(2500L);
/* 244 */       if (this.clientSocket.getInputStream().available() > 0) {
/*     */         break;
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean processCommandPacket() {
/*     */     try {
/* 252 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, this.iccid, null);
/* 253 */       List<SP_024DataHolder> cmdsList = PegasusDBManager.executeSP_024(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).idModule);
/* 254 */       int count = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 262 */       FileChannel fc = null;
/*     */       
/* 264 */       int maxReadLength = 256;
/*     */       
/* 266 */       while (isSocketConnected() && count < cmdsList.size()) {
/* 267 */         SP_024DataHolder sp24DH = cmdsList.get(count++);
/* 268 */         if (PegasusDBManager.isCommandCancelled(sp24DH.getId_Command())) {
/*     */           continue;
/*     */         }
/* 271 */         String remoteCommandData = sp24DH.getCommandData();
/*     */ 
/*     */         
/*     */         label302: while (true) {
/* 275 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Sending_command") + sp24DH.getCommand_Type() + ":" + remoteCommandData, Enums.EnumMessagePriority.LOW, this.iccid, null);
/* 276 */           PegasusDBManager.updateCommandStatus(sp24DH.getId_Command());
/* 277 */           if (sendCommand(sp24DH.getCommand_Type(), remoteCommandData, sp24DH)) {
/* 278 */             if (sp24DH.getCommand_Type() == 14) {
/* 279 */               sleepCommandThread();
/* 280 */               if (this.clientSocket.getInputStream().available() == 1) {
/* 281 */                 byte[] arrayOfByte = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 282 */                 if (arrayOfByte != null && arrayOfByte[0] == 21) {
/* 283 */                   registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + arrayOfByte[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */                   break;
/*     */                 } 
/* 286 */                 registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */                 break;
/*     */               } 
/* 289 */               if (this.clientSocket.getInputStream().available() > 1) {
/* 290 */                 byte[] arrayOfByte = SocketFunctions.receive(this.clientSocket, 0, 41);
/* 291 */                 if (arrayOfByte != null && arrayOfByte.length == 41 && arrayOfByte[40] == 6) {
/* 292 */                   String data = Functions.getASCIIFromByteArray(arrayOfByte);
/* 293 */                   data = data.substring(0, data.length() - 1);
/* 294 */                   PegasusDBManager.updateSIMCardICCID(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).idModule, data.substring(0, 20), data.substring(20));
/* 295 */                   endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*     */                   break;
/*     */                 } 
/* 298 */                 registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */                 break;
/*     */               } 
/*     */               continue;
/*     */             } 
/* 303 */             byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 304 */             if (buffer.length == 1) {
/* 305 */               if (buffer[0] == 6) {
/* 306 */                 if (sp24DH.getCommand_Type() == 32769 || sp24DH.getCommand_Type() == 32770) {
/* 307 */                   String sourceFile = remoteCommandData;
/* 308 */                   String fileName = sourceFile;
/* 309 */                   if (fileName != null) {
/* 310 */                     while (fileName.contains(":")) {
/* 311 */                       fileName = fileName.replace(":", "$");
/*     */                     }
/*     */                   }
/* 314 */                   if (fileName != null && fileName.contains("/")) {
/* 315 */                     fileName = fileName.replaceAll("/", "@");
/*     */                   }
/*     */ 
/*     */                   
/* 319 */                   try { fc = (new RandomAccessFile(this.iccid + "_" + fileName, "rw")).getChannel();
/* 320 */                     fc.position(0L);
/* 321 */                     long flen = fc.size();
/* 322 */                     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*     */                     
/* 324 */                     while (isSocketConnected())
/* 325 */                     { int packetSize = (int)((flen - fc.position() > maxReadLength) ? maxReadLength : (flen - fc.position()));
/* 326 */                       if (packetSize > 0)
/* 327 */                       { ByteBuffer fReadBuffer = ByteBuffer.allocate(packetSize);
/* 328 */                         if (fc.read(fReadBuffer) == packetSize)
/*     */                           
/* 330 */                           try { SocketFunctions.send(this.clientSocket, fReadBuffer.array());
/*     */                             
/* 332 */                             try { Thread.sleep(10L);
/* 333 */                               buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 334 */                               if (buffer[0] == 6)
/* 335 */                               { Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + sourceFile + " (" + fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, this.iccid, null); }
/*     */                               else
/* 337 */                               { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Invalid_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sourceFile + " :" + buffer[0], sp24DH.getExec_Retries(), sp24DH.getId_Command());
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
/*     */ 
/*     */ 
/*     */ 
/*     */                                 
/* 378 */                                 if (fc != null) {
/* 379 */                                   fc.close();
/*     */                                 }
/* 381 */                                 if (this.tmpDownloadFile != null) { if (this.tmpDownloadFile.exists())
/* 382 */                                   { this.tmpDownloadFile.delete(); break label302; }  break label302; }  break label302; }  } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sourceFile, sp24DH.getExec_Retries(), sp24DH.getId_Command()); if (fc != null) fc.close();  if (this.tmpDownloadFile != null) { if (this.tmpDownloadFile.exists()) { this.tmpDownloadFile.delete(); break label302; }  break label302; }  }  break label302; } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_a_data_block_of_the_file") + sourceFile + LocaleMessage.getLocaleMessage("to_the_module"), sp24DH.getExec_Retries(), sp24DH.getId_Command()); if (fc != null) fc.close();  if (this.tmpDownloadFile != null) { if (this.tmpDownloadFile.exists()) { this.tmpDownloadFile.delete(); break label302; }  break label302; }  break label302; }   registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_reading_data_of_the_file") + sourceFile, sp24DH.getExec_Retries(), sp24DH.getId_Command()); if (fc != null) fc.close();  if (this.tmpDownloadFile != null) { if (this.tmpDownloadFile.exists()) { this.tmpDownloadFile.delete(); break label302; }  break label302; }  break label302; }  try { Thread.sleep(30L); buffer = SocketFunctions.receive(this.clientSocket, 0, 1); if (buffer[0] == 6) { Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("The_file_[") + sourceFile + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, this.iccid, null); endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); if (fc != null) fc.close();  if (this.tmpDownloadFile != null) { if (this.tmpDownloadFile.exists()) { this.tmpDownloadFile.delete(); break label302; }  break label302; }  break label302; }  registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_the_file") + sourceFile + LocaleMessage.getLocaleMessage("(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command()); } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + sourceFile + LocaleMessage.getLocaleMessage("was_sent_successfully"), sp24DH.getExec_Retries(), sp24DH.getId_Command()); }  }  if (fc != null) fc.close();  if (this.tmpDownloadFile != null && this.tmpDownloadFile.exists()) this.tmpDownloadFile.delete();  } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Exception_while_sending_the_file") + sourceFile + ": " + ex.getMessage(), sp24DH.getExec_Retries(), sp24DH.getId_Command()); if (fc != null) fc.close();  if (this.tmpDownloadFile != null && this.tmpDownloadFile.exists()) this.tmpDownloadFile.delete();  } finally { if (fc != null) fc.close();  if (this.tmpDownloadFile != null && this.tmpDownloadFile.exists()) this.tmpDownloadFile.delete();  }
/*     */                    continue;
/*     */                 } 
/* 385 */                 if (sp24DH.getCommand_Type() == 32771) {
/*     */                   
/* 387 */                   String destinationFile = this.iccid + "_" + remoteCommandData;
/* 388 */                   if (destinationFile != null) {
/* 389 */                     while (destinationFile.contains(":")) {
/* 390 */                       destinationFile = destinationFile.replace(":", "$");
/*     */                     }
/*     */                   }
/* 393 */                   if (destinationFile != null && destinationFile.contains("/")) {
/* 394 */                     destinationFile = destinationFile.replaceAll("/", "@");
/*     */                   }
/*     */                   
/* 397 */                   File destFile = null;
/* 398 */                   FileChannel dfc = null;
/*     */                   
/* 400 */                   try { destFile = new File(destinationFile);
/* 401 */                     if (destFile.exists()) {
/* 402 */                       destFile.delete();
/*     */                     }
/* 404 */                     dfc = (new RandomAccessFile(destFile, "rw")).getChannel();
/* 405 */                     dfc.position(0L);
/*     */                     
/* 407 */                     try { Thread.sleep(10L);
/* 408 */                       buffer = SocketFunctions.receive(this.clientSocket, 0, 4);
/* 409 */                       int flen = buffer[0] & 0xFF;
/* 410 */                       flen = flen * 256 + (buffer[1] & 0xFF);
/* 411 */                       flen = flen * 256 + (buffer[2] & 0xFF);
/* 412 */                       flen = flen * 256 + (buffer[3] & 0xFF);
/*     */                       
/* 414 */                       try { Thread.sleep(10L);
/* 415 */                         SocketFunctions.send(this.clientSocket, new byte[] { 6 }); }
/* 416 */                       catch (IOException|InterruptedException ex)
/* 417 */                       { ex.printStackTrace();
/* 418 */                         boolean bool = false;
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
/* 489 */                         if (dfc != null) {
/* 490 */                           dfc.close();
/*     */                         }
/* 492 */                         if (destFile != null && destFile.exists())
/* 493 */                           destFile.delete();  return bool; }  int crcCalculated = 65535; long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L; while (isSocketConnected()) { int packetSize = (int)((flen - dfc.position() > maxReadLength) ? maxReadLength : (flen - dfc.position())); if (packetSize > 0) try { buffer = SocketFunctions.receive(this.clientSocket, 0, packetSize); dfc.write(ByteBuffer.wrap(buffer)); crcCalculated = CRC16.calculate(buffer, 0, packetSize, crcCalculated); try { SocketFunctions.send(this.clientSocket, new byte[] { 6 }); } catch (IOException ex) { if (dfc != null) dfc.close();  if (destFile != null) { if (destFile.exists()) { destFile.delete(); break label302; }  break label302; }  }  break label302; } catch (IOException|InterruptedException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_receiving_a_data_block_of_the_file") + remoteCommandData, sp24DH.getExec_Retries(), sp24DH.getId_Command()); if (dfc != null) dfc.close();  if (destFile != null) { if (destFile.exists()) { destFile.delete(); break label302; }  break label302; }  break label302; }   try { Thread.sleep(10L); buffer = SocketFunctions.receive(this.clientSocket, 0, 2); int crcReceived = buffer[1] & 0xFF; crcReceived = crcReceived * 256 + (buffer[0] & 0xFF); if (crcCalculated == crcReceived) { try { byte[] fileData = Functions.getByteArrayFromFile(destinationFile); PegasusDBManager.executeSP_065(sp24DH.getId_Command(), new FileInputStream(destFile)); SocketFunctions.send(this.clientSocket, new byte[] { 6 }); Thread.sleep(10L); } catch (IOException|InterruptedException|SQLException ex) { if (dfc != null) dfc.close();  if (destFile != null) { if (destFile.exists()) { destFile.delete(); break label302; }  break label302; }  break label302; }  try { Thread.sleep(10L); buffer = SocketFunctions.receive(this.clientSocket, 0, 1); if (buffer[0] == 6) { Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("The_file_[") + remoteCommandData + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.iccid, null); endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); if (dfc != null) dfc.close();  if (destFile != null) { if (destFile.exists()) { destFile.delete(); break label302; }  break label302; }  break label302; }  registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_finalizing_upload_of_the_file") + remoteCommandData + LocaleMessage.getLocaleMessage("(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command()); } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_finalizing_upload_of_the_file") + remoteCommandData, sp24DH.getExec_Retries(), sp24DH.getId_Command()); }  break; }  registerFailureSendCommand(LocaleMessage.getLocaleMessage("CRC_of_the_file") + remoteCommandData + LocaleMessage.getLocaleMessage("invalid"), sp24DH.getExec_Retries(), sp24DH.getId_Command()); SocketFunctions.send(this.clientSocket, new byte[] { 21 }); } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_receiving_CRC_of_the_file") + remoteCommandData, sp24DH.getExec_Retries(), sp24DH.getId_Command()); }  }  } catch (IOException|InterruptedException|SQLException ex) { registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_expecting_file_size_information") + remoteCommandData, sp24DH.getExec_Retries(), sp24DH.getId_Command()); }  if (dfc != null) dfc.close();  if (destFile != null && destFile.exists()) destFile.delete();  } catch (IOException|InterruptedException|SQLException ex) { ex.printStackTrace(); registerFailureSendCommand(LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file") + remoteCommandData + ex.getMessage(), sp24DH.getExec_Retries(), sp24DH.getId_Command()); if (dfc != null) dfc.close();  if (destFile != null && destFile.exists()) destFile.delete();  } finally { if (dfc != null) dfc.close();  if (destFile != null && destFile.exists()) destFile.delete();  }
/*     */                   
/*     */                   continue;
/*     */                 } 
/* 497 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.iccid, null);
/* 498 */                 endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*     */                 
/*     */                 break;
/*     */               } 
/* 502 */               if (buffer[0] == 21 && sp24DH.getCommand_Type() == 32773) {
/* 503 */                 if (getNoofParams4mRemoteCommand(remoteCommandData) == 3) {
/* 504 */                   remoteCommandData = remoteCommandData.substring(0, remoteCommandData.lastIndexOf(";")); continue;
/*     */                 } 
/* 506 */                 registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */                 
/*     */                 break;
/*     */               } 
/* 510 */               if (buffer[0] == PegasusRoutines.EnumTipoPacote.COMMAND_PACKET.getPacket())
/*     */                 continue; 
/* 512 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */ 
/*     */               
/*     */               break;
/*     */             } 
/*     */             
/* 518 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */             
/*     */             break;
/*     */           } 
/*     */           
/* 523 */           registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*     */           
/*     */           break;
/*     */         } 
/* 527 */         Thread.sleep(10L);
/*     */       } 
/* 529 */       ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).newCommand = false;
/* 530 */       ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).nextSendingSolicitationReadingCommand = 0L;
/* 531 */       this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).communicationTimeout;
/*     */       try {
/* 533 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 534 */         return true;
/* 535 */       } catch (IOException ex) {
/* 536 */         return false;
/*     */       }
/*     */     
/* 539 */     } catch (Exception ex) {
/* 540 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_while_processing_the_COMMAND_packet"), Enums.EnumMessagePriority.HIGH, this.iccid, ex);
/*     */       try {
/* 542 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 543 */       } catch (IOException ex1) {
/* 544 */         Logger.getLogger(PegasusV1Handler.class.getName()).log(Level.SEVERE, (String)null, ex1);
/*     */       } 
/* 546 */       return false;
/*     */     } finally {}
/*     */   }
/*     */ 
/*     */   
/*     */   private void updateLastCommunicationModuleData() throws SQLException, InterruptedException {
/* 552 */     PegasusDBManager.executeSP_028(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).idModule, this.lastCommIface, 1);
/*     */   }
/*     */   
/*     */   private void endCommand(int id_Module, short exec_Retries) throws SQLException, InterruptedException {
/* 556 */     PegasusDBManager.executeSP_027(id_Module, (short)(exec_Retries + 1));
/*     */   }
/*     */   
/*     */   private int getNoofParams4mRemoteCommand(String remoteCommandData) {
/* 560 */     if (remoteCommandData != null && remoteCommandData.length() > 0) {
/* 561 */       return (remoteCommandData.split(";")).length;
/*     */     }
/* 563 */     return 0;
/*     */   }
/*     */ 
/*     */   
/*     */   private void registerFailureSendCommand(String msg, short exec_Retries, int id_Command) throws SQLException, InterruptedException {
/* 568 */     if (msg != null && msg.length() > 0) {
/* 569 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, msg, Enums.EnumMessagePriority.HIGH, this.iccid, null);
/*     */     }
/* 571 */     if (exec_Retries + 1 >= 3) {
/* 572 */       PegasusDBManager.executeSP_025(id_Command, (short)(exec_Retries + 1));
/*     */     } else {
/* 574 */       PegasusDBManager.executeSP_026(id_Command, (short)(exec_Retries + 1));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean sendCommand(int commandType, String commandData, SP_024DataHolder sp24DH) throws IOException {
/* 580 */     if (commandType == EnumCommandType.SET_CLOCK.getCommandType()) {
/* 581 */       if (commandData.length() == 0) {
/* 582 */         commandData = this.df.format(new Date());
/*     */       }
/* 584 */     } else if (commandType == 32769 || commandType == 32770) {
/* 585 */       FileChannel fc; String sourceFile = commandData;
/* 586 */       if (sourceFile != null) {
/* 587 */         while (sourceFile.contains(":")) {
/* 588 */           sourceFile = sourceFile.replace(":", "$");
/*     */         }
/*     */       }
/* 591 */       if (sourceFile != null && sourceFile.contains("/")) {
/* 592 */         sourceFile = sourceFile.replaceAll("/", "@");
/*     */       }
/* 594 */       this.tmpDownloadFile = Functions.writeByteArrayToFile(this.iccid + "_" + sourceFile, sp24DH.getCommandFileData());
/*     */       
/*     */       try {
/* 597 */         fc = (new RandomAccessFile(this.iccid + "_" + sourceFile, "rw")).getChannel();
/* 598 */       } catch (FileNotFoundException ex) {
/* 599 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_while_trying_to_open_the_file_to_be_sent_to_the_module"), Enums.EnumMessagePriority.HIGH, this.iccid, ex);
/* 600 */         return false;
/*     */       } 
/* 602 */       int maxReadLength = 256;
/*     */       
/* 604 */       int crcResult = 65535;
/* 605 */       long flen = fc.size();
/* 606 */       long tmpCount = 0L;
/*     */       
/*     */       try {
/* 609 */         fc.position(0L);
/* 610 */         while (tmpCount < flen) {
/* 611 */           int toRead = (int)((flen - fc.position() > maxReadLength) ? maxReadLength : (flen - fc.position()));
/* 612 */           if (toRead > 0) {
/* 613 */             ByteBuffer buffer = ByteBuffer.allocate(toRead);
/* 614 */             if (fc.read(buffer) == toRead) {
/* 615 */               crcResult = CRC16.calculate(buffer.array(), 0, toRead, crcResult);
/*     */             } else {
/* 617 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failure_while_reading_data_of_the_file_for_CRC_calculation"), Enums.EnumMessagePriority.HIGH, this.iccid, null);
/* 618 */               return false;
/*     */             } 
/* 620 */             tmpCount += toRead;
/*     */           } 
/*     */         } 
/* 623 */       } catch (IOException ex) {
/* 624 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_while_reading_data_of_the_file_for_CRC_calculation"), Enums.EnumMessagePriority.HIGH, this.iccid, ex);
/* 625 */         return false;
/*     */       } finally {
/* 627 */         fc.close();
/*     */       } 
/* 629 */       commandData = commandData + ";" + flen + ";" + crcResult;
/* 630 */     } else if (commandType != 32771) {
/*     */       
/* 632 */       if (commandType == 32775) {
/*     */         
/* 634 */         String[] tmp = commandData.split(";");
/* 635 */         StringBuilder sb = new StringBuilder();
/* 636 */         sb.append(Integer.parseInt(tmp[0]) - 1).append(';').append(tmp[2]);
/*     */         
/* 638 */         commandData = sb.toString();
/*     */       } 
/* 640 */     }  byte[] b = new byte[commandData.length() + 6];
/* 641 */     b[0] = (byte)PegasusRoutines.EnumTipoPacote.COMMAND_PACKET.getPacket();
/* 642 */     b[1] = (byte)getOldCommandType4mNew(commandType);
/* 643 */     b[2] = (byte)(commandData.length() & 0xFF);
/* 644 */     b[3] = (byte)((commandData.length() & 0xFF00) / 256);
/*     */     
/* 646 */     for (int count = 0; count < commandData.length(); count++) {
/* 647 */       b[count + 4] = (byte)commandData.charAt(count);
/*     */     }
/* 649 */     int crcCalculated = CRC16.calculate(b, 0, commandData.length() + 4, 65535);
/*     */     
/* 651 */     b[commandData.length() + 4] = (byte)(crcCalculated & 0xFF);
/* 652 */     b[commandData.length() + 5] = (byte)((crcCalculated & 0xFF00) / 256);
/*     */     
/*     */     try {
/* 655 */       SocketFunctions.send(this.clientSocket, b);
/* 656 */       return true;
/* 657 */     } catch (IOException ex) {
/* 658 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.iccid, null);
/* 659 */       ex.printStackTrace();
/* 660 */       return false;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean processIdentificationPacket() {
/*     */     try {
/* 667 */       TblPegasusActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/* 668 */       this.iccid = getICCID(this.bufferRx, 1);
/* 669 */       if (this.iccid.equals("00000000000000000000")) {
/* 670 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/* 671 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 672 */         return false;
/* 673 */       }  if (this.iccid.equals("00000000000000000001")) {
/* 674 */         String ip = this.remoteIP.substring(0, this.remoteIP.indexOf(":"));
/* 675 */         if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 676 */           if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/* 677 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/* 678 */             SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 679 */             return false;
/*     */           }
/*     */         
/* 682 */         } else if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/* 683 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/* 684 */           SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 685 */           return false;
/*     */         } 
/*     */       } 
/*     */       
/* 689 */       this.lastCommIface = (short)this.bufferRx[12];
/* 690 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_IDENTIFICATION_packet_received-ID") + this.iccid + Functions.getIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/* 691 */       executeStoredProcedureHandlingIdentificationPacket(this.bufferRx, this.iccid, this.remoteIP.substring(0, this.remoteIP.indexOf(":")), "", Enums.EnumNWProtocol.TCP.name());
/* 692 */       if (this.sp15DH != null) {
/* 693 */         if (this.sp15DH.getAuto_Registration_Executed() == 1) {
/* 694 */           if (this.sp15DH.getRegistered() == 0) {
/* 695 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + this.iccid, Enums.EnumMessagePriority.HIGH, null, null);
/* 696 */             SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/* 697 */             return false;
/*     */           } 
/* 699 */         } else if (this.sp15DH.getRegistered() == 1) {
/* 700 */           if (this.sp15DH.getEnabled() == 0) {
/* 701 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.iccid + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 702 */             SocketFunctions.send(this.clientSocket, new byte[] { -31 });
/* 703 */             return false;
/*     */           } 
/*     */         } else {
/* 706 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.iccid + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 707 */           SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/* 708 */           return false;
/*     */         } 
/*     */         
/* 711 */         if (TblPegasusActiveConnections.getInstance().containsKey(this.iccid)) {
/* 712 */           TblPegasusActiveConnections.removeConnection(this.iccid);
/*     */         }
/*     */         
/* 715 */         TblPegasusActiveConnections.addConnection(this.iccid, this.myThreadGuid);
/* 716 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).idClient = this.sp15DH.getId_Client();
/* 717 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).idModule = this.sp15DH.getId_Module();
/* 718 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).idGroup = this.sp15DH.getId_Group();
/* 719 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).communicationTimeout = this.sp15DH.getComm_Timeout() * 1000;
/* 720 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).clientName = this.sp15DH.getName();
/* 721 */         this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).communicationTimeout;
/*     */         try {
/* 723 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 724 */           return true;
/* 725 */         } catch (IOException ex) {
/* 726 */           return false;
/*     */         } 
/*     */       } 
/* 729 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Error_while_processing_the_IDENTIFICATION_packet"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/* 730 */       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 731 */       return false;
/*     */     
/*     */     }
/* 734 */     catch (IOException|InterruptedException|SQLException ex) {
/* 735 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_IDENTIFICATION_packet"), Enums.EnumMessagePriority.HIGH, null, ex);
/*     */       try {
/* 737 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 738 */       } catch (IOException ex1) {
/* 739 */         Logger.getLogger(PegasusV1Handler.class.getName()).log(Level.SEVERE, (String)null, ex1);
/*     */       } 
/* 741 */       return false;
/*     */     } finally {
/* 743 */       TblPegasusActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean processEventPacket() {
/*     */     try {
/*     */       byte[] bufferEvent;
/* 750 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Event_Packet_Received") + Functions.getIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, this.iccid, null);
/* 751 */       if (this.bufferRx[1] == Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId()) {
/* 752 */         bufferEvent = new byte[8];
/* 753 */         System.arraycopy(this.bufferRx, 6, bufferEvent, 0, 8);
/*     */       } else {
/* 755 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Event_received_with_invalid_protocol_identifier"), Enums.EnumMessagePriority.HIGH, this.iccid, null);
/* 756 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 757 */         return false;
/*     */       } 
/* 759 */       InfoModule iModule = (InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid);
/* 760 */       updateClientCode(bufferEvent, iModule.idClient);
/* 761 */       String comPort = Functions.getReceiverCOMPortByGroupID(iModule.idGroup, "PEGASUS");
/* 762 */       Functions.insertEvent(Util.EnumProductIDs.PEGASUS.getProductId(), iModule.idModule, iModule.idGroup, iModule.idClient, comPort, this.bufferRx[1], bufferEvent, 1, Enums.EnumNWProtocol.TCP.name(), this.lastCommIface, -1, 1);
/* 763 */       generateEventFailureTransmissionTestTelephoneLine(this.bufferRx, iModule.idClient, iModule.idModule, iModule.idGroup, 2, Enums.EnumNWProtocol.TCP.name());
/*     */       
/* 765 */       if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/* 766 */         ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*     */       }
/*     */       
/* 769 */       this.idleTimeout = System.currentTimeMillis() + iModule.communicationTimeout;
/*     */       try {
/* 771 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 772 */         return true;
/* 773 */       } catch (IOException ex) {
/* 774 */         return false;
/*     */       } 
/* 776 */     } catch (Exception ex) {
/* 777 */       byte[] bufferEvent; Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_while_processing_the_EVENT_packet"), Enums.EnumMessagePriority.HIGH, this.iccid, (Throwable)bufferEvent);
/*     */       try {
/* 779 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 780 */       } catch (IOException iOException) {}
/*     */       
/* 782 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean processAlivePacket() {
/*     */     try {
/*     */       InfoModule infoModule;
/*     */       try {
/* 790 */         TblPegasusActiveConnections.semaphoreAlivePacketsReceived.acquire();
/* 791 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("ALIVE_packet_received") + Functions.getIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, this.iccid, null);
/* 792 */         infoModule = (InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid);
/* 793 */         executeStoredProcedureHandlingAlivePacket(this.bufferRx, infoModule.idModule, Enums.EnumNWProtocol.TCP.name());
/*     */       } finally {
/* 795 */         TblPegasusActiveConnections.semaphoreAlivePacketsReceived.release();
/*     */       } 
/* 797 */       if (this.sp03DH != null) {
/* 798 */         if (this.sp03DH.getRegistered() == 0) {
/* 799 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Module_ID_(") + infoModule.idModule + LocaleMessage.getLocaleMessage(")_was_not_found_in_the_database_or_was_disabled"), Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/* 800 */           SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 801 */           return false;
/*     */         } 
/* 803 */         generateEventReceptionAlivePacket(infoModule.idClient, infoModule.idModule, infoModule.idGroup, "SP_003", Enums.EnumNWProtocol.TCP.name());
/* 804 */         infoModule.communicationDebug = false;
/*     */         
/* 806 */         if (this.sp03DH.getComm_Debug() == 1) {
/* 807 */           infoModule.communicationDebug = true;
/*     */         }
/* 809 */         infoModule.clientName = this.sp03DH.getName();
/* 810 */         infoModule.communicationTimeout = this.sp03DH.getComm_Timeout() * 1000;
/* 811 */         this.idleTimeout = System.currentTimeMillis() + infoModule.communicationTimeout;
/* 812 */         TblPegasusActiveConnections.getInstance().put(this.iccid, infoModule);
/*     */         try {
/* 814 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 815 */           return true;
/* 816 */         } catch (IOException ex) {
/* 817 */           return false;
/*     */         } 
/*     */       } 
/*     */       
/* 821 */       if (!DerbyDBBackup.backupModeActivated) {
/* 822 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Error_while_retrieving_module_information_ID_(") + infoModule.idModule + ")", Enums.EnumMessagePriority.AVERAGE, this.iccid, null);
/* 823 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */       } 
/* 825 */       return false;
/*     */     }
/* 827 */     catch (Exception ex) {
/* 828 */       InfoModule infoModule; if (!DerbyDBBackup.backupModeActivated) {
/* 829 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_while_processing_the_ALIVE_packet") + infoModule.toString(), Enums.EnumMessagePriority.HIGH, this.iccid, null);
/*     */         try {
/* 831 */           SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/* 832 */         } catch (IOException iOException) {}
/*     */       } 
/*     */ 
/*     */       
/* 836 */       return false;
/*     */     } finally {}
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void dispose() {
/* 843 */     if (TblPegasusActiveConnections.getInstance().containsKey(this.iccid) && 
/* 844 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.iccid)).ownerThreadGuid)) {
/* 845 */       TblPegasusActiveConnections.removeConnection(this.iccid);
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
/*     */   private int getOldCommandType4mNew(int newCommandType) {
/* 857 */     int oldCommandType = 0;
/* 858 */     switch (newCommandType) {
/*     */       case 32769:
/* 860 */         oldCommandType = 8;
/*     */         break;
/*     */       case 32770:
/* 863 */         oldCommandType = 9;
/*     */         break;
/*     */       case 32771:
/* 866 */         oldCommandType = 11;
/*     */         break;
/*     */       case 32772:
/* 869 */         oldCommandType = 3;
/*     */         break;
/*     */       case 32773:
/* 872 */         oldCommandType = 1;
/*     */         break;
/*     */       case 32774:
/* 875 */         oldCommandType = 2;
/*     */         break;
/*     */       case 32775:
/* 878 */         oldCommandType = 10;
/*     */         break;
/*     */       case 14:
/* 881 */         oldCommandType = 14; break;
/*     */     } 
/* 883 */     return oldCommandType;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\PegasusV1Handler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */