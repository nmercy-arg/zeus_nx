/*     */ package com.zeusServer.box;
/*     */ 
/*     */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.Main;
/*     */ import com.zeusServer.util.SocketFunctions;
/*     */ import com.zeusbox.nativeLibrary.ZeusBoxNativeLibrary;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.sql.SQLException;
/*     */ import java.text.DateFormat;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Comparator;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
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
/*     */ public class ZeusBoxHandler
/*     */ {
/*  40 */   private static DateFormat timeF = new SimpleDateFormat("HH:mm:ss");
/*  41 */   private static DateFormat dateF = new SimpleDateFormat("dd/MM/yyyy");
/*  42 */   private static DateFormat dateMMMF = new SimpleDateFormat("dd-MMM-yyyy");
/*  43 */   public static int boxSelectedProduct = -1;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void handleZeusBoxEventPush(Socket clientSocket, byte[] data) throws IOException, InterruptedException, ParseException, SQLException {
/*  51 */     if ((data[0] & 0xFF) == 66) {
/*  52 */       int messageLen = data[2] & 0xFF;
/*  53 */       messageLen = messageLen * 256 + (data[1] & 0xFF);
/*  54 */       byte[] bufferRx = new byte[messageLen + 5];
/*  55 */       System.arraycopy(data, 0, bufferRx, 0, 3);
/*  56 */       data = SocketFunctions.receive(clientSocket, 0, messageLen + 2);
/*  57 */       if (data != null && data.length == messageLen + 2) {
/*  58 */         System.arraycopy(data, 0, bufferRx, 3, messageLen + 2);
/*  59 */         int crcReceived = bufferRx[messageLen + 4] & 0xFF;
/*  60 */         crcReceived = crcReceived * 256 + (bufferRx[messageLen + 3] & 0xFF);
/*  61 */         int crcCalc = CRC16.calculate(bufferRx, 0, messageLen + 3, 65535);
/*  62 */         if (crcReceived == crcCalc) {
/*     */           
/*  64 */           int index = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*  72 */           List<ZeusBoxEvents> events = new ArrayList<>();
/*  73 */           Date rcvdDate = dateF.parse(dateF.format(new Date()));
/*     */ 
/*     */           
/*  76 */           while (index < data.length - 2) {
/*  77 */             int tmp1, tmp2; byte[] eData; String timeDate; ZeusBoxEvents zEvent; int fidVal = data[index++];
/*  78 */             short flen = (short)data[index++];
/*  79 */             int eLen = flen + 2 + 8;
/*  80 */             switch (fidVal) {
/*     */               case 80:
/*  82 */                 tmp1 = index++;
/*  83 */                 tmp2 = index++;
/*  84 */                 eData = new byte[flen + 4 + 8];
/*  85 */                 eData[0] = 80;
/*  86 */                 eData[1] = (byte)eLen;
/*  87 */                 eData[2] = data[tmp1];
/*  88 */                 eData[3] = data[tmp2];
/*  89 */                 eData[4] = 73;
/*  90 */                 eData[5] = 8;
/*  91 */                 timeDate = timeF.format(new Date());
/*  92 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 6, 8);
/*  93 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/*  94 */                 events.add(zEvent);
/*  95 */                 if (Main.getCheckOccurrences() != null) {
/*  96 */                   Main.getCheckOccurrences().setZeusBoxEvents(80, data[tmp1], ((data[tmp2] & 0xFF) != 1));
/*     */                 }
/*     */               
/*     */               case 81:
/* 100 */                 tmp1 = index++;
/* 101 */                 eData = new byte[flen + 4 + 8];
/* 102 */                 eData[0] = 81;
/* 103 */                 eData[1] = (byte)eLen;
/* 104 */                 eData[2] = data[tmp1];
/* 105 */                 eData[3] = 73;
/* 106 */                 eData[4] = 8;
/* 107 */                 timeDate = timeF.format(new Date());
/* 108 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 5, 8);
/* 109 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/* 110 */                 events.add(zEvent);
/* 111 */                 if (Main.getCheckOccurrences() != null) {
/* 112 */                   Main.getCheckOccurrences().setZeusBoxEvents(81, 2, ((data[tmp1] & 0xFF) != 1));
/*     */                 }
/*     */               
/*     */               case 82:
/* 116 */                 tmp1 = index++;
/* 117 */                 eData = new byte[flen + 4 + 8];
/* 118 */                 eData[0] = 82;
/* 119 */                 eData[1] = (byte)eLen;
/* 120 */                 eData[2] = data[tmp1];
/* 121 */                 eData[3] = 73;
/* 122 */                 eData[4] = 8;
/* 123 */                 timeDate = timeF.format(new Date());
/* 124 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 5, 8);
/* 125 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/* 126 */                 events.add(zEvent);
/* 127 */                 if (Main.getCheckOccurrences() != null) {
/* 128 */                   Main.getCheckOccurrences().setZeusBoxEvents(82, 3, ((data[tmp1] & 0xFF) != 1));
/*     */                 }
/*     */               
/*     */               case 83:
/* 132 */                 tmp1 = index++;
/* 133 */                 eData = new byte[flen + 4 + 8];
/* 134 */                 eData[0] = 83;
/* 135 */                 eData[1] = (byte)eLen;
/* 136 */                 eData[2] = data[tmp1];
/* 137 */                 eData[3] = 73;
/* 138 */                 eData[4] = 8;
/* 139 */                 timeDate = timeF.format(new Date());
/* 140 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 5, 8);
/* 141 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/* 142 */                 events.add(zEvent);
/* 143 */                 if (Main.getCheckOccurrences() != null) {
/* 144 */                   Main.getCheckOccurrences().setZeusBoxEvents(83, 4, ((data[tmp1] & 0xFF) != 1));
/*     */                 }
/*     */               
/*     */               case 84:
/* 148 */                 tmp1 = index++;
/* 149 */                 eData = new byte[flen + 4 + 8];
/* 150 */                 eData[0] = 84;
/* 151 */                 eData[1] = (byte)eLen;
/* 152 */                 eData[2] = data[tmp1];
/* 153 */                 eData[3] = 73;
/* 154 */                 eData[4] = 8;
/* 155 */                 timeDate = timeF.format(new Date());
/* 156 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 5, 8);
/* 157 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/* 158 */                 events.add(zEvent);
/* 159 */                 if (Main.getCheckOccurrences() != null) {
/* 160 */                   Main.getCheckOccurrences().setZeusBoxEvents(84, 5, ((data[tmp1] & 0xFF) != 1));
/*     */                 }
/*     */               
/*     */               case 85:
/* 164 */                 tmp1 = index;
/* 165 */                 eData = new byte[flen + 4 + 8];
/* 166 */                 eData[0] = 85;
/* 167 */                 eData[1] = (byte)eLen;
/* 168 */                 eData[2] = data[tmp1];
/* 169 */                 eData[3] = 73;
/* 170 */                 eData[4] = 8;
/* 171 */                 timeDate = timeF.format(new Date());
/* 172 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 5, 8);
/* 173 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/* 174 */                 events.add(zEvent);
/* 175 */                 if (Main.getCheckOccurrences() != null) {
/* 176 */                   Main.getCheckOccurrences().setZeusBoxEvents(85, 6, ((data[tmp1] & 0xFF) != 1));
/*     */                 }
/*     */               
/*     */               case 86:
/* 180 */                 tmp1 = index++;
/* 181 */                 eData = new byte[flen + 4 + 8];
/* 182 */                 eData[0] = 86;
/* 183 */                 eData[1] = (byte)eLen;
/* 184 */                 eData[2] = data[tmp1];
/* 185 */                 eData[3] = 73;
/* 186 */                 eData[4] = 8;
/* 187 */                 timeDate = timeF.format(new Date());
/* 188 */                 System.arraycopy(timeDate.getBytes(), 0, eData, 5, 8);
/* 189 */                 zEvent = new ZeusBoxEvents(getListfromByteArray(eData), rcvdDate, false);
/* 190 */                 events.add(zEvent);
/* 191 */                 if (Main.getCheckOccurrences() != null) {
/* 192 */                   Main.getCheckOccurrences().setZeusBoxEvents(86, 7, ((data[tmp1] & 0xFF) != 1));
/*     */                 }
/*     */             } 
/*     */           
/*     */           } 
/* 197 */           if (events.size() > 0) {
/* 198 */             ZeusSettingsDBManager.insertZeusBoxEvents(events);
/*     */           }
/*     */           
/* 201 */           SocketFunctions.send(clientSocket, new byte[] { 6 });
/*     */         } else {
/* 203 */           SocketFunctions.send(clientSocket, new byte[] { 21 });
/*     */         } 
/*     */       } else {
/* 206 */         SocketFunctions.send(clientSocket, new byte[] { 21 });
/*     */       } 
/* 208 */     } else if ((data[0] & 0xFF) == 57) {
/* 209 */       if (data[2] == 1) {
/* 210 */         SocketFunctions.send(clientSocket, new byte[] { 6 });
/* 211 */         ZeusBoxNativeLibrary.nShutdown();
/*     */       }
/* 213 */       else if (data[2] == 2) {
/* 214 */         SocketFunctions.send(clientSocket, new byte[] { 6 });
/*     */         
/* 216 */         ZeusBoxNativeLibrary.nRestart();
/*     */       } else {
/* 218 */         SocketFunctions.send(clientSocket, new byte[] { 21 });
/*     */       } 
/* 220 */     } else if ((data[0] & 0xFF) == 56) {
/* 221 */       if (data[2] > 4) {
/* 222 */         boxSelectedProduct = -1;
/* 223 */         SocketFunctions.send(clientSocket, new byte[] { 21 });
/*     */       } else {
/* 225 */         boxSelectedProduct = data[2];
/* 226 */         SocketFunctions.send(clientSocket, new byte[] { 6 });
/*     */       } 
/*     */     } else {
/*     */       
/* 230 */       SocketFunctions.send(clientSocket, new byte[] { 21 });
/*     */     } 
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
/*     */   public static void handleZeusBoxEventsPop(Socket clientSocket) throws SQLException, InterruptedException, IOException {
/* 243 */     List<ZeusBoxEvents> events = ZeusSettingsDBManager.getZeusBoxEvents();
/* 244 */     int count = 0;
/* 245 */     Map<String, List<ZeusBoxEvents>> data = new LinkedHashMap<>();
/* 246 */     String date = "";
/*     */     
/* 248 */     for (ZeusBoxEvents event : events) {
/* 249 */       String eDate = dateMMMF.format(event.getReceivedDate());
/* 250 */       if (!date.equalsIgnoreCase(eDate)) {
/* 251 */         List<ZeusBoxEvents> subEvents = new ArrayList<>();
/* 252 */         subEvents.add(event);
/* 253 */         data.put(eDate, subEvents);
/* 254 */         date = eDate;
/* 255 */         count += event.getEventData().size() + 13; continue;
/*     */       } 
/* 257 */       ((List<ZeusBoxEvents>)data.get(date)).add(event);
/* 258 */       count += event.getEventData().size();
/*     */     } 
/*     */     
/* 261 */     count += data.size() * 2;
/* 262 */     byte[] pop = new byte[count + data.size() * 2];
/*     */     
/* 264 */     int index = 0;
/*     */ 
/*     */     
/* 267 */     for (Map.Entry<String, List<ZeusBoxEvents>> entry : data.entrySet()) {
/* 268 */       pop[index++] = 72;
/* 269 */       pop[index++] = 11;
/* 270 */       System.arraycopy(((String)entry.getKey()).getBytes(), 0, pop, index, ((String)entry.getKey()).length());
/* 271 */       index += ((String)entry.getKey()).length();
/* 272 */       int tmpIndex = index;
/* 273 */       index += 2;
/* 274 */       int deLen = 0;
/*     */       
/* 276 */       List<ZeusBoxEvents> sortedList = sortEventDataByTime(entry.getValue());
/* 277 */       for (ZeusBoxEvents event : sortedList) {
/* 278 */         byte[] conData = getByteArrayfromList(event.getEventData());
/* 279 */         System.arraycopy(conData, 0, pop, index, event.getEventData().size());
/* 280 */         if (event.getEventData() != null && event.getEventData().size() > 4) {
/* 281 */           updateOccurrence(event.getEventData());
/*     */         }
/* 283 */         index += event.getEventData().size();
/* 284 */         deLen += event.getEventData().size();
/*     */       } 
/* 286 */       byte[] tmp2 = Functions.get2ByteArrayFromInt(deLen);
/* 287 */       pop[tmpIndex++] = tmp2[1];
/* 288 */       pop[tmpIndex++] = tmp2[0];
/*     */     } 
/* 290 */     index = 0;
/* 291 */     int blockIndex = 0;
/*     */ 
/*     */ 
/*     */     
/* 295 */     int popLen = 0;
/* 296 */     int retry = 0;
/* 297 */     while (popLen < count && retry++ < 3) {
/* 298 */       int blockLength = (count - popLen > 240) ? 240 : (count - popLen);
/* 299 */       byte[] chunk = new byte[blockLength + 4];
/* 300 */       chunk[index++] = (byte)blockIndex;
/* 301 */       chunk[index] = (byte)blockLength;
/* 302 */       if (blockIndex == 0) {
/* 303 */         index++;
/* 304 */         chunk[index++] = 71;
/* 305 */         byte[] arrayOfByte = Functions.get2ByteArrayFromInt(count);
/* 306 */         chunk[index++] = arrayOfByte[1];
/* 307 */         chunk[index] = arrayOfByte[0];
/* 308 */         System.arraycopy(pop, 0, chunk, index + 1, blockLength - 3);
/* 309 */         popLen += blockLength - 3;
/* 310 */         index += blockLength - 3;
/*     */       } else {
/* 312 */         System.arraycopy(pop, popLen, chunk, index + 1, blockLength);
/* 313 */         popLen += blockLength;
/* 314 */         index += blockLength;
/*     */       } 
/* 316 */       int crcCalc = CRC16.calculate(chunk, 0, blockLength + 2, 65535);
/* 317 */       byte[] tmp = Functions.get2ByteArrayFromInt(crcCalc);
/* 318 */       chunk[index + 1] = tmp[1];
/* 319 */       chunk[index + 2] = tmp[0];
/*     */       try {
/* 321 */         SocketFunctions.send(clientSocket, chunk);
/* 322 */         Thread.sleep(500L);
/* 323 */         tmp = SocketFunctions.receive(clientSocket, 0, 1);
/* 324 */         if ((tmp[0] & 0xFF) == 6) {
/* 325 */           retry = 0;
/* 326 */           blockIndex++;
/* 327 */         } else if ((tmp[0] & 0xFF) == 21) {
/* 328 */           popLen -= blockLength;
/*     */         } 
/* 330 */       } catch (IOException|InterruptedException ex) {
/* 331 */         Thread.sleep(2500L);
/* 332 */         popLen -= blockLength;
/* 333 */         ex.printStackTrace();
/*     */         break;
/*     */       } finally {
/* 336 */         index = 0;
/*     */       } 
/*     */     } 
/* 339 */     if (count > 0) {
/* 340 */       SocketFunctions.send(clientSocket, new byte[] { 6 });
/*     */     } else {
/* 342 */       SocketFunctions.send(clientSocket, new byte[] { 7 });
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void updateOccurrence(List<Byte> data) {
/* 347 */     switch (((Byte)data.get(0)).byteValue()) {
/*     */       case 80:
/* 349 */         Main.getCheckOccurrences().setZeusBoxEvents(80, ((Byte)data.get(2)).byteValue(), ((((Byte)data.get(3)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */       case 81:
/* 352 */         Main.getCheckOccurrences().setZeusBoxEvents(81, 2, ((((Byte)data.get(2)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */       case 82:
/* 355 */         Main.getCheckOccurrences().setZeusBoxEvents(82, 3, ((((Byte)data.get(2)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */       case 83:
/* 358 */         Main.getCheckOccurrences().setZeusBoxEvents(83, 4, ((((Byte)data.get(2)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */       case 84:
/* 361 */         Main.getCheckOccurrences().setZeusBoxEvents(84, 5, ((((Byte)data.get(2)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */       case 85:
/* 364 */         Main.getCheckOccurrences().setZeusBoxEvents(85, 6, ((((Byte)data.get(2)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */       case 86:
/* 367 */         Main.getCheckOccurrences().setZeusBoxEvents(86, 7, ((((Byte)data.get(2)).byteValue() & 0xFF) != 1));
/*     */         break;
/*     */     } 
/*     */   }
/*     */   private static List<Byte> getListfromByteArray(byte[] data) {
/* 372 */     List<Byte> lst = new ArrayList<>(data.length);
/* 373 */     for (byte b : data) {
/* 374 */       lst.add(Byte.valueOf(b));
/*     */     }
/* 376 */     return lst;
/*     */   }
/*     */   
/*     */   private static byte[] getByteArrayfromList(List<Byte> data) {
/* 380 */     int len = data.size();
/* 381 */     byte[] b = new byte[len];
/* 382 */     int i = 0;
/* 383 */     for (Iterator<Byte> iterator = data.iterator(); iterator.hasNext(); ) { byte bb = ((Byte)iterator.next()).byteValue();
/* 384 */       b[i++] = bb; }
/*     */     
/* 386 */     return b;
/*     */   }
/*     */   
/*     */   private static List<ZeusBoxEvents> sortEventDataByTime(List<ZeusBoxEvents> list) {
/* 390 */     Collections.sort(list, new Comparator<ZeusBoxEvents>() {
/*     */           public int compare(ZeusBoxEvents o1, ZeusBoxEvents o2) {
/* 392 */             return o1.getTime().compareTo(o2.getTime());
/*     */           }
/*     */         });
/*     */     
/* 396 */     return list;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\box\ZeusBoxHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */