/*    */ package com.zeusServer.tblConnections;
/*    */ 
/*    */ import com.zeusServer.util.Functions;
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
/*    */ 
/*    */ 
/*    */ public class TblEventMonitoringMessages
/*    */ {
/*    */   private static final int MAXIMUM_ENTRIES_TABLE_EVENTS_MONITORING = 256;
/* 23 */   private static long key = 0L;
/*    */   private static ConcurrentHashMap<Long, String> instance;
/*    */   
/*    */   public static ConcurrentHashMap<Long, String> getInstance() {
/* 27 */     if (instance == null) {
/* 28 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 30 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addEvent(String event) {
/* 34 */     synchronized (getInstance()) {
/*    */       
/* 36 */       instance.put(Long.valueOf(key++), event);
/*    */       
/* 38 */       instance.remove(Long.valueOf(Functions.getOldestKey(instance.keySet())));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblEventMonitoringMessages.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */