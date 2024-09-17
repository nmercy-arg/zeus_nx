/*    */ package com.zeusServer.box;
/*    */ 
/*    */ import java.util.Date;
/*    */ import java.util.List;
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
/*    */ public class ZeusBoxEvents
/*    */ {
/*    */   private List<Byte> eventData;
/*    */   private Date receivedDate;
/*    */   private Long time;
/*    */   
/*    */   public ZeusBoxEvents(List<Byte> eventData, Date receivedDate, boolean flag) {
/* 27 */     this.eventData = eventData;
/* 28 */     this.receivedDate = receivedDate;
/* 29 */     if (flag) {
/* 30 */       int size = this.eventData.size();
/* 31 */       byte[] b = new byte[8];
/* 32 */       int index = 0;
/* 33 */       for (int i = size - 8; i < size; i++) {
/* 34 */         b[index++] = ((Byte)this.eventData.get(i)).byteValue();
/*    */       }
/* 36 */       String time = new String(b);
/* 37 */       int hour = Integer.parseInt(time.substring(0, 2));
/* 38 */       int minute = Integer.parseInt(time.substring(3, 5));
/* 39 */       int seconds = Integer.parseInt(time.substring(6));
/* 40 */       this.time = Long.valueOf((hour * 60 * 60 + minute * 60 + seconds) * 1000L);
/*    */     } 
/*    */   }
/*    */   
/*    */   public List<Byte> getEventData() {
/* 45 */     return this.eventData;
/*    */   }
/*    */   
/*    */   public void setEventData(List<Byte> eventData) {
/* 49 */     this.eventData = eventData;
/*    */   }
/*    */   
/*    */   public Date getReceivedDate() {
/* 53 */     return this.receivedDate;
/*    */   }
/*    */   
/*    */   public void setReceivedDate(Date receivedDate) {
/* 57 */     this.receivedDate = receivedDate;
/*    */   }
/*    */   
/*    */   public Long getTime() {
/* 61 */     return this.time;
/*    */   }
/*    */   
/*    */   public void setTime(Long time) {
/* 65 */     this.time = time;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\box\ZeusBoxEvents.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */