/*    */ package com.zeusServer.pegasus;
/*    */ 
/*    */ import com.zeuscc.pegasus.derby.beans.ModuleBean;
/*    */ import com.zeuscc.pegasus.derby.beans.SP_V2_001_VO;
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
/*    */ public class PegasusV2Routines
/*    */ {
/*    */   protected long idleTimeout;
/*    */   protected ModuleBean mBean;
/*    */   protected SP_V2_001_VO spV201VO;
/*    */   protected short lastCommIface;
/*    */   protected short currentSIM;
/*    */   protected int timezone;
/* 28 */   protected DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\PegasusV2Routines.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */