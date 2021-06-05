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

    private Container(String id, Class<? extends IMAPStore> clazz) {
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
    public static Container containerFor(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }
        for (Container container : values()) {
            if (id.equalsIgnoreCase(container.id)) {
                return container;
            }
        }
        return null;
    }

}
