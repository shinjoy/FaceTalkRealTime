package com.nomadsoft.chat.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

public class Config {
	                 
	//String urlSql = "jdbc:jtds:sqlserver://218.234.17.222:1433;tds=8.0;lastupdatecount=true;DatabaseName=POLICE";
	//String dbUserName = "mars";
	//String dbPass = "shak05em03";
	String urlSql = "jdbc:jtds:sqlserver://218.234.17.164:1433;tds=8.0;lastupdatecount=true;DatabaseName=FaceTalk";
	String dbUserName = "promise";
	String dbPass = "facepro$$$";

	String serverId = "";
	String runNotice = "0";
	String logFileName = "";
	int checkLogoutUser= 0;
	
	String apns_path="";
	String apns_pass="";
	int production=0;
	int cntNotice=300;
	int intarvalNotice=30000;
	String gcm_api_key="AIzaSyCXHIIAKVPzCHZzFnF404IzP0SZkj12mQc";
	String gcm_pkg_id="com.nomadsoft.facetalk";	
	
	public String getGcm_api_key() {
		return gcm_api_key;
	}
	public String getGcm_pkg_id() {
		return gcm_pkg_id;
	}	
	public String getUrlSql() {
		return urlSql;
	}
	public void setUrlSql(String urlSql) {
		this.urlSql = urlSql;
	}
	public String getDbUserName() {
		return dbUserName;
	}
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}
	public String getDbPass() {
		return dbPass;
	}
	public void setDbPass(String dbPass) {
		this.dbPass = dbPass;
	}

	public String getRunNotice() {
		return runNotice;
	}
	public void setRunNotice(String runNotice) {
		this.runNotice = runNotice;
	}
	


	static Config instance = null;	
	public static Config getInstance() {
		
		if(instance == null) {
			instance = new Config();
		}
		
		return instance;
	}
	public Config() {
		Properties prop = new Properties();
		
		File jarPath=new File(Config.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String propertiesPath=jarPath.getParentFile().getAbsolutePath();
		System.out.println(" propertiesPath-"+propertiesPath);

		
		//String fileName = "config.properties";
		//InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
		try {
			FileInputStream fInput = new FileInputStream(propertiesPath+"/config.properties");
			prop.load(fInput);
			//prop.load(inputStream);
						
			urlSql = prop.getProperty("urlSql");
			dbUserName = prop.getProperty("dbUserName");
			dbPass = prop.getProperty("dbPass");
			serverId = prop.getProperty("serverId");
			runNotice = prop.getProperty("runNotice");
			logFileName = prop.getProperty("logFileName");
			String checkLogoutUserStr = prop.getProperty("checkLogoutUser");	
			checkLogoutUser = Integer.valueOf(checkLogoutUserStr);
			
	
			apns_path = prop.getProperty("apns_path");
			apns_pass = prop.getProperty("apns_pass");
			production= Integer.valueOf(prop.getProperty("production"));
			
			cntNotice = Integer.valueOf(prop.getProperty("cntNotice"));
			intarvalNotice= Integer.valueOf(prop.getProperty("intarvalNotice"));
			
			Properties pidProp = new Properties();
			FileOutputStream fOutput = new FileOutputStream(propertiesPath+"/pid.properties");
			int pid = getProcessPidImpl();
			pidProp.setProperty("pid", String.valueOf(pid));
			pidProp.store(fOutput, "");
			fOutput.close();
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public int getCntNotice() {
		return cntNotice;
	}
	public int getIntarvalNotice() {
		return intarvalNotice;
	}
	private static int getProcessPidImpl() throws Exception {
	   RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	  
	   Field jvmField = runtimeMXBean.getClass().getDeclaredField("jvm");
	   jvmField.setAccessible(true);
	  
	   Object vmManagement = jvmField.get(runtimeMXBean); 
	   Method getProcessIdMethod = vmManagement.getClass().getDeclaredMethod("getProcessId");
	  
	   getProcessIdMethod.setAccessible(true);
	   return (Integer) getProcessIdMethod.invoke(vmManagement);
	}
	
	public int getCheckLogoutUser() {
		return checkLogoutUser;
	}
	public void setCheckLogoutUser(int checkLogoutUser) {
		this.checkLogoutUser = checkLogoutUser;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public String getLogFileName() {
		return logFileName;
	}
	
	public String getApns_path() {
		return apns_path;
	}
	public void setApns_path(String apns_path) {
		this.apns_path = apns_path;
	}
	public String getApns_pass() {
		return apns_pass;
	}
	public void setApns_pass(String apns_pass) {
		this.apns_pass = apns_pass;
	}
	public int getProduction() {
		return production;
	}
	public void setProduction(int production) {
		this.production = production;
	}

}
