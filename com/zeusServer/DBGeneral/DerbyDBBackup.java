/*     */ package com.zeusServer.DBGeneral;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import com.zeusServer.util.Main;
/*     */ import com.zeusServer.util.ZeusServerCfg;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.Calendar;
/*     */ import jcifs.smb.NtlmPasswordAuthentication;
/*     */ import jcifs.smb.SmbException;
/*     */ import jcifs.smb.SmbFile;
/*     */ import org.hyperic.sigar.SigarException;
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
/*     */ public class DerbyDBBackup
/*     */   implements Runnable
/*     */ {
/*     */   public static long wdt;
/*     */   public static boolean backupModeActivated;
/*     */   public boolean flag;
/*     */   public boolean isFirstTime = false;
/*     */   
/*     */   public DerbyDBBackup(boolean flag, boolean isFirstTime) {
/*  42 */     this.flag = flag;
/*  43 */     this.isFirstTime = isFirstTime;
/*  44 */     wdt = System.currentTimeMillis() + 900000L;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  49 */     long lastThreadExecution = 0L;
/*     */     
/*  51 */     File dbAbsFolder = null;
/*  52 */     SmbFile rootFolder = null;
/*     */     
/*  54 */     File root = new File("");
/*     */     try {
/*  56 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Database_backup_task_initialized"), Enums.EnumMessagePriority.LOW, null, null);
/*  57 */       if (ZeusServerCfg.getInstance().getDbBackupDirectory() != null) {
/*     */         
/*  59 */         if (!ZeusServerCfg.getInstance().isNetworkAuth()) {
/*  60 */           if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/*     */             try {
/*  62 */               dbAbsFolder = new File(ZeusServerCfg.getInstance().getDbBackupDirectory());
/*  63 */             } catch (Exception ex) {
/*  64 */               dbAbsFolder = null;
/*     */             } 
/*     */           }
/*     */           try {
/*  68 */             if (dbAbsFolder != null && 
/*  69 */               !dbAbsFolder.exists()) {
/*  70 */               dbAbsFolder.mkdirs();
/*     */             }
/*     */           }
/*  73 */           catch (Exception ex) {
/*  74 */             dbAbsFolder = null;
/*     */           } 
/*     */         } 
/*  77 */         if (ZeusServerCfg.getInstance().isNetworkAuth()) {
/*     */           try {
/*  79 */             NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(ZeusServerCfg.getInstance().getNetworkDomain(), ZeusServerCfg.getInstance().getNetworkUser(), ZeusServerCfg.getInstance().getNetworkPassword());
/*  80 */             String fPath = "smb://" + ZeusServerCfg.getInstance().getDbBackupDirectory();
/*  81 */             rootFolder = new SmbFile(fPath, auth);
/*  82 */             if (!rootFolder.exists()) {
/*  83 */               rootFolder.mkdirs();
/*     */             }
/*  85 */           } catch (MalformedURLException|SmbException ex) {
/*     */             
/*  87 */             ex.printStackTrace();
/*     */           } 
/*     */           
/*  90 */           if (rootFolder != null) {
/*     */             try {
/*  92 */               if (rootFolder.exists() && rootFolder.isDirectory()) {
/*  93 */                 Calendar dtLastBackup = null;
/*     */                 
/*  95 */                 for (SmbFile dirBackup : rootFolder.listFiles()) {
/*  96 */                   if (dirBackup.isFile() && dirBackup.getName().endsWith("System.zip")) {
/*  97 */                     Calendar dtBackup = convert2Date(dirBackup.getName());
/*  98 */                     if (dtBackup != null) {
/*  99 */                       if (dtLastBackup == null) {
/* 100 */                         dtLastBackup = dtBackup;
/* 101 */                       } else if (dtLastBackup.before(dtBackup)) {
/* 102 */                         dtLastBackup = dtBackup;
/*     */                       } 
/*     */                     }
/*     */                   } 
/*     */                 } 
/*     */                 
/* 108 */                 if (dtLastBackup == null) {
/* 109 */                   lastThreadExecution = 0L;
/*     */                 } else {
/* 111 */                   lastThreadExecution = dtLastBackup.getTimeInMillis() + 1000L;
/*     */                 } 
/*     */               } 
/* 114 */             } catch (SmbException ex) {
/* 115 */               File file = new File(root.getAbsolutePath() + "/backup_db");
/* 116 */               file.mkdirs();
/* 117 */               lastThreadExecution = 0L;
/*     */             } 
/*     */           } else {
/* 120 */             File file = new File(root.getAbsolutePath() + "/backup_db");
/* 121 */             file.mkdirs();
/* 122 */             lastThreadExecution = 0L;
/*     */           } 
/*     */         } else {
/* 125 */           File file; if (dbAbsFolder != null && dbAbsFolder.exists() && dbAbsFolder.isDirectory()) {
/* 126 */             file = dbAbsFolder;
/*     */           } else {
/* 128 */             file = new File(root.getAbsolutePath() + "/backup_db");
/*     */           } 
/*     */           
/* 131 */           if (file.exists() && file.isDirectory()) {
/* 132 */             Calendar dtLastBackup = null;
/*     */             
/* 134 */             int cnt = 0;
/* 135 */             for (File dirBackup : file.listFiles()) {
/* 136 */               if (dirBackup.isFile() && dirBackup.getName().endsWith("System.zip")) {
/* 137 */                 cnt++;
/* 138 */                 Calendar dtBackup = convert2Date(dirBackup.getName());
/* 139 */                 if (dtBackup != null) {
/* 140 */                   if (dtLastBackup == null) {
/* 141 */                     dtLastBackup = dtBackup;
/* 142 */                   } else if (dtLastBackup.before(dtBackup)) {
/* 143 */                     dtLastBackup = dtBackup;
/*     */                   } 
/*     */                 }
/*     */               } 
/*     */             } 
/* 148 */             if (file.listFiles() != null && (file.listFiles()).length > cnt) {
/* 149 */               for (File dirBackup : file.listFiles()) {
/* 150 */                 if (dirBackup.isDirectory()) {
/* 151 */                   Functions.deleteDirectory(dirBackup);
/*     */                 }
/*     */               } 
/*     */             }
/* 155 */             if (dtLastBackup == null) {
/* 156 */               lastThreadExecution = 0L;
/*     */             } else {
/* 158 */               lastThreadExecution = dtLastBackup.getTimeInMillis() + 1000L;
/*     */             } 
/*     */           } else {
/* 161 */             file.mkdirs();
/* 162 */             lastThreadExecution = 0L;
/*     */           } 
/*     */         } 
/*     */ 
/*     */         
/* 167 */         boolean startFlag = false;
/* 168 */         Enums.enumDbStatus tmp = Enums.enumDbStatus.NORMAL;
/* 169 */         boolean isNeedtoDisconnectDevices = true;
/* 170 */         while (this.flag) {
/* 171 */           Calendar nextCal = getNextValidCalendar(true);
/* 172 */           Calendar prevCal = getPrevValidCalendar(nextCal);
/* 173 */           if (lastThreadExecution > 0L && 
/* 174 */             lastThreadExecution < prevCal.getTimeInMillis()) {
/* 175 */             lastThreadExecution = 0L;
/* 176 */             isNeedtoDisconnectDevices = false;
/*     */           } 
/*     */           
/* 179 */           if (lastThreadExecution <= 0L && !GlobalVariables.mainTimerRoutineRunning && (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.NORMAL || Functions.isDbInCleanupState())) {
/*     */             try {
/* 181 */               String backupDir; tmp = Functions.isDbInCleanupState() ? Enums.enumDbStatus.CLEANUP : GlobalVariables.dbCurrentStatus;
/* 182 */               GlobalVariables.mainTimerStopped = true;
/* 183 */               if (isNeedtoDisconnectDevices && this.isFirstTime) {
/* 184 */                 Main.finishTasks(false);
/* 185 */                 this.isFirstTime = false;
/*     */               } 
/* 187 */               backupModeActivated = true;
/* 188 */               GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.AUTO_BACKUP;
/* 189 */               Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Executing_the_backup_of_the_original_database"), Enums.EnumMessagePriority.LOW, null, null);
/* 190 */               if (ZeusServerCfg.getInstance().isNetworkAuth()) {
/*     */                 try {
/* 192 */                   NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(ZeusServerCfg.getInstance().getNetworkDomain(), ZeusServerCfg.getInstance().getNetworkUser(), ZeusServerCfg.getInstance().getNetworkPassword());
/* 193 */                   String fPath = "smb://" + ZeusServerCfg.getInstance().getDbBackupDirectory();
/* 194 */                   rootFolder = new SmbFile(fPath, auth);
/* 195 */                   if (!rootFolder.exists()) {
/* 196 */                     rootFolder.mkdirs();
/*     */                   }
/* 198 */                   backupDir = rootFolder.getPath() + "/" + Functions.getCurrentDateFolder() + "$System/";
/* 199 */                 } catch (MalformedURLException|SmbException ex) {
/* 200 */                   rootFolder = null;
/* 201 */                   backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + "$System/";
/*     */                   
/* 203 */                   ex.printStackTrace();
/*     */                 } 
/* 205 */                 if (rootFolder == null) {
/* 206 */                   if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/* 207 */                     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_find_configured_backup_path") + ZeusServerCfg.getInstance().getDbBackupDirectory() + " " + LocaleMessage.getLocaleMessage("Because_of_that_we_are_going_to_save_the_backup_files_in_the_default_path") + "/backup_db", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                   }
/* 209 */                   backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + "$System/";
/*     */                 }
/*     */               
/* 212 */               } else if (dbAbsFolder != null && dbAbsFolder.exists() && dbAbsFolder.isDirectory()) {
/* 213 */                 backupDir = dbAbsFolder.getAbsolutePath() + "/" + Functions.getCurrentDateFolder() + "$System/";
/*     */               } else {
/* 215 */                 if (!ZeusServerCfg.getInstance().getDbBackupDirectory().equalsIgnoreCase("/backup_db")) {
/* 216 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Unable_to_find_configured_backup_path") + ZeusServerCfg.getInstance().getDbBackupDirectory() + " " + LocaleMessage.getLocaleMessage("Because_of_that_we_are_going_to_save_the_backup_files_in_the_default_path") + "/backup_db", Enums.EnumMessagePriority.HIGH, null, null);
/*     */                 }
/* 218 */                 backupDir = root.getAbsolutePath() + "/backup_db" + "/" + Functions.getCurrentDateFolder() + "$System/";
/*     */               } 
/*     */ 
/*     */               
/* 222 */               File backupDirFile = new File(backupDir);
/* 223 */               Functions.dbBackup(backupDirFile, rootFolder);
/* 224 */               wdt = Functions.updateWatchdog(Long.valueOf(wdt), 0L).longValue();
/* 225 */               lastThreadExecution = convert2Date(backupDirFile.getName()).getTimeInMillis();
/* 226 */               Main.startTasks(false, false);
/* 227 */               startFlag = true;
/* 228 */               if (ZeusServerCfg.getInstance().getCleanDataBaseAfterBackup()) {
/* 229 */                 if (tmp != Enums.enumDbStatus.CLEANUP) {
/* 230 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Executing_the_cleaning_of_the_tables_of_the_original_database"), Enums.EnumMessagePriority.LOW, null, null);
/* 231 */                   GlobalVariables.dbCleaner = new DBCleaner(new Timestamp(System.currentTimeMillis()));
/* 232 */                   GlobalVariables.cleanupThread = new Thread(GlobalVariables.dbCleaner);
/* 233 */                   GlobalVariables.cleanupThread.setDaemon(true);
/* 234 */                   GlobalVariables.cleanupThread.start();
/* 235 */                   tmp = Enums.enumDbStatus.CLEANUP;
/*     */                 } else {
/* 237 */                   Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Schedule_cleanup_is_not_starting_as_another_cleanup_task_going_on"), Enums.EnumMessagePriority.LOW, null, null);
/*     */                 } 
/*     */               }
/* 240 */               wdt = Functions.updateWatchdog(Long.valueOf(wdt), 0L).longValue();
/* 241 */               backupModeActivated = false;
/* 242 */               GlobalVariables.dbCurrentStatus = tmp;
/*     */             } finally {
/* 244 */               if (!startFlag) {
/* 245 */                 Main.startTasks(false, false);
/*     */               }
/* 247 */               GlobalVariables.dbCurrentStatus = tmp;
/*     */             } 
/*     */           }
/* 250 */           wdt = Functions.updateWatchdog(Long.valueOf(wdt), 60000L).longValue();
/*     */         } 
/*     */       } 
/* 253 */     } catch (Exception ex) {
/* 254 */       Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Exception_on_the_database_backup_task"), Enums.EnumMessagePriority.HIGH, null, ex);
/* 255 */       GlobalVariables.buzzerActivated = true;
/* 256 */       ex.printStackTrace();
/*     */     } finally {
/*     */       
/* 259 */       try { Main.startTasks(false, false); }
/* 260 */       catch (SigarException sigarException) {  }
/* 261 */       catch (Exception exception) {}
/*     */       
/* 263 */       dispose();
/* 264 */       backupModeActivated = false;
/* 265 */       GlobalVariables.dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void dispose() {
/* 270 */     Functions.printMessage(Util.EnumProductIDs.ZEUS, LocaleMessage.getLocaleMessage("Database_backup_task_finalized"), Enums.EnumMessagePriority.LOW, null, null);
/*     */   }
/*     */   
/*     */   private Calendar convert2Date(String name) {
/* 274 */     if (name == null) {
/* 275 */       return null;
/*     */     }
/* 277 */     Calendar cal = Calendar.getInstance();
/* 278 */     cal.set(Integer.parseInt(name.substring(0, 4)), Integer.parseInt(name.substring(5, 7)) - 1, Integer.parseInt(name.substring(8, 10)), 
/* 279 */         Integer.parseInt(name.substring(11, 13)), Integer.parseInt(name.substring(14, 16)), Integer.parseInt(name.substring(17, 19)));
/* 280 */     return cal;
/*     */   } private Calendar getNextValidCalendar(boolean flag) {
/*     */     String startTime, startTimeData[];
/*     */     int diff, nth, weekDay, month, year, nthDay;
/* 284 */     String[] freqData = ZeusServerCfg.getInstance().getDbBackupFrequency().split(";");
/*     */ 
/*     */ 
/*     */     
/* 288 */     Calendar cal = null;
/* 289 */     switch (Integer.parseInt(freqData[0])) {
/*     */       case 1:
/* 291 */         startTime = freqData[1];
/* 292 */         startTimeData = startTime.split(":");
/* 293 */         cal = Calendar.getInstance();
/* 294 */         cal.set(cal.get(1), cal.get(2), cal.get(5), Integer.parseInt(startTimeData[0]), Integer.parseInt(startTimeData[1]), Integer.parseInt(startTimeData[2]));
/* 295 */         if (flag && cal.getTimeInMillis() < System.currentTimeMillis()) {
/* 296 */           cal.add(5, 1);
/*     */         }
/*     */         break;
/*     */       case 2:
/* 300 */         startTime = freqData[2];
/* 301 */         startTimeData = startTime.split(":");
/* 302 */         cal = Calendar.getInstance();
/* 303 */         diff = Integer.parseInt(freqData[1]) - cal.get(7);
/* 304 */         if (diff < -1) {
/* 305 */           diff += 7;
/*     */         }
/* 307 */         cal.add(7, diff);
/* 308 */         cal.set(cal.get(1), cal.get(2), cal.get(5), Integer.parseInt(startTimeData[0]), Integer.parseInt(startTimeData[1]), Integer.parseInt(startTimeData[2]));
/* 309 */         if (flag && cal.getTimeInMillis() < System.currentTimeMillis()) {
/* 310 */           cal.add(7, 7);
/*     */         }
/*     */         break;
/*     */       case 3:
/* 314 */         startTime = freqData[3];
/* 315 */         startTimeData = startTime.split(":");
/* 316 */         cal = Calendar.getInstance();
/* 317 */         nth = Integer.parseInt(freqData[1]);
/* 318 */         weekDay = Integer.parseInt(freqData[2]);
/* 319 */         month = cal.get(2) + 1;
/* 320 */         year = cal.get(1);
/* 321 */         nthDay = (int)findNthWeekDayOfTheMonth(nth, weekDay, month, year);
/* 322 */         cal.set(cal.get(1), cal.get(2), nthDay, Integer.parseInt(startTimeData[0]), Integer.parseInt(startTimeData[1]), Integer.parseInt(startTimeData[2]));
/* 323 */         if (nthDay > cal.get(5)) {
/* 324 */           cal.set(5, nthDay);
/*     */         }
/* 326 */         if (flag && cal.getTimeInMillis() < System.currentTimeMillis()) {
/* 327 */           cal.add(2, 1);
/*     */         }
/* 329 */         cal.set(cal.get(1), cal.get(2), cal.get(5), Integer.parseInt(startTimeData[0]), Integer.parseInt(startTimeData[1]), Integer.parseInt(startTimeData[2]));
/*     */         break;
/*     */     } 
/* 332 */     return cal;
/*     */   }
/*     */   
/*     */   private Calendar getPrevValidCalendar(Calendar ncal) {
/* 336 */     String[] freqData = ZeusServerCfg.getInstance().getDbBackupFrequency().split(";");
/* 337 */     Calendar cal = Calendar.getInstance();
/* 338 */     cal.set(ncal.get(1), ncal.get(2), ncal.get(5), ncal.get(11), ncal.get(12), ncal.get(13));
/* 339 */     switch (Integer.parseInt(freqData[0])) {
/*     */       case 1:
/* 341 */         cal.add(5, -1);
/*     */         break;
/*     */       case 2:
/* 344 */         cal.add(7, -7);
/*     */         break;
/*     */       case 3:
/* 347 */         cal.add(2, -1);
/*     */         break;
/*     */     } 
/* 350 */     return cal;
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
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static double findNthWeekDayOfTheMonth(int nth, int weekDay, int month, int year) {
/* 367 */     int days, daysOfMonth[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
/* 368 */     int[] daysOfMonthLeapYear = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
/*     */     
/* 370 */     Calendar c = Calendar.getInstance();
/* 371 */     int daysInMonth = c.getMaximum(5);
/* 372 */     int count = 0;
/* 373 */     for (int i = 1; i <= daysInMonth; i++) {
/* 374 */       c.set(5, i);
/* 375 */       if (c.get(7) == weekDay) {
/* 376 */         count++;
/*     */       }
/*     */     } 
/* 379 */     if (nth >= 4) {
/* 380 */       nth = count;
/*     */     }
/* 382 */     if (nth > 0) {
/* 383 */       return ((nth - 1) * 7 + 1) + ((7 + weekDay) - findDayOfTheWeek((nth - 1) * 7 + 1, month, year)) % 7.0D;
/*     */     }
/*     */     
/* 386 */     if (leapYear(year)) {
/* 387 */       days = daysOfMonthLeapYear[month - 1];
/*     */     } else {
/* 389 */       days = daysOfMonth[month - 1];
/*     */     } 
/* 391 */     return days - (findDayOfTheWeek(days, month, year) - weekDay + 7.0D) % 7.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean leapYear(int year) {
/* 400 */     if ((year / 4) != Math.floor((year / 4))) {
/* 401 */       return false;
/*     */     }
/* 403 */     if ((year / 100) != Math.floor((year / 100))) {
/* 404 */       return true;
/*     */     }
/* 406 */     return ((year / 400) == Math.floor((year / 400)));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static double findDayOfTheWeek(int day, int month, int year) {
/* 417 */     double a = Math.floor(((14 - month) / 12));
/* 418 */     double y = year - a;
/* 419 */     double m = month + 12.0D * a - 2.0D;
/*     */     
/* 421 */     double d = (day + y + Math.floor(y / 4.0D) - Math.floor(y / 100.0D) + Math.floor(y / 400.0D) + Math.floor(31.0D * m / 12.0D)) % 7.0D;
/*     */     
/* 423 */     return d + 1.0D;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBGeneral\DerbyDBBackup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */