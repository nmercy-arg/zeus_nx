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
/*    */ public class TblPegasusActiveConnections
/*    */ {
/* 24 */   public static volatile AtomicInteger numberOfPendingIdentificationPackets = new AtomicInteger(0);
/* 25 */   public static Semaphore semaphoreAlivePacketsReceived = new Semaphore(2500, true);
/*    */   private static ConcurrentHashMap<String, InfoModule> instance;
/*    */   
/*    */   public static ConcurrentHashMap<String, InfoModule> getInstance() {
/* 29 */     if (instance == null) {
/* 30 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 32 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addConnection(String iccid, String threadUUID) {
/* 36 */     synchronized (getInstance()) {
/* 37 */       instance.put(iccid, new InfoModule());
/* 38 */       ((InfoModule)instance.get(iccid)).ownerThreadGuid = threadUUID;
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void removeConnection(String iccid) {
/* 43 */     synchronized (getInstance()) {
/* 44 */       instance.remove(iccid);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblPegasusActiveConnections.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */