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

package com.openexchange.chat.json.roster;

import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.exception.OXException;


/**
 * {@link RosterID} - Represents a roster identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RosterID {

    /**
     * Gets the conversation UID from specified identifier.
     *
     * @param uid The conversation identifier
     * @return The conversation identifier
     * @throws OXException If parsing fails
     */
    public static RosterID valueOf(final String uid) throws OXException {
        final RosterID rosterId = new RosterID();
        rosterId.uid = uid;
        parseUid(rosterId, uid);
        if (null == rosterId.accountId) {
            throw ChatExceptionCodes.INVALID_ROSTER_ID.create(uid);
        }
        if (null == rosterId.serviceId) {
            throw ChatExceptionCodes.INVALID_ROSTER_ID.create(uid);
        }
        return rosterId;
    }

    private static final char DELIM = '-';

    private static void parseUid(final RosterID rosterId, final String uid) {
        if (null == uid) {
            return;
        }
        int off = 0;
        final int pos = uid.indexOf(DELIM, off);
        rosterId.serviceId = uid.substring(off, pos);
        off = pos + 1;
        rosterId.accountId = uid.substring(off);
    }

    /*-
     * -------------------- -- - MEMBER STUFF - -- --------------------------
     */

    private String serviceId;

    private String accountId;

    private String uid;

    /**
     * Initializes a new {@link RosterID}.
     */
    public RosterID() {
        super();
    }

    /**
     * Initializes a new {@link RosterID}.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     */
    public RosterID(final String serviceId, final String accountId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
    }

    /**
     * Resets this conversation identifier.
     */
    public void reset() {
        serviceId = null;
        accountId = null;
        uid = null;
    }

    /**
     * Gets the service identifier
     *
     * @return The service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service identifier
     *
     * @param serviceId The service identifier to set
     * @return This conversation identifier with given identifier applied
     */
    public RosterID setServiceId(final String serviceId) {
        this.serviceId = serviceId;
        uid = null;
        return this;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Sets the account identifier
     *
     * @param accountId The account identifier to set
     * @return This conversation identifier with given identifier applied
     */
    public RosterID setAccountId(final String accountId) {
        this.accountId = accountId;
        uid = null;
        return this;
    }

    @Override
    public String toString() {
        if (null == uid) {
            uid = new StringBuilder(16).append(serviceId).append(DELIM).append(accountId).toString();
        }
        return uid;
    }

}
