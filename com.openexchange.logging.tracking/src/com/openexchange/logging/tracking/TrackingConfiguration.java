package com.openexchange.logging.tracking;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public interface TrackingConfiguration {

	boolean isDebugEnabled();

	boolean isErrorEnabled();

	boolean isFatalEnabled();

	boolean isInfoEnabled();

	boolean isTraceEnabled();

	boolean isWarnEnabled();

	Log getLog();

}
