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

package com.openexchange.osgi.console.osgi;

import java.util.regex.Pattern;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.console.ServiceStateCommandProvider;
import com.openexchange.osgi.console.ServiceStateLookup;

/**
 * {@link ConsoleActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConsoleActivator implements BundleActivator, CommandProvider {

    private ServiceTracker<ServiceStateLookup, ServiceStateLookup> tracker;

    private volatile BundleContext context;

    protected volatile ServiceRegistration<CommandProvider> registration;

    private volatile ServiceRegistration<CommandProvider> thisRegistration;

    @Override
    public void start(final BundleContext context) throws Exception {
        this.context = context;
        tracker =
            new ServiceTracker<ServiceStateLookup, ServiceStateLookup>(
                context,
                ServiceStateLookup.class,
                new ServiceTrackerCustomizer<ServiceStateLookup, ServiceStateLookup>() {

                    @Override
                    public ServiceStateLookup addingService(final ServiceReference<ServiceStateLookup> reference) {
                        final ServiceStateLookup lookup = context.getService(reference);
                        registration = context.registerService(CommandProvider.class, new ServiceStateCommandProvider(lookup), null);
                        return lookup;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<ServiceStateLookup> reference, final ServiceStateLookup service) {
                        // Ignore
                    }

                    @Override
                    public void removedService(final ServiceReference<ServiceStateLookup> reference, final ServiceStateLookup service) {
                        context.ungetService(reference);
                        registration.unregister();
                    }
                });
        tracker.open();
        thisRegistration = context.registerService(CommandProvider.class, this, null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        this.context = null;
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
        final ServiceRegistration<CommandProvider> thisRegistration = this.thisRegistration;
        if (null != thisRegistration) {
            thisRegistration.unregister();
            this.thisRegistration = null;
        }
    }

    @Override
    public String getHelp() {
        return "\tuname - returns framework information\n\tfind <wild-card-expression> - looks-up services matching given search expression\n";
    }

    public void _uname(final CommandInterpreter ci) {
        final BundleContext context = this.context;
        final String vendor = context.getProperty(Constants.FRAMEWORK_VENDOR);
        final String version = context.getProperty(Constants.FRAMEWORK_VERSION);
        final String osName = context.getProperty(Constants.FRAMEWORK_OS_NAME);
        final String osVersion = context.getProperty(Constants.FRAMEWORK_OS_VERSION);
        ci.println("\n " + vendor + " " + version + " (" + osName + " " + osVersion + ")");
    }

    public void _find(final CommandInterpreter ci) throws Exception {
        final BundleContext context = this.context;
        final ServiceReference<?>[] references = context.getAllServiceReferences(null, null);
        final String filter = ci.nextArgument();
        if (null == filter) {
            ci.println("Missing argument for 'find'; e.g. \"find *MyService\"");
        } else {
            final Pattern pattern = Pattern.compile(wildcardToRegex(filter), Pattern.CASE_INSENSITIVE);
            for (final ServiceReference<?> serviceReference : references) {
                final String className = context.getService(serviceReference).getClass().getName();
                if (pattern.matcher(className).matches()) {
                    ci.println(className);
                }
                context.ungetService(serviceReference);
            }
        }
    }

    /**
     * Converts specified wild-card string to a regular expression
     *
     * @param wildcard The wild-card string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    private static String wildcardToRegex(final String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

}
