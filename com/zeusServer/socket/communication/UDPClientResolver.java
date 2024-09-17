/*    */ package com.zeusServer.socket.communication;
/*    */ 
/*    */ import com.zeus.settings.beans.Util;
/*    */ import com.zeusServer.griffon.GriffonUdpHandler;
/*    */ import com.zeusServer.mercurius.MercuriusUdpHandler;
/*    */ import com.zeusServer.pegasus.PegasusUdpV2Handler;
/*    */ import com.zeusServer.tblConnections.TblActiveUdpConnections;
/*    */ import com.zeusServer.tblConnections.TblGriffonActiveUdpConnections;
/*    */ import com.zeusServer.tblConnections.TblMercuriusAVLActiveUdpConnections;
/*    */ import com.zeusServer.util.Main;
/*    */ import java.net.DatagramPacket;
/*    */ import java.net.DatagramSocket;
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
/*    */ public class UDPClientResolver
/*    */   implements Runnable
/*    */ {
/*    */   private DatagramPacket inPacket;
/*    */   private DatagramSocket socket;
/*    */   
/*    */   public UDPClientResolver(DatagramSocket socket, DatagramPacket inPacket) {
/* 33 */     this.inPacket = inPacket;
/* 34 */     this.socket = socket;
/*    */   }
/*    */ 
/*    */   
/*    */   public void run() {
/* 39 */     String str = this.inPacket.getAddress().toString().substring(1) + ":" + this.inPacket.getPort();
/* 40 */     byte[] data = this.inPacket.getData();
/* 41 */     if (data[0] == 84 && data[1] == 83 && data[2] == 84) {
/* 42 */       Runnable r = new ChkOtherIfaceHandler(null, this.inPacket, this.socket, "UDP");
/* 43 */       (Main.getTCPDataServer()).chkOIfaceHandler.execute(r);
/*    */     } else {
/* 45 */       byte[] prod = new byte[2];
/* 46 */       prod[0] = data[1];
/* 47 */       prod[1] = data[0];
/* 48 */       String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/* 49 */       short enc = Short.parseShort(prodBin.substring(0, 2), 2);
/* 50 */       prodBin = prodBin.substring(2);
/* 51 */       prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/* 52 */       short productID = Short.parseShort(prodBin, 2);
/* 53 */       if (TblActiveUdpConnections.getInstance().containsKey(str)) {
/* 54 */         ((UdpV2Handler)TblActiveUdpConnections.getInstance().get(str)).processM2SPacket(this.inPacket.getData());
/* 55 */       } else if (TblGriffonActiveUdpConnections.getInstance().containsKey(str) && TblGriffonActiveUdpConnections.getInstance().get(str) != null) {
/*    */         try {
/* 57 */           Thread.sleep(5L);
/* 58 */         } catch (InterruptedException ex) {
/* 59 */           ex.printStackTrace();
/*    */         } 
/* 61 */         ((UdpV2Handler)TblGriffonActiveUdpConnections.getInstance().get(str)).processM2SPacket(this.inPacket.getData(), this.inPacket.getLength());
/* 62 */       } else if (TblMercuriusAVLActiveUdpConnections.getInstance().containsKey(str) && TblMercuriusAVLActiveUdpConnections.getInstance().get(str) != null) {
/* 63 */         ((UdpV2Handler)TblMercuriusAVLActiveUdpConnections.getInstance().get(str)).processM2SPacket(this.inPacket.getData());
/*    */       } else {
/* 65 */         switch (Util.EnumProductIDs.getProductID(productID)) {
/*    */           case PEGASUS:
/* 67 */             synchronized (TblActiveUdpConnections.getInstance()) {
/* 68 */               if (!TblActiveUdpConnections.getInstance().containsKey(str)) {
/*    */                 
/* 70 */                 PegasusUdpV2Handler pegasusUdpV2Handler = new PegasusUdpV2Handler(this.socket, this.inPacket, enc);
/* 71 */                 TblActiveUdpConnections.getInstance().put(str, pegasusUdpV2Handler);
/* 72 */                 pegasusUdpV2Handler.processM2SPacket(data);
/*    */               } else {
/* 74 */                 ((UdpV2Handler)TblActiveUdpConnections.getInstance().get(str)).processM2SPacket(this.inPacket.getData());
/*    */               } 
/*    */             } 
/*    */             break;
/*    */           case GRIFFON_V1:
/*    */           case GRIFFON_V2:
/* 80 */             synchronized (TblGriffonActiveUdpConnections.getInstance()) {
/* 81 */               if (!TblGriffonActiveUdpConnections.getInstance().containsKey(str)) {
/* 82 */                 GriffonUdpHandler griffonUdpHandler = new GriffonUdpHandler(this.socket, this.inPacket, productID, enc);
/* 83 */                 TblGriffonActiveUdpConnections.getInstance().put(str, griffonUdpHandler);
/* 84 */                 griffonUdpHandler.processM2SPacket(data, this.inPacket.getLength());
/*    */               } else {
/* 86 */                 ((UdpV2Handler)TblGriffonActiveUdpConnections.getInstance().get(str)).processM2SPacket(this.inPacket.getData(), this.inPacket.getLength());
/*    */               } 
/*    */             } 
/*    */             break;
/*    */           case MERCURIUS:
/* 91 */             synchronized (TblMercuriusAVLActiveUdpConnections.getInstance()) {
/* 92 */               if (!TblMercuriusAVLActiveUdpConnections.getInstance().containsKey(str)) {
/*    */                 
/* 94 */                 MercuriusUdpHandler mercuriusUdpHandler = new MercuriusUdpHandler(this.socket, this.inPacket, enc);
/* 95 */                 TblMercuriusAVLActiveUdpConnections.getInstance().put(str, mercuriusUdpHandler);
/* 96 */                 mercuriusUdpHandler.processM2SPacket(data);
/*    */               } else {
/* 98 */                 ((UdpV2Handler)TblMercuriusAVLActiveUdpConnections.getInstance().get(str)).processM2SPacket(this.inPacket.getData());
/*    */               } 
/*    */             } 
/*    */             break;
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\UDPClientResolver.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */