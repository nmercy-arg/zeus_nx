/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ import javax.sound.sampled.AudioInputStream;
/*    */ import javax.sound.sampled.AudioSystem;
/*    */ import javax.sound.sampled.Clip;
/*    */ import javax.sound.sampled.DataLine;
/*    */ import javax.sound.sampled.LineEvent;
/*    */ import javax.sound.sampled.LineListener;
/*    */ import javax.sound.sampled.LineUnavailableException;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SoundPlayer
/*    */   implements LineListener
/*    */ {
/*    */   Clip clip;
/*    */   
/*    */   public void playSound(String fileName) {
/* 28 */     AudioInputStream aStream = null;
/*    */     try {
/* 30 */       aStream = AudioSystem.getAudioInputStream(SoundPlayer.class.getClassLoader().getResource(fileName));
/* 31 */       DataLine.Info info = new DataLine.Info(Clip.class, aStream.getFormat());
/* 32 */       this.clip = (Clip)AudioSystem.getLine(info);
/* 33 */       this.clip.addLineListener(this);
/* 34 */       this.clip.open(aStream);
/* 35 */       this.clip.start();
/*    */     }
/* 37 */     catch (LineUnavailableException|javax.sound.sampled.UnsupportedAudioFileException|IOException ex) {
/* 38 */       Logger.getLogger(SoundPlayer.class.getName()).log(Level.SEVERE, (String)null, ex);
/*    */     } finally {
/*    */       try {
/* 41 */         if (aStream != null) {
/* 42 */           aStream.close();
/*    */         }
/* 44 */       } catch (IOException iOException) {}
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void update(LineEvent event) {
/* 52 */     if (event.getType() == LineEvent.Type.STOP)
/* 53 */       this.clip.close(); 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\SoundPlayer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */