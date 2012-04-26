package com.openexchange.logging.tracking;

import org.apache.commons.logging.Log;

public class TrackingFileLog implements Log {

	private ScopedTrackingConfiguration config;
	private Log delegate;

	public TrackingFileLog(ScopedTrackingConfiguration config, Log delegate) {
		this.config = config;
		this.delegate = delegate;
	}

	public void debug(Object msg) {
		delegate.debug(msg);

		if (!config.isDebugEnabled()) {
			return;
		}
		
		config.getLog().debug(msg);
	}

	public void debug(Object msg, Throwable exc) {
		delegate.debug(msg, exc);

		if (!config.isDebugEnabled()) {
			return;
		}

		config.getLog().debug(msg, exc);
	}

	public void error(Object msg) {
		delegate.error(msg);

		if (!config.isErrorEnabled()) {
			return;
		}

		config.getLog().error(msg);
	}

	public void error(Object msg, Throwable exc) {
		delegate.error(msg, exc);

		if (!config.isErrorEnabled()) {
			return;
		}

		config.getLog().error(msg, exc);
	}

	public void fatal(Object msg) {
		delegate.fatal(msg);

		if (!config.isFatalEnabled()) {
			return;
		}

		config.getLog().fatal(msg);

	}

	public void fatal(Object msg, Throwable exc) {
		delegate.fatal(msg, exc);

		if (!config.isFatalEnabled()) {
			return;
		}

		config.getLog().fatal(msg, exc);
	}

	public void info(Object msg) {
		delegate.info(msg);

		if (!config.isInfoEnabled()) {
			return;
		}

		config.getLog().info(msg);
	}

	public void info(Object msg, Throwable exc) {
		delegate.info(msg, exc);

		if (!config.isInfoEnabled()) {
			return;
		}

		config.getLog().info(msg, exc);
	}

	public void trace(Object msg) {
		delegate.trace(msg);

		if (!config.isTraceEnabled()) {
			return;
		}

		config.getLog().trace(msg);
	}

	public void trace(Object msg, Throwable exc) {
		delegate.trace(msg, exc);

		if (!config.isTraceEnabled()) {
			return;
		}

		config.getLog().trace(msg, exc);
	}

	public void warn(Object msg) {
		delegate.warn(msg);

		if (!config.isWarnEnabled()) {
			return;
		}
		
		config.getLog().warn(msg);
	}

	public void warn(Object msg, Throwable exc) {
		delegate.warn(msg, exc);

		if (!config.isWarnEnabled()) {
			return;
		}

		config.getLog().warn(msg, exc);
	}

	public boolean isDebugEnabled() {
		return config.isDebugEnabled();
	}

	public boolean isErrorEnabled() {
		return config.isErrorEnabled() || delegate.isErrorEnabled();
	}

	public boolean isFatalEnabled() {
		return config.isFatalEnabled() || delegate.isFatalEnabled();
	}

	public boolean isInfoEnabled() {
		return config.isInfoEnabled() || delegate.isInfoEnabled();
	}

	public boolean isTraceEnabled() {
		return config.isTraceEnabled() || delegate.isTraceEnabled();
	}

	public boolean isWarnEnabled() {
		return config.isWarnEnabled() || delegate.isWarnEnabled();
	}
}
