/*     */ package com.zeusServer.util;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class LoggedInUser
/*     */ {
/*     */   private int idUser;
/*     */   private int idClient;
/*     */   private int idGroup;
/*     */   private int clientType;
/*     */   private int enabled;
/*     */   private String permissions;
/*     */   private int dateFormat;
/*     */   private String timeZone;
/*     */   private String language;
/*     */   private String userName;
/*     */   private String remoteIp;
/*     */   private int remoteUdpPort;
/*  34 */   private long lastAlivePacketReceived = System.currentTimeMillis() + 70000L;
/*     */   private String assignedProducts;
/*     */   
/*     */   public int getIdUser() {
/*  38 */     return this.idUser;
/*     */   }
/*     */   
/*     */   public void setIdUser(int idUser) {
/*  42 */     this.idUser = idUser;
/*     */   }
/*     */   
/*     */   public int getIdClient() {
/*  46 */     return this.idClient;
/*     */   }
/*     */   
/*     */   public void setIdClient(int idClient) {
/*  50 */     this.idClient = idClient;
/*     */   }
/*     */   
/*     */   public int getIdGroup() {
/*  54 */     return this.idGroup;
/*     */   }
/*     */   
/*     */   public void setIdGroup(int idGroup) {
/*  58 */     this.idGroup = idGroup;
/*     */   }
/*     */   
/*     */   public int getClientType() {
/*  62 */     return this.clientType;
/*     */   }
/*     */   
/*     */   public void setClientType(int clientType) {
/*  66 */     this.clientType = clientType;
/*     */   }
/*     */   
/*     */   public int getEnabled() {
/*  70 */     return this.enabled;
/*     */   }
/*     */   
/*     */   public void setEnabled(int enabled) {
/*  74 */     this.enabled = enabled;
/*     */   }
/*     */   
/*     */   public String getPermissions() {
/*  78 */     return this.permissions;
/*     */   }
/*     */   
/*     */   public void setPermissions(String permissions) {
/*  82 */     this.permissions = permissions;
/*     */   }
/*     */   
/*     */   public int getDateFormat() {
/*  86 */     return this.dateFormat;
/*     */   }
/*     */   
/*     */   public void setDateFormat(int dateFormat) {
/*  90 */     this.dateFormat = dateFormat;
/*     */   }
/*     */   
/*     */   public String getTimeZone() {
/*  94 */     return this.timeZone;
/*     */   }
/*     */   
/*     */   public void setTimeZone(String timeZone) {
/*  98 */     this.timeZone = timeZone;
/*     */   }
/*     */   
/*     */   public String getLanguage() {
/* 102 */     return this.language;
/*     */   }
/*     */   
/*     */   public void setLanguage(String language) {
/* 106 */     this.language = language;
/*     */   }
/*     */   
/*     */   public String getUserName() {
/* 110 */     return this.userName;
/*     */   }
/*     */   
/*     */   public void setUserName(String userName) {
/* 114 */     this.userName = userName;
/*     */   }
/*     */   
/*     */   public String getRemoteIp() {
/* 118 */     return this.remoteIp;
/*     */   }
/*     */   
/*     */   public void setRemoteIp(String remoteIp) {
/* 122 */     this.remoteIp = remoteIp;
/*     */   }
/*     */   
/*     */   public int getRemoteUdpPort() {
/* 126 */     return this.remoteUdpPort;
/*     */   }
/*     */   
/*     */   public void setRemoteUdpPort(int remoteUdpPort) {
/* 130 */     this.remoteUdpPort = remoteUdpPort;
/*     */   }
/*     */   
/*     */   public long getLastAlivePacketReceived() {
/* 134 */     return this.lastAlivePacketReceived;
/*     */   }
/*     */   
/*     */   public void setLastAlivePacketReceived(long lastAlivePacketReceived) {
/* 138 */     this.lastAlivePacketReceived = lastAlivePacketReceived;
/*     */   }
/*     */   
/*     */   public String getAssignedProducts() {
/* 142 */     return this.assignedProducts;
/*     */   }
/*     */   
/*     */   public void setAssignedProducts(String assignedProducts) {
/* 146 */     this.assignedProducts = assignedProducts;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\LoggedInUser.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */