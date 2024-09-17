/*    */ package com.zeusServer.ui;
/*    */ 
/*    */ import com.zeusServer.serialPort.communication.PrinterFunctions;
/*    */ import com.zeusServer.tblConnections.TblPrinterSpool;
/*    */ import com.zeusServer.util.Functions;
/*    */ import com.zeusServer.util.Main;
/*    */ import com.zeusServer.util.ZeusServerCfg;
/*    */ import java.util.concurrent.ScheduledFuture;
/*    */ import java.util.concurrent.TimeUnit;
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
/*    */ public class EventViewer
/*    */ {
/*    */   private ScheduledFuture events2SPSF;
/*    */   
/*    */   public EventViewer() {
/*    */     try {
/* 27 */       if (ZeusServerCfg.getInstance().getEnableSerialPrinter()) {
/* 28 */         Runnable events2Printer = new Runnable()
/*    */           {
/*    */             public void run() {
/*    */               try {
/* 32 */                 if (TblPrinterSpool.getInstance().size() > 0 && PrinterFunctions.printerCommPort != null) {
/* 33 */                   synchronized (TblPrinterSpool.getInstance()) {
/* 34 */                     long oldestKey = Functions.getOldestKey(TblPrinterSpool.getInstance().keySet());
/* 35 */                     PrinterFunctions.print(((String)TblPrinterSpool.getInstance().get(Long.valueOf(oldestKey))).concat("\n"));
/* 36 */                     TblPrinterSpool.getInstance().remove(Long.valueOf(oldestKey));
/*    */                   } 
/*    */                 }
/* 39 */               } catch (Exception ex) {
/* 40 */                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */               } 
/*    */             }
/*    */           };
/* 44 */         this.events2SPSF = Functions.addRunnable2ScheduleExecutor(events2Printer, 0L, 250L, TimeUnit.MILLISECONDS);
/*    */       }
/*    */     
/* 47 */     } catch (Exception ex) {
/* 48 */       Logger.getLogger(EventViewer.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\EventViewer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */