/*     */ package com.zeusServer.util;
/*     */ 
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.concurrent.ConcurrentHashMap;
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
/*     */ public class Defines
/*     */ {
/*     */   public static final String DEFAULT_ACCOUNT_SN = "00000000000000000000";
/*     */   public static final String WATCHDOG_SN = "00000000000000000001";
/*     */   public static final String GRIFFON_DEFAULT_ACCOUNT_SN = "0000000000";
/*     */   public static final String GRIFFON_WATCHDOG_SN = "0000000001";
/*     */   public static final String AVL_DEFAULT_ACCOUNT_SN = "0000000000";
/*     */   public static final String AVL_WATCHDOG_SN = "0000000001";
/*     */   public static final int DATA_SERVER_BACKLOG = 1000;
/*     */   public static final int MESSAGE_SERVER_BACKLOG = 50;
/*     */   public static final int MONITOR_SERVER_BACKLOG = 50;
/*     */   public static final int MAXIMUM_NUMBER_PENDING_IDENTIFICATION_PACKETS = 2500;
/*     */   public static final int MAXIMUM_NUMBER_ALIVE_PACKET_PROCESSSED_AT_SAME_TIME = 2500;
/*     */   public static final String DERBY_SERVICE_NAME = "ZeusDerby";
/*     */   public static final String WATCHDOG_SERVICE_NAME = "ZeusServerWatchdog";
/*     */   public static final int MAXIMUM_RETIRES = 3;
/*  37 */   public static final DateFormat DTFORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
/*     */   
/*     */   public static final int TIME_WITHOUT_PRINT_DB_ERRORS_AFTER_SPACE_RECLAIM = 300000;
/*     */   
/*     */   public static final int MAX_DAYS_COMM_LOG_ENABLED = 30;
/*     */   
/*     */   public static final int MIN_TIME_BETWEEN_CYCLIC_PRINTS = 5000;
/*     */   
/*     */   public static final byte ACK = 6;
/*     */   
/*     */   public static final byte NACK = 21;
/*     */   
/*     */   public static final byte NACK_1 = 20;
/*     */   
/*     */   public static final byte NACK_2 = 19;
/*     */   
/*     */   public static final byte NACK_3 = 18;
/*     */   
/*     */   public static final byte NACK_4 = 17;
/*     */   
/*     */   public static final byte ESC = 27;
/*     */   
/*     */   public static final byte CRC_32_NACK = 22;
/*     */   
/*     */   public static final byte GRIFFON_CFG_CRC_32_NACK = 20;
/*     */   
/*     */   public static final byte GRIFFON_CFG_INVALID_NACK = 18;
/*     */   
/*     */   public static final short MODULE_NOT_REGISTERED = 224;
/*     */   
/*     */   public static final short MODULE_NOT_ENABLED = 225;
/*     */   
/*     */   public static final short MAX_READ_COUNT = 240;
/*     */   
/*     */   public static final short MAX_CHUNK_RETRIES = 3;
/*     */   
/*     */   public static final int SERIAL_MULTIPLEXER_COMMUNICATION_TIMEOUT = 2500;
/*     */   
/*     */   public static final long TIME_BETWEEN_ATTEMPTS_OPEN_SERIAL_PORT = 15000L;
/*     */   
/*     */   public static final long THREAD_WATCHDOG_TIMEOUT = 900000L;
/*     */   
/*     */   public static final int NUMBER_BYTES_CONTACT_ID_EVENT = 8;
/*     */   
/*     */   public static final String DEFAULT_DB_BACKUP_PATH = "/backup_db";
/*     */   
/*     */   public static final int MSG_SCK_TIMEOUT = 30000;
/*     */   
/*     */   public static final int NEW_COMMAND_AVAILABLE = 128;
/*     */   
/*     */   public static final int SCAN_SERIAL_PORTS = 129;
/*     */   
/*     */   public static final int RESTORE_DB_CMD = 134;
/*     */   
/*     */   public static final int DEVICE_DELETE_CMD = 135;
/*     */   
/*     */   public static final int DISCONNECT_MODULES_CMD = 136;
/*     */   
/*     */   public static final int DB_BACKUP = 137;
/*     */   
/*     */   public static final int DB_CLEANUP = 144;
/*     */   
/*     */   public static final int GRIFFON_MOBILE_MSG_CLIENT = 145;
/*     */   
/*     */   public static final int GRIFFON_MOBILE_MSG_CLIENT_PING = 146;
/*     */   
/*     */   public static final int GRIFFON_MOBILE_MSG_CLIENT_ADDED = 147;
/*     */   
/*     */   public static final int GRIFFON_MOBILE_MSG_CLIENT_REMOVED = 148;
/*     */   
/*     */   public static final int GRIFFON_MOBILE_MSG_CLIENT_DATA_UPDATED = 149;
/*     */   
/*     */   public static final int GRIFFON_MOBILE_COMMAND_FAILED = 150;
/*     */   
/*     */   public static final int ZEUS_EVENTS_CLEAN_UP = 152;
/*     */   
/*     */   public static final int PEGASUS_MOBILE_MSG_CLIENT = 113;
/*     */   
/*     */   public static final int PEGASUS_MOBILE_MSG_CLIENT_PING = 114;
/*     */   
/*     */   public static final int PEGASUS_MOBILE_MSG_CLIENT_ADDED = 115;
/*     */   
/*     */   public static final int PEGASUS_MOBILE_MSG_CLIENT_REMOVED = 116;
/*     */   
/*     */   public static final int PEGASUS_MOBILE_MSG_CLIENT_DATA_UPDATED = 117;
/*     */   
/*     */   public static final int PEGASUS_MOBILE_COMMAND_FAILED = 118;
/*     */   
/*     */   public static final int INTIMATE_BUZZER = 153;
/*     */   
/*     */   public static final int LOGIN_PACKET_IDENTIFIER = 130;
/*     */   
/*     */   public static final int ZEUS_UI_PING = 131;
/*     */   
/*     */   public static final int ZEUS_UI_LOGOUT = 132;
/*     */   
/*     */   public static final int ZEUS_LOGIN_RESPONSE = 133;
/*     */   
/*     */   public static final int MESSAGE_LOG_ID = 144;
/*     */   
/*     */   public static final int EVENT_LOG_ID = 145;
/*     */   
/*     */   public static final int MONITORING_LOG_ID = 146;
/*     */   
/*     */   public static final int PLAY_SOUND = 148;
/*     */   
/*     */   public static final int BEEP_SOUND = 1;
/*     */   
/*     */   public static final int BUZINA_SOUND = 2;
/*     */   
/*     */   public static final int RUNTIME_ID = 16;
/*     */   
/*     */   public static final int TASKS_RUNNING_ID = 17;
/*     */   
/*     */   public static final int PENDING_EVENTS_ID = 18;
/*     */   
/*     */   public static final int REG_MODULES_ID = 19;
/*     */   public static final int DISABLED_MODULES_ID = 20;
/*     */   public static final int CONNECTED_MODULES_ID = 21;
/*     */   public static final int CONNECTED_VIA_TCP_ID = 22;
/*     */   public static final int CONNECTED_VIA_UDP_ID = 23;
/*     */   public static final int INTERNET_STATUS_ID = 24;
/*     */   public static final int CSD_STATUS_ID = 25;
/*     */   public static final int MONITORING_STATION_STATUS_ID = 32;
/*     */   public static final int NO_MONITORING_STATION_STATUS_ID = 33;
/*     */   public static final int PRODUCT_ID = 34;
/*     */   public static final int PENDING_ALIVES_ID = 35;
/*     */   public static final int RUNTIME_LENGTH = 8;
/*     */   public static final int TASKS_RUNNING_LENGTH = 2;
/*     */   public static final int PENDING_EVENTS_LENGTH = 4;
/*     */   public static final int PENDING_ALIVES_LENGTH = 4;
/*     */   public static final int REG_MODULES_LENGTH = 4;
/*     */   public static final int DISABLED_MODULES_LENGTH = 4;
/*     */   public static final int CONNECTED_MODULES_LENGTH = 4;
/*     */   public static final int CONNECTED_VIA_TCP_LENGTH = 4;
/*     */   public static final int CONNECTED_VIA_UDP_LENGTH = 4;
/*     */   public static final int INTERNET_STATUS_LENGTH = 1;
/*     */   public static final int ZEUS_BOX_SELECTED_PRODUCT = 56;
/*     */   public static final int ZEUS_BOX_RESTART_SHUTDOWN = 57;
/*     */   public static final int ZEUS_BOX_EVENTS_PUSH_HEADER = 66;
/*     */   public static final int ZEUS_BOX_EVENTS_POP = 64;
/*     */   public static final int ZEUS_BOX_EVENTS_POP_HEADER = 65;
/*     */   public static final int EVENTS_LENGTH_HEADER = 71;
/*     */   public static final int DATE_HEADER = 72;
/*     */   public static final int TIME_HEADER = 73;
/*     */   public static final int DATE_LENGTH = 11;
/*     */   public static final int TIME_LENGTH = 8;
/*     */   public static final int ETH_STATUS = 80;
/*     */   public static final int LOW_BATTERY = 81;
/*     */   public static final int ZEUS_BOX_TAMPER = 82;
/*     */   public static final int ZEUS_BOX_AC_POWER = 83;
/*     */   public static final int ZEUS_BOX_HW_FAILURE = 84;
/*     */   public static final int ZEUS_BOX_LOW_DISK_SPACE = 85;
/*     */   public static final int ZEUS_BOX_OVER_TEMPERATURE = 86;
/*     */   public static final int LOGIN_SUCCESS = 6;
/*     */   public static final int LOGIN_FAILED = 21;
/*     */   public static final int LOGIN_USER_DISABLED = 22;
/*     */   public static final int LOGIN_USER_PERMISSION_DENIED = 23;
/*     */   public static final int LOGIN_PACKET_CRC_FAIL = 24;
/*     */   public static final int LOGIN_ERROR_OCCURRED = 25;
/*     */   public static final int LOGIN_USER_TYPE_NOT_ALLOWED = 32;
/*     */   public static final int REMOTE_ZEUS_LOGS_PERMISSION = 256;
/*     */   public static final int GRIFFON_NX_MONITOR_MESSAGE_MONITORING = 16;
/*     */   public static final int MERCURIUS_AVL_MONITOR_MESSAGE_MONITORING = 16;
/*     */   public static final int PEGASUS_MODULE_ID = 1;
/*     */   public static final int PEGASUS_MODEM_IMEI = 2;
/*     */   public static final int PEGASUS_MODEM_MODEL = 3;
/*     */   public static final int PEGASUS_GE865_MODEM_FIRMWARE_VERSION = 4;
/*     */   public static final int PEGASUS_SIMCARD_ICCID = 5;
/*     */   public static final int PEGASUS_SIMCARD_OPERATOR_NAME = 6;
/*     */   public static final int PEGASUS_SIMCARD_HARDWARE_FAILURE = 7;
/*     */   public static final int PEGASUS_CURRENT_SIMCARD_AND_APN = 8;
/*     */   public static final int PEGASUS_CURRENT_INTERFACE = 9;
/*     */   public static final int PEGASUS_CFG_FILE_CRC16 = 10;
/*     */   public static final int PEGASUS_MODULE_HW_DETAILS = 11;
/*     */   public static final int PEGASUS_MODULE_FIRMWARE_VERSION = 12;
/*     */   public static final int PEGASUS_MODULE_OPERATION_MODE = 13;
/*     */   public static final int PEGASUS_PHONE_LINE_STATUS = 14;
/*     */   public static final int PEGASUS_ALARM_PANEL_RETURN_STATUS = 15;
/*     */   public static final int PEGASUS_DUAL_MONITORING_STATUS = 16;
/*     */   public static final int PEGASUS_DIGITAL_INPUT_STATUS = 17;
/*     */   public static final int PEGASUS_MAIN_POWER_SUPPLY_STATUS = 18;
/*     */   public static final int PEGASUS_ALARM_PANEL_COMM_STATUS = 19;
/*     */   public static final int PEGASUS_GSM_JAMMER_STATUS = 20;
/*     */   public static final int PEGASUS_GSM_SIGNAL_LEVEL = 21;
/*     */   public static final int PEGASUS_BATTERY_VOLTAGE_LEVEL = 22;
/*     */   public static final int PEGASUS_BATTERY_CHARGE_PERCENTAGE = 23;
/*     */   public static final int PEGASUS_BATTERY_STATUS = 24;
/*     */   public static final int PEGASUS_ALARM_PANEL_CONNECTION_OPERATION_MODE = 25;
/*     */   public static final int PEGASUS_ALARM_PANEL_CONNECTION_STATUS = 26;
/*     */   public static final int PEGASUS_ALARM_PANEL_COMM_TEST_STATUS = 27;
/*     */   public static final int PEGASUS_TELEPHONE_LINE_COMM_TEST_STATUS = 28;
/*     */   public static final int PEGASUS_ETHERNET_INTERFACE_TEST_STATUS = 29;
/*     */   public static final int PEGASUS_MODEM_INTERFACE_TEST_STATUS = 30;
/*     */   public static final int PEGASUS_CONTACT_ID_EVENT = 31;
/*     */   public static final int PEGASUS_GSM_FREQ = 32;
/*     */   public static final int PEGASUS_PERIPHERAL_OFFLINE = 33;
/*     */   public static final int PEGASUS_LINE_SIMULATOR_STATUS = 34;
/*     */   public static final int PEGASUS_DOWNLOAD_FILE = 32769;
/*     */   public static final int PEGASUS_DOWNLOAD_FILE_AND_REBOOT = 32770;
/*     */   public static final int PEGASUS_UPLOAD_FILE = 32771;
/*     */   public static final int PEGASUS_REBOOT_MODULE = 32772;
/*     */   public static final int PEGASUS_CONTROL_ALARM_PANEL_CONNECTION = 32773;
/*     */   public static final int PEGASUS_CONTROL_REMOTE_DEBUG = 32774;
/*     */   public static final int PEGASUS_CONTROL_DIGITAL_OUTPUT = 32775;
/*     */   public static final int PEGASUS_FORCE_ETHERNET_AS_ACTIVE_INTERFACE = 32776;
/*     */   public static final int PEGASUS_FORCE_MODEM_AS_ACTIVE_INTERFACE = 32777;
/*     */   public static final int PEGASUS_CALLBACK_CONFIG_TOOL = 32778;
/*     */   public static final int PEGASUS_FORCE_WIFI_AS_ACTIVE_INTERFACE = 32779;
/*     */   public static final int PEGASUS_RESET_GPRS_DATA_COUNTER = 32780;
/*     */   public static final int PEGASUS_MODEM_FIRMWARE_UPGRADE_FOTA = 32781;
/*     */   public static final int PEGASUS_UPDATE_MODULE_RTC_TIME = 32782;
/*     */   public static final int PEGASUS_ARM_DISARM_SYSTEM = 32784;
/*     */   public static final int PEGASUS_ZONE_BYPASS_UNBYPASS = 32785;
/*     */   public static final int PEGASUS_READ_SIM_CARD_ICCID = 14;
/*     */   public static final int PEGASUS_V2_MODULE_ID = 1;
/*     */   public static final int PEGASUS_V2_MODEM_IMEI = 2;
/*     */   public static final int PEGASUS_V2_MODEM_MODEL = 3;
/*     */   public static final int PEGASUS_V2_GE865_MODEM_FIRMWARE_VERSION = 4;
/*     */   public static final int PEGASUS_V2_SIMCARD_ICCID = 5;
/*     */   public static final int PEGASUS_V2_SIMCARD_OPERATOR_NAME = 6;
/*     */   public static final int PEGASUS_V2_HARDWARE_FAILURE = 7;
/*     */   public static final int PEGASUS_V2_CURRENT_SIMCARD_AND_APN = 8;
/*     */   public static final int PEGASUS_V2_CURRENT_INTERFACE = 9;
/*     */   public static final int PEGASUS_V2_CFG_FILE_CRC32 = 10;
/*     */   public static final int PEGASUS_V2_MODULE_HW_DETAILS = 11;
/*     */   public static final int PEGASUS_V2_MODULE_FIRMWARE_VERSION = 12;
/*     */   public static final int PEGASUS_V2_MODULE_OPERATION_MODE = 13;
/*     */   public static final int PEGASUS_V2_PHONE_LINE_STATUS = 14;
/*     */   public static final int PEGASUS_V2_ALARM_PANEL_RETURN_STATUS = 15;
/*     */   public static final int PEGASUS_V2_DUAL_MONITORING_STATUS = 16;
/*     */   public static final int PEGASUS_V2_DIGITAL_INPUT_STATUS = 17;
/*     */   public static final int PEGASUS_V2_MAIN_POWER_SUPPLY_STATUS = 18;
/*     */   public static final int PEGASUS_V2_ALARM_PANEL_COMM_STATUS = 19;
/*     */   public static final int PEGASUS_V2_GSM_JAMMER_STATUS = 20;
/*     */   public static final int PEGASUS_V2_GSM_SIGNAL_LEVEL = 21;
/*     */   public static final int PEGASUS_V2_BATTERY_VOLTAGE_LEVEL = 22;
/*     */   public static final int PEGASUS_V2_BATTERY_CHARGE_PERCENTAGE = 23;
/*     */   public static final int PEGASUS_V2_BATTERY_STATUS = 24;
/*     */   public static final int PEGASUS_V2_ALARM_PANEL_CONNECTION_OPERATION_MODE = 25;
/*     */   public static final int PEGASUS_V2_ALARM_PANEL_CONNECTION_STATUS = 26;
/*     */   public static final int PEGASUS_V2_ALARM_PANEL_COMM_TEST_STATUS = 27;
/*     */   public static final int PEGASUS_V2_TELEPHONE_LINE_COMM_TEST_STATUS = 28;
/*     */   public static final int PEGASUS_V2_ETHERNET_INTERFACE_TEST_STATUS = 29;
/*     */   public static final int PEGASUS_V2_MODEM_INTERFACE_TEST_STATUS = 30;
/*     */   public static final int PEGASUS_V2_CONTACT_ID_EVENT = 31;
/*     */   public static final int PEGASUS_V2_TAMPER_DETECTION = 32;
/*     */   public static final int PEGASUS_V2_WIFI_MODEL = 33;
/*     */   public static final int PEGASUS_V2_WIFI_FIRMWARE = 34;
/*     */   public static final int PEGASUS_V2_WIFI_ACCESS_POINT = 35;
/*     */   public static final int PEGASUS_V2_WIFI_INTERFACE_TEST_STATUS = 36;
/*     */   public static final int PEGASUS_V2_WIFI_SIGNAL_LEVEL = 37;
/*     */   public static final int PEGASUS_V2_SIM_CARD_0_STATUS = 38;
/*     */   public static final int PEGASUS_V2_SIM_CARD_1_STATUS = 39;
/*     */   public static final int PEGASUS_V2_KBPS_DATA_TRANSFER_VIA_GPRS = 40;
/*     */   public static final int PEGASUS_V2_OTA_STATUS = 41;
/*     */   public static final int PEGASUS_V2_BATTERY_OVER_TEMPARATURE = 42;
/*     */   public static final int PEGASUS_V2_BATTERY_OVER_TIMECHARGE = 43;
/*     */   public static final int PEGASUS_V2_NTP_UPDATE_REQUIRED = 44;
/*     */   public static final int PEGASUS_V2_MODEM_IMSI = 45;
/*     */   public static final int PEGASUS_V2_PGM_STATUS = 46;
/*     */   public static final int PEGASUS_V2_LONGITUDE = 47;
/*     */   public static final int PEGASUS_V2_LATITUDE = 48;
/*     */   public static final int PEGASUS_V2_ALTITUDE = 49;
/*     */   public static final int PEGASUS_V2_3I_LOCATE = 50;
/*     */   public static final int PEGASUS_V2_TIMEZONE = 51;
/*     */   public static final int PEGASUS_V2_ZONE_1_STATUS = 52;
/*     */   public static final int PEGASUS_V2_ZONE_2_STATUS = 53;
/*     */   public static final int PEGASUS_V2_ZONE_3_STATUS = 54;
/*     */   public static final int PEGASUS_V2_ZONE_4_STATUS = 55;
/*     */   public static final int PEGASUS_V2_SYSTEM_STATUS = 56;
/*     */   public static final int PEGASUS_V2_KEYFOB_PANIC = 57;
/*     */   public static final int PEGASUS_V2_BATTERY_DISCONNECTED = 64;
/*     */   public static final int PEGASUS_V2_KEYFOB_COMM_TEST = 58;
/*     */   public static final int PEGASUS_V2_KEYFOB_LOW_BATTERY = 59;
/*     */   public static final int PEGASUS_V2_SYSTEM_IN_ALARM = 60;
/*     */   public static final int PEGASUS_V2_IPIC = 61;
/*     */   public static final int PEGASUS_V2_BATT_PRESENT_NOT_CONFIGURED = 62;
/*     */   public static final int PEGASUS_V2_COMMAND_PACKET = 4;
/*     */   public static final int PEGASUS_V2_GSM_DATA_CARRIER = 63;
/*     */   public static final float PEGASUS_CONVERSION_FACTOR_FOR_BATTERY_VOLTAGE_MEASUREMENTS = 0.075F;
/*     */   public static final int GRIFFON_ZONE_REPORT = 1;
/*     */   public static final int GRIFFON_PARTITION_EVENTS = 2;
/*     */   public static final int GRIFFON_SYSTEM = 3;
/*     */   public static final int GRIFFON_MISCELLANEOUS = 4;
/*     */   public static final int GRIFFON_ACCESS = 5;
/*     */   public static final int GRIFFON_MODULE_ID = 80;
/*     */   public static final int GRIFFON_MODULE_FW_VERSION = 81;
/*     */   public static final int GRIFFON_MODULE_HW_DTLS = 82;
/*     */   public static final int GRIFFON_BATTERY_STATUS = 83;
/*     */   public static final int GRIFFON_CURRENT_INTERFACE = 84;
/*     */   public static final int GRIFFON_CFG_CRC32 = 85;
/*     */   public static final int GRIFFON_MODEM_IMEI = 86;
/*     */   public static final int GRIFFON_MODEM_MODEL = 87;
/*     */   public static final int GRIFFON_GSM_MODEM_FW_VERSION = 88;
/*     */   public static final int GRIFFON_GSM_SIGNAL_LEVEL = 89;
/*     */   public static final int GRIFFON_GSM_MODEM_IFACE_TEST_STATUS = 90;
/*     */   public static final int GRIFFON_GSM_JAMMER_STATUS = 91;
/*     */   public static final int GRIFFON_OTA_STATUS = 92;
/*     */   public static final int GRIFFON_DATA_TRANSFERED_GPRS = 93;
/*     */   public static final int GRIFFON_CURRENT_SIM_APN = 94;
/*     */   public static final int GRIFFON_SIMCARD_ICCID = 95;
/*     */   public static final int GRIFFON_SIMCARD_OPERATOR = 96;
/*     */   public static final int GRIFFON_SIM_IMSI = 97;
/*     */   public static final int GRIFFON_SIM_CARD_STATUS = 98;
/*     */   public static final int GRIFFON_POSITION = 99;
/*     */   public static final int GRIFFON_TELEPHONE_LINE_COMM_TEST_STATUS = 100;
/*     */   public static final int GRIFFON_ETH_IFACE_TEST_STATUS = 101;
/*     */   public static final int GRIFFON_WIFI_MODEL = 102;
/*     */   public static final int GRIFFON_WIFI_FW = 103;
/*     */   public static final int GRIFFON_WIFI_ACCESS_POINT = 104;
/*     */   public static final int GRIFFON_WIFI_IFACE_COMM_TEST_STATUS = 105;
/*     */   public static final int GRIFFON_WIFI_SIGNAL_LEVEL = 106;
/*     */   public static final int GRIFFON_SYNC_SERVER_TIME = 107;
/*     */   public static final int GRIFFON_ALL_CFG_CRC32 = 108;
/*     */   public static final int GRIFFON_DASHBOARD_STATUS = 109;
/*     */   public static final int GRIFFON_EVENTS_LOG_UPLOAD = 110;
/*     */   public static final int GRIFFON_ZONE_STATUS_UPDATE = 111;
/*     */   public static final int GRIFFON_ANALOG_PGM_STATUS = 112;
/*     */   public static final int GRIFFON_AUX_STATUS = 113;
/*     */   public static final int GRIFFON_EB_TAMPER_STATUS = 114;
/*     */   public static final int GRIFFON_TEMPERATURE = 115;
/*     */   public static final int GRIFFON_KP_FW_CRC32 = 116;
/*     */   public static final int GRIFFON_SINGLE_FILE_CRC32_AUTO_ENROLLMENT = 117;
/*     */   public static final int GRIFFON_DIGITAL_PGM_BUFFER = 118;
/*     */   public static final int GRIFFON_RECORDED_FILES_LOOKUP_REQUEST = 120;
/*     */   public static final int GRIFFON_LOGOUT = 32800;
/*     */   public static final byte GRIFFON_ACK_2 = 2;
/*     */   public static final int GRIFFON_V1_MAX_FILES = 37;
/*     */   public static final int GRIFFON_V2_MAX_FILES = 40;
/*     */   public static final int GRIFFON_REBOOT_MODULE = 32769;
/*     */   public static final int GRIFFON_CONTROL_REMOTE_DEBUG = 32770;
/*     */   public static final int GRIFFON_CALLBACK_CONFIGURATION_TOOL = 32771;
/*     */   public static final int GRIFFON_FORCE_ACTIVE_INTERFACE = 32772;
/*     */   public static final int GRIFFON_SEND_FILE = 32773;
/*     */   public static final int GRIFFON_RECEIVE_FILE = 32774;
/*     */   public static final int GRIFFON_RESET_GRPS_DATA_COUNTER = 32775;
/*     */   public static final int GRIFFON_UPDATE_RTC_TIME = 32777;
/*     */   public static final int GRIFFON_BYPASS_UNBYPASS = 32778;
/*     */   public static final int GRIFFON_CONTROL_PGM = 32779;
/*     */   public static final int GRIFFON_CONTROL_PARTITION = 32780;
/*     */   public static final int GRIFFON_CONTROL_WALK_TEST = 32781;
/*     */   public static final int GRIFFON_RUN_SCHEDULE = 32782;
/*     */   public static final int GRIFFON_PLAY_AUDIO_FILE = 32783;
/*     */   public static final int GRIFFON_BELL_TEST = 32784;
/*     */   public static final int GRIFFON_UPDATE_STATUS = 32785;
/*     */   public static final int GRIFFON_ACK_TROUBLES = 32787;
/*     */   public static final int GRIFFON_ENABLE_DISABLE_PGM_DIGITAL_BUFFER = 32788;
/*     */   public static final int GRIFFON_UPDATE_SINGLE_CRC32 = 32799;
/*     */   public static final int GRIFFON_UPDATE_EM_FW_DATA = 32800;
/*     */   public static final int GRIFFON_VOICE_LIST_FILE_ID = 21;
/*     */   public static final int GRIFFON_EACH_RECORD_FILE_DATA_LENGTH = 16;
/*     */   public static final int GRIFFON_ALIVE_PACKET_RECEIVED = 611;
/*     */   public static final int GRIFFON_DASHBOARD_BUFFER_LENGTH = 203;
/*     */   public static final int MERCURIUS_STD_FILE_HEADER = 12;
/*     */   public static final int MERCURIUS_SIGNATURE_16 = 16;
/*     */   public static final int MERCURIUS_MAX_FILES = 13;
/*     */   public static final int MERCURIUS_DEVICE_ID = 1;
/*     */   public static final int MERCURIUS_THIRD_PARTY_MODULES_DETAILS = 2;
/*     */   public static final int MERCURIUS_DEVICE_FIRMWARE_VERSION = 3;
/*     */   public static final int MERCURIUS_MODEM_FW_VERSION = 4;
/*     */   public static final int MERCURIUS_GSM_MODULE_IMEI = 5;
/*     */   public static final int MERCURIUS_SIMCARD_ICCID = 6;
/*     */   public static final int MERCURIUS_SIMCARD_IMSI = 7;
/*     */   public static final int MERCURIUS_SIMCARD_OPERATOR_NAME = 8;
/*     */   public static final int MERCURIUS_GPRS_INTERFACE_TEST_STATUS = 9;
/*     */   public static final int MERCURIUS_CURRENT_INTERFACE = 10;
/*     */   public static final int MERCURIUS_CURRENT_SIMCARD_AND_APN = 11;
/*     */   public static final int MERCURIUS_GSM_SIGNAL_LEVEL = 12;
/*     */   public static final int MERCURIUS_BATTERY_CHARGE_LEVEL = 13;
/*     */   public static final int MERCURIUS_GPRS_TOTAL_DATA_VOLUME = 14;
/*     */   public static final int MERCURIUS_SIM_CARD_STATUS = 15;
/*     */   public static final int MERCURIUS_TIME_SYNC = 16;
/*     */   public static final int MERCURIUS_GEOFENCE_CRC32 = 17;
/*     */   public static final int MERCURIUS_PGM_STATUS = 18;
/*     */   public static final int MERCURIUS_GPS_FW_VERSION = 19;
/*     */   public static final int MERCURIUS_CURRENT_SATILITES_COUNT = 20;
/*     */   public static final int MERCURIUS_HW_DETAILS = 21;
/*     */   public static final int MERCURIUS_DI_DATA = 22;
/*     */   public static final int MERCURIUS_SCHEDULER_EVENT = 100;
/*     */   public static final int MERCURIUS_ANALOG_INPUT_EVENT = 101;
/*     */   public static final int MERCURIUS_DIGITAL_INPUT_EVENT = 102;
/*     */   public static final int MERCURIUS_TRACKING_EVENT = 103;
/*     */   public static final int MERCURIUS_VEHICLE_SAFETY_EVENT = 104;
/*     */   public static final int MERCURIUS_ROUTE_PATH_EVENT = 105;
/*     */   public static final int MERCURIUS_GEOFENCE_EVENT = 106;
/*     */   public static final int MERCURIUS_HARDWARE_FAILURE_EVENT = 107;
/*     */   public static final int MERCURIUS_POWER_STATUS_EVENT = 108;
/*     */   public static final int MERCURIUS_GPS_STATUS_EVENT = 109;
/*     */   public static final int MERCURIUS_TAMPER_STATUS_EVENT = 110;
/*     */   public static final int MERCURIUS_GSM_SIGNAL = 112;
/*     */   public static final int MERCURIUS_INCOMING_CALL_EVENT = 113;
/*     */   public static final int MERCURIUS_INCOMING_SMS_EVENT = 114;
/*     */   public static final int MERCURIUS_DOWNLOAD_FILE = 32769;
/*     */   public static final int MERCURIUS_DOWNLOAD_FILE_REBOOT = 32770;
/*     */   public static final int MERCURIUS_UPLOAD_CFG_FILE = 32773;
/*     */   public static final int MERCURIUS_REBOOT_MODULE = 32774;
/*     */   public static final int MERCURIUS_CONTROL_REMOTE_DEBUG = 32775;
/*     */   public static final int MERCURIUS_FORCE_MODEM_AS_ACTIVE_INTERFACE = 32776;
/*     */   public static final int MERCURIUS_CALLBACK_CONFIGURATION_TOOL = 32777;
/*     */   public static final int MERCURIUS_RESET_GRPS_DATA_COUNTER = 32778;
/*     */   public static final int MERCURIUS_MODEM_FIRMWARE_UPGRADE_FOTA = 32779;
/*     */   public static final int MERCURIUS_UPDATE_MODULE_RTC_TIME = 32780;
/*     */   public static final int MERCURIUS_CURRENT_ROUTE_PATH = 32781;
/*     */   public static final int MERCURIUS_DIGITAL_OUTPUT_CONTROL = 32782;
/*     */   public static final int MERCURIUS_TRACK_ON_DEMAND = 32783;
/*     */   public static final int MERCURIUS_CLEAR_JOURNEY_RUNNING_TIME = 32784;
/*     */   public static final int MERCURIUS_CLEAR_SMS_COUNTER = 32785;
/*     */   public static final int MERCURIUS_CLEAR_TRACKING_GPRS_SMS_BUFFER = 32786;
/*     */   public static final int MERCURIUS_UPDATE_GPS_FW_VERSION = 32787;
/*     */   public static final int MERCURIUS_REQUEST_GEOFENCEDATA = 32788;
/*     */   public static final int MERCURIUS_REQUEST_AUDIO_JS_SINGLE_CRC32 = 32790;
/*     */   public static final int MERCURIUS_REQUEST_AUDIO_JS_LOOKUP_DATA = 32791;
/*     */   public static final int MERCURIUS_INITIATE_AUDIO_FILE_SEND = 32792;
/*     */   public static final int MERCURIUS_INITIATE_JS_FILE_SEND = 32793;
/*     */   public static final int MERCURIUS_REQUEST_AUDIO_FILE = 32794;
/*     */   public static final int MERCURIUS_REQUEST_JS_FILE = 32795;
/*     */   public static final int MERCURIUS_REQUEST_ALL_FILES_CRC32 = 32796;
/*     */   public static final int MERCURIUS_AUDIO_FILE_START_COUNTER = 71098;
/*     */   public static final int MERCURIUS_JS_FILE_START_COUNTER = 74098;
/*     */   public static final int NEW_EVENT = 1;
/*     */   public static final int RESTORE_EVENT = 3;
/*     */   public static final int DB_RETRIES = 3;
/*     */   public static final int DB_RETRY_TIME = 5000;
/*     */   public static final long MINIMUM_TIME_BETWEEN_UPDATES_FIELD_LAST_COMMUNICATION = 60000L;
/*     */   public static final int MAXIMUM_RETRIES_COMMAND_EXECUTION = 3;
/*     */   public static final long INITIAL_DATA_CONNECTION_IDLE_TIMEOUT = 15000L;
/*     */   public static final long TIME_BETWEEN_REQUESTS_COMMAND_RETRIEVE = 60000L;
/*     */   public static final long MINIMUM_INACTIVITY_TIME = 10000L;
/*     */   public static final int PEGASUS_MODULE_COMMUNICATION_TIMEOUT = 30000;
/*     */   public static final int MAXIMUM_DIFF_BETWEEN_CLOCKS_SERVER_AND_MODULE = 150;
/*     */   public static final String MONITORING_STATION_ON = "images/Monitoring_Station_On.png";
/*     */   public static final String MONITORING_STATION_OFF = "images/Monitoring_Station_Off.png";
/*     */   public static final String INTERNET_ON = "images/Internet_On.png";
/*     */   public static final String INTERNET_OFF = "images/Internet_Off.png";
/*     */   public static final String CSD_ON = "images/CSD_On.png";
/*     */   public static final String CSD_OFF = "images/CSD_Off.png";
/*     */   public static final String SIGNAL_0 = "images/signal_0.png";
/*     */   public static final String SIGNAL_1 = "images/signal_1.png";
/*     */   public static final String SIGNAL_2 = "images/signal_2.png";
/*     */   public static final String SIGNAL_3 = "images/signal_3.png";
/*     */   public static final String SIGNAL_4 = "images/signal_4.png";
/*     */   public static final int ALARM_PANEL_COMM_TEST_FAIL = 1;
/*     */   public static final int TELEPHONE_LINE_COMM_TEST_FAIL = 2;
/*     */   public static final int ETH_INTERFACE_COMM_TEST_FAIL = 3;
/*     */   public static final int MODEM_INTERFACE_COMM_TEST_1_1_FAIL = 4;
/*     */   public static final int MODEM_INTERFACE_COMM_TEST_1_2_FAIL = 5;
/*     */   public static final int MODEM_INTERFACE_COMM_TEST_2_1_FAIL = 6;
/*     */   public static final int MODEM_INTERFACE_COMM_TEST_2_2_FAIL = 7;
/*     */   public static final int WIFI_INTERFACE_COMM_TEST_AP_1_FAIL = 8;
/*     */   public static final int WIFI_INTERFACE_COMM_TEST_AP_2_FAIL = 9;
/*     */   public static final int GRIFFON_HW_FAIL_CAN = 2;
/*     */   public static final int GRIFFON_HW_FAIL_EEPROM = 4;
/*     */   public static final int GRIFFON_HW_FAIL_SDCARD = 8;
/*     */   public static final int GRIFFON_HW_FAIL_GSM_MODEM = 16;
/*     */   public static final int GRIFFON_HW_FAIL_WIFI = 32;
/*     */   public static final int GRIFFON_HW_FAIL_ETH = 64;
/*     */   public static final int GRIFFON_HW_FAIL_PHONE_DIALER = 128;
/*     */   public static final int GRIFFON_HW_FAIL_BLUETOOTH = 256;
/*     */   public static final int GRIFFON_HW_FAIL_ZIGBEE = 512;
/*     */   public static final int GRIFFON_HW_FAIL_WIEGAND1 = 1024;
/*     */   public static final int GRIFFON_HW_FAIL_WIEGAND2 = 2048;
/*     */   public static final int GRIFFON_HW_FAIL_UART = 4096;
/*     */   public static final int GRIFFON_HW_FAIL_KEYFOB_RECEIVER_ON_USB = 8192;
/*     */   public static final int GRIFFON_HW_FAIL_ACCELEROMETER = 16384;
/*     */   public static final int GRIFFON_HW_FAIL_PROXIMITY = 32768;
/*     */   public static final int GRIFFON_HW_FAIL_WATCHDOG_RESET = 65536;
/*     */   public static final int GRIFFON_HW_FAIL_SIMCARD1 = 131072;
/*     */   public static final int GRIFFON_HW_FAIL_SIMCARD2 = 262144;
/*     */   public static final int GRIFFON_HW_FAIL_CANBUS = 524288;
/* 507 */   public static final ConcurrentHashMap<Integer, String> TIMEZONE_NAMES = new ConcurrentHashMap<>(33);
/*     */   static {
/* 509 */     TIMEZONE_NAMES.put(Integer.valueOf(-720), "Etc/GMT-12");
/* 510 */     TIMEZONE_NAMES.put(Integer.valueOf(-660), "US/Samoa");
/* 511 */     TIMEZONE_NAMES.put(Integer.valueOf(-600), "US/Hawaii");
/* 512 */     TIMEZONE_NAMES.put(Integer.valueOf(-540), "US/Alaska");
/* 513 */     TIMEZONE_NAMES.put(Integer.valueOf(-480), "America/Los_Angeles");
/* 514 */     TIMEZONE_NAMES.put(Integer.valueOf(-420), "Mexico/BajaSur");
/* 515 */     TIMEZONE_NAMES.put(Integer.valueOf(-360), "America/Chicago");
/* 516 */     TIMEZONE_NAMES.put(Integer.valueOf(-300), "America/Bogota");
/* 517 */     TIMEZONE_NAMES.put(Integer.valueOf(-240), "Canada/Atlantic");
/* 518 */     TIMEZONE_NAMES.put(Integer.valueOf(-210), "America/St_Johns");
/* 519 */     TIMEZONE_NAMES.put(Integer.valueOf(-180), "America/Sao_Paulo");
/* 520 */     TIMEZONE_NAMES.put(Integer.valueOf(-120), "Atlantic/South_Georgia");
/* 521 */     TIMEZONE_NAMES.put(Integer.valueOf(-60), "Atlantic/Cape_Verde");
/* 522 */     TIMEZONE_NAMES.put(Integer.valueOf(0), "GMT");
/* 523 */     TIMEZONE_NAMES.put(Integer.valueOf(60), "CET");
/* 524 */     TIMEZONE_NAMES.put(Integer.valueOf(120), "EET");
/* 525 */     TIMEZONE_NAMES.put(Integer.valueOf(180), "EAT");
/* 526 */     TIMEZONE_NAMES.put(Integer.valueOf(210), "Iran");
/* 527 */     TIMEZONE_NAMES.put(Integer.valueOf(240), "Etc/GMT-4");
/* 528 */     TIMEZONE_NAMES.put(Integer.valueOf(270), "Asia/Kabul");
/* 529 */     TIMEZONE_NAMES.put(Integer.valueOf(300), "Etc/GMT-5");
/* 530 */     TIMEZONE_NAMES.put(Integer.valueOf(330), "Asia/Calcutta");
/* 531 */     TIMEZONE_NAMES.put(Integer.valueOf(345), "Asia/Katmandu");
/* 532 */     TIMEZONE_NAMES.put(Integer.valueOf(360), "Etc/GMT-6");
/* 533 */     TIMEZONE_NAMES.put(Integer.valueOf(390), "Asia/Rangoon");
/* 534 */     TIMEZONE_NAMES.put(Integer.valueOf(420), "Etc/GMT-7");
/* 535 */     TIMEZONE_NAMES.put(Integer.valueOf(480), "CTT");
/* 536 */     TIMEZONE_NAMES.put(Integer.valueOf(540), "JST");
/* 537 */     TIMEZONE_NAMES.put(Integer.valueOf(570), "ACT");
/* 538 */     TIMEZONE_NAMES.put(Integer.valueOf(600), "AET");
/* 539 */     TIMEZONE_NAMES.put(Integer.valueOf(660), "Etc/GMT-11");
/* 540 */     TIMEZONE_NAMES.put(Integer.valueOf(720), "Etc/GMT-12");
/* 541 */     TIMEZONE_NAMES.put(Integer.valueOf(780), "MIT");
/*     */   }
/*     */ 
/*     */   
/* 545 */   public static String[] SCHEMA_NAMES = new String[] { "PEGASUS", "GRIFFON", "MERCURIUS", "ZEUSSETTINGS" };
/* 546 */   public static int[] T_COUNT = new int[] { 12, 29, 15, 8 };
/* 547 */   public static int[] P_COUNT = new int[] { 55, 50, 46, 9 };
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\Defines.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */