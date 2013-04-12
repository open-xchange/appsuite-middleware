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

package com.openexchange.mail;

import com.openexchange.mailaccount.MailAccount;

/**
 * {@link FullnameArgument} - Represents a full name argument; e.g. &quot;default347/INBOX/folder3&quot;.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FullnameArgument {

    private final int accountId;
    private final String fullName;

    /**
     * Initializes a new {@link FullnameArgument} with default account ID.
     *
     * @param fullName The full name
     */
    public FullnameArgument(final String fullName) {
        this(MailAccount.DEFAULT_ID, fullName);
    }

    /**
     * Initializes a new {@link FullnameArgument}.
     *
     * @param accountId The account ID
     * @param fullName The full name
     */
    public FullnameArgument(final int accountId, final String fullName) {
        super();
        this.accountId = accountId;
        this.fullName = fullName;
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getFullname() {
        return fullName;
    }

    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FullnameArgument other = (FullnameArgument) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new com.openexchange.java.StringAllocator(32).append("Account-ID=").append(accountId).append(" Full-Name=").append(fullName).toString();
    }
}
