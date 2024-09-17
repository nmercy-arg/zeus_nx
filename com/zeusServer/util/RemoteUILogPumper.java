/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.net.DatagramPacket;
/*    */ import java.net.DatagramSocket;
/*    */ import java.net.InetAddress;
/*    */ import java.net.SocketException;
/*    */ import java.util.concurrent.ConcurrentHashMap;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
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
/*    */ 
/*    */ public class RemoteUILogPumper
/*    */   implements Runnable
/*    */ {
/*    */   private DatagramSocket socket;
/*    */   private String remoteIp;
/*    */   private byte[] message;
/*    */   private int remotePort;
/* 33 */   private static ConcurrentHashMap<String, InetAddress> remoteAdd = new ConcurrentHashMap<>();
/*    */   
/*    */   boolean flag = true;
/*    */   
/*    */   public RemoteUILogPumper(String remoteIp, byte[] message, int remotePort) {
/*    */     try {
/* 39 */       this.socket = new DatagramSocket();
/* 40 */       this.remoteIp = remoteIp;
/* 41 */       this.message = message;
/* 42 */       this.remotePort = remotePort;
/* 43 */       if (!remoteAdd.containsKey(remoteIp)) {
/* 44 */         remoteAdd.put(remoteIp, InetAddress.getByName(remoteIp));
/*    */       }
/* 46 */     } catch (SocketException|java.net.UnknownHostException ex) {
/* 47 */       this.flag = false;
/* 48 */       Logger.getLogger(RemoteUILogPumper.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void run() {
/*    */     try {
/* 57 */       if (this.flag) {
/* 58 */         DatagramPacket packet = new DatagramPacket(this.message, 0, this.message.length, remoteAdd.get(this.remoteIp), this.remotePort);
/* 59 */         this.socket.send(packet);
/*    */       } 
/* 61 */     } catch (IOException ex) {
/* 62 */       Logger.getLogger(RemoteUILogPumper.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\RemoteUILogPumper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */