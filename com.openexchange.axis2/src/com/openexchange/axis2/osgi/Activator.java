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

package com.openexchange.axis2.osgi;

import java.util.LinkedList;
import java.util.Queue;
import org.osgi.framework.BundleActivator;
import org.osgi.service.http.HttpService;
import com.openexchange.axis2.internal.Axis2ServletInit;
import com.openexchange.axis2.services.Axis2ServletServices;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link Activator}
 */
public class Activator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(Activator.class);

    private volatile Queue<BundleActivator> activators;

    /**
     * Initializes a new {@link Axis2ServletActivator}
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class };
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize server service registry with available services
             */
            Axis2ServletServices.setServiceLookup(this);
            Axis2ServletInit.getInstance().start();
            /*
             * Start-up 3rd party activators
             */
            final Queue<BundleActivator> activators = new LinkedList<BundleActivator>();
            this.activators = activators;
            {
                final org.apache.axis2.osgi.internal.Activator axis2Activator = new org.apache.axis2.osgi.internal.Activator();
                axis2Activator.start(context);
                activators.offer(axis2Activator);
            }
            {
                final org.apache.axiom.locator.Activator axiomActivator = new org.apache.axiom.locator.Activator();
                axiomActivator.start(context);
                activators.offer(axiomActivator);
            }
            {
                org.apache.geronimo.osgi.locator.Activator geronimoActivator = new org.apache.geronimo.osgi.locator.Activator();
                geronimoActivator.start(context);
                activators.offer(geronimoActivator);
            }

            // TODO: ConfigTree may be needed or not...
            // serviceRegistration = context.registerService(PreferencesItemService.class.getName(), new MailFilterPreferencesItem(), null);
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }

    }

    @Override
    public void stopBundle() throws Exception {
        try {
            final Queue<BundleActivator> activators = this.activators;
            if (null != activators) {
                BundleActivator activator;
                while ((activator = activators.poll()) != null) {
                    activator.stop(context);
                }
                this.activators = null;
            }

            Axis2ServletInit.getInstance().stop();
            /*
             * Clear service registry
             */
            Axis2ServletServices.setServiceLookup(null);
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        } finally {
            started.set(false);
        }
    }

}
