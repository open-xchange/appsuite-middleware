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

package com.openexchange.oauth.impl.internal;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccountDeleteListener;

/**
 * {@link DeleteListenerRegistry} - Registry for OAuth account delete listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DeleteListenerRegistry {

    private static volatile DeleteListenerRegistry instance;

    /**
     * Initializes the registry instance.
     */
    public static void initInstance() {
        instance = new DeleteListenerRegistry();
    }

    /**
     * Releases the registry instance.
     */
    public static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static DeleteListenerRegistry getInstance() {
        return instance;
    }

    /*
     * Member section
     */

    private final ConcurrentMap<Class<? extends OAuthAccountDeleteListener>, OAuthAccountDeleteListener> registry;

    /**
     * Initializes a new {@link DeleteListenerRegistry}.
     */
    public DeleteListenerRegistry() {
        super();
        registry = new ConcurrentHashMap<Class<? extends OAuthAccountDeleteListener>, OAuthAccountDeleteListener>();
    }

    /**
     * Adds specified delete listener to this registry.
     *
     * @param deleteListener The delete listener to add
     * @return <code>true</code> if listener could be successfully added; otherwise <code>false</code>
     */
    public boolean addDeleteListener(final OAuthAccountDeleteListener deleteListener) {
        return (null == registry.putIfAbsent(deleteListener.getClass(), deleteListener));
    }

    /**
     * Removes specified delete listener from this registry.
     *
     * @param deleteListener The delete listener to add
     */
    public void removeDeleteListener(final OAuthAccountDeleteListener deleteListener) {
        registry.remove(deleteListener.getClass());
    }

    /**
     * Triggers the {@link OAuthAccountDeleteListener#onBeforeOAuthAccountDeletion()} event for registered listeners.
     */
    public void triggerOnBeforeDeletion(final int id, final Map<String, Object> properties, final int user, final int cid, final Connection con) throws OXException {
        for (final OAuthAccountDeleteListener listener : registry.values()) {
            listener.onBeforeOAuthAccountDeletion(id, properties, user, cid, con);
        }
    }

    /**
     * Triggers the {@link OAuthAccountDeleteListener#onAfterOAuthAccountDeletion()} event for registered listeners.
     */
    public void triggerOnAfterDeletion(final int id, final Map<String, Object> properties, final int user, final int cid, final Connection con) throws OXException {
        for (final OAuthAccountDeleteListener listener : registry.values()) {
            listener.onAfterOAuthAccountDeletion(id, properties, user, cid, con);
        }
    }

}
