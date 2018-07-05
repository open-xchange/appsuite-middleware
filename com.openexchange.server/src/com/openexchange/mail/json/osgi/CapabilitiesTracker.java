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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link CapabilitiesTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class CapabilitiesTracker implements ServiceTrackerCustomizer<CapabilityService, CapabilityService> {

    private final BundleContext context;
    private List<ServiceRegistration<CapabilityChecker>> serviceRegistrations;

    /**
     * Initializes a new {@link CapabilitiesTracker}.
     *
     * @param context The bundle context
     */
    public CapabilitiesTracker(BundleContext context) {
        this.context = context;
    }

    private void declareCapability(String capability, CapabilityChecker optChecker, CapabilityService service) {
        if (null != optChecker) {
            List<ServiceRegistration<CapabilityChecker>> serviceRegistrations = this.serviceRegistrations;
            if (null == serviceRegistrations) {
                serviceRegistrations = new ArrayList<>(4);
                this.serviceRegistrations = serviceRegistrations;
            }

            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, capability);
            serviceRegistrations.add(context.registerService(CapabilityChecker.class, optChecker, properties));
        }

        // Declare capability
        service.declareCapability(capability);
    }

    @Override
    public synchronized CapabilityService addingService(final ServiceReference<CapabilityService> reference) {
        CapabilityService service = context.getService(reference);

        {
            final String sCapability = "publish_mail_attachments";
            CapabilityChecker capabilityChecker = new CapabilityChecker() {

                @Override
                public boolean isEnabled(String capabilityToCheck, Session ses) throws OXException {
                    if (sCapability.equals(capabilityToCheck)) {
                        return false;
                    }

                    return true;
                }
            };
            declareCapability(sCapability, capabilityChecker, service);
        }

        boolean b = false;
        if (b) {
            final String sCapability = "text_preview";
            CapabilityChecker capabilityChecker = new CapabilityChecker() {

                @Override
                public boolean isEnabled(String capabilityToCheck, Session ses) throws OXException {
                    if (false == sCapability.equals(capabilityToCheck)) {
                        return true;
                    }
                    ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous()) {
                        return false;
                    }

                    MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
                    try {
                        mailAccess = MailAccess.getInstance(ses, MailAccount.DEFAULT_ID);
                        mailAccess.connect();
                        return mailAccess.getMailConfig().getCapabilities().hasTextPreview();
                    } finally {
                        if (mailAccess != null) {
                            mailAccess.close(true);
                        }
                    }

                }
            };
            declareCapability(sCapability, capabilityChecker, service);
        }

        // Return tracked service
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<CapabilityService> reference, final CapabilityService service) {
        // Ignore
    }

    @Override
    public synchronized void removedService(final ServiceReference<CapabilityService> reference, final CapabilityService service) {
        List<ServiceRegistration<CapabilityChecker>> serviceRegistrations = this.serviceRegistrations;
        if (null != serviceRegistrations) {
            this.serviceRegistrations = null;
            for (ServiceRegistration<CapabilityChecker> serviceRegistration : serviceRegistrations) {
                serviceRegistration.unregister();
            }
        }

        context.ungetService(reference);
    }
}