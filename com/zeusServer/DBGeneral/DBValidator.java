/*    */ package com.zeusServer.DBGeneral;
/*    */ 
/*    */ import com.zeusServer.util.ZeusServerCfg;
/*    */ import java.sql.Connection;
/*    */ import java.sql.DatabaseMetaData;
/*    */ import java.sql.DriverManager;
/*    */ import java.sql.ResultSet;
/*    */ import java.sql.SQLException;
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
/*    */ public class DBValidator
/*    */ {
/*    */   public static boolean validateDB(String schemaName, int tECount, int pECount) throws SQLException, ClassNotFoundException {
/* 25 */     Connection conn = null;
/* 26 */     ResultSet rs = null;
/*    */     
/*    */     try {
/* 29 */       int tCount = 0;
/* 30 */       int pCount = 0;
/*    */       
/* 32 */       Class.forName("org.apache.derby.jdbc.ClientDriver");
/* 33 */       conn = DriverManager.getConnection("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/SYS;upgrade=true", "SYS", "SYS");
/* 34 */       DatabaseMetaData dmd = conn.getMetaData();
/* 35 */       rs = dmd.getTables(null, schemaName, "%", null);
/* 36 */       while (rs.next()) {
/* 37 */         tCount++;
/*    */       }
/* 39 */       rs.close();
/*    */       
/* 41 */       rs = dmd.getProcedures(null, schemaName, "%");
/* 42 */       while (rs.next()) {
/* 43 */         pCount++;
/*    */       }
/* 45 */       rs.close();
/*    */       
/* 47 */       return (tCount >= tECount && pCount >= pECount);
/*    */     } finally {
/*    */       
/* 50 */       if (rs != null) {
/* 51 */         rs.close();
/*    */       }
/* 53 */       if (conn != null)
/* 54 */         conn.close(); 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBGeneral\DBValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */