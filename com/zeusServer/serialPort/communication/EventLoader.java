/*     */ package com.zeusServer.serialPort.communication;
/*     */ 
/*     */ import com.zeus.settings.beans.Util;
/*     */ import com.zeusServer.DBManagers.GriffonDBManager;
/*     */ import com.zeusServer.DBManagers.MercuriusDBManager;
/*     */ import com.zeusServer.DBManagers.PegasusDBManager;
/*     */ import com.zeusServer.DBManagers.ZeusSettingsDBManager;
/*     */ import com.zeusServer.util.Enums;
/*     */ import com.zeusServer.util.Functions;
/*     */ import com.zeusServer.util.GlobalVariables;
/*     */ import com.zeusServer.util.LocaleMessage;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.PriorityBlockingQueue;
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
/*     */ public class EventLoader
/*     */   implements Runnable
/*     */ {
/*     */   public boolean flag;
/*     */   private static long nextPrintTimeSpentLoadEventsFromDb;
/*  41 */   public static final List<String> availableProducts = new ArrayList<>(4);
/*  42 */   public static ConcurrentHashMap<String, ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>>> nonProcessedEventData = new ConcurrentHashMap<>();
/*     */   
/*     */   static {
/*  45 */     if (availableProducts.isEmpty()) {
/*  46 */       availableProducts.add("PEGASUS");
/*  47 */       availableProducts.add("GRIFFON");
/*  48 */       availableProducts.add("MERCURIUS");
/*  49 */       availableProducts.add("ZEUSSETTINGS");
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*     */     try {
/*  56 */       Thread.currentThread().setPriority(10);
/*  57 */       while (this.flag) {
/*  58 */         for (String product : availableProducts) {
/*  59 */           long startTime = System.currentTimeMillis();
/*  60 */           if (GlobalVariables.dbCurrentStatus == Enums.enumDbStatus.SPACE_RECLAIM) {
/*  61 */             Thread.sleep(100L);
/*     */             continue;
/*     */           } 
/*  64 */           List<EventDataHolder> edhList = null;
/*     */           try {
/*  66 */             if (product.equalsIgnoreCase("PEGASUS") && getNonProcessedEventCountByProduct(product) <= 0) {
/*  67 */               edhList = PegasusDBManager.getNonProcessedEvents();
/*  68 */             } else if (product.equalsIgnoreCase("GRIFFON") && getNonProcessedEventCountByProduct(product) <= 0) {
/*  69 */               edhList = GriffonDBManager.getNonProcessedEvents();
/*  70 */             } else if (product.equalsIgnoreCase("MERCURIUS") && getNonProcessedEventCountByProduct(product) <= 0) {
/*  71 */               edhList = MercuriusDBManager.getNonProcessedEvents();
/*  72 */             } else if (product.equalsIgnoreCase("ZEUSSETTINGS") && getNonProcessedEventCountByProduct(product) <= 0) {
/*  73 */               edhList = ZeusSettingsDBManager.getNonProcessedZeusEvents();
/*     */             } 
/*  75 */             if (edhList != null) {
/*  76 */               LinkedHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> eData = new LinkedHashMap<>();
/*  77 */               for (EventDataHolder edh : edhList) {
/*  78 */                 if (eData.containsKey(Integer.valueOf(edh.getIdGroup()))) {
/*  79 */                   ((PriorityBlockingQueue<EventDataHolder>)eData.get(Integer.valueOf(edh.getIdGroup()))).add(edh); continue;
/*     */                 } 
/*  81 */                 PriorityBlockingQueue<EventDataHolder> eList = new PriorityBlockingQueue<>();
/*  82 */                 eList.add(edh);
/*  83 */                 eData.put(Integer.valueOf(edh.getIdGroup()), eList);
/*     */               } 
/*     */ 
/*     */               
/*  87 */               if (nonProcessedEventData.containsKey(product)) {
/*  88 */                 for (Map.Entry<Integer, PriorityBlockingQueue<EventDataHolder>> entry : eData.entrySet()) {
/*  89 */                   if (((ConcurrentHashMap)nonProcessedEventData.get(product)).containsKey(entry.getKey())) {
/*  90 */                     Iterator<EventDataHolder> iterator = ((PriorityBlockingQueue)eData.get(entry.getKey())).iterator();
/*  91 */                     while (iterator.hasNext()) {
/*  92 */                       EventDataHolder event = iterator.next();
/*  93 */                       ((PriorityBlockingQueue<EventDataHolder>)((ConcurrentHashMap)nonProcessedEventData.get(product)).get(entry.getKey())).add(event);
/*     */                     }  continue;
/*     */                   } 
/*  96 */                   PriorityBlockingQueue<EventDataHolder> pbqEvents = new PriorityBlockingQueue<>();
/*  97 */                   Iterator<EventDataHolder> itr = ((PriorityBlockingQueue)eData.get(entry.getKey())).iterator();
/*  98 */                   while (itr.hasNext()) {
/*  99 */                     EventDataHolder event = itr.next();
/* 100 */                     pbqEvents.add(event);
/*     */                   } 
/* 102 */                   ((ConcurrentHashMap)nonProcessedEventData.get(product)).put(entry.getKey(), pbqEvents);
/*     */                 } 
/*     */               } else {
/*     */                 
/* 106 */                 ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> pEMap = new ConcurrentHashMap<>();
/* 107 */                 nonProcessedEventData.put(product, pEMap);
/* 108 */                 for (Map.Entry<Integer, PriorityBlockingQueue<EventDataHolder>> entry : eData.entrySet()) {
/* 109 */                   PriorityBlockingQueue<EventDataHolder> pbqEvents = new PriorityBlockingQueue<>();
/* 110 */                   pbqEvents.addAll(entry.getValue());
/* 111 */                   pEMap.put(entry.getKey(), pbqEvents);
/*     */                 } 
/*     */               } 
/* 114 */               if (nextPrintTimeSpentLoadEventsFromDb < System.currentTimeMillis()) {
/* 115 */                 Functions.printMessage(Util.EnumProductIDs.ZEUS, String.format(LocaleMessage.getLocaleMessage("Time_spent_to_load_events_from_Database"), new Object[] { product, Float.valueOf((float)(System.currentTimeMillis() - startTime) / 1000.0F) }), Enums.EnumMessagePriority.LOW, null, null);
/*     */               }
/*     */             } 
/* 118 */           } catch (InterruptedException|java.sql.SQLException ex) {
/* 119 */             ex.printStackTrace();
/*     */           } 
/*     */         } 
/* 122 */         if (nextPrintTimeSpentLoadEventsFromDb < System.currentTimeMillis()) {
/* 123 */           nextPrintTimeSpentLoadEventsFromDb = System.currentTimeMillis() + 5000L;
/*     */         }
/*     */         try {
/* 126 */           Thread.sleep(1000L);
/* 127 */         } catch (InterruptedException interruptedException) {}
/*     */       }
/*     */     
/*     */     }
/* 131 */     catch (InterruptedException ex) {
/* 132 */       ex.printStackTrace();
/*     */     } finally {
/* 134 */       this.flag = false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public int getNonProcessedEventCountByProduct(String product) {
/* 139 */     int cnt = 0;
/* 140 */     synchronized (nonProcessedEventData) {
/* 141 */       if (nonProcessedEventData.containsKey(product)) {
/* 142 */         ConcurrentHashMap<Integer, PriorityBlockingQueue<EventDataHolder>> pMap = nonProcessedEventData.get(product);
/* 143 */         for (Integer i : pMap.keySet()) {
/* 144 */           cnt += ((PriorityBlockingQueue)pMap.get(i)).size();
/*     */         }
/*     */       } 
/*     */     } 
/* 148 */     return cnt;
/*     */   }
/*     */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\serialPort\communication\EventLoader.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */