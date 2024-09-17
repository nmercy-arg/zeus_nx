/*     */ package com.zeusServer.util;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import org.apache.log4j.FileAppender;
/*     */ import org.apache.log4j.spi.LoggingEvent;
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
/*     */ public class DatedFileAppender
/*     */   extends FileAppender
/*     */ {
/* 156 */   private String m_directory = "logs";
/*     */ 
/*     */ 
/*     */   
/* 160 */   private String m_prefix = "tomcat.";
/*     */ 
/*     */ 
/*     */   
/* 164 */   private String m_suffix = ".log";
/*     */ 
/*     */ 
/*     */   
/* 168 */   private File m_path = null;
/*     */ 
/*     */ 
/*     */   
/* 172 */   private Calendar m_calendar = null;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 177 */   private long m_tomorrow = 0L;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public DatedFileAppender() {}
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public DatedFileAppender(String directory, String prefix, String suffix) {
/* 197 */     this.m_directory = directory;
/* 198 */     this.m_prefix = prefix;
/* 199 */     this.m_suffix = suffix;
/* 200 */     activateOptions();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getDirectory() {
/* 208 */     return this.m_directory;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setDirectory(String directory) {
/* 217 */     this.m_directory = directory;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getPrefix() {
/* 224 */     return this.m_prefix;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setPrefix(String prefix) {
/* 233 */     this.m_prefix = prefix;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getSuffix() {
/* 240 */     return this.m_suffix;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setSuffix(String suffix) {
/* 249 */     this.m_suffix = suffix;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void activateOptions() {
/* 258 */     if (this.m_prefix == null) {
/* 259 */       this.m_prefix = "";
/*     */     }
/* 261 */     if (this.m_suffix == null) {
/* 262 */       this.m_suffix = "";
/*     */     }
/* 264 */     if (this.m_directory == null || this.m_directory.length() == 0) {
/* 265 */       this.m_directory = ".";
/*     */     }
/* 267 */     this.m_path = new File(this.m_directory);
/* 268 */     if (!this.m_path.isAbsolute()) {
/* 269 */       String base = System.getProperty("catalina.base");
/* 270 */       if (base != null) {
/* 271 */         this.m_path = new File(base, this.m_directory);
/*     */       }
/*     */     } 
/* 274 */     this.m_path.mkdirs();
/* 275 */     if (this.m_path.canWrite()) {
/* 276 */       this.m_calendar = Calendar.getInstance();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void append(LoggingEvent event) {
/* 286 */     if (this.layout == null) {
/* 287 */       this.errorHandler.error("No layout set for the appender named [" + this.name + "].");
/*     */       return;
/*     */     } 
/* 290 */     if (this.m_calendar == null) {
/* 291 */       this.errorHandler.error("Improper initialization for the appender named [" + this.name + "].");
/*     */       return;
/*     */     } 
/* 294 */     long n = System.currentTimeMillis();
/* 295 */     if (n >= this.m_tomorrow) {
/*     */       
/* 297 */       this.m_calendar.setTime(new Date(n));
/* 298 */       String datestamp = datestamp(this.m_calendar);
/* 299 */       tomorrow(this.m_calendar);
/*     */       
/* 301 */       this.m_tomorrow = this.m_calendar.getTime().getTime();
/* 302 */       File newFile = new File(this.m_path, this.m_prefix + datestamp + this.m_suffix);
/* 303 */       this.fileName = newFile.getAbsolutePath();
/* 304 */       super.activateOptions();
/*     */     } 
/* 306 */     if (this.qw == null) {
/* 307 */       this.errorHandler.error("No output stream or file set for the appender named [" + this.name + "].");
/*     */       
/*     */       return;
/*     */     } 
/* 311 */     subAppend(event);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String datestamp(Calendar calendar) {
/* 322 */     int year = calendar.get(1);
/* 323 */     int month = calendar.get(2) + 1;
/* 324 */     int day = calendar.get(5);
/* 325 */     StringBuffer sb = new StringBuffer();
/* 326 */     if (year < 1000) {
/* 327 */       sb.append('0');
/* 328 */       if (year < 100) {
/* 329 */         sb.append('0');
/* 330 */         if (year < 10) {
/* 331 */           sb.append('0');
/*     */         }
/*     */       } 
/*     */     } 
/* 335 */     sb.append(Integer.toString(year));
/* 336 */     sb.append('-');
/* 337 */     if (month < 10) {
/* 338 */       sb.append('0');
/*     */     }
/* 340 */     sb.append(Integer.toString(month));
/* 341 */     sb.append('-');
/* 342 */     if (day < 10) {
/* 343 */       sb.append('0');
/*     */     }
/* 345 */     sb.append(Integer.toString(day));
/* 346 */     return sb.toString();
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
/*     */   public static void tomorrow(Calendar calendar) {
/* 362 */     int year = calendar.get(1);
/* 363 */     int month = calendar.get(2);
/* 364 */     int day = calendar.get(5) + 1;
/* 365 */     calendar.clear();
/* 366 */     calendar.set(year, month, day);
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\DatedFileAppender.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */