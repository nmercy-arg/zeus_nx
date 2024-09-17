/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeusServer.util.Enums;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Frame;
/*     */ import java.awt.Image;
/*     */ import java.awt.TrayIcon;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.awt.event.WindowFocusListener;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPopupMenu;
/*     */ import javax.swing.event.PopupMenuEvent;
/*     */ import javax.swing.event.PopupMenuListener;
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
/*     */ public class JTrayIcon
/*     */   extends TrayIcon
/*     */ {
/*     */   private JPopupMenu menu;
/*     */   private Enums.Platform currentPlatform;
/*  37 */   private static JDialog dialog = new JDialog((Frame)null); static {
/*  38 */     dialog.setUndecorated(true);
/*  39 */     dialog.setAlwaysOnTop(true);
/*     */   }
/*  41 */   private static PopupMenuListener popupListener = new PopupMenuListener()
/*     */     {
/*     */       public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
/*     */ 
/*     */ 
/*     */       
/*     */       public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
/*  48 */         JTrayIcon.dialog.setVisible(false);
/*     */       }
/*     */ 
/*     */       
/*     */       public void popupMenuCanceled(PopupMenuEvent e) {
/*  53 */         JTrayIcon.dialog.setVisible(false);
/*     */       }
/*     */     };
/*     */   
/*     */   public JTrayIcon(Enums.Platform platform, Image image, String tooltip) {
/*  58 */     super(image, tooltip);
/*  59 */     this.currentPlatform = platform;
/*  60 */     addMouseListener(new MouseAdapter()
/*     */         {
/*     */           public void mousePressed(MouseEvent e) {
/*  63 */             JTrayIcon.this.showJPopupMenu(e);
/*     */           }
/*     */ 
/*     */           
/*     */           public void mouseReleased(MouseEvent e) {
/*  68 */             JTrayIcon.this.showJPopupMenu(e);
/*     */           }
/*     */         });
/*     */     
/*  72 */     if (dialog != null) {
/*  73 */       dialog.addWindowFocusListener(new WindowFocusListener()
/*     */           {
/*     */             public void windowGainedFocus(WindowEvent e) {}
/*     */ 
/*     */ 
/*     */ 
/*     */             
/*     */             public void windowLostFocus(WindowEvent e) {
/*  81 */               JTrayIcon.dialog.setVisible(false);
/*     */             }
/*     */           });
/*     */     }
/*     */   }
/*     */   
/*     */   protected void showJPopupMenu(MouseEvent e) {
/*  88 */     if (e.isPopupTrigger() && this.menu != null) {
/*  89 */       Dimension size = this.menu.getPreferredSize();
/*  90 */       showJPopupMenu(e.getX() - 50, e.getY() - size.height);
/*     */     } 
/*     */   }
/*     */   
/*     */   protected void showJPopupMenu(int x, int y) {
/*  95 */     dialog.setLocation(x, y);
/*  96 */     if (this.currentPlatform != null && this.currentPlatform == Enums.Platform.WINDOWS) {
/*  97 */       MonitorZeus.getInstance().enableDisableStartStopMenus();
/*     */     }
/*  99 */     dialog.setVisible(true);
/* 100 */     this.menu.show(dialog.getContentPane(), 0, 0);
/*     */     
/* 102 */     dialog.toFront();
/*     */   }
/*     */   
/*     */   public JPopupMenu getJPopupMenu() {
/* 106 */     return this.menu;
/*     */   }
/*     */   
/*     */   public void setJPopupMenu(JPopupMenu menu) {
/* 110 */     if (this.menu != null) {
/* 111 */       this.menu.removePopupMenuListener(popupListener);
/*     */     }
/* 113 */     this.menu = menu;
/* 114 */     menu.addPopupMenuListener(popupListener);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\JTrayIcon.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */