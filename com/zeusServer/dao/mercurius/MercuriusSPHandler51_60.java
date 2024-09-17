/*    */ package com.zeusServer.dao.mercurius;
/*    */ 
/*    */ import com.zeusServer.util.Enums;
/*    */ import com.zeusServer.util.GlobalVariables;
/*    */ import com.zeusServer.util.LoggedInUser;
/*    */ import java.sql.CallableStatement;
/*    */ import java.sql.Connection;
/*    */ import java.sql.SQLException;
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
/*    */ public class MercuriusSPHandler51_60
/*    */ {
/*    */   public static LoggedInUser executeSP_055(String userName, String password, Connection conn) throws SQLException {
/* 25 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 28 */       LoggedInUser user = null;
/*    */       
/* 30 */       cst = conn.prepareCall("call SP_055(?,?,?,?,?,?,?,?,?,?,?,?)");
/* 31 */       cst.setString(1, userName);
/* 32 */       cst.setString(2, password);
/* 33 */       cst.registerOutParameter(3, 4);
/* 34 */       cst.registerOutParameter(4, 4);
/* 35 */       cst.registerOutParameter(5, 4);
/* 36 */       cst.registerOutParameter(6, 12);
/* 37 */       cst.registerOutParameter(7, 4);
/* 38 */       cst.registerOutParameter(8, 4);
/* 39 */       cst.registerOutParameter(9, 12);
/* 40 */       cst.registerOutParameter(10, 12);
/* 41 */       cst.registerOutParameter(11, 12);
/* 42 */       cst.registerOutParameter(12, 4);
/* 43 */       cst.execute();
/* 44 */       if (cst.getInt(12) > 0) {
/* 45 */         user = new LoggedInUser();
/* 46 */         user.setIdClient(cst.getInt(3));
/* 47 */         user.setIdGroup(cst.getInt(4));
/* 48 */         user.setClientType(cst.getInt(5));
/* 49 */         user.setPermissions(cst.getString(6));
/* 50 */         user.setEnabled(cst.getInt(7));
/* 51 */         user.setDateFormat(cst.getInt(8));
/* 52 */         user.setTimeZone(cst.getString(9));
/* 53 */         user.setLanguage(cst.getString(10));
/* 54 */         user.setUserName(cst.getString(11));
/* 55 */         user.setIdUser(cst.getInt(12));
/*    */       } 
/* 57 */       cst.close();
/*    */       
/* 59 */       return user;
/*    */     }
/* 61 */     catch (SQLException e) {
/* 62 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 63 */         Logger.getLogger(MercuriusSPHandler51_60.class.getName()).log(Level.SEVERE, (String)null, e);
/*    */       }
/* 65 */       throw e;
/*    */     } finally {
/* 67 */       if (cst != null) {
/* 68 */         cst.close();
/*    */       }
/* 70 */       if (conn != null)
/* 71 */         conn.close(); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\mercurius\MercuriusSPHandler51_60.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */