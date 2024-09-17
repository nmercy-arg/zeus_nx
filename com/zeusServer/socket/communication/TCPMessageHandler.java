/*     */ package com.zeusServer.socket.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBGeneral.DBCleaner;
/*     */ import com.zeusServer.DBGeneral.DBUpdater;
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*     */ import com.zeusServer.DBPools.GriffonPool;
/*     */ import com.zeusServer.DBPools.MercuriusPool;
/*     */ import com.zeusServer.DBPools.PegasusPool;
/*     */ import com.zeusServer.DBPools.SysPool;
/*     */ import com.zeusServer.DBPools.ZeusSettingsPool;
/*     */ import com.zeusServer.box.ZeusBoxHandler;
/*     */ import com.zeusServer.tblConnections.TblActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblActiveUserLogins;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblMercuriusAVLActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.InfoModule;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.LoggedInUser;
/*     */ import com.zeusServer.util.Main;
/*     */ import com.zeusServer.util.Rijndael;
/*     */ import com.zeusServer.util.SocketFunctions;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Timestamp;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Map;
/*     */ import jcifs.smb.NtlmPasswordAuthentication;
/*     */ import jcifs.smb.SmbFile;
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
/*     */ public class TCPMessageHandler
/*     */   implements Runnable
/*     */ {
/*     */   private Socket clientSocket;
/*  70 */   private final long MESSAGE_CONNECTION_IDLE_TIMEOUT = 30000L;
/*  71 */   private final int SOCKET_TIMEOUT = 30000;
/*     */   private String remoteIP;
/*     */   private String ip;
/*     */   boolean isForGriffonMobile = false;
/*     */   boolean isForPegasusMobile = false;
/*     */   
/*     */   public TCPMessageHandler(Socket messageChannel) throws SocketException, IOException {
/*  78 */     this.clientSocket = messageChannel;
/*  79 */     this.clientSocket.setSoTimeout(30000);
/*  80 */     this.clientSocket.setTcpNoDelay(true);
/*  81 */     this.remoteIP = this.clientSocket.getRemoteSocketAddress().toString();
/*  82 */     this.remoteIP = this.remoteIP.substring(1);
/*  83 */     this.ip = this.remoteIP.substring(0, this.remoteIP.indexOf(":"));
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  88 */     long idleTimeout = System.currentTimeMillis() + 30000L;
/*     */ 
/*     */     
/*  91 */     boolean keepRunning = true;
/*     */     
/*  93 */     int readCount = 0;
/*     */     
/*     */     try {
/*  96 */       while (keepRunning && this.clientSocket.isConnected() && idleTimeout > System.currentTimeMillis()) {
/*  97 */         if (this.clientSocket.getInputStream().available() >= 3) {
/*  98 */           byte[] tmpBuffer = SocketFunctions.receive(this.clientSocket, 0, 3);
/*  99 */           if (tmpBuffer != null) {
/* 100 */             readCount = tmpBuffer.length;
/*     */           }
/* 102 */           if (readCount >= 3) {
/* 103 */             if (readCount == 3) {
/* 104 */               int rcvdCommand = tmpBuffer[0] & 0xFF;
/* 105 */               if ((tmpBuffer[0] & 0xFF) == 128 || (tmpBuffer[0] & 0xFF) == 135) {
/* 106 */                 int messageLen = tmpBuffer[2] & 0xFF;
/* 107 */                 messageLen = messageLen * 256 + (tmpBuffer[1] & 0xFF);
/* 108 */                 byte[] bufferRx = new byte[messageLen + 5];
/* 109 */                 System.arraycopy(tmpBuffer, 0, bufferRx, 0, 3);
/* 110 */                 tmpBuffer = SocketFunctions.receive(this.clientSocket, 0, messageLen + 2);
/*     */                 
/* 112 */                 if (tmpBuffer != null) {
/* 113 */                   readCount = tmpBuffer.length;
/*     */                 }
/* 115 */                 if (readCount == messageLen + 2) {
/* 116 */                   System.arraycopy(tmpBuffer, 0, bufferRx, 3, messageLen + 2);
/* 117 */                   int crcReceived = bufferRx[messageLen + 4] & 0xFF;
/* 118 */                   crcReceived = crcReceived * 256 + (bufferRx[messageLen + 3] & 0xFF);
/* 119 */                   int crcCalc = CRC16.calculate(bufferRx, 0, messageLen + 3, 65535);
/* 120 */                   if (crcReceived == crcCalc) {
/* 121 */                     SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 122 */                     byte[] cryptedBuffer = new byte[messageLen];
/* 123 */                     System.arraycopy(bufferRx, 3, cryptedBuffer, 0, messageLen);
/* 124 */                     byte[] decryptedBuffer = Rijndael.decryptBytes(cryptedBuffer, Rijndael.msgKeyBytes, true);
/* 125 */                     String st = new String(decryptedBuffer);
/* 126 */                     int idClient = Integer.parseInt(st.substring(0, st.indexOf(";")));
/* 127 */                     int productId = Integer.parseInt(st.substring(st.indexOf(";") + 1));
/* 128 */                     switch (Util.EnumProductIDs.getProductID(productId)) {
/*     */                       case PEGASUS:
/* 130 */                         synchronized (TblPegasusActiveConnections.getInstance()) {
/* 131 */                           for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblPegasusActiveConnections.getInstance().entrySet()) {
/* 132 */                             if (((InfoModule)connection.getValue()).idClient == idClient) {
/* 133 */                               if (rcvdCommand == 128) {
/* 134 */                                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Client_[") + this.remoteIP + LocaleMessage.getLocaleMessage("]_Registered_a_new_command_for_client_ID_[") + idClient + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 135 */                                 ((InfoModule)connection.getValue()).newCommand = true; break;
/* 136 */                               }  if (rcvdCommand == 135) {
/* 137 */                                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Client_[") + this.remoteIP + LocaleMessage.getLocaleMessage("]_Received_delete_device_command_for_client_ID_[") + idClient + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 138 */                                 ((InfoModule)connection.getValue()).fecharConexao = true;
/* 139 */                                 synchronized (TblActiveUdpConnections.getInstance()) {
/* 140 */                                   for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblActiveUdpConnections.getInstance().entrySet()) {
/* 141 */                                     if (((UdpV2Handler)handler.getValue()).sn != null && ((UdpV2Handler)handler.getValue()).sn.equalsIgnoreCase(((InfoModule)connection.getValue()).sn)) {
/* 142 */                                       ((UdpV2Handler)handler.getValue()).dispose();
/*     */                                     }
/*     */                                   } 
/*     */                                 } 
/*     */                               } 
/*     */                               break;
/*     */                             } 
/*     */                           } 
/*     */                         } 
/*     */                         break;
/*     */                       
/*     */                       case GRIFFON_V1:
/*     */                       case GRIFFON_V2:
/* 155 */                         synchronized (TblGriffonActiveConnections.getInstance()) {
/* 156 */                           for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblGriffonActiveConnections.getInstance().entrySet()) {
/* 157 */                             if (((InfoModule)connection.getValue()).idClient == idClient) {
/* 158 */                               if (rcvdCommand == 128) {
/* 159 */                                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Client_[") + this.remoteIP + LocaleMessage.getLocaleMessage("]_Registered_a_new_command_for_client_ID_[") + idClient + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 160 */                                 ((InfoModule)connection.getValue()).newCommand = true; break;
/* 161 */                               }  if (rcvdCommand == 135) {
/* 162 */                                 Functions.printMessage(Util.EnumProductIDs.GRIFFON_V1, LocaleMessage.getLocaleMessage("Client_[") + this.remoteIP + LocaleMessage.getLocaleMessage("]_Received_delete_device_command_for_client_ID_[") + idClient + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 163 */                                 ((InfoModule)connection.getValue()).fecharConexao = true;
/* 164 */                                 synchronized (TblGriffonActiveUdpConnections.getInstance()) {
/* 165 */                                   for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblGriffonActiveUdpConnections.getInstance().entrySet()) {
/* 166 */                                     if (((UdpV2Handler)handler.getValue()).sn != null && ((UdpV2Handler)handler.getValue()).sn.equalsIgnoreCase(((InfoModule)connection.getValue()).sn)) {
/* 167 */                                       ((UdpV2Handler)handler.getValue()).dispose();
/*     */                                     }
/*     */                                   } 
/*     */                                 } 
/*     */                               } 
/*     */                               break;
/*     */                             } 
/*     */                           } 
/*     */                         } 
/*     */                         break;
/*     */                       
/*     */                       case MERCURIUS:
/* 179 */                         synchronized (TblMercuriusActiveConnections.getInstance()) {
/* 180 */                           for (Map.Entry<String, InfoModule> connection : (Iterable<Map.Entry<String, InfoModule>>)TblMercuriusActiveConnections.getInstance().entrySet()) {
/* 181 */                             if (((InfoModule)connection.getValue()).idClient == idClient) {
/* 182 */                               if (rcvdCommand == 128) {
/* 183 */                                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Client_[") + this.remoteIP + LocaleMessage.getLocaleMessage("]_Registered_a_new_command_for_client_ID_[") + idClient + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 184 */                                 ((InfoModule)connection.getValue()).newCommand = true; break;
/* 185 */                               }  if (rcvdCommand == 135) {
/* 186 */                                 Functions.printMessage(Util.EnumProductIDs.MERCURIUS, LocaleMessage.getLocaleMessage("Client_[") + this.remoteIP + LocaleMessage.getLocaleMessage("]_Received_delete_device_command_for_client_ID_[") + idClient + "]", Enums.EnumMessagePriority.LOW, null, null);
/* 187 */                                 ((InfoModule)connection.getValue()).fecharConexao = true;
/* 188 */                                 synchronized (TblMercuriusAVLActiveUdpConnections.getInstance()) {
/* 189 */                                   for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblMercuriusAVLActiveUdpConnections.getInstance().entrySet()) {
/* 190 */                                     if (((UdpV2Handler)handler.getValue()).sn != null && ((UdpV2Handler)handler.getValue()).sn.equalsIgnoreCase(((InfoModule)connection.getValue()).sn)) {
/* 191 */                                       ((UdpV2Handler)handler.getValue()).dispose();
/*     */                                     }
/*     */                                   } 
/*     */                                 } 
/*     */                               } 
/*     */                               break;
/*     */                             } 
/*     */                           } 
/*     */                         } 
/*     */                         break;
/*     */                     } 
/*     */                     break;
/*     */                   } 
/*     */                 } 
/*     */               } else {
/* 206 */                 if ((tmpBuffer[0] & 0xFF) == 152) {
/* 207 */                   if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.NORMAL) {
/* 208 */                     SocketFunctions.send(this.clientSocket, new byte[] { 21, (byte)GlobalVariables.dbCurrentStatus.getDB_CURRENT_STATUS() });
/*     */                     break;
/*     */                   } 
/* 211 */                   SocketFunctions.send(this.clientSocket, new byte[] { 6, (byte)GlobalVariables.dbCurrentStatus.getDB_CURRENT_STATUS() });
/*     */                   
/*     */                   break;
/*     */                 } 
/* 215 */                 if ((tmpBuffer[0] & 0xFF) == 130) {
/*     */                   
/* 217 */                   int packLen = tmpBuffer[1] & 0xFF;
/* 218 */                   byte[] data = SocketFunctions.receive(this.clientSocket, 0, packLen + 1);
/* 219 */                   byte[] loginPacket = new byte[packLen + 4];
/* 220 */                   loginPacket[0] = tmpBuffer[0];
/* 221 */                   loginPacket[1] = tmpBuffer[1];
/* 222 */                   loginPacket[2] = tmpBuffer[2];
/* 223 */                   System.arraycopy(data, 0, loginPacket, 3, packLen + 1);
/* 224 */                   byte[] crcbits = new byte[2];
/* 225 */                   crcbits[0] = data[packLen];
/* 226 */                   crcbits[1] = data[packLen - 1];
/* 227 */                   int crcRecv = Functions.getIntFrom2ByteArray(crcbits);
/* 228 */                   int crcCalc = CRC16.calculate(loginPacket, 0, packLen + 2, 65535);
/* 229 */                   if (crcCalc == crcRecv) {
/* 230 */                     byte response, lData[] = new byte[packLen];
/* 231 */                     System.arraycopy(loginPacket, 2, lData, 0, packLen);
/* 232 */                     lData = Rijndael.decryptBytes(lData, Rijndael.msgKeyBytes, true);
/* 233 */                     String userName = null;
/* 234 */                     String password = null;
/* 235 */                     int udpPort = 0;
/* 236 */                     int uLen = 0;
/*     */                     
/* 238 */                     StringBuilder sb = new StringBuilder();
/* 239 */                     int i = 0;
/* 240 */                     if ((lData[i++] & 0xFF) == 85) {
/* 241 */                       uLen = lData[i++] & 0xFF;
/* 242 */                       for (; i < uLen + 2; i++) {
/* 243 */                         sb.append((char)(lData[i] & 0xFF));
/*     */                       }
/* 245 */                       userName = sb.toString();
/*     */                     } 
/* 247 */                     if ((lData[i++] & 0xFF) == 80) {
/* 248 */                       sb = new StringBuilder();
/* 249 */                       int pLen = lData[i++] & 0xFF;
/* 250 */                       for (; i < uLen + 4 + pLen; i++) {
/* 251 */                         sb.append((char)(lData[i] & 0xFF));
/*     */                       }
/* 253 */                       password = sb.toString();
/*     */                     } 
/* 255 */                     if ((lData[i++] & 0xFF) == 67) {
/* 256 */                       int pLen = lData[i++] & 0xFF;
/* 257 */                       byte[] udpP = new byte[pLen];
/* 258 */                       udpP[0] = lData[i++];
/* 259 */                       udpP[1] = lData[i++];
/* 260 */                       udpPort = Functions.getIntFrom2ByteArray(udpP);
/*     */                     } 
/*     */                     
/* 263 */                     LoggedInUser user = ZeusSettingsDBManager.executeSP_S004(userName, Rijndael.encryptString(password, Rijndael.dbKeyBytes).trim());
/* 264 */                     if (user != null) {
/* 265 */                       user.setClientType((user.getClientType() == 2) ? 31 : ((user.getClientType() == 1) ? 15 : 0));
/* 266 */                       if (user.getClientType() == 31) {
/* 267 */                         user.setAssignedProducts(Integer.toString(Util.EnumProductIDs.PEGASUS.getProductId()) + "," + 
/* 268 */                             Integer.toString(Util.EnumProductIDs.GRIFFON_V1.getProductId()) + "," + 
/* 269 */                             Integer.toString(Util.EnumProductIDs.MERCURIUS.getProductId()));
/*     */                       }
/*     */                     } 
/* 272 */                     String schemaName = "";
/* 273 */                     if (user == null) {
/* 274 */                       for (String tmpSchemaName : Util.getAvailbleProductSchemas()) {
/* 275 */                         user = GenericDBManager.executeSP_055(userName, Rijndael.encryptString(password, Rijndael.dbKeyBytes).trim(), tmpSchemaName);
/* 276 */                         if (user != null) {
/* 277 */                           user.setAssignedProducts(Integer.toString(GenericDBManager.getProductIdBySchemaName(tmpSchemaName)));
/* 278 */                           schemaName = tmpSchemaName;
/*     */                           
/*     */                           break;
/*     */                         } 
/*     */                       } 
/*     */                     }
/* 284 */                     if (user != null) {
/* 285 */                       response = 6;
/* 286 */                       if (user.getClientType() == 7) {
/* 287 */                         if (user.getPermissions() == null) {
/* 288 */                           response = 23;
/*     */                         } else {
/*     */                           int pm;
/* 291 */                           switch (schemaName) {
/*     */                             case "PEGASUS":
/* 293 */                               pm = Integer.parseInt(user.getPermissions());
/* 294 */                               if ((pm & 0x100) != 256) {
/* 295 */                                 response = 23;
/*     */                               }
/*     */                               break;
/*     */                             case "GRIFFON":
/* 299 */                               pm = Integer.parseInt(user.getPermissions().split(",")[0]);
/* 300 */                               if ((pm & 0x10) != 16) {
/* 301 */                                 response = 23;
/*     */                               }
/*     */                               break;
/*     */                             case "MERCURIUS":
/* 305 */                               pm = Integer.parseInt(user.getPermissions().split(",")[0]);
/* 306 */                               if ((pm & 0x10) != 16) {
/* 307 */                                 response = 23;
/*     */                               }
/*     */                               break;
/*     */                           } 
/*     */                         } 
/* 312 */                       } else if (user.getClientType() == 1 || user.getClientType() == 3) {
/* 313 */                         response = 32;
/*     */                       } 
/*     */                     } else {
/* 316 */                       response = 21;
/* 317 */                       user = new LoggedInUser();
/*     */                     } 
/* 319 */                     if (response == 6) {
/*     */                       
/* 321 */                       user.setRemoteIp(this.remoteIP.substring(0, this.remoteIP.indexOf(":")));
/* 322 */                       user.setRemoteUdpPort(udpPort);
/* 323 */                       TblActiveUserLogins.addUIUser(user);
/*     */                     } 
/* 325 */                     byte[] loginResponse = prepareLoginResponse(user, response);
/*     */                     
/* 327 */                     SocketFunctions.send(this.clientSocket, loginResponse);
/*     */                     break;
/*     */                   } 
/* 330 */                   SocketFunctions.send(this.clientSocket, new byte[] { 24 });
/*     */                 
/*     */                 }
/* 333 */                 else if ((tmpBuffer[0] & 0xFF) == 132) {
/*     */                   
/* 335 */                   int idx = 0;
/* 336 */                   int packLen = tmpBuffer[1] & 0xFF;
/* 337 */                   byte[] data = SocketFunctions.receive(this.clientSocket, 0, packLen - 1);
/* 338 */                   if ((tmpBuffer[2] & 0xFF) == 67) {
/* 339 */                     int pLen = data[idx++] & 0xFF;
/* 340 */                     byte[] udpP = new byte[pLen];
/* 341 */                     udpP[0] = data[idx++];
/* 342 */                     udpP[1] = data[idx++];
/* 343 */                     int udpPort = Functions.getIntFrom2ByteArray(udpP);
/* 344 */                     String remoteUrl = this.remoteIP.substring(0, this.remoteIP.indexOf(":") + 1) + udpPort;
/* 345 */                     TblActiveUserLogins.removeConnection(remoteUrl);
/* 346 */                     SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*     */                     break;
/*     */                   } 
/*     */                 } else {
/* 350 */                   if ((tmpBuffer[0] & 0xFF) == 131) {
/*     */                     
/* 352 */                     byte[] udpP = new byte[2];
/* 353 */                     udpP[0] = tmpBuffer[1];
/* 354 */                     udpP[1] = tmpBuffer[2];
/* 355 */                     int udpPort = Functions.getIntFrom2ByteArray(udpP);
/* 356 */                     String remoteUrl = this.remoteIP.substring(0, this.remoteIP.indexOf(":") + 1) + udpPort;
/* 357 */                     TblActiveUserLogins.updateAlivePacketReceived(remoteUrl);
/*     */                     break;
/*     */                   } 
/* 360 */                   if ((tmpBuffer[0] & 0xFF) == 134) {
/*     */                     
/* 362 */                     if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.BACKUP || Functions.isDbInCleanupState()) {
/* 363 */                       SocketFunctions.send(this.clientSocket, new byte[] { (byte)GlobalVariables.dbCurrentStatus.getDB_CURRENT_STATUS() });
/* 364 */                       Thread.sleep(500L);
/*     */                       break;
/*     */                     } 
/* 367 */                     if (Functions.isDbInCleanupState() && GlobalVariables.cleanupThread != null && GlobalVariables.dbCleaner != null) {
/* 368 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Stopping_cleanup_task_to_process_restore"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 369 */                       GlobalVariables.dbCleaner.stopCleaner();
/* 370 */                       Thread.sleep(500L);
/*     */                     } 
/* 372 */                     GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.RESTORE;
/* 373 */                     int messageLen = tmpBuffer[2] & 0xFF;
/* 374 */                     messageLen = messageLen * 256 + (tmpBuffer[1] & 0xFF);
/* 375 */                     byte[] bufferRx = new byte[messageLen + 5];
/* 376 */                     System.arraycopy(tmpBuffer, 0, bufferRx, 0, 3);
/* 377 */                     tmpBuffer = SocketFunctions.receive(this.clientSocket, 0, messageLen + 2);
/* 378 */                     if (tmpBuffer != null) {
/* 379 */                       readCount = tmpBuffer.length;
/*     */                     }
/* 381 */                     if (readCount == messageLen + 2) {
/* 382 */                       System.arraycopy(tmpBuffer, 0, bufferRx, 3, messageLen + 2);
/* 383 */                       int crcReceived = bufferRx[messageLen + 4] & 0xFF;
/* 384 */                       crcReceived = crcReceived * 256 + (bufferRx[messageLen + 3] & 0xFF);
/* 385 */                       int crcCalc = CRC16.calculate(bufferRx, 0, messageLen + 3, 65535);
/* 386 */                       if (crcReceived == crcCalc) {
/* 387 */                         byte[] cryptedBuffer = new byte[messageLen];
/* 388 */                         System.arraycopy(bufferRx, 3, cryptedBuffer, 0, messageLen);
/* 389 */                         byte[] decryptedBuffer = Rijndael.decryptBytes(cryptedBuffer, Rijndael.msgKeyBytes, true);
/* 390 */                         String recvPath = getPath(decryptedBuffer);
/* 391 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("The_Zeus_Server_received_a_Database_Restore_command"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 392 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, "Path: " + recvPath, Enums.EnumMessagePriority.AVERAGE, null, null);
/* 393 */                         Connection conn = null;
/*     */                         
/*     */                         try {
/* 396 */                           GlobalVariables.mainTimerStopped = true;
/* 397 */                           Main.finishTasks(false);
/*     */ 
/*     */ 
/*     */                           
/*     */                           try {
/* 402 */                             conn = DriverManager.getConnection("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/" + ZeusServerCfg.getInstance().getDbFile() + ";shutdown=true");
/* 403 */                           } catch (SQLException sQLException) {
/*     */                           
/*     */                           } finally {
/* 406 */                             if (conn != null) {
/* 407 */                               conn.close();
/*     */                             }
/*     */                           } 
/*     */ 
/*     */                           
/* 412 */                           GriffonPool.closeConnectionPool();
/* 413 */                           MercuriusPool.closeConnectionPool();
/* 414 */                           PegasusPool.closeConnectionPool();
/* 415 */                           SysPool.closeConnectionPool();
/* 416 */                           ZeusSettingsPool.closeConnectionPool();
/*     */ 
/*     */                           
/* 419 */                           if (GlobalVariables.currentPlatform == Enums.Platform.ARM || GlobalVariables.currentPlatform == Enums.Platform.LINUX) {
/* 420 */                             Functions.copyFolder(new File(recvPath), new File(System.getProperty("user.dir") + File.separator + "SYS"));
/*     */                           }
/*     */                           
/*     */                           try {
/* 424 */                             int retriesRestoreDb = 0;
/* 425 */                             if (GlobalVariables.currentPlatform != Enums.Platform.ARM && GlobalVariables.currentPlatform != Enums.Platform.LINUX) {
/*     */                               while (true) {
/*     */                                 
/*     */                                 try {
/* 429 */                                   conn = DriverManager.getConnection("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/" + ZeusServerCfg.getInstance().getDbFile() + ";restoreFrom=" + recvPath);
/* 430 */                                   conn.commit();
/*     */                                   break;
/* 432 */                                 } catch (SQLException e) {
/* 433 */                                   if (++retriesRestoreDb >= 3) {
/* 434 */                                     throw e;
/*     */                                   }
/* 436 */                                   Thread.sleep(5000L);
/*     */                                 } finally {
/* 438 */                                   if (conn != null) {
/* 439 */                                     conn.close();
/*     */                                   }
/*     */                                 } 
/*     */                               } 
/*     */                             }
/*     */ 
/*     */                             
/* 446 */                             conn = DriverManager.getConnection("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/" + ZeusServerCfg.getInstance().getDbFile());
/* 447 */                             DBUpdater.updateDB(conn);
/*     */ 
/*     */                             
/* 450 */                             GlobalVariables.dbCleaner = new DBCleaner(new Timestamp(System.currentTimeMillis()));
/* 451 */                             GlobalVariables.cleanupThread = new Thread((Runnable)GlobalVariables.dbCleaner);
/* 452 */                             GlobalVariables.cleanupThread.setDaemon(true);
/* 453 */                             GlobalVariables.cleanupThread.start();
/* 454 */                             GlobalVariables.mainTimerStopped = true;
/*     */                             
/* 456 */                             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Database_restored_successfully"), Enums.EnumMessagePriority.AVERAGE, null, null);
/* 457 */                             SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*     */                           } finally {
/*     */                             
/* 460 */                             if (conn != null) {
/* 461 */                               conn.close();
/*     */                             }
/*     */                           }
/*     */                         
/* 465 */                         } catch (Exception ex) {
/* 466 */                           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_while_restoring_a_backup_file"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 467 */                           SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */                         } finally {
/* 469 */                           GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/* 470 */                           Main.startTasks(false, false);
/*     */                         } 
/*     */                         
/*     */                         break;
/*     */                       } 
/*     */                     } 
/* 476 */                   } else if ((tmpBuffer[0] & 0xFF) == 137) {
/* 477 */                     if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.BACKUP || Functions.isDbInCleanupState()) {
/* 478 */                       SocketFunctions.send(this.clientSocket, new byte[] { (byte)GlobalVariables.dbCurrentStatus.getDB_CURRENT_STATUS() });
/* 479 */                       Thread.sleep(500L);
/*     */                       break;
/*     */                     } 
/* 482 */                     boolean startFlag = false;
/* 483 */                     Enums.enumDbStatus tmp = GlobalVariables.dbCurrentStatus;
/*     */                     
/* 485 */                     try { GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.BACKUP;
/* 486 */                       int idx = 0;
/* 487 */                       int packLen = tmpBuffer[1] & 0xFF;
/* 488 */                       byte[] data = SocketFunctions.receive(this.clientSocket, 0, packLen);
/* 489 */                       if ((tmpBuffer[2] & 0xFF) == 85)
/* 490 */                       { String backupDir; GlobalVariables.mainTimerStopped = true;
/* 491 */                         int pLen = data[idx++] & 0xFF;
/* 492 */                         StringBuilder sb = new StringBuilder();
/* 493 */                         for (int i = 1; i <= pLen; i++) {
/* 494 */                           sb.append((char)(data[i] & 0xFF));
/*     */                         }
/* 496 */                         String userName = sb.toString();
/* 497 */                         File dbAbsFolder = null;
/* 498 */                         File root = new File("");
/* 499 */                         if (!ZeusServerCfg.getInstance().isNetworkAuth() && !ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/*     */                           try {
/* 501 */                             dbAbsFolder = new File(ZeusServerCfg.getInstance().getDbBackupDirectory());
/* 502 */                             if (!dbAbsFolder.exists()) {
/* 503 */                               dbAbsFolder.mkdirs();
/*     */                             }
/* 505 */                           } catch (Exception ex) {
/* 506 */                             dbAbsFolder = null;
/*     */                           } 
/*     */                         }
/*     */                         
/* 510 */                         SmbFile rootFolder = null;
/* 511 */                         if (ZeusServerCfg.getInstance().isNetworkAuth()) {
/*     */                           try {
/* 513 */                             NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(ZeusServerCfg.getInstance().getNetworkDomain(), ZeusServerCfg.getInstance().getNetworkUser(), ZeusServerCfg.getInstance().getNetworkPassword());
/* 514 */                             String fPath = "smb://" + ZeusServerCfg.getInstance().getDbBackupDirectory();
/* 515 */                             rootFolder = new SmbFile(fPath, auth);
/* 516 */                             if (!rootFolder.exists()) {
/* 517 */                               rootFolder.mkdirs();
/*     */                             }
/* 519 */                             backupDir = rootFolder.getPath() + "/" + Functions.getCurrentDateFolder() + userName + "/";
/* 520 */                           } catch (MalformedURLException|jcifs.smb.SmbException ex) {
/* 521 */                             rootFolder = null;
/* 522 */                             backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                           } 
/*     */                           
/* 525 */                           if (rootFolder == null) {
/* 526 */                             if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/* 527 */                               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_find_configured_backup_path") + ZeusServerCfg.getInstance().getDbBackupDirectory() + " " + LocaleMessage.getLocaleMessage("Because_of_that_we_are_going_to_save_the_backup_files_in_the_default_path") + "/backup_db", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                             }
/* 529 */                             backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                           }
/*     */                         
/* 532 */                         } else if (dbAbsFolder != null && dbAbsFolder.exists() && dbAbsFolder.isDirectory()) {
/* 533 */                           backupDir = dbAbsFolder.getAbsolutePath() + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                         } else {
/* 535 */                           if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/* 536 */                             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_find_configured_backup_path") + ZeusServerCfg.getInstance().getDbBackupDirectory() + " " + LocaleMessage.getLocaleMessage("Because_of_that_we_are_going_to_save_the_backup_files_in_the_default_path") + "/backup_db", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                           }
/* 538 */                           backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                         } 
/*     */                         
/* 541 */                         Thread zipThread = Functions.dbBackup(new File(backupDir), rootFolder);
/* 542 */                         zipThread.setName("zipThread");
/* 543 */                         Main.startTasks(false, false);
/* 544 */                         startFlag = true;
/* 545 */                         zipThread.join();
/* 546 */                         GlobalVariables.dbCurrentStatus = tmp;
/* 547 */                         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 548 */                         keepRunning = false;
/*     */ 
/*     */ 
/*     */                         
/* 552 */                         if (!startFlag) {
/* 553 */                           Main.startTasks(false, false);
/*     */                         }
/* 555 */                         GlobalVariables.dbCurrentStatus = tmp; break; }  } finally { if (!startFlag) Main.startTasks(false, false);  GlobalVariables.dbCurrentStatus = tmp; }
/*     */                   
/*     */                   } else {
/* 558 */                     if ((tmpBuffer[0] & 0xFF) == 144) {
/* 559 */                       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.NORMAL) {
/* 560 */                         SocketFunctions.send(this.clientSocket, new byte[] { (byte)GlobalVariables.dbCurrentStatus.getDB_CURRENT_STATUS() });
/* 561 */                         Thread.sleep(500L);
/*     */                         break;
/*     */                       } 
/*     */                       try {
/* 565 */                         GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.CLEANUP;
/* 566 */                         int packLen = tmpBuffer[1] & 0xFF;
/* 567 */                         byte[] data = SocketFunctions.receive(this.clientSocket, 0, packLen);
/* 568 */                         String userName = null;
/* 569 */                         String date = null;
/* 570 */                         int dbClean = 0;
/* 571 */                         int i = 0;
/* 572 */                         int dLen = 0;
/*     */                         
/* 574 */                         StringBuilder sb = new StringBuilder();
/* 575 */                         if ((tmpBuffer[2] & 0xFF) == 68) {
/* 576 */                           dLen = data[i++] & 0xFF;
/* 577 */                           for (; i < dLen + 1; i++) {
/* 578 */                             sb.append((char)(data[i] & 0xFF));
/*     */                           }
/* 580 */                           date = sb.toString();
/*     */                         } 
/* 582 */                         if ((data[i++] & 0xFF) == 66) {
/* 583 */                           int len = data[i++] & 0xFF;
/* 584 */                           dbClean = data[i++] & 0xFF;
/*     */                         } 
/* 586 */                         if ((data[i++] & 0xFF) == 85) {
/* 587 */                           sb = new StringBuilder();
/* 588 */                           int uLen = data[i++] & 0xFF;
/* 589 */                           for (; i < dLen + uLen + 6; i++) {
/* 590 */                             sb.append((char)(data[i] & 0xFF));
/*     */                           }
/* 592 */                           userName = sb.toString();
/*     */                         } 
/* 594 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Cleanup_process_initiated_by") + " " + userName.substring(0), Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */                         
/* 596 */                         if (dbClean == 1) {
/* 597 */                           String backupDir; GlobalVariables.mainTimerStopped = true;
/* 598 */                           File dbAbsFolder = null;
/* 599 */                           File root = new File("");
/* 600 */                           if (!ZeusServerCfg.getInstance().isNetworkAuth() && !ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/*     */                             try {
/* 602 */                               dbAbsFolder = new File(ZeusServerCfg.getInstance().getDbBackupDirectory());
/* 603 */                               if (!dbAbsFolder.exists()) {
/* 604 */                                 dbAbsFolder.mkdirs();
/*     */                               }
/* 606 */                             } catch (Exception ex) {
/* 607 */                               dbAbsFolder = null;
/*     */                             } 
/*     */                           }
/*     */                           
/* 611 */                           SmbFile rootFolder = null;
/* 612 */                           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Executing_the_backup_of_the_original_database"), Enums.EnumMessagePriority.LOW, null, null);
/* 613 */                           if (ZeusServerCfg.getInstance().isNetworkAuth()) {
/*     */                             try {
/* 615 */                               NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(ZeusServerCfg.getInstance().getNetworkDomain(), ZeusServerCfg.getInstance().getNetworkUser(), ZeusServerCfg.getInstance().getNetworkPassword());
/* 616 */                               String fPath = "smb://" + ZeusServerCfg.getInstance().getDbBackupDirectory();
/* 617 */                               rootFolder = new SmbFile(fPath, auth);
/* 618 */                               if (!rootFolder.exists()) {
/* 619 */                                 rootFolder.mkdirs();
/*     */                               }
/* 621 */                               backupDir = rootFolder.getPath() + "/" + Functions.getCurrentDateFolder() + userName + "/";
/* 622 */                             } catch (MalformedURLException|jcifs.smb.SmbException ex) {
/* 623 */                               rootFolder = null;
/* 624 */                               backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                             } 
/*     */                             
/* 627 */                             if (rootFolder == null) {
/* 628 */                               if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/* 629 */                                 Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_find_configured_backup_path") + ZeusServerCfg.getInstance().getDbBackupDirectory() + " " + LocaleMessage.getLocaleMessage("Because_of_that_we_are_going_to_save_the_backup_files_in_the_default_path") + "/backup_db", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                               }
/* 631 */                               backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                             }
/*     */                           
/* 634 */                           } else if (dbAbsFolder != null && dbAbsFolder.exists() && dbAbsFolder.isDirectory()) {
/* 635 */                             backupDir = dbAbsFolder.getAbsolutePath() + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                           } else {
/* 637 */                             if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/* 638 */                               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_find_configured_backup_path") + ZeusServerCfg.getInstance().getDbBackupDirectory() + " " + LocaleMessage.getLocaleMessage("Because_of_that_we_are_going_to_save_the_backup_files_in_the_default_path") + "/backup_db", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                             }
/* 640 */                             backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + userName + "/";
/*     */                           } 
/*     */                           
/* 643 */                           Functions.dbBackup(new File(backupDir), rootFolder);
/* 644 */                           Main.startTasks(false, false);
/*     */                         } 
/*     */                         
/* 647 */                         DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/* 648 */                         GlobalVariables.dbCleaner = new DBCleaner(new Timestamp(df1.parse(date).getTime()));
/* 649 */                         GlobalVariables.cleanupThread = new Thread((Runnable)GlobalVariables.dbCleaner);
/* 650 */                         GlobalVariables.cleanupThread.setDaemon(true);
/* 651 */                         GlobalVariables.cleanupThread.start();
/*     */ 
/*     */ 
/*     */                         
/* 655 */                         GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.CLEANUP;
/*     */                         
/* 657 */                         SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 658 */                         keepRunning = false;
/*     */                       }
/* 660 */                       catch (Exception ex) {
/* 661 */                         GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/* 662 */                         throw ex;
/*     */                       } finally {}
/*     */                       break;
/*     */                     } 
/* 666 */                     if (GlobalVariables.currentPlatform == Enums.Platform.ARM && (this.ip.equals("0.0.0.0") || this.ip.equals("127.0.0.1") || this.ip.equals(InetAddress.getLocalHost().getHostAddress()) || this.ip.equals(Functions.getMsgServerIP()))) {
/*     */                       
/* 668 */                       try { ZeusBoxHandler.handleZeusBoxEventPush(this.clientSocket, tmpBuffer); }
/*     */                       
/* 670 */                       catch (IOException|InterruptedException|SQLException|java.text.ParseException ex)
/* 671 */                       { ex.printStackTrace();
/* 672 */                         SocketFunctions.send(this.clientSocket, new byte[] { 21 }); }  break;
/*     */                     } 
/*     */                   } 
/*     */                 } 
/*     */               } 
/* 677 */             }  SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */           } 
/* 679 */         } else if (this.clientSocket.getInputStream().available() == 1) {
/* 680 */           byte[] tmpBuffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/* 681 */           if (tmpBuffer != null) {
/* 682 */             readCount = tmpBuffer.length;
/*     */           }
/* 684 */           if (readCount == 1) {
/* 685 */             if ((tmpBuffer[0] & 0xFF) == 129) {
/*     */               try {
/* 687 */                 Main.updateZeusSettings();
/* 688 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*     */               }
/* 690 */               catch (IOException|InterruptedException|SQLException ex) {
/* 691 */                 ex.printStackTrace();
/* 692 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */               }  break;
/*     */             } 
/* 695 */             if ((tmpBuffer[0] & 0xFF) == 136) {
/*     */               try {
/* 697 */                 Main.disconnectAllConnections();
/* 698 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*     */               }
/* 700 */               catch (IOException|InterruptedException ex) {
/* 701 */                 ex.printStackTrace();
/* 702 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */               }  break;
/*     */             } 
/* 705 */             if ((tmpBuffer[0] & 0xFF) == 153) {
/*     */               try {
/* 707 */                 GlobalVariables.buzzerActivated = false;
/* 708 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*     */               }
/* 710 */               catch (IOException ex) {
/* 711 */                 ex.printStackTrace();
/* 712 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */               }  break;
/*     */             } 
/* 715 */             if ((tmpBuffer[0] & 0xFF) == 145) {
/*     */               try {
/* 717 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 718 */                 GriffonMobileClientReceiver gmcr = new GriffonMobileClientReceiver(this.clientSocket);
/* 719 */                 TCPMessageServer.mobileAppDataUpdater = gmcr;
/* 720 */                 Thread thread = new Thread(gmcr);
/* 721 */                 thread.setName("GriffonMobileClientReceiver");
/* 722 */                 thread.setDaemon(true);
/* 723 */                 thread.start();
/* 724 */                 this.isForGriffonMobile = true;
/*     */               }
/* 726 */               catch (IOException ex) {
/* 727 */                 ex.printStackTrace();
/* 728 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */               }  break;
/*     */             } 
/* 731 */             if ((tmpBuffer[0] & 0xFF) == 113) {
/*     */               try {
/* 733 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 734 */                 PegasusMobileClientReceiver gmcr = new PegasusMobileClientReceiver(this.clientSocket);
/* 735 */                 TCPMessageServer.pegamobileAppDataUpdater = gmcr;
/* 736 */                 Thread thread = new Thread(gmcr);
/* 737 */                 thread.setName("PegasusMobileClientReceiver");
/* 738 */                 thread.setDaemon(true);
/* 739 */                 thread.start();
/* 740 */                 this.isForPegasusMobile = true;
/*     */               }
/* 742 */               catch (IOException ex) {
/* 743 */                 ex.printStackTrace();
/* 744 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */               }  break;
/*     */             } 
/* 747 */             if ((tmpBuffer[0] & 0xFF) == 64 && GlobalVariables.currentPlatform == Enums.Platform.ARM && (this.ip.equals("0.0.0.0") || this.ip.equals("127.0.0.1") || this.ip.equals(InetAddress.getLocalHost().getHostAddress()) || this.ip.equals(Functions.getMsgServerIP()))) {
/*     */               try {
/* 749 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/* 750 */                 ZeusBoxHandler.handleZeusBoxEventsPop(this.clientSocket);
/*     */               }
/* 752 */               catch (IOException|InterruptedException|SQLException ex) {
/* 753 */                 ex.printStackTrace();
/* 754 */                 SocketFunctions.send(this.clientSocket, new byte[] { 21 });
/*     */               } 
/*     */               break;
/*     */             } 
/*     */           } 
/*     */         } 
/* 760 */         Thread.sleep(300L);
/*     */       } 
/* 762 */     } catch (InterruptedException interruptedException) {
/*     */     
/* 764 */     } catch (Exception ex) {
/* 765 */       ex.printStackTrace();
/* 766 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_Message_Server_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/*     */     } finally {
/* 768 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private byte[] prepareLoginResponse(LoggedInUser user, byte resp) {
/* 773 */     int uLen = 0;
/* 774 */     if (user != null) {
/* 775 */       uLen += (user.getUserName() != null) ? user.getUserName().length() : 0;
/* 776 */       uLen += (user.getAssignedProducts() != null) ? user.getAssignedProducts().length() : 0;
/*     */     } 
/* 778 */     byte[] response = new byte[uLen + 10];
/* 779 */     int index = 0;
/* 780 */     response[index++] = -123;
/* 781 */     response[index++] = (byte)(uLen + 6);
/* 782 */     response[index++] = resp;
/* 783 */     response[index++] = 85;
/* 784 */     response[index++] = (byte)((user.getUserName() != null) ? user.getUserName().length() : 0);
/* 785 */     if (user.getUserName() != null) {
/* 786 */       for (char c : user.getUserName().toCharArray()) {
/* 787 */         response[index++] = (byte)c;
/*     */       }
/*     */     }
/* 790 */     response[index++] = 80;
/* 791 */     response[index++] = (byte)((user.getAssignedProducts() != null) ? user.getAssignedProducts().length() : 0);
/* 792 */     if (user.getAssignedProducts() != null) {
/* 793 */       for (char c : user.getAssignedProducts().toCharArray()) {
/* 794 */         response[index++] = (byte)c;
/*     */       }
/*     */     }
/* 797 */     response[index++] = (byte)user.getClientType();
/* 798 */     int crcCalc = CRC16.calculate(response, 0, uLen + 8, 65535);
/* 799 */     byte[] crc = Functions.get2ByteArrayFromInt(crcCalc);
/* 800 */     response[index++] = crc[1];
/* 801 */     response[index++] = crc[0];
/* 802 */     return response;
/*     */   }
/*     */   
/*     */   private void dispose() {
/*     */     try {
/* 807 */       if (!this.isForPegasusMobile && !this.isForGriffonMobile) {
/* 808 */         this.clientSocket.close();
/*     */       }
/* 810 */     } catch (IOException iOException) {}
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private String getPath(byte[] decryptedBuffer) {
/* 817 */     StringBuilder sb = new StringBuilder(decryptedBuffer.length);
/* 818 */     for (byte b : decryptedBuffer) {
/* 819 */       sb.append((char)(b & 0xFF));
/*     */     }
/* 821 */     return sb.toString();
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\TCPMessageHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */