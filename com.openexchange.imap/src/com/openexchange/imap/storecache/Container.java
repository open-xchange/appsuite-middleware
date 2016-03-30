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

package com.openexchange.imap.storecache;

import com.openexchange.java.Strings;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.JavaIMAPStore;
import com.sun.mail.imap.QueuingIMAPStore;

/**
 * {@link Container} - The container types. Default is {@link #BOUNDARY_AWARE}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.1
 */
public enum Container {

    /**
     * An unbounded IMAP store container.
     * <p>
     * Acts as a temporary keep-alive cache for connected {@link IMAPStore} instances.<br>
     * Does <b>not</b> consider <code>"com.openexchange.imap.maxNumConnections"</code> property at all.
     */
    UNBOUNDED("unbounded", JavaIMAPStore.class),
    /**
     * A bounded IMAP store container (default).
     * <p>
     * Extends {@link #UNBOUNDED} by allowing to connect new {@link IMAPStore} instances with respect to
     * <code>"com.openexchange.imap.maxNumConnections"</code> property.
     */
    BOUNDARY_AWARE("boundary-aware", JavaIMAPStore.class),
    /**
     * A non-caching IMAP store container.
     * <p>
     * Delegates <code>"com.openexchange.imap.maxNumConnections"</code> setting to <code>'com.sun.mail.imap.QueuingIMAPStore'</code> that
     * performs an exact limitation based on established socket connections.
     */
    NON_CACHING("non-caching", QueuingIMAPStore.class);

    private final String id;
    private final Class<? extends IMAPStore> clazz;

    private Container(final String id, final Class<? extends IMAPStore> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the class of the associated IMAP store.
     *
     * @return The IMAP store class
     */
    public Class<? extends IMAPStore> getStoreClass() {
        return clazz;
    }

    /**
     * Gets the default container.
     */
    public static Container getDefault() {
        return BOUNDARY_AWARE;
    }

    /**
     * Gets the container for given identifier.
     *
     * @param id The identifier
     * @return The associated container or <code>null</code>
     */
    public static Container containerFor(final String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }
        for (final Container container : values()) {
            if (id.equalsIgnoreCase(container.id)) {
                return container;
            }
        }
        return null;
    }

}
