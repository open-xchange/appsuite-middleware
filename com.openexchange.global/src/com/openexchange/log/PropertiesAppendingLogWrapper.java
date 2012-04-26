package com.openexchange.log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

import com.openexchange.log.LogPropertyName.LogLevel;

public class PropertiesAppendingLogWrapper implements Log {

	private org.apache.commons.logging.Log delegate;

	public PropertiesAppendingLogWrapper(org.apache.commons.logging.Log delegate) {
		this.delegate = delegate;
	}

	public void debug(Object arg0, Throwable arg1) {
		delegate.debug(appendProperties(arg0.toString(), LogLevel.DEBUG), arg1);
	}

	public void debug(Object arg0) {
		delegate.debug(appendProperties(arg0.toString(), LogLevel.DEBUG));
	}

	public void error(Object arg0, Throwable arg1) {
		delegate.error(appendProperties(arg0.toString(), LogLevel.ERROR), arg1);
	}

	public void error(Object arg0) {
		delegate.error(appendProperties(arg0.toString(), LogLevel.ERROR));
	}

	public void fatal(Object arg0, Throwable arg1) {
		delegate.fatal(appendProperties(arg0.toString(), LogLevel.FATAL), arg1);
	}

	public void fatal(Object arg0) {
		delegate.fatal(appendProperties(arg0.toString(), LogLevel.FATAL));
	}

	public void info(Object arg0, Throwable arg1) {
		delegate.info(appendProperties(arg0.toString(), LogLevel.INFO), arg1);
	}

	public void info(Object arg0) {
		delegate.info(appendProperties(arg0.toString(), LogLevel.INFO));
	}
	
	public void trace(Object arg0, Throwable arg1) {
		delegate.trace(appendProperties(arg0.toString(), LogLevel.TRACE), arg1);
	}

	public void trace(Object arg0) {
		delegate.trace(appendProperties(arg0.toString(), LogLevel.TRACE));
	}

	public void warn(Object arg0, Throwable arg1) {
		delegate.warn(appendProperties(arg0.toString(), LogLevel.WARNING), arg1);
	}

	public void warn(Object arg0) {
		delegate.warn(appendProperties(arg0.toString(), LogLevel.WARNING));
	}

	public boolean isDebugEnabled() {
		return delegate.isDebugEnabled();
	}

	public boolean isErrorEnabled() {
		return delegate.isErrorEnabled();
	}

	public boolean isFatalEnabled() {
		return delegate.isFatalEnabled();
	}

	public boolean isInfoEnabled() {
		return delegate.isInfoEnabled();
	}

	public boolean isTraceEnabled() {
		return delegate.isTraceEnabled();
	}

	public boolean isWarnEnabled() {
		return delegate.isWarnEnabled();
	}

	
	public String appendProperties(String message,
			LogLevel logLevel) {
		if (!LogProperties.isEnabled()) {
			return message;
		}
		Props logProps = LogProperties.optLogProperties();
		if (logProps == null) {
			return message;
		}
		StringBuilder sb = new StringBuilder(message);
		final Map<String, Object> properties = logProps.getMap();
		if (properties == null) {
			return message;
		}
		if (null != properties) {
			final Map<String, String> sorted = new TreeMap<String, String>();
			final List<LogPropertyName> names = LogProperties
					.getPropertyNames();
			final Set<String> alreadyLogged;
			if (names.isEmpty()) {
				alreadyLogged = Collections.emptySet();
			} else {
				alreadyLogged = new HashSet<String>(names.size());
				for (final LogPropertyName name : names) {
					if (name.implies(logLevel)) {
						final String propertyName = name.getPropertyName();
						alreadyLogged.add(propertyName);
						final Object value = properties.get(propertyName);
						if (null != value) {
							sorted.put(propertyName, value.toString());
						}
					}
				}
			}
			for (final Entry<String, Object> entry : properties.entrySet()) {
				final String propertyName = entry.getKey();
				if (!alreadyLogged.contains(propertyName)) {
					final Object value = entry.getValue();
					if (value instanceof ForceLog) {
						sorted.put(propertyName, value.toString());
					}
				}
			}
			for (final Entry<String, String> entry : sorted.entrySet()) {
				sb.append('\n').append(entry.getKey()).append('=')
						.append(entry.getValue());
			}
		}
		return sb.toString();
	}

}
