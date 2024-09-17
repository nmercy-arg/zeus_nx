/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.awt.HeadlessException;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import javax.swing.JOptionPane;
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
/*     */ public class CleanupWorker
/*     */   extends Worker<Void, Void>
/*     */ {
/*     */   private Date timestamp;
/*     */   private boolean backupRequired;
/*     */   
/*     */   public CleanupWorker(Date timestamp, boolean backupRequired) {
/*  33 */     this.timestamp = timestamp;
/*  34 */     this.backupRequired = backupRequired;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Void doInBackground() throws Exception {
/*     */     try {
/*  40 */       DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/*  41 */       int res = cleanupDatabase(df1.format(this.timestamp), this.backupRequired ? 1 : 0, "$ZeusNxManager");
/*  42 */       if (res == 6) {
/*  43 */         if (this.backupRequired) {
/*  44 */           JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Database_backup_file_created_successfully_Cleanup_process_submitted_as_background_task"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*     */         } else {
/*  46 */           JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Cleanup_process_submitted_as_background_task"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*     */         } 
/*  48 */       } else if (res == 1) {
/*  49 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("AutoBackup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  50 */       } else if (res == 2) {
/*  51 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Restore_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  52 */       } else if (res == 3) {
/*  53 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Manual_Backup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  54 */       } else if (res == 4) {
/*  55 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Cleanup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*     */       } else {
/*  57 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Error_while_the_cleanup_process"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */       } 
/*  59 */     } catch (HeadlessException ex) {
/*  60 */       ex.printStackTrace();
/*  61 */       JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Error_while_the_cleanup_process"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */     } finally {
/*  63 */       DatabaseCleaner.getInstance().enableDisableFields(true);
/*     */     } 
/*  65 */     return null;
/*     */   }
/*     */   
/*     */   public static int cleanupDatabase(String date, int dbClean, String userName) {
/*  69 */     Socket sock = null;
/*  70 */     int res = 0;
/*  71 */     byte[] bufferRx = new byte[1];
/*     */     try {
/*  73 */       if (sock == null) {
/*  74 */         sock = new Socket(Functions.getMsgServerIP(), ZeusServerCfg.getInstance().getMsgServerPort().intValue());
/*  75 */         sock.setReceiveBufferSize(32);
/*  76 */         sock.setSoTimeout(30000);
/*  77 */         sock.setTcpNoDelay(true);
/*     */       } 
/*     */       
/*  80 */       byte[] cleanupPacket = prepareDBCleanupPacket(date, dbClean, userName);
/*  81 */       if (sock.isConnected() && !sock.isClosed()) {
/*  82 */         flushReceiveBuffer(sock);
/*  83 */         sock.getOutputStream().write(cleanupPacket, 0, cleanupPacket.length);
/*  84 */         sleepThread(sock);
/*  85 */         if (sock.getInputStream().available() > 0) {
/*  86 */           sock.getInputStream().read(bufferRx, 0, 1);
/*  87 */           res = bufferRx[0];
/*     */         } 
/*     */       } 
/*  90 */     } catch (IOException|InterruptedException ex) {
/*  91 */       ex.printStackTrace();
/*     */     } finally {
/*     */       try {
/*  94 */         if (sock != null) {
/*  95 */           sock.close();
/*     */         }
/*  97 */       } catch (Exception exception) {
/*     */       
/*     */       } finally {}
/*     */     } 
/*     */     
/* 102 */     return res;
/*     */   }
/*     */   
/*     */   private static byte[] prepareDBCleanupPacket(String date, int dbClean, String userName) {
/* 106 */     int dLen = date.length() + userName.length() + 1;
/* 107 */     byte[] packet = new byte[dLen + 8];
/* 108 */     int idx = 0;
/* 109 */     packet[idx++] = -112;
/* 110 */     packet[idx++] = (byte)(dLen + 6);
/* 111 */     packet[idx++] = 68;
/* 112 */     packet[idx++] = (byte)date.length();
/* 113 */     for (char c : date.toCharArray()) {
/* 114 */       packet[idx++] = (byte)c;
/*     */     }
/* 116 */     packet[idx++] = 66;
/* 117 */     packet[idx++] = 1;
/* 118 */     packet[idx++] = (byte)dbClean;
/* 119 */     packet[idx++] = 85;
/* 120 */     packet[idx++] = (byte)userName.length();
/* 121 */     for (char c : userName.toCharArray()) {
/* 122 */       packet[idx++] = (byte)c;
/*     */     }
/* 124 */     return packet;
/*     */   }
/*     */   
/*     */   private static void flushReceiveBuffer(Socket sock) throws IOException {
/* 128 */     int val = sock.getInputStream().available();
/* 129 */     if (val > 0) {
/* 130 */       sock.getInputStream().skip(val);
/*     */     }
/*     */   }
/*     */   
/*     */   private static void sleepThread(Socket sock) throws InterruptedException, IOException {
/* 135 */     for (byte ii = 0; ii < 72; ii = (byte)(ii + 1)) {
/* 136 */       Thread.sleep(2500L);
/* 137 */       if (sock != null && sock.getInputStream().available() > 0)
/*     */         break; 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\CleanupWorker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */