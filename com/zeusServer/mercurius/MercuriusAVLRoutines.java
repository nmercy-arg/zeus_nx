/*    */ package com.zeusServer.mercurius;
/*    */ 
/*    */ import com.zeus.mercuriusAVL.derby.beans.MercuriusAVLModule;
/*    */ import java.text.DateFormat;
/*    */ import java.text.SimpleDateFormat;
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
/*    */ public class MercuriusAVLRoutines
/*    */ {
/*    */   protected MercuriusAVLModule module;
/*    */   protected long idleTimeout;
/*    */   protected short lastCommIface;
/*    */   protected short currentSIM;
/*    */   protected int timezone;
/* 27 */   protected DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\mercurius\MercuriusAVLRoutines.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */