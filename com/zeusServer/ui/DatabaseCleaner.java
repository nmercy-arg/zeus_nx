/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import java.awt.HeadlessException;
/*     */ import java.awt.Image;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.WindowAdapter;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.beans.PropertyChangeEvent;
/*     */ import java.beans.PropertyChangeListener;
/*     */ import java.io.IOException;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.imageio.ImageIO;
/*     */ import javax.swing.GroupLayout;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JSpinner;
/*     */ import javax.swing.LayoutStyle;
/*     */ import javax.swing.SpinnerDateModel;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class DatabaseCleaner
/*     */   extends JFrame
/*     */ {
/*     */   private JButton cleanButton;
/*     */   private JCheckBox backupRequired;
/*     */   private JLabel lblDelete;
/*     */   private JSpinner timeSpinner;
/*     */   private JLabel progressBar;
/*     */   private Image frameIcon;
/*     */   private static DatabaseCleaner instance;
/*     */   
/*     */   public DatabaseCleaner() throws HeadlessException {
/*  47 */     instance = this;
/*  48 */     this.lblDelete = new JLabel();
/*  49 */     this.timeSpinner = new JSpinner(new SpinnerDateModel());
/*  50 */     this.backupRequired = new JCheckBox();
/*  51 */     this.cleanButton = new JButton();
/*  52 */     this.progressBar = new JLabel(ProcessingPopup.getProcessingImage());
/*  53 */     JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(this.timeSpinner, "dd-MM-yyyy HH:mm:ss");
/*  54 */     this.timeSpinner.setEditor(timeEditor);
/*  55 */     Calendar cal = Calendar.getInstance();
/*  56 */     cal.set(2, cal.get(2));
/*  57 */     this.timeSpinner.setValue(cal.getTime());
/*  58 */     this.progressBar.setVisible(false);
/*     */     
/*  60 */     setDefaultCloseOperation(0);
/*  61 */     addWindowListener(new WindowAdapter()
/*     */         {
/*     */           public void windowClosing(WindowEvent event) {
/*  64 */             DatabaseCleaner.this.setVisible(false);
/*  65 */             MonitorZeus.getInstance().setTaskRunningStatus(false);
/*     */           }
/*     */         });
/*  68 */     this.lblDelete.setText(LocaleMessage.getLocaleMessage("Delete_prior_to"));
/*  69 */     this.backupRequired.setText(LocaleMessage.getLocaleMessage("Create_a_database_backup_before_cleanup_process"));
/*  70 */     this.cleanButton.setText(LocaleMessage.getLocaleMessage("Cleanup"));
/*  71 */     setIconImage(getFrameIcon());
/*  72 */     GroupLayout layout = new GroupLayout(getContentPane());
/*  73 */     getContentPane().setLayout(layout);
/*     */     
/*  75 */     this.cleanButton.addActionListener(new ActionListener()
/*     */         {
/*     */           public void actionPerformed(ActionEvent e) {
/*  78 */             DatabaseCleaner.this.enableDisableFields(false);
/*     */             try {
/*  80 */               CleanupWorker worker = new CleanupWorker((Date)DatabaseCleaner.this.timeSpinner.getValue(), DatabaseCleaner.this.backupRequired.isSelected());
/*  81 */               worker.addPropertyChangeListener(new PropertyChangeListener()
/*     */                   {
/*     */                     public void propertyChange(PropertyChangeEvent evt) {}
/*     */                   });
/*     */               
/*  86 */               worker.execute();
/*     */             }
/*  88 */             catch (Exception ex) {
/*  89 */               Logger.getLogger(DatabaseCleaner.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */             } 
/*     */           }
/*     */         });
/*     */     
/*  94 */     layout.setHorizontalGroup(layout
/*  95 */         .createParallelGroup(GroupLayout.Alignment.LEADING)
/*  96 */         .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
/*  97 */           .addComponent(this.cleanButton, GroupLayout.Alignment.CENTER, -1, -1, 32767)
/*  98 */           .addComponent(this.progressBar, GroupLayout.Alignment.CENTER, -1, -1, 32767))
/*  99 */         .addGroup(layout.createSequentialGroup()
/* 100 */           .addGap(15, 15, 15)
/* 101 */           .addComponent(this.lblDelete)
/* 102 */           .addComponent(this.timeSpinner, -2, 155, -2))
/* 103 */         .addGroup(layout.createSequentialGroup()
/* 104 */           .addGap(15, 15, 15)
/* 105 */           .addComponent(this.backupRequired)));
/*     */     
/* 107 */     layout.setVerticalGroup(layout
/* 108 */         .createParallelGroup(GroupLayout.Alignment.LEADING)
/* 109 */         .addGroup(layout.createSequentialGroup()
/* 110 */           .addGap(25, 25, 25)
/* 111 */           .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
/* 112 */             .addComponent(this.timeSpinner, -1, -1, 32767)
/* 113 */             .addComponent(this.lblDelete, -1, -1, 32767))
/* 114 */           .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
/* 115 */           .addComponent(this.backupRequired)
/* 116 */           .addGap(15, 15, 15)
/* 117 */           .addComponent(this.cleanButton)
/* 118 */           .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
/* 119 */           .addComponent(this.progressBar, -2, -1, -2)
/* 120 */           .addContainerGap(25, 32767)));
/*     */     
/* 122 */     pack();
/* 123 */     setLocationRelativeTo(null);
/* 124 */     setTitle(LocaleMessage.getLocaleMessage("Zeus_Nx_Database_Cleaner"));
/* 125 */     setResizable(false);
/* 126 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   
/*     */   public void enableDisableFields(boolean state) {
/* 131 */     this.backupRequired.setEnabled(state);
/* 132 */     this.timeSpinner.setEnabled(state);
/* 133 */     this.cleanButton.setEnabled(state);
/* 134 */     this.lblDelete.setEnabled(state);
/* 135 */     this.progressBar.setVisible(!state);
/*     */   }
/*     */ 
/*     */   
/*     */   public Image getFrameIcon() {
/* 140 */     if (this.frameIcon == null) {
/*     */       try {
/* 142 */         this.frameIcon = ImageIO.read(DatabaseCleaner.class.getClassLoader().getResource("images/Zeus_16x16.png"));
/* 143 */       } catch (IOException ex) {
/* 144 */         Logger.getLogger(DatabaseCleaner.class.getName()).log(Level.SEVERE, (String)null, ex);
/* 145 */         return null;
/*     */       } 
/*     */     }
/* 148 */     return this.frameIcon;
/*     */   }
/*     */ 
/*     */   
/*     */   public void updateMessages2Locale() {
/* 153 */     this.lblDelete.setText(LocaleMessage.getLocaleMessage("Delete_prior_to"));
/* 154 */     this.backupRequired.setText(LocaleMessage.getLocaleMessage("Create_a_database_backup_before_cleanup_process"));
/* 155 */     this.cleanButton.setText(LocaleMessage.getLocaleMessage("Cleanup"));
/* 156 */     setTitle(LocaleMessage.getLocaleMessage("Zeus_Nx_Database_Cleaner"));
/*     */   }
/*     */ 
/*     */   
/*     */   public static DatabaseCleaner getInstance() {
/* 161 */     return instance;
/*     */   }
/*     */ 
/*     */   
/*     */   public void showPopup() {
/* 166 */     Calendar cal = Calendar.getInstance();
/* 167 */     cal.set(2, cal.get(2));
/* 168 */     this.timeSpinner.setValue(cal.getTime());
/* 169 */     setVisible(true);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\DatabaseCleaner.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */