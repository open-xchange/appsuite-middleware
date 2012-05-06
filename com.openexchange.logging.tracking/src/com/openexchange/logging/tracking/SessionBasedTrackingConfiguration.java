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

import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tools.session.SessionHolderExtended;

/**
 * {@link SessionBasedTrackingConfiguration}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionBasedTrackingConfiguration implements ScopedTrackingConfiguration {

    private static final Pattern SPLIT = Pattern.compile("\\.");

	private final SessionHolderExtended sh;
	private final TrackingConfiguration config;
	private final String[] className;
	
	public SessionBasedTrackingConfiguration(final String className, final TrackingConfiguration config, final SessionHolder sh) {
	    super();
		this.sh = (sh instanceof SessionHolderExtended ? (SessionHolderExtended) sh : new DelegateSessionHolder(sh));
		this.config = config;
		this.className = SPLIT.split(className, 0);
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
		return config.getLog(sh.optSessionObject());
	}

	private static final class DelegateSessionHolder implements SessionHolderExtended {
	    
	    private final SessionHolder sessionHolder;

        protected DelegateSessionHolder(final SessionHolder sessionHolder) {
            super();
            this.sessionHolder = sessionHolder;
        }

        public Session getSessionObject() {
            return sessionHolder.getSessionObject();
        }

        public Context getContext() {
            return sessionHolder.getContext();
        }

        public User getUser() {
            return sessionHolder.getUser();
        }

        public Session optSessionObject() {
            return sessionHolder.getSessionObject();
        }

	} // End of class DelegateSessionHolder

}
