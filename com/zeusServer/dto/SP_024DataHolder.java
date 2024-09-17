/*    */ package com.zeusServer.dto;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
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
/*    */ public class SP_024DataHolder
/*    */ {
/*    */   private int id_Module;
/*    */   private int id_Command;
/*    */   private int command_Type;
/*    */   private byte[] command_Data;
/*    */   private short exec_Retries;
/*    */   private String commandData;
/*    */   private byte[] commandFileData;
/*    */   
/*    */   public byte[] getCommand_Data() {
/* 28 */     return this.command_Data;
/*    */   }
/*    */   
/*    */   public void setCommand_Data(InputStream is) throws IOException {
/* 32 */     byte[] tmp = new byte[is.available()];
/* 33 */     is.read(tmp, 0, is.available());
/* 34 */     StringBuilder sb = new StringBuilder();
/* 35 */     for (byte b : tmp) {
/* 36 */       sb.append((char)b);
/*    */     }
/* 38 */     this.commandData = sb.toString();
/*    */   }
/*    */   
/*    */   public byte[] getCommandFileData() {
/* 42 */     return this.commandFileData;
/*    */   }
/*    */   
/*    */   public void setCommandFileData(byte[] commandFileData) {
/* 46 */     this.commandFileData = commandFileData;
/*    */   }
/*    */   
/*    */   public int getCommand_Type() {
/* 50 */     return this.command_Type;
/*    */   }
/*    */   
/*    */   public void setCommand_Type(int command_Type) {
/* 54 */     this.command_Type = command_Type;
/*    */   }
/*    */   
/*    */   public short getExec_Retries() {
/* 58 */     return this.exec_Retries;
/*    */   }
/*    */   
/*    */   public void setExec_Retries(short exec_Retries) {
/* 62 */     this.exec_Retries = exec_Retries;
/*    */   }
/*    */   
/*    */   public int getId_Command() {
/* 66 */     return this.id_Command;
/*    */   }
/*    */   
/*    */   public void setId_Command(int id_Command) {
/* 70 */     this.id_Command = id_Command;
/*    */   }
/*    */   
/*    */   public int getId_Module() {
/* 74 */     return this.id_Module;
/*    */   }
/*    */   
/*    */   public void setId_Module(int id_Module) {
/* 78 */     this.id_Module = id_Module;
/*    */   }
/*    */   
/*    */   public String getCommandData() {
/* 82 */     return this.commandData;
/*    */   }
/*    */   
/*    */   public void setCommandData(String commandData) {
/* 86 */     this.commandData = commandData;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\dto\SP_024DataHolder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */