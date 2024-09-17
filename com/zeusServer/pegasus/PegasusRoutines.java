/*     */ package com.zeusServer.pegasus;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.dto.SP_022DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_003DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_003_VO;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_015DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_029DataHolder;
/*     */ import com.zeuscc.pegasus.derby.beans.SP_030DataHolder;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
/*     */ import java.util.List;
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
/*     */ public class PegasusRoutines
/*     */ {
/*     */   protected long idleTimeout;
/*     */   protected short lastCommIface;
/*     */   protected SP_003DataHolder sp03DH;
/*     */   protected SP_015DataHolder sp15DH;
/*     */   protected SP_029DataHolder sp29DH;
/*     */   protected SP_030DataHolder sp30DH;
/*     */   
/*     */   protected enum EnumTipoPacote
/*     */   {
/*  46 */     IDENTIFICATION_PACKET(1),
/*  47 */     EVENT_PACKET(2),
/*  48 */     ALIVE_PACKET(3),
/*  49 */     COMMAND_PACKET(4),
/*  50 */     EXTENDED_ALIVE_PACKET(5),
/*  51 */     ACCESS_PACKET(6);
/*     */     private int packet;
/*     */     
/*     */     EnumTipoPacote(int packet) {
/*  55 */       this.packet = packet;
/*     */     }
/*     */     
/*     */     public int getPacket() {
/*  59 */       return this.packet;
/*     */     }
/*     */   }
/*     */   
/*     */   protected String getICCID(byte[] data, int offset) {
/*  64 */     StringBuilder sb = new StringBuilder();
/*  65 */     for (int i = offset; i < offset + 10; i++) {
/*  66 */       sb.append(String.format("%02d", new Object[] { Byte.valueOf(data[i]) }));
/*     */     } 
/*  68 */     return sb.toString();
/*     */   }
/*     */   
/*     */   protected void executeStoredProcedureHandlingIdentificationPacket(byte[] buffer, String iccid, String moduleIPAddress, String phonePegasus, String lastNWProtocol) throws SQLException, InterruptedException {
/*  72 */     this.sp15DH = new SP_015DataHolder();
/*  73 */     this.sp15DH.setSn(iccid);
/*  74 */     this.sp15DH.setAuto_Registration_Enabled((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/*  75 */     this.sp15DH.setDeleteOccAfterFinalization((short)(ZeusServerCfg.getInstance().getDeleteOccAfterFinalization() ? 1 : 0));
/*  76 */     this.sp15DH.setLast_Comm_Interface((short)buffer[12]);
/*  77 */     this.sp15DH.setModule_Ip_Addr(moduleIPAddress);
/*  78 */     this.sp15DH.setPhone_Pegasus(phonePegasus);
/*  79 */     this.sp15DH.setOperation_Mode((short)buffer[14]);
/*  80 */     this.sp15DH.setPegasus_Firmware_Version((short)(buffer[11] & 0xFF));
/*  81 */     this.sp15DH.setLine_Simulator_Firmware_Version((short)buffer[13]);
/*  82 */     this.sp15DH.setLastNWProtocol(lastNWProtocol);
/*  83 */     this.sp15DH = PegasusDBManager.executeSP_015(this.sp15DH);
/*     */   }
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
/*     */   protected void executeStoredProcedureHandlingAlivePacket(byte[] buffer, int idModule, String nwProtocol) throws SQLException, InterruptedException {
/*  96 */     List<SP_003_VO> sp03VOList = new ArrayList<>(1);
/*  97 */     SP_003_VO sp03VO = new SP_003_VO();
/*  98 */     sp03VO.setId_module(idModule);
/*  99 */     sp03VO.setData(buffer);
/* 100 */     sp03VO.setNwProtocol(nwProtocol);
/* 101 */     sp03VOList.add(sp03VO);
/* 102 */     this.sp03DH = PegasusDBManager.executeSP_003(sp03VOList);
/*     */   }
/*     */   
/*     */   protected void generateEventReceptionAlivePacket(int idClient, int idModule, int idGroup, String spName, String nwProtocol) throws SQLException, InterruptedException, Exception {
/* 106 */     if (spName.equals("SP_003")) {
/* 107 */       if (this.sp03DH != null && this.sp03DH.getClient_Code() != null && this.sp03DH.getClient_Code().length() == 4 && 
/* 108 */         this.sp03DH.getE_Alive_Received() != null && this.sp03DH.getE_Alive_Received().length() == 8) {
/*     */         boolean generateEvent;
/* 110 */         if (this.sp03DH.getLast_Alive_Packet_Event() != null && this.sp03DH.getF_Alive_Received() >= 0) {
/* 111 */           Calendar cal = this.sp03DH.getLast_Alive_Packet_Event();
/* 112 */           cal = Functions.addTime2Calendar(cal, 12, this.sp03DH.getF_Alive_Received());
/* 113 */           generateEvent = (cal.getTimeInMillis() < System.currentTimeMillis());
/*     */         } else {
/* 115 */           generateEvent = true;
/*     */         } 
/* 117 */         if (generateEvent)
/*     */         {
/* 119 */           Functions.saveEvent(Util.EnumProductIDs.PEGASUS.getProductId(), idModule, idGroup, idClient, null, this.sp03DH.getClient_Code(), this.sp03DH.getE_Alive_Received(), Enums.EnumEventQualifier.NEW_EVENT, 1, nwProtocol, this.lastCommIface, -1, -1);
/* 120 */           GenericDBManager.executeSP_019(idClient, "PEGASUS");
/*     */         }
/*     */       
/*     */       }
/*     */     
/* 125 */     } else if (this.sp30DH != null && this.sp30DH.getClient_Code() != null && this.sp30DH.getClient_Code().length() == 4 && 
/* 126 */       this.sp30DH.getE_Alive_Received() != null && this.sp30DH.getE_Alive_Received().length() == 8) {
/*     */       boolean generateEvent;
/* 128 */       if (this.sp30DH.getLast_Alive_Packet_Event() != null && this.sp30DH.getF_Alive_Received() >= 0) {
/* 129 */         Calendar cal = this.sp30DH.getLast_Alive_Packet_Event();
/* 130 */         cal = Functions.addTime2Calendar(cal, 12, this.sp30DH.getF_Alive_Received());
/* 131 */         generateEvent = (cal.getTimeInMillis() < System.currentTimeMillis());
/*     */       } else {
/* 133 */         generateEvent = true;
/*     */       } 
/*     */       
/* 136 */       if (generateEvent) {
/*     */         
/* 138 */         Functions.saveEvent(Util.EnumProductIDs.PEGASUS.getProductId(), idModule, idGroup, idClient, null, this.sp30DH.getClient_Code(), this.sp30DH.getE_Alive_Received(), Enums.EnumEventQualifier.NEW_EVENT, 1, nwProtocol, this.lastCommIface, -1, -1);
/* 139 */         GenericDBManager.executeSP_019(idClient, "PEGASUS");
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected void generateAVLEventReceptionAlivePacket(int idClient, int idModule, int idGroup, String spName, String nwProtocol) throws SQLException, InterruptedException, Exception {
/* 147 */     if (this.sp30DH != null && this.sp30DH.getClient_Code() != null && this.sp30DH.getClient_Code().length() == 4)
/*     */     {
/* 149 */       if (this.sp30DH.getE_Alive_Received() != null && this.sp30DH.getE_Alive_Received().length() > 8) {
/*     */         boolean generateEvent;
/* 151 */         String[] dd = this.sp30DH.getE_Alive_Received().split(";");
/* 152 */         String evnt = dd[0];
/* 153 */         if (this.sp30DH.getLast_Alive_Packet_Event() != null && Integer.parseInt(dd[1]) >= 0) {
/* 154 */           Calendar cal = this.sp30DH.getLast_Alive_Packet_Event();
/* 155 */           cal = Functions.addTime2Calendar(cal, 12, this.sp30DH.getF_Alive_Received());
/* 156 */           generateEvent = (cal.getTimeInMillis() < System.currentTimeMillis());
/*     */         } else {
/* 158 */           generateEvent = true;
/*     */         } 
/* 160 */         if (generateEvent) {
/* 161 */           Functions.saveEvent(Util.EnumProductIDs.MERCURIUS.getProductId(), idModule, idGroup, idClient, null, this.sp30DH.getClient_Code(), evnt, Enums.EnumEventQualifier.NEW_EVENT, 1, nwProtocol, this.lastCommIface, -1, 1);
/* 162 */           GenericDBManager.executeSP_019(idClient, "MERCURIUS");
/*     */         } 
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   protected void updateClientCode(byte[] eventBuffer, int idClient) throws SQLException, InterruptedException {
/* 169 */     if (ZeusServerCfg.getInstance().getAutoRegistration()) {
/* 170 */       StringBuilder newClientCode = new StringBuilder();
/* 171 */       for (int i = 0; i <= 1; i++) {
/* 172 */         newClientCode.append(Functions.convertContactIdDigitToHex((eventBuffer[i] & 0xF0) / 16)).append(Functions.convertContactIdDigitToHex(eventBuffer[i] & 0xF));
/*     */       }
/* 174 */       PegasusDBManager.executeSP_004(idClient, newClientCode.toString());
/*     */     } 
/*     */   }
/*     */   
/*     */   protected void generateEventFailureTransmissionTestTelephoneLine(byte[] buffer, int idClient, int idModule, int idGroup, int tstPacket, String nwProtocol) throws SQLException, InterruptedException, Exception {
/* 179 */     if ((buffer[2] & 0x1) == 1) {
/* 180 */       SP_022DataHolder sp22DH = PegasusDBManager.executeSP_022(idClient, tstPacket);
/* 181 */       if (sp22DH != null && sp22DH.getClient_Code() != null && sp22DH.getClient_Code().length() == 4 && 
/* 182 */         sp22DH.getEventCode() != null && sp22DH.getEventCode().length() == 8) {
/* 183 */         Functions.saveEvent(Util.EnumProductIDs.PEGASUS.getProductId(), idModule, idGroup, idClient, null, sp22DH.getClient_Code(), sp22DH.getEventCode(), Enums.EnumEventQualifier.NEW_EVENT, 1, nwProtocol, this.lastCommIface, -1, 1);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected void generateEventFailureTransmissionTestTelephoneLine(int idClient, int idModule, int idGroup, int tstPacket) throws SQLException, InterruptedException, Exception {
/* 190 */     SP_022DataHolder sp22DH = PegasusDBManager.executeSP_022(idClient, tstPacket);
/* 191 */     if (sp22DH != null && sp22DH.getClient_Code() != null && sp22DH.getClient_Code().length() == 4 && 
/* 192 */       sp22DH.getEventCode() != null && sp22DH.getEventCode().length() == 8)
/* 193 */       Functions.saveEvent(Util.EnumProductIDs.PEGASUS.getProductId(), idModule, idGroup, idClient, null, sp22DH.getClient_Code(), sp22DH.getEventCode(), Enums.EnumEventQualifier.NEW_EVENT, 1, Enums.EnumNWProtocol.CSD.name(), this.lastCommIface, -1, 1); 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\PegasusRoutines.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */