/*    */ package com.zeusServer.tblConnections;
/*    */ 
/*    */ import com.zeusServer.socket.communication.UdpV2Handler;
/*    */ import java.util.concurrent.ConcurrentHashMap;
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
/*    */ public class TblActiveUdpConnections
/*    */ {
/*    */   private static ConcurrentHashMap<String, UdpV2Handler> instance;
/*    */   
/*    */   public static ConcurrentHashMap<String, UdpV2Handler> getInstance() {
/* 23 */     if (instance == null) {
/* 24 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 26 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addConnection(String ip, UdpV2Handler handler) {
/* 30 */     synchronized (getInstance()) {
/* 31 */       instance.put(ip, handler);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void removeConnection(String ip) {
/* 36 */     synchronized (getInstance()) {
/* 37 */       instance.remove(ip);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblActiveUdpConnections.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */