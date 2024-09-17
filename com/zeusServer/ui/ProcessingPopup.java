/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Window;
/*     */ import java.beans.PropertyChangeEvent;
/*     */ import java.beans.PropertyChangeListener;
/*     */ import java.io.IOException;
/*     */ import java.net.URL;
/*     */ import javax.imageio.ImageIO;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.GroupLayout;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.SwingWorker;
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
/*     */ public class ProcessingPopup
/*     */ {
/*     */   private static ImageIcon processingIcon;
/*     */   private static JFrame dialog;
/*     */   private static Worker<Boolean, String> worker;
/*     */   private static Worker<Integer, String> worker1;
/*     */   
/*     */   public static void setWorker(Worker<Boolean, String> worker) {
/*  39 */     ProcessingPopup.worker = worker;
/*     */     
/*  41 */     ProcessingPopup.worker.addPropertyChangeListener(new PropertyChangeListener()
/*     */         {
/*     */           public void propertyChange(PropertyChangeEvent evt) {
/*  44 */             if (evt.getPropertyName().equals("state") && ProcessingPopup
/*  45 */               .worker.getState().equals(SwingWorker.StateValue.DONE)) {
/*  46 */               ProcessingPopup.dialog.setVisible(false);
/*  47 */               ProcessingPopup.dialog.dispose();
/*  48 */               MonitorZeus.getInstance().setTaskRunningStatus(false);
/*     */             } 
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */   
/*     */   public static void setWorker1(Worker<Integer, String> worker) {
/*  56 */     worker1 = worker;
/*     */     
/*  58 */     worker1.addPropertyChangeListener(new PropertyChangeListener()
/*     */         {
/*     */           public void propertyChange(PropertyChangeEvent evt) {
/*  61 */             if (evt.getPropertyName().equals("state") && ProcessingPopup
/*  62 */               .worker1.getState().equals(SwingWorker.StateValue.DONE)) {
/*  63 */               ProcessingPopup.dialog.setVisible(false);
/*  64 */               ProcessingPopup.dialog.dispose();
/*  65 */               MonitorZeus.getInstance().setTaskRunningStatus(false);
/*     */             } 
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */   
/*     */   public static JFrame createProcessingPopup(String processingMessage, String plsWaitMessage, boolean worker) throws IOException {
/*  73 */     dialog = new JFrame();
/*  74 */     dialog.setAutoRequestFocus(true);
/*  75 */     dialog.getRootPane().setBorder(BorderFactory.createEtchedBorder(0));
/*  76 */     dialog.setUndecorated(true);
/*  77 */     dialog.setType(Window.Type.UTILITY);
/*  78 */     dialog.setMinimumSize(new Dimension(300, 200));
/*  79 */     JLabel processing = new JLabel(getProcessingImage());
/*  80 */     JLabel msg = new JLabel();
/*  81 */     JLabel plsWaitLable = new JLabel();
/*  82 */     if (GlobalVariables.currentPlatform == Enums.Platform.LINUX) {
/*  83 */       msg.setText(processingMessage);
/*  84 */       plsWaitLable.setText(plsWaitMessage);
/*     */     } else {
/*  86 */       msg.setText("<html> &nbsp;&nbsp;" + processingMessage + "</html>");
/*  87 */       plsWaitLable.setText("<html> &nbsp;&nbsp;" + plsWaitMessage + "</html>");
/*     */     } 
/*  89 */     msg.setForeground(Color.DARK_GRAY);
/*     */     
/*  91 */     plsWaitLable.setForeground(Color.DARK_GRAY);
/*     */     
/*  93 */     BackgroundPanel bgpSettings = new BackgroundPanel(ImageIO.read(ProcessingPopup.class.getClassLoader().getResource("images/Zeus_PG_BG.png")));
/*  94 */     JPanel pnlSettings = new JPanel();
/*  95 */     pnlSettings.setVisible(true);
/*  96 */     GroupLayout gl = new GroupLayout(pnlSettings);
/*  97 */     pnlSettings.setLayout(gl);
/*  98 */     gl.setHorizontalGroup(gl
/*  99 */         .createParallelGroup(GroupLayout.Alignment.LEADING)
/* 100 */         .addGroup(gl.createSequentialGroup()
/* 101 */           .addContainerGap(-1, 32767)
/* 102 */           .addGroup(gl.createParallelGroup(GroupLayout.Alignment.TRAILING)
/* 103 */             .addGroup(GroupLayout.Alignment.LEADING, gl.createSequentialGroup()
/* 104 */               .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING, false)
/* 105 */                 .addComponent(msg, GroupLayout.Alignment.LEADING)
/* 106 */                 .addComponent(plsWaitLable, GroupLayout.Alignment.LEADING)
/* 107 */                 .addComponent(processing, GroupLayout.Alignment.LEADING))
/* 108 */               .addContainerGap(-1, 32767)))));
/* 109 */     gl.setVerticalGroup(gl
/* 110 */         .createParallelGroup(GroupLayout.Alignment.LEADING)
/* 111 */         .addGroup(gl.createSequentialGroup()
/* 112 */           .addContainerGap(110, 32767)
/* 113 */           .addComponent(processing)
/* 114 */           .addContainerGap(0, 32767)
/* 115 */           .addComponent(msg)
/* 116 */           .addContainerGap(0, 32767)
/* 117 */           .addComponent(plsWaitLable)
/* 118 */           .addContainerGap()));
/*     */     
/* 120 */     GroupLayout layouts = new GroupLayout(dialog.getContentPane());
/* 121 */     dialog.getContentPane().setLayout(layouts);
/* 122 */     bgpSettings.add(pnlSettings);
/*     */     
/* 124 */     layouts.setHorizontalGroup(layouts
/* 125 */         .createParallelGroup(GroupLayout.Alignment.LEADING)
/* 126 */         .addComponent(bgpSettings, -1, -1, 32767));
/* 127 */     layouts.setVerticalGroup(layouts
/* 128 */         .createParallelGroup(GroupLayout.Alignment.LEADING)
/* 129 */         .addComponent(bgpSettings, -1, -1, 32767));
/*     */     
/* 131 */     dialog.setVisible(true);
/* 132 */     dialog.setLocationRelativeTo(null);
/* 133 */     dialog.setDefaultCloseOperation(2);
/* 134 */     if (worker) {
/* 135 */       ProcessingPopup.worker.execute();
/*     */     } else {
/* 137 */       worker1.execute();
/*     */     } 
/* 139 */     return dialog;
/*     */   }
/*     */   
/*     */   public static ImageIcon getProcessingImage() {
/* 143 */     URL cldr = ProcessingPopup.class.getClassLoader().getResource("images/progress.gif");
/* 144 */     processingIcon = new ImageIcon(cldr);
/* 145 */     return processingIcon;
/*     */   }
/*     */   
/*     */   public static JFrame getActivePopupFrame() {
/* 149 */     if (dialog != null) {
/* 150 */       return dialog;
/*     */     }
/* 152 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\ProcessingPopup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */