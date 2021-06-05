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

package com.openexchange.control.console;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.openexchange.control.internal.BundleNotFoundException;

/**
 * {@link RefreshBundles} - The console handler for <code>&quot;refreshbundles&quot;</code> command.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class RefreshBundles extends AbstractConsoleHandler {

    /**
     * Initializes a new {@link RefreshBundles} with specified arguments and performs {@link #refresh() refresh}.
     *
     * @param args The command-line arguments
     */
    public RefreshBundles(final String args[]) {
        boolean error = true;
        try {
            init(args, true);
            refresh();
            error = false;
        } catch (Exception exc) {
            final Throwable cause = exc.getCause();
            if (null == cause) {
                System.out.println(exc.getMessage());
                exc.printStackTrace(System.out);
            } else {
                if (cause instanceof BundleNotFoundException) {
                    System.out.println(cause.getMessage());
                } else {
                    System.out.println(exc.getMessage());
                    exc.printStackTrace(System.out);
                }
            }
        } finally {
            try {
                close();
            } catch (Exception exc) {
                System.out.println("closing all connections failed: " + exc.getMessage());
                exc.printStackTrace(System.out);
            }
        }
        if (error) {
            exit();
        }
    }

    public RefreshBundles(final String jmxHost, final int jmxPort, final String jmxLogin, final String jmxPassword) throws Exception {
        initJMX(jmxHost, jmxPort, jmxLogin, jmxPassword);
    }

    public void refresh() throws Exception {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();
        mBeanServerConnection.invoke(objectName, "refresh", new Object[] {}, new String[] {});
    }

    @SuppressWarnings("unused")
    public static void main(final String args[]) {
        new RefreshBundles(args);
    }

    @Override
    protected void showHelp() {
        System.out.println("refreshbundles (-h <jmx host> -p <jmx port> -l (optional) <jmx login> -pw (optional) <jmx password>)");
    }

    @Override
    protected void exit() {
        System.exit(1);
    }

    @Override
    protected String[] getParameter() {
        return DEFAULT_PARAMETER;
    }
}
