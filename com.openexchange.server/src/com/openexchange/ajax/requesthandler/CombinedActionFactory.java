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

package com.openexchange.ajax.requesthandler;

import java.util.Arrays;
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
            } catch (OXException e) {
                // Next
            }
        }
        return null;
    }
}
