/*     */ package com.zeusServer.dao.pegasus.v1;
/*     */ 
/*     */ import com.zeusServer.dto.SP_022DataHolder;
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_030DataHolder;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PegasusSPHandler21_30
/*     */ {
/*     */   public static SP_022DataHolder executeSP_022(int idClient, int tstPacket, Connection conn) throws SQLException {
/*  37 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  40 */       SP_022DataHolder sp22DH = new SP_022DataHolder();
/*     */       
/*  42 */       cst = conn.prepareCall("call SP_022(?,?,?,?)");
/*  43 */       cst.setInt(1, idClient);
/*  44 */       cst.setInt(2, tstPacket);
/*  45 */       cst.registerOutParameter(3, 12);
/*  46 */       cst.registerOutParameter(4, 12);
/*  47 */       cst.execute();
/*  48 */       sp22DH.setClient_Code(cst.getString(3));
/*  49 */       sp22DH.setEventCode(cst.getString(4));
/*  50 */       cst.close();
/*     */       
/*  52 */       return sp22DH;
/*     */     }
/*  54 */     catch (SQLException e) {
/*  55 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  56 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  58 */       throw e;
/*     */     } finally {
/*  60 */       if (cst != null) {
/*  61 */         cst.close();
/*     */       }
/*  63 */       if (conn != null) {
/*  64 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_024DataHolder> executeSP_024(int idModule, Connection conn) throws IOException, SQLException {
/*  71 */     CallableStatement cst = null;
/*  72 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  75 */       List<SP_024DataHolder> data = new ArrayList<>();
/*     */       
/*  77 */       cst = conn.prepareCall("call SP_024(?)");
/*  78 */       cst.setInt(1, idModule);
/*  79 */       rs = cst.executeQuery();
/*  80 */       while (rs.next()) {
/*  81 */         SP_024DataHolder sp24DH = new SP_024DataHolder();
/*  82 */         sp24DH.setId_Command(rs.getInt("ID_COMMAND"));
/*  83 */         sp24DH.setCommand_Type(rs.getInt("COMMAND_TYPE"));
/*  84 */         sp24DH.setCommand_Data(rs.getBinaryStream("COMMAND_DATA"));
/*  85 */         sp24DH.setExec_Retries(rs.getShort("EXEC_RETRIES"));
/*  86 */         sp24DH.setCommandFileData(rs.getBytes("COMMAND_FILE_DATA"));
/*  87 */         data.add(sp24DH);
/*     */       } 
/*  89 */       rs.close();
/*  90 */       cst.close();
/*     */       
/*  92 */       return data;
/*     */     }
/*  94 */     catch (IOException|SQLException e) {
/*  95 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  96 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  98 */       throw e;
/*     */     } finally {
/* 100 */       if (rs != null) {
/* 101 */         rs.close();
/*     */       }
/* 103 */       if (cst != null) {
/* 104 */         cst.close();
/*     */       }
/* 106 */       if (conn != null) {
/* 107 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_025(int id_Command, short num_Exec_Retries, Connection conn) throws SQLException {
/* 114 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 117 */       cst = conn.prepareCall("call SP_025(?,?)");
/* 118 */       cst.setInt(1, id_Command);
/* 119 */       cst.setShort(2, num_Exec_Retries);
/* 120 */       cst.execute();
/* 121 */       cst.close();
/*     */     }
/* 123 */     catch (SQLException e) {
/* 124 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 125 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 127 */       throw e;
/*     */     } finally {
/* 129 */       if (cst != null) {
/* 130 */         cst.close();
/*     */       }
/* 132 */       if (conn != null) {
/* 133 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_026(int id_Command, short num_Exec_Retries, Connection conn) throws SQLException {
/* 140 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 143 */       cst = conn.prepareCall("call SP_026(?,?)");
/* 144 */       cst.setInt(1, id_Command);
/* 145 */       cst.setShort(2, num_Exec_Retries);
/* 146 */       cst.execute();
/* 147 */       cst.close();
/*     */     }
/* 149 */     catch (SQLException e) {
/* 150 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 151 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 153 */       throw e;
/*     */     } finally {
/* 155 */       if (cst != null) {
/* 156 */         cst.close();
/*     */       }
/* 158 */       if (conn != null) {
/* 159 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_027(int id_Command, short num_Exec_Retries, Connection conn) throws SQLException {
/* 166 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 169 */       cst = conn.prepareCall("call SP_027(?,?)");
/* 170 */       cst.setInt(1, id_Command);
/* 171 */       cst.setShort(2, num_Exec_Retries);
/* 172 */       cst.execute();
/* 173 */       cst.close();
/*     */     }
/* 175 */     catch (SQLException e) {
/* 176 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 177 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 179 */       throw e;
/*     */     } finally {
/* 181 */       if (cst != null) {
/* 182 */         cst.close();
/*     */       }
/* 184 */       if (conn != null) {
/* 185 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_028(int idModule, int lastCommInterface, int currentSIM, Connection conn) throws SQLException {
/* 192 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 195 */       cst = conn.prepareCall("call SP_028(?,?,?)");
/* 196 */       cst.setInt(1, idModule);
/* 197 */       cst.setInt(2, lastCommInterface);
/* 198 */       cst.setInt(3, currentSIM);
/* 199 */       cst.execute();
/* 200 */       cst.close();
/*     */     }
/* 202 */     catch (SQLException e) {
/* 203 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 204 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 206 */       throw e;
/*     */     } finally {
/* 208 */       if (cst != null) {
/* 209 */         cst.close();
/*     */       }
/* 211 */       if (conn != null) {
/* 212 */         conn.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_030DataHolder executeSP_030(int idModule, Connection conn) throws SQLException {
/* 219 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 222 */       cst = conn.prepareCall("call SP_030(?,?)");
/* 223 */       cst.setInt(1, idModule);
/* 224 */       cst.registerOutParameter(2, 2000);
/* 225 */       cst.execute();
/* 226 */       return (SP_030DataHolder)cst.getObject(2);
/*     */     }
/* 228 */     catch (SQLException e) {
/* 229 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 230 */         Logger.getLogger(PegasusSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 232 */       throw e;
/*     */     } finally {
/* 234 */       if (cst != null) {
/* 235 */         cst.close();
/*     */       }
/* 237 */       if (conn != null)
/* 238 */         conn.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\pegasus\v1\PegasusSPHandler21_30.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */