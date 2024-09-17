/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.Rijndael;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.awt.HeadlessException;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RestoreWorker
/*     */   extends Worker<Integer, String>
/*     */ {
/*     */   private File zipFile;
/*     */   
/*     */   public RestoreWorker(File zipFile) {
/*  43 */     this.zipFile = zipFile;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Integer doInBackground() throws Exception {
/*  48 */     File inFolder = null;
/*  49 */     if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*  50 */       String outFolderPath = "/media/zeusdisk/3iCorporation/ZeusNx/UploadedRestoreFiles/" + this.zipFile.getName().substring(0, this.zipFile.getName().length() - 4);
/*  51 */       Functions.createFolder(outFolderPath);
/*  52 */       Process pr = Runtime.getRuntime().exec("unzip -o " + this.zipFile.getAbsolutePath() + " -d " + outFolderPath);
/*  53 */       pr.waitFor();
/*  54 */       inFolder = new File(outFolderPath);
/*  55 */     } else if (GlobalVariables.currentPlatform == Enums.Platform.LINUX) {
/*  56 */       String outFolderPath = this.zipFile.getAbsolutePath().substring(0, this.zipFile.getAbsolutePath().length() - 4);
/*  57 */       Functions.createFolder(outFolderPath);
/*  58 */       Process pr = Runtime.getRuntime().exec("unzip -o " + this.zipFile.getAbsolutePath() + " -d " + outFolderPath);
/*  59 */       pr.waitFor();
/*  60 */       inFolder = new File(outFolderPath);
/*     */     } else {
/*  62 */       inFolder = Functions.unZipIt(this.zipFile);
/*     */     } 
/*     */     try {
/*  65 */       return Integer.valueOf(initiateRestoreDB(inFolder.listFiles()[0].getPath()));
/*     */     } finally {
/*  67 */       Functions.deleteDirectory(inFolder);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected void done() {
/*     */     try {
/*  74 */       if (ProcessingPopup.getActivePopupFrame() != null) {
/*  75 */         ProcessingPopup.getActivePopupFrame().setVisible(false);
/*     */       }
/*  77 */       int res = get().intValue();
/*  78 */       if (res == 6) {
/*  79 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Database_restored_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  80 */       } else if (res == 1) {
/*  81 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("AutoBackup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  82 */       } else if (res == 2) {
/*  83 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Restore_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  84 */       } else if (res == 3) {
/*  85 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Manual_Backup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*  86 */       } else if (res == 4) {
/*  87 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Cleanup_going_on_Please_retry_later"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*     */       } else {
/*  89 */         JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Error_while_restoring_a_backup_file"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */       } 
/*  91 */       MonitorZeus.getInstance().setTaskRunningStatus(false);
/*  92 */     } catch (HeadlessException|InterruptedException|java.util.concurrent.ExecutionException ex) {
/*  93 */       Logger.getLogger(RestoreWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  94 */       JOptionPane.showOptionDialog(MonitorZeus.getInstance().getParentFrame(), LocaleMessage.getLocaleMessage("Error_while_restoring_a_backup_file"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  95 */       MonitorZeus.getInstance().setTaskRunningStatus(false);
/*     */     } 
/*     */   }
/*     */   
/*     */   public int initiateRestoreDB(String path) {
/* 100 */     Socket sock = null;
/* 101 */     int val = 0;
/*     */     try {
/* 103 */       byte[] crytedBuffer = Rijndael.encryptBytes(path, Rijndael.msgKeyBytes, true);
/* 104 */       int len = crytedBuffer.length;
/* 105 */       byte[] bufferTx = new byte[len + 5];
/* 106 */       byte[] bufferRx = new byte[1];
/* 107 */       if (sock == null) {
/* 108 */         sock = new Socket(Functions.getMsgServerIP(), ZeusServerCfg.getInstance().getMsgServerPort().intValue());
/* 109 */         sock.setReceiveBufferSize(32);
/* 110 */         sock.setSoTimeout(30000);
/* 111 */         sock.setTcpNoDelay(true);
/*     */       } 
/* 113 */       if (sock.isConnected() && !sock.isClosed()) {
/* 114 */         bufferTx[0] = -122;
/* 115 */         bufferTx[1] = (byte)(len & 0xFF);
/* 116 */         bufferTx[2] = (byte)((len & 0xFF00) / 256);
/* 117 */         System.arraycopy(crytedBuffer, 0, bufferTx, 3, len);
/* 118 */         int crcCalc = CRC16.calculate(bufferTx, 0, len + 3, 65535);
/* 119 */         bufferTx[len + 3] = (byte)(crcCalc & 0xFF);
/* 120 */         bufferTx[len + 4] = (byte)((crcCalc & 0xFF00) / 256);
/* 121 */         flushReceiveBuffer(sock);
/* 122 */         sock.getOutputStream().write(bufferTx, 0, bufferTx.length);
/* 123 */         sleepThread(sock);
/* 124 */         if (sock.getInputStream().available() > 0) {
/* 125 */           sock.getInputStream().read(bufferRx, 0, 1);
/* 126 */           val = bufferRx[0];
/*     */         } 
/*     */       } 
/* 129 */     } catch (IOException|InterruptedException|java.security.InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/* 130 */       ex.printStackTrace();
/*     */     } finally {
/*     */       try {
/* 133 */         if (sock != null) {
/* 134 */           sock.close();
/*     */         }
/* 136 */       } catch (IOException iOException) {}
/*     */     } 
/*     */ 
/*     */     
/* 140 */     return val;
/*     */   }
/*     */   
/*     */   private void flushReceiveBuffer(Socket sock) throws IOException {
/* 144 */     int val = sock.getInputStream().available();
/* 145 */     if (val > 0) {
/* 146 */       sock.getInputStream().skip(val);
/*     */     }
/*     */   }
/*     */   
/*     */   private void sleepThread(Socket sock) throws InterruptedException, IOException {
/* 151 */     for (byte ii = 0; ii < 900; ii = (byte)(ii + 1)) {
/* 152 */       Thread.sleep(2500L);
/* 153 */       if (sock != null && sock.getInputStream().available() > 0)
/*     */         break; 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\RestoreWorker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */