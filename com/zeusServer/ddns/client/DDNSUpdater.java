/*    */ package com.zeusServer.ddns.client;
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
/*    */ public abstract class DDNSUpdater
/*    */ {
/*    */   protected String name;
/*    */   protected String[] settings;
/*    */   
/*    */   public DDNSUpdater(String[] settings) {
/* 21 */     this.settings = settings;
/*    */   }
/*    */   
/*    */   protected abstract boolean update(String paramString) throws Exception;
/*    */   
/*    */   protected abstract String getDDNSProviderName();
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\ddns\client\DDNSUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */