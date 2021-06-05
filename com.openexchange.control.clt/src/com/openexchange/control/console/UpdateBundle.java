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
 * {@link UpdateBundle} - The console handler for <code>&quot;updatebundle&quot;</code> command.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class UpdateBundle extends AbstractConsoleHandler {

    protected String bundleName;

    protected boolean autorefresh = false;

    /**
     * Initializes a new {@link UpdateBundle} with specified arguments and performs {@link #update(String, boolean) update}.
     *
     * @param args The command-line arguments
     */
    public UpdateBundle(final String args[]) {
        try {
            init(args);
            final ValueParser valueParser = getParser();
            final ValueObject[] valueObjectArray = valueParser.getValueObjects();
            if (valueObjectArray.length > 0) {
                bundleName = valueObjectArray[0].getValue();
                if (valueObjectArray.length > 1) {
                    autorefresh = Boolean.parseBoolean(valueObjectArray[1].getValue());
                }
                update(bundleName, autorefresh);
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

    public UpdateBundle(final String jmxHost, final int jmxPort, final String jmxLogin, final String jmxPassword) throws Exception {
        initJMX(jmxHost, jmxPort, jmxLogin, jmxPassword);
    }

    public void update(final String bundleName, final boolean autorefresh) throws Exception {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();
        mBeanServerConnection.invoke(objectName, "update", new Object[] { bundleName, Boolean.valueOf(autorefresh) }, new String[] {
            "java.lang.String", "boolean" });
    }

    public static void main(final String args[]) {
        new UpdateBundle(args);
    }

    @Override
    protected void showHelp() {
        System.out.println("updatebundle (-h <jmx host> -p <jmx port> -l (optional) <jmx login> -pw (optional) <jmx password>) bundlename (autorefresh (true|false) default value is false)");
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
