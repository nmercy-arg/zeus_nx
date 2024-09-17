/*    */ package com.zeusServer.griffon;
/*    */ 
/*    */ import com.zeuscc.griffon.derby.beans.GriffonModule;
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
/*    */ public class GriffonRoutines
/*    */ {
/*    */   protected GriffonModule module;
/*    */   protected long idleTimeout;
/*    */   protected short lastCommIface;
/*    */   protected short currentSIM;
/* 25 */   protected DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\griffon\GriffonRoutines.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */