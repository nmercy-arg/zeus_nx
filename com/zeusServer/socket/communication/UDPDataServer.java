/*     */ package com.zeusServer.socket.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.tblConnections.TblActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblMercuriusAVLActiveUdpConnections;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.Main;
/*     */ import java.io.IOException;
/*     */ import java.net.DatagramPacket;
/*     */ import java.net.DatagramSocket;
/*     */ import java.net.InetAddress;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.ThreadFactory;
/*     */ import java.util.concurrent.TimeUnit;
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
/*     */ public class UDPDataServer
/*     */   implements Runnable
/*     */ {
/*     */   private static final ScheduledFuture idleSF;
/*     */   static int serverHelperCount;
/*     */   static int clientHelperCount;
/*  41 */   private static int newCMD = 0;
/*     */   public static ExecutorService serverHelper;
/*     */   public static ExecutorService clientHelper;
/*     */   
/*     */   static {
/*  46 */     Runnable idleTimer = new Runnable()
/*     */       {
/*     */         public void run()
/*     */         {
/*     */           try {
/*     */             UDPDataServer.newCMD++;
/*  52 */             UDPDataServer.checkForIdleConnections((UDPDataServer.newCMD >= 3));
/*  53 */             if (UDPDataServer.newCMD >= 3) {
/*  54 */               UDPDataServer.newCMD = 0;
/*     */             }
/*  56 */           } catch (Exception ex) {
/*  57 */             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */           } 
/*     */         }
/*     */       };
/*  61 */     idleSF = Functions.addRunnable2ScheduleExecutor(idleTimer, 180000L, 20000L, TimeUnit.MILLISECONDS);
/*     */     
/*  63 */     serverHelperCount = 5;
/*  64 */     clientHelperCount = serverHelperCount * 10;
/*  65 */     serverHelper = Executors.newFixedThreadPool(serverHelperCount, new ThreadFactory()
/*     */         {
/*     */           public Thread newThread(Runnable r) {
/*  68 */             Thread t = new Thread(r);
/*  69 */             t.setPriority(1);
/*  70 */             t.setName("UDPDataServer");
/*  71 */             t.setDaemon(true);
/*  72 */             return t;
/*     */           }
/*     */         });
/*  75 */     clientHelper = Executors.newFixedThreadPool(clientHelperCount, new ThreadFactory()
/*     */         {
/*     */           public Thread newThread(Runnable r) {
/*  78 */             Thread t = new Thread(r);
/*  79 */             t.setName("UDPProductIdentifier");
/*  80 */             t.setDaemon(true);
/*  81 */             return t;
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private final int port;
/*     */   private final String ip;
/*  88 */   private DatagramSocket ds = null; public boolean flag; public boolean udpServerRunning;
/*  89 */   private DatagramPacket inPacket = null;
/*     */   
/*     */   public UDPDataServer(String ip, int port, boolean flag) {
/*  92 */     this.ip = ip;
/*  93 */     this.port = port;
/*  94 */     this.flag = flag;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/* 100 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/* 101 */         if (this.ip.equalsIgnoreCase("any")) {
/* 102 */           this.ds = new DatagramSocket(this.port);
/* 103 */         } else if (this.ip.equalsIgnoreCase("eth0")) {
/* 104 */           this.ds = new DatagramSocket(this.port, InetAddress.getByName(Functions.getUdpServerIP()));
/* 105 */         } else if (this.ip.equalsIgnoreCase("eth1")) {
/* 106 */           this.ds = new DatagramSocket(this.port, InetAddress.getByName(Functions.getUdpServerIP()));
/*     */         } else {
/* 108 */           this.ds = new DatagramSocket(this.port);
/*     */         } 
/*     */       } else {
/* 111 */         this.ds = new DatagramSocket(this.port, InetAddress.getByName(this.ip));
/*     */       } 
/*     */       
/* 114 */       while (this.flag) {
/* 115 */         byte[] inBuf = new byte[1024];
/* 116 */         this.inPacket = new DatagramPacket(inBuf, inBuf.length);
/* 117 */         this.ds.receive(this.inPacket);
/* 118 */         if (this.udpServerRunning) {
/* 119 */           Runnable r = new UDPClientResolver(this.ds, this.inPacket);
/* 120 */           serverHelper.execute(r);
/*     */         } 
/*     */       } 
/* 123 */     } catch (IOException ex) {
/* 124 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "", Enums.EnumMessagePriority.HIGH, null, ex);
/*     */     } finally {
/*     */       try {
/* 127 */         this.udpServerRunning = false;
/* 128 */         this.flag = false;
/* 129 */         this.ds.close();
/* 130 */         shutdown();
/* 131 */       } catch (Exception exception) {}
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void shutdown() {
/* 138 */     this; clientHelper.shutdownNow();
/* 139 */     this; serverHelper.shutdownNow();
/* 140 */     if (idleSF != null) {
/* 141 */       idleSF.cancel(true);
/*     */     }
/*     */   }
/*     */   
/*     */   private static void checkForIdleConnections(boolean newCMD) {
/* 146 */     synchronized (TblActiveUdpConnections.getInstance()) {
/* 147 */       for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblActiveUdpConnections.getInstance().entrySet()) {
/* 148 */         ((UdpV2Handler)handler.getValue()).removeIdleConnections();
/* 149 */         if (newCMD) {
/* 150 */           ((UdpV2Handler)handler.getValue()).sendNewCMDAtInActiveTime();
/*     */         }
/*     */       } 
/*     */     } 
/* 154 */     synchronized (TblGriffonActiveUdpConnections.getInstance()) {
/* 155 */       for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblGriffonActiveUdpConnections.getInstance().entrySet()) {
/* 156 */         ((UdpV2Handler)handler.getValue()).removeIdleConnections();
/* 157 */         if (newCMD) {
/* 158 */           ((UdpV2Handler)handler.getValue()).sendNewCMDAtInActiveTime();
/*     */         }
/*     */       } 
/*     */     } 
/* 162 */     synchronized (TblMercuriusAVLActiveUdpConnections.getInstance()) {
/* 163 */       for (Map.Entry<String, UdpV2Handler> handler : (Iterable<Map.Entry<String, UdpV2Handler>>)TblMercuriusAVLActiveUdpConnections.getInstance().entrySet()) {
/* 164 */         ((UdpV2Handler)handler.getValue()).removeIdleConnections();
/* 165 */         if (newCMD)
/* 166 */           ((UdpV2Handler)handler.getValue()).sendNewCMDAtInActiveTime(); 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\UDPDataServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */