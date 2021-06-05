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
import com.openexchange.control.consoleold.internal.ValueObject;
import com.openexchange.control.consoleold.internal.ValueParser;
import com.openexchange.control.internal.BundleNotFoundException;

/**
 * {@link InstallBundle} - The console handler for <code>&quot;installbundle&quot;</code> command.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class InstallBundle extends AbstractConsoleHandler {

    protected String location;

    /**
     * Initializes a new {@link InstallBundle} with specified arguments and performs {@link #install(String) install}.
     *
     * @param args The command-line arguments
     */
    public InstallBundle(final String args[]) {
        try {
            init(args);
            final ValueParser valueParser = getParser();
            final ValueObject[] valueObjectArray = valueParser.getValueObjects();
            if (valueObjectArray.length > 0) {
                location = valueObjectArray[0].getValue();
                install(location);
            } else {
                showHelp();
                exit();
            }
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

    public InstallBundle(final String jmxHost, final int jmxPort, final String jmxLogin, final String jmxPassword) throws Exception {
        initJMX(jmxHost, jmxPort, jmxLogin, jmxPassword);
    }

    public void install(final String location) throws Exception {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();
        mBeanServerConnection.invoke(objectName, "install", new Object[] { location }, new String[] { "java.lang.String" });
    }

    public static void main(final String args[]) {
        new InstallBundle(args);
    }

    @Override
    protected void showHelp() {
        System.out.println("installbundle (-h <jmx host> -p <jmx port> -l (optional) <jmx login> -pw (optional) <jmx password>) location");
    }

    @Override
    protected void exit() {
        System.exit(1);
    }

    @Override
    protected void exit(int code) {
        System.exit(code);
    }

    @Override
    protected String[] getParameter() {
        return DEFAULT_PARAMETER;
    }
}
