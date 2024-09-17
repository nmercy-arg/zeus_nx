/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.service.controller.ServiceController;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.awt.Desktop;
/*     */ import java.awt.HeadlessException;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.net.URI;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JMenuItem;
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
/*     */ public class MonitorZeusWorker
/*     */   extends Worker<Boolean, String>
/*     */ {
/*     */   private int option;
/*     */   private File file;
/*     */   private ServiceController service;
/*     */   private ServiceController dService;
/*     */   private JMenuItem mItem;
/*     */   
/*     */   public MonitorZeusWorker(int option, File file, ServiceController service, ServiceController dService, JMenuItem mItem) {
/*  46 */     this.option = option;
/*  47 */     this.file = file;
/*  48 */     this.service = service;
/*  49 */     this.dService = dService;
/*  50 */     this.mItem = mItem;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Boolean doInBackground() throws Exception {
/*  55 */     boolean result = false;
/*  56 */     switch (this.option) {
/*     */       case 1:
/*     */         try {
/*  59 */           result = this.service.startService();
/*  60 */           Thread.sleep(2500L);
/*  61 */           this.mItem.setEnabled(!result);
/*  62 */         } catch (Exception ex) {
/*  63 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  64 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 2:
/*     */         try {
/*  69 */           result = this.service.stopService();
/*  70 */           this.dService.stopService();
/*  71 */           Thread.sleep(2500L);
/*  72 */           this.mItem.setEnabled(!result);
/*  73 */         } catch (Exception ex) {
/*  74 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  75 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 3:
/*     */         try {
/*  80 */           result = this.service.startService();
/*  81 */           Thread.sleep(2500L);
/*  82 */           this.mItem.setEnabled(!result);
/*  83 */         } catch (Exception ex) {
/*  84 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  85 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 4:
/*     */         try {
/*  90 */           result = this.service.stopService();
/*  91 */           Thread.sleep(2500L);
/*  92 */           this.mItem.setEnabled(!result);
/*  93 */         } catch (Exception ex) {
/*  94 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  95 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 5:
/*     */         try {
/* 100 */           result = this.service.startService();
/* 101 */           Thread.sleep(2500L);
/* 102 */           this.mItem.setEnabled(!result);
/* 103 */         } catch (Exception ex) {
/* 104 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 105 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 6:
/*     */         try {
/* 110 */           result = this.service.stopService();
/* 111 */           Thread.sleep(2500L);
/* 112 */           this.mItem.setEnabled(!result);
/* 113 */         } catch (Exception ex) {
/* 114 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 115 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 7:
/*     */         try {
/* 120 */           result = this.service.startService();
/* 121 */           Thread.sleep(2500L);
/* 122 */           this.mItem.setEnabled(!result);
/* 123 */         } catch (Exception ex) {
/* 124 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 125 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 8:
/*     */         try {
/* 130 */           result = this.service.stopService();
/* 131 */           Thread.sleep(2500L);
/* 132 */           this.mItem.setEnabled(!result);
/* 133 */         } catch (Exception ex) {
/* 134 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 135 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 9:
/*     */         try {
/* 140 */           if (GlobalVariables.currentPlatform == Enums.Platform.WINDOWS || Desktop.isDesktopSupported()) {
/* 141 */             Desktop.getDesktop().open(this.file);
/* 142 */             result = true; break;
/* 143 */           }  if (GlobalVariables.currentPlatform == Enums.Platform.LINUX) {
/* 144 */             Process pr = Runtime.getRuntime().exec(this.file.getAbsolutePath() + " &");
/* 145 */             result = true;
/*     */           } 
/* 147 */         } catch (IOException ex) {
/* 148 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 149 */           result = false;
/*     */         } 
/*     */         break;
/*     */       case 10:
/*     */         try {
/* 154 */           if (GlobalVariables.currentPlatform == Enums.Platform.WINDOWS || Desktop.isDesktopSupported()) {
/* 155 */             Desktop.getDesktop().browse(new URI("http://localhost:" + ZeusServerCfg.getInstance().getWebserverPort()));
/* 156 */             result = true; break;
/* 157 */           }  if (GlobalVariables.currentPlatform == Enums.Platform.LINUX) {
/* 158 */             Process pr = Runtime.getRuntime().exec("/usr/bin/firefox -new-window http://localhost:" + ZeusServerCfg.getInstance().getWebserverPort() + " &");
/* 159 */             result = true;
/*     */           } 
/* 161 */         } catch (IOException|java.net.URISyntaxException ex) {
/* 162 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 163 */           result = false;
/*     */         } 
/*     */         break;
/*     */       case 11:
/*     */         try {
/* 168 */           if (GlobalVariables.currentPlatform == Enums.Platform.WINDOWS || Desktop.isDesktopSupported()) {
/* 169 */             Desktop.getDesktop().open(this.file);
/* 170 */             result = true; break;
/* 171 */           }  if (GlobalVariables.currentPlatform == Enums.Platform.LINUX) {
/* 172 */             Process pr = Runtime.getRuntime().exec(this.file.getAbsolutePath() + " &");
/* 173 */             result = true;
/*     */           } 
/* 175 */         } catch (IOException ex) {
/* 176 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 177 */           result = false;
/*     */         } 
/*     */         break;
/*     */       case 12:
/*     */         try {
/* 182 */           Socket sock = null;
/*     */           try {
/* 184 */             byte[] bufferTx = new byte[1];
/* 185 */             byte[] bufferRx = new byte[1];
/*     */             
/* 187 */             for (int i = 0; i < 3; i++) {
/* 188 */               if (sock == null) {
/* 189 */                 sock = new Socket(Functions.getMsgServerIP(), ZeusServerCfg.getInstance().getMsgServerPort().intValue());
/* 190 */                 sock.setReceiveBufferSize(32);
/* 191 */                 sock.setSoTimeout(30000);
/* 192 */                 sock.setTcpNoDelay(true);
/*     */               } 
/* 194 */               if (sock.isConnected() && !sock.isClosed()) {
/* 195 */                 bufferTx[0] = -120;
/*     */                 
/* 197 */                 flushReceiveBuffer(sock);
/* 198 */                 sock.getOutputStream().write(bufferTx, 0, bufferTx.length);
/* 199 */                 Thread.sleep(100L);
/* 200 */                 if (sock.getInputStream().read(bufferRx, 0, 1) > 0 && bufferRx[0] == 6)
/*     */                 {
/*     */                   break;
/*     */                 
/*     */                 }
/*     */               
/*     */               }
/*     */ 
/*     */             
/*     */             }
/*     */           
/*     */           }
/* 212 */           catch (IOException|InterruptedException ex) {
/* 213 */             ex.printStackTrace();
/*     */           } finally {
/*     */             try {
/* 216 */               if (sock != null) {
/* 217 */                 sock.close();
/*     */               }
/* 219 */             } catch (IOException iOException) {
/*     */             
/*     */             } finally {}
/*     */           } 
/*     */           
/* 224 */           result = true;
/* 225 */         } catch (Exception ex) {
/* 226 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 227 */           result = false;
/*     */         } 
/*     */         break;
/*     */       case 14:
/*     */         try {
/* 232 */           result = MonitorZeus.getInstance().startAllServiceControllers();
/* 233 */           this.mItem.setEnabled(!result);
/* 234 */         } catch (Exception ex) {
/* 235 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 236 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 13:
/*     */         try {
/* 241 */           result = MonitorZeus.getInstance().stopAllServiceControllers();
/* 242 */           this.mItem.setEnabled(!result);
/* 243 */         } catch (Exception ex) {
/* 244 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 245 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 15:
/*     */         try {
/* 250 */           result = MonitorZeus.getInstance().restartAllServiceControllers();
/* 251 */         } catch (Exception ex) {
/* 252 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */         } 
/*     */         break;
/*     */       case 16:
/*     */         try {
/* 257 */           result = this.service.stopService();
/* 258 */           this.dService.stopService();
/* 259 */           Thread.sleep(2500L);
/* 260 */           result = this.service.startService();
/* 261 */           Thread.sleep(2500L);
/* 262 */         } catch (Exception ex) {
/* 263 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 264 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 17:
/*     */         try {
/* 269 */           result = this.service.stopService();
/* 270 */           Thread.sleep(2500L);
/* 271 */           result = this.service.startService();
/* 272 */           Thread.sleep(2500L);
/* 273 */         } catch (Exception ex) {
/* 274 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 275 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 18:
/*     */         try {
/* 280 */           result = this.service.stopService();
/* 281 */           Thread.sleep(2500L);
/* 282 */           result = this.service.startService();
/* 283 */           Thread.sleep(2500L);
/* 284 */         } catch (Exception ex) {
/* 285 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 286 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 19:
/*     */         try {
/* 291 */           result = this.service.stopService();
/* 292 */           Thread.sleep(2500L);
/* 293 */           result = this.service.startService();
/* 294 */           Thread.sleep(2500L);
/* 295 */         } catch (Exception ex) {
/* 296 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 297 */           this.mItem.setEnabled(!result);
/*     */         } 
/*     */         break;
/*     */       case 20:
/*     */         try {
/* 302 */           Desktop.getDesktop().open(this.file);
/* 303 */           result = true;
/* 304 */         } catch (IOException ex) {
/* 305 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 306 */           result = false;
/*     */         } 
/*     */         break;
/*     */       case 21:
/*     */         try {
/* 311 */           Desktop.getDesktop().open(this.file);
/* 312 */           result = true;
/* 313 */         } catch (IOException ex) {
/* 314 */           Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 315 */           result = false;
/*     */         } 
/*     */         break;
/*     */     } 
/* 319 */     return Boolean.valueOf(result);
/*     */   }
/*     */   
/*     */   private static void flushReceiveBuffer(Socket sock) throws IOException {
/* 323 */     int val = sock.getInputStream().available();
/* 324 */     if (val > 0) {
/* 325 */       sock.getInputStream().skip(val);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   protected void done() {
/* 331 */     JFrame parentFrame = MonitorZeus.getInstance().getParentFrame();
/*     */     try {
/* 333 */       if (ProcessingPopup.getActivePopupFrame() != null) {
/* 334 */         ProcessingPopup.getActivePopupFrame().setVisible(false);
/* 335 */         ProcessingPopup.getActivePopupFrame().dispose();
/*     */       } 
/* 337 */       switch (this.option) {
/*     */         case 1:
/* 339 */           if (get().booleanValue()) {
/* 340 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_started_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 342 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 2:
/* 346 */           if (get().booleanValue()) {
/* 347 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_stopped_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 349 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 3:
/* 353 */           if (get().booleanValue()) {
/* 354 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Control_Center_started_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 356 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 4:
/* 360 */           if (get().booleanValue()) {
/* 361 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Control_Center_stopped_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 363 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 5:
/* 367 */           if (get().booleanValue()) {
/* 368 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Database_started_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 370 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 6:
/* 374 */           if (get().booleanValue()) {
/* 375 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Database_stopped_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 377 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 7:
/* 381 */           if (get().booleanValue()) {
/* 382 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Watchdog_started_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 384 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 8:
/* 388 */           if (get().booleanValue()) {
/* 389 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Watchdog_stopped_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 391 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 9:
/* 395 */           if (get().booleanValue()) {
/*     */             break;
/*     */           }
/* 398 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Monitor"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 10:
/* 402 */           if (get().booleanValue()) {
/*     */             break;
/*     */           }
/* 405 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Control_Center"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 11:
/* 409 */           if (get().booleanValue()) {
/*     */             break;
/*     */           }
/* 412 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Remote_Debugger"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 12:
/* 416 */           if (get().booleanValue()) {
/* 417 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("All_modules_were_successfully_disconnected"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 419 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_disconnecting_modules"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 14:
/* 423 */           if (get().booleanValue()) {
/* 424 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("All_services_were_successfully_started"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 426 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 13:
/* 430 */           if (get().booleanValue()) {
/* 431 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("All_services_were_successfully_stopped"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 433 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 15:
/* 437 */           if (get().booleanValue()) {
/* 438 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("All_services_were_successfully_restarted"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 440 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 16:
/* 444 */           if (get().booleanValue()) {
/* 445 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Server_restarted_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 447 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 17:
/* 451 */           if (get().booleanValue()) {
/* 452 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Control_Center_restarted_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 454 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 18:
/* 458 */           if (get().booleanValue()) {
/* 459 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Database_restarted_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 461 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 19:
/* 465 */           if (get().booleanValue()) {
/* 466 */             JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Zeus_Nx_Watchdog_restarted_successfully"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null); break;
/*     */           } 
/* 468 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 20:
/* 472 */           if (get().booleanValue()) {
/*     */             break;
/*     */           }
/* 475 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Benchmark"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         
/*     */         case 21:
/* 479 */           if (get().booleanValue()) {
/*     */             break;
/*     */           }
/* 482 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Migrator"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */       } 
/*     */       
/* 486 */       MonitorZeus.getInstance().setTaskRunningStatus(false);
/* 487 */     } catch (HeadlessException|InterruptedException|java.util.concurrent.ExecutionException ex) {
/* 488 */       Logger.getLogger(MonitorZeusWorker.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 489 */       switch (this.option) {
/*     */         case 1:
/* 491 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 2:
/* 494 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 3:
/* 497 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 4:
/* 500 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 5:
/* 503 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 6:
/* 506 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 7:
/* 509 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 8:
/* 512 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 9:
/* 515 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Monitor"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 10:
/* 518 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Control_Center"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 11:
/* 521 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Remote_Debugger"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 12:
/* 524 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("783"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 14:
/* 527 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 13:
/* 530 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 15:
/* 533 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 16:
/* 536 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 17:
/* 539 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 18:
/* 542 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 19:
/* 545 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 20:
/* 548 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Benchmark"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */         case 21:
/* 551 */           JOptionPane.showOptionDialog(parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Migrator"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*     */           break;
/*     */       } 
/*     */       
/* 555 */       MonitorZeus.getInstance().setTaskRunningStatus(false);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\MonitorZeusWorker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */