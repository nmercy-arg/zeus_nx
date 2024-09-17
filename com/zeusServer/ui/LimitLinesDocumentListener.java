/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.event.DocumentEvent;
/*     */ import javax.swing.event.DocumentListener;
/*     */ import javax.swing.text.BadLocationException;
/*     */ import javax.swing.text.Document;
/*     */ import javax.swing.text.Element;
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
/*     */ public class LimitLinesDocumentListener
/*     */   implements DocumentListener
/*     */ {
/*     */   private int maximumLines;
/*     */   private boolean isRemoveFromStart;
/*     */   
/*     */   public LimitLinesDocumentListener(int maximumLines) {
/*  33 */     this(maximumLines, true);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public LimitLinesDocumentListener(int maximumLines, boolean isRemoveFromStart) {
/*  42 */     setLimitLines(maximumLines);
/*  43 */     this.isRemoveFromStart = isRemoveFromStart;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getLimitLines() {
/*  50 */     return this.maximumLines;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setLimitLines(int maximumLines) {
/*  57 */     if (maximumLines < 1) {
/*  58 */       String message = "Maximum lines must be greater than 0";
/*  59 */       throw new IllegalArgumentException(message);
/*     */     } 
/*     */     
/*  62 */     this.maximumLines = maximumLines;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void insertUpdate(final DocumentEvent e) {
/*  71 */     SwingUtilities.invokeLater(new Runnable()
/*     */         {
/*     */           public void run() {
/*  74 */             LimitLinesDocumentListener.this.removeLines(e);
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void removeUpdate(DocumentEvent e) {}
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void changedUpdate(DocumentEvent e) {}
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void removeLines(DocumentEvent e) {
/*  94 */     Document document = e.getDocument();
/*  95 */     Element root = document.getDefaultRootElement();
/*     */     
/*  97 */     while (root.getElementCount() > this.maximumLines) {
/*  98 */       if (this.isRemoveFromStart) {
/*  99 */         removeFromStart(document, root); continue;
/*     */       } 
/* 101 */       removeFromEnd(document, root);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void removeFromStart(Document document, Element root) {
/* 110 */     Element line = root.getElement(0);
/* 111 */     int end = line.getEndOffset();
/*     */     
/*     */     try {
/* 114 */       document.remove(0, end);
/* 115 */     } catch (BadLocationException ble) {
/* 116 */       Logger.getLogger(LimitLinesDocumentListener.class.getName()).log(Level.SEVERE, (String)null, ble);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void removeFromEnd(Document document, Element root) {
/* 127 */     Element line = root.getElement(root.getElementCount() - 1);
/* 128 */     int start = line.getStartOffset();
/* 129 */     int end = line.getEndOffset();
/*     */     
/*     */     try {
/* 132 */       document.remove(start - 1, end - start);
/* 133 */     } catch (BadLocationException ble) {
/* 134 */       Logger.getLogger(LimitLinesDocumentListener.class.getName()).log(Level.SEVERE, (String)null, ble);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\LimitLinesDocumentListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */