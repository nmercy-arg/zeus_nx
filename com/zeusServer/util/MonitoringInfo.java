/*     */ package com.zeusServer.util;
/*     */ 
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ConcurrentHashMap;
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
/*     */ public class MonitoringInfo
/*     */ {
/*     */   private byte _deleteEventAfterTransmission;
/*     */   private byte _beepAfterEventTransmission;
/*     */   private Integer _receiverNumber;
/*     */   private Integer _receiverGroup;
/*     */   private Integer _receiverLine;
/*     */   private String _receiverSerialPort;
/*     */   private Integer _receiverTimeout;
/*     */   private Integer _receiverBaudrate;
/*     */   private Integer _receiverDatabits;
/*     */   private Integer _receiverStopbits;
/*     */   private Integer _receiverParity;
/*     */   private String _monitoringSoftwareIP;
/*     */   private Integer _monitoringSoftwarePort;
/*     */   private Integer _eventsTimeGap;
/*     */   private byte _waitEventAck;
/*     */   private int _ackByte;
/*     */   private Integer _receiverType;
/*     */   private Integer _valueAddedPartition;
/*     */   private Integer _partitionScheme;
/*     */   private String _selfTestEvent;
/*     */   private Integer _selfTestFrequency;
/*  41 */   public long lastTransmissionEventAutoTest = 0L;
/*     */   
/*     */   private byte _enableHeartbeat;
/*     */   private String _heartBeatData;
/*     */   private Integer _heartBeatFrequency;
/*     */   private Integer _commRetries;
/*     */   private ConcurrentHashMap<String, List<MonitoringGroupInfo>> assignedGroupsByProduct;
/*     */   
/*     */   public final boolean getEnableHeartbeat() {
/*  50 */     return (this._enableHeartbeat > 0);
/*     */   }
/*     */   
/*     */   public final String getHeartBeatData() {
/*  54 */     return this._heartBeatData;
/*     */   }
/*     */   
/*     */   public final Integer getHeartBeatFrequency() {
/*  58 */     return this._heartBeatFrequency;
/*     */   }
/*     */   
/*     */   public final boolean getDeleteEventAfterTransmission() {
/*  62 */     return (this._deleteEventAfterTransmission > 0);
/*     */   }
/*     */   
/*     */   public final boolean getBeepAfterEventTransmission() {
/*  66 */     return (this._beepAfterEventTransmission > 0);
/*     */   }
/*     */   
/*     */   public void setDeleteEventAfterTransmission(byte _deleteEventAfterTransmission) {
/*  70 */     this._deleteEventAfterTransmission = _deleteEventAfterTransmission;
/*     */   }
/*     */   
/*     */   public void setBeepAfterEventTransmission(byte _beepAfterEventTransmission) {
/*  74 */     this._beepAfterEventTransmission = _beepAfterEventTransmission;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverNumber() {
/*  78 */     return this._receiverNumber;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverGroup() {
/*  82 */     return this._receiverGroup;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverLine() {
/*  86 */     return this._receiverLine;
/*     */   }
/*     */   
/*     */   public final String getReceiverSerialPort() {
/*  90 */     return this._receiverSerialPort;
/*     */   }
/*     */   
/*     */   public void setEnableHeartbeat(byte _enableHeartbeat) {
/*  94 */     this._enableHeartbeat = _enableHeartbeat;
/*     */   }
/*     */   
/*     */   public void setHeartBeatData(String _heartBeatData) {
/*  98 */     this._heartBeatData = _heartBeatData;
/*     */   }
/*     */   
/*     */   public void setHeartBeatFrequency(Integer _heartBeatFrequency) {
/* 102 */     this._heartBeatFrequency = _heartBeatFrequency;
/*     */   }
/*     */   
/*     */   public void setReceiverNumber(Integer _receiverNumber) {
/* 106 */     this._receiverNumber = _receiverNumber;
/*     */   }
/*     */   
/*     */   public void setReceiverGroup(Integer _receiverGroup) {
/* 110 */     this._receiverGroup = _receiverGroup;
/*     */   }
/*     */   
/*     */   public void setReceiverLine(Integer _receiverLine) {
/* 114 */     this._receiverLine = _receiverLine;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverTimeout() {
/* 118 */     return this._receiverTimeout;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverBaudrate() {
/* 122 */     return this._receiverBaudrate;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverDatabits() {
/* 126 */     return this._receiverDatabits;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverStopbits() {
/* 130 */     return this._receiverStopbits;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverParity() {
/* 134 */     return this._receiverParity;
/*     */   }
/*     */   
/*     */   public final String getMonitoringSoftwareIP() {
/* 138 */     return this._monitoringSoftwareIP;
/*     */   }
/*     */   
/*     */   public final Integer getMonitoringSoftwarePort() {
/* 142 */     return this._monitoringSoftwarePort;
/*     */   }
/*     */   
/*     */   public final Integer getEventsTimeGap() {
/* 146 */     return this._eventsTimeGap;
/*     */   }
/*     */   
/*     */   public final boolean getWaitEventAck() {
/* 150 */     return (this._waitEventAck > 0);
/*     */   }
/*     */   
/*     */   public final int getAckByte() {
/* 154 */     return this._ackByte;
/*     */   }
/*     */   
/*     */   public final Integer getReceiverType() {
/* 158 */     return this._receiverType;
/*     */   }
/*     */   
/*     */   public final Integer getValueAddedPartition() {
/* 162 */     return this._valueAddedPartition;
/*     */   }
/*     */   
/*     */   public final Integer getPartitionScheme() {
/* 166 */     return this._partitionScheme;
/*     */   }
/*     */   
/*     */   public void setReceiverSerialPort(String _receiverSerialPort) {
/* 170 */     this._receiverSerialPort = _receiverSerialPort;
/*     */   }
/*     */   
/*     */   public void setReceiverTimeout(Integer _receiverTimeout) {
/* 174 */     this._receiverTimeout = _receiverTimeout;
/*     */   }
/*     */   
/*     */   public void setReceiverBaudrate(Integer _receiverBaudrate) {
/* 178 */     this._receiverBaudrate = _receiverBaudrate;
/*     */   }
/*     */   
/*     */   public void setReceiverDatabits(Integer _receiverDatabits) {
/* 182 */     this._receiverDatabits = _receiverDatabits;
/*     */   }
/*     */   
/*     */   public void setReceiverStopbits(Integer _receiverStopbits) {
/* 186 */     this._receiverStopbits = _receiverStopbits;
/*     */   }
/*     */   
/*     */   public void setReceiverParity(Integer _receiverParity) {
/* 190 */     this._receiverParity = _receiverParity;
/*     */   }
/*     */   
/*     */   public void setMonitoringSoftwareIP(String _monitoringSoftwareIP) {
/* 194 */     this._monitoringSoftwareIP = _monitoringSoftwareIP;
/*     */   }
/*     */   
/*     */   public void setMonitoringSoftwarePort(Integer _monitoringSoftwarePort) {
/* 198 */     this._monitoringSoftwarePort = _monitoringSoftwarePort;
/*     */   }
/*     */   
/*     */   public void setEventsTimeGap(Integer _eventsTimeGap) {
/* 202 */     this._eventsTimeGap = _eventsTimeGap;
/*     */   }
/*     */   
/*     */   public void setWaitEventAck(byte _waitEventAck) {
/* 206 */     this._waitEventAck = _waitEventAck;
/*     */   }
/*     */   
/*     */   public void setAckByte(int _ackByte) {
/* 210 */     this._ackByte = _ackByte;
/*     */   }
/*     */   
/*     */   public void setReceiverType(Integer _receiverType) {
/* 214 */     this._receiverType = _receiverType;
/*     */   }
/*     */   
/*     */   public void setValueAddedPartition(Integer _valueAddedPartition) {
/* 218 */     this._valueAddedPartition = _valueAddedPartition;
/*     */   }
/*     */   
/*     */   public void setPartitionScheme(Integer _partitionScheme) {
/* 222 */     this._partitionScheme = _partitionScheme;
/*     */   }
/*     */   
/*     */   public long getLastTransmissionEventAutoTest() {
/* 226 */     return this.lastTransmissionEventAutoTest;
/*     */   }
/*     */   
/*     */   public void setLastTransmissionEventAutoTest(long lastTransmissionEventAutoTest) {
/* 230 */     this.lastTransmissionEventAutoTest = lastTransmissionEventAutoTest;
/*     */   }
/*     */   
/*     */   public ConcurrentHashMap<String, List<MonitoringGroupInfo>> getAssignedGroupsByProduct() {
/* 234 */     return this.assignedGroupsByProduct;
/*     */   }
/*     */   
/*     */   public void setAssignedGroupsByProduct(ConcurrentHashMap<String, List<MonitoringGroupInfo>> assignedGroupsByProduct) {
/* 238 */     this.assignedGroupsByProduct = assignedGroupsByProduct;
/*     */   }
/*     */   
/*     */   public String getSelfTestEvent() {
/* 242 */     return this._selfTestEvent;
/*     */   }
/*     */   
/*     */   public void setSelfTestEvent(String _selfTestEvent) {
/* 246 */     this._selfTestEvent = _selfTestEvent;
/*     */   }
/*     */   
/*     */   public Integer getSelfTestFrequency() {
/* 250 */     return this._selfTestFrequency;
/*     */   }
/*     */   
/*     */   public void setSelfTestFrequency(Integer _selfTestFrequency) {
/* 254 */     this._selfTestFrequency = _selfTestFrequency;
/*     */   }
/*     */   
/*     */   public Integer getCommRetries() {
/* 258 */     return this._commRetries;
/*     */   }
/*     */   
/*     */   public void setCommRetries(Integer _commRetries) {
/* 262 */     this._commRetries = _commRetries;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\MonitoringInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */