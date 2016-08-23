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
        } catch (final CloneNotSupportedException e) {
            throw new InternalError("Clone failed although Cloneable is implemented.");
        }
    }

}
