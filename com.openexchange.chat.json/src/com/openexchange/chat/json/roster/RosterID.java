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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.util.regex.Pattern;

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
     */
    public static RosterID valueOf(final String uid) {
        final RosterID cId = new RosterID();
        cId.uid = uid;
        parseUid(cId, uid);
        return cId;
    }

    private static final char DELIM = '-';

    private static final Pattern SPLIT = Pattern.compile(Pattern.quote(String.valueOf(DELIM)));

    private static void parseUid(final RosterID rId, final String uid) {
        if (null == uid) {
            return;
        }
        final String[] args = SPLIT.split(uid);
        final int length = args.length;
        if (length != 3) {
            return;
        }
        rId.serviceId = args[0];
        rId.accountId = args[1];
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
