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

package com.openexchange.heapdump;

import java.io.File;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.java.Strings;

/**
 * {@link HeapDumper} - Command-line tool to obtain a heap dump.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HeapDumper extends AbstractMBeanCLI<Void> {

    /** This is the name of the HotSpot Diagnostic MBean */
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    /**
     * @param args
     */
    public static void main(String[] args) {
        new HeapDumper().execute(args);
    }

    /**
     * Initializes a new {@link HeapDumper}.
     */
    public HeapDumper() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        if (!cmd.hasOption('f')) {
            System.out.println("You must provide a file name.");
            if (null != options) {
                printHelp(options);
            }
            System.exit(-1);
            return;
        }
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.FALSE;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        // Nothing
    }

    @Override
    protected String getFooter() {
        return "The Open-Xchange heap dump tool";
    }

    @Override
    protected String getName() {
        return "heapdump";
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("f", "file", true, "The path name of the file in which to dump the heap snapshot; e.g. \"/tmp/heap.bin\"");
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, final MBeanServerConnection mbsc) throws Exception {
        // The MBean object name
        final ObjectName name = new ObjectName(HOTSPOT_BEAN_NAME);

        // The name/path of the dump file
        final String fn;
        {
            String fileName = cmd.getOptionValue('f');
            fn = Strings.isEmpty(fileName) ? "heap.bin" : fileName;

            if (new File(fn).exists()) {
                throw new Exception("File already exists: " + fn);
            }
        }

        // Invoke...
        final AtomicReference<Exception> errorRef = new AtomicReference<Exception>();
        try {
            // Trigger heap dump
            Runnable runnbable = new Runnable() {

                @Override
                public void run() {
                    try {
                        mbsc.invoke(name, "dumpHeap", new Object[] { fn, Boolean.TRUE }, new String[] { String.class.getCanonicalName(), "boolean" });
                    } catch (Exception e) {
                        errorRef.set(e);
                    }
                }
            };
            FutureTask<Void> ft = new FutureTask<Void>(runnbable, null);
            new Thread(ft, "Open-Xchange Heap Dumper").start();

            // Await termination
            System.out.print("Dumping heap snapshot");
            int c = 21;
            while (false == ft.isDone()) {
                System.out.print(".");
                if (c++ >= 76) {
                    c = 0;
                    System.out.println();
                }
                LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(500L, TimeUnit.MILLISECONDS));
            }
            System.out.println();

            // Check for error
            Exception error = errorRef.get();
            if (null != error) {
                if (error instanceof javax.management.InstanceNotFoundException) {
                    System.out.println("The \"" + getFooter() + "\" is not supported by installed Java version/vendor.");
                    return null;
                }
                throw error;
            }

            // Success...
            System.out.println("Heap snapshot successfully dumped to file " + fn);
        } catch (Exception e) {
            System.out.println("Heap snapshot could not be dumped to file " + fn + ". Reason: " + e.getMessage());
            e.printStackTrace(System.out);
            throw e;
        }

        // Return
        return null;
    }

}
