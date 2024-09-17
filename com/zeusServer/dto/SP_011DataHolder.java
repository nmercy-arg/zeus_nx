/*     */ package com.zeusServer.dto;
/*     */ 
/*     */ import java.util.Calendar;
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
/*     */ public class SP_011DataHolder
/*     */ {
/*     */   private int id_Client;
/*     */   private String name;
/*     */   private String client_Code;
/*     */   private int id_Module;
/*     */   private int id_Group;
/*     */   private int id_Occurrence;
/*     */   private short occurrence_Type;
/*     */   private Calendar occurred;
/*     */   private Calendar notified;
/*     */   private Calendar acknowledged;
/*     */   private Calendar terminated;
/*     */   private String event_Code;
/*     */   private int event_Freq;
/*     */   private int event_Desc;
/*     */   private int versionRcvd;
/*     */   private String nwProtocol;
/*     */   
/*     */   public int getId_Client() {
/*  39 */     return this.id_Client;
/*     */   }
/*     */   
/*     */   public void setId_Client(int id_Client) {
/*  43 */     this.id_Client = id_Client;
/*     */   }
/*     */   
/*     */   public String getName() {
/*  47 */     return this.name;
/*     */   }
/*     */   
/*     */   public void setName(String name) {
/*  51 */     this.name = name;
/*     */   }
/*     */   
/*     */   public String getClient_Code() {
/*  55 */     return this.client_Code;
/*     */   }
/*     */   
/*     */   public void setClient_Code(String client_Code) {
/*  59 */     this.client_Code = client_Code;
/*     */   }
/*     */   
/*     */   public int getId_Module() {
/*  63 */     return this.id_Module;
/*     */   }
/*     */   
/*     */   public void setId_Module(int id_Module) {
/*  67 */     this.id_Module = id_Module;
/*     */   }
/*     */   
/*     */   public int getId_Group() {
/*  71 */     return this.id_Group;
/*     */   }
/*     */   
/*     */   public void setId_Group(int id_Group) {
/*  75 */     this.id_Group = id_Group;
/*     */   }
/*     */   
/*     */   public int getId_Occurrence() {
/*  79 */     return this.id_Occurrence;
/*     */   }
/*     */   
/*     */   public void setId_Occurrence(int id_Occurrence) {
/*  83 */     this.id_Occurrence = id_Occurrence;
/*     */   }
/*     */   
/*     */   public short getOccurrence_Type() {
/*  87 */     return this.occurrence_Type;
/*     */   }
/*     */   
/*     */   public void setOccurrence_Type(short occurrence_Type) {
/*  91 */     this.occurrence_Type = occurrence_Type;
/*     */   }
/*     */   
/*     */   public Calendar getOccurred() {
/*  95 */     return this.occurred;
/*     */   }
/*     */   
/*     */   public void setOccurred(Calendar occurred) {
/*  99 */     this.occurred = occurred;
/*     */   }
/*     */   
/*     */   public Calendar getNotified() {
/* 103 */     return this.notified;
/*     */   }
/*     */   
/*     */   public void setNotified(Calendar notified) {
/* 107 */     this.notified = notified;
/*     */   }
/*     */   
/*     */   public Calendar getAcknowledged() {
/* 111 */     return this.acknowledged;
/*     */   }
/*     */   
/*     */   public void setAcknowledged(Calendar acknowledged) {
/* 115 */     this.acknowledged = acknowledged;
/*     */   }
/*     */   
/*     */   public Calendar getTerminated() {
/* 119 */     return this.terminated;
/*     */   }
/*     */   
/*     */   public void setTerminated(Calendar terminated) {
/* 123 */     this.terminated = terminated;
/*     */   }
/*     */   
/*     */   public String getEvent_Code() {
/* 127 */     return this.event_Code;
/*     */   }
/*     */   
/*     */   public void setEvent_Code(String event_Code) {
/* 131 */     this.event_Code = event_Code;
/*     */   }
/*     */   
/*     */   public int getEvent_Freq() {
/* 135 */     return this.event_Freq;
/*     */   }
/*     */   
/*     */   public void setEvent_Freq(int event_Freq) {
/* 139 */     this.event_Freq = event_Freq;
/*     */   }
/*     */   
/*     */   public int getVersionRcvd() {
/* 143 */     return this.versionRcvd;
/*     */   }
/*     */   
/*     */   public void setVersionRcvd(int versionRcvd) {
/* 147 */     this.versionRcvd = versionRcvd;
/*     */   }
/*     */   
/*     */   public String getNwProtocol() {
/* 151 */     return this.nwProtocol;
/*     */   }
/*     */   
/*     */   public void setNwProtocol(String nwProtocol) {
/* 155 */     this.nwProtocol = nwProtocol;
/*     */   }
/*     */   
/*     */   public int getEvent_Desc() {
/* 159 */     return this.event_Desc;
/*     */   }
/*     */   
/*     */   public void setEvent_Desc(int event_Desc) {
/* 163 */     this.event_Desc = event_Desc;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dto\SP_011DataHolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */