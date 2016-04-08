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

package com.openexchange.contactcollector.internal;

import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.session.Session;

/**
 * {@link MemorizerTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemorizerTask {

    private final List<InternetAddress> addresses;
    private final boolean incrementUseCount;
    private final Session session;

    /**
     * Initializes a new {@link MemorizerTask}.
     *
     * @param address The addresses to memorize
     * @param incrementUseCount Whether use-count is supposed to be incremented
     * @param session The associated session
     */
    public MemorizerTask(List<InternetAddress> addresses, boolean incrementUseCount, Session session) {
        super();
        this.addresses = addresses;
        this.incrementUseCount = incrementUseCount;
        this.session = session;
    }

    /**
     * Checks whether use-count is supposed to be incremented.
     *
     * @return <code>true</code> to increment; otherwise <code>false</code>
     */
    public boolean isIncrementUseCount() {
        return incrementUseCount;
    }

    /**
     * Gets the addresses
     *
     * @return The addresses
     */
    public List<InternetAddress> getAddresses() {
        return addresses;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addresses == null) ? 0 : addresses.hashCode());
        result = prime * result + session.getContextId();
        result = prime * result + session.getUserId();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemorizerTask)) {
            return false;
        }
        final MemorizerTask other = (MemorizerTask) obj;
        if (addresses == null) {
            if (other.addresses != null) {
                return false;
            }
        } else if (!addresses.equals(other.addresses)) {
            return false;
        }
        if (session.getContextId() != other.session.getContextId()) {
            return false;
        }
        if (session.getUserId() != other.session.getUserId()) {
            return false;
        }
        return true;
    }

}
