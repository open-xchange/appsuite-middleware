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

package com.openexchange.filestore;


/**
 * {@link StorageInfo} - Information for a file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class StorageInfo {

    private final int id;
    private final long quota;
    private final String name;
    private final OwnerInfo ownerInfo;

    /**
     * Initializes a new {@link StorageInfo}.
     *
     * @param id The file storage identifier
     * @param ownerInfo The owner information for the file storage
     * @param name The entity-specific location inside the file storage
     * @param quota The file storage quota
     */
    public StorageInfo(int id, OwnerInfo ownerInfo, String name, long quota) {
        super();
        this.id = id;
        this.ownerInfo = ownerInfo;
        this.name = name;
        this.quota = quota;
    }

    /**
     * Gets the file storage quota
     *
     * @return The quota for the file storage or <code>0</code> if there is no quota.
     */
    public long getQuota() {
        return quota;
    }

    /**
     * Gets the file storage identifier
     *
     * @return The file storage identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the entity-specific location inside the file storage; e.g. <code>"1_ctx_store"</code>.
     *
     * @return The entity-specific location inside the file storage.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the owner information for the file storage.
     * <p>
     * The owner determines to what 'filestore_usage' entry the quota gets accounted.
     *
     * @return The owner information
     */
    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StorageInfo [id=").append(id).append(", quota=").append(quota).append(", ");
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        builder.append("ownerInfo=").append(ownerInfo).append("]");
        return builder.toString();
    }

}
