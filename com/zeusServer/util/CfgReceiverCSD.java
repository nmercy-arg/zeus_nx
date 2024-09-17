/*    */ package com.zeusServer.util;
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
/*    */ 
/*    */ public class CfgReceiverCSD
/*    */ {
/*    */   private String _serialPort;
/*    */   private String _pin;
/*    */   private Enums.EnumGsmBand _gsmBand;
/*    */   
/*    */   public final String getSerialPort() {
/* 25 */     return this._serialPort;
/*    */   }
/*    */   
/*    */   public final String getPin() {
/* 29 */     return this._pin;
/*    */   }
/*    */   
/*    */   public final Enums.EnumGsmBand getGsmBand() {
/* 33 */     return this._gsmBand;
/*    */   }
/*    */   
/*    */   public CfgReceiverCSD(String cfg) {
/* 37 */     String[] rcvrCSD = cfg.split(",");
/* 38 */     this._serialPort = rcvrCSD[0];
/* 39 */     this._pin = rcvrCSD[1];
/* 40 */     this._gsmBand = Enums.EnumGsmBand.getGsmBandFromString(rcvrCSD[2]);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\CfgReceiverCSD.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */