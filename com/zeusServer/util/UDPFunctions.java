/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.net.DatagramPacket;
/*    */ import java.net.DatagramSocket;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class UDPFunctions
/*    */ {
/*    */   public static void send(DatagramSocket socket, DatagramPacket packet, byte[] data) throws IOException {
/* 18 */     DatagramPacket outPacket = new DatagramPacket(data, 0, data.length, packet.getAddress(), packet.getPort());
/* 19 */     socket.send(outPacket);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\UDPFunctions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */