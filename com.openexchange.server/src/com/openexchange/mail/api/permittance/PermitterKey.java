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

package com.openexchange.mail.api.permittance;

import com.openexchange.session.Session;

/**
 * A permitter key.
 */
public final class PermitterKey {

    /**
     * Gets a key instance for given arguments.
     *
     * @param accountId The account identifier
     * @param session The user session
     * @return The key instance
     */
    public static PermitterKey keyFor(int accountId, Session session) {
        return keyFor(accountId, session.getUserId(), session.getContextId());
    }

    /**
     * Gets a key instance for given arguments.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The key instance
     */
    public static PermitterKey keyFor(int accountId, int userId, int contextId) {
        return new PermitterKey(accountId, userId, contextId);
    }

    // --------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final int accountId;
    private final int hash;

    /**
     * Initializes a new {@link PermitterKey}.
     */
    private PermitterKey(int accountId, int userId, int contextId) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;

        int prime = 31;
        int result = prime * 1 + contextId;
        result = prime * result + userId;
        result = prime * result + accountId;
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PermitterKey)) {
            return false;
        }
        PermitterKey pk = (PermitterKey) obj;
        if (contextId != pk.contextId) {
            return false;
        }
        if (userId != pk.userId) {
            return false;
        }
        if (accountId != pk.accountId) {
            return false;
        }
        return true;
    }

}
