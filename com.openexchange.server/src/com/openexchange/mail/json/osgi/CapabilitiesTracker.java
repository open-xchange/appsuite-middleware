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
    public synchronized CapabilityService addingService(ServiceReference<CapabilityService> reference) {
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
    public void modifiedService(ServiceReference<CapabilityService> reference, CapabilityService service) {
        // Ignore
    }

    @Override
    public synchronized void removedService(ServiceReference<CapabilityService> reference, CapabilityService service) {
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