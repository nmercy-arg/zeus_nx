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
/*    */ 
/*    */ public class TblPrinterSpool
/*    */ {
/* 20 */   private static long key = 0L;
/*    */   private static ConcurrentHashMap<Long, String> instance;
/*    */   
/*    */   public static ConcurrentHashMap<Long, String> getInstance() {
/* 24 */     if (instance == null) {
/* 25 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 27 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addPrintJob(String printJob) {
/* 31 */     synchronized (getInstance()) {
/* 32 */       instance.put(Long.valueOf(key++), printJob);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblPrinterSpool.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */