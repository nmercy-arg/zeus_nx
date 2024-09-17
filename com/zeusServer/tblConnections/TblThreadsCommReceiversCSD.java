/*    */ package com.zeusServer.tblConnections;
/*    */ 
/*    */ import com.zeusServer.serialPort.communication.CommReceiverCSD;
/*    */ import java.util.UUID;
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
/*    */ public class TblThreadsCommReceiversCSD
/*    */ {
/*    */   private static ConcurrentHashMap<String, CommReceiverCSD> instance;
/*    */   
/*    */   public static ConcurrentHashMap<String, CommReceiverCSD> getInstance() {
/* 25 */     if (instance == null) {
/* 26 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 28 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addThread(CommReceiverCSD crCSD) {
/* 32 */     synchronized (getInstance()) {
/* 33 */       instance.put(UUID.randomUUID().toString(), crCSD);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblThreadsCommReceiversCSD.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */