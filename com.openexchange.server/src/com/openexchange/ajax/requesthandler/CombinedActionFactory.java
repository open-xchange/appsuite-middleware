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

package com.openexchange.ajax.requesthandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;

/**
 * {@link CombinedActionFactory} - Gathers {@link AJAXActionServiceFactory AJAX action factories} which share the same module identifier but
 * offer different actions as indicated by {@link Module} annotation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CombinedActionFactory implements AJAXActionServiceFactory {

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<AJAXActionServiceFactory, Object> facs;

    private final ConcurrentMap<String, Object> actions;

    /**
     * Initializes a new {@link CombinedActionFactory}.
     */
    public CombinedActionFactory() {
        super();
        facs = new ConcurrentHashMap<AJAXActionServiceFactory, Object>(2, 0.9f, 1);
        actions = new ConcurrentHashMap<String, Object>(6, 0.9f, 1);
    }

    /**
     * Checks if this combined factory is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return facs.isEmpty();
    }

    /**
     * Adds specified factory (if not already contained).
     *
     * @param factory The factory to add
     * @throws IllegalArgumentException If factory has no {@link Module} annotation or an action-conflict is detected
     */
    public void add(final AJAXActionServiceFactory factory) {
        if (null == facs.put(factory, PRESENT)) {
            final Set<String> actions = getActionsFrom(factory);
            final ConcurrentMap<String, Object> thisActions = this.actions;
            for (final String action : actions) {
                if (thisActions.containsKey(action)) {
                    throw new IllegalArgumentException(
                        "Conflicting action \"" + action + "\" provided by factory \"" + factory.getClass() + "\".");
                }
                thisActions.put(action, PRESENT);
            }
        }
    }

    private static Set<String> getActionsFrom(final AJAXActionServiceFactory factory) {
        final Module moduleAnnotation = factory.getClass().getAnnotation(Module.class);
        if (null == moduleAnnotation) {
            throw new IllegalArgumentException("Specified factory has no \""+Module.class.getName()+"\" annotation: " + factory.getClass());
        }
        return new HashSet<String>(Arrays.asList(moduleAnnotation.actions()));
    }

    /**
     * Removes specified factory.
     *
     * @param factory The factory to remove
     */
    public void remove(final AJAXActionServiceFactory factory) {
        facs.remove(factory);
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        for (final AJAXActionServiceFactory factory : facs.keySet()) {
            try {
                final AJAXActionService service = factory.createActionService(action);
                if (null != service) {
                    return service;
                }
            } catch (final OXException e) {
                // Next
            }
        }
        return null;
    }

    @Override
    public Collection<?> getSupportedServices() {
        final Collection<Object> supportedServices = new ArrayList<Object>();
        for (final AJAXActionServiceFactory factory : facs.keySet()) {
            supportedServices.addAll(factory.getSupportedServices());
        }
        return supportedServices;
    }

}
