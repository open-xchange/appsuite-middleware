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
 *    trademarks of the OX Software GmbH. group of companies.
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
            this.ownerId = ownerId;
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
        this.ownerId = ownerId;
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
