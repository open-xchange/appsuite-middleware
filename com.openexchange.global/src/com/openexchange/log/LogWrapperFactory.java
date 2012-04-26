package com.openexchange.log;

public interface LogWrapperFactory {
	public org.apache.commons.logging.Log wrap(String className, org.apache.commons.logging.Log log);
}
