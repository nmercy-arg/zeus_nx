/*     */ package com.zeusServer.serialPort.communication;
/*     */ 
/*     */ import Serialio.SerInputStream;
/*     */ import Serialio.SerialConfig;
/*     */ import Serialio.SerialPortLocal;
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import gnu.io.CommPort;
/*     */ import gnu.io.CommPortIdentifier;
/*     */ import gnu.io.PortInUseException;
/*     */ import gnu.io.SerialPort;
/*     */ import gnu.io.UnsupportedCommOperationException;
/*     */ import java.io.IOException;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SerialPortFunctions
/*     */ {
/*     */   public static SerialPort open(Class receiverClass, String serialPortNumber, int serialBaudRate, int serialDataBit, int stopBits, int parityBit) {
/*  38 */     SerialPort serialPort = null;
/*     */     
/*     */     try {
/*  41 */       CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortNumber);
/*     */       
/*  43 */       if (portIdentifier.isCurrentlyOwned()) {
/*  44 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_Serial_Port_is_currently_in_use") + serialPortNumber, Enums.EnumMessagePriority.HIGH, null, null);
/*     */       } else {
/*  46 */         CommPort commPort = portIdentifier.open(receiverClass.getClass().getName(), 2000);
/*  47 */         if (commPort instanceof SerialPort) {
/*  48 */           serialPort = (SerialPort)commPort;
/*  49 */           serialPort.setSerialPortParams(serialBaudRate, serialDataBit, stopBits, parityBit);
/*  50 */           serialPort.setFlowControlMode(0);
/*     */         } 
/*     */       } 
/*  53 */     } catch (UnsupportedCommOperationException|gnu.io.NoSuchPortException ex) {
/*  54 */       initializeRXTXDriver();
/*  55 */     } catch (PortInUseException ex) {
/*  56 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_Serial_Port_is_currently_in_use") + serialPortNumber, Enums.EnumMessagePriority.HIGH, null, null);
/*  57 */       initializeRXTXDriver();
/*     */     } 
/*  59 */     return serialPort;
/*     */   }
/*     */   
/*     */   public static SerialPortLocal openSPL(Class receiverClass, String serialPortNumber, int serialBaudRate, int serialDataBits, int stopBits, int parityBits) {
/*  63 */     SerialPortLocal spl = null;
/*  64 */     SerialConfig cfg = new SerialConfig(serialPortNumber);
/*  65 */     cfg.setBitRate(getSerialIOBaudrate(serialBaudRate));
/*  66 */     cfg.setDataBits(getSerialDataBits(serialDataBits));
/*  67 */     cfg.setStopBits(getSerialStopBits(stopBits));
/*  68 */     cfg.setParity(getSerialParityBits(parityBits));
/*     */     try {
/*  70 */       spl = new SerialPortLocal(cfg);
/*  71 */     } catch (IOException ex) {
/*  72 */       if (spl != null) {
/*     */         
/*  74 */         try { spl.close(); }
/*  75 */         catch (IOException iOException) {  }
/*     */         finally
/*  77 */         { spl = null; }
/*     */       
/*     */       }
/*  80 */     } catch (Exception ex) {
/*  81 */       if (spl != null) {
/*     */         
/*  83 */         try { spl.close(); }
/*  84 */         catch (IOException iOException) {  }
/*     */         finally
/*  86 */         { spl = null; }
/*     */       
/*     */       }
/*     */     } 
/*  90 */     return spl;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] read(SerialPort sp, int offset, int bytes2Read, int timeout) {
/*     */     try {
/*  96 */       long t = System.currentTimeMillis() + timeout;
/*     */       
/*  98 */       while (sp.getInputStream().available() < bytes2Read && t > System.currentTimeMillis()) {
/*  99 */         Thread.sleep(10L);
/*     */       }
/* 101 */       if (sp.getInputStream().available() > 0) {
/* 102 */         byte[] buffer = new byte[(sp.getInputStream().available() < bytes2Read) ? sp.getInputStream().available() : bytes2Read];
/* 103 */         sp.getInputStream().read(buffer, 0, buffer.length);
/* 104 */         return buffer;
/*     */       } 
/* 106 */       return null;
/*     */     }
/* 108 */     catch (IOException ex) {
/* 109 */       Logger.getLogger(SerialPortFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 110 */     } catch (InterruptedException ex) {
/* 111 */       Logger.getLogger(SerialPortFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } finally {
/*     */       try {
/* 114 */         sp.getInputStream().close();
/* 115 */       } catch (IOException ex) {
/* 116 */         Logger.getLogger(SerialPortFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */       } 
/*     */     } 
/* 119 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] readSPL(SerInputStream sis, int offset, int bytes2Read, int timeout) {
/*     */     try {
/* 125 */       long t = System.currentTimeMillis() + timeout;
/*     */       
/* 127 */       while (sis.available() < bytes2Read && t > System.currentTimeMillis()) {
/* 128 */         Thread.sleep(10L);
/*     */       }
/* 130 */       if (sis.available() > 0) {
/* 131 */         byte[] buffer = new byte[(sis.available() < bytes2Read) ? sis.available() : bytes2Read];
/* 132 */         sis.read(buffer, 0, buffer.length);
/* 133 */         return buffer;
/*     */       } 
/* 135 */       return null;
/*     */     }
/* 137 */     catch (InterruptedException interruptedException) {
/*     */     
/* 139 */     } catch (IOException ex) {
/* 140 */       Logger.getLogger(SerialPortFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 141 */     } catch (Exception ex) {
/* 142 */       Logger.getLogger(SerialPortFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } 
/* 144 */     return null;
/*     */   }
/*     */   
/*     */   private static void initializeRXTXDriver() {
/*     */     try {
/* 149 */       CommPortIdentifier.getPortIdentifiers();
/* 150 */     } catch (Exception ex1) {
/* 151 */       Logger.getLogger(SerialPortFunctions.class.getName()).log(Level.SEVERE, (String)null, ex1);
/*     */     } 
/*     */   }
/*     */   
/*     */   public enum EnumSerialIOBaudrates {
/* 156 */     BR_110(110L),
/* 157 */     BR_150(150L),
/* 158 */     BR_300(300L),
/* 159 */     BR_600(600L),
/* 160 */     BR_1200(1200L),
/* 161 */     BR_2400(2400L),
/* 162 */     BR_4800(4800L),
/* 163 */     BR_9600(9600L),
/* 164 */     BR_19200(19200L),
/* 165 */     BR_38400(38400L),
/* 166 */     BR_57600(57600L),
/* 167 */     BR_115200(115200L),
/* 168 */     BR_230400(230400L),
/* 169 */     BR_460800(460800L),
/* 170 */     BR_921600(921600L); private long br;
/*     */     
/*     */     public static EnumSerialIOBaudrates getBaudrateByValue(long br) {
/* 173 */       for (EnumSerialIOBaudrates ebr : values()) {
/* 174 */         if (ebr.br == br) {
/* 175 */           return ebr;
/*     */         }
/*     */       } 
/* 178 */       return null;
/*     */     }
/*     */ 
/*     */     
/*     */     EnumSerialIOBaudrates(long br) {
/* 183 */       this.br = br;
/*     */     }
/*     */     
/*     */     public long getBaudrate() {
/* 187 */       return this.br;
/*     */     }
/*     */   }
/*     */   
/*     */   private static int getSerialIOBaudrate(int serialBaudRate) {
/* 192 */     int sbr = 0;
/* 193 */     switch (EnumSerialIOBaudrates.getBaudrateByValue(serialBaudRate)) {
/*     */       case BR_110:
/* 195 */         sbr = 0;
/*     */         break;
/*     */       case BR_115200:
/* 198 */         sbr = 11;
/*     */         break;
/*     */       case BR_1200:
/* 201 */         sbr = 4;
/*     */         break;
/*     */       case BR_150:
/* 204 */         sbr = 1;
/*     */         break;
/*     */       case BR_19200:
/* 207 */         sbr = 8;
/*     */         break;
/*     */       case BR_230400:
/* 210 */         sbr = 12;
/*     */         break;
/*     */       case BR_2400:
/* 213 */         sbr = 5;
/*     */         break;
/*     */       case BR_300:
/* 216 */         sbr = 2;
/*     */         break;
/*     */       case BR_38400:
/* 219 */         sbr = 9;
/*     */         break;
/*     */       case BR_460800:
/* 222 */         sbr = 13;
/*     */         break;
/*     */       case BR_4800:
/* 225 */         sbr = 6;
/*     */         break;
/*     */       case BR_57600:
/* 228 */         sbr = 10;
/*     */         break;
/*     */       case BR_600:
/* 231 */         sbr = 3;
/*     */         break;
/*     */       case BR_921600:
/* 234 */         sbr = 14;
/*     */         break;
/*     */       case BR_9600:
/* 237 */         sbr = 7;
/*     */         break;
/*     */     } 
/* 240 */     return sbr;
/*     */   }
/*     */   
/*     */   private static int getSerialDataBits(int databits) {
/* 244 */     int db = 0;
/* 245 */     if (databits == 7) {
/* 246 */       databits = 2;
/* 247 */     } else if (databits == 8) {
/* 248 */       databits = 3;
/*     */     } 
/* 250 */     switch (databits) {
/*     */       case 0:
/* 252 */         db = 0;
/*     */         break;
/*     */       case 1:
/* 255 */         db = 1;
/*     */         break;
/*     */       case 2:
/* 258 */         db = 2;
/*     */         break;
/*     */       case 3:
/* 261 */         db = 3;
/*     */         break;
/*     */     } 
/* 264 */     return db;
/*     */   }
/*     */   
/*     */   private static int getSerialStopBits(int stopbits) {
/* 268 */     int sb = 0;
/* 269 */     switch (stopbits - 1) {
/*     */       case 0:
/* 271 */         sb = 0;
/*     */         break;
/*     */       case 1:
/* 274 */         sb = 1;
/*     */         break;
/*     */     } 
/*     */     
/* 278 */     return sb;
/*     */   }
/*     */   
/*     */   private static int getSerialParityBits(int parityBits) {
/* 282 */     int pb = 0;
/* 283 */     switch (parityBits) {
/*     */       case 0:
/* 285 */         pb = 0;
/*     */         break;
/*     */       case 2:
/* 288 */         pb = 1;
/*     */         break;
/*     */       case 1:
/* 291 */         pb = 2;
/*     */         break;
/*     */       case 3:
/* 294 */         pb = 3;
/*     */         break;
/*     */       
/*     */       case 4:
/* 298 */         pb = 4;
/*     */         break;
/*     */     } 
/* 301 */     return pb;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\SerialPortFunctions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */