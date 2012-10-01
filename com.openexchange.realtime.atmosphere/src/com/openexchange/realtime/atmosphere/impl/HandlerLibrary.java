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

package com.openexchange.realtime.atmosphere.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.realtime.atmosphere.OXRTHandler;

/**
 * {@link HandlerLibrary} - Tracks registered {@link OXRTHandler handlers} and
 * makes them accessible through {@link #getHandlerFor(String)}.
 * This is important to the AtmosphereChannel and associated Channel handler.
 * The Channel can decide if it is able to process incoming Stanzas into POJOs
 * and back again. The Channel handler can delegate the transformation to the
 * proper OXRTHandler.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HandlerLibrary {

    /**
     * The collection for registered {@link OXRTHandler handlers}.
     */
    private final List<OXRTHandler> handlers;
    
    private final List<String> namespaces;

    /**
     * Initializes a new {@link HandlerLibrary}.
     */
    public HandlerLibrary() {
        super();
        handlers = new CopyOnWriteArrayList<OXRTHandler>();
        namespaces = new CopyOnWriteArrayList<String>();
    }

    /**
     * Gets the appropriate handler for the specified Stanz class.
     * 
     * @param stanzaClass The Stanza subclass we want to transform.
     * @return The appropriate handler or <code>null</code> if none is applicable.
     */
    public OXRTHandler getHandlerFor(String namespace) {
        for (OXRTHandler handler : handlers) {
            if (handler.getNamespace().equals(namespace)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Adds specified handler/transformer to this library.
     * 
     * @param transformer The handler to add
     */
    public void add(OXRTHandler transformer) {
        boolean isAdded = handlers.add(transformer);
        if(isAdded) {
            namespaces.add(transformer.getNamespace());
        }
    }

    /**
     * Removes specified handler/transformer from this library.
     * 
     * @param transformer The handler to remove
     */
    public void remove(OXRTHandler transformer) {
        handlers.remove(transformer);
        boolean isRemoved = handlers.remove(transformer);
        if(isRemoved) {
            namespaces.remove(transformer.getNamespace());
        }
    }
    
    /**
     * Get the collected namespaces the registered OXRTHandlers are able to transform.
     * @return the collected namespaces the registered OXRTHandlers are able to transform.
     */
    public Set<String> getManageableNamespaces() {
        return new HashSet<String>(namespaces);
    }
    
}
