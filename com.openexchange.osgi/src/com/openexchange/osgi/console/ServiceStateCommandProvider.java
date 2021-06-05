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

package com.openexchange.osgi.console;

import java.util.Collections;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

/**
 * {@link ServiceStateCommandProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServiceStateCommandProvider implements CommandProvider {

    private final ServiceStateLookup stateLookup;

    /**
     * Initializes a new {@link ServiceStateCommandProvider}.
     *
     * @param stateLookup The service state lookup
     */
    public ServiceStateCommandProvider(final ServiceStateLookup stateLookup) {
        this.stateLookup = stateLookup;
    }

    public Object _serviceState(final CommandInterpreter intp) {
        final String bundleName = intp.nextArgument();
        if (bundleName == null) {
            final List<String> names = stateLookup.getNames();
            Collections.sort(names);
            for (final String name : names) {
                print(name, stateLookup.determineState(name), intp);
            }
        } else {
            print(bundleName, stateLookup.determineState(bundleName), intp);
        }

        return null;
    }

    private void print(final String name, final ServiceState state, final CommandInterpreter intp) {
        final StringBuilder builder = new StringBuilder();
        builder.append("=====[").append(name).append("]=====");
        intp.println(builder);
        if (state == null) {
            intp.println("Nothing known. Sorry.");
            return;
        }
        final List<String> missing = state.getMissingServices();
        final List<String> present = state.getPresentServices();

        intp.println("Present (" + present.size() + ")");
        print(present, intp);

        intp.println("Missing (" + missing.size() + ")");
        print(missing, intp);

    }

    private void print(final List<String> list, final CommandInterpreter intp) {
        for (final String string : list) {
            intp.println("\t" + string);
        }
    }

    public Object _missing(final CommandInterpreter intp) {
        final String bundleName = intp.nextArgument();
        if (bundleName == null) {
            final List<String> names = stateLookup.getNames();
            Collections.sort(names);
            for (final String name : names) {
                final ServiceState state = stateLookup.determineState(name);
                final List<String> services = state.getMissingServices();
                if (!services.isEmpty()) {
                    intp.println("=====[" + name + " Missing Services ]=====");
                    print(services, intp);
                }
            }
        } else {
            final ServiceState state = stateLookup.determineState(bundleName);
            final List<String> services = state.getMissingServices();
            if (!services.isEmpty()) {
                intp.println("=====[" + bundleName + " Missing Services]=====");
                print(services, intp);
            } else {
                intp.println("All services were found");
            }
        }

        return null;
    }

    @Override
    public String getHelp() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("---Deferred Activator Services---\n\t");
        builder.append("serviceState - Print all service states. Give a bundle name as argument to list only the state of that bundle.\n\t");
        builder.append("missing - Print all bundles with missing services. Give a bundle name as argument to find the missing services for that bundle only.\n");
        return builder.toString();
    }

}
