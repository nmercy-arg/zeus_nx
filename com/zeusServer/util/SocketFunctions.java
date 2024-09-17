/*     */ package com.zeusServer.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.net.Socket;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.channels.SelectionKey;
/*     */ import java.nio.channels.Selector;
/*     */ import java.nio.channels.SocketChannel;
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.Iterator;
/*     */ import java.util.Set;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
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
/*     */ public class SocketFunctions
/*     */ {
/*     */   private static Charset charset;
/*     */   
/*     */   public static Socket closeSocket(Socket socket) {
/*     */     try {
/*  36 */       if (socket != null) {
/*  37 */         socket.shutdownInput();
/*  38 */         socket.shutdownOutput();
/*  39 */         socket.close();
/*     */       } 
/*  41 */     } catch (IOException iOException) {
/*     */     
/*     */     } finally {
/*  44 */       socket = null;
/*     */     } 
/*  46 */     return socket;
/*     */   }
/*     */   
/*     */   public static void connect(Socket socket, String ip, int port, int timeout) throws IOException, Exception {
/*     */     try {
/*  51 */       socket.connect(new InetSocketAddress(ip, port), timeout);
/*  52 */     } catch (IOException ex) {
/*  53 */       throw ex;
/*  54 */     } catch (Exception ex) {
/*  55 */       throw ex;
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void send(Socket sck, String data) throws IOException {
/*  60 */     sck.getOutputStream().write(data.getBytes());
/*     */   }
/*     */   
/*     */   public static void send(Socket sck, byte[] data) throws IOException {
/*  64 */     if (sck.getInputStream().available() > 0) {
/*  65 */       sck.getInputStream().skip(sck.getInputStream().available());
/*     */     }
/*  67 */     sck.getOutputStream().flush();
/*  68 */     sck.getOutputStream().write(data);
/*     */   }
/*     */   
/*     */   public static void sendWithOutSkip(Socket sck, byte[] data) throws IOException {
/*  72 */     sck.getOutputStream().flush();
/*  73 */     sck.getOutputStream().write(data);
/*     */   }
/*     */   
/*     */   public static void clearInputStream(Socket sck, int len) throws IOException {
/*  77 */     if (sck.getInputStream().available() > 0) {
/*  78 */       sck.getInputStream().skip(len);
/*     */     }
/*     */   }
/*     */   
/*     */   public static int send(SocketChannel client, String data) throws IOException {
/*  83 */     return send(client, getCharset().encode(data));
/*     */   }
/*     */   
/*     */   public static int sendString(SocketChannel client, ByteBuffer buffer) throws IOException {
/*  87 */     return client.write(buffer);
/*     */   }
/*     */   
/*     */   public static int sendString(SocketChannel client, String data) throws IOException {
/*  91 */     return client.write(getCharset().encode(data));
/*     */   }
/*     */   
/*     */   public static int send(SocketChannel client, byte[] buffer, int offset, int size) throws IOException {
/*  95 */     return send(client, ByteBuffer.wrap(buffer, offset, size));
/*     */   }
/*     */   
/*     */   public static int send(SocketChannel client, ByteBuffer buffer) throws IOException {
/*  99 */     SelectionKey writeKey = null;
/* 100 */     Selector writeSelector = null;
/*     */     try {
/* 102 */       writeSelector = Selector.open();
/* 103 */       client.register(writeSelector, 4);
/* 104 */       writeSelector.select(1000L);
/*     */       
/* 106 */       Set<SelectionKey> key = writeSelector.selectedKeys();
/* 107 */       Iterator<SelectionKey> ir = key.iterator();
/* 108 */       while (ir.hasNext()) {
/* 109 */         writeKey = ir.next();
/* 110 */         ir.remove();
/* 111 */         if (writeKey.isWritable()) {
/* 112 */           return client.write(buffer);
/*     */         }
/*     */       } 
/*     */     } finally {
/* 116 */       if (writeKey != null) {
/* 117 */         writeKey.cancel();
/*     */       }
/* 119 */       if (writeSelector != null) {
/* 120 */         writeSelector.close();
/*     */       }
/*     */     } 
/* 123 */     return 0;
/*     */   }
/*     */   
/*     */   public static int send(SocketChannel client, byte data) throws IOException {
/* 127 */     byte[] buffer = { data };
/* 128 */     return send(client, buffer, 0, 1);
/*     */   }
/*     */   
/*     */   public static byte[] receive(Socket sck, int offset, int size) throws IOException, InterruptedException {
/* 132 */     byte[] buff = new byte[size];
/* 133 */     long t = System.currentTimeMillis() + 30000L;
/* 134 */     while (sck.getInputStream().available() < size && t > System.currentTimeMillis()) {
/* 135 */       Thread.sleep(10L);
/*     */     }
/* 137 */     sck.getInputStream().read(buff, offset, size);
/* 138 */     return buff;
/*     */   }
/*     */   
/*     */   public static int receive(SocketChannel client, ByteBuffer buffer) throws IOException {
/* 142 */     SelectionKey readkey = null;
/* 143 */     Selector readSelector = null;
/*     */     try {
/* 145 */       readSelector = Selector.open();
/* 146 */       client.register(readSelector, 1);
/* 147 */       readSelector.select(10000L);
/*     */       
/* 149 */       Set<SelectionKey> key = readSelector.selectedKeys();
/* 150 */       Iterator<SelectionKey> ir = key.iterator();
/* 151 */       while (ir.hasNext()) {
/* 152 */         readkey = ir.next();
/* 153 */         ir.remove();
/* 154 */         if (readkey.isReadable()) {
/* 155 */           return client.read(buffer);
/*     */         }
/*     */       } 
/*     */     } finally {
/* 159 */       if (readkey != null) {
/* 160 */         readkey.cancel();
/*     */       }
/* 162 */       if (readSelector != null) {
/* 163 */         readSelector.close();
/*     */       }
/*     */     } 
/* 166 */     return 0;
/*     */   }
/*     */ 
/*     */   
/*     */   public static int receiveCommand(SocketChannel client, ByteBuffer buffer) throws IOException {
/* 171 */     SelectionKey readkey = null;
/* 172 */     Selector readSelector = null;
/*     */     try {
/* 174 */       readSelector = Selector.open();
/* 175 */       client.register(readSelector, 1);
/* 176 */       readSelector.select(20000L);
/*     */       
/* 178 */       Set<SelectionKey> key = readSelector.selectedKeys();
/* 179 */       Iterator<SelectionKey> ir = key.iterator();
/* 180 */       while (ir.hasNext()) {
/* 181 */         readkey = ir.next();
/* 182 */         ir.remove();
/* 183 */         if (readkey.isReadable()) {
/* 184 */           return client.read(buffer);
/*     */         }
/*     */       } 
/*     */     } finally {
/* 188 */       if (readkey != null) {
/* 189 */         readkey.cancel();
/*     */       }
/* 191 */       if (readSelector != null) {
/* 192 */         readSelector.close();
/*     */       }
/*     */     } 
/* 195 */     return 0;
/*     */   }
/*     */ 
/*     */   
/*     */   public static int receiveString(SocketChannel client, ByteBuffer buffer) throws IOException {
/* 200 */     return client.read(buffer);
/*     */   }
/*     */   
/*     */   public static void closeSocket(SocketChannel clientSocket) {
/*     */     try {
/* 205 */       if (clientSocket != null && clientSocket.isConnected()) {
/* 206 */         clientSocket.close();
/*     */       }
/* 208 */     } catch (IOException ex) {
/* 209 */       Logger.getLogger(SocketFunctions.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */     } 
/*     */   }
/*     */   
/*     */   private static Charset getCharset() {
/* 214 */     if (charset == null) {
/* 215 */       charset = Charset.forName("US-ASCII");
/*     */     }
/* 217 */     return charset;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\SocketFunctions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */