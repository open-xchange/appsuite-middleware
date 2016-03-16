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

package com.openexchange.control.console;

import java.io.IOException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import com.openexchange.control.consoleold.internal.ValueObject;
import com.openexchange.control.internal.BundleNotFoundException;

/**
 * {@link ShutDown} - The console handler for <code>&quot;shutdown&quot;</code> command.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class ShutDown extends AbstractConsoleHandler {

    /**
     * Initializes a new {@link ShutDown} with specified arguments and performs {@link #shutdown() shutdown}.
     *
     * @param args The command-line arguments
     */
    public ShutDown(final String args[]) {
        boolean completed = false;
        try {
            init(args, true);
            boolean waitForExit = false;
            for (ValueObject valueObject : getParser().getValueObjects()) {
                if ("-w".equals(valueObject.getValue())) {
                    waitForExit = true;
                }
            }
            completed = shutdown(waitForExit) || !waitForExit;
        } catch (final Exception exc) {
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
            } catch (final Exception exc) {
                // Ignore. If the backend terminates successfully exception may appear here.
            }
        }
        if (!completed) {
            System.exit(1);
        }
    }

    public ShutDown(final String jmxHost, final int jmxPort, final String jmxLogin, final String jmxPassword) throws Exception {
        initJMX(jmxHost, jmxPort, jmxLogin, jmxPassword);
    }

    public boolean shutdown(boolean waitForExit) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException, InterruptedException {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();

        waitForUpdateTasksToComplete(objectName, mBeanServerConnection);

        boolean retval = false;
        try {
            Object result = mBeanServerConnection.invoke(objectName, "shutdown", new Object[] { Boolean.valueOf(waitForExit) }, new String[] { "boolean" });
            if (result instanceof Boolean) {
                retval = ((Boolean) result).booleanValue();
            }
        } catch (ReflectionException e) {
            if (e.getCause() instanceof NoSuchMethodException) {
                // Happens only once when upgrading to version 7.4.0 because newer CLT tries to stop older backend which does not have extended method.
                // This can be removed if no version before 7.4.0 is running somewhere anymore.
                mBeanServerConnection.invoke(objectName, "shutdown", new Object[] { }, new String[] { });
            } else {
                throw e;
            }
        }
        return retval;
    }

    private static void waitForUpdateTasksToComplete(ObjectName objectName, MBeanServerConnection mBeanServerConnection) throws InterruptedException, InstanceNotFoundException, MBeanException, IOException, ReflectionException {
        try {
            boolean updateTasksRunning = updateTasksRunning(objectName, mBeanServerConnection);
            if (updateTasksRunning) {
                System.out.print("The process is still executing database update tasks. Waiting for completion");
            }

            while (updateTasksRunning) {
                Thread.sleep(1000);
                System.out.print(" .");
                updateTasksRunning = updateTasksRunning(objectName, mBeanServerConnection);
                if (!updateTasksRunning) {
                    System.out.print(" Done!\n");
                }
            }
        } catch (ReflectionException e) {
            if (e.getCause() instanceof NoSuchMethodException) {
                // Should only happen if an old (pre-7.6.1) server is stopped with a newer shutdown script.
                return;
            } else {
                throw e;
            }
        }
    }

    private static boolean updateTasksRunning(ObjectName objectName, MBeanServerConnection mBeanServerConnection) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        try {
            Object updateTasksRunningRetval = mBeanServerConnection.invoke(objectName, "updateTasksRunning", new Object[0], new String[0]);
            if (updateTasksRunningRetval instanceof Boolean) {
                return ((Boolean) updateTasksRunningRetval).booleanValue();
            }
        } catch (ReflectionException e) {
            // NoSuchMethodException should only happen if an old (pre-7.6.1) server is stopped with a newer shutdown script.
            if (!(e.getCause() instanceof NoSuchMethodException)) {
                throw e;
            }
        }

        return false;
    }

    public static void main(final String args[]) {
        new ShutDown(args);
    }

    @Override
    protected void showHelp() {
        System.out.println("Shuts down the OSGi framework through invoking closure of top-level system bundle.");
        System.out.println("If the parameter -w is defined the tools awaits the completion of the shutdown process and returns if it was successful.");
        System.out.println("Usage: shutdown (-h <jmx host> -p <jmx port> -l (optional) <jmx login> -pw (optional) <jmx password>) (-w (optional))");
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
