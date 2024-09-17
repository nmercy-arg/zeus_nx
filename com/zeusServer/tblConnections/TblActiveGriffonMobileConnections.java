/*    */ package com.zeusServer.tblConnections;
/*    */ 
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
/*    */ public class TblActiveGriffonMobileConnections
/*    */ {
/*    */   private static ConcurrentHashMap<Integer, String> instance;
/*    */   
/*    */   public static ConcurrentHashMap<Integer, String> getInstance() {
/* 22 */     if (instance == null) {
/* 23 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 25 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addConnection(int idClient) {
/* 29 */     synchronized (getInstance()) {
/* 30 */       instance.put(Integer.valueOf(idClient), "");
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void removeConnection(int idClient) {
/* 35 */     synchronized (getInstance()) {
/* 36 */       instance.remove(Integer.valueOf(idClient));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblActiveGriffonMobileConnections.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */