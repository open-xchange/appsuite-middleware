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

package com.openexchange.authentication.kerberos.osgi;

import static com.openexchange.osgi.Tools.generateServiceFilter;
import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.osgi.Tools;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * Activator for Kerberos authentication bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AuthenticationActivator implements BundleActivator {

    private final Stack<ServiceTracker<?, ?>> trackers = new Stack<ServiceTracker<?, ?>>();

    public AuthenticationActivator() {
        super();
    }

    @Override
    public void start(BundleContext context) throws InvalidSyntaxException {
        Filter filter = generateServiceFilter(context, KerberosService.class, ContextService.class, UserService.class, ConfigurationService.class);
        trackers.push(new ServiceTracker<Object, Object>(context, filter, new AuthenticationRegisterer(context)));
        filter = generateServiceFilter(context, TimerService.class, KerberosService.class);
        trackers.push(new ServiceTracker<Object, Object>(context, filter, new RenewalLoginHandlerRegisterer(context)));
        Tools.open(trackers);
    }

    @Override
    public void stop(final BundleContext context) {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
    }
}
