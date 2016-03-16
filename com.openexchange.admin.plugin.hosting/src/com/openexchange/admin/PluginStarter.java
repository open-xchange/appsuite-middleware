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

package com.openexchange.admin;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.admin.rmi.OXContextGroupInterface;
import com.openexchange.admin.rmi.impl.OXContextGroup;

public class PluginStarter {

    private BundleContext context;
    private final List<ServiceRegistration<Remote>> services = new LinkedList<ServiceRegistration<Remote>>();

    /**
     * Initializes a new {@link PluginStarter}.
     */
    public PluginStarter() {
        super();
    }

    public void start(BundleContext context) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PluginStarter.class);
        try {
            this.context = context;

            // Create all OLD Objects and bind export them
            com.openexchange.admin.rmi.impl.OXContext oxctx_v2 = new com.openexchange.admin.rmi.impl.OXContext(context);

            // bind all NEW Objects to registry
            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", com.openexchange.admin.rmi.OXContextInterface.RMI_NAME);
            services.add(context.registerService(Remote.class, oxctx_v2, properties));
            
            // Register RMI interface for ContextGroup
            properties = new Hashtable<String, Object>(2);
            properties.put("RMIName", OXContextGroupInterface.RMI_NAME);
            OXContextGroupInterface oxContextGroup = new OXContextGroup();
            services.add(context.registerService(Remote.class, oxContextGroup, properties));
        } catch (Exception e) {
            logger.error("Error while creating one instance for RMI interface", e);
            throw e;
        }
    }

    public void stop() {
        for (ServiceRegistration<Remote> registration : services) {
            context.ungetService(registration.getReference());
        }
    }

}
