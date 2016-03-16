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
 *    trademarks of the OX Software GmbH group of companies.
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
