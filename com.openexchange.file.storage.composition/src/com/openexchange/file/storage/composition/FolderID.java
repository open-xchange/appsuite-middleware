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

package com.openexchange.file.storage.composition;

import static com.openexchange.file.storage.composition.FileID.INFOSTORE_ACCOUNT_ID;
import static com.openexchange.file.storage.composition.FileID.INFOSTORE_SERVICE_ID;
import java.util.List;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FolderID} - The folder identifier consisting of service identifier, account identifier and folder identifier.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderID {

    private String service;
    private String accountId;
    private String folderId;

    /**
     * Initializes a new {@link FolderID}.
     *
     * @param service The service identifier
     * @param accountId The account identifier
     * @param folderId The folder identifier
     */
    public FolderID(String service, String accountId, String folderId) {
        super();
        this.service = service;
        this.accountId = accountId;
        this.folderId = folderId;
    }

    /**
     * Initializes a new {@link FolderID}.
     *
     * @param uniqueID The composite identifier; e.g. <code>"com.openexchange.file.storage.custom://1234/MyFolder"</code>
     */
    public FolderID(String uniqueID) {
        final List<String> unmangled = IDMangler.unmangle(uniqueID);
        final int size = unmangled.size();
        if (size == 1) {
            service = INFOSTORE_SERVICE_ID;
            accountId = INFOSTORE_ACCOUNT_ID;
            folderId = uniqueID;
        } else {
            service = unmangled.get(0);
            accountId = unmangled.get(1);
            if (size > 2) {
                folderId = unmangled.get(2);
            } else {
                folderId = "";
            }
        }
    }

    /**
     * Gets the service identifier
     *
     * @return The service identifier
     */
    public String getService() {
        return service;
    }

    /**
     * Sets the service identifier
     *
     * @param service The service identifier
     */
    public void setService(String service) {
        this.service = service;
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
     * @param accountId The account identifier
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Gets the folder identifier
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Sets the folder identifier
     *
     * @param folderId The folder identifier
     */
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    /**
     * Gets the unified identifier
     *
     * @return The unified identifier
     */
    public String toUniqueID() {
        if (INFOSTORE_SERVICE_ID.equals(service) && INFOSTORE_ACCOUNT_ID.equals(accountId)) {
            return folderId;
        }
        return IDMangler.mangle(service, accountId, folderId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FolderID)) {
            return false;
        }
        final FolderID other = (FolderID) obj;
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
        if (service == null) {
            if (other.service != null) {
                return false;
            }
        } else if (!service.equals(other.service)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toUniqueID();
    }

}
