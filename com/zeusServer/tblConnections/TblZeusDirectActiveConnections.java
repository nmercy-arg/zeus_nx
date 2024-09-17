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
/*    */ 
/*    */ 
/*    */ public class TblZeusDirectActiveConnections
/*    */ {
/*    */   private static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> instance;
/*    */   
/*    */   public static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> getInstance() {
/* 25 */     if (instance == null) {
/* 26 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 28 */     return instance;
/*    */   }
/*    */   
/*    */   public static boolean addConnection(String product, String user) {
/* 32 */     synchronized (getInstance()) {
/* 33 */       if (instance.containsKey(product)) {
/* 34 */         if (((ConcurrentHashMap)instance.get(product)).containsKey(user)) {
/* 35 */           if (((Integer)((ConcurrentHashMap)instance.get(product)).get(user)).intValue() < 5) {
/* 36 */             int val = ((Integer)((ConcurrentHashMap)instance.get(product)).get(user)).intValue();
/* 37 */             ((ConcurrentHashMap<String, Integer>)instance.get(product)).put(user, Integer.valueOf(val + 1));
/* 38 */             return true;
/*    */           } 
/* 40 */           return false;
/*    */         } 
/*    */         
/* 43 */         ((ConcurrentHashMap<String, Integer>)instance.get(product)).put(user, Integer.valueOf(1));
/* 44 */         return true;
/*    */       } 
/*    */       
/* 47 */       ConcurrentHashMap<String, Integer> inner = new ConcurrentHashMap<>();
/* 48 */       inner.put(user, Integer.valueOf(1));
/* 49 */       instance.put(product, inner);
/* 50 */       return true;
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public static void removeConnection(String iccid) {
/* 56 */     synchronized (getInstance()) {
/* 57 */       instance.remove(iccid);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblZeusDirectActiveConnections.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */