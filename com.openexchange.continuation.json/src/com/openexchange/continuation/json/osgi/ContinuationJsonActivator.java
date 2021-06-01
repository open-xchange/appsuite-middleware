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

package com.openexchange.continuation.json.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.continuation.json.ContinuationActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ContinuationJsonActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class ContinuationJsonActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link ContinuationJsonActivator}.
     */
    public ContinuationJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, CapabilityService.class, ContinuationRegistryService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Register AJAX module
        registerModule(new ContinuationActionFactory(this), "continuation");

        // Register CapabilityChecker
        final String sCapability = "continuation";
        {
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new CapabilityChecker() {
                @Override
                public boolean isEnabled(final String capability, final Session ses) throws OXException {
                    if (sCapability.equals(capability)) {
                        final ServerSession session = ServerSessionAdapter.valueOf(ses);
                        if (session.isAnonymous()) {
                            return false;
                        }
                        final ConfigViewFactory factory = getService(ConfigViewFactory.class);
                        final ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                        final ComposedConfigProperty<Boolean> property = view.property("com.openexchange.continuation.enabled", boolean.class);
                        return (property.isDefined() && property.get().booleanValue());
                    }
                    return true;
                }
            }, properties);
        }

        // Declare capability
        getService(CapabilityService.class).declareCapability(sCapability);
    }

}
