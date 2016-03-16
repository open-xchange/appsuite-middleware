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

package com.openexchange.imap.entity2acl;

/**
 * {@link UserGroupID} - A user/group identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserGroupID {

    /**
     * The constant representing a failed mapping of a ACL entity name to a system user/group.
     */
    public static final UserGroupID NULL = new NullUserGroupID();

    private final int id;

    private final boolean group;

    /**
     * Initializes a new {@link UserGroupID}.
     *
     * @param id The user identifier
     * @throws IllegalArgumentException If identifier is less than zero
     */
    UserGroupID(final int id) {
        this(id, false);
    }

    /**
     * Initializes a new {@link UserGroupID}.
     *
     * @param id The user/group identifier
     * @param group <code>true</code> if identifier denotes a group; otherwise <code>false</code>
     * @throws IllegalArgumentException If identifier is less than zero
     */
    UserGroupID(final int id, final boolean group) {
        this(id, group, false);
    }

    /**
     * Initializes a new {@link UserGroupID}.
     *
     * @param id The user/group identifier
     * @param group <code>true</code> if identifier denotes a group; otherwise <code>false</code>
     * @param allowNegative <code>true</code> if a negative identifier is allowed; otherwise <code>false</code>
     * @throws IllegalArgumentException If identifier is less than zero
     */
    protected UserGroupID(final int id, final boolean group, final boolean allowNegative) {
        super();
        if (!allowNegative && id < 0) {
            throw new IllegalArgumentException("Identifier is less than zero.");
        }
        this.id = id;
        this.group = group;
    }

    /**
     * Gets the user/group identifier.
     *
     * @return The user/group identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Checks if identifier denotes a group.
     *
     * @return <code>true</code> if identifier denotes a group; otherwise <code>false</code>
     */
    public boolean isGroup() {
        return group;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserGroupID)) {
            return false;
        }
        final UserGroupID other = (UserGroupID) obj;
        if (group != other.group) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    private static final class NullUserGroupID extends UserGroupID {

        public NullUserGroupID() {
            super(-1, false, true);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isGroup() ? 1231 : 1237);
            result = prime * result + getId();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return true;
            }
            return false;
        }

    }

}
