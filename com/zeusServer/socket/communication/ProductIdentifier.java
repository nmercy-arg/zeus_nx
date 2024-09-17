/*     */ package com.zeusServer.socket.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.griffon.GriffonHandler;
/*     */ import com.zeusServer.mercurius.MercuriusHandler;
/*     */ import com.zeusServer.pegasus.PegasusV1Handler;
/*     */ import com.zeusServer.pegasus.PegasusV2Handler;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.Main;
/*     */ import com.zeusServer.util.SocketFunctions;
/*     */ import java.net.Socket;
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
/*     */ public class ProductIdentifier
/*     */   implements Runnable
/*     */ {
/*     */   private Socket sock;
/*     */   private boolean dataServerRunning;
/*     */   private boolean isFromWatchDogOldConnection;
/*  39 */   private byte[] watchdogProdId = new byte[2];
/*     */ 
/*     */   
/*     */   public ProductIdentifier(Socket sock, boolean dataServerRunning, boolean isFromWatchDogOldConnection, byte[] watchdogProdId) {
/*  43 */     this.sock = sock;
/*  44 */     this.dataServerRunning = dataServerRunning;
/*  45 */     this.isFromWatchDogOldConnection = isFromWatchDogOldConnection;
/*  46 */     if (watchdogProdId != null) {
/*  47 */       System.arraycopy(watchdogProdId, 0, this.watchdogProdId, 0, 2);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  54 */       Thread.currentThread().setPriority(1);
/*  55 */       int counter = 0;
/*     */       
/*     */       while (true) {
/*  58 */         if (this.sock.getInputStream().available() > 0 || this.isFromWatchDogOldConnection) {
/*     */           byte[] prod;
/*  60 */           if (this.isFromWatchDogOldConnection && this.watchdogProdId != null) {
/*  61 */             prod = new byte[2];
/*  62 */             System.arraycopy(this.watchdogProdId, 0, prod, 0, 2);
/*     */           } else {
/*  64 */             prod = Functions.swapLSB2MSB(SocketFunctions.receive(this.sock, 0, 2));
/*     */           } 
/*  66 */           if (prod[1] == 84 && prod[0] == 83) {
/*  67 */             if (this.sock.getInputStream().available() >= 1 && 
/*  68 */               SocketFunctions.receive(this.sock, 0, 1)[0] == 84) {
/*  69 */               Runnable r = new ChkOtherIfaceHandler(this.sock, null, null, "TCP");
/*  70 */               (Main.getTCPDataServer()).chkOIfaceHandler.execute(r);
/*     */               break;
/*     */             } 
/*     */             continue;
/*     */           } 
/*  75 */           String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/*  76 */           short enc = Short.parseShort(prodBin.substring(0, 2), 2);
/*  77 */           prodBin = prodBin.substring(2);
/*  78 */           prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/*  79 */           short productID = Short.parseShort(prodBin, 2);
/*  80 */           int len = this.sock.getInputStream().available() + 2;
/*  81 */           if (productID == Util.EnumProductIDs.PEGASUS.getProductId() && len != 17) {
/*  82 */             if (TblPegasusActiveConnections.numberOfPendingIdentificationPackets.get() < 2500 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  83 */               PegasusV2Handler pv2 = new PegasusV2Handler(this.sock, enc, prod);
/*  84 */               (Main.getTCPDataServer()).dataWorkers.execute((Runnable)pv2); break;
/*     */             } 
/*  86 */             checkAndCloseConnection(prod);
/*     */             break;
/*     */           } 
/*  89 */           if ((productID == Util.EnumProductIDs.GRIFFON_V1.getProductId() || productID == Util.EnumProductIDs.GRIFFON_V2.getProductId()) && len != 17) {
/*  90 */             if (TblGriffonActiveConnections.numberOfPendingIdentificationPackets.get() < 2500 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  91 */               GriffonHandler gh = new GriffonHandler(this.sock, productID, enc, prod);
/*  92 */               (Main.getTCPDataServer()).dataWorkers.execute((Runnable)gh); break;
/*     */             } 
/*  94 */             checkAndCloseConnection(prod);
/*     */             break;
/*     */           } 
/*  97 */           if (productID == Util.EnumProductIDs.MERCURIUS.getProductId() && len != 17) {
/*  98 */             if (TblMercuriusActiveConnections.numberOfPendingIdentificationPackets.get() < 2500 && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.RESTORE && GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM) {
/*  99 */               MercuriusHandler avlH = new MercuriusHandler(this.sock, enc, prod);
/* 100 */               (Main.getTCPDataServer()).dataWorkers.execute((Runnable)avlH); break;
/*     */             } 
/* 102 */             checkAndCloseConnection(prod);
/*     */             break;
/*     */           } 
/* 105 */           if (len == 17) {
/* 106 */             if (TblPegasusActiveConnections.numberOfPendingIdentificationPackets.get() < 2500) {
/* 107 */               PegasusV1Handler pv1 = new PegasusV1Handler(this.sock, Functions.swapLSB2MSB(prod));
/* 108 */               (Main.getTCPDataServer()).dataWorkers.execute((Runnable)pv1); break;
/*     */             } 
/* 110 */             checkAndCloseConnection(prod);
/*     */             
/*     */             break;
/*     */           } 
/* 114 */           if (this.sock != null) {
/* 115 */             this.sock.close();
/*     */           }
/*     */           
/*     */           break;
/*     */         } 
/*     */         
/* 121 */         if (counter++ > 30) {
/* 122 */           if (this.sock != null) {
/* 123 */             this.sock.close();
/*     */           }
/* 125 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, " Identifier not able to Read DATA in 30 Attempts ........  IP : " + this.sock.getRemoteSocketAddress().toString(), Enums.EnumMessagePriority.AVERAGE, null, null);
/*     */           break;
/*     */         } 
/* 128 */         Thread.sleep(2000L);
/*     */       }
/*     */     
/*     */     }
/* 132 */     catch (InterruptedException interruptedException) {
/*     */     
/* 134 */     } catch (Exception ex) {
/* 135 */       Logger.getLogger(ProductIdentifier.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void checkAndCloseConnection(byte[] prod) throws Exception {
/* 140 */     if (this.dataServerRunning && GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.NORMAL) {
/* 141 */       String lip = this.sock.getRemoteSocketAddress().toString();
/* 142 */       lip = lip.substring(1, lip.indexOf(":"));
/* 143 */       if (TCPDataServer.ips.contains(lip)) {
/* 144 */         Thread.sleep(2000L);
/* 145 */         Runnable r = new ProductIdentifier(this.sock, this.dataServerRunning, true, prod);
/* 146 */         TCPDataServer.serverHelper.execute(r);
/*     */       } else {
/* 148 */         this.sock.shutdownInput();
/* 149 */         this.sock.shutdownOutput();
/* 150 */         this.sock.close();
/*     */       }
/*     */     
/* 153 */     } else if (this.dataServerRunning || GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.NORMAL) {
/* 154 */       String lip = this.sock.getRemoteSocketAddress().toString();
/* 155 */       lip = lip.substring(1, lip.indexOf(":"));
/* 156 */       if (TCPDataServer.ips.contains(lip)) {
/* 157 */         WatchdogBusyHandler busyHandler = new WatchdogBusyHandler(this.sock);
/* 158 */         Thread busyThread = new Thread(busyHandler);
/* 159 */         busyThread.setName("WatchdogBusyHandler");
/* 160 */         busyThread.setDaemon(true);
/* 161 */         busyThread.start();
/*     */       } else {
/* 163 */         this.sock.shutdownInput();
/* 164 */         this.sock.shutdownOutput();
/* 165 */         this.sock.close();
/*     */       } 
/*     */     } else {
/* 168 */       this.sock.shutdownInput();
/* 169 */       this.sock.shutdownOutput();
/* 170 */       this.sock.close();
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\ProductIdentifier.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */