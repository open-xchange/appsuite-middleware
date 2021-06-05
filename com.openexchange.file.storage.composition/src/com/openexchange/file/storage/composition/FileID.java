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

import java.util.List;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FileID}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileID {

    /** The service identifier of the default "infostore" file storage */
    public static final String INFOSTORE_SERVICE_ID = "com.openexchange.infostore";

    /** The account identifier of the default "infostore" file storage */
    public static final String INFOSTORE_ACCOUNT_ID = "infostore";

    // ------------------------------------------------------------------------------------------------------------------------------

    private String serviceId;
    private String accountId;
    private String folderId;
    private String fileId;

    /**
     * Initializes a new {@link FileID}.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param folderId The folder identifier
     * @param fileId The file identifier
     */
    public FileID(String serviceId, String accountId, String folderId, String fileId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
        this.folderId = folderId;
        this.fileId = fileId;
    }

    /**
     * Initializes a new {@link FileID}.
     *
     * @param uniqueID The unified identifier
     * @throws IllegalArgumentException If passed <code>uniqueID</code> argument is <code>null</code>
     */
    public FileID(String uniqueID) {
        super();
        if (null == uniqueID) {
            throw new IllegalArgumentException("Unique ID is null.");
        }

        List<String> unmangled = IDMangler.unmangle(uniqueID);
        int size = unmangled.size();
        switch (size) {
            case 1:
                serviceId = INFOSTORE_SERVICE_ID;
                accountId = INFOSTORE_ACCOUNT_ID;
                folderId = null;
                fileId = uniqueID;
                break;
            case 2:
                serviceId = INFOSTORE_SERVICE_ID;
                accountId = INFOSTORE_ACCOUNT_ID;
                folderId = unmangled.get(0);
                fileId = unmangled.get(1);
                break;
            case 4:
                serviceId = unmangled.get(0);
                accountId = unmangled.get(1);
                folderId = unmangled.get(2);
                fileId = unmangled.get(3);
                break;
            default:
                throw new IllegalArgumentException("Unique ID is not valid.");
        }
    }

    /**
     * Gets the service identifier
     *
     * @return The service identifier
     */
    public String getService() {
        return serviceId;
    }

    /**
     * Sets the service identifier
     *
     * @param serviceId The service identifier
     */
    public void setService(String serviceId) {
        this.serviceId = serviceId;
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
     * Gets the file identifier
     *
     * @return The file identifier
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Sets the file identifier
     *
     * @param fileId The file identifier
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Gets the unified identifier
     *
     * @return The unified identifier
     */
    public String toUniqueID() {
        if (INFOSTORE_SERVICE_ID.equals(serviceId) && INFOSTORE_ACCOUNT_ID.equals(accountId)) {
            return null == folderId ? fileId : folderId + IDMangler.SECONDARY_DELIM + fileId;
        }
        return IDMangler.mangle(serviceId, accountId, folderId, fileId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileID)) {
            return false;
        }
        FileID other = (FileID) obj;
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        if (fileId == null) {
            if (other.fileId != null) {
                return false;
            }
        } else if (!fileId.equals(other.fileId)) {
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

    @Override
    public String toString() {
        return toUniqueID();
    }
}
