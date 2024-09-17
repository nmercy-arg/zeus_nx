/*      */ package com.zeusServer.ui;
/*      */ 
/*      */ import com.zeusServer.service.controller.LinuxServiceController;
/*      */ import com.zeusServer.service.controller.MacServiceController;
/*      */ import com.zeusServer.service.controller.ServiceController;
/*      */ import com.zeusServer.service.controller.WindowsServiceController;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.Main;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import java.awt.AWTException;
/*      */ import java.awt.Component;
/*      */ import java.awt.Font;
/*      */ import java.awt.HeadlessException;
/*      */ import java.awt.Image;
/*      */ import java.awt.SystemTray;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.net.URL;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;
/*      */ import java.util.concurrent.Executors;
/*      */ import java.util.concurrent.ScheduledFuture;
/*      */ import java.util.concurrent.ScheduledThreadPoolExecutor;
/*      */ import java.util.concurrent.ThreadFactory;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ import javax.imageio.ImageIO;
/*      */ import javax.swing.ImageIcon;
/*      */ import javax.swing.JFileChooser;
/*      */ import javax.swing.JFrame;
/*      */ import javax.swing.JMenu;
/*      */ import javax.swing.JMenuItem;
/*      */ import javax.swing.JOptionPane;
/*      */ import javax.swing.JPopupMenu;
/*      */ import javax.swing.JSeparator;
/*      */ import javax.swing.SwingUtilities;
/*      */ import javax.swing.UIManager;
/*      */ import javax.swing.filechooser.FileNameExtensionFilter;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class MonitorZeus
/*      */ {
/*      */   protected static final String ZEUS_SERVICE = "ZeusNx";
/*      */   protected static final String ZEUSCC_SERVICE = "ZeusControlCenter";
/*      */   protected static final String ZEUSDB_SERVICE = "ZeusDerby";
/*      */   protected static final String ZEUSWD_SERVICE = "ZeusServerWatchdog";
/*      */   protected static final String ZUES_ICON_PATH = "/images/Zeus_16x16.png";
/*      */   protected static final String SOFTWARES_ICON_PATH = "/images/softwares_16.png";
/*      */   protected static final String SERVICES_ICON_PATH = "/images/services_18.png";
/*      */   protected static final int ZEUS_NX_START = 1;
/*      */   protected static final int ZEUS_NX_STOP = 2;
/*      */   protected static final int ZEUS_NX_CC_START = 3;
/*      */   protected static final int ZEUS_NX_CC_STOP = 4;
/*      */   protected static final int ZEUS_DB_START = 5;
/*      */   protected static final int ZEUS_DB_STOP = 6;
/*      */   protected static final int ZEUS_WD_START = 7;
/*      */   protected static final int ZEUS_WD_STOP = 8;
/*      */   protected static final int ZEUS_NX_MONITOR = 9;
/*      */   protected static final int ZEUS_NX_CC = 10;
/*      */   protected static final int ZEUS_NX_RD = 11;
/*      */   protected static final int DISCONNECT_MODULES = 12;
/*      */   protected static final int STOP_ALL_SERVICES = 13;
/*      */   protected static final int START_ALL_SERVICES = 14;
/*      */   protected static final int RESTART_ALL_SERVICES = 15;
/*      */   protected static final int RESTART_ZEUS_NX = 16;
/*      */   protected static final int RESTART_ZEUS_NX_CC = 17;
/*      */   protected static final int RESTART_ZEUS_DB = 18;
/*      */   protected static final int RESTART_ZEUS_WD = 19;
/*      */   protected static final int ZEUS_NX_BENCHMARK = 20;
/*      */   protected static final int ZEUS_NX_MERGER = 21;
/*      */   private JTrayIcon trayIcon;
/*      */   private SystemTray tray;
/*      */   private JPopupMenu popup;
/*      */   private JMenuItem zeusSWStart;
/*      */   private JMenuItem zeusSWStop;
/*      */   private JMenuItem zeusSWRestart;
/*      */   private JMenuItem disconnectModules;
/*      */   private JMenuItem zeusCCSWStart;
/*      */   private JMenuItem zeusCCSWStop;
/*      */   private JMenuItem zeusCCSWRestart;
/*      */   private JMenuItem zeusDBSWStart;
/*      */   private JMenuItem zeusDBSWStop;
/*      */   private JMenuItem zeusDBSWRestart;
/*      */   private JMenuItem zeusWDSWStart;
/*      */   private JMenuItem zeusWDSWStop;
/*      */   private JMenuItem zeusWDSWRestart;
/*      */   private JMenuItem zeusNxMonitor;
/*      */   private JSeparator zeusNxMonitorSepartor;
/*      */   private JMenuItem zeusRemoteDebugger;
/*      */   private JSeparator zeusRemoteDebuggerSepartor;
/*      */   private JMenuItem zeusBenchMark;
/*      */   private JSeparator zeusBenchMarkSepartor;
/*      */   private JMenuItem zeusMerger;
/*      */   private JSeparator zeusMergerSepartor;
/*      */   private JMenuItem zeusControlCenter;
/*      */   private JMenuItem stopAllServices;
/*      */   private JMenuItem startAllServices;
/*      */   private JMenuItem restartAllServices;
/*      */   private JMenuItem backupNow;
/*      */   private JMenuItem restoreNow;
/*      */   private JMenuItem cleanup;
/*      */   private JMenu maintainance;
/*      */   private JMenu sws;
/*      */   private JMenu srvs;
/*      */   private JMenuItem exit;
/*      */   private ServiceController zeusServiceController;
/*      */   private ServiceController zeusCCServiceController;
/*      */   private ServiceController zeusDBServiceController;
/*      */   private ServiceController zeusWDServiceController;
/*      */   private File flZeusNxMonitor;
/*      */   private File flZeusNxRemoteDebugger;
/*      */   private File flZeusNxBenchmark;
/*      */   private File flZeusNxMerger;
/*      */   private DatabaseCleaner dcFrame;
/*      */   private boolean taskRunning = false;
/*      */   private static MonitorZeus instance;
/*  140 */   private Font font_14 = new Font("Verdana", 1, 14);
/*  141 */   private Font font_12 = new Font("Verdana", 0, 12);
/*  142 */   private static int count = 0;
/*      */   private JFrame parentFrame;
/*      */   
/*  145 */   private static ScheduledThreadPoolExecutor stpe = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1, new ThreadFactory()
/*      */       {
/*      */         public Thread newThread(Runnable r) {
/*  148 */           Thread t = new Thread(r);
/*  149 */           t.setName("MonitorZeus");
/*  150 */           t.setDaemon(true);
/*  151 */           return t;
/*      */         }
/*      */       });
/*      */   private ScheduledFuture monitorServiceStatus;
/*      */   
/*      */   public static void main(String[] args) {
/*  157 */     for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
/*  158 */       if (info.getName().equals("Nimbus")) {
/*      */         try {
/*  160 */           UIManager.setLookAndFeel(info.getClassName());
/*      */           break;
/*  162 */         } catch (ClassNotFoundException|InstantiationException|IllegalAccessException|javax.swing.UnsupportedLookAndFeelException ex) {
/*  163 */           Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */         } 
/*      */       }
/*      */     } 
/*      */     
/*  168 */     LocaleMessage.createInstance(ZeusServerCfg.getInstance().getLanguageID());
/*  169 */     SwingUtilities.invokeLater(new Runnable()
/*      */         {
/*      */           public void run() {
/*  172 */             MonitorZeus.instance = new MonitorZeus();
/*  173 */             MonitorZeus.instance.createMonitorZeusTrayIcon();
/*      */           }
/*      */         });
/*      */   }
/*      */   
/*      */   public static ScheduledFuture addRunnable2ScheduleExecutor(Runnable task, long initialDelay, long repeatDelay, TimeUnit unit) {
/*  179 */     return stpe.scheduleWithFixedDelay(task, initialDelay, repeatDelay, unit);
/*      */   }
/*      */   
/*      */   public void createServiceStatusMonitor() {
/*  183 */     if (GlobalVariables.currentPlatform != null && GlobalVariables.currentPlatform != Enums.Platform.WINDOWS) {
/*  184 */       Runnable monitorTask = new Runnable()
/*      */         {
/*      */           public void run()
/*      */           {
/*      */             try {
/*  189 */               MonitorZeus.this.enableDisableStartStopMenus();
/*  190 */             } catch (Exception ex) {
/*  191 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */             } 
/*      */           }
/*      */         };
/*  195 */       this.monitorServiceStatus = addRunnable2ScheduleExecutor(monitorTask, 400L, 8000L, TimeUnit.MILLISECONDS);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private void createMonitorZeusTrayIcon() {
/*      */     File cMonitorFile;
/*      */     try {
/*  203 */       File platform = new File(Main.platformFilePath);
/*  204 */       if (platform.exists()) {
/*  205 */         BufferedReader br = new BufferedReader(new FileReader(platform));
/*      */         String line;
/*  207 */         while ((line = br.readLine()) != null) {
/*  208 */           if (line.contains("ARMv7")) {
/*  209 */             GlobalVariables.currentPlatform = Enums.Platform.ARM;
/*      */             break;
/*      */           } 
/*      */         } 
/*      */       } else {
/*  214 */         GlobalVariables.currentPlatform = Functions.detectPlatform();
/*      */       } 
/*  216 */     } catch (IOException ex) {
/*  217 */       ex.printStackTrace();
/*      */     } 
/*      */     
/*  220 */     if (!SystemTray.isSupported()) {
/*  221 */       JOptionPane.showOptionDialog(null, LocaleMessage.getLocaleMessage("System_tray_is_not_supported_in_your_machine"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*      */       
/*      */       return;
/*      */     } 
/*      */     
/*  226 */     createServiceStatusMonitor();
/*      */     
/*  228 */     initialiazeServiceControllers();
/*  229 */     String currLocation = (new File(MonitorZeus.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getParent();
/*  230 */     String cLocation = (new File(MonitorZeus.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getPath();
/*      */     
/*  232 */     switch (GlobalVariables.currentPlatform) {
/*      */       case WINDOWS:
/*  234 */         cMonitorFile = new File(cLocation + "/ZeusNxMonitor.exe");
/*  235 */         if (cMonitorFile.exists()) {
/*  236 */           this.flZeusNxMonitor = cMonitorFile;
/*      */         } else {
/*  238 */           this.flZeusNxMonitor = new File(currLocation + "/ZeusNxTools/ZeusNxMonitor/ZeusNxMonitor.exe");
/*      */         } 
/*  240 */         this.flZeusNxRemoteDebugger = new File(currLocation + "/ZeusNxTools/ZeusNxRemoteDebugger/ZeusNxRemoteDebugger.exe");
/*  241 */         this.flZeusNxBenchmark = new File(currLocation + "/ZeusNxTools/ZeusNxBenchMark/ZeusNxBenchMark.exe");
/*  242 */         this.flZeusNxMerger = new File(currLocation + "/ZeusNxTools/ZeusNxMigrator/ZeusNxMigrator.exe");
/*      */         break;
/*      */       case LINUX:
/*  245 */         cMonitorFile = new File(cLocation + "/ZeusNxMonitor");
/*  246 */         if (cMonitorFile.exists()) {
/*  247 */           this.flZeusNxMonitor = cMonitorFile;
/*      */         } else {
/*  249 */           this.flZeusNxMonitor = new File(currLocation + "/ZeusNxTools/ZeusNxMonitor/ZeusNxMonitor");
/*      */         } 
/*  251 */         this.flZeusNxRemoteDebugger = new File(currLocation + "/ZeusNxTools/ZeusNxRemoteDebugger/ZeusNxRemoteDebugger.sh");
/*  252 */         this.flZeusNxBenchmark = new File(currLocation + "/ZeusNxTools/ZeusNxBenchMark/ZeusNxBenchMark.sh");
/*  253 */         this.flZeusNxMerger = new File(currLocation + "/ZeusNxTools/ZeusNxMigrator/ZeusNxMigrator.sh");
/*      */         break;
/*      */     } 
/*      */ 
/*      */     
/*  258 */     Image image = createImage("/images/Zeus.png");
/*  259 */     Image image_32 = createImage("/images/Zeus_Nx_32.png");
/*  260 */     ImageIcon serviceICON = new ImageIcon(createImage("/images/services_18.png"));
/*      */     
/*  262 */     this.parentFrame = new JFrame();
/*  263 */     this.parentFrame.setUndecorated(true);
/*  264 */     this.parentFrame.setVisible(false);
/*  265 */     this.parentFrame.setLocationRelativeTo((Component)null);
/*  266 */     List<Image> imgList = new ArrayList<>(2);
/*  267 */     imgList.add(image);
/*  268 */     imgList.add(image_32);
/*  269 */     this.parentFrame.setIconImages(imgList);
/*  270 */     this.popup = new JPopupMenu();
/*      */     
/*  272 */     JMenuItem version = new JMenuItem(LocaleMessage.getLocaleMessage("full_version"));
/*  273 */     version.setFont(this.font_14);
/*  274 */     version.setIcon(new ImageIcon(createImage("/images/Zeus_16x16.png")));
/*  275 */     version.setIconTextGap(5);
/*  276 */     version.setHorizontalTextPosition(11);
/*  277 */     this.popup.add(version);
/*  278 */     this.popup.addSeparator();
/*      */     
/*  280 */     this.sws = new JMenu(LocaleMessage.getLocaleMessage("Softwares"));
/*  281 */     this.sws.setFont(this.font_12);
/*  282 */     this.sws.setIcon(new ImageIcon(createImage("/images/softwares_16.png")));
/*      */     
/*  284 */     this.sws.setIconTextGap(5);
/*  285 */     this.sws.setHorizontalTextPosition(11);
/*  286 */     this.popup.add(this.sws);
/*  287 */     this.popup.addSeparator();
/*      */     
/*  289 */     this.srvs = new JMenu(LocaleMessage.getLocaleMessage("Services"));
/*  290 */     this.srvs.setFont(this.font_12);
/*  291 */     this.srvs.setIcon(serviceICON);
/*  292 */     this.srvs.setIconTextGap(5);
/*  293 */     this.srvs.setHorizontalTextPosition(11);
/*  294 */     this.popup.add(this.srvs);
/*  295 */     this.popup.addSeparator();
/*      */     
/*  297 */     this.startAllServices = new JMenuItem(LocaleMessage.getLocaleMessage("Start_All"));
/*  298 */     this.startAllServices.setFont(this.font_12);
/*  299 */     this.startAllServices.setIconTextGap(5);
/*  300 */     this.startAllServices.setHorizontalTextPosition(11);
/*  301 */     this.srvs.add(this.startAllServices);
/*  302 */     this.srvs.addSeparator();
/*  303 */     this.startAllServices.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  307 */               ZeusServerCfg.nullfyInstance();
/*  308 */               MonitorZeus.this.reloadMenuText();
/*  309 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  310 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(14, null, null, null, MonitorZeus.this.startAllServices));
/*  311 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  312 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Starting_all_services_please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  314 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  315 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  316 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  319 */             } catch (IOException ex) {
/*  320 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  321 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  322 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  327 */     this.stopAllServices = new JMenuItem(LocaleMessage.getLocaleMessage("Stop_All"));
/*  328 */     this.stopAllServices.setFont(this.font_12);
/*  329 */     this.stopAllServices.setIconTextGap(5);
/*  330 */     this.stopAllServices.setHorizontalTextPosition(11);
/*  331 */     this.srvs.add(this.stopAllServices);
/*  332 */     this.srvs.addSeparator();
/*  333 */     this.stopAllServices.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  337 */               ZeusServerCfg.nullfyInstance();
/*  338 */               MonitorZeus.this.reloadMenuText();
/*  339 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  340 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(13, null, null, null, MonitorZeus.this.stopAllServices));
/*  341 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  342 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Stopping_all_services_please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  344 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  345 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  346 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  349 */             } catch (IOException ex) {
/*  350 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  351 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  352 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  357 */     this.restartAllServices = new JMenuItem(LocaleMessage.getLocaleMessage("Restart_All"));
/*  358 */     this.restartAllServices.setFont(this.font_12);
/*  359 */     this.restartAllServices.setIconTextGap(5);
/*  360 */     this.restartAllServices.setHorizontalTextPosition(11);
/*  361 */     this.srvs.add(this.restartAllServices);
/*  362 */     this.restartAllServices.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  366 */               ZeusServerCfg.nullfyInstance();
/*  367 */               MonitorZeus.this.reloadMenuText();
/*  368 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  369 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(15, null, null, null, MonitorZeus.this.restartAllServices));
/*  370 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  371 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Restarting_all_services_please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  373 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  374 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  375 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  378 */             } catch (IOException ex) {
/*  379 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  380 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_all_services"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  381 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  386 */     if (GlobalVariables.currentPlatform != Enums.Platform.ARM) {
/*  387 */       this.zeusControlCenter = new JMenuItem(LocaleMessage.getLocaleMessage("Zeus_Nx_Control_Center"));
/*  388 */       this.zeusControlCenter.setFont(this.font_12);
/*  389 */       this.zeusControlCenter.setIconTextGap(5);
/*  390 */       this.zeusControlCenter.setHorizontalTextPosition(11);
/*  391 */       if (GlobalVariables.currentPlatform != Enums.Platform.ARM) {
/*  392 */         this.sws.add(this.zeusControlCenter);
/*      */       } else {
/*  394 */         this.sws.add(this.zeusControlCenter);
/*  395 */         this.zeusControlCenter.setEnabled(false);
/*      */       } 
/*      */       
/*  398 */       this.zeusControlCenter.addActionListener(new ActionListener()
/*      */           {
/*      */             public void actionPerformed(ActionEvent e) {
/*      */               try {
/*  402 */                 if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  403 */                   ProcessingPopup.setWorker(new MonitorZeusWorker(10, null, null, null, null));
/*  404 */                   MonitorZeus.this.setTaskRunningStatus(true);
/*  405 */                   JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Opening_Zeus_Nx_Control_Center_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */                 }
/*  407 */                 else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  408 */                   ProcessingPopup.getActivePopupFrame().requestFocus();
/*  409 */                   ProcessingPopup.getActivePopupFrame().toFront();
/*      */                 }
/*      */               
/*  412 */               } catch (IOException ex) {
/*  413 */                 Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  414 */                 JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Control_Center"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  415 */                 MonitorZeus.this.setTaskRunningStatus(false);
/*      */               } 
/*      */             }
/*      */           });
/*      */       
/*  420 */       if (this.flZeusNxMonitor != null) {
/*  421 */         if (this.flZeusNxMonitor.exists()) {
/*  422 */           addZeusNxMonitorMenu();
/*      */         } else {
/*  424 */           removeZeusNxMonitorMenu();
/*      */         } 
/*      */       }
/*      */       
/*  428 */       if (this.flZeusNxRemoteDebugger != null) {
/*  429 */         if (this.flZeusNxRemoteDebugger.exists()) {
/*  430 */           addRemoteDebuggerMenu();
/*      */         } else {
/*  432 */           removeRemoteDebuggerMenu();
/*      */         } 
/*      */       }
/*      */       
/*  436 */       if (this.flZeusNxBenchmark != null) {
/*  437 */         if (this.flZeusNxBenchmark.exists()) {
/*  438 */           addZeusNxBenchMarkMenu();
/*      */         } else {
/*  440 */           removeZeusNxBenchMarkMenu();
/*      */         } 
/*      */       }
/*      */       
/*  444 */       if (this.flZeusNxMerger != null) {
/*  445 */         if (this.flZeusNxMerger.exists()) {
/*  446 */           addZeusNxMergerMenu();
/*      */         } else {
/*  448 */           removeZeusNxMergerMenu();
/*      */         } 
/*      */       }
/*      */     } 
/*      */     
/*  453 */     this.maintainance = new JMenu(LocaleMessage.getLocaleMessage("Zeus_Nx_Database_Tools"));
/*  454 */     this.maintainance.setFont(this.font_12);
/*  455 */     this.maintainance.setIconTextGap(5);
/*      */     
/*  457 */     this.backupNow = new JMenuItem(LocaleMessage.getLocaleMessage("Backup"));
/*  458 */     this.backupNow.setFont(this.font_12);
/*  459 */     this.backupNow.setIconTextGap(5);
/*  460 */     this.backupNow.setHorizontalTextPosition(11);
/*  461 */     this.maintainance.add(this.backupNow);
/*  462 */     this.maintainance.addSeparator();
/*  463 */     this.backupNow.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  467 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  468 */                 ProcessingPopup.setWorker1(new BackupWorker());
/*  469 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  470 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Database_backup_file_being_created_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), false);
/*      */               }
/*  472 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  473 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  474 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  477 */             } catch (IOException ex) {
/*  478 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  479 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_creating_a_backup_file"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  480 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  485 */     this.restoreNow = new JMenuItem(LocaleMessage.getLocaleMessage("Restore"));
/*  486 */     this.restoreNow.setFont(this.font_12);
/*  487 */     this.restoreNow.setIconTextGap(5);
/*  488 */     this.restoreNow.setHorizontalTextPosition(11);
/*  489 */     this.maintainance.add(this.restoreNow);
/*  490 */     this.maintainance.addSeparator();
/*  491 */     this.restoreNow.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  495 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*      */                 
/*  497 */                 File fc, file = new File(".");
/*  498 */                 if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/*  499 */                   fc = new File(ZeusServerCfg.getInstance().getDbBackupDirectory());
/*      */                 } else {
/*  501 */                   fc = new File(file.getPath() + "/backup_db");
/*      */                 } 
/*  503 */                 JFileChooser chooser = new JFileChooser(fc);
/*  504 */                 chooser.setDialogTitle(LocaleMessage.getLocaleMessage("Select_backup_file"));
/*  505 */                 chooser.setFileFilter(new FileNameExtensionFilter(LocaleMessage.getLocaleMessage("Backup_Zip_Files"), new String[] { "zip" }));
/*  506 */                 JFrame frm = new JFrame();
/*  507 */                 frm.setIconImage(ImageIO.read(MonitorZeus.class.getClassLoader().getResource("images/Zeus_16x16.png")));
/*      */                 
/*  509 */                 if (chooser.showOpenDialog(frm) == 0) {
/*  510 */                   ProcessingPopup.setWorker1(new RestoreWorker(chooser.getSelectedFile()));
/*  511 */                   MonitorZeus.this.setTaskRunningStatus(true);
/*  512 */                   ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Please_wait_while_database_is_restored"), LocaleMessage.getLocaleMessage("Please_wait"), false);
/*      */                 }
/*      */               
/*  515 */               } else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  516 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*      */               }
/*      */             
/*  519 */             } catch (HeadlessException|IOException ex) {
/*  520 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  521 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_restoring_a_backup_file"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  526 */     this.cleanup = new JMenuItem(LocaleMessage.getLocaleMessage("Cleanup"));
/*  527 */     this.cleanup.setFont(this.font_12);
/*  528 */     this.cleanup.setIconTextGap(5);
/*  529 */     this.cleanup.setHorizontalTextPosition(11);
/*  530 */     this.maintainance.add(this.cleanup);
/*  531 */     this.cleanup.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  535 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  536 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  537 */                 if (MonitorZeus.this.dcFrame == null) {
/*  538 */                   MonitorZeus.this.dcFrame = new DatabaseCleaner();
/*      */                 }
/*  540 */                 MonitorZeus.this.dcFrame.updateMessages2Locale();
/*  541 */                 if (!MonitorZeus.this.dcFrame.isVisible()) {
/*  542 */                   MonitorZeus.this.dcFrame.showPopup();
/*      */                 }
/*      */               }
/*  545 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  546 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  547 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  550 */             } catch (HeadlessException ex) {
/*  551 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  552 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_the_cleanup_process"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  553 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  558 */     JMenu zeusSW = new JMenu(LocaleMessage.getLocaleMessage("Zeus_Nx"));
/*  559 */     zeusSW.setIconTextGap(5);
/*  560 */     zeusSW.setIcon(serviceICON);
/*  561 */     zeusSW.setHorizontalTextPosition(11);
/*  562 */     zeusSW.setFont(this.font_12);
/*  563 */     this.popup.add(zeusSW);
/*  564 */     this.popup.addSeparator();
/*      */ 
/*      */ 
/*      */     
/*  568 */     this.zeusSWStart = new JMenuItem(LocaleMessage.getLocaleMessage("Start"));
/*  569 */     this.zeusSWStart.setFont(this.font_12);
/*  570 */     this.zeusSWStart.setIconTextGap(5);
/*  571 */     this.zeusSWStart.setHorizontalTextPosition(11);
/*  572 */     zeusSW.add(this.zeusSWStart);
/*  573 */     zeusSW.addSeparator();
/*  574 */     this.zeusSWStart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  578 */               ZeusServerCfg.nullfyInstance();
/*  579 */               MonitorZeus.this.reloadMenuText();
/*  580 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  581 */                 if (MonitorZeus.this.zeusServiceController == null) {
/*  582 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  584 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(1, null, MonitorZeus.this.zeusServiceController, null, MonitorZeus.this.zeusSWStart));
/*  585 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  586 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Starting_Zeus_Nx_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  588 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  589 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  590 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  593 */             } catch (IOException ex) {
/*  594 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  595 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  596 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  597 */               MonitorZeus.this.zeusSWStart.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  602 */     this.zeusSWStop = new JMenuItem(LocaleMessage.getLocaleMessage("Stop"));
/*  603 */     this.zeusSWStop.setFont(this.font_12);
/*  604 */     this.zeusSWStop.setIconTextGap(5);
/*  605 */     this.zeusSWStop.setHorizontalTextPosition(11);
/*  606 */     zeusSW.add(this.zeusSWStop);
/*  607 */     zeusSW.addSeparator();
/*  608 */     this.zeusSWStop.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  612 */               ZeusServerCfg.nullfyInstance();
/*  613 */               MonitorZeus.this.reloadMenuText();
/*  614 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  615 */                 if (MonitorZeus.this.zeusServiceController == null) {
/*  616 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  618 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(2, null, MonitorZeus.this.zeusServiceController, MonitorZeus.this.zeusWDServiceController, MonitorZeus.this.zeusSWStop));
/*  619 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  620 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Stopping_Zeus_Nx_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  622 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  623 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  624 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  627 */             } catch (IOException ex) {
/*  628 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  629 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  630 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  631 */               MonitorZeus.this.zeusSWStop.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  636 */     this.zeusSWRestart = new JMenuItem(LocaleMessage.getLocaleMessage("Restart"));
/*  637 */     this.zeusSWRestart.setFont(this.font_12);
/*  638 */     this.zeusSWRestart.setIconTextGap(5);
/*  639 */     this.zeusSWRestart.setHorizontalTextPosition(11);
/*  640 */     zeusSW.add(this.zeusSWRestart);
/*  641 */     zeusSW.addSeparator();
/*  642 */     this.zeusSWRestart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  646 */               ZeusServerCfg.nullfyInstance();
/*  647 */               MonitorZeus.this.reloadMenuText();
/*  648 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  649 */                 if (MonitorZeus.this.zeusServiceController == null) {
/*  650 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  652 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(16, null, MonitorZeus.this.zeusServiceController, MonitorZeus.this.zeusWDServiceController, MonitorZeus.this.zeusSWRestart));
/*  653 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  654 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Restarting_Zeus_Nx_Server_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  656 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  657 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  658 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  661 */             } catch (IOException ex) {
/*  662 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  663 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Server_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  664 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */ 
/*      */     
/*  670 */     this.disconnectModules = new JMenuItem(LocaleMessage.getLocaleMessage("Disconnect_Modules"));
/*  671 */     this.disconnectModules.setFont(this.font_12);
/*  672 */     this.disconnectModules.setIconTextGap(5);
/*  673 */     this.disconnectModules.setHorizontalTextPosition(11);
/*  674 */     zeusSW.add(this.disconnectModules);
/*  675 */     this.disconnectModules.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  679 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  680 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(12, null, MonitorZeus.this.zeusServiceController, null, MonitorZeus.this.disconnectModules));
/*  681 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  682 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Disconnecting_Modules_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  684 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  685 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  686 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  689 */             } catch (IOException ex) {
/*  690 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  691 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_disconnecting_modules"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  692 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  697 */     JMenu zeusCCSW = new JMenu(LocaleMessage.getLocaleMessage("Zeus_Nx_Control_Center"));
/*  698 */     zeusCCSW.setIconTextGap(5);
/*  699 */     zeusCCSW.setIcon(serviceICON);
/*  700 */     zeusCCSW.setHorizontalTextPosition(11);
/*  701 */     zeusCCSW.setFont(this.font_12);
/*  702 */     this.popup.add(zeusCCSW);
/*  703 */     this.popup.addSeparator();
/*      */ 
/*      */ 
/*      */     
/*  707 */     this.zeusCCSWStart = new JMenuItem(LocaleMessage.getLocaleMessage("Start"));
/*  708 */     this.zeusCCSWStart.setFont(this.font_12);
/*  709 */     this.zeusCCSWStart.setIconTextGap(5);
/*  710 */     this.zeusCCSWStart.setHorizontalTextPosition(11);
/*  711 */     zeusCCSW.add(this.zeusCCSWStart);
/*  712 */     zeusCCSW.addSeparator();
/*  713 */     this.zeusCCSWStart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  717 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  718 */                 if (MonitorZeus.this.zeusCCServiceController == null) {
/*  719 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  721 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(3, null, MonitorZeus.this.zeusCCServiceController, null, MonitorZeus.this.zeusCCSWStart));
/*  722 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  723 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Starting_Zeus_Nx_Control_Center_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  725 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  726 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  727 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  730 */             } catch (IOException ex) {
/*  731 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  732 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  733 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  734 */               MonitorZeus.this.zeusCCSWStart.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  739 */     this.zeusCCSWStop = new JMenuItem(LocaleMessage.getLocaleMessage("Stop"));
/*  740 */     this.zeusCCSWStop.setFont(this.font_12);
/*  741 */     this.zeusCCSWStop.setIconTextGap(5);
/*  742 */     this.zeusCCSWStop.setHorizontalTextPosition(11);
/*  743 */     zeusCCSW.add(this.zeusCCSWStop);
/*  744 */     zeusCCSW.addSeparator();
/*  745 */     this.zeusCCSWStop.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  749 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  750 */                 if (MonitorZeus.this.zeusCCServiceController == null) {
/*  751 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  753 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(4, null, MonitorZeus.this.zeusCCServiceController, null, MonitorZeus.this.zeusCCSWStop));
/*  754 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  755 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Stopping_Zeus_Nx_Control_Center_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  757 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  758 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  759 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  762 */             } catch (IOException ex) {
/*  763 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  764 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  765 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  766 */               MonitorZeus.this.zeusCCSWStop.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  771 */     this.zeusCCSWRestart = new JMenuItem(LocaleMessage.getLocaleMessage("Restart"));
/*  772 */     this.zeusCCSWRestart.setFont(this.font_12);
/*  773 */     this.zeusCCSWRestart.setIconTextGap(5);
/*  774 */     this.zeusCCSWRestart.setHorizontalTextPosition(11);
/*  775 */     zeusCCSW.add(this.zeusCCSWRestart);
/*  776 */     this.zeusCCSWRestart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  780 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  781 */                 if (MonitorZeus.this.zeusCCServiceController == null) {
/*  782 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  784 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(17, null, MonitorZeus.this.zeusCCServiceController, null, MonitorZeus.this.zeusCCSWRestart));
/*  785 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  786 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Restarting_Zeus_Nx_Control_Center_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  788 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  789 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  790 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  793 */             } catch (IOException ex) {
/*  794 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  795 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Control_Center_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  796 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */ 
/*      */     
/*  802 */     JMenu zeusDBSW = new JMenu(LocaleMessage.getLocaleMessage("Zeus_Nx_Database"));
/*  803 */     zeusDBSW.setIconTextGap(5);
/*  804 */     zeusDBSW.setIcon(serviceICON);
/*  805 */     zeusDBSW.setHorizontalTextPosition(11);
/*  806 */     zeusDBSW.setFont(this.font_12);
/*  807 */     this.popup.add(zeusDBSW);
/*  808 */     this.popup.addSeparator();
/*      */ 
/*      */ 
/*      */     
/*  812 */     this.zeusDBSWStart = new JMenuItem(LocaleMessage.getLocaleMessage("Start"));
/*  813 */     this.zeusDBSWStart.setFont(this.font_12);
/*  814 */     this.zeusDBSWStart.setIconTextGap(5);
/*  815 */     this.zeusDBSWStart.setHorizontalTextPosition(11);
/*  816 */     zeusDBSW.add(this.zeusDBSWStart);
/*  817 */     zeusDBSW.addSeparator();
/*  818 */     this.zeusDBSWStart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  822 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  823 */                 if (MonitorZeus.this.zeusDBServiceController == null) {
/*  824 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  826 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(5, null, MonitorZeus.this.zeusDBServiceController, null, MonitorZeus.this.zeusDBSWStart));
/*  827 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  828 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Starting_Zeus_Nx_Database_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  830 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  831 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  832 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  835 */             } catch (IOException ex) {
/*  836 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  837 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  838 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  839 */               MonitorZeus.this.zeusDBSWStart.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  844 */     this.zeusDBSWStop = new JMenuItem(LocaleMessage.getLocaleMessage("Stop"));
/*  845 */     this.zeusDBSWStop.setFont(this.font_12);
/*  846 */     this.zeusDBSWStop.setIconTextGap(5);
/*  847 */     this.zeusDBSWStop.setHorizontalTextPosition(11);
/*  848 */     zeusDBSW.add(this.zeusDBSWStop);
/*  849 */     zeusDBSW.addSeparator();
/*  850 */     this.zeusDBSWStop.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  854 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  855 */                 if (MonitorZeus.this.zeusDBServiceController == null) {
/*  856 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  858 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(6, null, MonitorZeus.this.zeusDBServiceController, null, MonitorZeus.this.zeusDBSWStop));
/*  859 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  860 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Stopping_Zeus_Nx_Database_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  862 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  863 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  864 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  867 */             } catch (IOException ex) {
/*  868 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  869 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  870 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  871 */               MonitorZeus.this.zeusDBSWStop.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  876 */     this.zeusDBSWRestart = new JMenuItem(LocaleMessage.getLocaleMessage("Restart"));
/*  877 */     this.zeusDBSWRestart.setFont(this.font_12);
/*  878 */     this.zeusDBSWRestart.setIconTextGap(5);
/*  879 */     this.zeusDBSWRestart.setHorizontalTextPosition(11);
/*  880 */     zeusDBSW.add(this.zeusDBSWRestart);
/*  881 */     this.zeusDBSWRestart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  885 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  886 */                 if (MonitorZeus.this.zeusDBServiceController == null) {
/*  887 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  889 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(18, null, MonitorZeus.this.zeusDBServiceController, null, MonitorZeus.this.zeusDBSWRestart));
/*  890 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  891 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Restarting_Zeus_Nx_Database_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  893 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  894 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  895 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  898 */             } catch (IOException ex) {
/*  899 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  900 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Database_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  901 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  906 */     this.sws.addSeparator();
/*  907 */     this.sws.add(this.maintainance);
/*      */     
/*  909 */     JMenu zeusWDSW = new JMenu(LocaleMessage.getLocaleMessage("Zeus_Nx_Watchdog"));
/*  910 */     zeusWDSW.setIconTextGap(5);
/*  911 */     zeusWDSW.setIcon(serviceICON);
/*  912 */     zeusWDSW.setHorizontalTextPosition(11);
/*  913 */     zeusWDSW.setFont(this.font_12);
/*  914 */     this.popup.add(zeusWDSW);
/*  915 */     this.popup.addSeparator();
/*      */ 
/*      */ 
/*      */     
/*  919 */     this.zeusWDSWStart = new JMenuItem(LocaleMessage.getLocaleMessage("Start"));
/*  920 */     this.zeusWDSWStart.setFont(this.font_12);
/*  921 */     this.zeusWDSWStart.setIconTextGap(5);
/*  922 */     this.zeusWDSWStart.setHorizontalTextPosition(11);
/*  923 */     zeusWDSW.add(this.zeusWDSWStart);
/*  924 */     zeusWDSW.addSeparator();
/*  925 */     this.zeusWDSWStart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  929 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  930 */                 if (MonitorZeus.this.zeusWDServiceController == null) {
/*  931 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  933 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(7, null, MonitorZeus.this.zeusWDServiceController, null, MonitorZeus.this.zeusWDSWStart));
/*  934 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  935 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Starting_Zeus_Nx_Watchdog_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  937 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  938 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  939 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  942 */             } catch (IOException ex) {
/*  943 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  944 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_starting_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  945 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  946 */               MonitorZeus.this.zeusWDSWStart.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  951 */     this.zeusWDSWStop = new JMenuItem(LocaleMessage.getLocaleMessage("Stop"));
/*  952 */     this.zeusWDSWStop.setFont(this.font_12);
/*  953 */     this.zeusWDSWStop.setIconTextGap(5);
/*  954 */     this.zeusWDSWStop.setHorizontalTextPosition(11);
/*  955 */     zeusWDSW.add(this.zeusWDSWStop);
/*  956 */     zeusWDSW.addSeparator();
/*  957 */     this.zeusWDSWStop.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  961 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  962 */                 if (MonitorZeus.this.zeusWDServiceController == null) {
/*  963 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  965 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(8, null, MonitorZeus.this.zeusWDServiceController, null, MonitorZeus.this.zeusWDSWStop));
/*  966 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  967 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Stopping_Zeus_Nx_Watchdog_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/*  969 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/*  970 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/*  971 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/*  974 */             } catch (IOException ex) {
/*  975 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  976 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_stopping_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/*  977 */               MonitorZeus.this.setTaskRunningStatus(false);
/*  978 */               MonitorZeus.this.zeusWDSWStop.setEnabled(true);
/*      */             } 
/*      */           }
/*      */         });
/*      */     
/*  983 */     this.zeusWDSWRestart = new JMenuItem(LocaleMessage.getLocaleMessage("Restart"));
/*  984 */     this.zeusWDSWRestart.setFont(this.font_12);
/*  985 */     this.zeusWDSWRestart.setIconTextGap(5);
/*  986 */     this.zeusWDSWRestart.setHorizontalTextPosition(11);
/*  987 */     zeusWDSW.add(this.zeusWDSWRestart);
/*  988 */     this.zeusWDSWRestart.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/*  992 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/*  993 */                 if (MonitorZeus.this.zeusWDServiceController == null) {
/*  994 */                   MonitorZeus.this.initialiazeServiceControllers();
/*      */                 }
/*  996 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(19, null, MonitorZeus.this.zeusWDServiceController, null, MonitorZeus.this.zeusWDSWRestart));
/*  997 */                 MonitorZeus.this.setTaskRunningStatus(true);
/*  998 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Restarting_Zeus_Nx_Watchdog_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/* 1000 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/* 1001 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/* 1002 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/* 1005 */             } catch (IOException ex) {
/* 1006 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 1007 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_restarting_Zeus_Nx_Server_Watchdog_service"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/* 1008 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */ 
/*      */     
/* 1014 */     this.exit = new JMenuItem(LocaleMessage.getLocaleMessage("Exit"));
/* 1015 */     this.exit.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/* 1018 */             int confirm = JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Do_you_really_want_to_close_the_Zeus_Nx_Manager"), LocaleMessage.getLocaleMessage("full_version"), 0, 3, null, null, null);
/* 1019 */             if (confirm == 0 && 
/* 1020 */               MonitorZeus.this.tray != null && MonitorZeus.this.trayIcon != null) {
/* 1021 */               MonitorZeus.this.tray.remove(MonitorZeus.this.trayIcon);
/* 1022 */               System.exit(0);
/*      */             } 
/*      */           }
/*      */         });
/*      */ 
/*      */     
/* 1028 */     this.exit.setIconTextGap(5);
/* 1029 */     this.exit.setHorizontalTextPosition(11);
/* 1030 */     this.exit.setFont(this.font_12);
/* 1031 */     this.popup.add(this.exit);
/*      */ 
/*      */     
/* 1034 */     this.trayIcon = new JTrayIcon(GlobalVariables.currentPlatform, image, LocaleMessage.getLocaleMessage("full_version"));
/* 1035 */     this.trayIcon.setImageAutoSize(true);
/*      */     
/* 1037 */     this.trayIcon.setJPopupMenu(this.popup);
/* 1038 */     this.tray = SystemTray.getSystemTray();
/*      */     
/*      */     try {
/* 1041 */       this.tray.add(this.trayIcon);
/* 1042 */     } catch (AWTException e) {
/*      */       return;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean stopAllServiceControllers() {
/* 1048 */     if (this.zeusServiceController == null || this.zeusCCServiceController == null || this.zeusDBServiceController == null || this.zeusWDServiceController == null) {
/* 1049 */       initialiazeServiceControllers();
/*      */     }
/*      */     
/*      */     try {
/* 1053 */       ZeusServerCfg.nullfyInstance();
/* 1054 */       reloadMenuText();
/* 1055 */       if (this.zeusServiceController.getServiceStatus() == 4) {
/* 1056 */         this.zeusServiceController.stopService();
/*      */       }
/* 1058 */       Thread.sleep(2500L);
/* 1059 */       if (this.zeusWDServiceController.getServiceStatus() == 4) {
/* 1060 */         this.zeusWDServiceController.stopService();
/*      */       }
/* 1062 */       Thread.sleep(2500L);
/* 1063 */       if (this.zeusCCServiceController.getServiceStatus() == 4) {
/* 1064 */         this.zeusCCServiceController.stopService();
/*      */       }
/* 1066 */       Thread.sleep(2500L);
/* 1067 */       if (this.zeusDBServiceController.getServiceStatus() == 4) {
/* 1068 */         this.zeusDBServiceController.stopService();
/*      */       }
/* 1070 */       Thread.sleep(2500L);
/* 1071 */       return true;
/* 1072 */     } catch (Exception ex) {
/* 1073 */       ex.printStackTrace();
/* 1074 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean startAllServiceControllers() {
/* 1079 */     if (this.zeusServiceController == null || this.zeusCCServiceController == null || this.zeusDBServiceController == null || this.zeusWDServiceController == null) {
/* 1080 */       initialiazeServiceControllers();
/*      */     }
/*      */     try {
/* 1083 */       if (this.zeusDBServiceController.getServiceStatus() != 4) {
/* 1084 */         this.zeusDBServiceController.startService();
/* 1085 */         Thread.sleep(30000L);
/*      */       } 
/*      */       
/* 1088 */       if (this.zeusServiceController.getServiceStatus() != 4) {
/* 1089 */         this.zeusServiceController.startService();
/* 1090 */         Thread.sleep(2500L);
/*      */       }
/* 1092 */       else if (this.zeusWDServiceController.getServiceStatus() != 4) {
/* 1093 */         this.zeusWDServiceController.startService();
/* 1094 */         Thread.sleep(2500L);
/*      */       } 
/*      */ 
/*      */       
/* 1098 */       if (this.zeusCCServiceController.getServiceStatus() != 4) {
/* 1099 */         this.zeusCCServiceController.startService();
/* 1100 */         Thread.sleep(2500L);
/*      */       } 
/* 1102 */       return true;
/* 1103 */     } catch (Exception ex) {
/* 1104 */       ex.printStackTrace();
/* 1105 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean restartAllServiceControllers() {
/* 1110 */     if (this.zeusServiceController == null || this.zeusCCServiceController == null || this.zeusDBServiceController == null || this.zeusWDServiceController == null) {
/* 1111 */       initialiazeServiceControllers();
/*      */     }
/*      */     try {
/* 1114 */       if (this.zeusServiceController.getServiceStatus() == 4) {
/* 1115 */         this.zeusServiceController.stopService();
/*      */       }
/* 1117 */       Thread.sleep(1000L);
/* 1118 */       if (this.zeusWDServiceController.getServiceStatus() == 4) {
/* 1119 */         this.zeusWDServiceController.stopService();
/*      */       }
/* 1121 */       Thread.sleep(1000L);
/* 1122 */       if (this.zeusCCServiceController.getServiceStatus() == 4) {
/* 1123 */         this.zeusCCServiceController.stopService();
/*      */       }
/* 1125 */       Thread.sleep(1000L);
/* 1126 */       if (this.zeusDBServiceController.getServiceStatus() == 4) {
/* 1127 */         this.zeusDBServiceController.stopService();
/*      */       }
/* 1129 */       Thread.sleep(3000L);
/*      */       
/* 1131 */       if (this.zeusDBServiceController.getServiceStatus() != 4) {
/* 1132 */         this.zeusDBServiceController.startService();
/* 1133 */         Thread.sleep(30000L);
/*      */       } 
/*      */       
/* 1136 */       if (this.zeusServiceController.getServiceStatus() != 4) {
/* 1137 */         this.zeusServiceController.startService();
/* 1138 */         Thread.sleep(2500L);
/*      */       } 
/*      */       
/* 1141 */       if (this.zeusCCServiceController.getServiceStatus() != 4) {
/* 1142 */         this.zeusCCServiceController.startService();
/* 1143 */         Thread.sleep(2500L);
/*      */       } 
/* 1145 */       return true;
/* 1146 */     } catch (Exception ex) {
/* 1147 */       ex.printStackTrace();
/* 1148 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void initialiazeServiceControllers() {
/*      */     try {
/* 1154 */       switch (GlobalVariables.currentPlatform) {
/*      */         case WINDOWS:
/* 1156 */           if (this.zeusServiceController == null) {
/* 1157 */             this.zeusServiceController = (ServiceController)new WindowsServiceController("ZeusNx");
/*      */           }
/* 1159 */           if (this.zeusCCServiceController == null) {
/* 1160 */             this.zeusCCServiceController = (ServiceController)new WindowsServiceController("ZeusControlCenter");
/*      */           }
/* 1162 */           if (this.zeusDBServiceController == null) {
/* 1163 */             this.zeusDBServiceController = (ServiceController)new WindowsServiceController("ZeusDerby");
/*      */           }
/* 1165 */           if (this.zeusWDServiceController == null) {
/* 1166 */             this.zeusWDServiceController = (ServiceController)new WindowsServiceController("ZeusServerWatchdog");
/*      */           }
/*      */           break;
/*      */         case LINUX:
/*      */         case ARM:
/* 1171 */           if (this.zeusServiceController == null) {
/* 1172 */             this.zeusServiceController = (ServiceController)new LinuxServiceController("ZeusNx");
/*      */           }
/* 1174 */           if (this.zeusCCServiceController == null) {
/* 1175 */             this.zeusCCServiceController = (ServiceController)new LinuxServiceController("ZeusControlCenter");
/*      */           }
/* 1177 */           if (this.zeusDBServiceController == null) {
/* 1178 */             this.zeusDBServiceController = (ServiceController)new LinuxServiceController("ZeusDerby");
/*      */           }
/* 1180 */           if (this.zeusWDServiceController == null) {
/* 1181 */             this.zeusWDServiceController = (ServiceController)new LinuxServiceController("ZeusServerWatchdog");
/*      */           }
/*      */           break;
/*      */         case MAC:
/* 1185 */           if (this.zeusServiceController == null) {
/* 1186 */             this.zeusServiceController = (ServiceController)new MacServiceController("ZeusNx");
/*      */           }
/* 1188 */           if (this.zeusCCServiceController == null) {
/* 1189 */             this.zeusCCServiceController = (ServiceController)new MacServiceController("ZeusControlCenter");
/*      */           }
/* 1191 */           if (this.zeusDBServiceController == null) {
/* 1192 */             this.zeusDBServiceController = (ServiceController)new MacServiceController("ZeusDerby");
/*      */           }
/* 1194 */           if (this.zeusWDServiceController == null) {
/* 1195 */             this.zeusWDServiceController = (ServiceController)new MacServiceController("ZeusServerWatchdog");
/*      */           }
/*      */           break;
/*      */       } 
/* 1199 */     } catch (Exception ex) {
/* 1200 */       ex.printStackTrace();
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public void enableDisableStartStopMenus() {
/* 1206 */     boolean flag = false;
/*      */     
/*      */     try {
/* 1209 */       count = 0;
/* 1210 */       if (this.zeusServiceController == null || this.zeusCCServiceController == null || this.zeusDBServiceController == null || this.zeusWDServiceController == null) {
/* 1211 */         initialiazeServiceControllers();
/*      */       }
/* 1213 */       boolean s1 = flag = (this.zeusServiceController.getServiceStatus() == 4);
/* 1214 */       this.zeusSWStart.setEnabled((flag != true));
/* 1215 */       this.zeusSWStop.setEnabled(flag);
/* 1216 */       this.zeusSWRestart.setEnabled(flag);
/* 1217 */       boolean s2 = flag = (this.zeusCCServiceController.getServiceStatus() == 4);
/* 1218 */       this.zeusCCSWStart.setEnabled((flag != true));
/* 1219 */       this.zeusCCSWStop.setEnabled(flag);
/* 1220 */       this.zeusCCSWRestart.setEnabled(flag);
/* 1221 */       boolean s3 = flag = (this.zeusDBServiceController.getServiceStatus() == 4);
/* 1222 */       this.zeusDBSWStart.setEnabled((flag != true));
/* 1223 */       this.zeusDBSWStop.setEnabled(flag);
/* 1224 */       this.zeusDBSWRestart.setEnabled(flag);
/* 1225 */       boolean s4 = flag = (this.zeusWDServiceController.getServiceStatus() == 4);
/* 1226 */       this.zeusWDSWStart.setEnabled((flag != true));
/* 1227 */       this.zeusWDSWStop.setEnabled(flag);
/* 1228 */       this.zeusWDSWRestart.setEnabled(flag);
/* 1229 */       if (s1 && s2 && s3 && s4) {
/* 1230 */         this.startAllServices.setEnabled(false);
/* 1231 */         this.stopAllServices.setEnabled(true);
/* 1232 */         this.restartAllServices.setEnabled(true);
/*      */       }
/* 1234 */       else if (!s1 && !s2 && !s3 && !s4) {
/* 1235 */         this.restartAllServices.setEnabled(false);
/* 1236 */         this.stopAllServices.setEnabled(false);
/* 1237 */         this.startAllServices.setEnabled(true);
/*      */       } else {
/* 1239 */         this.restartAllServices.setEnabled(true);
/* 1240 */         this.stopAllServices.setEnabled(true);
/* 1241 */         this.startAllServices.setEnabled(true);
/*      */       } 
/*      */       
/* 1244 */       if (this.flZeusNxMonitor == null || this.flZeusNxRemoteDebugger == null || this.flZeusNxBenchmark == null || this.flZeusNxMerger == null) {
/* 1245 */         File cMonitorFile; String currLocation = (new File(MonitorZeus.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile().getParent();
/* 1246 */         switch (GlobalVariables.currentPlatform) {
/*      */           case WINDOWS:
/* 1248 */             this.flZeusNxMonitor = new File(currLocation + "/ZeusNxTools/ZeusNxMonitor/ZeusNxMonitor.exe");
/* 1249 */             this.flZeusNxRemoteDebugger = new File(currLocation + "/ZeusNxTools/ZeusNxRemoteDebugger/ZeusNxRemoteDebugger.exe");
/* 1250 */             this.flZeusNxBenchmark = new File(currLocation + "/ZeusNxTools/ZeusNxBenchMark/ZeusNxBenchMark.exe");
/* 1251 */             this.flZeusNxMerger = new File(currLocation + "/ZeusNxTools/ZeusNxMerger/ZeusNxMerger.exe");
/*      */             break;
/*      */           case LINUX:
/* 1254 */             cMonitorFile = new File(currLocation + "/ZeusNxMonitor");
/* 1255 */             if (cMonitorFile.exists()) {
/* 1256 */               this.flZeusNxMonitor = cMonitorFile;
/*      */             } else {
/* 1258 */               this.flZeusNxMonitor = new File(currLocation + "/ZeusNxTools/ZeusNxMonitor/ZeusNxMonitor.sh");
/*      */             } 
/* 1260 */             this.flZeusNxRemoteDebugger = new File(currLocation + "/ZeusNxTools/ZeusNxRemoteDebugger/ZeusNxRemoteDebugger.sh");
/* 1261 */             this.flZeusNxBenchmark = new File(currLocation + "/ZeusNxTools/ZeusNxBenchMark/ZeusNxBenchMark.sh");
/* 1262 */             this.flZeusNxMerger = new File(currLocation + "/ZeusNxTools/ZeusNxMerger/ZeusNxMerger.sh");
/*      */             break;
/*      */         } 
/*      */ 
/*      */       
/*      */       } 
/* 1268 */       if (this.flZeusNxMonitor != null) {
/* 1269 */         if (this.flZeusNxMonitor.exists()) {
/* 1270 */           if (this.zeusNxMonitor == null) {
/* 1271 */             addZeusNxMonitorMenu();
/*      */           }
/*      */         } else {
/* 1274 */           removeZeusNxMonitorMenu();
/*      */         } 
/*      */       }
/*      */       
/* 1278 */       if (this.flZeusNxRemoteDebugger != null) {
/* 1279 */         if (this.flZeusNxRemoteDebugger.exists()) {
/* 1280 */           if (this.zeusRemoteDebugger == null) {
/* 1281 */             addRemoteDebuggerMenu();
/*      */           }
/*      */         } else {
/* 1284 */           removeRemoteDebuggerMenu();
/*      */         } 
/*      */       }
/*      */       
/* 1288 */       if (this.flZeusNxBenchmark != null) {
/* 1289 */         if (this.flZeusNxBenchmark.exists()) {
/* 1290 */           if (this.zeusBenchMark == null) {
/* 1291 */             addZeusNxBenchMarkMenu();
/*      */           }
/*      */         } else {
/* 1294 */           removeZeusNxBenchMarkMenu();
/*      */         } 
/*      */       }
/*      */       
/* 1298 */       if (this.flZeusNxMerger != null) {
/* 1299 */         if (this.flZeusNxMerger.exists()) {
/* 1300 */           if (this.zeusMerger == null) {
/* 1301 */             addZeusNxMergerMenu();
/*      */           }
/*      */         } else {
/* 1304 */           removeZeusNxMergerMenu();
/*      */         }
/*      */       
/*      */       }
/* 1308 */     } catch (Exception ex) {
/* 1309 */       Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 1310 */       this.zeusSWStart.setEnabled(flag);
/* 1311 */       this.zeusSWStop.setEnabled(flag);
/* 1312 */       this.zeusSWRestart.setEnabled(flag);
/* 1313 */       this.zeusCCSWStart.setEnabled(flag);
/* 1314 */       this.zeusCCSWStop.setEnabled(flag);
/* 1315 */       this.zeusCCSWRestart.setEnabled(flag);
/* 1316 */       this.zeusDBSWStart.setEnabled(flag);
/* 1317 */       this.zeusDBSWStop.setEnabled(flag);
/* 1318 */       this.zeusDBSWRestart.setEnabled(flag);
/* 1319 */       this.zeusWDSWStart.setEnabled(flag);
/* 1320 */       this.zeusWDSWStop.setEnabled(flag);
/* 1321 */       this.zeusWDSWRestart.setEnabled(flag);
/* 1322 */       this.startAllServices.setEnabled(flag);
/* 1323 */       this.stopAllServices.setEnabled(flag);
/* 1324 */       this.restartAllServices.setEnabled(flag);
/* 1325 */       JOptionPane.showOptionDialog(this.parentFrame, LocaleMessage.getLocaleMessage("You_dont_have_administrator_privileges"), LocaleMessage.getLocaleMessage("full_version"), -1, 1, null, null, null);
/*      */     } 
/*      */   }
/*      */   
/*      */   private static Image createImage(String path) {
/* 1330 */     URL imageURL = MonitorZeus.class.getResource(path);
/*      */     
/* 1332 */     if (imageURL == null) {
/* 1333 */       System.err.println("Resource not found: " + path);
/* 1334 */       return null;
/*      */     } 
/* 1336 */     return (new ImageIcon(imageURL)).getImage();
/*      */   }
/*      */ 
/*      */   
/*      */   public void setTaskRunningStatus(boolean status) {
/* 1341 */     this.taskRunning = status;
/*      */   }
/*      */   
/*      */   public boolean getTaskRunningStatus() {
/* 1345 */     return this.taskRunning;
/*      */   }
/*      */   
/*      */   public JFrame getParentFrame() {
/* 1349 */     return this.parentFrame;
/*      */   }
/*      */   
/*      */   public static MonitorZeus getInstance() {
/* 1353 */     return instance;
/*      */   }
/*      */   
/*      */   private void removeRemoteDebuggerMenu() {
/* 1357 */     if (this.sws != null && this.zeusRemoteDebugger != null) {
/* 1358 */       this.sws.remove(this.zeusRemoteDebugger);
/* 1359 */       this.sws.remove(this.zeusRemoteDebuggerSepartor);
/* 1360 */       this.zeusRemoteDebugger = null;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void addRemoteDebuggerMenu() {
/* 1365 */     this.zeusRemoteDebugger = new JMenuItem(LocaleMessage.getLocaleMessage("Zeus_Nx_Remote_Debugger"));
/* 1366 */     this.zeusRemoteDebugger.setFont(this.font_12);
/* 1367 */     this.zeusRemoteDebugger.setIconTextGap(5);
/* 1368 */     this.zeusRemoteDebugger.setHorizontalTextPosition(11);
/* 1369 */     this.zeusRemoteDebuggerSepartor = new JSeparator();
/* 1370 */     this.sws.add(this.zeusRemoteDebuggerSepartor, ++count);
/* 1371 */     this.sws.insert(this.zeusRemoteDebugger, ++count);
/*      */     
/* 1373 */     this.zeusRemoteDebugger.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e) {
/*      */             try {
/* 1377 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/* 1378 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(11, MonitorZeus.this.flZeusNxRemoteDebugger, null, null, null));
/* 1379 */                 MonitorZeus.this.setTaskRunningStatus(true);
/* 1380 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Opening_Zeus_Nx_Remote_Debugger_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/* 1382 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/* 1383 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/* 1384 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/* 1387 */             } catch (IOException ex) {
/* 1388 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 1389 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Remote_Debugger"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/* 1390 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */   }
/*      */   
/*      */   private void removeZeusNxMonitorMenu() {
/* 1397 */     if (this.sws != null && this.zeusNxMonitor != null) {
/* 1398 */       this.sws.remove(this.zeusNxMonitor);
/* 1399 */       this.sws.remove(this.zeusNxMonitorSepartor);
/* 1400 */       this.zeusNxMonitor = null;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void addZeusNxMonitorMenu() {
/* 1405 */     this.zeusNxMonitor = new JMenuItem(LocaleMessage.getLocaleMessage("Zeus_Nx_Monitor"));
/* 1406 */     this.zeusNxMonitor.setFont(this.font_12);
/* 1407 */     this.zeusNxMonitor.setIconTextGap(5);
/* 1408 */     this.zeusNxMonitor.setHorizontalTextPosition(11);
/* 1409 */     this.zeusNxMonitorSepartor = new JSeparator();
/* 1410 */     this.sws.add(this.zeusNxMonitorSepartor, ++count);
/* 1411 */     this.sws.insert(this.zeusNxMonitor, ++count);
/*      */     
/* 1413 */     this.zeusNxMonitor.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e)
/*      */           {
/*      */             try {
/* 1418 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/* 1419 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(9, MonitorZeus.this.flZeusNxMonitor, null, null, null));
/* 1420 */                 MonitorZeus.this.setTaskRunningStatus(true);
/* 1421 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Opening_Zeus_Nx_Monitor_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/* 1423 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/* 1424 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/* 1425 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/* 1428 */             } catch (IOException ex) {
/* 1429 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 1430 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Monitor"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/* 1431 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */   }
/*      */   
/*      */   private void removeZeusNxBenchMarkMenu() {
/* 1438 */     if (this.sws != null && this.zeusBenchMark != null) {
/* 1439 */       this.sws.remove(this.zeusBenchMark);
/* 1440 */       this.sws.remove(this.zeusBenchMarkSepartor);
/* 1441 */       this.zeusBenchMark = null;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void addZeusNxBenchMarkMenu() {
/* 1446 */     this.zeusBenchMark = new JMenuItem(LocaleMessage.getLocaleMessage("Zeus_Nx_Benchmark"));
/* 1447 */     this.zeusBenchMark.setFont(this.font_12);
/* 1448 */     this.zeusBenchMark.setIconTextGap(5);
/* 1449 */     this.zeusBenchMark.setHorizontalTextPosition(11);
/* 1450 */     this.zeusBenchMarkSepartor = new JSeparator();
/* 1451 */     this.sws.add(this.zeusBenchMarkSepartor, ++count);
/* 1452 */     this.sws.insert(this.zeusBenchMark, ++count);
/*      */     
/* 1454 */     this.zeusBenchMark.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e)
/*      */           {
/*      */             try {
/* 1459 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/* 1460 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(20, MonitorZeus.this.flZeusNxBenchmark, null, null, null));
/* 1461 */                 MonitorZeus.this.setTaskRunningStatus(true);
/* 1462 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Opening_Zeus_Nx_Benchmark_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/* 1464 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/* 1465 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/* 1466 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/* 1469 */             } catch (IOException ex) {
/* 1470 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 1471 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Benchmark"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/* 1472 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */   }
/*      */   
/*      */   private void removeZeusNxMergerMenu() {
/* 1479 */     if (this.sws != null && this.zeusMerger != null) {
/* 1480 */       this.sws.remove(this.zeusMerger);
/* 1481 */       this.sws.remove(this.zeusMergerSepartor);
/* 1482 */       this.zeusMerger = null;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void addZeusNxMergerMenu() {
/* 1487 */     this.zeusMerger = new JMenuItem(LocaleMessage.getLocaleMessage("Zeus_Nx_Migrator"));
/* 1488 */     this.zeusMerger.setFont(this.font_12);
/* 1489 */     this.zeusMerger.setIconTextGap(5);
/* 1490 */     this.zeusMerger.setHorizontalTextPosition(11);
/* 1491 */     this.zeusMergerSepartor = new JSeparator();
/* 1492 */     this.sws.add(this.zeusMergerSepartor, ++count);
/* 1493 */     this.sws.insert(this.zeusMerger, ++count);
/*      */     
/* 1495 */     this.zeusMerger.addActionListener(new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e)
/*      */           {
/*      */             try {
/* 1500 */               if (!MonitorZeus.this.getTaskRunningStatus()) {
/* 1501 */                 ProcessingPopup.setWorker(new MonitorZeusWorker(21, MonitorZeus.this.flZeusNxMerger, null, null, null));
/* 1502 */                 MonitorZeus.this.setTaskRunningStatus(true);
/* 1503 */                 JFrame jFrame = ProcessingPopup.createProcessingPopup(LocaleMessage.getLocaleMessage("Opening_Zeus_Nx_Migrator_Please_wait"), LocaleMessage.getLocaleMessage("Please_wait"), true);
/*      */               }
/* 1505 */               else if (ProcessingPopup.getActivePopupFrame() != null) {
/* 1506 */                 ProcessingPopup.getActivePopupFrame().requestFocus();
/* 1507 */                 ProcessingPopup.getActivePopupFrame().toFront();
/*      */               }
/*      */             
/* 1510 */             } catch (IOException ex) {
/* 1511 */               Logger.getLogger(MonitorZeus.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 1512 */               JOptionPane.showOptionDialog(MonitorZeus.this.parentFrame, LocaleMessage.getLocaleMessage("Error_while_opening_Zeus_Nx_Migrator"), LocaleMessage.getLocaleMessage("full_version"), -1, 0, null, null, null);
/* 1513 */               MonitorZeus.this.setTaskRunningStatus(false);
/*      */             } 
/*      */           }
/*      */         });
/*      */   }
/*      */   
/*      */   private void reloadMenuText() {
/* 1520 */     this.zeusSWStart.setText(LocaleMessage.getLocaleMessage("Start"));
/* 1521 */     this.zeusSWStop.setText(LocaleMessage.getLocaleMessage("Stop"));
/* 1522 */     this.zeusSWRestart.setText(LocaleMessage.getLocaleMessage("Restart"));
/* 1523 */     this.disconnectModules.setText(LocaleMessage.getLocaleMessage("Disconnect_Modules"));
/* 1524 */     this.zeusCCSWStart.setText(LocaleMessage.getLocaleMessage("Start"));
/* 1525 */     this.zeusCCSWStop.setText(LocaleMessage.getLocaleMessage("Stop"));
/* 1526 */     this.zeusCCSWRestart.setText(LocaleMessage.getLocaleMessage("Restart"));
/* 1527 */     this.zeusDBSWStart.setText(LocaleMessage.getLocaleMessage("Start"));
/* 1528 */     this.zeusDBSWStop.setText(LocaleMessage.getLocaleMessage("Stop"));
/* 1529 */     this.zeusDBSWRestart.setText(LocaleMessage.getLocaleMessage("Restart"));
/* 1530 */     this.zeusWDSWStart.setText(LocaleMessage.getLocaleMessage("Start"));
/* 1531 */     this.zeusWDSWStop.setText(LocaleMessage.getLocaleMessage("Stop"));
/* 1532 */     this.zeusWDSWRestart.setText(LocaleMessage.getLocaleMessage("Restart"));
/* 1533 */     this.stopAllServices.setText(LocaleMessage.getLocaleMessage("Stop_All"));
/* 1534 */     this.startAllServices.setText(LocaleMessage.getLocaleMessage("Start_All"));
/* 1535 */     this.restartAllServices.setText(LocaleMessage.getLocaleMessage("Restart_All"));
/* 1536 */     this.backupNow.setText(LocaleMessage.getLocaleMessage("Backup"));
/* 1537 */     this.restoreNow.setText(LocaleMessage.getLocaleMessage("Restore"));
/* 1538 */     this.cleanup.setText(LocaleMessage.getLocaleMessage("Cleanup"));
/* 1539 */     this.maintainance.setText(LocaleMessage.getLocaleMessage("Zeus_Nx_Database_Tools"));
/* 1540 */     this.sws.setText(LocaleMessage.getLocaleMessage("Softwares"));
/* 1541 */     this.srvs.setText(LocaleMessage.getLocaleMessage("Services"));
/* 1542 */     this.exit.setText(LocaleMessage.getLocaleMessage("Exit"));
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\MonitorZeus.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */