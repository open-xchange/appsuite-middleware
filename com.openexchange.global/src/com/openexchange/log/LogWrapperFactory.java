package com.openexchange.log;

public interface LogWrapperFactory {
	public org.apache.commons.logging.Log wrap(org.apache.commons.logging.Log log);
}
