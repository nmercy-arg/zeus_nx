/*    */ package com.zeusServer.tblConnections;
/*    */ 
/*    */ import com.zeusServer.util.LoggedInUser;
/*    */ import java.util.Map;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TblActiveUserLogins
/*    */ {
/*    */   public static final String SUPER_ADMIN = "SUPER_ADMIN";
/*    */   public static final String ADMIN = "ADMIN";
/*    */   public static final String PRODUCT_USER = "PRODUCT_USER";
/*    */   private static final int ADMIN_USER_TYPE = 7;
/*    */   private static final int ADMIN_TYPE = 15;
/*    */   private static final int SUPER_ADMIN_YPE = 31;
/*    */   private static ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, LoggedInUser>>> instance;
/*    */   
/*    */   public static ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, LoggedInUser>>> getInstance() {
/* 36 */     if (instance == null) {
/* 37 */       instance = new ConcurrentHashMap<>();
/*    */     }
/* 39 */     return instance;
/*    */   }
/*    */   
/*    */   public static void addUIUser(LoggedInUser user) {
/* 43 */     synchronized (getInstance()) {
/* 44 */       String key = null;
/* 45 */       switch (user.getClientType()) {
/*    */         case 7:
/* 47 */           key = "PRODUCT_USER";
/*    */           break;
/*    */         case 15:
/* 50 */           key = "ADMIN";
/*    */           break;
/*    */         case 31:
/* 53 */           key = "SUPER_ADMIN";
/*    */           break;
/*    */       } 
/* 56 */       if (key != null) {
/* 57 */         if (!instance.containsKey(key)) {
/* 58 */           ConcurrentHashMap<Integer, ConcurrentHashMap<String, LoggedInUser>> map = new ConcurrentHashMap<>();
/* 59 */           instance.put(key, map);
/*    */         } 
/*    */         
/* 62 */         if (((ConcurrentHashMap)instance.get(key)).containsKey(Integer.valueOf(user.getIdClient()))) {
/*    */           
/* 64 */           ((ConcurrentHashMap<String, LoggedInUser>)((ConcurrentHashMap)instance.get(key)).get(Integer.valueOf(user.getIdClient()))).put(user.getRemoteIp() + ":" + user.getRemoteUdpPort(), user);
/*    */         } else {
/* 66 */           ConcurrentHashMap<String, LoggedInUser> map = new ConcurrentHashMap<>();
/* 67 */           map.put(user.getRemoteIp() + ":" + user.getRemoteUdpPort(), user);
/* 68 */           ((ConcurrentHashMap<Integer, ConcurrentHashMap<String, LoggedInUser>>)instance.get(key)).put(Integer.valueOf(user.getIdClient()), map);
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void removeConnection(String remoteUrl) {
/* 75 */     synchronized (getInstance()) {
/*    */       
/* 77 */       label23: for (Map.Entry<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, LoggedInUser>>> allUsers : instance.entrySet()) {
/* 78 */         for (Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>> entry : (Iterable<Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>>>)((ConcurrentHashMap)allUsers.getValue()).entrySet()) {
/* 79 */           for (Map.Entry<String, LoggedInUser> user : (Iterable<Map.Entry<String, LoggedInUser>>)((ConcurrentHashMap)entry.getValue()).entrySet()) {
/* 80 */             if (((String)user.getKey()).equalsIgnoreCase(remoteUrl)) {
/* 81 */               ((ConcurrentHashMap)entry.getValue()).remove(remoteUrl);
/*    */               break label23;
/*    */             } 
/*    */           } 
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void updateAlivePacketReceived(String remoteUrl) {
/* 91 */     synchronized (getInstance()) {
/*    */       
/* 93 */       label23: for (Map.Entry<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, LoggedInUser>>> allUsers : instance.entrySet()) {
/* 94 */         for (Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>> entry : (Iterable<Map.Entry<Integer, ConcurrentHashMap<String, LoggedInUser>>>)((ConcurrentHashMap)allUsers.getValue()).entrySet()) {
/* 95 */           for (Map.Entry<String, LoggedInUser> user : (Iterable<Map.Entry<String, LoggedInUser>>)((ConcurrentHashMap)entry.getValue()).entrySet()) {
/* 96 */             if (((String)user.getKey()).equalsIgnoreCase(remoteUrl)) {
/* 97 */               ((LoggedInUser)user.getValue()).setLastAlivePacketReceived(((LoggedInUser)user.getValue()).getLastAlivePacketReceived() + 65000L);
/*    */               break label23;
/*    */             } 
/*    */           } 
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\tblConnections\TblActiveUserLogins.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */