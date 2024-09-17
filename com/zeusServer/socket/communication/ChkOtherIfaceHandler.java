/*    */ package com.zeusServer.socket.communication;
/*    */ 
/*    */ import com.zeusServer.util.SocketFunctions;
/*    */ import com.zeusServer.util.UDPFunctions;
/*    */ import java.io.IOException;
/*    */ import java.net.DatagramPacket;
/*    */ import java.net.DatagramSocket;
/*    */ import java.net.Socket;
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
/*    */ public class ChkOtherIfaceHandler
/*    */   implements Runnable
/*    */ {
/*    */   private Socket clientSocket;
/*    */   private DatagramPacket inPacket;
/*    */   private DatagramSocket socket;
/*    */   private String type;
/*    */   
/*    */   public ChkOtherIfaceHandler(Socket clientSocket, DatagramPacket inPacket, DatagramSocket socket, String type) {
/* 34 */     this.clientSocket = clientSocket;
/* 35 */     this.inPacket = inPacket;
/* 36 */     this.socket = socket;
/* 37 */     this.type = type;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 42 */     byte[] data = { 6 };
/*    */     try {
/* 44 */       if (this.type.equals("TCP")) {
/* 45 */         SocketFunctions.send(this.clientSocket, data);
/*    */       } else {
/* 47 */         UDPFunctions.send(this.socket, this.inPacket, data);
/*    */       } 
/* 49 */     } catch (IOException ex) {
/* 50 */       Logger.getLogger(ChkOtherIfaceHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\ChkOtherIfaceHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */