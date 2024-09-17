/*    */ package com.zeusServer.util;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import javax.xml.parsers.DocumentBuilder;
/*    */ import javax.xml.parsers.DocumentBuilderFactory;
/*    */ import javax.xml.parsers.ParserConfigurationException;
/*    */ import org.w3c.dom.Document;
/*    */ import org.w3c.dom.NamedNodeMap;
/*    */ import org.w3c.dom.Node;
/*    */ import org.w3c.dom.NodeList;
/*    */ import org.xml.sax.SAXException;
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
/*    */ public class ZeusPathCfgUtil
/*    */ {
/*    */   public static final String ZEUS_CFG_PATH_FILE_NAME = "ZeusPathCfg.xml";
/*    */   
/*    */   public static String getZeusCfgPath() throws ParserConfigurationException, SAXException, IOException {
/* 31 */     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
/* 32 */     DocumentBuilder db = dbf.newDocumentBuilder();
/* 33 */     Document doc = db.parse(new File("ZeusPathCfg.xml"));
/* 34 */     NodeList nl = doc.getElementsByTagName("zeus");
/* 35 */     if (nl.getLength() == 0) {
/* 36 */       System.out.println("It was not possible to locate the element 'zeus' in the configuration file (ZeusPathCfg.xml)");
/* 37 */     } else if (nl.getLength() > 1) {
/* 38 */       System.out.println("It was located more than one element 'general' in the configuration file (ZeusPathCfg.xml)");
/*    */     } else {
/* 40 */       NamedNodeMap nnm = nl.item(0).getAttributes();
/* 41 */       Node node = nnm.getNamedItem("cfgPath");
/* 42 */       if (node == null) {
/* 43 */         System.out.println("It was not possible to read the parameter 'cfgPath' of the configuration file (ZeusPathCfg.xml)");
/*    */       } else {
/* 45 */         return node.getNodeValue();
/*    */       } 
/*    */     } 
/* 48 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServe\\util\ZeusPathCfgUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */