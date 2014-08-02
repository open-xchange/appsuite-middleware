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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.push.imapidlev2.locking;

import java.util.concurrent.TimeUnit;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link AbstractImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractImapIdleClusterLock implements ImapIdleClusterLock {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractImapIdleClusterLock}.
     */
    protected AbstractImapIdleClusterLock(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Generate an appropriate value for given time stamp and session identifier pair
     *
     * @param nanos The time stamp
     * @param sessionId The session identifier
     * @return The value
     */
    protected String generateValue(long nanos, String sessionId) {
        return new StringBuilder(32).append(nanos).append('?').append(sessionId).toString();
    }

    /**
     * Parses the time stamp nanos from given value
     *
     * @param value The value
     * @return The nano seconds
     */
    protected long parseNanosFromValue(String value) {
        return Long.parseLong(value.substring(0, value.indexOf('?')));
    }

    /**
     * Checks if the session referenced by given value does still exists
     *
     * @param value The value
     * @return <code>true</code> if session still exists; otherwise <code>false</code>
     */
    protected boolean existsSessionFromValue(String value) {
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (null != sessiondService) {
            return sessiondService.getSession(value.substring(value.indexOf('?') + 1)) != null;
        }
        return false;
    }

    /**
     * Checks validity of passed value in comparison to given time stamp (and session).
     *
     * @param value The value to check
     * @param now The current time stamp nano seconds
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    protected boolean validValue(String value, long now) {
        return (TimeUnit.NANOSECONDS.toMillis(now - parseNanosFromValue(value)) <= TIMEOUT_MILLIS) && existsSessionFromValue(value);
    }

}
