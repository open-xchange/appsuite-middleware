package com.openexchange.log;

public class LogFactory {
	public static org.apache.commons.logging.Log getLog(Class<?> klass) {
		return Log.valueOf(com.openexchange.exception.Log.valueOf(com.openexchange.log.LogFactory.getLog(klass)));
	}

	public static org.apache.commons.logging.Log getLog(String def) {
		return Log.valueOf(com.openexchange.exception.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(def)));
	}
}
