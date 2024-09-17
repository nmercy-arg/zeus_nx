/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import jcifs.smb.SmbException;
/*    */ import jcifs.smb.SmbFile;
/*    */ import jcifs.smb.SmbFilenameFilter;
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
/*    */ public class NetworkZipFilter
/*    */   implements SmbFilenameFilter
/*    */ {
/*    */   public boolean accept(SmbFile sf, String name) throws SmbException {
/* 23 */     return name.endsWith(".zip");
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\NetworkZipFilter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */