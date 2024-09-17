/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.util.Date;
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
/*    */ public class InfoSms
/*    */ {
/*    */   private int _id;
/*    */   private String _status;
/*    */   private String _sender;
/*    */   private String _senderName;
/* 22 */   private Date _timeStamp = new Date(0L);
/*    */   private String _content;
/*    */   
/*    */   public final int getid() {
/* 26 */     return this._id;
/*    */   }
/*    */   
/*    */   public final void setid(int v) {
/* 30 */     this._id = v;
/*    */   }
/*    */   
/*    */   public final String getstatus() {
/* 34 */     return this._status;
/*    */   }
/*    */   
/*    */   public final void setstatus(String v) {
/* 38 */     this._status = v;
/*    */   }
/*    */   
/*    */   public final String getsender() {
/* 42 */     return this._sender;
/*    */   }
/*    */   
/*    */   public final void setsender(String v) {
/* 46 */     this._sender = v;
/*    */   }
/*    */   
/*    */   public final String getsenderName() {
/* 50 */     return this._senderName;
/*    */   }
/*    */   
/*    */   public final void setsenderName(String v) {
/* 54 */     this._senderName = v;
/*    */   }
/*    */   
/*    */   public final Date gettimeStamp() {
/* 58 */     return this._timeStamp;
/*    */   }
/*    */   
/*    */   public final void settimeStamp(Date v) {
/* 62 */     this._timeStamp = v;
/*    */   }
/*    */   
/*    */   public final String getcontent() {
/* 66 */     return this._content;
/*    */   }
/*    */   
/*    */   public final void setcontent(String v) {
/* 70 */     this._content = v;
/*    */   }
/*    */   
/*    */   public InfoSms(int id, String status, String sender, String senderName, Date timeStamp, String content) {
/* 74 */     this._id = id;
/* 75 */     this._status = status;
/* 76 */     this._sender = sender;
/* 77 */     this._senderName = senderName;
/* 78 */     this._timeStamp = timeStamp;
/* 79 */     this._content = content;
/*    */   }
/*    */ 
/*    */   
/*    */   public String toString() {
/* 84 */     StringBuilder sb = new StringBuilder();
/* 85 */     sb.append("\r\n").append("ID: ").append(this._id).append("\r\n");
/* 86 */     sb.append("STATUS: ").append(this._status).append("\r\n");
/* 87 */     sb.append("SENDER: ").append(this._sender).append("\r\n");
/* 88 */     sb.append("SENDER NAME: ").append(this._senderName).append("\r\n");
/* 89 */     sb.append("DATE: ").append(this._timeStamp).append("\r\n");
/* 90 */     sb.append("CONTENT: ").append(this._content).append("\r\n");
/* 91 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\InfoSms.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */