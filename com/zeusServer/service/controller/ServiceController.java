package com.zeusServer.service.controller;

public interface ServiceController {
  boolean startService() throws Exception;
  
  boolean stopService() throws Exception;
  
  int getServiceStatus() throws Exception;
}


/* Location:              C:\Users\P042508\Desktop\jd-gui-windows-1.6.6\ZeusService.jar!\com\zeusServer\service\controller\ServiceController.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */