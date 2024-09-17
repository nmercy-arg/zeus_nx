/*     */ package com.zeusServer.dto;
/*     */ 
/*     */ import java.sql.Timestamp;
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
/*     */ public class PendingDataHolder
/*     */ {
/*     */   private int idPendingAlive;
/*     */   private int idClient;
/*     */   private int idModule;
/*     */   private int timezone;
/*     */   private Timestamp received;
/*     */   private byte[] content;
/*     */   private short version;
/*     */   private short lastCommInterface;
/*     */   private short minGsmSignalLevel;
/*     */   private String lastNWProtocol;
/*     */   
/*     */   public int getIdPendingAlive() {
/*  30 */     return this.idPendingAlive;
/*     */   }
/*     */   
/*     */   public void setIdPendingAlive(int idPendingAlive) {
/*  34 */     this.idPendingAlive = idPendingAlive;
/*     */   }
/*     */   
/*     */   public int getIdClient() {
/*  38 */     return this.idClient;
/*     */   }
/*     */   
/*     */   public void setIdClient(int idClient) {
/*  42 */     this.idClient = idClient;
/*     */   }
/*     */   
/*     */   public int getIdModule() {
/*  46 */     return this.idModule;
/*     */   }
/*     */   
/*     */   public void setIdModule(int idModule) {
/*  50 */     this.idModule = idModule;
/*     */   }
/*     */   
/*     */   public Timestamp getReceived() {
/*  54 */     return this.received;
/*     */   }
/*     */   
/*     */   public void setReceived(Timestamp received) {
/*  58 */     this.received = received;
/*     */   }
/*     */   
/*     */   public byte[] getContent() {
/*  62 */     return this.content;
/*     */   }
/*     */   
/*     */   public void setContent(byte[] content) {
/*  66 */     this.content = content;
/*     */   }
/*     */   
/*     */   public short getVersion() {
/*  70 */     return this.version;
/*     */   }
/*     */   
/*     */   public void setVersion(short version) {
/*  74 */     this.version = version;
/*     */   }
/*     */   
/*     */   public short getLastCommInterface() {
/*  78 */     return this.lastCommInterface;
/*     */   }
/*     */   
/*     */   public void setLastCommInterface(short lastCommInterface) {
/*  82 */     this.lastCommInterface = lastCommInterface;
/*     */   }
/*     */   
/*     */   public short getMinGsmSignalLevel() {
/*  86 */     return this.minGsmSignalLevel;
/*     */   }
/*     */   
/*     */   public void setMinGsmSignalLevel(short minGsmSignalLevel) {
/*  90 */     this.minGsmSignalLevel = minGsmSignalLevel;
/*     */   }
/*     */   
/*     */   public String getLastNWProtocol() {
/*  94 */     return this.lastNWProtocol;
/*     */   }
/*     */   
/*     */   public void setLastNWProtocol(String lastNWProtocol) {
/*  98 */     this.lastNWProtocol = lastNWProtocol;
/*     */   }
/*     */   
/*     */   public int getTimezone() {
/* 102 */     return this.timezone;
/*     */   }
/*     */   
/*     */   public void setTimezone(int timezone) {
/* 106 */     this.timezone = timezone;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dto\PendingDataHolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */