package com.openexchange.logging.tracking;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public interface ScopedTrackingConfiguration {

	boolean isDebugEnabled();

	boolean isErrorEnabled();

	boolean isFatalEnabled();

	boolean isInfoEnabled();

	boolean isTraceEnabled();

	boolean isWarnEnabled();

	Log getLog();

}
