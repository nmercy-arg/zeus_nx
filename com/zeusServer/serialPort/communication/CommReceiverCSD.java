/*      */ package com.zeusServer.serialPort.communication;
/*      */ 
/*      */ import Serialio.SerInputStream;
/*      */ import Serialio.SerOutputStream;
/*      */ import Serialio.SerialPort;
/*      */ import Serialio.SerialPortLocal;
/*      */ import com.zeus.settings.beans.Util;
/*      */ import com.zeusServer.DBManagers.GenericDBManager;
/*      */ import com.zeusServer.DBManagers.PegasusDBManager;
/*      */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*      */ import com.zeusServer.pegasus.PegasusRoutines;
/*      */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*      */ import com.zeusServer.tblConnections.TblThreadsEmulaReceivers;
/*      */ import com.zeusServer.ui.UILogInitiator;
/*      */ import com.zeusServer.util.CRC16;
/*      */ import com.zeusServer.util.CfgReceiverCSD;
/*      */ import com.zeusServer.util.Enums;
/*      */ import com.zeusServer.util.Functions;
/*      */ import com.zeusServer.util.GlobalVariables;
/*      */ import com.zeusServer.util.InfoModule;
/*      */ import com.zeusServer.util.InfoSms;
/*      */ import com.zeusServer.util.LocaleMessage;
/*      */ import com.zeusServer.util.MonitoringInfo;
/*      */ import com.zeusServer.util.Rijndael;
/*      */ import com.zeusServer.util.ZeusServerCfg;
/*      */ import com.zeusServer.util.ZeusServerLogger;
/*      */ import com.zeuscc.pegasus.derby.beans.SP_062DataHolder;
/*      */ import com.zeuscc.pegasus.derby.beans.SP_V2_001_VO;
/*      */ import java.io.IOException;
/*      */ import java.security.InvalidAlgorithmParameterException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class CommReceiverCSD
/*      */   extends PegasusRoutines
/*      */   implements Runnable
/*      */ {
/*   59 */   private final int MAXIMUM_TIME_WAIT_REPLY_MODEM_COMMAND_AT = 30000;
/*   60 */   private final int MAXIMUM_TIME_WAIT_CALL_IDENTIFICATION = 500;
/*   61 */   private final int TIME_BETWEEN_CHECK_IF_MODEM_WAS_INITIALIZED = 60000;
/*   62 */   private final int TIME_BETWEEN_CHECK_NEW_SMS_MESSAGES = 10000;
/*   63 */   private final int MAXIMUM_RETRIES_TRYING_HANGUP = 3;
/*   64 */   private final int CSD_COMMUNICATION_TIMEOUT = 5000;
/*   65 */   private final long MAXIMUM_TIME_CSD_CONNECTION_INACTIVITY = 15000L;
/*   66 */   private final int MINIMUM_LENGTH_VALID_TELEPHONE_NUMBER = 7;
/*      */   private SerialPortLocal receiverCSDCommPort;
/*      */   private SerOutputStream sos;
/*      */   private SerInputStream sis;
/*      */   private InfoModule infoModule;
/*      */   private String telephoneNumberIdentified;
/*      */   private byte[] serialInputBuffer;
/*      */   private byte[] tmpSerialBuffer;
/*   74 */   private byte csdReceiverDetectedModel = (byte)Enums.EnumCSDReceiverModel.NONE.getModel();
/*   75 */   public Long wdt = Long.valueOf(System.currentTimeMillis() + 900000L);
/*      */   public boolean online = true;
/*      */   public boolean flag;
/*   78 */   public int csdReceiverSignalLevel = 31;
/*   79 */   private final String vbCrLf = "\r\n";
/*      */   public CfgReceiverCSD cfgReceiverCSD;
/*      */   public Thread myThread;
/*   82 */   public final String LEGACY_PEGASUS_SMS_EVENT = "02;";
/*   83 */   public final String LEGACY_PEGASUS_SMS_ALIVE = "05;";
/*      */ 
/*      */   
/*      */   public CommReceiverCSD(boolean flag) {
/*   87 */     this.flag = flag;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void run() {
/*   93 */     long nextCheckModemInitialized = 0L;
/*   94 */     long nextCheckNewSmsMessages = 0L;
/*      */ 
/*      */     
/*   97 */     byte[] cryptedBuffer = new byte[32];
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  102 */     String port = null;
/*  103 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Task_for_communication_with_the_CSD/SMS_Receiver_started"), Enums.EnumMessagePriority.LOW, null, null);
/*      */     
/*  105 */     label389: while (this.flag) {
/*  106 */       port = this.cfgReceiverCSD.getSerialPort();
/*  107 */       this.receiverCSDCommPort = SerialPortFunctions.openSPL(getClass(), port, 57600, 8, 1, 0);
/*  108 */       if (this.receiverCSDCommPort == null) {
/*  109 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("[CSD]_Cant_open_serial_port") + " " + port, Enums.EnumMessagePriority.HIGH, null, null);
/*  110 */         GlobalVariables.buzzerActivated = true;
/*  111 */         this.online = false;
/*  112 */         UILogInitiator.toggleImageById((short)3, false, port);
/*  113 */         this.wdt = Functions.updateWatchdog(this.wdt, 15000L); continue;
/*      */       } 
/*      */       try {
/*  116 */         this.receiverCSDCommPort.setDTR(true);
/*  117 */         this.receiverCSDCommPort.setRTS(true);
/*  118 */         if (this.sos == null) {
/*  119 */           this.sos = new SerOutputStream((SerialPort)this.receiverCSDCommPort);
/*      */         }
/*  121 */         if (this.sis == null) {
/*  122 */           this.sis = new SerInputStream((SerialPort)this.receiverCSDCommPort);
/*      */         }
/*  124 */         UILogInitiator.toggleImageById((short)3, true, port);
/*      */         break label389;
/*  126 */       } catch (IOException ex) {
/*  127 */         Logger.getLogger(CommReceiverCSD.class.getName()).log(Level.SEVERE, (String)null, ex);
/*      */       } 
/*      */     } 
/*      */     
/*      */     try {
/*  132 */       while (this.flag) {
/*      */         try {
/*  134 */           if (nextCheckModemInitialized < System.currentTimeMillis()) {
/*  135 */             byte nStepInitModem = 0;
/*  136 */             byte nErrosInitModem = 0;
/*      */             while (true) {
/*      */               String strRcv, oldBand;
/*  139 */               switch (nStepInitModem) {
/*      */                 case 0:
/*  141 */                   strRcv = sendATCommand("AT", (String)null);
/*  142 */                   if (strRcv.contains("OK")) {
/*  143 */                     nStepInitModem = (byte)(nStepInitModem + 1); break;
/*      */                   } 
/*  145 */                   if (nErrosInitModem > 0) {
/*  146 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("[CSD]_Cant_communicate_with_serial_port") + " " + port, Enums.EnumMessagePriority.HIGH, null, null);
/*  147 */                     UILogInitiator.toggleImageById((short)3, false, port);
/*      */                   } 
/*  149 */                   nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  150 */                   hangupModem((String)null, Boolean.valueOf(false));
/*      */                   break;
/*      */                 
/*      */                 case 1:
/*  154 */                   strRcv = sendATCommand("ATI", (String)null);
/*  155 */                   if (strRcv.contains("OK")) {
/*  156 */                     if (strRcv.contains("SIM300")) {
/*  157 */                       this.csdReceiverDetectedModel = (byte)Enums.EnumCSDReceiverModel.SIM340.getModel();
/*  158 */                       nStepInitModem = (byte)(nStepInitModem + 1); break;
/*  159 */                     }  if (strRcv.contains("SIM900")) {
/*  160 */                       this.csdReceiverDetectedModel = (byte)Enums.EnumCSDReceiverModel.SIM900.getModel();
/*  161 */                       nStepInitModem = (byte)(nStepInitModem + 1); break;
/*  162 */                     }  if (strRcv.contains("MC35")) {
/*  163 */                       this.csdReceiverDetectedModel = (byte)Enums.EnumCSDReceiverModel.MC35.getModel();
/*  164 */                       nStepInitModem = (byte)(nStepInitModem + 1); break;
/*      */                     } 
/*  166 */                     nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                     break;
/*      */                   } 
/*  169 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_reading_the_model_of_the_CSD/SMS_Receiver_connected_to_the_serial_port_[") + port + " ] (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  170 */                   nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  171 */                   nStepInitModem = 0;
/*      */                   break;
/*      */                 
/*      */                 case 2:
/*  175 */                   strRcv = sendATCommand("AT+CPIN?", (String)null);
/*  176 */                   if (strRcv.contains("ERROR")) {
/*  177 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_requesting_the_state_of_the_PIN_of_the_SIM-CARD_inserted_in_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port, Enums.EnumMessagePriority.HIGH, null, null);
/*  178 */                     this.online = false;
/*  179 */                     UILogInitiator.toggleImageById((short)3, false, port);
/*  180 */                     String modemReboot = sendATCommand("AT+CFUN=0", (String)null);
/*  181 */                     if (modemReboot.contains("OK")) {
/*  182 */                       modemReboot = sendATCommand("AT+CFUN=1", (String)null);
/*  183 */                       if (modemReboot.contains("OK")) {
/*  184 */                         waitModemResponse("Call Ready", (Integer)null);
/*      */                       }
/*      */                     } 
/*  187 */                     nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  188 */                     nStepInitModem = 0; break;
/*  189 */                   }  if (!strRcv.contains("+CPIN: READY")) {
/*  190 */                     if (strRcv.contains("+CPIN: SIM PIN")) {
/*  191 */                       strRcv = sendATCommand("AT+CPIN=" + this.cfgReceiverCSD.getPin(), (String)null);
/*  192 */                       if (strRcv.contains("OK")) {
/*  193 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("PIN_code_sent_successfully_to_the_SIM-CARD_inserted_in_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port, Enums.EnumMessagePriority.LOW, null, null);
/*  194 */                         nStepInitModem = (byte)(nStepInitModem + 1); break;
/*      */                       } 
/*  196 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_sending_the_PIN_code_to_the_SIM-CARD_inserted_in_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  197 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Please_fix_this_problem_and_restart_the_Zeus_Server"), Enums.EnumMessagePriority.HIGH, null, null);
/*  198 */                       nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  199 */                       nStepInitModem = 0; break;
/*      */                     } 
/*  201 */                     if (strRcv.contains("PUK")) {
/*  202 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("For_the_unlocking_of_the_SIM-CARD_inserted_in_the_CSD/SMS_Receiver_connected_to_the_serial_port_[") + port + LocaleMessage.getLocaleMessage("]_the_input_of_the_PUK_code_is_necessary"), Enums.EnumMessagePriority.HIGH, null, null);
/*  203 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Please_fix_this_problem_and_restart_the_Zeus_Server"), Enums.EnumMessagePriority.HIGH, null, null);
/*  204 */                       nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  205 */                       nStepInitModem = 0; break;
/*      */                     } 
/*  207 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("SIM-CARD_is_not_inserted_in_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port, Enums.EnumMessagePriority.HIGH, null, null);
/*  208 */                     nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  209 */                     nStepInitModem = 0;
/*      */                     break;
/*      */                   } 
/*  212 */                   nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                   break;
/*      */                 
/*      */                 case 3:
/*  216 */                   oldBand = sendATCommand("AT+CBAND?", (String)null);
/*  217 */                   if (this.csdReceiverDetectedModel == Enums.EnumCSDReceiverModel.SIM340.getModel() || this.csdReceiverDetectedModel == Enums.EnumCSDReceiverModel.SIM900.getModel()) {
/*  218 */                     String sBand = "";
/*  219 */                     if (Enums.EnumGsmBand.PGSM_900_MHZ.getBand() == this.cfgReceiverCSD.getGsmBand().getBand()) {
/*  220 */                       sBand = "PGSM_MODE";
/*  221 */                     } else if (Enums.EnumGsmBand.DCS_1800_MHZ.getBand() == this.cfgReceiverCSD.getGsmBand().getBand()) {
/*  222 */                       sBand = "DCS_MODE";
/*  223 */                     } else if (Enums.EnumGsmBand.PCS_1900_MHZ.getBand() == this.cfgReceiverCSD.getGsmBand().getBand()) {
/*  224 */                       sBand = "PCS_MODE";
/*  225 */                     } else if (Enums.EnumGsmBand.EGSM_DCS_900_1800_MHZ.getBand() == this.cfgReceiverCSD.getGsmBand().getBand()) {
/*  226 */                       sBand = "EGSM_DCS_MODE";
/*  227 */                     } else if (Enums.EnumGsmBand.GSM_PCS_850_1900_MHZ.getBand() == this.cfgReceiverCSD.getGsmBand().getBand()) {
/*  228 */                       sBand = "GSM850_PCS_MODE";
/*      */                     } 
/*  230 */                     if (oldBand != null && (oldBand.contains("ALL_BAND") || oldBand.contains(sBand))) {
/*  231 */                       nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                       break;
/*      */                     } 
/*  234 */                     strRcv = sendATCommand("AT+CBAND=\"" + sBand + "\"", (String)null);
/*      */                     
/*  236 */                     if (!strRcv.contains("OK")) {
/*  237 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_configuring_the_operation_band_of_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  238 */                       nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  239 */                       nStepInitModem = 0; break;
/*      */                     } 
/*  241 */                     nStepInitModem = (byte)(nStepInitModem + 1);
/*  242 */                     int lo = 0;
/*  243 */                     while (lo++ < 3) {
/*  244 */                       strRcv = sendATCommand("AT+CREG?", (String)null);
/*  245 */                       if (strRcv.contains("0,1") || strRcv.contains("0,5")) {
/*      */                         break;
/*      */                       }
/*  248 */                       Thread.sleep(8000L);
/*      */                       
/*  250 */                       if (lo == 3) {
/*  251 */                         nStepInitModem = 0;
/*      */                       }
/*      */                     } 
/*      */                     break;
/*      */                   } 
/*  256 */                   nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                   break;
/*      */                 
/*      */                 case 4:
/*  260 */                   strRcv = sendATCommand("AT+CLIP=1", (String)null);
/*  261 */                   if (this.csdReceiverDetectedModel != 0 && !strRcv.contains("OK")) {
/*  262 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_enabling_the_caller-ID_of_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  263 */                     nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  264 */                     nStepInitModem = 0; break;
/*      */                   } 
/*  266 */                   nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                   break;
/*      */                 
/*      */                 case 5:
/*  270 */                   strRcv = sendATCommand("AT+CSNS=4", (String)null);
/*  271 */                   if (!strRcv.contains("OK") && this.csdReceiverDetectedModel != 0) {
/*  272 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_enabling_the_reception_of_data_calls_through_the_CSD/SMS_Receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  273 */                     nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  274 */                     nStepInitModem = 0; break;
/*      */                   } 
/*  276 */                   nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                   break;
/*      */                 
/*      */                 case 6:
/*  280 */                   strRcv = sendATCommand("AT+CMGF=1", (String)null);
/*  281 */                   if (this.csdReceiverDetectedModel != 0 && !strRcv.contains("OK")) {
/*  282 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_selecting_the_TEXT_format_for_the_SMS_messages_of_the_CSD/SMS_receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  283 */                     nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  284 */                     nStepInitModem = 0; break;
/*      */                   } 
/*  286 */                   nStepInitModem = (byte)(nStepInitModem + 1);
/*      */                   break;
/*      */                 
/*      */                 case 7:
/*  290 */                   strRcv = sendATCommand("AT+CSQ", (String)null);
/*  291 */                   if (strRcv.contains("OK")) {
/*  292 */                     int i = strRcv.indexOf("+CSQ: ");
/*  293 */                     if (i >= 0) {
/*  294 */                       i += 6;
/*  295 */                       int j = strRcv.indexOf(",", i);
/*  296 */                       if (j > 0) {
/*  297 */                         String tmp = strRcv.substring(i, j);
/*  298 */                         if (Functions.isInteger(tmp)) {
/*  299 */                           this.csdReceiverSignalLevel = Integer.parseInt(tmp);
/*  300 */                           if (this.csdReceiverDetectedModel == 99) {
/*  301 */                             this.csdReceiverDetectedModel = 0;
/*      */                           }
/*  303 */                           UILogInitiator.setSignalImageByLevel(this.csdReceiverSignalLevel, port);
/*  304 */                           UILogInitiator.toggleImageById((short)3, true, port);
/*  305 */                           if (this.csdReceiverSignalLevel < ZeusServerCfg.getInstance().getMinimumReceivingSignalLevel().intValue()) {
/*  306 */                             Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("The_signal_level_of_the_CSD/SMS_receiver_connected_to_the_serial_port_[") + port + LocaleMessage.getLocaleMessage("]_is_very_low"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  307 */                             GlobalVariables.buzzerActivated = true;
/*      */                           } 
/*  309 */                           this.online = true;
/*      */                           break;
/*      */                         } 
/*      */                       } 
/*      */                     } 
/*      */                   } 
/*  315 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_reading_the_signal_level_of_the_CSD/SMS_receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  316 */                   nErrosInitModem = (byte)(nErrosInitModem + 1);
/*  317 */                   nStepInitModem = 0;
/*      */                   break;
/*      */               } 
/*  320 */               if (nErrosInitModem >= 3) {
/*  321 */                 nErrosInitModem = 0;
/*  322 */                 GlobalVariables.buzzerActivated = true;
/*  323 */                 this.online = false;
/*  324 */                 this.wdt = Functions.updateWatchdog(this.wdt, 60000L); continue;
/*      */               } 
/*  326 */               this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*      */             } 
/*      */             
/*  329 */             nextCheckModemInitialized = System.currentTimeMillis() + 60000L;
/*  330 */             this.sis.skip(this.sis.available());
/*      */           } 
/*  332 */           if (this.sis.available() > 0) {
/*  333 */             String strRcv = waitModemResponse("+CLIP: ", Integer.valueOf(500));
/*  334 */             int i = strRcv.indexOf("RING\r\n\r\n\r\n+CLIP: ");
/*  335 */             if (i < 0) {
/*  336 */               i = strRcv.indexOf("RING\r\n\r\n+CLIP: ");
/*      */             }
/*  338 */             if (i >= 0) {
/*  339 */               i = strRcv.indexOf("\"", i);
/*  340 */               if (i > 0) {
/*  341 */                 i++;
/*  342 */                 int j = strRcv.indexOf("\"", i);
/*  343 */                 if (j > 0) {
/*  344 */                   this.telephoneNumberIdentified = removeInitialSignalPlusAndZeroDigit(strRcv.substring(i, i + j - i));
/*  345 */                   this.telephoneNumberIdentified = (this.telephoneNumberIdentified != null) ? this.telephoneNumberIdentified.trim() : "";
/*  346 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Received_call_(") + this.telephoneNumberIdentified + ")", Enums.EnumMessagePriority.LOW, null, null);
/*  347 */                   if (this.telephoneNumberIdentified.length() >= 7) {
/*  348 */                     boolean clientEnabled = false;
/*  349 */                     boolean clientRegistration = false;
/*  350 */                     int productId = ZeusSettingsDBManager.getProductIdByPhoneNumber(this.telephoneNumberIdentified);
/*  351 */                     if (productId > 0) {
/*  352 */                       this.sp29DH = GenericDBManager.executeSP_029(this.telephoneNumberIdentified, productId);
/*      */                     }
/*  354 */                     if (this.sp29DH != null) {
/*  355 */                       clientRegistration = (this.sp29DH.getRegistered() == 1);
/*  356 */                       if (clientRegistration) {
/*  357 */                         clientEnabled = (this.sp29DH.getEnabled() == 1);
/*  358 */                         this.infoModule = new InfoModule();
/*  359 */                         this.infoModule.clientName = this.sp29DH.getName();
/*  360 */                         this.infoModule.idClient = this.sp29DH.getId_Client();
/*  361 */                         this.infoModule.idModule = this.sp29DH.getId_Module();
/*  362 */                         this.infoModule.idGroup = this.sp29DH.getId_Group();
/*  363 */                         this.infoModule.sn = this.sp29DH.getSn();
/*  364 */                         this.infoModule.current_csd_sms_sim = this.sp29DH.getCurrentSIM();
/*      */                       } else {
/*  366 */                         clientEnabled = false;
/*      */                       } 
/*      */                     } 
/*  369 */                     if (clientEnabled || (!clientRegistration && ZeusServerCfg.getInstance().getAutoRegistration())) {
/*  370 */                       strRcv = sendATCommand("ATA", "OK;ERROR;CONNECT;NO CARRIER;");
/*  371 */                       if (strRcv.contains("OK") || strRcv.contains("NO CARRIER")) {
/*  372 */                         if (clientRegistration) {
/*  373 */                           processAlivePacketReceivedByVoice(productId);
/*      */                         } else {
/*  375 */                           Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_The_reception_of_ALIVE_packets_via_voice_channel_is_not_allowed_when_coming_from_non-registrated_modules"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                         } 
/*  377 */                       } else if (strRcv.contains("CONNECT")) {
/*  378 */                         this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  379 */                         label400: while (this.receiverCSDCommPort.sigCD() && this.idleTimeout > System.currentTimeMillis()) {
/*  380 */                           if (this.sis.available() < 2) {
/*      */                             continue;
/*      */                           }
/*  383 */                           byte[] prod = SerialPortFunctions.readSPL(this.sis, 0, 2, 5000);
/*  384 */                           if (prod == null) {
/*      */                             continue;
/*      */                           }
/*  387 */                           prod = Functions.swapLSB2MSB(prod);
/*  388 */                           String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/*  389 */                           short enc = Short.parseShort(prodBin.substring(0, 2), 2);
/*  390 */                           prodBin = prodBin.substring(2);
/*  391 */                           prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/*  392 */                           short prodI = Short.parseShort(prodBin, 2);
/*  393 */                           if (prodI == Util.EnumProductIDs.PEGASUS.getProductId()) {
/*      */                             
/*  395 */                             while (this.receiverCSDCommPort.sigCD() && this.idleTimeout > System.currentTimeMillis()) {
/*  396 */                               if (this.sis.available() < 2) {
/*      */                                 continue;
/*      */                               }
/*  399 */                               byte[] length = Functions.swapLSB2MSB(SerialPortFunctions.readSPL(this.sis, 0, 2, 5000));
/*      */                               
/*  401 */                               int msgLen = Functions.getIntFrom2ByteArray(length);
/*      */ 
/*      */                               
/*  404 */                               while (this.receiverCSDCommPort.sigCD() && this.idleTimeout > System.currentTimeMillis()) {
/*  405 */                                 if (this.sis.available() < msgLen) {
/*      */                                   continue;
/*      */                                 }
/*  408 */                                 byte[] data = SerialPortFunctions.readSPL(this.sis, 0, msgLen, 5000);
/*  409 */                                 StringBuilder sb = new StringBuilder();
/*  410 */                                 for (int ii = 0; ii < data.length; ii++) {
/*  411 */                                   sb.append(data[ii] & 0xFF).append(" ");
/*      */                                 }
/*  413 */                                 byte[] cbits = new byte[msgLen + 2];
/*  414 */                                 cbits[0] = prod[1];
/*  415 */                                 cbits[1] = prod[0];
/*  416 */                                 cbits[2] = length[1];
/*  417 */                                 cbits[3] = length[0];
/*      */                                 
/*  419 */                                 System.arraycopy(data, 0, cbits, 4, msgLen - 2);
/*  420 */                                 int crcCalc = CRC16.calculate(cbits, 0, msgLen + 2, 65535);
/*  421 */                                 byte[] crcbits = new byte[2];
/*  422 */                                 crcbits[0] = data[msgLen - 2];
/*  423 */                                 crcbits[1] = data[msgLen - 1];
/*  424 */                                 crcbits = Functions.swapLSB2MSB(crcbits);
/*  425 */                                 int crcRecv = Functions.getIntFrom2ByteArray(crcbits);
/*  426 */                                 if (crcCalc == crcRecv) {
/*      */                                   
/*  428 */                                   byte[] encData = new byte[msgLen - 2];
/*  429 */                                   System.arraycopy(data, 0, encData, 0, msgLen - 2);
/*  430 */                                   byte[] decData = new byte[msgLen - 2];
/*  431 */                                   byte[] decBlock = null;
/*      */                                   try {
/*  433 */                                     if (encData.length >= 16) { if (encData.length % 16 == 0) {
/*  434 */                                         for (int k = 0; k < encData.length; )
/*  435 */                                         { byte[] block = new byte[16];
/*  436 */                                           System.arraycopy(encData, k, block, 0, 16);
/*  437 */                                           if (enc == 1) {
/*  438 */                                             decBlock = Rijndael.decryptBytes(block, Rijndael.dataKeyBytes, false);
/*  439 */                                           } else if (enc == 2) {
/*  440 */                                             decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */                                           } 
/*  442 */                                           System.arraycopy(decBlock, 0, decData, k, 16);
/*  443 */                                           k += 16; }  continue;
/*      */                                       }  continue; }
/*      */                                      continue;
/*  446 */                                   } catch (InvalidAlgorithmParameterException|java.security.InvalidKeyException|java.security.NoSuchAlgorithmException|javax.crypto.BadPaddingException|javax.crypto.IllegalBlockSizeException|javax.crypto.NoSuchPaddingException ex) {
/*  447 */                                     Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Error_while_decrypting_data_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, null);
/*  448 */                                     sendByte((short)21);
/*      */                                     
/*  450 */                                     if (!processM2SPacketReceivedByCSD(decData, enc))
/*      */                                       continue;  break;
/*      */                                   } 
/*      */                                 } 
/*  454 */                                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Error_on_the_CRC_of_the_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, null);
/*  455 */                                 sendByte((short)21);
/*      */ 
/*      */                                 
/*      */                                 continue label400;
/*      */                               } 
/*      */                             } 
/*  461 */                           } else if ((prod[1] & 0x7) == PegasusRoutines.EnumTipoPacote.IDENTIFICATION_PACKET.getPacket() || (prod[1] & 0x7) == PegasusRoutines.EnumTipoPacote.EXTENDED_ALIVE_PACKET.getPacket() || (prod[1] & 0x7) == PegasusRoutines.EnumTipoPacote.EVENT_PACKET.getPacket()) {
/*      */                             
/*  463 */                             while (this.receiverCSDCommPort.sigCD() && this.idleTimeout > System.currentTimeMillis()) {
/*  464 */                               if (this.sis.available() >= 15) {
/*  465 */                                 this.tmpSerialBuffer = SerialPortFunctions.readSPL(this.sis, 0, 15, 5000);
/*  466 */                                 this.serialInputBuffer = new byte[17];
/*  467 */                                 this.serialInputBuffer[0] = prod[1];
/*  468 */                                 this.serialInputBuffer[1] = prod[0];
/*  469 */                                 System.arraycopy(this.tmpSerialBuffer, 0, this.serialInputBuffer, 2, 15);
/*  470 */                                 StringBuilder sb = new StringBuilder();
/*  471 */                                 for (int ii = 0; ii < this.serialInputBuffer.length; ii++) {
/*  472 */                                   sb.append(this.serialInputBuffer[ii] & 0xFF).append(" ");
/*      */                                 }
/*  474 */                                 System.arraycopy(this.serialInputBuffer, 1, cryptedBuffer, 0, 16);
/*  475 */                                 byte[] decrytedBuffer = Rijndael.decryptBytes(cryptedBuffer, Rijndael.dataKeyBytes, false);
/*  476 */                                 if (decrytedBuffer.length >= 16) {
/*  477 */                                   System.arraycopy(decrytedBuffer, 0, this.serialInputBuffer, 1, 16);
/*  478 */                                   int receivedCRC = this.serialInputBuffer[16] & 0xFF;
/*  479 */                                   receivedCRC = receivedCRC * 256 + (this.serialInputBuffer[15] & 0xFF);
/*  480 */                                   int crcCalc = CRC16.calculate(this.serialInputBuffer, 0, 15, 65535);
/*  481 */                                   if (receivedCRC == crcCalc) {
/*  482 */                                     if (this.infoModule == null && (this.serialInputBuffer[0] & 0x7) != PegasusRoutines.EnumTipoPacote.IDENTIFICATION_PACKET.getPacket()) {
/*  483 */                                       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Module_tried_to_send_a_packet_via_CSD_before_being_identified"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  484 */                                       sendByte((short)21);
/*      */                                       break;
/*      */                                     } 
/*  487 */                                     if (((this.serialInputBuffer[0] & 0x7) == PegasusRoutines.EnumTipoPacote.IDENTIFICATION_PACKET.getPacket()) ? 
/*  488 */                                       !processIdentificationPacketReceivedByCSD() : (
/*      */ 
/*      */                                       
/*  491 */                                       ((this.serialInputBuffer[0] & 0x7) == PegasusRoutines.EnumTipoPacote.EXTENDED_ALIVE_PACKET.getPacket()) ? 
/*  492 */                                       !processAlivePacketReceivedByCSD() : ((
/*      */ 
/*      */                                       
/*  495 */                                       this.serialInputBuffer[0] & 0x7) != PegasusRoutines.EnumTipoPacote.EVENT_PACKET.getPacket() || 
/*  496 */                                       !processEventPacketReceivedByCSD())));
/*      */ 
/*      */ 
/*      */                                     
/*      */                                     break;
/*      */                                   } 
/*      */ 
/*      */                                   
/*  504 */                                   Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Error_on_the_CRC_of_the_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, null);
/*  505 */                                   sendByte((short)21);
/*      */                                   break;
/*      */                                 } 
/*  508 */                                 Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Error_while_decrypting_data_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, null);
/*  509 */                                 sendByte((short)21);
/*      */                                 
/*      */                                 break;
/*      */                               } 
/*      */                             } 
/*      */                           } 
/*      */                           
/*  516 */                           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*      */                         } 
/*      */                       } 
/*      */                     } else {
/*  520 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_Telephone_call_from_a_module_not_registered_or_disabled_(") + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  521 */                       GlobalVariables.buzzerActivated = true;
/*      */                     } 
/*      */                   } else {
/*  524 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATENTTION_It_was_not_possible_to_receive_this_call_because_the_telephone_number_identified_(no.") + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage(")_has_less_than") + '\007' + LocaleMessage.getLocaleMessage("digits"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                   } 
/*  526 */                   hangupModem(this.telephoneNumberIdentified, (Boolean)null);
/*  527 */                   this.infoModule = null;
/*      */                 }
/*      */               
/*      */               } 
/*      */             } 
/*  532 */           } else if (nextCheckNewSmsMessages < System.currentTimeMillis()) {
/*  533 */             String strRcv = sendATCommand("AT+CMGL=\"ALL\"", (String)null);
/*  534 */             if (strRcv.contains("OK")) {
/*  535 */               Hashtable<Integer, InfoSms> tblSMS = new Hashtable<>();
/*  536 */               int k = strRcv.indexOf("+CMGL: ");
/*  537 */               while (k >= 0) {
/*  538 */                 int i = k + 7;
/*  539 */                 int j = strRcv.indexOf(",", i);
/*  540 */                 if (j > 0) {
/*  541 */                   String temp = strRcv.substring(i, j);
/*  542 */                   if (Functions.isInteger(temp)) {
/*  543 */                     int smsId = Integer.parseInt(temp);
/*  544 */                     i = j + 1;
/*  545 */                     j = strRcv.indexOf(",", i);
/*  546 */                     if (j > 0) {
/*  547 */                       String smsStatus = strRcv.substring(i, j).replaceAll("\"", "");
/*  548 */                       i = j + 1;
/*  549 */                       j = strRcv.indexOf(",", i);
/*  550 */                       if (j > 0) {
/*  551 */                         String smsSender = strRcv.substring(i, j).replaceAll("\"", "");
/*  552 */                         i = j + 1;
/*  553 */                         j = strRcv.indexOf(",", i);
/*  554 */                         if (j > 0) {
/*  555 */                           String smsSenderName = strRcv.substring(i, j).replaceAll("\"", "");
/*  556 */                           i = j + 1;
/*  557 */                           j = strRcv.indexOf("\r\n", i);
/*  558 */                           if (j > 0) {
/*  559 */                             String sTimeStamp = strRcv.substring(i, j).replaceAll("\"", "");
/*  560 */                             if (sTimeStamp.indexOf("\\+") > 0) {
/*  561 */                               sTimeStamp = sTimeStamp.replaceAll("\\+", "");
/*  562 */                             } else if (sTimeStamp.indexOf("-") > 0) {
/*  563 */                               sTimeStamp = sTimeStamp.replaceAll("-", "");
/*      */                             } 
/*  565 */                             Date smsTimeStamp = Functions.format2DateTime(sTimeStamp);
/*  566 */                             i = j + 2;
/*  567 */                             j = strRcv.indexOf("\r\n", i);
/*  568 */                             if (j > 0) {
/*  569 */                               tblSMS.put(Integer.valueOf(smsId), new InfoSms(smsId, smsStatus, smsSender, smsSenderName, smsTimeStamp, strRcv.substring(i, j)));
/*  570 */                               ZeusServerLogger.logSMSAll(tblSMS.get(Integer.valueOf(smsId)));
/*      */                             } 
/*      */                           } 
/*      */                         } 
/*      */                       } 
/*      */                     } 
/*      */                   } 
/*      */                 } 
/*  578 */                 k = strRcv.indexOf("+CMGL: ", k + 7);
/*      */               } 
/*  580 */               while (tblSMS.size() > 0) {
/*      */                 
/*  582 */                 InfoSms iSMS = tblSMS.get(Functions.getOldestInfoSMSByDate(tblSMS));
/*  583 */                 this.telephoneNumberIdentified = removeInitialSignalPlusAndZeroDigit(iSMS.getsender()).trim();
/*  584 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("SMS_received_(") + this.telephoneNumberIdentified + ")", Enums.EnumMessagePriority.LOW, null, null);
/*  585 */                 if (this.telephoneNumberIdentified.length() >= 7) {
/*  586 */                   int productId = ZeusSettingsDBManager.getProductIdByPhoneNumber(this.telephoneNumberIdentified);
/*  587 */                   SP_062DataHolder sp62DH = null;
/*  588 */                   if (productId > 0) {
/*  589 */                     sp62DH = GenericDBManager.executeSP_062(this.telephoneNumberIdentified, productId);
/*      */                   } else {
/*  591 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_A_SMS_message_coming_from_a_telephone_number_not_enabled_(") + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  592 */                     GlobalVariables.buzzerActivated = true;
/*      */                   } 
/*  594 */                   if (sp62DH != null) {
/*  595 */                     if (sp62DH.getRegistered() == 1) {
/*  596 */                       if (sp62DH.getEnabled() == 1) {
/*  597 */                         this.infoModule = new InfoModule();
/*  598 */                         this.infoModule.clientName = sp62DH.getName();
/*  599 */                         this.infoModule.idClient = sp62DH.getId_Client();
/*  600 */                         this.infoModule.idModule = sp62DH.getId_Module();
/*  601 */                         this.infoModule.idGroup = sp62DH.getId_Group();
/*  602 */                         this.infoModule.sn = sp62DH.getSn();
/*  603 */                         this.infoModule.current_csd_sms_sim = sp62DH.getCurrentSIM();
/*      */                       } else {
/*  605 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_A_SMS_message_coming_from_a_telephone_number_not_enabled_(") + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  606 */                         GlobalVariables.buzzerActivated = true;
/*      */                       } 
/*      */                     } else {
/*  609 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("SMS_received_from_a_non_registred_telephone_number_(") + this.telephoneNumberIdentified + ").", Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                     } 
/*      */                   }
/*  612 */                   if (this.infoModule != null) {
/*  613 */                     if (isPegasusLegacySMS(iSMS.getcontent())) {
/*  614 */                       if (isSmsContentValid(iSMS.getcontent())) {
/*  615 */                         int j = iSMS.getcontent().indexOf(";");
/*  616 */                         if (j > 0) {
/*  617 */                           String tmp = iSMS.getcontent().substring(0, j);
/*  618 */                           if (Functions.isInteger(tmp)) {
/*  619 */                             int tmpPacket = Integer.parseInt(tmp);
/*  620 */                             if (tmpPacket == PegasusRoutines.EnumTipoPacote.EXTENDED_ALIVE_PACKET.getPacket()) {
/*  621 */                               processAlivePacketReceivedBySMS(iSMS.getcontent().substring(j + 1));
/*  622 */                             } else if (tmpPacket == PegasusRoutines.EnumTipoPacote.EVENT_PACKET.getPacket()) {
/*  623 */                               while (j > 0) {
/*  624 */                                 int i = j + 1;
/*  625 */                                 j = iSMS.getcontent().indexOf(";", i);
/*  626 */                                 if (j > 0) {
/*  627 */                                   processEventReceivedBySMS(iSMS.getcontent().substring(i, j)); continue;
/*      */                                 } 
/*  629 */                                 processEventReceivedBySMS(iSMS.getcontent().substring(i));
/*      */                               } 
/*      */                             } else {
/*      */                               
/*  633 */                               Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_A_SMS_message_with_invalid_content_(") + iSMS.getcontent() + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                             } 
/*      */                           } else {
/*  636 */                             Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_A_SMS_message_with_invalid_content_(") + iSMS.getcontent() + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                           } 
/*      */                         } else {
/*  639 */                           Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_A_SMS_message_with_invalid_content_(") + iSMS.getcontent() + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                         } 
/*      */                       } else {
/*  642 */                         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_A_SMS_message_with_invalid_content_(") + iSMS.getcontent() + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                       }
/*      */                     
/*  645 */                     } else if (isSmsContentValid(iSMS.getcontent())) {
/*  646 */                       processM2SPacketReceivedBySMS(iSMS.getcontent(), this.infoModule);
/*      */                     } else {
/*  648 */                       Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_A_SMS_message_with_invalid_content_(") + iSMS.getcontent() + LocaleMessage.getLocaleMessage(")_was_received"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */                     } 
/*      */                     
/*  651 */                     this.infoModule = null;
/*      */                   } 
/*      */                 } else {
/*  654 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("ATTENTION_It_was_not_possible_to_receive_this_SMS_message_because_the_telephone_number_identified_(") + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage(")_has_less_than") + '\007' + LocaleMessage.getLocaleMessage("digits"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  655 */                   ZeusServerLogger.logSMSBAD(iSMS);
/*      */                 } 
/*  657 */                 strRcv = sendATCommand("AT+CMGD=" + iSMS.getid(), (String)null);
/*  658 */                 if (!strRcv.contains("OK")) {
/*  659 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_deleting_SMS_message_no.") + iSMS.getid() + LocaleMessage.getLocaleMessage("of_the_CSD/SMS_receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*      */                 }
/*  661 */                 tblSMS.remove(Integer.valueOf(iSMS.getid()));
/*  662 */                 this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*      */               } 
/*      */             } else {
/*  665 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Error_while_reading_SMS_messages_from_CSD/SMS_receiver_connected_to_the_serial_port") + port + " (" + strRcv + ")", Enums.EnumMessagePriority.HIGH, null, null);
/*  666 */               UILogInitiator.toggleImageById((short)3, false, port);
/*  667 */               GlobalVariables.buzzerActivated = true;
/*  668 */               String modemReboot = sendATCommand("AT+CFUN=0", (String)null);
/*  669 */               if (modemReboot.contains("OK")) {
/*  670 */                 modemReboot = sendATCommand("AT+CFUN=1", (String)null);
/*  671 */                 if (modemReboot.contains("OK")) {
/*  672 */                   waitModemResponse("Call Ready", (Integer)null);
/*      */                 }
/*      */               } 
/*  675 */               nextCheckModemInitialized = 0L;
/*      */             } 
/*  677 */             nextCheckNewSmsMessages = System.currentTimeMillis() + 10000L;
/*      */           } 
/*      */           
/*  680 */           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*  681 */         } catch (Exception ex) {
/*  682 */           Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_communication_task_with_the_CSD/SMS_receiver_connected_to_the_serial_port") + port, Enums.EnumMessagePriority.HIGH, null, ex);
/*  683 */           this.online = false;
/*  684 */           UILogInitiator.toggleImageById((short)3, false, port);
/*  685 */           GlobalVariables.buzzerActivated = true;
/*  686 */           this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*      */           break;
/*      */         } 
/*      */       } 
/*  690 */     } catch (Exception ex) {
/*  691 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_in_the_communication_task_with_the_CSD/SMS_receiver_connected_to_the_serial_port") + port, Enums.EnumMessagePriority.HIGH, null, ex);
/*  692 */       this.online = false;
/*  693 */       UILogInitiator.toggleImageById((short)3, false, port);
/*  694 */       GlobalVariables.buzzerActivated = true;
/*      */     } finally {
/*  696 */       dispose();
/*      */     } 
/*      */   }
/*      */   
/*      */   private void dispose() {
/*  701 */     if (this.sis != null) {
/*      */       try {
/*  703 */         this.sis.close();
/*      */       }
/*  705 */       catch (IOException iOException) {}
/*      */     }
/*      */     
/*  708 */     this.sis = null;
/*  709 */     if (this.sos != null) {
/*      */       try {
/*  711 */         this.sos.close();
/*  712 */       } catch (IOException iOException) {}
/*      */     }
/*      */     
/*  715 */     this.sos = null;
/*  716 */     if (this.receiverCSDCommPort != null) {
/*      */       try {
/*  718 */         this.receiverCSDCommPort.close();
/*  719 */       } catch (IOException iOException) {}
/*      */     }
/*      */     
/*  722 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Communication_task_with_CSD/SMS_receiver_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*      */   }
/*      */   
/*      */   private void hangupModem(String telephoneNumberIdentified, Boolean activateBuzzer) throws InterruptedException, IOException {
/*  726 */     int retries = 0;
/*  727 */     telephoneNumberIdentified = (telephoneNumberIdentified == null) ? "" : telephoneNumberIdentified;
/*  728 */     activateBuzzer = Boolean.valueOf((activateBuzzer == null) ? true : activateBuzzer.booleanValue());
/*  729 */     while (retries < 3) {
/*  730 */       this.receiverCSDCommPort.setDTR(false);
/*  731 */       this.receiverCSDCommPort.setRTS(false);
/*  732 */       Thread.sleep(250L);
/*  733 */       this.receiverCSDCommPort.setDTR(true);
/*  734 */       this.receiverCSDCommPort.setRTS(true);
/*  735 */       Thread.sleep(250L);
/*  736 */       String strRcv = sendATCommand("ATH", "OK;ERROR;NO CARRIER;");
/*  737 */       if (strRcv.contains("OK") || strRcv.contains("ERROR") || strRcv.contains("NO CARRIER")) {
/*  738 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Call_ended_(") + telephoneNumberIdentified + ")", Enums.EnumMessagePriority.LOW, null, null); break;
/*      */       } 
/*  740 */       if (activateBuzzer.booleanValue()) {
/*  741 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_end_call_(") + telephoneNumberIdentified + "): " + strRcv, Enums.EnumMessagePriority.AVERAGE, null, null);
/*  742 */         GlobalVariables.buzzerActivated = true;
/*      */       } 
/*  744 */       retries++;
/*  745 */       this.wdt = Functions.updateWatchdog(this.wdt, 100L);
/*      */     } 
/*      */   }
/*      */   
/*      */   private String waitModemResponse(String resp2Wait, Integer time2Wait) throws IOException {
/*  750 */     time2Wait = Integer.valueOf((time2Wait == null) ? 30000 : time2Wait.intValue());
/*  751 */     String strRcv = "";
/*  752 */     ArrayList<String> response2Wait = new ArrayList<>();
/*  753 */     int numResponses2Wait = 0;
/*  754 */     String[] res = resp2Wait.split(";");
/*  755 */     response2Wait.addAll(Arrays.asList(res));
/*      */     
/*  757 */     long timeout = System.currentTimeMillis() + time2Wait.intValue();
/*      */     
/*  759 */     label22: while (timeout > System.currentTimeMillis()) {
/*  760 */       if (this.sis.available() > 0) {
/*  761 */         strRcv = strRcv + new String(SerialPortFunctions.readSPL(this.sis, 0, this.sis.available(), time2Wait.intValue()));
/*  762 */         if (numResponses2Wait > 0 || response2Wait.size() > 0) {
/*  763 */           for (String str : response2Wait) {
/*  764 */             if (strRcv.contains(str)) {
/*      */               break label22;
/*      */             }
/*      */           } 
/*      */         }
/*  769 */         timeout = System.currentTimeMillis() + time2Wait.intValue();
/*      */       } 
/*  771 */       this.wdt = Functions.updateWatchdog(this.wdt, 10L);
/*      */     } 
/*  773 */     return strRcv;
/*      */   }
/*      */   
/*      */   private String removeInitialSignalPlusAndZeroDigit(String phoneNum) {
/*  777 */     if (phoneNum.charAt(0) == '+') {
/*  778 */       phoneNum = phoneNum.substring(1);
/*      */     }
/*  780 */     while (phoneNum.startsWith("0")) {
/*  781 */       phoneNum = phoneNum.substring(1);
/*      */     }
/*  783 */     return phoneNum;
/*      */   }
/*      */ 
/*      */   
/*      */   private String sendATCommand(String cmdAt2Send, String response2Wait) throws IOException {
/*  788 */     response2Wait = (response2Wait == null) ? "OK;ERROR;" : response2Wait;
/*  789 */     this.sis.skip(this.sis.available());
/*  790 */     cmdAt2Send = cmdAt2Send.concat("\r");
/*  791 */     this.sos.write(cmdAt2Send.getBytes());
/*  792 */     return waitModemResponse(response2Wait, (Integer)null);
/*      */   }
/*      */   
/*      */   private void processAlivePacketReceivedByVoice(int productId) {
/*      */     try {
/*  797 */       switch (Util.EnumProductIDs.getProductID(productId)) {
/*      */         case PEGASUS:
/*  799 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_ALIVE_packet_received_via_voice_channel"), Enums.EnumMessagePriority.LOW, null, null);
/*  800 */           this.sp30DH = PegasusDBManager.executeSP_030(this.infoModule.idModule);
/*  801 */           if (this.sp30DH.getRegistered() == 0) {
/*  802 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Module_ID_(") + this.infoModule.idModule + LocaleMessage.getLocaleMessage(")_was_not_found_in_the_database_or_was_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null); break;
/*      */           } 
/*  804 */           generateEventReceptionAlivePacket(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, "SP_030", Enums.EnumNWProtocol.CSD.name());
/*      */           break;
/*      */       } 
/*      */     
/*  808 */     } catch (Exception ex) {
/*  809 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_ALIVE_packet_received_via_voice_channel"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */     } 
/*      */   }
/*      */   
/*      */   private void sendByte(short value) throws IOException {
/*  814 */     byte[] bValue = { (byte)value };
/*  815 */     this.sis.skip(this.sis.available());
/*      */     
/*  817 */     this.sos.write(bValue, 0, 1);
/*      */   }
/*      */   
/*      */   private boolean processM2SPacketReceivedByCSD(byte[] decData, short enc) throws IOException {
/*      */     try {
/*  822 */       this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  823 */       SP_V2_001_VO spV201VO = executeM2SPacket(decData, (InfoModule)null, true, enc);
/*  824 */       if (spV201VO != null) {
/*  825 */         if (spV201VO.getAuto_Registration_Executed() == 1) {
/*  826 */           if (spV201VO.getRegistered() == 0) {
/*  827 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_via_CSD_of_the_module_with_ID") + spV201VO.getSn(), Enums.EnumMessagePriority.HIGH, null, null);
/*  828 */             sendByte((short)224);
/*  829 */             return false;
/*      */           } 
/*  831 */         } else if (spV201VO.getRegistered() == 1) {
/*  832 */           if (spV201VO.getEnabled() == 0) {
/*  833 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Module_with_ID") + spV201VO.getSn() + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  834 */             sendByte((short)225);
/*  835 */             return false;
/*      */           } 
/*      */         } else {
/*  838 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Module_with_ID") + spV201VO.getSn() + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  839 */           sendByte((short)224);
/*  840 */           return false;
/*      */         } 
/*  842 */         Functions.generateEventReceptionAlivePacket(1, spV201VO.getId_Client(), spV201VO.getId_Module(), spV201VO.getId_Group(), spV201VO.getClientCode(), spV201VO.getE_Alive_Received(), spV201VO.getF_Alive_Received(), spV201VO.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.CSD.name(), Enums.EnumLastCommInterface.CSDSMS.getType(), -1);
/*  843 */         if (this.infoModule == null) {
/*  844 */           this.infoModule = new InfoModule();
/*      */         }
/*  846 */         this.infoModule.idClient = spV201VO.getId_Client();
/*  847 */         this.infoModule.idModule = spV201VO.getId_Module();
/*  848 */         this.infoModule.clientName = spV201VO.getName();
/*  849 */         this.idleTimeout = System.currentTimeMillis() + 15000L;
/*      */         
/*  851 */         sendByte((short)6);
/*  852 */         return true;
/*      */       } 
/*  854 */       sendByte((short)21);
/*  855 */       return false;
/*      */     }
/*  857 */     catch (Exception ex) {
/*  858 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_IDENTIFICATION_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  859 */       sendByte((short)21);
/*  860 */       return false;
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private void processM2SPacketReceivedBySMS(String smsMessage, InfoModule infoModule) throws IOException, Exception {
/*      */     try {
/*  867 */       byte[] m2sBuffer = Functions.converthexString2ByteArray(smsMessage);
/*  868 */       byte[] prod = new byte[2];
/*  869 */       prod[0] = m2sBuffer[1];
/*  870 */       prod[1] = m2sBuffer[0];
/*  871 */       String prodBin = String.format("%8s", new Object[] { Integer.toBinaryString(prod[0] & 0xFF) }).replace(' ', '0');
/*  872 */       short enc = Short.parseShort(prodBin.substring(0, 2), 2);
/*  873 */       prodBin = prodBin.substring(2);
/*  874 */       prodBin = prodBin.concat(String.format("%8s", new Object[] { Integer.toBinaryString(prod[1] & 0xFF) }).replace(' ', '0'));
/*  875 */       short prodI = Short.parseShort(prodBin, 2);
/*  876 */       if (prodI == Util.EnumProductIDs.PEGASUS.getProductId()) {
/*  877 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + infoModule.clientName + LocaleMessage.getLocaleMessage("]_M2S_packet_received_via_SMS"), Enums.EnumMessagePriority.LOW, null, null);
/*  878 */         byte[] length = new byte[2];
/*  879 */         length[0] = m2sBuffer[3];
/*  880 */         length[1] = m2sBuffer[2];
/*  881 */         int msgLen = Functions.getIntFrom2ByteArray(length);
/*  882 */         int crcCalc = CRC16.calculate(m2sBuffer, 0, msgLen + 2, 65535);
/*  883 */         byte[] crcbits = new byte[2];
/*  884 */         crcbits[0] = m2sBuffer[m2sBuffer.length - 2];
/*  885 */         crcbits[1] = m2sBuffer[m2sBuffer.length - 1];
/*  886 */         crcbits = Functions.swapLSB2MSB(crcbits);
/*  887 */         int crcRecv = Functions.getIntFrom2ByteArray(crcbits);
/*  888 */         if (crcCalc == crcRecv) {
/*      */           
/*  890 */           byte[] encData = new byte[msgLen - 2];
/*  891 */           System.arraycopy(m2sBuffer, 4, encData, 0, msgLen - 2);
/*  892 */           byte[] decData = new byte[msgLen - 2];
/*  893 */           byte[] decBlock = null;
/*      */           try {
/*  895 */             if (encData.length >= 16 && encData.length % 16 == 0) {
/*  896 */               for (int ii = 0; ii < encData.length; ) {
/*  897 */                 byte[] block = new byte[16];
/*  898 */                 System.arraycopy(encData, ii, block, 0, 16);
/*  899 */                 if (enc == 1) {
/*  900 */                   decBlock = Rijndael.decryptBytes(block, Rijndael.dataKeyBytes, false);
/*  901 */                 } else if (enc == 2) {
/*  902 */                   decBlock = Rijndael.decryptBytes(block, Rijndael.aes_256, false);
/*      */                 } 
/*  904 */                 System.arraycopy(decBlock, 0, decData, ii, 16);
/*  905 */                 ii += 16;
/*      */               } 
/*      */             }
/*  908 */           } catch (Exception ex) {
/*  909 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Error_while_decrypting_M2S_Packet_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */           } 
/*  911 */           SP_V2_001_VO spV201VO = executeM2SPacket(decData, infoModule, false, enc);
/*  912 */           if (spV201VO != null) {
/*  913 */             if (spV201VO.getRegistered() == 0) {
/*  914 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + infoModule.clientName + LocaleMessage.getLocaleMessage("]_Module_ID_(") + infoModule.idModule + LocaleMessage.getLocaleMessage(")_was_not_found_in_the_database_or_was_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */             } else {
/*  916 */               Functions.generateEventReceptionAlivePacket(Util.EnumProductIDs.PEGASUS.getProductId(), spV201VO.getId_Client(), spV201VO.getId_Module(), spV201VO.getId_Group(), spV201VO.getClientCode(), spV201VO.getE_Alive_Received(), spV201VO.getF_Alive_Received(), spV201VO.getLast_Alive_Packet_Event(), 2, Enums.EnumNWProtocol.SMS.name(), Enums.EnumLastCommInterface.CSDSMS.getType(), -1);
/*      */             } 
/*      */           }
/*      */         } else {
/*  920 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_M2S_packet_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, null);
/*  921 */           GlobalVariables.buzzerActivated = true;
/*      */         } 
/*      */       } else {
/*  924 */         Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + infoModule.clientName + LocaleMessage.getLocaleMessage("Invalid_product_identifier_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, null);
/*      */       } 
/*  926 */     } catch (Exception ex) {
/*  927 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, "[" + infoModule.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_a_M2S_packet_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean processIdentificationPacketReceivedByCSD() throws IOException {
/*      */     try {
/*  933 */       String iccid = getICCID(this.serialInputBuffer, 1);
/*  934 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_IDENTIFICATION_packet_received_via_CSD-ID") + iccid, Enums.EnumMessagePriority.LOW, null, null);
/*  935 */       if (TblPegasusActiveConnections.getInstance().containsKey(iccid)) {
/*  936 */         TblPegasusActiveConnections.getInstance().remove(iccid);
/*      */       }
/*  938 */       executeStoredProcedureHandlingIdentificationPacket(this.serialInputBuffer, iccid, "", this.telephoneNumberIdentified, Enums.EnumNWProtocol.CSD.name());
/*  939 */       if (this.sp15DH.getAuto_Registration_Executed() == 1) {
/*  940 */         if (this.sp15DH.getRegistered() == 0) {
/*  941 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Failure_while_executing_the_auto-registration_via_CSD_of_the_module_with_ID") + iccid, Enums.EnumMessagePriority.HIGH, null, null);
/*  942 */           sendByte((short)224);
/*  943 */           return false;
/*      */         } 
/*  945 */       } else if (this.sp15DH.getRegistered() == 1) {
/*  946 */         if (this.sp15DH.getEnabled() == 0) {
/*  947 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Module_with_ID") + iccid + LocaleMessage.getLocaleMessage("is_trying_connection_with_the_server_in_spite_of_being_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  948 */           sendByte((short)225);
/*  949 */           return false;
/*      */         } 
/*      */       } else {
/*  952 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Module_with_ID") + iccid + LocaleMessage.getLocaleMessage("it_is_not_registered_in_the_server"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  953 */         sendByte((short)224);
/*  954 */         return false;
/*      */       } 
/*      */       
/*  957 */       if (this.infoModule == null) {
/*  958 */         this.infoModule = new InfoModule();
/*      */       }
/*  960 */       this.infoModule.idClient = this.sp15DH.getId_Client();
/*  961 */       this.infoModule.idModule = this.sp15DH.getId_Module();
/*  962 */       this.infoModule.clientName = this.sp15DH.getName();
/*  963 */       this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  964 */       sendByte((short)6);
/*  965 */       return true;
/*  966 */     } catch (Exception ex) {
/*  967 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_IDENTIFICATION_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  968 */       sendByte((short)21);
/*  969 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean processAlivePacketReceivedByCSD() throws IOException {
/*      */     try {
/*  975 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_ALIVE_packet_received_via_CSD"), Enums.EnumMessagePriority.LOW, null, null);
/*  976 */       executeStoredProcedureHandlingAlivePacket(this.serialInputBuffer, this.infoModule.idModule, Enums.EnumNWProtocol.CSD.name());
/*  977 */       if (this.sp03DH.getRegistered() == 0) {
/*  978 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Module_ID_(") + this.infoModule.idModule + LocaleMessage.getLocaleMessage(")_was_not_found_in_the_database_or_was_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*  979 */         sendByte((short)21);
/*  980 */         return false;
/*      */       } 
/*  982 */       generateEventReceptionAlivePacket(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, "SP_003", Enums.EnumNWProtocol.CSD.name());
/*  983 */       this.infoModule.clientName = this.sp03DH.getName();
/*      */       
/*  985 */       this.idleTimeout = System.currentTimeMillis() + 15000L;
/*  986 */       sendByte((short)6);
/*  987 */       return true;
/*  988 */     } catch (Exception ex) {
/*  989 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_ALIVE_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, ex);
/*  990 */       sendByte((short)21);
/*  991 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean processEventPacketReceivedByCSD() throws IOException {
/*      */     try {
/*      */       byte[] bufferEvent;
/*  998 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_EVENT_packet_received_via_CSD"), Enums.EnumMessagePriority.LOW, null, null);
/*  999 */       if (this.serialInputBuffer[1] == Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId()) {
/* 1000 */         bufferEvent = new byte[8];
/* 1001 */         System.arraycopy(this.serialInputBuffer, 6, bufferEvent, 0, 8);
/*      */       } else {
/* 1003 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Event_received_with_invalid_protocol_identifier"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1004 */         sendByte((short)21);
/* 1005 */         return false;
/*      */       } 
/* 1007 */       updateClientCode(bufferEvent, this.infoModule.idClient);
/* 1008 */       String comPort = Functions.getReceiverCOMPortByGroupID(this.infoModule.idGroup, "PEGASUS");
/* 1009 */       Functions.insertEvent(Util.EnumProductIDs.PEGASUS.getProductId(), this.infoModule.idModule, this.infoModule.idGroup, this.infoModule.idClient, comPort, this.serialInputBuffer[1], bufferEvent, 1, Enums.EnumNWProtocol.CSD.name(), Enums.EnumLastCommInterface.CSDSMS.getType(), -1, 1);
/* 1010 */       generateEventFailureTransmissionTestTelephoneLine(this.serialInputBuffer, this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, 2, Enums.EnumNWProtocol.CSD.name());
/*      */       
/* 1012 */       if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/* 1013 */         ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */       }
/*      */       
/* 1016 */       this.idleTimeout = System.currentTimeMillis() + 15000L;
/* 1017 */       sendByte((short)6);
/* 1018 */       return true;
/* 1019 */     } catch (Exception ex) {
/* 1020 */       byte[] bufferEvent; Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_EVENT_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, (Throwable)bufferEvent);
/* 1021 */       sendByte((short)21);
/* 1022 */       return false;
/*      */     } 
/*      */   }
/*      */   
/*      */   private boolean isSmsContentValid(String content) {
/* 1027 */     if (content != null && content.length() > 0) {
/* 1028 */       content = content.toUpperCase();
/* 1029 */       return content.matches("^[0-9A-F;]+$");
/*      */     } 
/* 1031 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean isPegasusLegacySMS(String content) {
/* 1036 */     return (content.startsWith("05;") || content.startsWith("02;"));
/*      */   }
/*      */   
/*      */   private void processAlivePacketReceivedBySMS(String packet) throws Exception {
/*      */     try {
/* 1041 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_ALIVE_packet_received_via_SMS"), Enums.EnumMessagePriority.LOW, null, null);
/* 1042 */       if (packet != null && packet.length() == 34) {
/* 1043 */         byte[] aliveBuffer = Functions.converthexString2ByteArray(packet);
/* 1044 */         int crcReceived = aliveBuffer[16] & 0xFF;
/* 1045 */         crcReceived = crcReceived * 256 + (aliveBuffer[15] & 0xFF);
/* 1046 */         int crcCalc = CRC16.calculate(aliveBuffer, 0, 15, 65535);
/* 1047 */         if (crcReceived == crcCalc) {
/* 1048 */           executeStoredProcedureHandlingAlivePacket(aliveBuffer, this.infoModule.idModule, Enums.EnumNWProtocol.SMS.name());
/* 1049 */           if (this.sp03DH.getRegistered() == 0) {
/* 1050 */             Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Module_ID_(") + this.infoModule.idModule + LocaleMessage.getLocaleMessage(")_was_not_found_in_the_database_or_was_disabled"), Enums.EnumMessagePriority.AVERAGE, null, null);
/*      */           } else {
/* 1052 */             generateEventReceptionAlivePacket(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, "SP_003", Enums.EnumNWProtocol.SMS.name());
/*      */           } 
/*      */         } else {
/* 1055 */           Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_CRC_error_on_the_ALIVE_packet_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1056 */           GlobalVariables.buzzerActivated = true;
/*      */         } 
/*      */       } else {
/* 1059 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_An_ALIVE_packet_with_incorrect_size_(") + packet + LocaleMessage.getLocaleMessage(")_was_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1060 */         GlobalVariables.buzzerActivated = true;
/*      */       } 
/* 1062 */     } catch (Exception ex) {
/* 1063 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_ALIVE_packet_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1064 */       throw ex;
/*      */     } 
/*      */   }
/*      */   
/*      */   private void processEventReceivedBySMS(String event) throws Exception {
/*      */     try {
/* 1070 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_EVENT_packet_received_via_SMS"), Enums.EnumMessagePriority.LOW, null, null);
/* 1071 */       if (event != null && event.length() == 16) {
/* 1072 */         byte[] eventBuffer = Functions.converthexString2ByteArray(event);
/* 1073 */         updateClientCode(eventBuffer, this.infoModule.idClient);
/*      */         
/* 1075 */         String comPort = Functions.getReceiverCOMPortByGroupID(this.infoModule.idGroup, "PEGASUS");
/* 1076 */         Functions.insertEvent(Util.EnumProductIDs.PEGASUS.getProductId(), this.infoModule.idModule, this.infoModule.idGroup, this.infoModule.idClient, comPort, (byte)Enums.EnumAlarmPanelProtocol.CONTACT_ID.getId(), eventBuffer, 1, Enums.EnumNWProtocol.SMS.name(), Enums.EnumLastCommInterface.CSDSMS.getType(), -1, 0);
/*      */         
/* 1078 */         if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/* 1079 */           ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */         }
/*      */       } else {
/* 1082 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_An_event_with_incorrect_size_(") + event + LocaleMessage.getLocaleMessage(")_was_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1083 */         GlobalVariables.buzzerActivated = true;
/*      */       } 
/* 1085 */     } catch (Exception ex) {
/* 1086 */       Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.infoModule.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_an_event_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 1087 */       throw ex;
/*      */     } 
/*      */   }
/*      */   
/*      */   private SP_V2_001_VO executeM2SPacket(byte[] decData, InfoModule im, boolean csdFlag, short enc) throws IOException {
/*      */     try {
/* 1093 */       SP_V2_001_VO spV201VO = new SP_V2_001_VO();
/* 1094 */       spV201VO.setM2sData(decData);
/* 1095 */       if (enc == 1) {
/* 1096 */         spV201VO.setLastEncryption(Enums.EnumEncyption.AES128.getType());
/* 1097 */       } else if (enc == 2) {
/* 1098 */         spV201VO.setLastEncryption(Enums.EnumEncyption.AES256.getType());
/*      */       } 
/*      */ 
/*      */       
/* 1102 */       int contactIdCounter = 0;
/* 1103 */       List<String> originalClientCodes = new ArrayList<>(6);
/* 1104 */       byte[] eventData = new byte[48];
/*      */       
/* 1106 */       int index = 0;
/* 1107 */       byte[] fid = new byte[2];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 1114 */       while (index < decData.length && 
/* 1115 */         index + 2 <= decData.length) {
/*      */         short simNum; StringBuilder sb; int i; byte oper[], tmpRevEvent[], numByte; StringBuilder originalEvent; String sEvent; int idGroup; String comPort; int st;
/*      */         short apn;
/* 1118 */         System.arraycopy(decData, index, fid, 0, 2);
/* 1119 */         index += 2;
/* 1120 */         fid = Functions.swapLSB2MSB(fid);
/* 1121 */         int fidVal = Functions.getIntFrom2ByteArray(fid);
/* 1122 */         if (fidVal <= 0) {
/*      */           break;
/*      */         }
/* 1125 */         short flen = (short)Functions.getIntFromHexByte(decData[index]);
/* 1126 */         byte[] fcon = new byte[flen];
/* 1127 */         System.arraycopy(decData, ++index, fcon, 0, flen);
/* 1128 */         index += flen;
/* 1129 */         switch (fidVal) {
/*      */           case 1:
/* 1131 */             sb = new StringBuilder();
/* 1132 */             for (i = 0; i < flen; i++) {
/* 1133 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/* 1135 */             spV201VO.setSn(sb.toString());
/* 1136 */             spV201VO.setInitialPacket(true);
/*      */           
/*      */           case 2:
/* 1139 */             sb = new StringBuilder();
/* 1140 */             for (i = 0; i < flen - 1; i++) {
/* 1141 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/* 1143 */             sb.append(fcon[7] / 10);
/* 1144 */             spV201VO.setModemIMEI(sb.toString());
/*      */           
/*      */           case 45:
/* 1147 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1148 */             sb = new StringBuilder();
/* 1149 */             for (i = 1; i < flen - 1; i++) {
/* 1150 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/* 1152 */             sb.append(fcon[8] / 10);
/* 1153 */             if (simNum == 1) {
/* 1154 */               spV201VO.setSimcard1IMSI(sb.toString()); continue;
/* 1155 */             }  if (simNum == 2) {
/* 1156 */               spV201VO.setSimcard2IMSI(sb.toString());
/*      */             }
/*      */           
/*      */           case 3:
/* 1160 */             spV201VO.setModemModel((short)fcon[0]);
/*      */           
/*      */           case 4:
/* 1163 */             sb = new StringBuilder();
/* 1164 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append(".").append(Functions.getIntFromHexByte(fcon[1])).append(".").append(Functions.getIntFromHexByte(fcon[2])).append(".").append(Functions.getIntFromHexByte(fcon[3]));
/* 1165 */             spV201VO.setModemFWVersion(sb.toString());
/*      */           
/*      */           case 5:
/* 1168 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1169 */             sb = new StringBuilder();
/* 1170 */             for (i = 1; i < flen; i++) {
/* 1171 */               sb.append(Functions.getFormatIntFromHexByte(fcon[i]));
/*      */             }
/* 1173 */             if (simNum == 1) {
/* 1174 */               spV201VO.setSimcard1ICCID(sb.toString()); continue;
/* 1175 */             }  if (simNum == 2) {
/* 1176 */               spV201VO.setSimcard2ICCID(sb.toString());
/*      */             }
/*      */           
/*      */           case 6:
/* 1180 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1181 */             oper = new byte[flen - 1];
/* 1182 */             System.arraycopy(fcon, 1, oper, 0, flen - 1);
/*      */             
/* 1184 */             if (simNum == 1) {
/* 1185 */               spV201VO.setSimcard1Operator(Functions.getASCIIFromByteArray(oper)); continue;
/* 1186 */             }  if (simNum == 2) {
/* 1187 */               spV201VO.setSimcard2Operator(Functions.getASCIIFromByteArray(oper));
/*      */             }
/*      */           
/*      */           case 8:
/* 1191 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1192 */             spV201VO.setCurrentSim(simNum);
/* 1193 */             simNum = (short)Functions.getIntFromHexByte(fcon[1]);
/* 1194 */             spV201VO.setCurrentAPN(simNum);
/*      */           
/*      */           case 9:
/* 1197 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1198 */             spV201VO.setLast_Comm_Interface(simNum);
/* 1199 */             spV201VO.setInitialPacket(true);
/*      */           
/*      */           case 11:
/* 1202 */             spV201VO.setModuleHWDtls(Functions.getIntFrom2ByteArray(fcon));
/*      */           
/*      */           case 12:
/* 1205 */             sb = new StringBuilder();
/* 1206 */             sb.append(Functions.getIntFromHexByte(fcon[0])).append('.').append(Functions.getIntFromHexByte(fcon[1])).append('.').append(Functions.getIntFromHexByte(fcon[2]));
/* 1207 */             spV201VO.setPegasus_Firmware_Version(sb.toString());
/*      */           
/*      */           case 13:
/* 1210 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1211 */             spV201VO.setOperation_Mode(simNum);
/*      */           
/*      */           case 31:
/* 1214 */             if (flen != 8) {
/* 1215 */               Functions.printMessage(Util.EnumProductIDs.PEGASUS, LocaleMessage.getLocaleMessage("An_event_was_received/generated_with_incorrect_size"), Enums.EnumMessagePriority.HIGH, null, null);
/* 1216 */               GlobalVariables.buzzerActivated = true;
/*      */             } 
/* 1218 */             tmpRevEvent = new byte[8];
/* 1219 */             System.arraycopy(fcon, 0, tmpRevEvent, 0, 8);
/*      */             
/* 1221 */             originalEvent = new StringBuilder();
/* 1222 */             for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/* 1223 */               originalEvent.append(Functions.convertContactIdDigitToHex((fcon[numByte] & 0xF0) / 16)).append(Functions.convertContactIdDigitToHex(fcon[numByte] & 0xF));
/*      */             }
/* 1225 */             sEvent = originalEvent.toString();
/* 1226 */             originalClientCodes.add(originalEvent.substring(0, 4));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/* 1234 */             idGroup = this.infoModule.idGroup;
/* 1235 */             comPort = Functions.getReceiverCOMPortByGroupID(idGroup, "PEGASUS");
/* 1236 */             spV201VO.setRcvrCOMPort(comPort);
/* 1237 */             spV201VO.setRcvrGroup(idGroup);
/* 1238 */             if (ZeusServerCfg.getInstance().getMonitoringInfo() != null && ZeusServerCfg.getInstance().getMonitoringInfo().containsKey(comPort)) {
/* 1239 */               if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.ADICIONAR.getPartitionScheme()) {
/* 1240 */                 int partition = Integer.parseInt(sEvent.substring(10, 12), 16);
/* 1241 */                 partition = (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue() > 99) ? 99 : (partition + ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getValueAddedPartition().intValue());
/* 1242 */                 sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { Integer.valueOf(partition) }) + sEvent.substring(12, 16);
/* 1243 */               } else if (((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme().intValue() == Enums.EnumPartitionScheme.SUBSTITUIR.getPartitionScheme()) {
/* 1244 */                 sEvent = sEvent.substring(0, 10) + String.format("%02d", new Object[] { ((MonitoringInfo)ZeusServerCfg.getInstance().getMonitoringInfo().get(comPort)).getPartitionScheme() }) + sEvent.substring(12, 16);
/*      */               } 
/*      */             }
/* 1247 */             if (sEvent.equals(originalEvent.toString())) {
/* 1248 */               byte checksum = 0;
/* 1249 */               sEvent = sEvent.substring(0, 15).replaceAll("0", "A");
/* 1250 */               for (numByte = 0; numByte <= 14; numByte = (byte)(numByte + 1)) {
/* 1251 */                 checksum = (byte)(checksum + Integer.parseInt(sEvent.substring(numByte, numByte + 1), 16));
/*      */               }
/* 1253 */               sEvent = sEvent.concat(Functions.convertInt2Hex((checksum % 15 == 0) ? 15 : (15 - checksum % 15)));
/* 1254 */               for (numByte = 0; numByte <= 7; numByte = (byte)(numByte + 1)) {
/* 1255 */                 fcon[numByte] = (byte)Integer.parseInt(sEvent.substring(numByte * 2, numByte * 2 + 2), 16);
/*      */               }
/*      */             } 
/* 1258 */             Functions.printEvent(Util.EnumProductIDs.PEGASUS.getProductId(), fcon, idGroup, this.infoModule.idClient);
/* 1259 */             System.arraycopy(fcon, 0, eventData, contactIdCounter, 8);
/* 1260 */             contactIdCounter += 8;
/* 1261 */             if (TblThreadsEmulaReceivers.getInstance().containsKey(comPort)) {
/* 1262 */               ((EmulateReceiver)TblThreadsEmulaReceivers.getInstance().get(comPort)).newEvent = true;
/*      */             }
/*      */ 
/*      */           
/*      */           case 33:
/* 1267 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1268 */             spV201VO.setWifiModel(simNum);
/*      */           
/*      */           case 34:
/* 1271 */             spV201VO.setWifiFW(fcon[0] + "." + fcon[1]);
/*      */           
/*      */           case 35:
/* 1274 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1275 */             spV201VO.setCurrentWifiAccessPoint(simNum);
/*      */ 
/*      */           
/*      */           case 28:
/* 1279 */             if (Functions.getIntFromHexByte(fcon[0]) == 1) {
/* 1280 */               generateEventFailureTransmissionTestTelephoneLine(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, 2);
/*      */             }
/*      */           
/*      */           case 36:
/* 1284 */             if (Functions.getIntFromHexByte(fcon[1]) == 0) {
/* 1285 */               generateEventFailureTransmissionTestTelephoneLine(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, (Functions.getIntFromHexByte(fcon[0]) == 0) ? 8 : 9);
/*      */             }
/*      */           
/*      */           case 27:
/* 1289 */             if (Functions.getIntFromHexByte(fcon[0]) == 0) {
/* 1290 */               generateEventFailureTransmissionTestTelephoneLine(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, 1);
/*      */             }
/*      */           
/*      */           case 29:
/* 1294 */             if (Functions.getIntFromHexByte(fcon[0]) == 0) {
/* 1295 */               generateEventFailureTransmissionTestTelephoneLine(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, 3);
/*      */             }
/*      */           
/*      */           case 30:
/* 1299 */             st = 0;
/* 1300 */             simNum = (short)Functions.getIntFromHexByte(fcon[0]);
/* 1301 */             apn = (short)Functions.getIntFromHexByte(fcon[1]);
/* 1302 */             if (simNum == 1 && apn == 1) {
/* 1303 */               st = 4;
/* 1304 */             } else if (simNum == 1 && apn == 2) {
/* 1305 */               st = 5;
/* 1306 */             } else if (simNum == 2 && apn == 1) {
/* 1307 */               st = 6;
/* 1308 */             } else if (simNum == 2 && apn == 2) {
/* 1309 */               st = 7;
/*      */             } 
/* 1311 */             if (Functions.getIntFromHexByte(fcon[2]) == 0) {
/* 1312 */               generateEventFailureTransmissionTestTelephoneLine(this.infoModule.idClient, this.infoModule.idModule, this.infoModule.idGroup, st);
/*      */             }
/*      */         } 
/*      */ 
/*      */       
/*      */       } 
/* 1318 */       spV201VO.setOriginalClientCodes(originalClientCodes);
/* 1319 */       spV201VO.setEventData(eventData);
/* 1320 */       spV201VO.setAlarmPanelProtocol((short)1);
/*      */       
/* 1322 */       if (csdFlag) {
/* 1323 */         spV201VO.setLastNWProtocol(Enums.EnumNWProtocol.CSD.name());
/* 1324 */         if (!spV201VO.isInitialPacket()) {
/* 1325 */           spV201VO.setSn(this.infoModule.sn);
/*      */         }
/* 1327 */         spV201VO.setCurrentSim((short)this.infoModule.current_csd_sms_sim);
/* 1328 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_M2S_packet_received_via_CSD") + spV201VO.getSn(), Enums.EnumMessagePriority.LOW, null, null);
/* 1329 */         if (TblPegasusActiveConnections.getInstance().containsKey(spV201VO.getSn())) {
/* 1330 */           TblPegasusActiveConnections.getInstance().remove(spV201VO.getSn());
/*      */         }
/*      */       } else {
/* 1333 */         spV201VO.setSn(im.sn);
/* 1334 */         spV201VO.setCurrentSim((short)im.current_csd_sms_sim);
/* 1335 */         spV201VO.setLastNWProtocol(Enums.EnumNWProtocol.SMS.name());
/* 1336 */         spV201VO.setLast_Comm_Interface((short)7);
/*      */       } 
/* 1338 */       spV201VO.setAuto_Registration_Enabled((short)(ZeusServerCfg.getInstance().getAutoRegistration() ? 1 : 0));
/* 1339 */       spV201VO.setModule_Ip_Addr("");
/*      */       
/* 1341 */       if (spV201VO.getCurrentSim() == 1) {
/* 1342 */         spV201VO.setSimcard1(this.telephoneNumberIdentified);
/* 1343 */       } else if (spV201VO.getCurrentSim() == 2) {
/* 1344 */         spV201VO.setSimcard2(this.telephoneNumberIdentified);
/*      */       } 
/* 1346 */       return PegasusDBManager.executeSP_V2_001(spV201VO);
/* 1347 */     } catch (Exception ex) {
/* 1348 */       if (csdFlag) {
/* 1349 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + this.telephoneNumberIdentified + LocaleMessage.getLocaleMessage("]_Exception_while_processing_the_M2S_packet_received_via_CSD"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */       } else {
/* 1351 */         Functions.printMessage(Util.EnumProductIDs.PEGASUS, "[" + im.clientName + LocaleMessage.getLocaleMessage("]_Exception_while_processing_a_M2S_packet_received_via_SMS"), Enums.EnumMessagePriority.HIGH, null, ex);
/*      */       } 
/* 1353 */       return null;
/*      */     } 
/*      */   }
/*      */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\CommReceiverCSD.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */