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

package com.openexchange.pop3.storage;

/**
 * {@link FullnameUIDPair} - Represents a fullname-mailId-pair.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FullnameUIDPair implements Comparable<FullnameUIDPair> {

    private static final String INBOX = "INBOX";

    /**
     * Creates a new {@link FullnameUIDPair} with full name set to <code>&quot;INBOX&quot;</code> and mail ID set to given mail ID.
     *
     * @param mailId The mail ID
     * @return A new {@link FullnameUIDPair} with full name set to <code>&quot;INBOX&quot;</code> and mail ID set to given mail ID
     */
    public static FullnameUIDPair newINBOXInstance(final String mailId) {
        return new FullnameUIDPair(INBOX, mailId);
    }

    private final String fullName;
    private final String mailId;
    private final int hash;

    /**
     * Initializes a new {@link FullnameUIDPair}.
     *
     * @param fullName The folder full name
     * @param mailId The mail ID
     */
    public FullnameUIDPair(final String fullName, final String mailId) {
        super();
        this.fullName = fullName;
        this.mailId = mailId;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + ((mailId == null) ? 0 : mailId.hashCode());
        this.hash = result;
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
     * Gets the mail ID.
     *
     * @return The mail ID
     */
    public String getMailId() {
        return mailId;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FullnameUIDPair)) {
            return false;
        }
        final FullnameUIDPair other = (FullnameUIDPair) obj;
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        if (mailId == null) {
            if (other.mailId != null) {
                return false;
            }
        } else if (!mailId.equals(other.mailId)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final FullnameUIDPair other) {
        if (fullName == null) {
            if (other.fullName != null) {
                return -1;
            }
            return 0;
        } else if (other.fullName == null) {
            return 1;
        }
        final int folderComp = fullName.compareTo(other.fullName);
        if (folderComp != 0) {
            return folderComp;
        }
        if (mailId == null) {
            if (other.mailId != null) {
                return -1;
            }
            return 0;
        } else if (other.mailId == null) {
            return 1;
        }
        return mailId.compareTo(other.mailId);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Fullname=").append(fullName).append(" Mail-ID=").append(mailId).toString();
    }
}
