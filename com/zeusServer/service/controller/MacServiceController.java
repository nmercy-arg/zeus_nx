/*    */ package com.zeusServer.service.controller;
/*    */ 
/*    */ import org.hyperic.sigar.win32.Service;
/*    */ import org.hyperic.sigar.win32.Win32Exception;
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
/*    */ public class MacServiceController
/*    */   implements ServiceController
/*    */ {
/*    */   private Service service;
/*    */   private String sName;
/*    */   
/*    */   public MacServiceController(String sName) throws Win32Exception {
/* 24 */     this.sName = sName;
/* 25 */     if (this.service == null) {
/* 26 */       this.service = new Service(sName);
/*    */     }
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean startService() throws Win32Exception {
/* 32 */     if (this.service == null) {
/* 33 */       this.service = new Service(this.sName);
/*    */     }
/*    */     try {
/* 36 */       this.service.start();
/* 37 */       return true;
/* 38 */     } catch (Win32Exception ex) {
/* 39 */       ex.printStackTrace();
/* 40 */       return false;
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean stopService() throws Win32Exception {
/* 46 */     if (this.service == null) {
/* 47 */       this.service = new Service(this.sName);
/*    */     }
/*    */     try {
/* 50 */       this.service.stop();
/* 51 */       return true;
/* 52 */     } catch (Win32Exception ex) {
/* 53 */       ex.printStackTrace();
/* 54 */       return false;
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public int getServiceStatus() throws Win32Exception {
/* 60 */     if (this.service == null) {
/* 61 */       this.service = new Service(this.sName);
/*    */     }
/*    */     try {
/* 64 */       return this.service.getStatus();
/* 65 */     } catch (Exception ex) {
/* 66 */       ex.printStackTrace();
/* 67 */       return -1;
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\service\controller\MacServiceController.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */