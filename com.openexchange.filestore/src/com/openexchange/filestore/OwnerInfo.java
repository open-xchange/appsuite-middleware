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
 * {@link OwnerInfo} - Carries the owner information for an initialized file storage.
 * <p>
 * The file storage owner determines to what <code>'filestore_usage'</code> entry the quota gets accounted.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 * @see #NO_OWNER
 */
public class OwnerInfo {

    /** The owner information signaling no dedicated file storage is used, but the context-associated one. */
    public static final OwnerInfo NO_OWNER = new OwnerInfo(0, false);

    /**
     * Creates a new builder instance with owner set to <code>0</code> (zero) and master flag set to <code>true</code>.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The builder for an {@code OwnerInfo} instance.
     */
    public static class Builder {

        private int ownerId;
        private boolean master;

        Builder() {
            super();
            ownerId = 0;
            master = true;
        }

        /**
         * Sets the owner identifier
         *
         * @param ownerId The owner identifier to set
         * @return This instance
         */
        public Builder setOwnerId(int ownerId) {
            this.ownerId = ownerId <= 0 ? 0 : ownerId;
            return this;
        }

        /**
         * Sets the master flag (if owner is equal to master)
         *
         * @param master <code>true</code> if owner is master; otherwise <code>false</code>
         * @return This instance
         */
        public Builder setMaster(boolean master) {
            this.master = master;
            return this;
        }

        /**
         * Builds the {@code OwnerInfo} instance from this builder's attributes.
         *
         * @return The {@code OwnerInfo} instance
         */
        public OwnerInfo build() {
            return new OwnerInfo(ownerId, master);
        }
    }

    // ----------------------------------------------------------------------------

    private final int ownerId;
    private final boolean master;

    /**
     * Initializes a new {@link OwnerInfo}.
     */
    protected OwnerInfo(int ownerId, boolean master) {
        super();
        this.ownerId = ownerId <= 0 ? 0 : ownerId;
        this.master = master;
    }

    /**
     * Gets the owner identifier
     * <p>
     * The file storage owner or <code>0</code> (zero); the owner determines to what 'filestore_usage' entry the quota gets accounted
     *
     * @return The owner identifier
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * Gets the master flag (if owner is equal to master)
     *
     * @return <code>true</code> if owner is master; otherwise <code>false</code>
     */
    public boolean isMaster() {
        return master;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("[ownerId=").append(ownerId).append(", master=").append(master).append("]").toString();
    }

}
