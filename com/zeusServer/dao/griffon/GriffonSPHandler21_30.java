/*     */ package com.zeusServer.dao.griffon;
/*     */ 
/*     */ import com.zeusServer.dto.SP_022DataHolder;
/*     */ import com.zeusServer.dto.SP_024DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_029DataHolder;
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
/*     */ public class GriffonSPHandler21_30
/*     */ {
/*     */   public static SP_022DataHolder executeSP_022(int idClient, int tstPacket, Connection con) throws SQLException {
/*  38 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/*  41 */       cst = con.prepareCall("call SP_022(?,?,?,?)");
/*  42 */       cst.setInt(1, idClient);
/*  43 */       cst.setInt(2, tstPacket);
/*  44 */       cst.registerOutParameter(3, 12);
/*  45 */       cst.registerOutParameter(4, 12);
/*  46 */       cst.execute();
/*     */       
/*  48 */       SP_022DataHolder sp22DH = new SP_022DataHolder();
/*  49 */       sp22DH.setClient_Code(cst.getString(3));
/*  50 */       sp22DH.setEventCode(cst.getString(4));
/*  51 */       return sp22DH;
/*     */     }
/*  53 */     catch (SQLException e) {
/*  54 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  55 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  57 */       throw e;
/*     */     } finally {
/*  59 */       if (cst != null) {
/*  60 */         cst.close();
/*     */       }
/*  62 */       if (con != null) {
/*  63 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<SP_024DataHolder> executeSP_024(int idModule, Connection con) throws IOException, SQLException {
/*  70 */     CallableStatement cst = null;
/*  71 */     ResultSet rs = null;
/*     */     
/*     */     try {
/*  74 */       List<SP_024DataHolder> data = new ArrayList<>();
/*     */       
/*  76 */       cst = con.prepareCall("call SP_024(?)");
/*  77 */       cst.setInt(1, idModule);
/*  78 */       rs = cst.executeQuery();
/*  79 */       while (rs.next()) {
/*  80 */         SP_024DataHolder sp24DH = new SP_024DataHolder();
/*  81 */         sp24DH.setId_Command(rs.getInt("ID_COMMAND"));
/*  82 */         sp24DH.setCommand_Type(rs.getInt("COMMAND_TYPE"));
/*  83 */         sp24DH.setCommand_Data(rs.getBinaryStream("COMMAND_DATA"));
/*  84 */         sp24DH.setExec_Retries(rs.getShort("EXEC_RETRIES"));
/*  85 */         sp24DH.setCommandFileData(rs.getBytes("COMMAND_FILE_DATA"));
/*  86 */         data.add(sp24DH);
/*     */       } 
/*  88 */       rs.close();
/*  89 */       cst.close();
/*     */       
/*  91 */       return data;
/*     */     }
/*  93 */     catch (IOException|SQLException e) {
/*  94 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/*  95 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/*  97 */       throw e;
/*     */     } finally {
/*  99 */       if (rs != null) {
/* 100 */         rs.close();
/*     */       }
/* 102 */       if (cst != null) {
/* 103 */         cst.close();
/*     */       }
/* 105 */       if (con != null) {
/* 106 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_025(int id_Command, short num_Exec_Retries, Connection con) throws SQLException {
/* 113 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 116 */       cst = con.prepareCall("call SP_025(?,?)");
/* 117 */       cst.setInt(1, id_Command);
/* 118 */       cst.setShort(2, num_Exec_Retries);
/* 119 */       cst.execute();
/* 120 */       cst.close();
/*     */     }
/* 122 */     catch (SQLException e) {
/* 123 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 124 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 126 */       throw e;
/*     */     } finally {
/* 128 */       if (cst != null) {
/* 129 */         cst.close();
/*     */       }
/* 131 */       if (con != null) {
/* 132 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_026(int id_Command, short num_Exec_Retries, Connection con) throws SQLException {
/* 139 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 142 */       cst = con.prepareCall("call SP_026(?,?)");
/* 143 */       cst.setInt(1, id_Command);
/* 144 */       cst.setShort(2, num_Exec_Retries);
/* 145 */       cst.execute();
/* 146 */       cst.close();
/*     */     }
/* 148 */     catch (SQLException e) {
/* 149 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 150 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 152 */       throw e;
/*     */     } finally {
/* 154 */       if (cst != null) {
/* 155 */         cst.close();
/*     */       }
/* 157 */       if (con != null) {
/* 158 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_027(int id_Command, short num_Exec_Retries, Connection con) throws SQLException {
/* 165 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 168 */       cst = con.prepareCall("call SP_027(?,?)");
/* 169 */       cst.setInt(1, id_Command);
/* 170 */       cst.setShort(2, num_Exec_Retries);
/* 171 */       cst.execute();
/* 172 */       cst.close();
/*     */     }
/* 174 */     catch (SQLException e) {
/* 175 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 176 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 178 */       throw e;
/*     */     } finally {
/* 180 */       if (cst != null) {
/* 181 */         cst.close();
/*     */       }
/* 183 */       if (con != null) {
/* 184 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static void executeSP_028(int idModule, int lastCommInterface, int currentSIM, Connection con) throws SQLException {
/* 191 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 194 */       cst = con.prepareCall("call SP_028(?,?,?)");
/* 195 */       cst.setInt(1, idModule);
/* 196 */       cst.setInt(2, lastCommInterface);
/* 197 */       cst.setInt(3, currentSIM);
/* 198 */       cst.execute();
/* 199 */       cst.close();
/*     */     }
/* 201 */     catch (SQLException e) {
/* 202 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 203 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 205 */       throw e;
/*     */     } finally {
/* 207 */       if (cst != null) {
/* 208 */         cst.close();
/*     */       }
/* 210 */       if (con != null) {
/* 211 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_029DataHolder executeSP_029(String phonePegasus, Connection con) throws SQLException {
/* 218 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 221 */       cst = con.prepareCall("call SP_029(?,?)");
/* 222 */       cst.setString(1, phonePegasus);
/* 223 */       cst.registerOutParameter(2, 2000);
/* 224 */       cst.execute();
/* 225 */       return (SP_029DataHolder)cst.getObject(2);
/*     */     }
/* 227 */     catch (SQLException e) {
/* 228 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 229 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 231 */       throw e;
/*     */     } finally {
/* 233 */       if (cst != null) {
/* 234 */         cst.close();
/*     */       }
/* 236 */       if (con != null) {
/* 237 */         con.close();
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static SP_030DataHolder executeSP_030(int idModule, Connection con) throws SQLException {
/* 244 */     CallableStatement cst = null;
/*     */     
/*     */     try {
/* 247 */       cst = con.prepareCall("call SP_030(?,?)");
/* 248 */       cst.setInt(1, idModule);
/* 249 */       cst.registerOutParameter(2, 2000);
/* 250 */       cst.execute();
/* 251 */       return (SP_030DataHolder)cst.getObject(2);
/*     */     }
/* 253 */     catch (SQLException e) {
/* 254 */       if (GlobalVariables.dbCurrentStatus != Enums.enumDbStatus.SPACE_RECLAIM && GlobalVariables.lastDbSpaceReclaim + 300000L < System.currentTimeMillis()) {
/* 255 */         Logger.getLogger(GriffonSPHandler21_30.class.getName()).log(Level.SEVERE, (String)null, e);
/*     */       }
/* 257 */       throw e;
/*     */     } finally {
/* 259 */       if (cst != null) {
/* 260 */         cst.close();
/*     */       }
/* 262 */       if (con != null)
/* 263 */         con.close(); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dao\griffon\GriffonSPHandler21_30.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */