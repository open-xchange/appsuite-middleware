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

package com.openexchange.mail.json.compose.share;

import com.openexchange.groupware.ldap.User;

/**
 * {@link Recipient}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Recipient {

    /**
     * Creates a recipient for an internal user.
     *
     * @param personal The personal
     * @param address The address
     * @param user The user
     * @return The created recipient
     */
    public static Recipient createInternalRecipient(String personal, String address, User user) {
        return new Recipient(personal, address, user);
    }

    /**
     * Creates a recipient for an external address.
     *
     * @param personal The personal
     * @param address The address
     * @return The created recipient
     */
    public static Recipient createExternalRecipient(String personal, String address) {
        return new Recipient(personal, address, null);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String personal;
    private final String address;
    private final int hash;
    private final User user;

    /**
     * Initializes a new {@link Recipient}.
     */
    private Recipient(String personal, String address, User user) {
        super();
        this.personal = personal;
        this.address = address;
        this.user = user;

        int prime = 31;
        int result = prime * 1 + ((address == null) ? 0 : address.hashCode());
        hash = result;
    }

    /**
     * Gets the personal
     *
     * @return The personal
     */
    public String getPersonal() {
        return personal;
    }

    /**
     * Gets the address
     *
     * @return The address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Checks if this recipient denotes an internal user.
     *
     * @return <code>true</code> if this recipient denotes an internal user; otherwise <code>false</code>
     */
    public boolean isUser() {
        return null != user;
    }

    /**
     * Gets the user
     *
     * @return The user or <code>null</code> if this recipient denotes an external contact
     */
    public User getUser() {
        return user;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Recipient)) {
            return false;
        }
        Recipient other = (Recipient) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

}
