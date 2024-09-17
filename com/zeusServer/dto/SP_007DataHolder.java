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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SP_007DataHolder
/*     */ {
/*     */   private int id_Client;
/*     */   private Calendar last_Communication;
/*     */   private int comm_Timeout;
/*     */   private short phone_Line_Status;
/*     */   private short alarm_Panel_Return_Status;
/*     */   private short digital_Input_1_Status;
/*     */   private short digital_Input_2_Status;
/*     */   private short digital_Input_3_Status;
/*     */   private short digital_Input_4_Status;
/*     */   private short line_Simulator_Status;
/*     */   private short alarm_Panel_Connection_Status;
/*     */   private short dual_Monitoring_Status;
/*     */   private short min_Signal_Level;
/*     */   private short last_Signal_Level;
/*     */   private short alarm_Panel_Comm_Status;
/*     */   private float last_Battery_Level;
/*     */   private short ac_Supply_Status;
/*     */   private int peripherals_Offline;
/*     */   
/*     */   public int getId_Client() {
/*  41 */     return this.id_Client;
/*     */   }
/*     */   
/*     */   public void setId_Client(int id_Client) {
/*  45 */     this.id_Client = id_Client;
/*     */   }
/*     */   
/*     */   public Calendar getLast_Communication() {
/*  49 */     return this.last_Communication;
/*     */   }
/*     */   
/*     */   public void setLast_Communication(Calendar last_Communication) {
/*  53 */     this.last_Communication = last_Communication;
/*     */   }
/*     */   
/*     */   public int getComm_Timeout() {
/*  57 */     return this.comm_Timeout;
/*     */   }
/*     */   
/*     */   public void setComm_Timeout(int comm_Timeout) {
/*  61 */     this.comm_Timeout = comm_Timeout;
/*     */   }
/*     */   
/*     */   public short getPhone_Line_Status() {
/*  65 */     return this.phone_Line_Status;
/*     */   }
/*     */   
/*     */   public void setPhone_Line_Status(short phone_Line_Status) {
/*  69 */     this.phone_Line_Status = phone_Line_Status;
/*     */   }
/*     */   
/*     */   public short getAlarm_Panel_Return_Status() {
/*  73 */     return this.alarm_Panel_Return_Status;
/*     */   }
/*     */   
/*     */   public void setAlarm_Panel_Return_Status(short alarm_Panel_Return_Status) {
/*  77 */     this.alarm_Panel_Return_Status = alarm_Panel_Return_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_1_Status() {
/*  81 */     return this.digital_Input_1_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_1_Status(short digital_Input_1_Status) {
/*  85 */     this.digital_Input_1_Status = digital_Input_1_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_2_Status() {
/*  89 */     return this.digital_Input_2_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_2_Status(short digital_Input_2_Status) {
/*  93 */     this.digital_Input_2_Status = digital_Input_2_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_3_Status() {
/*  97 */     return this.digital_Input_3_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_3_Status(short digital_Input_3_Status) {
/* 101 */     this.digital_Input_3_Status = digital_Input_3_Status;
/*     */   }
/*     */   
/*     */   public short getDigital_Input_4_Status() {
/* 105 */     return this.digital_Input_4_Status;
/*     */   }
/*     */   
/*     */   public void setDigital_Input_4_Status(short digital_Input_4_Status) {
/* 109 */     this.digital_Input_4_Status = digital_Input_4_Status;
/*     */   }
/*     */   
/*     */   public short getLine_Simulator_Status() {
/* 113 */     return this.line_Simulator_Status;
/*     */   }
/*     */   
/*     */   public void setLine_Simulator_Status(short line_Simulator_Status) {
/* 117 */     this.line_Simulator_Status = line_Simulator_Status;
/*     */   }
/*     */   
/*     */   public short getAlarm_Panel_Connection_Status() {
/* 121 */     return this.alarm_Panel_Connection_Status;
/*     */   }
/*     */   
/*     */   public void setAlarm_Panel_Connection_Status(short alarm_Panel_Connection_Status) {
/* 125 */     this.alarm_Panel_Connection_Status = alarm_Panel_Connection_Status;
/*     */   }
/*     */   
/*     */   public short getDual_Monitoring_Status() {
/* 129 */     return this.dual_Monitoring_Status;
/*     */   }
/*     */   
/*     */   public void setDual_Monitoring_Status(short dual_Monitoring_Status) {
/* 133 */     this.dual_Monitoring_Status = dual_Monitoring_Status;
/*     */   }
/*     */   
/*     */   public short getMin_Signal_Level() {
/* 137 */     return this.min_Signal_Level;
/*     */   }
/*     */   
/*     */   public void setMin_Signal_Level(short min_Signal_Level) {
/* 141 */     this.min_Signal_Level = min_Signal_Level;
/*     */   }
/*     */   
/*     */   public short getLast_Signal_Level() {
/* 145 */     return this.last_Signal_Level;
/*     */   }
/*     */   
/*     */   public void setLast_Signal_Level(short last_Signal_Level) {
/* 149 */     this.last_Signal_Level = last_Signal_Level;
/*     */   }
/*     */   
/*     */   public short getAlarm_Panel_Comm_Status() {
/* 153 */     return this.alarm_Panel_Comm_Status;
/*     */   }
/*     */   
/*     */   public void setAlarm_Panel_Comm_Status(short alarm_Panel_Comm_Status) {
/* 157 */     this.alarm_Panel_Comm_Status = alarm_Panel_Comm_Status;
/*     */   }
/*     */   
/*     */   public float getLast_Battery_Level() {
/* 161 */     return this.last_Battery_Level;
/*     */   }
/*     */   
/*     */   public void setLast_Battery_Level(float last_Battery_Level) {
/* 165 */     this.last_Battery_Level = last_Battery_Level;
/*     */   }
/*     */   
/*     */   public short getAc_Supply_Status() {
/* 169 */     return this.ac_Supply_Status;
/*     */   }
/*     */   
/*     */   public void setAc_Supply_Status(short ac_Supply_Status) {
/* 173 */     this.ac_Supply_Status = ac_Supply_Status;
/*     */   }
/*     */   
/*     */   public int getPeripherals_Offline() {
/* 177 */     return this.peripherals_Offline;
/*     */   }
/*     */   
/*     */   public void setPeripherals_Offline(int peripherals_Offline) {
/* 181 */     this.peripherals_Offline = peripherals_Offline;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dto\SP_007DataHolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */