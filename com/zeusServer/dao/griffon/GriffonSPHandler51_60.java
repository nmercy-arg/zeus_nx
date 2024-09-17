/*    */ package com.zeusServer.dao.griffon;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class GriffonSPHandler51_60
/*    */ {
/*    */   public static LoggedInUser executeSP_055(String userName, String password, Connection conn) throws SQLException {
/* 29 */     CallableStatement cst = null;
/*    */     
/*    */     try {
/* 32 */       LoggedInUser user = null;
/*    */       
/* 34 */       cst = conn.prepareCall("call SP_055(?,?,?,?,?,?,?,?,?,?,?,?,?)");
/* 35 */       cst.setInt(1, 0);
/* 36 */       cst.setString(2, userName);
/* 37 */       cst.setString(3, password);
/* 38 */       cst.registerOutParameter(4, 4);
/* 39 */       cst.registerOutParameter(5, 4);
/* 40 */       cst.registerOutParameter(6, 4);
/* 41 */       cst.registerOutParameter(7, 12);
/* 42 */       cst.registerOutParameter(8, 4);
/* 43 */       cst.registerOutParameter(9, 4);
/* 44 */       cst.registerOutParameter(10, 12);
/* 45 */       cst.registerOutParameter(11, 12);
/* 46 */       cst.registerOutParameter(12, 12);
/* 47 */       cst.registerOutParameter(13, 4);
/* 48 */       cst.execute();
/* 49 */       if (cst.getInt(13) > 0) {
/* 50 */         user = new LoggedInUser();
/* 51 */         user.setIdClient(cst.getInt(4));
/* 52 */         user.setIdGroup(cst.getInt(5));
/* 53 */         user.setClientType(cst.getInt(6));
/* 54 */         user.setPermissions(cst.getString(7));
/* 55 */         user.setEnabled(cst.getInt(8));
/* 56 */         user.setDateFormat(cst.getInt(9));
/* 57 */         user.setTimeZone(cst.getString(10));
/* 58 */         user.setLanguage(cst.getString(11));
/* 59 */         user.setUserName(cst.getString(12));
/* 60 */         user.setIdUser(cst.getInt(13));
/*    */       } 
/* 62 */       cst.close();
/*    */       
/* 64 */       return user;
/*    */     }
/* 66 */     catch (SQLException e) {
/* 67 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 68 */         Logger.getLogger(GriffonSPHandler51_60.class.getName()).log(Level.SEVERE, (String)null, e);
/*    */       }
/* 70 */       throw e;
/*    */     } finally {
/* 72 */       if (cst != null) {
/* 73 */         cst.close();
/*    */       }
/* 75 */       if (conn != null)
/* 76 */         conn.close(); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\griffon\GriffonSPHandler51_60.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */