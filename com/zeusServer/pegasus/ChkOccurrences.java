/*     */ package com.zeusServer.pegasus;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.dto.SP_007DataHolder;
/*     */ import com.zeusServer.serialPort.communication.CommReceiverCSD;
/*     */ import com.zeusServer.tblConnections.TblThreadsCommReceiversCSD;
/*     */ import com.zeusServer.util.ChkInternetLink;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.MonitoringInfo;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Calendar;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TimeZone;
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
/*     */ public class ChkOccurrences
/*     */   implements Runnable
/*     */ {
/*  39 */   private final long TIME_BETWEEN_EXECUTIONS_THREAD_CHECK_OCCURRENCES = 30000L;
/*  40 */   private long lastTransmissionEventInternetOffline = 0L;
/*  41 */   private long lastTransmissionEventGsmReceiverSignalLevelBelowMin = 0L;
/*  42 */   private long lastTransmissionEventLostCommunicationGsmReceiver = 0L;
/*  43 */   private long[] lastTransmissionEventsOfZeusBox = new long[8];
/*  44 */   private boolean[] lastStatusEventsOfZeusBox = new boolean[8];
/*     */ 
/*     */ 
/*     */   
/*     */   public static Long wdt;
/*     */ 
/*     */   
/*     */   public boolean flag;
/*     */ 
/*     */   
/*     */   Calendar c1;
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  60 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_occurrences_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  61 */       while (this.flag) {
/*  62 */         if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  63 */           wdt = Functions.updateWatchdog(wdt, 100L);
/*     */           continue;
/*     */         } 
/*     */         try {
/*  67 */           chkServerOccurrences(0);
/*  68 */           chkModuleOccurrences();
/*  69 */           wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  70 */         } catch (Exception ex) {
/*  71 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_verification_of_the_occurrences"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  72 */           Logger.getLogger(ChkOccurrences.class.getName()).log(Level.SEVERE, (String)null, ex);
/*  73 */           GlobalVariables.buzzerActivated = true;
/*     */         } 
/*  75 */         Thread.sleep(30000L);
/*     */       } 
/*  77 */     } catch (InterruptedException interruptedException) {
/*     */     
/*     */     } finally {
/*  80 */       dispose();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void dispose() {
/*  86 */     this.flag = false;
/*  87 */     Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Task_for_verification_of_the_occurrences_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private void chkServerOccurrences(int productId) throws SQLException, InterruptedException, Exception {
/*  91 */     if (ZeusServerCfg.getInstance().getClientCode().length() == 4) {
/*  92 */       if (ZeusServerCfg.getInstance().getMonitoringInfo() != null) {
/*  93 */         for (Map.Entry<String, MonitoringInfo> receiver : (Iterable<Map.Entry<String, MonitoringInfo>>)ZeusServerCfg.getInstance().getMonitoringInfo().entrySet()) {
/*  94 */           if (((MonitoringInfo)receiver.getValue()).getSelfTestEvent().length() == 8 && 
/*  95 */             ((MonitoringInfo)receiver.getValue()).lastTransmissionEventAutoTest <= System.currentTimeMillis()) {
/*  96 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ((MonitoringInfo)receiver.getValue()).getSelfTestEvent(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/*  97 */             ((MonitoringInfo)receiver.getValue()).lastTransmissionEventAutoTest = System.currentTimeMillis() + (((((MonitoringInfo)receiver.getValue()).getSelfTestFrequency().intValue() == 0) ? 1440 : ((MonitoringInfo)receiver.getValue()).getSelfTestFrequency().intValue()) * 60000);
/*     */           } 
/*     */         } 
/*     */       }
/*     */ 
/*     */       
/* 103 */       if (ZeusServerCfg.getInstance().getInternetOfflineEvent().length() == 8) {
/* 104 */         if (ChkInternetLink.online) {
/* 105 */           if (this.lastTransmissionEventInternetOffline > 0L) {
/* 106 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getInternetOfflineEvent(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 107 */             this.lastTransmissionEventInternetOffline = 0L;
/*     */           }
/*     */         
/* 110 */         } else if (this.lastTransmissionEventInternetOffline <= System.currentTimeMillis()) {
/* 111 */           Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getInternetOfflineEvent(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 112 */           this.lastTransmissionEventInternetOffline = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getInternetOfflineFrequency().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getInternetOfflineFrequency().intValue()) * 60000);
/*     */         } 
/*     */       }
/*     */ 
/*     */       
/* 117 */       if (ZeusServerCfg.getInstance().getRecipientsCSD().length() > 0) {
/* 118 */         if (ZeusServerCfg.getInstance().getEventGsmReceiverSignalLevelBelowMin().length() == 8) {
/* 119 */           boolean generateEventSignalLevelBelowMin = false;
/* 120 */           synchronized (TblThreadsCommReceiversCSD.getInstance()) {
/* 121 */             for (Map.Entry<String, CommReceiverCSD> receiver : (Iterable<Map.Entry<String, CommReceiverCSD>>)TblThreadsCommReceiversCSD.getInstance().entrySet()) {
/* 122 */               if (((CommReceiverCSD)receiver.getValue()).csdReceiverSignalLevel == ZeusServerCfg.getInstance().getMinimumReceivingSignalLevel().intValue()) {
/* 123 */                 generateEventSignalLevelBelowMin = true;
/*     */                 break;
/*     */               } 
/*     */             } 
/*     */           } 
/* 128 */           if (generateEventSignalLevelBelowMin) {
/* 129 */             if (this.lastTransmissionEventGsmReceiverSignalLevelBelowMin <= System.currentTimeMillis()) {
/* 130 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventGsmReceiverSignalLevelBelowMin(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 131 */               this.lastTransmissionEventGsmReceiverSignalLevelBelowMin = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqGenerationEventGsmReceiverSignalLevelBelowMin().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqGenerationEventGsmReceiverSignalLevelBelowMin().intValue()) * 60000);
/*     */             } 
/* 133 */           } else if (this.lastTransmissionEventGsmReceiverSignalLevelBelowMin > 0L) {
/* 134 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventGsmReceiverSignalLevelBelowMin(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 135 */             this.lastTransmissionEventGsmReceiverSignalLevelBelowMin = 0L;
/*     */           } 
/*     */         } 
/* 138 */         if (ZeusServerCfg.getInstance().getEventLostCommGsmReceiver().length() == 8) {
/* 139 */           boolean gerarEventoPerdaComunicacaoReceptora = false;
/* 140 */           synchronized (TblThreadsCommReceiversCSD.getInstance()) {
/* 141 */             for (Map.Entry<String, CommReceiverCSD> receiver : (Iterable<Map.Entry<String, CommReceiverCSD>>)TblThreadsCommReceiversCSD.getInstance().entrySet()) {
/* 142 */               if (!((CommReceiverCSD)receiver.getValue()).online) {
/* 143 */                 gerarEventoPerdaComunicacaoReceptora = true;
/*     */                 
/*     */                 break;
/*     */               } 
/*     */             } 
/*     */           } 
/* 149 */           if (gerarEventoPerdaComunicacaoReceptora) {
/* 150 */             if (this.lastTransmissionEventLostCommunicationGsmReceiver < System.currentTimeMillis()) {
/* 151 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventLostCommGsmReceiver(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 152 */               this.lastTransmissionEventLostCommunicationGsmReceiver = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqGenerationEventLostCommGsmReceiver().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqGenerationEventLostCommGsmReceiver().intValue()) * 60000);
/*     */             } 
/* 154 */           } else if (this.lastTransmissionEventLostCommunicationGsmReceiver > 0L) {
/* 155 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventLostCommGsmReceiver(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 156 */             this.lastTransmissionEventLostCommunicationGsmReceiver = 0L;
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 161 */       if (GlobalVariables.currentPlatform == Enums.Platform.ARM) {
/*     */         
/* 163 */         if (ZeusServerCfg.getInstance().getEventETH_1_disconnect() != null && 
/* 164 */           ZeusServerCfg.getInstance().getEventETH_1_disconnect().length() == 8) {
/* 165 */           if (this.lastStatusEventsOfZeusBox[0]) {
/* 166 */             if (this.lastTransmissionEventsOfZeusBox[0] > 0L) {
/* 167 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventETH_1_disconnect(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 168 */               this.lastTransmissionEventsOfZeusBox[0] = 0L;
/*     */             }
/*     */           
/* 171 */           } else if (this.lastTransmissionEventsOfZeusBox[0] <= System.currentTimeMillis()) {
/* 172 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventETH_1_disconnect(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 173 */             this.lastTransmissionEventsOfZeusBox[0] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqETH_1_disconnect().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqETH_1_disconnect().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 179 */         if (ZeusServerCfg.getInstance().getEventETH_2_disconnect() != null && 
/* 180 */           ZeusServerCfg.getInstance().getEventETH_2_disconnect().length() == 8) {
/* 181 */           if (this.lastStatusEventsOfZeusBox[1]) {
/* 182 */             if (this.lastTransmissionEventsOfZeusBox[1] > 0L) {
/* 183 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventETH_2_disconnect(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 184 */               this.lastTransmissionEventsOfZeusBox[1] = 0L;
/*     */             }
/*     */           
/* 187 */           } else if (this.lastTransmissionEventsOfZeusBox[1] <= System.currentTimeMillis()) {
/* 188 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventETH_2_disconnect(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 189 */             this.lastTransmissionEventsOfZeusBox[1] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqETH_2_disconnect().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqETH_2_disconnect().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 195 */         if (ZeusServerCfg.getInstance().getEventLowBattery() != null && 
/* 196 */           ZeusServerCfg.getInstance().getEventLowBattery().length() == 8) {
/* 197 */           if (this.lastStatusEventsOfZeusBox[2]) {
/* 198 */             if (this.lastTransmissionEventsOfZeusBox[2] > 0L) {
/* 199 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventLowBattery(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 200 */               this.lastTransmissionEventsOfZeusBox[2] = 0L;
/*     */             }
/*     */           
/* 203 */           } else if (this.lastTransmissionEventsOfZeusBox[2] <= System.currentTimeMillis()) {
/* 204 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventLowBattery(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 205 */             this.lastTransmissionEventsOfZeusBox[2] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqLowBattery().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqLowBattery().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 211 */         if (ZeusServerCfg.getInstance().getEventDeviceTampered() != null && 
/* 212 */           ZeusServerCfg.getInstance().getEventDeviceTampered().length() == 8) {
/* 213 */           if (this.lastStatusEventsOfZeusBox[3]) {
/* 214 */             if (this.lastTransmissionEventsOfZeusBox[3] > 0L) {
/* 215 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventDeviceTampered(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 216 */               this.lastTransmissionEventsOfZeusBox[3] = 0L;
/*     */             }
/*     */           
/* 219 */           } else if (this.lastTransmissionEventsOfZeusBox[3] <= System.currentTimeMillis()) {
/* 220 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventDeviceTampered(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 221 */             this.lastTransmissionEventsOfZeusBox[3] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqDeviceTampered().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqDeviceTampered().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 227 */         if (ZeusServerCfg.getInstance().getEventACPower() != null && 
/* 228 */           ZeusServerCfg.getInstance().getEventACPower().length() == 8) {
/* 229 */           if (this.lastStatusEventsOfZeusBox[4]) {
/* 230 */             if (this.lastTransmissionEventsOfZeusBox[4] > 0L) {
/* 231 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventACPower(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 232 */               this.lastTransmissionEventsOfZeusBox[4] = 0L;
/*     */             }
/*     */           
/* 235 */           } else if (this.lastTransmissionEventsOfZeusBox[4] <= System.currentTimeMillis()) {
/* 236 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventACPower(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 237 */             this.lastTransmissionEventsOfZeusBox[4] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqACPower().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqACPower().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 243 */         if (ZeusServerCfg.getInstance().getEventSDCardFailure() != null && 
/* 244 */           ZeusServerCfg.getInstance().getEventSDCardFailure().length() == 8) {
/* 245 */           if (this.lastStatusEventsOfZeusBox[5]) {
/* 246 */             if (this.lastTransmissionEventsOfZeusBox[5] > 0L) {
/* 247 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventSDCardFailure(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 248 */               this.lastTransmissionEventsOfZeusBox[5] = 0L;
/*     */             }
/*     */           
/* 251 */           } else if (this.lastTransmissionEventsOfZeusBox[5] <= System.currentTimeMillis()) {
/* 252 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventSDCardFailure(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 253 */             this.lastTransmissionEventsOfZeusBox[5] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqSDCardFailure().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqSDCardFailure().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 259 */         if (ZeusServerCfg.getInstance().getEventLowDiskSpace() != null && 
/* 260 */           ZeusServerCfg.getInstance().getEventLowDiskSpace().length() == 8) {
/* 261 */           if (this.lastStatusEventsOfZeusBox[6]) {
/* 262 */             if (this.lastTransmissionEventsOfZeusBox[6] > 0L) {
/* 263 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventLowDiskSpace(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 264 */               this.lastTransmissionEventsOfZeusBox[6] = 0L;
/*     */             }
/*     */           
/* 267 */           } else if (this.lastTransmissionEventsOfZeusBox[6] <= System.currentTimeMillis()) {
/* 268 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventLowDiskSpace(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 269 */             this.lastTransmissionEventsOfZeusBox[6] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqLowDiskSpace().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqLowDiskSpace().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 275 */         if (ZeusServerCfg.getInstance().getEventOverTemperature() != null && 
/* 276 */           ZeusServerCfg.getInstance().getEventOverTemperature().length() == 8) {
/* 277 */           if (this.lastStatusEventsOfZeusBox[7]) {
/* 278 */             if (this.lastTransmissionEventsOfZeusBox[7] > 0L) {
/* 279 */               Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventOverTemperature(), Enums.EnumEventQualifier.NEW_RESTORE, 0, "", 0, -1, 1);
/* 280 */               this.lastTransmissionEventsOfZeusBox[7] = 0L;
/*     */             }
/*     */           
/* 283 */           } else if (this.lastTransmissionEventsOfZeusBox[7] <= System.currentTimeMillis()) {
/* 284 */             Functions.saveEvent(productId, 0, 0, 0, ZeusServerCfg.getInstance().getDefaultMonitoringCOMPort(), ZeusServerCfg.getInstance().getClientCode(), ZeusServerCfg.getInstance().getEventOverTemperature(), Enums.EnumEventQualifier.NEW_EVENT, 0, "", 0, -1, 1);
/* 285 */             this.lastTransmissionEventsOfZeusBox[7] = System.currentTimeMillis() + (((ZeusServerCfg.getInstance().getFreqOverTemperature().intValue() == 0) ? 1440 : ZeusServerCfg.getInstance().getFreqOverTemperature().intValue()) * 60000);
/*     */           } 
/*     */         }
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void setZeusBoxEvents(int type, int index, boolean status) {
/* 295 */     this.lastStatusEventsOfZeusBox[index] = status;
/*     */   }
/*     */   
/*     */   public int getZeusBoxEventStatusByIndex(int index) {
/* 299 */     return this.lastStatusEventsOfZeusBox[index] ? 1 : 0;
/*     */   }
/*     */   
/*     */   private void chkModuleOccurrences() throws SQLException, InterruptedException {
/* 303 */     List<SP_007DataHolder> sp7DHList = PegasusDBManager.executeSP_007();
/* 304 */     if (sp7DHList != null) {
/* 305 */       for (SP_007DataHolder sp7DH : sp7DHList) {
/* 306 */         if (sp7DH.getLast_Communication() != null) {
/* 307 */           Calendar cal = Functions.addTime2Calendar(sp7DH.getLast_Communication(), 13, sp7DH.getComm_Timeout());
/* 308 */           if (cal.getTimeInMillis() < getCurrentTimeInMillisInGMT()) {
/* 309 */             if (System.currentTimeMillis() - GlobalVariables.applicationStartupTime.getTimeInMillis() > (sp7DH.getComm_Timeout() * 1000)) {
/* 310 */               PegasusDBManager.executeSP_001(sp7DH.getId_Client(), Enums.EnumOccurrenceType.MODULE_OFFLINE.getOccuranceType());
/*     */             }
/*     */           } else {
/* 313 */             PegasusDBManager.executeSP_009(sp7DH.getId_Client(), (short)Enums.EnumOccurrenceType.MODULE_OFFLINE.getOccuranceType());
/*     */           } 
/*     */         } 
/* 316 */         wdt = Functions.updateWatchdog(wdt, 0L);
/*     */       } 
/*     */     }
/*     */   }
/*     */   
/*     */   private long getCurrentTimeInMillisInGMT() {
/* 322 */     Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
/* 323 */     this.c1.set(1, c.get(1));
/* 324 */     this.c1.set(2, c.get(2));
/* 325 */     this.c1.set(5, c.get(5));
/* 326 */     this.c1.set(11, c.get(11));
/* 327 */     this.c1.set(12, c.get(12));
/* 328 */     this.c1.set(13, c.get(13));
/* 329 */     return this.c1.getTimeInMillis();
/*     */   } public ChkOccurrences() {
/* 331 */     this.c1 = Calendar.getInstance();
/*     */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*     */     this.flag = true;
/*     */     for (int i = 0; i < this.lastStatusEventsOfZeusBox.length; i++)
/*     */       this.lastStatusEventsOfZeusBox[i] = true; 
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\ChkOccurrences.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */