/*    */ package com.zeusServer.tblConnections;
/*    */ 
/*    */ import com.zeusServer.util.InfoModule;
/*    */ import java.util.concurrent.ConcurrentHashMap;
/*    */ import java.util.concurrent.Semaphore;
/*    */ import java.util.concurrent.atomic.AtomicInteger;
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
/*    */ 
/*    */ public class TblMercuriusActiveConnections
/*    */ {
/* 25 */   public static volatile AtomicInteger numberOfPendingIdentificationPackets = new AtomicInteger(0);
/* 26 */   public static Semaphore semaphoreAlivePacketsReceived = new Semaphore(2500, true);
/*    */   private static ConcurrentHashMap<String, InfoModule> instance;
/*    */   
/*    */   public static ConcurrentHashMap<String, InfoModule> getInstance() {
/* 30 */     if (instance == null) {
/* 31 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 33 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addConnection(String iccid, String threadUUID) {
/* 37 */     synchronized (getInstance()) {
/* 38 */       instance.put(iccid, new InfoModule());
/* 39 */       ((InfoModule)instance.get(iccid)).ownerThreadGuid = threadUUID;
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void removeConnection(String iccid) {
/* 44 */     synchronized (getInstance()) {
/* 45 */       instance.remove(iccid);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblMercuriusActiveConnections.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */