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

package com.openexchange.halo;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;

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
