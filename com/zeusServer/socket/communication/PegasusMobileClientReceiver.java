/*     */ package com.zeusServer.socket.communication;
/*     */ 
/*     */ import com.zeusServer.tblConnections.TblActivePegasusMobileConnections;
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.SocketFunctions;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
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
/*     */ public class PegasusMobileClientReceiver
/*     */   implements Runnable
/*     */ {
/*     */   public Socket clientSocket;
/*     */   public boolean keepRunning;
/*     */   private long idleTimeout;
/*     */   
/*     */   public PegasusMobileClientReceiver(Socket clientSocket) {
/*  31 */     this.clientSocket = clientSocket;
/*  32 */     this.keepRunning = true;
/*  33 */     this.idleTimeout = System.currentTimeMillis() + 70000L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  41 */       while (this.keepRunning && this.clientSocket.isConnected() && this.idleTimeout > System.currentTimeMillis()) {
/*     */         try {
/*  43 */           if (this.clientSocket.getInputStream().available() == 1) {
/*  44 */             byte[] tmpBuffer = SocketFunctions.receive(this.clientSocket, 0, 1);
/*  45 */             if (tmpBuffer != null && tmpBuffer.length == 1 && (
/*  46 */               tmpBuffer[0] & 0xFF) == 114) {
/*  47 */               SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*  48 */               this.idleTimeout = System.currentTimeMillis() + 70000L;
/*     */             }
/*     */           
/*  51 */           } else if (this.clientSocket.getInputStream().available() > 1) {
/*  52 */             byte[] tmpBuffer = SocketFunctions.receive(this.clientSocket, 0, this.clientSocket.getInputStream().available());
/*  53 */             if (tmpBuffer != null) {
/*     */               
/*  55 */               int messageLen = tmpBuffer[1] & 0xFF;
/*  56 */               int crcReceived = tmpBuffer[messageLen + 2 + 1] & 0xFF;
/*  57 */               crcReceived = crcReceived * 256 + (tmpBuffer[messageLen + 2] & 0xFF);
/*  58 */               int crcCalc = CRC16.calculate(tmpBuffer, 0, messageLen + 2, 65535);
/*  59 */               if (crcReceived == crcCalc) {
/*  60 */                 int idClient = getIdClient(tmpBuffer, 2, messageLen);
/*  61 */                 if ((tmpBuffer[0] & 0xFF) == 115) {
/*  62 */                   TblActivePegasusMobileConnections.addConnection(idClient);
/*  63 */                 } else if ((tmpBuffer[0] & 0xFF) == 116) {
/*  64 */                   TblActivePegasusMobileConnections.removeConnection(idClient);
/*     */                 } 
/*  66 */                 SocketFunctions.send(this.clientSocket, new byte[] { 6 });
/*     */               } 
/*  68 */               this.idleTimeout = System.currentTimeMillis() + 70000L;
/*     */             } 
/*     */           } 
/*  71 */           monitorThread();
/*  72 */         } catch (IOException|InterruptedException ex) {
/*  73 */           ex.printStackTrace();
/*     */         } 
/*  75 */         Thread.sleep(300L);
/*     */       } 
/*  77 */     } catch (InterruptedException interruptedException) {
/*     */     
/*  79 */     } catch (Exception ex) {
/*  80 */       ex.printStackTrace();
/*     */     } finally {
/*     */       
/*  83 */       dispose();
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
/*     */   public void sendData(byte[] data) throws IOException {
/*  95 */     SocketFunctions.send(this.clientSocket, data);
/*  96 */     this.idleTimeout = System.currentTimeMillis() + 70000L;
/*     */   }
/*     */   
/*     */   private void monitorThread() throws InterruptedException, IOException {
/* 100 */     for (byte ii = 0; ii < 30; ii = (byte)(ii + 1)) {
/* 101 */       Thread.sleep(1000L);
/* 102 */       if (this.clientSocket.getInputStream().available() > 0) {
/*     */         break;
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   private int getIdClient(byte[] buffer, int offset, int length) {
/* 109 */     char[] ch = new char[length];
/* 110 */     for (int i = 0; i < length; i++) {
/* 111 */       ch[i] = (char)buffer[i + offset];
/*     */     }
/* 113 */     return Integer.parseInt("0" + (new String(ch)).trim());
/*     */   }
/*     */   
/*     */   private void dispose() {
/*     */     try {
/* 118 */       this.clientSocket.close();
/* 119 */     } catch (IOException iOException) {}
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\PegasusMobileClientReceiver.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */