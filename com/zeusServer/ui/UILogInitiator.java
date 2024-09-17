/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.DBGeneral.DerbyDBBackup;
/*     */ import com.zeusServer.tblConnections.TblThreadsCommReceiversCSD;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.MonitoringInfo;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.net.MalformedURLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.swing.ImageIcon;
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
/*     */ public class UILogInitiator
/*     */ {
/*  37 */   EventViewer evtViewer = null;
/*  38 */   MonitoringViewer mntViewer = null;
/*  39 */   WidgetFactory wf = new WidgetFactory();
/*     */   
/*     */   private static boolean internetStatusChanged;
/*     */   private static boolean csdStatusChanged;
/*     */   private static boolean monitoringStationStatusChanged;
/*     */   private static ConcurrentHashMap<String, WidgetFactory.ToggledImage> comports;
/*     */   private static ConcurrentHashMap<String, WidgetFactory.LabeledImage> signalLevelImgs;
/*     */   private static WidgetFactory.ToggledImage monitorImage;
/*     */   private static WidgetFactory.ToggledImage internetImage;
/*     */   private static WidgetFactory.ToggledImage csdImage;
/*     */   private static WidgetFactory.LabeledImage csdSignalLevel;
/*  50 */   private static short oldSignalRange = 0;
/*     */   private ScheduledFuture buzzerSF;
/*     */   
/*     */   public UILogInitiator() {
/*     */     try {
/*  55 */       initUI();
/*  56 */     } catch (MalformedURLException ex) {
/*  57 */       Logger.getLogger(UILogInitiator.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void initUI() throws MalformedURLException {
/*  62 */     int size = (ZeusServerCfg.getInstance().getMonitoringInfo() != null) ? ZeusServerCfg.getInstance().getMonitoringInfo().size() : 0;
/*  63 */     int csdSize = TblThreadsCommReceiversCSD.getInstance().size();
/*  64 */     comports = new ConcurrentHashMap<>(size + csdSize);
/*  65 */     signalLevelImgs = new ConcurrentHashMap<>(csdSize);
/*  66 */     this.wf.getClass(); internetImage = new WidgetFactory.ToggledImage(this.wf, "images/Internet_On.png", "images/Internet_Off.png", "Internet Available", "Internet Not Available");
/*  67 */     internetImage.getToggleImage(true, false, null);
/*     */ 
/*     */     
/*  70 */     if (ZeusServerCfg.getInstance().getRecipientsCSD() != null && ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/*  71 */       String[] rcvrCSD = ZeusServerCfg.getInstance().getRecipientsCSD().split(";");
/*  72 */       for (String rcvr : rcvrCSD) {
/*  73 */         String port = rcvr.substring(0, rcvr.indexOf(","));
/*  74 */         this.wf.getClass(); WidgetFactory.ToggledImage csdti = new WidgetFactory.ToggledImage(this.wf, "images/CSD_On.png", "images/CSD_Off.png", port + " - CSD Receiver Connected", port + " - CSD Receiver Disconnected");
/*  75 */         csdti.getToggleImage(true, false, null);
/*     */         
/*  77 */         comports.put(port, csdti);
/*  78 */         this.wf.getClass(); WidgetFactory.LabeledImage csdSignalLevel = new WidgetFactory.LabeledImage(this.wf);
/*  79 */         csdSignalLevel.setImage(new ImageIcon(getClass().getClassLoader().getResource("images/signal_0.png")), "0");
/*  80 */         csdSignalLevel.getLabeledImage();
/*  81 */         signalLevelImgs.put(port, csdSignalLevel);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/*  86 */     if (ZeusServerCfg.getInstance().getMonitoringInfo() != null) {
/*  87 */       for (Map.Entry<String, MonitoringInfo> receiver : (Iterable<Map.Entry<String, MonitoringInfo>>)ZeusServerCfg.getInstance().getMonitoringInfo().entrySet()) {
/*  88 */         this.wf.getClass(); WidgetFactory.ToggledImage msti = new WidgetFactory.ToggledImage(this.wf, "images/Monitoring_Station_On.png", "images/Monitoring_Station_Off.png", (String)receiver.getKey() + " - Monitor Station Connected", (String)receiver.getKey() + " - Monitor Station Disconnected");
/*  89 */         msti.getToggleImage(true, true, receiver.getKey());
/*     */         
/*  91 */         comports.put(receiver.getKey(), msti);
/*     */       } 
/*     */     }
/*     */     
/*  95 */     this.mntViewer = new MonitoringViewer();
/*     */     
/*  97 */     Runnable buzzerTask = new Runnable()
/*     */       {
/*     */         public void run() {
/*     */           try {
/* 101 */             if (GlobalVariables.buzzerActivated)
/*     */             {
/* 103 */               if (!DerbyDBBackup.backupModeActivated) {
/* 104 */                 byte[] log = new byte[4];
/* 105 */                 log[0] = -108;
/* 106 */                 byte[] len = Functions.get2ByteArrayFromInt(1);
/* 107 */                 log[1] = len[0];
/* 108 */                 log[2] = len[1];
/* 109 */                 log[3] = 2;
/* 110 */                 Functions.pumpMessage2RemoteUI(0, log, null, 148, 0, 0);
/*     */               } 
/*     */             }
/* 113 */           } catch (Exception exception) {}
/*     */         }
/*     */       };
/*     */ 
/*     */ 
/*     */     
/* 119 */     this.buzzerSF = Functions.addRunnable2ScheduleExecutor(buzzerTask, 0L, 5000L, TimeUnit.MILLISECONDS);
/*     */   }
/*     */   
/*     */   public static void toggleImageById(short id, boolean state, String port) {
/* 123 */     switch (id) {
/*     */       
/*     */       case 1:
/* 126 */         if (port != null && comports != null && comports.containsKey(port)) {
/* 127 */           if (state) {
/* 128 */             ((WidgetFactory.ToggledImage)comports.get(port)).getCurrentLabel().firePropertyChange("state", false, state);
/*     */           } else {
/* 130 */             ((WidgetFactory.ToggledImage)comports.get(port)).getCurrentLabel().firePropertyChange("state", true, state);
/*     */           } 
/* 132 */           monitoringStationStatusChanged = true;
/*     */         } 
/*     */         break;
/*     */       case 2:
/* 136 */         if (internetImage != null) {
/* 137 */           if (internetImage.getCurrentLabel() == null) {
/* 138 */             internetImage.getToggleImage(true, false, null);
/*     */           }
/* 140 */           if (state) {
/* 141 */             internetImage.getCurrentLabel().firePropertyChange("state", false, state);
/*     */           } else {
/* 143 */             internetImage.getCurrentLabel().firePropertyChange("state", true, state);
/*     */           } 
/* 145 */           internetStatusChanged = true;
/*     */         } 
/*     */         break;
/*     */       
/*     */       case 3:
/* 150 */         if (port != null && comports != null && comports.containsKey(port) && signalLevelImgs != null && signalLevelImgs.containsKey(port)) {
/* 151 */           if (state) {
/* 152 */             ((WidgetFactory.ToggledImage)comports.get(port)).getCurrentLabel().firePropertyChange("state", false, state);
/* 153 */             ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).getLabeledImage().setEnabled(false);
/*     */           } else {
/* 155 */             ((WidgetFactory.ToggledImage)comports.get(port)).getCurrentLabel().firePropertyChange("state", true, state);
/* 156 */             ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).getLabeledImage().setEnabled(false);
/*     */           } 
/*     */           
/* 159 */           csdStatusChanged = true;
/*     */         } 
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void setSignalImageByLevel(int signalLevel, String port) {
/* 167 */     if (port != null && signalLevelImgs.containsKey(port)) {
/* 168 */       if (signalLevel == 0) {
/* 169 */         ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).setImage(new ImageIcon(UILogInitiator.class.getClassLoader().getResource("images/signal_0.png")), "0");
/* 170 */         ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).getLabeledImage().setEnabled(false);
/* 171 */         ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).setSignalLevel(0);
/* 172 */         csdStatusChanged = true;
/*     */       } else {
/*     */         String imageName;
/* 175 */         ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).setSignalLevel(signalLevel);
/* 176 */         csdStatusChanged = true;
/* 177 */         switch (signalLevel / 8) {
/*     */           case 1:
/* 179 */             imageName = "images/signal_1.png";
/*     */             break;
/*     */           case 2:
/* 182 */             imageName = "images/signal_2.png";
/*     */             break;
/*     */           case 3:
/* 185 */             imageName = "images/signal_3.png";
/*     */             break;
/*     */           case 4:
/* 188 */             imageName = "images/signal_3.png";
/*     */             break;
/*     */           default:
/* 191 */             imageName = "images/signal_0.png";
/*     */             break;
/*     */         } 
/* 194 */         ImageIcon ii = new ImageIcon(UILogInitiator.class.getClassLoader().getResource(imageName));
/* 195 */         ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).setImage(ii, String.valueOf(signalLevel));
/* 196 */         ((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).getLabeledImage().setEnabled(true);
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   public static List<Byte> getMonitoringIconsInitialStatus(boolean flag) {
/* 202 */     List<Byte> data = new ArrayList<>();
/* 203 */     if (flag) {
/* 204 */       if (ZeusServerCfg.getInstance().getServersTestInternet() != null && ZeusServerCfg.getInstance().getServersTestInternet().length() > 0) {
/* 205 */         data.add(Byte.valueOf((byte)24));
/* 206 */         data.add(Byte.valueOf((byte)1));
/* 207 */         data.add(Byte.valueOf((byte)0));
/*     */       } 
/* 209 */       if (ZeusServerCfg.getInstance().getRecipientsCSD() != null && ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/* 210 */         String[] rcvrCSD = ZeusServerCfg.getInstance().getRecipientsCSD().split(";");
/* 211 */         for (String rcvr : rcvrCSD) {
/* 212 */           String port = rcvr.substring(0, rcvr.indexOf(","));
/* 213 */           if (GlobalVariables.currentPlatform == Enums.Platform.ARM && Functions.zeusBoxSerialPortNames.containsKey(port)) {
/* 214 */             port = (String)Functions.zeusBoxSerialPortNames.get(port);
/*     */           }
/* 216 */           data.add(Byte.valueOf((byte)25));
/* 217 */           data.add(Byte.valueOf((byte)(port.length() + 2)));
/* 218 */           for (byte b : port.getBytes()) {
/* 219 */             data.add(Byte.valueOf(b));
/*     */           }
/* 221 */           data.add(Byte.valueOf((byte)0));
/* 222 */           data.add(Byte.valueOf((byte)0));
/*     */         } 
/*     */       } 
/*     */       
/* 226 */       if (ZeusServerCfg.getInstance().getMonitoringInfo() != null) {
/* 227 */         for (Map.Entry<String, MonitoringInfo> receiver : (Iterable<Map.Entry<String, MonitoringInfo>>)ZeusServerCfg.getInstance().getMonitoringInfo().entrySet()) {
/* 228 */           data.add(Byte.valueOf((byte)32));
/* 229 */           String port = receiver.getKey();
/* 230 */           if (GlobalVariables.currentPlatform == Enums.Platform.ARM && Functions.zeusBoxSerialPortNames.containsKey(port)) {
/* 231 */             port = (String)Functions.zeusBoxSerialPortNames.get(port);
/*     */           }
/* 233 */           data.add(Byte.valueOf((byte)(port.length() + 2)));
/* 234 */           if (((MonitoringInfo)receiver.getValue()).getReceiverType().intValue() == Enums.EnumTipoReceptora.SURGARD_VIA_TCPIP.getType()) {
/* 235 */             data.add(Byte.valueOf((byte)0));
/*     */           } else {
/* 237 */             data.add(Byte.valueOf((byte)1));
/*     */           } 
/* 239 */           for (byte b : port.getBytes()) {
/* 240 */             data.add(Byte.valueOf(b));
/*     */           }
/* 242 */           data.add(Byte.valueOf((byte)0));
/*     */         } 
/*     */       } else {
/*     */         
/* 246 */         data.add(Byte.valueOf((byte)33));
/* 247 */         data.add(Byte.valueOf((byte)0));
/*     */       } 
/*     */     } else {
/* 250 */       if (ZeusServerCfg.getInstance().getServersTestInternet() != null && ZeusServerCfg.getInstance().getServersTestInternet().length() > 0) {
/* 251 */         data.add(Byte.valueOf((byte)24));
/* 252 */         data.add(Byte.valueOf((byte)1));
/* 253 */         data.add(Byte.valueOf((byte)(internetImage.isStatus() ? 1 : 0)));
/*     */       } 
/*     */       
/* 256 */       if (ZeusServerCfg.getInstance().getRecipientsCSD() != null && ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/* 257 */         String[] rcvrCSD = ZeusServerCfg.getInstance().getRecipientsCSD().split(";");
/* 258 */         for (String rcvr : rcvrCSD) {
/*     */           
/* 260 */           String port = rcvr.substring(0, rcvr.indexOf(","));
/* 261 */           String prt = port;
/* 262 */           if (GlobalVariables.currentPlatform == Enums.Platform.ARM && Functions.zeusBoxSerialPortNames.containsKey(port)) {
/* 263 */             prt = (String)Functions.zeusBoxSerialPortNames.get(port);
/*     */           }
/* 265 */           data.add(Byte.valueOf((byte)25));
/* 266 */           data.add(Byte.valueOf((byte)(prt.length() + 2)));
/* 267 */           for (byte b : prt.getBytes()) {
/* 268 */             data.add(Byte.valueOf(b));
/*     */           }
/* 270 */           data.add(Byte.valueOf((byte)(((WidgetFactory.ToggledImage)comports.get(port)).isStatus() ? 1 : 0)));
/* 271 */           data.add(Byte.valueOf((byte)((WidgetFactory.LabeledImage)signalLevelImgs.get(port)).getSignalLevel()));
/*     */         } 
/*     */       } 
/*     */       
/* 275 */       if (ZeusServerCfg.getInstance().getMonitoringInfo() != null) {
/* 276 */         for (Map.Entry<String, MonitoringInfo> receiver : (Iterable<Map.Entry<String, MonitoringInfo>>)ZeusServerCfg.getInstance().getMonitoringInfo().entrySet()) {
/* 277 */           data.add(Byte.valueOf((byte)32));
/* 278 */           String port = receiver.getKey();
/* 279 */           if (GlobalVariables.currentPlatform == Enums.Platform.ARM && Functions.zeusBoxSerialPortNames.containsKey(port)) {
/* 280 */             port = (String)Functions.zeusBoxSerialPortNames.get(port);
/*     */           }
/* 282 */           data.add(Byte.valueOf((byte)(port.length() + 2)));
/* 283 */           if (((MonitoringInfo)receiver.getValue()).getReceiverType().intValue() == Enums.EnumTipoReceptora.SURGARD_VIA_TCPIP.getType()) {
/* 284 */             data.add(Byte.valueOf((byte)0));
/*     */           } else {
/* 286 */             data.add(Byte.valueOf((byte)1));
/*     */           } 
/* 288 */           for (byte b : port.getBytes()) {
/* 289 */             data.add(Byte.valueOf(b));
/*     */           }
/* 291 */           data.add(Byte.valueOf((byte)(((WidgetFactory.ToggledImage)comports.get(receiver.getKey())).isStatus() ? 1 : 0)));
/*     */         } 
/*     */       } else {
/* 294 */         data.add(Byte.valueOf((byte)33));
/* 295 */         data.add(Byte.valueOf((byte)0));
/*     */       } 
/*     */     } 
/* 298 */     return data;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\UILogInitiator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */