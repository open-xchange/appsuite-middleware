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

package com.openexchange.login.listener.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.login.listener.LoginListener;
import com.openexchange.osgi.util.RankedService;

/**
 * {@link LoginListenerRegistryImpl} - A registry for login listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class LoginListenerRegistryImpl implements LoginListenerRegistry {

    private static volatile LoginListenerRegistryImpl instance;

    /**
     * Gets the login performer instance.
     *
     * @return The login performer instance.
     */
    public static LoginListenerRegistryImpl getInstance() {
        LoginListenerRegistryImpl tmp = instance;
        if (null == tmp) {
            synchronized (LoginListenerRegistryImpl.class) {
                tmp = instance;
                if (tmp == null) {
                    instance = tmp = new LoginListenerRegistryImpl();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the login performer instance.
     */
    public static void releaseInstance() {
        LoginListenerRegistry tmp = instance;
        if (null != tmp) {
            synchronized (LoginListenerRegistryImpl.class) {
                tmp = instance;
                if (tmp != null) {
                    instance = null;
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------

    private final SortableConcurrentList<RankedService<LoginListener>> listeners;
    private volatile boolean empty;

    /**
     * Initializes a new {@link LoginListenerRegistryImpl}.
     */
    private LoginListenerRegistryImpl() {
        super();
        listeners = new SortableConcurrentList<>();
        empty = true; // Initially empty
    }

    /**
     * Adds specified login listener.
     *
     * @param loginListener The login listener to add
     * @param ranking The ranking to pay respect to
     * @return <code>true</code> if login listener could be successfully added; otherwise <code>false</code>
     */
    public synchronized boolean addLoginListener(LoginListener loginListener, int ranking) {
        RankedService<LoginListener> rankedService = new RankedService<LoginListener>(loginListener, ranking);
        if (listeners.addAndSort(rankedService)) {
            empty = false;
            return true;
        }
        return false;
    }

    /**
     * Removes specified login listener.
     *
     * @param loginListener The login listener to remove
     */
    public synchronized void removeLoginListener(LoginListener loginListener) {
        listeners.remove(new RankedService<LoginListener>(loginListener, 0));
        empty = listeners.isEmpty();
    }

    @Override
    public List<LoginListener> getLoginListeners() {
        if (empty) {
            return Collections.emptyList();
        }

        List<RankedService<LoginListener>> snapshot = listeners.getSnapshot();
        List<LoginListener> ret = new ArrayList<LoginListener>(snapshot.size());
        for (RankedService<LoginListener> rs : snapshot) {
            ret.add(rs.service);
        }
        return ret;
    }

}
