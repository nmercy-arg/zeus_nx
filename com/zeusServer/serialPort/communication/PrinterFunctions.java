/*    */ package com.zeusServer.serialPort.communication;
/*    */ 
/*    */ import Serialio.SerInputStream;
/*    */ import Serialio.SerOutputStream;
/*    */ import Serialio.SerialPort;
/*    */ import Serialio.SerialPortLocal;
/*    */ import com.zeus.settings.beans.Util;
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.Functions;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import com.zeusServer.util.LocaleMessage;
/*    */ import com.zeusServer.util.ZeusServerCfg;
/*    */ import java.io.IOException;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class PrinterFunctions
/*    */ {
/* 33 */   public static SerialPortLocal printerCommPort = null;
/*    */   protected static SerOutputStream sos;
/*    */   protected static SerInputStream sis;
/*    */   
/*    */   public static void openPrinterSerialPort() {
/* 38 */     String port = ZeusServerCfg.getInstance().getPrinterSerialPort();
/*    */     try {
/* 40 */       printerCommPort = SerialPortFunctions.openSPL(PrinterFunctions.class.getClass(), port, ZeusServerCfg.getInstance().getPrinterBaudrate().intValue(), ZeusServerCfg.getInstance().getPrinterDatabits().intValue(), ZeusServerCfg.getInstance().getPrinterStopbits().intValue(), ZeusServerCfg.getInstance().getPrinterParity().intValue());
/* 41 */       if (printerCommPort != null) {
/* 42 */         printerCommPort.setDTR(true);
/* 43 */         printerCommPort.setRTS(true);
/* 44 */         if (sos == null) {
/* 45 */           sos = new SerOutputStream((SerialPort)printerCommPort);
/*    */         }
/* 47 */         if (sis == null) {
/* 48 */           sis = new SerInputStream((SerialPort)printerCommPort);
/*    */         }
/*    */       } else {
/*    */         
/* 52 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_not_possible_to_open_the_serial_port_[") + port + LocaleMessage.getLocaleMessage("]_for_communication_with_the_serial_printer"), Enums.EnumMessagePriority.HIGH, null, null);
/* 53 */         GlobalVariables.buzzerActivated = true;
/*    */       } 
/* 55 */     } catch (IOException ex) {
/* 56 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_not_possible_to_open_the_serial_port_[") + port + LocaleMessage.getLocaleMessage("]_for_communication_with_the_serial_printer"), Enums.EnumMessagePriority.HIGH, null, null);
/* 57 */       GlobalVariables.buzzerActivated = true;
/*    */     } 
/*    */   }
/*    */   
/*    */   public static void print(String line) {
/* 62 */     if (printerCommPort != null) {
/*    */       try {
/* 64 */         sos.write(line.getBytes());
/* 65 */       } catch (IOException ex) {
/* 66 */         Logger.getLogger(PrinterFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */       } 
/*    */     }
/*    */   }
/*    */   
/*    */   public static void closePrinterSerialPort() {
/* 72 */     if (printerCommPort != null) {
/*    */       try {
/* 74 */         printerCommPort.close();
/* 75 */       } catch (IOException ex) {
/* 76 */         Logger.getLogger(PrinterFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */       } 
/*    */     }
/* 79 */     sos = null;
/* 80 */     sis = null;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\PrinterFunctions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */