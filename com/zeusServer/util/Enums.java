/*     */ package com.zeusServer.util;
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
/*     */ public class Enums
/*     */ {
/*     */   public enum enumDbStatus
/*     */   {
/*  16 */     NORMAL(0),
/*  17 */     AUTO_BACKUP(1),
/*  18 */     RESTORE(2),
/*  19 */     BACKUP(3),
/*  20 */     CLEANUP(4),
/*  21 */     SPACE_RECLAIM(7),
/*  22 */     CLEAN_PEGASUS_EVENT_TABLE(8),
/*  23 */     CLEAN_GRCP_EVENT_TABLE(9),
/*  24 */     CLEAN_AVL_EVENT_TABLE(10),
/*  25 */     CLEAN_PEGASUS_OCCUR_TABLE(11),
/*  26 */     CLEAN_GRCP_OCCUR_TABLE(12),
/*  27 */     CLEAN_AVL_OCCUR_TABLE(13),
/*  28 */     CLEAN_PEGASUS_CMD_TABLE(14),
/*  29 */     CLEAN_GRCP_CMD_TABLE(15),
/*  30 */     CLEAN_AVL_CMD_TABLE(16);
/*     */     private int dbCurrentStatus;
/*     */     
/*     */     enumDbStatus(int partitionScheme) {
/*  34 */       this.dbCurrentStatus = partitionScheme;
/*     */     }
/*     */     
/*     */     public int getDB_CURRENT_STATUS() {
/*  38 */       return this.dbCurrentStatus;
/*     */     }
/*     */   }
/*     */   
/*     */   public enum EnumDDNSServiceProvider
/*     */   {
/*  44 */     NOIP(1),
/*  45 */     DNSMADEEASY(2),
/*  46 */     DYNDNS(3);
/*     */     private int ddnsSP;
/*     */     
/*     */     EnumDDNSServiceProvider(int ddnsSP) {
/*  50 */       this.ddnsSP = ddnsSP;
/*     */     }
/*     */     
/*     */     public int getDDNSServiceProvider() {
/*  54 */       return this.ddnsSP;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumAlarmPanelProtocol
/*     */   {
/*  61 */     CONTACT_ID(1);
/*     */     private int id;
/*     */     
/*     */     EnumAlarmPanelProtocol(int id) {
/*  65 */       this.id = id;
/*     */     }
/*     */     
/*     */     public int getId() {
/*  69 */       return this.id;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumGsmBand
/*     */   {
/*  76 */     PGSM_900_MHZ(0),
/*  77 */     DCS_1800_MHZ(1),
/*  78 */     PCS_1900_MHZ(2),
/*  79 */     EGSM_DCS_900_1800_MHZ(3),
/*  80 */     GSM_PCS_850_1900_MHZ(4);
/*     */ 
/*     */     
/*     */     private int band;
/*     */ 
/*     */ 
/*     */     
/*     */     EnumGsmBand(int band) {
/*  88 */       this.band = band;
/*     */     }
/*     */     
/*     */     public int getBand() {
/*  92 */       return this.band;
/*     */     }
/*     */     
/*     */     public static EnumGsmBand getGsmBandFromString(String band) {
/*  96 */       if (band != null) {
/*  97 */         for (EnumGsmBand gsm : values()) {
/*  98 */           if (gsm.getBand() == Integer.parseInt(band)) {
/*  99 */             return gsm;
/*     */           }
/*     */         } 
/*     */       }
/* 103 */       return null;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumTipoReceptora
/*     */   {
/* 110 */     ADEMCO_685(0),
/* 111 */     SURGARD(1),
/* 112 */     CM_PLUS(4),
/* 113 */     SURGARD_VIA_TCPIP(5),
/* 114 */     RADIONICS_D6600(6);
/*     */     private int type;
/*     */     
/*     */     EnumTipoReceptora(int type) {
/* 118 */       this.type = type;
/*     */     }
/*     */     
/*     */     public int getType() {
/* 122 */       return this.type;
/*     */     }
/*     */   }
/*     */   
/*     */   public enum EnumNWProtocol
/*     */   {
/* 128 */     TCP,
/* 129 */     UDP,
/* 130 */     CSD,
/* 131 */     SMS;
/*     */   }
/*     */   
/*     */   public enum EnumEncyption
/*     */   {
/* 136 */     AES128(128),
/* 137 */     AES256(256);
/*     */     private int type;
/*     */     
/*     */     EnumEncyption(int type) {
/* 141 */       this.type = type;
/*     */     }
/*     */     
/*     */     public int getType() {
/* 145 */       return this.type;
/*     */     }
/*     */   }
/*     */   
/*     */   public enum EnumLastCommInterface
/*     */   {
/* 151 */     GPRS(1),
/* 152 */     CSDSMS(2),
/* 153 */     ETH(3),
/* 154 */     WIFI(6);
/*     */     private int type;
/*     */     
/*     */     EnumLastCommInterface(int type) {
/* 158 */       this.type = type;
/*     */     }
/*     */     
/*     */     public int getType() {
/* 162 */       return this.type;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumCSDReceiverModel
/*     */   {
/* 169 */     NONE(0),
/* 170 */     SIM340(1),
/* 171 */     MC35(2),
/* 172 */     SIM900(3);
/*     */     private int model;
/*     */     
/*     */     EnumCSDReceiverModel(int model) {
/* 176 */       this.model = model;
/*     */     }
/*     */     
/*     */     public int getModel() {
/* 180 */       return this.model;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumEventQualifier
/*     */   {
/* 187 */     NEW_EVENT(1),
/* 188 */     NEW_RESTORE(3),
/* 189 */     PREVIOUS_EVENT(6);
/*     */     private int event;
/*     */     
/*     */     EnumEventQualifier(int event) {
/* 193 */       this.event = event;
/*     */     }
/*     */     
/*     */     public int getEvent() {
/* 197 */       return this.event;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumOccurrenceType
/*     */   {
/* 204 */     NONE(0),
/* 205 */     MODULE_OFFLINE(1),
/* 206 */     LINE_SIMULATOR_OFFLINE(2),
/* 207 */     SOFTWARE_MONITORING_OFFLINE(3),
/* 208 */     MANUAL_CONTROL_ALARM_PANEL_CONNECTION(4),
/* 209 */     INTERNET_OFFLINE(5),
/* 210 */     TELEPHONE_LINE_NOT_DETECTED(6),
/* 211 */     DUAL_MONITORING_FAILURE(7),
/* 212 */     TELEPHONE_LINE_TRANSMISSION_FAILURE(8),
/* 213 */     SIGNAL_LEVEL_BELOW_MINIMUM(9),
/* 214 */     ALARM_PANEL_COMMUNICATION_FAILURE(10),
/* 215 */     GPRS_NETWORK_OFFLINE(11),
/* 216 */     BATTERY_VOLTAGE_BELOW_MINIMUM(12),
/* 217 */     POWER_SUPPLY_NOT_DETECTED(13),
/* 218 */     PERIPHERAL_OFFLINE(14),
/* 219 */     ALARM_PANEL_RETURN_CUTOFF(15),
/* 220 */     DIGITAL_INPUT_1_ACTIVATED(16),
/* 221 */     DIGITAL_INPUT_2_ACTIVATED(17),
/* 222 */     DIGITAL_INPUT_3_ACTIVATED(18),
/* 223 */     DIGITAL_INPUT_4_ACTIVATED(19),
/* 224 */     SIMCARD_1_FAIL(20),
/* 225 */     PHONE_LINE_STATUS(21),
/* 226 */     ALARM_PANEL_RETURN_STATUS(22),
/* 227 */     DUAL_MONITORING_STATUS(23),
/* 228 */     SIMCARD_2_FAIL(24),
/* 229 */     GSM_MODEM_FAIL(25),
/* 230 */     ETH_FAIL(26),
/* 231 */     WIFI_FAIL(27),
/* 232 */     MAIN_PWR_SUPPLY_STATUS(28),
/* 233 */     ALARM_PANEL_COMM_STATUS(29),
/* 234 */     GSM_JAMMER_STATUS(30),
/* 235 */     GSM_SIGNAL_BELOW_MIN(31),
/* 236 */     BATTERY_VOLTAGE_BELOW_MIN(32),
/* 237 */     BATTERY_CHARGER_STATUS(33),
/* 238 */     ALARM_PANEL_OPERATION_MODE(34),
/* 239 */     ALARM_PANEL_CONNECTION_STATUS(35),
/* 240 */     ALARM_PANEL_COMM_TEST_STATUS(36),
/* 241 */     TELEPHONE_LINE_COMM_TEST_STATUS(37),
/* 242 */     ETH_IFACE_TEST_STATUS(38),
/* 243 */     MODEM_IFACE_STATUS_1(39),
/* 244 */     MODEM_IFACE_STATUS_2(40),
/* 245 */     MODEM_IFACE_STATUS_3(41),
/* 246 */     MODEM_IFACE_STATUS_4(42),
/* 247 */     TAMPER_DETECTION(43),
/* 248 */     WIFI_IFACE_TEST_STATUS_AP_1(44),
/* 249 */     WIFI_IFACE_TEST_STATUS_AP_2(84),
/* 250 */     SIMCARD_1_STATUS(45),
/* 251 */     SIMCARD_2_STATUS(46),
/* 252 */     NTP_SYNC_DATA_STATUS(47),
/* 253 */     LAST_OTA_STATUS(48),
/* 254 */     SIMCARD_1_REG_STATUS(49),
/* 255 */     SIMCARD_1_OPERATIVE_STATUS(50),
/* 256 */     SIMCARD_2_REG_STATUS(51),
/* 257 */     SIMCARD_2_OPERATIVE_STATUS(52),
/* 258 */     BATTERY_OVER_TEMPERATURE(53),
/* 259 */     BATTERY_CHARGING_OVER_TIME(54),
/* 260 */     ZONE_1_NORMAL(60),
/* 261 */     ZONE_1_ACTIVE(61),
/* 262 */     ZONE_1_WIREFAULT(62),
/* 263 */     ZONE_1_TAMPER(63),
/* 264 */     ZONE_1_ALARM(64),
/* 265 */     ZONE_2_NORMAL(65),
/* 266 */     ZONE_2_ACTIVE(66),
/* 267 */     ZONE_2_WIREFAULT(67),
/* 268 */     ZONE_2_TAMPER(68),
/* 269 */     ZONE_2_ALARM(69),
/* 270 */     ZONE_3_NORMAL(70),
/* 271 */     ZONE_3_ACTIVE(71),
/* 272 */     ZONE_3_WIREFAULT(72),
/* 273 */     ZONE_3_TAMPER(73),
/* 274 */     ZONE_3_ALARM(74),
/* 275 */     ZONE_4_NORMAL(75),
/* 276 */     ZONE_4_ACTIVE(76),
/* 277 */     ZONE_4_WIREFAULT(77),
/* 278 */     ZONE_4_TAMPER(78),
/* 279 */     ZONE_4_ALARM(79),
/* 280 */     SYSTEM_AWAY_ARM(80),
/* 281 */     SYSTEM_FORCE_ARM(81),
/* 282 */     SYSTEM_STAY_ARM(82),
/* 283 */     SYSTEM_FORCE_STAY_ARM(83),
/* 284 */     ZONE_1_BYPASS(85),
/* 285 */     ZONE_1_FORCE_ARMED(86),
/* 286 */     ZONE_2_BYPASS(87),
/* 287 */     ZONE_2_FORCE_ARMED(88),
/* 288 */     ZONE_3_BYPASS(89),
/* 289 */     ZONE_3_FORCE_ARMED(90),
/* 290 */     ZONE_4_BYPASS(91),
/* 291 */     ZONE_4_FORCE_ARMED(92),
/* 292 */     KEYLOQ_HW_FAILURE(93),
/* 293 */     IPIC(94),
/* 294 */     SYSTEM_DISARMED(95),
/* 295 */     KEYFOB_LOW_BATTERY_15(101),
/* 296 */     KEYFOB_LOW_BATTERY_14(102),
/* 297 */     KEYFOB_LOW_BATTERY_13(103),
/* 298 */     KEYFOB_LOW_BATTERY_12(104),
/* 299 */     KEYFOB_LOW_BATTERY_11(105),
/* 300 */     KEYFOB_LOW_BATTERY_10(106),
/* 301 */     KEYFOB_LOW_BATTERY_9(107),
/* 302 */     KEYFOB_LOW_BATTERY_8(108),
/* 303 */     KEYFOB_LOW_BATTERY_7(109),
/* 304 */     KEYFOB_LOW_BATTERY_6(110),
/* 305 */     KEYFOB_LOW_BATTERY_5(111),
/* 306 */     KEYFOB_LOW_BATTERY_4(112),
/* 307 */     KEYFOB_LOW_BATTERY_3(113),
/* 308 */     KEYFOB_LOW_BATTERY_2(114),
/* 309 */     KEYFOB_LOW_BATTERY_1(115),
/* 310 */     KEYFOB_PANIC_15(116),
/* 311 */     KEYFOB_PANIC_14(117),
/* 312 */     KEYFOB_PANIC_13(118),
/* 313 */     KEYFOB_PANIC_12(119),
/* 314 */     KEYFOB_PANIC_11(120),
/* 315 */     KEYFOB_PANIC_10(121),
/* 316 */     KEYFOB_PANIC_9(122),
/* 317 */     KEYFOB_PANIC_8(123),
/* 318 */     KEYFOB_PANIC_7(124),
/* 319 */     KEYFOB_PANIC_6(125),
/* 320 */     KEYFOB_PANIC_5(126),
/* 321 */     KEYFOB_PANIC_4(127),
/* 322 */     KEYFOB_PANIC_3(128),
/* 323 */     KEYFOB_PANIC_2(129),
/* 324 */     KEYFOB_PANIC_1(130),
/* 325 */     KEYFOB_COMM_TEST_15(131),
/* 326 */     KEYFOB_COMM_TEST_14(132),
/* 327 */     KEYFOB_COMM_TEST_13(133),
/* 328 */     KEYFOB_COMM_TEST_12(134),
/* 329 */     KEYFOB_COMM_TEST_11(135),
/* 330 */     KEYFOB_COMM_TEST_10(136),
/* 331 */     KEYFOB_COMM_TEST_9(137),
/* 332 */     KEYFOB_COMM_TEST_8(138),
/* 333 */     KEYFOB_COMM_TEST_7(139),
/* 334 */     KEYFOB_COMM_TEST_6(140),
/* 335 */     KEYFOB_COMM_TEST_5(141),
/* 336 */     KEYFOB_COMM_TEST_4(142),
/* 337 */     KEYFOB_COMM_TEST_3(143),
/* 338 */     KEYFOB_COMM_TEST_2(144),
/* 339 */     KEYFOB_COMM_TEST_1(145),
/* 340 */     BATTERY_DISCONNECT(146);
/*     */     private int occuranceType;
/*     */     
/*     */     EnumOccurrenceType(int occuranceType) {
/* 344 */       this.occuranceType = occuranceType;
/*     */     }
/*     */     
/*     */     public int getOccuranceType() {
/* 348 */       return this.occuranceType;
/*     */     }
/*     */   }
/*     */   
/*     */   public enum EMPegasusV2OccurrenceType
/*     */   {
/* 354 */     HARDWARE_FAILURE(20),
/* 355 */     PHONE_LINE_STATUS(21),
/* 356 */     ALARM_PANEL_RETURN_STATUS(22),
/* 357 */     DUAL_MONITORING_STATUS(23),
/* 358 */     MAIN_POWER_SUPPLY_STATUS(28),
/* 359 */     ALRAM_PANEL_COMM_STATUS(29),
/* 360 */     GSM_JAMMER_STATUS(30),
/* 361 */     GSM_SIGNAL_LEVEL_BELOW_MIN(31),
/* 362 */     BATTERY_VOLTAGE_BELOW_MINIMUM(32),
/* 363 */     BATTERY_CHARGER_STATUS(33),
/* 364 */     ALARM_PANEL_CONNECTION_OPERATION_MODE(34),
/* 365 */     ALARM_PANEL_CONNECTION_STATUS(35),
/* 366 */     ALARM_PANEL_COMM_TEST_STATUS(36),
/* 367 */     TELEPHONE_LINE_COMM_TEST_STATUS(37),
/* 368 */     ETHERNET_IFACE_TEST_STATUS(38),
/* 369 */     MODEM_IFACE_STATUS_0(39),
/* 370 */     MODEM_IFACE_STATUS_1(40),
/* 371 */     MODEM_IFACE_STATUS_2(41),
/* 372 */     MODEM_IFACE_STATUS_3(42),
/* 373 */     TAMPER_DETECTION(43),
/* 374 */     WIFI_IFACE_TEST_STATUS(44),
/* 375 */     NTP_SYNC_DATA(47),
/* 376 */     OTA_STATUS(48);
/*     */     private int occuranceType;
/*     */     
/*     */     EMPegasusV2OccurrenceType(int occuranceType) {
/* 380 */       this.occuranceType = occuranceType;
/*     */     }
/*     */     
/*     */     public int getOccuranceType() {
/* 384 */       return this.occuranceType;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public enum EnumMessagePriority
/*     */   {
/* 391 */     HIGH(0),
/* 392 */     AVERAGE(1),
/* 393 */     LOW(2);
/*     */     private int priority;
/*     */     
/*     */     EnumMessagePriority(int priority) {
/* 397 */       this.priority = priority;
/*     */     }
/*     */     
/*     */     public int getPriority() {
/* 401 */       return this.priority;
/*     */     }
/*     */   }
/*     */   
/*     */   public enum EnumPartitionScheme {
/* 406 */     NENHUM(0),
/* 407 */     ADICIONAR(1),
/* 408 */     SUBSTITUIR(2);
/*     */     private int partitionScheme;
/*     */     
/*     */     EnumPartitionScheme(int partitionScheme) {
/* 412 */       this.partitionScheme = partitionScheme;
/*     */     }
/*     */     
/*     */     public int getPartitionScheme() {
/* 416 */       return this.partitionScheme;
/*     */     }
/*     */   }
/*     */   
/*     */   public enum EnumDbTableIDs {
/* 421 */     TB_DEVICE_CONNECTION,
/* 422 */     TB_EVENT,
/* 423 */     TB_SIGNAL_LEVEL,
/* 424 */     TB_OCCURRENCE,
/* 425 */     TB_RECEIVED_COMM,
/* 426 */     TB_COMMAND,
/* 427 */     TB_PENDING_DATA_FIELDS;
/*     */   }
/*     */   
/*     */   public enum Platform
/*     */   {
/* 432 */     WINDOWS,
/* 433 */     LINUX,
/* 434 */     MAC,
/* 435 */     ARM,
/* 436 */     SOLARIS,
/* 437 */     EMPTY;
/*     */   }
/*     */   
/*     */   public enum MercuriusUDPCommStates {
/* 441 */     EXPECTED_REPLY,
/* 442 */     CFG_FILE_RECEIVE_INITIATED,
/* 443 */     CFG_FILE_RECEIVING,
/* 444 */     FILE_SENDING_INITIATED,
/* 445 */     FILE_SENDING,
/* 446 */     NEW_COMMAND_RESPONSE,
/* 447 */     COMMAND_PACKET_REPSONE,
/* 448 */     M2S_PACKET_PARSING,
/* 449 */     HANDLE_DASH_BOARD_BUFFER,
/* 450 */     FIRMWARE_CRC_RESPONSE,
/* 451 */     GEOFENCE_FILE_RECEIVING,
/* 452 */     GPS_FW_RESPONSE,
/* 453 */     AJS_READ_INITIATED,
/* 454 */     AJS_READ_COMMAND,
/* 455 */     AJS_READ_IN_PROGRESS,
/* 456 */     AJS_SEND_INITIATED,
/* 457 */     AJS_SEND_IN_PROGRESS,
/* 458 */     AJS_SINGLE_CRC32_REQUESTED,
/* 459 */     AJS_SINGLE_CRC32_READING,
/* 460 */     AJS_LOOKUP_DATA_INITIATED,
/* 461 */     AJS_LOOKUP_DATA_READ_IN_PROGRESS,
/* 462 */     ALL_CFG_CRC32_REQUESTED,
/* 463 */     ALL_CFG_CRC32_DATA_RECIEVING;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\Enums.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */