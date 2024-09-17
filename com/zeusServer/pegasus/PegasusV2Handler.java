/*      */ package com.zeusServer.pegasus;
/*      */ 
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.PegasusDBManager;
/*      */ import com.zeusServer.dto.SP_024DataHolder;
/*      */ import com.zeusServer.serialPort.communication.EmulateReceiver;
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
/*      */ import com.zeusServer.util.SocketFunctions;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import com.zeusServer.util.ZeusServerLogger;
/*      */ import com.zeuscc.pegasus.derby.beans.SP_V2_001_VO;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.net.InetAddress;
/*      */ import java.net.Socket;
/*      */ import java.net.SocketException;
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
/*      */ public class PegasusV2Handler
/*      */   extends PegasusV2Routines
/*      */   implements Runnable
/*      */ {
/*      */   private Socket clientSocket;
/*      */   private short encType;
/*   65 */   private String myThreadGuid = UUID.randomUUID().toString();
/*   66 */   private String sn = "";
/*      */   private String remoteIP;
/*      */   private boolean initialMsgFlag;
/*      */   private byte[] prod;
/*   70 */   private Logger ownLogger = null;
/*      */ 
/*      */   
/*      */   public PegasusV2Handler(Socket sock, short encType, byte[] prod) throws SocketException {
/*   74 */     this.clientSocket = sock;
/*   75 */     this.encType = encType;
/*   76 */     this.clientSocket.setSoTimeout(30000);
/*   77 */     this.clientSocket.setTcpNoDelay(false);
/*   78 */     this.prod = prod;
/*   79 */     this.initialMsgFlag = true;
/*   80 */     this.remoteIP = this.clientSocket.getRemoteSocketAddress().toString();
/*   81 */     this.remoteIP = this.remoteIP.substring(1);
/*   82 */     this.idleTimeout = System.currentTimeMillis() + 15000L;
/*   83 */     Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_connected"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */ 
/*      */   
/*      */   public void run() {
/*      */     
/*   89 */     try { int bytesReceived = 0;
/*      */       
/*   91 */       byte[] decBlock = null;
/*      */       
/*   93 */       while (isSocketConnected() && keepThreadRunning() && this.idleTimeout > System.currentTimeMillis()) {
/*   94 */         if (this.clientSocket.getInputStream().available() > 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*   95 */           if (this.clientSocket.getInputStream().available() == 1) {
/*   96 */             byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*   97 */             if (buffer.length == 1) {
/*   98 */               if ((buffer[0] & 0x7) == 4) {
/*   99 */                 if (!processCommandPacket()) {
/*      */                   break;
/*      */                 }
/*      */               } else {
/*  103 */                 bytesReceived++;
/*      */               } 
/*      */             }
/*  106 */           } else if (this.clientSocket.getInputStream().available() > 1 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  107 */             if (!this.initialMsgFlag) {
/*  108 */               this.prod = Functions.swapLSB2MSB(SocketFunctions.receive(this.clientSocket, 0, 2));
/*  109 */               String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(this.prod[0] & 0xFF) }).replace(' ', '0');
/*  110 */               this.encType = Short.parseShort(prodBin.substring(0, 2), 2);
/*  111 */               prodBin = prodBin.substring(2);
/*  112 */               prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(this.prod[1] & 0xFF) }).replace(' ', '0'));
/*  113 */               short prodI = Short.parseShort(prodBin, 2);
/*  114 */               if (this.prod[0] == 43 && this.prod[1] == 43) {
/*  115 */                 dispose();
/*      */                 
/*      */                 break;
/*      */               } 
/*  119 */               if (prodI != Util.EnumProductIDs.PEGASUS.getProductId() && this.prod[0] != 43 && this.prod[1] != 43) {
/*      */                 
/*  121 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.sn + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_TCP"), Enums.EnumMessagePriority.HIGH, null, null);
/*      */                 try {
/*  123 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  124 */                 } catch (IOException ex) {}
/*      */                 
/*      */                 break;
/*      */               } 
/*      */             } 
/*      */             
/*  130 */             if (this.encType != 1 && this.encType != 2) {
/*  131 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Module_Connected_with_Invalid_Encryption"), Enums.EnumMessagePriority.AVERAGE, this.remoteIP, null);
/*      */               break;
/*      */             } 
/*  134 */             byte[] length = Functions.swapLSB2MSB(SocketFunctions.receive(this.clientSocket, 0, 2));
/*      */             
/*  136 */             int msgLen = Functions.getIntFrom2ByteArray(length);
/*      */             
/*  138 */             byte[] data = SocketFunctions.receive(this.clientSocket, 0, msgLen);
/*  139 */             byte[] cbits = new byte[msgLen + 2];
/*  140 */             cbits[0] = this.prod[1];
/*  141 */             cbits[1] = this.prod[0];
/*  142 */             cbits[2] = length[1];
/*  143 */             cbits[3] = length[0];
/*      */             
/*  145 */             System.arraycopy(data, 0, cbits, 4, msgLen - 2);
/*  146 */             int crcCalc = CRC16.calculate(cbits, 0, msgLen + 2, 65535);
/*  147 */             byte[] crcbits = new byte[2];
/*  148 */             crcbits[0] = data[msgLen - 2];
/*  149 */             crcbits[1] = data[msgLen - 1];
/*  150 */             crcbits = Functions.swapLSB2MSB(crcbits);
/*  151 */             int crcRecv = Functions.getIntFrom2ByteArray(crcbits);
/*  152 */             if (crcCalc == crcRecv) {
/*      */               
/*  154 */               byte[] encData = new byte[msgLen - 2];
/*  155 */               System.arraycopy(data, 0, encData, 0, msgLen - 2);
/*  156 */               byte[] decData = new byte[msgLen - 2];
/*  157 */               if (encData.length >= 16 && encData.length % 16 == 0) {
/*  158 */                 for (int i = 0; i < encData.length; ) {
/*  159 */                   byte[] block = new byte[16];
/*  160 */                   System.arraycopy(encData, i, block, 0, 16);
/*  161 */                   if (this.encType == 1) {
/*  162 */                     decBlock = Rijndael.decryptBytes(block, Rijndael.dataKeyBytes, false);
/*  163 */                   } else if (this.encType == 2) {
/*  164 */                     decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */                   } 
/*  166 */                   System.arraycopy(decBlock, 0, decData, i, 16);
/*  167 */                   i += 16;
/*      */                 } 
/*      */               }
/*  170 */               if (!parseM2SPacket(decData)) {
/*      */                 break;
/*      */               }
/*      */             } else {
/*  174 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_packet_received_from_the_module"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*      */               try {
/*  176 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  177 */               } catch (IOException ex) {
/*      */                 break;
/*      */               } 
/*      */             } 
/*  181 */             bytesReceived = 0;
/*  182 */             if (this.initialMsgFlag) {
/*  183 */               this.initialMsgFlag = false;
/*      */             }
/*      */           } 
/*  186 */         } else if (bytesReceived == 0 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && 
/*  187 */           TblPegasusActiveConnections.getInstance().containsKey(this.sn) && 
/*  188 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).newCommand) {
/*      */           
/*  190 */           if (this.idleTimeout - ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).communicationTimeout + 10000L < System.currentTimeMillis() && ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand < System.currentTimeMillis()) {
/*  191 */             byte[] newCmd = Functions.intToByteArray(128, 1);
/*  192 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("New_command_prepared_to_be_transmitted_to_the_device") + Integer.toHexString(128), Enums.EnumMessagePriority.LOW, this.sn, null);
/*      */             try {
/*  194 */               SocketFunctions.send(this.clientSocket, newCmd);
/*  195 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Requesting_to_the_module_the_reading_of_a_NEW_COMMAND_saved_in_the_database"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  196 */               ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = System.currentTimeMillis() + 60000L;
/*  197 */             } catch (IOException ex) {
/*      */               break;
/*      */             } 
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
/*  218 */       { Logger.getLogger(PegasusV2Handler.class.getName()).log(Level.SEVERE, (String)null, ex); }  } catch (IOException|NumberFormatException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) { ex.printStackTrace(); Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Exception_in_the_Data_Server_task"), Enums.EnumMessagePriority.HIGH, this.sn, ex); } finally { try { if (this.clientSocket != null) this.clientSocket.close();  dispose(); } catch (IOException ex) { Logger.getLogger(PegasusV2Handler.class.getName()).log(Level.SEVERE, (String)null, ex); }
/*      */        }
/*      */   
/*      */   }
/*      */   
/*      */   private boolean isSocketConnected() {
/*  224 */     if (this.clientSocket.isConnected()) {
/*  225 */       if (TblPegasusActiveConnections.getInstance().containsKey(this.sn)) {
/*  226 */         return !((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).fecharConexao;
/*      */       }
/*  228 */       return true;
/*      */     } 
/*      */     
/*  231 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean keepThreadRunning() {
/*  236 */     if (TblPegasusActiveConnections.getInstance().containsKey(this.sn)) {
/*  237 */       return this.myThreadGuid.equals(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).ownerThreadGuid);
/*      */     }
/*  239 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   private void sleepThread() throws InterruptedException, IOException {
/*  244 */     for (byte ii = 0; ii < 2; ii = (byte)(ii + 1)) {
/*  245 */       Thread.sleep(2500L);
/*  246 */       if (this.clientSocket.getInputStream().available() > 0) {
/*      */         break;
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void dispose() {
/*  253 */     this.clientSocket = SocketFunctions.closeSocket(this.clientSocket);
/*  254 */     if (TblPegasusActiveConnections.getInstance().containsKey(this.sn) && 
/*  255 */       this.myThreadGuid.equalsIgnoreCase(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).ownerThreadGuid)) {
/*  256 */       TblPegasusActiveConnections.removeConnection(this.sn);
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean processCommandPacket() {
/*      */     try {
/*  263 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("COMMAND_packet_received"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  264 */       List<SP_024DataHolder> cmdsList = PegasusDBManager.executeSP_024(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idModule);
/*  265 */       int count = 0;
/*  266 */       int cmdsSize = cmdsList.size();
/*      */       
/*  268 */       while (isSocketConnected() && count < cmdsSize) {
/*  269 */         SP_024DataHolder sp24DH = cmdsList.get(count++);
/*  270 */         if (PegasusDBManager.isCommandCancelled(sp24DH.getId_Command())) {
/*      */           continue;
/*      */         }
/*  273 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Sending_command") + sp24DH.getCommand_Type() + ":" + sp24DH.getCommandData(), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  274 */         PegasusDBManager.updateCommandStatus(sp24DH.getId_Command());
/*  275 */         if (sendRemoteCommand(sp24DH)) {
/*  276 */           byte[] buffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  277 */           if (buffer.length == 1) {
/*  278 */             if (buffer[0] == 6) {
/*  279 */               if (sp24DH.getCommand_Type() == 32769 || sp24DH.getCommand_Type() == 32770) {
/*  280 */                 sendFile2Module(sp24DH);
/*  281 */               } else if (sp24DH.getCommand_Type() == 32771) {
/*  282 */                 receiveFile4MModule(sp24DH);
/*      */               } else {
/*  284 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Command_sent_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/*  285 */                 endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/*      */               } 
/*  287 */             } else if ((buffer[0] & 0xFF) == 20 && (sp24DH.getCommand_Type() == 32785 || sp24DH
/*  288 */               .getCommand_Type() == 32784 || sp24DH
/*  289 */               .getCommand_Type() == 32775)) {
/*  290 */               if (sp24DH.getCommand_Type() == 32785) {
/*  291 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  292 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Armed_cant_bypass_a_zone"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  293 */                 } else if (sp24DH.getCommandData().charAt(0) == '2') {
/*  294 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Armed_cant_unbypass_a_zone"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*  296 */               } else if (sp24DH.getCommand_Type() == 32784) {
/*  297 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  298 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  299 */                 } else if (sp24DH.getCommandData().charAt(0) == '2') {
/*  300 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Disarmed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  301 */                 } else if (sp24DH.getCommandData().charAt(0) == '3') {
/*  302 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  303 */                 } else if (sp24DH.getCommandData().charAt(0) == '4') {
/*  304 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  305 */                 } else if (sp24DH.getCommandData().charAt(0) == '5') {
/*  306 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_is_Already_Armed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*  308 */               } else if (sp24DH.getCommand_Type() == 32775) {
/*  309 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  310 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Generate_Pulse_In_Relay"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } else {
/*  312 */                   int pgmIndex = Character.getNumericValue(sp24DH.getCommandData().charAt(0));
/*  313 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Generate_Pulse_Selected_output") + (pgmIndex - 1), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*      */               } 
/*  316 */               PegasusDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + '\024');
/*  317 */             } else if ((buffer[0] & 0xFF) == 19 && (sp24DH.getCommand_Type() == 32785 || sp24DH.getCommand_Type() == 32784)) {
/*  318 */               if (sp24DH.getCommand_Type() == 32785) {
/*  319 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  320 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_is_Already_Bypassed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  321 */                 } else if (sp24DH.getCommandData().charAt(0) == '2') {
/*  322 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_is_Already_Unbypassed"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*  324 */               } else if (sp24DH.getCommand_Type() == 32784) {
/*  325 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  326 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_arm"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  327 */                 } else if (sp24DH.getCommandData().charAt(0) == '2') {
/*  328 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_disarm"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  329 */                 } else if (sp24DH.getCommandData().charAt(0) == '3') {
/*  330 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_forcearm"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  331 */                 } else if (sp24DH.getCommandData().charAt(0) == '4') {
/*  332 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_stayarm"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  333 */                 } else if (sp24DH.getCommandData().charAt(0) == '5') {
/*  334 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_cant_forcestayarm"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*      */               } 
/*  337 */               PegasusDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + '\023');
/*  338 */             } else if ((buffer[0] & 0xFF) == 18 && (sp24DH.getCommand_Type() == 32785 || sp24DH.getCommand_Type() == 32784)) {
/*  339 */               if (sp24DH.getCommand_Type() == 32785) {
/*  340 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  341 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_cant_bypass"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  342 */                 } else if (sp24DH.getCommandData().charAt(0) == '2') {
/*  343 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_Zone_cant_unbypass"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*  345 */               } else if (sp24DH.getCommand_Type() == 32784) {
/*  346 */                 if (sp24DH.getCommandData().charAt(0) == '1') {
/*  347 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  348 */                 } else if (sp24DH.getCommandData().charAt(0) == '2') {
/*  349 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  350 */                 } else if (sp24DH.getCommandData().charAt(0) == '3') {
/*  351 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  352 */                 } else if (sp24DH.getCommandData().charAt(0) == '4') {
/*  353 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  354 */                 } else if (sp24DH.getCommandData().charAt(0) == '5') {
/*  355 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_configuration_error"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */                 } 
/*      */               } 
/*  358 */               PegasusDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + '\022');
/*  359 */             } else if ((buffer[0] & 0xFF) == 17 && sp24DH.getCommand_Type() == 32784) {
/*  360 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failed_to_execute_command_System_In_Alarm"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*  361 */               PegasusDBManager.updateCommandFailureStatus(sp24DH.getId_Command(), sp24DH.getExec_Retries(), sp24DH.getCommandData() + ";" + '\021');
/*      */             }
/*  363 */             else if (buffer[0] == 21) {
/*  364 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             } else {
/*  366 */               if (buffer[0] == PegasusRoutines.EnumTipoPacote.COMMAND_PACKET.getPacket()) {
/*      */                 continue;
/*      */               }
/*  369 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command_(response") + buffer[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             }
/*      */           
/*      */           } else {
/*      */             
/*  374 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */           } 
/*      */         } else {
/*  377 */           registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_command"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         } 
/*  379 */         Thread.sleep(100L);
/*      */       } 
/*  381 */       ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).newCommand = false;
/*  382 */       ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).nextSendingSolicitationReadingCommand = 0L;
/*  383 */       this.idleTimeout = System.currentTimeMillis() + ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).communicationTimeout;
/*      */       try {
/*  385 */         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  386 */         return true;
/*  387 */       } catch (IOException ex) {
/*  388 */         return false;
/*      */       } 
/*  390 */     } catch (Exception ex) {
/*  391 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_while_processing_the_COMMAND_packet"), Enums.EnumMessagePriority.HIGH, this.sn, ex);
/*      */       try {
/*  393 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  394 */       } catch (IOException ex1) {
/*  395 */         Logger.getLogger(PegasusV2Handler.class.getName()).log(Level.SEVERE, (String)null, ex1);
/*      */       } 
/*  397 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public byte[] prepareCommandPacket(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/*  402 */     int plen = data.length;
/*      */     
/*  404 */     int lpad = plen % 16;
/*  405 */     if (lpad > 0) {
/*  406 */       lpad = 16 - lpad;
/*      */     }
/*  408 */     byte[] packet = new byte[plen + lpad + 4];
/*  409 */     byte[] toEnc = new byte[plen + lpad];
/*  410 */     System.arraycopy(data, 0, toEnc, 0, plen);
/*  411 */     if (lpad > 0) {
/*  412 */       for (int j = plen; j < plen + lpad; j++) {
/*  413 */         toEnc[j] = 0;
/*      */       }
/*      */     }
/*      */ 
/*      */     
/*  418 */     for (int i = 0; i < toEnc.length; i += 16) {
/*  419 */       byte[] block = new byte[16];
/*  420 */       System.arraycopy(toEnc, i, block, 0, 16);
/*  421 */       byte[] decBlock = null;
/*  422 */       if (this.encType == 1) {
/*  423 */         decBlock = Rijndael.encryptBytes(block, Rijndael.dataKeyBytes, false);
/*  424 */       } else if (this.encType == 2) {
/*  425 */         decBlock = Rijndael.encryptBytes(block, Rijndael.aes_256, false);
/*      */       } 
/*  427 */       System.arraycopy(decBlock, 0, toEnc, i, 16);
/*      */     } 
/*      */     
/*  430 */     byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(plen + lpad + 2));
/*  431 */     System.arraycopy(tmp, 0, packet, 0, 2);
/*  432 */     System.arraycopy(toEnc, 0, packet, 2, plen + lpad);
/*      */     
/*  434 */     int crcCalc = CRC16.calculate(packet, 0, plen + lpad + 2, 65535);
/*  435 */     tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/*  436 */     System.arraycopy(tmp, 0, packet, plen + lpad + 2, 2);
/*  437 */     return packet;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean parseM2SPacket(byte[] decData) {
/*  442 */     List<String> originalClientCodes = new ArrayList<>(6);
/*  443 */     byte[] eventData = new byte[48];
/*  444 */     byte[] fid = new byte[2];
/*  445 */     int contactIdCounter = 0;
/*      */     
/*  447 */     int index = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     try {
/*  453 */       TblPegasusActiveConnections.numberOfPendingIdentificationPackets.incrementAndGet();
/*  454 */       this.spV201VO = new SP_V2_001_VO();
/*  455 */       this.spV201VO.setM2sData(decData);
/*  456 */       this.spV201VO.setLastNWProtocol(Enums.EnumNWProtocol.TCP.name());
/*  457 */       if (this.encType == 1) {
/*  458 */         this.spV201VO.setLastEncryption(Enums.EnumEncyption.AES128.getType());
/*  459 */       } else if (this.encType == 2) {
/*  460 */         this.spV201VO.setLastEncryption(Enums.EnumEncyption.AES256.getType());
/*      */       } 
/*      */       
/*  463 */       boolean firstPacket_RCVD_LCI = false;
/*  464 */       if (!this.initialMsgFlag) {
/*  465 */         this.spV201VO.setSn(this.sn);
/*      */       }
/*      */ 
/*      */       
/*  469 */       while (index < decData.length && 
/*  470 */         index + 2 <= decData.length) {
/*      */         StringBuilder sb; int i; byte oper[], numByte; StringBuilder originalEvent; String sEvent; int idGroup; String comPort;
/*      */         byte[] rawTimezone;
/*  473 */         System.arraycopy(decData, index, fid, 0, 2);
/*  474 */         index += 2;
/*  475 */         fid = Functions.swapLSB2MSB(fid);
/*  476 */         int fidVal = Functions.getIntFrom2ByteArray(fid);
/*  477 */         if (fidVal <= 0) {
/*      */           break;
/*      */         }
/*  480 */         short flen = (short)Functions.getIntFromHexByte(decData[index]);
/*  481 */         byte[] fcon = new byte[flen];
/*  482 */         System.arraycopy(decData, ++index, fcon, 0, flen);
/*  483 */         index += flen;
/*      */         
/*  485 */         switch (fidVal) {
/*      */           case 1:
/*  487 */             sb = new StringBuilder();
/*  488 */             for (i = 0; i < flen; i++) {
/*  489 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  491 */             this.spV201VO.setSn(sb.toString());
/*  492 */             this.sn = this.spV201VO.getSn();
/*  493 */             if (this.sn.equals("00000000000000000000")) {
/*  494 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_DEFAULT_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  495 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  496 */               i = 0; return i;
/*  497 */             }  if (this.sn.equals("00000000000000000001")) {
/*  498 */               String ip = this.remoteIP.substring(0, this.remoteIP.indexOf(":"));
/*  499 */               if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  500 */                 if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress()) && !ip.equals(Functions.getDataServerIP())) {
/*  501 */                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  502 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  503 */                   return false;
/*      */                 }  break;
/*      */               } 
/*  506 */               if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.equals(InetAddress.getLocalHost().getHostAddress())) {
/*  507 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Connection_refused_This_ID_is_reserved_for_the_WATCHDOG_account"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  508 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  509 */                 return false;
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case 2:
/*  516 */             sb = new StringBuilder();
/*  517 */             for (i = 0; i < flen - 1; i++) {
/*  518 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  520 */             sb.append(fcon[7] / 10);
/*  521 */             this.spV201VO.setModemIMEI(sb.toString());
/*      */             break;
/*      */           
/*      */           case 45:
/*  525 */             sb = new StringBuilder();
/*  526 */             for (i = 1; i < flen - 1; i++) {
/*  527 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  529 */             sb.append(fcon[8] / 10);
/*  530 */             if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  531 */               this.spV201VO.setSimcard1IMSI(sb.toString()); break;
/*  532 */             }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 2) {
/*  533 */               this.spV201VO.setSimcard2IMSI(sb.toString());
/*      */             }
/*      */             break;
/*      */           
/*      */           case 3:
/*  538 */             this.spV201VO.setModemModel((short)fcon[0]);
/*      */             break;
/*      */           
/*      */           case 4:
/*  542 */             sb = new StringBuilder();
/*  543 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFromHexByte(fcon[2])).append(".").append(Functions.getIntFromHexByte(fcon[3]));
/*  544 */             this.spV201VO.setModemFWVersion(sb.toString());
/*      */             break;
/*      */           
/*      */           case 5:
/*  548 */             sb = new StringBuilder();
/*  549 */             for (i = 1; i < flen; i++) {
/*  550 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/*  552 */             if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  553 */               this.spV201VO.setSimcard1ICCID(sb.toString()); break;
/*  554 */             }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 2) {
/*  555 */               this.spV201VO.setSimcard2ICCID(sb.toString());
/*      */             }
/*      */             break;
/*      */           
/*      */           case 6:
/*  560 */             oper = new byte[flen - 1];
/*  561 */             System.arraycopy(fcon, 1, oper, 0, flen - 1);
/*  562 */             if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  563 */               this.spV201VO.setSimcard1Operator(Functions.getASCIIFromByteArray(oper)); break;
/*  564 */             }  if ((short)Functions.getIntFromHexByte(fcon[0]) == 2) {
/*  565 */               this.spV201VO.setSimcard2Operator(Functions.getASCIIFromByteArray(oper));
/*      */             }
/*      */             break;
/*      */           
/*      */           case 7:
/*      */             break;
/*      */           
/*      */           case 8:
/*  573 */             this.currentSIM = (short)Functions.getIntFromHexByte(fcon[0]);
/*  574 */             this.spV201VO.setCurrentSim(this.currentSIM);
/*  575 */             this.spV201VO.setCurrentAPN((short)Functions.getIntFromHexByte(fcon[1]));
/*      */             break;
/*      */           
/*      */           case 9:
/*  579 */             firstPacket_RCVD_LCI = true;
/*  580 */             this.lastCommIface = (short)Functions.getIntFromHexByte(fcon[0]);
/*  581 */             this.spV201VO.setLast_Comm_Interface(this.lastCommIface);
/*  582 */             this.spV201VO.setInitialPacket(true);
/*      */             break;
/*      */           
/*      */           case 10:
/*      */             break;
/*      */           
/*      */           case 11:
/*  589 */             this.spV201VO.setModuleHWDtls(Functions.getIntFrom2ByteArray(fcon));
/*      */             break;
/*      */           
/*      */           case 12:
/*  593 */             sb = new StringBuilder();
/*  594 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1])).append('.').append(Functions.getIntFromHexByte(fcon[2]));
/*  595 */             this.spV201VO.setPegasus_Firmware_Version(sb.toString());
/*      */             break;
/*      */           
/*      */           case 13:
/*  599 */             this.spV201VO.setOperation_Mode((short)Functions.getIntFromHexByte(fcon[0]));
/*      */             break;
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
/*      */           case 14:
/*      */           case 15:
/*      */           case 16:
/*      */           case 17:
/*      */           case 18:
/*      */           case 19:
/*      */           case 20:
/*      */           case 21:
/*      */           case 22:
/*      */           case 23:
/*      */           case 24:
/*      */           case 25:
/*      */           case 26:
/*      */           case 27:
/*      */           case 28:
/*      */           case 29:
/*      */           case 30:
/*      */             break;
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
/*      */           case 31:
/*  654 */             if (flen != 8) {
/*  655 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("An_event_was_received/generated_with_incorrect_size"), Enums.EnumMessagePriority.HIGH, null, null);
/*  656 */               GlobalVariables.buzzerActivated = true;
/*      */             } 
/*      */             
/*  659 */             originalEvent = new StringBuilder();
/*  660 */             for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/*  661 */               originalEvent.append(Functions.convertContactIdDigitToHex((fcon[numByte] & 0xF0) / 16)).append(Functions.convertContactIdDigitToHex(fcon[numByte] & 0xF));
/*      */             }
/*  663 */             sEvent = originalEvent.toString();
/*  664 */             originalClientCodes.add(originalEvent.substring(0, 4));
/*  665 */             idGroup = ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idGroup;
/*      */             
/*  667 */             comPort = Functions.getReceiverCOMPortByGroupID(idGroup, "PEGASUS");
/*  668 */             this.spV201VO.setRcvrCOMPort(comPort);
/*  669 */             this.spV201VO.setRcvrGroup(idGroup);
/*  670 */             if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().containsKey(comPort)) {
/*  671 */               if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.ADICIONAR.getPartitionScheme()) {
/*  672 */                 int partition = Integer.parseInt(sEvent.substring(10, 12), 16);
/*  673 */                 partition = (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue() > 99) ? 99 : (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue());
/*  674 */                 sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { Integer.valueOf(partition) }) + sEvent.substring(12, 16);
/*  675 */               } else if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.SUBSTITUIR.getPartitionScheme()) {
/*  676 */                 sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme() }) + sEvent.substring(12, 16);
/*      */               } 
/*      */             }
/*  679 */             if (sEvent.equals(originalEvent.toString())) {
/*  680 */               byte checksum = 0;
/*  681 */               sEvent = sEvent.substring(0, 15).replaceAll("0", "A");
/*  682 */               for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/*  683 */                 checksum = (byte)(checksum + Integer.parseInt(sEvent.substring(numByte, numByte + 1), 16));
/*      */               }
/*  685 */               sEvent = sEvent.concat(Functions.convertInt2Hex((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/*  686 */               for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/*  687 */                 fcon[numByte] = (byte)Integer.parseInt(sEvent.substring(numByte * 2, numByte * 2 + 2), 16);
/*      */               }
/*      */             } 
/*  690 */             Functions.printEvent(Util.EnumProductIDs.PEGASUS.getProductId(), fcon, idGroup, ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idClient);
/*  691 */             System.arraycopy(fcon, 0, eventData, contactIdCounter, 8);
/*  692 */             contactIdCounter += 8;
/*  693 */             if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/*  694 */               ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */             }
/*      */             break;
/*      */           
/*      */           case 32:
/*      */             break;
/*      */           
/*      */           case 33:
/*  702 */             this.spV201VO.setWifiModel((short)Functions.getIntFromHexByte(fcon[0]));
/*      */             break;
/*      */           
/*      */           case 34:
/*  706 */             sb = new StringBuilder();
/*  707 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1]));
/*  708 */             this.spV201VO.setWifiFW(sb.toString());
/*      */             break;
/*      */           
/*      */           case 35:
/*  712 */             this.spV201VO.setCurrentWifiAccessPoint((short)Functions.getIntFromHexByte(fcon[0]));
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 36:
/*      */           case 37:
/*      */           case 38:
/*      */           case 39:
/*      */           case 40:
/*      */           case 41:
/*      */           case 42:
/*      */           case 43:
/*      */           case 64:
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 44:
/*  743 */             if ((short)Functions.getIntFromHexByte(fcon[0]) == 1) {
/*  744 */               this.spV201VO.setNtpUpdateRequired(true);
/*      */             }
/*      */             break;
/*      */           
/*      */           case 46:
/*      */             break;
/*      */           
/*      */           case 51:
/*  752 */             rawTimezone = new byte[2];
/*  753 */             rawTimezone[0] = fcon[0];
/*  754 */             rawTimezone[1] = fcon[1];
/*  755 */             this.timezone = Functions.getSignedIntFrom2ByteArray(Functions.swapLSB2MSB(rawTimezone));
/*      */             break;
/*      */         } 
/*      */       
/*      */       } 
/*  760 */       this.spV201VO.setOriginalClientCodes(originalClientCodes);
/*  761 */       this.spV201VO.setEventData(eventData);
/*  762 */       this.spV201VO.setAlarmPanelProtocol((short)1);
/*      */       
/*  764 */       if (this.initialMsgFlag && !firstPacket_RCVD_LCI) {
/*      */         try {
/*  766 */           this.clientSocket.close();
/*  767 */         } catch (IOException iOException) {}
/*      */ 
/*      */         
/*  770 */         return false;
/*      */       } 
/*      */       
/*  773 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.sn + LocaleMessage.getLocaleMessage("]_M2S_packet_received") + Functions.getIface(this.lastCommIface), Enums.EnumMessagePriority.LOW, null, null);
/*  774 */       this.spV201VO.setAuto_Registration_Enabled((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/*  775 */       this.spV201VO.setModule_Ip_Addr(this.remoteIP.substring(0, this.remoteIP.indexOf(":")));
/*      */       
/*      */       try {
/*  778 */         TblPegasusActiveConnections.semaphoreAlivePacketsReceived.acquire();
/*  779 */         this.spV201VO = PegasusDBManager.executeSP_V2_001(this.spV201VO);
/*      */       } finally {
/*  781 */         TblPegasusActiveConnections.semaphoreAlivePacketsReceived.release();
/*      */       } 
/*      */       
/*  784 */       if (this.spV201VO != null) {
/*  785 */         if (this.spV201VO.getAuto_Registration_Executed() == 1) {
/*  786 */           if (this.spV201VO.getRegistered() == 0) {
/*  787 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_of_the_module_with_ID") + this.sn, Enums.EnumMessagePriority.HIGH, null, null);
/*  788 */             SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/*  789 */             return false;
/*      */           } 
/*  791 */         } else if (this.spV201VO.getRegistered() == 1) {
/*  792 */           if (this.spV201VO.getEnabled() == 0) {
/*  793 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.sn + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  794 */             SocketFunctions.send(this.clientSocket, new byte[] { -31 });
/*  795 */             return false;
/*      */           } 
/*      */         } else {
/*  798 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.remoteIP + LocaleMessage.getLocaleMessage("]_Module_with_ID") + this.sn + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  799 */           SocketFunctions.send(this.clientSocket, new byte[] { -32 });
/*  800 */           return false;
/*      */         } 
/*      */         
/*  803 */         Functions.generateEventReceptionAlivePacket(1, this.spV201VO.getId_Client(), this.spV201VO.getId_Module(), this.spV201VO.getId_Group(), this.spV201VO.getClientCode(), this.spV201VO.getE_Alive_Received(), this.spV201VO.getF_Alive_Received(), this.spV201VO.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.TCP.name(), this.lastCommIface, -1);
/*  804 */         if (this.initialMsgFlag) {
/*  805 */           if (TblPegasusActiveConnections.getInstance().containsKey(this.sn)) {
/*  806 */             TblPegasusActiveConnections.removeConnection(this.sn);
/*      */           }
/*  808 */           TblPegasusActiveConnections.addConnection(this.sn, this.myThreadGuid);
/*  809 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idClient = this.spV201VO.getId_Client();
/*  810 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idModule = this.spV201VO.getId_Module();
/*      */         } 
/*  812 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idGroup = this.spV201VO.getId_Group();
/*  813 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).clientName = this.spV201VO.getName();
/*  814 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).communicationDebug = (this.spV201VO.getCommDebug() == 1);
/*      */         
/*  816 */         if (this.spV201VO.getCommLog() == 1 && this.spV201VO.getCommLogEnabledDate() != null) {
/*  817 */           Calendar sys = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/*  818 */           if (sys.get(5) - this.spV201VO.getCommLogEnabledDate().get(5) > 30) {
/*  819 */             PegasusDBManager.disableCommunicationLog(this.spV201VO.getId_Module());
/*  820 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).commLog = false;
/*  821 */             if (this.ownLogger != null) {
/*  822 */               this.ownLogger = null;
/*      */             }
/*      */           } else {
/*  825 */             ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).commLog = true;
/*  826 */             if (this.ownLogger == null && this.sn != null) {
/*  827 */               this.ownLogger = ZeusServerLogger.getDeviceLogger("Pegasus/", this.sn);
/*      */             }
/*  829 */             if (this.ownLogger != null) {
/*  830 */               Functions.logPegasusIncomingPacket(this.ownLogger, decData);
/*      */             }
/*      */           } 
/*      */         } else {
/*      */           
/*  835 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).commLog = false;
/*  836 */           if (this.ownLogger != null) {
/*  837 */             this.ownLogger = null;
/*      */           }
/*      */         } 
/*      */         
/*  841 */         ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).communicationTimeout = this.spV201VO.getComm_Timeout() * 1000;
/*  842 */         this.idleTimeout = System.currentTimeMillis() + (this.spV201VO.getComm_Timeout() * 1000);
/*  843 */         if (this.spV201VO.isNtpUpdateRequired()) {
/*  844 */           ((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).newCommand = true;
/*      */         }
/*      */         try {
/*  847 */           SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  848 */           return true;
/*  849 */         } catch (IOException ex) {
/*  850 */           return false;
/*      */         } 
/*      */       } 
/*      */       
/*  854 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Error_while_processing_the_M2S_packet"), Enums.EnumMessagePriority.AVERAGE, this.sn, null);
/*  855 */       SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  856 */       return false;
/*      */     
/*      */     }
/*  859 */     catch (Exception ex) {
/*      */       try {
/*  861 */         SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*  862 */       } catch (IOException iOException) {}
/*      */       
/*  864 */       return false;
/*      */     } finally {
/*  866 */       TblPegasusActiveConnections.numberOfPendingIdentificationPackets.decrementAndGet();
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean sendRemoteCommand(SP_024DataHolder sp24DH) {
/*      */     byte[] tmp, ascii;
/*      */     String[] cData, date, dData, hData;
/*  873 */     byte[] data = null;
/*      */ 
/*      */     
/*  876 */     switch (sp24DH.getCommand_Type()) {
/*      */       case 32769:
/*  878 */         tmp = Functions.get2ByteArrayFromInt(32769);
/*  879 */         tmp = Functions.swapLSB2MSB(tmp);
/*  880 */         data = new byte[5];
/*  881 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  882 */         data[2] = Byte.valueOf(Integer.toHexString(2), 16).byteValue();
/*  883 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/*  884 */         data = new byte[ascii.length + 3];
/*  885 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  886 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  887 */         System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */         break;
/*      */       case 32770:
/*  890 */         tmp = Functions.get2ByteArrayFromInt(32770);
/*  891 */         tmp = Functions.swapLSB2MSB(tmp);
/*  892 */         data = new byte[5];
/*  893 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  894 */         data[2] = Byte.valueOf(Integer.toHexString(2), 16).byteValue();
/*  895 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/*  896 */         data = new byte[ascii.length + 3];
/*  897 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  898 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  899 */         System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */         break;
/*      */       case 32771:
/*  902 */         tmp = Functions.get2ByteArrayFromInt(32771);
/*  903 */         tmp = Functions.swapLSB2MSB(tmp);
/*  904 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/*  905 */         data = new byte[ascii.length + 3];
/*  906 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  907 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  908 */         System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */         break;
/*      */       case 32772:
/*  911 */         data = new byte[3];
/*  912 */         tmp = Functions.get2ByteArrayFromInt(32772);
/*  913 */         tmp = Functions.swapLSB2MSB(tmp);
/*  914 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  915 */         data[2] = 0;
/*      */         break;
/*      */       case 32773:
/*  918 */         tmp = Functions.get2ByteArrayFromInt(32773);
/*  919 */         tmp = Functions.swapLSB2MSB(tmp);
/*  920 */         data = new byte[7];
/*  921 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  922 */         cData = sp24DH.getCommandData().split(";");
/*  923 */         data[2] = 4;
/*  924 */         data[3] = Byte.parseByte(cData[0]);
/*      */         
/*  926 */         data[4] = Byte.parseByte(cData[2]);
/*  927 */         tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[1]));
/*  928 */         data[5] = tmp[1];
/*  929 */         data[6] = tmp[0];
/*      */         break;
/*      */       case 32774:
/*  932 */         tmp = Functions.get2ByteArrayFromInt(32774);
/*  933 */         tmp = Functions.swapLSB2MSB(tmp);
/*  934 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/*  935 */         data = new byte[ascii.length + 3];
/*  936 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  937 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length), 16).byteValue();
/*  938 */         System.arraycopy(ascii, 0, data, 3, ascii.length);
/*      */         break;
/*      */       case 32775:
/*  941 */         tmp = Functions.get2ByteArrayFromInt(32775);
/*  942 */         tmp = Functions.swapLSB2MSB(tmp);
/*  943 */         data = new byte[7];
/*  944 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  945 */         data[2] = 4;
/*  946 */         cData = sp24DH.getCommandData().split(";");
/*  947 */         data[3] = Byte.parseByte(cData[0]);
/*  948 */         data[4] = (byte)(Byte.parseByte(cData[1]) - 1);
/*  949 */         tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(cData[2]));
/*  950 */         data[5] = tmp[1];
/*  951 */         data[6] = tmp[0];
/*      */         break;
/*      */       case 32776:
/*  954 */         data = new byte[3];
/*  955 */         data[2] = 0;
/*  956 */         tmp = Functions.get2ByteArrayFromInt(32776);
/*  957 */         tmp = Functions.swapLSB2MSB(tmp);
/*  958 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32777:
/*  961 */         data = new byte[5];
/*  962 */         data[2] = 2;
/*  963 */         cData = sp24DH.getCommandData().split(";");
/*  964 */         data[3] = Byte.parseByte(cData[0]);
/*  965 */         data[4] = Byte.parseByte(cData[1]);
/*  966 */         tmp = Functions.get2ByteArrayFromInt(32777);
/*  967 */         tmp = Functions.swapLSB2MSB(tmp);
/*  968 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32778:
/*  971 */         tmp = Functions.get2ByteArrayFromInt(32778);
/*  972 */         tmp = Functions.swapLSB2MSB(tmp);
/*  973 */         ascii = Functions.getASCII4mString(sp24DH.getCommandData());
/*  974 */         data = new byte[ascii.length + 4];
/*  975 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  976 */         data[2] = Byte.valueOf(Integer.toHexString(ascii.length + 1), 16).byteValue();
/*  977 */         data[3] = 0;
/*  978 */         System.arraycopy(ascii, 0, data, 4, ascii.length);
/*      */         break;
/*      */       case 32779:
/*  981 */         data = new byte[4];
/*  982 */         data[2] = 1;
/*  983 */         cData = sp24DH.getCommandData().split(";");
/*  984 */         data[3] = Byte.parseByte(cData[0]);
/*  985 */         tmp = Functions.get2ByteArrayFromInt(32779);
/*  986 */         tmp = Functions.swapLSB2MSB(tmp);
/*  987 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32780:
/*  990 */         data = new byte[3];
/*  991 */         tmp = Functions.get2ByteArrayFromInt(32780);
/*  992 */         tmp = Functions.swapLSB2MSB(tmp);
/*  993 */         System.arraycopy(tmp, 0, data, 0, 2);
/*  994 */         data[2] = 0;
/*      */         break;
/*      */       case 32781:
/*  997 */         cData = sp24DH.getCommandData().split(";");
/*  998 */         ascii = Functions.getASCII4mString(cData[2]);
/*  999 */         data = new byte[ascii.length + 5];
/* 1000 */         data[2] = (byte)(ascii.length + 2);
/* 1001 */         data[3] = Byte.parseByte(cData[0]);
/* 1002 */         data[4] = Byte.parseByte(cData[1]);
/* 1003 */         System.arraycopy(ascii, 0, data, 5, ascii.length);
/* 1004 */         tmp = Functions.get2ByteArrayFromInt(32781);
/* 1005 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1006 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         break;
/*      */       case 32782:
/* 1009 */         tmp = Functions.get2ByteArrayFromInt(32782);
/* 1010 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1011 */         cData = sp24DH.getCommandData().split(";");
/* 1012 */         if (cData.length == 1) {
/* 1013 */           if (cData[0].equals("1")) {
/* 1014 */             data = new byte[4];
/* 1015 */             System.arraycopy(tmp, 0, data, 0, 2);
/* 1016 */             data[2] = 1;
/* 1017 */             data[3] = 1; break;
/* 1018 */           }  if (cData[0].equals("2")) {
/* 1019 */             data = new byte[11];
/* 1020 */             System.arraycopy(tmp, 0, data, 0, 2);
/* 1021 */             this.df.setTimeZone(TimeZone.getTimeZone((String)Defines.TIMEZONE_NAMES.get(Integer.valueOf(this.timezone))));
/* 1022 */             String ddd = this.df.format(new Date());
/* 1023 */             data[2] = 8;
/* 1024 */             data[3] = 2;
/* 1025 */             data[4] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(0, 2))), 16).byteValue();
/* 1026 */             data[5] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(3, 5))), 16).byteValue();
/* 1027 */             tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(ddd.substring(6, 10)));
/* 1028 */             data[6] = tmp[0];
/* 1029 */             data[7] = tmp[1];
/* 1030 */             data[8] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(11, 13))), 16).byteValue();
/* 1031 */             data[9] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(14, 16))), 16).byteValue();
/* 1032 */             data[10] = Byte.valueOf(Integer.toHexString(Integer.parseInt(ddd.substring(17))), 16).byteValue();
/*      */           }  break;
/*      */         } 
/* 1035 */         data = new byte[11];
/* 1036 */         System.arraycopy(tmp, 0, data, 0, 2);
/*      */         
/* 1038 */         date = cData[1].split(" ");
/* 1039 */         dData = date[0].split("-");
/* 1040 */         hData = date[1].split(":");
/* 1041 */         data[2] = 8;
/* 1042 */         data[3] = 3;
/* 1043 */         data[4] = Byte.valueOf(dData[2]).byteValue();
/* 1044 */         data[5] = Byte.valueOf(dData[1]).byteValue();
/* 1045 */         tmp = Functions.get2ByteArrayFromInt(Integer.parseInt(dData[0]));
/* 1046 */         data[6] = tmp[0];
/* 1047 */         data[7] = tmp[1];
/* 1048 */         data[8] = Byte.valueOf(hData[0]).byteValue();
/* 1049 */         data[9] = Byte.valueOf(hData[1]).byteValue();
/* 1050 */         data[10] = Byte.valueOf(hData[2]).byteValue();
/*      */         break;
/*      */       
/*      */       case 32784:
/* 1054 */         data = new byte[4];
/* 1055 */         tmp = Functions.get2ByteArrayFromInt(32784);
/* 1056 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1057 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1058 */         data[2] = 1;
/* 1059 */         data[3] = Byte.parseByte(sp24DH.getCommandData());
/*      */         break;
/*      */       case 32785:
/* 1062 */         data = new byte[5];
/* 1063 */         tmp = Functions.get2ByteArrayFromInt(32785);
/* 1064 */         tmp = Functions.swapLSB2MSB(tmp);
/* 1065 */         System.arraycopy(tmp, 0, data, 0, 2);
/* 1066 */         data[2] = 2;
/* 1067 */         cData = sp24DH.getCommandData().split(";");
/* 1068 */         data[3] = Byte.parseByte(cData[0]);
/* 1069 */         data[4] = Byte.parseByte(cData[1]);
/*      */         break;
/*      */     } 
/*      */     
/*      */     try {
/* 1074 */       tmp = prepareCommandPacket(data);
/* 1075 */       SocketFunctions.send(this.clientSocket, tmp);
/* 1076 */       return true;
/* 1077 */     } catch (IOException|InvalidAlgorithmParameterException|InvalidKeyException|NoSuchAlgorithmException|BadPaddingException|IllegalBlockSizeException|NoSuchPaddingException ex) {
/* 1078 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Failure_while_sending_command"), Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1079 */       ex.printStackTrace();
/* 1080 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void registerFailureSendCommand(String msg, short exec_Retries, int id_Command) throws SQLException, InterruptedException {
/* 1085 */     if (msg != null && msg.length() > 0) {
/* 1086 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, msg, Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */     }
/* 1088 */     if (exec_Retries + 1 >= 3) {
/* 1089 */       PegasusDBManager.executeSP_025(id_Command, (short)(exec_Retries + 1));
/*      */     } else {
/* 1091 */       PegasusDBManager.executeSP_026(id_Command, (short)(exec_Retries + 1));
/*      */     } 
/*      */   }
/*      */   
/*      */   private void updateLastCommunicationModuleData() throws SQLException, InterruptedException {
/* 1096 */     PegasusDBManager.executeSP_028(((InfoModule)TblPegasusActiveConnections.getInstance().get(this.sn)).idModule, this.lastCommIface, this.currentSIM);
/*      */   }
/*      */   
/*      */   private void endCommand(int id_Module, short exec_Retries) throws SQLException, InterruptedException {
/* 1100 */     PegasusDBManager.executeSP_027(id_Module, (short)(exec_Retries + 1));
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendFile2Module(SP_024DataHolder sp24DH) throws IOException, InterruptedException, SQLException {
/* 1106 */     int blockIndex = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1111 */     short maxRetries = 3;
/* 1112 */     short retry = 0;
/*      */     
/* 1114 */     boolean commandProcessed = true;
/*      */     
/* 1116 */     File file = Functions.writeByteArrayToFile(this.sn + "_" + sp24DH.getCommandData(), sp24DH.getCommandFileData());
/* 1117 */     FileChannel fc = (new RandomAccessFile(file, "r")).getChannel();
/* 1118 */     fc.position(0L);
/* 1119 */     long flen = fc.size();
/* 1120 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */     
/* 1122 */     label81: while (isSocketConnected() && fc.position() < flen) {
/*      */       
/* 1124 */       for (retry = (short)(retry + 1); retry < maxRetries; ) {
/* 1125 */         int blockLength = (int)((flen - fc.position() > 240L) ? 240L : (flen - fc.position()));
/* 1126 */         ByteBuffer blockBuf = ByteBuffer.allocate(blockLength);
/* 1127 */         if (fc.read(blockBuf) == blockLength) {
/* 1128 */           byte[] block = blockBuf.array();
/* 1129 */           byte[] packet = new byte[blockLength + 5];
/* 1130 */           byte[] tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(blockIndex));
/* 1131 */           System.arraycopy(tmp, 0, packet, 0, 2);
/* 1132 */           packet[2] = (byte)Integer.parseInt(Integer.toHexString(blockLength), 16);
/* 1133 */           System.arraycopy(block, 0, packet, 3, blockLength);
/* 1134 */           int crcCalc = CRC16.calculate(packet, 0, blockLength + 3, 65535);
/* 1135 */           tmp = Functions.swapLSB2MSB(Functions.get2ByteArrayFromInt(crcCalc));
/* 1136 */           System.arraycopy(tmp, 0, packet, blockLength + 3, 2);
/*      */           try {
/* 1138 */             SocketFunctions.send(this.clientSocket, packet);
/* 1139 */             Thread.sleep(50L);
/*      */             try {
/* 1141 */               if (retry - 1 == 0) {
/* 1142 */                 this.clientSocket.setSoTimeout(120000);
/* 1143 */               } else if (retry - 1 == 1) {
/* 1144 */                 this.clientSocket.setSoTimeout(210000);
/* 1145 */               } else if (retry - 1 == 2) {
/* 1146 */                 this.clientSocket.setSoTimeout(300000);
/*      */               } 
/* 1148 */               if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1149 */                 updateLastCommunicationModuleData();
/* 1150 */                 nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */               } 
/* 1152 */               tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1153 */               if (tmp[0] == 6) {
/* 1154 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Progress_of_the_file_transmission") + sp24DH.getCommandData() + " (" + fc.position() + LocaleMessage.getLocaleMessage("bytes)"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1155 */                 blockIndex++; break;
/*      */               } 
/* 1157 */               if (tmp[0] == 21) {
/* 1158 */                 if (blockIndex > 0) {
/* 1159 */                   blockIndex--;
/*      */                 }
/*      */                 
/* 1162 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Invalid_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sp24DH.getCommandData() + " :" + tmp[0], Enums.EnumMessagePriority.HIGH, this.sn, null);
/* 1163 */                 fc.position(fc.position() - blockLength); break;
/*      */               } 
/* 1165 */               if ((tmp[0] & 0xFF) == 4) {
/* 1166 */                 commandProcessed = false;
/* 1167 */                 registerFailureSendCommand("COMMAND Request Received in between file processing " + sp24DH.getCommandData(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */                 break label81;
/*      */               } 
/* 1170 */             } catch (SocketException ex) {
/*      */ 
/*      */               
/* 1173 */               if (blockIndex > 0) {
/* 1174 */                 blockIndex--;
/*      */               }
/* 1176 */               fc.position(fc.position() - blockLength);
/* 1177 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_to_the_transmission_of_a_data_block_of_the_file") + sp24DH.getCommandData(), Enums.EnumMessagePriority.HIGH, this.sn, null);
/*      */             } 
/* 1179 */           } catch (IOException|InterruptedException|SQLException ex) {
/*      */             
/* 1181 */             registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_a_data_block_of_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("to_the_module"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             
/*      */             break label81;
/*      */           } 
/* 1185 */           if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1186 */             updateLastCommunicationModuleData();
/* 1187 */             nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */           } 
/* 1189 */           Thread.sleep(100L); continue;
/*      */         } 
/* 1191 */         registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_reading_data_of_the_file") + sp24DH.getCommandData(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         
/*      */         break label81;
/*      */       } 
/*      */     } 
/* 1196 */     fc.close();
/*      */     
/*      */     try {
/* 1199 */       if (commandProcessed) {
/* 1200 */         Thread.sleep(100L);
/* 1201 */         byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 1202 */         if (tmp[0] == 6) {
/* 1203 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("The_file_[") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("]_was_sent_successfully_to_the_module"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1204 */           endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries());
/* 1205 */         } else if (tmp[0] == 22) {
/* 1206 */           registerFailureSendCommand(LocaleMessage.getLocaleMessage("The_module_informed_that_CRC-32_is_not_matching_for_the_file_[") + sp24DH.getCommandData() + "]", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         } else {
/* 1208 */           registerFailureSendCommand(LocaleMessage.getLocaleMessage("Failure_while_sending_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("(response") + tmp[0] + ")", sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */         } 
/*      */       } 
/* 1211 */     } catch (IOException|InterruptedException|SQLException ex) {
/* 1212 */       registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_expecting_response_of_the_module_indicating_that_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("was_sent_successfully"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */     } finally {
/* 1214 */       if (file != null && file.exists()) {
/* 1215 */         file.delete();
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   private void receiveFile4MModule(SP_024DataHolder sp24DH) throws SQLException, InterruptedException {
/* 1221 */     int flen = 0;
/*      */     
/* 1223 */     int fileContentIndex = 0;
/* 1224 */     int expBlockIndex = 0;
/*      */     
/* 1226 */     int recvCfgCRC32 = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1231 */     byte[] bid = new byte[2];
/* 1232 */     byte[] fileContent = null;
/* 1233 */     File file = null;
/* 1234 */     FileChannel fc = null;
/* 1235 */     short maxRetries = 3;
/* 1236 */     short retry = 0;
/* 1237 */     long nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */ 
/*      */     
/*      */     try { label73: while (true)
/* 1241 */       { if (isSocketConnected()) {
/*      */           
/* 1243 */           for (retry = (short)(retry + 1); retry < maxRetries; ) {
/*      */             try {
/* 1245 */               byte[] tmp = SocketFunctions.receive(this.clientSocket, 0, 3);
/* 1246 */               int blockLength = tmp[2] & 0xFF;
/* 1247 */               System.arraycopy(tmp, 0, bid, 0, 2);
/* 1248 */               int rcvBlockIndex = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(bid));
/* 1249 */               if (expBlockIndex == rcvBlockIndex) {
/* 1250 */                 byte[] block = SocketFunctions.receive(this.clientSocket, 0, blockLength + 2);
/* 1251 */                 byte[] packet = new byte[blockLength + 3];
/* 1252 */                 System.arraycopy(tmp, 0, packet, 0, 3);
/* 1253 */                 System.arraycopy(block, 0, packet, 3, blockLength);
/* 1254 */                 int crcCalc = CRC16.calculate(packet, 0, packet.length, 65535);
/* 1255 */                 System.arraycopy(block, blockLength, tmp, 0, 2);
/* 1256 */                 int crcRecv = Functions.getIntFrom2ByteArray(Functions.swapLSB2MSB(tmp));
/* 1257 */                 if (crcCalc == crcRecv) {
/* 1258 */                   if (expBlockIndex == 0) {
/* 1259 */                     tmp = new byte[4];
/* 1260 */                     System.arraycopy(block, 0, tmp, 0, 4);
/* 1261 */                     flen = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp));
/* 1262 */                     System.arraycopy(block, 4, tmp, 0, 4);
/* 1263 */                     recvCfgCRC32 = Functions.getIntFrom4ByteArray(Functions.swapLSB2MSB4ByteArray(tmp));
/* 1264 */                     fileContent = new byte[flen + 8];
/* 1265 */                     file = new File(this.sn + "_" + sp24DH.getCommandData());
/* 1266 */                     if (file.exists()) {
/* 1267 */                       file.delete();
/*      */                     }
/* 1269 */                     fc = (new RandomAccessFile(file, "rw")).getChannel();
/* 1270 */                     fc.position(0L);
/*      */                   } 
/* 1272 */                   ByteBuffer blockBuf = ByteBuffer.wrap(block, 0, blockLength);
/* 1273 */                   fc.write(blockBuf);
/* 1274 */                   System.arraycopy(block, 0, fileContent, fileContentIndex, blockLength);
/* 1275 */                   fileContentIndex += blockLength;
/* 1276 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1277 */                   if (fileContentIndex >= flen + 8 && flen > 0) {
/*      */                     break;
/*      */                   }
/* 1280 */                   expBlockIndex++;
/*      */                 } else {
/* 1282 */                   SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */                 } 
/*      */               } else {
/*      */                 
/* 1286 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               } 
/* 1288 */             } catch (SocketException ex) {
/*      */               
/* 1290 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_receiving_a_data_block_of_the_file") + sp24DH.getCommandData(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */               
/*      */               break label73;
/*      */             } 
/* 1294 */             if (nextUpdateFieldLastCommunication < System.currentTimeMillis()) {
/* 1295 */               updateLastCommunicationModuleData();
/* 1296 */               nextUpdateFieldLastCommunication = System.currentTimeMillis() + 60000L;
/*      */             } 
/*      */           } 
/* 1299 */           if (fileContentIndex >= flen + 8 && flen > 0) {
/*      */ 
/*      */             
/*      */             try {
/* 1303 */               byte[] tmp = new byte[flen];
/* 1304 */               System.arraycopy(fileContent, 8, tmp, 0, flen);
/* 1305 */               int calcCfgCrc32 = CRC32.calculateCRC32(tmp, flen, -1);
/* 1306 */               if (calcCfgCrc32 == recvCfgCRC32) {
/* 1307 */                 PegasusDBManager.executeSP_065(sp24DH.getId_Command(), new FileInputStream(file));
/* 1308 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 1309 */                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("The_file_[") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("]_was_read_successfully"), Enums.EnumMessagePriority.LOW, this.sn, null);
/* 1310 */                 endCommand(sp24DH.getId_Command(), sp24DH.getExec_Retries()); break;
/*      */               } 
/* 1312 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("CRC_32_of_the_file") + sp24DH.getCommandData() + LocaleMessage.getLocaleMessage("invalid"), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/* 1313 */               SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*      */               break;
/* 1315 */             } catch (IOException|InterruptedException|SQLException ex) {
/* 1316 */               registerFailureSendCommand(LocaleMessage.getLocaleMessage("Timeout_while_finalizing_upload_of_the_file") + sp24DH.getCommandData(), sp24DH.getExec_Retries(), sp24DH.getId_Command());
/*      */             } finally {
/*      */               Exception exception;
/*      */             } 
/*      */           } else {
/*      */             continue;
/*      */           } 
/*      */         } else {
/*      */           break;
/*      */         } 
/*      */         
/* 1327 */         try { if (fc != null) {
/* 1328 */             fc.close();
/*      */           }
/* 1330 */           if (file != null) {
/* 1331 */             file.delete();
/*      */           } }
/* 1333 */         catch (IOException ex)
/* 1334 */         { Logger.getLogger(PegasusV2Handler.class.getName()).log(Level.SEVERE, (String)null, ex); }  }  } catch (IOException|InterruptedException|SQLException ex) { ex.printStackTrace(); registerFailureSendCommand(LocaleMessage.getLocaleMessage("Exception_while_uploading_the_file") + sp24DH.getCommandData() + ex.getMessage(), sp24DH.getExec_Retries(), sp24DH.getId_Command()); } finally { try { if (fc != null) fc.close();  if (file != null) file.delete();  } catch (IOException ex) { Logger.getLogger(PegasusV2Handler.class.getName()).log(Level.SEVERE, (String)null, ex); }
/*      */        }
/*      */   
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\PegasusV2Handler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */