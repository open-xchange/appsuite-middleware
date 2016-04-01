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

package com.openexchange.groupware.delete.contextgroup;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;

/**
 * {@link DeleteContextGroupRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteContextGroupRegistry {

    /**
     * The singleton instance.
     */
    private static volatile DeleteContextGroupRegistry INSTANCE;

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static DeleteContextGroupRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeleteContextGroupRegistry();
        }
        return INSTANCE;
    }

    /**
     * Perform clean up and release the instance
     */
    static void releaseInstance() {
        INSTANCE.cleanup();
        INSTANCE = null;
    }

    /**
     * The listener registry
     */
    private final ConcurrentMap<Class<? extends DeleteContextGroupListener>, DeleteContextGroupListener> listeners;

    /**
     * Initialises a new {@link DeleteContextGroupRegistry}.
     */
    private DeleteContextGroupRegistry() {
        listeners = new ConcurrentHashMap<>();
    }

    /**
     * Registers an instance of {@link DeleteContextGroupListener}
     *
     * @param listener The listener to register
     * @return true if the specified listener has been added to the registry; false otherwise
     */
    public boolean registerDeleteContextGroupListener(DeleteContextGroupListener listener) {
        return (listeners.putIfAbsent(listener.getClass(), listener) != null);
    }

    /**
     * Removes the specified instance of the {@link DeleteContextGroupListener} from the registry.
     *
     * @param listener The listener to remove
     * @return true if the listener was removed from the registry; false otherwise.
     */
    public boolean unregisterDeleteContextGroupListener(DeleteContextGroupListener listener) {
        return (listeners.remove(listener.getClass()) != null);
    }

    /**
     * Fire the delete context group event
     *
     * @param event The event to fire
     * @param readConnection The read connection to the global db
     * @param writeConnection The write connection to the global db
     * @throws OXException If an error occurs
     */
    public void fireDeleteContextGroupEvent(DeleteContextGroupEvent event, Connection readConnection, Connection writeConnection) throws OXException {
        for (DeleteContextGroupListener listener : listeners.values()) {
            listener.deletePerformed(event, readConnection, writeConnection);
        }
    }

    /**
     * Cleanup
     */
    private void cleanup() {
        listeners.clear();
    }
}
