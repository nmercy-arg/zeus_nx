/*    */ package com.zeusServer.dto;
/*    */ 
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
/*    */ public class GeofenceBean
/*    */ {
/*    */   private int geofenceIndex;
/*    */   private String geofenceName;
/*    */   private short type;
/*    */   private List<Float> latList;
/*    */   private List<Float> longList;
/*    */   private float radius;
/*    */   
/*    */   public int getGeofenceIndex() {
/* 27 */     return this.geofenceIndex;
/*    */   }
/*    */   
/*    */   public void setGeofenceIndex(int geofenceIndex) {
/* 31 */     this.geofenceIndex = geofenceIndex;
/*    */   }
/*    */   
/*    */   public String getGeofenceName() {
/* 35 */     return this.geofenceName;
/*    */   }
/*    */   
/*    */   public void setGeofenceName(String geofenceName) {
/* 39 */     this.geofenceName = geofenceName;
/*    */   }
/*    */   
/*    */   public short getType() {
/* 43 */     return this.type;
/*    */   }
/*    */   
/*    */   public void setType(short type) {
/* 47 */     this.type = type;
/*    */   }
/*    */   
/*    */   public List<Float> getLatList() {
/* 51 */     return this.latList;
/*    */   }
/*    */   
/*    */   public void setLatList(List<Float> latList) {
/* 55 */     this.latList = latList;
/*    */   }
/*    */   
/*    */   public List<Float> getLongList() {
/* 59 */     return this.longList;
/*    */   }
/*    */   
/*    */   public void setLongList(List<Float> longList) {
/* 63 */     this.longList = longList;
/*    */   }
/*    */   
/*    */   public float getRadius() {
/* 67 */     return this.radius;
/*    */   }
/*    */   
/*    */   public void setRadius(float radius) {
/* 71 */     this.radius = radius;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dto\GeofenceBean.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */