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
/*    */ public class EventAppender
/*    */   extends WriterAppender
/*    */ {
/* 29 */   private Color[] colors = new Color[] { Color.RED, Color.YELLOW, Color.GREEN };
/* 30 */   private static JTextPane logTextPane = null;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void setTextArea(JTextPane logTextPane) {
/* 37 */     EventAppender.logTextPane = logTextPane;
/* 38 */     logTextPane.getDocument().addDocumentListener(new LimitLinesDocumentListener(2000, true));
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void append(LoggingEvent loggingEvent) {
/* 45 */     final String message = this.layout.format(loggingEvent);
/* 46 */     final LoggingEvent currentEvent = loggingEvent;
/*    */     
/* 48 */     SwingUtilities.invokeLater(new Runnable()
/*    */         {
/*    */           public void run()
/*    */           {
/* 52 */             if (EventAppender.logTextPane != null) {
/* 53 */               StyledDocument styledDocMainLowerText = (StyledDocument)EventAppender.logTextPane.getDocument();
/* 54 */               Style style = styledDocMainLowerText.addStyle("StyledDocument", (Style)null);
/*    */ 
/*    */               
/* 57 */               StyleConstants.setFontFamily(style, "Cursive");
/*    */               
/* 59 */               StyleConstants.setFontSize(style, 12);
/*    */               
/* 61 */               if (currentEvent.getLevel().toString().equals("DEBUG")) {
/* 62 */                 StyleConstants.setForeground(style, EventAppender.this.colors[0]);
/* 63 */               } else if (currentEvent.getLevel().toString().equals("FATAL")) {
/* 64 */                 StyleConstants.setForeground(style, EventAppender.this.colors[1]);
/* 65 */               } else if (currentEvent.getLevel().toString().equals("INFO")) {
/* 66 */                 StyleConstants.setForeground(style, EventAppender.this.colors[2]);
/*    */               } 
/*    */               
/*    */               try {
/* 70 */                 styledDocMainLowerText.insertString(styledDocMainLowerText.getLength(), message, style);
/* 71 */               } catch (BadLocationException e) {
/* 72 */                 e.printStackTrace();
/*    */               } 
/*    */             } 
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\EventAppender.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */