package com.nomadsoft.chat.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	private static Logger logger = null;
	private static FileHandler fileHandler = null;
	private static String logFileName = "";
	public static void setLoggerName(String value) {
		logFileName = value;
	}
	
	private static Logger getLogger() {
		if(logger == null) { 
			logger = Logger.getLogger("com.nomadsoft.chat");
			
			try {
				fileHandler = new FileHandler(logFileName, 1024000, 3);//"C:/TEMP/log/pchat.log");

			} catch (IOException e) {
			}
			
			logger.addHandler(fileHandler);
			logger.setLevel(Level.ALL);
		}
		
		return logger;
	}
	
	public static void i(String msg) {
		getLogger().info(msg);
	}
	
	public static void w(String msg) {
		getLogger().warning(msg);
	}

	public static void l(Level level, String msg, Throwable error) {
		getLogger().log(level, msg, error);
	}
}
