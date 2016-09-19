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

package com.openexchange.folderstorage.messaging;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.io.Serializable;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.messaging.MessagingFolder;

/**
 * {@link MessagingFolderIdentifier} - A parsed messaging folder identifier:<br>
 * <code>(&lt;service-id&gt;)://(&lt;account-id&gt;)/(&lt;fullname&gt;)</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingFolderIdentifier implements Serializable {

    private static final long serialVersionUID = 2518247692416742487L;

    private static final String DELIM = "://";

    /**
     * Gets the fully qualified name:<br>
     * <code>(&lt;service-id&gt;)://(&lt;account-id&gt;)/(&lt;fullname&gt;)</code>
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param fullname The folder fullname
     * @return The fully qualified name
     */
    public static String getFQN(final String serviceId, final int accountId, final String fullname) {
        return new StringBuilder(64).append(serviceId).append(DELIM).append(accountId).append('/').append(fullname).toString();
    }

    /**
     * Checks if given identifier is a valid fully qualified name.
     *
     * @param identifier The identifier to check
     * @return <code>true</code> if given identifier is a valid fully qualified name; otherwise <code>false</code>
     */
    public static boolean isFQN(final String identifier) {
        if (null == identifier) {
            return false;
        }
        int pos = identifier.indexOf(DELIM);
        if (pos <= 0) {
            return false;
        }
        final int prev = pos + DELIM.length();
        pos = identifier.indexOf('/', prev);
        if (pos <= 0) {
            /*
             * "/" character is missing, then expect root folder
             */
            if (getUnsignedInteger(identifier.substring(prev)) <= 0) {
                return false;
            }
            return true;
        }
        if (getUnsignedInteger(identifier.substring(prev, pos)) <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Parses given identifier to a valid fully qualified name.
     *
     * @param identifier The identifier to parse
     * @return The parsed fully qualified name or <code>null</code> if not parseable
     */
    public static MessagingFolderIdentifier parseFQN(final String identifier) {
        try {
            return new MessagingFolderIdentifier(identifier);
        } catch (final OXException e) {
            return null;
        }
    }

    private final String serviceId;

    private final int accountId;

    private final String fullname;

    private final int hash;

    private final String fqn;

    /**
     * Initializes a new {@link MessagingFolderIdentifier}.
     *
     * @param identifier The identifier according to pattern:<br>
     *            <code>(&lt;service-id&gt;)://(&lt;account-id&gt;)/(&lt;fullname&gt;)</code>
     * @throws OXException If identifier is <code>null</code> or invalid
     */
    public MessagingFolderIdentifier(final String identifier) throws OXException {
        super();
        if (null == identifier) {
            throw FolderExceptionErrorMessage.MISSING_FOLDER_ID.create();
        }
        int pos = identifier.indexOf(DELIM);
        if (pos <= 0) {
            throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
        }
        int prev = 0;
        serviceId = identifier.substring(prev, pos);
        prev = pos + DELIM.length();
        pos = identifier.indexOf('/', prev);
        if (pos <= 0) {
            /*
             * "/" character is missing, then expect root folder
             */
            accountId = getUnsignedInteger(identifier.substring(prev));
            if (accountId <= 0) {
                throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
            }
            fullname = MessagingFolder.ROOT_FULLNAME;

            fqn = new StringBuilder(identifier).append('/').toString();
        } else {
            accountId = getUnsignedInteger(identifier.substring(prev, pos));
            if (accountId <= 0) {
                throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
            }
            fullname = identifier.substring(pos + 1);

            fqn = identifier;
        }
        /*
         * Hash code
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hash = result;
    }

    /**
     * Initializes a new {@link MessagingFolderIdentifier}.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param fullname The folder fullname
     */
    public MessagingFolderIdentifier(final String serviceId, final int accountId, final String fullname) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
        this.fullname = fullname;
        fqn = new StringBuilder(64).append(serviceId).append(DELIM).append(accountId).append('/').append(fullname).toString();
        /*
         * Hash code
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hash = result;
    }

    /**
     * Gets the service identifier.
     *
     * @return The service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets the account identifier.
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the fullname
     *
     * @return The fullname
     */
    public String getFullname() {
        return fullname;
    }

    @Override
    public String toString() {
        return fqn;
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
        if (!(obj instanceof MessagingFolderIdentifier)) {
            return false;
        }
        final MessagingFolderIdentifier other = (MessagingFolderIdentifier) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (fullname == null) {
            if (other.fullname != null) {
                return false;
            }
        } else if (!fullname.equals(other.fullname)) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        } else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        return true;
    }
}
