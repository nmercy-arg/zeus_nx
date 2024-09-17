/*    */ package com.zeusServer.socket.communication;
/*    */ 
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class UdpV2Handler
/*    */ {
/*    */   protected long idleTimeout;
/*    */   protected short lastCommIface;
/*    */   public String sn;
/* 25 */   public long lastCommunicationTime = 0L;
/*    */   
/*    */   public abstract void processM2SPacket(byte[] paramArrayOfbyte);
/*    */   
/*    */   public abstract void processM2SPacket(byte[] paramArrayOfbyte, int paramInt);
/*    */   
/*    */   public abstract void removeIdleConnections();
/*    */   
/*    */   public abstract void sendNewCMDAtInActiveTime();
/*    */   
/*    */   public abstract void updateRemoteIP(String paramString, DatagramSocket paramDatagramSocket, DatagramPacket paramDatagramPacket);
/*    */   
/*    */   public abstract DatagramSocket getCurrentSocket();
/*    */   
/*    */   public abstract DatagramPacket getCurrentPacket();
/*    */   
/*    */   public abstract void dispose();
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\UdpV2Handler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */