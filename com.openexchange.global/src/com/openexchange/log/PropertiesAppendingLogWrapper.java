package com.openexchange.log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogPropertyName.LogLevel;

public class PropertiesAppendingLogWrapper implements Log {

	private final org.apache.commons.logging.Log delegate;

	public PropertiesAppendingLogWrapper(final org.apache.commons.logging.Log delegate) {
		this.delegate = delegate;
	}

	@Override
    public void debug(final Object arg0, final Throwable arg1) {
		delegate.debug(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.DEBUG), arg1);
	}

	@Override
    public void debug(final Object arg0) {
		delegate.debug(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.DEBUG));
	}

	@Override
    public void error(final Object arg0, final Throwable arg1) {
		delegate.error(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.ERROR), arg1);
	}

	@Override
    public void error(final Object arg0) {
		delegate.error(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.ERROR));
	}

	@Override
    public void fatal(final Object arg0, final Throwable arg1) {
		delegate.fatal(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.FATAL), arg1);
	}

	@Override
    public void fatal(final Object arg0) {
		delegate.fatal(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.FATAL));
	}

	@Override
    public void info(final Object arg0, final Throwable arg1) {
		delegate.info(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.INFO), arg1);
	}

	@Override
    public void info(final Object arg0) {
		delegate.info(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.INFO));
	}
	
	@Override
    public void trace(final Object arg0, final Throwable arg1) {
		delegate.trace(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.TRACE), arg1);
	}

	@Override
    public void trace(final Object arg0) {
		delegate.trace(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.TRACE));
	}

	@Override
    public void warn(final Object arg0, final Throwable arg1) {
		delegate.warn(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.WARNING), arg1);
	}

	@Override
    public void warn(final Object arg0) {
		delegate.warn(appendProperties(arg0 == null ? null : arg0.toString(), LogLevel.WARNING));
	}

	@Override
    public boolean isDebugEnabled() {
		return delegate.isDebugEnabled();
	}

	@Override
    public boolean isErrorEnabled() {
		return delegate.isErrorEnabled();
	}

	@Override
    public boolean isFatalEnabled() {
		return delegate.isFatalEnabled();
	}

	@Override
    public boolean isInfoEnabled() {
		return delegate.isInfoEnabled();
	}

	@Override
    public boolean isTraceEnabled() {
		return delegate.isTraceEnabled();
	}

	@Override
    public boolean isWarnEnabled() {
		return delegate.isWarnEnabled();
	}

	
	public String appendProperties(final String message,
			final LogLevel logLevel) {
		if (!LogProperties.isEnabled()) {
			return message;
		}
		final Props logProps = LogProperties.optLogProperties();
		if (logProps == null) {
			return message;
		}
		final StringBuilder sb = new StringBuilder(message);
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
