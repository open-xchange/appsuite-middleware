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

package com.openexchange.pop3.storage.mailaccount;

/**
 * {@link SessionParameterNames} - Constants for session parameter names.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionParameterNames {

    /**
     * Initializes a new {@link SessionParameterNames}.
     */
    private SessionParameterNames() {
        super();
    }

    /**
     * Property name prefix for maps.
     */
    private static final String PROP_MAP = "pop3.uidlmap";

    /**
     * Gets the property name for UIDL map.
     *
     * @param accountId The account ID
     * @return The property name for UIDL map
     */
    public static String getUIDLMap(final int accountId) {
        return new StringBuilder(PROP_MAP.length() + 4).append(PROP_MAP).append(accountId).toString();
    }

    /**
     * Property name prefix for properties.
     */
    private static final String PROP_PROPS = "pop3.props";

    /**
     * Gets the property name for POP3 storage properties.
     *
     * @param accountId The account ID
     * @return The property name for POP3 storage properties
     */
    public static String getStorageProperties(final int accountId) {
        return new StringBuilder(PROP_PROPS.length() + 4).append(PROP_PROPS).append(accountId).toString();
    }

    /**
     * Property name prefix for trash container.
     */
    private static final String PROP_TRASH = "pop3.trash";

    /**
     * Gets the property name for trash container.
     *
     * @param accountId The account ID
     * @return The property name for trash container
     */
    public static String getTrashContainer(final int accountId) {
        return new StringBuilder(PROP_TRASH.length() + 4).append(PROP_TRASH).append(accountId).toString();
    }

}
