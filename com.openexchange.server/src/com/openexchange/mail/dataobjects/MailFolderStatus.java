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

package com.openexchange.mail.dataobjects;

import java.io.Serializable;

/**
 * {@link MailFolderStatus} - A light-weight object providing basic information for a mail folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailFolderStatus implements Serializable, Cloneable {

    private static final long serialVersionUID = -7361056527326931996L;

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for a <code>MailFolderStatus</code> instance */
    public static class Builder {

        String validity;
        String nextId;
        int total;
        int unread;

        Builder() {
            super();
            total = -1;
            unread = -1;
        }

        /**
         * Sets the validity string.
         *
         * @param validity The validity string
         * @return This builder
         */
        public Builder validity(String validity) {
            this.validity = validity;
            return this;
        }

        /**
         * Sets the expected next identifier.
         *
         * @param nextId The next identifier
         * @return This builder
         */
        public Builder nextId(String nextId) {
            this.nextId = nextId;
            return this;
        }

        /**
         * Sets the number of total messages.
         *
         * @param total The number of total messages
         * @return This builder
         */
        public Builder total(int total) {
            this.total = total;
            return this;
        }

        /**
         * Sets the number of unread messages.
         *
         * @param unread The number of unread messages
         * @return This builder
         */
        public Builder unread(int unread) {
            this.unread = unread;
            return this;
        }

        /**
         * Builds the appropriate instance of <code>MailFolderStatus</code>.
         *
         * @return The <code>MailFolderStatus</code> instance
         */
        public MailFolderStatus build() {
            return new MailFolderStatus(this);
        }
    }

    // ------------------------------------------------------------------------------------------------------

    private final String nextId;
    private final int total;
    private final int unread;
    private final String validity;

    /**
     * Initializes a new {@link MailFolderStatus}
     */
    MailFolderStatus(Builder builder) {
        super();
        nextId = builder.nextId;
        total = builder.total;
        unread = builder.unread;
        validity = builder.validity;
    }

    /**
     * Gets the expected next identifier.
     *
     * @return The next identifier.
     */
    public String getNextId() {
        return nextId;
    }

    /**
     * Gets the number of total messages
     *
     * @return The number of total messages
     */
    public int getTotal() {
        return total;
    }

    /**
     * Gets the number of unread messages
     *
     * @return The number of unread messages
     */
    public int getUnread() {
        return unread;
    }

    /**
     * Gets the validity string
     *
     * @return The validity string
     */
    public String getValidity() {
        return validity;
    }

    @Override
    public MailFolderStatus clone() throws CloneNotSupportedException {
        try {
            return (MailFolderStatus) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Clone failed although Cloneable is implemented.");
        }
    }

}
