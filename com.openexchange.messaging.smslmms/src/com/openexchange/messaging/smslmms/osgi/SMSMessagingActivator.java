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

package com.openexchange.messaging.smslmms.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.smslmms.SMSMessagingMessage;
import com.openexchange.messaging.smslmms.SMSMessagingService;
import com.openexchange.messaging.smslmms.api.DefaultSMSMessage;
import com.openexchange.messaging.smslmms.api.SMSService;
import com.openexchange.messaging.smslmms.internal.SMSFolderSupportPreferencesItem;
import com.openexchange.messaging.smslmms.internal.SMSPreferencesItem;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link SMSMessagingActivator} - The activator for SMS/MMS bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMSMessagingActivator extends HousekeepingActivator {

    protected volatile ServiceRegistration<MessagingService> messagingServiceRegistration;

    /**
     * Initializes a new {@link SMSMessagingActivator}.
     */
    public SMSMessagingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ManagedFileManagement.class, SessiondService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(SMSMessagingActivator.class));
        SMSMessagingMessage.setServiceLookup(this);
        DefaultSMSMessage.setServiceLookup(this);
        trackService(SMSService.class);
        /*
         * Publish tracked SMSService as MessagingService
         */
        final BundleContext context = this.context;
        track(SMSService.class, new SimpleRegistryListener<SMSService>() {

            @Override
            public void added(final ServiceReference<SMSService> ref, final SMSService service) {
                synchronized (this) {
                    if (null != messagingServiceRegistration) {
                        logger.error("Detected multiple SMS/MMS services!");
                        return;
                    }
                    messagingServiceRegistration = context.registerService(MessagingService.class, new SMSMessagingService(service), null);
                }
            }

            @Override
            public void removed(final ServiceReference<SMSService> ref, final SMSService service) {
                final ServiceRegistration<MessagingService> registration = messagingServiceRegistration;
                if (null != registration) {
                    registration.unregister();
                    messagingServiceRegistration = null;
                }
            }
        });
        openTrackers();

        registerService(PreferencesItemService.class, new SMSPreferencesItem(this));
        registerService(PreferencesItemService.class, new SMSFolderSupportPreferencesItem(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        SMSMessagingMessage.setServiceLookup(null);
        DefaultSMSMessage.setServiceLookup(null);
        final ServiceRegistration<MessagingService> registration = messagingServiceRegistration;
        if (null != registration) {
            registration.unregister();
            messagingServiceRegistration = null;
        }
        super.stopBundle();
    }

}
