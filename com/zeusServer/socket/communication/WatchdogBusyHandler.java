/*    */ package com.zeusServer.socket.communication;
/*    */ 
/*    */ import com.zeusServer.util.SocketFunctions;
/*    */ import java.io.IOException;
/*    */ import java.net.Socket;
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
/*    */ public class WatchdogBusyHandler
/*    */   implements Runnable
/*    */ {
/*    */   private Socket sock;
/*    */   
/*    */   public WatchdogBusyHandler(Socket sock) {
/* 25 */     this.sock = sock;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 30 */     byte[] data = { 6 };
/* 31 */     long currTime = System.currentTimeMillis();
/* 32 */     int cnt = 0;
/*    */     try {
/* 34 */       while (System.currentTimeMillis() < currTime + 300000L) {
/*    */         try {
/* 36 */           int len = this.sock.getInputStream().available();
/* 37 */           byte[] tmp = new byte[len];
/* 38 */           if (len > 0) {
/* 39 */             cnt++;
/* 40 */             this.sock.getInputStream().read(tmp, 0, len);
/* 41 */             SocketFunctions.send(this.sock, data);
/* 42 */             if (cnt >= 2) {
/*    */               break;
/*    */             }
/*    */           } 
/* 46 */         } catch (IOException iOException) {}
/*    */ 
/*    */         
/*    */         try {
/* 50 */           Thread.sleep(10000L);
/* 51 */         } catch (InterruptedException interruptedException) {}
/*    */       } 
/*    */     } finally {
/*    */       
/*    */       try {
/* 56 */         this.sock.close();
/* 57 */       } catch (IOException iOException) {}
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\WatchdogBusyHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */