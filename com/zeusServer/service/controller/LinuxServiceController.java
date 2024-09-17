/*    */ package com.zeusServer.service.controller;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.InputStreamReader;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
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
/*    */ public class LinuxServiceController
/*    */   implements ServiceController
/*    */ {
/*    */   private String sName;
/*    */   
/*    */   public LinuxServiceController(String sName) {
/* 23 */     this.sName = sName;
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean startService() {
/* 28 */     return (runCommand(" service " + this.sName + " start ", 1) == 0);
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean stopService() {
/* 33 */     return (runCommand(" service " + this.sName + " stop ", 2) == 0);
/*    */   }
/*    */ 
/*    */   
/*    */   public int getServiceStatus() {
/* 38 */     return runCommand(" service " + this.sName + " status ", 3);
/*    */   } private int runCommand(String cmd, int cmdType) {
/*    */     try {
/*    */       BufferedReader br;
/*    */       String line;
/* 43 */       Process pr = Runtime.getRuntime().exec(cmd);
/* 44 */       pr.waitFor();
/* 45 */       switch (cmdType) {
/*    */         case 1:
/*    */         case 2:
/* 48 */           return pr.exitValue();
/*    */         case 3:
/* 50 */           br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
/*    */           
/* 52 */           while ((line = br.readLine()) != null) {
/* 53 */             if (line.toUpperCase().contains("IS RUNNING"))
/* 54 */               return 4; 
/* 55 */             if (line.toUpperCase().contains("IS STOPPED")) {
/* 56 */               return 1;
/*    */             }
/*    */           } 
/*    */           break;
/*    */       } 
/* 61 */     } catch (InterruptedException|java.io.IOException ex) {
/* 62 */       Logger.getLogger(LinuxServiceController.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } 
/* 64 */     return -1;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\service\controller\LinuxServiceController.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */