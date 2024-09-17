/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import java.beans.PropertyChangeEvent;
/*     */ import java.beans.PropertyChangeListener;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JLabel;
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
/*     */ public class WidgetFactory
/*     */ {
/*     */   class ToggledImage
/*     */   {
/*     */     private ImageIcon onII;
/*     */     private ImageIcon offII;
/*     */     private String onToolTip;
/*     */     private String offToolTip;
/*     */     private JLabel label;
/*     */     private boolean status;
/*     */     
/*     */     public ToggledImage(String on, String off, String onToolTip, String offToolTip) {
/*  33 */       this.onToolTip = onToolTip;
/*  34 */       this.offToolTip = offToolTip;
/*  35 */       this.onII = new ImageIcon(getClass().getClassLoader().getResource(on));
/*  36 */       this.offII = new ImageIcon(getClass().getClassLoader().getResource(off));
/*     */     }
/*     */     
/*     */     public JLabel getToggleImage(boolean initialStatus, boolean caption, String captionMsg) {
/*  40 */       if (initialStatus) {
/*  41 */         this.label = new JLabel(this.onII);
/*  42 */         if (caption) {
/*  43 */           if (captionMsg.length() > 6) {
/*  44 */             captionMsg = captionMsg.substring(captionMsg.length() - 6);
/*     */           }
/*  46 */           this.label.setText(captionMsg);
/*  47 */           this.label.setHorizontalTextPosition(0);
/*  48 */           this.label.setVerticalTextPosition(3);
/*     */         } 
/*  50 */         this.label.setToolTipText(this.onToolTip);
/*     */       } else {
/*  52 */         this.label = new JLabel(this.offII);
/*  53 */         this.label.setToolTipText(this.offToolTip);
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  60 */       this.label.addPropertyChangeListener(new PropertyChangeListener()
/*     */           {
/*     */             public void propertyChange(PropertyChangeEvent evt) {
/*  63 */               if ("state".equals(evt.getPropertyName())) {
/*  64 */                 WidgetFactory.ToggledImage.this.setStatus(((Boolean)evt.getNewValue()).booleanValue());
/*  65 */                 if (((Boolean)evt.getNewValue()).booleanValue()) {
/*  66 */                   WidgetFactory.ToggledImage.this.label.setIcon(WidgetFactory.ToggledImage.this.onII);
/*  67 */                   WidgetFactory.ToggledImage.this.label.setToolTipText(WidgetFactory.ToggledImage.this.onToolTip);
/*     */                 } else {
/*  69 */                   WidgetFactory.ToggledImage.this.label.setIcon(WidgetFactory.ToggledImage.this.offII);
/*  70 */                   WidgetFactory.ToggledImage.this.label.setToolTipText(WidgetFactory.ToggledImage.this.offToolTip);
/*     */                 } 
/*     */               } 
/*     */             }
/*     */           });
/*  75 */       return this.label;
/*     */     }
/*     */     
/*     */     public JLabel getCurrentLabel() {
/*  79 */       return this.label;
/*     */     }
/*     */     
/*     */     public boolean isStatus() {
/*  83 */       return this.status;
/*     */     }
/*     */     
/*     */     public void setStatus(boolean status) {
/*  87 */       this.status = status;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   class LabeledImage
/*     */   {
/*  97 */     private JLabel label = new JLabel();
/*     */     private int signalLevel;
/*     */     
/*     */     public void setImage(ImageIcon image, String tooltip) {
/* 101 */       this.label.setIcon(image);
/* 102 */       this.label.setToolTipText(tooltip);
/*     */     }
/*     */     
/*     */     public JLabel getLabeledImage() {
/* 106 */       return this.label;
/*     */     }
/*     */     
/*     */     public int getSignalLevel() {
/* 110 */       return this.signalLevel;
/*     */     }
/*     */     
/*     */     public void setSignalLevel(int signalLevel) {
/* 114 */       this.signalLevel = signalLevel;
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\WidgetFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */