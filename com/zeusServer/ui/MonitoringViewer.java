/*     */ package com.zeusServer.ui;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GenericDBManager;
/*     */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*     */ import com.zeusServer.dto.SP_013DataHolder;
/*     */ import com.zeusServer.tblConnections.TblActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblGriffonActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblMercuriusAVLActiveUdpConnections;
/*     */ import com.zeusServer.tblConnections.TblMercuriusActiveConnections;
/*     */ import com.zeusServer.tblConnections.TblPegasusActiveConnections;
/*     */ import com.zeusServer.util.CRC16;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.Main;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.TimeUnit;
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
/*     */ public class MonitoringViewer
/*     */ {
/*     */   private ScheduledFuture monitorSF;
/*     */   private ScheduledFuture zeusLiveDataUpdater;
/*  42 */   long accessDB = 0L;
/*     */   private boolean flag = true;
/*  44 */   private final int NEXT_ACCESS_DB = 15000;
/*     */   
/*     */   public MonitoringViewer() {
/*  47 */     initMonitoringInfo();
/*     */   }
/*     */   
/*     */   private void initMonitoringInfo() {
/*  51 */     initiateMonitoring();
/*     */   }
/*     */   
/*     */   private void initiateMonitoring() {
/*  55 */     Runnable monitorTask = new Runnable()
/*     */       {
/*     */         public void run() {
/*     */           try {
/*  59 */             Functions.pumpMessage2RemoteUI(0, MonitoringViewer.this.prepareMonitoringPacket(), null, 146, 0, 0);
/*  60 */           } catch (InterruptedException|SQLException ex) {
/*  61 */             Logger.getLogger(MonitoringViewer.class.getName()).log(Level.SEVERE, (String)null, ex);
/*     */           } 
/*     */         }
/*     */       };
/*  65 */     this.monitorSF = Functions.addRunnable2ScheduleExecutor(monitorTask, 0L, 1000L, TimeUnit.MILLISECONDS);
/*     */   }
/*     */   private byte[] prepareMonitoringPacket() throws SQLException, InterruptedException {
/*     */     byte[] data;
/*  69 */     List<Byte> iData = UILogInitiator.getMonitoringIconsInitialStatus(this.flag);
/*  70 */     int iDataSize = (iData != null) ? iData.size() : 0;
/*  71 */     int availableProducts = Util.getAvailbleProductSchemas().size();
/*  72 */     long tgap = System.currentTimeMillis() - GlobalVariables.applicationStartupTime.getTimeInMillis();
/*  73 */     String strRuntime = String.format("%dd %02d:%02d:%02d", new Object[] { Long.valueOf(TimeUnit.MILLISECONDS.toDays(tgap)), Long.valueOf(TimeUnit.MILLISECONDS.toHours(tgap) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(tgap))), Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(tgap) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(tgap))), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(tgap) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tgap))) });
/*  74 */     int bufferLength = 11 + 18 * (availableProducts + 1) + strRuntime.length() + iDataSize + (availableProducts + 1) * 3;
/*  75 */     boolean dbData = false;
/*  76 */     int apPendingEvents = 0;
/*  77 */     int apPendingAlives = 0;
/*  78 */     int apRegisteredDevices = 0;
/*  79 */     int apDisabledDevices = 0;
/*  80 */     int apConnectedDevices = 0;
/*  81 */     int apConnectedViaTCP = 0;
/*  82 */     int apConnectedViaUDP = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  87 */     int indx = 3;
/*     */     
/*  89 */     if (this.flag) {
/*  90 */       this.flag = false;
/*     */     }
/*  92 */     if (this.accessDB < System.currentTimeMillis() && !GlobalVariables.mainTimerStopped) {
/*  93 */       bufferLength = 11 + 42 * (availableProducts + 1) + strRuntime.length() + iDataSize + (availableProducts + 1) * 3;
/*  94 */       data = new byte[bufferLength];
/*  95 */       dbData = true;
/*     */     } else {
/*  97 */       data = new byte[bufferLength];
/*     */     } 
/*     */     
/* 100 */     data[0] = -110;
/* 101 */     byte[] tmp = Functions.get2ByteArrayFromInt(bufferLength - 5);
/* 102 */     data[1] = tmp[0];
/* 103 */     data[2] = tmp[1];
/*     */     
/* 105 */     for (String schemaName : Util.getAvailbleProductSchemas()) {
/* 106 */       int totalNumConnections, udpNumConnections, regDevices = 0;
/* 107 */       int disDevices = 0;
/* 108 */       int connDevices = 0;
/* 109 */       int connTCPDevices = 0;
/* 110 */       int connUDPDevices = 0;
/* 111 */       int pendingEvents = 0;
/* 112 */       int pendingAlives = 0;
/*     */       
/* 114 */       switch (schemaName) {
/*     */         case "PEGASUS":
/* 116 */           if (dbData) {
/* 117 */             SP_013DataHolder sp13dh = GenericDBManager.executeSP_013("PEGASUS");
/* 118 */             if (sp13dh != null) {
/* 119 */               data[indx++] = 34;
/* 120 */               data[indx++] = 43;
/* 121 */               data[indx++] = (byte)Util.EnumProductIDs.PEGASUS.getProductId();
/*     */               
/* 123 */               data[indx++] = 18;
/* 124 */               data[indx++] = 4;
/* 125 */               tmp = Functions.intToByteArray(sp13dh.getNum_Pending_Events(), 4);
/* 126 */               pendingEvents += sp13dh.getNum_Pending_Events();
/* 127 */               apPendingEvents += pendingEvents;
/* 128 */               data[indx++] = tmp[0];
/* 129 */               data[indx++] = tmp[1];
/* 130 */               data[indx++] = tmp[2];
/* 131 */               data[indx++] = tmp[3];
/*     */               
/* 133 */               data[indx++] = 35;
/* 134 */               data[indx++] = 4;
/* 135 */               tmp = Functions.intToByteArray(sp13dh.getNum_Pending_Alives(), 4);
/* 136 */               pendingAlives += sp13dh.getNum_Pending_Alives();
/* 137 */               apPendingAlives += pendingAlives;
/* 138 */               data[indx++] = tmp[0];
/* 139 */               data[indx++] = tmp[1];
/* 140 */               data[indx++] = tmp[2];
/* 141 */               data[indx++] = tmp[3];
/*     */               
/* 143 */               data[indx++] = 19;
/* 144 */               data[indx++] = 4;
/* 145 */               tmp = Functions.intToByteArray(sp13dh.getNum_Registered_Modules() - 1, 4);
/* 146 */               regDevices = sp13dh.getNum_Registered_Modules() - 1;
/* 147 */               apRegisteredDevices += sp13dh.getNum_Registered_Modules() - 1;
/* 148 */               data[indx++] = tmp[0];
/* 149 */               data[indx++] = tmp[1];
/* 150 */               data[indx++] = tmp[2];
/* 151 */               data[indx++] = tmp[3];
/*     */               
/* 153 */               data[indx++] = 20;
/* 154 */               data[indx++] = 4;
/* 155 */               tmp = Functions.intToByteArray(sp13dh.getDisableModulesCount(), 4);
/* 156 */               disDevices = sp13dh.getDisableModulesCount();
/* 157 */               apDisabledDevices = sp13dh.getDisableModulesCount();
/* 158 */               data[indx++] = tmp[0];
/* 159 */               data[indx++] = tmp[1];
/* 160 */               data[indx++] = tmp[2];
/* 161 */               data[indx++] = tmp[3];
/*     */             } 
/*     */           } else {
/* 164 */             data[indx++] = 34;
/* 165 */             data[indx++] = 19;
/* 166 */             data[indx++] = (byte)Util.EnumProductIDs.PEGASUS.getProductId();
/*     */           } 
/*     */           
/* 169 */           totalNumConnections = TblPegasusActiveConnections.getInstance().size();
/* 170 */           udpNumConnections = TblActiveUdpConnections.getInstance().size();
/*     */           
/* 172 */           data[indx++] = 21;
/* 173 */           data[indx++] = 4;
/* 174 */           tmp = Functions.intToByteArray(totalNumConnections, 4);
/* 175 */           data[indx++] = tmp[0];
/* 176 */           data[indx++] = tmp[1];
/* 177 */           data[indx++] = tmp[2];
/* 178 */           data[indx++] = tmp[3];
/* 179 */           connDevices = totalNumConnections;
/* 180 */           apConnectedDevices += totalNumConnections;
/*     */           
/* 182 */           data[indx++] = 22;
/* 183 */           data[indx++] = 4;
/* 184 */           connTCPDevices = (totalNumConnections - udpNumConnections < 0) ? 0 : (totalNumConnections - udpNumConnections);
/* 185 */           apConnectedViaTCP += connTCPDevices;
/* 186 */           tmp = Functions.intToByteArray(connTCPDevices, 4);
/* 187 */           data[indx++] = tmp[0];
/* 188 */           data[indx++] = tmp[1];
/* 189 */           data[indx++] = tmp[2];
/* 190 */           data[indx++] = tmp[3];
/*     */           
/* 192 */           data[indx++] = 23;
/* 193 */           data[indx++] = 4;
/* 194 */           connUDPDevices = (udpNumConnections > totalNumConnections) ? totalNumConnections : udpNumConnections;
/* 195 */           apConnectedViaUDP += connUDPDevices;
/* 196 */           tmp = Functions.intToByteArray(connUDPDevices, 4);
/* 197 */           data[indx++] = tmp[0];
/* 198 */           data[indx++] = tmp[1];
/* 199 */           data[indx++] = tmp[2];
/* 200 */           data[indx++] = tmp[3];
/*     */           break;
/*     */         
/*     */         case "GRIFFON":
/* 204 */           if (dbData) {
/* 205 */             SP_013DataHolder sp13dh = GenericDBManager.executeSP_013("GRIFFON");
/* 206 */             if (sp13dh != null) {
/* 207 */               data[indx++] = 34;
/* 208 */               data[indx++] = 43;
/* 209 */               data[indx++] = (byte)Util.EnumProductIDs.GRIFFON_V1.getProductId();
/*     */               
/* 211 */               data[indx++] = 18;
/* 212 */               data[indx++] = 4;
/* 213 */               tmp = Functions.intToByteArray(sp13dh.getNum_Pending_Events(), 4);
/* 214 */               pendingEvents += sp13dh.getNum_Pending_Events();
/* 215 */               apPendingEvents += pendingEvents;
/* 216 */               data[indx++] = tmp[0];
/* 217 */               data[indx++] = tmp[1];
/* 218 */               data[indx++] = tmp[2];
/* 219 */               data[indx++] = tmp[3];
/*     */               
/* 221 */               data[indx++] = 35;
/* 222 */               data[indx++] = 4;
/* 223 */               tmp = Functions.intToByteArray(sp13dh.getNum_Pending_Alives(), 4);
/* 224 */               pendingAlives += sp13dh.getNum_Pending_Alives();
/* 225 */               apPendingAlives += pendingAlives;
/* 226 */               data[indx++] = tmp[0];
/* 227 */               data[indx++] = tmp[1];
/* 228 */               data[indx++] = tmp[2];
/* 229 */               data[indx++] = tmp[3];
/*     */               
/* 231 */               data[indx++] = 19;
/* 232 */               data[indx++] = 4;
/* 233 */               tmp = Functions.intToByteArray(sp13dh.getNum_Registered_Modules() - 1, 4);
/* 234 */               apRegisteredDevices += sp13dh.getNum_Registered_Modules() - 1;
/* 235 */               regDevices = sp13dh.getNum_Registered_Modules() - 1;
/* 236 */               data[indx++] = tmp[0];
/* 237 */               data[indx++] = tmp[1];
/* 238 */               data[indx++] = tmp[2];
/* 239 */               data[indx++] = tmp[3];
/*     */               
/* 241 */               data[indx++] = 20;
/* 242 */               data[indx++] = 4;
/* 243 */               tmp = Functions.intToByteArray(sp13dh.getDisableModulesCount(), 4);
/* 244 */               apDisabledDevices = sp13dh.getDisableModulesCount();
/* 245 */               disDevices = sp13dh.getDisableModulesCount();
/* 246 */               data[indx++] = tmp[0];
/* 247 */               data[indx++] = tmp[1];
/* 248 */               data[indx++] = tmp[2];
/* 249 */               data[indx++] = tmp[3];
/*     */             } 
/*     */           } else {
/* 252 */             data[indx++] = 34;
/* 253 */             data[indx++] = 19;
/* 254 */             data[indx++] = (byte)Util.EnumProductIDs.GRIFFON_V1.getProductId();
/*     */           } 
/*     */           
/* 257 */           totalNumConnections = TblGriffonActiveConnections.getInstance().size();
/* 258 */           udpNumConnections = TblGriffonActiveUdpConnections.getInstance().size();
/*     */           
/* 260 */           data[indx++] = 21;
/* 261 */           data[indx++] = 4;
/* 262 */           tmp = Functions.intToByteArray(totalNumConnections, 4);
/* 263 */           data[indx++] = tmp[0];
/* 264 */           data[indx++] = tmp[1];
/* 265 */           data[indx++] = tmp[2];
/* 266 */           data[indx++] = tmp[3];
/* 267 */           connDevices = totalNumConnections;
/* 268 */           apConnectedDevices += totalNumConnections;
/*     */           
/* 270 */           data[indx++] = 22;
/* 271 */           data[indx++] = 4;
/* 272 */           connTCPDevices = (totalNumConnections - udpNumConnections < 0) ? 0 : (totalNumConnections - udpNumConnections);
/* 273 */           apConnectedViaTCP += connTCPDevices;
/* 274 */           tmp = Functions.intToByteArray(connTCPDevices, 4);
/* 275 */           data[indx++] = tmp[0];
/* 276 */           data[indx++] = tmp[1];
/* 277 */           data[indx++] = tmp[2];
/* 278 */           data[indx++] = tmp[3];
/*     */           
/* 280 */           data[indx++] = 23;
/* 281 */           data[indx++] = 4;
/* 282 */           connUDPDevices = (udpNumConnections > totalNumConnections) ? totalNumConnections : udpNumConnections;
/* 283 */           apConnectedViaUDP += connUDPDevices;
/* 284 */           tmp = Functions.intToByteArray(connUDPDevices, 4);
/* 285 */           data[indx++] = tmp[0];
/* 286 */           data[indx++] = tmp[1];
/* 287 */           data[indx++] = tmp[2];
/* 288 */           data[indx++] = tmp[3];
/*     */           break;
/*     */         
/*     */         case "MERCURIUS":
/* 292 */           if (dbData) {
/* 293 */             SP_013DataHolder sp13dh = GenericDBManager.executeSP_013("MERCURIUS");
/* 294 */             if (sp13dh != null) {
/* 295 */               data[indx++] = 34;
/* 296 */               data[indx++] = 43;
/* 297 */               data[indx++] = (byte)Util.EnumProductIDs.MERCURIUS.getProductId();
/*     */               
/* 299 */               data[indx++] = 18;
/* 300 */               data[indx++] = 4;
/* 301 */               tmp = Functions.intToByteArray(sp13dh.getNum_Pending_Events(), 4);
/* 302 */               pendingEvents += sp13dh.getNum_Pending_Events();
/* 303 */               apPendingEvents += pendingEvents;
/* 304 */               data[indx++] = tmp[0];
/* 305 */               data[indx++] = tmp[1];
/* 306 */               data[indx++] = tmp[2];
/* 307 */               data[indx++] = tmp[3];
/*     */               
/* 309 */               data[indx++] = 35;
/* 310 */               data[indx++] = 4;
/* 311 */               tmp = Functions.intToByteArray(sp13dh.getNum_Pending_Alives(), 4);
/* 312 */               pendingAlives += sp13dh.getNum_Pending_Alives();
/* 313 */               apPendingAlives += pendingAlives;
/* 314 */               data[indx++] = tmp[0];
/* 315 */               data[indx++] = tmp[1];
/* 316 */               data[indx++] = tmp[2];
/* 317 */               data[indx++] = tmp[3];
/*     */               
/* 319 */               data[indx++] = 19;
/* 320 */               data[indx++] = 4;
/* 321 */               tmp = Functions.intToByteArray(sp13dh.getNum_Registered_Modules() - 1, 4);
/* 322 */               apRegisteredDevices += sp13dh.getNum_Registered_Modules() - 1;
/* 323 */               regDevices = sp13dh.getNum_Registered_Modules() - 1;
/* 324 */               data[indx++] = tmp[0];
/* 325 */               data[indx++] = tmp[1];
/* 326 */               data[indx++] = tmp[2];
/* 327 */               data[indx++] = tmp[3];
/*     */               
/* 329 */               data[indx++] = 20;
/* 330 */               data[indx++] = 4;
/* 331 */               tmp = Functions.intToByteArray(sp13dh.getDisableModulesCount(), 4);
/* 332 */               apDisabledDevices = sp13dh.getDisableModulesCount();
/* 333 */               disDevices = sp13dh.getDisableModulesCount();
/* 334 */               data[indx++] = tmp[0];
/* 335 */               data[indx++] = tmp[1];
/* 336 */               data[indx++] = tmp[2];
/* 337 */               data[indx++] = tmp[3];
/*     */             } 
/*     */           } else {
/* 340 */             data[indx++] = 34;
/* 341 */             data[indx++] = 19;
/* 342 */             data[indx++] = (byte)Util.EnumProductIDs.MERCURIUS.getProductId();
/*     */           } 
/*     */           
/* 345 */           totalNumConnections = TblMercuriusActiveConnections.getInstance().size();
/* 346 */           udpNumConnections = TblMercuriusAVLActiveUdpConnections.getInstance().size();
/*     */           
/* 348 */           data[indx++] = 21;
/* 349 */           data[indx++] = 4;
/* 350 */           tmp = Functions.intToByteArray(totalNumConnections, 4);
/* 351 */           data[indx++] = tmp[0];
/* 352 */           data[indx++] = tmp[1];
/* 353 */           data[indx++] = tmp[2];
/* 354 */           data[indx++] = tmp[3];
/* 355 */           connDevices = totalNumConnections;
/* 356 */           apConnectedDevices += totalNumConnections;
/*     */           
/* 358 */           data[indx++] = 22;
/* 359 */           data[indx++] = 4;
/* 360 */           connTCPDevices = (totalNumConnections - udpNumConnections < 0) ? 0 : (totalNumConnections - udpNumConnections);
/* 361 */           apConnectedViaTCP += connTCPDevices;
/* 362 */           tmp = Functions.intToByteArray(connTCPDevices, 4);
/* 363 */           data[indx++] = tmp[0];
/* 364 */           data[indx++] = tmp[1];
/* 365 */           data[indx++] = tmp[2];
/* 366 */           data[indx++] = tmp[3];
/*     */           
/* 368 */           data[indx++] = 23;
/* 369 */           data[indx++] = 4;
/* 370 */           connUDPDevices = (udpNumConnections > totalNumConnections) ? totalNumConnections : udpNumConnections;
/* 371 */           apConnectedViaUDP += connUDPDevices;
/* 372 */           tmp = Functions.intToByteArray(connUDPDevices, 4);
/* 373 */           data[indx++] = tmp[0];
/* 374 */           data[indx++] = tmp[1];
/* 375 */           data[indx++] = tmp[2];
/* 376 */           data[indx++] = tmp[3];
/*     */           break;
/*     */       } 
/* 379 */       if (dbData) {
/* 380 */         ZeusSettingsDBManager.updateZeusLiveData(GenericDBManager.getProductIdBySchemaName(schemaName), regDevices, disDevices, connDevices, connTCPDevices, connUDPDevices, pendingEvents, pendingAlives);
/*     */       }
/*     */     } 
/*     */     
/* 384 */     if (dbData) {
/* 385 */       data[indx++] = 34;
/* 386 */       data[indx++] = 43;
/* 387 */       data[indx++] = (byte)Util.EnumProductIDs.ZEUS.getProductId();
/*     */       
/* 389 */       data[indx++] = 18;
/* 390 */       data[indx++] = 4;
/* 391 */       tmp = Functions.intToByteArray(apPendingEvents, 4);
/* 392 */       data[indx++] = tmp[0];
/* 393 */       data[indx++] = tmp[1];
/* 394 */       data[indx++] = tmp[2];
/* 395 */       data[indx++] = tmp[3];
/*     */       
/* 397 */       data[indx++] = 35;
/* 398 */       data[indx++] = 4;
/* 399 */       tmp = Functions.intToByteArray(apPendingAlives, 4);
/* 400 */       data[indx++] = tmp[0];
/* 401 */       data[indx++] = tmp[1];
/* 402 */       data[indx++] = tmp[2];
/* 403 */       data[indx++] = tmp[3];
/*     */       
/* 405 */       data[indx++] = 19;
/* 406 */       data[indx++] = 4;
/* 407 */       tmp = Functions.intToByteArray(apRegisteredDevices, 4);
/* 408 */       data[indx++] = tmp[0];
/* 409 */       data[indx++] = tmp[1];
/* 410 */       data[indx++] = tmp[2];
/* 411 */       data[indx++] = tmp[3];
/*     */       
/* 413 */       data[indx++] = 20;
/* 414 */       data[indx++] = 4;
/* 415 */       tmp = Functions.intToByteArray(apDisabledDevices, 4);
/* 416 */       data[indx++] = tmp[0];
/* 417 */       data[indx++] = tmp[1];
/* 418 */       data[indx++] = tmp[2];
/* 419 */       data[indx++] = tmp[3];
/*     */       
/* 421 */       this.accessDB = System.currentTimeMillis() + 15000L;
/*     */     } else {
/* 423 */       data[indx++] = 34;
/* 424 */       data[indx++] = 19;
/* 425 */       data[indx++] = (byte)Util.EnumProductIDs.ZEUS.getProductId();
/*     */     } 
/*     */     
/* 428 */     data[indx++] = 21;
/* 429 */     data[indx++] = 4;
/* 430 */     tmp = Functions.intToByteArray(apConnectedDevices, 4);
/* 431 */     data[indx++] = tmp[0];
/* 432 */     data[indx++] = tmp[1];
/* 433 */     data[indx++] = tmp[2];
/* 434 */     data[indx++] = tmp[3];
/*     */     
/* 436 */     data[indx++] = 22;
/* 437 */     data[indx++] = 4;
/* 438 */     tmp = Functions.intToByteArray(apConnectedViaTCP, 4);
/* 439 */     data[indx++] = tmp[0];
/* 440 */     data[indx++] = tmp[1];
/* 441 */     data[indx++] = tmp[2];
/* 442 */     data[indx++] = tmp[3];
/*     */     
/* 444 */     data[indx++] = 23;
/* 445 */     data[indx++] = 4;
/* 446 */     tmp = Functions.intToByteArray(apConnectedViaUDP, 4);
/* 447 */     data[indx++] = tmp[0];
/* 448 */     data[indx++] = tmp[1];
/* 449 */     data[indx++] = tmp[2];
/* 450 */     data[indx++] = tmp[3];
/*     */     
/* 452 */     if (dbData) {
/* 453 */       ZeusSettingsDBManager.updateZeusLiveData(Util.EnumProductIDs.ZEUS.getProductId(), apRegisteredDevices, apDisabledDevices, apConnectedDevices, apConnectedViaTCP, apConnectedViaUDP, apPendingEvents, apPendingAlives);
/*     */     }
/*     */     
/* 456 */     data[indx++] = 16;
/* 457 */     data[indx++] = (byte)strRuntime.length();
/* 458 */     System.arraycopy(strRuntime.getBytes(), 0, data, indx, strRuntime.length());
/* 459 */     indx += strRuntime.length();
/* 460 */     data[indx++] = 17;
/* 461 */     data[indx++] = 2;
/* 462 */     tmp = Functions.get2ByteArrayFromInt(Main.getCurrentThreadCount());
/* 463 */     data[indx++] = tmp[0];
/* 464 */     data[indx++] = tmp[1];
/*     */     
/* 466 */     if (iData != null) {
/* 467 */       for (Iterator<Byte> iterator = iData.iterator(); iterator.hasNext(); ) { byte b = ((Byte)iterator.next()).byteValue();
/* 468 */         data[indx++] = b; }
/*     */     
/*     */     }
/*     */     
/* 472 */     int crcCalc = CRC16.calculate(data, 0, bufferLength - 2, 65535);
/* 473 */     tmp = Functions.get2ByteArrayFromInt(crcCalc);
/* 474 */     data[indx++] = tmp[1];
/* 475 */     data[indx++] = tmp[0];
/* 476 */     return data;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\ui\MonitoringViewer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */