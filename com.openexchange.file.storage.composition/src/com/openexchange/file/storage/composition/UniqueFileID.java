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
 * 
 * {@link UniqueFileID}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v7.10.6
 */
public class UniqueFileID {

    private String serviceId;
    private String accountId;
    private String uniqueId;

    /**
     * Initializes a new {@link UniqueFileID}.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @param uniqueId The unique file identifier
     */
    public UniqueFileID(String serviceId, String accountId, String uniqueId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
        this.uniqueId = uniqueId;
    }

    /**
     * Initializes a new {@link UniqueFileID}.
     *
     * @param uniqueID The unified identifier
     * @throws IllegalArgumentException If passed <code>uniqueID</code> argument is <code>null</code>
     */
    public UniqueFileID(String uniqueID) {
        super();
        if (null == uniqueID) {
            throw new IllegalArgumentException("Unique File ID is null.");
        }

        List<String> unmangled = IDMangler.unmangle(uniqueID);
        int size = unmangled.size();
        switch (size) {
            case 1:
                serviceId = INFOSTORE_SERVICE_ID;
                accountId = INFOSTORE_ACCOUNT_ID;
                uniqueId = uniqueID;
                break;
            case 3:
                serviceId = unmangled.get(0);
                accountId = unmangled.get(1);
                uniqueId = unmangled.get(2);
                break;
            default:
                throw new IllegalArgumentException("Unique File ID is not valid.");
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
     * Gets the (account relative) unique file identifier
     *
     * @return The (account relative) unique file identifier
     */
    public String getUniqueFileId() {
        return uniqueId;
    }

    /**
     * Sets the unique file identifier
     *
     * @param uniqueFileId The unique file identifier
     */
    public void setUniqueFileId(String uniqueFileId) {
        this.uniqueId = uniqueFileId;
    }

    /**
     * Gets the (fully qualified) unified identifier
     *
     * @return The (fully qualified) unified identifier
     */
    public String toUniqueID() {
        if (INFOSTORE_SERVICE_ID.equals(serviceId) && INFOSTORE_ACCOUNT_ID.equals(accountId)) {
            return uniqueId;
        }
        if (uniqueId == null) {
            return null;
        }
        return IDMangler.mangle(serviceId, accountId, uniqueId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
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
        if (!(obj instanceof UniqueFileID)) {
            return false;
        }
        UniqueFileID other = (UniqueFileID) obj;
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        if (uniqueId == null) {
            if (other.uniqueId != null) {
                return false;
            }
        } else if (!uniqueId.equals(other.uniqueId)) {
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
