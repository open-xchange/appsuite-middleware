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

package com.openexchange.spamhandler.parallels.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.spamhandler.parallels.ParallelsSpamdService;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;
import com.openexchange.user.UserService;

/**
 * Registers the SpamdService implementation as an OSGi service.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpamdServiceRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamdServiceRegisterer.class);

    private final BundleContext context;
    private ServiceRegistration<SpamdService> registration;
    private ConfigViewFactory factory;
    private ContextService contextService;
    private UserService userService;

    /**
     * Initializes a new {@link SpamdServiceRegisterer}.
     *
     * @param context The bundle context
     */
    public SpamdServiceRegisterer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        {
            if (obj instanceof ConfigViewFactory) {
                factory = (ConfigViewFactory) obj;
            }
            if (obj instanceof ContextService) {
                contextService = (ContextService) obj;
            }
            if (obj instanceof UserService) {
                userService = (UserService) obj;
            }
            needsRegistration = null != factory && null != contextService && null != userService && null == registration;
        }
        if (needsRegistration) {
            LOG.info("Registering Parallels spam handler service.");
            registration = context.registerService(SpamdService.class, new ParallelsSpamdService(factory, userService), null);
        }
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing to do.
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<?> unregister = null;
        {
            if (service instanceof ConfigViewFactory) {
                factory = null;
            }
            if (service instanceof ContextService) {
                contextService = null;
            }
            if (service instanceof UserService) {
                userService = null;
            }
            if (null != registration && (null == contextService || null == userService || null == factory)) {
                unregister = registration;
                registration = null;
            }
        }
        if (null != unregister) {
            LOG.info("Unregistering Parallels spam handler service.");
            unregister.unregister();
        }
        context.ungetService(reference);
    }
}
