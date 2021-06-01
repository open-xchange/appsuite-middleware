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

package com.openexchange.ajax.requesthandler.osgiservice;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.ExtendableAJAXActionServiceFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AJAXModuleActivator} - The {@link BundleActivator activator} to register a module.
 *
 *  @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AJAXModuleActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link AJAXModuleActivator}.
     */
    protected AJAXModuleActivator() {
        super();
    }

    /**
     * Registers specified factory with given module identifier.
     *
     * @param factory The factory to register
     * @param module The module identifier; accessible path would be: &lt;prefix&gt; + <code>"/"</code> + &lt;module&gt;
     */
    public void registerModule(final AJAXActionServiceFactory factory, final String module) {
        this.registerInternal(factory, module, true);
    }

    /**
     * Registers specified factory with given module identifier which is not accessible by multiple module.
     *
     * @param factory The factory to register
     * @param module The module identifier; accessible path would be: &lt;prefix&gt; + <code>"/"</code> + &lt;module&gt;
     */
    public void registerModuleWithoutMultipleAccess(final AJAXActionServiceFactory factory, final String module) {
        this.registerInternal(factory, module, false);
    }

    private void registerInternal(final AJAXActionServiceFactory factory, final String module, final boolean multiple) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(4);
        properties.put("module", module);
        properties.put("multiple", multiple ? "true" : "false");
        /*-
         *
        final Module moduleAnnotation = factory.getClass().getAnnotation(Module.class);
        if (null != moduleAnnotation) {
            final String[] actions = moduleAnnotation.actions();
            if (null != actions && actions.length > 0) {
                final List<String> list = new ArrayList<String>(actions.length);
                for (int i = 0; i < actions.length; i++) {
                    final String action = actions[i];
                    if (action.length() > 0) {
                        list.add(action);
                    }
                }
                properties.put("actions", list);
            }
        }
         *
         */
        registerService(AJAXActionServiceFactory.class, factory, properties);

        if (factory instanceof ExtendableAJAXActionServiceFactory) {
            // Register for other interface, too
            registerService(ExtendableAJAXActionServiceFactory.class, (ExtendableAJAXActionServiceFactory) factory, null);
        }
    }
}
