/*    */ package com.zeusServer.dao.pegasus.v2;
/*    */ 
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import com.zeuscc.pegasus.derby.beans.ModuleBean;
/*    */ import com.zeuscc.pegasus.derby.beans.SP_V2_001_VO;
/*    */ import java.sql.CallableStatement;
/*    */ import java.sql.Connection;
/*    */ import java.sql.SQLException;
/*    */ import java.util.List;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
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
/*    */ 
/*    */ 
/*    */ public class PegasusV2QueryHandler
/*    */ {
/*    */   public static SP_V2_001_VO executeSP_V2_001(SP_V2_001_VO spV2_001_VO, Connection conn) throws SQLException {
/* 32 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 35 */       cst = conn.prepareCall("call SP_V2_001(?)");
/* 36 */       cst.registerOutParameter(1, 2000);
/* 37 */       cst.setObject(1, spV2_001_VO);
/* 38 */       cst.execute();
/* 39 */       return (SP_V2_001_VO)cst.getObject(1);
/*    */     }
/* 41 */     catch (SQLException e) {
/* 42 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 43 */         Logger.getLogger(PegasusV2QueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*    */       }
/* 45 */       throw e;
/*    */     } finally {
/* 47 */       if (cst != null) {
/* 48 */         cst.close();
/*    */       }
/* 50 */       if (conn != null) {
/* 51 */         conn.close();
/*    */       }
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public static void executeSP_V2_002(List<ModuleBean> mdBean, Connection conn) throws SQLException {
/* 58 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 61 */       cst = conn.prepareCall("call SP_V2_002(?)");
/* 62 */       for (ModuleBean mBean : mdBean) {
/* 63 */         cst.setObject(1, mBean);
/* 64 */         cst.addBatch();
/*    */       } 
/* 66 */       cst.executeBatch();
/* 67 */       cst.close();
/*    */     }
/* 69 */     catch (SQLException e) {
/* 70 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 71 */         Logger.getLogger(PegasusV2QueryHandler.class.getName()).log(Level.SEVERE, (String)null, e);
/*    */       }
/* 73 */       throw e;
/*    */     } finally {
/* 75 */       if (cst != null) {
/* 76 */         cst.close();
/*    */       }
/* 78 */       if (conn != null)
/* 79 */         conn.close(); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v2\PegasusV2QueryHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */