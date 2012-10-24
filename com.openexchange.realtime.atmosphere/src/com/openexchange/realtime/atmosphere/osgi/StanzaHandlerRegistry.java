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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.atmosphere.osgi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.realtime.atmosphere.impl.stanza.handler.StanzaHandler;
import com.openexchange.realtime.packet.Stanza;

/**
 * {@link StanzaHandlerRegistry} - Tracks registered StanzaHandlers and
 * makes them accessible through {@link #getHandlerFor(Class<? extends Stanza>)}.
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StanzaHandlerRegistry extends ServiceRegistry {

    private static final StanzaHandlerRegistry INSTANCE = new StanzaHandlerRegistry();

    private final Map<Class<? extends Stanza>, StanzaHandler> handlers;

    /**
     * Encapsulated constructor.
     */
    private StanzaHandlerRegistry() {
        super();
        handlers = new ConcurrentHashMap<Class<? extends Stanza>, StanzaHandler>();
    }

    /**
     * Get the Registry singleton.
     * 
     * @return the Registry singleton
     */
    public static StanzaHandlerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the appropriate handler for the specified Stanz class.
     * 
     * @param stanzaClass The Stanza subclass we want to handle.
     * @return The appropriate handler or <code>null</code> if none is applicable.
     */
    public StanzaHandler getHandlerFor(Stanza stanza) {
        return handlers.get(stanza.getClass());
    }

    /**
     * Adds specified handler/transformer to this library.
     * 
     * @param transformer The handler to add
     */
    public void add(StanzaHandler handler) {
        handlers.put(handler.getStanzaClass(), handler);
    }

    /**
     * Removes specified handler/transformer from this library.
     * 
     * @param transformer The handler to remove
     */
    public void remove(StanzaHandler handler) {
        handlers.remove(handler.getStanzaClass());
    }

}
