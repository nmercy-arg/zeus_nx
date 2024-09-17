/*     */ package com.zeusServer.dto;
/*     */ 
/*     */ import java.util.Calendar;
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
/*     */ public class SP_044DataHolder
/*     */ {
/*     */   private int id_Module;
/*     */   private String iccid;
/*     */   private int operation_Mode;
/*     */   private Calendar connection_Timestamp;
/*     */   private short alarm_Panel_Connection_Status;
/*     */   private Calendar last_Communication;
/*     */   private String module_Ip_Addr;
/*     */   private short line_Simulator_Status;
/*     */   private short phone_Line_Status;
/*     */   private short pegasus_Firmware_Version;
/*     */   private short line_Simulator_Firmware_Version;
/*     */   private short dual_Monitoring_Status;
/*     */   private short last_Signal_Level;
/*     */   private short alarm_Panel_Comm_Status;
/*     */   private int gprs_Comm_Timeout;
/*     */   private int csd_Comm_Timeout;
/*     */   private int eth_Comm_Timeout;
/*     */   private short last_Comm_Interface;
/*     */   private float last_Battery_Level;
/*     */   private short ac_Supply_Status;
/*     */   private short gsm_Freq;
/*     */   private short alarm_Panel_Return_Status;
/*     */   private short digital_Input_1_Status;
/*     */   private short digital_Input_2_Status;
/*     */   private short digital_Input_3_Status;
/*     */   private short digital_Input_4_Status;
/*     */   
/*     */   public int getId_Module() {
/*  45 */     return this.id_Module;
/*     */   }
/*     */   
/*     */   public void setId_Module(int id_Module) {
/*  49 */     this.id_Module = id_Module;
/*     */   }
/*     */   
/*     */   public String getIccid() {
/*  53 */     return this.iccid;
/*     */   }
/*     */   
/*     */   public void setIccid(String iccid) {
/*  57 */     this.iccid = iccid;
/*     */   }
/*     */   
/*     */   public int getOperation_Mode() {
/*  61 */     return this.operation_Mode;
/*     */   }
/*     */   
/*     */   public void setOperation_Mode(int operation_Mode) {
/*  65 */     this.operation_Mode = operation_Mode;
/*     */   }
/*     */   
/*     */   public Calendar getConnection_Timestamp() {
/*  69 */     return this.connection_Timestamp;
/*     */   }
/*     */   
/*     */   public void setConnection_Timestamp(Calendar connection_Timestamp) {
/*  73 */     this.connection_Timestamp = connection_Timestamp;
/*     */   }
/*     */   
/*     */   public short getAlarm_Panel_Connection_Status() {
/*  77 */     return this.alarm_Panel_Connection_Status;
/*     */   }
/*     */   
/*     */   public void setAlarm_Panel_Connection_Status(short alarm_Panel_Connection_Status) {
/*  81 */     this.alarm_Panel_Connection_Status = alarm_Panel_Connection_Status;
/*     */   }
/*     */   
/*     */   public Calendar getLast_Communication() {
/*  85 */     return this.last_Communication;
/*     */   }
/*     */   
/*     */   public void setLast_Communication(Calendar last_Communication) {
/*  89 */     this.last_Communication = last_Communication;
/*     */   }
/*     */   
/*     */   public String getModule_Ip_Addr() {
/*  93 */     return this.module_Ip_Addr;
/*     */   }
/*     */   
/*     */   public void setModule_Ip_Addr(String module_Ip_Addr) {
/*  97 */     this.module_Ip_Addr = module_Ip_Addr;
/*     */   }
/*     */   
/*     */   public short getLine_Simulator_Status() {
/* 101 */     return this.line_Simulator_Status;
/*     */   }
/*     */   
/*     */   public void setLine_Simulator_Status(short line_Simulator_Status) {
/* 105 */     this.line_Simulator_Status = line_Simulator_Status;
/*     */   }
/*     */   
/*     */   public short getPhone_Line_Status() {
/* 109 */     return this.phone_Line_Status;
/*     */   }
/*     */   
/*     */   public void setPhone_Line_Status(short phone_Line_Status) {
/* 113 */     this.phone_Line_Status = phone_Line_Status;
/*     */   }
/*     */   
/*     */   public short getPegasus_Firmware_Version() {
/* 117 */     return this.pegasus_Firmware_Version;
/*     */   }
/*     */   
/*     */   public void setPegasus_Firmware_Version(short pegasus_Firmware_Version) {
/* 121 */     this.pegasus_Firmware_Version = pegasus_Firmware_Version;
/*     */   }
/*     */   
/*     */   public short getLine_Simulator_Firmware_Version() {
/* 125 */     return this.line_Simulator_Firmware_Version;
/*     */   }
/*     */   
/*     */   public void setLine_Simulator_Firmware_Version(short line_Simulator_Firmware_Version) {
/* 129 */     this.line_Simulator_Firmware_Version = line_Simulator_Firmware_Version;
/*     */   }
/*     */   
/*     */   public short getDual_Monitoring_Status() {
/* 133 */     return this.dual_Monitoring_Status;
/*     */   }
/*     */   
/*     */   public void setDual_Monitoring_Status(short dual_Monitoring_Status) {
/* 137 */     this.dual_Monitoring_Status = dual_Monitoring_Status;
/*     */   }
/*     */   
/*     */   public short getLast_Signal_Level() {
/* 141 */     return this.last_Signal_Level;
/*     */   }
/*     */   
/*     */   public void setLast_Signal_Level(short last_Signal_Level) {
/* 145 */     this.last_Signal_Level = last_Signal_Level;
/*     */   }
/*     */   
/*     */   public short getAlarm_Panel_Comm_Status() {
/* 149 */     return this.alarm_Panel_Comm_Status;
/*     */   }
/*     */   
/*     */   public void setAlarm_Panel_Comm_Status(short alarm_Panel_Comm_Status) {
/* 153 */     this.alarm_Panel_Comm_Status = alarm_Panel_Comm_Status;
/*     */   }
/*     */   
/*     */   public int getGprs_Comm_Timeout() {
/* 157 */     return this.gprs_Comm_Timeout;
/*     */   }
/*     */   
/*     */   public void setGprs_Comm_Timeout(int gprs_Comm_Timeout) {
/* 161 */     this.gprs_Comm_Timeout = gprs_Comm_Timeout;
/*     */   }
/*     */   
/*     */   public int getCsd_Comm_Timeout() {
/* 165 */     return this.csd_Comm_Timeout;
/*     */   }
/*     */   
/*     */   public void setCsd_Comm_Timeout(int csd_Comm_Timeout) {
/* 169 */     this.csd_Comm_Timeout = csd_Comm_Timeout;
/*     */   }
/*     */   
/*     */   public int getEth_Comm_Timeout() {
/* 173 */     return this.eth_Comm_Timeout;
/*     */   }
/*     */   
/*     */   public void setEth_Comm_Timeout(int eth_Comm_Timeout) {
/* 177 */     this.eth_Comm_Timeout = eth_Comm_Timeout;
/*     */   }
/*     */   
/*     */   public short getLast_Comm_Interface() {
/* 181 */     return this.last_Comm_Interface;
/*     */   }
/*     */   
/*     */   public void setLast_Comm_Interface(short last_Comm_Interface) {
/* 185 */     this.last_Comm_Interface = last_Comm_Interface;
/*     */   }
/*     */   
/*     */   public float getLast_Battery_Level() {
/* 189 */     return this.last_Battery_Level;
/*     */   }
/*     */   
/*     */   public void setLast_Battery_Level(float last_Battery_Level) {
/* 193 */     this.last_Battery_Level = last_Battery_Level;
/*     */   }
/*     */   
/*     */   public short getAc_Supply_Status() {
/* 197 */     return this.ac_Supply_Status;
/*     */   }
/*     */   
/*     */   public void setAc_Supply_Status(short ac_Supply_Status) {
/* 201 */     this.ac_Supply_Status = ac_Supply_Status;
/*     */   }
/*     */   
/*     */   public short getGsm_Freq() {
/* 205 */     return this.gsm_Freq;
/*     */   }
/*     */   
/*     */   public void setGsm_Freq(short gsm_Freq) {
/* 209 */     this.gsm_Freq = gsm_Freq;
/*     */   }
/*     */   
/*     */   public short getAlarm_Panel_Return_Status() {
/* 213 */     return this.alarm_Panel_Return_Status;
/*     */   }
/*     */   
/*     */   public void setAlarm_Panel_Return_Status(short alarm_Panel_Return_Status) {
/* 217 */     this.alarm_Panel_Return_Status = alarm_Panel_Return_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_1_Status() {
/* 221 */     return this.digital_Input_1_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_1_Status(short digital_Input_1_Status) {
/* 225 */     this.digital_Input_1_Status = digital_Input_1_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_2_Status() {
/* 229 */     return this.digital_Input_2_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_2_Status(short digital_Input_2_Status) {
/* 233 */     this.digital_Input_2_Status = digital_Input_2_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_3_Status() {
/* 237 */     return this.digital_Input_3_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_3_Status(short digital_Input_3_Status) {
/* 241 */     this.digital_Input_3_Status = digital_Input_3_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_4_Status() {
/* 245 */     return this.digital_Input_4_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_4_Status(short digital_Input_4_Status) {
/* 249 */     this.digital_Input_4_Status = digital_Input_4_Status;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dto\SP_044DataHolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */