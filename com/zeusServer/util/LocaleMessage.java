/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.util.Locale;
/*    */ import java.util.ResourceBundle;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public class LocaleMessage
/*    */ {
/* 22 */   private static ResourceBundle message = null;
/* 23 */   private static String currentLanguage = "";
/*    */ 
/*    */   
/*    */   public static void createInstance(String language) {
/* 27 */     if (message == null || !currentLanguage.equalsIgnoreCase(language)) {
/* 28 */       String[] countryAndLanguage = language.split("_");
/* 29 */       message = ResourceBundle.getBundle("com.zeusServer.languageResources.MessageBundle", new Locale(countryAndLanguage[0], countryAndLanguage[1]));
/* 30 */       currentLanguage = language;
/*    */     } 
/*    */   }
/*    */   
/*    */   public static String getLocaleMessage(String text) {
/* 35 */     createInstance(ZeusServerCfg.getInstance().getLanguageID());
/* 36 */     return message.getString(text);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\LocaleMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */