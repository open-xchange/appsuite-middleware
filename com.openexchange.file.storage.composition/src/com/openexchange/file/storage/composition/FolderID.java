/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
     * @throws IllegalArgumentException If given composite identifier is <code>null</code>
     */
    public FolderID(String uniqueID) {
        super();
        if (null == uniqueID) {
            throw new IllegalArgumentException("uniqueID must not be null");
        }

        List<String> unmangled = IDMangler.unmangle(uniqueID);
        int size = unmangled.size();
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
