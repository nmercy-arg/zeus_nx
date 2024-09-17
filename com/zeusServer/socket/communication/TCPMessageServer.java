/*     */ package com.zeusServer.socket.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.io.IOException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.ServerSocket;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.ThreadFactory;
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
/*     */ public class TCPMessageServer
/*     */   implements Runnable
/*     */ {
/*     */   private final String ip;
/*     */   private final int port;
/*     */   private ServerSocket messageServer;
/*     */   public boolean flag;
/*     */   public boolean msgServerRunning;
/*     */   public static GriffonMobileClientReceiver mobileAppDataUpdater;
/*     */   public static PegasusMobileClientReceiver pegamobileAppDataUpdater;
/*     */   
/*  40 */   private final ExecutorService messageWorkers = Executors.newCachedThreadPool(new ThreadFactory()
/*     */       {
/*     */         public Thread newThread(Runnable r) {
/*  43 */           Thread t = new Thread(r);
/*  44 */           t.setName("TCPMessageServer");
/*  45 */           t.setDaemon(true);
/*  46 */           return t;
/*     */         }
/*     */       });
/*     */   
/*     */   public TCPMessageServer(String ip, int port, boolean flag) {
/*  51 */     this.ip = ip;
/*  52 */     this.port = port;
/*  53 */     this.flag = flag;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  60 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  61 */         if (this.ip.equalsIgnoreCase("any")) {
/*  62 */           this.messageServer = new ServerSocket(this.port, 1000);
/*  63 */         } else if (this.ip.equalsIgnoreCase("eth0")) {
/*  64 */           this.messageServer = new ServerSocket(this.port, 1000, InetAddress.getByName(Functions.getMsgServerIP()));
/*  65 */         } else if (this.ip.equalsIgnoreCase("eth1")) {
/*  66 */           this.messageServer = new ServerSocket(this.port, 1000, InetAddress.getByName(Functions.getMsgServerIP()));
/*     */         } else {
/*  68 */           this.messageServer = new ServerSocket(this.port, 1000);
/*     */         } 
/*     */       } else {
/*  71 */         this.messageServer = new ServerSocket(this.port, 1000, InetAddress.getByName(this.ip));
/*     */       } 
/*  73 */       while (this.flag) {
/*     */         try {
/*  75 */           Socket sock = this.messageServer.accept();
/*  76 */           TCPMessageHandler messageHandler = new TCPMessageHandler(sock);
/*  77 */           this.messageWorkers.execute(messageHandler);
/*  78 */         } catch (SocketTimeoutException te) {
/*  79 */           te.printStackTrace();
/*  80 */         } catch (IOException ioe) {
/*  81 */           ioe.printStackTrace();
/*  82 */           Thread.yield();
/*     */         } 
/*     */       } 
/*  85 */     } catch (IOException ex) {
/*  86 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "", Enums.EnumMessagePriority.HIGH, null, ex);
/*     */     } finally {
/*     */       try {
/*  89 */         this.flag = false;
/*  90 */         this.msgServerRunning = false;
/*  91 */         if (this.messageServer != null) {
/*  92 */           this.messageServer.close();
/*     */         }
/*  94 */         this.messageServer = null;
/*  95 */         shutdown();
/*  96 */       } catch (IOException iOException) {}
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void shutdown() {
/* 103 */     this.messageWorkers.shutdownNow();
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\TCPMessageServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */