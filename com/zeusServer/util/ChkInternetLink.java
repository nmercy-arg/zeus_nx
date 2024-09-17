/*     */ package com.zeusServer.util;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.ui.UILogInitiator;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
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
/*     */ public class ChkInternetLink
/*     */   implements Runnable
/*     */ {
/*  23 */   private final int TIME_BETWEEN_ATTEMPTS_CONNECTION_SAME_SERVER = 2500;
/*     */   
/*     */   public static Long wdt;
/*     */   public static boolean online = true;
/*     */   private Socket sck;
/*     */   public boolean flag;
/*     */   
/*     */   public ChkInternetLink() {
/*  31 */     online = true;
/*  32 */     this.flag = true;
/*  33 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  40 */       int retries = 0;
/*     */ 
/*     */       
/*  43 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_internet_connection_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  44 */       while (this.flag) {
/*  45 */         String[] iServerInfo = ZeusServerCfg.getInstance().getServersTestInternet().split(";");
/*  46 */         int count = 0;
/*     */         
/*  48 */         label40: for (String iServer : iServerInfo) {
/*  49 */           String[] eachServer = iServer.split(",");
/*  50 */           if (eachServer != null && eachServer.length == 3) {
/*  51 */             String sIP = eachServer[0];
/*  52 */             String sPort = eachServer[1];
/*  53 */             String sInterface = eachServer[2];
/*  54 */             retries = 0;
/*  55 */             while (retries < ZeusServerCfg.getInstance().getRetriesTestInternet().intValue()) {
/*     */               
/*  57 */               try { openSocket();
/*     */                 
/*  59 */                 try { SocketFunctions.connect(this.sck, sIP, Integer.parseInt(sPort), ZeusServerCfg.getInstance().getTimeoutTestInternet().intValue() * 1000);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */                   
/*  69 */                   this.sck = SocketFunctions.closeSocket(this.sck); break label40; } catch (Exception ex) { Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Failure_while_connecting_to_the_server") + iServer + "] ", Enums.EnumMessagePriority.AVERAGE, null, null); this.sck = SocketFunctions.closeSocket(this.sck); }  } catch (Exception ex) { Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Failure_while_connecting_to_the_server") + iServer + "]: ", Enums.EnumMessagePriority.AVERAGE, null, ex); this.sck = SocketFunctions.closeSocket(this.sck); } finally { this.sck = SocketFunctions.closeSocket(this.sck); }
/*     */               
/*  71 */               wdt = Functions.updateWatchdog(wdt, 2500L);
/*  72 */               retries++;
/*     */             } 
/*     */           } 
/*  75 */           count++;
/*     */         } 
/*  77 */         if (count >= iServerInfo.length) {
/*  78 */           changeOnline(false);
/*  79 */           UILogInitiator.toggleImageById((short)2, false, null);
/*  80 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_The_Zeus_Server_detected_a_connection_failure_with_the_internet"), Enums.EnumMessagePriority.HIGH, null, null);
/*     */         } else {
/*  82 */           changeOnline(true);
/*  83 */           UILogInitiator.toggleImageById((short)2, true, null);
/*     */         } 
/*  85 */         wdt = Functions.updateWatchdog(wdt, (ZeusServerCfg.getInstance().getFrequenceTestInternet().intValue() * 1000));
/*     */       } 
/*  87 */     } catch (Exception ex) {
/*  88 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_verification_of_the_internet_connection"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  89 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/*  91 */       dispose();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void dispose() {
/*  96 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_internet_connection_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private void openSocket() throws SocketException {
/* 100 */     if (this.sck == null) {
/* 101 */       this.sck = new Socket();
/* 102 */       this.sck.setTcpNoDelay(true);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void changeOnline(boolean state) {
/* 107 */     if (((!state ? 1 : 0) & online) != 0) {
/* 108 */       GlobalVariables.buzzerActivated = true;
/*     */     }
/* 110 */     online = state;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\ChkInternetLink.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */