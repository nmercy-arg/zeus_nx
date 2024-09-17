/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import java.security.InvalidAlgorithmParameterException;
/*    */ import java.security.InvalidKeyException;
/*    */ import java.security.NoSuchAlgorithmException;
/*    */ import java.util.Base64;
/*    */ import javax.crypto.BadPaddingException;
/*    */ import javax.crypto.Cipher;
/*    */ import javax.crypto.IllegalBlockSizeException;
/*    */ import javax.crypto.NoSuchPaddingException;
/*    */ import javax.crypto.spec.IvParameterSpec;
/*    */ import javax.crypto.spec.SecretKeySpec;
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
/*    */ public class Rijndael
/*    */ {
/* 32 */   public static byte[] cfgKey = new byte[] { 103, 52, 1, -1, 86, 17, -104, -81, 43, 77, 58, 30, 85, 37, 67, -103 };
/* 33 */   public static byte[] dataKeyBytes = new byte[] { 18, -9, 68, 108, 55, -103, -59, -81, 1, -101, -38, 84, -114, 25, 120, 105 };
/* 34 */   public static byte[] msgKeyBytes = new byte[] { -48, 91, -103, 10, -108, 69, 39, 18, 9, 118, 73, -102, -114, 44, 86, 104 };
/* 35 */   public static byte[] dbKeyBytes = new byte[] { -18, 10, 79, 16, -108, -52, 86, 71, -112, 18, 0, -103, -81, 120, 102, 69 };
/* 36 */   public static byte[] aes_256 = new byte[] { 126, 101, 62, 85, 114, -122, -68, -111, 39, 51, -81, -67, 17, 76, 116, 31, 46, -97, 37, 79, -126, -77, -43, -87, -83, -7, 99, Byte.MAX_VALUE, -78, 83, 41, -25 };
/* 37 */   private static byte[] iv = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
/*    */ 
/*    */   
/*    */   public static String decryptString(String cipherText, byte[] key, boolean padding) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
/* 41 */     Cipher cipher = padding ? Cipher.getInstance("AES/CBC/PKCS5Padding") : Cipher.getInstance("AES/CBC/NoPadding");
/* 42 */     cipher.init(2, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
/* 43 */     return Functions.getString(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
/*    */   }
/*    */   
/*    */   public static String encryptString(String text, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
/* 47 */     Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
/* 48 */     cipher.init(1, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
/* 49 */     return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
/*    */   }
/*    */   
/*    */   public static byte[] encryptBytes(String text, byte[] key, boolean padding) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/* 53 */     Cipher cipher = padding ? Cipher.getInstance("AES/CBC/PKCS5Padding") : Cipher.getInstance("AES/CBC/NoPadding");
/* 54 */     cipher.init(1, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
/* 55 */     return cipher.doFinal(text.getBytes("ASCII"));
/*    */   }
/*    */   
/*    */   public static byte[] encryptBytes(byte[] normal, byte[] key, boolean padding) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
/* 59 */     Cipher cipher = padding ? Cipher.getInstance("AES/CBC/PKCS5Padding") : Cipher.getInstance("AES/CBC/NoPadding");
/* 60 */     cipher.init(1, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
/* 61 */     return cipher.doFinal(normal);
/*    */   }
/*    */   
/*    */   public static byte[] decryptBytes(byte[] encryptedBytes, byte[] key, boolean padding) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
/* 65 */     Cipher cipher = padding ? Cipher.getInstance("AES/CBC/PKCS5Padding") : Cipher.getInstance("AES/CBC/NoPadding");
/* 66 */     cipher.init(2, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
/* 67 */     return cipher.doFinal(encryptedBytes);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\Rijndael.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */