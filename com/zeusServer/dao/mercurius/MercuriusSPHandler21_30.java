/*     */ package com.zeusServer.dao.mercurius;
/*     */ 
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import java.io.IOException;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class MercuriusSPHandler21_30
/*     */ {
/*     */   public static List<SP_024DataHolder> executeSP_024(int idModule, Connection conn) throws IOException, SQLException {
/*  32 */     CallableStatement cst = null;
/*  33 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  36 */       List<SP_024DataHolder> data = new ArrayList<>();
/*     */       
/*  38 */       cst = conn.prepareCall("call SP_024(?)");
/*  39 */       cst.setInt(1, idModule);
/*  40 */       rs = cst.executeQuery();
/*  41 */       while (rs.next()) {
/*  42 */         SP_024DataHolder sp24DH = new SP_024DataHolder();
/*  43 */         sp24DH.setId_Command(rs.getInt("ID_COMMAND"));
/*  44 */         sp24DH.setCommand_Type(rs.getInt("COMMAND_TYPE"));
/*  45 */         sp24DH.setCommand_Data(rs.getBinaryStream("COMMAND_DATA"));
/*  46 */         sp24DH.setExec_Retries(rs.getShort("EXEC_RETRIES"));
/*  47 */         sp24DH.setCommandFileData(rs.getBytes("COMMAND_FILE_DATA"));
/*  48 */         data.add(sp24DH);
/*     */       } 
/*  50 */       rs.close();
/*  51 */       cst.close();
/*     */       
/*  53 */       return data;
/*     */     }
/*  55 */     catch (IOException|SQLException e) {
/*  56 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  57 */         Logger.getLogger(MercuriusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  59 */       throw e;
/*     */     } finally {
/*  61 */       if (cst != null) {
/*  62 */         cst.close();
/*     */       }
/*  64 */       if (rs != null) {
/*  65 */         rs.close();
/*     */       }
/*  67 */       if (conn != null) {
/*  68 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_025(int id_Command, short num_Exec_Retries, Connection conn) throws SQLException {
/*  75 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  78 */       cst = conn.prepareCall("call SP_025(?,?)");
/*  79 */       cst.setInt(1, id_Command);
/*  80 */       cst.setShort(2, num_Exec_Retries);
/*  81 */       cst.execute();
/*  82 */       cst.close();
/*     */     }
/*  84 */     catch (SQLException e) {
/*  85 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  86 */         Logger.getLogger(MercuriusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  88 */       throw e;
/*     */     } finally {
/*  90 */       if (cst != null) {
/*  91 */         cst.close();
/*     */       }
/*  93 */       if (conn != null) {
/*  94 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_026(int id_Command, short num_Exec_Retries, Connection conn) throws SQLException {
/* 101 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 104 */       cst = conn.prepareCall("call SP_026(?,?)");
/* 105 */       cst.setInt(1, id_Command);
/* 106 */       cst.setShort(2, num_Exec_Retries);
/* 107 */       cst.execute();
/* 108 */       cst.close();
/*     */     }
/* 110 */     catch (SQLException e) {
/* 111 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 112 */         Logger.getLogger(MercuriusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 114 */       throw e;
/*     */     } finally {
/* 116 */       if (cst != null) {
/* 117 */         cst.close();
/*     */       }
/* 119 */       if (conn != null) {
/* 120 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_027(int id_Command, short num_Exec_Retries, Connection conn) throws SQLException {
/* 127 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 130 */       cst = conn.prepareCall("call SP_027(?,?)");
/* 131 */       cst.setInt(1, id_Command);
/* 132 */       cst.setShort(2, num_Exec_Retries);
/* 133 */       cst.execute();
/* 134 */       cst.close();
/*     */     }
/* 136 */     catch (SQLException e) {
/* 137 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 138 */         Logger.getLogger(MercuriusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 140 */       throw e;
/*     */     } finally {
/* 142 */       if (cst != null) {
/* 143 */         cst.close();
/*     */       }
/* 145 */       if (conn != null) {
/* 146 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_028(int idModule, int lastCommInterface, int currentSIM, Connection conn) throws SQLException {
/* 153 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 156 */       cst = conn.prepareCall("call SP_028(?,?,?)");
/* 157 */       cst.setInt(1, idModule);
/* 158 */       cst.setInt(2, lastCommInterface);
/* 159 */       cst.setInt(3, currentSIM);
/* 160 */       cst.execute();
/* 161 */       cst.close();
/*     */     }
/* 163 */     catch (SQLException e) {
/* 164 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 165 */         Logger.getLogger(MercuriusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 167 */       throw e;
/*     */     } finally {
/* 169 */       if (cst != null) {
/* 170 */         cst.close();
/*     */       }
/* 172 */       if (conn != null)
/* 173 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\mercurius\MercuriusSPHandler21_30.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */