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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link ContactInterfaceProviderRegistry} - A registry for {@link ContactInterfaceProvider} instances.
 * 
 * @author <a href="mailto:ben.pahne@open-xchange.com">Ben Pahne</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactInterfaceProviderRegistry {

    private static final ContactInterfaceProviderRegistry instance = new ContactInterfaceProviderRegistry();

    /**
     * Gets the {@link ContactInterfaceProviderRegistry} instance.
     * 
     * @return The {@link ContactInterfaceProviderRegistry} instance
     */
    public static ContactInterfaceProviderRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<Key, ContactInterfaceProvider> services;

    private ContactInterfaceProviderRegistry() {
        super();
        services = new ConcurrentHashMap<Key, ContactInterfaceProvider>();
    }

    /**
     * Adds specified {@link ContactInterfaceProvider} instance and binds it to given folder ID/context ID pair.
     * 
     * @param folderId The folder ID
     * @param contextId The context ID
     * @param contactInterface The {@link ContactInterfaceProvider} instance to add
     * @return <code>true</code> if specified {@link ContactInterfaceProvider} instance was successfully added; otherwise <code>false</code>
     */
    public boolean addService(final int folderId, final int contextId, final ContactInterfaceProvider contactInterface) {
        return (null == services.putIfAbsent(new Key(folderId, contextId), contactInterface));
    }

    /**
     * Checks if this registry contains a {@link ContactInterfaceProvider} instance bound to given folder ID/context ID pair.
     * 
     * @param folderId The folder ID
     * @param contextId The context ID
     * @return <code>true</code> if this registry contains a {@link ContactInterfaceProvider} instance bound to given folder ID/context ID
     *         pair; otherwise <code>false</code>
     */
    public boolean containsService(final int folderId, final int contextId) {
        return services.containsKey(new Key(folderId, contextId));
    }

    /**
     * Removes possibly existing binding of given folder ID/context ID pair to specified {@link ContactInterfaceProvider} instance.
     * 
     * @param folderId The folder ID
     * @param contextId The context ID
     * @param contactInterface he {@link ContactInterfaceProvider} instance to remove
     * @return <code>true</code> if specified {@link ContactInterfaceProvider} instance was successfully removed; otherwise
     *         <code>false</code>
     */
    public boolean removeService(final int folderId, final int contextId, final ContactInterfaceProvider contactInterface) {
        return services.remove(new Key(folderId, contextId), contactInterface);
    }

    /**
     * Gets the number of registered {@link ContactInterfaceProvider} instances.
     * 
     * @return The number of registered {@link ContactInterfaceProvider} instances
     */
    public int getNumberOfServices() {
        return services.size();
    }

    /*
     * public ContactInterfaceProvider getService(int folderId){ ContactInterfaceProvider contactInterface = services.get(folderId); return
     * contactInterface; }
     */

    /**
     * Gets the {@link ContactInterfaceProvider} instance bound to given folder ID in given context.
     * 
     * @param folderId The folder ID
     * @param contextId The context ID
     * @return The {@link ContactInterfaceProvider} instance bound to given folder ID in given context
     */
    public ContactInterfaceProvider getService(final int folderId, final int contextId) {
        return services.get(new Key(folderId, contextId));
    }

    private static final class Key {

        private final int folderId;

        private final int contextId;

        private final int hash;

        public Key(final int folderId, final int contextId) {
            super();
            this.folderId = folderId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + folderId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (folderId != other.folderId) {
                return false;
            }
            return true;
        }
    } // End of class Key

}
