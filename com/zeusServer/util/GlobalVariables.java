/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import com.zeusServer.DBGeneral.DBCleaner;
/*    */ import java.util.Calendar;
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
/*    */ public class GlobalVariables
/*    */ {
/* 17 */   public static Calendar applicationStartupTime = Calendar.getInstance();
/* 18 */   public static volatile DBCleaner dbCleaner = null;
/* 19 */   public static volatile Enums.enumDbStatus dbCurrentStatus = Enums.enumDbStatus.NORMAL;
/* 20 */   public static volatile long lastDbSpaceReclaim = 0L;
/*    */   public static boolean buzzerActivated = false;
/* 22 */   public static volatile long lastDbCleanup = 0L;
/*    */   public static boolean mainTimerRoutineRunning = false;
/* 24 */   public static volatile Thread cleanupThread = null;
/*    */   public static boolean mainTimerStopped = true;
/*    */   public static Enums.Platform currentPlatform;
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\GlobalVariables.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */