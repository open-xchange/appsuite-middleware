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

package com.openexchange.voipnow.json.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.api2.ContactInterfaceFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.osgi.RegistryServiceTrackerCustomizer;
import com.openexchange.user.UserService;
import com.openexchange.voipnow.json.actions.VoipNowActionFactory;
import com.openexchange.voipnow.json.preferences.GUI;
import com.openexchange.voipnow.json.preferences.VoipNowEnabled;
import com.openexchange.voipnow.json.preferences.VoipNowFaxAddress;
import com.openexchange.voipnow.json.services.ServiceRegistry;

/**
 * {@link VoipNowActivator} - Activator for VoipNow component.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VoipNowActivator extends AJAXModuleActivator {

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    /**
     * Initializes a new {@link VoipNowActivator}.
     */
    public VoipNowActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            PREFIX.set(getService(DispatcherPrefixService.class));
            /*
             * Register user multiple service
             */
            registerModule(VoipNowActionFactory.getInstance(), com.openexchange.voipnow.json.Constants.MODULE);
            registerService(PreferencesItemService.class, new VoipNowEnabled());
            registerService(PreferencesItemService.class, new VoipNowFaxAddress());
            registerService(PreferencesItemService.class, new GUI());
            /*
             * User service tracker
             */
            final ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();
            track(UserService.class, new RegistryServiceTrackerCustomizer<UserService>(context, serviceRegistry, UserService.class));
            /*
             * Configuration service tracker
             */
            track(ConfigurationService.class, new InitializingRegistryServiceTrackerCustomizer(context, serviceRegistry));
            /*
             * Contact interface factory tracker
             */
            track(ContactInterfaceFactory.class, new RegistryServiceTrackerCustomizer<ContactInterfaceFactory>(
                context,
                serviceRegistry,
                ContactInterfaceFactory.class));
            /*
             * HTTP service tracker
             */
            track(ContactInterfaceDiscoveryService.class, new RegistryServiceTrackerCustomizer<ContactInterfaceDiscoveryService>(
                context,
                serviceRegistry,
                ContactInterfaceDiscoveryService.class));
            openTrackers();
        } catch (final Throwable e) {
            final org.apache.commons.logging.Log LOG =
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(VoipNowActivator.class));
            LOG.error(e.getMessage(), e);
            // throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            cleanUp();
            PREFIX.set(null);
        } catch (final Exception e) {
            final org.apache.commons.logging.Log LOG =
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(VoipNowActivator.class));
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

}
