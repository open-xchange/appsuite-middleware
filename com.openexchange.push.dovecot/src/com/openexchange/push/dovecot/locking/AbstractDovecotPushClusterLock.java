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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.push.dovecot.locking;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Future;
import com.hazelcast.core.Member;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractDovecotPushClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractDovecotPushClusterLock implements DovecotPushClusterLock {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractDovecotPushClusterLock.class);

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractDovecotPushClusterLock}.
     */
    protected AbstractDovecotPushClusterLock(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Generate an appropriate value for given time stamp and session identifier pair
     *
     * @param millis The time stamp
     * @param sessionInfo The session info
     * @return The value
     */
    protected String generateValue(long millis, SessionInfo sessionInfo) {
        if (sessionInfo.isPermanent()) {
            return Long.toString(millis);
        }
        return new StringBuilder(32).append(millis).append('?').append(sessionInfo.getCompositeId()).toString();
    }

    /**
     * Checks validity of passed value in comparison to given time stamp (and session).
     *
     * @param value The value to check
     * @param now The current time stamp milliseconds
     * @param hzInstance The Hazelcast instance
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    protected boolean validValue(String value, long now) {
        return (now - parseMillisFromValue(value) <= TIMEOUT_MILLIS);
    }

    /**
     * Parses the time stamp milliseconds from given value
     *
     * @param value The value
     * @return The milliseconds
     */
    protected long parseMillisFromValue(String value) {
        int pos = value.indexOf('?');
        return Long.parseLong(pos > 0 ? value.substring(0, pos) : value);
    }

    /**
     * Gets the other cluster members
     *
     * @param allMembers All known members
     * @param localMember The local member
     * @return Other cluster members
     */
    public static Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
        Set<Member> otherMembers = new LinkedHashSet<Member>(allMembers);
        if (!otherMembers.remove(localMember)) {
            LOG.warn("Couldn't remove local member from cluster members.");
        }
        return otherMembers;
    }

    /**
     * Cancels given {@link Future} safely
     *
     * @param future The {@code Future} to cancel
     */
    public static <V> void cancelFutureSafe(Future<V> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

}
