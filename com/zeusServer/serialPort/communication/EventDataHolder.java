/*    */ package com.zeusServer.serialPort.communication;
/*    */ 
/*    */ import java.util.Calendar;
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
/*    */ public class EventDataHolder
/*    */   implements Comparable<Object>
/*    */ {
/*    */   private int id_Event;
/*    */   private short event_Protocol;
/*    */   private short transmission_Retries;
/*    */   private Calendar received;
/*    */   private String eventData;
/*    */   private byte[] eventBuffer;
/*    */   private int idGroup;
/*    */   private int productId;
/*    */   
/*    */   public int getId_Event() {
/* 29 */     return this.id_Event;
/*    */   }
/*    */   
/*    */   public void setId_Event(int id_Event) {
/* 33 */     this.id_Event = id_Event;
/*    */   }
/*    */   
/*    */   public short getEvent_Protocol() {
/* 37 */     return this.event_Protocol;
/*    */   }
/*    */   
/*    */   public void setEvent_Protocol(short event_Protocol) {
/* 41 */     this.event_Protocol = event_Protocol;
/*    */   }
/*    */   
/*    */   public short getTransmission_Retries() {
/* 45 */     return this.transmission_Retries;
/*    */   }
/*    */   
/*    */   public void setTransmission_Retries(short transmission_Retries) {
/* 49 */     this.transmission_Retries = transmission_Retries;
/*    */   }
/*    */   
/*    */   public Calendar getReceived() {
/* 53 */     return this.received;
/*    */   }
/*    */   
/*    */   public void setReceived(Calendar received) {
/* 57 */     this.received = received;
/*    */   }
/*    */   
/*    */   public String getEventData() {
/* 61 */     return this.eventData;
/*    */   }
/*    */   
/*    */   public void setEventData(String eventData) {
/* 65 */     this.eventData = eventData;
/*    */   }
/*    */   
/*    */   public void setEventBuffer(byte[] eventBuffer) {
/* 69 */     this.eventBuffer = eventBuffer;
/*    */   }
/*    */   
/*    */   public byte[] getEventBuffer() {
/* 73 */     return this.eventBuffer;
/*    */   }
/*    */   
/*    */   public int getIdGroup() {
/* 77 */     return this.idGroup;
/*    */   }
/*    */   
/*    */   public void setIdGroup(int idGroup) {
/* 81 */     this.idGroup = idGroup;
/*    */   }
/*    */   
/*    */   public int getProductId() {
/* 85 */     return this.productId;
/*    */   }
/*    */   
/*    */   public void setProductId(int productId) {
/* 89 */     this.productId = productId;
/*    */   }
/*    */ 
/*    */   
/*    */   public int compareTo(Object o) {
/* 94 */     return 0;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EventDataHolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */