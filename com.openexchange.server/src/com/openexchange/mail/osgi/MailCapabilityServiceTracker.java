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

package com.openexchange.mail.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link MailCapabilityServiceTracker} - A <code>ServiceTrackerCustomizer</code> for <code>CapabilityService</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailCapabilityServiceTracker implements ServiceTrackerCustomizer<CapabilityService, CapabilityService> {

    private static final String MSISDN = "msisdn";

    private final BundleContext context;
    private volatile ServiceRegistration<CapabilityChecker> registration;

    /**
     * Initializes a new {@link MailCapabilityServiceTracker}.
     */
    public MailCapabilityServiceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public CapabilityService addingService(ServiceReference<CapabilityService> reference) {
        final CapabilityService service = context.getService(reference);

        final CapabilityChecker checker = new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session ses) throws OXException {
                if (MSISDN.equals(capability)) {
                    ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous() || !session.getUserPermissionBits().hasWebMail()) {
                        return false;
                    }
                    return MailProperties.getInstance().isSupportMsisdnAddresses();
                }
                return true;
            }
        };
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, MSISDN);
        registration = context.registerService(CapabilityChecker.class, checker, properties);

        service.declareCapability(MSISDN);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<CapabilityService> reference, CapabilityService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<CapabilityService> reference, CapabilityService service) {
        final ServiceRegistration<CapabilityChecker> registration = this.registration;
        if (null != registration) {
            registration.unregister();
            this.registration = null;
        }

        context.ungetService(reference);
    }

}
