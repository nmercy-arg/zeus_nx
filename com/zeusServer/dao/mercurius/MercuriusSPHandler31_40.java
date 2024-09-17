/*    */ package com.zeusServer.dao.mercurius;
/*    */ 
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import java.sql.CallableStatement;
/*    */ import java.sql.Connection;
/*    */ import java.sql.ResultSet;
/*    */ import java.sql.SQLException;
/*    */ import java.util.ArrayList;
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
/*    */ public class MercuriusSPHandler31_40
/*    */ {
/*    */   public static List<String> executeSP_031(int idClient, Connection conn) throws SQLException {
/* 31 */     CallableStatement cst = null;
/* 32 */     ResultSet rs = null;
/*    */     
/*    */     try {
/* 35 */       List<String> data = new ArrayList<>();
/*    */       
/* 37 */       cst = conn.prepareCall("call SP_031(?)");
/* 38 */       cst.setInt(1, idClient);
/* 39 */       cst.execute();
/* 40 */       rs = cst.getResultSet();
/* 41 */       while (rs.next()) {
/* 42 */         data.add(rs.getString("EMAIL"));
/*    */       }
/* 44 */       rs.close();
/* 45 */       cst.close();
/*    */       
/* 47 */       return data;
/*    */     }
/* 49 */     catch (SQLException e) {
/* 50 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 51 */         Logger.getLogger(MercuriusSPHandler31_40.class.getName()).log(Level.SEVERE, (String)null, e);
/*    */       }
/* 53 */       throw e;
/*    */     } finally {
/* 55 */       if (cst != null) {
/* 56 */         cst.close();
/*    */       }
/* 58 */       if (rs != null) {
/* 59 */         rs.close();
/*    */       }
/* 61 */       if (conn != null)
/* 62 */         conn.close(); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\mercurius\MercuriusSPHandler31_40.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */