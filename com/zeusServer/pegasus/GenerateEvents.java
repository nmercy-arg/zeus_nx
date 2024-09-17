/*     */ package com.zeusServer.pegasus;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.dto.SP_011DataHolder;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.SendEmail;
/*     */ import com.zeusServer.util.ZeusServerCfg;
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
/*     */ public class GenerateEvents
/*     */   implements Runnable
/*     */ {
/*     */   public static Long wdt;
/*  34 */   private final int TIME_BETWEEN_EXECUTIONS_THREAD_EVENTS_GENERATION_AND_EMAILS_DISPATCH = 10000;
/*  35 */   private final int DISPATCH_EMAIL_TIMEOUT = 30000;
/*     */   public boolean flag;
/*  37 */   private static List<Short> occList = new ArrayList<>(11);
/*     */   
/*     */   public GenerateEvents() {
/*  40 */     wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*  41 */     this.flag = true;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  48 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Task_for_event_generation_and_transmission_of_e-mails_started"), Enums.EnumMessagePriority.LOW, null, null);
/*  49 */       if (isEmailConfigured()) {
/*  50 */         SendEmail.initializeSMTP(30000);
/*     */       }
/*  52 */       while (this.flag) {
/*     */         try {
/*  54 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.RESTORE || GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  55 */             wdt = Functions.updateWatchdog(wdt, 100L);
/*     */             
/*     */             continue;
/*     */           } 
/*  59 */           List<SP_011DataHolder> sp11DHList = PegasusDBManager.executeSP_011();
/*  60 */           if (sp11DHList != null) {
/*  61 */             for (SP_011DataHolder sp11DH : sp11DHList) {
/*  62 */               boolean newEvent = false;
/*  63 */               if (sp11DH.getAcknowledged() == null) {
/*  64 */                 if (sp11DH.getNotified() != null) {
/*  65 */                   Calendar cal = sp11DH.getNotified();
/*  66 */                   cal = Functions.addTime2Calendar(cal, 12, sp11DH.getEvent_Freq());
/*  67 */                   newEvent = (sp11DH.getEvent_Freq() > 0 && cal.getTimeInMillis() < System.currentTimeMillis());
/*     */                 } else {
/*  69 */                   newEvent = true;
/*     */                 } 
/*     */               }
/*  72 */               boolean newRestore = (sp11DH.getTerminated() != null);
/*  73 */               if (newEvent || newRestore) {
/*  74 */                 if (sp11DH.getEvent_Code() != null && sp11DH.getEvent_Code().length() == 8 && sp11DH.getClient_Code() != null && sp11DH.getClient_Code().length() == 4) {
/*     */                   
/*  76 */                   if (newEvent) {
/*  77 */                     Functions.saveEvent(Util.EnumProductIDs.PEGASUS.getProductId(), sp11DH.getId_Module(), sp11DH.getId_Group(), sp11DH.getId_Client(), null, sp11DH.getClient_Code(), sp11DH.getEvent_Code(), Enums.EnumEventQualifier.NEW_EVENT, sp11DH.getVersionRcvd(), sp11DH.getNwProtocol(), 0, -1, -1);
/*     */                   }
/*  79 */                   if (newRestore) {
/*  80 */                     Functions.saveEvent(Util.EnumProductIDs.PEGASUS.getProductId(), sp11DH.getId_Module(), sp11DH.getId_Group(), sp11DH.getId_Client(), null, sp11DH.getClient_Code(), sp11DH.getEvent_Code(), Enums.EnumEventQualifier.NEW_RESTORE, sp11DH.getVersionRcvd(), sp11DH.getNwProtocol(), 0, -1, -1);
/*     */                   }
/*     */                 } 
/*  83 */                 if (isEmailConfigured() && SendEmail.getMailSessionExists()) {
/*  84 */                   if (newEvent) {
/*  85 */                     fetchReceipts(sp11DH.getId_Client(), sp11DH.getName(), sp11DH.getOccurrence_Type(), sp11DH.getOccurred(), false);
/*     */                   }
/*  87 */                   if (newRestore) {
/*  88 */                     fetchReceipts(sp11DH.getId_Client(), sp11DH.getName(), sp11DH.getOccurrence_Type(), sp11DH.getOccurred(), true);
/*     */                   }
/*     */                 } 
/*  91 */                 if (newEvent) {
/*  92 */                   PegasusDBManager.executeSP_020(sp11DH.getId_Occurrence());
/*     */                 }
/*  94 */                 if (newRestore) {
/*  95 */                   PegasusDBManager.executeSP_017(sp11DH.getId_Occurrence());
/*     */                 }
/*     */               } 
/*  98 */               wdt = Functions.updateWatchdog(wdt, 0L);
/*     */             } 
/*     */           }
/* 101 */         } catch (Exception ex) {
/* 102 */           ex.printStackTrace();
/*     */         } 
/* 104 */         wdt = Functions.updateWatchdog(wdt, 10000L);
/*     */       }
/*     */     
/* 107 */     } catch (Exception ex) {
/* 108 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Exception_in_the_task_for_event_generation_and_transmission_of_e-mails"), Enums.EnumMessagePriority.LOW, null, ex);
/* 109 */       GlobalVariables.buzzerActivated = true;
/*     */     } finally {
/* 111 */       dispose();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void fetchReceipts(int id_Client, String name, short occurrence_Type, Calendar occured, boolean restorationOccured) {
/*     */     try {
/* 118 */       for (String email : PegasusDBManager.executeSP_031(id_Client)) {
/* 119 */         if (email != null && email.length() > 0) {
/* 120 */           String mailMessage = makeMsgBody(name, occurrence_Type, occured, restorationOccured);
/* 121 */           if (mailMessage != null && mailMessage.length() > 0) {
/* 122 */             SendEmail.sendMail(email, LocaleMessage.getLocaleMessage("Occurrence"), mailMessage, false, 1);
/*     */           }
/*     */         } 
/*     */       } 
/* 126 */     } catch (Exception ex) {
/* 127 */       ex.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean isEmailConfigured() {
/* 132 */     boolean boolRc = false;
/* 133 */     if (ZeusServerCfg.getInstance().getNameSender() != null && ZeusServerCfg.getInstance().getMailAccount() != null && ZeusServerCfg.getInstance().getSmtpServer() != null && ZeusServerCfg.getInstance().getNameSender().length() > 0 && ZeusServerCfg.getInstance().getMailAccount().length() > 0 && ZeusServerCfg.getInstance().getSmtpServer().length() > 0) {
/* 134 */       boolRc = true;
/*     */     }
/* 136 */     if (ZeusServerCfg.getInstance().getSmtpServerRequiresAuth()) {
/* 137 */       boolRc = (boolRc && ZeusServerCfg.getInstance().getPop3User() != null && ZeusServerCfg.getInstance().getPop3User().length() > 0 && ZeusServerCfg.getInstance().getPop3Pass() != null && ZeusServerCfg.getInstance().getPop3Pass().length() > 0);
/*     */     }
/* 139 */     return boolRc;
/*     */   }
/*     */   
/*     */   private void dispose() {
/* 143 */     Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("Task_for_event_generation_and_transmission_of_e-mails_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */ 
/*     */   
/*     */   private String makeMsgBody(String name, short occurrence_Type, Calendar occured, boolean restorationOccured) {
/* 148 */     if (occurrence_Type == Enums.EnumOccurrenceType.MODULE_OFFLINE.getOccuranceType())
/* 149 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_module_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ONLINE_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_module_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OFFLINE_since") + Functions.formatDate2String(occured.getTime())); 
/* 150 */     if (occurrence_Type == Enums.EnumOccurrenceType.GPRS_NETWORK_OFFLINE.getOccuranceType())
/* 151 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_GPRS_communication_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ONLINE_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_GPRS_communication_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OFFLINE_since") + Functions.formatDate2String(occured.getTime())); 
/* 152 */     if (occurrence_Type == Enums.EnumOccurrenceType.LINE_SIMULATOR_OFFLINE.getOccuranceType())
/* 153 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_line_simulator_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ONLINE_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_line_simulator_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OFFLINE_since") + Functions.formatDate2String(occured.getTime())); 
/* 154 */     if (occurrence_Type == Enums.EnumOccurrenceType.TELEPHONE_LINE_NOT_DETECTED.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.PHONE_LINE_STATUS.getOccuranceType())
/* 155 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_telephone_line_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_telephone_line_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ABSENT_since") + Functions.formatDate2String(occured.getTime())); 
/* 156 */     if (occurrence_Type == Enums.EnumOccurrenceType.DUAL_MONITORING_FAILURE.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.DUAL_MONITORING_STATUS.getOccuranceType())
/* 157 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_double_monitoring_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_double_monitoring_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 158 */     if (occurrence_Type == Enums.EnumOccurrenceType.SIGNAL_LEVEL_BELOW_MINIMUM.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.GSM_SIGNAL_BELOW_MIN.getOccuranceType())
/* 159 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_GSM_signal_level_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Low_GSM_signal_on_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 160 */     if (occurrence_Type == Enums.EnumOccurrenceType.BATTERY_VOLTAGE_BELOW_MINIMUM.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.BATTERY_VOLTAGE_BELOW_MIN.getOccuranceType())
/* 161 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_battery_voltage_level_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_battery_voltage_level_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_below_the_minimum_since") + Functions.formatDate2String(occured.getTime())); 
/* 162 */     if (occurrence_Type == Enums.EnumOccurrenceType.POWER_SUPPLY_NOT_DETECTED.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.MAIN_PWR_SUPPLY_STATUS.getOccuranceType())
/* 163 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_main_power_supply_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_main_power_supply_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ABSENT_since") + Functions.formatDate2String(occured.getTime())); 
/* 164 */     if (occurrence_Type == Enums.EnumOccurrenceType.PERIPHERAL_OFFLINE.getOccuranceType())
/* 165 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_peripheral_communication_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_peripheral_communication_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_has_PROBLEMS_since") + Functions.formatDate2String(occured.getTime())); 
/* 166 */     if (occurrence_Type == Enums.EnumOccurrenceType.DIGITAL_INPUT_1_ACTIVATED.getOccuranceType())
/* 167 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_digital_input_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DEACTIVATED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_digital_input_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ACTIVATED_since") + Functions.formatDate2String(occured.getTime())); 
/* 168 */     if (occurrence_Type == Enums.EnumOccurrenceType.DIGITAL_INPUT_2_ACTIVATED.getOccuranceType())
/* 169 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_digital_input_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DEACTIVATED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_digital_input_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ACTIVATED_since") + Functions.formatDate2String(occured.getTime())); 
/* 170 */     if (occurrence_Type == Enums.EnumOccurrenceType.DIGITAL_INPUT_3_ACTIVATED.getOccuranceType())
/* 171 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_digital_input_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DEACTIVATED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_digital_input_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ACTIVATED_since") + Functions.formatDate2String(occured.getTime())); 
/* 172 */     if (occurrence_Type == Enums.EnumOccurrenceType.DIGITAL_INPUT_4_ACTIVATED.getOccuranceType())
/* 173 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_digital_input_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DEACTIVATED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_digital_input_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ACTIVATED_since") + Functions.formatDate2String(occured.getTime())); 
/* 174 */     if (occurrence_Type == Enums.EnumOccurrenceType.GSM_JAMMER_STATUS.getOccuranceType())
/* 175 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("No_GSM_Jammer_detected_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("GSM_Jammer_detected_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 176 */     if (occurrence_Type == Enums.EnumOccurrenceType.SIMCARD_1_STATUS.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.SIMCARD_1_FAIL.getOccuranceType())
/* 177 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_SIM-Card_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OPERATING_NORMALLY_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_SIM-Card_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_has_PROBLEMS_since") + Functions.formatDate2String(occured.getTime())); 
/* 178 */     if (occurrence_Type == Enums.EnumOccurrenceType.SIMCARD_2_STATUS.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.SIMCARD_2_FAIL.getOccuranceType())
/* 179 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_SIM-Card_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OPERATING_NORMALLY_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_SIM-Card_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_has_PROBLEMS_since") + Functions.formatDate2String(occured.getTime())); 
/* 180 */     if (occurrence_Type == Enums.EnumOccurrenceType.SIMCARD_1_OPERATIVE_STATUS.getOccuranceType())
/* 181 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("No_GSM_Jammer_detected_on_the_SIM-Card_#1_of_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_SIM-Card_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_JAMMED_since") + Functions.formatDate2String(occured.getTime())); 
/* 182 */     if (occurrence_Type == Enums.EnumOccurrenceType.SIMCARD_2_OPERATIVE_STATUS.getOccuranceType())
/* 183 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("No_GSM_Jammer_detected_on_the_SIM-Card_#2_of_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_SIM-Card_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_JAMMED_since") + Functions.formatDate2String(occured.getTime())); 
/* 184 */     if (occurrence_Type == Enums.EnumOccurrenceType.ALARM_PANEL_RETURN_CUTOFF.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.ALARM_PANEL_RETURN_STATUS.getOccuranceType())
/* 185 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_alarm_panel_return_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_alarm_panel_return_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ABSENT_since") + Functions.formatDate2String(occured.getTime())); 
/* 186 */     if (occurrence_Type == Enums.EnumOccurrenceType.MANUAL_CONTROL_ALARM_PANEL_CONNECTION.getOccuranceType())
/* 187 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Control_of_the_alarm_panel_connection_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_in_AUTOMATIC_MODE_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Control_of_the_alarm_panel_connection_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_in_MANUAL_MODE_since") + Functions.formatDate2String(occured.getTime())); 
/* 188 */     if (occurrence_Type == Enums.EnumOccurrenceType.ALARM_PANEL_COMMUNICATION_FAILURE.getOccuranceType() || occurrence_Type == Enums.EnumOccurrenceType.ALARM_PANEL_COMM_STATUS.getOccuranceType())
/* 189 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_communication_with_the_alarm_panel_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Communication_failure_with_the_alarm_panel_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 190 */     if (occurrence_Type == Enums.EnumOccurrenceType.ALARM_PANEL_COMM_TEST_STATUS.getOccuranceType())
/* 191 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_with_the_alarm_panel_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_with_the_alarm_panel_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 192 */     if (occurrence_Type == Enums.EnumOccurrenceType.TELEPHONE_LINE_COMM_TEST_STATUS.getOccuranceType())
/* 193 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_telephone_line_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_telephone_line_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 194 */     if (occurrence_Type == Enums.EnumOccurrenceType.ETH_IFACE_TEST_STATUS.getOccuranceType())
/* 195 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_Ethernet__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_Ethernet__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 196 */     if (occurrence_Type == Enums.EnumOccurrenceType.MODEM_IFACE_STATUS_1.getOccuranceType())
/* 197 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_GPRS_(SIM-Card_#1_APN_#1)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_GPRS_(SIM-Card_#1_APN_#1)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 198 */     if (occurrence_Type == Enums.EnumOccurrenceType.MODEM_IFACE_STATUS_2.getOccuranceType())
/* 199 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_GPRS_(SIM-Card_#1_APN_#2)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_GPRS_(SIM-Card_#1_APN_#2)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 200 */     if (occurrence_Type == Enums.EnumOccurrenceType.MODEM_IFACE_STATUS_3.getOccuranceType())
/* 201 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_GPRS_(SIM-Card_#2_APN_#1)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_GPRS_(SIM-Card_#2_APN_#1)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 202 */     if (occurrence_Type == Enums.EnumOccurrenceType.MODEM_IFACE_STATUS_4.getOccuranceType())
/* 203 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_GPRS_(SIM-Card_#2_APN_#2)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_GPRS_(SIM-Card_#2_APN_#2)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 204 */     if (occurrence_Type == Enums.EnumOccurrenceType.WIFI_IFACE_TEST_STATUS_AP_1.getOccuranceType())
/* 205 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_Wi-Fi_(AP_#1)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_Wi-Fi_(AP_#1)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 206 */     if (occurrence_Type == Enums.EnumOccurrenceType.WIFI_IFACE_TEST_STATUS_AP_2.getOccuranceType())
/* 207 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Success_on_the_communication_test_via_Wi-Fi_(AP_#2)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Failure_on_the_communication_test_via_Wi-Fi_(AP_#2)__client_[") + name + LocaleMessage.getLocaleMessage("]_at") + Functions.formatDate2String(occured.getTime())); 
/* 208 */     if (occurrence_Type == Enums.EnumOccurrenceType.TAMPER_DETECTION.getOccuranceType())
/* 209 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("Tamper_occurrence_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Tamper_occurrence_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_ACTIVATED_since") + Functions.formatDate2String(occured.getTime())); 
/* 210 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_NORMAL.getOccuranceType())
/* 211 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_VIOLATED_at") + Functions.formatDate2String(occured.getTime())); 
/* 212 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_ACTIVE.getOccuranceType())
/* 213 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(occured.getTime())); 
/* 214 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_WIREFAULT.getOccuranceType())
/* 215 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 216 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_TAMPER.getOccuranceType())
/* 217 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#1_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 218 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_NORMAL.getOccuranceType())
/* 219 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_VIOLATED_at") + Functions.formatDate2String(occured.getTime())); 
/* 220 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_ACTIVE.getOccuranceType())
/* 221 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(occured.getTime())); 
/* 222 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_WIREFAULT.getOccuranceType())
/* 223 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 224 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_TAMPER.getOccuranceType())
/* 225 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#2_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 226 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_NORMAL.getOccuranceType())
/* 227 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_VIOLATED_at") + Functions.formatDate2String(occured.getTime())); 
/* 228 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_ACTIVE.getOccuranceType())
/* 229 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(occured.getTime())); 
/* 230 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_WIREFAULT.getOccuranceType())
/* 231 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 232 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_TAMPER.getOccuranceType())
/* 233 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#3_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 234 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_NORMAL.getOccuranceType())
/* 235 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_VIOLATED_at") + Functions.formatDate2String(occured.getTime())); 
/* 236 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_ACTIVE.getOccuranceType())
/* 237 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Zone_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(occured.getTime())); 
/* 238 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_WIREFAULT.getOccuranceType())
/* 239 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_WIRE_FAULT_on_the_Zone_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 240 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_TAMPER.getOccuranceType())
/* 241 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_TAMPER_on_the_Zone_#4_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime())); 
/* 242 */     if (occurrence_Type == Enums.EnumOccurrenceType.GSM_MODEM_FAIL.getOccuranceType())
/* 243 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_GSM/GPRS_modem_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OPERATING_NORMALLY_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_GSM/GPRS_modem_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_has_PROBLEMS_since") + Functions.formatDate2String(occured.getTime())); 
/* 244 */     if (occurrence_Type == Enums.EnumOccurrenceType.ETH_FAIL.getOccuranceType())
/* 245 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_hardware_of_the_Ethernet_interface_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OPERATING_NORMALLY_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_hardware_of_the_Ethernet_interface_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_has_PROBLEMS_since") + Functions.formatDate2String(occured.getTime())); 
/* 246 */     if (occurrence_Type == Enums.EnumOccurrenceType.WIFI_FAIL.getOccuranceType()) {
/* 247 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_Wi-Fi_module_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_is_OPERATING_NORMALLY_since") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_Wi-Fi_module_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_has_PROBLEMS_since") + Functions.formatDate2String(occured.getTime()));
/*     */     }
/* 249 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_ALARM.getOccuranceType())
/* 250 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_ALARM_on_the_Zone_#") + '\001' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("ALARM_on_the_Zone_#") + '\001' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 251 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_ALARM.getOccuranceType())
/* 252 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_ALARM_on_the_Zone_#") + '\002' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("ALARM_on_the_Zone_#") + '\002' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 253 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_ALARM.getOccuranceType())
/* 254 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_ALARM_on_the_Zone_#") + '\003' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("ALARM_on_the_Zone_#") + '\003' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 255 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_ALARM.getOccuranceType())
/* 256 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_ALARM_on_the_Zone_#") + '\004' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("ALARM_on_the_Zone_#") + '\004' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 257 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_BYPASS.getOccuranceType())
/* 258 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\001' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\001' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 259 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_1_FORCE_ARMED.getOccuranceType())
/* 260 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\001' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\001' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 261 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_BYPASS.getOccuranceType())
/* 262 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\002' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\002' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 263 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_2_FORCE_ARMED.getOccuranceType())
/* 264 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\002' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\002' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 265 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_BYPASS.getOccuranceType())
/* 266 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\003' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\003' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 267 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_3_FORCE_ARMED.getOccuranceType())
/* 268 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\003' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\003' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 269 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_BYPASS.getOccuranceType())
/* 270 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\004' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_BYPASS_on_the_Zone_#") + '\004' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 271 */     if (occurrence_Type == Enums.EnumOccurrenceType.ZONE_4_FORCE_ARMED.getOccuranceType())
/* 272 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\004' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_FORCE_ARMED_on_the_Zone_#") + '\004' + LocaleMessage.getLocaleMessage("_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DONE_at") + Functions.formatDate2String(occured.getTime())); 
/* 273 */     if (occurrence_Type == Enums.EnumOccurrenceType.SYSTEM_AWAY_ARM.getOccuranceType())
/* 274 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_System_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_AWAY_ARMED_at") + Functions.formatDate2String(occured.getTime())); 
/* 275 */     if (occurrence_Type == Enums.EnumOccurrenceType.SYSTEM_FORCE_ARM.getOccuranceType())
/* 276 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_System_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_FORCE_ARMED_at") + Functions.formatDate2String(occured.getTime())); 
/* 277 */     if (occurrence_Type == Enums.EnumOccurrenceType.SYSTEM_STAY_ARM.getOccuranceType())
/* 278 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_System_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_STAY_ARMED_at") + Functions.formatDate2String(occured.getTime())); 
/* 279 */     if (occurrence_Type == Enums.EnumOccurrenceType.SYSTEM_FORCE_STAY_ARM.getOccuranceType())
/* 280 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_System_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_FORCE_STAY_ARMED_at") + Functions.formatDate2String(occured.getTime())); 
/* 281 */     if (occurrence_Type == Enums.EnumOccurrenceType.SYSTEM_DISARMED.getOccuranceType())
/* 282 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_System_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DISARMED_at") + Functions.formatDate2String(occured.getTime())); 
/* 283 */     if (occurrence_Type == Enums.EnumOccurrenceType.KEYLOQ_HW_FAILURE.getOccuranceType())
/* 284 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_communication_with_the_Keyfob_Receiver_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Communication_failure_with_the_Keyfob_Receiver_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 285 */     if (occurrence_Type == Enums.EnumOccurrenceType.IPIC.getOccuranceType())
/* 286 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_Insufficient_power_input_current_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_FIXED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Insufficient_power_input_current_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_since") + Functions.formatDate2String(occured.getTime())); 
/* 287 */     if (occurrence_Type >= Enums.EnumOccurrenceType.KEYFOB_LOW_BATTERY_15.getOccuranceType() && occurrence_Type <= Enums.EnumOccurrenceType.KEYFOB_LOW_BATTERY_1.getOccuranceType()) {
/* 288 */       int num = Enums.EnumOccurrenceType.KEYFOB_LOW_BATTERY_1.getOccuranceType() - occurrence_Type + 1;
/* 289 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_Keyfob_#") + num + LocaleMessage.getLocaleMessage("'Low_Battery_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("Keyfob_#") + num + LocaleMessage.getLocaleMessage("'s_Low_Battery_on_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_DETECTED_at") + Functions.formatDate2String(occured.getTime()));
/* 290 */     }  if (occurrence_Type >= Enums.EnumOccurrenceType.KEYFOB_PANIC_15.getOccuranceType() && occurrence_Type <= Enums.EnumOccurrenceType.KEYFOB_PANIC_1.getOccuranceType()) {
/* 291 */       int num = Enums.EnumOccurrenceType.KEYFOB_PANIC_1.getOccuranceType() - occurrence_Type + 1;
/* 292 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Keyfob_#") + num + LocaleMessage.getLocaleMessage("'Panic_Button_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_PRESSED_at") + Functions.formatDate2String(occured.getTime()));
/* 293 */     }  if (occurrence_Type >= Enums.EnumOccurrenceType.KEYFOB_COMM_TEST_15.getOccuranceType() && occurrence_Type <= Enums.EnumOccurrenceType.KEYFOB_COMM_TEST_1.getOccuranceType()) {
/* 294 */       int num = Enums.EnumOccurrenceType.KEYFOB_COMM_TEST_1.getOccuranceType() - occurrence_Type + 1;
/* 295 */       return (restorationOccured == true) ? "" : (LocaleMessage.getLocaleMessage("The_Keyfob_#") + num + LocaleMessage.getLocaleMessage("'Communication_Test_of_the_client_[") + name + LocaleMessage.getLocaleMessage("]_was_RECEIVED_at") + Functions.formatDate2String(occured.getTime()));
/* 296 */     }  if (occurrence_Type >= Enums.EnumOccurrenceType.BATTERY_DISCONNECT.getOccuranceType() && occurrence_Type <= Enums.EnumOccurrenceType.BATTERY_DISCONNECT.getOccuranceType()) {
/* 297 */       int num = Enums.EnumOccurrenceType.BATTERY_DISCONNECT.getOccuranceType() - occurrence_Type + 1;
/* 298 */       return (restorationOccured == true) ? (LocaleMessage.getLocaleMessage("The_Battery_Disconnect_of_the_client[") + num + LocaleMessage.getLocaleMessage("]_was_RESTORED_at") + Functions.formatDate2String(null)) : (LocaleMessage.getLocaleMessage("The_Battery_Disconnect_of_the_client[") + name + LocaleMessage.getLocaleMessage("]_is_ABSENT_since") + Functions.formatDate2String(occured.getTime()));
/*     */     } 
/* 300 */     return "";
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\pegasus\GenerateEvents.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */