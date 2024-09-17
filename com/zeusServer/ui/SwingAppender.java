/*    */ package com.zeusServer.ui;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import javax.swing.JTextPane;
/*    */ import javax.swing.SwingUtilities;
/*    */ import javax.swing.text.BadLocationException;
/*    */ import javax.swing.text.Style;
/*    */ import javax.swing.text.StyleConstants;
/*    */ import javax.swing.text.StyledDocument;
/*    */ import org.apache.log4j.WriterAppender;
/*    */ import org.apache.log4j.spi.LoggingEvent;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SwingAppender
/*    */   extends WriterAppender
/*    */ {
/* 29 */   private Color[] colors = new Color[] { Color.RED, Color.YELLOW, Color.GREEN };
/* 30 */   private static JTextPane logTextPane = null;
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void setTextArea(JTextPane logTextPane) {
/* 36 */     SwingAppender.logTextPane = logTextPane;
/* 37 */     logTextPane.getDocument().addDocumentListener(new LimitLinesDocumentListener(2000, true));
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void append(LoggingEvent loggingEvent) {
/* 43 */     final String message = this.layout.format(loggingEvent);
/* 44 */     final LoggingEvent currentEvent = loggingEvent;
/*    */     
/* 46 */     SwingUtilities.invokeLater(new Runnable()
/*    */         {
/*    */           public void run()
/*    */           {
/* 50 */             StyledDocument styledDocMainLowerText = (StyledDocument)SwingAppender.logTextPane.getDocument();
/* 51 */             Style style = styledDocMainLowerText.addStyle("StyledDocument", (Style)null);
/*    */ 
/*    */             
/* 54 */             StyleConstants.setFontFamily(style, "Cursive");
/*    */             
/* 56 */             StyleConstants.setFontSize(style, 12);
/*    */             
/* 58 */             if (currentEvent.getLevel().toString().equals("DEBUG")) {
/* 59 */               StyleConstants.setForeground(style, SwingAppender.this.colors[0]);
/* 60 */             } else if (currentEvent.getLevel().toString().equals("FATAL")) {
/* 61 */               StyleConstants.setForeground(style, SwingAppender.this.colors[1]);
/* 62 */             } else if (currentEvent.getLevel().toString().equals("INFO")) {
/* 63 */               StyleConstants.setForeground(style, SwingAppender.this.colors[2]);
/*    */             } 
/*    */             
/*    */             try {
/* 67 */               styledDocMainLowerText.insertString(styledDocMainLowerText.getLength(), message, style);
/* 68 */             } catch (BadLocationException e) {
/* 69 */               e.printStackTrace();
/*    */             } 
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\SwingAppender.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */