package com.openexchange.logging.tracking;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;

import com.openexchange.tools.session.SessionHolder;

public class SessionBasedTrackingConfiguration implements TrackingConfiguration {
	
	private SessionHolder sh;
	private GlobalTrackingConfiguration config;
	private String[] className;
	
	public SessionBasedTrackingConfiguration(String className, GlobalTrackingConfiguration config, SessionHolder sh) {
		this.sh = sh;
		this.config = config;
		this.className = className.split("\\.");
	}

	public boolean isDebugEnabled() {
		return config.isDebugEnabled(className, sh.getSessionObject());
	}

	public boolean isErrorEnabled() {
		return config.isErrorEnabled(className, sh.getSessionObject());
	}

	public boolean isFatalEnabled() {
		return config.isFatalEnabled(className, sh.getSessionObject());
	}

	public boolean isInfoEnabled() {
		return config.isInfoEnabled(className, sh.getSessionObject());
	}

	public boolean isTraceEnabled() {
		return config.isTraceEnabled(className, sh.getSessionObject());
	}

	public boolean isWarnEnabled() {
		return config.isWarnEnabled(className, sh.getSessionObject());
	}

	public Log getLog() {
		return config.getLog(className, sh.getSessionObject());
	}
}
