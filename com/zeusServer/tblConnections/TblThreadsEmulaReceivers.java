/*    */ package com.zeusServer.tblConnections;
/*    */ 
/*    */ import com.zeusServer.serialPort.communication.EmulateReceiver;
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
/*    */ public class TblThreadsEmulaReceivers
/*    */ {
/*    */   private static ConcurrentHashMap<String, EmulateReceiver> instance;
/*    */   private static ConcurrentHashMap<String, Thread> threadHolder;
/*    */   
/*    */   public static ConcurrentHashMap<String, EmulateReceiver> getInstance() {
/* 25 */     if (instance == null) {
/* 26 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 28 */     if (threadHolder == null) {
/* 29 */       threadHolder = new ConcurrentHashMap<>();
/*    */     }
/* 31 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addThread(String comPort, EmulateReceiver crCSD, Thread thread) {
/* 35 */     synchronized (getInstance()) {
/* 36 */       instance.put(comPort, crCSD);
/* 37 */       threadHolder.put(comPort, thread);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static Thread getThread4Receiver(String comPort) {
/* 42 */     if (threadHolder != null && threadHolder.containsKey(comPort)) {
/* 43 */       return threadHolder.get(comPort);
/*    */     }
/* 45 */     return null;
/*    */   }
/*    */ 
/*    */   
/*    */   public static void clearThreads() {
/* 50 */     if (threadHolder != null)
/* 51 */       threadHolder.clear(); 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblThreadsEmulaReceivers.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */