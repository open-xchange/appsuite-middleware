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
