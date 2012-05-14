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

package com.openexchange.authentication.imap.osgi;

import static com.openexchange.authentication.imap.osgi.ImapAuthServiceRegistry.getServiceRegistry;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.imap.impl.IMAPAuthentication;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.log.LogFactory;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.user.UserService;

public class ActivatorNew extends DeferredActivator {

    private static transient final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ActivatorNew.class));

    private ServiceRegistration<AuthenticationService> registration;

    // private ServiceRegistration serviceRegistration;

    public ActivatorNew() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ContextService.class, UserService.class, MailAccountStorageService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }

        getServiceRegistry().addService(clazz, getService(clazz));
        // wenn alle services da und nicht authservice published, dann authservice publishen
        if (registration == null) {
            registration = context.registerService(AuthenticationService.class, new IMAPAuthentication(), null);
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        getServiceRegistry().removeService(clazz);
        // wenn authservice gepublished, dann publish wegnehmen
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    @Override
    protected void startBundle() throws Exception {

        try {
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            if (registration == null) {
                // authservice publishen
                registration = context.registerService(AuthenticationService.class, new IMAPAuthentication(), null);
            }
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            // wenn authservice gepublished, dann publish wegnehmen
            if (registration != null) {
                registration.unregister();
                registration = null;
            }

            getServiceRegistry().clearRegistry();
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }
}
