/*    */ package com.zeusServer.ui;
/*    */ 
/*    */ import com.zeusServer.util.Functions;
/*    */ import com.zeusServer.util.SocketFunctions;
/*    */ import com.zeusServer.util.ZeusServerCfg;
/*    */ import java.io.IOException;
/*    */ import java.net.Socket;
/*    */ import java.net.SocketException;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class BackupUtil
/*    */ {
/*    */   private Socket socket;
/*    */   
/*    */   public int backup() {
/* 28 */     int res = 0;
/*    */     try {
/* 30 */       openSocket();
/* 31 */       SocketFunctions.connect(this.socket, Functions.getMsgServerIP(), ZeusServerCfg.getInstance().getMsgServerPort().intValue(), 30000);
/* 32 */       byte[] backupPacket = prepareDBBackupPacket("$ZeusNxManager");
/* 33 */       if (this.socket.isConnected() && !this.socket.isClosed()) {
/* 34 */         SocketFunctions.send(this.socket, backupPacket);
/* 35 */         sleepThread();
/* 36 */         if (this.socket.getInputStream().available() > 0) {
/* 37 */           byte[] responsePacket = SocketFunctions.receive(this.socket, 0, 1);
/* 38 */           res = responsePacket[0];
/*    */         } 
/*    */       } 
/* 41 */     } catch (Exception ex) {
/* 42 */       ex.printStackTrace();
/*    */     } finally {
/* 44 */       SocketFunctions.closeSocket(this.socket);
/* 45 */       this.socket = null;
/*    */     } 
/* 47 */     return res;
/*    */   }
/*    */   
/*    */   private void openSocket() throws SocketException {
/* 51 */     if (this.socket == null) {
/* 52 */       this.socket = new Socket();
/* 53 */       this.socket.setTcpNoDelay(true);
/* 54 */       this.socket.setSoTimeout(30000);
/*    */     } 
/*    */   }
/*    */   
/*    */   private void sleepThread() throws InterruptedException, IOException {
/* 59 */     for (byte ii = 0; ii < 200; ii = (byte)(ii + 1)) {
/* 60 */       Thread.sleep(1000L);
/* 61 */       if (this.socket != null && this.socket.getInputStream().available() > 0) {
/*    */         break;
/*    */       }
/*    */     } 
/*    */   }
/*    */   
/*    */   private byte[] prepareDBBackupPacket(String name) {
/* 68 */     byte[] packet = new byte[name.length() + 4];
/* 69 */     int idx = 0;
/* 70 */     packet[idx++] = -119;
/* 71 */     packet[idx++] = (byte)(name.length() + 2);
/* 72 */     packet[idx++] = 85;
/* 73 */     packet[idx++] = (byte)name.length();
/* 74 */     for (char c : name.toCharArray()) {
/* 75 */       packet[idx++] = (byte)c;
/*    */     }
/* 77 */     return packet;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\BackupUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */