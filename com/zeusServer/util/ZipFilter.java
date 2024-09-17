/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FilenameFilter;
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
/*    */ public class ZipFilter
/*    */   implements FilenameFilter
/*    */ {
/*    */   public boolean accept(File dir, String name) {
/* 22 */     return name.endsWith(".zip");
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\ZipFilter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */