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

package com.openexchange.folderstorage.filestorage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FileStorageFolderIdentifier} - A parsed folder storage folder identifier:<br>
 * <code>(&lt;service-id&gt;)://(&lt;account-id&gt;)/(&lt;folder-id&gt;)</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageFolderIdentifier {

    private static final String DELIM = "://";

    /**
     * Gets the fully qualified name:<br>
     * <code>(&lt;service-id&gt;)://(&lt;account-id&gt;)/(&lt;folder-id&gt;)</code>
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param folderId The folder identifier
     * @return The fully qualified name
     */
    public static String getFQN(final String serviceId, final String accountId, String folderId) {
        if(serviceId == null && accountId == null) {
            // Looks like a global and regular OX folder id.
            return folderId;
        }
        if(serviceId == null) {
            throw new NullPointerException("In plugin supplied folder IDs, the service may not be null");
        }
        if(accountId == null) {
            throw new NullPointerException("In plugin supplied folder IDs, the acccountId should not be null");
        }
        if(folderId == null) {
            // Assume some kind of root folder
            folderId = "";
        }
        return IDMangler.mangle(serviceId, accountId, folderId);
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
            if (identifier.substring(prev).length() == 0) {
                return false;
            }
            return true;
        }
        if (identifier.substring(prev, pos).length() == 0) {
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
    public static FileStorageFolderIdentifier parseFQN(final String identifier) {
        try {
            return new FileStorageFolderIdentifier(identifier);
        } catch (final OXException e) {
            return null;
        }
    }

    private final String serviceId;

    private final String accountId;

    private final String folderId;

    private final int hash;

    private final String fqn;

    /**
     * Initializes a new {@link FileStorageFolderIdentifier}.
     *
     * @param identifier The identifier according to pattern:<br>
     *            <code>(&lt;service-id&gt;)://(&lt;account-id&gt;)/(&lt;fullname&gt;)</code>
     * @throws OXException If identifier is <code>null</code> or invalid
     */
    public FileStorageFolderIdentifier(final String identifier) throws OXException {
        super();
        if (null == identifier) {
            throw FolderExceptionErrorMessage.MISSING_FOLDER_ID.create();
        }
        // Parse identifier
        final List<String> components = IDMangler.unmangle(identifier);
        if (components.isEmpty()) {
            throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
        }
        final int size = components.size();
        if (size == 1) {
            throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
        }
        serviceId = components.get(0);
        if (size == 2) {
            /*
             * Have only service and account ID, so expect root folder
             */
            accountId = components.get(1);
            if (isEmpty(accountId)) {
                throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
            }
            folderId = MessagingFolder.ROOT_FULLNAME;

            fqn = IDMangler.mangle(serviceId, accountId, folderId);
        } else {
            accountId = components.get(1);
            if (isEmpty(accountId)) {
                throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(identifier);
            }
            folderId = components.get(2);

            fqn = identifier;
        }
        /*
         * Hash code
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hash = result;
    }

    /**
     * Initializes a new {@link FileStorageFolderIdentifier}.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param folderId The folder fullname
     */
    public FileStorageFolderIdentifier(final String serviceId, final String accountId, final String folderId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
        this.folderId = folderId;
        // Parseable identifier
        fqn = IDMangler.mangle(serviceId, accountId, folderId);
        /*
         * Hash code
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
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
    public String getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder identifier
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
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
        if (!(obj instanceof FileStorageFolderIdentifier)) {
            return false;
        }
        final FileStorageFolderIdentifier other = (FileStorageFolderIdentifier) obj;
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        if (folderId == null) {
            if (other.folderId != null) {
                return false;
            }
        } else if (!folderId.equals(other.folderId)) {
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

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
