/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.logging.tracking;

import org.apache.commons.logging.Log;

public class TrackingFileLog implements Log {

	private final ScopedTrackingConfiguration config;
	private final Log delegate;

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
		return config.isDebugEnabled() || delegate.isDebugEnabled();
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
