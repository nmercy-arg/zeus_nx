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
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
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
/*     */ 
/*     */ 
/*     */ public class TCPDataServer
/*     */   implements Runnable
/*     */ {
/*     */   private final int port;
/*     */   private final String ip;
/*     */   private ServerSocket dataServer;
/*     */   public boolean flag;
/*     */   public boolean dataServerRunning;
/*  41 */   private static int serverHelperCount = 100;
/*  42 */   protected static List<String> ips = new ArrayList<>(3);
/*     */ 
/*     */   
/*  45 */   public final ExecutorService dataWorkers = Executors.newCachedThreadPool(new ThreadFactory()
/*     */       {
/*     */         public Thread newThread(Runnable r) {
/*  48 */           Thread t = new Thread(null, r, "TCPDataWorker", 131072L);
/*  49 */           t.setPriority(1);
/*  50 */           t.setDaemon(true);
/*  51 */           return t;
/*     */         }
/*     */       });
/*     */   
/*  55 */   public static final ExecutorService serverHelper = Executors.newFixedThreadPool(serverHelperCount, new ThreadFactory()
/*     */       {
/*     */         public Thread newThread(Runnable r) {
/*  58 */           Thread t = new Thread(null, r, "TCPProductIdentifier", 131072L);
/*  59 */           t.setDaemon(true);
/*  60 */           return t;
/*     */         }
/*     */       });
/*     */   
/*  64 */   public final ExecutorService chkOIfaceHandler = Executors.newFixedThreadPool(10, new ThreadFactory()
/*     */       {
/*     */         public Thread newThread(Runnable r) {
/*  67 */           Thread t = new Thread(null, r, "TestOtherInterfacesHandler", 131072L);
/*  68 */           t.setDaemon(true);
/*  69 */           return t;
/*     */         }
/*     */       });
/*     */   
/*     */   public TCPDataServer(String ip, int port, boolean flag) {
/*  74 */     this.ip = ip;
/*  75 */     this.port = port;
/*  76 */     this.flag = flag;
/*  77 */     ips.clear();
/*  78 */     ips.add("0.0.0.0");
/*  79 */     ips.add("127.0.0.1");
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  85 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  86 */         if (this.ip.equalsIgnoreCase("any")) {
/*  87 */           this.dataServer = new ServerSocket(this.port, 1000);
/*  88 */         } else if (this.ip.equalsIgnoreCase("eth0")) {
/*  89 */           this.dataServer = new ServerSocket(this.port, 1000, InetAddress.getByName(Functions.getDataServerIP()));
/*  90 */         } else if (this.ip.equalsIgnoreCase("eth1")) {
/*  91 */           this.dataServer = new ServerSocket(this.port, 1000, InetAddress.getByName(Functions.getDataServerIP()));
/*     */         } else {
/*  93 */           this.dataServer = new ServerSocket(this.port, 1000);
/*     */         } 
/*     */       } else {
/*  96 */         this.dataServer = new ServerSocket(this.port, 1000, InetAddress.getByName(this.ip));
/*     */       } 
/*  98 */       ips.add(Functions.getDataServerIP());
/*     */       try {
/* 100 */         ips.add(InetAddress.getLocalHost().getHostAddress());
/* 101 */       } catch (Exception exception) {}
/*     */ 
/*     */       
/* 104 */       while (this.flag) {
/*     */         try {
/* 106 */           Socket sock = this.dataServer.accept();
/* 107 */           if (this.dataServerRunning) {
/* 108 */             Runnable r = new ProductIdentifier(sock, this.dataServerRunning, false, null);
/* 109 */             serverHelper.execute(r); continue;
/* 110 */           }  if (this.dataServerRunning && GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.NORMAL) {
/* 111 */             String lip = sock.getRemoteSocketAddress().toString();
/* 112 */             lip = lip.substring(1, lip.indexOf(":"));
/* 113 */             if (ips.contains(lip)) {
/* 114 */               Runnable r = new ProductIdentifier(sock, this.dataServerRunning, false, null);
/* 115 */               serverHelper.execute(r); continue;
/*     */             } 
/* 117 */             sock.shutdownInput();
/* 118 */             sock.shutdownOutput();
/* 119 */             sock.close();
/*     */             continue;
/*     */           } 
/* 122 */           if (this.dataServerRunning || GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.NORMAL) {
/* 123 */             String lip = sock.getRemoteSocketAddress().toString();
/* 124 */             lip = lip.substring(1, lip.indexOf(":"));
/* 125 */             if (ips.contains(lip)) {
/* 126 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, "Found Watchdog While Database Restoring/Backup/Cleaning..... ", Enums.EnumMessagePriority.AVERAGE, null, null);
/* 127 */               WatchdogBusyHandler busyHandler = new WatchdogBusyHandler(sock);
/* 128 */               Thread busyThread = new Thread(busyHandler);
/* 129 */               busyThread.setDaemon(true);
/* 130 */               busyThread.start(); continue;
/*     */             } 
/* 132 */             sock.shutdownInput();
/* 133 */             sock.shutdownOutput();
/* 134 */             sock.close();
/*     */             continue;
/*     */           } 
/* 137 */           sock.shutdownInput();
/* 138 */           sock.shutdownOutput();
/* 139 */           sock.close();
/*     */         
/*     */         }
/* 142 */         catch (SocketTimeoutException socketTimeoutException) {
/*     */         
/* 144 */         } catch (IOException ioe) {
/* 145 */           Thread.yield();
/* 146 */         } catch (Exception ex) {
/* 147 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, "Exception Occurred in TCP Data Server.", Enums.EnumMessagePriority.HIGH, null, ex);
/*     */         }
/*     */       
/*     */       } 
/* 151 */     } catch (Exception ex) {
/* 152 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "", Enums.EnumMessagePriority.HIGH, null, ex);
/*     */     } finally {
/*     */       try {
/* 155 */         this.dataServerRunning = false;
/* 156 */         this.flag = false;
/* 157 */         if (this.dataServer != null) {
/* 158 */           this.dataServer.close();
/*     */         }
/* 160 */       } catch (IOException iOException) {}
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void shutdown() {
/* 166 */     this.dataWorkers.shutdownNow();
/* 167 */     this; serverHelper.shutdownNow();
/* 168 */     this.chkOIfaceHandler.shutdownNow();
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\socket\communication\TCPDataServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */