/*    */ package com.zeusServer.DBPools;
/*    */ 
/*    */ import com.zeusServer.util.ZeusServerCfg;
/*    */ import org.apache.tomcat.jdbc.pool.DataSource;
/*    */ import org.apache.tomcat.jdbc.pool.PoolConfiguration;
/*    */ import org.apache.tomcat.jdbc.pool.PoolProperties;
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
/*    */ public class PegasusPool
/*    */ {
/* 23 */   private static PoolProperties p = new PoolProperties();
/* 24 */   private static DataSource datasource = new DataSource();
/*    */   
/*    */   static {
/* 27 */     p.setUrl("jdbc:derby://" + ZeusServerCfg.getInstance().getDbServer() + ":" + ZeusServerCfg.getInstance().getDbServerPort() + "/" + ZeusServerCfg.getInstance().getDbFile() + ";upgrade=true");
/* 28 */     p.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
/* 29 */     p.setUsername(ZeusServerCfg.getInstance().getDbUser());
/* 30 */     p.setPassword(ZeusServerCfg.getInstance().getDbPass());
/* 31 */     p.setJmxEnabled(false);
/* 32 */     p.setTestWhileIdle(false);
/* 33 */     p.setTestOnBorrow(true);
/* 34 */     p.setValidationQuery("SELECT 1 from SYSIBM.SYSDUMMY1");
/* 35 */     p.setTestOnReturn(true);
/* 36 */     p.setValidationInterval(120000L);
/* 37 */     p.setTimeBetweenEvictionRunsMillis(120000);
/* 38 */     p.setMaxActive(2000);
/* 39 */     p.setInitialSize(100);
/* 40 */     p.setMaxWait(120000);
/* 41 */     p.setRemoveAbandonedTimeout(120);
/* 42 */     p.setMinEvictableIdleTimeMillis(1000);
/* 43 */     p.setMinIdle(10);
/* 44 */     p.setMaxIdle(100);
/* 45 */     p.setLogAbandoned(false);
/* 46 */     p.setRemoveAbandoned(true);
/* 47 */     p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
/* 48 */     datasource.setPoolProperties((PoolConfiguration)p);
/*    */   }
/*    */   
/*    */   public static DataSource getPegasusConnectionPool() {
/* 52 */     return datasource;
/*    */   }
/*    */   
/*    */   public static void closeConnectionPool() {
/* 56 */     datasource.close(true);
/*    */   }
/*    */ }


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\DBPools\PegasusPool.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */