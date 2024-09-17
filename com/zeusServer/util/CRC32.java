/*    */ package com.zeusServer.util;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class CRC32
/*    */ {
/*    */   public static int createCRC32(byte[] data, int len, int previousCrc32) {
/* 29 */     int crc = previousCrc32;
/* 30 */     int poly = -306674912;
/*    */     
/* 32 */     for (byte b : data) {
/* 33 */       int temp = (crc ^ b) & 0xFF;
/*    */ 
/*    */       
/* 36 */       for (int i = 0; i < 8; i++) {
/* 37 */         if ((temp & 0x1) == 1) {
/* 38 */           temp = temp >>> 1 ^ poly;
/*    */         } else {
/* 40 */           temp >>>= 1;
/*    */         } 
/*    */       } 
/* 43 */       crc = crc >>> 8 ^ temp;
/*    */     } 
/*    */ 
/*    */     
/* 47 */     crc ^= 0xFFFFFFFF;
/* 48 */     return crc;
/*    */   }
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
/*    */   public static int calculateCRC32(byte[] data, int len, int previousCrc32) {
/* 61 */     int marker = 0;
/* 62 */     int crc32 = -1;
/*    */     
/*    */     while (true) {
/* 65 */       int count = (len - marker > 256) ? 256 : (len - marker);
/* 66 */       byte[] tmp = new byte[count];
/* 67 */       System.arraycopy(data, marker, tmp, 0, count);
/* 68 */       crc32 = createCRC32(tmp, count, crc32 & 0xFFFFFFFF);
/* 69 */       marker += count;
/* 70 */       if (len <= marker) {
/* 71 */         return crc32;
/*    */       }
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static int getCRC32(byte[] data) {
/* 81 */     return createCRC32(data, data.length, -1);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\CRC32.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */