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

package com.openexchange.halo;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.groupware.container.Contact;
import com.openexchange.user.User;

/**
 * Provides information for a Halo contact query.
 */
public class HaloContactQuery {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds an instance of <code>HaloContactQuery</code> */
    public static class Builder {

        private Contact contact;
        private User user;
        private List<Contact> merged;

        Builder() {
            super();
        }

        /**
         * Sets the user to apply to <code>HaloContactQuery</code> instance.
         *
         * @param user The user
         * @return This builder
         */
        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the contact to apply to <code>HaloContactQuery</code> instance.
         *
         * @param contact The contact
         * @return This builder
         */
        public Builder withContact(Contact contact) {
            this.contact = contact;
            return this;
        }

        /**
         * Sets the list of contacts to merge to apply to <code>HaloContactQuery</code> instance.
         *
         * @param contactsToMerge The contacts to merge
         * @return This builder
         */
        public Builder withMergedContacts(List<Contact> contactsToMerge) {
            this.merged = contactsToMerge;
            return this;
        }

        /**
         * Builds the resulting instance of <code>HaloContactQuery</code> from this builder's arguments.
         *
         * @return The <code>HaloContactQuery</code> instance
         */
        public HaloContactQuery build() {
            return new HaloContactQuery(contact, user, merged);
        }
    }

    // -------------------------------------------------------------------------------

    private final Contact contact;
    private final User user;
    private final List<Contact> merged;

    HaloContactQuery(Contact contact, User user, List<Contact> merged) {
        super();
        this.contact = contact;
        this.user = user;
        this.merged = null == merged ? null : ImmutableList.copyOf(merged);
    }

    /**
     * Gets the user
     *
     * @return The user or <code>null</code> (if not set)
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the contact
     *
     * @return The contact or <code>null</code> (if not set)
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Gets the (<b>immutable</b>) list of merged contacts
     *
     * @return The merged contacts or <code>null</code> (if not set)
     */
    public List<Contact> getMergedContacts() {
        return merged;
    }

    /**
     * Gets a copy for the list of merged contacts
     *
     * @return The merged contacts' copy or <code>null</code> (if not set)
     */
    public List<Contact> getCopyOfMergedContacts() {
        return null == merged ? null : new ArrayList<>(merged);
    }

}
