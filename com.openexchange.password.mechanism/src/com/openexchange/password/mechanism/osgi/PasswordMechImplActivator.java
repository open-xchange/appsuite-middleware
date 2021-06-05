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

package com.openexchange.password.mechanism.osgi;

import static com.openexchange.java.Autoboxing.L;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.impl.mech.PasswordMechRegistryImpl;

/**
 * {@link PasswordMechImplActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - moved from c.o.global
 * @since v7.10.2
 */
public class PasswordMechImplActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        long before = currentTimeMillis();
        getLogger(PasswordMechImplActivator.class).info("Starting bundle {}", context.getBundle().getSymbolicName());

        Services.setServiceLookup(this);

        PasswordMechRegistryImpl passwordMechRegistryImpl = new PasswordMechRegistryImpl(getService(ConfigurationService.class));

        track(PasswordMech.class, new PasswordMechServiceTrackerCustomizer(context, passwordMechRegistryImpl));
        openTrackers();

        registerService(PasswordMechRegistry.class, passwordMechRegistryImpl, null);
        registerService(Reloadable.class, passwordMechRegistryImpl);
        long after = currentTimeMillis();
        long duration = after - before;
        if (duration > 20000) {
            getLogger(PasswordMechImplActivator.class).warn("STARTING BUNDLE {} TOOK {}ms! PLEASE MAKE SURE TO HAVE AN APPROPRIATE LEVEL OF ENTROPY ON THE SYSTEM!", context.getBundle().getSymbolicName(), L(duration));
        } else {
            getLogger(PasswordMechImplActivator.class).info("Starting bundle {} took {}ms", context.getBundle().getSymbolicName(), L(duration));
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(PasswordMechImplActivator.class).info("Stopping bundle {}", context.getBundle().getSymbolicName());

        Services.setServiceLookup(null);

        super.stopBundle();
    }
}
