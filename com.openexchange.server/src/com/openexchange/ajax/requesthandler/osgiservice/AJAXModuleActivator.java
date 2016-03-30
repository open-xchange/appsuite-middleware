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
