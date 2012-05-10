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

package com.openexchange.oauth.json.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.log.LogFactory;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.oauthaccount.actions.AccountActionFactory;
import com.openexchange.oauth.json.oauthmeta.actions.MetaDataActionFactory;
import com.openexchange.oauth.json.service.ServiceRegistry;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;

/**
 * {@link OAuthJSONActivator} - Activator for JSON OAuth interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthJSONActivator extends AJAXModuleActivator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(OAuthJSONActivator.class));

    private OSGiOAuthService oAuthService;

    private WhiteboardSecretService secretService;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        ServiceRegistry.getInstance().addService(clazz, getService(clazz));
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        ServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            final ServiceRegistry registry = ServiceRegistry.getInstance();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (final Class<?> classe : classes) {
                final Object service = getService(classe);
                if (null != service) {
                    registry.addService(classe, service);
                }
            }
            AbstractOAuthAJAXActionService.PREFIX.set(getService(DispatcherPrefixService.class));
            /*
             * Service trackers
             */
            track(OAuthService.class, new RegistryServiceTrackerCustomizer<OAuthService>(context, registry, OAuthService.class));
            /*
             * Open trackers
             */
            openTrackers();
            /*
             * Service registrations
             */
            registerModule(AccountActionFactory.getInstance(), "oauth/accounts");
            registerModule(MetaDataActionFactory.getInstance(), "oauth/services");
            /*
             * Apply OAuth service to actions
             */
            oAuthService = new OSGiOAuthService().start(context);
            // registry.addService(OAuthService.class, oAuthService);
            AbstractOAuthAJAXActionService.setOAuthService(oAuthService);
            secretService = new WhiteboardSecretService(context);
            secretService.open();
            AbstractOAuthAJAXActionService.setSecretService(secretService);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            if (secretService != null) {
                secretService.close();
            }
            cleanUp();
            if (null != oAuthService) {
                oAuthService.stop();
                oAuthService = null;
            }
            AbstractOAuthAJAXActionService.setOAuthService(null);
            AbstractOAuthAJAXActionService.PREFIX.set(null);
            ServiceRegistry.getInstance().clearRegistry();
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(OAuthJSONActivator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

}
