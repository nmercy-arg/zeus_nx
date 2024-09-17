/*    */ package com.zeusServer.dao.griffon;
/*    */ 
/*    */ import com.zeuscc.griffon.derby.beans.GriffonModule;
/*    */ import java.sql.CallableStatement;
/*    */ import java.sql.Connection;
/*    */ import java.sql.SQLException;
/*    */ import java.util.List;
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
/*    */ public class GriffonSPGHandler01_10
/*    */ {
/*    */   public static GriffonModule executeSPG_001(GriffonModule gModule, Connection conn) throws SQLException {
/* 25 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 28 */       cst = conn.prepareCall("call SP_G001(?)");
/* 29 */       cst.registerOutParameter(1, 2000);
/* 30 */       cst.setObject(1, gModule);
/* 31 */       cst.execute();
/* 32 */       return (GriffonModule)cst.getObject(1);
/*    */     } finally {
/*    */       
/* 35 */       if (cst != null) {
/* 36 */         cst.close();
/*    */       }
/* 38 */       if (conn != null) {
/* 39 */         conn.close();
/*    */       }
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public static void executeSPG_003(List<GriffonModule> gModuleList, Connection conn) throws SQLException {
/* 46 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 49 */       cst = conn.prepareCall("call SP_G003(?)");
/* 50 */       for (GriffonModule gModule : gModuleList) {
/* 51 */         cst.setObject(1, gModule);
/* 52 */         cst.addBatch();
/*    */       } 
/* 54 */       cst.executeBatch();
/* 55 */       cst.close();
/*    */     } finally {
/*    */       
/* 58 */       if (cst != null) {
/* 59 */         cst.close();
/*    */       }
/* 61 */       if (conn != null) {
/* 62 */         conn.close();
/*    */       }
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public static void executeSPG_005(GriffonModule gModule, Connection conn) throws SQLException {
/* 69 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 72 */       cst = conn.prepareCall("call SP_G005(?)");
/* 73 */       cst.registerOutParameter(1, 2000);
/* 74 */       cst.setObject(1, gModule);
/* 75 */       cst.execute();
/* 76 */       cst.close();
/*    */     } finally {
/*    */       
/* 79 */       if (cst != null) {
/* 80 */         cst.close();
/*    */       }
/* 82 */       if (conn != null)
/* 83 */         conn.close(); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\griffon\GriffonSPGHandler01_10.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */