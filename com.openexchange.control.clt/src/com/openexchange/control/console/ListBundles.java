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

import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import com.openexchange.control.internal.BundleNotFoundException;

/**
 * {@link ListBundles} - The console handler for <code>&quot;listbundles&quot;</code> command.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ListBundles extends AbstractConsoleHandler {

    /**
     * Initializes a new {@link ListBundles} with specified arguments and performs {@link #listBundles() list bundles}.
     *
     * @param args The command-line arguments
     */
    public ListBundles(final String args[]) {
        try {
            init(args, true);
            listBundles();
        } catch (Exception exc) {
            final Throwable cause = exc.getCause();
            if (cause != null) {
                if (cause instanceof BundleNotFoundException) {
                    System.out.println(cause.getMessage());
                } else {
                    exc.printStackTrace();
                }
            } else {
                exc.printStackTrace();
            }
            exit();
        } finally {
            try {
                close();
            } catch (Exception exc) {
                System.out.println("closing all connections failed: " + exc);
                exc.printStackTrace();
            }
        }
    }

    public void listBundles() throws Exception {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();
        @SuppressWarnings("unchecked") final List<Map<String, String>> bundleList = (List<Map<String, String>>) mBeanServerConnection.invoke(
            objectName,
            "list",
            new Object[] {},
            new String[] {});
        for (int a = 0; a < bundleList.size(); a++) {
            final Map<String, String> data = bundleList.get(a);
            System.out.println("bundlename: " + data.get("bundlename") + " status: " + data.get("status"));
        }
    }

    @SuppressWarnings("unused")
    public static void main(final String args[]) {
        new ListBundles(args);
    }

    @Override
    protected void showHelp() {
        System.out.println("listbundles (-h <jmx host> -p <jmx port> -l (optional) <jmx login> -pw (optional) <jmx password>)");
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
